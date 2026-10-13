package io.github.zopulus.coogle.migration

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import io.github.zopulus.coogle.FeatureConfig
import io.github.zopulus.coogle.FeaturePolicy
import io.github.zopulus.coogle.migration.compat.ModernHookBridge as XposedBridge
import io.github.zopulus.coogle.migration.compat.ModernMethodHook as XC_MethodHook
import io.github.zopulus.coogle.migration.compat.ModernReflect as XposedHelpers
import io.github.zopulus.coogle.migration.AssistantRuntime.installedHooks
import io.github.zopulus.coogle.migration.AssistantRuntime.log

/**
 * The lock screen Home shortcut opens Android's device controls over the lock screen, as on the
 * global ROM, where Google Home shows its own panel. SystemUI embeds that panel, but HyperOS
 * starts it as a task of its own, which may not show over the lock screen: the lock screen comes
 * back, SystemUI opens the panel again, and it loops. Let that panel show over the lock screen,
 * as the embedded one does.
 */
internal object HomeControlsHooks {
    const val ACTION = "io.github.leekin2.xgoogle.SHOW_HOME_CONTROLS"
    private const val CONTROLS_PAGE = "com.android.systemui.action.controls.page"
    private val HOME_PANEL = android.content.ComponentName("com.google.android.apps.chromecast.app",
        "com.google.android.apps.chromecast.app.systemcontrol.panel.GoogleHomePanelActivity")

    /** system_server: the panel SystemUI opens may show over the lock screen. */
    fun hookSystem(loader: ClassLoader) {
        val key = "home-controls:show-when-locked"
        if (!installedHooks.add(key)) return
        runCatching {
            val record = XposedHelpers.findClass("com.android.server.wm.ActivityRecord", loader)
            val component = record.getDeclaredField("mActivityComponent").apply { isAccessible = true }
            val from = record.getDeclaredField("launchedFromPackage").apply { isAccessible = true }
            val method = record.getDeclaredMethod("canShowWhenLocked")
            XposedBridge.hookMethod(method, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (param.result == true || component.get(param.thisObject) != HOME_PANEL) return
                    if (from.get(param.thisObject) != "com.android.systemui") return
                    if (FeatureConfig.enabled(FeaturePolicy.QUICK_ENTRIES)) param.result = true
                }
            })
            log("Google Home's device controls panel may show over the lock screen")
        }.onFailure { installedHooks.remove(key); log("Google Home panel left behind the lock screen", it) }
    }

    /** SystemUI: XGoogle's lock screen shortcut asks for the device controls. */
    fun install() {
        val key = "home-controls:receiver"
        if (!installedHooks.add(key)) return
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            runCatching {
                val context = Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null) as Context
                context.registerReceiver(object : BroadcastReceiver() {
                    override fun onReceive(c: Context, intent: Intent) {
                        resultCode = if (show(c)) 1 else 0
                    }
                }, IntentFilter(ACTION), AssistantShortcutReceiver.PERMISSION, null, Context.RECEIVER_EXPORTED)
            }.onFailure { installedHooks.remove(key); log("Home controls receiver unavailable", it) }
        }
    }

    private fun show(context: Context): Boolean = runCatching {
        // Android's own switch for "Show device controls" on the lock screen.
        android.provider.Settings.Secure.putInt(context.contentResolver, "lockscreen_show_controls", 1)
        context.startActivity(Intent(CONTROLS_PAGE).setPackage(context.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
        true
    }.getOrElse { log("Home controls not shown", it); false }
}
