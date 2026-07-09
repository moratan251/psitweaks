package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamMatrix;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

public class PieceOperatorMatrixSubtract extends PsitweaksPieceOperator {
    private SpellParam<MatrixValue> matrix1;
    private SpellParam<MatrixValue> matrix2;
    private SpellParam<MatrixValue> matrix3;

    public PieceOperatorMatrixSubtract(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(matrix1 = new ParamMatrix(PsitweaksSpellParams.MATRIX1, PsitweaksSpellParams.MATRIX_COLOR, false, false));
        addParam(matrix2 = new ParamMatrix(PsitweaksSpellParams.MATRIX2, PsitweaksSpellParams.MATRIX_COLOR, false, false));
        addParam(matrix3 = new ParamMatrix(PsitweaksSpellParams.MATRIX3, PsitweaksSpellParams.MATRIX_COLOR, true, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        MatrixValue a = getNotNullParamValue(context, matrix1);
        MatrixValue b = getNotNullParamValue(context, matrix2);
        MatrixValue c = getParamValueOrDefault(context, matrix3, null);
        return MatrixOperations.subtract(a, b, c);
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
