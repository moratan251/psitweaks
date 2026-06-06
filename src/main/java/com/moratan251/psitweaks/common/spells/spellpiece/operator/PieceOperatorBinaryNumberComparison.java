package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceOperator;

abstract class PieceOperatorBinaryNumberComparison extends PieceOperator {
    private SpellParam<Number> value1;
    private SpellParam<Number> value2;

    protected PieceOperatorBinaryNumberComparison(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(value1 = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER1, SpellParam.PURPLE, false, false));
        addParam(value2 = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER2, SpellParam.PURPLE, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Number left = getNotNullParamValue(context, value1);
        Number right = getNotNullParamValue(context, value2);
        return StringSpellHelper.bool(compare(left.doubleValue(), right.doubleValue()));
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }

    protected abstract boolean compare(double left, double right);
}
