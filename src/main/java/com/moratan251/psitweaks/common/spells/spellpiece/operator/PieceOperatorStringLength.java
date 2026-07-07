package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorStringLength extends PsitweaksPieceOperator {
    private SpellParam<String> string;

    public PieceOperatorStringLength(Spell spell) {
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
        return (double) value.codePointCount(0, value.length());
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psi.datatype.number");
    }
}
