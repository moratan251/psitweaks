package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
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
}

