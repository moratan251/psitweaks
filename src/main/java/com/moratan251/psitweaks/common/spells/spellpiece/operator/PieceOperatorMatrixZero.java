package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.MatrixValue;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;

public class PieceOperatorMatrixZero extends PsitweaksPieceOperator {
    private SpellParam<Number> rows;
    private SpellParam<Number> cols;

    public PieceOperatorMatrixZero(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(rows = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER1, SpellParam.PURPLE, false, false));
        addParam(cols = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER2, SpellParam.PURPLE, true, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        int rowCount = getNotNullParamValue(context, rows).intValue();
        Number colCount = getParamValueOrDefault(context, cols, null);
        if (colCount == null) {
            return MatrixOperations.zero(rowCount, rowCount);
        }
        return MatrixOperations.zero(rowCount, colCount.intValue());
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
