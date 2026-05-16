package com.moratan251.psitweaks.client.gui;

import com.moratan251.psitweaks.common.spells.PieceConstantString;
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
    private static final int PANEL_WIDTH = 260;
    private static final int PANEL_HEIGHT = 92;
    private static final int MARGIN = 8;
    private static final int MAX_LINES = 4;
    private static final int GRID_CELL_SIZE = 18;
    private static final int GRID_SIZE = 9;
    private static final int DOUBLE_CLICK_INTERVAL_MS = 500;
    private static final int TEXT_X_OFFSET = 11;
    private static final int TEXT_Y_OFFSET = 27;
    private static final int TEXT_WIDTH_INSET = 22;
    private static final int TEXT_AREA_LEFT_OFFSET = 8;
    private static final int TEXT_AREA_TOP_OFFSET = 20;
    private static final int TEXT_AREA_BOTTOM_OFFSET = 18;

    private static final int OUTER_COLOR = 0xF0100018;
    private static final int BORDER_COLOR = 0xFF7F1ED4;
    private static final int INNER_COLOR = 0xF0222024;
    private static final int TEXT_COLOR = 0xFFEDE8F8;
    private static final int MUTED_TEXT_COLOR = 0xFFB8ACC8;
    private static final int COUNT_COLOR = 0xFFCFA9FF;

    private static int activeX = -1;
    private static int activeY = -1;
    private static int lastClickX = -1;
    private static int lastClickY = -1;
    private static long lastClickTime = 0L;

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

        if (button == 0 && layout.containsTextArea(mouseX, mouseY)) {
            Font font = Minecraft.getInstance().font;
            piece.moveCursorTo(cursorPositionForClick(font, piece, layout, mouseX, mouseY));
        }
        return true;
    }

    public static boolean handleKeyPressedPre(GuiProgrammer screen, int keyCode, int scanCode) {
        PieceConstantString piece = getActiveSelectedStringConstant(screen);
        if (piece == null) {
            return false;
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (Screen.hasShiftDown()) {
                insertLineBreak(screen, piece);
            } else {
                deactivate(screen);
            }
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) {
            moveCursorVertically(screen, piece, keyCode == GLFW.GLFW_KEY_UP ? -1 : 1);
            return true;
        }

        if (isHorizontalCursorKey(keyCode)) {
            moveCursorHorizontally(piece, keyCode);
            return true;
        }
        return false;
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
    }

    private static void activate(PieceConstantString piece, int x, int y) {
        activeX = x;
        activeY = y;
        piece.setCursorEditing(true);
        piece.moveCursorTo(piece.getValue().length());
    }

    private static PieceConstantString getActiveSelectedStringConstant(GuiProgrammer screen) {
        if (activeX != GuiProgrammer.selectedX || activeY != GuiProgrammer.selectedY) {
            deactivate(screen);
            return null;
        }

        PieceConstantString piece = getStringConstant(screen, activeX, activeY);
        if (piece == null) {
            deactivate(screen);
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
        guiGraphics.fill(x + 8, y + 20, x + width - 8, y + height - 18, INNER_COLOR);
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
            Component emptyText = Component.translatable("psitweaks.gui.string_constant_input.empty");
            guiGraphics.drawString(font, emptyText, layout.textX(), layout.textY(), MUTED_TEXT_COLOR, false);
            drawCursor(screen, guiGraphics, layout.textX(), layout.textY());
        } else {
            List<TextLine> lines = splitLines(font, value, layout.textWidth());
            int cursorLine = cursorLine(lines, piece.getCursorPosition());
            int firstLine = firstVisibleLine(lines, cursorLine);
            int textY = layout.textY();
            for (int i = firstLine; i < Math.min(lines.size(), firstLine + MAX_LINES); i++) {
                guiGraphics.drawString(font, lines.get(i).text, layout.textX(), textY, TEXT_COLOR, false);
                if (i == cursorLine) {
                    int cursorX = layout.textX()
                            + font.width(value.substring(lines.get(i).start, piece.getCursorPosition()));
                    drawCursor(screen, guiGraphics, cursorX, textY);
                }
                textY += font.lineHeight;
            }
        }

        Component hint = Component.translatable(screen.isSpectator()
                ? "psitweaks.gui.string_constant_input.read_only"
                : "psitweaks.gui.string_constant_input.hint");
        guiGraphics.drawString(font, hint, layout.x + 8, layout.y + layout.height - 12, MUTED_TEXT_COLOR, false);
    }

    private static void drawCursor(GuiProgrammer screen, GuiGraphics guiGraphics, int x, int y) {
        if (!screen.isSpectator() && shouldShowCursor()) {
            guiGraphics.fill(x, y - 1, x + 1, y + Minecraft.getInstance().font.lineHeight, TEXT_COLOR);
        }
    }

    private static boolean shouldShowCursor() {
        return (System.currentTimeMillis() / 500L) % 2L == 0L;
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
        int cursorLine = cursorLine(lines, piece.getCursorPosition());
        int firstLine = firstVisibleLine(lines, cursorLine);
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

    private static void moveCursorVertically(GuiProgrammer screen, PieceConstantString piece, int direction) {
        String value = piece.getValue();
        if (value.isEmpty()) {
            piece.moveCursorTo(0);
            return;
        }

        Font font = Minecraft.getInstance().font;
        List<TextLine> lines = splitLines(font, value, layoutFor(screen).textWidth());
        int currentLineIndex = cursorLine(lines, piece.getCursorPosition());
        int targetLineIndex = Math.max(0, Math.min(lines.size() - 1, currentLineIndex + direction));
        TextLine currentLine = lines.get(currentLineIndex);
        TextLine targetLine = lines.get(targetLineIndex);
        int targetX = font.width(value.substring(currentLine.start, piece.getCursorPosition()));
        piece.moveCursorTo(cursorPositionForLineX(font, value, targetLine, targetX));
    }

    private static void moveCursorHorizontally(PieceConstantString piece, int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            piece.moveCursorTo(piece.getCursorPosition() - 1);
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            piece.moveCursorTo(piece.getCursorPosition() + 1);
        } else if (keyCode == GLFW.GLFW_KEY_HOME) {
            piece.moveCursorTo(0);
        } else if (keyCode == GLFW.GLFW_KEY_END) {
            piece.moveCursorTo(piece.getValue().length());
        }
    }

    private static void insertLineBreak(GuiProgrammer screen, PieceConstantString piece) {
        if (screen.isSpectator() || piece.getValue().length() >= StringSpellHelper.MAX_STRING_LENGTH) {
            return;
        }

        screen.pushState(true);
        piece.insertLineBreak(true);
        screen.onSpellChanged(false);
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

    private static int firstVisibleLine(List<TextLine> lines, int cursorLine) {
        int maxFirstLine = Math.max(0, lines.size() - MAX_LINES);
        return Math.max(0, Math.min(maxFirstLine, cursorLine - MAX_LINES + 1));
    }

    private record GridPosition(int x, int y) {
    }

    private record TextLine(int start, int end, String text) {
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
