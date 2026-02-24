package com.moratan251.psitweaks.client.gui.machine;

import com.moratan251.psitweaks.common.tile.machine.TileEntityMaterialMutator;
import mekanism.client.gui.machine.GuiAdvancedElectricMachine;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiMaterialMutator extends GuiAdvancedElectricMachine<TileEntityMaterialMutator, MekanismTileContainer<TileEntityMaterialMutator>> {

    public GuiMaterialMutator(MekanismTileContainer<TileEntityMaterialMutator> container, Inventory inv, Component title) {
        super(container, inv, title);
    }
}
