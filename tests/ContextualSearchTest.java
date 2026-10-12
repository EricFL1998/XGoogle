package io.github.zopulus.coogle;

import android.app.contextualsearch.ContextualSearchConfig;
import android.app.contextualsearch.IContextualSearchManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.ServiceManager;
import java.lang.reflect.Method;

/** Compiled production dispatch with a replaceable framework Binder boundary. */
public final class ContextualSearchTest {
    private static final class Service implements IContextualSearchManager, IBinder {
        int calls;
        boolean fail;
        @Override public void startContextualSearch(int entrypoint, ContextualSearchConfig config) {
            if (entrypoint != 1 || config != null) throw new AssertionError("Native defaults changed");
            if (fail) throw new IllegalStateException("Dead service");
            calls++;
        }
    }
    public static void main(String[] args) throws Exception {
        Class<?> type = Class.forName("io.github.zopulus.coogle.migration.ContextualSearchHooks");
        Object hook = type.getField("INSTANCE").get(null);
        Method dispatch = type.getDeclaredMethod("triggerCircleToSearchC17");
        check(!(boolean) dispatch.invoke(hook), "Missing service accepted");
        Service first = new Service();
        ServiceManager.current = first;
        check((boolean) dispatch.invoke(hook), "Framework interface dispatch failed");
        check(first.calls == 1, "Wrong service called");
        Service second = new Service();
        ServiceManager.current = second;
        check((boolean) dispatch.invoke(hook), "Replacement Binder rejected");
        check(first.calls == 1 && second.calls == 1, "Stale Binder reused");
        second.fail = true;
        check(!(boolean) dispatch.invoke(hook), "Service failure hidden");
        second.fail = false;
        check((boolean) dispatch.invoke(hook), "Recovery after service failure failed");
        check(ServiceManager.reads == 5, "Service lookup was cached");
        check(Binder.cleared == 5 && Binder.restored == 5, "Caller identity not restored");
        System.out.println("PASS: contextual search native defaults, fresh Binder, failure recovery and identity restoration");
    }
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
