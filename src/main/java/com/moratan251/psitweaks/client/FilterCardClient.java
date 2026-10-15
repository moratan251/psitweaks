package com.moratan251.psitweaks.client;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.items.FilterCards;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

/** Client side of the Filter Card: the in-use / idle texture and the idle tooltip line. */
public final class FilterCardClient {
    private FilterCardClient() {
    }

    /** Call from client setup's enqueued work. */
    public static void registerItemProperties() {
        // 1 = normal texture. Only a card the local player carries behind the card in use reads 0 (idle texture).
        ItemProperties.register(PsitweaksItems.FILTER_CARD.get(), Psitweaks.location("active"),
                (stack, level, entity, seed) -> entity instanceof Player player && FilterCards.isIdle(player, stack)
                        ? 0.0F : 1.0F);
    }

    public static void registerEvents() {
        MinecraftForge.EVENT_BUS.addListener(FilterCardClient::onItemTooltip);
    }

    private static void onItemTooltip(ItemTooltipEvent event) {
        Player player = event.getEntity();
        if (player != null && FilterCards.isIdle(player, event.getItemStack())) {
            event.getToolTip().add(Component.translatable("tooltip.psitweaks.filter_card.idle")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
