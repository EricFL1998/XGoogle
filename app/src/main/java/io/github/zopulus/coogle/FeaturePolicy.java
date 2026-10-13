package io.github.zopulus.coogle;

/** Defaults retain verified features; newly added integrations are opt-in. */
public final class FeaturePolicy {
    public static final String NAVIGATION="navigation", GMS="gms",
            GOOGLE_ENTRY="google_entry", SEARCH_BAR="search_bar", HEY_GOOGLE="hey_google", LENS="lens",
            WALLET="wallet", QUICK_ENTRIES="quick_entries", LIVE_TRANSLATE="live_translate",
            CIRCLE_VIBRATE="circle_vibrate", CAMERA_LENS="camera_lens";
    public static boolean defaultValue(String key) {
        return NAVIGATION.equals(key) || LENS.equals(key) || LIVE_TRANSLATE.equals(key) || GOOGLE_ENTRY.equals(key);
    }
    public static boolean effective(String key, boolean requested, boolean companionReady) {
        return requested && (!HEY_GOOGLE.equals(key) || companionReady);
    }
    private FeaturePolicy() {}
}
