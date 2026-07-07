package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.PsitweaksListAdapter;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.util.ModeStringConversionHelper;
import com.moratan251.psitweaks.common.spells.util.WildcardStringMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

public abstract class PieceOperatorListStringFilterBase extends PieceOperatorModeListBase {
    private SpellParam<?> list;
    private SpellParam<String> string;

    protected PieceOperatorListStringFilterBase(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Object source = getNotNullParamValue(context, typed(list));
        String wildcard = getNotNullParamValue(context, string);
        WildcardStringMatcher matcher = WildcardStringMatcher.compile(wildcard);
        PsitweaksListAdapter<Object> adapter = currentAdapter();
        List<Object> result = new ArrayList<>();

        int size = adapter.size(source);
        for (int i = 0; i < size; i++) {
            Object value = adapter.get(source, i);
            boolean matches = matcher.matches(searchText(context, value));
            if (shouldKeep(matches)) {
                result.add(value);
            }
        }

        return adapter.add(adapter.emptyList(), result);
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
    protected void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        clearModeParams();
        addParam(list = currentAdapter().createListParam(SpellParam.GENERIC_NAME_LIST, false));
        addParam(string = new ParamString(PsitweaksSpellParams.STRING, PsitweaksSpellParams.STRING_COLOR, false,
                false));
        restoreParamSides(savedSides);
    }

    private static String searchText(SpellContext context, Object value) throws SpellRuntimeException {
        return ModeStringConversionHelper.anyToString(value);
    }

    protected abstract boolean shouldKeep(boolean matches);
}
