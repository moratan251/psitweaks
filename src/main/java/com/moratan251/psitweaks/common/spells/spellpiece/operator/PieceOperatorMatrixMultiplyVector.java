package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamMatrix;
import com.moratan251.psitweaks.common.spells.param.ParamVectorOrNumberList;
import com.moratan251.psitweaks.common.spells.wrapper.NumberListWrapper;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorMatrixMultiplyVector extends PieceOperator {
    private SpellParam<Object> vector;
    private SpellParam<MatrixValue> matrix;

    public PieceOperatorMatrixMultiplyVector(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(vector = new ParamVectorOrNumberList(PsitweaksSpellParams.VECTOR_OR_NUMBER_LIST, PsitweaksSpellParams.NUMBER_LIST_COLOR, false));
        addParam(matrix = new ParamMatrix(PsitweaksSpellParams.MATRIX1, PsitweaksSpellParams.MATRIX_COLOR, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        NumberListWrapper list = MatrixOperations.asNumberList(getNotNullParamValue(context, vector));
        MatrixValue mat = getNotNullParamValue(context, matrix);
        return MatrixOperations.multiplyVector(mat, list);
    }

    @Override
    public Class<?> getEvaluationType() {
        return NumberListWrapper.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.number_list");
    }
}
