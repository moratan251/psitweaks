package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.util.RankedSelection;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;

/**
 * Keeps the n elements with the smallest values, smallest first, or the -n largest, largest first, for a negative n.
 * An Entity List always takes Psi's own order.
 */
public class PieceOperatorListValueKeep extends PieceOperatorListValueBase {
    private SpellParam<Number> number;

    public PieceOperatorListValueKeep(Spell spell) {
        super(spell);
    }

    @Override
    protected void addValueParams() {
        addParam(number = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER, SpellParam.RED, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        double count = getNotNullParamValue(context, number).doubleValue();
        return makeList(RankedSelection.keep(ascending(context), count));
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
