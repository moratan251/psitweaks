package com.moratan251.psitweaks.common.spells.operator;

import vazkii.psi.api.spell.Spell;

public class PieceOperatorTanh extends PieceOperatorUnaryMath {
    public PieceOperatorTanh(Spell spell) {
        super(spell);
    }

    @Override
    protected double apply(double value) {
        return Math.tanh(value);
    }
}
