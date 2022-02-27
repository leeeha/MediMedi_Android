package com.gdsc.medimedi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import com.gdsc.medimedi.fragment.SearchFragment
@SuppressLint("ViewConstructor")
class GraphicOverlay(context: Context?, var rect: Rect, var text: String): View(context) {
    lateinit var paint: Paint
    lateinit var textPaint: Paint

    init {
        init()
    }

    private fun init() {
        paint = Paint()
        paint.color = Color.RED
        paint.strokeWidth = 8f
        paint.style = Paint.Style.STROKE

        textPaint = Paint()
        textPaint.color = Color.RED
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 80f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 꼭짓점의 좌표값 얻어서 직사각형 그리기
        canvas.drawRect(rect.left.toFloat(), rect.top.toFloat(),
            rect.right.toFloat(), rect.bottom.toFloat(), paint)

        // 직사각형의 중앙에 텍스트 그리기
        canvas.drawText(text, rect.centerX().toFloat(), rect.centerY().toFloat(), textPaint)
    }
}