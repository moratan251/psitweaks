package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import java.util.Map;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;

public class PieceOperatorListSlice extends PieceOperatorModeListBase {
    private SpellParam<?> list;
    private SpellParam<Number> numberA;
    private SpellParam<Number> numberB;

    public PieceOperatorListSlice(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Object source = getNotNullParamValue(context, typed(list));
        int size = currentAdapter().size(source);
        Number startValue = getParamValue(context, numberA);
        Number endValue = getParamValue(context, numberB);
        int start = startValue == null ? 0 : SliceIndexHelper.normalize(startValue.intValue(), size);
        int end = endValue == null ? size : SliceIndexHelper.normalize(endValue.intValue(), size);
        return currentAdapter().slice(source, start, end);
    }

    @Override
    public Class<?> getEvaluationType() {
        return currentListType();
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable(currentMode().listTranslationKey());
    }

    @Override
    protected void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        clearModeParams();
        addParam(list = currentAdapter().createListParam(SpellParam.GENERIC_NAME_LIST, false));
        addParam(numberA = new ParamNumber(PsitweaksSpellParams.NUMBER_A, SpellParam.RED, true, false));
        addParam(numberB = new ParamNumber(PsitweaksSpellParams.NUMBER_B, SpellParam.RED, true, false));
        restoreParamSides(savedSides);
    }

}
