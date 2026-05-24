package com.moratan251.psitweaks.common.spells.operator;

import com.moratan251.psitweaks.common.spells.util.ModeStringConversionHelper;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamAny;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorToString extends PieceOperator {
    private SpellParam<?> target;

    public PieceOperatorToString(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(target = new ParamAny(SpellParam.GENERIC_NAME_TARGET, SpellParam.GRAY, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Object value = getParamValue(context, typed(target));
        if (value == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }
        return ModeStringConversionHelper.anyToString(value);
    }

    @Override
    public Class<?> getEvaluationType() {
        return String.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.string");
    }

    @SuppressWarnings("unchecked")
    private static <T> SpellParam<T> typed(SpellParam<?> param) {
        return (SpellParam<T>) param;
    }
}
