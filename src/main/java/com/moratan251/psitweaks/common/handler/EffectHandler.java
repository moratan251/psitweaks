package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.effects.FlightPsiCostProfile;
import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.registries.PsitweaksTags;
import java.util.Objects;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

@EventBusSubscriber(modid = Psitweaks.MOD_ID)
public final class EffectHandler {
    private EffectHandler() {
    }

    @SubscribeEvent
    public static void onMobEffectAdded(MobEffectEvent.Added event) {
        if (!event.getEntity().level().isClientSide
                && event.getEffectInstance().is(PsitweaksEffects.FLIGHT)) {
            FlightPsiCostProfile.clear(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().is(PsitweaksDamageTypes.CARBON_POISONING)) {
            event.setInvulnerabilityTicks(0);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (event.getSource().is(PsitweaksDamageTypes.CARBON_POISONING)) {
            return;
        }

        LivingEntity entity = event.getEntity();
        float damage = event.getNewDamage();
        // Trick: Barrier no longer uses its effect; its shield is handled by BarrierShieldHandler.
        if (entity.hasEffect(PsitweaksEffects.HARDENING) && isCappedByHardening(event.getSource())) {
            MobEffectInstance effect = entity.getEffect(PsitweaksEffects.HARDENING);
            damage = Math.min(damage, hardeningCap(Objects.requireNonNull(effect).getAmplifier()));
        }

        event.setNewDamage(Math.max(0.0F, damage));
    }

    /**
     * Whether Trick: Hardening caps this damage: damage that armor reduces, plus {@link PsitweaksTags.DamageTypes#HARDENING_REDUCES}.
     * Poison, magic, /kill, the void and other damage that bypasses armor pass through.
     */
    public static boolean isCappedByHardening(DamageSource source) {
        return !source.is(DamageTypeTags.BYPASSES_ARMOR) || source.is(PsitweaksTags.DamageTypes.HARDENING_REDUCES);
    }

    /** The most damage one hit deals under Hardening of this amplifier: 12 − 2 × level for levels 1 to 5, then 1. */
    public static float hardeningCap(int amplifier) {
        return amplifier <= 4 ? 12.0F - (amplifier + 1) * 2.0F : 1.0F;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void keepCarbonPoisoningDamageNonLethal(LivingDamageEvent.Pre event) {
        if (!event.getSource().is(PsitweaksDamageTypes.CARBON_POISONING)) {
            return;
        }

        float maximumDamage = Math.max(0.0F, event.getEntity().getHealth() - 1.0F);
        event.setNewDamage(Math.min(event.getNewDamage(), maximumDamage));
    }
}
