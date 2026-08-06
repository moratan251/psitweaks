package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import vazkii.psi.api.spell.Spell;

public class PieceOperatorStrongRaycastAxis extends PieceOperatorRaycastBase {
    public PieceOperatorStrongRaycastAxis(Spell spell) {
        super(spell, RaycastHelper.Mode.STRONG, true);
    }
}
