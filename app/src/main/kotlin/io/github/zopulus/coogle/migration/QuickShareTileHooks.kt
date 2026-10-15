package io.github.zopulus.coogle.migration

import io.github.zopulus.coogle.FeatureConfig
import io.github.zopulus.coogle.FeaturePolicy
import io.github.zopulus.coogle.migration.compat.ModernHookBridge as XposedBridge
import io.github.zopulus.coogle.migration.compat.ModernMethodHook as XC_MethodHook
import io.github.zopulus.coogle.migration.compat.ModernReflect as XposedHelpers
import io.github.zopulus.coogle.migration.AssistantRuntime.installedHooks
import io.github.zopulus.coogle.migration.AssistantRuntime.log

/**
 * HyperOS leaves Google's Quick Share tile out of the control center on the China ROM
 * (IS_INTERNATIONAL_BUILD): the control center plugin leaves it out of the edit list, and
 * SystemUI never creates a stored one. With the GMS restriction removed Quick Share works,
 * so offer its tile as the global ROM does.
 */
internal object QuickShareTileHooks {
    private const val SPEC = "custom(com.google.android.gms/.nearby.sharing.SharingTileService)"
    private const val PLUGIN = "miui.systemui.plugin"

    fun install(loader: ClassLoader) {
        if (!installedHooks.add("quick-share-tile")) return
        runCatching { hookCreation(loader) }.onFailure { log("Quick Share tile not created on the China ROM", it) }
        runCatching { hookPluginLoader(loader) }.onFailure { log("Control center plugin loader not found", it) }
    }

    /** The stored tile: create it where HyperOS returns null for it. */
    private fun hookCreation(loader: ClassLoader) {
        val impl = XposedHelpers.findClass("com.android.systemui.qs.pipeline.domain.interactor.CurrentTilesInteractorImpl", loader)
        val tileSpec = XposedHelpers.findClass("com.android.systemui.qs.pipeline.shared.TileSpec", loader)
        val getSpec = tileSpec.getMethod("getSpec")
        val ext = impl.getDeclaredField("interactorExt").apply { isAccessible = true }
        val factoryField = ext.type.getDeclaredField("tileFactory").apply { isAccessible = true }
        val createTile = factoryField.type.getMethod("createTile", String::class.java)
        XposedBridge.hookMethod(impl.getDeclaredMethod("createTileSync", tileSpec), object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (param.result != null || param.throwable != null) return
                if (getSpec.invoke(param.args[0]) != SPEC || !FeatureConfig.enabled(FeaturePolicy.GMS)) return
                val factory = factoryField.get(ext.get(param.thisObject)) ?: return
                param.result = createTile.invoke(factory, SPEC)
            }
        })
        log("Quick Share tile can be created in the control center")
    }

    /** SystemUI loads the control center plugin with a class loader of its own, unseen by LSPosed. */
    private fun hookPluginLoader(loader: ClassLoader) {
        val factory = XposedHelpers.findClass("com.android.systemui.shared.plugins.PluginInstance\$PluginFactory", loader)
        val appInfo = factory.getDeclaredField("pluginAppInfo").apply { isAccessible = true }
        XposedBridge.hookMethod(factory.getDeclaredMethod("createClassLoader"), object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val plugin = param.result as? ClassLoader ?: return
                if ((appInfo.get(param.thisObject) as? android.content.pm.ApplicationInfo)?.packageName == PLUGIN) installPlugin(plugin)
            }
        })
    }

    /** miui.systemui.plugin, loaded into SystemUI: its edit list leaves the tile out (filterNearby). */
    fun installPlugin(loader: ClassLoader) {
        if (!installedHooks.add("quick-share-tile-plugin")) return
        runCatching {
            val companion = XposedHelpers.findClass("miui.systemui.controlcenter.qs.customize.TileQueryHelper\$Companion", loader)
            XposedBridge.hookMethod(companion.getDeclaredMethod("filterNearby", String::class.java), object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (param.args[0] == SPEC && FeatureConfig.enabled(FeaturePolicy.GMS)) param.result = false
                }
            })
            log("Quick Share tile offered in the control center edit list")
        }.onFailure { installedHooks.remove("quick-share-tile-plugin"); log("Quick Share tile not offered in the control center edit list", it) }
    }
}
