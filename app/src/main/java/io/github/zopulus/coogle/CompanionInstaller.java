package io.github.zopulus.coogle;

import android.content.Context;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import static io.github.zopulus.coogle.RootShell.run;

/** Root is used only after the user asks to check/install the companion. */
public final class CompanionInstaller {
    private static final String DIR="/data/adb/modules/coogle_companion";
    public static String check() throws Exception {
        String output = run("d="+DIR+"; u=/data/adb/modules_update/coogle_companion; "
                +"if [ -f \"$u/module.prop\" ]; then "
                +"if [ -f \"$u/disable\" ] || [ -f \"$u/remove\" ]; then echo DISABLED; else echo PENDING; fi; "
                +"elif [ ! -f \"$d/module.prop\" ]; then echo MISSING; "
                +"elif [ -f \"$d/disable\" ] || [ -f \"$d/remove\" ]; then echo DISABLED; "
                +"else echo READY; fi",20).trim();
        String state = output.substring(output.lastIndexOf('\n') + 1).trim();
        if (!java.util.Set.of("MISSING", "DISABLED", "PENDING", "READY").contains(state))
            throw new java.io.IOException("Ek modül durumu okunamadı");
        return state;
    }
    public static void install(Context context) throws Exception {
        Path zip=context.getCacheDir().toPath().resolve("coogle-companion.zip");
        try (InputStream source=context.getAssets().open("coogle-companion.zip")) { Files.copy(source,zip,java.nio.file.StandardCopyOption.REPLACE_EXISTING); }
        String quoted="'"+zip.toString().replace("'","'\\''")+"'";
        // Some KernelSU-Next / SukiSU builds keep ksud at /data/adb/ksud.
        run("if [ -x /data/adb/ksu/bin/ksud ]; then /data/adb/ksu/bin/ksud module install "+quoted+"; "
                +"elif [ -x /data/adb/ksud ]; then /data/adb/ksud module install "+quoted+"; "
                +"elif [ -x /data/adb/apd ]; then /data/adb/apd module install "+quoted+"; "
                +"else magisk --install-module "+quoted+"; fi",120);
    }
    /**
     * HyperOS: point the native double-press-power function at the default wallet
     * (launch_QAW_wallet), remembering the previous choice so turning it off restores it.
     */
    public static void applyHyperOsWalletPower(boolean enabled) throws Exception {
        String saved="/data/adb/coogle_double_click_power_key";
        if (enabled) run("cur=$(settings get system double_click_power_key); "
                +"[ \"$cur\" = launch_QAW_wallet ] || echo \"$cur\" > "+saved+" && "
                +"settings put system double_click_power_key launch_QAW_wallet",20);
        // Keep the saved choice until it is really back in place.
        else run("prev=$(cat "+saved+" 2>/dev/null); [ -n \"$prev\" ] && [ \"$prev\" != null ] || prev=none; "
                +"settings put system double_click_power_key \"$prev\" && rm -f "+saved,20);
    }
    /**
     * HyperOS: the companion routes the nav-handle long press to Google on every boot. While
     * Circle to Search is off, leave a marker so it stops, and put the user's own choice back.
     */
    public static void applyHyperOsNavigationKey(boolean enabled) throws Exception {
        String backup="/data/adb/coogle_keys_backup", off="/data/adb/coogle_nav_off";
        if (enabled) run("rm -f "+off+" && if [ -f "+backup+" ]; then "
                +"settings put system long_press_home_key launch_google_search; fi",20);
        else run("touch "+off+" && if [ -f "+backup+" ] && "
                +"[ \"$(settings get system long_press_home_key)\" = launch_google_search ]; then "
                +"home=$(sed -n 's/^home=//p' "+backup+"); "
                +"if [ -z \"$home\" ] || [ \"$home\" = null ]; then settings delete system long_press_home_key; "
                +"else settings put system long_press_home_key \"$home\"; fi; fi",20);
    }
    private CompanionInstaller() {}
}
