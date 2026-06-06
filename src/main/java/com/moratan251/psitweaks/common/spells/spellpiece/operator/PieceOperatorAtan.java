package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import vazkii.psi.api.spell.Spell;

public class PieceOperatorAtan extends PieceOperatorUnaryMath {
    public PieceOperatorAtan(Spell spell) {
        super(spell);
    }

    @Override
    protected double apply(double value) {
        return Math.atan(value);
    }
}
