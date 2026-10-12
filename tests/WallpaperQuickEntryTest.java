package io.github.zopulus.coogle;
import android.content.Context;
import io.github.zopulus.coogle.migration.compat.ModernHookRegistry;
import io.github.zopulus.coogle.migration.compat.ModernMethodHook.MethodHookParam;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/** Exercises compiled production binding and callbacks with an injected install failure. */
public class WallpaperQuickEntryTest {
    public static void main(String[] args) throws Exception {
        Class<?> type=Class.forName("io.github.zopulus.coogle.migration.WallpaperQuickEntryHooks");
        Object hook=type.getField("INSTANCE").get(null);
        Method bind=type.getDeclaredMethod("bind",ClassLoader.class); bind.setAccessible(true);
        ClassLoader loader=WallpaperQuickEntryTest.class.getClassLoader();
        ModernHookRegistry.failList=true;
        check(!(boolean)bind.invoke(hook,loader),"Injected failure accepted");
        check(ModernHookRegistry.hooks.isEmpty(),"Partial hook retained after failure");
        ModernHookRegistry.failList=false;
        check((boolean)bind.invoke(hook,loader),"Retry failed");
        int count=ModernHookRegistry.installs;
        check((boolean)bind.invoke(hook,loader),"Already bound rejected");
        check(count==ModernHookRegistry.installs,"Duplicate hooks installed");
        Context context=new Context(); b.j callback=new b.j();
        d1.c manager=new d1.c(context,context,callback);
        var constructor=d1.c.class.getConstructor(Context.class,Context.class,b.j.class);
        ModernHookRegistry.hooks.get("quick-entry:clock-editor").afterHookedMethod(
            new MethodHookParam(constructor,manager,new Object[]{context,context,callback}));
        Method invoke=b.j.class.getMethod("invoke",Object.class);
        List<d1.a> original=new ArrayList<>(List.of(entry("flashlight"),entry("add")));
        var param=new MethodHookParam(invoke,callback,new Object[]{original});
        var listHook=ModernHookRegistry.hooks.get("quick-entry:clock-list");
        listHook.beforeHookedMethod(param);
        @SuppressWarnings("unchecked") List<d1.a> completed=(List<d1.a>)param.getArgs()[0];
        check(completed.stream().map(x->x.a).toList().equals(
            List.of("flashlight","assistant","wallet","qr_code_scanner","home","add")),"Native row order changed");
        check(original.size()==2,"Original list mutated");
        check(!completed.get(2).enabled && !completed.get(4).enabled,"Native provider eligibility ignored");
        check(completed.get(1).icon.equals("affordance_digital_assistant_icon"),"Native Gemini icon lost");
        int lookups=context.getResources().lookups, packages=i5.d.calls;
        var completeParam=new MethodHookParam(invoke,callback,new Object[]{completed});
        listHook.beforeHookedMethod(completeParam);
        check(completeParam.getArgs()[0]==completed,"Already complete list copied");
        check(lookups==context.getResources().lookups && packages==i5.d.calls,"Complete list repeated resource/package work");
        context.getResources().locale="en";
        var refresh=new MethodHookParam(invoke,callback,new Object[]{original});
        listHook.beforeHookedMethod(refresh);
        check(lookups==context.getResources().lookups,"Resource IDs not cached");
        check(((d1.a)((List<?>)refresh.getArgs()[0]).get(1)).title.startsWith("en"),"Localized text cached incorrectly");
        System.out.println("PASS: wallpaper rollback, retry, idempotence, native rows and resource cache");
    }
    private static d1.a entry(String id) { return new d1.a(id,id,0,"","","",true,true,0); }
    private static void check(boolean ok,String message) { if(!ok)throw new AssertionError(message); }
}
