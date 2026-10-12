package io.github.zopulus.coogle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.ArrayList;
public final class SignalModelAdapterTest {
    public static void main(String[] args) throws Exception {
        byte[] original = Files.readAllBytes(Path.of(args[0]));
        byte[] baseline = original.clone();
        byte[] expected = Files.readAllBytes(Path.of(args[1]));
        byte[] actual = SignalModelAdapter.adapt(original);
        CRC32 crc = new CRC32();
        crc.update(actual, 0, actual.length - 4);
        long trailer = Integer.toUnsignedLong(ByteBuffer.wrap(actual).order(ByteOrder.LITTLE_ENDIAN).getInt(actual.length - 4));
        if (crc.getValue() != trailer) throw new AssertionError("DSP CRC32 trailer mismatch");
        if (!Arrays.equals(expected, actual)) throw new AssertionError("Offline reference differs");
        if (!Arrays.equals(original, baseline)) throw new AssertionError("Source was modified");
        actual[0] ^= 1;
        byte[] again = SignalModelAdapter.adapt(original.clone());
        if (!Arrays.equals(expected, again)) throw new AssertionError("Caller modified the cached model");
        original[0] ^= 1;
        try { SignalModelAdapter.adapt(original); throw new AssertionError("Mutated input accepted by cache"); }
        catch (IllegalArgumentException expectedFailure) { }
        original[0] ^= 1;
        byte[] changed = original.clone();
        changed[changed.length / 2] ^= 1;
        try { SignalModelAdapter.adapt(changed); throw new AssertionError("Changed model accepted by cache"); }
        catch (IllegalArgumentException expectedFailure) { }
        if (!Arrays.equals(expected, SignalModelAdapter.adapt(original)))
            throw new AssertionError("Rejected model damaged the cache");
        try { SignalModelAdapter.adapt(new byte[16]); throw new AssertionError("Unknown accepted"); }
        catch (IllegalArgumentException expectedFailure) { }
        var worker = Executors.newFixedThreadPool(8);
        try {
            var results = new ArrayList<Future<byte[]>>();
            for (int i = 0; i < 32; i++) results.add(worker.submit(() -> SignalModelAdapter.adapt(original.clone())));
            for (Future<byte[]> result : results) {
                byte[] model = result.get();
                if (!Arrays.equals(expected, model)) throw new AssertionError("Concurrent cache output changed");
                model[0] ^= 1;
            }
        } finally { worker.shutdown(); }
        System.out.println("Signal model adapter: reference, source preservation, rejection passed");
    }
}
