package com.gdsc.medimedi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View

// 커스텀 뷰를 만들 때는 부모 클래스에게 context를 전달해줘야 한다.
// 강제로 @SuppressLint("ViewConstructor") 붙였더니 네비게이션 에러가 잠시 사라짐...
@SuppressLint("ViewConstructor")
class GraphicOverlay(context: Context?, var rect: Rect, var text: String): View(context) {
    private lateinit var paint: Paint
    private lateinit var textPaint: Paint

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

        // 꼭짓점의 좌표값 얻어서 직사각형 그리기 (상자 모양이 부정확해서 일단 생략)
        //canvas.drawRect(rect.left.toFloat(), rect.top.toFloat(), rect.right.toFloat(), rect.bottom.toFloat(), paint)

        // 직사각형의 중앙에 텍스트 그리기
        canvas.drawText(text, rect.centerX().toFloat(), rect.centerY().toFloat(), textPaint)
    }
}