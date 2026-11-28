package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamMatrix;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceOperator;

/**
 * Operator: Affine Matrix. Returns the 4x4 matrix [L t; 0 0 0 1], where L is the
 * top-left 3x3 of the input and t is Position, or else the top three values of the input's 4th column, or else zero.
 */
public class PieceOperatorMatrixAffine extends PieceOperator {
    private SpellParam<MatrixValue> matrix;
    private SpellParam<Vector3> position;

    public PieceOperatorMatrixAffine(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(matrix = new ParamMatrix(PsitweaksSpellParams.MATRIX, PsitweaksSpellParams.MATRIX_COLOR, false, false));
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, true, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        return MatrixOperations.affine(getNotNullParamValue(context, matrix), getParamValue(context, position));
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
