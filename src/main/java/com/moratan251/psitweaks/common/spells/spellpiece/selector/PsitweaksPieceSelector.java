package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceSelector;

public abstract class PsitweaksPieceSelector extends PieceSelector {
    protected PsitweaksPieceSelector(Spell spell) {
        super(spell);
    }

    protected final <T> T getNotNullParamValue(SpellContext context, SpellParam<T> param) throws SpellRuntimeException {
        T value = getParamValue(context, param);
        if (value == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }
        return value;
    }
}