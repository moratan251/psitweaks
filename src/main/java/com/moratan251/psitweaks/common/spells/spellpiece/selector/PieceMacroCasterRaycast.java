package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.spellpiece.operator.RaycastHelper;
import vazkii.psi.api.spell.Spell;

public class PieceMacroCasterRaycast extends PieceMacroCasterRaycastBase {
    public PieceMacroCasterRaycast(Spell spell) {
        super(spell, RaycastHelper.Mode.NORMAL, false);
    }
}
