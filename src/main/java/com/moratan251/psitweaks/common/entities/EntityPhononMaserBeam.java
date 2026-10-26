package com.moratan251.psitweaks.common.entities;

import com.moratan251.psitweaks.common.spells.combat.AttackCaster;
import com.moratan251.psitweaks.common.compat.SableRangeCompat;
import com.moratan251.psitweaks.common.compat.MekanismGameplayCompat;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
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
    private int power = 1;
    /** 術者と攻撃の設定。術者は保存した ID だけで引き直し、撤去された機械やいなくなった術者なら光線は消える。 */
    private final AttackCaster attackCaster = AttackCaster.byId();
    private boolean soundPlayed = false;

    public EntityPhononMaserBeam(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public EntityPhononMaserBeam(Level level, LivingEntity caster, int power, int durationTicks) {
        this(PsitweaksEntities.PHONON_MASER_BEAM.get(), level);
        this.power = power;
        this.ticksRemaining = durationTicks;
        this.totalDuration = durationTicks;
        this.attackCaster.set(caster);

        // 初期位置を設定
        updateBeamPositions(caster);
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
                supplyEnergyToBlock(hitBlockPos, caster);
            }

            ticksRemaining--;
            if (ticksRemaining <= 0) {
                this.discard();
            }
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true; //
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
            endPos = SableRangeCompat.projectForEffect(level(), blockHit.getLocation());
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
                MekanismGameplayCompat.laserSound(),
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
    }

    @Nullable
    private LivingEntity getCaster() {
        // 自律詠唱機の詠唱主体はワールドに入らないため、保存した ID で引き直す（AttackCaster）。
        return attackCaster.get(level());
    }

    /**
     * MekanismのLASERダメージタイプを使用して、術者を攻撃元とするDamageSourceを作成
     */
    private DamageSource createLaserDamageSource(@Nullable LivingEntity caster) {
        Holder<DamageType> laserDamageType = level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(MekanismGameplayCompat.laserDamageType());

        return new DamageSource(laserDamageType, this, caster);
    }

    /**
     * レーザー経路上のエンティティにダメージを与える（ノックバックなし）
     */
    private void damageEntitiesAlongPath(@Nullable LivingEntity caster) {
        Vec3 start = getStartPos();
        Vec3 end = getEndPos();

        AABB laserBounds = new AABB(start, end).inflate(0.5);

        // 攻撃術式で共通の命中判定（術者側・防具立て・プレイヤーに安全・受ける側の PvP）。
        List<Entity> entities = level().getEntities(this, laserBounds, entity ->
                AttackRules.canHarm(caster, entity, attackCaster.options()) && isEntityOnLaserPath(entity, start, end));

        if (entities.isEmpty()) {
            return;
        }

        // 1tick当たりのダメージ（個別倍率 x 全体倍率 x SpellDamageFactor）
        double damageMul = AttackRules.multiplier(PsitweaksConfig.COMMON.phononMaserDamageMultiplier.get(), caster);
        float damagePerTick = (float) power * 2.0f * (float) damageMul;
        DamageSource damageSource = createLaserDamageSource(caster);

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity living) {
                Vec3 oldDeltaMovement = living.getDeltaMovement();

                living.invulnerableTime = 0;
                AttackRules.harm(caster, living, attackCaster.options(), damageSource, damagePerTick);

                // ノックバックを無効化
                living.setDeltaMovement(oldDeltaMovement);
                living.hurtMarked = true;

                //炎上させる
                living.igniteForSeconds(5);
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
    private void supplyEnergyToBlock(BlockPos hitBlockPos, LivingEntity caster) {
        BlockEntity blockEntity = level().getBlockEntity(hitBlockPos);
        if (blockEntity != null) {
            // 1tick当たりのエネルギー
            long energyPerTick = (long) power * 2000000L;
            // 核融合炉を点火させた術者には隠し進捗
            MekanismGameplayCompat.supplyLaserEnergy(blockEntity, energyPerTick, caster instanceof ServerPlayer player ? player : null);
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
        this.attackCaster.setOptions(AttackRules.Options.of(safeToPlayers));
    }

    public int getPower() {
        return power;
    }

    public int getTotalDuration() {
        return totalDuration;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.ticksRemaining = tag.getInt("TicksRemaining");
        this.totalDuration = tag.getInt("TotalDuration");
        this.power = tag.getInt("Power");
        this.soundPlayed = tag.getBoolean("SoundPlayed");
        this.attackCaster.load(tag, "CasterUUID");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("TicksRemaining", this.ticksRemaining);
        tag.putInt("TotalDuration", this.totalDuration);
        tag.putInt("Power", this.power);
        tag.putBoolean("SoundPlayed", this.soundPlayed);
        this.attackCaster.save(tag, "CasterUUID");
    }
}
