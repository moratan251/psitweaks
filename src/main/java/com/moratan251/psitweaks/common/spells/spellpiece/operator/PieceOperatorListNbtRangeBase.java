package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.util.NbtPathHelper;
import java.util.OptionalDouble;
import net.minecraft.nbt.Tag;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;

/**
 * Filters by the number values at the path, compared with Min and Max (both included; an unconnected bound is open).
 * Only number tags count, as in Selector: NBT Value's Number mode, so an element without one is always dropped. One
 * qualifying value is enough. Min above Max is an empty range.
 */
public abstract class PieceOperatorListNbtRangeBase extends PieceOperatorListNbtBase {
    private SpellParam<Number> min;
    private SpellParam<Number> max;

    protected PieceOperatorListNbtRangeBase(Spell spell) {
        super(spell);
    }

    @Override
    protected void addFilterParams() {
        addParam(min = new ParamNumber(SpellParam.GENERIC_NAME_MIN, SpellParam.GREEN, true, false));
        addParam(max = new ParamNumber(SpellParam.GENERIC_NAME_MAX, SpellParam.RED, true, false));
    }

    @Override
    protected ElementTest prepare(SpellContext context) throws SpellRuntimeException {
        Number low = getParamValue(context, min);
        Number high = getParamValue(context, max);
        double lowValue = low == null ? Double.NEGATIVE_INFINITY : low.doubleValue();
        double highValue = high == null ? Double.POSITIVE_INFINITY : high.doubleValue();
        return found -> {
            for (Tag tag : found) {
                OptionalDouble number = NbtPathHelper.number(tag);
                if (number.isPresent() && qualifies(number.getAsDouble(), lowValue, highValue)) {
                    return true;
                }
            }
            return false;
        };
    }

    /** Whether one value keeps its element, given the bounds (open bounds are infinite). */
    protected abstract boolean qualifies(double value, double low, double high);
}
