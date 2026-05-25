package com.moratan251.psitweaks.common.spells.mode;

import com.moratan251.psitweaks.api.PsitweaksModeOption;
import com.moratan251.psitweaks.api.PsitweaksModeConfigurable;
import java.util.Objects;

public interface ModeConfigurableSpellPiece extends PsitweaksModeConfigurable {
    default ListElementMode getElementMode() {
        return ListElementMode.fromOption(getModeOption(), ListElementMode.STRING);
    }

    default void setElementMode(ListElementMode mode) {
        setModeOption((mode == null ? ListElementMode.STRING : mode).option());
    }

    default ListElementMode[] getAvailableElementModes() {
        return getAvailableModeOptions().stream()
                .map(ListElementMode::fromOption)
                .filter(Objects::nonNull)
                .toArray(ListElementMode[]::new);
    }

    default ListElementMode normalizeElementMode(ListElementMode mode) {
        return ListElementMode.fromOption(normalizeModeOption(mode == null ? null : mode.option()),
                ListElementMode.STRING);
    }

    default void cycleElementMode() {
        cycleModeOption();
    }
}
