package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.spells.combat.BarrierShields;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Wires the shield of Trick: Barrier into the damage pipeline and expires it on time. */
@EventBusSubscriber(modid = Psitweaks.MOD_ID)
public final class BarrierShieldHandler {
    private static final int EXPIRY_CHECK_INTERVAL = 5;

    private BarrierShieldHandler() {
    }

    // HIGHEST: before any listener or vanilla measures this hit against the previous one.
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void beforeLivingIncomingDamage(LivingIncomingDamageEvent event) {
        BarrierShields.restoreBrokenHit(event);
    }

    // LOW: after evasion (Parade) and other listeners that may cancel or reduce the hit, so the shield only pays
    // for what would actually land.
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        BarrierShields.absorb(event);
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity living && !living.level().isClientSide
                && living.tickCount % EXPIRY_CHECK_INTERVAL == 0) {
            BarrierShields.tick(living);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        BarrierShields.cleanUpAfterTick();
    }
}
