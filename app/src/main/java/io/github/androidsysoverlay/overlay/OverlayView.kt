package io.github.androidsysoverlay.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.core.graphics.ColorUtils
import io.github.androidsysoverlay.data.FontWeightOption
import io.github.androidsysoverlay.data.Metric
import io.github.androidsysoverlay.data.OverlaySettings
import io.github.androidsysoverlay.stats.StatsSnapshot
import kotlin.math.roundToInt

class OverlayView(context: Context, private val onPositionChanged: (Int, Int) -> Unit) : LinearLayout(context) {

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var lastSettings: OverlaySettings? = null

    private val textViews = mutableMapOf<Metric, OutlinedTextView>()

    init {
        orientation = VERTICAL
        setPadding(16, 16, 16, 16)
    }

    private fun dp(dp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()
    }

    fun render(snap: StatsSnapshot, settings: OverlaySettings) {
        lastSettings = settings

        if (settings.backgroundScrim) {
            val bg = GradientDrawable().apply {
                cornerRadius = dp(8f).toFloat()
                setColor(settings.backgroundColor.toInt())
            }
            background = bg
        } else {
            background = null
        }
        alpha = settings.opacity

        val metricsToDisplay = settings.enabledMetrics

        // Remove views that are no longer enabled
        val toRemove = textViews.keys.filter { it !in metricsToDisplay }
        toRemove.forEach {
            removeView(textViews[it])
            textViews.remove(it)
        }

        val resolvedTextColor = if (settings.minContrastAuto && settings.backgroundScrim) {
            if (ColorUtils.calculateLuminance(settings.backgroundColor.toInt()) < 0.5) Color.WHITE else Color.BLACK
        } else {
            settings.textColor.toInt()
        }

        val typeface = when (settings.fontWeight) {
            FontWeightOption.LIGHT -> Typeface.create("sans-serif-light", Typeface.NORMAL)
            FontWeightOption.NORMAL -> Typeface.DEFAULT
            FontWeightOption.MEDIUM -> Typeface.create("sans-serif-medium", Typeface.NORMAL)
            FontWeightOption.BOLD -> Typeface.DEFAULT_BOLD
        }

        // Update or create views in correct order
        settings.metricOrder.filter { it in metricsToDisplay }.forEachIndexed { index, metric ->
            val text = when (metric) {
                Metric.CPU -> "CPU: ${snap.cpuPercent.roundToInt()}%"
                Metric.RAM -> "RAM: ${snap.ramUsedMb}/${snap.ramTotalMb}MB"
                Metric.GPU -> if (snap.gpuPercent != null) "GPU: ${snap.gpuPercent.roundToInt()}%" else null
                Metric.CPU_TEMP -> if (snap.cpuTempC != null) "CPU Temp: ${snap.cpuTempC}°C" else null
                Metric.BATTERY_TEMP -> if (snap.batteryTempC != null) "Bat Temp: ${snap.batteryTempC}°C" else null
                Metric.DISCHARGE -> "Bat: ${snap.dischargeMa.roundToInt()}mA"
                Metric.SWAP -> "SWAP: ${snap.swapUsedMb}/${snap.swapTotalMb}MB"
            }

            if (text != null) {
                val tv = textViews.getOrPut(metric) {
                    OutlinedTextView(context).also { addView(it) }
                }

                // Ensure correct order in layout
                if (indexOfChild(tv) != index) {
                    removeView(tv)
                    addView(tv, index)
                }

                tv.text = text
                tv.setTextColor(resolvedTextColor)
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, settings.fontSizeSp)
                tv.typeface = typeface

                if (settings.textOutline) {
                    tv.outlineColor = settings.outlineColor.toInt()
                    tv.outlineWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, settings.outlineWidthDp, resources.displayMetrics)
                } else {
                    tv.outlineWidthPx = 0f
                }

                if (settings.textShadow) {
                    tv.setShadowLayer(3f, 0f, 1f, Color.BLACK)
                } else {
                    tv.setShadowLayer(0f, 0f, 0f, 0)
                }
            } else {
                textViews[metric]?.let {
                    removeView(it)
                    textViews.remove(metric)
                }
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (lastSettings?.lockPosition == true) return false

        val params = layoutParams as WindowManager.LayoutParams

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = params.x
                initialY = params.y
                initialTouchX = ev.rawX
                initialTouchY = ev.rawY
                return false
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = (ev.rawX - initialTouchX).toInt()
                val dy = (ev.rawY - initialTouchY).toInt()
                if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                    return true
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (lastSettings?.lockPosition == true) return false

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val params = layoutParams as WindowManager.LayoutParams

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                params.x = initialX + (event.rawX - initialTouchX).toInt()
                params.y = initialY + (event.rawY - initialTouchY).toInt()
                wm.updateViewLayout(this, params)
                return true
            }
            MotionEvent.ACTION_UP -> {
                onPositionChanged(params.x, params.y)
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
