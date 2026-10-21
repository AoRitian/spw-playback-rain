@file:OptIn(UnstableSpwWorkshopApi::class)

package com.aoritian.playbackrain

import com.xuncorp.spw.workshop.api.UnstableSpwWorkshopApi
import com.xuncorp.spw.workshop.api.config.ConfigHelper

data class RainSettings(
    val enabled: Boolean = true,
    val playbackOnly: Boolean = true,
    val speed: Float = 1f,
    val size: Float = 1f,
    val density: Float = 0.4f,
    val lightAngle: Float = 135f,
    val shadow: Float = 0f
)

object RainConfig {
    private const val ENABLED = "rain.enabled"
    private const val PLAYBACK_ONLY = "rain.playbackOnly"
    private const val SPEED = "rain.speed"
    private const val SIZE = "rain.size"
    private const val DENSITY = "rain.density"
    private const val LIGHT_ANGLE = "rain.lightAngle"
    private const val SHADOW = "rain.shadow"

    fun read(config: ConfigHelper): RainSettings = RainSettings(
        enabled = config.get(ENABLED, true),
        playbackOnly = config.get(PLAYBACK_ONLY, true),
        speed = percent(config.get(SPEED, "100")),
        size = percent(config.get(SIZE, "100")),
        density = percent(config.get(DENSITY, "40")),
        lightAngle = config.get(LIGHT_ANGLE, "135").toFloatOrNull() ?: 135f,
        shadow = percent(config.get(SHADOW, "0"))
    )

    private fun percent(value: String): Float =
        (value.toFloatOrNull() ?: 100f).coerceIn(0f, 200f) / 100f
}

