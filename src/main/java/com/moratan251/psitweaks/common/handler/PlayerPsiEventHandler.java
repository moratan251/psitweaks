package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.items.curios.CuriosSlotChecker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import vazkii.psi.api.exosuit.IPsiEventArmor;
import vazkii.psi.api.exosuit.PsiArmorEvent;

@EventBusSubscriber(modid = Psitweaks.MOD_ID)
public final class PlayerPsiEventHandler {
    private PlayerPsiEventHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.isSpectator() || player.level().isClientSide) {
            return;
        }

        PsiArmorEvent.post(new PsiArmorEvent(player, "psitweaks.event.custom_tick"));
    }

    @SubscribeEvent
    public static void onPsiCurioEvent(PsiArmorEvent event) {
        Player player = event.getEntity();
        if (player.isSpectator() || !ModList.get().isLoaded("curios")) {
            return;
        }

        for (int slot = 0; slot < 4; slot++) {
            ItemStack curio = CuriosSlotChecker.getItemFromSelectedMagicSlot(player, slot);
            if (curio.getItem() instanceof IPsiEventArmor handler) {
                handler.onEvent(curio, event);
            }
        }
    }
}
