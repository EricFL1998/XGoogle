package io.github.leekin2.xgoogle;

import java.util.Arrays;

public final class NativeQuickEntryPolicyTest {
    public static void main(String[] args) {
        String[] cn = {"pictorial", "flashlight", "none"};
        String[] expected = {"pictorial", "flashlight", "assistant", "wallet", "qr_code_scanner", "home", "none"};
        if (!Arrays.equals(expected, NativeQuickEntryPolicy.completeLockscreenEntries(cn))) throw new AssertionError("CN entries");
        if (!Arrays.equals(expected, NativeQuickEntryPolicy.completeLockscreenEntries(expected))) throw new AssertionError("Idempotence");
        if (!Arrays.equals(cn, new String[]{"pictorial", "flashlight", "none"})) throw new AssertionError("Input mutated");
        String[] custom = NativeQuickEntryPolicy.completeLockscreenEntries(new String[]{"camera", "wallet"});
        if (!Arrays.equals(custom, new String[]{"camera", "wallet", "assistant", "qr_code_scanner", "home"})) throw new AssertionError("Preserve custom entries");
        System.out.println("NativeQuickEntryPolicyTest passed");
    }
}
