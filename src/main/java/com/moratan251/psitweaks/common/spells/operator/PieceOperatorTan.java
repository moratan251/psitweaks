package com.moratan251.psitweaks.common.spells.operator;

import vazkii.psi.api.spell.Spell;

public class PieceOperatorTan extends PieceOperatorUnaryMath {
    public PieceOperatorTan(Spell spell) {
        super(spell);
    }

    @Override
    protected double apply(double value) {
        return Math.tan(value);
    }
}
