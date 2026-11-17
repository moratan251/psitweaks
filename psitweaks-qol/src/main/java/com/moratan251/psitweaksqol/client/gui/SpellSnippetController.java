package com.moratan251.psitweaksqol.client.gui;

import com.moratan251.psitweaksqol.PsitweaksQol;
import com.moratan251.psitweaksqol.client.snippet.SpellPieceLayout;
import com.moratan251.psitweaksqol.client.snippet.SpellSnippet;
import com.moratan251.psitweaksqol.client.snippet.SpellSnippetStore;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;
import vazkii.psi.api.spell.SpellGrid;
import vazkii.psi.client.gui.GuiProgrammer;

/**
 * スニペット（名前を付けて保存したピースの並び）の保存・一覧・貼り付け。
 * <ul>
 * <li>保存: 範囲を選んだ状態でボタンか Ctrl+S を押すと、グリッドの上に名前欄が出る。Enter で保存、Esc でやめる。</li>
 * <li>一覧: 範囲を選んでいないときにボタンか Ctrl+S を押すと、グリッドを覆って一覧が開く。</li>
 * <li>貼り付け: 一覧で選ぶと、カーソルのマスを左上にした下書きが出る。左クリックで置き、Shift を押していれば続けて置ける。</li>
 * </ul>
 * 名前欄と一覧を開いている間は、ほかの入力をすべてここで受け取る。
 */
public final class SpellSnippetController {
    private static final int LEFT_MOUSE_BUTTON = 0;
    private static final int RIGHT_MOUSE_BUTTON = 1;
    private static final int GRID_CELLS = SpellPieceLayout.GRID_SIZE;
    private static final int CELL_SIZE = SpellSnippetPreview.CELL_SIZE;
    private static final int PIECE_SIZE = 16;
    private static final int BUTTON_OFFSET_X = 14;
    private static final int BUTTON_OFFSET_Y = -16;
    private static final int BUTTON_SIZE = 12;
    private static final float OVERLAY_Z = 500.0F;
    private static final float GHOST_Z = 250.0F;
    private static final long STATUS_MILLIS = 3000L;

    private static final int PANEL_BACKGROUND_COLOR = 0xF0141414;
    private static final int PANEL_BORDER_COLOR = 0xFF5CB3FF;
    private static final int FIELD_BACKGROUND_COLOR = 0xFF000000;
    private static final int FIELD_BORDER_COLOR = 0xFF606060;
    private static final int ROW_HOVER_COLOR = 0x559999FF;
    private static final int TITLE_COLOR = 0xFFFFFF;
    private static final int TEXT_COLOR = 0xE0E0E0;
    private static final int SUB_TEXT_COLOR = 0x909090;
    private static final int HINT_COLOR = 0xA0A0A0;
    private static final int WARNING_COLOR = 0xFFD860;
    private static final int ERROR_COLOR = 0xFF6060;
    private static final int SUCCESS_COLOR = 0x80FF80;
    private static final int LINK_COLOR = 0x8CC8FF;
    private static final int DELETE_COLOR = 0xFFE05050;
    private static final int DELETE_HOVER_COLOR = 0xFFFF8080;
    private static final int SCROLLBAR_COLOR = 0xFF707070;

    private static final int GHOST_BACKING_COLOR = 0xE0141414;
    private static final int GHOST_FREE_TINT = 0x304A90E2;
    private static final int GHOST_OVERWRITE_TINT = 0x60FF9020;
    private static final int GHOST_BLOCKED_TINT = 0x70FF3030;
    private static final int GHOST_FRAME_COLOR = 0xFF5CB3FF;
    private static final int GHOST_BLOCKED_FRAME_COLOR = 0xFFFF4040;

    private static final int DIALOG_WIDTH = 160;
    private static final int DIALOG_HEIGHT = 70;
    private static final int DIALOG_BUTTON_HEIGHT = 12;
    private static final int DIALOG_BUTTON_MIN_WIDTH = 36;
    private static final int BUTTON_BACKGROUND_COLOR = 0xFF2A2A2A;
    private static final int BUTTON_HOVER_COLOR = 0xFF3A5A7A;
    private static final int BUTTON_BORDER_COLOR = 0xFF707070;
    private static final int BUTTON_DISABLED_TEXT_COLOR = 0x707070;

    private static final int PANEL_PADDING = 4;
    private static final int PANEL_SEARCH_TOP = 16;
    private static final int PANEL_LIST_TOP = 32;
    private static final int PANEL_FOOTER_HEIGHT = 14;
    private static final int ROW_HEIGHT = 11;
    private static final int DELETE_WIDTH = 9;
    private static final int PREVIEW_MAX_SIZE = 100;

    private enum Mode {
        NONE,
        NAMING,
        LIST,
        PLACING
    }

    private static Mode mode = Mode.NONE;

    private static EditBox nameField;
    private static SpellPieceLayout pendingLayout = SpellPieceLayout.EMPTY;
    private static boolean pendingSelectionInTopHalf;
    private static String overwriteArmedName;
    private static String existsCheckedName;
    private static boolean existsCheckedResult;

    private static EditBox searchField;
    private static String lastSearchText = "";
    private static List<SpellSnippet> snippets = List.of();
    private static List<SpellSnippet> filtered = List.of();
    private static int scrollOffset;
    private static int highlightedIndex = -1;
    private static SpellSnippetPreview listPreview;

    private static SpellSnippet placingSnippet;
    private static SpellSnippetPreview placingPreview;

    private static Component statusMessage;
    private static int statusColor;
    private static long statusUntil;

    private SpellSnippetController() {
    }

