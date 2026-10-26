package com.moratan251.psitweaks.common.entities.combat;

import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.network.MessageSphereFlash;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * One meteor of Trick: Starfall. It waits for its turn (a target ring marks where it will land), then falls along a
 * straight line from the sky and explodes on arrival. It passes through terrain and never breaks blocks.
 */
public class EntityStarfallMeteor extends CombatSpellEntity {
    public static final int FLIGHT_TICKS = 14;
    public static final float IMPACT_RADIUS = 2.5F;
    private static final double EDGE_DAMAGE_FACTOR = 0.5;
    private static final int BURN_SECONDS = 3;

    // Where the fall starts, relative to the landing spot. The entity itself stays on the landing spot, whose
    // position is synced exactly; absolute coordinates in floats would drift far from the world origin.
    private static final EntityDataAccessor<Float> FALL_X = SynchedEntityData.defineId(EntityStarfallMeteor.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> FALL_Y = SynchedEntityData.defineId(EntityStarfallMeteor.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> FALL_Z = SynchedEntityData.defineId(EntityStarfallMeteor.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DELAY = SynchedEntityData.defineId(EntityStarfallMeteor.class, EntityDataSerializers.INT);
    /** Game time the meteor was cast; timing uses world time so clients that saw it late still stay in step. */
    private static final EntityDataAccessor<Long> CAST_TIME = SynchedEntityData.defineId(EntityStarfallMeteor.class, EntityDataSerializers.LONG);

    public EntityStarfallMeteor(EntityType<?> type, Level level) {
        super(type, level);
    }

    public EntityStarfallMeteor(Level level, Vec3 start, Vec3 target, int delay) {
        this(PsitweaksEntities.STARFALL_METEOR.get(), level);
        setPos(target.x, target.y, target.z);
        entityData.set(FALL_X, (float) (start.x - target.x));
        entityData.set(FALL_Y, (float) (start.y - target.y));
        entityData.set(FALL_Z, (float) (start.z - target.z));
        entityData.set(DELAY, delay);
        entityData.set(CAST_TIME, level.getGameTime());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FALL_X, 0.0F);
        builder.define(FALL_Y, 0.0F);
        builder.define(FALL_Z, 0.0F);
        builder.define(DELAY, 0);
        builder.define(CAST_TIME, 0L);
    }

    public Vec3 getStart() {
        return getTarget().add(entityData.get(FALL_X), entityData.get(FALL_Y), entityData.get(FALL_Z));
    }

    /** The landing spot, which is where the entity stays. */
    public Vec3 getTarget() {
        return position();
    }

    public int getDelay() {
        return entityData.get(DELAY);
    }

    /** Ticks since the cast, smoothed between ticks. */
    public float getAge(float partialTick) {
        return (float) (level().getGameTime() - entityData.get(CAST_TIME)) + partialTick;
    }

    /** 0 while waiting, then 0..1 along the fall. */
    public float getProgress(float partialTick) {
        return Mth.clamp((getAge(partialTick) - getDelay()) / FLIGHT_TICKS, 0.0F, 1.0F);
    }

    public boolean isFalling(float partialTick) {
        return getAge(partialTick) >= getDelay();
    }

    /** Where the meteor is at this moment of its fall. */
    public Vec3 getFlightPosition(float partialTick) {
        return getStart().lerp(getTarget(), getProgress(partialTick));
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return new AABB(getStart(), getTarget()).inflate(IMPACT_RADIUS + 1.0);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level)) {
            Vec3 position = getFlightPosition(0.0F);
            if (isFalling(0.0F)) {
                for (int i = 0; i < 3; i++) {
                    level().addParticle(ParticleTypes.FLAME, position.x + (random.nextDouble() - 0.5) * 0.4,
                            position.y + (random.nextDouble() - 0.5) * 0.4, position.z + (random.nextDouble() - 0.5) * 0.4,
                            0.0, 0.02, 0.0);
                }
                level().addParticle(ParticleTypes.LARGE_SMOKE, position.x, position.y, position.z, 0.0, 0.03, 0.0);
            }
            return;
        }
        int age = (int) getAge(0.0F);
        if (age == getDelay()) {
            CombatEffects.sound(level, getStart(), SoundEvents.FIRECHARGE_USE, 2.0F, 0.6F);
        }
        if (age >= getDelay() + FLIGHT_TICKS) {
            impact(level);
            discard();
        }
    }

    private void impact(ServerLevel level) {
        Vec3 at = getTarget();
        LivingEntity caster = getCaster();
        DamageSource source = AttackRules.source(level, PsitweaksDamageTypes.STARFALL, this, caster);
        AABB area = new AABB(at, at).inflate(IMPACT_RADIUS + 1.0);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (!canAffect(caster, living)) {
                continue;
            }
            Vec3 center = CombatSpellHelper.center(living);
            double distance = Math.max(0.0, center.distanceTo(at) - living.getBbWidth() * 0.5);
            if (distance > IMPACT_RADIUS) {
                continue;
            }
            if (harm(caster, living, source, CombatSpellHelper.falloff(getDamage(), distance, IMPACT_RADIUS, EDGE_DAMAGE_FACTOR))) {
                living.igniteForSeconds(BURN_SECONDS);
                Vec3 away = center.subtract(at);
                Vec3 horizontal = new Vec3(away.x, 0.0, away.z);
                horizontal = horizontal.lengthSqr() < 1.0E-4 ? Vec3.ZERO : horizontal.normalize().scale(0.6);
                CombatSpellHelper.push(living, horizontal.add(0.0, 0.35, 0.0));
            }
        }
        level.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y + 0.5, at.z, 3, 0.8, 0.4, 0.8, 0.0);
        level.sendParticles(ParticleTypes.LAVA, at.x, at.y + 0.3, at.z, 12, 0.6, 0.2, 0.6, 0.0);
        level.sendParticles(ParticleTypes.FLAME, at.x, at.y + 0.3, at.z, 30, 1.0, 0.3, 1.0, 0.08);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, at.x, at.y + 0.5, at.z, 12, 0.8, 0.4, 0.8, 0.04);
        CombatEffects.sphereFlash(level, at.add(0.0, 0.5, 0.0), IMPACT_RADIUS + 1.0F, getColor(),
                MessageSphereFlash.Style.SHOCKWAVE, 1.3F);
        CombatEffects.sound(level, at, SoundEvents.GENERIC_EXPLODE, 1.4F, 0.85F + random.nextFloat() * 0.2F);
    }
}
