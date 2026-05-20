package com.moratan251.psitweaks.common.spells.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.param.ParamStringListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorStringListAdd extends PieceOperator {
    private SpellParam<String> string;
    private SpellParam<StringListWrapper> list;

    public PieceOperatorStringListAdd(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(string = new ParamString(PsitweaksSpellParams.STRING, PsitweaksSpellParams.STRING_COLOR, false, false));
        addParam(list = new ParamStringListWrapper(SpellParam.GENERIC_NAME_LIST,
                PsitweaksSpellParams.STRING_LIST_COLOR, true, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        String value = getNotNullParamValue(context, string);
        StringListWrapper source = getParamValueOrDefault(context, list, StringListWrapper.EMPTY);
        return StringListWrapper.withAdded(source, value);
    }

    @Override
    public Class<?> getEvaluationType() {
        return StringListWrapper.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.string_list");
    }
}
