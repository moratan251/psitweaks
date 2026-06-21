package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamMatrix;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.MassBlockBreakHelper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorRegionVectorList extends PieceOperator {
    private SpellParam<MatrixValue> matrix;

    public PieceOperatorRegionVectorList(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(matrix = new ParamMatrix(PsitweaksSpellParams.MATRIX1, PsitweaksSpellParams.MATRIX_COLOR, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        List<BlockPos> positions = MassBlockBreakHelper.collectRegionPositions(getNotNullParamValue(context, matrix));
        List<Vector3> vectors = new ArrayList<>(positions.size());
        for (BlockPos position : positions) {
            vectors.add(new Vector3(position.getX(), position.getY(), position.getZ()));
        }
        return VectorListWrapper.make(vectors);
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
