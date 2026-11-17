package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import vazkii.psi.api.spell.Spell;

/** Keeps the elements with a number below Min or above Max at the path; with neither bound, nothing is outside. */
public class PieceOperatorListNbtRangeOutside extends PieceOperatorListNbtRangeBase {
    public PieceOperatorListNbtRangeOutside(Spell spell) {
        super(spell);
    }

    @Override
    protected boolean qualifies(double value, double low, double high) {
        return value < low || value > high;
    }
}
