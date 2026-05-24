package com.moratan251.psitweaks.common.spells.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamNumberOrVector;
import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

abstract class PieceOperatorBinaryMagnitudeComparison extends PieceOperator {
    private SpellParam<Object> value1;
    private SpellParam<Object> value2;

    protected PieceOperatorBinaryMagnitudeComparison(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(value1 = new ParamNumberOrVector(PsitweaksSpellParams.VALUE1, SpellParam.PURPLE, false));
        addParam(value2 = new ParamNumberOrVector(PsitweaksSpellParams.VALUE2, SpellParam.PURPLE, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        double left = magnitude(getNotNullParamValue(context, value1));
        double right = magnitude(getNotNullParamValue(context, value2));
        return StringSpellHelper.bool(compare(left, right));
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }

    protected abstract boolean compare(double left, double right);

    private static double magnitude(Object value) throws SpellRuntimeException {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof Vector3 vector) {
            return vector.mag();
        }
        throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
    }
}
