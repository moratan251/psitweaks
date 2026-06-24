package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.common.spells.spellpiece.trick.MassBlockBreakHelper;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.MassBlockBreakHelper.MassBreakDrops;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public final class MassBlockBreakDropHandler {
    private MassBlockBreakDropHandler() {
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (MassBlockBreakHelper.getActiveBreaker() == null) {
            return;
        }

        MassBreakDrops loot = MassBlockBreakHelper.getActiveDrops();
        if (loot == null) {
            return;
        }

        Entity entity = event.getEntity();
        if (entity instanceof ItemEntity item) {
            ItemStack stack = item.getItem();
            if (!stack.isEmpty()) {
                loot.addDrop(stack.copy());
            }
            event.setCanceled(true);
        } else if (entity instanceof ExperienceOrb orb) {
            loot.addExperience(orb.getValue());
            event.setCanceled(true);
        }
    }
}
