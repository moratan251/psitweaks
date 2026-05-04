package com.moratan251.psitweaks.client.gui.machine;

import com.moratan251.psitweaks.common.menu.PortableCADAssemblerMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.cad.EnumCADStat;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.common.Psi;
import vazkii.psi.common.block.base.ModBlocks;
import vazkii.psi.common.block.tile.TileCADAssembler;

public class GuiPortableCADAssembler extends AbstractContainerScreen<PortableCADAssemblerMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("psi:textures/gui/cad_assembler.png");

    private final Player player;
    private final TileCADAssembler assembler;

    public GuiPortableCADAssembler(PortableCADAssemblerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.player = inventory.player;
        this.assembler = menu.getAssembler();
        this.imageWidth = 256;
        this.imageHeight = 225;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int color = 4210752;
        String name = new ItemStack(ModBlocks.cadAssembler.get()).getHoverName().getString();
        graphics.drawString(this.font, name, this.imageWidth / 2 - this.font.width(name) / 2, 10, color, false);

        ItemStack cad = this.assembler.getCachedCAD(this.player);
        if (!cad.isEmpty()) {
            color = 16777215;
            ICAD cadItem = (ICAD) cad.getItem();
            String statHeader = ChatFormatting.BOLD + I18n.get("psimisc.stats");
            graphics.drawString(this.font, statHeader, 213 - this.font.width(statHeader) / 2, 32, color, true);

            int index = 0;
            for (EnumCADStat stat : EnumCADStat.values()) {
                String row = (Psi.magical ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.AQUA)
                        + I18n.get(stat.getName())
                        + ChatFormatting.RESET
                        + ": " + cadItem.getStatValue(cad, stat);
                graphics.drawString(this.font, row, 179, 45 + index * 10, color, true);
                index++;
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        for (int slot = 0; slot < 12; slot++) {
            if (!this.assembler.isBulletSlotEnabled(slot)) {
                graphics.blit(TEXTURE, x + 17 + slot % 3 * 18, y + 57 + slot / 3 * 18, 16, this.imageHeight, 16, 16);
            }
        }
    }
}
