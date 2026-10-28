package com.moratan251.psitweaks.client.spellgram;

import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramPsiLock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.common.NeoForge;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.platform.PsiConfig;

/**
 * 「Psi を確保する版」の試作: Psi ゲージの上側に、領域型の魔法式に確保している分を帯で描く。
 * 位置は Psi の HUDHandler.drawPsiBar の中身の部分（幅 16・高さ 106）に合わせる。
 */
public final class SpellGramPsiLockHud {
    private static final ResourceLocation PSI_BAR_LAYER = ResourceLocation.fromNamespaceAndPath("psi", "psi_bar");
    private static final int BAR_WIDTH = 32;
    private static final int BAR_HEIGHT = 140;
    private static final int BAR_PAD = 3;
    private static final int FILL_OFFSET_X = 8;
    private static final int FILL_OFFSET_Y = 26;
    private static final int FILL_WIDTH = 16;
    private static final int FILL_HEIGHT = 106;
    private static final int BAND_COLOR = 0x80FFB040;
    private static final int EDGE_COLOR = 0xFFFFD080;
    private static final int STRIPE_COLOR = 0x40000000;

    private SpellGramPsiLockHud() {
    }

    static void registerEvents() {
        NeoForge.EVENT_BUS.addListener(SpellGramPsiLockHud::onRenderLayer);
        NeoForge.EVENT_BUS.addListener(SpellGramPsiLockHud::onLoggingOut);
    }

    private static void onRenderLayer(RenderGuiLayerEvent.Post event) {
        if (!PSI_BAR_LAYER.equals(event.getName())) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || PsiAPI.getPlayerCAD(mc.player).isEmpty()) {
            return;
        }
        int lock = SpellGramPsiLock.lockOf(mc.player);
        int total = PlayerDataHandler.get(mc.player).getTotalPsi();
        if (lock <= 0 || total <= 0) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        int x = (PsiConfig.client().psiBarOnRight() ? graphics.guiWidth() + BAR_PAD - BAR_WIDTH : -BAR_PAD) + FILL_OFFSET_X;
        int y = graphics.guiHeight() / 2 - BAR_HEIGHT / 2 + FILL_OFFSET_Y;
        int height = Math.max(1, (int) Math.ceil(FILL_HEIGHT * Math.min(1.0D, (double) lock / total)));
        graphics.fill(x, y, x + FILL_WIDTH, y + height, BAND_COLOR);
        for (int stripe = y + 2; stripe < y + height; stripe += 4) {
            graphics.fill(x, stripe, x + FILL_WIDTH, stripe + 1, STRIPE_COLOR);
        }
        graphics.fill(x, y + height - 1, x + FILL_WIDTH, y + height, EDGE_COLOR);
    }

    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        SpellGramPsiLock.setClientLock(0);
    }
}
