package com.moratan251.psitweaks.common.spells.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.util.ComparisonValueHelper;
import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamAny;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorEqual extends PieceOperator {
    private SpellParam<?> value1;
    private SpellParam<?> value2;

    public PieceOperatorEqual(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(value1 = new ParamAny(PsitweaksSpellParams.VALUE1, SpellParam.GRAY, false));
        addParam(value2 = new ParamAny(PsitweaksSpellParams.VALUE2, SpellParam.GRAY, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Object left = getParamValue(context, typed(value1));
        Object right = getParamValue(context, typed(value2));
        return StringSpellHelper.bool(ComparisonValueHelper.equalsValue(left, right));
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }

    @SuppressWarnings("unchecked")
    private static <T> SpellParam<T> typed(SpellParam<?> param) {
        return (SpellParam<T>) param;
    }
}