    public static void onScreenInit(GuiProgrammer screen, Consumer<GuiEventListener> addListener) {
        if (screen.isSpectator() || screen.helpButton == null) {
            deactivate();
            return;
        }
        addListener.accept(new SpellSnippetButton(
                screen.helpButton.getX() + BUTTON_OFFSET_X,
                screen.helpButton.getY() + BUTTON_OFFSET_Y,
                screen));
    }

    /** 名前欄か一覧を開いていて、ほかの入力をここで受け取る間は true。 */
    public static boolean isActive(GuiProgrammer screen) {
        return mode == Mode.NAMING || mode == Mode.LIST;
    }

    public static boolean isOpen() {
        return mode != Mode.NONE;
    }

    /** 名前欄や一覧を開ける状態か。観戦中、コメント入力中、ピース一覧やほかの QoL の操作中は開けない。 */
    public static boolean canOpen(GuiProgrammer screen) {
        return screen.spell != null
                && !screen.isSpectator()
                && !screen.commentEnabled
                && (screen.panelWidget == null || !screen.panelWidget.panelEnabled)
                && !SpellPieceModeButtonOverlay.isActive(screen)
                && !EditableStringInputOverlay.isActive(screen)
                && !SpellNameFromTrickController.isPicking(screen);
    }

    public static void onButtonPressed(GuiProgrammer screen) {
        if (mode != Mode.NONE) {
            close();
            return;
        }
        open(screen);
    }

    private static void open(GuiProgrammer screen) {
        if (!canOpen(screen)) {
            return;
        }
        SpellGridMultiSelectionController.SelectionSnapshot selection =
                SpellGridMultiSelectionController.snapshotSelection(screen);
        if (selection.layout().isEmpty()) {
            openList();
        } else {
            startNaming(screen, selection);
        }
    }

    public static void deactivate() {
        close();
        lastSearchText = "";
        statusMessage = null;
    }

    private static void close() {
        mode = Mode.NONE;
        nameField = null;
        pendingLayout = SpellPieceLayout.EMPTY;
        overwriteArmedName = null;
        existsCheckedName = null;
        if (searchField != null) {
            lastSearchText = searchField.getValue();
        }
        searchField = null;
        snippets = List.of();
        filtered = List.of();
        scrollOffset = 0;
        highlightedIndex = -1;
        listPreview = null;
        placingSnippet = null;
        placingPreview = null;
    }

    // ---- 描画 ----

    /** Psi の描画の前に呼ぶ。開いている間は、下に隠れたピースやボタンのツールチップを Psi に描かせない。 */
    public static void beforeRender(GuiProgrammer screen, int mouseX, int mouseY) {
        if (isActive(screen) || (mode == Mode.PLACING && cellAt(screen, mouseX, mouseY) != null)) {
            screen.mouseMoved = false;
        }
    }

    public static void render(GuiProgrammer screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (mode != Mode.NONE && !canStayOpen(screen)) {
            close();
        }

        switch (mode) {
            case NAMING -> renderNaming(screen, guiGraphics, mouseX, mouseY, partialTick);
            case LIST -> renderList(screen, guiGraphics, mouseX, mouseY, partialTick);
            case PLACING -> renderPlacing(screen, guiGraphics, mouseX, mouseY);
            default -> {
            }
        }
        renderMessages(screen, guiGraphics);
    }

    private static boolean canStayOpen(GuiProgrammer screen) {
        return screen.spell != null
                && !screen.commentEnabled
                && (screen.panelWidget == null || !screen.panelWidget.panelEnabled);
    }

