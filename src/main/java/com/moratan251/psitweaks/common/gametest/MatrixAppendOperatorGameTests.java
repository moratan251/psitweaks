package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.MatrixOperations;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorMatrixAffine;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorMatrixAppendColumn;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorMatrixAppendRow;
import com.moratan251.psitweaks.common.spells.wrapper.NumberListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellRuntimeException;

/** Operators that build matrices from parts: Matrix Append Column / Row and Affine Matrix. */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MatrixAppendOperatorGameTests {
    private static final String TEMPLATE = "psi110_empty";
    private static final double EPSILON = 1.0E-9;

    private MatrixAppendOperatorGameTests() {
    }

    private static String errorOf(SpellPiece piece) {
        try {
            piece.execute(new SpellContext());
            return null;
        } catch (SpellRuntimeException e) {
            return e.getMessage();
        }
    }

    private static boolean matrixEquals(MatrixValue matrix, int rows, int cols, double... values) {
        if (matrix.rows() != rows || matrix.cols() != cols) {
            return false;
        }
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (Math.abs(matrix.get(r, c) - values[r * cols + c]) > EPSILON) {
                    return false;
                }
            }
        }
        return true;
    }

    private static NumberListWrapper list(Double... values) {
        return NumberListWrapper.make(List.of(values));
    }

    private static PieceOperatorMatrixAppendColumn appendColumn(Map<String, Object> inputs) {
        return new PieceOperatorMatrixAppendColumn(new Spell()) {
            @Override public Object getRawParamValue(SpellContext context, SpellParam<?> param) {
                return inputs.get(param.name);
            }
        };
    }

    private static PieceOperatorMatrixAppendRow appendRow(Map<String, Object> inputs) {
        return new PieceOperatorMatrixAppendRow(new Spell()) {
            @Override public Object getRawParamValue(SpellContext context, SpellParam<?> param) {
                return inputs.get(param.name);
            }
        };
    }

    private static PieceOperatorMatrixAffine affine(Map<String, Object> inputs) {
        return new PieceOperatorMatrixAffine(new Spell()) {
            @Override public Object getRawParamValue(SpellContext context, SpellParam<?> param) {
                return inputs.get(param.name);
            }
        };
    }

    @GameTest(template = TEMPLATE)
    public static void appendColumnBuildsMatricesFromValues(GameTestHelper helper) throws SpellRuntimeException {
        Map<String, Object> inputs = new HashMap<>();
        var piece = appendColumn(inputs);
        inputs.put(PsitweaksSpellParams.VALUE1, new Vector3(1, 2, 3));
        inputs.put(PsitweaksSpellParams.VALUE2, new Vector3(4, 5, 6));
        inputs.put(PsitweaksSpellParams.VALUE3, new Vector3(7, 8, 9));
        var matrix = (MatrixValue) piece.execute(new SpellContext());
        helper.assertTrue(matrixEquals(matrix, 3, 3, 1, 4, 7, 2, 5, 8, 3, 6, 9),
                "Three Vectors must become the columns of a 3x3 matrix: " + matrix);

        inputs.clear();
        inputs.put(PsitweaksSpellParams.MATRIX, matrix);
        inputs.put(PsitweaksSpellParams.VALUE1, new Vector3(10, 11, 12));
        var region = (MatrixValue) piece.execute(new SpellContext());
        helper.assertTrue(matrixEquals(region, 3, 4, 1, 4, 7, 10, 2, 5, 8, 11, 3, 6, 9, 12),
                "A Vector must be added on the right of the Matrix: " + region);

        inputs.clear();
        inputs.put(PsitweaksSpellParams.VALUE1, VectorListWrapper.make(List.of(new Vector3(1, 0, 0), new Vector3(0, 1, 0))));
        inputs.put(PsitweaksSpellParams.VALUE2, list(7.0, 8.0, 9.0));
        matrix = (MatrixValue) piece.execute(new SpellContext());
        helper.assertTrue(matrixEquals(matrix, 3, 3, 1, 0, 7, 0, 1, 8, 0, 0, 9),
                "A Vector List must add one column per Vector, then the Number List: " + matrix);

        inputs.clear();
        inputs.put(PsitweaksSpellParams.VALUE1, list(1.0, 2.0, 3.0, 4.0));
        inputs.put(PsitweaksSpellParams.VALUE2, list(5.0, 6.0, 7.0, 8.0));
        matrix = (MatrixValue) piece.execute(new SpellContext());
        helper.assertTrue(matrixEquals(matrix, 4, 2, 1, 5, 2, 6, 3, 7, 4, 8),
                "Number Lists of 4 must give 4 rows: " + matrix);
        helper.assertTrue(piece.getEvaluationType() == MatrixValue.class, "The result must be a Matrix");
        helper.assertTrue(piece.params.get(PsitweaksSpellParams.MATRIX).canDisable
                        && !piece.params.get(PsitweaksSpellParams.VALUE1).canDisable
                        && piece.params.get(PsitweaksSpellParams.VALUE2).canDisable
                        && piece.params.get(PsitweaksSpellParams.VALUE3).canDisable
                        && piece.params.size() == 4,
                "Matrix, Value 2 and Value 3 must be optional and Value 1 required");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void appendColumnRefusesWrongSizes(GameTestHelper helper) throws SpellRuntimeException {
        Map<String, Object> inputs = new HashMap<>();
        var piece = appendColumn(inputs);
        inputs.put(PsitweaksSpellParams.VALUE1, new Vector3(1, 2, 3));
        inputs.put(PsitweaksSpellParams.VALUE2, list(1.0, 2.0));
        helper.assertTrue(MatrixOperations.ERROR_INCOMPATIBLE_SIZES.equals(errorOf(piece)),
                "Columns of different lengths must be refused");

        inputs.clear();
        inputs.put(PsitweaksSpellParams.MATRIX, MatrixOperations.identity(3));
        inputs.put(PsitweaksSpellParams.VALUE1, new Vector3(1, 2, 3));
        inputs.put(PsitweaksSpellParams.VALUE2, new Vector3(4, 5, 6));
        helper.assertTrue(MatrixOperations.ERROR_TOO_LARGE.equals(errorOf(piece)), "A 5th column must be refused");

        inputs.clear();
        inputs.put(PsitweaksSpellParams.VALUE1, list(1.0, 2.0, 3.0, 4.0, 5.0));
        helper.assertTrue(MatrixOperations.ERROR_TOO_LARGE.equals(errorOf(piece)), "A column of 5 must be refused");

        inputs.put(PsitweaksSpellParams.VALUE1, NumberListWrapper.EMPTY);
        helper.assertTrue(MatrixOperations.ERROR_INVALID_DIMENSION.equals(errorOf(piece)),
                "An empty Number List must be refused");
        inputs.put(PsitweaksSpellParams.VALUE1, VectorListWrapper.EMPTY);
        helper.assertTrue(MatrixOperations.ERROR_INVALID_DIMENSION.equals(errorOf(piece)),
                "Nothing to add without a Matrix must be refused");

        inputs.remove(PsitweaksSpellParams.VALUE1);
        helper.assertTrue(errorOf(piece) != null, "Value 1 must be required");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void appendRowAddsRowsAtTheBottom(GameTestHelper helper) throws SpellRuntimeException {
        Map<String, Object> inputs = new HashMap<>();
        var piece = appendRow(inputs);
        inputs.put(PsitweaksSpellParams.VALUE1, new Vector3(1, 2, 3));
        inputs.put(PsitweaksSpellParams.VALUE2, new Vector3(4, 5, 6));
        var matrix = (MatrixValue) piece.execute(new SpellContext());
        helper.assertTrue(matrixEquals(matrix, 2, 3, 1, 2, 3, 4, 5, 6), "Vectors must become rows: " + matrix);

        inputs.clear();
        inputs.put(PsitweaksSpellParams.MATRIX, matrix);
        inputs.put(PsitweaksSpellParams.VALUE1, VectorListWrapper.make(List.of(new Vector3(7, 8, 9), new Vector3(0, 0, 1))));
        matrix = (MatrixValue) piece.execute(new SpellContext());
        helper.assertTrue(matrixEquals(matrix, 4, 3, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 0, 1),
                "A Vector List must add one row per Vector below the Matrix: " + matrix);

        inputs.put(PsitweaksSpellParams.MATRIX, MatrixOperations.identity(2));
        inputs.put(PsitweaksSpellParams.VALUE1, new Vector3(1, 2, 3));
        helper.assertTrue(MatrixOperations.ERROR_INCOMPATIBLE_SIZES.equals(errorOf(piece)),
                "A row must have as many columns as the Matrix");
        inputs.put(PsitweaksSpellParams.MATRIX, MatrixOperations.identity(4));
        inputs.put(PsitweaksSpellParams.VALUE1, list(1.0, 2.0, 3.0, 4.0));
        helper.assertTrue(MatrixOperations.ERROR_TOO_LARGE.equals(errorOf(piece)), "A 5th row must be refused");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void affineMatrixAddsTheLastRowAndPosition(GameTestHelper helper) throws SpellRuntimeException {
        Map<String, Object> inputs = new HashMap<>();
        var piece = affine(inputs);
        MatrixValue linear = MatrixOperations.reshape(list(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0), 3, 3);
        inputs.put(PsitweaksSpellParams.MATRIX, linear);
        var matrix = (MatrixValue) piece.execute(new SpellContext());
        helper.assertTrue(matrixEquals(matrix, 4, 4, 1, 2, 3, 0, 4, 5, 6, 0, 7, 8, 9, 0, 0, 0, 0, 1),
                "A 3x3 must get a zero 4th column and the row 0 0 0 1: " + matrix);

        inputs.put(SpellParam.GENERIC_NAME_POSITION, new Vector3(10, 20, 30));
        matrix = (MatrixValue) piece.execute(new SpellContext());
        helper.assertTrue(matrixEquals(matrix, 4, 4, 1, 2, 3, 10, 4, 5, 6, 20, 7, 8, 9, 30, 0, 0, 0, 1),
                "Position must fill the 4th column: " + matrix);

        MatrixValue region = MatrixOperations.reshape(list(1.0, 0.0, 0.0, 5.0, 0.0, 2.0, 0.0, 6.0, 0.0, 0.0, 3.0, 7.0), 3, 4);
        inputs.put(PsitweaksSpellParams.MATRIX, region);
        inputs.remove(SpellParam.GENERIC_NAME_POSITION);
        matrix = (MatrixValue) piece.execute(new SpellContext());
        helper.assertTrue(matrixEquals(matrix, 4, 4, 1, 0, 0, 5, 0, 2, 0, 6, 0, 0, 3, 7, 0, 0, 0, 1),
                "A 3x4 must keep its 4th column and get the row 0 0 0 1: " + matrix);

        MatrixValue broken = MatrixOperations.replaceColumn(MatrixOperations.identity(4), 3,
                MatrixOperations.asNumberList(new Vector3(1, 2, 3)));
        helper.assertTrue(broken.get(3, 3) == 0.0, "Matrix Replace Column with a Vector must still clear the corner");
        inputs.put(PsitweaksSpellParams.MATRIX, broken);
        inputs.put(SpellParam.GENERIC_NAME_POSITION, new Vector3(4, 5, 6));
        matrix = (MatrixValue) piece.execute(new SpellContext());
        helper.assertTrue(matrixEquals(matrix, 4, 4, 1, 0, 0, 4, 0, 1, 0, 5, 0, 0, 1, 6, 0, 0, 0, 1),
                "A 4x4 must get the new start and the corner 1 back: " + matrix);
        Vector3 moved = MatrixOperations.transformVector(matrix, new Vector3(1, 1, 1));
        helper.assertTrue(Math.abs(moved.x - 5) < EPSILON && Math.abs(moved.y - 6) < EPSILON && Math.abs(moved.z - 7) < EPSILON,
                "The result must work in Matrix Transform Vector: " + moved);

        inputs.put(PsitweaksSpellParams.MATRIX, MatrixOperations.identity(2));
        helper.assertTrue(MatrixOperations.ERROR_INCOMPATIBLE_SIZES.equals(errorOf(piece)),
                "Matrices smaller than 3x3 must be refused");
        helper.assertTrue(piece.params.get(SpellParam.GENERIC_NAME_POSITION).canDisable
                        && !piece.params.get(PsitweaksSpellParams.MATRIX).canDisable,
                "Position must be optional and Matrix required");
        helper.succeed();
    }

    /** Four Vectors take two pieces: Matrix Append Column for the axes, then Affine Matrix for the start. */
    @GameTest(template = TEMPLATE)
    public static void fourVectorsBecomeATransformInTwoPieces(GameTestHelper helper) throws SpellRuntimeException {
        Map<String, Object> columnInputs = new HashMap<>();
        columnInputs.put(PsitweaksSpellParams.VALUE1, new Vector3(2, 0, 0));
        columnInputs.put(PsitweaksSpellParams.VALUE2, new Vector3(0, 3, 0));
        columnInputs.put(PsitweaksSpellParams.VALUE3, new Vector3(0, 0, 4));
        var axes = (MatrixValue) appendColumn(columnInputs).execute(new SpellContext());
        Map<String, Object> affineInputs = new HashMap<>();
        affineInputs.put(PsitweaksSpellParams.MATRIX, axes);
        affineInputs.put(SpellParam.GENERIC_NAME_POSITION, new Vector3(10, 20, 30));
        var transform = (MatrixValue) affine(affineInputs).execute(new SpellContext());
        Vector3 corner = MatrixOperations.transformVector(transform, new Vector3(1, 1, 1));
        helper.assertTrue(Math.abs(corner.x - 12) < EPSILON && Math.abs(corner.y - 23) < EPSILON && Math.abs(corner.z - 34) < EPSILON,
                "(1, 1, 1) must map to start + the three edges: " + corner);
        MatrixValue turned = MatrixOperations.multiply(MatrixOperations.identity(4), transform);
        helper.assertTrue(turned.rows() == 4 && turned.cols() == 4, "The result must multiply with a 4x4 matrix");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void matrixBuildOperatorsAreRegisteredInTheMatrixGroup(GameTestHelper helper) {
        for (String id : List.of("operator_matrix_append_column", "operator_matrix_append_row", "operator_matrix_affine")) {
            var location = Psitweaks.location(id);
            helper.assertTrue(PsiAPI.SPELL_PIECE_REGISTRY.get(location) != null, "Not registered: " + id);
            var group = PsiAPI.getPieceGroup(helper.getLevel().registryAccess(), location);
            helper.assertTrue(group.isPresent() && group.get().key().location().equals(Psitweaks.location("matrix")),
                    id + " must be in the matrix group: " + group.map(holder -> holder.key().location()));
        }
        helper.succeed();
    }
}
