package com.moratan251.psitweaks.client.spellgram;

import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramPotencyLoad;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.common.NeoForge;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.common.platform.PsiConfig;

/**
 * 「CAD の規模で数える版」の試作: 領域型の魔法式を置いている間、Psi ゲージの上に「枠 使用量/大きさ」を出す。
 * 位置は Psi の HUDHandler.drawPsiBar のゲージ（幅 32・高さ 140）に合わせる。
 */
public final class SpellGramLoadHud {
    private static final ResourceLocation PSI_BAR_LAYER = ResourceLocation.fromNamespaceAndPath("psi", "psi_bar");
    private static final int BAR_WIDTH = 32;
    private static final int BAR_HEIGHT = 140;
    private static final int BAR_PAD = 3;
    private static final int TEXT_COLOR = 0xFFFFD080;
    private static final int FULL_COLOR = 0xFFFF8060;

    private SpellGramLoadHud() {
    }

    static void registerEvents() {
        NeoForge.EVENT_BUS.addListener(SpellGramLoadHud::onRenderLayer);
        NeoForge.EVENT_BUS.addListener(SpellGramLoadHud::onLoggingOut);
    }

    private static void onRenderLayer(RenderGuiLayerEvent.Post event) {
        if (!PSI_BAR_LAYER.equals(event.getName())) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || PsiAPI.getPlayerCAD(mc.player).isEmpty()) {
            return;
        }
        int load = SpellGramPotencyLoad.loadOf(mc.player);
        if (load <= 0) {
            return;
        }
        int capacity = SpellGramPotencyLoad.capacityOf(mc.player);
        Component text = Component.translatable("psitweaks.spellgram_region.load", load, capacity >= 0 ? capacity : "∞");
        GuiGraphics graphics = event.getGuiGraphics();
        int width = mc.font.width(text);
        boolean right = PsiConfig.client().psiBarOnRight();
        int barX = right ? graphics.guiWidth() + BAR_PAD - BAR_WIDTH : -BAR_PAD;
        int x = right ? graphics.guiWidth() - width - 2 : Math.max(2, barX + 8);
        int y = graphics.guiHeight() / 2 - BAR_HEIGHT / 2 - 10;
        int color = capacity >= 0 && load >= capacity ? FULL_COLOR : TEXT_COLOR;
        graphics.drawString(mc.font, text, x, y, color, true);
    }

    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        SpellGramPotencyLoad.setClientLoad(0);
    }
}
