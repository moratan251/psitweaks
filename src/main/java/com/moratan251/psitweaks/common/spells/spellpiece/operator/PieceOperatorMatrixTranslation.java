package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceOperator;

/** The 4x4 matrix that moves a point by the offset. */
public class PieceOperatorMatrixTranslation extends PieceOperator {
    private SpellParam<Vector3> offset;

    public PieceOperatorMatrixTranslation(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(offset = new ParamVector(PsitweaksSpellParams.OFFSET, SpellParam.GREEN, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 offsetVal = getNonnullParamValue(context, offset);
        return MatrixOperations.checkedMatrix(4, 4, new double[]{
                1.0, 0.0, 0.0, offsetVal.x,
                0.0, 1.0, 0.0, offsetVal.y,
                0.0, 0.0, 1.0, offsetVal.z,
                0.0, 0.0, 0.0, 1.0
        });
    }

    @Override
    public Class<?> getEvaluationType() {
        return MatrixValue.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.matrix");
    }
}
