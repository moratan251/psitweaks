package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

public class PieceMacroFaceAxialOffset extends PieceMacroCasterAxialOffsetBase {
    public PieceMacroFaceAxialOffset(Spell spell) {
        super(spell, false);
    }

    @Override
    protected boolean isPositionOptional() {
        return true;
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        CasterAxialBasis.TargetFace targetFace = CasterAxialBasis.targetFace(context);
        Vector3 positionVal = getParamValue(context, position);
        if (positionVal == null) {
            positionVal = targetFace.blockPosition();
        }
        return executeWithBasis(context, positionVal, targetFace.basis());
    }
}
