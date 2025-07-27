package com.darsuddeen.academy.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var currentPath = Path()
    private val paths = mutableListOf<Pair<Path, Paint>>()

    private var drawColor = Color.RED
    private var strokeWidth = 6f

    private val paint = Paint().apply {
        color = drawColor
        isAntiAlias = true
        strokeWidth = this@DrawingView.strokeWidth
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    var isDrawingEnabled: Boolean = false

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for ((path, p) in paths) {
            canvas.drawPath(path, p)
        }
        canvas.drawPath(currentPath, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isDrawingEnabled) return false

        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPath.moveTo(x, y)
            }
            MotionEvent.ACTION_MOVE -> {
                currentPath.lineTo(x, y)
            }
            MotionEvent.ACTION_UP -> {
                val newPaint = Paint(paint)
                paths.add(Pair(Path(currentPath), newPaint))
                currentPath.reset()
            }
        }

        invalidate()
        return true
    }

    fun setColor(color: Int) {
        drawColor = color
        paint.color = drawColor
    }

    fun setStrokeWidth(width: Float) {
        strokeWidth = width
        paint.strokeWidth = width
    }

    fun clearDrawing() {
        paths.clear()
        currentPath.reset()
        invalidate()
        }
}