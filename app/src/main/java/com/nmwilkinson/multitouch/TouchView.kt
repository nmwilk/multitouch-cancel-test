package com.nmwilkinson.multitouch

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import logcat.logcat

class TouchView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val strokes = mutableMapOf<Int, MutableList<PointF>>()
    private val pointersActive = mutableMapOf<Int, Boolean>()

    private val colors = listOf(
        0xFFd895ba.toInt(),
        0xFFfc0d32.toInt(),
        0xFF7cd28c.toInt(),
        0xFF113028.toInt(),
        0xFFbb5075.toInt(),
        0xFF4d6c6b.toInt()
    )
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val newPoints = mutableListOf<PointF>()
                val pointerId = event.getPointerId(0)
                pointersActive[pointerId] = true
                strokes[pointerId] = newPoints
                logcat { "DOWN pointerId $pointerId" }
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                val newPoints = mutableListOf<PointF>()
                val pointerId = event.getPointerId(event.actionIndex)
                logcat { "POINTER_DOWN actionIndex ${event.actionIndex}  pointerId $pointerId" }
                pointersActive[pointerId] = true
                strokes[pointerId] = newPoints
            }

            MotionEvent.ACTION_MOVE -> {
                for (actionIndex in 0 until event.pointerCount) {
                    val points = strokes[event.getPointerId(actionIndex)]!!
                    points.add(PointF(event.getX(actionIndex), event.getY(actionIndex)))
                }
            }

            MotionEvent.ACTION_UP -> {
                val isCancel =
                    event.flags and MotionEvent.FLAG_CANCELED == MotionEvent.FLAG_CANCELED
                val pointerId = event.getPointerId(event.actionIndex)
                logcat { "UP pointerId $pointerId  isCancel $isCancel" }
                val points = strokes[pointerId]!!
                pointersActive[pointerId] = false
                if (isCancel) {
                    strokes.remove(pointerId)
                } else {
                    points.add(PointF(event.x, event.y))
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val isCancel =
                    event.flags and MotionEvent.FLAG_CANCELED == MotionEvent.FLAG_CANCELED
                val pointerId = event.getPointerId(event.actionIndex)
                logcat { "POINTER_UP actionIndex ${event.actionIndex}  pointerId $pointerId  isCancel $isCancel" }
                val points = strokes[pointerId]!!
                pointersActive[pointerId] = false
                points.add(PointF(event.getX(event.actionIndex), event.getY(event.actionIndex)))
            }

            MotionEvent.ACTION_CANCEL -> {
                val pointerId = event.getPointerId(event.actionIndex)
                logcat { "CANCEL actionIndex ${event.actionIndex}  pointerId $pointerId  pointerCount ${event.pointerCount}" }
                pointersActive[pointerId] = false
                strokes.remove(pointerId)
            }
        }
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        strokes.entries.forEachIndexed { index, entry ->
            drawStroke(canvas, colors[entry.key % colors.size], pointersActive[index] ?: false, entry.value)
        }
    }

    private fun drawStroke(
        canvas: Canvas,
        color: Int,
        active: Boolean,
        points: MutableList<PointF>
    ) {
        paint.color = color
        paint.strokeWidth = if (active) 30f else 15f
        for (i: Int in 0 until points.size - 1) {
            canvas.drawLine(points[i].x, points[i].y, points[i + 1].x, points[i + 1].y, paint)
        }
    }
}