package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.MatrixValue;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorMatrixIdentity extends PieceOperator {
    private SpellParam<Number> size;

    public PieceOperatorMatrixIdentity(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(size = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER1, SpellParam.PURPLE, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        int n = getNotNullParamValue(context, size).intValue();
        return MatrixOperations.identity(n);
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
