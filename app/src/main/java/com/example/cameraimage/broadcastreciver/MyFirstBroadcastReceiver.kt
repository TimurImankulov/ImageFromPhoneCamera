package com.example.cameraimage.broadcastreciver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MyFirstBroadcastReceiver : BroadcastReceiver( ) {

    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.d("_____adasasad", "adsadasdasd")
    }
}