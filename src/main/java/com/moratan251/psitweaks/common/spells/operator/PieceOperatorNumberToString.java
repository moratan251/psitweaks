package com.moratan251.psitweaks.common.spells.operator;

import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorNumberToString extends PieceOperator {
    private SpellParam<Number> number;

    public PieceOperatorNumberToString(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(number = new ParamNumber("psi.spellparam.number", SpellParam.RED, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Object value = getRawParamValue(context, number);
        if (!(value instanceof Number numericValue)) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }
        return StringSpellHelper.numberToString(numericValue);
    }

    @Override
    public Class<?> getEvaluationType() {
        return String.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.string");
    }
}
