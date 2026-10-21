package io.github.leekin2.xgoogle;

public final class SystemCompatibilityTest {
    public static void main(String[] args) {
        for (String value : new String[]{"V17.0.0", "17.0", "17", "v17.1.123", " 17.0 "}) {
            if (!SystemCompatibility.matchesVersion(value)) throw new AssertionError(value);
        }
        for (String value : new String[]{"V16.0.0", "18.0", "170.0", "Android 17", "", null}) {
            if (SystemCompatibility.matchesVersion(value)) throw new AssertionError(value);
        }
        System.out.println("ColorOS major version eligibility passed");
    }
}
