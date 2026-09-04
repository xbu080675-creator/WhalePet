package com.whalepet.android

import android.app.*
import android.content.*
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.*
import android.widget.Toast

class WhalePetService:Service(){

    lateinit var wm:WindowManager
    lateinit var fish:WhaleView


    override fun onCreate(){

        super.onCreate()

        wm=getSystemService(WINDOW_SERVICE) as WindowManager

        fish=WhaleView(this)


        val params=WindowManager.LayoutParams(

            220,
            220,

            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,

            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,

            PixelFormat.TRANSLUCENT
        )


        params.gravity=Gravity.RIGHT or Gravity.CENTER_VERTICAL


        wm.addView(fish,params)


        fish.setOnTouchListener(
            object:View.OnTouchListener{

                var x=0f
                var y=0f

                override fun onTouch(v:View,e:MotionEvent):Boolean{

                    when(e.action){

                        MotionEvent.ACTION_DOWN->{
                            x=e.rawX
                            y=e.rawY
                        }

                        MotionEvent.ACTION_MOVE->{

                            params.x-=(
                                e.rawX-x
                            ).toInt()

                            params.y+=(
                                e.rawY-y
                            ).toInt()

                            x=e.rawX
                            y=e.rawY

                            wm.updateViewLayout(
                                fish,
                                params
                            )
                        }


                        MotionEvent.ACTION_UP->{
                            fish.toggleSleep()
                        }

                    }

                    return true
                }
            }
        )

    }


    override fun onBind(i:Intent?):IBinder?=null


    override fun onDestroy(){

        wm.removeView(fish)

        super.onDestroy()

    }
}
