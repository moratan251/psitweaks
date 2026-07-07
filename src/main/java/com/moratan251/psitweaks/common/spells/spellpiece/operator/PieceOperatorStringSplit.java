package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorStringSplit extends PsitweaksPieceOperator {
    private static final String DEFAULT_DELIMITER = ",";
    private static final String ERROR_EMPTY_DELIMITER = "psitweaks.spellerror.empty_delimiter";

    private SpellParam<String> string1;
    private SpellParam<String> string2;

    public PieceOperatorStringSplit(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(string1 = new ParamString(PsitweaksSpellParams.STRING1, PsitweaksSpellParams.STRING_COLOR, false, false));
        addParam(string2 = new ParamString(PsitweaksSpellParams.STRING2, PsitweaksSpellParams.STRING_COLOR, true, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        String source = getNotNullParamValue(context, string1);
        String delimiter = getParamValueOrDefault(context, string2, DEFAULT_DELIMITER);
        if (delimiter == null) {
            delimiter = DEFAULT_DELIMITER;
        }
        if (delimiter.isEmpty()) {
            throw new SpellRuntimeException(ERROR_EMPTY_DELIMITER);
        }

        return StringListWrapper.make(splitLiteral(source, delimiter));
    }

    @Override
    public Class<?> getEvaluationType() {
        return StringListWrapper.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.string_list");
    }

    private static List<String> splitLiteral(String source, String delimiter) {
        List<String> result = new ArrayList<>();
        int start = 0;
        while (true) {
            int index = source.indexOf(delimiter, start);
            if (index < 0) {
                result.add(source.substring(start));
                return result;
            }

            result.add(source.substring(start, index));
            start = index + delimiter.length();
        }
    }
}
