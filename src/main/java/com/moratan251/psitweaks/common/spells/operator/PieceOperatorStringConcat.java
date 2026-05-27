package com.moratan251.psitweaks.common.spells.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorStringConcat extends PieceOperator {
    private SpellParam<String> string1;
    private SpellParam<String> string2;
    private SpellParam<String> string3;

    public PieceOperatorStringConcat(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(string1 = new ParamString(PsitweaksSpellParams.STRING1, PsitweaksSpellParams.STRING_COLOR, false, false));
        addParam(string2 = new ParamString(PsitweaksSpellParams.STRING2, PsitweaksSpellParams.STRING_COLOR, false, false));
        addParam(string3 = new ParamString(PsitweaksSpellParams.STRING3, PsitweaksSpellParams.STRING_COLOR, true, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        String first = getNotNullParamValue(context, string1);
        String second = getNotNullParamValue(context, string2);
        String third = getParamValueOrDefault(context, string3, "");
        return StringSpellHelper.sanitize(first + second + (third == null ? "" : third));
    }

    @Override
    public Class<?> getEvaluationType() {
        return String.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.string");
    }
}
