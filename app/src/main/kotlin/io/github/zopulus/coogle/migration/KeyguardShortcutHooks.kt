package io.github.zopulus.coogle.migration

import io.github.zopulus.coogle.migration.compat.ModernHookBridge as XposedBridge
import io.github.zopulus.coogle.migration.compat.ModernMethodHook as XC_MethodHook
import io.github.zopulus.coogle.migration.compat.ModernReflect as XposedHelpers
import io.github.zopulus.coogle.migration.AssistantRuntime.log
import io.github.zopulus.coogle.migration.AssistantRuntime.installedHooks

/**
 * HyperOS lock screen shortcuts: Coogle publishes its entries through the
 * "miui_keyguard_shortcuts" meta-data; this admits them past the picker's white list.
 * The shortcut code ships in com.miui.aod and is also loaded into SystemUI, which
 * clears any saved shortcut that is not white-listed.
 */
internal object KeyguardShortcutHooks {
    const val AOD_PACKAGE = "com.miui.aod"
    private const val COOGLE_PACKAGE = "io.github.leekin2.xgoogle"
    private const val DATA_UTILS = "com.miui.keyguard.shortcuts.utils.DataUtils"
    private val TAGS = listOf("coogle_assistant", "coogle_lens", "coogle_wallet", "coogle_home")

    /** com.miui.aod: the shortcut classes are in the app's own loader. */
    fun install(classLoader: ClassLoader) {
        hookWhiteList(classLoader)
    }

    /** SystemUI: catch the loader created for MIUIAod.apk. */
    fun installInSystemUi() {
        val key = "keyguard-shortcuts:aod-loader"
        if (!installedHooks.add(key)) return
        runCatching {
            XposedBridge.hookAllConstructors(
                dalvik.system.BaseDexClassLoader::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val dexPath = param.args.firstOrNull() as? String ?: return
                        if (!dexPath.contains("MIUIAod")) return
                        val loader = param.thisObject as? ClassLoader ?: return
                        hookWhiteList(loader)
                    }
                },
            )
        }.onFailure {
            installedHooks.remove(key)
            log("Failed to watch for the HyperOS lock screen shortcut loader", it)
        }
    }

    private fun hookWhiteList(classLoader: ClassLoader) {
        val cls = XposedHelpers.findClassIfExists(DATA_UTILS, classLoader) ?: return
        val key = "keyguard-shortcuts:white-list@${System.identityHashCode(classLoader)}"
        if (!installedHooks.add(key)) return
        runCatching {
            XposedBridge.hookAllMethods(
                cls,
                "loadWhiteItems",
                object : XC_MethodHook() {
                    @Suppress("UNCHECKED_CAST")
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val items = param.result as? MutableList<Any?> ?: return
                        // DataUtils.buildUniqueTag joins package and tag with "-".
                        TAGS.map { "$COOGLE_PACKAGE-$it" }
                            .filterNot(items::contains)
                            .forEach(items::add)
                    }
                },
            )
            log("Admitted Coogle entries to the HyperOS lock screen shortcut list")
        }.onFailure {
            installedHooks.remove(key)
            log("Failed to admit Coogle lock screen shortcuts", it)
        }
    }
}
