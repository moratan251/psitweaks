package com.moratan251.psitweaks.client.gui.machine;

import com.moratan251.psitweaks.common.caster.CasterLog;
import com.moratan251.psitweaks.common.network.MessageAutonomousCasterAction;
import com.moratan251.psitweaks.common.network.MessageAutonomousCasterAction.Action;
import com.moratan251.psitweaks.common.tile.machine.AutonomousCasterBlockEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar;
import mekanism.client.gui.element.bar.GuiChemicalBar;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.lib.Color;
import mekanism.common.util.text.InputValidator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.psi.api.cad.EnumCADComponent;

/** 自律詠唱機の運転画面。作動ログは {@link GuiAutonomousCasterLog} の別ウィンドウで開く。 */
public class GuiAutonomousCaster extends GuiConfigurableTile<AutonomousCasterBlockEntity, MekanismTileContainer<AutonomousCasterBlockEntity>> {
    private static final String KEY = "gui.psitweaks.autonomous_caster.";
    private static final int BULLET_OUTLINE = 0xFF5FB8FF;
    private static final int ITEM_OUTLINE = 0xFFFFD040;
    private static final float LABEL_SCALE = 0.75F;

    private GuiTextField intervalField;
    private GuiTextField yawField;
    private GuiTextField pitchField;
    @Nullable
    private GuiAutonomousCasterLog logWindow;

    public GuiAutonomousCaster(MekanismTileContainer<AutonomousCasterBlockEntity> container, Inventory inv, Component title) {
        super(container, inv, title);
        imageHeight += 74;
        inventoryLabelY = imageHeight - 94;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiDynamicHorizontalRateBar(this, new GuiBar.IBarInfoHandler() {
            @Override
            public Component getTooltip() {
                return Component.translatable(KEY + "psi", tile.getPsi(), tile.getPsiCapacity(), tile.getRecoveryPerTick());
            }

            @Override
            public double getLevel() {
                int capacity = tile.getPsiCapacity();
                return capacity <= 0 ? 0.0D : Math.min(1.0D, tile.getPsi() / (double) capacity);
            }
        }, 27, 61, 122, Color.ColorFunction.scale(Color.rgb(0x2E4BFF), Color.rgb(0x8FE0FF))));
        // ゲージは左のエコー補充スロットと右の着色器スロットの間に置く。
        addRenderableWidget(new GuiChemicalBar(this, GuiChemicalBar.getProvider(tile.getEchoTank(), tile.getChemicalTanks(null)),
                27, 71, 122, 8, true));

        // 選択中の弾・参照アイテムスロット
        addRenderableWidget(button(8, 84, 10, 12, () -> Component.literal("<"),
                () -> send(Action.SELECT_BULLET, Math.floorMod(tile.getSelectedBullet() - 1, AutonomousCasterBlockEntity.BULLET_SLOTS))))
                .setTooltip(Tooltip.create(Component.translatable(KEY + "bullet.tooltip")));
        addRenderableWidget(button(68, 84, 10, 12, () -> Component.literal(">"),
                () -> send(Action.SELECT_BULLET, Math.floorMod(tile.getSelectedBullet() + 1, AutonomousCasterBlockEntity.BULLET_SLOTS))))
                .setTooltip(Tooltip.create(Component.translatable(KEY + "bullet.tooltip")));
        addRenderableWidget(button(90, 84, 10, 12, () -> Component.literal("<"),
                () -> send(Action.SELECT_ITEM_SLOT, Math.floorMod(tile.getSelectedItemSlot() - 1, AutonomousCasterBlockEntity.ITEM_SLOTS))))
                .setTooltip(Tooltip.create(Component.translatable(KEY + "item.tooltip")));
        addRenderableWidget(button(150, 84, 10, 12, () -> Component.literal(">"),
                () -> send(Action.SELECT_ITEM_SLOT, Math.floorMod(tile.getSelectedItemSlot() + 1, AutonomousCasterBlockEntity.ITEM_SLOTS))))
                .setTooltip(Tooltip.create(Component.translatable(KEY + "item.tooltip")));

