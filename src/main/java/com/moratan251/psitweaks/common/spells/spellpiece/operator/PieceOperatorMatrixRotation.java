package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.MatrixValue;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceOperator;

/**
 * A 4x4 rotation around an axis through the origin. The columns are Psi's own rotation of the unit vectors, so the
 * matrix turns a vector exactly like Operator: Rotate Vector with the same axis and angle (radians).
 */
public class PieceOperatorMatrixRotation extends PieceOperator {
    private static final double SNAP_EPSILON = 1e-12;

    private SpellParam<Vector3> axis;
    private SpellParam<Number> angle;

    public PieceOperatorMatrixRotation(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(axis = new ParamVector(SpellParam.GENERIC_NAME_AXIS, SpellParam.CYAN, false, false));
        addParam(angle = new ParamNumber(SpellParam.GENERIC_NAME_ANGLE, SpellParam.GREEN, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 axisVal = getNotNullParamValue(context, axis);
        double angleVal = this.<Number>getNotNullParamValue(context, angle).doubleValue();
        if (axisVal.isZero() || !Double.isFinite(axisVal.mag()) || !Double.isFinite(angleVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        return rotation(axisVal, angleVal);
    }

    static MatrixValue rotation(Vector3 axis, double angle) throws SpellRuntimeException {
        Vector3 x = new Vector3(1, 0, 0).rotate(angle, axis);
        Vector3 y = new Vector3(0, 1, 0).rotate(angle, axis);
        Vector3 z = new Vector3(0, 0, 1).rotate(angle, axis);
        return MatrixOperations.checkedMatrix(4, 4, new double[]{
                snap(x.x), snap(y.x), snap(z.x), 0.0,
                snap(x.y), snap(y.y), snap(z.y), 0.0,
                snap(x.z), snap(y.z), snap(z.z), 0.0,
                0.0, 0.0, 0.0, 1.0
        });
    }

    /** Keeps quarter turns exact: rounding noise around 0 and ±1 would shift block lattice tests. */
    private static double snap(double value) {
        double rounded = Math.rint(value);
        return Math.abs(value - rounded) < SNAP_EPSILON ? rounded + 0.0 : value;
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
