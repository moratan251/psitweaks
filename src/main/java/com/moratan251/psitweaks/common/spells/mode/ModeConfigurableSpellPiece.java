package com.moratan251.psitweaks.common.spells.mode;

public interface ModeConfigurableSpellPiece {
    ListElementMode getElementMode();

    void setElementMode(ListElementMode mode);

    default void cycleElementMode() {
        setElementMode(getElementMode().next());
    }
}
