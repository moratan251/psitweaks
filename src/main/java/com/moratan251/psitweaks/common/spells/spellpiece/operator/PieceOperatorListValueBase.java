package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.PsitweaksListAdapter;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamNumberListWrapper;
import com.moratan251.psitweaks.common.spells.util.RankedSelection;
import com.moratan251.psitweaks.common.spells.wrapper.NumberListWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * List operators that rank or filter the elements of a List by a Number List of the same size, element by element
 * (the Values input, such as the output of Selector: NBT Number List). Lists of different sizes are an error.
 */
public abstract class PieceOperatorListValueBase extends PieceOperatorModeListBase {
    public static final String ERROR_SIZE_MISMATCH = "psitweaks.spellerror.value_list_size_mismatch";

    private SpellParam<?> list;
    private SpellParam<NumberListWrapper> values;

    protected PieceOperatorListValueBase(Spell spell) {
        super(spell);
    }

    @Override
    protected void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        clearModeParams();
        addParam(list = currentAdapter().createListParam(SpellParam.GENERIC_NAME_LIST, false));
        addParam(values = new ParamNumberListWrapper(PsitweaksSpellParams.VALUES, PsitweaksSpellParams.NUMBER_LIST_COLOR,
                false, false));
        addValueParams();
        restoreParamSides(savedSides);
    }

    /** Adds the inputs after the List and the Values. */
    protected abstract void addValueParams();

    /** The elements of the List input, in its order. */
    protected final List<Object> elements(SpellContext context) throws SpellRuntimeException {
        Object source = getNotNullParamValue(context, typed(list));
        PsitweaksListAdapter<Object> adapter = currentAdapter();
        int size = adapter.size(source);
        List<Object> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(adapter.get(source, i));
        }
        return result;
    }

    /** The Values input, checked to have one value per element. */
    protected final double[] values(SpellContext context, List<Object> elements) throws SpellRuntimeException {
        NumberListWrapper numbers = getNotNullParamValue(context, values);
        if (numbers.size() != elements.size()) {
            throw new SpellRuntimeException(ERROR_SIZE_MISMATCH);
        }
        double[] result = new double[numbers.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = numbers.get(i);
        }
        return result;
    }

    /** The elements sorted by their values, smallest first; equal values keep the List's order. */
    protected final List<Object> ascending(SpellContext context) throws SpellRuntimeException {
        List<Object> elements = elements(context);
        return RankedSelection.ascending(elements, values(context, elements));
    }

    /** {@link RankedSelection#keep}: the {@code count} smallest, or the -{@code count} largest, by value. */
    protected final List<Object> keep(SpellContext context, double count) throws SpellRuntimeException {
        List<Object> elements = elements(context);
        return RankedSelection.keep(elements, values(context, elements), count);
    }

    /** A List of the current mode with these elements, in this order (an Entity List takes Psi's own order). */
    protected final Object makeList(List<Object> elements) {
        PsitweaksListAdapter<Object> adapter = currentAdapter();
        return adapter.add(adapter.emptyList(), elements);
    }
}
