package com.moratan251.psitweaksqol.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;

/** ピース一覧のタブ 1 つ分。枠は描かず、選択中は黄色、それ以外は灰色の文字で示す。 */
public final class PiecePanelTabButton extends Button {
    public static final int WIDTH = 14;
    public static final int HEIGHT = 16;

    static final int SELECTED_COLOR = 0xFFFF55;
    static final int UNSELECTED_COLOR = 0xA0A0A0;
    static final int HOVERED_COLOR = 0xFFFFFF;
    static final int SELECTED_UNDERLINE_COLOR = 0xFFFFFF55;

    private final PiecePanelTab tab;
    private boolean selected;

    public PiecePanelTabButton(PiecePanelTab tab, OnPress onPress) {
        super(0, 0, WIDTH, HEIGHT, tab.label(false), onPress, DEFAULT_NARRATION);
        this.tab = tab;
    }

    public PiecePanelTab getTab() {
        return tab;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        setMessage(tab.label(selected));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int color;
        if (isHoveredOrFocused()) {
            color = HOVERED_COLOR;
        } else {
            color = selected ? SELECTED_COLOR : UNSELECTED_COLOR;
        }

        if (tab == PiecePanelTab.RECENT) {
            // フォントの「↺」は小さくつぶれて読めなかったので、時計を点で描く。
            PixelIcon.draw(guiGraphics, PixelIcon.CLOCK,
                    getX() + (getWidth() - PixelIcon.CLOCK.length) / 2, getY() + 3, 0xFF000000 | color);
        } else {
            guiGraphics.drawCenteredString(
                    Minecraft.getInstance().font,
                    getMessage(),
                    getX() + getWidth() / 2,
                    getY() + 4,
                    color
            );
        }
        if (selected) {
            guiGraphics.fill(getX() + 2, getY() + HEIGHT - 2, getX() + getWidth() - 2, getY() + HEIGHT - 1,
                    SELECTED_UNDERLINE_COLOR);
        }
    }
}
