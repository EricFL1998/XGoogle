package io.github.zopulus.coogle;

import android.content.Context;
import android.content.pm.PackageManager;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/** Exercises the compiled production hook, including its early-boot context transition. */
public final class HotwordUidTest {
    public static void main(String[] args) throws Exception {
        Class<?> hook = Class.forName("io.github.zopulus.coogle.migration.OkGoogleHotwordCompatibilityHooker");
        Object instance = hook.getField("INSTANCE").get(null);
        Field context = hook.getDeclaredField("systemContext");
        Field cache = hook.getDeclaredField("googleUidCache");
        Method lookup = hook.getDeclaredMethod("isGoogleUid", int.class);
        context.setAccessible(true);
        cache.setAccessible(true);
        lookup.setAccessible(true);
        ((Map<?, ?>) cache.get(null)).clear();
        context.set(null, null);
        if ((boolean) lookup.invoke(instance, 10001)) throw new AssertionError("Unknown UID granted before context");
        final int[] calls = {0};
        PackageManager packages = new PackageManager() {
            @Override public String[] getPackagesForUid(int uid) {
                calls[0]++;
                return new String[]{uid == 10001 ? "com.google.android.googlequicksearchbox" : "com.example.other"};
            }
        };
        context.set(null, new Context() {
            @Override public PackageManager getPackageManager() { return packages; }
        });
        if (!(boolean) lookup.invoke(instance, 10001)) throw new AssertionError("Early-boot failure permanently cached");
        if (!(boolean) lookup.invoke(instance, 10001)) throw new AssertionError("Verified Google UID rejected");
        if ((boolean) lookup.invoke(instance, 10002)) throw new AssertionError("Other app granted hotword access");
        if ((boolean) lookup.invoke(instance, 10002)) throw new AssertionError("Other app cached incorrectly");
        if ((boolean) lookup.invoke(instance, -1)) throw new AssertionError("Invalid UID accepted");
        if (calls[0] != 2) throw new AssertionError("Resolved UID cache not used: " + calls[0]);
        Method bypass = hook.getDeclaredMethod("permissionToBypass", List.class, Object.class);
        bypass.setAccessible(true);
        // Framework logging is outside this JVM boundary; skip only its one-shot messages.
        for (String name : List.of("loggedFirstPackageBypass", "loggedFirstUidBypass")) {
            Field logged = hook.getDeclaredField(name);
            logged.setAccessible(true);
            ((AtomicBoolean) logged.get(null)).set(true);
        }
        Poison identity = new Poison();
        String manage = "android.permission.MANAGE_SOUND_TRIGGER";
        if (bypass.invoke(instance, List.of("android.permission.CAMERA", identity), null) != null)
            throw new AssertionError("Unrelated permission bypassed");
        if (identity.calls != 0) throw new AssertionError("Unrelated permission inspected identity");
        if (bypass.invoke(instance, List.of("android.permission.MANAGE_ASSISTANT_AUDIO", 10001), null) != null)
            throw new AssertionError("Excluded permission bypassed");
        if (!manage.equals(bypass.invoke(instance, List.of(manage, 10001), null)))
            throw new AssertionError("Google UID not granted");
        if (!manage.equals(bypass.invoke(instance, List.of(manage, "com.google.android.googlequicksearchbox", 0), null)))
            throw new AssertionError("Google package not granted");
        if (bypass.invoke(instance, List.of(manage, "com.example.other", 10002), null) != null)
            throw new AssertionError("Other app granted");
        for (String permission : List.of(manage, "android.permission.MANAGE_VOICE_KEYPHRASES",
                "android.permission.CAPTURE_AUDIO_HOTWORD", "android.permission.MANAGE_HOTWORD_DETECTION")) {
            if (!permission.equals(bypass.invoke(instance, List.of(permission, 10001), null)))
                throw new AssertionError("Supported permission rejected: " + permission);
        }
        if (!manage.equals(bypass.invoke(instance, List.of(manage, new GoogleIdentity()), null)))
            throw new AssertionError("Package getter identity rejected");
        if (!manage.equals(bypass.invoke(instance, List.of(manage, new GoogleFieldIdentity()), null)))
            throw new AssertionError("Package field identity rejected");
        System.out.println("PASS: hotword UID lookup recovers after context becomes available; other apps remain rejected");
    }
    public static final class Poison {
        int calls;
        public String getPackageName() { calls++; throw new AssertionError("Unexpected identity inspection"); }
    }
    public static final class GoogleIdentity {
        public String getPackageName() { return "com.google.android.googlequicksearchbox"; }
    }
    public static final class GoogleFieldIdentity {
        public String packageName = "com.google.android.googlequicksearchbox";
    }
}
