package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;

/** Keeps the elements whose value is within Min and Max, both included; an unconnected bound is open. */
public class PieceOperatorListValueRange extends PieceOperatorListValueBase {
    private SpellParam<Number> min;
    private SpellParam<Number> max;

    public PieceOperatorListValueRange(Spell spell) {
        super(spell);
    }

    @Override
    protected void addValueParams() {
        addParam(min = new ParamNumber(SpellParam.GENERIC_NAME_MIN, SpellParam.GREEN, true, false));
        addParam(max = new ParamNumber(SpellParam.GENERIC_NAME_MAX, SpellParam.RED, true, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        List<Object> elements = elements(context);
        double[] values = values(context, elements);
        Number low = getParamValue(context, min);
        Number high = getParamValue(context, max);
        List<Object> kept = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            if ((low == null || values[i] >= low.doubleValue()) && (high == null || values[i] <= high.doubleValue())) {
                kept.add(elements.get(i));
            }
        }
        return makeList(kept);
    }

    @Override
    public Class<?> getEvaluationType() {
        return currentListType();
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable(currentMode().listTranslationKey());
    }
}
