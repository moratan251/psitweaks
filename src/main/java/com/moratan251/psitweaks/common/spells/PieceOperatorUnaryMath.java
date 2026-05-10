package com.moratan251.psitweaks.common.spells;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceOperator;

abstract class PieceOperatorUnaryMath extends PieceOperator {
    private SpellParam<Number> target;

    protected PieceOperatorUnaryMath(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        this.addParam(this.target = new ParamNumber("psi.spellparam.target", SpellParam.BLUE, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        double value = this.getParamValue(context, this.target).doubleValue();
        return this.apply(value);
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }

    protected abstract double apply(double value);
}
