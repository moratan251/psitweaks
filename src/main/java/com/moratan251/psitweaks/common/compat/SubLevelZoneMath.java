package com.moratan251.psitweaks.common.compat;

/**
 * 機体（Sable のサブレベル）の座標で作った領域の行列を、世界の座標へ写す計算（2026-11-11 ユーザーの決定: 方針 A）。
 * Minecraft や Sable のクラスに頼らない計算だけを置く（JUnit で確かめるため）。行列はどれも 4×4 の行優先の {@code double[16]}
 * （1〜3 列目が辺、4 列目が始点、4 行目は 0 0 0 1）。
 * <p>
 * 領域行列はブロックの角（整数の点）が中にあるかで判定し、演算子: 直方体領域は行列を -0.5 ずらして置く。そのため写すときは、
 * 点に 0.5 を足して機体の姿勢で写してから 0.5 を引く（{@link #cornerAffine}）。こうすると、写した後の判定が「世界のブロックの中心が、
 * 機体の座標の範囲（見た目の箱）に入るか」と同じになる。
 */
public final class SubLevelZoneMath {
    private static final double EPSILON = 1.0E-9;

    private SubLevelZoneMath() {
    }

    /** 単位行列に平行移動 (t, t, t) を足したもの。 */
    public static double[] translation(double t) {
        return new double[]{
                1, 0, 0, t,
                0, 1, 0, t,
                0, 0, 1, t,
                0, 0, 0, 1};
    }

    /** 4×4 の積 {@code a × b}。 */
    public static double[] multiply(double[] a, double[] b) {
        double[] out = new double[16];
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                double sum = 0.0;
                for (int k = 0; k < 4; k++) {
                    sum += a[row * 4 + k] * b[k * 4 + col];
                }
                out[row * 4 + col] = sum;
            }
        }
        return out;
    }

    /**
     * アフィン変換（4 行目が 0 0 0 1）の逆。線形の部分（3×3）が潰れているときは null。世界の座標の範囲を機体の座標へ写すのに使う。
     */
    public static double[] invertAffine(double[] m) {
        double a = m[0], b = m[1], c = m[2];
        double d = m[4], e = m[5], f = m[6];
        double g = m[8], h = m[9], i = m[10];
        double det = a * (e * i - f * h) - b * (d * i - f * g) + c * (d * h - e * g);
        if (!Double.isFinite(det) || Math.abs(det) < EPSILON) {
            return null;
        }
        double inv = 1.0 / det;
        double[] out = new double[16];
        out[0] = (e * i - f * h) * inv;
        out[1] = (c * h - b * i) * inv;
        out[2] = (b * f - c * e) * inv;
        out[4] = (f * g - d * i) * inv;
        out[5] = (a * i - c * g) * inv;
        out[6] = (c * d - a * f) * inv;
        out[8] = (d * h - e * g) * inv;
        out[9] = (b * g - a * h) * inv;
        out[10] = (a * e - b * d) * inv;
        double tx = m[3], ty = m[7], tz = m[11];
        out[3] = -(out[0] * tx + out[1] * ty + out[2] * tz);
        out[7] = -(out[4] * tx + out[5] * ty + out[6] * tz);
        out[11] = -(out[8] * tx + out[9] * ty + out[10] * tz);
        out[15] = 1.0;
        return out;
    }

    /** 点 ({@code x}, {@code y}, {@code z}) を {@code m} で写した点 {x, y, z}。 */
    public static double[] transformPoint(double[] m, double x, double y, double z) {
        return new double[]{
                m[0] * x + m[1] * y + m[2] * z + m[3],
                m[4] * x + m[5] * y + m[6] * z + m[7],
                m[8] * x + m[9] * y + m[10] * z + m[11]};
    }

    /**
     * 機体の姿勢の行列 {@code pose}（機体の座標の点 → 世界の座標の点）を、領域行列の判定のしかた（ブロックの角で判定）に合わせた
     * 行列にする: 0.5 ずらす × 姿勢 × 0.5 戻す。
     */
    public static double[] cornerAffine(double[] pose) {
        return multiply(translation(-0.5), multiply(pose, translation(0.5)));
    }

    /** 軸にそった直方体の領域行列: 始点 ({@code x}, {@code y}, {@code z})、辺の長さ ({@code sizeX}, {@code sizeY}, {@code sizeZ})。 */
    public static double[] cuboid(double x, double y, double z, double sizeX, double sizeY, double sizeZ) {
        return new double[]{
                sizeX, 0, 0, x,
                0, sizeY, 0, y,
                0, 0, sizeZ, z,
                0, 0, 0, 1};
    }

    /** 3 つの辺（1〜3 列目）が、それぞれ 1 つの軸にそっているか（向きが入れ替わっていてもよい）。 */
    public static boolean isAxisAligned(double[] matrix) {
        for (int col = 0; col < 3; col++) {
            int nonZero = 0;
            for (int row = 0; row < 3; row++) {
                if (Math.abs(matrix[row * 4 + col]) > EPSILON) {
                    nonZero++;
                }
            }
            if (nonZero != 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * 領域行列 {@code matrix} の中の整数の点が、行列の中心のブロック（ZoneArea と同じ求め方）を中心にした一辺の立方体に全部入る、
     * いちばん小さい奇数の一辺。写した後の範囲を、ZoneArea の立方体で欠けさせないために使う。
     */
    public static int enclosingEdge(double[] matrix) {
        int[] center = new int[3];
        for (int axis = 0; axis < 3; axis++) {
            double start = matrix[axis * 4 + 3];
            double far = start + matrix[axis * 4] + matrix[axis * 4 + 1] + matrix[axis * 4 + 2];
            center[axis] = (int) Math.floor((start + far) * 0.5 + EPSILON);
        }
        return enclosingEdge(matrix, center);
    }

    /**
     * 範囲の見た目の中心（角で判定する行列の中点 + 0.5）が入るブロック {x, y, z}。ちょうどブロックの境目なら負の側（ZoneArea が
     * 偶数のサイズで中心を負の側に取るのと同じ）。
     */
    public static int[] visualCenter(double[] matrix) {
        int[] center = new int[3];
        for (int axis = 0; axis < 3; axis++) {
            double middle = matrix[axis * 4 + 3] + (matrix[axis * 4] + matrix[axis * 4 + 1] + matrix[axis * 4 + 2]) * 0.5;
            center[axis] = (int) Math.floor(middle + 0.5 - EPSILON);
        }
        return center;
    }

    /** {@link #enclosingEdge(double[])} を、中心のブロック {@code center} を決めて求める。 */
    public static int enclosingEdge(double[] matrix, int[] center) {
        int edge = 1;
        for (int axis = 0; axis < 3; axis++) {
            double start = matrix[axis * 4 + 3];
            double low = start;
            double high = start;
            for (int i = 1; i < 8; i++) {
                double corner = start;
                for (int col = 0; col < 3; col++) {
                    if ((i & (1 << col)) != 0) {
                        corner += matrix[axis * 4 + col];
                    }
                }
                low = Math.min(low, corner);
                high = Math.max(high, corner);
            }
            int below = center[axis] - (int) Math.ceil(low - EPSILON);
            int above = (int) Math.floor(high + EPSILON) - center[axis];
            edge = Math.max(edge, 2 * Math.max(0, Math.max(below, above)) + 1);
        }
        return edge;
    }
}
