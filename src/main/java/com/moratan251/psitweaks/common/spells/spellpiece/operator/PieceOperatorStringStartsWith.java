package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorStringStartsWith extends PieceOperator {
    private SpellParam<String> string1;
    private SpellParam<String> string2;

    public PieceOperatorStringStartsWith(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(string1 = new ParamString(PsitweaksSpellParams.STRING1, PsitweaksSpellParams.STRING_COLOR, false, false));
        addParam(string2 = new ParamString(PsitweaksSpellParams.STRING2, PsitweaksSpellParams.STRING_COLOR, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        String first = getNotNullParamValue(context, string1);
        String second = getNotNullParamValue(context, string2);
        return StringSpellHelper.bool(first.startsWith(second));
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }
}
