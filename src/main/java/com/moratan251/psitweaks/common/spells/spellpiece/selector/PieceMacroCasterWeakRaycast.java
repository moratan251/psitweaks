package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.spellpiece.operator.RaycastHelper;
import vazkii.psi.api.spell.Spell;

public class PieceMacroCasterWeakRaycast extends PieceMacroCasterRaycastBase {
    public PieceMacroCasterWeakRaycast(Spell spell) {
        super(spell, RaycastHelper.Mode.WEAK, false);
    }
}

