package io.github.androidsysoverlay.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.widget.TextView

class OutlinedTextView(context: Context) : TextView(context) {
    var outlineColor: Int = android.graphics.Color.BLACK
    var outlineWidthPx: Float = 0f

    override fun onDraw(canvas: Canvas) {
        if (outlineWidthPx > 0f) {
            val keep = currentTextColor
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = outlineWidthPx
            setTextColor(outlineColor)
            super.onDraw(canvas)          // pass 1: outline
            paint.style = Paint.Style.FILL
            setTextColor(keep)
        }
        super.onDraw(canvas)              // pass 2: fill
    }
}
