package com.moratan251.psitweaks.common.compat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
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
    /** 作動式: 加重領域の連携のフック（作動式: 物理推進のフックとは別にし、片方が見つからなくてももう片方は止めない）。 */
    private static volatile ZoneHooks zoneHooks;
    private static volatile boolean zoneDisabled;

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

    /**
     * 作動式: 加重領域（2026-11-11 の設計 C）: 重心が {@code inside} のブロックにあるサブレベル（Create Aeronautics の機体など）を、
     * 世界座標の向き {@code direction} へ加速する（サーバー側）。加速度は {@code gravityFraction} × その場の重力の大きさで、重さに
     * よらない。重心をまっすぐ押すので機体は傾かない。押した数を返す。Sable がない・API が合わないときは 0。
     */
    public static int accelerateSubLevels(Level level, AABB bounds, Vec3 direction, double gravityFraction,
                                          Predicate<BlockPos> inside) {
        if (level == null || level.isClientSide || zoneDisabled || bounds == null || direction == null
                || direction.lengthSqr() < MIN_DIRECTION_LENGTH_SQUARED || gravityFraction <= 0.0D || !isLoaded()) {
            return 0;
        }
        ZoneHooks loaded = getZoneHooks();
        if (loaded == null) {
            return 0;
        }
        try {
            Vec3 unit = direction.normalize();
            Vector3d worldDirection = new Vector3d(unit.x, unit.y, unit.z);
            double gravity = gravityOf(loaded, level);
            Object box = loaded.boundingBoxFromAabb.newInstance(bounds);
            Iterable<?> subLevels = (Iterable<?>) loaded.getAllIntersecting.invoke(loaded.helper, level, box);
            if (subLevels == null) {
                return 0;
            }
            int pushed = 0;
            for (Object subLevel : subLevels) {
                if (!loaded.serverSubLevelClass.isInstance(subLevel)) {
                    continue;
                }
                Object massData = loaded.getMassTracker.invoke(subLevel);
                if (massData == null) {
                    continue;
                }
                Vector3dc localCenter = (Vector3dc) loaded.getCenterOfMass.invoke(massData);
                if (localCenter == null) {
                    continue;
                }
                double mass = ((Number) loaded.getMass.invoke(massData)).doubleValue();
                double impulse = SubLevelPushMath.impulsePerTick(mass, gravityFraction, gravity);
                if (impulse <= 0.0D) {
                    continue;
                }
                Object pose = loaded.logicalPose.invoke(subLevel);
                Vector3d center = (Vector3d) loaded.transformPosition.invoke(pose, localCenter, new Vector3d());
                if (!inside.test(BlockPos.containing(center.x, center.y, center.z))) {
                    continue;
                }
                Object handle = loaded.rigidBodyHandleOf.invoke(null, subLevel);
                if (handle == null || !((Boolean) loaded.rigidBodyHandleIsValid.invoke(handle))) {
                    continue;
                }
                // 力積はサブレベルの座標系で渡す（作動式: 物理推進と同じ直し方）。
                Vector3d local = (Vector3d) loaded.transformNormalInverse.invoke(pose, worldDirection, new Vector3d());
                if (local.lengthSquared() < MIN_DIRECTION_LENGTH_SQUARED) {
                    continue;
                }
                loaded.applyLinearImpulse.invoke(handle, local.normalize().mul(impulse));
                pushed++;
            }
            return pushed;
        } catch (ReflectiveOperationException | LinkageError | ClassCastException e) {
            zoneDisabled = true;
            return 0;
        }
    }

    /**
     * 作動式: 加重領域の連携のフックが Sable の今の版で揃うか（Sable があるときだけ意味がある。GameTest で、Sable を上げたときに
     * 連携が黙って止まっていないかを見るため）。
     */
    public static boolean zoneLinkAvailable() {
        return isLoaded() && getZoneHooks() != null && !zoneDisabled;
    }

    /** その場の Sable の重力の大きさ。取れなければ既定の 11（重力のフックだけは見つからなくても全体を止めない）。 */
    private static double gravityOf(ZoneHooks loaded, Level level) {
        if (loaded.getGravity == null) {
            return SubLevelPushMath.DEFAULT_GRAVITY;
        }
        try {
            Vector3dc gravity = (Vector3dc) loaded.getGravity.invoke(null, level);
            double length = gravity == null ? 0.0D : gravity.length();
            return Double.isFinite(length) && length > 0.0D ? length : SubLevelPushMath.DEFAULT_GRAVITY;
        } catch (ReflectiveOperationException | LinkageError | ClassCastException e) {
            return SubLevelPushMath.DEFAULT_GRAVITY;
        }
    }

    private static ZoneHooks getZoneHooks() {
        ZoneHooks loaded = zoneHooks;
        if (loaded != null || zoneDisabled) {
            return loaded;
        }
        synchronized (INIT_LOCK) {
            if (zoneHooks != null || zoneDisabled) {
                return zoneHooks;
            }
            try {
                Class<?> sableClass = Class.forName("dev.ryanhcode.sable.Sable");
                Class<?> serverSubLevelClass = Class.forName("dev.ryanhcode.sable.sublevel.ServerSubLevel");
                Class<?> rigidBodyHandleClass = Class.forName("dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle");
                Class<?> massDataClass = Class.forName("dev.ryanhcode.sable.api.physics.mass.MassData");
                Class<?> pose3dClass = Class.forName("dev.ryanhcode.sable.companion.math.Pose3d");
                Class<?> boundingBoxClass = Class.forName("dev.ryanhcode.sable.companion.math.BoundingBox3d");
                Class<?> boundingBoxInterface = Class.forName("dev.ryanhcode.sable.companion.math.BoundingBox3dc");

                Object helper = sableClass.getField("HELPER").get(null);
                Method getAllIntersecting = helper.getClass().getMethod("getAllIntersecting", Level.class, boundingBoxInterface);
                Constructor<?> boundingBoxFromAabb = boundingBoxClass.getConstructor(AABB.class);
                Method getMassTracker = serverSubLevelClass.getMethod("getMassTracker");
                Method getMass = massDataClass.getMethod("getMass");
                Method getCenterOfMass = massDataClass.getMethod("getCenterOfMass");
                Method logicalPose = serverSubLevelClass.getMethod("logicalPose");
                Method transformPosition = pose3dClass.getMethod("transformPosition", Vector3dc.class, Vector3d.class);
                Method transformNormalInverse = pose3dClass.getMethod("transformNormalInverse", Vector3dc.class, Vector3d.class);
                Method rigidBodyHandleOf = rigidBodyHandleClass.getMethod("of", serverSubLevelClass);
                Method rigidBodyHandleIsValid = rigidBodyHandleClass.getMethod("isValid");
                Method applyLinearImpulse = rigidBodyHandleClass.getMethod("applyLinearImpulse", Vector3dc.class);
                Method getGravity = null;
                try {
                    getGravity = Class.forName("dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData")
                            .getMethod("getGravity", Level.class);
                } catch (ReflectiveOperationException | LinkageError ignored) {
                    // 重力が取れなければ既定の 11 を使う。
                }
                zoneHooks = new ZoneHooks(helper, serverSubLevelClass, getAllIntersecting, boundingBoxFromAabb,
                        getMassTracker, getMass, getCenterOfMass, logicalPose, transformPosition, transformNormalInverse,
                        rigidBodyHandleOf, rigidBodyHandleIsValid, applyLinearImpulse, getGravity);
                return zoneHooks;
            } catch (ReflectiveOperationException | LinkageError e) {
                zoneDisabled = true;
                return null;
            }
        }
    }

    private record ZoneHooks(
            Object helper,
            Class<?> serverSubLevelClass,
            Method getAllIntersecting,
            Constructor<?> boundingBoxFromAabb,
            Method getMassTracker,
            Method getMass,
            Method getCenterOfMass,
            Method logicalPose,
            Method transformPosition,
            Method transformNormalInverse,
            Method rigidBodyHandleOf,
            Method rigidBodyHandleIsValid,
            Method applyLinearImpulse,
            Method getGravity
    ) {
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
