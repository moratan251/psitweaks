package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import java.util.Objects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Psitweaks.MOD_ID)
public final class EffectHandler {
    private EffectHandler() {
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

    @SubscribeEvent
    public static void onFlightEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect().is(PsitweaksEffects.FLIGHT)) {
            disableSurvivalFlight(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onFlightEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance effect = event.getEffectInstance();
        if (effect != null && effect.getEffect().is(PsitweaksEffects.FLIGHT)) {
            disableSurvivalFlight(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeGameMode(PlayerEvent.PlayerChangeGameModeEvent event) {
        Player player = event.getEntity();
        GameType oldGameMode = event.getCurrentGameMode();
        GameType newGameMode = event.getNewGameMode();
        if ((oldGameMode == GameType.CREATIVE || oldGameMode == GameType.SPECTATOR)
                && (newGameMode == GameType.ADVENTURE || newGameMode == GameType.SURVIVAL)
                && player.hasEffect(PsitweaksEffects.FLIGHT)) {
            player.getAbilities().flying = true;
        }
    }

    private static void disableSurvivalFlight(LivingEntity entity) {
        if (entity instanceof Player player && !player.level().isClientSide && !player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }
}
