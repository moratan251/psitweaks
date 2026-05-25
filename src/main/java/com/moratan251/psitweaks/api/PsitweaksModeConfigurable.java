package com.moratan251.psitweaks.api;

import java.util.List;

/**
 * Implement on spell pieces that expose a Psitweaks mode-selection menu.
 */
public interface PsitweaksModeConfigurable {
    PsitweaksModeOption getModeOption();

    void setModeOption(PsitweaksModeOption mode);

    default List<PsitweaksModeOption> getAvailableModeOptions() {
        return PsitweaksModeOptions.snapshot();
    }

    default PsitweaksModeOption normalizeModeOption(PsitweaksModeOption mode) {
        List<PsitweaksModeOption> modes = getAvailableModeOptions();
        if (modes == null || modes.isEmpty()) {
            return PsitweaksModeOptions.STRING;
        }

        if (mode != null) {
            for (PsitweaksModeOption availableMode : modes) {
                if (availableMode.id().equals(mode.id())) {
                    return availableMode;
                }
            }
        }
        return modes.get(0);
    }

    default void cycleModeOption() {
        List<PsitweaksModeOption> modes = getAvailableModeOptions();
        if (modes == null || modes.isEmpty()) {
            return;
        }

        PsitweaksModeOption currentMode = getModeOption();
        for (int i = 0; i < modes.size(); i++) {
            if (currentMode != null && modes.get(i).id().equals(currentMode.id())) {
                setModeOption(modes.get((i + 1) % modes.size()));
                return;
            }
        }
        setModeOption(modes.get(0));
    }
}
