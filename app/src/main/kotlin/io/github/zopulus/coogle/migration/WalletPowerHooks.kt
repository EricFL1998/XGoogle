package io.github.zopulus.coogle.migration

import android.app.Activity
import android.app.KeyguardManager
import android.content.ContentResolver
import android.content.Context
import android.provider.Settings
import io.github.zopulus.coogle.FeatureConfig
import io.github.zopulus.coogle.FeaturePolicy
import io.github.zopulus.coogle.WalletPowerPolicy
import io.github.zopulus.coogle.migration.compat.ModernMethodHook
import io.github.zopulus.coogle.migration.compat.ModernReflect

/** Native wallet route; member bindings are resolved once per process. */
internal object WalletPowerHooks {
    fun hookPackage(packageName: String, loader: ClassLoader) {
        if (packageName != "com.google.android.apps.walletnfcrel") return
        runCatching {
            val previewUnlock = ModernReflect.findClass("awvk", loader)
            val unitClass = ModernReflect.findClass("bqmv", loader)
            val activityField = ModernReflect.findField(previewUnlock, "c")
            val keyguardField = ModernReflect.findField(previewUnlock, "b")
            // Do not silently apply an old obfuscation binding to a changed layout.
            require(Activity::class.java.isAssignableFrom(activityField.type))
            require(KeyguardManager::class.java.isAssignableFrom(keyguardField.type))
            val unit = requireNotNull(ModernReflect.findField(unitClass, "a").get(null))
            ModernReflect.findAndHookMethod(previewUnlock, "b", Any::class.java,
                object : ModernMethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!FeatureConfig.enabled(FeaturePolicy.WALLET)) return
                        val request = param.thisObject ?: return
                        val activity = activityField.get(request) as? Activity ?: return
                        if (!WalletPowerPolicy.isQuickDrawPreview(
                                activity.javaClass.name, activity.intent?.action,
                            )) return
                        val keyguard = keyguardField.get(request) as? KeyguardManager ?: return
                        if (!keyguard.isDeviceLocked) return
                        // No authentication-success callback; TAP_EVENT runs unchanged.
                        param.result = unit
                    }
                },
            )
            HookLog.i("WalletPowerHooks", "Installed QUICKDRAW automatic unlock hook")
        }.onFailure { HookLog.w("WalletPowerHooks", "Wallet preview binding unavailable; original behavior retained", it) }
    }

    fun hook(loader: ClassLoader) {
        runCatching {
            val target = ModernReflect.findClass("com.android.server.policy.OplusWalletController", loader)
            val contextField = ModernReflect.findField(target, "mContext")
            val userField = ModernReflect.findField(target, "mCurrentUserId")
            require(Context::class.java.isAssignableFrom(contextField.type))
            require(userField.type == Int::class.javaPrimitiveType)
            val localServices = ModernReflect.findClass("com.android.server.LocalServices", loader)
            val statusBarService = ModernReflect.findClass("com.android.server.statusbar.StatusBarManagerInternal", loader)
            val getService = ModernReflect.findMethodExact(localServices, "getService", arrayOf(Class::class.java))
            val launchWallet = ModernReflect.findMethodExact(statusBarService, "onWalletLaunchGestureDetected", emptyArray())
            val getPayment = ModernReflect.findMethodExact(Settings.Secure::class.java, "getStringForUser",
                arrayOf(ContentResolver::class.java, String::class.java, Int::class.javaPrimitiveType!!))
            ModernReflect.findAndHookMethod(target, "startWallet", Boolean::class.javaPrimitiveType,
                object : ModernMethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (param.args.firstOrNull() != true || !FeatureConfig.enabled(FeaturePolicy.WALLET)) return
                        val wallet = param.thisObject ?: return
                        val context = contextField.get(wallet) as? Context ?: return
                        val userId = userField.getInt(wallet)
                        // Read current user/payment selection; do not retain mutable state.
                        val payment = getPayment.invoke(null, context.contentResolver,
                            "nfc_payment_default_component", userId) as? String
                        if (!WalletPowerPolicy.isGooglePayment(payment)) return
                        val service = getService.invoke(null, statusBarService) ?: return
                        launchWallet.invoke(service)
                        param.result = null
                    }
                },
            )
            HookLog.i("WalletPowerHooks", "Installed native SystemUI wallet route")
        }.onFailure { HookLog.w("WalletPowerHooks", "Native wallet binding unavailable; original behavior retained", it) }
    }
}