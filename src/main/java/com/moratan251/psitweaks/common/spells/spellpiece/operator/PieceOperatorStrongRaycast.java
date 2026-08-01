package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import vazkii.psi.api.spell.Spell;

public class PieceOperatorStrongRaycast extends PieceOperatorRaycastBase {
    public PieceOperatorStrongRaycast(Spell spell) {
        super(spell, RaycastHelper.Mode.STRONG, false);
    }
}
