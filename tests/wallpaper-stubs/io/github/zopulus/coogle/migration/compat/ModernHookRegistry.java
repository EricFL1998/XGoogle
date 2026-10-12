package io.github.zopulus.coogle.migration.compat;
import java.lang.reflect.Executable;
import java.util.LinkedHashMap;
import java.util.Map;
import io.github.libxposed.api.XposedInterface;
/** Fault-injection boundary; never packaged in the APK. */
public class ModernHookRegistry {
    public static final ModernHookRegistry INSTANCE = new ModernHookRegistry();
    public static final Map<String,ModernMethodHook> hooks = new LinkedHashMap<>();
    public static boolean failList;
    public static int installs;
    public XposedInterface.HookHandle installCompat(String key,Executable target,ModernMethodHook hook) {
        if (key.equals("quick-entry:clock-list") && failList) throw new IllegalStateException("Injected hook failure");
        installs++;
        hooks.putIfAbsent(key,hook);
        return null;
    }
    public boolean unhook(String key) { return hooks.remove(key)!=null; }
    public int unhookPrefix(String prefix) { int size=hooks.size(); hooks.keySet().removeIf(k->k.startsWith(prefix)); return size-hooks.size(); }
}
