package io.github.zopulus.coogle;
import io.github.libxposed.api.XposedInterface;
import java.lang.reflect.Executable;
public class HookInstaller {
    public int installed;
    public XposedInterface.Hooker callback;
    public void install(Executable method,XposedInterface.Hooker hook) { installed++; callback=hook; }
    public void info(String message) {}
    public void error(String message,Throwable failure) {}
}
