package android.os;
public class ServiceManager {
    public static IBinder current;
    public static int reads;
    public static IBinder getService(String name) {
        if (!name.equals("contextual_search")) throw new AssertionError("Incorrect service");
        reads++;
        return current;
    }
}
