package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moratan251.psitweaks.api.value.ContextualValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.spellpiece.EditableStringSpellPiece;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.DisplayNameTargetHelper;
import com.moratan251.psitweaks.common.spells.util.ModeStringConversionHelper;
import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.lwjgl.glfw.GLFW;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamAny;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorFormatString extends PieceOperator implements EditableStringSpellPiece {
    private static final String TAG_VALUE = "value";
    private static final int MAX_RENDER_WIDTH = 16;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int PACKED_LIGHT = 15728880;

    private SpellParam<?> value1;
    private SpellParam<?> value2;
    private SpellParam<?> value3;

    private String value = "";
    private int cursorPosition = 0;
    private boolean cursorEditing = false;

    public PieceOperatorFormatString(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(value1 = new ParamAny(PsitweaksSpellParams.VALUE1, SpellParam.GRAY, true));
        addParam(value2 = new ParamAny(PsitweaksSpellParams.VALUE2, SpellParam.GRAY, true));
        addParam(value3 = new ParamAny(PsitweaksSpellParams.VALUE3, SpellParam.GRAY, true));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        String result = value
                .replace("{1}", formatArgument(context, getParamValue(context, typed(value1))))
                .replace("{2}", formatArgument(context, getParamValue(context, typed(value2))))
                .replace("{3}", formatArgument(context, getParamValue(context, typed(value3))));
        return StringSpellHelper.sanitize(result);
    }

    private String formatArgument(SpellContext context, Object argument) throws SpellRuntimeException {
        if (argument == null) {
            return "";
        }
        if (argument instanceof Entity || argument instanceof ContextualValue) {
            return DisplayNameTargetHelper.getDisplayName(context, argument);
        }
        return ModeStringConversionHelper.anyToString(argument);
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
        String sanitized = StringSpellHelper.sanitize(input, StringSpellHelper.MAX_CONSTANT_STRING_LENGTH);
        if (sanitized.isEmpty()) {
            return consumeWhenUnchanged;
        }

        int available = StringSpellHelper.MAX_CONSTANT_STRING_LENGTH - value.length();
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
        value = StringSpellHelper.sanitize(tag.getString(TAG_VALUE), StringSpellHelper.MAX_CONSTANT_STRING_LENGTH);
        cursorPosition = value.length();
        cursorEditing = false;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public int getCursorPosition() {
        cursorPosition = Math.max(0, Math.min(cursorPosition, value.length()));
        return cursorPosition;
    }

    @Override
    public void moveCursorTo(int position) {
        cursorEditing = true;
        cursorPosition = Math.max(0, Math.min(position, value.length()));
    }

    @Override
    public boolean replaceRange(int start, int end, String input, boolean modify, boolean consumeWhenUnchanged) {
        int rangeStart = clampCursorPosition(Math.min(start, end));
        int rangeEnd = clampCursorPosition(Math.max(start, end));
        String sanitized = StringSpellHelper.sanitize(input, StringSpellHelper.MAX_CONSTANT_STRING_LENGTH);
        if (sanitized.isEmpty()) {
            return consumeWhenUnchanged;
        }

        int available = StringSpellHelper.MAX_CONSTANT_STRING_LENGTH - (value.length() - (rangeEnd - rangeStart));
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

    @Override
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

    @Override
    public void replaceValue(String input) {
        value = StringSpellHelper.sanitize(input, StringSpellHelper.MAX_CONSTANT_STRING_LENGTH);
        cursorEditing = true;
        cursorPosition = value.length();
    }

    @Override
    public void setCursorEditing(boolean cursorEditing) {
        this.cursorEditing = cursorEditing;
        if (!cursorEditing) {
            cursorPosition = value.length();
        } else {
            getCursorPosition();
        }
    }

    @Override
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

    @SuppressWarnings("unchecked")
    private static <T> SpellParam<T> typed(SpellParam<?> param) {
        return (SpellParam<T>) param;
    }
}
