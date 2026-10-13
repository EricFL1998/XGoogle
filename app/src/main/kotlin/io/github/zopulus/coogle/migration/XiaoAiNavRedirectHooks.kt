package io.github.zopulus.coogle.migration

import android.content.Intent
import android.os.SystemClock
import io.github.zopulus.coogle.migration.compat.ModernHookBridge as XposedBridge
import io.github.zopulus.coogle.migration.compat.ModernMethodHook as XC_MethodHook
import io.github.zopulus.coogle.migration.compat.ModernReflect as XposedHelpers
import io.github.zopulus.coogle.migration.ContextualSearchHooks.triggerCircleToSearchC17
import io.github.zopulus.coogle.migration.AssistantRuntime.log
import io.github.zopulus.coogle.migration.AssistantRuntime.installedHooks

/**
 * HyperOS 4: the native (Rust) launcher hands the nav-handle long press to XiaoAi's
 * VoiceService as ACTION_ASSIST. Answer it with Circle to Search instead.
 */
internal object XiaoAiNavRedirectHooks {
    const val XIAOAI_PACKAGE = "com.miui.voiceassist"
    private const val START_NOT_STICKY = 2

    @Volatile
    private var lastRedirectAt = 0L

    /** A short tick as Circle to Search opens, like on Pixel; follows the touch feedback setting. */
    private fun tick(context: android.content.Context?) {
        context ?: return
        runCatching {
            if (android.provider.Settings.System.getInt(context.contentResolver,
                    android.provider.Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) == 0) return
            val vibrator = context.getSystemService(android.os.VibratorManager::class.java)?.defaultVibrator ?: return
            vibrator.vibrate(android.os.VibrationEffect.createPredefined(android.os.VibrationEffect.EFFECT_HEAVY_CLICK))
        }.onFailure { log("Circle to Search haptic unavailable", it) }
    }

    fun install(classLoader: ClassLoader) {
        val cls = XposedHelpers.findClassIfExists(
            "com.xiaomi.voiceassistant.VoiceService",
            classLoader,
        ) ?: return
        val key = "xiaoai:nav-redirect:${cls.name}"
        if (!installedHooks.add(key)) return
        runCatching {
            XposedBridge.atomic {
                val targets = cls.declaredMethods.filter {
                    it.name == "onStartCommand" && it.parameterTypes.size == 3
                }
                check(targets.isNotEmpty()) { "VoiceService.onStartCommand was not found" }
                targets.forEach { method ->
                    XposedBridge.hookMethod(
                        method,
                        object : XC_MethodHook() {
                            override fun beforeHookedMethod(param: MethodHookParam) {
                                if (!LspConfig.isAssistantNativeCircleEnabledXposed()) return
                                val intent = param.args.firstOrNull() as? Intent ?: return
                                if (intent.action != Intent.ACTION_ASSIST) return
                                // The launcher can deliver the same press twice.
                                val now = SystemClock.elapsedRealtime()
                                if (now - lastRedirectAt < 1000) {
                                    param.result = START_NOT_STICKY
                                    return
                                }
                                lastRedirectAt = now
                                if (triggerCircleToSearchC17()) {
                                    param.result = START_NOT_STICKY
                                    tick(param.thisObject as? android.content.Context)
                                }
                            }
                        },
                    )
                }
            }
            log("Hooked the HyperOS nav-handle XiaoAi route")
        }.onFailure {
            installedHooks.remove(key)
            log("Failed to hook the HyperOS nav-handle XiaoAi route", it)
        }
    }
}
