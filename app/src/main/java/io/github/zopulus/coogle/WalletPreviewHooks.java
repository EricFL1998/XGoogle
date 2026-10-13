package io.github.zopulus.coogle;

import android.app.Activity;
import android.app.KeyguardManager;

/** Resolves repackaged Kotlin code while retaining the verified QUICKDRAW preview boundary. */
final class WalletPreviewHooks {
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
            hooks.info("WALLET dynamic QUICKDRAW preview hook installed");
        } catch (Throwable failure) {
            hooks.unhookSince(mark);
            hooks.error("WALLET preview lookup failed; original behavior retained", failure);
        }
    }
    private WalletPreviewHooks() {}
}
