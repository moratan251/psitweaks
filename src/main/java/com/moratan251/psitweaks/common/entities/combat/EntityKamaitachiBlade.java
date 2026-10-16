package com.moratan251.psitweaks.common.entities.combat;

import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
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
 * The wind blade of Trick: Kamaitachi: a wide crescent that flies straight ahead, cuts through every creature in
 * its way once and breaks apart on the first solid block.
 */
public class EntityKamaitachiBlade extends CombatSpellEntity {
    public static final float SPEED = 1.6F;
    public static final int MAX_LIFETIME = 15;
    /** How far above and below the blade's line a creature can be and still be cut. */
    private static final double VERTICAL_REACH = 0.7;

    private static final EntityDataAccessor<Float> DIR_X = SynchedEntityData.defineId(EntityKamaitachiBlade.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Y = SynchedEntityData.defineId(EntityKamaitachiBlade.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Z = SynchedEntityData.defineId(EntityKamaitachiBlade.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WIDTH = SynchedEntityData.defineId(EntityKamaitachiBlade.class, EntityDataSerializers.FLOAT);

    private final Set<UUID> struck = new HashSet<>();

    public EntityKamaitachiBlade(EntityType<?> type, Level level) {
        super(type, level);
    }

    public EntityKamaitachiBlade(Level level, Vec3 start, Vec3 direction, float width) {
        this(PsitweaksEntities.KAMAITACHI_BLADE.get(), level);
        Vec3 dir = direction.normalize();
        setPos(start.x, start.y, start.z);
        entityData.set(DIR_X, (float) dir.x);
        entityData.set(DIR_Y, (float) dir.y);
        entityData.set(DIR_Z, (float) dir.z);
        entityData.set(WIDTH, width);
        setDeltaMovement(dir.scale(SPEED));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DIR_X, 0.0F);
        builder.define(DIR_Y, 0.0F);
        builder.define(DIR_Z, 1.0F);
        builder.define(WIDTH, 2.0F);
    }

    public Vec3 getFlightDirection() {
        Vec3 dir = new Vec3(entityData.get(DIR_X), entityData.get(DIR_Y), entityData.get(DIR_Z));
        return dir.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : dir.normalize();
    }

    public float getWidth() {
        return entityData.get(WIDTH);
    }

    /** Horizontal unit vector across the blade. */
    public Vec3 getSide() {
        Vec3 side = getFlightDirection().cross(new Vec3(0.0, 1.0, 0.0));
        return side.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : side.normalize();
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(getWidth());
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
            cut(level, start, reach);
            if (blockHit.getType() != HitResult.Type.MISS) {
                shatter(level, reach);
                discard();
                return;
            }
        } else {
            level().addParticle(ParticleTypes.CLOUD, start.x, start.y, start.z, step.x * 0.05, 0.0, step.z * 0.05);
        }
        setPos(end.x, end.y, end.z);
    }

    private void cut(ServerLevel level, Vec3 from, Vec3 to) {
        LivingEntity caster = getCaster();
        Vec3 direction = getFlightDirection();
        Vec3 side = getSide();
        double halfWidth = getWidth() * 0.5;
        double length = to.subtract(from).length();
        AABB area = new AABB(from, to).inflate(halfWidth + 1.0, VERTICAL_REACH + 1.0, halfWidth + 1.0);
        DamageSource source = CombatSpellHelper.damageSource(level, PsitweaksDamageTypes.KAMAITACHI, this, caster);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (struck.contains(living.getUUID()) || !CombatSpellHelper.canHarm(caster, living, isSafeToPlayers())) {
                continue;
            }
            Vec3 offset = CombatSpellHelper.center(living).subtract(from);
            double along = offset.dot(direction);
            double across = Math.abs(offset.dot(side));
            double vertical = Math.abs(offset.y - direction.y * along);
            double bodyHalfWidth = living.getBbWidth() * 0.5;
            if (along < -bodyHalfWidth || along > length + bodyHalfWidth
                    || across > halfWidth + bodyHalfWidth || vertical > VERTICAL_REACH + living.getBbHeight() * 0.5) {
                continue;
            }
            struck.add(living.getUUID());
            if (living.hurt(source, getDamage())) {
                CombatSpellHelper.push(living, new Vec3(direction.x, 0.0, direction.z).scale(0.4).add(0.0, 0.1, 0.0));
                Vec3 center = CombatSpellHelper.center(living);
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
                CombatEffects.sound(level, center, SoundEvents.PLAYER_ATTACK_SWEEP, 0.7F, 1.6F);
            }
        }
    }

    private void shatter(ServerLevel level, Vec3 at) {
        level.sendParticles(ParticleTypes.CLOUD, at.x, at.y, at.z, 10, 0.4, 0.2, 0.4, 0.05);
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, at.x, at.y, at.z, 2, 0.3, 0.2, 0.3, 0.0);
        CombatEffects.sound(level, at, SoundEvents.WIND_CHARGE_BURST, 0.6F, 1.5F);
    }
}
