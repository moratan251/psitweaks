package com.moratan251.psitweaks.client.guis;

import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.items.data.PortableCADAssemblerData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.cad.EnumCADStat;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.client.gui.GuiCADAssembler;
import vazkii.psi.common.Psi;
import vazkii.psi.common.block.tile.container.ContainerCADAssembler;

public class GuiPortableCADAssembler extends GuiCADAssembler {
    private Player player;
    private PortableCADAssemblerData assembler;

    public GuiPortableCADAssembler(ContainerCADAssembler containerCADAssembler, Inventory inventory, Component component) {
        super(containerCADAssembler, inventory, component);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int color = 4210752;
        String name = (new ItemStack(PsitweaksItems.PORTABLE_CAD_ASSEMBLER.get())).getHoverName().getString();
        graphics.drawString(this.font, name, this.imageWidth / 2 - this.font.width(name) / 2, 10, color, false);
        ItemStack cad = this.assembler.getCachedCAD(this.player);
        if (!cad.isEmpty()) {
            color = 16777215;
            int i = 0;
            ICAD cadItem = (ICAD)cad.getItem();
            String stats = I18n.get("psimisc.stats", new Object[0]);
            String var10000 = String.valueOf(ChatFormatting.BOLD);
            String s = var10000 + stats;
            graphics.drawString(this.font, s, 213.0F - (float)this.font.width(s) / 2.0F, 32.0F, color, true);

            for(EnumCADStat stat : (EnumCADStat[])EnumCADStat.class.getEnumConstants()) {
                var10000 = String.valueOf(Psi.magical ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.AQUA);
                s = var10000 + I18n.get(stat.getName(), new Object[0]) + String.valueOf(ChatFormatting.RESET) + ": " + cadItem.getStatValue(cad, stat);
                graphics.drawString(this.font, s, 179, 45 + i * 10, color, true);
                ++i;
            }
        }

    }
}
