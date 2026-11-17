package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.util.RankedSelection;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;

/**
 * The element at index n when the List is sorted by value, smallest first. Index 0 has the smallest value and negative
 * indexes count from the largest (-1 is the largest). An index outside the List is an error.
 */
public class PieceOperatorListValueElement extends PieceOperatorListValueBase {
    private SpellParam<Number> number;

    public PieceOperatorListValueElement(Spell spell) {
        super(spell);
    }

    @Override
    protected void addValueParams() {
        addParam(number = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER, SpellParam.RED, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        double index = getNotNullParamValue(context, number).doubleValue();
        return RankedSelection.element(ascending(context), index);
    }

    @Override
    public Class<?> getEvaluationType() {
        return currentAdapter().elementType();
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable(currentMode().elementTranslationKey());
    }
}
