package com.moratan251.psitweaks.common.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import vazkii.psi.api.internal.Vector3;

public final class SablePhysicsCompat {
    private static final String SABLE_MOD_ID = "sable";
    private static final double IMPULSE_PER_POWER = 100.0D;
    private static final double MAX_DELTA_V_BASE = 10.0D;
    private static final double MAX_DELTA_V_PER_POWER = 5.0D;
    private static final double MAX_ANGULAR_SPEED_BASE = 8.0D;
    private static final double MAX_ANGULAR_SPEED_PER_POWER = 2.0D;
    private static final double MAX_ANGULAR_SPEED_HARD_CAP = 20.0D;
    private static final double RAYCAST_DISTANCE = 32.0D;
    private static final double MIN_DIRECTION_LENGTH_SQUARED = 1.0E-8D;
    private static final Object INIT_LOCK = new Object();
    private static volatile Hooks hooks;
    private static volatile boolean disabled;

    private SablePhysicsCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(SABLE_MOD_ID);
    }

    public static boolean applyImpulseAlongRay(Level level, Vector3 position, Vec3 direction, double power, Entity caster) {
        if (level == null || position == null || direction == null || level.isClientSide || disabled) {
            return false;
        }
        if (direction.lengthSqr() < MIN_DIRECTION_LENGTH_SQUARED || power <= 0.0D) {
            return false;
        }

        Hooks loadedHooks = getHooks();
        if (loadedHooks == null) {
            return false;
        }

        try {
            Vec3 start = position.toVec3D();
            Vec3 worldDirection = direction.normalize();
            BlockHitResult hit = level.clip(new ClipContext(
                    start,
                    start.add(worldDirection.scale(RAYCAST_DISTANCE)),
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    caster
            ));
            if (hit.getType() != HitResult.Type.BLOCK) {
                return false;
            }

            Object subLevel = getContainingSubLevel(loadedHooks, level, hit.getLocation(), hit.getBlockPos());
            if (!loadedHooks.serverSubLevelClass.isInstance(subLevel)) {
                return false;
            }

            Object handle = loadedHooks.rigidBodyHandleOf.invoke(null, subLevel);
            if (handle == null || !((Boolean) loadedHooks.rigidBodyHandleIsValid.invoke(handle))) {
                return false;
            }

            Object massTracker = loadedHooks.getMassTracker.invoke(subLevel);
            if (massTracker == null) {
                return false;
            }

            double mass = ((Number) loadedHooks.getMass.invoke(massTracker)).doubleValue();
            if (!Double.isFinite(mass) || mass <= 0.0D) {
                return false;
            }

            Vector3d worldDirectionVector = new Vector3d(worldDirection.x, worldDirection.y, worldDirection.z);
            Object pose = loadedHooks.logicalPose.invoke(subLevel);
            Vector3d localDirection = (Vector3d) loadedHooks.transformNormalInverse.invoke(
                    pose,
                    worldDirectionVector,
                    new Vector3d()
            );
            if (localDirection.lengthSquared() < MIN_DIRECTION_LENGTH_SQUARED) {
                return false;
            }

            Vec3 pointVec = hit.getLocation();
            Vector3d localPoint = new Vector3d(pointVec.x, pointVec.y, pointVec.z);

            double maxDeltaV = MAX_DELTA_V_BASE + MAX_DELTA_V_PER_POWER * power;
            double impulseMagnitude = Math.min(power * IMPULSE_PER_POWER, maxDeltaV * mass);
            Vector3d impulse = localDirection.normalize().mul(impulseMagnitude);
            loadedHooks.applyImpulseAtPoint.invoke(handle, localPoint, impulse);
            clampAngularVelocity(loadedHooks, handle, power);
            return true;
        } catch (ReflectiveOperationException | LinkageError | ClassCastException e) {
            disabled = true;
            return false;
        }
    }

    private static void clampAngularVelocity(Hooks loadedHooks, Object handle, double power)
            throws ReflectiveOperationException {
        Vector3dc angularVelocity = (Vector3dc) loadedHooks.getAngularVelocity.invoke(handle);
        double angularSpeedSquared = angularVelocity.lengthSquared();
        double maxAngularSpeed = Math.min(
                MAX_ANGULAR_SPEED_HARD_CAP,
                MAX_ANGULAR_SPEED_BASE + MAX_ANGULAR_SPEED_PER_POWER * power
        );
        double maxAngularSpeedSquared = maxAngularSpeed * maxAngularSpeed;
        if (!Double.isFinite(angularSpeedSquared)
                || angularSpeedSquared <= maxAngularSpeedSquared
                || angularSpeedSquared < MIN_DIRECTION_LENGTH_SQUARED) {
            return;
        }

        double angularSpeed = Math.sqrt(angularSpeedSquared);
        Vector3d correction = new Vector3d(angularVelocity).mul(maxAngularSpeed / angularSpeed).sub(angularVelocity);
        loadedHooks.addLinearAndAngularVelocity.invoke(handle, new Vector3d(), correction);
    }

    private static Object getContainingSubLevel(Hooks loadedHooks, Level level, Vec3 point, BlockPos blockPos)
            throws ReflectiveOperationException {
        Object subLevel = loadedHooks.getContainingPosition.invoke(loadedHooks.helper, level, point);
        if (subLevel != null) {
            return subLevel;
        }

        return loadedHooks.getContainingBlock.invoke(loadedHooks.helper, level, blockPos);
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
                Class<?> serverSubLevelClass = Class.forName("dev.ryanhcode.sable.sublevel.ServerSubLevel");
                Class<?> rigidBodyHandleClass = Class.forName("dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle");
                Class<?> massDataClass = Class.forName("dev.ryanhcode.sable.api.physics.mass.MassData");
                Class<?> pose3dClass = Class.forName("dev.ryanhcode.sable.companion.math.Pose3d");

                Field helperField = sableClass.getField("HELPER");
                Object helper = helperField.get(null);
                Method getContainingPosition = helper.getClass().getMethod("getContaining", Level.class, Position.class);
                Method getContainingBlock = helper.getClass().getMethod("getContaining", Level.class, Vec3i.class);
                Method rigidBodyHandleOf = rigidBodyHandleClass.getMethod("of", serverSubLevelClass);
                Method rigidBodyHandleIsValid = rigidBodyHandleClass.getMethod("isValid");
                Method applyImpulseAtPoint = rigidBodyHandleClass.getMethod("applyImpulseAtPoint", Vector3dc.class, Vector3dc.class);
                Method getAngularVelocity = rigidBodyHandleClass.getMethod("getAngularVelocity");
                Method addLinearAndAngularVelocity = rigidBodyHandleClass.getMethod(
                        "addLinearAndAngularVelocity",
                        Vector3dc.class,
                        Vector3dc.class
                );
                Method getMassTracker = serverSubLevelClass.getMethod("getMassTracker");
                Method getMass = massDataClass.getMethod("getMass");
                Method logicalPose = serverSubLevelClass.getMethod("logicalPose");
                Method transformNormalInverse = pose3dClass.getMethod(
                        "transformNormalInverse",
                        Vector3dc.class,
                        Vector3d.class
                );
                hooks = new Hooks(
                        helper,
                        serverSubLevelClass,
                        getContainingPosition,
                        getContainingBlock,
                        rigidBodyHandleOf,
                        rigidBodyHandleIsValid,
                        applyImpulseAtPoint,
                        getAngularVelocity,
                        addLinearAndAngularVelocity,
                        getMassTracker,
                        getMass,
                        logicalPose,
                        transformNormalInverse
                );
                return hooks;
            } catch (ReflectiveOperationException | LinkageError e) {
                disabled = true;
                return null;
            }
        }
    }

    private record Hooks(
            Object helper,
            Class<?> serverSubLevelClass,
            Method getContainingPosition,
            Method getContainingBlock,
            Method rigidBodyHandleOf,
            Method rigidBodyHandleIsValid,
            Method applyImpulseAtPoint,
            Method getAngularVelocity,
            Method addLinearAndAngularVelocity,
            Method getMassTracker,
            Method getMass,
            Method logicalPose,
            Method transformNormalInverse
    ) {
    }
}
