package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamMatrix;
import com.moratan251.psitweaks.common.spells.param.ParamNumberListWrapper;
import com.moratan251.psitweaks.common.spells.param.ParamVectorListWrapper;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.MatrixOperations;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorAtan2;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorConditionalSelect;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorEntityPositionList;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListAggregateBase;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListAverage;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListConditionalSelect;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListSum;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorMatrixReshape;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorNumberListAdd;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorNumberListArithmeticBase;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorNumberListDivide;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorNumberListMultiply;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorNumberListSubtract;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorNumberSequence;
import com.moratan251.psitweaks.common.spells.wrapper.NumberListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import com.moratan251.psitweaksqol.api.PsitweaksModeConfigurable;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;
import vazkii.psi.common.spell.operator.entity.PieceOperatorEntityPosition;

/**
 * Operators for List math and choices: Entity Position List, Arc Tangent 2, Conditional Select (value and List),
 * Matrix Reshape, List Sum / Average, element-wise Number List arithmetic and Number Sequence.
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ListMathOperatorGameTests {
    private static final String TEMPLATE = "psi110_empty";
    private static final double EPSILON = 1.0E-9;

    private ListMathOperatorGameTests() {
    }

    private static String errorOf(SpellPiece piece) {
        try {
            piece.execute(new SpellContext());
            return null;
        } catch (SpellRuntimeException e) {
            return e.getMessage();
        }
    }

    private static List<Double> numbers(Object result) {
        return ((NumberListWrapper) result).asList();
    }

    private static NumberListWrapper list(Double... values) {
        return NumberListWrapper.make(List.of(values));
    }

    private static boolean near(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }

    private static boolean near(Vector3 v, double x, double y, double z) {
        return near(v.x, x) && near(v.y, y) && near(v.z, z);
    }

    @GameTest(template = TEMPLATE)
    public static void atan2CoversEveryQuadrant(GameTestHelper helper) throws SpellRuntimeException {
        Map<String, Object> inputs = new HashMap<>();
        var atan2 = new PieceOperatorAtan2(new Spell()) {
            @Override public Object getRawParamValue(SpellContext context, SpellParam<?> param) {
                return inputs.get(param.name);
            }
        };
        double[][] cases = {{1, 1}, {1, -1}, {-1, -1}, {-1, 1}, {0, -2}, {0, 0}};
        for (double[] yx : cases) {
            inputs.put(SpellParam.GENERIC_NAME_Y, yx[0]);
            inputs.put(SpellParam.GENERIC_NAME_X, yx[1]);
            double result = (Double) atan2.execute(new SpellContext());
            helper.assertTrue(near(result, Math.atan2(yx[0], yx[1])), "atan2(" + yx[0] + ", " + yx[1] + ") was " + result);
        }
        helper.assertTrue(atan2.params.size() == 2 && atan2.getEvaluationType() == Double.class,
                "Arc Tangent 2 must take Y and X and return a Number");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void conditionalSelectUsesTheJumpThreshold(GameTestHelper helper) throws SpellRuntimeException {
        Map<String, Object> inputs = new HashMap<>();
        var select = new PieceOperatorConditionalSelect(new Spell()) {
            @Override public Object getRawParamValue(SpellContext context, SpellParam<?> param) {
                return inputs.get(param.name);
            }
        };
        helper.assertTrue(select.getModeOption().id().equals(PsitweaksModeOptions.NUMBER.id())
                        && select.getEvaluationType() == Double.class,
                "A new Conditional Select must start in Number mode");
        inputs.put(PsitweaksSpellParams.IF_TRUE, 10.0);
        inputs.put(PsitweaksSpellParams.IF_FALSE, 20.0);
        for (double condition : new double[] {1.0, -1.0, 2.5, -7.0}) {
            inputs.put(PsitweaksSpellParams.CONDITION, condition);
            helper.assertTrue(((Number) select.execute(new SpellContext())).doubleValue() == 10.0,
                    condition + " must pick If True");
        }
        for (double condition : new double[] {0.0, 0.99, -0.5}) {
            inputs.put(PsitweaksSpellParams.CONDITION, condition);
            helper.assertTrue(((Number) select.execute(new SpellContext())).doubleValue() == 20.0,
                    condition + " must pick If False");
        }

        select.setModeOption(PsitweaksModeOptions.VECTOR);
        inputs.put(PsitweaksSpellParams.IF_TRUE, new Vector3(1, 2, 3));
        inputs.put(PsitweaksSpellParams.IF_FALSE, new Vector3(4, 5, 6));
        inputs.put(PsitweaksSpellParams.CONDITION, 1.0);
        helper.assertTrue(select.getEvaluationType() == Vector3.class
                        && near((Vector3) select.execute(new SpellContext()), 1, 2, 3),
                "Vector mode must return the If True Vector");

        select.setModeOption(PsitweaksModeOptions.MATRIX);
        helper.assertTrue(select.getEvaluationType() == MatrixValue.class
                        && select.params.get(PsitweaksSpellParams.IF_TRUE) instanceof ParamMatrix,
                "Matrix mode must take and return Matrices");
        select.setModeOption(PsitweaksModeOptions.ENTITY);
        helper.assertTrue(select.getEvaluationType() == Entity.class, "Entity mode must return an Entity");
        helper.assertTrue(select.getAvailableModeOptions().size() == 7,
                "Every single value type must be offered: " + select.getAvailableModeOptions());
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void conditionalSelectKeepsItsModeAndWires(GameTestHelper helper) {
        for (String id : List.of("operator_conditional_select", "operator_list_conditional_select")) {
            var piece = (SpellPiece & PsitweaksModeConfigurable) PsiAPI.SPELL_PIECE_REGISTRY.get(Psitweaks.location(id))
                    .create(new Spell());
            var condition = piece.params.get(PsitweaksSpellParams.CONDITION);
            helper.assertTrue(condition != null && piece.params.get(PsitweaksSpellParams.IF_TRUE) != null
                    && piece.params.get(PsitweaksSpellParams.IF_FALSE) != null, "Missing an input: " + id);
            piece.paramSides.put(condition, SpellParam.Side.LEFT);
            piece.paramSides.put(piece.params.get(PsitweaksSpellParams.IF_TRUE), SpellParam.Side.TOP);
            piece.setModeOption(PsitweaksModeOptions.VECTOR);
            helper.assertTrue(piece.paramSides.get(piece.params.get(PsitweaksSpellParams.CONDITION)) == SpellParam.Side.LEFT
                            && piece.paramSides.get(piece.params.get(PsitweaksSpellParams.IF_TRUE)) == SpellParam.Side.TOP,
                    "A mode change must keep the wires: " + id);

            var tag = new CompoundTag();
            piece.writeToNBT(tag);
            var reloaded = SpellPiece.createFromNBT(new Spell(), tag);
            Class<?> expected = id.equals("operator_conditional_select") ? Vector3.class : VectorListWrapper.class;
            helper.assertTrue(reloaded != null && reloaded.getEvaluationType() == expected
                            && reloaded.paramSides.get(reloaded.params.get(PsitweaksSpellParams.IF_TRUE)) == SpellParam.Side.TOP,
                    "Vector mode or its wire lost on reload: " + id);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void listConditionalSelectPicksAWholeList(GameTestHelper helper) throws SpellRuntimeException {
        Map<String, Object> inputs = new HashMap<>();
        var select = new PieceOperatorListConditionalSelect(new Spell()) {
            @Override public Object getRawParamValue(SpellContext context, SpellParam<?> param) {
                return inputs.get(param.name);
            }
        };
        select.setModeOption(PsitweaksModeOptions.NUMBER);
        var first = list(1.0, 2.0);
        var second = list(3.0);
        inputs.put(PsitweaksSpellParams.IF_TRUE, first);
        inputs.put(PsitweaksSpellParams.IF_FALSE, second);
        inputs.put(PsitweaksSpellParams.CONDITION, -1.0);
        helper.assertTrue(select.getEvaluationType() == NumberListWrapper.class
                        && select.params.get(PsitweaksSpellParams.IF_TRUE) instanceof ParamNumberListWrapper,
                "Number mode must take and return Number Lists");
        helper.assertTrue(select.execute(new SpellContext()) == first, "-1 must pick the If True List");
        inputs.put(PsitweaksSpellParams.CONDITION, 0.5);
        helper.assertTrue(select.execute(new SpellContext()) == second, "0.5 must pick the If False List");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void matrixReshapeFillsRowByRow(GameTestHelper helper) throws SpellRuntimeException {
        Map<String, Object> inputs = new HashMap<>();
        var reshape = new PieceOperatorMatrixReshape(new Spell()) {
            @Override public Object getRawParamValue(SpellContext context, SpellParam<?> param) {
                return inputs.get(param.name);
            }
        };
        inputs.put(PsitweaksSpellParams.VECTOR_OR_NUMBER_LIST, list(1.0, 2.0, 3.0, 4.0, 5.0, 6.0));
        inputs.put(SpellParam.GENERIC_NAME_NUMBER1, 2.0);
        var matrix = (MatrixValue) reshape.execute(new SpellContext());
        helper.assertTrue(matrix.rows() == 2 && matrix.cols() == 3 && matrix.get(0, 2) == 3.0 && matrix.get(1, 0) == 4.0,
                "6 elements in 2 rows must give a 2x3 matrix filled row by row: " + matrix);
        helper.assertTrue(numbers(MatrixOperations.flatten(matrix)).equals(List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)),
                "Matrix Flatten must undo Matrix Reshape");

        inputs.put(SpellParam.GENERIC_NAME_NUMBER1, 3.0);
        inputs.put(SpellParam.GENERIC_NAME_NUMBER2, 2.0);
        matrix = (MatrixValue) reshape.execute(new SpellContext());
        helper.assertTrue(matrix.rows() == 3 && matrix.cols() == 2 && matrix.get(2, 1) == 6.0, "3x2 must be built: " + matrix);

        inputs.put(SpellParam.GENERIC_NAME_NUMBER2, 3.0);
        helper.assertTrue(MatrixOperations.ERROR_INCOMPATIBLE_SIZES.equals(errorOf(reshape)),
                "Rows x columns must match the element count");
        inputs.remove(SpellParam.GENERIC_NAME_NUMBER2);
        inputs.put(SpellParam.GENERIC_NAME_NUMBER1, 4.0);
        helper.assertTrue(MatrixOperations.ERROR_INCOMPATIBLE_SIZES.equals(errorOf(reshape)),
                "Without Number B the element count must divide by the rows");
        inputs.put(SpellParam.GENERIC_NAME_NUMBER1, 1.0);
        helper.assertTrue(MatrixOperations.ERROR_INVALID_DIMENSION.equals(errorOf(reshape)),
                "6 columns must be refused");

        inputs.put(PsitweaksSpellParams.VECTOR_OR_NUMBER_LIST, new Vector3(7, 8, 9));
        inputs.put(SpellParam.GENERIC_NAME_NUMBER1, 3.0);
        matrix = (MatrixValue) reshape.execute(new SpellContext());
        helper.assertTrue(matrix.rows() == 3 && matrix.cols() == 1 && matrix.get(1, 0) == 8.0,
                "A Vector in 3 rows must give a column matrix: " + matrix);
        helper.assertTrue(reshape.getEvaluationType() == MatrixValue.class, "Matrix Reshape must return a Matrix");

        inputs.remove(SpellParam.GENERIC_NAME_NUMBER1);
        matrix = (MatrixValue) reshape.execute(new SpellContext());
        helper.assertTrue(matrix.rows() == 3 && matrix.cols() == 1 && matrix.get(2, 0) == 9.0,
                "Without both counts a Vector must give a column matrix: " + matrix);
        inputs.put(PsitweaksSpellParams.VECTOR_OR_NUMBER_LIST, list(1.0, 2.0, 3.0, 4.0, 5.0, 6.0));
        inputs.put(SpellParam.GENERIC_NAME_NUMBER2, 2.0);
        matrix = (MatrixValue) reshape.execute(new SpellContext());
        helper.assertTrue(matrix.rows() == 3 && matrix.cols() == 2 && matrix.get(1, 0) == 3.0,
                "Without Number A the rows must be the element count divided by Number B: " + matrix);
        inputs.put(SpellParam.GENERIC_NAME_NUMBER2, 4.0);
        helper.assertTrue(MatrixOperations.ERROR_INCOMPATIBLE_SIZES.equals(errorOf(reshape)),
                "Without Number A the element count must divide by the columns");
        inputs.remove(SpellParam.GENERIC_NAME_NUMBER2);
        helper.assertTrue(MatrixOperations.ERROR_INVALID_DIMENSION.equals(errorOf(reshape)),
                "A column of 6 must be refused");
        inputs.put(PsitweaksSpellParams.VECTOR_OR_NUMBER_LIST, NumberListWrapper.EMPTY);
        helper.assertTrue(MatrixOperations.ERROR_INVALID_DIMENSION.equals(errorOf(reshape)),
                "An empty List must be refused");
        helper.assertTrue(reshape.params.get(SpellParam.GENERIC_NAME_NUMBER1).canDisable
                        && reshape.params.get(SpellParam.GENERIC_NAME_NUMBER2).canDisable,
                "Both counts must be optional");
        helper.succeed();
    }

    /** Operator: Column Matrix From List was folded into Matrix Reshape; saved spells load it there with the wire kept. */
    @GameTest(template = TEMPLATE)
    public static void columnMatrixFromListLoadsAsMatrixReshape(GameTestHelper helper) {
        var oldId = Psitweaks.location("operator_matrix_column_from_list");
        helper.assertFalse(PsiAPI.SPELL_PIECE_REGISTRY.containsKey(oldId), "The old piece must not be registered");
        var piece = PsiAPI.SPELL_PIECE_REGISTRY.get(Psitweaks.location("operator_matrix_reshape")).create(new Spell());
        piece.paramSides.put(piece.params.get(PsitweaksSpellParams.VECTOR_OR_NUMBER_LIST), SpellParam.Side.LEFT);
        var tag = new CompoundTag();
        piece.writeToNBT(tag);
        // The old piece saved only its one input.
        var params = tag.getCompound("params");
        for (String key : List.copyOf(params.getAllKeys())) {
            if (key.contains("number1") || key.contains("number2")) {
                params.remove(key);
            }
        }
        tag.putString("key", oldId.toString());

        var restored = SpellPiece.createFromNBT(new Spell(), tag);
        helper.assertTrue(restored instanceof PieceOperatorMatrixReshape, "The old piece must load as Matrix Reshape: " + restored);
        helper.assertTrue(restored.paramSides.get(restored.params.get(PsitweaksSpellParams.VECTOR_OR_NUMBER_LIST)) == SpellParam.Side.LEFT,
                "The wire into the List must be kept");
        helper.assertTrue(!restored.paramSides.get(restored.params.get(SpellParam.GENERIC_NAME_NUMBER1)).isEnabled()
                        && !restored.paramSides.get(restored.params.get(SpellParam.GENERIC_NAME_NUMBER2)).isEnabled(),
                "Both counts must stay unconnected, so the result is a column matrix");
        helper.succeed();
    }

    private static PieceOperatorListAggregateBase aggregate(boolean average, Map<String, Object> inputs) {
        if (average) {
            return new PieceOperatorListAverage(new Spell()) {
                @Override public Object getRawParamValue(SpellContext context, SpellParam<?> param) {
                    return inputs.get(param.name);
                }
            };
        }
        return new PieceOperatorListSum(new Spell()) {
            @Override public Object getRawParamValue(SpellContext context, SpellParam<?> param) {
                return inputs.get(param.name);
            }
        };
    }

    @GameTest(template = TEMPLATE)
    public static void listSumAndAverageFoldNumbersAndVectors(GameTestHelper helper) throws SpellRuntimeException {
        Map<String, Object> inputs = new HashMap<>();
        var sum = aggregate(false, inputs);
        var average = aggregate(true, inputs);
        helper.assertTrue(sum.getModeOption().id().equals(PsitweaksModeOptions.NUMBER.id())
                        && sum.getEvaluationType() == Double.class
                        && sum.params.get(SpellParam.GENERIC_NAME_LIST) instanceof ParamNumberListWrapper,
                "A new List Sum must start in Number mode");

        inputs.put(SpellParam.GENERIC_NAME_LIST, list(1.0, 2.0, 3.0, 4.0));
        helper.assertTrue((Double) sum.execute(new SpellContext()) == 10.0, "1+2+3+4 must be 10");
        helper.assertTrue((Double) average.execute(new SpellContext()) == 2.5, "The average of 1..4 must be 2.5");
        inputs.put(SpellParam.GENERIC_NAME_LIST, NumberListWrapper.EMPTY);
        helper.assertTrue((Double) sum.execute(new SpellContext()) == 0.0, "An empty List must sum to 0");
        helper.assertTrue(PieceOperatorListAggregateBase.ERROR_EMPTY_LIST.equals(errorOf(average)),
                "The average of an empty List must be an error");
        inputs.put(SpellParam.GENERIC_NAME_LIST, list(Double.MAX_VALUE, Double.MAX_VALUE));
        helper.assertTrue(SpellRuntimeException.NAN.equals(errorOf(sum)), "An overflowing sum must be an error");

        sum.setModeOption(PsitweaksModeOptions.VECTOR);
        average.setModeOption(PsitweaksModeOptions.VECTOR);
        helper.assertTrue(sum.getEvaluationType() == Vector3.class
                        && sum.params.get(SpellParam.GENERIC_NAME_LIST) instanceof ParamVectorListWrapper,
                "Vector mode must take a Vector List and return a Vector");
        inputs.put(SpellParam.GENERIC_NAME_LIST, VectorListWrapper.make(List.of(
                new Vector3(0, 0, 0), new Vector3(2, 4, 0), new Vector3(4, 2, 6))));
        helper.assertTrue(near((Vector3) sum.execute(new SpellContext()), 6, 6, 6), "The vectors must add up");
        helper.assertTrue(near((Vector3) average.execute(new SpellContext()), 2, 2, 2), "The average must be the centroid");
        inputs.put(SpellParam.GENERIC_NAME_LIST, VectorListWrapper.make(List.of()));
        helper.assertTrue(near((Vector3) sum.execute(new SpellContext()), 0, 0, 0), "An empty List must sum to the zero vector");
        helper.assertTrue(PieceOperatorListAggregateBase.ERROR_EMPTY_LIST.equals(errorOf(average)),
                "The centroid of an empty List must be an error");

        var tag = new CompoundTag();
        PsiAPI.SPELL_PIECE_REGISTRY.get(Psitweaks.location("operator_list_average")).create(new Spell()).writeToNBT(tag);
        var fresh = (PieceOperatorListAggregateBase) SpellPiece.createFromNBT(new Spell(), tag);
        fresh.setModeOption(PsitweaksModeOptions.VECTOR);
        tag = new CompoundTag();
        fresh.writeToNBT(tag);
        helper.assertTrue(SpellPiece.createFromNBT(new Spell(), tag).getEvaluationType() == Vector3.class,
                "Vector mode must survive a reload");
        helper.succeed();
    }

    private static PieceOperatorNumberListArithmeticBase arithmetic(char op, Map<String, Object> inputs) {
        return switch (op) {
            case '+' -> new PieceOperatorNumberListAdd(new Spell()) {
                @Override public Object getRawParamValue(SpellContext context, SpellParam<?> param) {
                    return inputs.get(param.name);
                }
            };
            case '-' -> new PieceOperatorNumberListSubtract(new Spell()) {
                @Override public Object getRawParamValue(SpellContext context, SpellParam<?> param) {
                    return inputs.get(param.name);
                }
            };
            case '*' -> new PieceOperatorNumberListMultiply(new Spell()) {
                @Override public Object getRawParamValue(SpellContext context, SpellParam<?> param) {
                    return inputs.get(param.name);
                }
            };
            default -> new PieceOperatorNumberListDivide(new Spell()) {
                @Override public Object getRawParamValue(SpellContext context, SpellParam<?> param) {
                    return inputs.get(param.name);
                }
            };
        };
    }

    @GameTest(template = TEMPLATE)
    public static void numberListArithmeticWorksElementByElement(GameTestHelper helper) throws SpellRuntimeException {
        Map<String, Object> inputs = new HashMap<>();
        var add = arithmetic('+', inputs);
        var subtract = arithmetic('-', inputs);
        var multiply = arithmetic('*', inputs);
        var divide = arithmetic('/', inputs);

        inputs.put(PsitweaksSpellParams.VALUE1, list(1.0, 2.0, 3.0));
        inputs.put(PsitweaksSpellParams.VALUE2, 10.0);
        helper.assertTrue(numbers(add.execute(new SpellContext())).equals(List.of(11.0, 12.0, 13.0)),
                "A Number must be added to every element");
        inputs.put(PsitweaksSpellParams.VALUE3, list(100.0, 200.0, 300.0));
        helper.assertTrue(numbers(add.execute(new SpellContext())).equals(List.of(111.0, 212.0, 313.0)),
                "Value 3 must be added too");
        helper.assertTrue(numbers(subtract.execute(new SpellContext())).equals(List.of(-109.0, -208.0, -307.0)),
                "Subtract must take Value 2 and Value 3 from Value 1");
        helper.assertTrue(numbers(multiply.execute(new SpellContext())).equals(List.of(1000.0, 4000.0, 9000.0)),
                "Multiply must work element by element");
        helper.assertTrue(divide.params.get(PsitweaksSpellParams.VALUE3) == null
                        && add.params.get(PsitweaksSpellParams.VALUE3).canDisable,
                "Divide must have two inputs and the others an optional Value 3");
        helper.assertTrue(numbers(divide.execute(new SpellContext())).equals(List.of(0.1, 0.2, 0.3)),
                "Divide must ignore Value 3 and divide Value 1 by Value 2");

        inputs.put(PsitweaksSpellParams.VALUE3, list(1.0, 2.0));
        helper.assertTrue(PieceOperatorNumberListArithmeticBase.ERROR_SIZE_MISMATCH.equals(errorOf(add)),
                "Lists of different sizes must be an error");
        inputs.remove(PsitweaksSpellParams.VALUE3);
        inputs.put(PsitweaksSpellParams.VALUE1, 6.0);
        inputs.put(PsitweaksSpellParams.VALUE2, 4.0);
        helper.assertTrue(numbers(subtract.execute(new SpellContext())).equals(List.of(2.0)),
                "Numbers only must give one element");
        inputs.put(PsitweaksSpellParams.VALUE2, list(2.0, 0.0));
        helper.assertTrue(SpellRuntimeException.DIVIDE_BY_ZERO.equals(errorOf(divide)), "Dividing by 0 must be an error");
        inputs.put(PsitweaksSpellParams.VALUE1, NumberListWrapper.EMPTY);
        inputs.put(PsitweaksSpellParams.VALUE2, 3.0);
        helper.assertTrue(numbers(multiply.execute(new SpellContext())).isEmpty(), "An empty List must stay empty");
        inputs.put(PsitweaksSpellParams.VALUE1, list(Double.MAX_VALUE));
        helper.assertTrue(SpellRuntimeException.NAN.equals(errorOf(multiply)), "An overflow must be an error");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void numberSequenceCountsFromNumberA(GameTestHelper helper) throws SpellRuntimeException {
        Map<String, Object> inputs = new HashMap<>();
        var sequence = new PieceOperatorNumberSequence(new Spell()) {
            @Override public Object getRawParamValue(SpellContext context, SpellParam<?> param) {
                return inputs.get(param.name);
            }
        };
        inputs.put(SpellParam.GENERIC_NAME_NUMBER1, 0.0);
        inputs.put(SpellParam.GENERIC_NAME_NUMBER2, 5.0);
        helper.assertTrue(numbers(sequence.execute(new SpellContext())).equals(List.of(0.0, 1.0, 2.0, 3.0, 4.0)),
                "A=0, B=5 must give 0..4");
        inputs.put(SpellParam.GENERIC_NAME_NUMBER1, 1.0);
        inputs.put(SpellParam.GENERIC_NAME_NUMBER2, 3.9);
        inputs.put(SpellParam.GENERIC_NAME_NUMBER3, -0.5);
        helper.assertTrue(numbers(sequence.execute(new SpellContext())).equals(List.of(1.0, 0.5, 0.0)),
                "The step must apply and the count must be truncated");
        inputs.put(SpellParam.GENERIC_NAME_NUMBER2, 0.0);
        helper.assertTrue(numbers(sequence.execute(new SpellContext())).isEmpty(), "A count of 0 must give an empty List");
        inputs.put(SpellParam.GENERIC_NAME_NUMBER2, 8192.0);
        helper.assertTrue(numbers(sequence.execute(new SpellContext())).size() == 8192, "8192 elements must be allowed");
        for (double count : new double[] {-1.0, 8193.0}) {
            inputs.put(SpellParam.GENERIC_NAME_NUMBER2, count);
            helper.assertTrue(SpellRuntimeException.OUT_OF_BOUNDS.equals(errorOf(sequence)), count + " must be refused");
        }
        inputs.put(SpellParam.GENERIC_NAME_NUMBER2, Double.NaN);
        helper.assertTrue(errorOf(sequence) != null, "NaN must be refused");
        helper.assertTrue(sequence.params.get(SpellParam.GENERIC_NAME_NUMBER3).canDisable
                        && !sequence.params.get(SpellParam.GENERIC_NAME_NUMBER2).canDisable,
                "Only the step must be optional");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void entityPositionListMatchesEntityPosition(GameTestHelper helper) throws SpellRuntimeException {
        var level = helper.getLevel();
        Vec3 base = helper.absoluteVec(new Vec3(1.5, 1.0, 1.5));
        var stand = new ArmorStand(level, base.x, base.y, base.z);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(base.x + 2, base.y, base.z);
        var entities = EntityListWrapper.make(List.<Entity>of(player, stand));

        Map<String, Object> inputs = new HashMap<>();
        inputs.put(SpellParam.GENERIC_NAME_LIST, entities);
        var positions = new PieceOperatorEntityPositionList(new Spell()) {
            @Override public Object getRawParamValue(SpellContext context, SpellParam<?> param) {
                return inputs.get(param.name);
            }
        };
        var result = (VectorListWrapper) positions.execute(new SpellContext());

        List<Vector3> expected = new ArrayList<>();
        for (Entity entity : entities) {
            var single = new PieceOperatorEntityPosition(new Spell()) {
                @Override public Object getRawParamValue(SpellContext context, SpellParam<?> param) {
                    return entity;
                }
            };
            expected.add((Vector3) single.execute(new SpellContext()));
        }
        List<Vector3> actual = new ArrayList<>();
        result.forEach(actual::add);
        helper.assertTrue(actual.size() == 2
                        && near(actual.get(0), expected.get(0).x, expected.get(0).y, expected.get(0).z)
                        && near(actual.get(1), expected.get(1).x, expected.get(1).y, expected.get(1).z),
                "Each position must match Operator: Entity Position, in order: " + actual + " vs " + expected);
        int index = 0;
        for (Entity entity : entities) {
            double expectedY = entity == player ? base.y + player.getEyeHeight() : base.y;
            helper.assertTrue(near(actual.get(index).y, expectedY),
                    "A player must be measured at the eyes and an armor stand at the feet: " + actual);
            index++;
        }
        helper.assertTrue(positions.getEvaluationType() == VectorListWrapper.class, "The result must be a Vector List");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void newOperatorsAreRegisteredInTheirGroups(GameTestHelper helper) {
        Map<String, String> groups = Map.ofEntries(
                Map.entry("operator_entity_position_list", "string"),
                Map.entry("operator_atan2", "operator_atan"),
                Map.entry("operator_conditional_select", "operator_equal"),
                Map.entry("operator_list_conditional_select", "operator_equal"),
                Map.entry("operator_matrix_reshape", "matrix"),
                Map.entry("operator_list_sum", "string"),
                Map.entry("operator_list_average", "string"),
                Map.entry("operator_number_list_add", "string"),
                Map.entry("operator_number_list_subtract", "string"),
                Map.entry("operator_number_list_multiply", "string"),
                Map.entry("operator_number_list_divide", "string"),
                Map.entry("operator_number_sequence", "string"));
        groups.forEach((id, group) -> {
            var location = Psitweaks.location(id);
            helper.assertTrue(PsiAPI.SPELL_PIECE_REGISTRY.get(location) != null, "Not registered: " + id);
            var actual = PsiAPI.getPieceGroup(helper.getLevel().registryAccess(), location);
            helper.assertTrue(actual.isPresent() && actual.get().key().location().equals(Psitweaks.location(group)),
                    id + " must be in the " + group + " group: " + actual.map(holder -> holder.key().location()));
        });
        helper.succeed();
    }
}
