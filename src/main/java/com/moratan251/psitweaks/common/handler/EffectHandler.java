package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.effects.FlightPsiCostProfile;
import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import java.util.Objects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
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
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(PsitweaksEffects.PARADE)) {
            MobEffectInstance effect = entity.getEffect(PsitweaksEffects.PARADE);
            int amplifier = Objects.requireNonNull(effect).getAmplifier();
            double chance = 0.70 + amplifier * 0.075;

            if (Math.random() < chance) {
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        float damage = event.getNewDamage();
        if (entity.hasEffect(PsitweaksEffects.BARRIER)) {
            MobEffectInstance effect = entity.getEffect(PsitweaksEffects.BARRIER);
            int amplifier = Objects.requireNonNull(effect).getAmplifier();
            damage -= (amplifier + 1) * 4.0F;
        }
        if (entity.hasEffect(PsitweaksEffects.HARDENING)) {
            MobEffectInstance effect = entity.getEffect(PsitweaksEffects.HARDENING);
            int amplifier = Objects.requireNonNull(effect).getAmplifier();
            if (amplifier <= 4) {
                damage = Math.min(damage, 12.0F - (amplifier + 1) * 2.0F);
            } else {
                damage = Math.min(damage, 1.0F);
            }
        }

        event.setNewDamage(Math.max(0.0F, damage));
    }
}
