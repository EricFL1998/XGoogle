package io.github.zopulus.coogle;

import android.app.Activity;
import android.app.KeyguardManager;

/** Resolves repackaged Kotlin code while retaining the verified QUICKDRAW preview boundary. */
final class WalletPreviewHooks {
    private static boolean unlocking;

    static void install(HookInstaller hooks, ClassLoader loader, String apkPath, String dataDir) {
        if (!FeatureConfig.enabled(FeaturePolicy.WALLET)) return;
        int mark = hooks.mark();
        try (AppDexTargets targets = new AppDexTargets(loader, apkPath, dataDir)) {
            var methods = targets.resolve(FeaturePolicy.WALLET);
            if (methods.size() != 2) { hooks.info("WALLET preview target not uniquely found"); return; }
            WalletPreviewBinding binding = WalletPreviewBinding.bind(methods.get(0), methods.get(1).getDeclaringClass());
            if (binding == null) return;
            hooks.install(binding.invoke, chain -> {
                if (!FeatureConfig.enabled(FeaturePolicy.WALLET)) return chain.proceed();
                Object request = chain.getThisObject();
                Activity activity = request == null ? null : (Activity)binding.activity.get(request);
                if (activity == null || !WalletPowerPolicy.isQuickDrawPreview(activity.getClass().getName(),
                        activity.getIntent() == null ? null : activity.getIntent().getAction())) return chain.proceed();
                KeyguardManager keyguard = (KeyguardManager)binding.keyguard.get(request);
                if (keyguard == null || !keyguard.isDeviceLocked()) return chain.proceed();
                // No authentication-success callback; the wallet's tap flow remains original.
                return binding.unit;
            });
            // "Open Wallet" starts the full app straight away, which then waits behind the lock
            // screen (the screen's own lock request was skipped above). Ask for the lock first.
            hooks.install(Activity.class.getDeclaredMethod("startActivity", android.content.Intent.class, android.os.Bundle.class), chain -> {
                Activity from = (Activity) chain.getThisObject();
                android.content.Intent intent = (android.content.Intent) chain.getArg(0);
                if (unlocking || intent == null || intent.getComponent() == null
                        || !WalletPowerPolicy.WALLET_PACKAGE.equals(intent.getComponent().getPackageName())
                        || !WalletPowerPolicy.isQuickDrawPreview(from.getClass().getName(),
                        from.getIntent() == null ? null : from.getIntent().getAction())) return chain.proceed();
                KeyguardManager keyguard = from.getSystemService(KeyguardManager.class);
                if (keyguard == null || !keyguard.isDeviceLocked()) return chain.proceed();
                android.os.Bundle options = (android.os.Bundle) chain.getArg(1);
                keyguard.requestDismissKeyguard(from, new KeyguardManager.KeyguardDismissCallback() {
                    @Override public void onDismissSucceeded() {
                        unlocking = true;
                        try { from.startActivity(intent, options); } finally { unlocking = false; }
                    }
                });
                return null;
            });
            hooks.info("WALLET dynamic QUICKDRAW preview hook installed");
        } catch (Throwable failure) {
            hooks.unhookSince(mark);
            hooks.error("WALLET preview lookup failed; original behavior retained", failure);
        }
    }
    private WalletPreviewHooks() {}
}
