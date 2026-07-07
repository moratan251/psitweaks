package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorStringSlice extends PsitweaksPieceOperator {
    private SpellParam<String> string;
    private SpellParam<Number> numberA;
    private SpellParam<Number> numberB;

    public PieceOperatorStringSlice(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(string = new ParamString(
                PsitweaksSpellParams.STRING,
                PsitweaksSpellParams.STRING_COLOR,
                false,
                false));
        addParam(numberA = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER1, SpellParam.RED, true, false));
        addParam(numberB = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER2, SpellParam.RED, true, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        String source = getNotNullParamValue(context, string);
        int size = source.codePointCount(0, source.length());
        Number startValue = getParamValue(context, numberA);
        Number endValue = getParamValue(context, numberB);
        int start = startValue == null ? 0 : SliceIndexHelper.normalize(startValue.intValue(), size);
        int end = endValue == null ? size : SliceIndexHelper.normalize(endValue.intValue(), size);
        if (end <= start) {
            return "";
        }

        int startOffset = source.offsetByCodePoints(0, start);
        int endOffset = source.offsetByCodePoints(startOffset, end - start);
        return source.substring(startOffset, endOffset);
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
