package io.github.androidsysoverlay.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import io.github.androidsysoverlay.data.OverlaySettings
import io.github.androidsysoverlay.data.SettingsRepository
import io.github.androidsysoverlay.data.ThemeMode
import io.github.androidsysoverlay.stats.StatsCollector
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
                val oldPassThrough = currentSettings.passThroughTouches
                val oldThemeMode = currentSettings.themeMode
                val oldDynamicColor = currentSettings.useDynamicColor
                val oldAccentOverride = currentSettings.accentOverride

                currentSettings = settings
                updateOverlay()

                if (oldPassThrough != settings.passThroughTouches ||
                    oldThemeMode != settings.themeMode ||
                    oldDynamicColor != settings.useDynamicColor ||
                    oldAccentOverride != settings.accentOverride) {
                    applyTheme(settings)
                }

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
                touchFlags(currentSettings.passThroughTouches),
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = currentSettings.posX
                y = currentSettings.posY
            }
            wm.addView(overlayView, params)
            applyTheme(currentSettings)
        } else {
            val params = overlayView!!.layoutParams as WindowManager.LayoutParams
            params.x = currentSettings.posX
            params.y = currentSettings.posY
            params.flags = touchFlags(currentSettings.passThroughTouches)
            wm.updateViewLayout(overlayView, params)
        }
    }

    private fun touchFlags(passThrough: Boolean): Int {
        var flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        flags = if (passThrough) {
            flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        } else {
            flags or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        }
        return flags
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyTheme(currentSettings)
    }

    private fun isSystemDark(ctx: Context): Boolean {
        val mode = ctx.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }

    private fun resolveDark(ctx: Context, mode: ThemeMode) = when (mode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.FOLLOW_SYSTEM -> isSystemDark(ctx)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun dynamicAccent(ctx: Context, dark: Boolean): Int {
        val res = if (dark) android.R.color.system_accent1_200
        else android.R.color.system_accent1_600
        return ContextCompat.getColor(ctx, res)
    }

    private fun resolveAccent(ctx: Context, s: OverlaySettings, dark: Boolean): Int = when {
        s.accentOverride != null -> s.accentOverride.toInt()
        s.useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicAccent(ctx, dark)
        else -> if (dark) Color.parseColor("#90CAF9") else Color.parseColor("#1565C0")
    }

    private fun applyTheme(s: OverlaySettings) {
        val dark = resolveDark(this, s.themeMode)
        val accent = resolveAccent(this, s, dark)
        val text = if (dark) Color.WHITE else Color.BLACK
        val scrim = if (dark) 0xCC000000.toInt() else 0xCCFFFFFF.toInt()

        // Update settings with resolved colors for OverlayView.render
        currentSettings = s.copy(
            textColor = text.toLong() and 0xFFFFFFFFL,
            backgroundColor = scrim.toLong() and 0xFFFFFFFFL
            // accent could be used if we had accent-dependent UI elements in OverlayView
        )
        // We don't really have a "scrim" separate from "backgroundColor" in OverlaySettings yet,
        // but the blueprint suggests we might want to override them.
        // Actually the blueprint says: overlayView.applyColors(text = text, accent = accent, scrim = scrim)
        // Let's just update currentSettings so the next render() uses them.
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
