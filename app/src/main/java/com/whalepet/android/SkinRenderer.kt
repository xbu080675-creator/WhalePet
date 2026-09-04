package com.whalepet.android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class SkinRenderer(private val context: Context) {

    fun loadSkinBitmap(
        skinId: String,
        state: String = "idle"
    ): Bitmap? {
        val candidates = listOf(
            "skins/$skinId/$state.png",
            "skins/$skinId/$state/$state.png",
            "skins/$skinId/$state.webp",
            "skins/$skinId/$state/$state.webp"
        )

        for (path in candidates) {
            try {
                return context.assets.open(path).use {
                    BitmapFactory.decodeStream(it)
                }
            } catch (_: Exception) {
            }
        }

        return null
    }
}
