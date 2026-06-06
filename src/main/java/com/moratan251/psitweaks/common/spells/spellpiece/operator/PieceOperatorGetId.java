package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.SpellParamContextualValue;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorGetId extends PieceOperator {
    private SpellParam<Object> target;

    public PieceOperatorGetId(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(target = new SpellParamContextualValue(SpellParam.GENERIC_NAME_TARGET,
                PsitweaksSpellParams.CONTEXTUAL_VALUE_COLOR, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Object value = getParamValue(context, target);
        if (value == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }
        return RegistryIdTargetHelper.getRegistryId(context, value);
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
