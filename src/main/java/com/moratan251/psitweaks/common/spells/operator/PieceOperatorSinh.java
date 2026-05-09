package com.moratan251.psitweaks.common.spells.operator;

import vazkii.psi.api.spell.Spell;

public class PieceOperatorSinh extends PieceOperatorUnaryMath {
    public PieceOperatorSinh(Spell spell) {
        super(spell);
    }

    @Override
    protected double apply(double value) {
        return Math.sinh(value);
    }
}
