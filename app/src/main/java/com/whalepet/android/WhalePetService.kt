package com.whalepet.android

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import kotlin.math.abs

class WhalePetService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var petView: WhaleView
    private lateinit var params: WindowManager.LayoutParams

    private val handler = Handler(Looper.getMainLooper())
    private var overlayAdded = false
    private var snapAnimator: ValueAnimator? = null
    private val prefs by lazy { getSharedPreferences(PREFS_NAME, MODE_PRIVATE) }

    private val sleepRunnable = Runnable {
        if (overlayAdded) {
            petView.setSleeping(true)
        }
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        addPetOverlay()
        scheduleSleep()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (overlayAdded) {
            petView.wakeUp()
            scheduleSleep()
        }

        return START_STICKY
    }

    private fun addPetOverlay() {
        if (overlayAdded) return

        val petSize = dp(168)
        val (screenWidth, screenHeight) = screenSize()
        val savedX = prefs.getInt(KEY_X, Int.MIN_VALUE)
        val savedY = prefs.getInt(KEY_Y, Int.MIN_VALUE)

        params = WindowManager.LayoutParams(
            petSize,
            petSize,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = if (savedX == Int.MIN_VALUE) {
                (screenWidth - petSize).coerceAtLeast(0)
            } else {
                savedX.coerceIn(0, (screenWidth - petSize).coerceAtLeast(0))
            }
            y = if (savedY == Int.MIN_VALUE) {
                ((screenHeight - petSize) / 2).coerceAtLeast(0)
            } else {
                savedY.coerceIn(0, (screenHeight - petSize).coerceAtLeast(0))
            }
        }

        petView = WhaleView(this)
        windowManager.addView(petView, params)
        overlayAdded = true
        installTouchHandler(petSize)
    }

    private fun installTouchHandler(petSize: Int) {
        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop

        petView.setOnTouchListener(object : View.OnTouchListener {
            var downRawX = 0f
            var downRawY = 0f
            var startX = 0
            var startY = 0
            var moved = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        snapAnimator?.cancel()
                        downRawX = event.rawX
                        downRawY = event.rawY
                        startX = params.x
                        startY = params.y
                        moved = false
                        petView.wakeUp()
                        scheduleSleep()
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - downRawX).toInt()
                        val dy = (event.rawY - downRawY).toInt()

                        if (!moved && (abs(dx) > touchSlop || abs(dy) > touchSlop)) {
                            moved = true
                            petView.setDragging(true)
                        }

                        if (moved) {
                            val (screenWidth, screenHeight) = screenSize()
                            params.x = (startX + dx).coerceIn(
                                0,
                                (screenWidth - petSize).coerceAtLeast(0)
                            )
                            params.y = (startY + dy).coerceIn(
                                0,
                                (screenHeight - petSize).coerceAtLeast(0)
                            )
                            updateOverlay()
                        }
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        if (moved) {
                            petView.setDragging(false)
                            snapToNearestEdge(petSize)
                        } else {
                            petView.performClick()
                        }
                        scheduleSleep()
                        return true
                    }

                    MotionEvent.ACTION_CANCEL -> {
                        if (moved) {
                            petView.setDragging(false)
                            snapToNearestEdge(petSize)
                        }
                        scheduleSleep()
                        return true
                    }
                }

                return false
            }
        })
    }

    private fun snapToNearestEdge(petSize: Int) {
        if (!overlayAdded) return

        val (screenWidth, screenHeight) = screenSize()
        params.y = params.y.coerceIn(0, (screenHeight - petSize).coerceAtLeast(0))

        val targetX = if (params.x + petSize / 2 < screenWidth / 2) {
            0
        } else {
            (screenWidth - petSize).coerceAtLeast(0)
        }

        val startX = params.x
        if (startX == targetX) {
            savePosition()
            return
        }

        snapAnimator?.cancel()
        snapAnimator = ValueAnimator.ofInt(startX, targetX).apply {
            duration = 180L
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                params.x = it.animatedValue as Int
                updateOverlay()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    savePosition()
                }
            })
            start()
        }
    }

    private fun scheduleSleep() {
        handler.removeCallbacks(sleepRunnable)
        handler.postDelayed(sleepRunnable, AUTO_SLEEP_MS)
    }

    private fun updateOverlay() {
        if (!overlayAdded) return
        try {
            windowManager.updateViewLayout(petView, params)
        } catch (_: IllegalArgumentException) {
            // The overlay may already have been removed while the service stops.
        }
    }

    private fun savePosition() {
        if (!::params.isInitialized) return
        prefs.edit()
            .putInt(KEY_X, params.x)
            .putInt(KEY_Y, params.y)
            .apply()
    }

    private fun screenSize(): Pair<Int, Int> {
        val metrics = resources.displayMetrics
        return metrics.widthPixels to metrics.heightPixels
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (!overlayAdded) return

        val petSize = params.width
        val (screenWidth, screenHeight) = screenSize()
        params.x = params.x.coerceIn(0, (screenWidth - petSize).coerceAtLeast(0))
        params.y = params.y.coerceIn(0, (screenHeight - petSize).coerceAtLeast(0))
        updateOverlay()
        snapToNearestEdge(petSize)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "WhalePet 悬浮桌宠",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "保持蓝色大肥鱼在屏幕上活动"
            setShowBadge(false)
        }

        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, WhalePetService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("蓝色大肥鱼正在摸鱼")
            .setContentText("拖动她会自动吸边，60 秒不理她就会 DeepSleep")
            .setContentIntent(openAppIntent)
            .addAction(
                Notification.Action.Builder(
                    null,
                    "回窝",
                    stopIntent
                ).build()
            )
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        snapAnimator?.cancel()

        if (overlayAdded) {
            savePosition()
            try {
                windowManager.removeView(petView)
            } catch (_: IllegalArgumentException) {
                // Already removed.
            }
            overlayAdded = false
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    companion object {
        const val ACTION_START = "com.whalepet.android.action.START"
        const val ACTION_STOP = "com.whalepet.android.action.STOP"

        private const val CHANNEL_ID = "whalepet_overlay"
        private const val NOTIFICATION_ID = 202
        private const val AUTO_SLEEP_MS = 60_000L

        private const val PREFS_NAME = "whalepet_overlay"
        private const val KEY_X = "pet_x"
        private const val KEY_Y = "pet_y"
    }
}
