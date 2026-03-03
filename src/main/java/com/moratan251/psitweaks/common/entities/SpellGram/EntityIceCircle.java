package com.moratan251.psitweaks.common.entities.SpellGram;

import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EntityIceCircle extends SpellGramObject {

    private static final String NBT_SAFE_TO_PLAYERS = "SafeToPlayers";
    private static final String NBT_VISUAL_SCALE = "VisualScale";

    private static final int RING_PARTICLE_COUNT = 12;
    private static final int INNER_PARTICLE_COUNT = 4;
    private static final double VERTICAL_RANGE = 1.0D;
    private static final int FREEZE_TICKS_PER_HIT = 140;

    private static final EntityDataAccessor<Boolean> DATA_SAFE_TO_PLAYERS =
            SynchedEntityData.defineId(EntityIceCircle.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_VISUAL_SCALE =
            SynchedEntityData.defineId(EntityIceCircle.class, EntityDataSerializers.FLOAT);

    public EntityIceCircle(EntityType<? extends EntityIceCircle> type, Level level) {
        super(type, level);
    }

    public EntityIceCircle(Level level) {
        this(PsitweaksEntities.ICE_CIRCLE.get(), level);
    }

    @Override
    protected void defineSpellGramSynchedData() {
        this.entityData.define(DATA_SAFE_TO_PLAYERS, false);
        this.entityData.define(DATA_VISUAL_SCALE, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide && !isRemoved()) {
            spawnClientParticles();
        }
    }

    @Override
    protected DamageSource createAreaDamageSource(@Nullable LivingEntity caster) {
        return this.damageSources().freeze();
    }

    @Override
    protected boolean canDamageEntity(LivingEntity target) {
        return !(isSafeToPlayers() && target instanceof Player);
    }

    @Override
    protected void onAreaDamageApplied(LivingEntity target, float damage, DamageSource source) {
        target.setTicksFrozen(target.getTicksFrozen() + FREEZE_TICKS_PER_HIT);
    }

    @Override
    protected void applyAreaDamage() {
        LivingEntity caster = getCaster();
        float horizontalRadius = getDamageRadius();
        if (horizontalRadius <= 0.0F || getAoEDamage() <= 0.0F) {
            return;
        }

        double radiusSq = horizontalRadius * horizontalRadius;
        AABB area = new AABB(
                getX() - horizontalRadius,
                getY() - VERTICAL_RANGE,
                getZ() - horizontalRadius,
                getX() + horizontalRadius,
                getY() + VERTICAL_RANGE,
                getZ() + horizontalRadius
        );

        List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class, area, target -> {
            if (!target.isAlive()) {
                return false;
            }
            if (caster != null && target.getUUID().equals(caster.getUUID())) {
                return false;
            }
            if (!canDamageEntity(target)) {
                return false;
            }

            Vec3 center = target.getBoundingBox().getCenter();
            double dx = center.x - getX();
            double dz = center.z - getZ();
            return dx * dx + dz * dz <= radiusSq;
        });

        if (targets.isEmpty()) {
            return;
        }

        DamageSource source = createAreaDamageSource(caster);
        float damage = getAoEDamage();
        for (LivingEntity target : targets) {
            if (caster instanceof Player player) {
                target.setLastHurtByPlayer(player);
            } else if (caster != null) {
                target.setLastHurtByMob(caster);
            }

            target.invulnerableTime = 0;
            if (target.hurt(source, damage)) {
                onAreaDamageApplied(target, damage, source);
            }
        }
    }

    public boolean isSafeToPlayers() {
        return this.entityData.get(DATA_SAFE_TO_PLAYERS);
    }

    public void setSafeToPlayers(boolean safeToPlayers) {
        this.entityData.set(DATA_SAFE_TO_PLAYERS, safeToPlayers);
    }

    public float getVisualScale() {
        return this.entityData.get(DATA_VISUAL_SCALE);
    }

    public void setVisualScale(float scale) {
        this.entityData.set(DATA_VISUAL_SCALE, Math.max(0.1F, scale));
    }

    private void spawnClientParticles() {
        float radius = getDamageRadius();
        if (radius <= 0.0F) {
            return;
        }

        double centerX = getX();
        double centerY = getY() + 0.05D;
        double centerZ = getZ();
        double baseAngle = (tickCount * 0.15D) % (Math.PI * 2.0D);

        for (int i = 0; i < RING_PARTICLE_COUNT; i++) {
            double angle = baseAngle + (Math.PI * 2.0D * i) / RING_PARTICLE_COUNT;
            double x = centerX + Math.cos(angle) * radius;
            double z = centerZ + Math.sin(angle) * radius;
            double ySpeed = 0.005D + random.nextDouble() * 0.015D;
            level().addParticle(ParticleTypes.SNOWFLAKE, x, centerY, z, 0.0D, ySpeed, 0.0D);
        }

        for (int i = 0; i < INNER_PARTICLE_COUNT; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = Math.sqrt(random.nextDouble()) * radius;
            double x = centerX + Math.cos(angle) * distance;
            double z = centerZ + Math.sin(angle) * distance;
            level().addParticle(ParticleTypes.ITEM_SNOWBALL, x, centerY, z, 0.0D, 0.01D, 0.0D);
        }
    }

    @Override
    protected void readSpellGramData(CompoundTag tag) {
        setSafeToPlayers(tag.getBoolean(NBT_SAFE_TO_PLAYERS));
        if (tag.contains(NBT_VISUAL_SCALE)) {
            setVisualScale(tag.getFloat(NBT_VISUAL_SCALE));
        }
    }

    @Override
    protected void addSpellGramData(CompoundTag tag) {
        tag.putBoolean(NBT_SAFE_TO_PLAYERS, isSafeToPlayers());
        tag.putFloat(NBT_VISUAL_SCALE, getVisualScale());
    }
}
