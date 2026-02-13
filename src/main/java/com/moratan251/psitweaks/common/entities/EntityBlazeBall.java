package com.moratan251.psitweaks.common.entities;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class EntityBlazeBall extends SmallFireball {

    private static final String NBT_DAMAGE = "Damage";
    private static final int MAX_LIFETIME_TICKS = 160;
    private float damage = 5.0F;

    public EntityBlazeBall(EntityType<? extends EntityBlazeBall> type, Level level) {
        super(type, level);
    }

    public void setDamage(float damage) {
        this.damage = Math.max(0.0F, damage);
    }

    public float getDamage() {
        return damage;
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide && !isRemoved() && tickCount >= MAX_LIFETIME_TICKS) {
            discard();
        }
    }

    @Override
    protected float getInertia() {
        return 1.0F;
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.SMALL_FLAME;
    }

    @Override
    public boolean displayFireAnimation() {
        return true;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (level().isClientSide) {
            return;
        }

        Entity target = result.getEntity();
        Entity owner = getOwner();
        DamageSource source = damageSources().fireball(this, owner);

        int originalFireTicks = target.getRemainingFireTicks();
        target.setSecondsOnFire(5);
        if (!target.hurt(source, damage)) {
            target.setRemainingFireTicks(originalFireTicks);
        } else if (owner instanceof LivingEntity livingOwner) {
            doEnchantDamageEffects(livingOwner, target);
        }

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.SMALL_FLAME,
                    target.getX(),
                    target.getY() + target.getBbHeight() * 0.5,
                    target.getZ(),
                    12,
                    0.25,
                    0.25,
                    0.25,
                    0.01
            );
        }

        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (level().isClientSide) {
            return;
        }

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, getX(), getY(), getZ(), 8, 0.1, 0.1, 0.1, 0.02);
        }
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 0.4F, 1.2F);

        // 地形へ着弾しても火を置かない。
        discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat(NBT_DAMAGE, damage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains(NBT_DAMAGE)) {
            damage = Math.max(0.0F, tag.getFloat(NBT_DAMAGE));
        }
    }
}
