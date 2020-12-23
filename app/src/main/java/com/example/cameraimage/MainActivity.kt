package com.example.cameraimage

import android.graphics.BitmapFactory
import android.os.Bundle
import com.google.android.material.shape.CornerFamily
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : BaseUserPhotoActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupRoundImage()
        setupListeners()
    }

    private fun setupRoundImage() {
        val radius = resources.getDimension(R.dimen.imageRadius)
        image.shapeAppearanceModel = image.shapeAppearanceModel.toBuilder()
            .setTopLeftCorner(CornerFamily.ROUNDED, radius)
            .setTopRightCorner(CornerFamily.ROUNDED, radius)
            .setBottomLeftCorner(CornerFamily.ROUNDED, radius)
            .setBottomRightCorner(CornerFamily.ROUNDED, radius)
            .build()
    }

    private fun setupListeners() {
        camera.setOnClickListener {
            shootPhotoWithPermissionCheck()   //метод загрузки фото с готовыми permission
        }
        gallery.setOnClickListener {
            pickPhotoFromGalleryWithPermissionCheck()
        }
    }

    override fun showPhoto(file: File) {      // метод для отображения фото
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        image.setImageBitmap(bitmap)
    }
}