package com.moratan251.psitweaks.common.spells.operator;

import com.moratan251.psitweaks.common.spells.util.ModeListOperations;
import com.moratan251.psitweaks.common.spells.util.ModeStringConversionHelper;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import java.util.Map;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

public class PieceOperatorListToStringList extends PieceOperatorModeConversionBase {
    private SpellParam<?> list;

    public PieceOperatorListToStringList(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Object source = getNotNullParamValue(context, typed(list));
        return ModeStringConversionHelper.listToStringList(currentMode(), source);
    }

    @Override
    public Class<?> getEvaluationType() {
        return StringListWrapper.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.string_list");
    }

    @Override
    protected void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        clearModeParams();
        addParam(list = ModeListOperations.createListParam(currentMode(), SpellParam.GENERIC_NAME_LIST, false));
        restoreParamSides(savedSides);
    }
}
