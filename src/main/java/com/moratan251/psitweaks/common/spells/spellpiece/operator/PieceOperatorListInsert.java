package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import java.util.Map;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;

public class PieceOperatorListInsert extends PieceOperatorModeListBase {
    private SpellParam<?> list;
    private SpellParam<?> element;
    private SpellParam<Number> number;

    public PieceOperatorListInsert(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Object source = getNotNullParamValue(context, typed(list));
        Object value = getNotNullParamValue(context, typed(element));
        int index = getNotNullParamValue(context, number).intValue();
        int normalizedIndex = normalizeInsertIndex(index, currentAdapter().size(source));
        return currentAdapter().insert(source, normalizedIndex, currentAdapter().coerceElement(context, value));
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
        addParam(element = currentAdapter().createElementParam(PsitweaksSpellParams.ELEMENT1, false));
        addParam(number = new ParamNumber("psi.spellparam.number", SpellParam.RED, false, false));
        restoreParamSides(savedSides);
    }

    private static int normalizeInsertIndex(int index, int size) {
        int normalized = index < 0 ? size + index : index;
        return Math.max(0, Math.min(normalized, size));
    }
}
