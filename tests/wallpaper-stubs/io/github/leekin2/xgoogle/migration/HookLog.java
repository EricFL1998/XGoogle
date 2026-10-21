package io.github.leekin2.xgoogle.migration;
public class HookLog {
    public static final HookLog INSTANCE = new HookLog();
    public void w(String tag,String message,Throwable failure) {}
    public static void i$default(HookLog log,String tag,String message,Throwable failure,int mask,Object marker) {}
}
