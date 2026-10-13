package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamMatrix;
import com.moratan251.psitweaks.common.spells.param.ParamVectorListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

/** Operator: Matrix Transform Vector applied to every element of a Vector List, keeping the order. */
public class PieceOperatorMatrixTransformVectorList extends PieceOperator {
    private SpellParam<MatrixValue> matrix;
    private SpellParam<VectorListWrapper> vectors;

    public PieceOperatorMatrixTransformVectorList(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(matrix = new ParamMatrix(PsitweaksSpellParams.MATRIX, PsitweaksSpellParams.MATRIX_COLOR, false, false));
        addParam(vectors = new ParamVectorListWrapper(SpellParam.GENERIC_NAME_LIST, PsitweaksSpellParams.VECTOR_LIST_COLOR,
                false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        MatrixValue mat = getNotNullParamValue(context, matrix);
        VectorListWrapper source = getNotNullParamValue(context, vectors);
        List<Vector3> result = new ArrayList<>(source.size());
        for (Vector3 vector : source) {
            result.add(MatrixOperations.transformVector(mat, vector));
        }
        return VectorListWrapper.make(result);
    }

    @Override
    public Class<?> getEvaluationType() {
        return VectorListWrapper.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.vector_list");
    }
}
