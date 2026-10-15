package com.moratan251.psitweaks.client.gui;

import com.moratan251.psitweaks.common.items.FilterCards;
import com.moratan251.psitweaks.common.menu.FilterCardMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class FilterCardScreen extends AbstractContainerScreen<FilterCardMenu> {
    // The vanilla dispenser screen already has a 3x3 grid above the player inventory.
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.withDefaultNamespace("textures/gui/container/dispenser.png");
    private static final int SLOT_NUMBER_COLOR = 0x8B8B8B;

    public FilterCardScreen(FilterCardMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        // Empty template slots show the number a spell uses to read them.
        for (int index = 0; index < FilterCards.SLOT_COUNT; index++) {
            Slot slot = menu.slots.get(index);
            if (!slot.hasItem()) {
                String number = String.valueOf(index + 1);
                guiGraphics.drawString(font, number, slot.x + 8 - font.width(number) / 2, slot.y + 4,
                        SLOT_NUMBER_COLOR, false);
            }
        }
    }
}
