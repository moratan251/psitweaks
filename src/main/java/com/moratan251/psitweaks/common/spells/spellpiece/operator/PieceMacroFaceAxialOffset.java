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
        Vector3 positionValue = getParamValue(context, position);
        if (positionValue == null) {
            positionValue = targetFace.blockPosition();
        }
        return executeWithBasis(context, positionValue, targetFace.basis());
    }
}
