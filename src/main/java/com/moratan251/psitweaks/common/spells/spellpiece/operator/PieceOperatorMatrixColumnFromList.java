package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamVectorOrNumberList;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorMatrixColumnFromList extends PieceOperator {
    private SpellParam<Object> values;

    public PieceOperatorMatrixColumnFromList(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(values = new ParamVectorOrNumberList(PsitweaksSpellParams.VECTOR_OR_NUMBER_LIST, PsitweaksSpellParams.NUMBER_LIST_COLOR, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        return MatrixOperations.columnFromList(MatrixOperations.asNumberList(getNotNullParamValue(context, values)));
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
