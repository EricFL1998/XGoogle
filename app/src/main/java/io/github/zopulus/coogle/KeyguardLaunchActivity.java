package io.github.zopulus.coogle;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * HyperOS lock screen shortcuts expect the opened screen to show over the lock screen, and say
 * "Couldn't launch app" when it does not. Google Assistant and Lens never show over it. This
 * invisible screen does; Lens then opens over the lock screen, the Assistant (Gemini), Wallet
 * and Google Home after the lock.
 */
public final class KeyguardLaunchActivity extends Activity {
    private static final String GOOGLE = "com.google.android.googlequicksearchbox";
    private static final String SHOW_ASSISTANT = "io.github.leekin2.xgoogle.SHOW_ASSISTANT";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        Intent target = target(getIntent().getData());
        if (target == null) { finish(); return; }
        KeyguardManager keyguard = getSystemService(KeyguardManager.class);
        if (keyguard == null || !keyguard.isKeyguardLocked()) { open(target); return; }
        // Lens shows over the lock screen (XGoogle marks it in the Google app), like a camera.
        if (Intent.ACTION_VIEW.equals(target.getAction())) {
            open(target.putExtra(LensHooks.KEYGUARD_EXTRA, true));
            return;
        }
        keyguard.requestDismissKeyguard(this, new KeyguardManager.KeyguardDismissCallback() {
            @Override public void onDismissSucceeded() { open(target); }
            @Override public void onDismissCancelled() { finish(); }
            @Override public void onDismissError() { finish(); }
        });
    }

    /**
     * Gemini's floating panel, as a long press of the power key opens it: only SystemUI can start
     * that session, so ask XGoogle's SystemUI hook. Without it, open the Gemini app (or the Google
     * app's assistant screen when Gemini is not installed).
     */
    private void showAssistant() {
        Intent ask = new Intent(SHOW_ASSISTANT).setPackage("com.android.systemui");
        sendOrderedBroadcast(ask, null, new android.content.BroadcastReceiver() {
            @Override public void onReceive(android.content.Context context, Intent intent) {
                if (getResultCode() != 1) {
                    Intent gemini = getPackageManager().getLaunchIntentForPackage("com.google.android.apps.bard");
                    Intent app = gemini != null ? gemini : new Intent(Intent.ACTION_ASSIST).setPackage(GOOGLE);
                    try { startActivity(app.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); }
                    catch (Exception e) { Log.w("XGoogle", "Assistant unavailable", e); }
                }
                finish();
            }
        }, null, 0, null, null);
    }

    private Intent target(Uri data) {
        String which = data == null ? null : data.getLastPathSegment();
        // Google Home's controls loop when HyperOS shows them over the lock screen: open the app
        // after the lock instead.
        if ("home".equals(which)) return getPackageManager().getLaunchIntentForPackage("com.google.android.apps.chromecast.app");
        if ("assistant".equals(which)) return new Intent(SHOW_ASSISTANT);
        // Wallet's tap-to-pay screen waits behind the lock screen: ask for the lock first.
        if ("wallet".equals(which)) return new Intent(WalletPowerPolicy.QUICKDRAW_ACTION)
                .setClassName(WalletPowerPolicy.WALLET_PACKAGE, WalletPowerPolicy.QUICKDRAW_ACTIVITY);
        if ("lens".equals(which)) return new Intent(Intent.ACTION_VIEW, Uri.parse("googleapp://lens")).setPackage(GOOGLE);
        return null;
    }

    private void open(Intent target) {
        if (SHOW_ASSISTANT.equals(target.getAction())) { showAssistant(); return; }
        try {
            startActivity(target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Exception e) {
            Log.w("XGoogle", "Lock screen shortcut target unavailable", e);
        }
        finish();
    }
}
