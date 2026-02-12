package com.moratan251.psitweaks.client.gui.machine;

import com.moratan251.psitweaks.common.menu.ProgramResearcherMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiProgramResearcher extends AbstractContainerScreen<ProgramResearcherMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/dispenser.png");

    private static final int ENERGY_X = 8;
    private static final int ENERGY_Y = 17;
    private static final int ENERGY_WIDTH = 8;
    private static final int ENERGY_HEIGHT = 54;

    private static final int PROGRESS_X = 104;
    private static final int PROGRESS_Y = 34;
    private static final int PROGRESS_WIDTH = 24;
    private static final int PROGRESS_HEIGHT = 16;

    private static final int INPUT_START_X = 38;
    private static final int INPUT_START_Y = 18;
    private static final int OUTPUT_X = 134;
    private static final int OUTPUT_Y = 35;
    private static final int SLOT_U = 61;
    private static final int SLOT_V = 16;

    private static final int GRID_MASK_COLOR = 0xFFC6C6C6;
    private static final int BAR_BACKGROUND_COLOR = 0xFF2B2B2B;
    private static final int PROGRESS_COLOR = 0xFF5ACF6B;
    private static final int ENERGY_COLOR = 0xFF4FA3FF;

    public GuiProgramResearcher(ProgramResearcherMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (isHovering(ENERGY_X, ENERGY_Y, ENERGY_WIDTH, ENERGY_HEIGHT, mouseX, mouseY)) {
            graphics.renderTooltip(
                    this.font,
                    Component.literal(menu.getEnergyStored() + " / " + menu.getMaxEnergyStored() + " FE"),
                    mouseX,
                    mouseY
            );
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        // Hide dispenser's built-in 3x3 slot art so only our custom slot layout is visible.
        graphics.fill(x + 61, y + 16, x + 116, y + 71, GRID_MASK_COLOR);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotX = INPUT_START_X + col * 18;
                int slotY = INPUT_START_Y + row * 18;
                drawSlotFrame(graphics, x, y, slotX, slotY);
            }
        }
        drawSlotFrame(graphics, x, y, OUTPUT_X, OUTPUT_Y);

        // Progress bar
        graphics.fill(x + PROGRESS_X, y + PROGRESS_Y, x + PROGRESS_X + PROGRESS_WIDTH, y + PROGRESS_Y + PROGRESS_HEIGHT, BAR_BACKGROUND_COLOR);
        int progress = menu.getScaledProgress(PROGRESS_WIDTH);
        if (progress > 0) {
            graphics.fill(x + PROGRESS_X, y + PROGRESS_Y, x + PROGRESS_X + progress, y + PROGRESS_Y + PROGRESS_HEIGHT, PROGRESS_COLOR);
        }

        // Energy bar
        graphics.fill(x + ENERGY_X, y + ENERGY_Y, x + ENERGY_X + ENERGY_WIDTH, y + ENERGY_Y + ENERGY_HEIGHT, BAR_BACKGROUND_COLOR);
        int energyHeight = menu.getScaledEnergy(ENERGY_HEIGHT);
        if (energyHeight > 0) {
            int top = y + ENERGY_Y + ENERGY_HEIGHT - energyHeight;
            graphics.fill(x + ENERGY_X, top, x + ENERGY_X + ENERGY_WIDTH, y + ENERGY_Y + ENERGY_HEIGHT, ENERGY_COLOR);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }

    private void drawSlotFrame(GuiGraphics graphics, int leftPos, int topPos, int slotX, int slotY) {
        graphics.blit(TEXTURE, leftPos + slotX - 1, topPos + slotY - 1, SLOT_U, SLOT_V, 18, 18);
    }
}
