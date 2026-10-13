package io.github.zopulus.coogle;

import android.app.Application;

/** Connects to LSPosed as soon as the app starts, so new scopes are offered without the UI. */
public final class XGoogleApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SettingsConnection.start(this);
    }
}
