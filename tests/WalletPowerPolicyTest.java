package io.github.leekin2.xgoogle;

public final class WalletPowerPolicyTest {
    public static void main(String[] args) {
        check(WalletPowerPolicy.isGooglePayment("com.google.android.gms/com.google.android.gms.tapandpay.hce.service.TpHceService"), "Google payment");
        check(!WalletPowerPolicy.isGooglePayment(null), "unset payment");
        check(!WalletPowerPolicy.isGooglePayment("other/com.google.android.gms"), "other payment package");
        check(!WalletPowerPolicy.isGooglePayment("com.google.android.gms.fake/a"), "similar package name");
        String preview = "com.google.android.apps.wallet.pmode.PresentationModeActivity";
        String action = "com.google.android.apps.wallet.main.QUICKDRAW";
        check(WalletPowerPolicy.isQuickDrawPreview(preview, action), "quick card preview");
        check(!WalletPowerPolicy.isQuickDrawPreview(preview, "com.google.android.apps.wallet.main.TAP_EVENT"), "NFC payment retains authentication");
        check(!WalletPowerPolicy.isQuickDrawPreview("other.Activity", action), "other activity retains authentication");
        check(!WalletPowerPolicy.isQuickDrawPreview(preview, null), "missing action retains authentication");
        System.out.println("WalletPowerPolicyTest passed");
    }
    private static void check(boolean passed, String message) {
        if (!passed) throw new AssertionError(message);
    }
}