package io.github.zopulus.coogle.migration

import android.content.ComponentName
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
 * VoiceService as ACTION_ASSIST. Answer it with Circle to Search instead. The side swipe-and-hold
 * (when set to XiaoAi's intent ball) reaches XiaoAi the same way and opens Circle to Search too;
 * the double tap keeps XiaoAi.
 */
internal object XiaoAiNavRedirectHooks {
    const val XIAOAI_PACKAGE = "com.miui.voiceassist"
    private const val START_NOT_STICKY = 2
    /** The launcher names the gesture: NavLongPress, NavDoubleClick or SideSlideHold. */
    private const val TRIGGER_TYPE = "triggerType"
    private const val LONG_PRESS = "NavLongPress"
    private const val SIDE_HOLD = "SideSlideHold"
    /** The side hold sends the ball press, then its release or cancel under the same trigger. */
    private const val FUNCTION_KEY = "voice_assist_function_key"
    private const val BALL_PRESS = "home_intentional_ball"
    private const val TRIGGER_FROM = "triggerFrom"
    private const val LAUNCHER = "MiuiHome"
    private val VOICE_SERVICE = ComponentName(XIAOAI_PACKAGE, "com.xiaomi.voiceassistant.VoiceService")

    @Volatile
    private var lastRedirectAt = 0L

    private enum class Route { OPEN, SWALLOW }

    /** OPEN for a gesture Circle to Search answers, SWALLOW for the side hold's release and cancel. */
    private fun route(intent: Intent, nullTrigger: Boolean): Route? = when (intent.getStringExtra(TRIGGER_TYPE)) {
        LONG_PRESS -> Route.OPEN
        SIDE_HOLD -> if (intent.getStringExtra(FUNCTION_KEY).let { it == null || it == BALL_PRESS }) Route.OPEN else Route.SWALLOW
        null -> if (nullTrigger) Route.OPEN else null
        else -> null
    }

    /**
     * A short tick as Circle to Search opens, like on Pixel. It follows the touch feedback setting
     * unless "always vibrate" is on; then it is sent as hardware feedback, which Android does not
     * silence with touch feedback.
     */
    private fun tick(context: android.content.Context?) {
        context ?: return
        runCatching {
            val always = io.github.zopulus.coogle.FeatureConfig.enabled(io.github.zopulus.coogle.FeaturePolicy.CIRCLE_VIBRATE)
            if (!always && android.provider.Settings.System.getInt(context.contentResolver,
                    android.provider.Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) == 0) return
            val vibrator = context.getSystemService(android.os.VibratorManager::class.java)?.defaultVibrator ?: return
            val effect = android.os.VibrationEffect.createPredefined(android.os.VibrationEffect.EFFECT_HEAVY_CLICK)
            if (always) vibrator.vibrate(effect, android.os.VibrationAttributes.createForUsage(android.os.VibrationAttributes.USAGE_HARDWARE_FEEDBACK))
            else vibrator.vibrate(effect)
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
                                // The long press and the side hold; the double tap opens XiaoAi as before.
                                val route = route(intent, nullTrigger = true) ?: return
                                // The launcher can deliver the same press twice.
                                val now = SystemClock.elapsedRealtime()
                                if (route == Route.SWALLOW || now - lastRedirectAt < 1000) {
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

    /**
     * system_server: answer the launcher's request before it reaches XiaoAi. HyperOS often stops
     * XiaoAi in the background, and starting its processes first made Circle to Search open about
     * half a second late. The XiaoAi hook above stays for any request that gets past this one.
     */
    fun hookSystem(classLoader: ClassLoader) {
        val key = "xiaoai:nav-redirect:system"
        if (!installedHooks.add(key)) return
        runCatching {
            val ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService", classLoader)
            val targets = ams.declaredMethods.filter { method ->
                method.name == "startService" && method.returnType == ComponentName::class.java &&
                    Intent::class.java in method.parameterTypes
            }
            check(targets.isNotEmpty()) { "ActivityManagerService.startService was not found" }
            XposedBridge.atomic {
                targets.forEach { method ->
                    val index = method.parameterTypes.indexOf(Intent::class.java)
                    XposedBridge.hookMethod(method, object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            val intent = param.args[index] as? Intent ?: return
                            if (intent.action != Intent.ACTION_ASSIST || intent.component != VOICE_SERVICE) return
                            if (!LspConfig.isAssistantNativeCircleEnabledXposed()) return
                            // Only the launcher's nav-handle long press and side hold; XiaoAi keeps everything else.
                            val route = runCatching {
                                if (intent.getStringExtra(TRIGGER_FROM) == LAUNCHER) route(intent, nullTrigger = false) else null
                            }.getOrNull() ?: return
                            // The launcher sends the same press twice.
                            val now = SystemClock.elapsedRealtime()
                            if (route == Route.SWALLOW || now - lastRedirectAt < 1000) {
                                param.result = VOICE_SERVICE
                                return
                            }
                            if (!triggerCircleToSearchC17()) return
                            lastRedirectAt = now
                            param.result = VOICE_SERVICE
                            tick(AssistantRuntime.findSystemContext())
                        }
                    })
                }
            }
            log("Nav-handle long press opens Circle to Search from the system, without waiting for XiaoAi")
        }.onFailure {
            installedHooks.remove(key)
            log("System nav-handle route unavailable; the XiaoAi route is kept", it)
        }
    }
}
