package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import vazkii.psi.api.spell.Spell;

public class PieceOperatorCosh extends PieceOperatorUnaryMath {
    public PieceOperatorCosh(Spell spell) {
        super(spell);
    }

    @Override
    protected double apply(double value) {
        return Math.cosh(value);
    }
}
