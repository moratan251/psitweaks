package com.moratan251.psitweaks.client.gui.machine;

import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.interfaces.IHasVisualization;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

/**
 * 自律詠唱機の視線をワールドに表示するかを切り替える左のタブ。デジタルマイナーの範囲の可視化タブと同じ見た目で、
 * 側面設定・輸送設定のタブの下に置く。
 */
public class GuiAutonomousCasterSightTab extends GuiInsetElement<IHasVisualization> {
    private static final String KEY = "gui.psitweaks.autonomous_caster.sight";
    private static final Tooltip SIGHT_ON = Tooltip.create(Component.translatable(KEY, OnOff.ON.getTextComponent()));
    private static final Tooltip SIGHT_OFF = Tooltip.create(Component.translatable(KEY, OnOff.OFF.getTextComponent()));

    public GuiAutonomousCasterSightTab(IGuiWrapper gui, IHasVisualization caster) {
        super(MekanismUtils.getResource(ResourceType.GUI, "visuals.png"), gui, caster, -26, 62, 26, 18, true);
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        setTooltip(dataSource.isClientRendering() ? SIGHT_ON : SIGHT_OFF);
    }

    @Override
    protected void colorTab(GuiGraphics guiGraphics) {
        MekanismRenderer.color(guiGraphics, SpecialColors.TAB_VISUALS);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        dataSource.toggleClientRendering();
    }
}
