package io.github.leekin2.xgoogle;
import android.content.SharedPreferences;
import java.lang.reflect.*;
import java.util.*;
import io.github.libxposed.api.XposedInterface;
import io.github.leekin2.xgoogle.migration.compat.ModernHookRuntime;
import android.app.ApplicationPackageManager;
public class FeatureQueryTest {
 public static void main(String[] args)throws Throwable {
  List<String> reads=new ArrayList<>();
  FeatureConfig.attach((SharedPreferences)Proxy.newProxyInstance(SharedPreferences.class.getClassLoader(),new Class<?>[]{SharedPreferences.class},(p,m,a)->{reads.add((String)a[0]);return true;}));
  check(FeatureConfig.enabled(FeaturePolicy.NAVIGATION),"Navigation disabled");
  check(reads.equals(List.of("navigation")),"Unrelated feature read companion state: "+reads);
  reads.clear();check(FeatureConfig.enabled(FeaturePolicy.HEY_GOOGLE),"Hotword disabled");
  check(reads.equals(List.of("hey_google","companion_ready")),"Companion requirement removed");
  Class<?> owner=Class.forName("io.github.leekin2.xgoogle.migration.AssistantFeatureHooks");
  owner.getMethod("hookContextualSearchSystemFeature",String.class,ClassLoader.class).invoke(owner.getField("INSTANCE").get(null),"com.android.launcher",FeatureQueryTest.class.getClassLoader());
  Method method=ApplicationPackageManager.class.getMethod("hasSystemFeature",String.class,int.class);
  var hook=ModernHookRuntime.INSTANCE.module.callbacks.get(method);check(hook!=null,"Feature query hook absent");
  reads.clear();invoke(hook,method,"other.feature",1);check(reads.isEmpty(),"Unrelated system query read preferences");
  reads.clear();invoke(hook,method,"com.google.android.feature.CONTEXTUAL_SEARCH",0);check(reads.equals(List.of("navigation")),"Circle configuration not read precisely");
  System.out.println("PASS: companion state read only for hotword; unrelated feature queries skip preferences");
 }
 static void invoke(XposedInterface.Hooker hook,Method method,String name,int expected)throws Throwable{
  int[] proceeded={0};
  var chain=(XposedInterface.Chain)Proxy.newProxyInstance(XposedInterface.Chain.class.getClassLoader(),new Class<?>[]{XposedInterface.Chain.class},(p,m,a)->switch(m.getName()){
   case "getExecutable"->method;case "getThisObject"->new ApplicationPackageManager();case "getArgs"->List.of(name,0);
   case "proceed"->{proceeded[0]++;yield false;}default->throw new AssertionError(m.getName());});
  hook.intercept(chain);check(proceeded[0]==expected,"Wrong native dispatch");
 }
 static void check(boolean yes,String message){if(!yes)throw new AssertionError(message);}
}
