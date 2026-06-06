package com.moratan251.psitweaks.client.gui;

import com.moratan251.psitweaks.common.spells.spellpiece.constant.PieceConstantString;
import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import vazkii.psi.api.spell.SpellGrid;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.client.gui.GuiProgrammer;

import java.util.ArrayList;
import java.util.List;

public final class StringConstantInputOverlay {
    private static final int PANEL_WIDTH = 320;
    private static final int PANEL_HEIGHT = 122;
    private static final int MARGIN = 8;
    private static final int MAX_LINES = 6;
    private static final int GRID_CELL_SIZE = 18;
    private static final int GRID_SIZE = 9;
    private static final int DOUBLE_CLICK_INTERVAL_MS = 500;
    private static final int TEXT_X_OFFSET = 11;
    private static final int TEXT_Y_OFFSET = 27;
    private static final int TEXT_WIDTH_INSET = 22;
    private static final int TEXT_AREA_LEFT_OFFSET = 8;
    private static final int TEXT_AREA_TOP_OFFSET = 20;
    private static final int TEXT_AREA_BOTTOM_OFFSET = 36;
    private static final int ACTION_BUTTON_WIDTH = 68;
    private static final int ACTION_BUTTON_HEIGHT = 14;
    private static final int ACTION_BUTTON_GAP = 4;
    private static final int ACTION_BUTTON_BOTTOM_OFFSET = 28;

    private static final int OUTER_COLOR = 0xF0100018;
    private static final int BORDER_COLOR = 0xFF7F1ED4;
    private static final int INNER_COLOR = 0xF0222024;
    private static final int BUTTON_COLOR = 0xFF34263D;
    private static final int BUTTON_DISABLED_COLOR = 0xFF242128;
    private static final int TEXT_COLOR = 0xFFEDE8F8;
    private static final int MUTED_TEXT_COLOR = 0xFFB8ACC8;
    private static final int COUNT_COLOR = 0xFFCFA9FF;
    private static final int SELECTION_COLOR = 0x805E35A5;

    private static int activeX = -1;
    private static int activeY = -1;
    private static int activeCursorPosition = -1;
    private static int selectionAnchor = -1;
    private static int lastClickX = -1;
    private static int lastClickY = -1;
    private static long lastClickTime = 0L;
    private static boolean draggingSelection = false;
    private static int scrollLineOffset = 0;
    private static boolean scrollFollowsCursor = true;

    private StringConstantInputOverlay() {
    }

    public static void render(GuiProgrammer screen, GuiGraphics guiGraphics) {
        PieceConstantString piece = getActiveSelectedStringConstant(screen);
        if (piece == null) {
            return;
        }

        Font font = Minecraft.getInstance().font;
        PanelLayout layout = layoutFor(screen);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 400.0F);

        drawPanel(guiGraphics, layout.x, layout.y, layout.width, layout.height);
        drawText(screen, guiGraphics, font, piece, layout);

