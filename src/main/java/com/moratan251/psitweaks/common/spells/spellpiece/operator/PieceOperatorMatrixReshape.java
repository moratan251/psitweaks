package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamVectorOrNumberList;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceOperator;

/**
 * Builds a matrix from a Number List (or Vector) row by row: the reverse of Operator: Matrix Flatten. Rows and columns
 * are both optional; with neither, the result is a column matrix.
 */
public class PieceOperatorMatrixReshape extends PieceOperator {
    private SpellParam<Object> values;
    private SpellParam<Number> rows;
    private SpellParam<Number> cols;

    public PieceOperatorMatrixReshape(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(values = new ParamVectorOrNumberList(PsitweaksSpellParams.VECTOR_OR_NUMBER_LIST,
                PsitweaksSpellParams.NUMBER_LIST_COLOR, false));
        addParam(rows = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER1, SpellParam.PURPLE, true, false));
        addParam(cols = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER2, SpellParam.PURPLE, true, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        var list = MatrixOperations.asNumberList(getNotNullParamValue(context, values));
        Number rowCount = getParamValueOrDefault(context, rows, null);
        Number colCount = getParamValueOrDefault(context, cols, null);
        return MatrixOperations.reshape(list, rowCount == null ? null : rowCount.intValue(),
                colCount == null ? null : colCount.intValue());
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
