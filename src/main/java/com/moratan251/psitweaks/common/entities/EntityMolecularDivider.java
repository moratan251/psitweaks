package com.moratan251.psitweaks.common.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 分子ディバイダーの三角形領域を表示するエンティティ
 */
public class EntityMolecularDivider extends Entity {

    // 三角形の3頂点
    private static final EntityDataAccessor<Float> V1_X = SynchedEntityData.defineId(EntityMolecularDivider.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> V1_Y = SynchedEntityData.defineId(EntityMolecularDivider.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> V1_Z = SynchedEntityData.defineId(EntityMolecularDivider.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Float> V2_X = SynchedEntityData.defineId(EntityMolecularDivider.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> V2_Y = SynchedEntityData.defineId(EntityMolecularDivider.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> V2_Z = SynchedEntityData.defineId(EntityMolecularDivider.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Float> V3_X = SynchedEntityData.defineId(EntityMolecularDivider.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> V3_Y = SynchedEntityData.defineId(EntityMolecularDivider.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> V3_Z = SynchedEntityData.defineId(EntityMolecularDivider.class, EntityDataSerializers.FLOAT);

    private int ticksRemaining = 10; // 表示時間（0.5秒）
    private int totalDuration = 10;

    public EntityMolecularDivider(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public EntityMolecularDivider(Level level, Vec3 v1, Vec3 v2, Vec3 v3, int durationTicks) {
        this(PsitweaksEntities.MOLECULAR_DIVIDER.get(), level);
        this.ticksRemaining = durationTicks;
        this.totalDuration = durationTicks;

        // エンティティの位置を三角形の中心に設定
        double centerX = (v1.x + v2.x + v3.x) / 3.0;
        double centerY = (v1.y + v2.y + v3.y) / 3.0;
        double centerZ = (v1.z + v2.z + v3.z) / 3.0;
        this.setPos(centerX, centerY, centerZ);

        // 頂点座標を設定
        setVertex1(v1);
        setVertex2(v2);
        setVertex3(v3);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(V1_X, 0.0F);
        this.entityData.define(V1_Y, 0.0F);
        this.entityData.define(V1_Z, 0.0F);
        this.entityData.define(V2_X, 0.0F);
        this.entityData.define(V2_Y, 0.0F);
        this.entityData.define(V2_Z, 0.0F);
        this.entityData.define(V3_X, 0.0F);
        this.entityData.define(V3_Y, 0.0F);
        this.entityData.define(V3_Z, 0.0F);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
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

    // ========== Getter/Setter ==========

    public void setVertex1(Vec3 v) {
        this.entityData.set(V1_X, (float) v.x);
        this.entityData.set(V1_Y, (float) v.y);
        this.entityData.set(V1_Z, (float) v.z);
    }

    public void setVertex2(Vec3 v) {
        this.entityData.set(V2_X, (float) v.x);
        this.entityData.set(V2_Y, (float) v.y);
        this.entityData.set(V2_Z, (float) v.z);
    }

    public void setVertex3(Vec3 v) {
        this.entityData.set(V3_X, (float) v.x);
        this.entityData.set(V3_Y, (float) v.y);
        this.entityData.set(V3_Z, (float) v.z);
    }

    public Vec3 getVertex1() {
        return new Vec3(
                this.entityData.get(V1_X),
                this.entityData.get(V1_Y),
                this.entityData.get(V1_Z)
        );
    }

    public Vec3 getVertex2() {
        return new Vec3(
                this.entityData.get(V2_X),
                this.entityData.get(V2_Y),
                this.entityData.get(V2_Z)
        );
    }

    public Vec3 getVertex3() {
        return new Vec3(
                this.entityData.get(V3_X),
                this.entityData.get(V3_Y),
                this.entityData.get(V3_Z)
        );
    }

    public int getTicksRemaining() {
        return ticksRemaining;
    }

    public int getTotalDuration() {
        return totalDuration;
    }

    /**
     * フェードアウト用の���ルファ値を取得（0.0〜1.0）
     */
    public float getAlphaProgress() {
        return (float) ticksRemaining / (float) totalDuration;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        ticksRemaining = tag.getInt("TicksRemaining");
        totalDuration = tag.getInt("TotalDuration");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("TicksRemaining", ticksRemaining);
        tag.putInt("TotalDuration", totalDuration);
    }
}