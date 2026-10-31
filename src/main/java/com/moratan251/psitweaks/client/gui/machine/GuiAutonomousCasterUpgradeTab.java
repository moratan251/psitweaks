package com.moratan251.psitweaks.client.gui.machine;

import com.moratan251.psitweaks.common.tile.machine.AutonomousCasterBlockEntity;
import java.util.function.Supplier;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.tab.window.GuiWindowCreatorTab;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

/**
 * 自律詠唱機のアップグレード窓を開く右のタブ。Mekanism の機械のアップグレードタブと同じ場所・見た目にする。
 * 窓の種類は Mekanism のアップグレード窓のものを借りるので、窓の位置とピン留めも Mekanism のアップグレード窓と共有する。
 */
public class GuiAutonomousCasterUpgradeTab extends GuiWindowCreatorTab<AutonomousCasterBlockEntity, GuiAutonomousCasterUpgradeTab> {
    private static final SelectedWindowData WINDOW_DATA = new SelectedWindowData(WindowType.UPGRADE);

    public GuiAutonomousCasterUpgradeTab(IGuiWrapper gui, AutonomousCasterBlockEntity tile, Supplier<GuiAutonomousCasterUpgradeTab> elementSupplier) {
        super(MekanismUtils.getResource(ResourceType.GUI, "upgrade.png"), gui, tile, gui.getXSize(), 6, 26, 18, false, elementSupplier);
        setTooltip(Tooltip.create(Component.translatable(GuiAutonomousCasterUpgrades.KEY + "tab.tooltip")));
    }

    @Override
    protected void colorTab(GuiGraphics guiGraphics) {
        MekanismRenderer.color(guiGraphics, SpecialColors.TAB_UPGRADE);
    }

    @Override
    protected GuiWindow createWindow(SelectedWindowData windowData) {
        return new GuiAutonomousCasterUpgrades(gui(), (getGuiWidth() - GuiAutonomousCasterUpgrades.WIDTH) / 2, 15, dataSource, windowData);
    }

    @Override
    protected SelectedWindowData getNextWindowData() {
        return WINDOW_DATA;
    }
}
