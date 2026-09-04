package com.whalepet.android

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import android.net.Uri
import android.widget.Button
import android.widget.LinearLayout

class MainActivity: Activity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val button = Button(this)
        button.text="召唤蓝色大肥鱼"

        button.setOnClickListener {

            if(!Settings.canDrawOverlays(this)){
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                )
            }else{
                startService(
                    Intent(this,WhalePetService::class.java)
                )
            }
        }

        val layout=LinearLayout(this)
        layout.addView(button)

        setContentView(layout)
    }
}
