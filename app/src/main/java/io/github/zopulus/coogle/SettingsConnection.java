package io.github.zopulus.coogle;

import io.github.libxposed.service.XposedService;
import io.github.libxposed.service.XposedServiceHelper;
import java.lang.ref.WeakReference;

/** One process listener; activities are never retained by the framework service. */
final class SettingsConnection {
    private static WeakReference<MainActivity> activity = new WeakReference<>(null);
    private static XposedService service;
    private static boolean registered;

    static synchronized void connect(MainActivity value) {
        activity = new WeakReference<>(value);
        if (!registered) {
            registered = true;
            XposedServiceHelper.registerListener(new XposedServiceHelper.OnServiceListener() {
                @Override public void onServiceBind(XposedService value) { update(value); }
                @Override public void onServiceDied(XposedService value) { update(null); }
            });
        }
        value.onFrameworkChanged(service);
    }

    private static synchronized void update(XposedService value) {
        service = value;
        MainActivity target = activity.get();
        if (target != null) target.runOnUiThread(() -> target.onFrameworkChanged(value));
    }

    static synchronized void detach(MainActivity value) {
        if (activity.get() == value) activity.clear();
    }
    private SettingsConnection() { }
}