    private static void renderMessages(GuiProgrammer screen, GuiGraphics guiGraphics) {
        Font font = Minecraft.getInstance().font;
        List<FormattedCharSequence> lines = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        int width = Math.max(screen.xSize, 240);
        if (statusMessage != null && Util.getMillis() < statusUntil) {
            for (FormattedCharSequence line : font.split(statusMessage, width)) {
                lines.add(line);
                colors.add(statusColor);
            }
        } else {
            statusMessage = null;
        }
        if (mode == Mode.PLACING && placingSnippet != null) {
            Component hint = Component.translatable("psitweaks_qol.snippet.place.hint", placingSnippet.name());
            for (FormattedCharSequence line : font.split(hint, width)) {
                lines.add(line);
                colors.add(HINT_COLOR);
            }
        }
        if (lines.isEmpty()) {
            return;
        }

        int centerX = screen.left + screen.xSize / 2;
        int y = Math.max(2, screen.top - 24 - lines.size() * 10);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, OVERLAY_Z);
        for (int i = 0; i < lines.size(); i++) {
            guiGraphics.drawCenteredString(font, lines.get(i), centerX, y + i * 10, colors.get(i));
        }
        guiGraphics.pose().popPose();
    }

    private static void setStatus(Component message, int color) {
        statusMessage = message;
        statusColor = color;
        statusUntil = Util.getMillis() + STATUS_MILLIS;
    }

    // ---- 保存（名前欄） ----

    private static void startNaming(GuiProgrammer screen, SpellGridMultiSelectionController.SelectionSnapshot selection) {
        close();
        mode = Mode.NAMING;
        pendingLayout = selection.layout();
        pendingSelectionInTopHalf = selection.originY() * 2 + selection.layout().height() <= GRID_CELLS;
        Font font = Minecraft.getInstance().font;
        nameField = new EditBox(font, 0, 0, DIALOG_WIDTH - 16, 10, Component.translatable("psitweaks_qol.snippet.save.name"));
        nameField.setBordered(false);
        nameField.setMaxLength(SpellSnippetStore.MAX_NAME_LENGTH);
        nameField.setTextColor(TITLE_COLOR);
        nameField.setHint(Component.translatable("psitweaks_qol.snippet.save.name_hint").withStyle(ChatFormatting.DARK_GRAY));
        SpellSnippetStore.validateName(screen.spell.name).ifPresent(nameField::setValue);
        nameField.setFocused(true);
    }

    private static int dialogLeft(GuiProgrammer screen) {
        return screen.gridLeft + (GRID_CELLS * CELL_SIZE - 2) / 2 - DIALOG_WIDTH / 2;
    }

    /** 選んだ範囲を隠さないよう、範囲がグリッドの上半分にあれば下に、そうでなければ上に出す。 */
    private static int dialogTop(GuiProgrammer screen) {
        return pendingSelectionInTopHalf
                ? screen.gridTop + GRID_CELLS * CELL_SIZE - 2 - DIALOG_HEIGHT - 2
                : screen.gridTop + 2;
    }

    private static void renderNaming(GuiProgrammer screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (nameField == null) {
            close();
            return;
        }
        Font font = Minecraft.getInstance().font;
        int left = dialogLeft(screen);
        int top = dialogTop(screen);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, OVERLAY_Z);
        drawPanel(guiGraphics, left, top, DIALOG_WIDTH, DIALOG_HEIGHT);
        guiGraphics.drawString(font, Component.translatable("psitweaks_qol.snippet.save.title",
                pendingLayout.width(), pendingLayout.height(), pendingLayout.pieceCount()), left + 6, top + 5, TITLE_COLOR);

        int fieldTop = top + 17;
        guiGraphics.fill(left + 5, fieldTop - 2, left + DIALOG_WIDTH - 5, fieldTop + 10, FIELD_BORDER_COLOR);
        guiGraphics.fill(left + 6, fieldTop - 1, left + DIALOG_WIDTH - 6, fieldTop + 9, FIELD_BACKGROUND_COLOR);
        nameField.setX(left + 8);
        nameField.setY(fieldTop);
        nameField.render(guiGraphics, mouseX, mouseY, partialTick);

        Optional<String> name = SpellSnippetStore.validateName(nameField.getValue());
        Component hint;
        int hintColor;
        if (name.isPresent() && nameExists(name.get())) {
            boolean armed = name.get().equals(overwriteArmedName);
            hint = Component.translatable(armed
                    ? "psitweaks_qol.snippet.save.overwrite_confirm"
                    : "psitweaks_qol.snippet.save.exists");
            hintColor = WARNING_COLOR;
        } else {
            hint = Component.translatable("psitweaks_qol.snippet.save.hint");
            hintColor = HINT_COLOR;
        }
        guiGraphics.drawWordWrap(font, hint, left + 6, top + 32, DIALOG_WIDTH - 12, hintColor);

        boolean armed = name.isPresent() && name.get().equals(overwriteArmedName) && nameExists(name.get());
        drawDialogButton(guiGraphics, font, saveButtonLeft(screen), dialogButtonTop(screen), dialogButtonWidth(font),
                Component.translatable(armed ? "psitweaks_qol.snippet.save.overwrite_button" : "psitweaks_qol.snippet.save.button"),
                name.isPresent(), isOverSaveButton(screen, mouseX, mouseY));
        drawDialogButton(guiGraphics, font, cancelButtonLeft(screen), dialogButtonTop(screen), dialogButtonWidth(font),
                Component.translatable("psitweaks_qol.snippet.save.cancel"),
                true, isOverCancelButton(screen, mouseX, mouseY));
        guiGraphics.pose().popPose();
    }

    private static void drawDialogButton(
            GuiGraphics guiGraphics,
            Font font,
            int left,
            int top,
            int width,
            Component label,
            boolean enabled,
            boolean hovered
    ) {
        guiGraphics.fill(left, top, left + width, top + DIALOG_BUTTON_HEIGHT, BUTTON_BORDER_COLOR);
        guiGraphics.fill(left + 1, top + 1, left + width - 1, top + DIALOG_BUTTON_HEIGHT - 1,
                enabled && hovered ? BUTTON_HOVER_COLOR : BUTTON_BACKGROUND_COLOR);
        guiGraphics.drawCenteredString(font, label, left + width / 2, top + 2,
                enabled ? TITLE_COLOR : BUTTON_DISABLED_TEXT_COLOR);
    }

    /** 保存とやめるのボタンの幅。どの言語でも文字が入り、押しているうちに幅が変わらないよう、いちばん長い文字に合わせる。 */
    private static int dialogButtonWidth(Font font) {
        int labelWidth = Math.max(font.width(Component.translatable("psitweaks_qol.snippet.save.button")),
                Math.max(font.width(Component.translatable("psitweaks_qol.snippet.save.overwrite_button")),
                        font.width(Component.translatable("psitweaks_qol.snippet.save.cancel"))));
        return Math.max(DIALOG_BUTTON_MIN_WIDTH, labelWidth + 10);
    }

    private static int dialogButtonTop(GuiProgrammer screen) {
        return dialogTop(screen) + DIALOG_HEIGHT - DIALOG_BUTTON_HEIGHT - 5;
    }

    private static int cancelButtonLeft(GuiProgrammer screen) {
        return dialogLeft(screen) + DIALOG_WIDTH - 6 - dialogButtonWidth(Minecraft.getInstance().font);
    }

    private static int saveButtonLeft(GuiProgrammer screen) {
        return cancelButtonLeft(screen) - 4 - dialogButtonWidth(Minecraft.getInstance().font);
    }

    private static boolean isOverSaveButton(GuiProgrammer screen, double mouseX, double mouseY) {
        return isOverDialogButton(screen, saveButtonLeft(screen), mouseX, mouseY);
    }

    private static boolean isOverCancelButton(GuiProgrammer screen, double mouseX, double mouseY) {
        return isOverDialogButton(screen, cancelButtonLeft(screen), mouseX, mouseY);
    }

    private static boolean isOverDialogButton(GuiProgrammer screen, int left, double mouseX, double mouseY) {
        int top = dialogButtonTop(screen);
        return mouseX >= left && mouseX < left + dialogButtonWidth(Minecraft.getInstance().font)
                && mouseY >= top && mouseY < top + DIALOG_BUTTON_HEIGHT;
    }

    /** 描画のたびにファイルを見ないよう、最後に調べた名前の結果を使い回す。 */
    private static boolean nameExists(String name) {
        if (!name.equals(existsCheckedName)) {
            existsCheckedName = name;
            existsCheckedResult = SpellSnippetStore.exists(name);
        }
        return existsCheckedResult;
    }

    private static void trySave(GuiProgrammer screen) {
        Optional<String> validName = SpellSnippetStore.validateName(nameField.getValue());
        if (validName.isEmpty()) {
            ClientGuiSounds.playError();
            return;
        }
        String name = validName.get();
        if (SpellSnippetStore.exists(name) && !name.equals(overwriteArmedName)) {
            overwriteArmedName = name;
            ClientGuiSounds.playClick();
            return;
        }

        try {
            SpellSnippetStore.save(name, pendingLayout);
            setStatus(Component.translatable("psitweaks_qol.snippet.saved", name), SUCCESS_COLOR);
            ClientGuiSounds.playClick();
            close();
        } catch (IOException | RuntimeException e) {
            PsitweaksQol.LOGGER.error("Failed to save spell snippet {}", name, e);
            setStatus(Component.translatable("psitweaks_qol.snippet.save_failed", String.valueOf(e.getMessage())), ERROR_COLOR);
            ClientGuiSounds.playError();
        }
    }

    // ---- 一覧 ----

    private static void openList() {
        close();
        mode = Mode.LIST;
        snippets = SpellSnippetStore.loadAll();
        Font font = Minecraft.getInstance().font;
        searchField = new EditBox(font, 0, 0, 100, 10, Component.translatable("psitweaks_qol.snippet.list.search"));
        searchField.setBordered(false);
        searchField.setMaxLength(SpellSnippetStore.MAX_NAME_LENGTH);
        searchField.setTextColor(TITLE_COLOR);
        searchField.setHint(Component.translatable("psitweaks_qol.snippet.list.search_hint").withStyle(ChatFormatting.DARK_GRAY));
        searchField.setValue(lastSearchText);
        searchField.setFocused(true);
        refilter();
    }

    private static void reloadList() {
        snippets = SpellSnippetStore.loadAll();
        refilter();
    }

    private static void refilter() {
        String query = searchField == null ? "" : searchField.getValue().strip().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            filtered = snippets;
        } else {
            filtered = snippets.stream()
                    .filter(snippet -> snippet.name().toLowerCase(Locale.ROOT).contains(query))
                    .toList();
        }
        highlightedIndex = filtered.isEmpty() ? -1 : Mth.clamp(highlightedIndex, 0, filtered.size() - 1);
        scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll());
        listPreview = null;
    }

    private static int panelLeft(GuiProgrammer screen) {
        return screen.gridLeft - 2;
    }

    private static int panelTop(GuiProgrammer screen) {
        return screen.gridTop - 2;
    }

    private static int panelSize() {
        return GRID_CELLS * CELL_SIZE + 2;
    }

    private static int visibleRows() {
        return (panelSize() - PANEL_LIST_TOP - PANEL_FOOTER_HEIGHT) / ROW_HEIGHT;
    }

    private static int maxScroll() {
        return Math.max(0, filtered.size() - visibleRows());
    }

    private static boolean isInsidePanel(GuiProgrammer screen, double mouseX, double mouseY) {
        int left = panelLeft(screen);
        int top = panelTop(screen);
        return mouseX >= left && mouseX < left + panelSize() && mouseY >= top && mouseY < top + panelSize();
    }

    /** カーソルの下の行（{@code filtered} の位置）。行の上でなければ -1。 */
    private static int rowIndexAt(GuiProgrammer screen, double mouseX, double mouseY) {
        int left = panelLeft(screen);
        int listTop = panelTop(screen) + PANEL_LIST_TOP;
        if (mouseX < left + 2 || mouseX >= left + panelSize() - 2 || mouseY < listTop) {
            return -1;
        }
        int row = (int) Math.floor((mouseY - listTop) / ROW_HEIGHT);
        if (row < 0 || row >= visibleRows()) {
            return -1;
        }
        int index = scrollOffset + row;
        return index < filtered.size() ? index : -1;
    }

    private static boolean isOverSearchField(GuiProgrammer screen, double mouseX, double mouseY) {
        int left = panelLeft(screen);
        int searchTop = panelTop(screen) + PANEL_SEARCH_TOP;
        return mouseX >= left + PANEL_PADDING && mouseX < left + panelSize() - PANEL_PADDING
                && mouseY >= searchTop && mouseY < searchTop + 12;
    }

    private static boolean isOverDelete(GuiProgrammer screen, double mouseX) {
        int deleteLeft = panelLeft(screen) + panelSize() - 6 - DELETE_WIDTH;
        return mouseX >= deleteLeft && mouseX < deleteLeft + DELETE_WIDTH;
    }

    private static boolean isOverFolderLink(GuiProgrammer screen, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        int left = panelLeft(screen) + PANEL_PADDING + 1;
        int top = panelTop(screen) + panelSize() - PANEL_FOOTER_HEIGHT + 3;
        int width = font.width(Component.translatable("psitweaks_qol.snippet.list.open_folder"));
        return mouseX >= left && mouseX < left + width && mouseY >= top - 1 && mouseY < top + 10;
    }

    private static void renderList(GuiProgrammer screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (searchField == null) {
            close();
            return;
        }
        Font font = Minecraft.getInstance().font;
        int left = panelLeft(screen);
        int top = panelTop(screen);
        int size = panelSize();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, OVERLAY_Z);
        drawPanel(guiGraphics, left, top, size, size);

        guiGraphics.drawString(font, Component.translatable("psitweaks_qol.snippet.list.title"), left + PANEL_PADDING + 1, top + 5, TITLE_COLOR);
        String count = Component.translatable("psitweaks_qol.snippet.list.count", snippets.size()).getString();
        guiGraphics.drawString(font, count, left + size - PANEL_PADDING - 1 - font.width(count), top + 5, SUB_TEXT_COLOR);

        int searchTop = top + PANEL_SEARCH_TOP;
        guiGraphics.fill(left + PANEL_PADDING, searchTop, left + size - PANEL_PADDING, searchTop + 12, FIELD_BORDER_COLOR);
        guiGraphics.fill(left + PANEL_PADDING + 1, searchTop + 1, left + size - PANEL_PADDING - 1, searchTop + 11, FIELD_BACKGROUND_COLOR);
        searchField.setX(left + PANEL_PADDING + 3);
        searchField.setY(searchTop + 2);
        searchField.setWidth(size - PANEL_PADDING * 2 - 6);
        searchField.render(guiGraphics, mouseX, mouseY, partialTick);

        int listTop = top + PANEL_LIST_TOP;
        int hoveredIndex = rowIndexAt(screen, mouseX, mouseY);
        boolean hoveringDelete = hoveredIndex >= 0 && isOverDelete(screen, mouseX);
        if (filtered.isEmpty()) {
            Component empty = Component.translatable(snippets.isEmpty()
                    ? "psitweaks_qol.snippet.list.empty"
                    : "psitweaks_qol.snippet.list.no_match");
            guiGraphics.drawWordWrap(font, empty, left + PANEL_PADDING + 2, listTop + 4, size - PANEL_PADDING * 2 - 4, HINT_COLOR);
        } else {
            int rows = Math.min(visibleRows(), filtered.size() - scrollOffset);
            for (int row = 0; row < rows; row++) {
                int index = scrollOffset + row;
                SpellSnippet snippet = filtered.get(index);
                int rowTop = listTop + row * ROW_HEIGHT;
                boolean hovered = index == hoveredIndex;
                if (hovered || (hoveredIndex < 0 && index == highlightedIndex)) {
                    guiGraphics.fill(left + 2, rowTop, left + size - 2, rowTop + ROW_HEIGHT, ROW_HOVER_COLOR);
                }

                String sizeText = Component.translatable("psitweaks_qol.snippet.size",
                        snippet.layout().width(), snippet.layout().height()).getString();
                int deleteLeft = left + size - 6 - DELETE_WIDTH;
                int sizeLeft = deleteLeft - 3 - font.width(sizeText);
                int nameMaxWidth = sizeLeft - (left + PANEL_PADDING + 2) - 4;
                guiGraphics.drawString(font, ellipsize(font, snippet.name(), nameMaxWidth),
                        left + PANEL_PADDING + 2, rowTop + 2, TEXT_COLOR);
                guiGraphics.drawString(font, sizeText, sizeLeft, rowTop + 2, SUB_TEXT_COLOR);
                if (hovered) {
                    guiGraphics.drawString(font, "×", deleteLeft + 2, rowTop + 2,
                            hoveringDelete ? DELETE_HOVER_COLOR : DELETE_COLOR);
                }
            }

            if (filtered.size() > visibleRows()) {
                int trackHeight = visibleRows() * ROW_HEIGHT;
                int barHeight = Math.max(6, trackHeight * visibleRows() / filtered.size());
                int barTop = listTop + (trackHeight - barHeight) * scrollOffset / Math.max(1, maxScroll());
                guiGraphics.fill(left + size - 4, barTop, left + size - 2, barTop + barHeight, SCROLLBAR_COLOR);
            }
        }

        Component folder = Component.translatable("psitweaks_qol.snippet.list.open_folder");
        boolean hoveringFolder = isOverFolderLink(screen, mouseX, mouseY);
        int footerTop = top + size - PANEL_FOOTER_HEIGHT + 3;
        guiGraphics.drawString(font, hoveringFolder ? folder.copy().withStyle(ChatFormatting.UNDERLINE) : folder,
                left + PANEL_PADDING + 1, footerTop, LINK_COLOR);

        int previewIndex = hoveredIndex >= 0 ? hoveredIndex : highlightedIndex;
        if (previewIndex >= 0 && previewIndex < filtered.size()) {
            renderListPreview(screen, guiGraphics, filtered.get(previewIndex));
        }

        if (hoveringDelete) {
            guiGraphics.renderTooltip(font, Component.translatable("psitweaks_qol.snippet.list.delete_hint"), mouseX, mouseY);
        } else if (hoveringFolder) {
            guiGraphics.renderTooltip(font, Component.literal(SpellSnippetStore.directory().toString()), mouseX, mouseY);
        }
        guiGraphics.pose().popPose();
    }

    /** 一覧で指しているスニペットの絵を、GUI の右（入らなければ左）に出す。 */
    private static void renderListPreview(GuiProgrammer screen, GuiGraphics guiGraphics, SpellSnippet snippet) {
        if (listPreview == null || listPreview.layout() != snippet.layout()) {
            listPreview = SpellSnippetPreview.of(snippet.layout());
        }
        Font font = Minecraft.getInstance().font;
        SpellPieceLayout layout = snippet.layout();
        int contentWidth = layout.width() * CELL_SIZE - 2;
        int contentHeight = layout.height() * CELL_SIZE - 2;
        float scale = Math.min(1.0F, (float) PREVIEW_MAX_SIZE / Math.max(contentWidth, contentHeight));
        int drawnWidth = Mth.ceil(contentWidth * scale);
        int drawnHeight = Mth.ceil(contentHeight * scale);

        String title = ellipsize(font, snippet.name(), 140);
        String info = Component.translatable("psitweaks_qol.snippet.info",
                layout.width(), layout.height(), layout.pieceCount()).getString();
        int unusable = listPreview.unusableCount();
        String warning = unusable > 0
                ? Component.translatable("psitweaks_qol.snippet.unusable", unusable).getString()
                : "";
        int textLines = unusable > 0 ? 3 : 2;
        int boxWidth = Math.max(drawnWidth, Math.max(font.width(title), Math.max(font.width(info), font.width(warning)))) + 10;
        int boxHeight = 5 + textLines * 10 + 3 + drawnHeight + 5;

        int boxLeft = screen.left + screen.xSize + 4;
        if (boxLeft + boxWidth > screen.width - 2) {
            boxLeft = screen.left - boxWidth - 4;
            if (boxLeft < 2) {
                return;
            }
        }
        int boxTop = Mth.clamp(screen.top + 20, 2, Math.max(2, screen.height - boxHeight - 2));

        drawPanel(guiGraphics, boxLeft, boxTop, boxWidth, boxHeight);
        guiGraphics.drawString(font, title, boxLeft + 5, boxTop + 5, TITLE_COLOR);
        guiGraphics.drawString(font, info, boxLeft + 5, boxTop + 15, SUB_TEXT_COLOR);
        if (unusable > 0) {
            guiGraphics.drawString(font, warning, boxLeft + 5, boxTop + 25, ERROR_COLOR);
        }
        listPreview.draw(guiGraphics, boxLeft + (boxWidth - drawnWidth) / 2.0F, boxTop + 8 + textLines * 10,
                scale, 1.0F, true);
    }

    private static void startPlacing(SpellSnippet snippet) {
        close();
        mode = Mode.PLACING;
        placingSnippet = snippet;
        placingPreview = SpellSnippetPreview.of(snippet.layout());
        SpellGridMultiSelectionController.clearSelection();
        SpellGridMultiSelectionController.setClipboard(snippet.layout());
    }

    private static void deleteSnippet(SpellSnippet snippet) {
        try {
            SpellSnippetStore.delete(snippet);
            setStatus(Component.translatable("psitweaks_qol.snippet.deleted", snippet.name()), SUCCESS_COLOR);
            ClientGuiSounds.playClick();
        } catch (IOException | RuntimeException e) {
            PsitweaksQol.LOGGER.error("Failed to delete spell snippet {}", snippet.fileName(), e);
            setStatus(Component.translatable("psitweaks_qol.snippet.delete_failed", String.valueOf(e.getMessage())), ERROR_COLOR);
            ClientGuiSounds.playError();
        }
        reloadList();
    }

    private static void openFolder() {
        Path directory = SpellSnippetStore.directory();
        try {
            Files.createDirectories(directory);
            Util.getPlatform().openFile(directory.toFile());
            ClientGuiSounds.playClick();
        } catch (IOException | RuntimeException e) {
            PsitweaksQol.LOGGER.error("Failed to open spell snippet folder {}", directory, e);
            ClientGuiSounds.playError();
        }
    }

    // ---- 貼り付け ----

    private static void renderPlacing(GuiProgrammer screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (placingSnippet == null || placingPreview == null) {
            close();
            return;
        }
        int[] cell = cellAt(screen, mouseX, mouseY);
        if (cell == null) {
            return;
        }

        SpellPieceLayout layout = placingSnippet.layout();
        int originX = cell[0];
        int originY = cell[1];
        boolean fits = SpellPieceLayoutPlacer.fits(layout, originX, originY);
        int gridRight = screen.gridLeft + GRID_CELLS * CELL_SIZE;
        int gridBottom = screen.gridTop + GRID_CELLS * CELL_SIZE;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, GHOST_Z);
        guiGraphics.enableScissor(screen.gridLeft - 1, screen.gridTop - 1, gridRight, gridBottom);
        List<SpellPieceLayout.Entry> entries = layout.entries();
        for (int i = 0; i < entries.size(); i++) {
            if (placingPreview.isUsable(i)) {
                int x = screen.gridLeft + (originX + entries.get(i).offsetX()) * CELL_SIZE;
                int y = screen.gridTop + (originY + entries.get(i).offsetY()) * CELL_SIZE;
                guiGraphics.fill(x, y, x + PIECE_SIZE, y + PIECE_SIZE, GHOST_BACKING_COLOR);
            }
        }
        placingPreview.draw(guiGraphics, screen.gridLeft + originX * CELL_SIZE, screen.gridTop + originY * CELL_SIZE,
                1.0F, 0.85F, false);
        for (int i = 0; i < entries.size(); i++) {
            int targetX = originX + entries.get(i).offsetX();
            int targetY = originY + entries.get(i).offsetY();
            int x = screen.gridLeft + targetX * CELL_SIZE;
            int y = screen.gridTop + targetY * CELL_SIZE;
            if (!fits) {
                guiGraphics.fill(x, y, x + PIECE_SIZE, y + PIECE_SIZE, GHOST_BLOCKED_TINT);
            } else if (!placingPreview.isUsable(i)) {
                SpellSnippetPreview.drawUnusableMark(guiGraphics, x, y);
            } else if (screen.spell.grid.gridData[targetX][targetY] != null) {
                guiGraphics.fill(x, y, x + PIECE_SIZE, y + PIECE_SIZE, GHOST_OVERWRITE_TINT);
            } else {
                guiGraphics.fill(x, y, x + PIECE_SIZE, y + PIECE_SIZE, GHOST_FREE_TINT);
            }
        }

        int frameLeft = screen.gridLeft + originX * CELL_SIZE - 1;
        int frameTop = screen.gridTop + originY * CELL_SIZE - 1;
        int frameRight = screen.gridLeft + (originX + layout.width()) * CELL_SIZE - 1;
        int frameBottom = screen.gridTop + (originY + layout.height()) * CELL_SIZE - 1;
        int frameColor = fits ? GHOST_FRAME_COLOR : GHOST_BLOCKED_FRAME_COLOR;
        guiGraphics.fill(frameLeft, frameTop, frameRight, frameTop + 1, frameColor);
        guiGraphics.fill(frameLeft, frameBottom - 1, frameRight, frameBottom, frameColor);
        guiGraphics.fill(frameLeft, frameTop, frameLeft + 1, frameBottom, frameColor);
        guiGraphics.fill(frameRight - 1, frameTop, frameRight, frameBottom, frameColor);
        guiGraphics.disableScissor();
        guiGraphics.pose().popPose();
    }

    private static void placeAt(GuiProgrammer screen, int originX, int originY) {
        SpellGridMultiSelectionController.clearSelection();
        if (!SpellPieceLayoutPlacer.place(screen, placingSnippet.layout(), originX, originY)) {
            return;
        }
        GuiProgrammer.selectedX = originX;
        GuiProgrammer.selectedY = originY;
        screen.onSelectedChanged();
        if (!Screen.hasShiftDown()) {
            close();
        }
    }

    // ---- 入力 ----

    public static boolean handleMousePressedPre(GuiProgrammer screen, double mouseX, double mouseY, int button) {
        return switch (mode) {
            case NAMING -> handleNamingMouse(screen, mouseX, mouseY, button);
            case LIST -> handleListMouse(screen, mouseX, mouseY, button);
            case PLACING -> handlePlacingMouse(screen, mouseX, mouseY, button);
            default -> handleButtonMouse(screen, mouseX, mouseY, button);
        };
    }

    /**
     * スニペットのボタンの左クリックを、範囲選択の解除より先に受け取る。画面の通常の流れに任せると、
     * 先に複数選択の「普通のクリックで選択を解除」が動き、範囲を選んでいても一覧が開いてしまう。
     */
    private static boolean handleButtonMouse(GuiProgrammer screen, double mouseX, double mouseY, int button) {
        if (button != LEFT_MOUSE_BUTTON
                || screen.takingScreenshot
                || !isOverButton(screen, mouseX, mouseY)
                || !canOpen(screen)) {
            return false;
        }
        ClientGuiSounds.playClick();
        open(screen);
        ProgrammerOverlayInputGuard.blockLeftGesture();
        return true;
    }

    private static boolean handleNamingMouse(GuiProgrammer screen, double mouseX, double mouseY, int button) {
        int left = dialogLeft(screen);
        int top = dialogTop(screen);
        boolean inside = mouseX >= left && mouseX < left + DIALOG_WIDTH && mouseY >= top && mouseY < top + DIALOG_HEIGHT;
        int fieldTop = top + 17;
        boolean overField = inside && mouseY >= fieldTop - 2 && mouseY < fieldTop + 10;
        if (!inside) {
            close();
        } else if (button == LEFT_MOUSE_BUTTON && isOverSaveButton(screen, mouseX, mouseY)) {
            trySave(screen);
        } else if (button == LEFT_MOUSE_BUTTON && isOverCancelButton(screen, mouseX, mouseY)) {
            ClientGuiSounds.playClick();
            close();
        } else if (nameField != null && button == RIGHT_MOUSE_BUTTON && overField) {
            nameField.setValue("");
            nameField.setFocused(true);
        } else if (nameField != null && button == LEFT_MOUSE_BUTTON) {
            nameField.mouseClicked(mouseX, mouseY, button);
            nameField.setFocused(true);
        }
        ProgrammerOverlayInputGuard.blockLeftGesture();
        return true;
    }

    private static boolean handleListMouse(GuiProgrammer screen, double mouseX, double mouseY, int button) {
        ProgrammerOverlayInputGuard.blockLeftGesture();
        if (button == RIGHT_MOUSE_BUTTON && searchField != null && isOverSearchField(screen, mouseX, mouseY)) {
            searchField.setValue("");
            refilter();
            return true;
        }
        if (!isInsidePanel(screen, mouseX, mouseY) || button == RIGHT_MOUSE_BUTTON) {
            close();
            return true;
        }
        if (button != LEFT_MOUSE_BUTTON) {
            return true;
        }

        if (isOverFolderLink(screen, mouseX, mouseY)) {
            openFolder();
            return true;
        }
        if (searchField != null && isOverSearchField(screen, mouseX, mouseY)) {
            searchField.mouseClicked(mouseX, mouseY, button);
            searchField.setFocused(true);
            return true;
        }

        int index = rowIndexAt(screen, mouseX, mouseY);
        if (index < 0) {
            return true;
        }
        SpellSnippet snippet = filtered.get(index);
        if (isOverDelete(screen, mouseX)) {
            if (Screen.hasShiftDown()) {
                deleteSnippet(snippet);
            } else {
                ClientGuiSounds.playError();
            }
            return true;
        }
        ClientGuiSounds.playClick();
        startPlacing(snippet);
        return true;
    }

    private static boolean handlePlacingMouse(GuiProgrammer screen, double mouseX, double mouseY, int button) {
        if (button == RIGHT_MOUSE_BUTTON) {
            close();
            return true;
        }
        if (button != LEFT_MOUSE_BUTTON) {
            return false;
        }

        int[] cell = cellAt(screen, mouseX, mouseY);
        if (cell == null) {
            // グリッドの外を押したらやめる。スニペットのボタンの上なら、押したことにして一覧を開き直さない。
            boolean overButton = isOverButton(screen, mouseX, mouseY);
            close();
            if (overButton) {
                ProgrammerOverlayInputGuard.blockLeftGesture();
            }
            return overButton;
        }

        placeAt(screen, cell[0], cell[1]);
        ProgrammerOverlayInputGuard.blockLeftGesture();
        return true;
    }

    public static boolean handleMouseScrolledPre(GuiProgrammer screen, double mouseX, double mouseY, double scrollDeltaY) {
        if (mode == Mode.LIST) {
            if (isInsidePanel(screen, mouseX, mouseY) && scrollDeltaY != 0) {
                scrollOffset = Mth.clamp(scrollOffset - (int) Math.signum(scrollDeltaY), 0, maxScroll());
            }
            return true;
        }
        return mode == Mode.NAMING;
    }

    public static boolean handleKeyPressedPre(GuiProgrammer screen, int keyCode, int scanCode, int modifiers) {
        return switch (mode) {
            case NAMING -> handleNamingKey(screen, keyCode, scanCode, modifiers);
            case LIST -> handleListKey(keyCode, scanCode, modifiers);
            case PLACING -> {
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                    close();
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    /** Ctrl+S。範囲を選んでいれば名前欄を、選んでいなければ一覧を開く。文字の入力欄を使っている間は取らない。 */
    public static boolean handleShortcutKey(GuiProgrammer screen, int keyCode) {
        if (mode != Mode.NONE
                || keyCode != GLFW.GLFW_KEY_S
                || !Screen.hasControlDown()
                || Screen.hasShiftDown()
                || Screen.hasAltDown()
                || screen.isSpectator()
                || isTextInputFocused(screen)
                || !canOpen(screen)) {
            return false;
        }
        open(screen);
        return true;
    }

    private static boolean isTextInputFocused(GuiProgrammer screen) {
        return (screen.spellNameField != null && screen.spellNameField.isFocused())
                || (screen.commentField != null && screen.commentField.isFocused())
                || (screen.panelWidget != null && screen.panelWidget.searchField != null
                && screen.panelWidget.searchField.isFocused());
    }

    private static boolean handleNamingKey(GuiProgrammer screen, int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            trySave(screen);
            return true;
        }
        if (nameField != null) {
            nameField.keyPressed(keyCode, scanCode, modifiers);
        }
        return true;
    }

    private static boolean handleListKey(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_ESCAPE -> close();
            case GLFW.GLFW_KEY_UP -> moveHighlight(-1);
            case GLFW.GLFW_KEY_DOWN -> moveHighlight(1);
            case GLFW.GLFW_KEY_PAGE_UP -> moveHighlight(-visibleRows());
            case GLFW.GLFW_KEY_PAGE_DOWN -> moveHighlight(visibleRows());
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                if (highlightedIndex >= 0 && highlightedIndex < filtered.size()) {
                    ClientGuiSounds.playClick();
                    startPlacing(filtered.get(highlightedIndex));
                } else {
                    ClientGuiSounds.playError();
                }
            }
            default -> {
                if (searchField != null) {
                    String before = searchField.getValue();
                    searchField.keyPressed(keyCode, scanCode, modifiers);
                    if (!before.equals(searchField.getValue())) {
                        refilter();
                    }
                }
            }
        }
        return true;
    }

    private static void moveHighlight(int delta) {
        if (filtered.isEmpty()) {
            return;
        }
        highlightedIndex = highlightedIndex < 0
                ? (delta > 0 ? 0 : filtered.size() - 1)
                : Mth.clamp(highlightedIndex + delta, 0, filtered.size() - 1);
        if (highlightedIndex < scrollOffset) {
            scrollOffset = highlightedIndex;
        } else if (highlightedIndex >= scrollOffset + visibleRows()) {
            scrollOffset = highlightedIndex - visibleRows() + 1;
        }
    }

    public static boolean handleCharacterTypedPre(GuiProgrammer screen, char codePoint, int modifiers) {
        if (mode == Mode.NAMING) {
            if (nameField != null) {
                nameField.charTyped(codePoint, modifiers);
            }
            return true;
        }
        if (mode == Mode.LIST) {
            if (searchField != null) {
                String before = searchField.getValue();
                searchField.charTyped(codePoint, modifiers);
                if (!before.equals(searchField.getValue())) {
                    refilter();
                }
            }
            return true;
        }
        return false;
    }

    // ---- 共通 ----

    private static int[] cellAt(GuiProgrammer screen, double mouseX, double mouseY) {
        if (mouseX < screen.gridLeft || mouseY < screen.gridTop) {
            return null;
        }
        int x = (int) Math.floor((mouseX - screen.gridLeft) / CELL_SIZE);
        int y = (int) Math.floor((mouseY - screen.gridTop) / CELL_SIZE);
        return SpellGrid.exists(x, y) ? new int[] {x, y} : null;
    }

    private static boolean isOverButton(GuiProgrammer screen, double mouseX, double mouseY) {
        if (screen.helpButton == null) {
            return false;
        }
        int left = screen.helpButton.getX() + BUTTON_OFFSET_X;
        int top = screen.helpButton.getY() + BUTTON_OFFSET_Y;
        return mouseX >= left && mouseX < left + BUTTON_SIZE && mouseY >= top && mouseY < top + BUTTON_SIZE;
    }

    private static void drawPanel(GuiGraphics guiGraphics, int left, int top, int width, int height) {
        guiGraphics.fill(left, top, left + width, top + height, PANEL_BORDER_COLOR);
        guiGraphics.fill(left + 1, top + 1, left + width - 1, top + height - 1, PANEL_BACKGROUND_COLOR);
    }

    private static String ellipsize(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "…";
        return font.plainSubstrByWidth(text, Math.max(0, maxWidth - font.width(ellipsis))) + ellipsis;
    }
}
