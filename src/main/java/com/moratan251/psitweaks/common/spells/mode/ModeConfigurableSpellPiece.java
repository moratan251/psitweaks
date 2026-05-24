package com.moratan251.psitweaks.common.spells.mode;

public interface ModeConfigurableSpellPiece {
    ListElementMode getElementMode();

    void setElementMode(ListElementMode mode);

    default ListElementMode[] getAvailableElementModes() {
        return ListElementMode.values();
    }

    default ListElementMode normalizeElementMode(ListElementMode mode) {
        ListElementMode[] modes = getAvailableElementModes();
        if (modes == null || modes.length == 0) {
            return ListElementMode.STRING;
        }

        for (ListElementMode availableMode : modes) {
            if (availableMode == mode) {
                return mode;
            }
        }
        return modes[0];
    }

    default void cycleElementMode() {
        ListElementMode[] modes = getAvailableElementModes();
        if (modes == null || modes.length == 0) {
            return;
        }

        ListElementMode currentMode = getElementMode();
        for (int i = 0; i < modes.length; i++) {
            if (modes[i] == currentMode) {
                setElementMode(modes[(i + 1) % modes.length]);
                return;
            }
        }
        setElementMode(modes[0]);
    }
}
