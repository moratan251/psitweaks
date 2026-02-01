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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class EntityPhononMaserBeam extends Entity {

    private static final EntityDataAccessor<Float> START_X = SynchedEntityData.defineId(EntityPhononMaserBeam.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> START_Y = SynchedEntityData.defineId(EntityPhononMaserBeam.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> START_Z = SynchedEntityData.defineId(EntityPhononMaserBeam.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> END_X = SynchedEntityData.defineId(EntityPhononMaserBeam.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> END_Y = SynchedEntityData.defineId(EntityPhononMaserBeam.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> END_Z = SynchedEntityData.defineId(EntityPhononMaserBeam.class, EntityDataSerializers.FLOAT);

    private static final double MAX_RANGE = 32.0;

    private int ticksRemaining = 20;
    private int totalDuration = 20;
    private double power = 1.0;
    private UUID casterUUID;
    private boolean soundPlayed = false;

    public EntityPhononMaserBeam(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public EntityPhononMaserBeam(Level level, LivingEntity caster, double power, int durationTicks) {
        this(PsitweaksEntities.PHONON_MASER_BEAM.get(), level);
        this.power = power;
        this.ticksRemaining = durationTicks;
        this.totalDuration = durationTicks;
        this.casterUUID = caster.getUUID();

        // 初期位置を設定
        updateBeamPositions(caster);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(START_X, 0.0F);
        this.entityData.define(START_Y, 0.0F);
        this.entityData.define(START_Z, 0.0F);
        this.entityData.define(END_X, 0.0F);
        this.entityData.define(END_Y, 0.0F);
        this.entityData.define(END_Z, 0.0F);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            // サーバー側
            LivingEntity caster = getCaster();

            if (caster == null) {
                this.discard();
                return;
            }

            // サウンド再生（最初のtickのみ）
            if (!soundPlayed) {
                playLaserSound();
                soundPlayed = true;
            }

            // 毎tickビームの位置を更新（プレイヤーの向きに追従）
            updateBeamPositions(caster);

            // ダメージ処理
            damageEntitiesAlongPath(caster);

            // ブロックへのエネルギー供給（毎tick）
            BlockPos hitBlockPos = getHitBlockPos();
            if (hitBlockPos != null) {
                supplyEnergyToBlock(hitBlockPos);
            }

            ticksRemaining--;
            if (ticksRemaining <= 0) {
                this.discard();
            }
        }
    }

    /**
     * 術者の視線に基づいてビームの開始点と終点を更新
     */
    private void updateBeamPositions(LivingEntity caster) {
        Vec3 startPos = caster.getEyePosition();
        Vec3 lookVec = caster.getLookAngle();
        Vec3 potentialEndPos = startPos.add(lookVec.scale(MAX_RANGE));

        // ブロックへのレイトレース
        BlockHitResult blockHit = level().clip(new ClipContext(
                startPos,
                potentialEndPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                caster
        ));

        Vec3 endPos = potentialEndPos;
        if (blockHit.getType() != HitResult.Type.MISS) {
            endPos = blockHit.getLocation();
        }

        // エンティティの位置を開始点に設定
        this.setPos(startPos.x, startPos.y, startPos.z);

        // 同期データを更新
        this.entityData.set(START_X, (float) startPos.x);
        this.entityData.set(START_Y, (float) startPos.y);
        this.entityData.set(START_Z, (float) startPos.z);
        this.entityData.set(END_X, (float) endPos.x);
        this.entityData.set(END_Y, (float) endPos.y);
        this.entityData.set(END_Z, (float) endPos.z);
    }

    /**
     * 現在のレイトレースでヒットしているブロック位置を取得
     */
    @Nullable
    private BlockPos getHitBlockPos() {
        Vec3 start = getStartPos();
        Vec3 end = getEndPos();

        LivingEntity caster = getCaster();
        if (caster == null) {
            return null;
        }

        BlockHitResult blockHit = level().clip(new ClipContext(
                start,
                end.add(caster.getLookAngle().scale(0.1)),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                caster
        ));

        if (blockHit.getType() != HitResult.Type.MISS) {
            return blockHit.getBlockPos();
        }
        return null;
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
        Vec3 start = getStartPos();
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

        // ダメージ計算と適用
        float damagePerTick = (float) ( power);
        DamageSource damageSource = createLaserDamageSource(caster);

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity living) {
                Vec3 oldDeltaMovement = living.getDeltaMovement();

                living.invulnerableTime = 0;
                living.hurt(damageSource, damagePerTick);

                // ノックバックを無効化
                living.setDeltaMovement(oldDeltaMovement);
                living.hurtMarked = true;
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
    private void supplyEnergyToBlock(BlockPos hitBlockPos) {
        BlockEntity blockEntity = level().getBlockEntity(hitBlockPos);
        if (blockEntity instanceof ILaserReceptor receptor) {
            // 1tick当たりのエネルギー = 威力 * 100MJ / 持続時間
            long energyPerTick = (long) (power * 5000L );
            receptor.receiveLaserEnergy(FloatingLong.create(energyPerTick));
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

    public double getPower() {
        return power;
    }

    public int getTotalDuration() {
        return totalDuration;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.ticksRemaining = tag.getInt("TicksRemaining");
        this.totalDuration = tag.getInt("TotalDuration");
        this.power = tag.getDouble("Power");
        this.soundPlayed = tag.getBoolean("SoundPlayed");
        if (tag.hasUUID("CasterUUID")) {
            this.casterUUID = tag.getUUID("CasterUUID");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("TicksRemaining", this.ticksRemaining);
        tag.putInt("TotalDuration", this.totalDuration);
        tag.putDouble("Power", this.power);
        tag.putBoolean("SoundPlayed", this.soundPlayed);
        if (this.casterUUID != null) {
            tag.putUUID("CasterUUID", this.casterUUID);
        }
    }
}