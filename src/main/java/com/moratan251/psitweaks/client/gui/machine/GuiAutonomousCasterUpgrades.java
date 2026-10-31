package com.moratan251.psitweaks.client.gui.machine;

import com.moratan251.psitweaks.common.caster.AutonomousCasterUpgrade;
import com.moratan251.psitweaks.common.tile.machine.AutonomousCasterBlockEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.slot.GuiVirtualSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.Slot;

/**
 * 自律詠唱機のアップグレード窓。アップグレードスロット3つと、アップグレードで上がる9つの性能の今の値を並べる。
 * アップグレードで上がった分は値の後ろに書き足す。
 */
public class GuiAutonomousCasterUpgrades extends GuiWindow {
    static final String KEY = "gui.psitweaks.autonomous_caster.upgrades.";
    static final int WIDTH = 160;
    private static final int SLOT_Y = 18;
    private static final int SLOT_GAP = 4;
    private static final int SCREEN_Y = 40;
    private static final int SCREEN_PADDING = 3;
    private static final int HEIGHT = SCREEN_Y + 2 * SCREEN_PADDING + 9 * AutonomousCasterUpgrade.values().length + 6;

    private final AutonomousCasterBlockEntity tile;

    public GuiAutonomousCasterUpgrades(IGuiWrapper gui, int x, int y, AutonomousCasterBlockEntity tile, SelectedWindowData windowData) {
        super(gui, x, y, WIDTH, HEIGHT, windowData);
        this.tile = tile;
        // Mekanism のアップグレード窓と同じく、開いたまま機械のスロットやインベントリも触れるようにする
        interactionStrategy = InteractionStrategy.ALL;

        List<VirtualInventoryContainerSlot> slots = upgradeContainerSlots(gui, tile);
        int slotsWidth = slots.size() * 18 + (slots.size() - 1) * SLOT_GAP;
        int slotX = (WIDTH - slotsWidth) / 2;
        for (VirtualInventoryContainerSlot slot : slots) {
            addChild(new GuiVirtualSlot(this, SlotType.NORMAL, gui, relativeX + slotX, relativeY + SLOT_Y, slot));
            slotX += 18 + SLOT_GAP;
        }
        addChild(new GuiInnerScreen(gui, relativeX + 6, relativeY + SCREEN_Y, WIDTH - 12, HEIGHT - SCREEN_Y - 6, this::statLines)
                .clearFormat()
                .padding(SCREEN_PADDING)
                .spacing(0)
                .textScale(0.8F));
    }

    /** 機械のアップグレードスロットに対応する、画面の仮想スロット（機械のスロットの順）。 */
    private static List<VirtualInventoryContainerSlot> upgradeContainerSlots(IGuiWrapper gui, AutonomousCasterBlockEntity tile) {
        List<VirtualInventoryContainerSlot> found = new ArrayList<>();
        if (gui instanceof GuiMekanism<?> screen) {
            for (Slot slot : screen.getMenu().slots) {
                if (slot instanceof VirtualInventoryContainerSlot virtual && tile.getCasterUpgradeSlots().contains(virtual.getInventorySlot())) {
                    found.add(virtual);
                }
            }
        }
        return found;
    }

    private List<Component> statLines() {
        List<Component> lines = new ArrayList<>(AutonomousCasterUpgrade.values().length);
        for (AutonomousCasterUpgrade upgrade : AutonomousCasterUpgrade.values()) {
            lines.add(statLine(upgrade));
        }
        return lines;
    }

    private Component statLine(AutonomousCasterUpgrade upgrade) {
        int value = tile.getStatValue(upgrade);
        int bonus = tile.getStatBonus(upgrade);
        // 変換効率は Psi 1 あたりのエコー（mB）の100倍で届く
        boolean hundredths = upgrade == AutonomousCasterUpgrade.CONVERSION_EFFICIENCY;
        MutableComponent line = Component.translatable(KEY + "stat." + upgrade.id(), hundredths ? hundredths(value) : Integer.toString(value));
        if (bonus != 0) {
            String amount = hundredths ? hundredths(bonus) : Integer.toString(bonus);
            boolean decrease = upgrade == AutonomousCasterUpgrade.CONVERSION_EFFICIENCY;
            line.append(" ").append(Component.translatable(KEY + (decrease ? "decrease" : "increase"), amount)
                    .withStyle(ChatFormatting.AQUA));
        }
        return line;
    }

    private static String hundredths(int value) {
        String text = String.format(Locale.ROOT, "%.2f", value / 100.0D);
        return text.replaceAll("\\.?0+$", "");
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        drawTitleText(guiGraphics, Component.translatable(KEY + "title"), 5);
    }
}
