package android.hardware.soundtrigger;
import java.util.UUID;
public class SoundTrigger {
    public static class Keyphrase {}
    public static class RecognitionConfig {}
    public static class KeyphraseSoundModel {
        public KeyphraseSoundModel(UUID uuid,UUID vendor,byte[] data,Keyphrase[] phrases,int version) {}
        public byte[] getData() { throw new AssertionError("Unexpected model read"); }
        public UUID getUuid() { return new UUID(0,0); }
        public UUID getVendorUuid() { return new UUID(0,0); }
        public int getVersion() { return 1; }
        public Keyphrase[] getKeyphrases() { return new Keyphrase[0]; }
    }
}
