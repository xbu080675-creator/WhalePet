package com.whalepet.android

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private var startAfterPermission = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = TextView(this).apply {
            text = "WhalePet M0.3\n蓝色大肥鱼"
            textSize = 24f
            gravity = Gravity.CENTER
        }

        val hint = TextView(this).apply {
            text = "单击逗她，双击喂白饭，长按切换 DeepSleep。\n拖动松手自动吸边，60 秒没人理她也会睡着。"
            textSize = 15f
            gravity = Gravity.CENTER
        }

        val startButton = Button(this).apply {
            text = "召唤蓝色大肥鱼"
            setOnClickListener { ensureOverlayPermissionAndStart() }
        }

        val stopButton = Button(this).apply {
            text = "让大肥鱼回窝"
            setOnClickListener {
                stopService(Intent(this@MainActivity, WhalePetService::class.java))
            }
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(24), dp(24), dp(24))
            addView(title)
            addView(hint)
            addView(startButton)
            addView(stopButton)
        }

        setContentView(layout)
    }

    override fun onResume() {
        super.onResume()
        if (startAfterPermission && Settings.canDrawOverlays(this)) {
            startAfterPermission = false
            startPetService()
        }
    }

    private fun ensureOverlayPermissionAndStart() {
        if (Settings.canDrawOverlays(this)) {
            startPetService()
            return
        }

        startAfterPermission = true
        startActivity(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
        )
    }

    private fun startPetService() {
        val intent = Intent(this, WhalePetService::class.java)
            .setAction(WhalePetService.ACTION_START)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
