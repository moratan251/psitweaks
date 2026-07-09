package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamMatrix;
import com.moratan251.psitweaks.common.spells.param.ParamVectorOrNumberList;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;

public class PieceOperatorMatrixReplaceColumn extends PsitweaksPieceOperator {
    private SpellParam<MatrixValue> matrix;
    private SpellParam<Number> col;
    private SpellParam<Object> values;

    public PieceOperatorMatrixReplaceColumn(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(matrix = new ParamMatrix(PsitweaksSpellParams.MATRIX, PsitweaksSpellParams.MATRIX_COLOR, false, false));
        addParam(col = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER1, SpellParam.PURPLE, false, false));
        addParam(values = new ParamVectorOrNumberList(PsitweaksSpellParams.VECTOR_OR_NUMBER_LIST, PsitweaksSpellParams.NUMBER_LIST_COLOR, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        MatrixValue mat = getNotNullParamValue(context, matrix);
        int index = getNotNullParamValue(context, col).intValue();
        var list = MatrixOperations.asNumberList(getNotNullParamValue(context, values));
        return MatrixOperations.replaceColumn(mat, index, list);
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
