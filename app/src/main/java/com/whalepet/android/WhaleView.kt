package com.whalepet.android

import android.content.Context
import android.graphics.*
import android.view.View

class WhaleView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val skinRenderer = SkinRenderer(context)
    private var skinBitmap: Bitmap? = null
    private var sleeping = false
    private var dragging = false
    private var eating = false
    private var bubbleText: String? = "本小姐登场。"

    init {
        isClickable = true
        loadSkin("idle")
    }

    private fun loadSkin(state: String) {
        skinBitmap = skinRenderer.loadSkinBitmap("deepseek_whale", state)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bitmap = skinBitmap
        if (bitmap != null) {
            val scale = minOf(width.toFloat() / bitmap.width, height.toFloat() / bitmap.height)
            val left = (width - bitmap.width * scale) / 2f
            val top = (height - bitmap.height * scale) / 2f
            canvas.drawBitmap(bitmap, null, RectF(left, top, left + bitmap.width * scale, top + bitmap.height * scale), paint)
        } else {
            paint.color = Color.rgb(70,136,224)
            canvas.drawOval(RectF(width*0.2f,height*0.35f,width*0.8f,height*0.85f),paint)
        }

        bubbleText?.let {
            paint.color = Color.rgb(30,70,120)
            paint.textSize = width*0.08f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(it,width/2f,height*0.2f,paint)
        }
    }

    fun onTapped(){
        bubbleText="今天也要吃 token 🍚"
        loadSkin("happy")
        invalidate()
    }

    fun feedRice(){
        eating=true
        bubbleText="白饭！🍚"
        invalidate()
    }

    fun toggleSleep(){
        sleeping=!sleeping
        loadSkin(if(sleeping) "sleep" else "idle")
        invalidate()
    }

    fun setSleeping(value:Boolean){
        sleeping=value
        loadSkin(if(value) "sleep" else "idle")
        invalidate()
    }

    fun wakeUp(){
        sleeping=false
        loadSkin("idle")
        invalidate()
    }

    fun setDragging(value:Boolean){
        dragging=value
        loadSkin(if(value) "drag" else "idle")
        invalidate()
    }

    override fun performClick():Boolean{
        super.performClick()
        onTapped()
        return true
    }
}