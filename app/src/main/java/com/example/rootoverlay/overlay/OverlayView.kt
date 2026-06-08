package com.example.rootoverlay.overlay

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.example.rootoverlay.data.Metric
import com.example.rootoverlay.data.OverlaySettings
import com.example.rootoverlay.stats.StatsSnapshot
import kotlin.math.roundToInt

class OverlayView(context: Context, private val onPositionChanged: (Int, Int) -> Unit) : LinearLayout(context) {

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    private val textViews = mutableMapOf<Metric, TextView>()

    init {
        orientation = VERTICAL
        setPadding(16, 16, 16, 16)
    }

    fun render(snap: StatsSnapshot, settings: OverlaySettings) {
        setBackgroundColor(settings.backgroundColor.toInt())
        alpha = settings.opacity

        val metricsToDisplay = settings.enabledMetrics

        // Remove views that are no longer enabled
        val toRemove = textViews.keys.filter { it !in metricsToDisplay }
        toRemove.forEach {
            removeView(textViews[it])
            textViews.remove(it)
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
                    TextView(context).also { addView(it) }
                }

                // Ensure correct order in layout
                if (indexOfChild(tv) != index) {
                    removeView(tv)
                    addView(tv, index)
                }

                tv.text = text
                tv.setTextColor(settings.textColor.toInt())
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, settings.fontSizeSp)
            } else {
                textViews[metric]?.let {
                    removeView(it)
                    textViews.remove(metric)
                }
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
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
