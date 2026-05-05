package com.moratan251.psitweaks.client.gui.machine;

import com.moratan251.psitweaks.common.network.MessagePsiLinkGeneratorSettingsSync;
import com.moratan251.psitweaks.common.tile.machine.PsionicGeneratorBlockEntity;
import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class GuiPsionicGenerator extends GuiMekanismTile<PsionicGeneratorBlockEntity, MekanismTileContainer<PsionicGeneratorBlockEntity>> {
    public GuiPsionicGenerator(MekanismTileContainer<PsionicGeneratorBlockEntity> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiEnergyTab(this, () -> List.of(
                EnergyDisplay.of(tile.getEnergyContainer()).getTextComponent(),
                Component.translatable("gui.psitweaks.psionic_generator.generate",
                        EnergyDisplay.of(tile.getCurrentGenerationRateJoules()).getTextComponent(),
                        EnergyDisplay.of(tile.getConfiguredGenerationRateJoules()).getTextComponent()),
                Component.translatable("gui.psitweaks.psionic_generator.consume",
                        tile.getConsumePsiPerTick())
        )));
        addRenderableWidget(new GuiInnerScreen(this, 45, 18, 92, 50, this::getSummaryLines)
                .tooltip(this::getDetailLines)
                .padding(4)
                .spacing(1)
                .textScale(0.7F));
        addRenderableWidget(new GuiSlot(SlotType.POWER, this, 142, 34).with(SlotOverlay.POWER));
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15));

        addRenderableWidget(new MekanismButton(this, 45, 66, 40, 12,
                Component.translatable("gui.psitweaks.psionic_generator.toggle"),
                (element, mouseX, mouseY) -> {
                    sendSettings(tile.getConsumePsiPerTick(), !tile.isLinkActive());
                    return true;
                }));
        addRenderableWidget(new MekanismButton(this, 89, 66, 18, 12,
                Component.literal("--"),
                (element, mouseX, mouseY) -> {
                    sendSettings(tile.getConsumePsiPerTick() - 10, tile.isLinkActive());
                    return true;
                }));
        addRenderableWidget(new MekanismButton(this, 109, 66, 14, 12,
                Component.literal("-"),
                (element, mouseX, mouseY) -> {
                    sendSettings(tile.getConsumePsiPerTick() - 1, tile.isLinkActive());
                    return true;
                }));
        addRenderableWidget(new MekanismButton(this, 125, 66, 14, 12,
                Component.literal("+"),
                (element, mouseX, mouseY) -> {
                    sendSettings(tile.getConsumePsiPerTick() + 1, tile.isLinkActive());
                    return true;
                }));
        addRenderableWidget(new MekanismButton(this, 141, 66, 18, 12,
                Component.literal("++"),
                (element, mouseX, mouseY) -> {
                    sendSettings(tile.getConsumePsiPerTick() + 10, tile.isLinkActive());
                    return true;
                }));
    }

    @Override
    protected void drawForegroundText(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    private List<Component> getSummaryLines() {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(trimToInnerWidth(tile.getOwnerDisplayName())));
        lines.add(Component.translatable("gui.psitweaks.psionic_generator.summary_status",
                Component.translatable(tile.isOwnerOnline()
                        ? "gui.psitweaks.psionic_generator.owner_online"
                        : "gui.psitweaks.psionic_generator.owner_offline"),
                Component.translatable(tile.isLinkActive()
                        ? "gui.psitweaks.psionic_generator.link_on"
                        : "gui.psitweaks.psionic_generator.link_off")));
        lines.add(Component.translatable("gui.psitweaks.psionic_generator.summary_psi",
                tile.getOwnerAvailablePsi(), tile.getOwnerTotalPsi()));
        lines.add(Component.translatable("gui.psitweaks.psionic_generator.consume",
                tile.getConsumePsiPerTick()));
        return lines;
    }

    private List<Component> getDetailLines() {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("gui.psitweaks.psionic_generator.owner", tile.getOwnerDisplayName()));
        lines.add(Component.translatable("gui.psitweaks.psionic_generator.summary_status",
                Component.translatable(tile.isOwnerOnline()
                        ? "gui.psitweaks.psionic_generator.owner_online"
                        : "gui.psitweaks.psionic_generator.owner_offline"),
                Component.translatable(tile.isLinkActive()
                        ? "gui.psitweaks.psionic_generator.link_on"
                        : "gui.psitweaks.psionic_generator.link_off")));
        lines.add(Component.translatable("gui.psitweaks.psionic_generator.psi",
                tile.getOwnerAvailablePsi(), tile.getOwnerTotalPsi()));
        lines.add(Component.translatable("gui.psitweaks.psionic_generator.consume",
                tile.getConsumePsiPerTick()));
        lines.add(Component.translatable("gui.psitweaks.psionic_generator.generate",
                EnergyDisplay.of(tile.getCurrentGenerationRateJoules()).getTextComponent(),
                EnergyDisplay.of(tile.getConfiguredGenerationRateJoules()).getTextComponent()));
        return lines;
    }

    private String trimToInnerWidth(String text) {
        return font.plainSubstrByWidth(text, 84);
    }

    private void sendSettings(int consumePsiPerTick, boolean linkActive) {
        PacketDistributor.sendToServer(new MessagePsiLinkGeneratorSettingsSync(tile.getBlockPos(), consumePsiPerTick, linkActive));
    }
}