        // 詠唱間隔・視線方向。現在値は上のラベルに出し、入力欄は確定（Enter / ✓）で送る。
        intervalField = addRenderableWidget(new GuiTextField(this, 8, 104, 48, 11));
        intervalField.setInputValidator(InputValidator.DIGIT).setMaxLength(5);
        intervalField.configureDigitalBorderInput(() -> submit(intervalField, Action.SET_INTERVAL));
        intervalField.setTooltip(Tooltip.create(Component.translatable(KEY + "interval.tooltip")));
        yawField = addRenderableWidget(new GuiTextField(this, 62, 104, 48, 11));
        yawField.setInputValidator(InputValidator.DIGIT_OR_NEGATIVE).setMaxLength(4);
        yawField.configureDigitalBorderInput(() -> submit(yawField, Action.SET_YAW));
        yawField.setTooltip(Tooltip.create(Component.translatable(KEY + "yaw.tooltip")));
        pitchField = addRenderableWidget(new GuiTextField(this, 116, 104, 52, 11));
        pitchField.setInputValidator(InputValidator.DIGIT_OR_NEGATIVE).setMaxLength(3);
        pitchField.configureDigitalBorderInput(() -> submit(pitchField, Action.SET_PITCH));
        pitchField.setTooltip(Tooltip.create(Component.translatable(KEY + "pitch.tooltip")));

        // トリガー・ループカウンタ・詠唱テスト
        addRenderableWidget(button(8, 118, 60, 12, this::triggerModeText,
                () -> send(Action.SET_TRIGGER_MODE, tile.getTriggerMode() == AutonomousCasterBlockEntity.TriggerMode.PULSE
                        ? AutonomousCasterBlockEntity.TriggerMode.LEVEL.ordinal()
                        : AutonomousCasterBlockEntity.TriggerMode.PULSE.ordinal())))
                .setTooltip(Tooltip.create(Component.translatable(KEY + "trigger.tooltip")));
        addRenderableWidget(button(112, 118, 14, 12, () -> Component.literal("0"),
                () -> send(Action.RESET_LOOP_COUNTER, tile.getSelectedBullet())))
                .setTooltip(Tooltip.create(Component.translatable(KEY + "loop.reset")));
        addRenderableWidget(button(130, 118, 38, 12, () -> Component.translatable(KEY + "cast_now"),
                () -> send(Action.CAST_NOW, 0)))
                .setTooltip(Tooltip.create(Component.translatable(KEY + "cast_now.tooltip")));

        addRenderableWidget(new GuiInnerScreen(this, 7, 132, 162, 12, () -> List.of(statusLine()))
                .tooltip(this::statusDetails)
                .clearFormat()
                .padding(3)
                .textScale(0.8F));

        addRenderableWidget(button(146, 4, 24, 11, () -> Component.translatable(KEY + "log"), this::openLog))
                .setTooltip(Tooltip.create(Component.translatable(KEY + "log.tooltip")));

