package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.param.ParamStringListWrapper;
import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import java.util.StringJoiner;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorStringListJoin extends PieceOperator {
    private static final String DEFAULT_DELIMITER = ",";

    private SpellParam<StringListWrapper> string1;
    private SpellParam<String> string2;

    public PieceOperatorStringListJoin(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(string1 = new ParamStringListWrapper(SpellParam.GENERIC_NAME_LIST,
                PsitweaksSpellParams.STRING_LIST_COLOR, false, false));
        addParam(string2 = new ParamString(PsitweaksSpellParams.STRING2, PsitweaksSpellParams.STRING_COLOR, true, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        StringListWrapper source = getNotNullParamValue(context, string1);
        String delimiter = getParamValueOrDefault(context, string2, DEFAULT_DELIMITER);
        if (delimiter == null) {
            delimiter = DEFAULT_DELIMITER;
        }

        StringJoiner joiner = new StringJoiner(delimiter);
        for (String value : source) {
            joiner.add(value);
        }
        return StringSpellHelper.sanitize(joiner.toString());
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
