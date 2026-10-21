package io.github.zopulus.coogle;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Lets Google Play services reach the network at any time, so push messages (FCM) arrive
 * without delay. The China ROM cuts GMS off in the background in two places: Battery and
 * performance (PowerKeeper) firewalls it and drops it from the doze allow list, and the
 * system's Greeze freezer defers or blocks its broadcasts. Ported from
 * Howard20181/HyperOS_FCM_Live (GPL-3.0) for XGoogle.
 */
final class GmsNetworkHooks {
    static final String POWER_KEEPER = "com.miui.powerkeeper";
    private static final String GMS = "com.google.android.gms", GMS_PERSISTENT = "com.google.android.gms.persistent";
    private static final String PUSH = "com.google.android.c2dm.intent.RECEIVE";
    private static final Set<String> DEFERRED = Set.of("com.google.android.intent.action.GCM_RECONNECT",
            "com.google.android.gcm.DISCONNECTED", "com.google.android.gcm.CONNECTED", "com.google.android.gms.gcm.HEARTBEAT_ALARM");
    private static Object exemptions;

    private static boolean on() { return FeatureConfig.enabled(FeaturePolicy.GMS_NETWORK); }

    /** PowerKeeper: no GMS firewall chain, no GMS limits, GMS on the doze allow list. */
    static void installPowerKeeper(HookInstaller hooks, ClassLoader loader) {
        int count = 0;
        try {
            Method chain = Class.forName("com.miui.powerkeeper.utils.NetdExecutor", false, loader)
                    .getDeclaredMethod("initGmsChain", String.class, int.class, String.class);
            hooks.install(chain, c -> {
                if (!on()) return c.proceed();
                Object[] args = c.getArgs().toArray();
                args[2] = "ACCEPT";
                return c.proceed(args);
            });
            hooks.deoptimize(chain);
            count++;
        } catch (Throwable error) {
            hooks.error("GMS_NETWORK: PowerKeeper firewall chain not found", error);
        }
        try {
            Class<?> observer = Class.forName("com.miui.powerkeeper.utils.GmsObserver", false, loader);
            for (String name : new String[]{"updateGmsAlarm", "updateGmsNetWork", "updateGoogleReletivesWakelock"}) {
                Method limit = observer.getDeclaredMethod(name, boolean.class);
                hooks.install(limit, c -> on() ? c.proceed(new Object[]{false}) : c.proceed());
                hooks.deoptimize(limit);
                count++;
            }
        } catch (Throwable error) {
            hooks.error("GMS_NETWORK: PowerKeeper GMS limits not found", error);
        }
        try {
            Method doze = Class.forName("com.miui.powerkeeper.provider.GlobalFeatureConfigureHelper", false, loader)
                    .getDeclaredMethod("getDozeWhiteListApps", Bundle.class);
            hooks.install(doze, c -> {
                Object result = c.proceed();
                if (on() && result instanceof List<?> list && !list.contains(GMS)) addTo(list, GMS);
                return result;
            });
            count++;
        } catch (Throwable error) {
            hooks.error("GMS_NETWORK: PowerKeeper doze list not found", error);
        }
        hooks.info("GMS_NETWORK: PowerKeeper lets GMS online (" + count + " hooks)");
    }

