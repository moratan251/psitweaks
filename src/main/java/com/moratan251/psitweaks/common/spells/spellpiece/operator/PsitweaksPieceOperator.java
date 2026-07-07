package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

public abstract class PsitweaksPieceOperator extends PieceOperator {
    protected PsitweaksPieceOperator(Spell spell) {
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