package com.moratan251.psitweaks.common.spells.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

public class PieceOperatorListAdd extends PieceOperatorModeListBase {
    private SpellParam<?> element1;
    private SpellParam<?> element2;
    private SpellParam<?> element3;
    private SpellParam<?> list;

    public PieceOperatorListAdd(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Object source = getParamValueOrDefault(context, typed(list), currentAdapter().emptyList());
        return currentAdapter().add(source, elementValues(context));
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
        addParam(element1 = currentAdapter().createElementParam(PsitweaksSpellParams.ELEMENT1, false));
        addParam(element2 = currentAdapter().createElementParam(PsitweaksSpellParams.ELEMENT2, true));
        addParam(element3 = currentAdapter().createElementParam(PsitweaksSpellParams.ELEMENT3, true));
        addParam(list = currentAdapter().createListParam(SpellParam.GENERIC_NAME_LIST, true));
        restoreParamSides(savedSides);
    }

    private List<Object> elementValues(SpellContext context) throws SpellRuntimeException {
        List<Object> values = new ArrayList<>(3);
        Object first = getParamValue(context, typed(element1));
        if (first == null) {
            throw new SpellRuntimeException("psi.spellerror.nulltarget");
        }
        values.add(currentAdapter().coerceElement(context, first));
        addOptionalElement(context, values, element2);
        addOptionalElement(context, values, element3);
        return values;
    }

    private void addOptionalElement(SpellContext context, List<Object> values, SpellParam<?> param)
            throws SpellRuntimeException {
        Object value = getParamValue(context, typed(param));
        if (value != null) {
            values.add(currentAdapter().coerceElement(context, value));
        }
    }
}
