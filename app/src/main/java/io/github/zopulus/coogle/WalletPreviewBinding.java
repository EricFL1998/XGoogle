package io.github.zopulus.coogle;

import android.app.Activity;
import android.app.KeyguardManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/** Validates the inspected coroutine layout without relying on repackaged Kotlin names. */
final class WalletPreviewBinding {
    final Method invoke;
    final Field activity;
    final Field keyguard;
    final Object unit;

    private WalletPreviewBinding(Method invoke, Field activity, Field keyguard, Object unit) {
        this.invoke = invoke; this.activity = activity; this.keyguard = keyguard; this.unit = unit;
    }

    static WalletPreviewBinding bind(Method invoke, Class<?> unitClass) throws IllegalAccessException {
        if (invoke.getReturnType() != Object.class || !java.util.Arrays.equals(invoke.getParameterTypes(), new Class<?>[]{Object.class})
                || Modifier.isStatic(invoke.getModifiers()) || invoke.isSynthetic()) return null;
        Class<?> owner = invoke.getDeclaringClass();
        boolean shape = false;
        for (Constructor<?> constructor : owner.getDeclaredConstructors()) {
            Class<?>[] p = constructor.getParameterTypes();
            if (p.length == 5 && p[0] == KeyguardManager.class && p[1] == Activity.class
                    && p[2] == p[3] && !p[2].isPrimitive() && !p[4].isPrimitive()) shape = true;
        }
        if (!shape) return null;
        Field activity = uniqueField(owner, Activity.class, false);
        Field keyguard = uniqueField(owner, KeyguardManager.class, false);
        Field singleton = uniqueField(unitClass, unitClass, true);
        if (activity == null || keyguard == null || singleton == null) return null;
        Object unit = singleton.get(null);
        return unit == null ? null : new WalletPreviewBinding(invoke, activity, keyguard, unit);
    }

    private static Field uniqueField(Class<?> owner, Class<?> type, boolean isStatic) {
        Field found = null;
        for (Field field : owner.getDeclaredFields()) {
            if (field.getType() != type || Modifier.isStatic(field.getModifiers()) != isStatic) continue;
            if (found != null) return null;
            found = field;
        }
        if (found != null) found.setAccessible(true);
        return found;
    }
}
