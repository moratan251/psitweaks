package com.moratan251.psitweaks.client.gui.machine;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.lib.Color;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Mekanism の {@link mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar} の縦向き版。下から満ち、量に応じて色が変わる。
 */
public class GuiDynamicVerticalRateBar extends GuiBar<GuiBar.IBarInfoHandler> {
    private static final ResourceLocation RATE_BAR = MekanismUtils.getResource(ResourceType.GUI_BAR, "dynamic_rate.png");
    private static final int TEX_WIDTH = 3;
    private static final int TEX_HEIGHT = 8;

    private final Color.ColorFunction colorFunction;

    public GuiDynamicVerticalRateBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y, int width, int height,
            Color.ColorFunction colorFunction) {
        super(RATE_BAR, gui, handler, x, y, width, height, false);
        this.colorFunction = colorFunction;
    }

    @Override
    protected void renderBarOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
        int innerWidth = width - 2;
        int innerHeight = height - 2;
        int displayInt = (int) (handlerLevel * innerHeight);
        // 横向きのテクスチャの行を1段ずつ、左端・中央（引き伸ばし）・右端に分けて描く。
        // 満ちた部分の上端は明るい縁の行、下端は暗い縁の行、間は中ほどの行を使う。
        for (int i = 0; i < displayInt; i++) {
            MekanismRenderer.color(guiGraphics, colorFunction.getColor(i / (float) innerHeight));
            int v = i == displayInt - 1 ? 0 : i == 0 ? TEX_HEIGHT - 1 : 1;
            int y = relativeY + innerHeight - i;
            guiGraphics.blit(getResource(), relativeX + 1, y, 0, v, 1, 1, TEX_WIDTH, TEX_HEIGHT);
            guiGraphics.blit(getResource(), relativeX + 2, y, innerWidth - 2, 1, 1, v, 1, 1, TEX_WIDTH, TEX_HEIGHT);
            guiGraphics.blit(getResource(), relativeX + innerWidth, y, TEX_WIDTH - 1, v, 1, 1, TEX_WIDTH, TEX_HEIGHT);
        }
        MekanismRenderer.resetColor(guiGraphics);
    }
}
