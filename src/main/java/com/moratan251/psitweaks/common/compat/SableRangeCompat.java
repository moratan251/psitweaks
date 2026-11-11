package com.moratan251.psitweaks.common.compat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;
import vazkii.psi.api.internal.Vector3;

public final class SableRangeCompat {
    private static final double DEFAULT_MAX_DISTANCE = 32.0D;
    private static final Object INIT_LOCK = new Object();
    private static volatile Hooks hooks;
    private static volatile boolean disabled;
    /** 機体の姿勢の行列が取れない（Sable の版で API が変わった）とき。射程の判定などとは別に止める。 */
    private static volatile boolean poseDisabled;
    /** 重なる機体を探す呼び出し（最初に使うときに引く）。 */
    private static volatile Method getAllIntersecting;
    private static volatile Constructor<?> boundingBoxFromAabb;

    private SableRangeCompat() {
    }

    public static Boolean isInRadius(Entity focalPoint, double x, double y, double z) {
        return isInRadius(focalPoint, x, y, z, DEFAULT_MAX_DISTANCE);
    }

    public static Boolean isInRadius(Entity focalPoint, double x, double y, double z, double maxDistance) {
        if (focalPoint == null || disabled) {
            return null;
        }

        Hooks loadedHooks = getHooks();
        if (loadedHooks == null) {
            return null;
        }

        Level level = focalPoint.level();
        Vec3 target = new Vec3(x, y, z);
        try {
            Object subLevel = loadedHooks.getContaining.invoke(loadedHooks.helper, level, target);
            if (subLevel == null) {
                return null;
            }

            Vec3 origin = new Vec3(focalPoint.getX(), focalPoint.getY(), focalPoint.getZ());
            double distanceSquared = ((Number) loadedHooks.distanceSquaredWithSubLevels.invoke(
                    loadedHooks.helper,
                    level,
                    origin,
                    target
            )).doubleValue();
            return distanceSquared <= maxDistance * maxDistance;
        } catch (ReflectiveOperationException | LinkageError | ClassCastException e) {
            disabled = true;
            return null;
        }
    }

    public static Vec3 projectForEffect(Level level, Vector3 position) {
        if (position == null) {
            return null;
        }
        return projectForEffect(level, position.toVec3D());
    }

    public static Vector3 projectVectorForEffect(Level level, Vector3 position) {
        if (position == null) {
            return null;
        }
        return new Vector3(projectForEffect(level, position));
    }

    public static Vec3 projectDirectionForEffect(Level level, Vector3 origin, Vector3 direction) {
        if (origin == null || direction == null) {
            return direction == null ? null : direction.toVec3D();
        }
        return projectDirectionForEffect(level, origin.toVec3D(), direction.toVec3D());
    }

    public static Vec3 projectDirectionForEffect(Level level, Vec3 origin, Vec3 direction) {
        if (origin == null || direction == null) {
            return direction;
        }

        Vec3 projectedOrigin = projectForEffect(level, origin);
        Vec3 projectedEnd = projectForEffect(level, origin.add(direction));
        return projectedEnd.subtract(projectedOrigin);
    }

    public static Vec3 projectForEffect(Level level, Vec3 position) {
        if (level == null || position == null || disabled) {
            return position;
        }

        Hooks loadedHooks = getHooks();
        if (loadedHooks == null) {
            return position;
        }

        try {
            Object subLevel = loadedHooks.getContaining.invoke(loadedHooks.helper, level, position);
            if (subLevel == null) {
                return position;
            }

            Object projected = loadedHooks.projectOutOfSubLevel.invoke(loadedHooks.helper, level, position);
            if (projected instanceof Vec3 projectedPosition) {
                return projectedPosition;
            }
        } catch (ReflectiveOperationException | LinkageError | ClassCastException e) {
            disabled = true;
        }
        return position;
    }

    /**
     * 点 {@code position} を含む機体（Sable のサブレベル）の、今の姿勢の行列（機体の座標の点 → 世界の座標の点、4×4 の行優先）。
     * 領域を、置く時点の機体の位置と向きで世界の座標へ写すのに使う。機体の中でない・Sable がない・API が合わないときは null。
     */
    public static double[] subLevelPoseAt(Level level, Vec3 position) {
        if (level == null || position == null || disabled || poseDisabled) {
            return null;
        }
        Hooks loadedHooks = getHooks();
        if (loadedHooks == null) {
            return null;
        }
        try {
            Object subLevel = loadedHooks.getContaining.invoke(loadedHooks.helper, level, position);
            if (subLevel == null) {
                return null;
            }
            return poseOf(subLevel);
        } catch (ReflectiveOperationException | LinkageError | ClassCastException e) {
            // 姿勢が取れないだけなら、射程の判定などは止めない。
            poseDisabled = true;
            return null;
        }
    }

