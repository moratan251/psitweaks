package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = "psitweaks")
public class EffectHandler {

    @SubscribeEvent
    public static void onMobEffectAdded(MobEffectEvent.Added event) {
        if (event.getEntity().level().isClientSide
                || event.getEffectInstance().getEffect() != PsitweaksEffects.CARBON_POISONING.get()) {
            return;
        }

        int duration = event.getEffectInstance().getDuration();
        LivingEntity entity = event.getEntity();
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 3), event.getEffectSource());
        entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration, 1), event.getEffectSource());
        entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration, 0), event.getEffectSource());
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(PsitweaksEffects.PARADE.get())) {
            MobEffectInstance effect = entity.getEffect(PsitweaksEffects.PARADE.get());
            int amp = Objects.requireNonNull(effect).getAmplifier();
            double chance = 0.70 + amp * 0.075;

            if (Math.random() < chance) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (event.getSource().is(PsitweaksDamageTypes.CARBON_POISONING)) {
            entity.invulnerableTime = 0;
            return;
        }

        float damage = event.getAmount();
        if (entity.hasEffect(PsitweaksEffects.BARRIER.get())) {
            MobEffectInstance effectBarrier = entity.getEffect(PsitweaksEffects.BARRIER.get());
            int ampBarrier = Objects.requireNonNull(effectBarrier).getAmplifier();
            damage -= (ampBarrier + 1) * 4;

        }
        if (entity.hasEffect(PsitweaksEffects.HARDENING.get())) {
            MobEffectInstance effectHardening = entity.getEffect(PsitweaksEffects.HARDENING.get());
            int ampHardening = Objects.requireNonNull(effectHardening).getAmplifier();
            if (ampHardening <= 4) {
                damage = Math.min(damage, 12.0F - (ampHardening + 1) * 2.0F);
            } else {
                damage = Math.min(damage, 1.0F);
            }

        }
        event.setAmount(Math.max(0, damage));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void keepCarbonPoisoningDamageNonLethal(LivingDamageEvent event) {
        if (!event.getSource().is(PsitweaksDamageTypes.CARBON_POISONING)) {
            return;
        }

        float maximumDamage = Math.max(0.0F, event.getEntity().getHealth() - 1.0F);
        event.setAmount(Math.min(event.getAmount(), maximumDamage));
        event.getEntity().invulnerableTime = 0;
    }
}

