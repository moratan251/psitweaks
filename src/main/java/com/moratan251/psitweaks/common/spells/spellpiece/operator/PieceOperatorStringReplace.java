package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorStringReplace extends PieceOperator {
    private SpellParam<String> string1;
    private SpellParam<String> string2;
    private SpellParam<String> string3;

    public PieceOperatorStringReplace(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(string1 = new ParamString(PsitweaksSpellParams.STRING1, PsitweaksSpellParams.STRING_COLOR, false, false));
        addParam(string2 = new ParamString(PsitweaksSpellParams.STRING2, PsitweaksSpellParams.STRING_COLOR, false, false));
        addParam(string3 = new ParamString(PsitweaksSpellParams.STRING3, PsitweaksSpellParams.STRING_COLOR, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        String source = getNotNullParamValue(context, string1);
        String target = getNotNullParamValue(context, string2);
        String replacement = getNotNullParamValue(context, string3);
        if (target.isEmpty()) {
            return source;
        }
        return replaceLiteral(source, target, replacement);
    }

    @Override
    public Class<?> getEvaluationType() {
        return String.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.string");
    }

    private static String replaceLiteral(String source, String target, String replacement) {
        StringBuilder result = new StringBuilder(Math.min(source.length(), StringSpellHelper.MAX_STRING_LENGTH));
        int start = 0;
        while (result.length() < StringSpellHelper.MAX_STRING_LENGTH) {
            int match = source.indexOf(target, start);
            if (match < 0) {
                appendCapped(result, source, start, source.length());
                break;
            }

            appendCapped(result, source, start, match);
            appendCapped(result, replacement, 0, replacement.length());
            start = match + target.length();
        }
        return result.toString();
    }

    private static void appendCapped(StringBuilder result, String value, int start, int end) {
        int available = StringSpellHelper.MAX_STRING_LENGTH - result.length();
        int length = Math.min(end - start, available);
        if (length > 0) {
            result.append(value, start, start + length);
        }
    }
}
