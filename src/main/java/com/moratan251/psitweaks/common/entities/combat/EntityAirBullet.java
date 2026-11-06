package com.moratan251.psitweaks.common.entities.combat;

import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The bullet of Trick: Air Bullet: a ball of compressed air that flies straight ahead. The pressure wave around it
 * strikes each creature in its way once (within {@link #HIT_REACH} of its line). A creature the ball itself touches
 * ({@link #DIRECT_REACH}) takes the full damage and stops the bullet there; one only the pressure wave grazes takes
 * {@link #GRAZE_DAMAGE_FACTOR} of it, and the bullet flies on. It also bursts on the first solid block.
 */
public class EntityAirBullet extends CombatSpellEntity {
    public static final float SPEED = 1.6F;
    public static final int MAX_LIFETIME = 15;
    /**
     * How far from the bullet's line, sideways and up or down alike, a creature can be and still be struck (measured to
     * the creature's centre, plus half its width or height). Power does not change it.
     */
    public static final float HIT_REACH = 0.7F;
    /** The radius of the ball of air: a creature within it (measured as {@link #HIT_REACH}) is struck directly. */
    public static final float DIRECT_REACH = 0.35F;
    /** The share of the damage a creature takes when only the pressure wave around the ball grazes it. */
    public static final float GRAZE_DAMAGE_FACTOR = 1.0F / 3.0F;

    private static final EntityDataAccessor<Float> DIR_X = SynchedEntityData.defineId(EntityAirBullet.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Y = SynchedEntityData.defineId(EntityAirBullet.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Z = SynchedEntityData.defineId(EntityAirBullet.class, EntityDataSerializers.FLOAT);

    private final Set<UUID> struck = new HashSet<>();

    public EntityAirBullet(EntityType<?> type, Level level) {
        super(type, level);
    }

    public EntityAirBullet(Level level, Vec3 start, Vec3 direction) {
        this(PsitweaksEntities.AIR_BULLET.get(), level);
        Vec3 dir = direction.normalize();
        setPos(start.x, start.y, start.z);
        entityData.set(DIR_X, (float) dir.x);
        entityData.set(DIR_Y, (float) dir.y);
        entityData.set(DIR_Z, (float) dir.z);
        setDeltaMovement(dir.scale(SPEED));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DIR_X, 0.0F);
        builder.define(DIR_Y, 0.0F);
        builder.define(DIR_Z, 1.0F);
    }

    public Vec3 getFlightDirection() {
        Vec3 dir = new Vec3(entityData.get(DIR_X), entityData.get(DIR_Y), entityData.get(DIR_Z));
        return dir.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : dir.normalize();
    }

    /** Horizontal unit vector across the bullet's path. */
    public Vec3 getSide() {
        Vec3 side = getFlightDirection().cross(new Vec3(0.0, 1.0, 0.0));
        return side.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : side.normalize();
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(1.5);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 start = position();
        Vec3 step = getFlightDirection().scale(SPEED);
        Vec3 end = start.add(step);
        if (level() instanceof ServerLevel level) {
            if (tickCount > MAX_LIFETIME) {
                discard();
                return;
            }
            BlockHitResult blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, this));
            Vec3 reach = blockHit.getType() == HitResult.Type.MISS ? end : blockHit.getLocation();
            Vec3 stoppedAt = strike(level, start, reach);
            if (stoppedAt != null) {
                burst(level, stoppedAt);
                discard();
                return;
            }
            if (blockHit.getType() != HitResult.Type.MISS) {
                burst(level, reach);
                discard();
                return;
            }
        } else {
            level().addParticle(ParticleTypes.CLOUD, start.x, start.y, start.z, step.x * 0.05, 0.0, step.z * 0.05);
        }
        setPos(end.x, end.y, end.z);
    }

    /**
     * Strikes the creatures between {@code from} and {@code to} in the order the bullet reaches them: each one the
     * pressure wave grazes, up to the first one the ball hits directly, where the bullet stops. Creatures beyond that
     * point are left alone.
     *
     * @return where the bullet hit a creature directly and stops, or null when it flies on
     */
    @Nullable
    private Vec3 strike(ServerLevel level, Vec3 from, Vec3 to) {
        LivingEntity caster = getCaster();
        Vec3 direction = getFlightDirection();
        Vec3 side = getSide();
        double length = to.subtract(from).length();
        AABB area = new AABB(from, to).inflate(HIT_REACH + 1.0);
        List<Hit> hits = new ArrayList<>();
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (struck.contains(living.getUUID()) || !canAffect(caster, living)) {
                continue;
            }
            Vec3 offset = CombatSpellHelper.center(living).subtract(from);
            double along = offset.dot(direction);
            double across = Math.abs(offset.dot(side));
            double vertical = Math.abs(offset.y - direction.y * along);
            double bodyHalfWidth = living.getBbWidth() * 0.5;
            double bodyHalfHeight = living.getBbHeight() * 0.5;
            if (along < -bodyHalfWidth || along > length + bodyHalfWidth
                    || across > HIT_REACH + bodyHalfWidth || vertical > HIT_REACH + bodyHalfHeight) {
                continue;
            }
            boolean direct = across <= DIRECT_REACH + bodyHalfWidth && vertical <= DIRECT_REACH + bodyHalfHeight;
            // Where along its line the bullet first reaches the creature's body.
            hits.add(new Hit(living, Mth.clamp(along - bodyHalfWidth, 0.0, length), direct));
        }
        hits.sort(Comparator.comparingDouble(Hit::reach));
        DamageSource source = AttackRules.source(level, PsitweaksDamageTypes.AIR_BULLET, this, caster);
        for (Hit hit : hits) {
            LivingEntity living = hit.living();
            struck.add(living.getUUID());
            float damage = hit.direct() ? getDamage() : getDamage() * GRAZE_DAMAGE_FACTOR;
            if (harm(caster, living, source, damage)) {
                CombatSpellHelper.push(living, new Vec3(direction.x, 0.0, direction.z).scale(0.4).add(0.0, 0.1, 0.0));
                Vec3 center = CombatSpellHelper.center(living);
                level.sendParticles(ParticleTypes.SMALL_GUST, center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
                CombatEffects.sound(level, center, SoundEvents.PLAYER_ATTACK_STRONG, 0.7F, 1.4F);
            }
            if (hit.direct()) {
                return from.add(direction.scale(hit.reach()));
            }
        }
        return null;
    }

    /** A creature in the bullet's way this tick: how far along the line the bullet reaches it, and whether directly. */
    private record Hit(LivingEntity living, double reach, boolean direct) {
    }

    private void burst(ServerLevel level, Vec3 at) {
        level.sendParticles(ParticleTypes.CLOUD, at.x, at.y, at.z, 10, 0.4, 0.2, 0.4, 0.05);
        level.sendParticles(ParticleTypes.GUST, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
        CombatEffects.sound(level, at, SoundEvents.WIND_CHARGE_BURST, 0.6F, 1.5F);
    }
}
