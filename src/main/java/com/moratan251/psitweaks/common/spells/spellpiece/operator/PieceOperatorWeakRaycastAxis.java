package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import vazkii.psi.api.spell.Spell;

public class PieceOperatorWeakRaycastAxis extends PieceOperatorRaycastBase {
    public PieceOperatorWeakRaycastAxis(Spell spell) {
        super(spell, RaycastHelper.Mode.WEAK, true);
    }
}

