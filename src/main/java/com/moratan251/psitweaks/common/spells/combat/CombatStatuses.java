package com.moratan251.psitweaks.common.spells.combat;

import com.moratan251.psitweaks.common.attachments.PsitweaksAttachments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * The wet state Trick: Aqua Cutter leaves on a creature. It is built on vanilla's own check, so other mods share it
 * without depending on Psitweaks: a soaked creature counts as in water or rain ({@code isInWaterOrRain}), the way Ars
 * Nouveau adds its Soaked effect and Bubble to that check. Anything that looks for it sees the creature as wet: Trick:
 * Chain Lightning's extra damage, Ars Nouveau's Cold Snap and lightning, a Riptide trident. Like a creature soaked by
 * Ars Nouveau, it does not catch fire while wet.
 */
public final class CombatStatuses {
    /** How long Trick: Aqua Cutter leaves a creature wet: 10 seconds. */
    public static final int AQUA_CUTTER_WET_TICKS = 200;

    private CombatStatuses() {
    }

    /** Whether the entity is soaked by Trick: Aqua Cutter. Works on both sides, as the state is synced. */
    public static boolean isSoaked(Entity entity) {
        Long wetUntil = entity.getExistingDataOrNull(PsitweaksAttachments.SOAK);
        return wetUntil != null && wetUntil > entity.level().getGameTime();
    }

    /** Makes the creature wet for {@code ticks} (longer if it already was) and puts out its fire. Server side. */
    public static void soak(LivingEntity entity, int ticks) {
        long wetUntil = entity.level().getGameTime() + Math.max(1, ticks);
        Long current = entity.getExistingDataOrNull(PsitweaksAttachments.SOAK);
        if (current != null && current > wetUntil) {
            wetUntil = current;
        }
        entity.setData(PsitweaksAttachments.SOAK, wetUntil);
        if (entity.getRemainingFireTicks() > 0) {
            entity.clearFire();
        }
    }

    /** Ends the wet state on time and draws the drops of a wet creature. Both sides. */
    public static void tick(LivingEntity entity) {
        if (!entity.hasData(PsitweaksAttachments.SOAK)) {
            return;
        }
        boolean soaked = isSoaked(entity);
        if (entity.level().isClientSide) {
            if (soaked && entity.tickCount % 3 == 0) {
                drip(entity);
            }
        } else if (!soaked) {
            entity.removeData(PsitweaksAttachments.SOAK);
        }
    }

    private static void drip(LivingEntity entity) {
        double x = entity.getRandomX(0.6);
        double y = entity.getY() + entity.getBbHeight() * (0.3 + 0.6 * entity.getRandom().nextDouble());
        double z = entity.getRandomZ(0.6);
        entity.level().addParticle(ParticleTypes.DRIPPING_WATER, x, y, z, 0.0, 0.0, 0.0);
    }
}
