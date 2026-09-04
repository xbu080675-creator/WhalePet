package com.whalepet.android

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View

class WhaleView(context: Context) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    private var sleeping = false
    private var dragging = false
    private var bubbleText: String? = "本小姐登场。"

    private val hideBubbleRunnable = Runnable {
        if (!sleeping && !dragging) {
            bubbleText = null
            invalidate()
        }
    }

    private val tapLines = listOf(
        "谁是大肥鱼！",
        "本小姐只是鲸。",
        "今天也要吃 token 🍚",
        "哼。",
        "别戳啦！",
        "DeepSeek 才没有偷懒。"
    )

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        isClickable = true
        postDelayed(hideBubbleRunnable, 2600L)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        drawBubble(canvas, w, h)
        drawWhale(canvas, w, h)
    }

    private fun drawBubble(canvas: Canvas, w: Float, h: Float) {
        val text = when {
            dragging -> "哎哎哎！别拎我！"
            sleeping -> "DeepSleep..."
            else -> bubbleText
        } ?: return

        val bubble = RectF(
            w * 0.08f,
            h * 0.03f,
            w * 0.92f,
            h * 0.31f
        )

        paint.style = Paint.Style.FILL
        paint.color = Color.argb(238, 245, 249, 255)
        paint.setShadowLayer(w * 0.025f, 0f, w * 0.012f, Color.argb(75, 0, 0, 0))
        canvas.drawRoundRect(bubble, w * 0.055f, w * 0.055f, paint)
        paint.clearShadowLayer()

        path.reset()
        path.moveTo(w * 0.68f, h * 0.29f)
        path.lineTo(w * 0.60f, h * 0.38f)
        path.lineTo(w * 0.77f, h * 0.30f)
        path.close()
        paint.color = Color.rgb(245, 249, 255)
        canvas.drawPath(path, paint)

        paint.color = Color.rgb(28, 67, 120)
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = w * 0.082f
        paint.isFakeBoldText = true
        canvas.drawText(text, w * 0.5f, h * 0.205f, paint)
        paint.isFakeBoldText = false
    }

    private fun drawWhale(canvas: Canvas, w: Float, h: Float) {
        val body = RectF(
            w * 0.22f,
            h * 0.39f,
            w * 0.82f,
            h * 0.86f
        )

        paint.style = Paint.Style.FILL
        paint.color = if (sleeping) {
            Color.rgb(84, 114, 156)
        } else {
            Color.rgb(70, 136, 224)
        }
        paint.setShadowLayer(w * 0.035f, 0f, w * 0.02f, Color.argb(85, 0, 0, 0))
        canvas.drawOval(body, paint)
        paint.clearShadowLayer()

        // Tail.
        path.reset()
        path.moveTo(w * 0.76f, h * 0.55f)
        path.cubicTo(w * 0.90f, h * 0.47f, w * 0.94f, h * 0.42f, w * 0.96f, h * 0.35f)
        path.cubicTo(w * 0.89f, h * 0.35f, w * 0.82f, h * 0.39f, w * 0.78f, h * 0.46f)
        path.cubicTo(w * 0.80f, h * 0.37f, w * 0.78f, h * 0.31f, w * 0.73f, h * 0.27f)
        path.cubicTo(w * 0.67f, h * 0.34f, w * 0.68f, h * 0.44f, w * 0.71f, h * 0.52f)
        path.close()
        canvas.drawPath(path, paint)

        // Belly.
        paint.color = Color.rgb(205, 231, 255)
        val belly = RectF(
            w * 0.30f,
            h * 0.58f,
            w * 0.68f,
            h * 0.85f
        )
        canvas.drawOval(belly, paint)

        // Side fin.
        paint.color = if (sleeping) Color.rgb(73, 103, 145) else Color.rgb(52, 116, 207)
        path.reset()
        path.moveTo(w * 0.31f, h * 0.63f)
        path.quadTo(w * 0.13f, h * 0.67f, w * 0.16f, h * 0.79f)
        path.quadTo(w * 0.28f, h * 0.76f, w * 0.36f, h * 0.68f)
        path.close()
        canvas.drawPath(path, paint)

        // Tiny water spout / ahoge.
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = w * 0.022f
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = Color.rgb(62, 126, 213)
        path.reset()
        path.moveTo(w * 0.47f, h * 0.42f)
        path.cubicTo(w * 0.42f, h * 0.34f, w * 0.43f, h * 0.29f, w * 0.49f, h * 0.27f)
        path.moveTo(w * 0.50f, h * 0.42f)
        path.cubicTo(w * 0.54f, h * 0.34f, w * 0.58f, h * 0.31f, w * 0.61f, h * 0.33f)
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.FILL

        // Eye / sleeping eye.
        if (sleeping) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = w * 0.017f
            paint.color = Color.rgb(19, 48, 83)
            val eyeArc = RectF(w * 0.38f, h * 0.53f, w * 0.49f, h * 0.61f)
            canvas.drawArc(eyeArc, 10f, 160f, false, paint)
            paint.style = Paint.Style.FILL

            paint.textAlign = Paint.Align.LEFT
            paint.textSize = w * 0.10f
            paint.color = Color.rgb(48, 88, 143)
            canvas.drawText("Z", w * 0.70f, h * 0.48f, paint)
            paint.textSize = w * 0.075f
            canvas.drawText("z", w * 0.78f, h * 0.42f, paint)
        } else {
            paint.color = Color.WHITE
            canvas.drawCircle(w * 0.43f, h * 0.57f, w * 0.055f, paint)
            paint.color = Color.rgb(17, 53, 96)
            canvas.drawCircle(w * 0.445f, h * 0.575f, w * 0.027f, paint)
            paint.color = Color.WHITE
            canvas.drawCircle(w * 0.455f, h * 0.562f, w * 0.009f, paint)
        }

        // Mouth.
        paint.color = Color.rgb(24, 67, 112)
        paint.strokeWidth = w * 0.014f
        paint.style = Paint.Style.STROKE
        val mouth = RectF(w * 0.40f, h * 0.62f, w * 0.55f, h * 0.71f)
        canvas.drawArc(mouth, 15f, 145f, false, paint)
        paint.style = Paint.Style.FILL

        // White whale mark.
        paint.color = Color.WHITE
        canvas.drawCircle(w * 0.64f, h * 0.50f, w * 0.025f, paint)
        canvas.drawOval(RectF(w * 0.65f, h * 0.48f, w * 0.72f, h * 0.52f), paint)
    }

    override fun performClick(): Boolean {
        super.performClick()
        onTapped()
        return true
    }

    fun onTapped() {
        wakeUp()
        removeCallbacks(hideBubbleRunnable)
        bubbleText = tapLines.random()
        invalidate()
        postDelayed(hideBubbleRunnable, 2400L)
    }

    fun setSleeping(value: Boolean) {
        sleeping = value
        if (value) {
            removeCallbacks(hideBubbleRunnable)
            bubbleText = null
        }
        invalidate()
    }

    fun wakeUp() {
        if (sleeping) {
            sleeping = false
            bubbleText = "谁、谁睡了！"
            removeCallbacks(hideBubbleRunnable)
            postDelayed(hideBubbleRunnable, 2200L)
            invalidate()
        }
    }

    fun setDragging(value: Boolean) {
        dragging = value
        if (value) {
            wakeUp()
            removeCallbacks(hideBubbleRunnable)
        } else if (!sleeping) {
            bubbleText = "放这儿就行。"
            removeCallbacks(hideBubbleRunnable)
            postDelayed(hideBubbleRunnable, 1800L)
        }
        invalidate()
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(hideBubbleRunnable)
        super.onDetachedFromWindow()
    }
}
