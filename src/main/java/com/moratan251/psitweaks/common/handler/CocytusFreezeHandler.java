package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.spells.combat.CocytusFreezes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Melts the ice of Trick: Cocytus once something turns a frozen mob's AI back on. */
@EventBusSubscriber(modid = Psitweaks.MOD_ID)
public final class CocytusFreezeHandler {
    private CocytusFreezeHandler() {
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Mob mob && mob.level() instanceof ServerLevel level) {
            CocytusFreezes.tick(level, mob);
        }
    }
}
