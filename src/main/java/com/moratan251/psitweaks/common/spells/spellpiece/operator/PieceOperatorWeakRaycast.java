package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import vazkii.psi.api.spell.Spell;

public class PieceOperatorWeakRaycast extends PieceOperatorRaycastBase {
    public PieceOperatorWeakRaycast(Spell spell) {
        super(spell, RaycastHelper.Mode.WEAK, false);
    }
}
