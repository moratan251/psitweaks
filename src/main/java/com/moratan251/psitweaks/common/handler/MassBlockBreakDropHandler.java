package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.common.spells.spellpiece.trick.MassBlockBreakHelper;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.MassBlockBreakHelper.MassBreakDrops;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

public final class MassBlockBreakDropHandler {
    private MassBlockBreakDropHandler() {
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof ServerPlayer player)) {
            return;
        }
        UUID activeBreaker = MassBlockBreakHelper.getActiveBreaker();
        if (activeBreaker == null || !player.getUUID().equals(activeBreaker)) {
            return;
        }
        if (player.level().isClientSide()) {
            return;
        }

        MassBreakDrops loot = MassBlockBreakHelper.getActiveDrops();
        if (loot == null) {
            return;
        }

        for (ItemEntity drop : event.getDrops()) {
            loot.addDrop(drop.getItem().copy());
        }
        loot.addExperience(event.getDroppedExperience());

        event.setCanceled(true);

        // BlockDropsEvent のキャンセルでスキップされる spawnAfterBreak 副作用を手動で維持
        event.getState().spawnAfterBreak((ServerLevel) event.getLevel(), event.getPos(), player.getMainHandItem(), false);
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
