package io.github.leekin2.xgoogle;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.*;
import android.provider.Settings;
import android.app.contextualsearch.*;
import com.android.systemui.assist.AssistManager;
import io.github.libxposed.api.XposedInterface;
import io.github.leekin2.xgoogle.migration.AssistantRuntime;
import io.github.leekin2.xgoogle.migration.compat.ModernHookRuntime;
import java.lang.reflect.*;
import java.util.List;

/** Exercise the installed production AssistManager callback, not a copied policy. */
public class HiddenCircleNoAssistantTest {
    public static class SearchService implements IBinder,IContextualSearchManager {
        int calls;
        public void startContextualSearch(int entry,ContextualSearchConfig config) {
            check(entry==1 && config==null,"Native contextual defaults changed");
            calls++;
        }
    }
    public static void main(String[] args) throws Throwable {
        boolean[] navigation={true};
        FeatureConfig.attach((SharedPreferences)Proxy.newProxyInstance(SharedPreferences.class.getClassLoader(),new Class<?>[]{SharedPreferences.class},
            (p,m,a)->m.getName().equals("getBoolean") ? navigation[0] : null));
        AssistantRuntime.INSTANCE.setSystemContext(new Context());
        Class<?> owner=Class.forName("io.github.leekin2.xgoogle.migration.AssistantSystemUiHooks");
        Method install=owner.getDeclaredMethod("hookNativeAssistantSystemUiGate",ClassLoader.class);
        install.setAccessible(true);
        install.invoke(owner.getField("INSTANCE").get(null),HiddenCircleNoAssistantTest.class.getClassLoader());
        Method method=AssistManager.class.getMethod("startAssist$1",Bundle.class);
        XposedInterface.Hooker hook=ModernHookRuntime.INSTANCE.module.callbacks.get(method);
        check(hook!=null,"Native AssistManager hook not installed");
        AssistManager manager=new AssistManager();
        SearchService search=new SearchService();
        ServiceManager.current=search;
        invoke(hook,method,manager,8,0);
        check(search.calls==1,"Missing assistant prevented hidden circle invocation");
        invoke(hook,method,manager,1000,0);
        check(search.calls==2,"Alternate circle invocation was not dispatched");
        manager.assistant=new Object(); invoke(hook,method,manager,8,0);
        check(search.calls==3,"Selected assistant did not use unified search path");
        manager.assistant=null; manager.mActivityManager.lockTaskMode=1;
        invoke(hook,method,manager,8,1); check(search.calls==3,"Lock task bypassed");
        manager.mActivityManager.lockTaskMode=0; Settings.Secure.circle=0;
        invoke(hook,method,manager,8,1); check(search.calls==3,"Disabled circle invoked");
        Settings.Secure.circle=1; navigation[0]=false;
        invoke(hook,method,manager,8,1); check(search.calls==3,"Disabled module invoked");
        navigation[0]=true; invoke(hook,method,manager,1,1);
        check(search.calls==3,"Corner assistant was redirected to search");
        ServiceManager.current=null; invoke(hook,method,manager,8,1);
        check(search.calls==3,"Unavailable service counted as success");
        for(String name:List.of("nativeAssistantRouteDepth","nativeAssistantRouteFrames")) {
            Field field=owner.getDeclaredField(name); field.setAccessible(true);
            check(((ThreadLocal<?>)field.get(owner.getField("INSTANCE").get(null))).get()==null,"Route state leaked: "+name);
        }
        check(Binder.cleared==Binder.restored,"Calling identity leaked");
        System.out.println("PASS: no-assistant circle 8/1000, selected assistant, lock task, switches, corner, missing service and route cleanup");
    }
    static void invoke(XposedInterface.Hooker hook,Method method,AssistManager manager,int type,int expectedProceed) throws Throwable {
        Bundle bundle=new Bundle(); bundle.putInt("invocation_type",type); int[] nativeCalls={0};
        XposedInterface.Chain chain=(XposedInterface.Chain)Proxy.newProxyInstance(XposedInterface.Chain.class.getClassLoader(),new Class<?>[]{XposedInterface.Chain.class},(p,m,a)->switch(m.getName()) {
            case "getExecutable" -> method; case "getThisObject" -> manager; case "getArgs" -> List.of(bundle);
            case "proceed" -> {nativeCalls[0]++;yield null;}
            default -> throw new AssertionError("Unexpected chain access: "+m.getName());
        });
        hook.intercept(chain);
        check(nativeCalls[0]==expectedProceed,"Unexpected native dispatch for type="+type+": "+nativeCalls[0]);
    }
    static void check(boolean yes,String message){if(!yes)throw new AssertionError(message);}
}
