package com.moratan251.psitweaks.common.spells.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.mode.ListElementMode;
import com.moratan251.psitweaks.common.spells.param.ParamStringListWrapper;
import com.moratan251.psitweaks.common.spells.util.ModeListOperations;
import com.moratan251.psitweaks.common.spells.util.ModeStringParsingHelper;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import java.util.Map;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

public class PieceOperatorListFromStringList extends PieceOperatorModeConversionBase {
    private static final ListElementMode[] PARSE_MODES = {
            ListElementMode.NUMBER,
            ListElementMode.VECTOR
    };

    private SpellParam<StringListWrapper> list;

    public PieceOperatorListFromStringList(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        StringListWrapper source = getNotNullParamValue(context, list);
        return ModeStringParsingHelper.listFromStringList(currentMode(), source);
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
    protected ListElementMode[] availableElementModes() {
        return PARSE_MODES;
    }

    @Override
    protected void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        clearModeParams();
        addParam(list = new ParamStringListWrapper(SpellParam.GENERIC_NAME_LIST,
                PsitweaksSpellParams.STRING_LIST_COLOR, false, false));
        restoreParamSides(savedSides);
    }
}
