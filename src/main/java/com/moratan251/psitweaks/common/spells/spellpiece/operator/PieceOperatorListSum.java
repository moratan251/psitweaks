package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import vazkii.psi.api.spell.Spell;

/** The sum of a Number List, or of a Vector List as a Vector; an empty List gives 0. */
public class PieceOperatorListSum extends PieceOperatorListAggregateBase {
    public PieceOperatorListSum(Spell spell) {
        super(spell);
    }

    @Override
    protected double finish(double sum, int count) {
        return sum;
    }
}
