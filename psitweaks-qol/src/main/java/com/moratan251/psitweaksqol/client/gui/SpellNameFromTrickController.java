package com.moratan251.psitweaksqol.client.gui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import vazkii.psi.api.spell.EnumPieceType;
import vazkii.psi.api.spell.SpellGrid;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.client.gui.GuiProgrammer;

/** 作動式の名前を術式名にするボタンと、作動式が複数あるときに名前の元を選ぶモード。 */
public final class SpellNameFromTrickController {
    private static final int GRID_SIZE = 9;
    private static final int CELL_SIZE = 18;
    private static final int BUTTON_SPACING = 16;
    /** Psi の術式名欄 (GuiProgrammer#spellNameField) の最大文字数。 */
    private static final int SPELL_NAME_MAX_LENGTH = 20;
    private static final int DIM_COLOR = 0xA0000000;
    private static final int TRICK_BORDER_COLOR = 0xFF58FC20;
    private static final int HOVER_FILL_COLOR = 0x6058FC20;
    private static final int HINT_COLOR = 0xFF58FC20;

    private static boolean picking;

    private SpellNameFromTrickController() {
    }

    public static void onScreenInit(GuiProgrammer screen, Consumer<GuiEventListener> addListener) {
        picking = false;
        if (screen.isSpectator() || screen.helpButton == null) {
            return;
        }

        addListener.accept(new SpellNameFromTrickButton(
                screen.helpButton.getX(),
                screen.helpButton.getY() - BUTTON_SPACING,
                screen));
    }

    public static boolean isPicking(GuiProgrammer screen) {
        return picking && canUse(screen);
    }

    public static void render(GuiProgrammer screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!picking) {
            return;
        }

        Map<GridPosition, String> tricks = trickNames(screen);
        if (!canUse(screen) || tricks.isEmpty()) {
            deactivate();
            return;
        }

        GridPosition hovered = gridPositionAt(screen, mouseX, mouseY);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 300.0F);
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                GridPosition position = new GridPosition(x, y);
                int left = screen.gridLeft + x * CELL_SIZE - 1;
                int top = screen.gridTop + y * CELL_SIZE - 1;
                if (!tricks.containsKey(position)) {
                    guiGraphics.fill(left, top, left + CELL_SIZE, top + CELL_SIZE, DIM_COLOR);
                    continue;
                }

                if (position.equals(hovered)) {
                    guiGraphics.fill(left + 1, top + 1, left + CELL_SIZE - 1, top + CELL_SIZE - 1, HOVER_FILL_COLOR);
                }
                drawBorder(guiGraphics, left, top);
            }
        }

        Font font = Minecraft.getInstance().font;
        Component hint = Component.translatable("psitweaks_qol.gui.spell_name_from_trick.pick");
        guiGraphics.drawString(font, hint,
                screen.left + screen.xSize / 2 - font.width(hint) / 2,
                screen.top - 11,
                HINT_COLOR,
                true);
        guiGraphics.pose().popPose();
    }

    public static boolean handleMousePressedPre(GuiProgrammer screen, double mouseX, double mouseY, int button) {
        if (!picking) {
            return false;
        }
        if (!canUse(screen)) {
            deactivate();
            return false;
        }

        // 選択中のクリックは名前の決定か中止だけに使い、Psi 側の選択やドラッグには渡さない。
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            String name = trickNames(screen).get(gridPositionAt(screen, mouseX, mouseY));
            if (name != null) {
                ClientGuiSounds.playClick();
                applyName(screen, name);
            }
        }
        deactivate();
        return true;
    }

    public static boolean handleKeyPressedPre(GuiProgrammer screen, int keyCode) {
        if (picking && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            deactivate();
            return true;
        }
        return false;
    }

    public static void deactivate() {
        picking = false;
    }

    static void onButtonPressed(GuiProgrammer screen) {
        if (!canUse(screen)) {
            return;
        }

        List<String> names = distinctTrickNames(screen);
        if (names.isEmpty()) {
            return;
        }
        if (names.size() == 1) {
            applyName(screen, names.getFirst());
            return;
        }

        picking = true;
    }

    /** 術式中の作動式から作れる術式名を、グリッドの左上から行ごとに重複なしで返す。 */
    static List<String> distinctTrickNames(GuiProgrammer screen) {
        return new ArrayList<>(new LinkedHashSet<>(trickNames(screen).values()));
    }

    static boolean canUse(GuiProgrammer screen) {
        return screen.spell != null
                && screen.spellNameField != null
                && !screen.isSpectator()
                && !screen.commentEnabled
                && (screen.panelWidget == null || !screen.panelWidget.panelEnabled);
    }

    /** "作動式: 障壁" や "Trick: Barrier" のような名前から、種類の前置きを除いた部分を術式名にする。 */
    static String spellNameFromPieceName(String pieceName) {
        String stripped = ChatFormatting.stripFormatting(pieceName);
        String name = stripped == null ? "" : stripped.strip();
        int separator = indexOfSeparator(name);
        if (separator >= 0 && !name.substring(separator + 1).isBlank()) {
            name = name.substring(separator + 1).strip();
        }
        if (name.length() > SPELL_NAME_MAX_LENGTH) {
            name = name.substring(0, SPELL_NAME_MAX_LENGTH).strip();
        }
        return name;
    }

    private static Map<GridPosition, String> trickNames(GuiProgrammer screen) {
        Map<GridPosition, String> names = new LinkedHashMap<>();
        if (screen.spell == null) {
            return names;
        }

        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                SpellPiece piece = screen.spell.grid.gridData[x][y];
                if (piece == null || piece.getPieceType() != EnumPieceType.TRICK) {
                    continue;
                }

                String name = spellNameFromPieceName(I18n.get(piece.getUnlocalizedName()));
                if (!name.isEmpty()) {
                    names.put(new GridPosition(x, y), name);
                }
            }
        }
        return names;
    }

    private static void applyName(GuiProgrammer screen, String name) {
        if (name.equals(screen.spell.name)) {
            return;
        }

        // Ctrl+Z で戻せるように履歴に積む。戻したときの名前欄の表示は GuiProgrammerSpellNameSyncMixin が合わせる。
        screen.pushState(true);
        screen.spellNameField.setValue(name);
        screen.spell.name = screen.spellNameField.getValue();
        screen.onSpellChanged(true);
    }

    private static int indexOfSeparator(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ':' || c == '：') {
                return i;
            }
        }
        return -1;
    }

    private static GridPosition gridPositionAt(GuiProgrammer screen, double mouseX, double mouseY) {
        int x = (int) Math.floor((mouseX - screen.gridLeft) / CELL_SIZE);
        int y = (int) Math.floor((mouseY - screen.gridTop) / CELL_SIZE);
        if (!SpellGrid.exists(x, y)) {
            return null;
        }
        return new GridPosition(x, y);
    }

    private static void drawBorder(GuiGraphics guiGraphics, int left, int top) {
        guiGraphics.fill(left, top, left + CELL_SIZE, top + 1, TRICK_BORDER_COLOR);
        guiGraphics.fill(left, top + CELL_SIZE - 1, left + CELL_SIZE, top + CELL_SIZE, TRICK_BORDER_COLOR);
        guiGraphics.fill(left, top, left + 1, top + CELL_SIZE, TRICK_BORDER_COLOR);
        guiGraphics.fill(left + CELL_SIZE - 1, top, left + CELL_SIZE, top + CELL_SIZE, TRICK_BORDER_COLOR);
    }

    private record GridPosition(int x, int y) {
    }
}
