package com.whalepet.android

import android.content.Context
import android.graphics.*
import android.view.View

class WhaleView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val skinRenderer = SkinRenderer(context)
    private var skinBitmap: Bitmap? = null

    private var sleeping = false
    private var dragging = false
    private var eating = false
    private var bubbleText: String? = "本小姐登场。"

    private val hideBubbleRunnable = Runnable {
        if (!sleeping && !dragging && !eating) {
            bubbleText = null
            invalidate()
        }
    }

    private val stopEatingRunnable = Runnable {
        eating = false
        bubbleText = null
        loadSkin("idle")
        invalidate()
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
        isClickable = true
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        loadSkin("idle")
        postDelayed(hideBubbleRunnable, 2600L)
    }

    private fun loadSkin(state: String) {
        skinBitmap = skinRenderer.loadSkinBitmap("deepseek_whale", state)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawPet(canvas)
        drawBubble(canvas)
    }

    private fun drawPet(canvas: Canvas) {
        val bitmap = skinBitmap
        if (bitmap != null) {
            // Reserve the upper part of the overlay for the speech bubble.
            val box = RectF(
                width * 0.04f,
                height * 0.20f,
                width * 0.96f,
                height * 0.99f
            )
            val scale = minOf(box.width() / bitmap.width, box.height() / bitmap.height)
            val drawWidth = bitmap.width * scale
            val drawHeight = bitmap.height * scale
            val left = box.centerX() - drawWidth / 2f
            val top = box.bottom - drawHeight

            canvas.drawBitmap(
                bitmap,
                null,
                RectF(left, top, left + drawWidth, top + drawHeight),
                paint
            )
        } else {
            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(70, 136, 224)
            canvas.drawOval(
                RectF(width * 0.2f, height * 0.35f, width * 0.8f, height * 0.85f),
                paint
            )
        }
    }

    private fun drawBubble(canvas: Canvas) {
        val text = when {
            dragging -> "哎哎哎！别拎我！"
            sleeping -> "DeepSleep..."
            eating -> "白饭！🍚"
            else -> bubbleText
        } ?: return

        val w = width.toFloat()
        val h = height.toFloat()
        val bubble = RectF(w * 0.05f, h * 0.025f, w * 0.95f, h * 0.30f)

        paint.style = Paint.Style.FILL
        paint.color = Color.argb(245, 248, 251, 255)
        paint.setShadowLayer(w * 0.025f, 0f, w * 0.012f, Color.argb(75, 0, 0, 0))
        canvas.drawRoundRect(bubble, w * 0.055f, w * 0.055f, paint)
        paint.clearShadowLayer()

        path.reset()
        path.moveTo(w * 0.67f, h * 0.285f)
        path.lineTo(w * 0.60f, h * 0.36f)
        path.lineTo(w * 0.77f, h * 0.295f)
        path.close()
        paint.color = Color.rgb(248, 251, 255)
        canvas.drawPath(path, paint)

        paint.color = Color.rgb(28, 67, 120)
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = w * 0.070f
        paint.isFakeBoldText = true

        // Keep short desktop-pet lines centered and safely inside the bubble.
        canvas.drawText(text, w * 0.50f, h * 0.19f, paint)
        paint.isFakeBoldText = false
    }

    private fun showBubble(text: String, durationMs: Long = 2200L) {
        removeCallbacks(hideBubbleRunnable)
        bubbleText = text
        invalidate()
        if (!sleeping && !dragging && !eating) {
            postDelayed(hideBubbleRunnable, durationMs)
        }
    }

    fun onTapped() {
        sleeping = false
        eating = false
        removeCallbacks(stopEatingRunnable)
        loadSkin("happy")
        showBubble(tapLines.random(), 2400L)

        postDelayed({
            if (!sleeping && !dragging && !eating) {
                loadSkin("idle")
                invalidate()
            }
        }, 900L)
    }

    fun feedRice() {
        sleeping = false
        eating = true
        removeCallbacks(hideBubbleRunnable)
        removeCallbacks(stopEatingRunnable)
        loadSkin("happy")
        bubbleText = "白饭！🍚"
        invalidate()
        postDelayed(stopEatingRunnable, 1600L)
    }

    fun toggleSleep() {
        setSleeping(!sleeping)
    }

    fun setSleeping(value: Boolean) {
        sleeping = value
        eating = false
        removeCallbacks(stopEatingRunnable)
        removeCallbacks(hideBubbleRunnable)
        loadSkin(if (value) "sleep" else "idle")
        bubbleText = if (value) null else "谁、谁睡了！"
        invalidate()
        if (!value) {
            postDelayed(hideBubbleRunnable, 2200L)
        }
    }

    fun wakeUp() {
        if (sleeping) {
            sleeping = false
            loadSkin("idle")
            showBubble("谁、谁睡了！", 2200L)
        }
    }

    fun setDragging(value: Boolean) {
        dragging = value
        if (value) {
            sleeping = false
            eating = false
            removeCallbacks(stopEatingRunnable)
            removeCallbacks(hideBubbleRunnable)
            loadSkin("drag")
        } else {
            loadSkin("idle")
            bubbleText = "放这儿就行。"
            removeCallbacks(hideBubbleRunnable)
            postDelayed(hideBubbleRunnable, 1800L)
        }
        invalidate()
    }

    override fun performClick(): Boolean {
        super.performClick()
        onTapped()
        return true
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(hideBubbleRunnable)
        removeCallbacks(stopEatingRunnable)
        super.onDetachedFromWindow()
    }
}
