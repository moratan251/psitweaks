package com.moratan251.psitweaks.client.gui.machine;

import com.moratan251.psitweaks.client.jei.PsitweaksMekanismJeiRecipeTypes;
import com.moratan251.psitweaks.common.tile.machine.TileEntitySculkEroder;
import mekanism.client.gui.machine.GuiElectricMachine;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiSculkEroder extends GuiElectricMachine<TileEntitySculkEroder, MekanismTileContainer<TileEntitySculkEroder>> {

    public GuiSculkEroder(MekanismTileContainer<TileEntitySculkEroder> container, Inventory inv, Component title) {
        super(container, inv, title);
        PsitweaksMekanismJeiRecipeTypes.ensureLoaded();
    }
}
