package io.github.zopulus.coogle;

import java.util.List;

/** The LSPosed scopes each switch works in on HyperOS (all of them are in scope.list). */
final class FeatureScopes {
    static final String SYSTEM = "system";
    private static final String GOOGLE = "com.google.android.googlequicksearchbox";
    private static final String SYSTEM_UI = "com.android.systemui";
    static List<String> required(String feature) {
        return switch (feature) {
            case FeaturePolicy.HEY_GOOGLE -> List.of(SYSTEM, GOOGLE);
            case FeaturePolicy.NAVIGATION -> List.of(SYSTEM, SYSTEM_UI, "com.android.launcher", "com.miui.voiceassist", GOOGLE);
            case FeaturePolicy.CIRCLE_VIBRATE -> List.of("com.miui.voiceassist");
            case FeaturePolicy.GOOGLE_ENTRY -> List.of("com.android.settings");
            case FeaturePolicy.GMS -> List.of(SYSTEM, SYSTEM_UI);
            case FeaturePolicy.LENS, FeaturePolicy.LIVE_TRANSLATE -> List.of(GOOGLE);
            case FeaturePolicy.CAMERA_LENS -> List.of(GOOGLE, "com.android.camera");
            case FeaturePolicy.WALLET -> List.of(SYSTEM, SYSTEM_UI, "com.miui.securitycore", "com.google.android.apps.walletnfcrel");
            case FeaturePolicy.QUICK_ENTRIES -> List.of(SYSTEM, SYSTEM_UI, "com.miui.aod");
            default -> List.of();
        };
    }
    private FeatureScopes() { }
}
