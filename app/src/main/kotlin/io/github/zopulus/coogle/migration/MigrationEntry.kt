package io.github.zopulus.coogle.migration

import io.github.zopulus.coogle.FeatureConfig
import io.github.zopulus.coogle.FeaturePolicy
import io.github.zopulus.coogle.migration.compat.ModernHookRuntime
import io.github.libxposed.api.XposedModule

/** Entry point for the Google-related features migrated from ONextBox. */
object MigrationEntry {
    @JvmStatic fun attach(module: XposedModule) = ModernHookRuntime.attach(module)
    @JvmStatic fun hookPackage(packageName: String, loader: ClassLoader) {
        if (packageName == "com.oplus.wallpapers") {
            if (FeatureConfig.enabled(FeaturePolicy.QUICK_ENTRIES)) WallpaperQuickEntryHooks.install(loader)
            return
        }
        if (packageName == "com.android.systemui" && FeatureConfig.enabled(FeaturePolicy.QUICK_ENTRIES)) {
            NativeQuickEntryHooks.install(loader)
            KeyguardShortcutHooks.installInSystemUi()
        }
        if (packageName == KeyguardShortcutHooks.AOD_PACKAGE) {
            if (FeatureConfig.enabled(FeaturePolicy.QUICK_ENTRIES)) KeyguardShortcutHooks.install(loader)
            return
        }
        if (packageName == XiaoAiNavRedirectHooks.XIAOAI_PACKAGE) {
            if (FeatureConfig.enabled(FeaturePolicy.NAVIGATION)) XiaoAiNavRedirectHooks.install(loader)
            return
        }
        if (packageName == "com.google.android.apps.walletnfcrel") {
            if (FeatureConfig.enabled(FeaturePolicy.WALLET)) WalletPowerHooks.hookPackage(packageName, loader)
            return
        }
        if (packageName == "com.google.android.googlequicksearchbox") {
            OkGoogleHotwordCompatibilityHooker.hookGoogleApp(loader)
        } else {
            AssistantHooker.hook(packageName, loader)
        }
    }
    @JvmStatic fun hookSystem(loader: ClassLoader) {
        if (FeatureConfig.enabled(FeaturePolicy.WALLET)) WalletPowerHooks.hook(loader)
        GmsRegionRestrictionHooker.hook(loader)
        AssistantHooker.hook("system", loader)
        OkGoogleHotwordCompatibilityHooker.hookSystemServer(loader)
    }
}
