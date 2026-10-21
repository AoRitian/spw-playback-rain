package com.aoritian.playbackrain

import kotlinx.coroutines.flow.MutableStateFlow

object PlaybackPresence {
    val expanded = MutableStateFlow(false)
    val visible = MutableStateFlow(false)

    fun updateExpanded(value: Boolean) {
        if (expanded.value != value) {
            expanded.value = value
            PluginLog.log("playback expanded=$value")
        }
    }

    fun updateVisible(value: Boolean) {
        if (visible.value != value) {
            visible.value = value
            PluginLog.log("playback visible=$value")
        }
    }
}
