package com.moratan251.psitweaks.common.entities;

import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;

@SuppressWarnings({"unchecked", "rawtypes"})
public class EntityTimeAccelerator extends Entity {

    private static final String NBT_POS = "targetPos";
    private static final String NBT_REMAINING = "remainingTime";
    private static final String NBT_RATE = "timeRate";

    private static final EntityDataAccessor<Integer> DATA_RATE =
            SynchedEntityData.defineId(com.moratan251.psitweaks.common.entities.EntityTimeAccelerator.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> DATA_REMAINING =
            SynchedEntityData.defineId(com.moratan251.psitweaks.common.entities.EntityTimeAccelerator.class, EntityDataSerializers.INT);

    // BlockPosはSynchedEntityDataに入れにくいのでNBT保持（クライアント不要ならこれで十分）
    private BlockPos targetPos;

    public EntityTimeAccelerator(EntityType<? extends com.moratan251.psitweaks.common.entities.EntityTimeAccelerator> type, Level level) {
        super(type, level);
    }

    public EntityTimeAccelerator(Level level, BlockPos targetPos) {
        this(PsitweaksEntities.TIME_ACCELERATOR.get(), level);
        this.targetPos = targetPos;
        setPos(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_RATE, 1);
        entityData.define(DATA_REMAINING, 20);
    }

    public int getTimeRate() {
        return entityData.get(DATA_RATE);
    }

    public void setTimeRate(int rate) {
        entityData.set(DATA_RATE, Math.max(1, rate));
    }

    public int getRemainingTime() {
        return entityData.get(DATA_REMAINING);
    }

    public void setRemainingTime(int ticks) {
        entityData.set(DATA_REMAINING, Math.max(0, ticks));
    }

    public BlockPos getTargetPos() {
        return targetPos;
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            return; // サーバー側だけで動かす
        }

        if (targetPos == null) {
            discard();
            return;
        }

        Level level = level();
        if (!(level instanceof ServerLevel serverLevel)) {
            discard();
            return;
        }

        // チャンク未ロードなら何もしない（安全寄り）
        if (!serverLevel.isLoaded(targetPos)) {
            return;
        }

        BlockState state = serverLevel.getBlockState(targetPos);
        BlockEntity blockEntity = serverLevel.getBlockEntity(targetPos);

        int rate = getTimeRate();
        rate -= 1;
        if (rate == 0) {
            discard();
            return;
        }

        for (int i = 0; i < rate; i++) {
            if (blockEntity != null) {
                BlockEntityTicker ticker = state.getTicker(serverLevel, (BlockEntityType) blockEntity.getType());
                if (ticker != null) {
                    ticker.tick(serverLevel, targetPos, state, blockEntity);
                }
            } else if (state.isRandomlyTicking()) {
                // バニラrandom tickの平均感は “確率” で合わせるのが近い。
                // ここは固定値でもOK（重いなら間引く）
                if (serverLevel.random.nextInt(1365) == 0) {
                    state.randomTick(serverLevel, targetPos, serverLevel.random);
                }
            } else {
                // 対象が無効になった（ブロック破壊など）
                discard();
                break;
            }
        }

        int remaining = getRemainingTime() - 1;
        setRemainingTime(remaining);
        if (remaining <= 0) {
            discard();
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains(NBT_POS)) {
            targetPos = NbtUtils.readBlockPos(tag.getCompound(NBT_POS));
        }
        if (tag.contains(NBT_RATE)) {
            setTimeRate(tag.getInt(NBT_RATE));
        }
        if (tag.contains(NBT_REMAINING)) {
            setRemainingTime(tag.getInt(NBT_REMAINING));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (targetPos != null) {
            tag.put(NBT_POS, NbtUtils.writeBlockPos(targetPos));
        }
        tag.putInt(NBT_RATE, getTimeRate());
        tag.putInt(NBT_REMAINING, getRemainingTime());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}