package com.moratan251.psitweaks.client.gui;

import com.moratan251.psitweaks.common.items.BladePouches;
import com.moratan251.psitweaks.common.menu.BladePouchMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** The Blade Pouch screen: a one-row chest with eight sword slots, greyed out while the blades are out. */
public class BladePouchScreen extends AbstractContainerScreen<BladePouchMenu> {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final int ROW_HEIGHT = 18 + 17;
    /** The chest row has nine slots; the ninth is painted over with the panel colour. */
    private static final int PANEL_COLOR = 0xFFC6C6C6;
    private static final int LOCKED_SHADE = 0xA0303030;
    private static final int LOCKED_TEXT_COLOR = 0xB02E26;
    private static final Component DEPLOYED = Component.translatable("gui.psitweaks.blade_pouch.deployed");

    public BladePouchScreen(BladePouchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageHeight = 114 + 18;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (menu.isDeployed()) {
            // Over the swords, so they read as out of reach.
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0F, 0.0F, 300.0F);
            int x = leftPos + BladePouchMenu.SWORDS_X;
            int y = topPos + BladePouchMenu.SWORDS_Y;
            guiGraphics.fill(x, y, x + BladePouches.SLOT_COUNT * 18 - 2, y + 16, LOCKED_SHADE);
            guiGraphics.pose().popPose();
        }
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        if (menu.isDeployed()) {
            guiGraphics.drawString(font, DEPLOYED, imageWidth - 8 - font.width(DEPLOYED), titleLabelY, LOCKED_TEXT_COLOR, false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, ROW_HEIGHT);
        guiGraphics.blit(BACKGROUND, leftPos, topPos + ROW_HEIGHT, 0, 126, imageWidth, 96);
        int spareX = leftPos + BladePouchMenu.SWORDS_X + BladePouches.SLOT_COUNT * 18 - 1;
        int spareY = topPos + BladePouchMenu.SWORDS_Y - 1;
        guiGraphics.fill(spareX, spareY, spareX + 18, spareY + 18, PANEL_COLOR);
    }
}
