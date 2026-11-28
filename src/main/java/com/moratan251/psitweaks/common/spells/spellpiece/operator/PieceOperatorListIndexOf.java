package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.util.ComparisonValueHelper;
import java.util.List;
import java.util.Map;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * The zero-based index of the first List element equal to the input element (compared like Operator: Equal), or -1
 * when there is none. The element is first stored the way the List stores it, so a String is sanitized and an empty
 * Item matches nothing.
 */
public class PieceOperatorListIndexOf extends PieceOperatorModeListBase {
    public static final double NOT_FOUND = -1.0D;

    private SpellParam<?> list;
    private SpellParam<?> element;

    public PieceOperatorListIndexOf(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Object source = getNotNullParamValue(context, typed(list));
        Object value = currentAdapter().coerceElement(context, getNotNullParamValue(context, typed(element)));
        Object stored = currentAdapter().add(currentAdapter().emptyList(), List.of(value));
        if (currentAdapter().size(stored) == 0) {
            return NOT_FOUND;
        }
        Object target = currentAdapter().get(stored, 0);
        int size = currentAdapter().size(source);
        for (int i = 0; i < size; i++) {
            if (ComparisonValueHelper.equalsValue(currentAdapter().get(source, i), target)) {
                return (double) i;
            }
        }
        return NOT_FOUND;
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }

    @Override
    protected void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        clearModeParams();
        addParam(list = currentAdapter().createListParam(SpellParam.GENERIC_NAME_LIST, false));
        addParam(element = currentAdapter().createElementParam(PsitweaksSpellParams.ELEMENT1, false));
        restoreParamSides(savedSides);
    }
}
