package com.moratan251.psitweaks.client.gui.machine;

import com.moratan251.psitweaks.common.caster.CasterLog;
import com.moratan251.psitweaks.common.network.MessageAutonomousCasterAction.Action;
import com.moratan251.psitweaks.common.tile.machine.AutonomousCasterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.scroll.GuiScrollBar;
import mekanism.client.gui.element.window.GuiConfirmationDialog;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.common.inventory.container.SelectedWindowData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

/**
 * 自律詠唱機の作動ログ窓。
 * 古いものを上・新しいものを下に並べ、Component のスタイルを保ったまま表示幅で折り返す。
 * 最下部を表示している間だけ新着に追従し、過去ログを読んでいる間は表示位置を動かさない。
 */
public class GuiAutonomousCasterLog extends GuiWindow {
    private static final String KEY = "gui.psitweaks.autonomous_caster.log_window.";
    private static final float TEXT_SCALE = 0.8F;
    private static final int LINE_HEIGHT = 8;
    private static final int VISIBLE_LINES = 15;
    private static final int PADDING = 3;
    private static final int VIEW_X = 6;
    private static final int VIEW_Y = 18;
    private static final int VIEW_WIDTH = 208;
    private static final int VIEW_HEIGHT = VISIBLE_LINES * LINE_HEIGHT + PADDING * 2;
    private static final int FOOTER_Y = VIEW_Y + VIEW_HEIGHT + 4;
    private static final int BUTTON_HEIGHT = 12;
    private static final int WIDTH = VIEW_X + VIEW_WIDTH + 2 + 14 + 6;
    private static final int HEIGHT = FOOTER_Y + BUTTON_HEIGHT + 6;
    /** 文字列を折り返す幅（縮小前のフォント単位）。 */
    private static final int WRAP_WIDTH = (int) ((VIEW_WIDTH - PADDING * 2) / TEXT_SCALE);
    private static final int DEFAULT_TEXT_COLOR = 0xFFFFFF;
    private static final int MUTED_TEXT_COLOR = 0xA0A0A0;

    private final GuiAutonomousCaster parent;
    private final AutonomousCasterBlockEntity tile;
    private final LogScrollBar scrollBar;
    private List<FormattedCharSequence> lines = List.of();
    /** 各行がどのエントリ（sequence）に属するか。再配置時に読んでいた位置を保つために使う。 */
    private long[] lineOwners = new long[0];
    private long layoutRevision = Long.MIN_VALUE;

