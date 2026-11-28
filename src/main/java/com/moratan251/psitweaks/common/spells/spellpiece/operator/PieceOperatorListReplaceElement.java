package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;

/**
 * Replaces the element at a zero-based index (negative counts back from the end) with the input element. The result is
 * rebuilt through the List type, so an Entity or Block List keeps its own order and drops duplicates.
 */
public class PieceOperatorListReplaceElement extends PieceOperatorModeListBase {
    private SpellParam<?> list;
    private SpellParam<?> element;
    private SpellParam<Number> number;

    public PieceOperatorListReplaceElement(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Object source = getNotNullParamValue(context, typed(list));
        Object value = currentAdapter().coerceElement(context, getNotNullParamValue(context, typed(element)));
        int index = getNotNullParamValue(context, number).intValue();
        int size = currentAdapter().size(source);
        int normalizedIndex = index < 0 ? size + index : index;
        if (normalizedIndex < 0 || normalizedIndex >= size) {
            throw new SpellRuntimeException(SpellRuntimeException.OUT_OF_BOUNDS);
        }

        List<Object> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(i == normalizedIndex ? value : currentAdapter().get(source, i));
        }
        return currentAdapter().add(currentAdapter().emptyList(), result);
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
        addParam(number = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER, SpellParam.RED, false, false));
        restoreParamSides(savedSides);
    }
}
