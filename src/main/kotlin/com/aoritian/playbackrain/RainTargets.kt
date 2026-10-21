package com.aoritian.playbackrain

data class RainTarget(
    val className: String,
    val methodName: String,
    val inClass: String,
    val parameterTypes: List<String>
)

data class MethodTarget(
    val className: String,
    val methodName: String,
    val parameterTypes: List<String>
)

object RainTargets {
    private const val PLAYBACK_COVER_CLASS = "androidx.compose.ui.\u05d7"
    private const val PLAYBACK_COVER_METHOD = "\u037f"
    private const val PLAYBACK_COVER_CALLER = "androidx.compose.ui.\u0537"

    private const val PLAYER_SHEET_STATE_CLASS = "com.xuncorp.voxzen.ui.component.PlayerSheetState"
    private const val SHEET_EXPANDED_METHOD = "\u0529"
    private const val SHEET_VISIBLE_METHOD = "\u0528"

    val for1190: RainTarget = RainTarget(
        className = PLAYBACK_COVER_CLASS,
        methodName = PLAYBACK_COVER_METHOD,
        inClass = PLAYBACK_COVER_CALLER,
        parameterTypes = listOf("boolean", "boolean")
    )

    val sheetExpanded1190: MethodTarget = MethodTarget(
        className = PLAYER_SHEET_STATE_CLASS,
        methodName = SHEET_EXPANDED_METHOD,
        parameterTypes = emptyList()
    )

    val sheetVisible1190: MethodTarget = MethodTarget(
        className = PLAYER_SHEET_STATE_CLASS,
        methodName = SHEET_VISIBLE_METHOD,
        parameterTypes = emptyList()
    )

    fun forVersion(version: String): RainTarget? =
        if (version == "1.19.0") for1190 else null
}
