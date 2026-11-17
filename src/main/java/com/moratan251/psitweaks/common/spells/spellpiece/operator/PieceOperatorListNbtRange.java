package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import vazkii.psi.api.spell.Spell;

/** Keeps the elements with a number between Min and Max at the path. */
public class PieceOperatorListNbtRange extends PieceOperatorListNbtRangeBase {
    public PieceOperatorListNbtRange(Spell spell) {
        super(spell);
    }

    @Override
    protected boolean qualifies(double value, double low, double high) {
        return value >= low && value <= high;
    }
}
