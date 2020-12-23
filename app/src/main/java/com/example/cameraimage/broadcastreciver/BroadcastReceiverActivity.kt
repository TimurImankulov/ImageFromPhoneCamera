package com.example.cameraimage.broadcastreciver

import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.cameraimage.R
import kotlinx.android.synthetic.main.activity_broadcast_receiver.*

class BroadcastReceiverActivity : AppCompatActivity() {

    private val receiver = TimeBroadcastReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_broadcast_receiver)
        setupListeners()
        registerReceiver(receiver, IntentFilter("android.intent.action.TIME_TICK"))
    }

    private fun setupListeners() {
        btnSendBroadcast.setOnClickListener {
            sendBroadcastMessage()
        }
    }

    private fun sendBroadcastMessage() {
      //  val intent = Intent(applicationContext, MyFirstBroadcastReceiver::class.java)
        val intent = Intent("com.example.cameraimage.TEST")

        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
       unregisterReceiver(receiver)
    }
}

