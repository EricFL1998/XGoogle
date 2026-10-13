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
 * invisible screen does; Lens then opens over the lock screen, the Assistant and Google Home
 * after the lock.
 */
public final class KeyguardLaunchActivity extends Activity {
    private static final String GOOGLE = "com.google.android.googlequicksearchbox";

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

    private Intent target(Uri data) {
        String which = data == null ? null : data.getLastPathSegment();
        // Google Home's controls loop when HyperOS shows them over the lock screen: open the app
        // after the lock instead.
        if ("home".equals(which)) return getPackageManager().getLaunchIntentForPackage("com.google.android.apps.chromecast.app");
        if ("assistant".equals(which)) return new Intent(Intent.ACTION_ASSIST).setPackage(GOOGLE);
        if ("lens".equals(which)) return new Intent(Intent.ACTION_VIEW, Uri.parse("googleapp://lens")).setPackage(GOOGLE);
        return null;
    }

    private void open(Intent target) {
        try {
            startActivity(target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Exception e) {
            Log.w("XGoogle", "Lock screen shortcut target unavailable", e);
        }
        finish();
    }
}
