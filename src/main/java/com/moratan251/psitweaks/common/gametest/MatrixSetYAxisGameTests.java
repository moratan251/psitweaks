package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorMatrixSetYAxis;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.MassBlockBreakHelper;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * 演算子: Y軸方向指定（2026-11-12 ユーザー）: 領域行列を箱の中心のまわりに回して Y 軸を方向へ向ける。大きさと中心は変わらず、
 * 真上・真下・逆向きでも止まらない。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MatrixSetYAxisGameTests {
    private static final String TEMPLATE = "connector_empty";

    private MatrixSetYAxisGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void setYAxisTurnsTheRegionAroundItsCenter(GameTestHelper helper) throws SpellRuntimeException {
        // 演算子: 直方体領域（位置 (0, 0, 0)、サイズ 9）と同じ行列。
        MatrixValue cube = new MatrixValue(4, 4, new double[]{
                9, 0, 0, -4.5,
                0, 9, 0, -4.5,
                0, 0, 9, -4.5,
                0, 0, 0, 1});

        MatrixValue east = PieceOperatorMatrixSetYAxis.pointYAxis(cube, new Vector3(2, 0, 0));
        helper.assertTrue(near(column(east, 1), 9, 0, 0), "The Y axis must point east: " + east);
        for (double value : east.valuesCopy()) {
            helper.assertTrue(value * 2 == Math.rint(value * 2), "A quarter turn must be exact: " + east);
        }
        keepsBox(helper, cube, east);

        Vector3 look = new Vector3(0.3, -0.5, 0.8);
        MatrixValue aimed = PieceOperatorMatrixSetYAxis.pointYAxis(cube, look);
        Vector3 y = column(aimed, 1);
        Vector3 unit = look.copy().normalize();
        helper.assertTrue(near(y.copy().normalize(), unit.x, unit.y, unit.z), "The Y axis must point along the direction: " + y);
        keepsBox(helper, cube, aimed);

        // 真下（ちょうど逆向き）でも止まらず、X の辺のまわりに半回転する。
        MatrixValue down = PieceOperatorMatrixSetYAxis.pointYAxis(cube, new Vector3(0, -1, 0));
        helper.assertTrue(near(column(down, 1), 0, -9, 0) && near(column(down, 0), 9, 0, 0) && near(column(down, 2), 0, 0, -9),
                "Straight down must be a half turn around the X edge: " + down);
        keepsBox(helper, cube, down);

        MatrixValue same = PieceOperatorMatrixSetYAxis.pointYAxis(cube, new Vector3(0, 5, 0));
        helper.assertTrue(java.util.Arrays.equals(same.valuesCopy(), cube.valuesCopy()), "The same direction must keep the matrix");

        // すでに回してある行列も、今の Y 軸から向け直す。
        MatrixValue again = PieceOperatorMatrixSetYAxis.pointYAxis(aimed, new Vector3(0, 0, -3));
        helper.assertTrue(near(column(again, 1), 0, 0, -9), "A turned matrix must be turned again: " + again);
        keepsBox(helper, cube, again);

        MatrixValue short3x4 = PieceOperatorMatrixSetYAxis.pointYAxis(new MatrixValue(3, 4, new double[]{
                3, 0, 0, 0,
                0, 3, 0, 0,
                0, 0, 3, 0}), new Vector3(0, 0, 1));
        helper.assertTrue(short3x4.rows() == 3 && short3x4.cols() == 4 && near(column(short3x4, 1), 0, 0, 3),
                "A 3x4 matrix must stay 3x4: " + short3x4);

        helper.assertTrue(SpellRuntimeException.NULL_VECTOR.equals(error(cube, new Vector3(0, 0, 0))), "A zero direction must fail");
        helper.assertTrue(MassBlockBreakHelper.ERROR_INVALID_MATRIX.equals(error(new MatrixValue(3, 3, new double[]{
                1, 0, 0, 0, 1, 0, 0, 0, 1}), new Vector3(1, 0, 0))), "A matrix that is not a region must fail");

        // 登録したピースの入力は「行列」と「方向」。
        PieceOperatorMatrixSetYAxis piece = (PieceOperatorMatrixSetYAxis) PsiAPI.SPELL_PIECE_REGISTRY
                .get(Psitweaks.location("operator_matrix_set_y_axis")).create(new Spell());
        helper.assertTrue(piece.params.size() == 2 && piece.params.containsKey(PsitweaksSpellParams.MATRIX)
                && piece.params.containsKey(SpellParam.GENERIC_NAME_DIRECTION), "Inputs must be Matrix and Direction: " + piece.params.keySet());
        PieceOperatorMatrixSetYAxis wired = new PieceOperatorMatrixSetYAxis(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return param.name.equals(PsitweaksSpellParams.MATRIX) ? cube : new Vector3(-1, 0, 0);
            }
        };
        helper.assertTrue(near(column((MatrixValue) wired.execute(new SpellContext()), 1), -9, 0, 0),
                "The piece must point the Y axis along its Direction input");
        helper.succeed();
    }

    /** 回しても辺の長さ・直交・箱の中心は変わらない。 */
    private static void keepsBox(GameTestHelper helper, MatrixValue before, MatrixValue after) {
        for (int col = 0; col < 3; col++) {
            helper.assertTrue(Math.abs(column(after, col).mag() - column(before, col).mag()) < 1.0E-7,
                    "Edge " + col + " must keep its length: " + after);
        }
        helper.assertTrue(Math.abs(column(after, 0).dotProduct(column(after, 1))) < 1.0E-7
                        && Math.abs(column(after, 1).dotProduct(column(after, 2))) < 1.0E-7,
                "Edges must stay perpendicular: " + after);
        Vector3 centerBefore = center(before);
        Vector3 centerAfter = center(after);
        helper.assertTrue(near(centerAfter, centerBefore.x, centerBefore.y, centerBefore.z),
                "The center must stay at " + centerBefore + ", was " + centerAfter);
    }

    private static Vector3 center(MatrixValue matrix) {
        return column(matrix, 3).add(column(matrix, 0).add(column(matrix, 1)).add(column(matrix, 2)).multiply(0.5));
    }

    private static Vector3 column(MatrixValue matrix, int col) {
        return new Vector3(matrix.get(0, col), matrix.get(1, col), matrix.get(2, col));
    }

    private static boolean near(Vector3 v, double x, double y, double z) {
        return Math.abs(v.x - x) < 1.0E-7 && Math.abs(v.y - y) < 1.0E-7 && Math.abs(v.z - z) < 1.0E-7;
    }

    private static String error(MatrixValue matrix, Vector3 direction) {
        try {
            PieceOperatorMatrixSetYAxis.pointYAxis(matrix, direction);
            return null;
        } catch (SpellRuntimeException e) {
            return e.getMessage();
        }
    }
}
