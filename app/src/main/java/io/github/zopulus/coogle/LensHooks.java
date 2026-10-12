package io.github.zopulus.coogle;

import io.github.libxposed.api.XposedInterface;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import static io.github.zopulus.coogle.HookInstaller.target;

final class LensHooks {
    static void install(HookInstaller hooks, ClassLoader loader) {
        List<XposedInterface.HookHandle> lensHandles = new ArrayList<>();
        try {
            Class<?> create = target(loader, "ctwn");
            Class<?> talk = target(loader, "dsky");
            Method createEligible = create.getDeclaredMethod("c");
            Method talkEligible = talk.getDeclaredMethod("f");
            if (createEligible.getReturnType() != boolean.class || talkEligible.getReturnType() != boolean.class
                    || Modifier.isStatic(createEligible.getModifiers()) || Modifier.isStatic(talkEligible.getModifiers())) {
                throw new NoSuchMethodException("Lens eligibility signature changed");
            }
            // These two pure eligibility predicates control the Lens Create and Talk entries.
            lensHandles.add(hooks.untracked(createEligible, chain -> FeatureConfig.enabled(FeaturePolicy.LENS) ? true : chain.proceed()));
            lensHandles.add(hooks.untracked(talkEligible, chain -> FeatureConfig.enabled(FeaturePolicy.LENS) ? true : chain.proceed()));
            String[][] callers = {{"dvwm", "K", "V"}, {"dsky", "c", "e"}, {"dupk", "af"}, {"dwcj", "af"},
                    {"dwax", "accept"}, {"dwbp", "a"}, {"dwbs", "a"}};
            for (String[] caller : callers) {
                Class<?> callerClass;
                try { callerClass = target(loader, caller[0]); }
                catch (ClassNotFoundException unavailable) {
                    hooks.info("Lens optional caller unavailable: " + caller[0]);
                    continue;
                }
                for (Method method : callerClass.getDeclaredMethods()) {
                    for (int i = 1; i < caller.length; i++) {
                        if (method.getName().equals(caller[i]) && !Modifier.isAbstract(method.getModifiers())
                                && !Modifier.isNative(method.getModifiers())) hooks.deoptimize(method);
                    }
                }
            }
            hooks.info("LENS Talk + Create eligibility hooks installed");
        } catch (Throwable error) {
            for (int i = lensHandles.size() - 1; i >= 0; i--) {
                try { lensHandles.get(i).unhook(); } catch (Throwable ignored) { }
            }
            hooks.error("LENS skip incompatible Google; eligibility hooks removed", error);
        }
    }
}
