package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.spellpiece.operator.RaycastHelper;
import vazkii.psi.api.spell.Spell;

public class PieceMacroCasterAxisRaycast extends PieceMacroCasterRaycastBase {
    public PieceMacroCasterAxisRaycast(Spell spell) {
        super(spell, RaycastHelper.Mode.NORMAL, true);
    }
}

