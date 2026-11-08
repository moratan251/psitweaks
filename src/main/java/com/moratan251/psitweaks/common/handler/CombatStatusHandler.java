package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.attachments.PsitweaksAttachments;
import com.moratan251.psitweaks.common.spells.combat.CombatStatuses;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Ends the wet state of Trick: Aqua Cutter on time and draws the drops of wet creatures (see {@link CombatStatuses}). */
@EventBusSubscriber(modid = Psitweaks.MOD_ID)
public final class CombatStatusHandler {
    private CombatStatusHandler() {
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity living && living.hasData(PsitweaksAttachments.SOAK)) {
            CombatStatuses.tick(living);
        }
    }
}
