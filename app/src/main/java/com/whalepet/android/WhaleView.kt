package com.whalepet.android

import android.view.*
import android.graphics.*
import android.content.Context


class WhaleView(
    c:Context
):View(c){


    var sleep=false


    val paint=Paint(
        Paint.ANTI_ALIAS_FLAG
    )


    override fun onDraw(canvas:Canvas){

        super.onDraw(canvas)


        paint.color=
            if(sleep)
                Color.DKGRAY
            else
                Color.BLUE


        canvas.drawCircle(
            width/2f,
            height/2f,
            80f,
            paint
        )


        paint.color=Color.WHITE


        canvas.drawText(
            if(sleep)
                "DeepSleep..."
            else
                "🐋",
            50f,
            120f,
            paint
        )

    }


    fun toggleSleep(){

        sleep=!sleep

        invalidate()

    }

}
