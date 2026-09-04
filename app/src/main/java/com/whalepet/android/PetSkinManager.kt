package com.whalepet.android

import android.content.Context

/**
 * 桌宠皮肤管理器
 *
 * 当前版本先提供基础框架，默认皮肤继续使用代码绘制。
 * 后续接入 assets/skins 图片资源。
 */
object PetSkinManager {

    const val DEFAULT_SKIN = "blue_whale"
    const val DEEPSEEK_SKIN = "deepseek_whale"

    private var currentSkin = DEFAULT_SKIN

    fun current(): String = currentSkin

    fun select(id: String) {
        currentSkin = id
    }

    fun availableSkins(context: Context): List<String> {
        return listOf(
            DEFAULT_SKIN,
            DEEPSEEK_SKIN
        )
    }
}
