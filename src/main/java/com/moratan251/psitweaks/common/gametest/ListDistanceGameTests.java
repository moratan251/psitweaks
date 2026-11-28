package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.api.value.BlockValue;
import com.moratan251.psitweaks.common.spells.param.ParamBlockListWrapper;
import com.moratan251.psitweaks.common.spells.param.ParamVectorListWrapper;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListDistanceBase;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListDistanceElement;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListDistanceKeep;
import com.moratan251.psitweaks.common.spells.wrapper.BlockListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
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
import vazkii.psi.api.spell.param.ParamEntityListWrapper;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;
import vazkii.psi.common.spell.operator.entity.PieceOperatorClosestToPoint;

/**
 * Operator: List Keep By Distance keeps the n closest (or -n farthest) elements, and Operator: Distance-Ranked Element
 * returns the element at an index of the closest-first order, for Entity, Vector and Block Lists.
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ListDistanceGameTests {
    private static final String TEMPLATE = "psi110_empty";
    private static final String LIST = SpellParam.GENERIC_NAME_LIST;
    private static final String POSITION = SpellParam.GENERIC_NAME_POSITION;

    private ListDistanceGameTests() {
    }

    /** A piece that reads its List, position and Number from {@code inputs[0]}, {@code [1]} and {@code [2]}. */
    private static PieceOperatorListDistanceBase piece(boolean keep, Object[] inputs) {
        if (keep) {
            return new PieceOperatorListDistanceKeep(new Spell()) {
                @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                    return input(param, inputs);
                }
            };
        }
        return new PieceOperatorListDistanceElement(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return input(param, inputs);
            }
        };
    }

    private static Object input(SpellParam<?> param, Object[] inputs) {
        if (param.name.equals(LIST)) {
            return inputs[0];
        }
        return param.name.equals(POSITION) ? inputs[1] : inputs[2];
    }

    private static List<Double> xs(Object result) {
        List<Double> xs = new ArrayList<>();
        for (Vector3 vector : (VectorListWrapper) result) {
            xs.add(vector.x);
        }
        return xs;
    }

    private static Set<Entity> entities(Object result) {
        Set<Entity> set = new HashSet<>();
        for (Entity entity : (EntityListWrapper) result) {
            set.add(entity);
        }
        return set;
    }

    private static List<BlockPos> positions(Object result) {
        return ((BlockListWrapper) result).asList().stream().map(BlockValue::blockPos).collect(Collectors.toList());
    }

    private static boolean throwsOutOfBounds(PieceOperatorListDistanceBase piece, SpellContext context) {
        try {
            piece.execute(context);
            return false;
        } catch (SpellRuntimeException e) {
            return e.getMessage().equals("psi.spellerror.out_of_bounds");
        }
    }

    private static Vector3 x(double x) {
        return new Vector3(x, 0, 0);
    }

    @GameTest(template = TEMPLATE)
    public static void vectorListsAreSortedByDistance(GameTestHelper helper) throws SpellRuntimeException {
        // No focal point: the distance falls back to plain coordinates.
        var context = new SpellContext();
        Object[] inputs = {VectorListWrapper.make(List.of(x(5), x(1), x(3), x(-2))), x(0), 2.0};
        var keep = piece(true, inputs);
        var element = piece(false, inputs);
        keep.setModeOption(PsitweaksModeOptions.VECTOR);
        element.setModeOption(PsitweaksModeOptions.VECTOR);
        helper.assertTrue(keep.getEvaluationType() == VectorListWrapper.class
                        && element.getEvaluationType() == Vector3.class,
                "Vector mode must return a Vector List and a Vector");

        helper.assertTrue(xs(keep.execute(context)).equals(List.of(1.0, -2.0)), "2 must keep the two closest, closest first");
        inputs[2] = -2.0;
        helper.assertTrue(xs(keep.execute(context)).equals(List.of(5.0, 3.0)), "-2 must keep the two farthest, farthest first");
        inputs[2] = 2.7;
        helper.assertTrue(xs(keep.execute(context)).equals(List.of(1.0, -2.0)), "2.7 must count as 2");
        inputs[2] = 0.0;
        helper.assertTrue(xs(keep.execute(context)).isEmpty(), "0 must give an empty List");
        inputs[2] = 10.0;
        helper.assertTrue(xs(keep.execute(context)).equals(List.of(1.0, -2.0, 3.0, 5.0)),
                "A count over the size must keep every element, sorted");
        inputs[2] = -10.0;
        helper.assertTrue(xs(keep.execute(context)).equals(List.of(5.0, 3.0, -2.0, 1.0)),
                "A negative count over the size must keep every element, farthest first");

        inputs[2] = 0.0;
        helper.assertTrue(((Vector3) element.execute(context)).x == 1.0, "Index 0 must be the closest");
        inputs[2] = 1.0;
        helper.assertTrue(((Vector3) element.execute(context)).x == -2.0, "Index 1 must be the second closest");
        inputs[2] = -1.0;
        helper.assertTrue(((Vector3) element.execute(context)).x == 5.0, "Index -1 must be the farthest");
        inputs[2] = -4.0;
        helper.assertTrue(((Vector3) element.execute(context)).x == 1.0, "Index -4 of 4 must be the closest");
        inputs[2] = 4.0;
        helper.assertTrue(throwsOutOfBounds(element, context), "Index 4 of 4 must be out of bounds");
        inputs[2] = -5.0;
        helper.assertTrue(throwsOutOfBounds(element, context), "Index -5 of 4 must be out of bounds");
        inputs[0] = VectorListWrapper.EMPTY;
        inputs[2] = 0.0;
        helper.assertTrue(throwsOutOfBounds(element, context), "An empty List has no element 0");
        helper.assertTrue(xs(keep.execute(context)).isEmpty(), "Keeping from an empty List must give an empty List");

        inputs[0] = VectorListWrapper.make(List.of(x(2), x(-2)));
        inputs[2] = 1.0;
        helper.assertTrue(xs(keep.execute(context)).equals(List.of(2.0)), "Ties must keep the input order");
        inputs[0] = VectorListWrapper.make(List.of(x(-2), x(2)));
        helper.assertTrue(xs(keep.execute(context)).equals(List.of(-2.0)), "Ties must keep the input order");
        inputs[0] = VectorListWrapper.make(List.of(x(1), x(3), x(-3), x(-1)));
        inputs[2] = -3.0;
        helper.assertTrue(xs(keep.execute(context)).equals(List.of(3.0, -3.0, 1.0)),
                "Ties must keep the input order for a negative Number too, got " + xs(keep.execute(context)));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void entityListsMatchClosestToPoint(GameTestHelper helper) throws SpellRuntimeException {
        var level = helper.getLevel();
        Vec3 base = helper.absoluteVec(new Vec3(1.5, 1.0, 1.5));
        var context = new SpellContext();
        context.focalPoint = new ArmorStand(level, base.x, base.y, base.z);
        var near = new ArmorStand(level, base.x + 1, base.y, base.z);
        var mid = new ArmorStand(level, base.x + 3, base.y, base.z);
        var far = new ArmorStand(level, base.x + 6, base.y, base.z);
        var list = EntityListWrapper.make(List.<Entity>of(far, near, mid));
        var origin = new Vector3(base.x, base.y, base.z);
        Object[] inputs = {list, origin, 2.0};
        var keep = piece(true, inputs);
        var element = piece(false, inputs);
        helper.assertTrue(keep.getModeOption().id().equals(PsitweaksModeOptions.ENTITY.id())
                        && keep.params.get(LIST) instanceof ParamEntityListWrapper
                        && keep.getEvaluationType() == EntityListWrapper.class
                        && element.getEvaluationType() == Entity.class,
                "A new piece must start in Entity mode");

        helper.assertTrue(entities(keep.execute(context)).equals(Set.of(near, mid)), "2 must keep the two closest");
        inputs[2] = -1.0;
        helper.assertTrue(entities(keep.execute(context)).equals(Set.of(far)), "-1 must keep the farthest");
        inputs[2] = 0.0;
        helper.assertTrue(entities(keep.execute(context)).isEmpty(), "0 must give an empty List");

        helper.assertTrue(element.execute(context) == near, "Index 0 must be the closest");
        helper.assertTrue(element.execute(context) == PieceOperatorClosestToPoint.closestToPoint(context, origin, list),
                "Index 0 must match Operator: Closest to Point");
        inputs[2] = 1.0;
        helper.assertTrue(element.execute(context) == mid, "Index 1 must be the second closest");
        inputs[2] = -1.0;
        helper.assertTrue(element.execute(context) == far, "Index -1 must be the farthest");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void blockListsAreMeasuredAtTheBlockCenter(GameTestHelper helper) throws SpellRuntimeException {
        var level = helper.getLevel();
        BlockPos a = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockPos west = a.west();
        BlockPos far = a.east(3);
        var context = new SpellContext();
        // The origin is a's minimum corner. Measured at the centers, a and its west neighbour are equally close, so
        // the input order decides; measured at the corners, a would be closer.
        Object[] inputs = {
                BlockListWrapper.make(List.of(BlockValue.snapshot(level, west), BlockValue.snapshot(level, far),
                        BlockValue.snapshot(level, a))),
                new Vector3(a.getX(), a.getY(), a.getZ()), 1.0};
        var keep = piece(true, inputs);
        var element = piece(false, inputs);
        keep.setModeOption(PsitweaksModeOptions.BLOCK);
        element.setModeOption(PsitweaksModeOptions.BLOCK);
        helper.assertTrue(keep.params.get(LIST) instanceof ParamBlockListWrapper
                        && keep.getEvaluationType() == BlockListWrapper.class
                        && element.getEvaluationType() == BlockValue.class,
                "Block mode must take a Block List and return a Block List and a Block");

        helper.assertTrue(positions(keep.execute(context)).equals(List.of(west)), "Blocks must be measured at their center");
        inputs[2] = 3.0;
        helper.assertTrue(positions(keep.execute(context)).equals(List.of(west, a, far)), "Blocks must come out closest first");
        inputs[2] = -1.0;
        helper.assertTrue(((BlockValue) element.execute(context)).blockPos().equals(far), "Index -1 must be the farthest block");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void distancePiecesAreRegisteredAndKeepTheirMode(GameTestHelper helper) {
        for (String id : List.of("operator_list_distance_keep", "operator_list_distance_element")) {
            var type = PsiAPI.SPELL_PIECE_REGISTRY.get(Psitweaks.location(id));
            helper.assertTrue(type != null, "Not registered: " + id);
            var piece = (PieceOperatorListDistanceBase) type.create(new Spell());
            var modes = piece.getAvailableModeOptions().stream().map(option -> option.id()).collect(Collectors.toList());
            helper.assertTrue(modes.equals(List.of(PsitweaksModeOptions.ENTITY.id(), PsitweaksModeOptions.VECTOR.id(),
                    PsitweaksModeOptions.BLOCK.id())), "Only Entity, Vector and Block have positions: " + modes);
            String numberName = SpellParam.GENERIC_NAME_NUMBER;
            helper.assertTrue(piece.params.get(POSITION) != null && piece.params.get(numberName) != null,
                    "Missing the position or number input: " + id);

            piece.paramSides.put(piece.params.get(LIST), SpellParam.Side.RIGHT);
            piece.paramSides.put(piece.params.get(numberName), SpellParam.Side.TOP);
            piece.setModeOption(PsitweaksModeOptions.VECTOR);
            helper.assertTrue(piece.params.get(LIST) instanceof ParamVectorListWrapper
                            && piece.paramSides.get(piece.params.get(LIST)) == SpellParam.Side.RIGHT
                            && piece.paramSides.get(piece.params.get(numberName)) == SpellParam.Side.TOP,
                    "A mode change must keep the wires: " + id);

            var tag = new CompoundTag();
            piece.writeToNBT(tag);
            var reloaded = (PieceOperatorListDistanceBase) SpellPiece.createFromNBT(new Spell(), tag);
            helper.assertTrue(reloaded.getModeOption().id().equals(PsitweaksModeOptions.VECTOR.id())
                            && reloaded.params.get(LIST) instanceof ParamVectorListWrapper
                            && reloaded.paramSides.get(reloaded.params.get(LIST)) == SpellParam.Side.RIGHT,
                    "Vector mode or its wire lost on reload: " + id);
        }
        helper.succeed();
    }
}
