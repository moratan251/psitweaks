package com.moratan251.psitweaks.common.spells;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import vazkii.psi.api.spell.EnumPieceType;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellPiece;

public class PieceConstantString extends SpellPiece {
    private static final String TAG_VALUE = "value";
    private static final int MAX_RENDER_WIDTH = 16;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int PACKED_LIGHT = 15728880;

    private String value = "";
    private int cursorPosition = 0;
    private boolean cursorEditing = false;

    public PieceConstantString(Spell spell) {
        super(spell);
    }

    @Override
    public EnumPieceType getPieceType() {
        return EnumPieceType.CONSTANT;
    }

    @Override
    public Class<?> getEvaluationType() {
        return String.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.string");
    }

    @Override
    public Object evaluate() {
        return value;
    }

    @Override
    public Object execute(SpellContext context) {
        return value;
    }

    @Override
    public void drawAdditional(PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        String display = displayValue();
        if (display.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        while (font.width(display) > MAX_RENDER_WIDTH && display.length() > 1) {
            display = display.substring(0, display.length() - 1);
        }

        int width = font.width(display);
        poseStack.pushPose();
        poseStack.translate(9F - width / 2F, 4F, 0F);
        font.drawInBatch(display, 0F, 0F, TEXT_COLOR, false, poseStack.last().pose(), bufferSource,
                Font.DisplayMode.NORMAL, 0, PACKED_LIGHT);
        poseStack.popPose();
    }

    @Override
    public boolean interceptKeystrokes() {
        return true;
    }

    @Override
    public boolean onCharTyped(char character, int keyCode, boolean modify) {
        return insertText(String.valueOf(character), modify, false);
    }

    @Override
    public boolean onKeyPressed(int keyCode, int scanCode, boolean modify) {
        if (cursorEditing
                && Screen.hasShiftDown()
                && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
            return insertText("\n", modify, true);
        }

        if (Screen.isPaste(keyCode)) {
            return pasteClipboard(modify);
        }

        if (cursorEditing) {
            if (keyCode == GLFW.GLFW_KEY_LEFT) {
                if (modify) {
                    moveCursorTo(cursorPosition - 1);
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                if (modify) {
                    moveCursorTo(cursorPosition + 1);
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_HOME) {
                if (modify) {
                    moveCursorTo(0);
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_END) {
                if (modify) {
                    moveCursorTo(value.length());
                }
                return true;
            }
        }

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            return deleteBeforeCursor(modify);
        }

        if (cursorEditing && keyCode == GLFW.GLFW_KEY_DELETE) {
            return deleteAfterCursor(modify);
        }

        return false;
    }

    private boolean insertText(String input, boolean modify, boolean consumeWhenUnchanged) {
        String sanitized = StringSpellHelper.sanitize(input);
        if (sanitized.isEmpty()) {
            return consumeWhenUnchanged;
        }

        int available = StringSpellHelper.MAX_STRING_LENGTH - value.length();
        if (available <= 0) {
            return consumeWhenUnchanged;
        }

        if (sanitized.length() > available) {
            sanitized = sanitized.substring(0, available);
        }

        int cursor = inputCursor();
        String updated = value.substring(0, cursor) + sanitized + value.substring(cursor);
        if (modify) {
            value = updated;
            cursorPosition = cursor + sanitized.length();
        }
        return true;
    }

    private boolean deleteBeforeCursor(boolean modify) {
        int cursor = inputCursor();
        if (cursor <= 0) {
            return cursorEditing;
        }

        if (modify) {
            value = value.substring(0, cursor - 1) + value.substring(cursor);
            cursorPosition = cursor - 1;
        }
        return true;
    }

    private boolean deleteAfterCursor(boolean modify) {
        int cursor = getCursorPosition();
        if (cursor >= value.length()) {
            return true;
        }

        if (modify) {
            value = value.substring(0, cursor) + value.substring(cursor + 1);
            cursorPosition = cursor;
        }
        return true;
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putString(TAG_VALUE, value);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        value = StringSpellHelper.sanitize(tag.getString(TAG_VALUE));
        cursorPosition = value.length();
        cursorEditing = false;
    }

    public String getValue() {
        return value;
    }

    public int getCursorPosition() {
        cursorPosition = Math.max(0, Math.min(cursorPosition, value.length()));
        return cursorPosition;
    }

    public void moveCursorTo(int position) {
        cursorEditing = true;
        cursorPosition = Math.max(0, Math.min(position, value.length()));
    }

    public boolean insertLineBreak(boolean modify) {
        return insertText("\n", modify, true);
    }

    public boolean replaceRange(int start, int end, String input, boolean modify, boolean consumeWhenUnchanged) {
        int rangeStart = clampCursorPosition(Math.min(start, end));
        int rangeEnd = clampCursorPosition(Math.max(start, end));
        String sanitized = StringSpellHelper.sanitize(input);
        if (sanitized.isEmpty()) {
            return consumeWhenUnchanged;
        }

        int available = StringSpellHelper.MAX_STRING_LENGTH - (value.length() - (rangeEnd - rangeStart));
        if (available <= 0) {
            return consumeWhenUnchanged;
        }

        if (sanitized.length() > available) {
            sanitized = sanitized.substring(0, available);
        }

        if (modify) {
            value = value.substring(0, rangeStart) + sanitized + value.substring(rangeEnd);
            cursorEditing = true;
            cursorPosition = rangeStart + sanitized.length();
        }
        return true;
    }

    public boolean deleteRange(int start, int end, boolean modify) {
        int rangeStart = clampCursorPosition(Math.min(start, end));
        int rangeEnd = clampCursorPosition(Math.max(start, end));
        if (rangeStart == rangeEnd) {
            return false;
        }

        if (modify) {
            value = value.substring(0, rangeStart) + value.substring(rangeEnd);
            cursorEditing = true;
            cursorPosition = rangeStart;
        }
        return true;
    }

    public void replaceValue(String input) {
        value = StringSpellHelper.sanitize(input);
        cursorEditing = true;
        cursorPosition = value.length();
    }

    public void setCursorEditing(boolean cursorEditing) {
        this.cursorEditing = cursorEditing;
        if (!cursorEditing) {
            cursorPosition = value.length();
        } else {
            getCursorPosition();
        }
    }

    public boolean isCursorEditing() {
        return cursorEditing;
    }

    private String displayValue() {
        if (value.isEmpty()) {
            return "\"\"";
        }
        return value.replace('\n', ' ');
    }

    private boolean pasteClipboard(boolean modify) {
        String clipboardValue = Minecraft.getInstance().keyboardHandler.getClipboard();
        return insertText(clipboardValue, modify, true);
    }

    private int inputCursor() {
        return cursorEditing ? getCursorPosition() : value.length();
    }

    private int clampCursorPosition(int position) {
        return Math.max(0, Math.min(position, value.length()));
    }
}
