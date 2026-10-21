package io.github.leekin2.xgoogle;
import android.content.Context;
import io.github.leekin2.xgoogle.migration.AssistantRuntime;
import java.lang.reflect.*;
import java.util.Map;
public class ContextBindingCacheTest {
 static class Base { Context context; Base(Context value){context=value;} }
 static class Owner extends Base {Owner(Context value){super(value);} }
 static class Outer { Object this$0; Outer(Object value){this$0=value;} }
 public static void main(String[] args)throws Exception{
  var runtime=AssistantRuntime.INSTANCE; runtime.setSystemContext(null);
  Context first=new Context(),second=new Context();Owner source=new Owner(first);
  check(runtime.findContext(source)==first,"Initial context lookup failed");
  Field cacheField=AssistantRuntime.class.getDeclaredField("contextFieldCache");cacheField.setAccessible(true);
  Map<?,?> cache=(Map<?,?>)cacheField.get(runtime);
  check(cache.containsKey(Owner.class),"Direct context binding was not cached");
  source.context=second;check(runtime.findContext(source)==second,"Mutable context value cached");
  source.context=null;check(runtime.findContext(source)==null,"Null field returned stale context");
  source.context=first;check(runtime.findContext(new Outer(source))==first,"Outer-object fallback lost");
  System.out.println("PASS: cached inherited field reads current instance and preserves null/outer fallbacks");
 }
 static void check(boolean yes,String message){if(!yes)throw new AssertionError(message);}
}
