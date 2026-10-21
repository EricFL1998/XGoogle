package io.github.leekin2.xgoogle;
public final class Gcj02Test {
    public static void main(String[] args) {
        double[] beijing = Gcj02.shift(39.9087, 116.3975);
        double moved = Math.hypot(beijing[0] - 39.9087, beijing[1] - 116.3975);
        if (moved < 0.002 || moved > 0.01) throw new AssertionError("Mainland offset wrong: " + moved);
        for (double[] p : new double[][]{{22.2819, 114.1582}, {22.1987, 113.5439}, {25.0330, 121.5654}, {52.52, 13.405}}) {
            double[] q = Gcj02.shift(p[0], p[1]);
            if (q[0] != p[0] || q[1] != p[1]) throw new AssertionError("Shifted outside the mainland: " + p[0] + "," + p[1]);
        }
        double[] shenzhen = Gcj02.shift(22.5431, 114.0579);
        if (shenzhen[0] == 22.5431) throw new AssertionError("Shenzhen treated as Hong Kong");
        System.out.println("PASS: GCJ-02 shift on the mainland only");
    }
}
