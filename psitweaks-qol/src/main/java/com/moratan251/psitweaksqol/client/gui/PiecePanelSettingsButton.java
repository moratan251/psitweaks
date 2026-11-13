package com.moratan251.psitweaksqol.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** タブの列の右端に置く歯車。押すとタブの列が一覧の広さの設定に替わり、もう一度押すと戻る。 */
public final class PiecePanelSettingsButton extends Button {
    public static final String TOOLTIP_KEY = "gui.psitweaks_qol.spell_programmer.settings";
    public static final String CLOSE_TOOLTIP_KEY = "gui.psitweaks_qol.spell_programmer.settings.close";

    private boolean selected;

    public PiecePanelSettingsButton(OnPress onPress) {
        super(0, 0, PiecePanelTabButton.WIDTH, PiecePanelTabButton.HEIGHT,
                Component.translatable(TOOLTIP_KEY), onPress, DEFAULT_NARRATION);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int color;
        if (isHoveredOrFocused()) {
            color = PiecePanelTabButton.HOVERED_COLOR;
        } else {
            color = selected ? PiecePanelTabButton.SELECTED_COLOR : PiecePanelTabButton.UNSELECTED_COLOR;
        }

        PixelIcon.draw(guiGraphics, PixelIcon.GEAR,
                getX() + (getWidth() - PixelIcon.GEAR.length) / 2, getY() + 3, 0xFF000000 | color);
        if (selected) {
            guiGraphics.fill(getX() + 2, getY() + getHeight() - 2, getX() + getWidth() - 2, getY() + getHeight() - 1,
                    PiecePanelTabButton.SELECTED_UNDERLINE_COLOR);
        }
    }
}
