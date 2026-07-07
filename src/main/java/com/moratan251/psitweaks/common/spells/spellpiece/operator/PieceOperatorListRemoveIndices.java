package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;

public class PieceOperatorListRemoveIndices extends PieceOperatorModeListBase {
    private SpellParam<?> list;
    private SpellParam<Number> numberA;
    private SpellParam<Number> numberB;

    public PieceOperatorListRemoveIndices(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Object source = getNotNullParamValue(context, typed(list));
        int size = currentAdapter().size(source);
        List<Integer> indices = new ArrayList<>(2);
        addIndex(indices, getNotNullParamValue(context, numberA).intValue(), size);

        Number optionalIndex = getParamValue(context, numberB);
        if (optionalIndex != null) {
            addIndex(indices, optionalIndex.intValue(), size);
        }

        return currentAdapter().removeIndices(source, indices);
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
        addParam(numberA = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER1, SpellParam.RED, false, false));
        addParam(numberB = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER2, SpellParam.RED, true, false));
        restoreParamSides(savedSides);
    }

    private static void addIndex(List<Integer> indices, int index, int size) throws SpellRuntimeException {
        int normalized = index < 0 ? size + index : index;
        if (normalized < 0 || normalized >= size) {
            throw new SpellRuntimeException("psi.spellerror.out_of_bounds");
        }
        if (!indices.contains(normalized)) {
            indices.add(normalized);
        }
    }
}
