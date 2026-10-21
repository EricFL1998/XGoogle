package io.github.leekin2.xgoogle;
import io.github.libxposed.api.XposedInterface;
import io.github.leekin2.xgoogle.migration.compat.*;
import java.lang.reflect.*;
import kotlin.jvm.functions.Function0;
public class HookInstallRollbackTest {
    public static class Target {
        public void hook() {} public void hook(int x) {} public void other() {}
        public Target() {} public Target(int x) {}
    }
    public static void main(String[] args) throws Exception {
        var bridge=ModernHookBridge.INSTANCE;
        var module=ModernHookRuntime.INSTANCE.module;
        var callback=new ModernMethodHook() {};
        var unrelated=bridge.hookMethod(Target.class.getMethod("other"),callback);
        module.failAt=module.attempts+2;
        try { bridge.hookAllMethods(Target.class,"hook",callback); throw new AssertionError("Failure hidden"); }
        catch (IllegalStateException expected) {}
        check(module.active.size()==1 && module.active.contains(unrelated),"Partial overload hook retained");
        module.failAt=-1;
        var handles=bridge.hookAllMethods(Target.class,"hook",callback);
        check(handles.size()==2 && module.active.size()==3,"Retry duplicated hooks");
        handles.forEach(XposedInterface.HookHandle::unhook);
        module.failAt=module.attempts+2;
        try { bridge.hookAllConstructors(Target.class,callback); throw new AssertionError("Constructor failure hidden"); }
        catch (IllegalStateException expected) {}
        check(module.active.size()==1,"Partial constructor hook retained");
        module.failAt=-1;
        Method atomic=ModernHookBridge.class.getMethod("atomic",Function0.class);
        Function0<Object> stages=()-> {
            bridge.hookMethod(get("hook"),callback);
            bridge.hookAllMethods(Target.class,"hook",callback);
            throw new IllegalStateException("Later stage unavailable");
        };
        try { atomic.invoke(bridge,stages); throw new AssertionError("Stage failure hidden"); }
        catch (InvocationTargetException expected) { check(expected.getCause() instanceof IllegalStateException,"Wrong failure preserved"); }
        check(module.active.size()==1,"Nested multi-stage installation retained partial hooks");
        System.out.println("PASS: overload/constructor/nested-stage rollback preserves unrelated hooks and clean retry");
    }
    static Method get(String name) { try { return Target.class.getMethod(name); } catch(Exception e) { throw new AssertionError(e); } }
    static void check(boolean yes,String message) { if(!yes)throw new AssertionError(message); }
}
