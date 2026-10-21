package io.github.zopulus.coogle;

import android.content.SharedPreferences;
import java.util.Collections;
import java.util.Set;

public final class FeatureConfig {
    public static final String GROUP="coogle";
    private static volatile SharedPreferences preferences;
    public static void attach(SharedPreferences value) { preferences=value; }
    public static boolean enabled(String key) {
        SharedPreferences p=preferences;
        boolean requested=p==null ? FeaturePolicy.defaultValue(key) : p.getBoolean(key,FeaturePolicy.defaultValue(key));
        return FeaturePolicy.effective(key,requested,requested && FeaturePolicy.HEY_GOOGLE.equals(key)
                && p!=null && p.getBoolean("companion_ready",false));
    }
    /** A saved package list; empty when unset or unreadable. */
    public static Set<String> packages(String key) {
        SharedPreferences p=preferences;
        try { Set<String> value=p==null ? null : p.getStringSet(key,null); return value==null ? Collections.emptySet() : value; }
        catch (RuntimeException unreadable) { return Collections.emptySet(); }
    }
    private FeatureConfig() {}
}
