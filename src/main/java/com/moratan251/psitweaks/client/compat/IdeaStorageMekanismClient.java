package com.moratan251.psitweaks.client.compat;

import com.moratan251.psitweaks.common.compat.IdeaStorageMekanismIntegration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

/** Only invoked behind the Mekanism-loaded boundary. */
final class IdeaStorageMekanismClient {
    private IdeaStorageMekanismClient() {}

    static Component ideaStorageChemicalName(ResourceLocation id) {
        var chemical = IdeaStorageMekanismIntegration.chemical(id);
        return chemical == null ? Component.literal(id.toString()) : chemical.getTextComponent();
    }

    static boolean renderIdeaStorageChemical(GuiGraphics graphics, ResourceLocation id, int x, int y) {
        var chemical = IdeaStorageMekanismIntegration.chemical(id);
        if (chemical == null) return false;
        var sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(chemical.getIcon());
        int color = chemical.getTint();
        graphics.setColor(((color >> 16) & 255) / 255F, ((color >> 8) & 255) / 255F, (color & 255) / 255F, 1F);
        graphics.blit(x, y, 300, 16, 16, sprite);
        graphics.setColor(1F, 1F, 1F, 1F);
        return true;
    }
}
