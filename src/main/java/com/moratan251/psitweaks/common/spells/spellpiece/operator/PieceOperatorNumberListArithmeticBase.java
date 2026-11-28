package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamNumberOrNumberList;
import com.moratan251.psitweaks.common.spells.wrapper.NumberListWrapper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

/**
 * Applies an arithmetic operation element by element to Number Lists. Each input is a Number List or a Number, and a
 * Number counts as that value at every position. All Number List inputs must have the same size; with Numbers only,
 * the result has one element.
 */
public abstract class PieceOperatorNumberListArithmeticBase extends PieceOperator {
    public static final String ERROR_SIZE_MISMATCH = "psitweaks.spellerror.list_size_mismatch";

    private SpellParam<Object> value1;
    private SpellParam<Object> value2;
    private SpellParam<Object> value3;

    protected PieceOperatorNumberListArithmeticBase(Spell spell) {
        super(spell);
    }

    /** Whether the optional Value 3 is applied after Value 2, as with Psi's arithmetic operators. */
    protected boolean hasThirdValue() {
        return true;
    }

    protected abstract double apply(double left, double right) throws SpellRuntimeException;

    @Override
    public void initParams() {
        addParam(value1 = new ParamNumberOrNumberList(PsitweaksSpellParams.VALUE1, PsitweaksSpellParams.NUMBER_LIST_COLOR, false));
        addParam(value2 = new ParamNumberOrNumberList(PsitweaksSpellParams.VALUE2, PsitweaksSpellParams.NUMBER_LIST_COLOR, false));
        if (hasThirdValue()) {
            addParam(value3 = new ParamNumberOrNumberList(PsitweaksSpellParams.VALUE3, PsitweaksSpellParams.NUMBER_LIST_COLOR, true));
        }
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        List<Object> inputs = new ArrayList<>(3);
        inputs.add(getNotNullParamValue(context, value1));
        inputs.add(getNotNullParamValue(context, value2));
        if (value3 != null) {
            Object third = getParamValueOrDefault(context, value3, null);
            if (third != null) {
                inputs.add(third);
            }
        }

        int size = -1;
        for (Object input : inputs) {
            if (input instanceof NumberListWrapper list) {
                if (size >= 0 && list.size() != size) {
                    throw new SpellRuntimeException(ERROR_SIZE_MISMATCH);
                }
                size = list.size();
            }
        }
        if (size < 0) {
            size = 1;
        }

        List<Double> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            double value = valueAt(inputs.get(0), i);
            for (int input = 1; input < inputs.size(); input++) {
                value = apply(value, valueAt(inputs.get(input), i));
            }
            if (!Double.isFinite(value)) {
                throw new SpellRuntimeException(SpellRuntimeException.NAN);
            }
            result.add(value);
        }
        return NumberListWrapper.make(result);
    }

    private static double valueAt(Object input, int index) {
        if (input instanceof NumberListWrapper list) {
            return list.get(index);
        }
        return ((Number) input).doubleValue();
    }

    @Override
    public Class<?> getEvaluationType() {
        return NumberListWrapper.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.number_list");
    }
}
