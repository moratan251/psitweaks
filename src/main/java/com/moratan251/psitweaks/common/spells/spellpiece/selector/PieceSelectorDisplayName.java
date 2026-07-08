package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.SpellParamContextualValue;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceSelector;

public class PieceSelectorDisplayName extends PsitweaksPieceSelector {
    private SpellParam<Object> target;

    public PieceSelectorDisplayName(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(target = new SpellParamContextualValue(SpellParam.GENERIC_NAME_TARGET,
                PsitweaksSpellParams.CONTEXTUAL_VALUE_COLOR, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Object value = getNotNullParamValue(context, target);
        return DisplayNameTargetHelper.getDisplayName(context, value);
    }

    @Override
    public Class<?> getEvaluationType() {
        return String.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.string");
    }
}
