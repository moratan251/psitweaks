package com.moratan251.psitweaks.common.spells.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorStringToNumber extends PieceOperator {
    private SpellParam<String> string;

    public PieceOperatorStringToNumber(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(string = new ParamString(PsitweaksSpellParams.STRING, PsitweaksSpellParams.STRING_COLOR, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        String value = getNotNullParamValue(context, string);
        return StringSpellHelper.parseFiniteDouble(value).orElse(0D);
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }
}
