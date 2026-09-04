package com.whalepet.android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class SkinRenderer(private val context: Context) {

    fun loadSkinBitmap(
        skinId: String,
        state: String = "idle"
    ): Bitmap? {
        val path = "skins/$skinId/$state.png"
        return try {
            context.assets.open(path).use {
                BitmapFactory.decodeStream(it)
            }
        } catch (_: Exception) {
            null
        }
    }
}
