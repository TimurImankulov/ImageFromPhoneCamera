package com.example.cameraimage

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

@RuntimePermissions                           // для работы с permissionsdispatcher нужно пометить анатацией
abstract class BaseUserPhotoActivity : AppCompatActivity() {

    abstract fun showPhoto(file: File)        // метод для отображения фото в MainActivity

    @NeedsPermission(Manifest.permission.CAMERA) //? метод для полуения фото, анатация говорит что метод сам будет вызывать permission для камеры
    fun shootPhoto() {             // если permission нет вызывается метод onRequestPermissionsResult()
        filename = System.nanoTime().toString()
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE) // запускаем Intent для открытия камеры
        val uri = getCaptureImageOutputUri(this, filename!!)

        if (uri != null) {
            val file = File(uri.path)
            if (Build.VERSION.SDK_INT >= 24) {
                intent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    FileProvider.getUriForFile(this, "${this.packageName}.provider", file)
                )
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)

            startActivityForResult(intent, RESULT_CAMERA)        // чтобы получить фото и вернуться обратно
        }
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE) // permission на доступ к фаловому хранилищу
    fun pickPhotoFromGallery() {
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )

        intent.type = "image/*"
        startActivityForResult(intent, RESULT_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { // ответ для startActivityForResult, ловим картинку
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {                                       // если пользователь сфотал картинку
            when (requestCode) {
                RESULT_CAMERA -> {                                           // приходящая картинка с камеры
                    val uri = getImageFromCameraUri(data, filename!!)              // ссылка на фото
                    val uriFile = getNormalizedUri(uri = uri)                     // окончательная ссылка на фото
                    val file = File(uriFile?.path)
                    file?.let { showPhoto(it)}                                          // передаем ссылку на фото
                    Log.d("sdvsdv", "adfbdfb")
                }
                RESULT_GALLERY -> {                                        // приходящая картинка с галерии
                    if (data != null && data.data != null) {
                        val fileName = getImagePathFromInputStreamUri(this, data.data!!)
                        val file = File(fileName)
                        showPhoto(file)
                    }
                }
            }
        }
    }

    private fun getImageFromCameraUri(data: Intent?, filename: String): Uri? {   // метод для получения ссылки на фото
        var isCamera = true
        if (data != null && data.data != null) {
            val action = data.action
            isCamera = action != null && action == MediaStore.ACTION_IMAGE_CAPTURE
        }

        return if (isCamera || data!!.data == null)                            // если мы не получили фото запускается getCaptureImageOutputUri
            getCaptureImageOutputUri(applicationContext, filename)
        else
            data.data
    }

    private fun getCaptureImageOutputUri(context: Context, fileName: String): Uri? { // этот метод ищет фото, и если находит возвращает фото
        var outputFileUri: Uri? = null
        val getImage = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (getImage != null) {
            outputFileUri = Uri.fromFile(File(getImage.path, "$fileName.jpeg"))
        }
        return outputFileUri
    }

    private fun getNormalizedUri(uri: Uri?): Uri? {                                           // если ссылка некорректная и содержит content: запускается метод getPath
        return if (uri != null && uri.toString().contains("content:"))
            Uri.fromFile(getPath(applicationContext, uri, MediaStore.Images.Media.DATA))
        else
            uri
    }

    private fun getPath(context: Context, uri: Uri, column: String): File? {                  // этот метод использует cursor для поиска ссылки на фото в БД
        val columns = arrayOf(column)
        val cursor = context.contentResolver.query(uri, columns, null, null, null) ?: return null
        val columnIndex = cursor.getColumnIndexOrThrow(column)
        cursor.moveToFirst()
        val path = cursor.getString(columnIndex)
        cursor.close()
        return File(path)
    }

    private fun getImagePathFromInputStreamUri(context: Context, uri: Uri): String? { // метод нормализующий ссылку на фото из файлового хранилища для разных телефонов
        var inputStream: InputStream? = null
        var filePath: String? = null

        if (uri.authority != null) {
            try {
                inputStream = context.contentResolver.openInputStream(uri)
                val photoFile = createTemporalFileFrom(context, inputStream)
                filePath = photoFile!!.path
            } finally {
                try {
                    inputStream!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return filePath
    }

    @Throws(IOException::class)
    private fun createTemporalFileFrom(context: Context, inputStream: InputStream?): File? {
        var targetFile: File? = null

        if (inputStream != null) {
            var read: Int
            val buffer = ByteArray(8 * 1024)

            targetFile = createTemporalFile(context)
            val outputStream = FileOutputStream(targetFile)

            while (true) {
                read = inputStream.read(buffer)
                if (read == -1)
                    break
                outputStream.write(buffer, 0, read)
            }
            outputStream.flush()

            try {
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return targetFile
    }


    private fun createTemporalFile(
        context: Context,
        filePath: String = Calendar.getInstance().timeInMillis.toString()
    ): File {
        return File(context.externalCacheDir, "$filePath.jpg") // context needed
    }

    override fun onRequestPermissionsResult(   // проверяем если пользователь дает permission вызывается shootPhoto()
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    companion object {             // для ActivityForResult
        private const val RESULT_CAMERA = 1
        private const val RESULT_GALLERY = 2
        private var filename: String? = null
    }
}