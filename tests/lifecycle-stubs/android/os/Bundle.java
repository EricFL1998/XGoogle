package android.os;
import java.util.HashMap;
public class Bundle {
 private final HashMap<String,Integer> values=new HashMap<>();
 public int getInt(String key,int fallback){return values.getOrDefault(key,fallback);}
 public void putInt(String key,int value){values.put(key,value);}
}
