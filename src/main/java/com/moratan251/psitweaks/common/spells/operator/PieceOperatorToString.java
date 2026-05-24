package com.moratan251.psitweaks.common.spells.operator;

import com.moratan251.psitweaks.common.spells.util.ModeListOperations;
import com.moratan251.psitweaks.common.spells.util.ModeStringConversionHelper;
import java.util.Map;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

public class PieceOperatorToString extends PieceOperatorModeConversionBase {
    private SpellParam<?> target;

    public PieceOperatorToString(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Object value = getParamValue(context, typed(target));
        if (value == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }
        return ModeStringConversionHelper.elementToString(currentMode(), value);
    }

    @Override
    public Class<?> getEvaluationType() {
        return String.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.string");
    }

    @Override
    protected void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        clearModeParams();
        addParam(target = ModeListOperations.createElementParam(currentMode(), SpellParam.GENERIC_NAME_TARGET,
                false));
        restoreParamSides(savedSides);
    }
}
