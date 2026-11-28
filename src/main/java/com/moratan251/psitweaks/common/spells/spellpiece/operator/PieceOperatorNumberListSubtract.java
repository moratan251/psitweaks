package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import vazkii.psi.api.spell.Spell;

public class PieceOperatorNumberListSubtract extends PieceOperatorNumberListArithmeticBase {
    public PieceOperatorNumberListSubtract(Spell spell) {
        super(spell);
    }

    @Override
    protected double apply(double left, double right) {
        return left - right;
    }
}
