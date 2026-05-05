package com.moratan251.psitweaks.client.gui.machine;

import com.moratan251.psitweaks.common.tile.machine.SculkEroderBlockEntity;
import mekanism.client.gui.machine.GuiElectricMachine;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiSculkEroder extends GuiElectricMachine<SculkEroderBlockEntity, MekanismTileContainer<SculkEroderBlockEntity>> {
    public GuiSculkEroder(MekanismTileContainer<SculkEroderBlockEntity> container, Inventory inv, Component title) {
        super(container, inv, title);
    }
}
