package com.moratan251.psitweaks.common.spells.operator;

import com.moratan251.psitweaks.common.spells.util.ModeListOperations;
import java.util.Map;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

public class PieceOperatorListSize extends PieceOperatorModeListBase {
    private SpellParam<?> list;

    public PieceOperatorListSize(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Object source = getNotNullParamValue(context, typed(list));
        return ModeListOperations.size(currentMode(), source);
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psi.datatype.number");
    }

    @Override
    protected void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        clearModeParams();
        addParam(list = ModeListOperations.createListParam(currentMode(), SpellParam.GENERIC_NAME_LIST, false));
        restoreParamSides(savedSides);
    }
}