        guiGraphics.pose().popPose();
    }

    public static boolean handleMousePressedPre(GuiProgrammer screen, double mouseX, double mouseY, int button) {
        PieceConstantString piece = getActiveSelectedStringConstant(screen);
        if (piece == null) {
            return false;
        }

        PanelLayout layout = layoutFor(screen);
        if (!layout.contains(mouseX, mouseY)) {
            return false;
        }

        if (button == 0) {
            ActionButton actionButton = actionButtonAt(layout, mouseX, mouseY);
            if (actionButton != null) {
                ClientGuiSounds.playClick();
                handleActionButton(screen, piece, actionButton);
                return true;
            }
        }

        if (button == 0 && layout.containsTextArea(mouseX, mouseY)) {
            Font font = Minecraft.getInstance().font;
            activeCursorPosition = clampCursor(piece, cursorPositionForClick(font, piece, layout, mouseX, mouseY));
            selectionAnchor = activeCursorPosition;
            draggingSelection = true;
            scrollFollowsCursor = false;
            piece.moveCursorTo(activeCursorPosition);
        }
        return true;
    }

    public static boolean handleMouseDraggedPre(GuiProgrammer screen, double mouseX, double mouseY, int button) {
        if (button != 0 || !draggingSelection) {
            return false;
        }

        PieceConstantString piece = getActiveSelectedStringConstant(screen);
        if (piece == null) {
            draggingSelection = false;
            return false;
        }

        PanelLayout layout = layoutFor(screen);
        Font font = Minecraft.getInstance().font;
        activeCursorPosition = clampCursor(piece, cursorPositionForClick(font, piece, layout, mouseX, mouseY));
        scrollFollowsCursor = false;
        piece.moveCursorTo(activeCursorPosition);
        return true;
    }

    public static boolean handleMouseReleasedPre(GuiProgrammer screen, int button) {
        if (button != 0 || !draggingSelection) {
            return false;
        }

        draggingSelection = false;
        if (getActiveSelectedStringConstant(screen) == null || !hasSelection()) {
            clearSelection();
        }
        return true;
    }

    public static boolean handleCharacterTypedPre(GuiProgrammer screen, char codePoint, int modifiers) {
        PieceConstantString piece = getActiveSelectedStringConstant(screen);
        if (piece == null) {
            return false;
        }

        if (!screen.isSpectator()) {
            replaceSelectionOrInsert(screen, piece, String.valueOf(codePoint), false);
        }
        return true;
    }

    public static boolean handleKeyPressedPre(GuiProgrammer screen, int keyCode, int scanCode) {
        PieceConstantString piece = getActiveSelectedStringConstant(screen);
        if (piece == null) {
            return false;
        }

        if (Screen.isSelectAll(keyCode)) {
            selectAll(piece);
            return true;
        }

        if (Screen.isCopy(keyCode)) {
            copySelection(piece);
            return true;
        }

        if (Screen.isCut(keyCode)) {
            cutSelection(screen, piece);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (Screen.hasShiftDown()) {
                replaceSelectionOrInsert(screen, piece, "\n", true);
            } else {
                deactivate(screen);
            }
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) {
            moveCursorVertically(screen, piece, keyCode == GLFW.GLFW_KEY_UP ? -1 : 1, Screen.hasShiftDown());
            return true;
        }

        if (isHorizontalCursorKey(keyCode)) {
            moveCursorHorizontally(
                    piece,
                    keyCode,
                    Screen.hasShiftDown(),
                    Screen.hasControlDown() && (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT)
            );
            return true;
        }

        return handleEditingKey(screen, piece, keyCode, scanCode);
    }

    public static boolean handleMouseScrolledPre(GuiProgrammer screen, double mouseX, double mouseY, double scrollDeltaY) {
        PieceConstantString piece = getActiveSelectedStringConstant(screen);
        if (piece == null) {
            return false;
        }

        PanelLayout layout = layoutFor(screen);
        if (!layout.contains(mouseX, mouseY)) {
            return false;
        }

        if (scrollDeltaY != 0.0D && !piece.getValue().isEmpty()) {
            Font font = Minecraft.getInstance().font;
            List<TextLine> lines = splitLines(font, piece.getValue(), layout.textWidth());
            scrollLineOffset = clampScrollLineOffset(lines,
                    scrollLineOffset + (scrollDeltaY > 0.0D ? -1 : 1));
            scrollFollowsCursor = false;
        }
        return true;
    }

    public static void handleMousePressedPost(GuiProgrammer screen, double mouseX, double mouseY, int button) {
        if (button != 0) {
            return;
        }

        GridPosition position = gridPositionAt(screen, mouseX, mouseY);
        if (position == null) {
            return;
        }

        PieceConstantString piece = getStringConstant(screen, position.x, position.y);
        if (piece == null) {
            deactivate(screen);
            lastClickX = -1;
            lastClickY = -1;
            return;
        }

        long now = System.currentTimeMillis();
        boolean isDoubleClick = lastClickX == position.x
                && lastClickY == position.y
                && now - lastClickTime <= DOUBLE_CLICK_INTERVAL_MS;
        lastClickX = position.x;
        lastClickY = position.y;
        lastClickTime = now;

        if (isDoubleClick && GuiProgrammer.selectedX == position.x && GuiProgrammer.selectedY == position.y) {
            activate(piece, position.x, position.y);
        } else if (activeX != position.x || activeY != position.y) {
            deactivate(screen);
        }
    }

    public static void deactivate(GuiProgrammer screen) {
        PieceConstantString piece = getStringConstant(screen, activeX, activeY);
        if (piece != null) {
            piece.setCursorEditing(false);
        }
        activeX = -1;
        activeY = -1;
        activeCursorPosition = -1;
        clearSelection();
        draggingSelection = false;
        scrollLineOffset = 0;
        scrollFollowsCursor = true;
    }

    private static void activate(PieceConstantString piece, int x, int y) {
        activeX = x;
        activeY = y;
        activeCursorPosition = piece.getValue().length();
        clearSelection();
        draggingSelection = false;
        scrollLineOffset = 0;
        scrollFollowsCursor = true;
        piece.setCursorEditing(true);
        piece.moveCursorTo(activeCursorPosition);
    }

    private static PieceConstantString getActiveSelectedStringConstant(GuiProgrammer screen) {
        if (activeX != GuiProgrammer.selectedX || activeY != GuiProgrammer.selectedY) {
            deactivate(screen);
            return null;
        }

        PieceConstantString piece = getStringConstant(screen, activeX, activeY);
        if (piece == null) {
            deactivate(screen);
        } else {
            applyActiveCursor(piece);
        }
        return piece;
    }

    private static PieceConstantString getStringConstant(GuiProgrammer screen, int x, int y) {
        if (screen.spell == null || !SpellGrid.exists(x, y)) {
            return null;
        }

        SpellPiece piece = screen.spell.grid.gridData[x][y];
        if (piece instanceof PieceConstantString stringPiece) {
            return stringPiece;
        }
        return null;
    }

    private static GridPosition gridPositionAt(GuiProgrammer screen, double mouseX, double mouseY) {
        if (mouseX < screen.gridLeft || mouseY < screen.gridTop) {
            return null;
        }

        int x = (int) ((mouseX - screen.gridLeft) / GRID_CELL_SIZE);
        int y = (int) ((mouseY - screen.gridTop) / GRID_CELL_SIZE);
        if (x < 0 || y < 0 || x >= GRID_SIZE || y >= GRID_SIZE) {
            return null;
        }

        return new GridPosition(x, y);
    }

    private static PanelLayout layoutFor(GuiProgrammer screen) {
        int panelWidth = Math.min(PANEL_WIDTH, screen.width - MARGIN * 2);
        int panelHeight = Math.min(PANEL_HEIGHT, screen.height - MARGIN * 2);
        int x = chooseX(screen, panelWidth);
        int y = Math.max(MARGIN, Math.min(screen.top + 42, screen.height - panelHeight - MARGIN));
        return new PanelLayout(x, y, panelWidth, panelHeight);
    }

    private static int chooseX(GuiProgrammer screen, int panelWidth) {
        int rightX = screen.left + screen.xSize + 10;
        if (rightX + panelWidth <= screen.width - MARGIN) {
            return rightX;
        }

        int leftX = screen.left - panelWidth - 10;
        if (leftX >= MARGIN) {
            return leftX;
        }

        return Math.max(MARGIN, (screen.width - panelWidth) / 2);
    }

    private static void drawPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, BORDER_COLOR);
        guiGraphics.fill(x, y, x + width, y + height, OUTER_COLOR);
        guiGraphics.fill(x + 8, y + 20, x + width - 8, y + height - TEXT_AREA_BOTTOM_OFFSET, INNER_COLOR);
    }

    private static void drawText(
            GuiProgrammer screen,
            GuiGraphics guiGraphics,
            Font font,
            PieceConstantString piece,
            PanelLayout layout
    ) {
        String value = piece.getValue();
        Component title = Component.translatable(piece.getUnlocalizedName());
        String count = value.length() + "/" + StringSpellHelper.MAX_STRING_LENGTH;

        guiGraphics.drawString(font, title, layout.x + 8, layout.y + 7, TEXT_COLOR, false);
        guiGraphics.drawString(font, count, layout.x + layout.width - 8 - font.width(count), layout.y + 7, COUNT_COLOR, false);

        if (value.isEmpty()) {
            scrollLineOffset = 0;
            Component emptyText = Component.translatable("psitweaks.gui.string_constant_input.empty");
            guiGraphics.drawString(font, emptyText, layout.textX(), layout.textY(), MUTED_TEXT_COLOR, false);
            drawCursor(screen, guiGraphics, layout.textX(), layout.textY());
        } else {
            List<TextLine> lines = splitLines(font, value, layout.textWidth());
            int cursorLine = cursorLine(lines, piece.getCursorPosition());
            int firstLine = visibleFirstLine(lines, cursorLine);
            int textY = layout.textY();
            for (int i = firstLine; i < Math.min(lines.size(), firstLine + MAX_LINES); i++) {
                TextLine line = lines.get(i);
                drawSelection(guiGraphics, font, value, line, layout.textX(), textY);
                guiGraphics.drawString(font, line.text, layout.textX(), textY, TEXT_COLOR, false);
                if (i == cursorLine) {
                    int cursorX = layout.textX()
                            + font.width(value.substring(line.start, piece.getCursorPosition()));
                    drawCursor(screen, guiGraphics, cursorX, textY);
                }
                textY += font.lineHeight;
            }
        }

        Component hint = Component.translatable(screen.isSpectator()
                ? "psitweaks.gui.string_constant_input.read_only"
                : "psitweaks.gui.string_constant_input.hint");
        guiGraphics.drawString(font, hint, layout.x + 8, layout.y + layout.height - 12, MUTED_TEXT_COLOR, false);
        drawActionButtons(screen, guiGraphics, font, layout);
    }

    private static void drawActionButtons(GuiProgrammer screen, GuiGraphics guiGraphics, Font font, PanelLayout layout) {
        for (ActionButton actionButton : ActionButton.values()) {
            drawActionButton(screen, guiGraphics, font, layout, actionButton);
        }
    }

    private static void drawActionButton(
            GuiProgrammer screen,
            GuiGraphics guiGraphics,
            Font font,
            PanelLayout layout,
            ActionButton actionButton
    ) {
        int x = actionButtonX(layout, actionButton);
        int y = actionButtonY(layout);
        boolean disabled = screen.isSpectator() && actionButton.requiresEditing;
        int fillColor = disabled ? BUTTON_DISABLED_COLOR : BUTTON_COLOR;
        int labelColor = disabled ? MUTED_TEXT_COLOR : TEXT_COLOR;
        Component label = Component.translatable(actionButton.translationKey);

        guiGraphics.fill(x - 1, y - 1, x + ACTION_BUTTON_WIDTH + 1, y + ACTION_BUTTON_HEIGHT + 1, BORDER_COLOR);
        guiGraphics.fill(x, y, x + ACTION_BUTTON_WIDTH, y + ACTION_BUTTON_HEIGHT, fillColor);
        guiGraphics.drawString(font, label,
                x + Math.max(2, (ACTION_BUTTON_WIDTH - font.width(label)) / 2),
                y + 3,
                labelColor,
                false);
    }

    private static void drawCursor(GuiProgrammer screen, GuiGraphics guiGraphics, int x, int y) {
        if (!screen.isSpectator() && shouldShowCursor()) {
            guiGraphics.fill(x, y - 1, x + 1, y + Minecraft.getInstance().font.lineHeight, TEXT_COLOR);
        }
    }

    private static boolean shouldShowCursor() {
        return (System.currentTimeMillis() / 500L) % 2L == 0L;
    }

    private static void drawSelection(
            GuiGraphics guiGraphics,
            Font font,
            String value,
            TextLine line,
            int x,
            int y
    ) {
        if (!hasSelection()) {
            return;
        }

        int selectedStart = selectionStart();
        int selectedEnd = selectionEnd();
        if (line.start == line.end) {
            if (selectedStart <= line.start && selectedEnd > line.start) {
                guiGraphics.fill(x, y - 1, x + Math.max(1, font.width(" ")), y + font.lineHeight, SELECTION_COLOR);
            }
            return;
        }

        int start = Math.max(line.start, selectedStart);
        int end = Math.min(line.end, selectedEnd);
        if (start >= end) {
            return;
        }

        int startX = x + font.width(value.substring(line.start, start));
        int endX = x + font.width(value.substring(line.start, end));
        guiGraphics.fill(startX, y - 1, Math.max(startX + 1, endX), y + font.lineHeight, SELECTION_COLOR);
    }

    private static int cursorPositionForClick(
            Font font,
            PieceConstantString piece,
            PanelLayout layout,
            double mouseX,
            double mouseY
    ) {
        String value = piece.getValue();
        if (value.isEmpty()) {
            return 0;
        }

        List<TextLine> lines = splitLines(font, value, layout.textWidth());
        int firstLine = clampScrollLineOffset(lines, scrollLineOffset);
        scrollLineOffset = firstLine;
        int visibleLine = Math.max(0, Math.min(MAX_LINES - 1, (int) ((mouseY - layout.textY()) / font.lineHeight)));
        int lineIndex = Math.max(0, Math.min(lines.size() - 1, firstLine + visibleLine));
        TextLine line = lines.get(lineIndex);
        int relativeX = (int) Math.max(0, mouseX - layout.textX());

        for (int index = line.start; index < line.end; index++) {
            int before = font.width(value.substring(line.start, index));
            int characterWidth = font.width(value.substring(index, index + 1));
            if (relativeX < before + characterWidth / 2) {
                return index;
            }
        }
        return line.end;
    }

    private static void moveCursorVertically(
            GuiProgrammer screen,
            PieceConstantString piece,
            int direction,
            boolean selecting
    ) {
        String value = piece.getValue();
        if (value.isEmpty()) {
            moveCursorTo(piece, 0, selecting);
            return;
        }

        Font font = Minecraft.getInstance().font;
        List<TextLine> lines = splitLines(font, value, layoutFor(screen).textWidth());
        int currentLineIndex = cursorLine(lines, piece.getCursorPosition());
        int targetLineIndex = Math.max(0, Math.min(lines.size() - 1, currentLineIndex + direction));
        TextLine currentLine = lines.get(currentLineIndex);
        TextLine targetLine = lines.get(targetLineIndex);
        int targetX = font.width(value.substring(currentLine.start, piece.getCursorPosition()));
        moveCursorTo(piece, cursorPositionForLineX(font, value, targetLine, targetX), selecting);
    }

    private static void moveCursorHorizontally(
            PieceConstantString piece,
            int keyCode,
            boolean selecting,
            boolean movingByWord
    ) {
        int cursorPosition = piece.getCursorPosition();
        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            moveCursorTo(piece, movingByWord ? previousWordPosition(piece.getValue(), cursorPosition) : cursorPosition - 1,
                    selecting);
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            moveCursorTo(piece, movingByWord ? nextWordPosition(piece.getValue(), cursorPosition) : cursorPosition + 1,
                    selecting);
        } else if (keyCode == GLFW.GLFW_KEY_HOME) {
            moveCursorTo(piece, 0, selecting);
        } else if (keyCode == GLFW.GLFW_KEY_END) {
            moveCursorTo(piece, piece.getValue().length(), selecting);
        }
    }

    private static int previousWordPosition(String value, int cursorPosition) {
        int index = Math.max(0, Math.min(cursorPosition, value.length()));
        if (index <= 0) {
            return 0;
        }

        index--;
        while (index > 0 && Character.isWhitespace(value.charAt(index))) {
            index--;
        }

        boolean word = isWordCharacter(value.charAt(index));
        while (index > 0 && isSameWordClass(value.charAt(index - 1), word)) {
            index--;
        }
        return index;
    }

    private static int nextWordPosition(String value, int cursorPosition) {
        int index = Math.max(0, Math.min(cursorPosition, value.length()));
        int length = value.length();
        while (index < length && Character.isWhitespace(value.charAt(index))) {
            index++;
        }
        if (index >= length) {
            return length;
        }

        boolean word = isWordCharacter(value.charAt(index));
        while (index < length && isSameWordClass(value.charAt(index), word)) {
            index++;
        }
        return index;
    }

    private static boolean isSameWordClass(char character, boolean word) {
        return !Character.isWhitespace(character) && isWordCharacter(character) == word;
    }

    private static boolean isWordCharacter(char character) {
        return Character.isLetterOrDigit(character)
                || character == '_'
                || character == ':'
                || character == '-'
                || character == '.'
                || character == '/';
    }

    private static void moveCursorTo(PieceConstantString piece, int cursorPosition, boolean selecting) {
        int previousCursor = piece.getCursorPosition();
        if (selecting) {
            if (selectionAnchor < 0) {
                selectionAnchor = previousCursor;
            }
        } else {
            clearSelection();
        }

        piece.moveCursorTo(cursorPosition);
        rememberCursor(piece);
        if (selecting && !hasSelection()) {
            clearSelection();
        }
    }

    private static boolean replaceSelectionOrInsert(
            GuiProgrammer screen,
            PieceConstantString piece,
            String input,
            boolean consumeWhenUnchanged
    ) {
        if (screen.isSpectator()) {
            return true;
        }

        int start = hasSelection() ? selectionStart() : piece.getCursorPosition();
        int end = hasSelection() ? selectionEnd() : piece.getCursorPosition();
        if (!piece.replaceRange(start, end, input, false, consumeWhenUnchanged)) {
            return consumeWhenUnchanged;
        }

        screen.pushState(true);
        piece.replaceRange(start, end, input, true, consumeWhenUnchanged);
        screen.onSpellChanged(false);
        rememberCursor(piece);
        clearSelection();
        return true;
    }

    private static boolean deleteSelection(GuiProgrammer screen, PieceConstantString piece) {
        if (!hasSelection()) {
            return false;
        }

        int start = selectionStart();
        int end = selectionEnd();
        if (!piece.deleteRange(start, end, false)) {
            return false;
        }

        screen.pushState(true);
        piece.deleteRange(start, end, true);
        screen.onSpellChanged(false);
        rememberCursor(piece);
        clearSelection();
        return true;
    }

    private static void handleActionButton(GuiProgrammer screen, PieceConstantString piece, ActionButton actionButton) {
        switch (actionButton) {
            case COPY_ALL -> copyAllText(piece);
            case CLEAR_ALL -> replaceAllText(screen, piece, "");
            case REPLACE_ALL -> replaceAllText(screen, piece, Minecraft.getInstance().keyboardHandler.getClipboard());
        }
    }

    private static void copyAllText(PieceConstantString piece) {
        Minecraft.getInstance().keyboardHandler.setClipboard(piece.getValue());
    }

    private static void replaceAllText(GuiProgrammer screen, PieceConstantString piece, String input) {
        if (screen.isSpectator()) {
            return;
        }

        String sanitized = StringSpellHelper.sanitize(input);
        if (piece.getValue().equals(sanitized)) {
            piece.moveCursorTo(sanitized.length());
            rememberCursor(piece);
            clearSelection();
            return;
        }

        screen.pushState(true);
        piece.replaceValue(input);
        screen.onSpellChanged(false);
        rememberCursor(piece);
        clearSelection();
    }

    private static boolean handleEditingKey(GuiProgrammer screen, PieceConstantString piece, int keyCode, int scanCode) {
        if (screen.isSpectator()) {
            return isContentEditingKey(keyCode);
        }

        if (Screen.isPaste(keyCode)) {
            return replaceSelectionOrInsert(screen, piece, Minecraft.getInstance().keyboardHandler.getClipboard(), true);
        }

        if ((keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) && hasSelection()) {
            return deleteSelection(screen, piece);
        }

        if (!isContentEditingKey(keyCode) || !piece.onKeyPressed(keyCode, scanCode, false)) {
            return false;
        }

        screen.pushState(true);
        piece.onKeyPressed(keyCode, scanCode, true);
        screen.onSpellChanged(false);
        rememberCursor(piece);
        return true;
    }

    private static boolean isContentEditingKey(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_BACKSPACE
                || keyCode == GLFW.GLFW_KEY_DELETE
                || Screen.isPaste(keyCode);
    }

    private static void applyActiveCursor(PieceConstantString piece) {
        if (activeCursorPosition < 0) {
            activeCursorPosition = piece.getValue().length();
        }

        int clamped = clampCursor(piece, activeCursorPosition);
        if (selectionAnchor >= 0) {
            selectionAnchor = clampCursor(piece, selectionAnchor);
        }
        if (!piece.isCursorEditing() || piece.getCursorPosition() != clamped) {
            piece.moveCursorTo(clamped);
        }
        activeCursorPosition = clamped;
    }

    private static void rememberCursor(PieceConstantString piece) {
        activeCursorPosition = piece.getCursorPosition();
        scrollFollowsCursor = true;
    }

    private static int clampCursor(PieceConstantString piece, int cursorPosition) {
        return Math.max(0, Math.min(cursorPosition, piece.getValue().length()));
    }

    private static boolean hasSelection() {
        return selectionAnchor >= 0 && selectionAnchor != activeCursorPosition;
    }

    private static int selectionStart() {
        return Math.min(selectionAnchor, activeCursorPosition);
    }

    private static int selectionEnd() {
        return Math.max(selectionAnchor, activeCursorPosition);
    }

    private static void clearSelection() {
        selectionAnchor = -1;
    }

    private static void selectAll(PieceConstantString piece) {
        activeCursorPosition = piece.getValue().length();
        piece.moveCursorTo(activeCursorPosition);
        selectionAnchor = activeCursorPosition == 0 ? -1 : 0;
        scrollFollowsCursor = true;
    }

    private static void copySelection(PieceConstantString piece) {
        if (hasSelection()) {
            Minecraft.getInstance().keyboardHandler.setClipboard(
                    piece.getValue().substring(selectionStart(), selectionEnd())
            );
        }
    }

    private static void cutSelection(GuiProgrammer screen, PieceConstantString piece) {
        copySelection(piece);
        if (!screen.isSpectator()) {
            deleteSelection(screen, piece);
        }
    }

    private static int cursorPositionForLineX(Font font, String value, TextLine line, int targetX) {
        for (int index = line.start; index < line.end; index++) {
            int before = font.width(value.substring(line.start, index));
            int characterWidth = font.width(value.substring(index, index + 1));
            if (targetX < before + characterWidth / 2) {
                return index;
            }
        }
        return line.end;
    }

    private static boolean isHorizontalCursorKey(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_LEFT
                || keyCode == GLFW.GLFW_KEY_RIGHT
                || keyCode == GLFW.GLFW_KEY_HOME
                || keyCode == GLFW.GLFW_KEY_END;
    }

    private static ActionButton actionButtonAt(PanelLayout layout, double mouseX, double mouseY) {
        int y = actionButtonY(layout);
        if (mouseY < y || mouseY >= y + ACTION_BUTTON_HEIGHT) {
            return null;
        }

        for (ActionButton actionButton : ActionButton.values()) {
            int x = actionButtonX(layout, actionButton);
            if (mouseX >= x && mouseX < x + ACTION_BUTTON_WIDTH) {
                return actionButton;
            }
        }
        return null;
    }

    private static int actionButtonX(PanelLayout layout, ActionButton actionButton) {
        int totalWidth = ActionButton.values().length * ACTION_BUTTON_WIDTH
                + (ActionButton.values().length - 1) * ACTION_BUTTON_GAP;
        return layout.x + layout.width - 8 - totalWidth
                + actionButton.ordinal() * (ACTION_BUTTON_WIDTH + ACTION_BUTTON_GAP);
    }

    private static int actionButtonY(PanelLayout layout) {
        return layout.y + layout.height - ACTION_BUTTON_BOTTOM_OFFSET;
    }

    private static List<TextLine> splitLines(Font font, String value, int width) {
        List<TextLine> lines = new ArrayList<>();
        int start = 0;
        while (start < value.length()) {
            int newline = value.indexOf('\n', start);
            if (newline == start) {
                lines.add(new TextLine(start, start, ""));
                start++;
                continue;
            }

            int end = start + 1;
            int lastFit = start;
            int lineEnd = newline == -1 ? value.length() : newline;
            while (end <= lineEnd && font.width(value.substring(start, end)) <= width) {
                lastFit = end;
                end++;
            }

            if (lastFit == start) {
                lastFit = start + 1;
            }

            lines.add(new TextLine(start, lastFit, value.substring(start, lastFit)));
            start = lastFit == newline ? newline + 1 : lastFit;
        }
        if (value.endsWith("\n")) {
            lines.add(new TextLine(value.length(), value.length(), ""));
        }
        return lines;
    }

    private static int cursorLine(List<TextLine> lines, int cursorPosition) {
        for (int i = 0; i < lines.size(); i++) {
            TextLine line = lines.get(i);
            if (cursorPosition >= line.start && cursorPosition <= line.end) {
                return i;
            }
        }
        return Math.max(0, lines.size() - 1);
    }

    private static int visibleFirstLine(List<TextLine> lines, int cursorLine) {
        int firstLine = clampScrollLineOffset(lines, scrollLineOffset);
        if (scrollFollowsCursor) {
            firstLine = firstVisibleLineForCursor(lines, cursorLine, firstLine);
        }
        scrollLineOffset = firstLine;
        return firstLine;
    }

    private static int firstVisibleLineForCursor(List<TextLine> lines, int cursorLine, int firstLine) {
        if (cursorLine < firstLine) {
            return cursorLine;
        }
        if (cursorLine >= firstLine + MAX_LINES) {
            return cursorLine - MAX_LINES + 1;
        }
        return clampScrollLineOffset(lines, firstLine);
    }

    private static int clampScrollLineOffset(List<TextLine> lines, int firstLine) {
        int maxFirstLine = Math.max(0, lines.size() - MAX_LINES);
        return Math.max(0, Math.min(maxFirstLine, firstLine));
    }

    private record GridPosition(int x, int y) {
    }

    private record TextLine(int start, int end, String text) {
    }

    private enum ActionButton {
        COPY_ALL("psitweaks.gui.string_constant_input.button.copy_all", false),
        CLEAR_ALL("psitweaks.gui.string_constant_input.button.clear_all", true),
        REPLACE_ALL("psitweaks.gui.string_constant_input.button.replace_all", true);

        private final String translationKey;
        private final boolean requiresEditing;

        ActionButton(String translationKey, boolean requiresEditing) {
            this.translationKey = translationKey;
            this.requiresEditing = requiresEditing;
        }
    }

    private record PanelLayout(int x, int y, int width, int height) {
        private int textX() {
            return x + TEXT_X_OFFSET;
        }

        private int textY() {
            return y + TEXT_Y_OFFSET;
        }

        private int textWidth() {
            return width - TEXT_WIDTH_INSET;
        }

        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }

        private boolean containsTextArea(double mouseX, double mouseY) {
            return mouseX >= x + TEXT_AREA_LEFT_OFFSET
                    && mouseX < x + width - TEXT_AREA_LEFT_OFFSET
                    && mouseY >= y + TEXT_AREA_TOP_OFFSET
                    && mouseY < y + height - TEXT_AREA_BOTTOM_OFFSET;
        }
    }
}
