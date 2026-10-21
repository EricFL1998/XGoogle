package io.github.leekin2.xgoogle;
import android.content.Context;
import android.os.Handler;
import java.lang.reflect.Method;
import java.util.*;
public class RoleReconciliationTest {
    public static void main(String[] args) throws Exception {
        Class<?> type=Class.forName("io.github.leekin2.xgoogle.migration.AssistantVoiceHooks");
        Object owner=type.getField("INSTANCE").get(null);
        Method schedule=type.getDeclaredMethod("scheduleAssistantRoleReconciliation",Context.class,int.class);
        schedule.setAccessible(true);
        Context context=new Context();
        schedule.invoke(owner,context,0);
        schedule.invoke(owner,context,0);
        check(Handler.tasks.size()==4,"Same-user reconciliation tasks accumulated");
        Object firstToken=Handler.tasks.get(0).token();
        check(firstToken!=null,"Reconciliation tasks are not cancellable as a group");
        check(Handler.tasks.stream().map(Handler.Task::delay).toList().equals(List.of(1000L,5000L,15000L,30000L)),"Native retry timing changed");
        schedule.invoke(owner,context,10);
        check(Handler.tasks.size()==8,"One user removed another user's tasks");
        schedule.invoke(owner,context,0);
        check(Handler.tasks.size()==8,"Repeated user event expanded work");
        check(Handler.tasks.stream().filter(t->t.token()==firstToken).count()==4,"User token not reused");
        System.out.println("PASS: same-user delayed checks coalesced; users isolated and timing retained");
    }
    static void check(boolean yes,String message) { if(!yes)throw new AssertionError(message); }
}
