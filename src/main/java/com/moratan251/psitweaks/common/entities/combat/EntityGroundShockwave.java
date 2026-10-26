package com.moratan251.psitweaks.common.entities.combat;

import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The tremor of Trick: Mine Genesis: a ring that runs outward along the ground. Everything standing on the ground
 * when the ring passes is hit once and thrown upward; anything in the air is spared.
 */
public class EntityGroundShockwave extends CombatSpellEntity {
    /** Blocks the ring travels per tick. */
    public static final float SPEED = 0.8F;
    /** Ticks the ring keeps fading after reaching its full radius. */
    public static final int FADE_TICKS = 8;
    private static final double EDGE_DAMAGE_FACTOR = 0.7;
    /** How high above the ground something may be and still count as standing on it. */
    private static final double GROUND_TOLERANCE = 1.0;

    private static final EntityDataAccessor<Float> MAX_RADIUS =
            SynchedEntityData.defineId(EntityGroundShockwave.class, EntityDataSerializers.FLOAT);

    private final Set<UUID> struck = new HashSet<>();
    private float launch = 0.5F;

    public EntityGroundShockwave(EntityType<?> type, Level level) {
        super(type, level);
    }

    public EntityGroundShockwave(Level level, Vec3 groundCenter, float maxRadius, float launch) {
        this(PsitweaksEntities.GROUND_SHOCKWAVE.get(), level);
        setPos(groundCenter.x, groundCenter.y, groundCenter.z);
        entityData.set(MAX_RADIUS, maxRadius);
        this.launch = launch;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(MAX_RADIUS, 6.0F);
    }

    public float getMaxRadius() {
        return entityData.get(MAX_RADIUS);
    }

    public int getExpandTicks() {
        return Mth.ceil(getMaxRadius() / SPEED);
    }

    /** Radius of the ring front, growing smoothly between ticks. */
    public float getRadius(float partialTick) {
        return Math.min(getMaxRadius(), (tickCount + partialTick) * SPEED);
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        float radius = getMaxRadius() + 1.0F;
        return new AABB(getX() - radius, getY() - 1.0, getZ() - radius, getX() + radius, getY() + 2.0, getZ() + radius);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        if (tickCount > getExpandTicks() + FADE_TICKS) {
            discard();
            return;
        }
        if (tickCount > getExpandTicks()) {
            return;
        }
        float inner = Math.max(0.0F, (tickCount - 1) * SPEED - 0.5F);
        float outer = Math.min(getMaxRadius(), tickCount * SPEED) + 0.5F;
        LivingEntity caster = getCaster();
        hitEntities(level, caster, inner, outer);
        spawnDebris(level, Math.min(getMaxRadius(), tickCount * SPEED));
        if (tickCount == getExpandTicks() / 2 && tickCount > 0) {
            CombatEffects.sound(level, position(), SoundEvents.MACE_SMASH_GROUND, 0.9F, 0.6F);
        }
    }

    private void hitEntities(ServerLevel level, LivingEntity caster, float inner, float outer) {
        AABB area = new AABB(getX() - outer, getY() - 2.0, getZ() - outer, getX() + outer, getY() + 3.0, getZ() + outer);
        DamageSource source = AttackRules.source(level, PsitweaksDamageTypes.MINE_GENESIS, this, caster);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (struck.contains(living.getUUID()) || !canAffect(caster, living)) {
                continue;
            }
            double dx = living.getX() - getX();
            double dz = living.getZ() - getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance < inner || distance > outer || !isOnGround(level, living)) {
                continue;
            }
            struck.add(living.getUUID());
            float dealt = CombatSpellHelper.falloff(getDamage(), distance, getMaxRadius(), EDGE_DAMAGE_FACTOR);
            if (harm(caster, living, source, dealt)) {
                Vec3 outward = distance > 0.01 ? new Vec3(dx / distance, 0.0, dz / distance).scale(0.25) : Vec3.ZERO;
                CombatSpellHelper.push(living, outward.add(0.0, launch, 0.0));
            }
        }
    }

    private boolean isOnGround(Level level, LivingEntity living) {
        if (living.onGround()) {
            return true;
        }
        Double ground = CombatSpellHelper.findGroundY(level, living.getX(), living.getY() + 0.5, living.getZ(), 3);
        return ground != null && living.getY() - ground <= GROUND_TOLERANCE;
    }

    private void spawnDebris(ServerLevel level, float radius) {
        int points = Mth.clamp((int) (radius * Mth.TWO_PI / 1.1F), 6, 48);
        float phase = random.nextFloat() * Mth.TWO_PI;
        for (int i = 0; i < points; i++) {
            float angle = phase + Mth.TWO_PI * i / points;
            double x = getX() + Mth.cos(angle) * radius;
            double z = getZ() + Mth.sin(angle) * radius;
            Double groundY = CombatSpellHelper.findGroundY(level, x, getY() + 2.0, z, 5);
            if (groundY == null) {
                continue;
            }
            BlockState state = level.getBlockState(BlockPos.containing(x, groundY - 0.01, z));
            if (state.isAir() || state.getRenderShape() == RenderShape.INVISIBLE) {
                continue;
            }
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), x, groundY + 0.1, z,
                    3, 0.15, 0.05, 0.15, 0.25);
            if (i % 4 == 0) {
                level.sendParticles(ParticleTypes.POOF, x, groundY + 0.2, z, 1, 0.1, 0.05, 0.1, 0.02);
            }
        }
    }
}
