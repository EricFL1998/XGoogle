package io.github.zopulus.coogle;

public final class GateBindingTest {
    public static final class Navigation {
        public static final Holder u0 = new Holder();
        boolean gesture = true, hidden = true, circle = true, broken;
        public boolean E() { return gesture; }
        public boolean w() { return hidden; }
        public boolean q() { if (broken) throw new IllegalStateException("unavailable"); return circle; }
    }
    public static final class Holder {
        final Navigation nav = new Navigation();
        public Navigation get() { return nav; }
    }
    public static final class MissingField { }
    public static final class WrongSignature {
        public static final Holder u0 = new Holder();
        public int E() { return 3; }
        public boolean w() { return true; }
        public boolean q() { return true; }
    }
    private static void check(boolean value, String name) {
        if (!value) throw new AssertionError(name);
    }
    public static void main(String[] args) throws Exception {
        GateBinding binding = GateBinding.bind(Navigation.class);
        Navigation nav = Navigation.u0.get();
        check(binding.eligible(), "all native conditions pass");
        nav.hidden = false;
        check(!binding.eligible(), "visible handle must disable hidden controller");
        nav.hidden = true; nav.gesture = false;
        check(!binding.eligible(), "button navigation must disable controller");
        nav.gesture = true; nav.circle = false;
        check(!binding.eligible(), "user disabling circle must be respected");
        nav.circle = true;
        check(binding.eligible(), "live observer updates must be read");
        nav.broken = true;
        try { binding.eligible(); throw new AssertionError("observer failure cannot enable controller"); }
        catch (ReflectiveOperationException expected) { }
        try { GateBinding.bind(MissingField.class); throw new AssertionError("missing target must be rejected"); }
        catch (ReflectiveOperationException expected) { }
        try { GateBinding.bind(WrongSignature.class); throw new AssertionError("wrong signature must be rejected"); }
        catch (ReflectiveOperationException expected) { }
        System.out.println("PASS: 8 navigation and compatibility checks");
    }
}
