package com.example.rootoverlay.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.rootoverlay.data.OverlaySettings
import com.example.rootoverlay.data.SettingsRepository
import com.example.rootoverlay.stats.StatsCollector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OverlayService : LifecycleService() {

    private lateinit var wm: WindowManager
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var collector: StatsCollector
    private var overlayView: OverlayView? = null
    private var currentSettings: OverlaySettings = OverlaySettings()

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        settingsRepository = SettingsRepository(this)
        collector = StatsCollector(this)

        startForeground(NOTIFICATION_ID, createNotification())

        lifecycleScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                val oldRefresh = currentSettings.refreshMs
                currentSettings = settings
                updateOverlay()
                if (oldRefresh != settings.refreshMs) {
                    // Ticker will pick up new delay on next iteration
                }
            }
        }

        lifecycleScope.launch {
            while (isActive) {
                val snap = withContext(Dispatchers.IO) { collector.sample() }
                withContext(Dispatchers.Main) {
                    overlayView?.render(snap, currentSettings)
                }
                delay(currentSettings.refreshMs)
            }
        }
    }

    private fun updateOverlay() {
        if (overlayView == null) {
            overlayView = OverlayView(this) { x, y ->
                lifecycleScope.launch { settingsRepository.setPosition(x, y) }
            }
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = currentSettings.posX
                y = currentSettings.posY
            }
            wm.addView(overlayView, params)
        } else {
            val params = overlayView!!.layoutParams as WindowManager.LayoutParams
            params.x = currentSettings.posX
            params.y = currentSettings.posY
            wm.updateViewLayout(overlayView, params)
        }
    }

    private fun createNotification(): Notification {
        val channelId = "overlay_service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Root Overlay Running")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let { wm.removeView(it) }
    }

    companion object {
        private const val NOTIFICATION_ID = 1
    }
}
