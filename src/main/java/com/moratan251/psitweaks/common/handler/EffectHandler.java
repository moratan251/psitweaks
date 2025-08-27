package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.common.effects.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;
import java.util.Random;

@Mod.EventBusSubscriber(modid = "psitweaks")
public class EffectHandler {

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(ModEffects.PARADE.get())) {
            MobEffectInstance effect = entity.getEffect(ModEffects.PARADE.get());
            int amp = Objects.requireNonNull(effect).getAmplifier();
            double chance = 0.70 + amp * 0.075;

            if (Math.random() < chance) {
                event.setCanceled(true);
            }
        }
    }
}

