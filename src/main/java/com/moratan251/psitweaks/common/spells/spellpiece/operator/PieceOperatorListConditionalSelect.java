package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import java.util.Map;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;

/** The List version of Operator: Conditional Select; the mode picks the List type. */
public class PieceOperatorListConditionalSelect extends PieceOperatorModeListBase {
    private SpellParam<Number> condition;
    private SpellParam<?> ifTrue;
    private SpellParam<?> ifFalse;

    public PieceOperatorListConditionalSelect(Spell spell) {
        super(spell);
    }

    @Override
    protected void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        clearModeParams();
        addParam(condition = new ParamNumber(PsitweaksSpellParams.CONDITION, SpellParam.BLUE, false, false));
        addParam(ifTrue = currentAdapter().createListParam(PsitweaksSpellParams.IF_TRUE, false));
        addParam(ifFalse = currentAdapter().createListParam(PsitweaksSpellParams.IF_FALSE, false));
        restoreParamSides(savedSides);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        double value = getNotNullParamValue(context, condition).doubleValue();
        return getNotNullParamValue(context, typed(PieceOperatorConditionalSelect.isTrue(value) ? ifTrue : ifFalse));
    }

    @Override
    public Class<?> getEvaluationType() {
        return currentListType();
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable(currentMode().listTranslationKey());
    }
}
