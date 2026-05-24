package com.moratan251.psitweaks.common.spells.operator;

import com.moratan251.psitweaks.common.spells.util.ModeListOperations;
import java.util.Map;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

public class PieceOperatorListIntersection extends PieceOperatorModeListBase {
    private SpellParam<?> list1;
    private SpellParam<?> list2;

    public PieceOperatorListIntersection(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Object left = getNotNullParamValue(context, typed(list1));
        Object right = getNotNullParamValue(context, typed(list2));
        return ModeListOperations.intersection(currentMode(), left, right);
    }

    @Override
    public Class<?> getEvaluationType() {
        return ModeListOperations.listType(currentMode());
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable(currentMode().listTranslationKey());
    }

    @Override
    protected void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        clearModeParams();
        addParam(list1 = ModeListOperations.createListParam(currentMode(), SpellParam.GENERIC_NAME_LIST1, false));
        addParam(list2 = ModeListOperations.createListParam(currentMode(), SpellParam.GENERIC_NAME_LIST2, false));
        restoreParamSides(savedSides);
    }
}
