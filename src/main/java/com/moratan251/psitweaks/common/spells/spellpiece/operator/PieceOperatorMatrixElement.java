package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamMatrix;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorMatrixElement extends PieceOperator {
    private SpellParam<MatrixValue> matrix;
    private SpellParam<Number> row;
    private SpellParam<Number> col;

    public PieceOperatorMatrixElement(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(matrix = new ParamMatrix(PsitweaksSpellParams.MATRIX1, PsitweaksSpellParams.MATRIX_COLOR, false, false));
        addParam(row = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER1, SpellParam.PURPLE, false, false));
        addParam(col = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER2, SpellParam.PURPLE, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        MatrixValue mat = getNotNullParamValue(context, matrix);
        int i = getNotNullParamValue(context, row).intValue();
        int j = getNotNullParamValue(context, col).intValue();
        return MatrixOperations.element(mat, i, j);
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }
}