        addRenderableWidget(new GuiAutonomousCasterSightTab(this, tile));
    }

    private MekanismButton button(int x, int y, int width, int height, Supplier<Component> text, Runnable onClick) {
        return new DynamicTextButton(this, x, y, width, height, text, onClick);
    }

    private void openLog() {
        if (logWindow != null && windows.contains(logWindow)) {
            return;
        }
        logWindow = new GuiAutonomousCasterLog(this, -30, 16, tile);
        logWindow.setTabListeners(window -> logWindow = null, window -> {
        });
        addWindow(logWindow);
    }

    private void submit(GuiTextField field, Action action) {
        String text = field.getText();
        if (text.isEmpty() || "-".equals(text)) {
            return;
        }
        try {
            send(action, Integer.parseInt(text));
            field.setText("");
        } catch (NumberFormatException ignored) {
            field.setText("");
        }
    }

    void send(Action action, int value) {
        PacketDistributor.sendToServer(new MessageAutonomousCasterAction(menu.containerId, action, value));
    }

    private Component triggerModeText() {
        return Component.translatable(KEY + "trigger." + (tile.getTriggerMode() == AutonomousCasterBlockEntity.TriggerMode.PULSE ? "pulse" : "level"));
    }

    private Component statusLine() {
        Component signal = Component.translatable(KEY + (tile.hasSignal() ? "signal_on" : "signal_off"));
        return switch (tile.getLastOutcome()) {
            case SUCCESS -> Component.translatable(KEY + "status.success", signal);
            case FAILURE -> Component.translatable(KEY + "status.failure", signal, failureReason());
            case NONE -> Component.translatable(KEY + "status.idle", signal);
        };
    }

    private List<Component> statusDetails() {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(KEY + "detail.signal",
                Component.translatable(KEY + (tile.hasSignal() ? "signal_on" : "signal_off"))));
        if (tile.getCooldown() > 0) {
            lines.add(Component.translatable(KEY + "detail.cooldown", tile.getCooldown()));
        }
        lines.add(Component.translatable(KEY + "detail.outcome", Component.translatable(KEY + "outcome." + tile.getLastOutcome().name().toLowerCase(java.util.Locale.ROOT))));
        if (tile.getLastOutcome() == AutonomousCasterBlockEntity.Outcome.FAILURE) {
            lines.add(failureReason());
        }
        lines.add(Component.translatable(KEY + "detail.loop", tile.getSelectedBullet() + 1, tile.getLoopCounter(tile.getSelectedBullet())));
        lines.add(Component.translatable(KEY + "detail.recovery", tile.getRecoveryPerTick()));
        return lines;
    }

    /** 最後の失敗理由。ログから消えていれば汎用の文言にする。 */
    private Component failureReason() {
        long sequence = tile.getLastFailureSequence();
        for (CasterLog.Entry entry : tile.getClientLog().entries()) {
            if (entry.sequence() == sequence) {
                return entry.message();
            }
        }
        return Component.translatable(KEY + "status.failure_unknown").withStyle(ChatFormatting.RED);
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleTextWithOffset(guiGraphics, 0, 144);
        renderInventoryText(guiGraphics);

        drawCentered(guiGraphics, Component.translatable(KEY + "bullet", tile.getSelectedBullet() + 1), 43, 86);
        drawCentered(guiGraphics, Component.translatable(KEY + "item", tile.getSelectedItemSlot() + 1), 125, 86);
        drawLabel(guiGraphics, Component.translatable(KEY + "interval", tile.getCastInterval()), 8, 97);
        drawLabel(guiGraphics, Component.translatable(KEY + "yaw", tile.getYawOffset()), 62, 97);
        drawLabel(guiGraphics, Component.translatable(KEY + "pitch", tile.getPitch()), 116, 97);
        drawLabel(guiGraphics, Component.translatable(KEY + "loop", tile.getLoopCounter(tile.getSelectedBullet())), 72, 121);

        int bulletX = AutonomousCasterBlockEntity.BULLET_X + tile.getSelectedBullet() * 18 - 1;
        guiGraphics.renderOutline(bulletX, AutonomousCasterBlockEntity.SLOT_ROW_Y - 1, 18, 18, BULLET_OUTLINE);
        int itemX = AutonomousCasterBlockEntity.ITEM_X + tile.getSelectedItemSlot() * 18 - 1;
        guiGraphics.renderOutline(itemX, AutonomousCasterBlockEntity.ITEM_Y - 1, 18, 18, ITEM_OUTLINE);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        if (menu.getCarried().isEmpty() && windows.isEmpty() && tile.getDyeSlot().isEmpty()) {
            int x = leftPos + AutonomousCasterBlockEntity.DYE_SLOT_X;
            int y = topPos + AutonomousCasterBlockEntity.DYE_SLOT_Y;
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                // 空の着色器スロットに、何を入れる枠かを示す。
                setTooltipForNextRenderPass(List.of(
                        Component.translatable(KEY + "component", Component.translatable(EnumCADComponent.DYE.getName())).getVisualOrderText(),
                        Component.translatable(KEY + "component.default.dye").withStyle(ChatFormatting.GRAY).getVisualOrderText()));
            }
        }
    }

    private void drawCentered(GuiGraphics guiGraphics, Component text, int centerX, int y) {
        guiGraphics.drawString(font, text, centerX - font.width(text) / 2, y, titleTextColor(), false);
    }

    private void drawLabel(GuiGraphics guiGraphics, Component text, int x, int y) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().scale(LABEL_SCALE, LABEL_SCALE, 1.0F);
        guiGraphics.drawString(font, text, 0, 0, titleTextColor(), false);
        guiGraphics.pose().popPose();
    }

    @Override
    public void removed() {
        boolean closing = !switchingToRecipeViewer;
        super.removed();
        if (closing) {
            tile.releaseClientLog();
        }
    }

    /** 表示文字を毎フレーム取り直すボタン。 */
    private static final class DynamicTextButton extends MekanismButton {
        private final Supplier<Component> text;

        private DynamicTextButton(IGuiWrapper gui, int x, int y, int width, int height, Supplier<Component> text, Runnable onClick) {
            super(gui, x, y, width, height, text.get(), (element, mouseX, mouseY) -> {
                onClick.run();
                return true;
            });
            this.text = text;
        }

        @NotNull
        @Override
        public Component getMessage() {
            return text.get();
        }
    }
}
