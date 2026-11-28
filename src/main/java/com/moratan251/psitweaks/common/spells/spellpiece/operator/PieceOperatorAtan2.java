package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceOperator;

/** atan2(Y, X): the angle in radians, from -π to π, of the point (X, Y) seen from the origin. */
public class PieceOperatorAtan2 extends PieceOperator {
    private SpellParam<Number> y;
    private SpellParam<Number> x;

    public PieceOperatorAtan2(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(y = new ParamNumber(SpellParam.GENERIC_NAME_Y, SpellParam.GREEN, false, false));
        addParam(x = new ParamNumber(SpellParam.GENERIC_NAME_X, SpellParam.RED, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        double yValue = getNotNullParamValue(context, y).doubleValue();
        double xValue = getNotNullParamValue(context, x).doubleValue();
        return Math.atan2(yValue, xValue);
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }
}
