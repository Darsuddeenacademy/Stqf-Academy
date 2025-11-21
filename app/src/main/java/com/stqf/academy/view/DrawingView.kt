package com.stqf.academy.view

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewParent

class DrawingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    // ---- External event sink (Activity-controlled) ----
    interface EventSink {
        fun onPenDown(x: Float, y: Float) {}
        fun onPenMove(x: Float, y: Float) {}
        fun onPenUp() {}
    }
    private var eventSink: EventSink? = null
    private var passthroughToSink: Boolean = false

    fun setEventSink(sink: EventSink?) { eventSink = sink }
    /** true হলে টাচ Activity-তে ফরওয়ার্ড হবে (লোকালে আঁকা হবে না) */
    fun setPassthroughToSink(enabled: Boolean) { passthroughToSink = enabled }

    // ---- Local drawing state ----
    private var currentPath = Path()
    private val paths = mutableListOf<Pair<Path, Paint>>()

    private var drawColor = Color.RED
    private var strokeWidth = 6f
    private var isHighlighterMode = false

    // 🔹 হাইলাইটার আলফা + MULTIPLY blend
    private val highlighterAlpha = 145

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = drawColor
        strokeWidth = this@DrawingView.strokeWidth
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    /** Activity থেকে অন/অফ করবেন */
    var isDrawingEnabled: Boolean = false

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for ((path, p) in paths) canvas.drawPath(path, p)
        canvas.drawPath(currentPath, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isDrawingEnabled) return false

        val x = event.x
        val y = event.y

        if (event.pointerCount > 1) return false

        // Passthrough মোড
        if (passthroughToSink && eventSink != null) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    (parent as? ViewParent)?.requestDisallowInterceptTouchEvent(true)
                    eventSink?.onPenDown(x, y)
                }
                MotionEvent.ACTION_MOVE -> {
                    for (i in 0 until event.historySize) {
                        eventSink?.onPenMove(event.getHistoricalX(i), event.getHistoricalY(i))
                    }
                    eventSink?.onPenMove(x, y)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    (parent as? ViewParent)?.requestDisallowInterceptTouchEvent(false)
                    eventSink?.onPenUp()
                }
            }
            return true
        }

        // লোকাল ড্রয়িং
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                (parent as? ViewParent)?.requestDisallowInterceptTouchEvent(true)
                currentPath.moveTo(x, y)
                performClick()
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.historySize) {
                    currentPath.lineTo(event.getHistoricalX(i), event.getHistoricalY(i))
                }
                currentPath.lineTo(x, y)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val newPaint = Paint(paint)
                paths.add(Path(currentPath) to newPaint)
                currentPath.reset()
                (parent as? ViewParent)?.requestDisallowInterceptTouchEvent(false)
            }
        }
        invalidate()
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    // ---- Public setters ----
    fun setColor(color: Int) {
        drawColor = color
        paint.color = drawColor
        paint.alpha = if (isHighlighterMode) highlighterAlpha else 255
        applyHighlighterBlend()
        invalidate()
    }

    fun setStrokeWidth(width: Float) {
        strokeWidth = width
        paint.strokeWidth = width
        invalidate()
    }

    fun enableHighlighterMode(enable: Boolean) {
        isHighlighterMode = enable
        paint.alpha = if (enable) highlighterAlpha else 255
        applyHighlighterBlend()
        invalidate()
    }

    private fun applyHighlighterBlend() {
        if (isHighlighterMode) {
            @Suppress("DEPRECATION")
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
            if (Build.VERSION.SDK_INT >= 29) {
                paint.xfermode = null
                paint.blendMode = BlendMode.MULTIPLY
            }
        } else {
            paint.xfermode = null
            if (Build.VERSION.SDK_INT >= 29) {
                paint.blendMode = null
            }
        }
    }

    fun clearDrawing() {
        paths.clear()
        currentPath.reset()
        invalidate()
    }
}