    public GuiAutonomousCasterLog(GuiAutonomousCaster gui, int x, int y, AutonomousCasterBlockEntity tile) {
        super(gui, x, y, WIDTH, HEIGHT, SelectedWindowData.WindowType.UNSPECIFIED);
        this.parent = gui;
        this.tile = tile;
        // ログを見ながら運転画面の「詠唱テスト」なども押せるようにする
        interactionStrategy = InteractionStrategy.ALL;
        addChild(new LogView(gui, relativeX + VIEW_X, relativeY + VIEW_Y));
        scrollBar = addChild(new LogScrollBar(gui, relativeX + VIEW_X + VIEW_WIDTH + 2, relativeY + VIEW_Y, VIEW_HEIGHT,
                () -> lines.size(), () -> VISIBLE_LINES));

        int clearX = relativeX + WIDTH - 6 - 40;
        addChild(new MekanismButton(gui, clearX, relativeY + FOOTER_Y, 40, BUTTON_HEIGHT,
                Component.translatable(KEY + "clear"), (element, mouseX, mouseY) -> {
            GuiConfirmationDialog.show(gui, Component.translatable(KEY + "clear.confirm"),
                    () -> parent.send(Action.CLEAR_LOG, 0), GuiConfirmationDialog.DialogType.DANGER);
            return true;
        })).setTooltip(Tooltip.create(Component.translatable(KEY + "clear.tooltip")));
        addChild(new MekanismButton(gui, clearX - 2 - 44, relativeY + FOOTER_Y, 44, BUTTON_HEIGHT,
                Component.translatable(KEY + "latest"), (element, mouseX, mouseY) -> {
            scrollBar.scrollToBottom();
            return true;
        })).setTooltip(Tooltip.create(Component.translatable(KEY + "latest.tooltip")));
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        relayoutIfChanged();
        super.renderForeground(guiGraphics, mouseX, mouseY);
        drawTitleText(guiGraphics, Component.translatable(KEY + "title"), 5);
        Component count = Component.translatable(KEY + "count", tile.getClientLog().size());
        guiGraphics.drawString(font(), count, relativeX + VIEW_X, relativeY + FOOTER_Y + 2, titleTextColor(), false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double xDelta, double yDelta) {
        if (isMouseOver(mouseX, mouseY)) {
            scrollBar.adjustScroll(yDelta);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, xDelta, yDelta);
    }

    /** ログの内容が変わっていれば折り返しをやり直す。読んでいた位置、または最下部への追従を保つ。 */
    private void relayoutIfChanged() {
        CasterLog log = tile.getClientLog();
        if (log.revision() == layoutRevision) {
            return;
        }
        boolean firstLayout = layoutRevision == Long.MIN_VALUE;
        boolean followTail = firstLayout || scrollBar.isAtBottom();
        int firstLine = scrollBar.getCurrentSelection();
        long anchor = firstLine < lineOwners.length ? lineOwners[firstLine] : Long.MAX_VALUE;
        int anchorOffset = 0;
        if (anchor != Long.MAX_VALUE) {
            int start = firstLine;
            while (start > 0 && lineOwners[start - 1] == anchor) {
                start--;
            }
            anchorOffset = firstLine - start;
        }

        layout(log);
        layoutRevision = log.revision();

        if (followTail) {
            scrollBar.scrollToBottom();
            return;
        }
        int newFirst = -1;
        for (int i = 0; i < lineOwners.length; i++) {
            if (lineOwners[i] == anchor) {
                int end = i;
                while (end + 1 < lineOwners.length && lineOwners[end + 1] == anchor) {
                    end++;
                }
                newFirst = Math.min(i + anchorOffset, end);
                break;
            }
            if (lineOwners[i] > anchor) {
                // 読んでいたエントリが消えた時は、その次のエントリから表示する
                newFirst = i;
                break;
            }
        }
        if (newFirst < 0) {
            scrollBar.scrollToBottom();
        } else {
            scrollBar.showLine(newFirst);
        }
    }

    private void layout(CasterLog log) {
        Font font = font();
        List<FormattedCharSequence> newLines = new ArrayList<>();
        List<Long> owners = new ArrayList<>();
        for (CasterLog.Entry entry : log.entries()) {
            MutableComponent text = entry.message().copy();
            if (entry.repeatCount() > 1) {
                text.append(Component.literal(" ×" + entry.repeatCount()).withStyle(ChatFormatting.GRAY));
            }
            List<FormattedCharSequence> wrapped = font.split(text, WRAP_WIDTH);
            if (wrapped.isEmpty()) {
                wrapped = List.of(FormattedCharSequence.EMPTY);
            }
            for (FormattedCharSequence line : wrapped) {
                newLines.add(line);
                owners.add(entry.sequence());
            }
        }
        lines = newLines;
        lineOwners = owners.stream().mapToLong(Long::longValue).toArray();
    }

    /** ログ本文を描く内側の画面。 */
    private final class LogView extends GuiInnerScreen {
        private LogView(IGuiWrapper gui, int x, int y) {
            super(gui, x, y, VIEW_WIDTH, VIEW_HEIGHT);
        }

        @Override
        public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            super.renderForeground(guiGraphics, mouseX, mouseY);
            PoseStack pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(relativeX + PADDING, relativeY + PADDING, 0);
            pose.scale(TEXT_SCALE, TEXT_SCALE, 1.0F);
            if (lines.isEmpty()) {
                Component empty = Component.translatable(KEY + "empty");
                int centerX = (int) ((VIEW_WIDTH - PADDING * 2) / TEXT_SCALE / 2);
                int centerY = (int) ((VIEW_HEIGHT - PADDING * 2) / TEXT_SCALE / 2) - 4;
                guiGraphics.drawString(font(), empty, centerX - font().width(empty) / 2, centerY, MUTED_TEXT_COLOR, false);
            } else {
                int first = scrollBar.getCurrentSelection();
                int step = Math.round(LINE_HEIGHT / TEXT_SCALE);
                for (int i = 0; i < VISIBLE_LINES && first + i < lines.size(); i++) {
                    guiGraphics.drawString(font(), lines.get(first + i), 0, i * step, DEFAULT_TEXT_COLOR, false);
                }
            }
            pose.popPose();
        }
    }

    /** 最下部判定と、行番号を指定したスクロールを足したスクロールバー。 */
    private static final class LogScrollBar extends GuiScrollBar {
        private static final int LINES_PER_NOTCH = 3;

        private LogScrollBar(IGuiWrapper gui, int x, int y, int height, IntSupplier maxElements, IntSupplier focusedElements) {
            super(gui, x, y, height, maxElements, focusedElements);
        }

        @Override
        protected int getScrollElementScaler() {
            return LINES_PER_NOTCH;
        }

        boolean isAtBottom() {
            return !needsScrollBars() || getCurrentSelection() >= getElements();
        }

        void scrollToBottom() {
            scroll = 1.0D;
        }

        /** 指定した行が先頭に来るようにする。 */
        void showLine(int line) {
            if (!needsScrollBars()) {
                scroll = 0.0D;
                return;
            }
            int elements = getElements();
            int clamped = Mth.clamp(line, 0, elements);
            // getCurrentSelection() は (elements + 0.5) * scroll を切り捨てるので、行の途中を指しておく
            scroll = Mth.clamp((clamped + 0.25D) / (elements + 0.5D), 0.0D, 1.0D);
        }
    }
}
