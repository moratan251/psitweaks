package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.common.spells.spellpiece.trick.MassBlockBreakHelper;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.MassBlockBreakHelper.ActiveBreakContext;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.MassBlockBreakHelper.MassBreakDrops;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

public final class MassBlockBreakDropHandler {
    private static final double ENTITY_CAPTURE_RADIUS = 1.5D;
    private static final double ENTITY_CAPTURE_RADIUS_SQ = ENTITY_CAPTURE_RADIUS * ENTITY_CAPTURE_RADIUS;

    private MassBlockBreakDropHandler() {
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (event.isCanceled()) {
            return;
        }
        if (!(event.getBreaker() instanceof ServerPlayer player)) {
            return;
        }
        ActiveBreakContext context = MassBlockBreakHelper.getActiveBreakContext();
        if (context == null || !player.getUUID().equals(context.breakerUuid())) {
            return;
        }
        if (player.level().isClientSide()) {
            return;
        }
        if (event.getLevel() != context.level() || !event.getPos().equals(context.currentPos())) {
            return;
        }

        MassBreakDrops loot = context.drops();
        if (loot == null) {
            return;
        }

        for (ItemEntity drop : event.getDrops()) {
            loot.addDrop(drop.getItem().copy());
        }
        loot.addExperience(event.getDroppedExperience());

        event.getDrops().clear();
        event.setDroppedExperience(0);
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (event.isCanceled()) {
            return;
        }

        ActiveBreakContext context = MassBlockBreakHelper.getActiveBreakContext();
        if (context == null || event.getLevel() != context.level()) {
            return;
        }

        Entity entity = event.getEntity();
        if (!isNearCurrentBlock(entity, context.currentPos())) {
            return;
        }

        MassBreakDrops loot = context.drops();
        if (loot == null) {
            return;
        }

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

    private static boolean isNearCurrentBlock(Entity entity, BlockPos pos) {
        double dx = entity.getX() - (pos.getX() + 0.5D);
        double dy = entity.getY() - (pos.getY() + 0.5D);
        double dz = entity.getZ() - (pos.getZ() + 0.5D);
        return dx * dx + dy * dy + dz * dz <= ENTITY_CAPTURE_RADIUS_SQ;
    }
}
