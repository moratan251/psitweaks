package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamMatrix;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.MassBlockBreakHelper;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceOperator;

/**
 * 演算子: Y軸方向指定（2026-11-12 ユーザー）。領域行列（3×4 / 4×4、1〜3 列目が辺、4 列目が始点）を、その箱の中心のまわりに
 * 回して、Y 軸（2 列目）を「方向」へ向けた行列を返す。回し方は今の Y 軸から方向へのいちばん小さい回転（すでに回してある行列にも
 * 使える）で、箱の大きさと中心は変わらない。方向がちょうど逆向きなら、Y 軸に垂直な軸のまわりに半回転する（真上・真下でも
 * 止まらない）。作動式: 加重領域の行列モードは Y 軸の向きへ押すので、直方体領域とこれ 1 つで、押す向きを好きな方向へ向けられる。
 * <p>
 * 回転は演算子: 回転行列と同じ（Psi の Operator: Rotate Vector の回し方）。90 度単位の回転は誤差なく計算される。
 */
public class PieceOperatorMatrixSetYAxis extends PieceOperator {
    private static final double PARALLEL_EPSILON = 1e-9;

    private SpellParam<MatrixValue> matrix;
    private SpellParam<Vector3> direction;

    public PieceOperatorMatrixSetYAxis(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(matrix = new ParamMatrix(PsitweaksSpellParams.MATRIX, PsitweaksSpellParams.MATRIX_COLOR, false, false));
        addParam(direction = new ParamVector(SpellParam.GENERIC_NAME_DIRECTION, SpellParam.GREEN, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        MatrixValue region = getNotNullParamValue(context, matrix);
        Vector3 toward = getNotNullParamValue(context, direction);
        return pointYAxis(region, toward);
    }

    /** {@code region} を箱の中心のまわりに回し、Y 軸を {@code toward} へ向けた行列（形は元と同じ 3×4 / 4×4）。 */
    public static MatrixValue pointYAxis(MatrixValue region, Vector3 toward) throws SpellRuntimeException {
        if ((region.rows() != 3 && region.rows() != 4) || region.cols() != 4) {
            throw new SpellRuntimeException(MassBlockBreakHelper.ERROR_INVALID_MATRIX);
        }
        if (toward.isZero() || !Double.isFinite(toward.mag())) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        Vector3 a = column(region, 0);
        Vector3 b = column(region, 1);
        Vector3 c = column(region, 2);
        Vector3 start = column(region, 3);
        if (b.isZero()) {
            throw new SpellRuntimeException(MassBlockBreakHelper.ERROR_DEGENERATE_EDGES);
        }

        Vector3 from = b.copy().normalize();
        Vector3 to = toward.copy().normalize();
        double dot = Math.max(-1.0, Math.min(1.0, from.dotProduct(to)));
        Vector3 axis = from.copy().crossProduct(to);
        MatrixValue rotation;
        if (axis.mag() < PARALLEL_EPSILON) {
            if (dot > 0.0) {
                return MatrixOperations.checkedMatrix(region.rows(), region.cols(), region.valuesCopy());
            }
            rotation = PieceOperatorMatrixRotation.rotation(perpendicular(from, a, c), Math.PI);
        } else {
            rotation = PieceOperatorMatrixRotation.rotation(axis, Math.acos(dot));
        }

        // 箱の中心は動かさない: 始点 = 中心 − R × (辺の和 ÷ 2)。
        Vector3 half = a.copy().add(b).add(c).multiply(0.5);
        Vector3 center = start.copy().add(half);
        Vector3 turnedA = turn(rotation, a);
        Vector3 turnedB = turn(rotation, b);
        Vector3 turnedC = turn(rotation, c);
        Vector3 turnedStart = center.copy().subtract(turn(rotation, half));

        double[] values = region.valuesCopy();
        Vector3[] columns = {turnedA, turnedB, turnedC, turnedStart};
        for (int col = 0; col < 4; col++) {
            values[col] = columns[col].x;
            values[4 + col] = columns[col].y;
            values[8 + col] = columns[col].z;
        }
        return MatrixOperations.checkedMatrix(region.rows(), region.cols(), values);
    }

    /** 逆向きに半回転するときの軸: Y 軸に垂直な単位ベクトル（X の辺、だめなら Z の辺、だめなら世界の軸から作る）。 */
    private static Vector3 perpendicular(Vector3 from, Vector3 a, Vector3 c) {
        for (Vector3 candidate : new Vector3[]{a, c, new Vector3(1, 0, 0), new Vector3(0, 0, 1)}) {
            Vector3 part = candidate.copy().subtract(from.copy().multiply(candidate.dotProduct(from)));
            if (part.mag() > PARALLEL_EPSILON) {
                return part.normalize();
            }
        }
        return new Vector3(1, 0, 0);
    }

    /** 回転行列（4×4）の左上 3×3 でベクトルを回す。 */
    private static Vector3 turn(MatrixValue rotation, Vector3 vector) {
        return new Vector3(
                rotation.get(0, 0) * vector.x + rotation.get(0, 1) * vector.y + rotation.get(0, 2) * vector.z,
                rotation.get(1, 0) * vector.x + rotation.get(1, 1) * vector.y + rotation.get(1, 2) * vector.z,
                rotation.get(2, 0) * vector.x + rotation.get(2, 1) * vector.y + rotation.get(2, 2) * vector.z);
    }

    private static Vector3 column(MatrixValue matrix, int col) {
        return new Vector3(matrix.get(0, col), matrix.get(1, col), matrix.get(2, col));
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
