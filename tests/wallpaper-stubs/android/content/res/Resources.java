package android.content.res;
public class Resources {
    public int lookups;
    public String locale = "zh";
    public int getIdentifier(String name, String type, String pkg) { lookups++; return name.hashCode(); }
    public String getString(int id) { return locale + id; }
}
