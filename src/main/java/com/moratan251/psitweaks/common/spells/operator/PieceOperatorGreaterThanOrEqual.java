package com.moratan251.psitweaks.common.spells.operator;

import vazkii.psi.api.spell.Spell;

public class PieceOperatorGreaterThanOrEqual extends PieceOperatorBinaryMagnitudeComparison {
    public PieceOperatorGreaterThanOrEqual(Spell spell) {
        super(spell);
    }

    @Override
    protected boolean compare(double left, double right) {
        return left >= right;
    }
}