    /** System: Greeze lets GMS broadcasts through and keeps GMS off its black lists; pushes wake their app. */
    static void installSystem(HookInstaller hooks, ClassLoader loader) {
        String[] parts = {"greeze", "domestic", "lists", "autostart", "process", "network", "push"};
        int done = 0;
        for (String part : parts) {
            try {
                switch (part) {
                    case "greeze" -> greeze(hooks, loader);
                    case "domestic" -> {
                        Method defer = Class.forName("com.miui.server.greeze.DomesticPolicyManager", false, loader)
                                .getDeclaredMethod("deferBroadcast", String.class);
                        hooks.install(defer, c -> on() ? Boolean.FALSE : c.proceed());
                        hooks.deoptimize(defer);
                    }
                    case "lists" -> lists(hooks, loader);
                    case "autostart" -> autostart(hooks, loader);
                    case "process" -> {
                        Method whiteList = Class.forName("com.android.server.am.ProcessPolicy", false, loader)
                                .getDeclaredMethod("getWhiteList", int.class);
                        hooks.install(whiteList, c -> {
                            Object result = c.proceed();
                            if (on() && c.getArg(0) instanceof Integer flags && (flags & 1) != 0 && result instanceof List<?> list) {
                                addTo(list, GMS);
                                addTo(list, GMS_PERSISTENT);
                            }
                            return result;
                        });
                    }
                    case "network" -> removeAfterConstruction(hooks, Class.forName("com.miui.server.greeze.power.AwareResourceControl", false, loader),
                            "mNoNetworkBlackUids");
                    case "push" -> push(hooks, loader);
                    default -> { }
                }
                done++;
            } catch (Throwable error) {
                hooks.error("GMS_NETWORK: system part " + part + " skipped", error);
            }
        }
        hooks.info("GMS_NETWORK: system lets GMS online (" + done + "/" + parts.length + " parts)");
    }

    private static void greeze(HookInstaller hooks, ClassLoader loader) throws Exception {
        Class<?> greeze = Class.forName("com.miui.server.greeze.GreezeManagerService", false, loader);
        // isAllowBroadcast(callerUid, callerPkg, calleeUid, calleePkgOrProcess, action)
        Method allow = greeze.getDeclaredMethod("isAllowBroadcast", int.class, String.class, int.class, String.class, String.class);
        Method packageOf = greeze.getDeclaredMethod("getPackageNameFromUid", int.class);
        packageOf.setAccessible(true);
        hooks.install(allow, c -> {
            if (!on() || !(c.getArg(4) instanceof String action)) return c.proceed();
            if (GMS.equals(c.getArg(1)) && PUSH.equals(action)) return true;
            if (DEFERRED.contains(action)) {
                Object callee = c.getArg(3);
                try {
                    Object resolved = packageOf.invoke(c.getThisObject(), c.getArg(2));
                    if (resolved instanceof String) callee = resolved;
                } catch (ReflectiveOperationException ignored) { }
                if (GMS.equals(callee) || GMS_PERSISTENT.equals(callee)) return true;
            }
            return c.proceed();
        });
        hooks.deoptimize(allow);
        Method defer = greeze.getDeclaredMethod("deferBroadcastForMiui", String.class);
        hooks.install(defer, c -> on() && DEFERRED.contains(c.getArg(0)) ? Boolean.FALSE : c.proceed());
        hooks.deoptimize(defer);
        Method limit;
        try {
            limit = greeze.getDeclaredMethod("triggerGMSLimitAction", boolean.class);
            hooks.install(limit, c -> on() ? c.proceed(new Object[]{false}) : c.proceed());
        } catch (NoSuchMethodException older) {
            limit = greeze.getDeclaredMethod("triggerGMSLimitAction");
            Field enabled = greeze.getDeclaredField("mGmsLimitEnabled");
            enabled.setAccessible(true);
            hooks.install(limit, c -> {
                if (on()) enabled.setBoolean(c.getThisObject(), false);
                return c.proceed();
            });
        }
        hooks.deoptimize(limit);
    }

    private static void lists(HookInstaller hooks, ClassLoader loader) throws Exception {
        Class<?> lists = Class.forName("com.miui.server.greeze.power.ListAppsManager", false, loader);
        removeAfterConstruction(hooks, lists, "mSystemBlackList");
        Field useData = lists.getDeclaredField("mUseDataWhiteList");
        useData.setAccessible(true);
        hooks.install(lists.getDeclaredMethod("isInWhiteList", String.class), c -> {
            if (on() && useData.get(c.getThisObject()) instanceof Collection<?> set) addTo(set, GMS);
            return c.proceed();
        });
    }

