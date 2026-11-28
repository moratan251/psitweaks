package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.wrapper.NumberListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.SpellListLimits;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceOperator;

/**
 * Number A, A + C, A + 2C, ... with Number B elements (C defaults to 1). B is truncated toward zero and must be
 * from 0 to the List size limit.
 */
public class PieceOperatorNumberSequence extends PieceOperator {
    private SpellParam<Number> start;
    private SpellParam<Number> count;
    private SpellParam<Number> step;

    public PieceOperatorNumberSequence(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(start = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER1, SpellParam.GREEN, false, false));
        addParam(count = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER2, SpellParam.RED, false, false));
        addParam(step = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER3, SpellParam.BLUE, true, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        double first = getNotNullParamValue(context, start).doubleValue();
        double size = getNotNullParamValue(context, count).doubleValue();
        double interval = getParamValueOrDefault(context, step, 1.0D).doubleValue();
        if (!(size >= 0) || size >= SpellListLimits.MAX_ELEMENTS + 1) {
            throw new SpellRuntimeException(SpellRuntimeException.OUT_OF_BOUNDS);
        }
        int elements = (int) size;
        List<Double> values = new ArrayList<>(elements);
        for (int i = 0; i < elements; i++) {
            double value = first + interval * i;
            if (!Double.isFinite(value)) {
                throw new SpellRuntimeException(SpellRuntimeException.NAN);
            }
            values.add(value);
        }
        return NumberListWrapper.make(values);
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
