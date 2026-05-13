package com.moratan251.psitweaks.common.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import vazkii.psi.api.internal.Vector3;

public final class SableRangeCompat {
    private static final double MAX_DISTANCE_SQUARED = 32.0D * 32.0D;
    private static final Object INIT_LOCK = new Object();
    private static volatile Hooks hooks;
    private static volatile boolean disabled;

    private SableRangeCompat() {
    }

    public static Boolean isInRadius(Entity focalPoint, double x, double y, double z) {
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
            return distanceSquared <= MAX_DISTANCE_SQUARED;
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
