package android.content.pm;

/** JVM test boundary; never included in the APK. */
public abstract class PackageManager {
    public abstract String[] getPackagesForUid(int uid);
}
