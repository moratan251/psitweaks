package com.moratan251.psitweaks.common.entities;

import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.spells.SpellSafetyUtils;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityMeteorLineBeam extends Entity {
    public static final int DEFAULT_DURATION_TICKS = 30;
    public static final float DAMAGE = 65_535.0F;
    private static final double HIT_RADIUS = 0.15D;

    private static final EntityDataAccessor<Float> START_X = SynchedEntityData.defineId(EntityMeteorLineBeam.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> START_Y = SynchedEntityData.defineId(EntityMeteorLineBeam.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> START_Z = SynchedEntityData.defineId(EntityMeteorLineBeam.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> END_X = SynchedEntityData.defineId(EntityMeteorLineBeam.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> END_Y = SynchedEntityData.defineId(EntityMeteorLineBeam.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> END_Z = SynchedEntityData.defineId(EntityMeteorLineBeam.class, EntityDataSerializers.FLOAT);

    private int ticksRemaining = DEFAULT_DURATION_TICKS;
    private int totalDuration = DEFAULT_DURATION_TICKS;
    private UUID casterUUID;
    private boolean soundPlayed = false;
    private boolean safeToPlayers = false;

    public EntityMeteorLineBeam(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public EntityMeteorLineBeam(Level level, LivingEntity caster, Vec3 start, Vec3 direction, double maxDistance) {
        this(PsitweaksEntities.METEOR_LINE_BEAM.get(), level);
        this.casterUUID = caster.getUUID();
        this.ticksRemaining = DEFAULT_DURATION_TICKS;
        this.totalDuration = DEFAULT_DURATION_TICKS;
        setBeamPositions(caster, start, direction, maxDistance);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(START_X, 0.0F);
        builder.define(START_Y, 0.0F);
        builder.define(START_Z, 0.0F);
        builder.define(END_X, 0.0F);
        builder.define(END_Y, 0.0F);
        builder.define(END_Z, 0.0F);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            LivingEntity caster = getCaster();
            if (caster == null) {
                discard();
                return;
            }

            if (!soundPlayed) {
                playLaserSound();
                soundPlayed = true;
            }

            damageEntitiesAlongPath(caster);

            ticksRemaining--;
            if (ticksRemaining <= 0) {
                discard();
            }
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    private void setBeamPositions(LivingEntity caster, Vec3 start, Vec3 direction, double maxDistance) {
        Vec3 normalizedDirection = direction.normalize();
        Vec3 end = start.add(normalizedDirection.scale(maxDistance));

        setPos(start.x, start.y, start.z);
        entityData.set(START_X, (float) start.x);
        entityData.set(START_Y, (float) start.y);
        entityData.set(START_Z, (float) start.z);
        entityData.set(END_X, (float) end.x);
        entityData.set(END_Y, (float) end.y);
        entityData.set(END_Z, (float) end.z);
    }

    private void playLaserSound() {
        level().playSound(
                null,
                getX(), getY(), getZ(),
                MekanismSounds.LASER.get(),
                SoundSource.PLAYERS,
                0.7F,
                1.6F
        );
    }

    @Nullable
    private LivingEntity getCaster() {
        if (casterUUID == null) {
            return null;
        }
        if (level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(casterUUID);
            if (entity instanceof LivingEntity living) {
                return living;
            }
        }
        return null;
    }

    private DamageSource createMeteorLineDamageSource(@Nullable LivingEntity caster) {
        Holder<DamageType> damageType = level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(PsitweaksDamageTypes.METEOR_LINE);
        return new DamageSource(damageType, this, caster);
    }

    private void damageEntitiesAlongPath(LivingEntity caster) {
        Vec3 start = getStartPos();
        Vec3 end = getEndPos();
        AABB bounds = new AABB(start, end).inflate(1.0D);
        DamageSource damageSource = createMeteorLineDamageSource(caster);

        List<Entity> entities = level().getEntities(this, bounds, entity -> {
            if (!(entity instanceof LivingEntity living)) {
                return false;
            }
            if (casterUUID != null && living.getUUID().equals(casterUUID)) {
                return false;
            }
            if (safeToPlayers && living instanceof Player) {
                return false;
            }
            return living.getBoundingBox().inflate(HIT_RADIUS).clip(start, end).isPresent();
        });

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity living) {
                living.hurt(damageSource, DAMAGE);
            }
        }
    }

    public Vec3 getStartPos() {
        return new Vec3(
                entityData.get(START_X),
                entityData.get(START_Y),
                entityData.get(START_Z)
        );
    }

    public Vec3 getEndPos() {
        return new Vec3(
                entityData.get(END_X),
                entityData.get(END_Y),
                entityData.get(END_Z)
        );
    }

    public void setSafeToPlayers(boolean safeToPlayers) {
        this.safeToPlayers = safeToPlayers;
    }

    public int getTicksRemaining() {
        return ticksRemaining;
    }

    public int getTotalDuration() {
        return totalDuration;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.ticksRemaining = tag.getInt("TicksRemaining");
        this.totalDuration = tag.getInt("TotalDuration");
        this.soundPlayed = tag.getBoolean("SoundPlayed");
        this.safeToPlayers = tag.getBoolean(SpellSafetyUtils.NBT_SAFE_TO_PLAYERS);
        if (tag.hasUUID("CasterUUID")) {
            this.casterUUID = tag.getUUID("CasterUUID");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("TicksRemaining", ticksRemaining);
        tag.putInt("TotalDuration", totalDuration);
        tag.putBoolean("SoundPlayed", soundPlayed);
        tag.putBoolean(SpellSafetyUtils.NBT_SAFE_TO_PLAYERS, safeToPlayers);
        if (casterUUID != null) {
            tag.putUUID("CasterUUID", casterUUID);
        }
    }
}
