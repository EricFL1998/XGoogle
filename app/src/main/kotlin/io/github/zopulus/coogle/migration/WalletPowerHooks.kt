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
        showOverLockScreen()
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

    /**
     * On HyperOS the tap-to-pay screen opened by the power key waits behind the lock screen until
     * it is unlocked. Show it over the lock screen like on Pixel; paying still needs Google's own
     * verification.
     */
    private fun showOverLockScreen() {
        runCatching {
            ModernReflect.findAndHookMethod(android.app.Instrumentation::class.java, "callActivityOnCreate",
                Activity::class.java, android.os.Bundle::class.java,
                object : ModernMethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val activity = param.args.firstOrNull() as? Activity ?: return
                        if (!FeatureConfig.enabled(FeaturePolicy.WALLET)) return
                        if (!WalletPowerPolicy.isQuickDrawPreview(activity.componentName.className, activity.intent?.action)) return
                        activity.setShowWhenLocked(true)
                        activity.setTurnScreenOn(true)
                        // Not still over the lock screen the next time the phone wakes.
                        activity.registerReceiver(object : android.content.BroadcastReceiver() {
                            override fun onReceive(context: android.content.Context, intent: android.content.Intent) {
                                runCatching { activity.unregisterReceiver(this) }
                                activity.finish()
                            }
                        }, android.content.IntentFilter(android.content.Intent.ACTION_SCREEN_OFF), android.content.Context.RECEIVER_EXPORTED)
                    }
                },
            )
            HookLog.i("WalletPowerHooks", "Tap-to-pay screen shows over the lock screen")
        }.onFailure { HookLog.w("WalletPowerHooks", "Tap-to-pay screen stays behind the lock screen", it) }
    }

    fun hook(loader: ClassLoader) {
        hookHyperOs(loader)
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

    /** Google Wallet's tap-to-pay screen; it shows over the lock screen and asks for the lock itself. */
    private fun openGoogleWallet(context: Context, user: Int): Boolean {
        runCatching { context.packageManager.getPackageInfo(WalletPowerPolicy.WALLET_PACKAGE, 0) }.getOrNull() ?: return false
        val intent = android.content.Intent(WalletPowerPolicy.QUICKDRAW_ACTION)
            .setClassName(WalletPowerPolicy.WALLET_PACKAGE, WalletPowerPolicy.QUICKDRAW_ACTIVITY)
            .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                // It shows over the lock screen: close it once it is left or the screen goes off.
                or android.content.Intent.FLAG_ACTIVITY_NO_HISTORY or android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        return try {
            val asUser = Context::class.java.getMethod("startActivityAsUser",
                android.content.Intent::class.java, android.os.UserHandle::class.java)
            asUser.invoke(context, intent, android.os.UserHandle.getUserHandleForUid(user * 100000))
            true
        } catch (t: Throwable) {
            HookLog.w("WalletPowerHooks", "Google Wallet not opened; HyperOS handles the power key", t)
            false
        }
    }

    private fun currentUser(): Int =
        android.app.ActivityManager::class.java.getMethod("getCurrentUser").invoke(null) as Int

    /**
     * HyperOS 4 opens the wallet role holder's quick access wallet on a double press of the power
     * key. Google's quick access wallet lives in Play services, not in Google Wallet, so HyperOS
     * finds no wallet and only shows "Can't open the default wallet". Open Google Wallet instead.
     */
    private fun hookHyperOs(loader: ClassLoader) {
        runCatching {
            val target = ModernReflect.findClass("com.miui.server.input.util.ShortCutActionsUtils", loader)
            val contextField = ModernReflect.findField(target, "mContext")
            require(Context::class.java.isAssignableFrom(contextField.type))
            ModernReflect.findAndHookMethod(target, "launchDefaultWallet",
                object : ModernMethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!FeatureConfig.enabled(FeaturePolicy.WALLET)) return
                        val context = contextField.get(param.thisObject ?: return) as? Context ?: return
                        if (openGoogleWallet(context, currentUser())) param.result = true
                    }
                },
            )
            HookLog.i("WalletPowerHooks", "Installed HyperOS power key route to Google Wallet")
        }.onFailure { HookLog.w("WalletPowerHooks", "HyperOS wallet binding unavailable; original behavior retained", it) }
    }

    /**
     * With the wallet picked for the power key, Android's own gesture service hands the double
     * press to SystemUI, which looks for the same missing quick access wallet. Open Google Wallet.
     */
    fun hookSystemUi(loader: ClassLoader) {
        runCatching {
            val target = ModernReflect.findClass("com.android.systemui.statusbar.phone.CentralSurfacesCommandQueueCallbacks", loader)
            ModernReflect.findAndHookMethod(target, "onWalletLaunchGestureDetected",
                object : ModernMethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!FeatureConfig.enabled(FeaturePolicy.WALLET)) return
                        val context = Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null) as? Context ?: return
                        if (openGoogleWallet(context, currentUser())) param.result = null
                    }
                },
            )
            HookLog.i("WalletPowerHooks", "Installed SystemUI wallet gesture route to Google Wallet")
        }.onFailure { HookLog.w("WalletPowerHooks", "SystemUI wallet binding unavailable; original behavior retained", it) }
    }
}
