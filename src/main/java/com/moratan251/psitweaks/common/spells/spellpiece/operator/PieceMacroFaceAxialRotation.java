package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

public class PieceMacroFaceAxialRotation extends PieceMacroCasterAxialRotationBase {
    public PieceMacroFaceAxialRotation(Spell spell) {
        super(spell, false);
    }

    @Override
    protected CasterAxialBasis getBasis(SpellContext context) throws SpellRuntimeException {
        return CasterAxialBasis.ofTargetFace(context);
    }
}
