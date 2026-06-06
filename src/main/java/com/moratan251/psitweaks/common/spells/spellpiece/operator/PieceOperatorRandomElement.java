package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import java.util.Map;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

public class PieceOperatorRandomElement extends PieceOperatorModeListBase {
    private SpellParam<?> list;

    public PieceOperatorRandomElement(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Object source = getNotNullParamValue(context, typed(list));
        int size = currentAdapter().size(source);
        if (size == 0) {
            return currentAdapter().emptyElement();
        }

        return currentAdapter().get(source, context.caster.getRandom().nextInt(size));
    }

    @Override
    public Class<?> getEvaluationType() {
        return currentAdapter().elementType();
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable(currentMode().elementTranslationKey());
    }

    @Override
    protected void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        clearModeParams();
        addParam(list = currentAdapter().createListParam(SpellParam.GENERIC_NAME_LIST, false));
        restoreParamSides(savedSides);
    }
}
