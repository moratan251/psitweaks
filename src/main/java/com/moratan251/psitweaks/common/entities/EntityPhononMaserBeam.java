package com.moratan251.psitweaks.common.entities;

import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.math.FloatingLong;
import mekanism.common.registries.MekanismDamageTypes;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class EntityPhononMaserBeam extends Entity {

    private static final EntityDataAccessor<Float> END_X = SynchedEntityData.defineId(EntityPhononMaserBeam.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> END_Y = SynchedEntityData.defineId(EntityPhononMaserBeam.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> END_Z = SynchedEntityData.defineId(EntityPhononMaserBeam.class, EntityDataSerializers.FLOAT);

    private static final int DURATION_TICKS = 20; // 1秒 = 20tick

    private int ticksRemaining = DURATION_TICKS;
    private double power = 1.0;
    private UUID casterUUID;
    private Vec3 endPos;
    private BlockPos hitBlockPos;
    private boolean soundPlayed = false;

    public EntityPhononMaserBeam(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public EntityPhononMaserBeam(Level level, LivingEntity caster, Vec3 start, Vec3 end, double power, @Nullable BlockPos hitBlock) {
        this(PsitweaksEntities.PHONON_MASER_BEAM.get(), level);
        this.setPos(start.x, start.y, start.z);
        this.endPos = end;
        this.power = power;
        this.casterUUID = caster.getUUID();
        this.hitBlockPos = hitBlock;

        // クライアント同期用にエンドポイントを設定
        this.entityData.set(END_X, (float) end.x);
        this.entityData.set(END_Y, (float) end.y);
        this.entityData.set(END_Z, (float) end.z);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(END_X, 0.0F);
        this.entityData.define(END_Y, 0.0F);
        this.entityData.define(END_Z, 0.0F);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            // サーバー側：サウンド再生（最初のtickのみ）
            if (!soundPlayed) {
                playLaserSound();
                soundPlayed = true;
            }

            // サーバー側：ダメージとエネルギー供給
            ticksRemaining--;

            if (ticksRemaining <= 0) {
                this.discard();
                return;
            }

            // 毎tick処理
            LivingEntity caster = getCaster();
            damageEntitiesAlongPath(caster);

            // 最初のtickでのみブロックにエネルギー供給
            if (ticksRemaining == DURATION_TICKS - 1 && hitBlockPos != null) {
                supplyEnergyToBlock();
            }
        }
    }

    /**
     * Mekanismのレーザー発射音を再生
     */
    private void playLaserSound() {
        level().playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                MekanismSounds.LASER.get(),
                SoundSource.PLAYERS,
                1.0F,
                1.0F
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

    /**
     * MekanismのLASERダメージタイプを使用して、術者を攻撃元とするDamageSourceを作成
     */
    private DamageSource createLaserDamageSource(@Nullable LivingEntity caster) {
        Holder<DamageType> laserDamageType = level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(MekanismDamageTypes.LASER.key());

        return new DamageSource(laserDamageType, this, caster);
    }

    /**
     * レーザー経路上のエンティティにダメージを与える（ノックバックなし）
     */
    private void damageEntitiesAlongPath(@Nullable LivingEntity caster) {
        Vec3 start = position();
        Vec3 end = getEndPos();

        AABB laserBounds = new AABB(start, end).inflate(0.5);

        List<Entity> entities = level().getEntities(this, laserBounds, entity -> {
            if (casterUUID != null && entity.getUUID().equals(casterUUID)) {
                return false;
            }
            if (entity == this) {
                return false;
            }
            if (!(entity instanceof LivingEntity)) {
                return false;
            }
            return isEntityOnLaserPath(entity, start, end);
        });

        if (entities.isEmpty()) {
            return;
        }

        float damagePerTick = (float) (power * 4.0);
        DamageSource damageSource = createLaserDamageSource(caster);

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity living) {
                // 現在の位置とモーションを保存
                double oldX = living.getX();
                double oldY = living.getY();
                double oldZ = living.getZ();
                Vec3 oldDeltaMovement = living.getDeltaMovement();

                // ダメージ無敵時間をリセットしてダメージを与える
                living.invulnerableTime = 0;
                living.hurt(damageSource, damagePerTick);

                // ノックバックを無効化（位置とモーションを復元）
                living.setDeltaMovement(oldDeltaMovement);
                living.hurtMarked = true; // クライアントに同期
            }
        }
    }

    /**
     * エンティティがレーザー経路上にいるかチェック
     */
    private boolean isEntityOnLaserPath(Entity entity, Vec3 start, Vec3 end) {
        Vec3 entityPos = entity.position().add(0, entity.getBbHeight() / 2, 0);
        Vec3 laserDir = end.subtract(start);
        double length = laserDir.length();
        if (length < 0.001) {
            return false;
        }
        laserDir = laserDir.normalize();
        Vec3 toEntity = entityPos.subtract(start);

        double projectionLength = toEntity.dot(laserDir);
        if (projectionLength < 0 || projectionLength > length) {
            return false;
        }

        Vec3 closestPoint = start.add(laserDir.scale(projectionLength));
        double distance = entityPos.distanceTo(closestPoint);

        return distance < entity.getBbWidth() + 0.5;
    }

    /**
     * ILaserReceptorにエネルギーを供給
     */
    private void supplyEnergyToBlock() {
        if (hitBlockPos == null) {
            return;
        }
        BlockEntity blockEntity = level().getBlockEntity(hitBlockPos);
        if (blockEntity instanceof ILaserReceptor receptor) {
            long energyToSend = (long) (power * 100_000_000L);
            receptor.receiveLaserEnergy(FloatingLong.create(energyToSend));
        }
    }

    public Vec3 getEndPos() {
        if (endPos != null) {
            return endPos;
        }
        return new Vec3(
                entityData.get(END_X),
                entityData.get(END_Y),
                entityData.get(END_Z)
        );
    }

    public double getPower() {
        return power;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.ticksRemaining = tag.getInt("TicksRemaining");
        this.power = tag.getDouble("Power");
        this.soundPlayed = tag.getBoolean("SoundPlayed");
        if (tag.hasUUID("CasterUUID")) {
            this.casterUUID = tag.getUUID("CasterUUID");
        }
        if (tag.contains("EndX")) {
            this.endPos = new Vec3(tag.getDouble("EndX"), tag.getDouble("EndY"), tag.getDouble("EndZ"));
        }
        if (tag.contains("HitBlockX")) {
            this.hitBlockPos = new BlockPos(tag.getInt("HitBlockX"), tag.getInt("HitBlockY"), tag.getInt("HitBlockZ"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("TicksRemaining", this.ticksRemaining);
        tag.putDouble("Power", this.power);
        tag.putBoolean("SoundPlayed", this.soundPlayed);
        if (this.casterUUID != null) {
            tag.putUUID("CasterUUID", this.casterUUID);
        }
        if (this.endPos != null) {
            tag.putDouble("EndX", this.endPos.x);
            tag.putDouble("EndY", this.endPos.y);
            tag.putDouble("EndZ", this.endPos.z);
        }
        if (this.hitBlockPos != null) {
            tag.putInt("HitBlockX", this.hitBlockPos.getX());
            tag.putInt("HitBlockY", this.hitBlockPos.getY());
            tag.putInt("HitBlockZ", this.hitBlockPos.getZ());
        }
    }
}