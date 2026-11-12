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
     * 作動式: 加重領域（2026-11-11 の設計 C、2026-11-12 から入っている部分の割合で押し、ある程度傾ける。{@link #samplePush}）:
     * 重心が {@code inside} のブロックにあるサブレベル（Create Aeronautics の機体など）を、
     * 世界座標の向き {@code direction} へ加速する（サーバー側）。加速度は {@code gravityFraction} × その場の重力の大きさで、重さに
     * よらない。重心をまっすぐ押すので機体は傾かない。押した数を返す。Sable がない・API が合わないときは 0。
     */
    public static int accelerateSubLevels(Level level, AABB bounds, Vec3 direction, double gravityFraction,
                                          Predicate<BlockPos> inside) {
        if (direction == null || direction.lengthSqr() < MIN_DIRECTION_LENGTH_SQUARED || gravityFraction <= 0.0D) {
            return 0;
        }
        Vec3 push = direction.normalize().scale(gravityFraction);
        return accelerateSubLevels(level, bounds, pos -> inside.test(pos) ? push : null);
    }

    /**
     * {@link #accelerateSubLevels(Level, AABB, Vec3, double, Predicate)} の、機体ごとに押す力を決める形: {@code pushAt} は
     * 重心のあるブロックで、向き × 重力に対する割合（大きさが割合）を返す。null か 0 ならその機体は押さない。重なった加重領域の
     * 押す力を足すのに使う。
     */
    public static int accelerateSubLevels(Level level, AABB bounds, java.util.function.Function<BlockPos, Vec3> pushAt) {
        if (level == null || level.isClientSide || zoneDisabled || bounds == null || !isLoaded()) {
            return 0;
        }
        ZoneHooks loaded = getZoneHooks();
        if (loaded == null) {
            return 0;
        }
        try {
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
                Object pose = loaded.logicalPose.invoke(subLevel);
                Vector3d center = (Vector3d) loaded.transformPosition.invoke(pose, localCenter, new Vector3d());
                ShipPush shipPush = samplePush(level, subLevel, pose, localCenter, center, pushAt);
                if (shipPush == null) {
                    continue;
                }
                Vec3 push = shipPush.force();
                double mass = ((Number) loaded.getMass.invoke(massData)).doubleValue();
                double impulse = SubLevelPushMath.impulsePerTick(mass, push.length(), gravity);
                if (impulse <= 0.0D) {
                    continue;
                }
                Object handle = loaded.rigidBodyHandleOf.invoke(null, subLevel);
                if (handle == null || !((Boolean) loaded.rigidBodyHandleIsValid.invoke(handle))) {
                    continue;
                }
                // 力積はサブレベルの座標系で渡す（作動式: 物理推進と同じ直し方）。
                Vec3 unit = push.normalize();
                Vector3d worldDirection = new Vector3d(unit.x, unit.y, unit.z);
                Vector3d local = (Vector3d) loaded.transformNormalInverse.invoke(pose, worldDirection, new Vector3d());
                if (local.lengthSquared() < MIN_DIRECTION_LENGTH_SQUARED) {
                    continue;
                }
                Vector3d localImpulse = local.normalize().mul(impulse);
                TiltHooks tilt = getTiltHooks();
                if (shipPush.localPoint() != null && tilt != null && spinsSlowly(tilt, handle)) {
                    // 入っている部分の側を押して、ある程度傾ける。回す力積は 重心からの位置 × 力積。点に力積を加える Sable の
                    // 窓口は止まっている機体を起こさないので、起こす窓口（直進と回転を一緒に渡す）を使う。
                    Vector3d arm = new Vector3d(shipPush.localPoint()).sub(localCenter);
                    Vector3d angular = arm.cross(localImpulse, new Vector3d());
                    tilt.applyLinearAndAngularImpulse().invoke(handle, localImpulse, angular, true);
                } else {
                    loaded.applyLinearImpulse.invoke(handle, localImpulse);
                }
                pushed++;
            }
            return pushed;
        } catch (ReflectiveOperationException | LinkageError | ClassCastException e) {
            zoneDisabled = true;
            return 0;
        }
    }

    /** 機体を押す力（向き × 重力に対する割合）と、押す点（サブレベルの座標、null なら重心をまっすぐ押す）。 */
    private record ShipPush(Vec3 force, Vector3d localPoint) {
    }

    /** 機体の形を粗く調べる格子の、1 辺あたりの点の数の上限（最大 4 × 4 × 4 = 64 点）。 */
    private static final int MAX_SAMPLES_PER_AXIS = 4;
    /** 格子の 1 マスのおよその大きさ（ブロック）。小さい機体は点を減らす（一辺 2 ブロック以上なら 2 点以上）。 */
    private static final double SAMPLE_CELL = 2.0D;
    /** 押す点を、重心から「入っている部分の真ん中」へどれだけ寄せるか（1 で真ん中、0 で重心）。傾きを抑えるため半分（仮）。 */
    private static final double TILT_SHARE = 0.5D;
    /** これより速く回っている機体は、傾ける力をかけずに重心を押す（rad/s、仮）。 */
    private static final double MAX_TILT_SPIN = 1.0D;

    /**
     * 機体を押す力と押す点を、機体の外箱（世界の座標）の中を最大 {@link #MAX_SAMPLES_PER_AXIS}³ 点だけ調べて粗く決める
     * （2026-11-12 ユーザー: 傾きをある程度効かせたいが、軽さ優先で粗くてよい）。点のうち機体のブロックがあるものを数え、
     * {@code pushAt} が力を返す点（領域に入っている部分）の力を足して、ブロックのある点の数で割る（入っている割合だけ押す）。押す点は
     * 重心と、入っている点の真ん中の間（{@link #TILT_SHARE}）。粗く調べられないとき（Sable の版が合わない、薄い機体で点が当たらない）
     * は、今までどおり重心のブロックで決めて重心を押す。
     */
    private static ShipPush samplePush(Level level, Object subLevel, Object pose, Vector3dc localCenter, Vector3d worldCenter,
                                       java.util.function.Function<BlockPos, Vec3> pushAt) {
        TiltHooks tilt = getTiltHooks();
        if (tilt != null) {
            try {
                Object box = tilt.boundingBox().invoke(subLevel);
                if (box != null) {
                    double minX = (Double) tilt.minX().invoke(box);
                    double minY = (Double) tilt.minY().invoke(box);
                    double minZ = (Double) tilt.minZ().invoke(box);
                    double sizeX = (Double) tilt.maxX().invoke(box) - minX;
                    double sizeY = (Double) tilt.maxY().invoke(box) - minY;
                    double sizeZ = (Double) tilt.maxZ().invoke(box) - minZ;
                    int nx = samples(sizeX);
                    int ny = samples(sizeY);
                    int nz = samples(sizeZ);
                    int solid = 0;
                    int inside = 0;
                    Vec3 sum = Vec3.ZERO;
                    Vector3d centroid = new Vector3d();
                    for (int i = 0; i < nx; i++) {
                        for (int j = 0; j < ny; j++) {
                            for (int k = 0; k < nz; k++) {
                                Vec3 world = new Vec3(minX + (i + 0.5D) * sizeX / nx, minY + (j + 0.5D) * sizeY / ny,
                                        minZ + (k + 0.5D) * sizeZ / nz);
                                Vec3 local = (Vec3) tilt.transformPositionInverse().invoke(pose, world);
                                if (level.getBlockState(BlockPos.containing(local)).isAir()) {
                                    continue;
                                }
                                solid++;
                                Vec3 push = pushAt.apply(BlockPos.containing(world));
                                if (push == null || push.lengthSqr() < MIN_DIRECTION_LENGTH_SQUARED) {
                                    continue;
                                }
                                inside++;
                                sum = sum.add(push);
                                centroid.add(local.x, local.y, local.z);
                            }
                        }
                    }
                    if (solid > 0) {
                        if (inside == 0) {
                            return null;
                        }
                        centroid.div(inside);
                        Vector3d point = new Vector3d(localCenter).lerp(centroid, TILT_SHARE);
                        return new ShipPush(sum.scale(1.0D / solid), point);
                    }
                }
            } catch (ReflectiveOperationException | LinkageError | ClassCastException e) {
                tiltHooksMissing = true;
            }
        }
        Vec3 push = pushAt.apply(BlockPos.containing(worldCenter.x, worldCenter.y, worldCenter.z));
        return push == null || push.lengthSqr() < MIN_DIRECTION_LENGTH_SQUARED ? null : new ShipPush(push, null);
    }

    private static int samples(double size) {
        return Math.max(1, Math.min(MAX_SAMPLES_PER_AXIS, (int) Math.ceil(size / SAMPLE_CELL)));
    }

    private static boolean spinsSlowly(TiltHooks tilt, Object handle) throws ReflectiveOperationException {
        Vector3dc angular = (Vector3dc) tilt.getAngularVelocity().invoke(handle);
        return angular == null || angular.lengthSquared() <= MAX_TILT_SPIN * MAX_TILT_SPIN;
    }

    /** 機体の形を粗く調べて傾けるためのフック（無ければ傾けず、今までどおり重心を押す）。 */
    private record TiltHooks(Method boundingBox, Method minX, Method minY, Method minZ, Method maxX, Method maxY, Method maxZ,
                             Method transformPositionInverse, Method applyLinearAndAngularImpulse, Method getAngularVelocity) {
    }

    private static volatile TiltHooks tiltHooks;
    private static volatile boolean tiltHooksMissing;

    private static TiltHooks getTiltHooks() {
        if (tiltHooks != null || tiltHooksMissing) {
            return tiltHooks;
        }
        synchronized (INIT_LOCK) {
            if (tiltHooks != null || tiltHooksMissing) {
                return tiltHooks;
            }
            try {
                Class<?> subLevelClass = Class.forName("dev.ryanhcode.sable.sublevel.SubLevel");
                Class<?> boundsClass = Class.forName("dev.ryanhcode.sable.companion.math.BoundingBox3dc");
                Class<?> poseClass = Class.forName("dev.ryanhcode.sable.companion.math.Pose3dc");
                Class<?> handleClass = Class.forName("dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle");
                tiltHooks = new TiltHooks(subLevelClass.getMethod("boundingBox"),
                        boundsClass.getMethod("minX"), boundsClass.getMethod("minY"), boundsClass.getMethod("minZ"),
                        boundsClass.getMethod("maxX"), boundsClass.getMethod("maxY"), boundsClass.getMethod("maxZ"),
                        poseClass.getMethod("transformPositionInverse", Vec3.class),
                        handleClass.getMethod("applyLinearAndAngularImpulse", Vector3dc.class, Vector3dc.class, boolean.class),
                        handleClass.getMethod("getAngularVelocity"));
            } catch (ReflectiveOperationException | LinkageError e) {
                tiltHooksMissing = true;
            }
            return tiltHooks;
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
