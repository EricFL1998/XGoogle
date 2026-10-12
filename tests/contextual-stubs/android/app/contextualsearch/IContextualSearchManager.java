package android.app.contextualsearch;
import android.os.IBinder;
public interface IContextualSearchManager {
    void startContextualSearch(int entrypoint, ContextualSearchConfig config);
    class Stub {
        public static IContextualSearchManager asInterface(IBinder binder) {
            return (IContextualSearchManager) binder;
        }
    }
}