    /** Pushes from GMS may start their app even when HyperOS's autostart control says no. */
    private static void autostart(HookInstaller hooks, ClassLoader loader) throws Exception {
        Class<?> record = Class.forName("com.android.server.am.BroadcastRecord", false, loader);
        Field caller = record.getDeclaredField("callerPackage"), intent = record.getDeclaredField("intent");
        caller.setAccessible(true);
        intent.setAccessible(true);
        Method check = Class.forName("com.android.server.am.BroadcastQueueModernStubImpl", false, loader).getDeclaredMethod(
                "checkApplicationAutoStart", Class.forName("com.android.server.am.BroadcastQueue", false, loader), record,
                android.content.pm.ResolveInfo.class);
        hooks.install(check, c -> {
            Object broadcast = c.getArg(1);
            if (on() && GMS.equals(caller.get(broadcast)) && intent.get(broadcast) instanceof Intent sent && PUSH.equals(sent.getAction()))
                return true;
            return c.proceed();
        });
        hooks.deoptimize(check);
    }

    /** A push sent by GMS reaches stopped apps, and its app may start from the background for a moment. */
    private static void push(HookInstaller hooks, ClassLoader loader) throws Exception {
        Class<?> ams = Class.forName("com.android.server.am.ActivityManagerService", false, loader);
        Field context = ams.getDeclaredField("mContext");
        context.setAccessible(true);
        Class<?> thread = Class.forName("android.app.IApplicationThread", false, loader);
        Method recordOf = ams.getDeclaredMethod("getRecordForAppLOSP", thread);
        recordOf.setAccessible(true);
        Field info = Class.forName("com.android.server.am.ProcessRecord", false, loader).getDeclaredField("info");
        info.setAccessible(true);
        int hooked = 0;
        for (Method broadcast : ams.getDeclaredMethods()) {
            Class<?>[] types = broadcast.getParameterTypes();
            if (!broadcast.getName().equals("broadcastIntentWithFeature") || types.length < 3
                    || types[0] != thread || types[2] != Intent.class) continue;
            hooks.install(broadcast, c -> {
                if (on() && c.getArg(2) instanceof Intent sent && PUSH.equals(sent.getAction())) {
                    Object app = recordOf.invoke(c.getThisObject(), c.getArg(0));
                    if (app != null && info.get(app) instanceof ApplicationInfo sender && GMS.equals(sender.packageName)) {
                        if (sent.getPackage() != null) allowBriefly((Context) context.get(c.getThisObject()), sent.getPackage());
                        sent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    }
                }
                return c.proceed();
            });
            hooks.deoptimize(broadcast);
            hooked++;
        }
        if (hooked == 0) throw new NoSuchMethodException("ActivityManagerService.broadcastIntentWithFeature");
    }

    /** PowerExemptionManager.addToTemporaryAllowList(pkg, REASON_PUSH_MESSAGING_OVER_QUOTA, "GOOGLE_C2DM", 2 s). */
    private static void allowBriefly(Context context, String packageName) {
        try {
            Class<?> type = Class.forName("android.os.PowerExemptionManager");
            if (exemptions == null) exemptions = type.getConstructor(Context.class).newInstance(context);
            type.getMethod("addToTemporaryAllowList", String.class, int.class, String.class, long.class)
                    .invoke(exemptions, packageName, 102, "GOOGLE_C2DM", 2000L);
        } catch (ReflectiveOperationException | RuntimeException ignored) { }
    }

    private static void removeAfterConstruction(HookInstaller hooks, Class<?> type, String fieldName) throws NoSuchFieldException {
        Field list = type.getDeclaredField(fieldName);
        list.setAccessible(true);
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            hooks.install(constructor, c -> {
                Object result = c.proceed();
                // Built once at boot, before settings may be readable; the hook only exists while the switch is on.
                if (list.get(c.getThisObject()) instanceof Collection<?> values) values.remove(GMS);
                return result;
            });
            hooks.deoptimize(constructor);
        }
    }

    @SuppressWarnings("unchecked")
    private static void addTo(Collection<?> values, String value) {
        if (!values.contains(value)) ((Collection<Object>) values).add(value);
    }

    private GmsNetworkHooks() { }
}
