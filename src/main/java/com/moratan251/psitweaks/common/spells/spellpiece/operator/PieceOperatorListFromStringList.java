package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.PsitweaksValueKind;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamStringListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import java.util.Map;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

public class PieceOperatorListFromStringList extends PieceOperatorModeConversionBase {
    private SpellParam<StringListWrapper> list;

    public PieceOperatorListFromStringList(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        StringListWrapper source = getNotNullParamValue(context, list);
        return currentAdapter().listFromStrings(source);
    }

    @Override
    public Class<?> getEvaluationType() {
        return currentListType();
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable(currentMode().listTranslationKey());
    }

    @Override
    protected PsitweaksValueKind modeOptionKindFilter() {
        return PsitweaksValueKind.PLAIN;
    }

    @Override
    protected void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        clearModeParams();
        addParam(list = new ParamStringListWrapper(SpellParam.GENERIC_NAME_LIST,
                PsitweaksSpellParams.STRING_LIST_COLOR, false, false));
        restoreParamSides(savedSides);
    }
}
