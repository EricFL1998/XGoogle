package io.github.libxposed.api;
import java.lang.reflect.*;
import java.util.*;
public class XposedModule {
    public final Set<XposedInterface.HookHandle> active = new LinkedHashSet<>();
    public int attempts, failAt = -1;
    public final Map<Executable,XposedInterface.Hooker> callbacks=new HashMap<>();
    public XposedInterface.HookBuilder hook(Executable executable) {
        return (XposedInterface.HookBuilder)Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{XposedInterface.HookBuilder.class}, (builder, method, args) -> {
            if (!method.getName().equals("intercept")) return builder;
            if (++attempts==failAt) throw new IllegalStateException("injected installation failure");
            XposedInterface.HookHandle handle=(XposedInterface.HookHandle)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{XposedInterface.HookHandle.class},(h,m,a)->switch(m.getName()) {
                case "unhook" -> { active.remove(h); yield null; }
                case "getExecutable" -> executable;
                case "equals" -> h==a[0];
                case "hashCode" -> System.identityHashCode(h);
                default -> null;
            });
            active.add(handle); callbacks.put(executable,(XposedInterface.Hooker)args[0]); return handle;
        });
    }
}