    /**
     * 世界の箱 {@code bounds} に重なる機体（Sable のサブレベル）ごとの、今の姿勢の行列（機体の座標の点 → 世界の座標の点、4×4 の
     * 行優先）。領域の効き目を、範囲に重なった機体のブロックにも届けるのに使う。Sable がない・API が合わないときは空。
     */
    public static List<double[]> subLevelPosesIntersecting(Level level, AABB bounds) {
        if (level == null || bounds == null || disabled || poseDisabled) {
            return List.of();
        }
        Hooks loadedHooks = getHooks();
        if (loadedHooks == null) {
            return List.of();
        }
        try {
            if (getAllIntersecting == null) {
                Class<?> boxInterface = Class.forName("dev.ryanhcode.sable.companion.math.BoundingBox3dc");
                boundingBoxFromAabb = Class.forName("dev.ryanhcode.sable.companion.math.BoundingBox3d")
                        .getConstructor(AABB.class);
                getAllIntersecting = loadedHooks.helper.getClass().getMethod("getAllIntersecting", Level.class, boxInterface);
            }
            Iterable<?> subLevels = (Iterable<?>) getAllIntersecting.invoke(loadedHooks.helper, level,
                    boundingBoxFromAabb.newInstance(bounds));
            if (subLevels == null) {
                return List.of();
            }
            List<double[]> poses = new ArrayList<>();
            for (Object subLevel : subLevels) {
                double[] pose = poseOf(subLevel);
                if (pose != null) {
                    poses.add(pose);
                }
            }
            return poses;
        } catch (ReflectiveOperationException | LinkageError | ClassCastException e) {
            poseDisabled = true;
            return List.of();
        }
    }

    /**
     * ブロック {@code pos} が機体（Sable のサブレベル）のブロックなら、それが今見えている世界の位置のブロック。そうでなければ
     * {@code pos} のまま（Create のブレイズバーナーが機体の上でも領域を探せるように）。
     */
    public static BlockPos worldBlockOf(Level level, BlockPos pos) {
        if (level == null || pos == null || disabled) {
            return pos;
        }
        Vec3 center = Vec3.atCenterOf(pos);
        Vec3 projected = projectForEffect(level, center);
        return projected == center ? pos : BlockPos.containing(projected);
    }

    private static double[] poseOf(Object subLevel) throws ReflectiveOperationException {
        Object pose = subLevel.getClass().getMethod("logicalPose").invoke(subLevel);
        Matrix4d matrix = (Matrix4d) pose.getClass().getMethod("bakeIntoMatrix", Matrix4d.class)
                .invoke(pose, new Matrix4d());
        double[] values = new double[16];
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                // JOML の get は（列, 行）。
                values[row * 4 + col] = matrix.get(col, row);
            }
        }
        for (double value : values) {
            if (!Double.isFinite(value)) {
                return null;
            }
        }
        return values;
    }

    private static Hooks getHooks() {
        Hooks loadedHooks = hooks;
        if (loadedHooks != null || disabled) {
            return loadedHooks;
        }

        synchronized (INIT_LOCK) {
            if (hooks != null || disabled) {
                return hooks;
            }
            try {
                Class<?> sableClass = Class.forName("dev.ryanhcode.sable.Sable");
                Field helperField = sableClass.getField("HELPER");
                Object helper = helperField.get(null);
                Method getContaining = helper.getClass().getMethod("getContaining", Level.class, Position.class);
                Method distanceSquaredWithSubLevels = helper.getClass().getMethod(
                        "distanceSquaredWithSubLevels",
                        Level.class,
                        Position.class,
                        Position.class
                );
                Method projectOutOfSubLevel = helper.getClass().getMethod(
                        "projectOutOfSubLevel",
                        Level.class,
                        Position.class
                );
                hooks = new Hooks(helper, getContaining, distanceSquaredWithSubLevels, projectOutOfSubLevel);
                return hooks;
            } catch (ReflectiveOperationException | LinkageError e) {
                disabled = true;
                return null;
            }
        }
    }

    private record Hooks(
            Object helper,
            Method getContaining,
            Method distanceSquaredWithSubLevels,
            Method projectOutOfSubLevel
    ) {
    }
}
