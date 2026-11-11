package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import vazkii.psi.api.spell.Spell;

public class PieceOperatorListNameSearch extends PieceOperatorListNameFilterBase {
    public PieceOperatorListNameSearch(Spell spell) {
        super(spell);
    }

    @Override
    protected boolean shouldKeep(boolean matches) {
        return matches;
    }
}
