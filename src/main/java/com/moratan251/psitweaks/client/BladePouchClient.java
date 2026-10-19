package com.moratan251.psitweaks.client;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.items.BladePouches;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Client side of the Blade Pouch: the in-use / idle texture and the idle tooltip line, as for the Filter Card. */
public final class BladePouchClient {
    private BladePouchClient() {
    }

    /** Call from client setup's enqueued work. */
    public static void registerItemProperties() {
        // 1 = normal texture. Only a pouch the local player carries behind the pouch in use reads 0 (idle texture).
        ItemProperties.register(PsitweaksItems.BLADE_POUCH.get(), Psitweaks.location("active"),
                (stack, level, entity, seed) -> entity instanceof Player player && BladePouches.isIdle(player, stack)
                        ? 0.0F : 1.0F);
    }

    public static void registerEvents() {
        NeoForge.EVENT_BUS.addListener(BladePouchClient::onItemTooltip);
    }

    private static void onItemTooltip(ItemTooltipEvent event) {
        Player player = event.getEntity();
        if (player != null && BladePouches.isIdle(player, event.getItemStack())) {
            event.getToolTip().add(Component.translatable("tooltip.psitweaks.blade_pouch.idle")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
