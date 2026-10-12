package android.os;
import java.util.*;
public class Handler {
    public record Task(Runnable callback, Object token, long delay) {}
    public static final List<Task> tasks = new ArrayList<>();
    public Handler(Looper looper) {}
    public boolean postDelayed(Runnable callback, long delay) { return postDelayed(callback, null, delay); }
    public boolean postDelayed(Runnable callback, Object token, long delay) { tasks.add(new Task(callback,token,delay)); return true; }
    public void removeCallbacksAndMessages(Object token) { tasks.removeIf(task -> task.token()==token); }
}
