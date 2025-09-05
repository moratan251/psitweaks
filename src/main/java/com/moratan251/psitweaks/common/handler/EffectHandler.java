package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = "psitweaks")
public class EffectHandler {

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
        float damage = event.getAmount();
        if (entity.hasEffect(PsitweaksEffects.BARRIER.get())) {
            MobEffectInstance effectBarrier = entity.getEffect(PsitweaksEffects.BARRIER.get());
            int ampBarrier = Objects.requireNonNull(effectBarrier).getAmplifier();
            damage -= (ampBarrier + 1) * 4;

        }
        if (entity.hasEffect(PsitweaksEffects.HARDENING.get())) {
            MobEffectInstance effectHardening = entity.getEffect(PsitweaksEffects.HARDENING.get());
            int ampHardening = Objects.requireNonNull(effectHardening).getAmplifier();
            if(ampHardening <= 4) {
                damage = Math.min(damage, 12.0F - (ampHardening + 1) * 2.0F);
            }else{
                damage = Math.min(damage, 1.0F);
            }

        }
        event.setAmount(Math.max(0,damage));
    }
}

