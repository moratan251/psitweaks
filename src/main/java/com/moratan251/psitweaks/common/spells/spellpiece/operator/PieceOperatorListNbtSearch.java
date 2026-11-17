package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import vazkii.psi.api.spell.Spell;

public class PieceOperatorListNbtSearch extends PieceOperatorListNbtFilterBase {
    public PieceOperatorListNbtSearch(Spell spell) {
        super(spell);
    }

    @Override
    protected boolean shouldKeep(boolean matches) {
        return matches;
    }
}
