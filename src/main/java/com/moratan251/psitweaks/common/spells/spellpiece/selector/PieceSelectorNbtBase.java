package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.SpellParamContextualValue;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceSelector;

abstract class PieceSelectorNbtBase extends PsitweaksPieceSelector {
    private SpellParam<Object> target;

    protected PieceSelectorNbtBase(Spell spell) {
        super(spell);
    }

    @Override
    public final void initParams() {
        addParam(target = new SpellParamContextualValue(SpellParam.GENERIC_NAME_TARGET,
                PsitweaksSpellParams.CONTEXTUAL_VALUE_COLOR, false));
        addAdditionalParams();
    }

    protected final NbtTargetHelper.TargetData getTargetData(SpellContext context) throws SpellRuntimeException {
        Object targetValue = getNotNullParamValue(context, target);
        return NbtTargetHelper.getNbt(context, targetValue).orElse(NbtTargetHelper.empty());
    }

    protected void addAdditionalParams() {
    }
}
