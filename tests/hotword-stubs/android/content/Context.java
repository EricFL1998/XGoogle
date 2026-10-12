package android.content;

import android.content.pm.PackageManager;

/** JVM test boundary; never included in the APK. */
public abstract class Context {
    public abstract PackageManager getPackageManager();
}
