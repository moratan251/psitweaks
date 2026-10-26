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
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.lib.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.psi.api.cad.EnumCADComponent;

/**
 * 自律詠唱機の運転画面。プレイヤーのインベントリと同じ並びのアイテムスロットを見せ、操作は
 * {@link GuiAutonomousCasterSettings}、作動ログは {@link GuiAutonomousCasterLog} の別ウィンドウで開く。
 */
public class GuiAutonomousCaster extends GuiConfigurableTile<AutonomousCasterBlockEntity, MekanismTileContainer<AutonomousCasterBlockEntity>> {
    private static final String KEY = "gui.psitweaks.autonomous_caster.";
    private static final int BULLET_OUTLINE = 0xFF5FB8FF;
    private static final int ITEM_OUTLINE = 0xFFFFD040;
    private static final int SETTINGS_BUTTON_X = 110;

    @Nullable
    private GuiAutonomousCasterSettings settingsWindow;
    @Nullable
    private GuiAutonomousCasterLog logWindow;

    public GuiAutonomousCaster(MekanismTileContainer<AutonomousCasterBlockEntity> container, Inventory inv, Component title) {
        super(container, inv, title);
        imageHeight += 84;
        inventoryLabelY = imageHeight - 94;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        // ゲージは術式弾スロットと、右端のエコー補充スロット・着色器スロットの間に縦に置く。
        addRenderableWidget(new GuiDynamicVerticalRateBar(this, new GuiBar.IBarInfoHandler() {
            @Override
            public Component getTooltip() {
                return Component.translatable(KEY + "psi", tile.getPsi(), tile.getPsiCapacity(), tile.getRecoveryPerTick());
            }

            @Override
            public double getLevel() {
                int capacity = tile.getPsiCapacity();
                return capacity <= 0 ? 0.0D : Math.min(1.0D, tile.getPsi() / (double) capacity);
            }
        }, 119, 17, 10, 34, Color.ColorFunction.scale(Color.rgb(0x2E4BFF), Color.rgb(0x8FE0FF))));
        addRenderableWidget(new GuiChemicalBar(this, GuiChemicalBar.getProvider(tile.getEchoTank(), tile.getChemicalTanks(null)),
                135, 17, 10, 34, false));

        addRenderableWidget(new GuiInnerScreen(this, 7, 138, 162, 12, () -> List.of(statusLine()))
                .tooltip(this::statusDetails)
                .clearFormat()
                .padding(3)
                .textScale(0.8F));

        addRenderableWidget(button(SETTINGS_BUTTON_X, 4, 34, 11, () -> Component.translatable(KEY + "settings"), this::openSettings))
                .setTooltip(Tooltip.create(Component.translatable(KEY + "settings.tooltip")));
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

    /** 設定ウィンドウはプレイヤーのインベントリの上に開き、機械のスロットと状態表示は見えたままにする。 */
    private void openSettings() {
        if (settingsWindow != null && windows.contains(settingsWindow)) {
            return;
        }
        settingsWindow = new GuiAutonomousCasterSettings(this, (imageWidth - GuiAutonomousCasterSettings.WIDTH) / 2,
                inventoryLabelY, tile);
        settingsWindow.setTabListeners(window -> settingsWindow = null, window -> {
        });
        addWindow(settingsWindow);
    }

    void send(Action action, int value) {
        PacketDistributor.sendToServer(new MessageAutonomousCasterAction(menu.containerId, action, value));
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
        lines.add(Component.translatable(KEY + "detail.loop", tile.getLoopCounter()));
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
        renderTitleTextWithOffset(guiGraphics, 0, SETTINGS_BUTTON_X - 2);
        renderInventoryText(guiGraphics);

        int bullet = tile.getSelectedBullet();
        guiGraphics.renderOutline(AutonomousCasterBlockEntity.bulletSlotX(bullet) - 1, AutonomousCasterBlockEntity.bulletSlotY(bullet) - 1,
                18, 18, BULLET_OUTLINE);
        int itemX = AutonomousCasterBlockEntity.ITEM_X + tile.getSelectedItemSlot() * 18 - 1;
        guiGraphics.renderOutline(itemX, AutonomousCasterBlockEntity.HOTBAR_Y - 1, 18, 18, ITEM_OUTLINE);
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

    @Override
    public void removed() {
        boolean closing = !switchingToRecipeViewer;
        super.removed();
        if (closing) {
            tile.releaseClientLog();
        }
    }

    /** 表示文字を毎フレーム取り直すボタン。 */
    static final class DynamicTextButton extends MekanismButton {
        private final Supplier<Component> text;

        DynamicTextButton(IGuiWrapper gui, int x, int y, int width, int height, Supplier<Component> text, Runnable onClick) {
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
