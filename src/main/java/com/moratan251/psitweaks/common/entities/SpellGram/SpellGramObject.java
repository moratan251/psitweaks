package com.moratan251.psitweaks.common.entities.SpellGram;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * SpellGram系オブジェクトの基底クラス。
 * 数秒間の存続、周囲ダメージ、外部スペルによる移動/破壊を共通化する。
 */
public abstract class SpellGramObject extends Entity {

    private static final String NBT_REMAINING_TICKS = "SpellGramRemainingTicks";
    private static final String NBT_DAMAGE_RADIUS = "SpellGramDamageRadius";
    private static final String NBT_AOE_DAMAGE = "SpellGramAoeDamage";
    private static final String NBT_DAMAGE_INTERVAL = "SpellGramDamageInterval";
    private static final String NBT_INTEGRITY = "SpellGramIntegrity";
    private static final String NBT_MOTION_DAMPING = "SpellGramMotionDamping";
    private static final String NBT_CASTER_UUID = "SpellGramCaster";
    private static final String NBT_AOE_COUNTER = "SpellGramAoeCounter";

    private static final EntityDataAccessor<Integer> DATA_REMAINING_TICKS =
            SynchedEntityData.defineId(SpellGramObject.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_DAMAGE_RADIUS =
            SynchedEntityData.defineId(SpellGramObject.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_AOE_DAMAGE =
            SynchedEntityData.defineId(SpellGramObject.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_DAMAGE_INTERVAL =
            SynchedEntityData.defineId(SpellGramObject.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_INTEGRITY =
            SynchedEntityData.defineId(SpellGramObject.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_MOTION_DAMPING =
            SynchedEntityData.defineId(SpellGramObject.class, EntityDataSerializers.FLOAT);

    private int aoeTickCounter;
    @Nullable
    private UUID casterUuid;

    protected SpellGramObject(EntityType<? extends SpellGramObject> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    @Override
    protected final void defineSynchedData() {
        this.entityData.define(DATA_REMAINING_TICKS, 40);
        this.entityData.define(DATA_DAMAGE_RADIUS, 0.0F);
        this.entityData.define(DATA_AOE_DAMAGE, 0.0F);
        this.entityData.define(DATA_DAMAGE_INTERVAL, 20);
        this.entityData.define(DATA_INTEGRITY, 1.0F);
        this.entityData.define(DATA_MOTION_DAMPING, 0.85F);
        defineSpellGramSynchedData();
    }

    protected void defineSpellGramSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        tickMotion();

        if (level().isClientSide) {
            return;
        }

        onSpellGramTickServer();
        tickAreaDamage();
        tickLifetime();
    }

    protected void tickMotion() {
        Vec3 velocity = getDeltaMovement();
        if (velocity.lengthSqr() <= 1.0E-7) {
            return;
        }

        move(MoverType.SELF, velocity);

        float damping = getMotionDamping();
        if (damping <= 0.0F) {
            setDeltaMovement(Vec3.ZERO);
        } else if (damping < 1.0F) {
            setDeltaMovement(velocity.scale(damping));
        }
    }

    protected void tickAreaDamage() {
        if (getAoEDamage() <= 0.0F || getDamageRadius() <= 0.0F) {
            return;
        }

        aoeTickCounter++;
        if (aoeTickCounter < Math.max(1, getDamageIntervalTicks())) {
            return;
        }
        aoeTickCounter = 0;
        applyAreaDamage();
    }

    protected void applyAreaDamage() {
        LivingEntity caster = getCaster();
        AABB area = getBoundingBox().inflate(getDamageRadius());
        List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class, area, target -> {
            if (!target.isAlive()) {
                return false;
            }
            if (caster != null && target.getUUID().equals(caster.getUUID())) {
                return false;
            }
            return canDamageEntity(target);
        });

        if (targets.isEmpty()) {
            return;
        }

        DamageSource source = createAreaDamageSource(caster);
        float damage = getAoEDamage();
        for (LivingEntity target : targets) {
            target.invulnerableTime = 0;
            if (target.hurt(source, damage)) {
                onAreaDamageApplied(target, damage, source);
            }
        }
    }

    protected DamageSource createAreaDamageSource(@Nullable LivingEntity caster) {
        if (caster != null) {
            return this.damageSources().indirectMagic(this, caster);
        }
        return this.damageSources().magic();
    }

    protected boolean canDamageEntity(LivingEntity target) {
        return true;
    }

    protected void onAreaDamageApplied(LivingEntity target, float damage, DamageSource source) {
    }

    protected void tickLifetime() {
        int remaining = getRemainingTicks() - 1;
        setRemainingTicks(remaining);
        if (remaining <= 0) {
            onLifetimeExpired();
            discard();
        }
    }

    protected void onLifetimeExpired() {
    }

    protected void onSpellGramTickServer() {
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source) || isRemoved()) {
            return false;
        }
        if (!canBeDamagedBy(source, amount)) {
            return false;
        }
        if (level().isClientSide) {
            return true;
        }

        markHurt();

        float effectiveAmount = Math.max(0.0F, amount);
        float integrity = Math.max(0.0F, getIntegrity() - effectiveAmount);
        setIntegrity(integrity);
        onSpellGramDamaged(source, effectiveAmount, integrity);

        if (integrity <= 0.0F) {
            destroyBySpell(source);
        }
        return true;
    }

    protected boolean canBeDamagedBy(DamageSource source, float amount) {
        return true;
    }

    protected void onSpellGramDamaged(DamageSource source, float amount, float remainingIntegrity) {
    }

    public void destroyBySpell() {
        destroyBySpell(null);
    }

    public void destroyBySpell(@Nullable DamageSource source) {
        if (isRemoved()) {
            return;
        }

        if (!level().isClientSide) {
            onSpellGramDestroyed(source);
            discard();
        }
    }

    protected void onSpellGramDestroyed(@Nullable DamageSource source) {
    }

    public void moveBySpell(Vec3 offset) {
        setPos(getX() + offset.x, getY() + offset.y, getZ() + offset.z);
    }

    public void addSpellVelocity(Vec3 velocity) {
        setDeltaMovement(getDeltaMovement().add(velocity));
    }

    public int getRemainingTicks() {
        return this.entityData.get(DATA_REMAINING_TICKS);
    }

    public void setRemainingTicks(int ticks) {
        this.entityData.set(DATA_REMAINING_TICKS, Math.max(0, ticks));
    }

    public void setLifetimeTicks(int ticks) {
        setRemainingTicks(ticks);
    }

    public float getDamageRadius() {
        return this.entityData.get(DATA_DAMAGE_RADIUS);
    }

    public void setDamageRadius(float radius) {
        this.entityData.set(DATA_DAMAGE_RADIUS, Math.max(0.0F, radius));
    }

    public float getAoEDamage() {
        return this.entityData.get(DATA_AOE_DAMAGE);
    }

    public void setAoEDamage(float damage) {
        this.entityData.set(DATA_AOE_DAMAGE, Math.max(0.0F, damage));
    }

    public int getDamageIntervalTicks() {
        return this.entityData.get(DATA_DAMAGE_INTERVAL);
    }

    public void setDamageIntervalTicks(int intervalTicks) {
        this.entityData.set(DATA_DAMAGE_INTERVAL, Math.max(1, intervalTicks));
    }

    public void configureAreaDamage(float radius, float damage, int intervalTicks) {
        setDamageRadius(radius);
        setAoEDamage(damage);
        setDamageIntervalTicks(intervalTicks);
    }

    public float getIntegrity() {
        return this.entityData.get(DATA_INTEGRITY);
    }

    public void setIntegrity(float integrity) {
        this.entityData.set(DATA_INTEGRITY, Math.max(0.0F, integrity));
    }

    public float getMotionDamping() {
        return this.entityData.get(DATA_MOTION_DAMPING);
    }

    public void setMotionDamping(float damping) {
        this.entityData.set(DATA_MOTION_DAMPING, Math.max(0.0F, Math.min(1.0F, damping)));
    }

    public void setCaster(@Nullable LivingEntity caster) {
        this.casterUuid = caster == null ? null : caster.getUUID();
    }

    @Nullable
    public UUID getCasterUuid() {
        return casterUuid;
    }

    @Nullable
    public LivingEntity getCaster() {
        if (casterUuid == null) {
            return null;
        }

        if (level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(casterUuid);
            if (entity instanceof LivingEntity living) {
                return living;
            }
        }
        return null;
    }

    @Override
    protected final void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains(NBT_REMAINING_TICKS)) {
            setRemainingTicks(tag.getInt(NBT_REMAINING_TICKS));
        }
        if (tag.contains(NBT_DAMAGE_RADIUS)) {
            setDamageRadius(tag.getFloat(NBT_DAMAGE_RADIUS));
        }
        if (tag.contains(NBT_AOE_DAMAGE)) {
            setAoEDamage(tag.getFloat(NBT_AOE_DAMAGE));
        }
        if (tag.contains(NBT_DAMAGE_INTERVAL)) {
            setDamageIntervalTicks(tag.getInt(NBT_DAMAGE_INTERVAL));
        }
        if (tag.contains(NBT_INTEGRITY)) {
            setIntegrity(tag.getFloat(NBT_INTEGRITY));
        }
        if (tag.contains(NBT_MOTION_DAMPING)) {
            setMotionDamping(tag.getFloat(NBT_MOTION_DAMPING));
        }

        aoeTickCounter = Math.max(0, tag.getInt(NBT_AOE_COUNTER));

        if (tag.hasUUID(NBT_CASTER_UUID)) {
            casterUuid = tag.getUUID(NBT_CASTER_UUID);
        } else {
            casterUuid = null;
        }

        readSpellGramData(tag);
    }

    @Override
    protected final void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt(NBT_REMAINING_TICKS, getRemainingTicks());
        tag.putFloat(NBT_DAMAGE_RADIUS, getDamageRadius());
        tag.putFloat(NBT_AOE_DAMAGE, getAoEDamage());
        tag.putInt(NBT_DAMAGE_INTERVAL, getDamageIntervalTicks());
        tag.putFloat(NBT_INTEGRITY, getIntegrity());
        tag.putFloat(NBT_MOTION_DAMPING, getMotionDamping());
        tag.putInt(NBT_AOE_COUNTER, aoeTickCounter);
        if (casterUuid != null) {
            tag.putUUID(NBT_CASTER_UUID, casterUuid);
        }

        addSpellGramData(tag);
    }

    protected void readSpellGramData(CompoundTag tag) {
    }

    protected void addSpellGramData(CompoundTag tag) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }
}
