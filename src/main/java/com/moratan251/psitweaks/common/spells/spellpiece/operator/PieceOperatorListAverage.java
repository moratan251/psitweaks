package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellRuntimeException;

/** The mean of a Number List, or the centroid of a Vector List; an empty List is an error. */
public class PieceOperatorListAverage extends PieceOperatorListAggregateBase {
    public PieceOperatorListAverage(Spell spell) {
        super(spell);
    }

    @Override
    protected double finish(double sum, int count) throws SpellRuntimeException {
        if (count == 0) {
            throw new SpellRuntimeException(ERROR_EMPTY_LIST);
        }
        return sum / count;
    }
}
