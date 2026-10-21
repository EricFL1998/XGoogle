package io.github.leekin2.xgoogle;
import android.content.SharedPreferences;
import io.github.libxposed.api.XposedInterface;
import java.lang.reflect.*;
public class SignalModelHookLifecycleTest {
    public static void main(String[] args) throws Throwable {
        boolean[] requested={false}, ready={false};
        FeatureConfig.attach((SharedPreferences)Proxy.newProxyInstance(SharedPreferences.class.getClassLoader(),new Class<?>[]{SharedPreferences.class},(p,m,a)-> {
            if(m.getName().equals("getBoolean")) return a[0].equals("companion_ready") ? ready[0] : requested[0];
            throw new AssertionError("Unexpected preference access");
        }));
        HookInstaller hooks=new HookInstaller();
        SignalModelHooks.install(hooks,SignalModelHookLifecycleTest.class.getClassLoader());
        check(hooks.installed==0,"Disabled feature installed model hook");
        requested[0]=true;
        SignalModelHooks.install(hooks,SignalModelHookLifecycleTest.class.getClassLoader());
        check(hooks.installed==0,"Missing companion installed model hook");
        ready[0]=true;
        SignalModelHooks.install(hooks,SignalModelHookLifecycleTest.class.getClassLoader());
        check(hooks.installed==1,"Enabled feature did not install model hook");
        requested[0]=false;
        Object expected=new Object(); int[] proceeds={0};
        XposedInterface.Chain chain=(XposedInterface.Chain)Proxy.newProxyInstance(XposedInterface.Chain.class.getClassLoader(),new Class<?>[]{XposedInterface.Chain.class},(p,m,a)-> {
            if(m.getName().equals("proceed")) { proceeds[0]++; return expected; }
            throw new AssertionError("Disabled model hook inspected arguments: "+m.getName());
        });
        check(hooks.callback.intercept(chain)==expected && proceeds[0]==1,"Disabled hook altered native outcome");
        System.out.println("PASS: model hook disabled-at-start, companion gate and immediate disable bypass");
    }
    static void check(boolean yes,String message) { if(!yes)throw new AssertionError(message); }
}
