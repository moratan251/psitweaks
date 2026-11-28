package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellRuntimeException;

/** Value 1 ÷ Value 2 element by element; two inputs only, like Psi's Operator: Divide. */
public class PieceOperatorNumberListDivide extends PieceOperatorNumberListArithmeticBase {
    public PieceOperatorNumberListDivide(Spell spell) {
        super(spell);
    }

    @Override
    protected boolean hasThirdValue() {
        return false;
    }

    @Override
    protected double apply(double left, double right) throws SpellRuntimeException {
        if (right == 0) {
            throw new SpellRuntimeException(SpellRuntimeException.DIVIDE_BY_ZERO);
        }
        return left / right;
    }
}
