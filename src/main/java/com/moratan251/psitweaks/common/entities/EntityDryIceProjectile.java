package com.moratan251.psitweaks.common.entities;

import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.spells.SpellSafetyUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class EntityDryIceProjectile extends ThrowableItemProjectile {

    private static final String NBT_DAMAGE = "Damage";
    private static final int MAX_LIFETIME_TICKS = 60;
    private static final int CARBON_POISONING_DURATION_TICKS = 200;
    private static final int BLOCK_FRAGMENT_PARTICLE_COUNT = 36;
    private static final int ENTITY_SNOW_PARTICLE_COUNT = 24;
    private static final int ENTITY_WHITE_SMOKE_PARTICLE_COUNT = 24;

    private float damage = 14.0F;
    private boolean safeToPlayers;

    public EntityDryIceProjectile(EntityType<? extends EntityDryIceProjectile> type, Level level) {
        super(type, level);
    }

    public EntityDryIceProjectile(Level level, LivingEntity owner) {
        super(PsitweaksEntities.DRY_ICE_PROJECTILE.get(), owner, level);
    }

    public void setDamage(float damage) {
        this.damage = Math.max(0.0F, damage);
    }

    public void setSafeToPlayers(boolean safeToPlayers) {
        this.safeToPlayers = safeToPlayers;
    }

    @Override
    protected Item getDefaultItem() {
        return PsitweaksItems.DRY_ICE_PROJECTILE.get();
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide && !isRemoved() && tickCount >= MAX_LIFETIME_TICKS) {
            playBlockImpactEffect(getX(), getY(), getZ());
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (level().isClientSide) {
            return;
        }

        Entity target = result.getEntity();
        if (!(safeToPlayers && target instanceof Player) && target instanceof LivingEntity livingTarget) {
            livingTarget.hurt(createDryMeteorDamageSource(), damage);
            if (livingTarget.isAlive()) {
                livingTarget.addEffect(
                        new MobEffectInstance(PsitweaksEffects.CARBON_POISONING.get(), CARBON_POISONING_DURATION_TICKS),
                        getOwner()
                );
            }
        }

        playEntityImpactEffect(target);
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (level().isClientSide) {
            return;
        }

        playBlockImpactEffect(result.getLocation().x, result.getLocation().y, result.getLocation().z);
        discard();
    }

    private DamageSource createDryMeteorDamageSource() {
        Holder<DamageType> dryMeteorDamageType = level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(PsitweaksDamageTypes.DRY_METEOR);
        return new DamageSource(dryMeteorDamageType, this, getOwner());
    }

    private void playBlockImpactEffect(double x, double y, double z) {
        if (level() instanceof ServerLevel serverLevel) {
            ItemParticleOption fragments = new ItemParticleOption(ParticleTypes.ITEM, getItem());
            serverLevel.sendParticles(
                    fragments,
                    x,
                    y,
                    z,
                    BLOCK_FRAGMENT_PARTICLE_COUNT,
                    0.25,
                    0.25,
                    0.25,
                    0.12
            );
        }
        playImpactSound(x, y, z);
    }

    private void playEntityImpactEffect(Entity target) {
        double x = target.getX();
        double y = target.getY() + target.getBbHeight() * 0.5;
        double z = target.getZ();
        double horizontalSpread = Math.max(0.35, target.getBbWidth() * 0.6);
        double verticalSpread = Math.max(0.45, target.getBbHeight() * 0.4);

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.SNOWFLAKE,
                    x,
                    y,
                    z,
                    ENTITY_SNOW_PARTICLE_COUNT,
                    horizontalSpread,
                    verticalSpread,
                    horizontalSpread,
                    0.08
            );
            serverLevel.sendParticles(
                    ParticleTypes.EXPLOSION,
                    x,
                    y,
                    z,
                    ENTITY_WHITE_SMOKE_PARTICLE_COUNT,
                    horizontalSpread,
                    verticalSpread,
                    horizontalSpread,
                    0.04
            );
        }
        playImpactSound(x, y, z);
    }

    private void playImpactSound(double x, double y, double z) {
        level().playSound(null, x, y, z, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.65F, 1.35F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat(NBT_DAMAGE, damage);
        tag.putBoolean(SpellSafetyUtils.NBT_SAFE_TO_PLAYERS, safeToPlayers);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains(NBT_DAMAGE)) {
            damage = Math.max(0.0F, tag.getFloat(NBT_DAMAGE));
        }
        safeToPlayers = tag.getBoolean(SpellSafetyUtils.NBT_SAFE_TO_PLAYERS);
    }
}
