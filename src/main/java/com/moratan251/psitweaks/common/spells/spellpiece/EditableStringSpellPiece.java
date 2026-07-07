package com.moratan251.psitweaks.common.spells.spellpiece;

public interface EditableStringSpellPiece {
    String getValue();

    int getCursorPosition();

    void moveCursorTo(int position);

    boolean replaceRange(int start, int end, String input, boolean modify, boolean consumeWhenUnchanged);

    boolean deleteRange(int start, int end, boolean modify);

    void replaceValue(String input);

    void setCursorEditing(boolean cursorEditing);

    boolean isCursorEditing();

    boolean onKeyPressed(int keyCode, int scanCode, boolean modify);

    String getUnlocalizedName();
}
