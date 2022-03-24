package com.gdsc.medimedi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View

// 뷰를 상속 받아 커스텀 뷰를 만들 때는
// 해당 뷰가 언제, 어떻게 생성되는지 정의하기 위한 생성자가 필요함.
// https://medium.com/@futureofdev/%EC%BD%94%ED%8B%80%EB%A6%B0-kotlin-customview-774e236ca034

@SuppressLint("ViewConstructor")
class GraphicOverlay(context: Context, var rect: Rect, var text: String) : View(context) {
    private lateinit var paint: Paint
    private lateinit var textPaint: Paint

    init {
        initPaint()
    }

    private fun initPaint() {
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
        // todo: 객체 모양에 맞춰서 상자 더 정확하게 그려야 함.
        //canvas.drawRect(rect.left.toFloat(), rect.top.toFloat(), rect.right.toFloat(), rect.bottom.toFloat(), paint)

        // 직사각형의 중앙에 텍스트 그리기
        canvas.drawText(text, rect.centerX().toFloat(), rect.centerY().toFloat(), textPaint)
    }
}