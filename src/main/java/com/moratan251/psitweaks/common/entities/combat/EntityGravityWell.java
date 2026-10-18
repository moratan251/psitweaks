package com.moratan251.psitweaks.common.entities.combat;

import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.network.MessageSphereFlash;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import vazkii.psi.api.spell.ISpellImmune;

/**
 * The singularity of Trick: Gravity Collapse. For three seconds it drags creatures within its radius towards the
 * centre, then collapses: everything close to the centre takes armour-piercing damage.
 */
public class EntityGravityWell extends CombatSpellEntity {
    public static final int CHARGE_TICKS = 60;
    private static final int LINGER_TICKS = 4;
    /** Pull acceleration per tick at the edge; wider wells pull harder so the edge still reaches the centre in time. */
    private static final double PULL_BASE = 0.15;
    private static final double PULL_PER_RADIUS = 0.008;
    /** Upward kick for creatures on the ground, so they leave it (and its friction) and are dragged through the air. */
    private static final double GROUND_LIFT = 0.09;
    private static final double AIR_LIFT = 0.035;
    private static final double MIN_COLLAPSE_RADIUS = 3.0;
    private static final double COLLAPSE_RADIUS_FACTOR = 0.6;
    private static final double EDGE_DAMAGE_FACTOR = 0.5;

    private static final EntityDataAccessor<Float> RADIUS =
            SynchedEntityData.defineId(EntityGravityWell.class, EntityDataSerializers.FLOAT);

    public EntityGravityWell(EntityType<?> type, Level level) {
        super(type, level);
    }

    public EntityGravityWell(Level level, Vec3 center, float radius) {
        this(PsitweaksEntities.GRAVITY_WELL.get(), level);
        setPos(center.x, center.y, center.z);
        entityData.set(RADIUS, radius);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(RADIUS, 6.0F);
    }

    public float getRadius() {
        return entityData.get(RADIUS);
    }

    /** 0..1 over the pull phase. */
    public float getCharge(float partialTick) {
        return Math.min(1.0F, (tickCount + partialTick) / CHARGE_TICKS);
    }

    public double getCollapseRadius() {
        return Math.max(MIN_COLLAPSE_RADIUS, getRadius() * COLLAPSE_RADIUS_FACTOR);
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(getRadius());
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level)) {
            if (tickCount < CHARGE_TICKS) {
                inflowParticles();
            }
            return;
        }
        if (tickCount > CHARGE_TICKS + LINGER_TICKS) {
            discard();
            return;
        }
        LivingEntity caster = getCaster();
        if (tickCount < CHARGE_TICKS) {
            pull(level, caster);
            if (tickCount % 20 == 1) {
                CombatEffects.sound(level, position(), SoundEvents.PORTAL_AMBIENT, 1.0F, 0.6F + tickCount / 60.0F);
            }
        } else if (tickCount == CHARGE_TICKS) {
            collapse(level, caster);
        }
    }

    private void pull(ServerLevel level, LivingEntity caster) {
        double radius = getRadius();
        Vec3 center = position();
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius))) {
            if (!CombatSpellHelper.canHarm(caster, living, isSafeToPlayers()) || ISpellImmune.isImmune(living)) {
                continue;
            }
            Vec3 offset = center.subtract(CombatSpellHelper.center(living));
            double distance = offset.length();
            if (distance > radius || distance < 0.05) {
                continue;
            }
            double strength = (PULL_BASE + PULL_PER_RADIUS * radius) * (0.6 + 0.4 * (1.0 - distance / radius));
            Vec3 pull = offset.scale(strength / distance);
            if (distance < 2.0) {
                // Hold things near the centre instead of letting them overshoot.
                living.setDeltaMovement(living.getDeltaMovement().scale(0.5));
            }
            CombatSpellHelper.push(living, pull.add(0.0, living.onGround() ? GROUND_LIFT : AIR_LIFT, 0.0));
            living.fallDistance = 0.0F;
        }
    }

    private void collapse(ServerLevel level, LivingEntity caster) {
        double collapseRadius = getCollapseRadius();
        Vec3 center = position();
        DamageSource source = CombatSpellHelper.damageSource(level, PsitweaksDamageTypes.GRAVITY_COLLAPSE, this, caster);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(collapseRadius + 1.0))) {
            if (!CombatSpellHelper.canHarm(caster, living, isSafeToPlayers())) {
                continue;
            }
            double distance = Math.max(0.0, CombatSpellHelper.center(living).distanceTo(center) - living.getBbWidth() * 0.5);
            if (distance <= collapseRadius) {
                living.hurt(source, CombatSpellHelper.falloff(getDamage(), distance, collapseRadius, EDGE_DAMAGE_FACTOR));
            }
        }
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z, 80, 0.3, 0.3, 0.3, 0.6);
        level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z, 2, 0.4, 0.4, 0.4, 0.0);
        level.sendParticles(ParticleTypes.SONIC_BOOM, center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
        CombatEffects.sphereFlash(level, center, getRadius(), getColor(), MessageSphereFlash.Style.IMPLOSION, 1.0F);
        CombatEffects.sound(level, center, SoundEvents.GENERIC_EXPLODE, 1.3F, 0.6F);
        CombatEffects.sound(level, center, SoundEvents.RESPAWN_ANCHOR_DEPLETE, 1.0F, 0.7F);
    }

    private void inflowParticles() {
        double radius = getRadius();
        // More particles for wider wells, so a large one does not look empty.
        int count = 4 + (int) (radius / 3.0);
        for (int i = 0; i < count; i++) {
            double theta = random.nextDouble() * Math.PI * 2.0;
            double phi = Math.acos(random.nextDouble() * 2.0 - 1.0);
            double distance = radius * (0.5 + random.nextDouble() * 0.5);
            double dx = Math.sin(phi) * Math.cos(theta) * distance;
            double dy = Math.cos(phi) * distance * 0.6;
            double dz = Math.sin(phi) * Math.sin(theta) * distance;
            // Portal particles drift from the given offset back to their origin: here, into the centre.
            level().addParticle(ParticleTypes.PORTAL, getX(), getY(), getZ(), dx, dy, dz);
        }
    }
}
