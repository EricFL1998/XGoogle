package io.github.zopulus.coogle.migration

import android.content.Context
import io.github.zopulus.coogle.FeatureConfig
import io.github.zopulus.coogle.FeaturePolicy
import io.github.zopulus.coogle.migration.compat.ModernHookBridge as XposedBridge
import io.github.zopulus.coogle.migration.compat.ModernMethodHook as XC_MethodHook
import io.github.zopulus.coogle.migration.compat.ModernReflect as XposedHelpers
import io.github.zopulus.coogle.migration.AssistantRuntime.log
import io.github.zopulus.coogle.migration.AssistantRuntime.installedHooks

/**
 * HyperOS Settings adds the Google services entry at the bottom of its main list only on
 * global builds. Add it on the CN ROM when the user wants it, and take it out when they
 * don't (another module may have turned it on).
 */
internal object GoogleSettingsEntryHooks {
    private const val MIUI_SETTINGS = "com.android.settings.MiuiSettings"

    fun install(classLoader: ClassLoader) {
        val cls = XposedHelpers.findClassIfExists(MIUI_SETTINGS, classLoader) ?: return
        val key = "google-settings-entry:${cls.name}"
        if (!installedHooks.add(key)) return
        runCatching {
            XposedBridge.hookAllMethods(
                cls,
                "updateHeaderList",
                object : XC_MethodHook() {
                    @Suppress("UNCHECKED_CAST")
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val headers = param.args.firstOrNull() as? MutableList<Any?> ?: return
                        val context = param.thisObject as? Context ?: return
                        val id = context.resources.getIdentifier("header_google_settings", "id", context.packageName)
                        if (id == 0) return
                        val isGoogle = { header: Any? ->
                            header != null &&
                                runCatching { XposedHelpers.getObjectField(header, "id") as Long }.getOrNull() == id.toLong()
                        }
                        if (FeatureConfig.enabled(FeaturePolicy.GOOGLE_ENTRY)) {
                            if (headers.none(isGoogle)) XposedHelpers.callMethod(param.thisObject, "AddGoogleSettingsHeaders", headers)
                        } else {
                            headers.removeAll(isGoogle)
                        }
                    }
                },
            )
            log("Google entry in HyperOS Settings follows the XGoogle switch")
        }.onFailure {
            installedHooks.remove(key)
            log("Failed to control the Google entry in HyperOS Settings", it)
        }
    }
}
