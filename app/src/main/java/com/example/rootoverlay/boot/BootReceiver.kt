package com.example.rootoverlay.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.rootoverlay.overlay.OverlayService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // In a real app we'd check if it's enabled in settings
            val serviceIntent = Intent(context, OverlayService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}
