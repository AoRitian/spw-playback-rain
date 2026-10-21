package com.aoritian.playbackrain

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object PluginLog {
    private var file: File? = null
    private val timeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    fun init(directory: File?) {
        if (directory == null) return
        directory.mkdirs()
        file = File(directory, "playback-rain.log")
        log("log started")
    }

    @Synchronized
    fun log(message: String) {
        val line = "[${LocalDateTime.now().format(timeFormat)}] $message"
        try {
            file?.appendText(line + "\n", Charsets.UTF_8)
        } catch (_: Throwable) {
        }
        println(line)
    }
}
