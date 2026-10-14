package io.github.zopulus.coogle;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.List;

/**
 * Google Wallet's newer tap-to-pay screen asks for the lock again on every locked double press,
 * even when Google itself reports a recent fingerprint/PIN (the older screen and the international
 * ROM do not). When Google says the authentication is recent, keep the first preview without a new
 * lock request. Google's own answer is never changed: when it says "not recent", Wallet asks as
 * usual. Idea and checks from Coogle 1.3.3 (WalletPreviewHooks.kt).
 *
 * The names below were read from each listed Wallet APK; any other APK keeps Wallet's behavior.
 */
final class WalletRecentAuthHooks {
    /** One reviewed Wallet build: model = PaymentFocusedViewModel, callback = its auth status collector. */
    private record Build(long size, String sha256, String model, String callback, String result,
                         String unwrap, String unwrapArg, String unit) { }
    private static final List<Build> VERIFIED = List.of(
            // Google Wallet 26.39.993999421 (933924722)
            new Build(42968817L, "a44afbadf411e8a4ae2e08998db7f71ea64514135788baaf21fa60b5560d05e7",
                    "xac", "wzf", "puh", "awqc", "awqh", "bqmv"));
    private static final List<String> PAYMENT_EXTRAS = List.of("paymentCardInfo", "safePaymentCardInfo", "valuables",
            "eventType", "failedReason", "failedUiInfo", "merchantName", "isTransit", "idCardDocumentRequestBytes");
    private record Initializing(Object model, boolean preview) { }
    private static final ThreadLocal<ArrayDeque<Initializing>> initializing = new ThreadLocal<>();

    static void install(HookInstaller hooks, ClassLoader loader, String apk) {
        if (!FeatureConfig.enabled(FeaturePolicy.WALLET)) return;
        Build build;
        try { build = verified(apk); }
        catch (Throwable failure) { hooks.error("WALLET recent-auth: APK check failed; original behavior retained", failure); return; }
        if (build == null) { hooks.info("WALLET recent-auth: unreviewed Wallet version; original behavior retained"); return; }
        int mark = hooks.mark();
        try {
            Class<?> modelType = HookInstaller.target(loader, build.model);
            Class<?> callbackType = HookInstaller.target(loader, build.callback);
            Class<?> resultType = HookInstaller.target(loader, build.result);
            Field recent = field(resultType, "a");
            if (recent.getType() != boolean.class) throw new NoSuchFieldException("result.a");
            Field modelField = field(callbackType, "b");
            if (modelField.getType() != modelType) throw new NoSuchFieldException("callback.b");
            Field responseField = field(callbackType, "a");
            Method unwrap = HookInstaller.target(loader, build.unwrap)
                    .getDeclaredMethod("a", HookInstaller.target(loader, build.unwrapArg));
            unwrap.setAccessible(true);
            Object unit = field(HookInstaller.target(loader, build.unit), "a").get(null);
            Field contextField = field(modelType, "b");
            Field requestField = field(modelType, "q");
            Method ui = modelType.getDeclaredMethod("a");
            ui.setAccessible(true);
            Field cardField = field(ui.getReturnType(), "g");
            Field valuablesField = field(ui.getReturnType(), "h");
            Method requestValue = requestField.getType().getMethod("e");
            Constructor<?>[] constructors = modelType.getDeclaredConstructors();
            if (constructors.length != 1 || !Context.class.isAssignableFrom(constructors[0].getParameterTypes()[0]))
                throw new NoSuchMethodException("model constructor");
            Method collect = callbackType.getDeclaredMethod("b", Object.class);

            hooks.install(constructors[0], chain -> {
                ArrayDeque<Initializing> stack = initializing.get();
                if (stack == null) { stack = new ArrayDeque<>(); initializing.set(stack); }
                boolean preview = false;
                try { preview = FeatureConfig.enabled(FeaturePolicy.WALLET) && isBarePreview((Context) chain.getArg(0)); }
                catch (Throwable ignored) { }
                stack.addLast(new Initializing(chain.getThisObject(), preview));
                try { return chain.proceed(); }
                finally { stack.removeLast(); if (stack.isEmpty()) initializing.remove(); }
            });
            hooks.install(collect, chain -> {
                ArrayDeque<Initializing> stack = initializing.get();
                Initializing window = stack == null ? null : stack.peekLast();
                if (window == null || !window.preview || chain.getArg(0) != unit
                        || !FeatureConfig.enabled(FeaturePolicy.WALLET)) return chain.proceed();
                boolean keep = false;
                try {
                    Object model = modelField.get(chain.getThisObject());
                    Object response = unwrap.invoke(null, responseField.get(chain.getThisObject()));
                    if (model == window.model && resultType.isInstance(response) && recent.getBoolean(response)) {
                        Context context = (Context) contextField.get(model);
                        Object state = ui.invoke(model);
                        keep = context.getSystemService(KeyguardManager.class).isDeviceLocked() && state != null
                                && cardField.get(state) == null && ((List<?>) valuablesField.get(state)).isEmpty()
                                && Boolean.FALSE.equals(requestValue.invoke(requestField.get(model)));
                    }
                } catch (Throwable ignored) { }
                if (!keep) return chain.proceed();
                // The collector only sets the lock request here and returns Unit; nothing else is skipped.
                hooks.info("WALLET recent authentication reported by Google; initial preview kept");
                return unit;
            });
            hooks.info("WALLET recent-auth preview gate installed for " + build.sha256.substring(0, 12));
        } catch (Throwable failure) {
            hooks.unhookSince(mark);
            hooks.error("WALLET recent-auth binding unavailable; original behavior retained", failure);
        }
    }

    private static Build verified(String apk) throws Exception {
        long size = new File(apk).length();
        Build candidate = null;
        for (Build build : VERIFIED) if (build.size == size) candidate = build;
        if (candidate == null) return null;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = new java.io.FileInputStream(apk)) {
            byte[] buffer = new byte[65536]; int n;
            while ((n = input.read(buffer)) >= 0) digest.update(buffer, 0, n);
        }
        StringBuilder hex = new StringBuilder();
        for (byte b : digest.digest()) hex.append(String.format("%02x", b));
        return candidate.sha256.contentEquals(hex) ? candidate : null;
    }

    @SuppressWarnings("deprecation")
    private static boolean isBarePreview(Context context) {
        List<ActivityManager.RunningTaskInfo> tasks = context.getSystemService(ActivityManager.class).getRunningTasks(1);
        if (tasks.isEmpty() || tasks.get(0).topActivity == null) return false;
        ActivityManager.RunningTaskInfo task = tasks.get(0);
        Intent intent = task.baseIntent;
        if (!WalletPowerPolicy.WALLET_PACKAGE.equals(task.topActivity.getPackageName())
                || !WalletPowerPolicy.QUICKDRAW_ACTIVITY.equals(task.topActivity.getClassName())
                || intent == null || !WalletPowerPolicy.QUICKDRAW_ACTION.equals(intent.getAction())) return false;
        for (String extra : PAYMENT_EXTRAS) if (intent.hasExtra(extra)) return false;
        return true;
    }

    private static Field field(Class<?> type, String name) throws NoSuchFieldException {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }
    private WalletRecentAuthHooks() { }
}
