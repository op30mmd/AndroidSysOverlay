package io.github.androidsysoverlay.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import io.github.androidsysoverlay.data.SettingsRepository
import io.github.androidsysoverlay.overlay.OverlayService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val repository = SettingsRepository(context)
                    val settings = repository.settingsFlow.first()
                    if (settings.startOnBoot) {
                        val serviceIntent = Intent(context, OverlayService::class.java)
                        ContextCompat.startForegroundService(context, serviceIntent)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
