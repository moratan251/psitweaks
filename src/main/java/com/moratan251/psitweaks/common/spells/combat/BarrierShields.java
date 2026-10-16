package com.moratan251.psitweaks.common.spells.combat;

import com.moratan251.psitweaks.common.attachments.PsitweaksAttachments;
import com.moratan251.psitweaks.common.network.MessageSphereFlash;
import com.moratan251.psitweaks.common.network.MessageSphereFlash.Style;
import com.moratan251.psitweaks.common.registries.PsitweaksTags;
import com.moratan251.psitweaks.mixin.LivingEntityAccessor;
import java.util.Collections;
import java.util.Set;
import java.util.Stack;
import java.util.WeakHashMap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The shield of Trick: Barrier. A shield holds a number of points; while it lasts, incoming damage is taken from
 * those points instead of from the entity, before armour. A hit the shield takes completely causes no knockback,
 * no hurt flash and no hurt sound, only the honeycomb flash. Damage in {@code #psitweaks:bypasses_barrier}
 * (void, starvation, drowning, falling, burning and other damage over time) goes past it.
 */
public final class BarrierShields {
    /** Shield points per point of Power, before the config multiplier. */
    public static final float SHIELD_PER_POWER = 10.0F;

    /** Entities whose hit was cancelled this tick; NeoForge leaves that hit's damage container behind. */
    private static final Set<LivingEntity> PENDING_CONTAINER_CLEANUP = Collections.newSetFromMap(new WeakHashMap<>());

    private BarrierShields() {
    }

    /** Raises (or replaces) the shield on the entity. */
    public static void raise(LivingEntity entity, float amount, int durationTicks, int color) {
        long expiresAt = entity.level().getGameTime() + Math.max(1, durationTicks);
        BarrierShield shield = new BarrierShield(amount, amount, expiresAt, color & 0xFFFFFF);
        entity.setData(PsitweaksAttachments.BARRIER_SHIELD, shield);
        flash(entity, shield, Style.BARRIER_RAISE, Vec3.ZERO, 1.0F);
        playSound(entity, SoundEvents.BEACON_ACTIVATE, 0.5F, 1.8F);
        playSound(entity, SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.2F);
    }

    /** Takes the hit from the shield. Runs late among incoming-damage listeners, after evasion and similar effects. */
    public static void absorb(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }
        BarrierShield shield = entity.getExistingDataOrNull(PsitweaksAttachments.BARRIER_SHIELD);
        if (shield == null) {
            return;
        }
        if (!shield.isActive(entity.level().getGameTime())) {
            expire(entity, shield);
            return;
        }
        DamageSource source = event.getSource();
        float amount = event.getAmount();
        if (amount <= 0.0F || source.is(PsitweaksTags.DamageTypes.BYPASSES_BARRIER) || entity.isDamageSourceBlocked(source)) {
            return;
        }

        LivingEntityAccessor accessor = (LivingEntityAccessor) entity;
        // Mirror vanilla's hurt cooldown: during it only the part of a hit above the previous hit counts.
        boolean cooldown = entity.invulnerableTime > 10 && !source.is(DamageTypeTags.BYPASSES_COOLDOWN);
        float effective = cooldown ? amount - accessor.psitweaks$getLastHurt() : amount;
        if (effective <= 0.0F) {
            return;
        }

        float absorbed = Math.min(shield.amount(), effective);
        float remaining = shield.amount() - absorbed;
        Vec3 direction = impactDirection(entity, source);
        if (remaining <= 0.001F) {
            entity.removeData(PsitweaksAttachments.BARRIER_SHIELD);
            flash(entity, shield, Style.BARRIER_BREAK, direction, 1.0F);
            playSound(entity, SoundEvents.GLASS_BREAK, 1.0F, 1.3F);
            playSound(entity, SoundEvents.AMETHYST_CLUSTER_BREAK, 1.0F, 0.8F);
        } else {
            entity.setData(PsitweaksAttachments.BARRIER_SHIELD, shield.withAmount(remaining));
            float strength = 0.6F + 0.8F * Mth.clamp(absorbed / Math.max(1.0F, shield.maxAmount() * 0.5F), 0.0F, 1.0F);
            flash(entity, shield, Style.BARRIER_HIT, direction, strength);
            playSound(entity, SoundEvents.AMETHYST_BLOCK_HIT, 1.0F, 1.4F);
            playSound(entity, SoundEvents.SHIELD_BLOCK, 0.5F, 1.6F);
        }

        if (absorbed >= effective - 1.0E-4F) {
            event.setCanceled(true);
            accessor.psitweaks$setLastHurt(amount);
            if (!cooldown) {
                entity.invulnerableTime = event.getContainer().getPostAttackInvulnerabilityTicks();
            }
            PENDING_CONTAINER_CLEANUP.add(entity);
        } else {
            event.setAmount(amount - absorbed);
        }
    }

    /** Removes a timed-out shield. Called regularly while the entity ticks. */
    public static void tick(LivingEntity entity) {
        BarrierShield shield = entity.getExistingDataOrNull(PsitweaksAttachments.BARRIER_SHIELD);
        if (shield != null && !shield.isActive(entity.level().getGameTime())) {
            expire(entity, shield);
        }
    }

    /** Drops the damage containers that cancelled hits left behind. Call at the end of a server tick. */
    public static void cleanUpCancelledHits() {
        if (PENDING_CONTAINER_CLEANUP.isEmpty()) {
            return;
        }
        for (LivingEntity entity : PENDING_CONTAINER_CLEANUP) {
            Stack<DamageContainer> containers = ((LivingEntityAccessor) entity).psitweaks$getDamageContainers();
            if (!containers.isEmpty()) {
                containers.clear();
            }
        }
        PENDING_CONTAINER_CLEANUP.clear();
    }

    private static void expire(LivingEntity entity, BarrierShield shield) {
        entity.removeData(PsitweaksAttachments.BARRIER_SHIELD);
        if (shield.isPresent()) {
            flash(entity, shield, Style.BARRIER_FADE, Vec3.ZERO, 1.0F);
            playSound(entity, SoundEvents.BEACON_DEACTIVATE, 0.4F, 1.8F);
        }
    }

    /** Radius of the honeycomb sphere: it encloses the entity's hitbox with a little room. */
    public static float sphereRadius(LivingEntity entity) {
        return Math.max(entity.getBbWidth(), entity.getBbHeight()) * 0.5F + 0.45F;
    }

    private static void flash(LivingEntity entity, BarrierShield shield, Style style, Vec3 direction, float strength) {
        Vec3 center = entity.position().add(0.0, entity.getBbHeight() * 0.5, 0.0);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new MessageSphereFlash(entity.getId(), center,
                sphereRadius(entity), shield.color(), style, direction, strength));
    }

    /** Unit vector from the entity's centre towards where the hit came from, or zero when unknown. */
    private static Vec3 impactDirection(LivingEntity entity, DamageSource source) {
        Vec3 from = source.getSourcePosition();
        if (from == null) {
            return Vec3.ZERO;
        }
        Vec3 offset = from.subtract(CombatSpellHelper.center(entity));
        return offset.lengthSqr() < 1.0E-6 ? Vec3.ZERO : offset.normalize();
    }

    private static void playSound(LivingEntity entity, SoundEvent sound, float volume, float pitch) {
        entity.level().playSound(null, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(),
                sound, SoundSource.PLAYERS, volume, pitch);
    }
}
