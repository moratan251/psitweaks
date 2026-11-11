package com.moratan251.psitweaks.common.compat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubLevelZoneMathTest {
    private static final double EPSILON = 1.0E-9;

    /** Sable の Pose3dc#bakeIntoMatrix と同じ形: 位置へ動かす × Y 軸まわりの回転 × 回転の中心を原点へ。 */
    private static double[] pose(double px, double py, double pz, double yawDegrees, double rx, double ry, double rz) {
        double angle = Math.toRadians(yawDegrees);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double[] rotation = {
                cos, 0, sin, 0,
                0, 1, 0, 0,
                -sin, 0, cos, 0,
                0, 0, 0, 1};
        double[] toPosition = {1, 0, 0, px, 0, 1, 0, py, 0, 0, 1, pz, 0, 0, 0, 1};
        double[] fromRotationPoint = {1, 0, 0, -rx, 0, 1, 0, -ry, 0, 0, 1, -rz, 0, 0, 0, 1};
        return SubLevelZoneMath.multiply(toPosition, SubLevelZoneMath.multiply(rotation, fromRotationPoint));
    }

    private static double[] identity() {
        return SubLevelZoneMath.translation(0.0);
    }

    /** 範囲の 8 つの角を囲む箱 {minX, minY, minZ, maxX, maxY, maxZ}。 */
    private static double[] corners(double[] m) {
        double[] box = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE,
                -Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
        for (int i = 0; i < 8; i++) {
            for (int axis = 0; axis < 3; axis++) {
                double value = m[axis * 4 + 3];
                for (int col = 0; col < 3; col++) {
                    if ((i & (1 << col)) != 0) {
                        value += m[axis * 4 + col];
                    }
                }
                box[axis] = Math.min(box[axis], value);
                box[axis + 3] = Math.max(box[axis + 3], value);
            }
        }
        return box;
    }

    @Test
    void identityPoseKeepsTheRegion() {
        assertArrayEquals(identity(), SubLevelZoneMath.cornerAffine(identity()), EPSILON);
        double[] cuboid = SubLevelZoneMath.cuboid(9.5, 4.5, 19.5, 3, 1, 1);
        assertArrayEquals(cuboid, SubLevelZoneMath.multiply(SubLevelZoneMath.cornerAffine(identity()), cuboid), EPSILON);
    }

    @Test
    void quarterTurnMapsTheBlocksUnderTheVisibleShip() {
        // 機体の座標のブロック x = 10〜12, y = 5, z = 20（角で判定する行列は -0.5 ずらす）。機体はブロック (11, 5, 20) の中心を
        // 回転の中心にして 90 度回り、その点が世界の (100.5, 64.5, 200.5) に見えている。
        double[] pose = pose(100.5, 64.5, 200.5, 90.0, 11.5, 5.5, 20.5);
        double[] world = SubLevelZoneMath.multiply(SubLevelZoneMath.cornerAffine(pose),
                SubLevelZoneMath.cuboid(9.5, 4.5, 19.5, 3, 1, 1));
        double[] box = corners(world);
        // 世界のブロック x = 100, y = 64, z = 199〜201 の角が、ちょうど中に入る（±0.5 の余白）。
        assertArrayEquals(new double[]{99.5, 63.5, 198.5, 100.5, 64.5, 201.5}, box, EPSILON);
        assertEquals(3, SubLevelZoneMath.enclosingEdge(world));
        // 見た目の中心は、世界のブロック (100, 64, 200)。
        assertArrayEquals(new int[]{100, 64, 200}, SubLevelZoneMath.visualCenter(world));
    }

    @Test
    void tiltedRegionGetsAnEnclosingCubeLargeEnough() {
        // 3×3 の床を 45 度回すと、角は中心から ±2.12 に出る。ZoneArea の立方体は 5 でないと角が欠ける。
        double[] pose = pose(0.5, 0.5, 0.5, 45.0, 0.5, 0.5, 0.5);
        double[] world = SubLevelZoneMath.multiply(SubLevelZoneMath.cornerAffine(pose),
                SubLevelZoneMath.cuboid(-1.5, -0.5, -1.5, 3, 1, 3));
        assertEquals(5, SubLevelZoneMath.enclosingEdge(world));
        // 偶数の立方体の中心は、ZoneArea と同じく負の側のブロック。
        assertArrayEquals(new int[]{4, 4, 4}, SubLevelZoneMath.visualCenter(SubLevelZoneMath.cuboid(2.5, 2.5, 2.5, 4, 4, 4)));
        assertFalse(SubLevelZoneMath.isAxisAligned(world));
    }

    @Test
    void inverseUndoesThePoseAndMapsTheWorldZoneOntoTheShip() {
        double[] pose = pose(100.5, 64.5, 200.5, 30.0, 11.5, 5.5, 20.5);
        double[] inverse = SubLevelZoneMath.invertAffine(pose);
        assertArrayEquals(identity(), SubLevelZoneMath.multiply(inverse, pose), 1.0E-9);
        // 機体の座標のブロック (11, 5, 20) の中心は、世界の (100.5, 64.5, 200.5)。逆に写すと戻る。
        double[] back = SubLevelZoneMath.transformPoint(inverse, 100.5, 64.5, 200.5);
        assertArrayEquals(new double[]{11.5, 5.5, 20.5}, back, 1.0E-9);
        // 潰れた行列には逆がない。
        assertEquals(null, SubLevelZoneMath.invertAffine(SubLevelZoneMath.cuboid(0, 0, 0, 1, 0, 1)));
    }

    @Test
    void axisAlignmentAllowsSwappedAndFlippedEdges() {
        assertTrue(SubLevelZoneMath.isAxisAligned(SubLevelZoneMath.cuboid(0, 0, 0, 2, 3, 4)));
        assertTrue(SubLevelZoneMath.isAxisAligned(new double[]{
                0, 0, -2, 0,
                3, 0, 0, 0,
                0, 4, 0, 0,
                0, 0, 0, 1}));
        assertFalse(SubLevelZoneMath.isAxisAligned(new double[]{
                1, 1, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1}));
    }
}
