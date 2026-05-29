package com.moratan251.psitweaks.common.spells.operator;

import vazkii.psi.api.spell.Spell;

public class PieceOperatorListSearch extends PieceOperatorListStringFilterBase {
    public PieceOperatorListSearch(Spell spell) {
        super(spell);
    }

    @Override
    protected boolean shouldKeep(boolean matches) {
        return matches;
    }
}
