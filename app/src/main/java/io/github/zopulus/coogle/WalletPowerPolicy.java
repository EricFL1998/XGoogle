package io.github.zopulus.coogle;

/** Boundaries for native wallet routing and locked card preview. */
public final class WalletPowerPolicy {
    private WalletPowerPolicy() { }

    public static boolean isQuickDrawPreview(String activityName, String action) {
        return "com.google.android.apps.wallet.pmode.PresentationModeActivity".equals(activityName)
                && "com.google.android.apps.wallet.main.QUICKDRAW".equals(action);
    }

    public static boolean isGooglePayment(String paymentComponent) {
        return paymentComponent != null
                && paymentComponent.startsWith("com.google.android.gms/");
    }
}
