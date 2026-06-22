package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorMatrixCuboidRegion extends PieceOperator {
    private static final double INTEGER_EPSILON = 1e-9;

    private SpellParam<Vector3> position;
    private SpellParam<Vector3> size;

    public PieceOperatorMatrixCuboidRegion(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(size = new ParamVector(PsitweaksSpellParams.SIZE, SpellParam.GREEN, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 pos = getNotNullParamValue(context, position);
        Vector3 sz = getNotNullParamValue(context, size);

        AxisRegion x = axisRegion(pos.x, sz.x);
        AxisRegion y = axisRegion(pos.y, sz.y);
        AxisRegion z = axisRegion(pos.z, sz.z);

        double[] values = new double[]{
                x.edge, 0.0, 0.0, x.start,
                0.0, y.edge, 0.0, y.start,
                0.0, 0.0, z.edge, z.start,
                0.0, 0.0, 0.0, 1.0
        };
        return MatrixOperations.checkedMatrix(4, 4, values);
    }

    @Override
    public Class<?> getEvaluationType() {
        return MatrixValue.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.matrix");
    }

    private static AxisRegion axisRegion(double center, double size) {
        if (size == 0.0 || !isWholeNumber(size)) {
            return new AxisRegion(center - size * 0.5, size);
        }

        int blocks = (int) Math.abs(Math.rint(size));
        if (size > 0) {
            int minOffset = -((blocks - 1) / 2);
            return new AxisRegion(center + minOffset - 0.5, blocks);
        }

        int maxOffset = (blocks - 1) / 2;
        return new AxisRegion(center + maxOffset + 0.5, -blocks);
    }

    private static boolean isWholeNumber(double value) {
        return Double.isFinite(value)
                && Math.abs(value) <= Integer.MAX_VALUE
                && Math.abs(value - Math.rint(value)) < INTEGER_EPSILON;
    }

    private record AxisRegion(double start, double edge) {
    }
}
