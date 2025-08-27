package com.moratan251.psitweaks.common.handler;


import com.moratan251.psitweaks.common.items.ModItems;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import vazkii.psi.api.internal.IPlayerData;
import vazkii.psi.common.core.handler.PlayerDataHandler;

/*
@Mod.EventBusSubscriber
public class PsiRegenHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        var player = event.player;
        IPlayerData data = PlayerDataHandler.get(player);
        if (data == null) return;

        boolean hasRing = CuriosApi.getCuriosHelper()
                .findEquippedCurio(ModItems.PSI_RING.get(), player)
                .isPresent();

        if (hasRing) {
            if (player.tickCount % 20 == 0) { // 毎秒
                int regen = 50; // 回復量
                int current = data.getAvailablePsi();
                int max = data.getTotalPsi();
                int newPsi = Math.min(current + regen, max);

                // 直接代入で回復
                data.availablePsi = newPsi;
            }
        }
    }
}

 */
