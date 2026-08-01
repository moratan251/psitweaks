package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.spellpiece.operator.RaycastHelper;
import vazkii.psi.api.spell.Spell;

public class PieceMacroCasterStrongAxisRaycast extends PieceMacroCasterRaycastBase {
    public PieceMacroCasterStrongAxisRaycast(Spell spell) {
        super(spell, RaycastHelper.Mode.STRONG, true);
    }
}
