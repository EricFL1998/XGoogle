package io.github.zopulus.coogle;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import io.github.libxposed.service.XposedService;
import io.github.libxposed.service.XposedServiceHelper;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/** One process listener; activities are never retained by the framework service. */
final class SettingsConnection {
    private static WeakReference<MainActivity> activity = new WeakReference<>(null);
    private static XposedService service;
    private static boolean registered;

    /** Scopes added after the first release; LSPosed does not add them to existing installs. */
    private static final String[] LATER_SCOPES = {"com.miui.securitycore"};
    private static Context app;

    /** From the application: connect early so missing scopes are offered even without the UI. */
    static synchronized void start(Context context) {
        app = context.getApplicationContext();
        register();
    }

    static synchronized void connect(MainActivity value) {
        activity = new WeakReference<>(value);
        register();
        value.onFrameworkChanged(service);
    }

    private static void register() {
        if (!registered) {
            registered = true;
            XposedServiceHelper.registerListener(new XposedServiceHelper.OnServiceListener() {
                @Override public void onServiceBind(XposedService value) { update(value); }
                @Override public void onServiceDied(XposedService value) { update(null); }
            });
        }
    }

    private static synchronized void update(XposedService value) {
        service = value;
        if (value != null) requestLaterScopes(value);
        MainActivity target = activity.get();
        if (target != null) target.runOnUiThread(() -> target.onFrameworkChanged(value));
    }

    static synchronized void detach(MainActivity value) {
        if (activity.get() == value) activity.clear();
    }
    /** LSPosed shows a one-tap approval notice; asked once per version so a "no" is not repeated. */
    private static void requestLaterScopes(XposedService value) {
        try {
            if (app == null) return;
            List<String> scope = value.getScope();
            List<String> missing = new ArrayList<>();
            for (String s : LATER_SCOPES) if (!scope.contains(s)) missing.add(s);
            if (missing.isEmpty()) return;
            SharedPreferences prefs = app.getSharedPreferences("scope", Context.MODE_PRIVATE);
            long version = app.getPackageManager().getPackageInfo(app.getPackageName(), 0).getLongVersionCode();
            if (prefs.getLong("asked", 0) == version) return;
            prefs.edit().putLong("asked", version).apply();
            value.requestScope(missing, new XposedService.OnScopeEventListener() { });
        } catch (Throwable t) {
            Log.w("XGoogle", "Scope request failed", t);
        }
    }

    private SettingsConnection() { }
}
