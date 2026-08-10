package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

public class PieceMacroFaceAxialOffset extends PieceMacroCasterAxialOffsetBase {
    public PieceMacroFaceAxialOffset(Spell spell) {
        super(spell, false);
    }

    @Override
    protected CasterAxialBasis getBasis(SpellContext context) throws SpellRuntimeException {
        return CasterAxialBasis.ofTargetFace(context);
    }
}
