package com.whalepet.android

import android.graphics.Bitmap

/**
 * WhalePet 皮肤资源描述
 *
 * M0.3 起将视觉资源与桌宠逻辑分离。
 */
data class SkinAsset(
    val id: String,
    val name: String,
    val idle: Bitmap? = null,
    val happy: Bitmap? = null,
    val sleep: Bitmap? = null,
    val drag: Bitmap? = null
)
