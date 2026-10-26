package com.moratan251.psitweaks.client.gui.machine;

import com.moratan251.psitweaks.common.network.MessageAutonomousCasterAction.Action;
import com.moratan251.psitweaks.common.tile.machine.AutonomousCasterBlockEntity;
import java.util.function.Supplier;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.util.text.InputValidator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

/**
 * 自律詠唱機の設定窓。選択中の術式弾・手に持つスロット、詠唱間隔、視線の向き、トリガー、ループ回数、テスト詠唱をまとめる。
 * 運転画面はアイテムスロットに場所を譲り、設定はここで変える。
 */
public class GuiAutonomousCasterSettings extends GuiWindow {
    private static final String KEY = "gui.psitweaks.autonomous_caster.";
    static final int WIDTH = 164;
    private static final int HEIGHT = 74;
    private static final float LABEL_SCALE = 0.75F;

    private final GuiAutonomousCaster parent;
    private final AutonomousCasterBlockEntity tile;
    private final GuiTextField intervalField;
    private final GuiTextField yawField;
    private final GuiTextField pitchField;

    public GuiAutonomousCasterSettings(GuiAutonomousCaster gui, int x, int y, AutonomousCasterBlockEntity tile) {
        super(gui, x, y, WIDTH, HEIGHT, SelectedWindowData.WindowType.UNSPECIFIED);
        this.parent = gui;
        this.tile = tile;
        // 設定を変えながら機械のスロットや運転画面のボタンも触れるようにする
        interactionStrategy = InteractionStrategy.ALL;

        // 選択中の弾・手に持つスロット
        button(6, 18, 10, 12, () -> Component.literal("<"),
                () -> parent.send(Action.SELECT_BULLET, Math.floorMod(tile.getSelectedBullet() - 1, AutonomousCasterBlockEntity.BULLET_SLOTS)))
                .setTooltip(Tooltip.create(Component.translatable(KEY + "bullet.tooltip")));
        button(66, 18, 10, 12, () -> Component.literal(">"),
                () -> parent.send(Action.SELECT_BULLET, Math.floorMod(tile.getSelectedBullet() + 1, AutonomousCasterBlockEntity.BULLET_SLOTS)))
                .setTooltip(Tooltip.create(Component.translatable(KEY + "bullet.tooltip")));
        button(88, 18, 10, 12, () -> Component.literal("<"),
                () -> parent.send(Action.SELECT_ITEM_SLOT, Math.floorMod(tile.getSelectedItemSlot() - 1, AutonomousCasterBlockEntity.HOTBAR_SLOTS)))
                .setTooltip(Tooltip.create(Component.translatable(KEY + "item.tooltip")));
        button(148, 18, 10, 12, () -> Component.literal(">"),
                () -> parent.send(Action.SELECT_ITEM_SLOT, Math.floorMod(tile.getSelectedItemSlot() + 1, AutonomousCasterBlockEntity.HOTBAR_SLOTS)))
                .setTooltip(Tooltip.create(Component.translatable(KEY + "item.tooltip")));

        // 詠唱間隔・視線方向。現在値は上のラベルに出し、入力欄は確定（Enter / ✓）で送る。
        intervalField = addChild(new GuiTextField(gui, this, relativeX + 6, relativeY + 40, 48, 11));
        intervalField.setInputValidator(InputValidator.DIGIT).setMaxLength(5);
        intervalField.configureDigitalBorderInput(() -> submit(intervalField, Action.SET_INTERVAL));
        intervalField.setTooltip(Tooltip.create(Component.translatable(KEY + "interval.tooltip")));
        yawField = addChild(new GuiTextField(gui, this, relativeX + 60, relativeY + 40, 48, 11));
        yawField.setInputValidator(InputValidator.DIGIT_OR_NEGATIVE).setMaxLength(4);
        yawField.configureDigitalBorderInput(() -> submit(yawField, Action.SET_YAW));
        yawField.setTooltip(Tooltip.create(Component.translatable(KEY + "yaw.tooltip")));
        pitchField = addChild(new GuiTextField(gui, this, relativeX + 114, relativeY + 40, 44, 11));
        pitchField.setInputValidator(InputValidator.DIGIT_OR_NEGATIVE).setMaxLength(3);
        pitchField.configureDigitalBorderInput(() -> submit(pitchField, Action.SET_PITCH));
        pitchField.setTooltip(Tooltip.create(Component.translatable(KEY + "pitch.tooltip")));

        // トリガー・ループカウンタ・詠唱テスト
        button(6, 56, 60, 12, this::triggerModeText,
                () -> parent.send(Action.SET_TRIGGER_MODE, tile.getTriggerMode() == AutonomousCasterBlockEntity.TriggerMode.PULSE
                        ? AutonomousCasterBlockEntity.TriggerMode.LEVEL.ordinal()
                        : AutonomousCasterBlockEntity.TriggerMode.PULSE.ordinal()))
                .setTooltip(Tooltip.create(Component.translatable(KEY + "trigger.tooltip")));
        button(108, 56, 14, 12, () -> Component.literal("0"),
                () -> parent.send(Action.RESET_LOOP_COUNTER, 0))
                .setTooltip(Tooltip.create(Component.translatable(KEY + "loop.reset")));
        button(126, 56, 32, 12, () -> Component.translatable(KEY + "cast_now"),
                () -> parent.send(Action.CAST_NOW, 0))
                .setTooltip(Tooltip.create(Component.translatable(KEY + "cast_now.tooltip")));
    }

