package io.github.zopulus.coogle.migration.compat;
import io.github.libxposed.api.XposedModule;
public class ModernHookRuntime {
    public static final ModernHookRuntime INSTANCE=new ModernHookRuntime();
    public final XposedModule module=new XposedModule();
    public XposedModule requireModule() { return module; }
    public void log(int priority,String tag,String message,Throwable failure) {}
}
