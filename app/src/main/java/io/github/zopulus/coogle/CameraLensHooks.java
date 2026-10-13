package io.github.zopulus.coogle;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import static io.github.zopulus.coogle.HookInstaller.target;

/**
 * Xiaomi's camera has a Google Lens button in its top bar, shown only on the global ROM: the gate
 * is "not a China build and Lens is installed". On the China ROM let the Google app count, so the
 * button appears; it opens google://lens, which LensHooks routes to Lens.
 */
final class CameraLensHooks {
    private static final String GOOGLE = "com.google.android.googlequicksearchbox";

    static void install(HookInstaller hooks, ClassLoader loader) {
        try {
            Method gate = target(loader, "Xr.m").getDeclaredMethod("b");
            // Do not apply this binding to a camera version whose layout changed.
            if (gate.getReturnType() != boolean.class || !Modifier.isStatic(gate.getModifiers()))
                throw new NoSuchMethodException("Camera Lens gate signature changed");
            hooks.untracked(gate, chain -> {
                Object shown = chain.proceed();
                if (Boolean.TRUE.equals(shown) || !FeatureConfig.enabled(FeaturePolicy.LENS)) return shown;
                return googleInstalled();
            });
            hooks.info("CAMERA Google Lens button enabled");
        } catch (Throwable error) {
            hooks.error("CAMERA Lens button unavailable for this camera version", error);
        }
    }

    private static boolean googleInstalled() {
        try {
            android.content.Context context = (android.content.Context) Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication").invoke(null);
            context.getPackageManager().getPackageInfo(GOOGLE, 0);
            return true;
        } catch (Throwable missing) {
            return false;
        }
    }

    private CameraLensHooks() { }
}
