package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.items.ItemGravstringer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Psitweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GravstringerModeHandler {
    private GravstringerModeHandler() {
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START
                || !player.isShiftKeyDown()) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof ItemGravstringer) {
            ItemGravstringer.cycleArrowMode(stack, player);
        }
    }
}
