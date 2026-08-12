package com.moratan251.psitweaksqol.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public final class PieceBookmarkButton extends Button {
    private boolean selected;

    public PieceBookmarkButton(OnPress onPress) {
        super(0, 0, 18, 16, Component.literal("☆"), onPress, DEFAULT_NARRATION);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        setMessage(Component.literal(selected ? "★" : "☆"));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int color;
        if (selected) {
            color = isHoveredOrFocused() ? 0xFFFFFF : 0xFFFF55;
        } else {
            color = isHoveredOrFocused() ? 0xFFFFFF : 0xA0A0A0;
        }

        guiGraphics.drawCenteredString(
                Minecraft.getInstance().font,
                getMessage(),
                getX() + getWidth() / 2,
                getY() + 4,
                color
        );
    }
}
