package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorStringTrim extends PsitweaksPieceOperator {
    private SpellParam<String> string;

    public PieceOperatorStringTrim(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(string = new ParamString(
                PsitweaksSpellParams.STRING,
                PsitweaksSpellParams.STRING_COLOR,
                false,
                false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        String value = getNotNullParamValue(context, string);
        return value.trim();
    }

    @Override
    public Class<?> getEvaluationType() {
        return String.class;
    }
}
