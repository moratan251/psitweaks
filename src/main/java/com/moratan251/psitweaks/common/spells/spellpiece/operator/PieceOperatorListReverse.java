package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

/** Reverses the order of a List. Entity Lists are left out: Psi always keeps them sorted by entity ID. */
public class PieceOperatorListReverse extends PieceOperatorModeListBase {
    private SpellParam<?> list;

    public PieceOperatorListReverse(Spell spell) {
        super(spell);
    }

    @Override
    protected boolean offersMode(PsitweaksModeOption option) {
        return !option.id().equals(PsitweaksModeOptions.ENTITY.id());
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Object source = getNotNullParamValue(context, typed(list));
        int size = currentAdapter().size(source);
        List<Object> result = new ArrayList<>(size);
        for (int i = size - 1; i >= 0; i--) {
            result.add(currentAdapter().get(source, i));
        }
        return currentAdapter().add(currentAdapter().emptyList(), result);
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
        restoreParamSides(savedSides);
    }
}
