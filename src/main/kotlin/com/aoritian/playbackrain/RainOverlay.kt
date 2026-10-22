package com.aoritian.playbackrain

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.StateFlow

@Composable
fun RainOverlay(settingsFlow: StateFlow<RainSettings>, label: String = "") {
    val settings by settingsFlow.collectAsState()
    val playbackExpanded by PlaybackPresence.expanded.collectAsState()
    val playbackVisible by PlaybackPresence.visible.collectAsState()
    if (!settings.enabled) return
    if (settings.playbackOnly && !playbackExpanded && !playbackVisible) return

    val time = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        PluginLog.log("rain overlay composed $label")
        var startNs = 0L
        startNs = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                time.floatValue = (now - startNs) / 1_000_000_000f
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Canvas(Modifier.fillMaxSize()) {
            RainShader.draw(this, settings, time.floatValue)
        }
    }
}