    private MekanismButton button(int x, int y, int width, int height, Supplier<Component> text, Runnable onClick) {
        return addChild(new GuiAutonomousCaster.DynamicTextButton(gui(), relativeX + x, relativeY + y, width, height, text, onClick));
    }

    private void submit(GuiTextField field, Action action) {
        String text = field.getText();
        if (text.isEmpty() || "-".equals(text)) {
            return;
        }
        try {
            parent.send(action, Integer.parseInt(text));
            field.setText("");
        } catch (NumberFormatException ignored) {
            field.setText("");
        }
    }

    private Component triggerModeText() {
        return Component.translatable(KEY + "trigger." + (tile.getTriggerMode() == AutonomousCasterBlockEntity.TriggerMode.PULSE ? "pulse" : "level"));
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        drawTitleText(guiGraphics, Component.translatable(KEY + "settings_window.title"), 5);
        drawCentered(guiGraphics, Component.translatable(KEY + "bullet", tile.getSelectedBullet() + 1), 41, 20);
        drawCentered(guiGraphics, Component.translatable(KEY + "item", tile.getSelectedItemSlot() + 1), 123, 20);
        drawLabel(guiGraphics, Component.translatable(KEY + "interval", tile.getCastInterval()), 6, 33);
        drawLabel(guiGraphics, Component.translatable(KEY + "yaw", tile.getYawOffset()), 60, 33);
        drawLabel(guiGraphics, Component.translatable(KEY + "pitch", tile.getPitch()), 114, 33);
        drawLabel(guiGraphics, Component.translatable(KEY + "loop", tile.getLoopCounter()), 70, 59);
    }

    private void drawCentered(GuiGraphics guiGraphics, Component text, int centerX, int y) {
        guiGraphics.drawString(font(), text, relativeX + centerX - font().width(text) / 2, relativeY + y, titleTextColor(), false);
    }

    private void drawLabel(GuiGraphics guiGraphics, Component text, int x, int y) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(relativeX + x, relativeY + y, 0);
        guiGraphics.pose().scale(LABEL_SCALE, LABEL_SCALE, 1.0F);
        guiGraphics.drawString(font(), text, 0, 0, titleTextColor(), false);
        guiGraphics.pose().popPose();
    }
}
