@file:OptIn(UnstableSpwWorkshopApi::class)

package com.aoritian.playbackrain

import com.xuncorp.spw.workshop.api.PluginContext
import com.xuncorp.spw.workshop.api.PluginPermission
import com.xuncorp.spw.workshop.api.SpwPlugin
import com.xuncorp.spw.workshop.api.UnstableSpwWorkshopApi
import com.xuncorp.spw.workshop.api.WorkshopApi
import com.xuncorp.spw.workshop.api.config.ConfigHelper
import com.xuncorp.spw.workshop.api.config.ConfigManager
import com.xuncorp.spw.workshop.api.hook.HookHandle
import java.util.function.Consumer

class PlaybackRainPlugin(
    pluginContext: PluginContext
) : SpwPlugin(pluginContext) {
    private lateinit var configManager: ConfigManager
    private lateinit var configHelper: ConfigHelper
    private var configListener: Consumer<ConfigHelper>? = null
    private var rainHandle: HookHandle? = null
    private var sheetExpandedHandle: HookHandle? = null
    private var sheetVisibleHandle: HookHandle? = null
    private val settingsState = kotlinx.coroutines.flow.MutableStateFlow(RainSettings())

    override fun start() {
        PluginLog.init(
            WorkshopApi.manager.createConfigManager()
                .getConfig()
                .getConfigPath()
                .parent
                ?.toFile()
        )
        PluginLog.log("start version=${pluginContext.spwVersion}")
        if (!WorkshopApi.manager.isPermissionGranted(PluginPermission.CLASS_TRANSFORM)) {
            PluginLog.log("missing CLASS_TRANSFORM permission")
            WorkshopApi.ui.toast(
                "\u64AD\u653E\u9875\u96E8\u6EF4\u9700\u8981\u6388\u4E88\u201C\u4FEE\u6539\u754C\u9762\u201D\u6743\u9650",
                WorkshopApi.Ui.ToastType.Warning
            )
            return
        }

        configManager = WorkshopApi.manager.createConfigManager()
        configHelper = configManager.getConfig()
        settingsState.value = RainConfig.read(configHelper)
        val listener = Consumer<ConfigHelper> { helper ->
            settingsState.value = RainConfig.read(helper)
        }
        configListener = listener
        configManager.addConfigChangeListener(listener)

        val target = RainTargets.latest
        try {
            rainHandle = WorkshopApi.hookRegistrar.afterComposable(
                className = target.className,
                methodName = target.methodName,
                inClass = target.inClass,
                parameterTypes = target.parameterTypes
            ) {
                RainOverlay(settingsState)
            }
            PluginLog.log("rain hook installed")
            WorkshopApi.ui.toast(
                "\u64AD\u653E\u9875\u96E8\u6EF4\u5DF2\u542F\u7528",
                WorkshopApi.Ui.ToastType.Success
            )
        } catch (failure: Throwable) {
            PluginLog.log(
                "rain hook failed: ${failure.javaClass.name}: ${failure.message}"
            )
            WorkshopApi.ui.toast(
                "\u64AD\u653E\u9875\u96E8\u6EF4 Hook \u5B89\u88C5\u5931\u8D25",
                WorkshopApi.Ui.ToastType.Error
            )
        }
        installPresenceHooks()
    }

    private fun installPresenceHooks() {
        val expanded = RainTargets.sheetExpanded
        try {
            sheetExpandedHandle = WorkshopApi.hookRegistrar.hookMethod(
                className = expanded.className,
                methodName = expanded.methodName,
                parameterTypes = expanded.parameterTypes
            ) {
                after { call ->
                    PlaybackPresence.updateExpanded(call.result as? Boolean ?: false)
                }
            }
            PluginLog.log("playback expanded hook installed")
        } catch (failure: Throwable) {
            PluginLog.log(
                "playback expanded hook failed: ${failure.javaClass.name}: ${failure.message}"
            )
        }
        val visible = RainTargets.sheetVisible
        try {
            sheetVisibleHandle = WorkshopApi.hookRegistrar.hookMethod(
                className = visible.className,
                methodName = visible.methodName,
                parameterTypes = visible.parameterTypes
            ) {
                after { call ->
                    PlaybackPresence.updateVisible(call.result as? Boolean ?: false)
                }
            }
            PluginLog.log("playback visible hook installed")
        } catch (failure: Throwable) {
            PluginLog.log(
                "playback visible hook failed: ${failure.javaClass.name}: ${failure.message}"
            )
        }
    }

    override fun stop() {
        PluginLog.log("stop")
        configListener?.let {
            if (::configManager.isInitialized) {
                try {
                    configManager.removeConfigChangeListener(it)
                } catch (_: Throwable) {
                }
            }
        }
        configListener = null
        rainHandle?.close()
        rainHandle = null
        sheetExpandedHandle?.close()
        sheetExpandedHandle = null
        sheetVisibleHandle?.close()
        sheetVisibleHandle = null
    }
}
