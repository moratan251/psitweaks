package com.moratan251.psitweaks.common.spells.mode;

import com.moratan251.psitweaks.api.PsitweaksModeConfigurable;

public interface ModeConfigurableSpellPiece extends PsitweaksModeConfigurable {
    default void cycleElementMode() {
        cycleModeOption();
    }
}
