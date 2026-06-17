package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import vazkii.psi.api.spell.Spell;

public class PieceOperatorListSearchExclude extends PieceOperatorListStringFilterBase {
    public PieceOperatorListSearchExclude(Spell spell) {
        super(spell);
    }

    @Override
    protected boolean shouldKeep(boolean matches) {
        return !matches;
    }
}
