package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.api.value.BlockValue;
import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.compat.BlockResourceChemicalChecks;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.ItemEntitySource;
import com.moratan251.psitweaks.common.spells.item.SpellItemFilters;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.item.StrictItemFilter;
import com.moratan251.psitweaks.common.spells.mode.BlockResourceModes;
import com.moratan251.psitweaks.common.spells.param.ParamBlockListWrapper;
import com.moratan251.psitweaks.common.spells.param.ParamSpellItemFilter;
import com.moratan251.psitweaks.common.spells.param.ParamSpellItemValue;
import com.moratan251.psitweaks.common.spells.param.ParamVectorListWrapper;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.*;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorHeldItem;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorHeldItems;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.MassBlockBreakHelper;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickIdeaStorageSlotBase;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickPullItem;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickShowRegion;
import com.moratan251.psitweaks.common.spells.wrapper.BlockListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import com.moratan251.psitweaks.common.storage.connector.ConnectorResource;
import com.moratan251.psitweaks.common.storage.connector.ConnectorSideMode;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageService;
import com.moratan251.psitweaks.common.storage.idea.ItemResourceKey;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamEntityListWrapper;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

/**
 * Ported from 1.21.1 (commit cdda583): the Item, Block and Matrix pieces of 0.10.10: block reads, dropped items,
 * region tools and Item List filters.
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ItemBlockMatrixGameTests {
    private static final String LIST = SpellParam.GENERIC_NAME_LIST;
    private static final String TARGET = SpellParam.GENERIC_NAME_TARGET;
    private static final String DIRECTION = SpellParam.GENERIC_NAME_DIRECTION;
    private static final String NUMBER = SpellParam.GENERIC_NAME_NUMBER;
    private static final String TIME = SpellParam.GENERIC_NAME_TIME;
    private static final String POSITION = SpellParam.GENERIC_NAME_POSITION;
    private static final String MATRIX = PsitweaksSpellParams.MATRIX;
    private static final String TAG_MODE = "psitweaksMode";

    private interface SpellAction {
        void run() throws SpellRuntimeException;
    }

    private static SpellPiece create(String id) {
        return SpellPiece.create(PsiAPI.getSpellPiece(Psitweaks.location(id)), new Spell());
    }

    private static FakePlayer fakeCaster(GameTestHelper helper, BlockPos relative, String name) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        player.moveTo(Vec3.atBottomCenterOf(helper.absolutePos(relative)));
        return player;
    }

    private static Vector3 vector(BlockPos pos) {
        return new Vector3(pos.getX(), pos.getY(), pos.getZ());
    }

    /** A cuboid region matrix from its start corner and edge length. */
    private static MatrixValue cube(Vector3 start, double edge) {
        return new MatrixValue(4, 4, new double[]{
                edge, 0, 0, start.x,
                0, edge, 0, start.y,
                0, 0, edge, start.z,
                0, 0, 0, 1});
    }

    private static boolean near(Vector3 vector, double x, double y, double z) {
        return Math.abs(vector.x - x) < 1e-9 && Math.abs(vector.y - y) < 1e-9 && Math.abs(vector.z - z) < 1e-9;
    }

    private static String key(Vector3 vector) {
        return Math.round(vector.x * 1000) + "," + Math.round(vector.y * 1000) + "," + Math.round(vector.z * 1000);
    }

    /** The error key a spell action fails with, or null when it succeeds. */
    private static String error(SpellAction action) {
        try {
            action.run();
            return null;
        } catch (SpellRuntimeException failure) {
            return failure.getMessage();
        }
    }

    private static long stored(PlayerIdeaStorage storage, ItemStack template) {
        return storage.simulateExtract(ItemResourceKey.of(template).orElseThrow(), Long.MAX_VALUE);
    }

    // 1.20.1 has no data components: the display name is an NBT tag.
    private static ItemStack named(ItemStack stack, String name) {
        stack.setHoverName(Component.literal(name));
        return stack;
    }

    @GameTest(template = "psi110_empty")
    public static void newPiecesAreRegisteredWithTheirGroupsTypesAndStats(GameTestHelper helper) throws Exception {
        // 1.20.1 has no shared block/item/matrix groups: each new piece is researched in its own group named after its ID.
        List<String> pieces = List.of("operator_block_item", "operator_block_resource_amount", "operator_block_free_capacity",
                "operator_dropped_items", "operator_matrix_caster_basis", "operator_matrix_rotation",
                "operator_matrix_translation", "trick_show_region", "operator_strict_item");
        Map<String, Class<?>> outputs = Map.of(
                "operator_strict_item", StrictItemFilter.class,
                "operator_block_item", SpellItemValue.class,
                "operator_block_resource_amount", Double.class,
                "operator_block_free_capacity", Double.class,
                "operator_dropped_items", SpellItemListWrapper.class,
                "operator_matrix_caster_basis", MatrixValue.class,
                "operator_matrix_rotation", MatrixValue.class,
                "operator_matrix_translation", MatrixValue.class);
        for (String name : pieces) {
            var id = Psitweaks.location(name);
            helper.assertTrue(PsiAPI.isPieceRegistered(id), "Piece is not registered: " + id);
            helper.assertTrue(id.equals(PsiAPI.getGroupForPiece(PsiAPI.getSpellPiece(id))),
                    "Piece is researched in the wrong group: " + id);
            var piece = create(name);
            Class<?> output = outputs.get(name);
            helper.assertTrue(output == null || piece.getEvaluationType() == output, "Unexpected output type: " + id);
        }

        var show = create("trick_show_region");
        var metadata = new SpellMetadata();
        show.addToMetadata(metadata);
        helper.assertTrue(metadata.getStat(EnumSpellStat.POTENCY) == 10 && metadata.getStat(EnumSpellStat.COST) == 0,
                "Show region must be a cheap preview: Potency 10");
        helper.assertTrue(show.params.get(TIME).canDisable && !show.params.get(TIME).canAccept(create("operator_block_resource_amount")),
                "Time must be an optional constant like Trick: Particle Trail");
        var amount = create("operator_block_resource_amount");
        var free = create("operator_block_free_capacity");
        helper.assertTrue(amount.params.get(PsitweaksSpellParams.STRING).canDisable
                        && !free.params.get(PsitweaksSpellParams.STRING).canDisable
                        && amount.params.get(DIRECTION).canDisable && !amount.params.get(TARGET).canDisable,
                "Amount filters optionally, free capacity needs one ID, and Direction is optional");
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void regionFiltersTakeEntityVectorAndBlockLists(GameTestHelper helper) throws Exception {
        var level = helper.getLevel();
        var context = new SpellContext();
        Object[] list = {null};
        MatrixValue region = cube(new Vector3(-0.5, -0.5, -0.5), 3);
        var inside = new PieceOperatorInsideRegion(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return param.name.equals(LIST) ? list[0] : region;
            }
        };
        var outside = new PieceOperatorOutsideRegion(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return param.name.equals(LIST) ? list[0] : region;
            }
        };
        helper.assertTrue(inside.getModeOption().id().equals(PsitweaksModeOptions.ENTITY.id())
                        && inside.params.get(LIST) instanceof ParamEntityListWrapper
                        && inside.getEvaluationType() == EntityListWrapper.class,
                "Region filters must start as Entity List filters, like before the mode menu");

        inside.paramSides.put(inside.params.get(LIST), SpellParam.Side.LEFT);
        inside.paramSides.put(inside.params.get(MATRIX), SpellParam.Side.TOP);
        for (var mode : List.of(PsitweaksModeOptions.VECTOR, PsitweaksModeOptions.BLOCK, PsitweaksModeOptions.ENTITY)) {
            inside.setModeOption(mode);
            helper.assertTrue(inside.params.size() == 2 && inside.paramSides.get(inside.params.get(LIST)) == SpellParam.Side.LEFT
                    && inside.paramSides.get(inside.params.get(MATRIX)) == SpellParam.Side.TOP, "Mode switch lost a wire: " + mode.id());
        }

        inside.setModeOption(PsitweaksModeOptions.VECTOR);
        outside.setModeOption(PsitweaksModeOptions.VECTOR);
        helper.assertTrue(inside.params.get(LIST) instanceof ParamVectorListWrapper && inside.getEvaluationType() == VectorListWrapper.class,
                "Vector mode must take and return a Vector List");
        list[0] = VectorListWrapper.make(List.of(new Vector3(3, 0, 0), new Vector3(0, 0, 0), new Vector3(-0.6, 1, 1),
                new Vector3(2.5, 2.5, -0.5)));
        var vectorsIn = ((VectorListWrapper) inside.execute(context)).asList();
        var vectorsOut = ((VectorListWrapper) outside.execute(context)).asList();
        helper.assertTrue(vectorsIn.size() == 2 && near(vectorsIn.get(0), 0, 0, 0) && near(vectorsIn.get(1), 2.5, 2.5, -0.5),
                "Vector mode must keep the vectors inside, boundary included, in order: " + vectorsIn);
        helper.assertTrue(vectorsOut.size() == 2 && near(vectorsOut.get(0), 3, 0, 0) && near(vectorsOut.get(1), -0.6, 1, 1),
                "Outside must keep exactly the rest: " + vectorsOut);

        inside.setModeOption(PsitweaksModeOptions.BLOCK);
        outside.setModeOption(PsitweaksModeOptions.BLOCK);
        helper.assertTrue(inside.params.get(LIST) instanceof ParamBlockListWrapper && inside.getEvaluationType() == BlockListWrapper.class,
                "Block mode must take and return a Block List");
        var stone = Blocks.STONE.defaultBlockState();
        list[0] = BlockListWrapper.make(List.of(new BlockValue(level.dimension(), new BlockPos(3, 0, 0), stone),
                new BlockValue(level.dimension(), new BlockPos(0, 0, 0), stone),
                new BlockValue(level.dimension(), new BlockPos(2, 2, 2), Blocks.DIRT.defaultBlockState())));
        var blocksIn = ((BlockListWrapper) inside.execute(context)).asList();
        var blocksOut = ((BlockListWrapper) outside.execute(context)).asList();
        helper.assertTrue(blocksIn.size() == 2 && blocksIn.get(0).blockPos().equals(BlockPos.ZERO)
                        && blocksIn.get(1).blockPos().equals(new BlockPos(2, 2, 2)) && blocksIn.get(1).state().is(Blocks.DIRT),
                "Block mode must test block positions and keep the blocks: " + blocksIn);
        helper.assertTrue(blocksOut.size() == 1 && blocksOut.get(0).blockPos().equals(new BlockPos(3, 0, 0)),
                "Outside must keep the block beyond the region: " + blocksOut);
        var regionBlocks = new HashSet<>(MassBlockBreakHelper.collectRegionPositions(region));
        helper.assertTrue(regionBlocks.size() == 27 && regionBlocks.contains(BlockPos.ZERO) && regionBlocks.contains(new BlockPos(2, 2, 2))
                && !regionBlocks.contains(new BlockPos(3, 0, 0)), "Block mode must agree with Operator: Region Vector List");

        inside.setModeOption(PsitweaksModeOptions.ENTITY);
        outside.setModeOption(PsitweaksModeOptions.ENTITY);
        var near = new ItemEntity(level, 1, 0, 1, new ItemStack(Items.APPLE));
        var far = new ItemEntity(level, 4, 0, 1, new ItemStack(Items.APPLE));
        list[0] = EntityListWrapper.make(List.<Entity>of(near, far));
        var entitiesIn = (EntityListWrapper) inside.execute(context);
        var entitiesOut = (EntityListWrapper) outside.execute(context);
        helper.assertTrue(entitiesIn.size() == 1 && entitiesIn.get(0) == near && entitiesOut.size() == 1 && entitiesOut.get(0) == far,
                "Entity mode must test the bounding box centers");

        var saved = (PieceOperatorRegionFilterBase) create("operator_inside_region");
        saved.setModeOption(PsitweaksModeOptions.BLOCK);
        saved.paramSides.put(saved.params.get(LIST), SpellParam.Side.RIGHT);
        saved.paramSides.put(saved.params.get(MATRIX), SpellParam.Side.BOTTOM);
        var tag = new CompoundTag();
        saved.writeToNBT(tag);
        var reloaded = (PieceOperatorRegionFilterBase) SpellPiece.createFromNBT(new Spell(), tag);
        helper.assertTrue(reloaded.getModeOption().id().equals(PsitweaksModeOptions.BLOCK.id())
                        && reloaded.params.get(LIST) instanceof ParamBlockListWrapper
                        && reloaded.paramSides.get(reloaded.params.get(LIST)) == SpellParam.Side.RIGHT
                        && reloaded.paramSides.get(reloaded.params.get(MATRIX)) == SpellParam.Side.BOTTOM,
                "Block mode or its wires lost on reload");
        saved.setModeOption(PsitweaksModeOptions.ENTITY);
        var legacy = new CompoundTag();
        saved.writeToNBT(legacy);
        legacy.remove(TAG_MODE);
        var old = (PieceOperatorRegionFilterBase) SpellPiece.createFromNBT(new Spell(), legacy);
        helper.assertTrue(old.getModeOption().id().equals(PsitweaksModeOptions.ENTITY.id())
                        && old.params.get(LIST) instanceof ParamEntityListWrapper
                        && old.paramSides.get(old.params.get(LIST)) == SpellParam.Side.RIGHT,
                "Spells saved before the mode menu must load as Entity List filters with their wires");
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void matrixBuildersMatchPsiVectorOperations(GameTestHelper helper) throws Exception {
        var context = new SpellContext();
        Object[] offset = {new Vector3(1, 2, 3)};
        var translation = new PieceOperatorMatrixTranslation(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return offset[0]; }
        };
        var moved = (MatrixValue) translation.execute(context);
        helper.assertTrue(moved.rows() == 4 && moved.cols() == 4
                        && near(MatrixOperations.transformVector(moved, new Vector3(1, 1, 1)), 2, 3, 4),
                "Translation matrix must move points by the offset");

        Object[] axis = {new Vector3(0, 1, 0)};
        double[] angle = {Math.PI / 2};
        var rotation = new PieceOperatorMatrixRotation(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return param.name.equals(SpellParam.GENERIC_NAME_AXIS) ? axis[0] : angle[0];
            }
        };
        var quarter = (MatrixValue) rotation.execute(context);
        for (double value : quarter.valuesCopy()) {
            helper.assertTrue(value == Math.rint(value), "Quarter turns must be exact: " + quarter);
        }
        Vector3 psiTurn = new Vector3(1, 0, 0).rotate(Math.PI / 2, new Vector3(0, 1, 0));
        helper.assertTrue(near(MatrixOperations.transformVector(quarter, new Vector3(1, 0, 0)),
                Math.rint(psiTurn.x), Math.rint(psiTurn.y), Math.rint(psiTurn.z)), "Quarter turn must match Operator: Rotate Vector");
        axis[0] = new Vector3(1, 2, -0.5);
        angle[0] = 0.7;
        var general = (MatrixValue) rotation.execute(context);
        for (Vector3 point : List.of(new Vector3(1, 0, 0), new Vector3(0.3, -2, 5), new Vector3(-4, 1, 0.25))) {
            Vector3 expected = point.copy().rotate(0.7, new Vector3(1, 2, -0.5));
            helper.assertTrue(near(MatrixOperations.transformVector(general, point), expected.x, expected.y, expected.z),
                    "Rotation matrix must turn " + point + " like Operator: Rotate Vector");
        }
        axis[0] = new Vector3(0, 0, 0);
        helper.assertTrue(SpellRuntimeException.NULL_VECTOR.equals(error(() -> rotation.execute(context))), "A zero axis must fail");

        Object[] vectors = {VectorListWrapper.make(List.of(new Vector3(0, 0, 0), new Vector3(1, 1, 1)))};
        Object[] matrix = {moved};
        var transform = new PieceOperatorMatrixTransformVector(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return param.name.equals(SpellParam.GENERIC_NAME_TARGET) ? vectors[0] : matrix[0];
            }
        };
        helper.assertTrue(transform.getModeOption() == PieceOperatorMatrixTransformVector.VECTOR
                        && transform.getEvaluationType() == Vector3.class,
                "Matrix Transform Vector must start in Vector mode");
        var legacy = new PieceOperatorMatrixTransformVector(new Spell());
        legacy.readFromNBT(new CompoundTag());
        helper.assertTrue(legacy.getModeOption() == PieceOperatorMatrixTransformVector.VECTOR,
                "Spells saved without a mode must load in Vector mode");
        var toSave = (PieceOperatorMatrixTransformVector) create("operator_matrix_transform_vector");
        toSave.setModeOption(PieceOperatorMatrixTransformVector.VECTOR_LIST);
        var saved = new CompoundTag();
        toSave.writeToNBT(saved);
        legacy.readFromNBT(saved);
        transform.setModeOption(PieceOperatorMatrixTransformVector.VECTOR_LIST);
        helper.assertTrue(legacy.isListMode() && legacy.getEvaluationType() == VectorListWrapper.class,
                "Vector List mode must be saved and output a Vector List");
        var transformed = ((VectorListWrapper) transform.execute(context)).asList();
        helper.assertTrue(transformed.size() == 2 && near(transformed.get(0), 1, 2, 3) && near(transformed.get(1), 2, 3, 4),
                "Each vector must be transformed in order");
        var level = helper.getLevel();
        vectors[0] = BlockListWrapper.make(List.of(new BlockValue(level.dimension(), new BlockPos(5, 5, 5), Blocks.STONE.defaultBlockState()),
                new BlockValue(level.dimension(), BlockPos.ZERO, Blocks.STONE.defaultBlockState())));
        transformed = ((VectorListWrapper) transform.execute(context)).asList();
        helper.assertTrue(transformed.size() == 2 && near(transformed.get(0), 6, 7, 8) && near(transformed.get(1), 1, 2, 3),
                "A Block List must be read as its block positions");
        matrix[0] = new MatrixValue(4, 4, new double[]{1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0});
        helper.assertTrue(error(() -> transform.execute(context)) != null, "A vector that cannot be transformed must fail the whole list");

        var blockListPiece = new PieceOperatorInsideRegion(new Spell());
        blockListPiece.setModeOption(PsitweaksModeOptions.BLOCK);
        helper.assertTrue(new ParamVectorListWrapper(LIST, 0, false, false).canAccept(blockListPiece)
                        && !new ParamVectorListWrapper(LIST, 0, false, true).canAccept(blockListPiece),
                "Vector List inputs must accept a Block List unless they need a constant");
        var itemList = create("operator_dropped_items");
        var item = create("operator_block_item");
        helper.assertTrue(new ParamSpellItemFilter(PsitweaksSpellParams.ITEM, 0, true, false).canAccept(itemList)
                        && new ParamSpellItemFilter(PsitweaksSpellParams.ITEM, 0, true, false).canAccept(item)
                        && !new ParamSpellItemValue(PsitweaksSpellParams.ITEM, 0, true, false).canAccept(itemList),
                "Only Item filter inputs accept an Item List");
        helper.succeed();
    }

    private static boolean columns(MatrixValue matrix, int[] right, int[] up, int[] forward, Vector3 origin) {
        int[][] expected = {right, up, forward};
        for (int col = 0; col < 3; col++) {
            for (int row = 0; row < 3; row++) {
                if (matrix.get(row, col) != expected[col][row]) {
                    return false;
                }
            }
        }
        return matrix.rows() == 4 && matrix.cols() == 4 && near(new Vector3(matrix.get(0, 3), matrix.get(1, 3), matrix.get(2, 3)),
                origin.x, origin.y, origin.z) && matrix.get(3, 0) == 0 && matrix.get(3, 1) == 0 && matrix.get(3, 2) == 0
                && matrix.get(3, 3) == 1;
    }

    @GameTest(template = "connector_empty")
    public static void casterBasisFollowsTheCasterAndTheFaceItLooksAt(GameTestHelper helper) throws Exception {
        var player = fakeCaster(helper, new BlockPos(2, 0, 0), "basis-caster");
        BlockPos feet = helper.absolutePos(new BlockPos(2, 0, 0));
        Object[] origin = {null};
        var basis = new PieceOperatorMatrixCasterBasis(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return origin[0]; }
        };
        int[] east = {1, 0, 0}, west = {-1, 0, 0}, up = {0, 1, 0}, down = {0, -1, 0}, south = {0, 0, 1}, north = {0, 0, -1};
        player.setYRot(0);
        player.setXRot(-60);
        helper.assertTrue(columns((MatrixValue) basis.execute(new SpellContext().setPlayer(player)), west, up, south, vector(feet)),
                "2D facing south: right = west, forward stays level, origin = the caster's block");
        player.setYRot(90);
        helper.assertTrue(columns((MatrixValue) basis.execute(new SpellContext().setPlayer(player)), north, up, west, vector(feet)),
                "2D facing west: right = north");
        origin[0] = new Vector3(10, 20, 30);
        helper.assertTrue(columns((MatrixValue) basis.execute(new SpellContext().setPlayer(player)), north, up, west, new Vector3(10, 20, 30)),
                "Position must replace the origin");
        origin[0] = null;

        basis.setModeOption(PieceOperatorMatrixCasterBasis.FULL);
        player.setYRot(0);
        player.setXRot(-90);
        helper.assertTrue(columns((MatrixValue) basis.execute(new SpellContext().setPlayer(player)), west, north, up, vector(feet)),
                "3D looking up: forward = up, up = away from the caster's back");
        player.setXRot(90);
        helper.assertTrue(columns((MatrixValue) basis.execute(new SpellContext().setPlayer(player)), west, south, down, vector(feet)),
                "3D looking down: up = the caster's facing");
        player.setXRot(10);
        helper.assertTrue(columns((MatrixValue) basis.execute(new SpellContext().setPlayer(player)), west, up, south, vector(feet)),
                "3D looking almost level: same frame as 2D");

        basis.setModeOption(PieceOperatorMatrixCasterBasis.FACE);
        helper.setBlock(new BlockPos(2, 1, 3), Blocks.STONE);
        player.setXRot(0);
        helper.assertTrue(columns((MatrixValue) basis.execute(new SpellContext().setPlayer(player)), west, up, south,
                        vector(helper.absolutePos(new BlockPos(2, 1, 3)))),
                "Face mode on a side face: forward into the block, origin = the block looked at");
        helper.setBlock(new BlockPos(1, 0, 3), Blocks.STONE);
        player.moveTo(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(1, 1, 3))));
        player.setYRot(90);
        player.setXRot(90);
        helper.assertTrue(columns((MatrixValue) basis.execute(new SpellContext().setPlayer(player)), north, west, down,
                        vector(helper.absolutePos(new BlockPos(1, 0, 3)))),
                "Face mode on a top face: up follows the caster's horizontal facing");
        player.moveTo(Vec3.atBottomCenterOf(feet.above(100)));
        player.setXRot(-90);
        helper.assertTrue(SpellRuntimeException.NULL_VECTOR.equals(error(() -> basis.execute(new SpellContext().setPlayer(player)))),
                "Face mode must fail when the caster looks at nothing");

        // The book example: a 3x3 tunnel five blocks deep in front of the caster.
        player.moveTo(Vec3.atBottomCenterOf(feet));
        var cuboid = new PieceOperatorMatrixCuboidRegion(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return param.name.equals(POSITION) ? new Vector3(0, 1, 3) : new Vector3(3, 3, 5);
            }
        };
        var local = (MatrixValue) cuboid.execute(new SpellContext().setPlayer(player));
        basis.setModeOption(PieceOperatorMatrixCasterBasis.HORIZONTAL);
        for (int yaw : new int[]{0, 90}) {
            player.setYRot(yaw);
            player.setXRot(0);
            var frame = (MatrixValue) basis.execute(new SpellContext().setPlayer(player));
            Set<BlockPos> tunnel = new HashSet<>(MassBlockBreakHelper.collectRegionPositions(MatrixOperations.multiply(frame, local)));
            Set<BlockPos> expected = new HashSet<>();
            for (int side = -1; side <= 1; side++) {
                for (int height = 0; height <= 2; height++) {
                    for (int depth = 1; depth <= 5; depth++) {
                        expected.add(yaw == 0 ? feet.offset(side, height, depth) : feet.offset(-depth, height, side));
                    }
                }
            }
            helper.assertTrue(tunnel.equals(expected), "The book's tunnel example must be in front of the caster at yaw " + yaw);
        }

        var saved = (PieceOperatorMatrixCasterBasis) create("operator_matrix_caster_basis");
        saved.setModeOption(PieceOperatorMatrixCasterBasis.FACE);
        var tag = new CompoundTag();
        saved.writeToNBT(tag);
        helper.assertTrue(((PieceOperatorMatrixCasterBasis) SpellPiece.createFromNBT(new Spell(), tag)).getModeOption()
                .equals(PieceOperatorMatrixCasterBasis.FACE), "Face mode lost on reload");
        tag.putString(TAG_MODE, "unknown");
        helper.assertTrue(((PieceOperatorMatrixCasterBasis) SpellPiece.createFromNBT(new Spell(), tag)).getModeOption()
                .equals(PieceOperatorMatrixCasterBasis.HORIZONTAL), "Unknown modes must load as 2D");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void showRegionOutlinesTheBlocksOfTheRegion(GameTestHelper helper) throws Exception {
        var area = RegionMatrix.parse(cube(new Vector3(-0.5, -0.5, -0.5), 3));
        for (boolean blocks : new boolean[]{true, false}) {
            var edges = PieceTrickShowRegion.outline(area, blocks);
            Set<String> corners = new HashSet<>();
            Set<String> distinct = new HashSet<>();
            double low = blocks ? 0 : -0.5;
            for (Vector3[] edge : edges) {
                corners.add(key(edge[0]));
                corners.add(key(edge[1]));
                distinct.add(key(edge[0]) + "|" + key(edge[1]));
                Vector3 span = edge[1].copy().sub(edge[0]);
                helper.assertTrue(Math.abs(span.mag() - 3) < 1e-9, "Every edge must span the region");
            }
            Set<String> expected = new HashSet<>();
            for (double x : new double[]{low, low + 3}) for (double y : new double[]{low, low + 3}) for (double z : new double[]{low, low + 3}) {
                expected.add(key(new Vector3(x, y, z)));
            }
            helper.assertTrue(edges.size() == 12 && distinct.size() == 12 && corners.equals(expected),
                    (blocks ? "Block" : "Vector") + " outline has the wrong corners: " + corners);
        }

        var player = fakeCaster(helper, new BlockPos(2, 1, 2), "region-viewer");
        var context = new SpellContext().setPlayer(player);
        Object[] matrix = {cube(vector(helper.absolutePos(new BlockPos(1, 1, 1))).add(-0.5, -0.5, -0.5), 3)};
        Object[] time = {null};
        var show = new PieceTrickShowRegion(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return param.name.equals(TIME) ? time[0] : matrix[0];
            }
        };
        helper.assertTrue(show.getModeOption().id().equals(PsitweaksModeOptions.BLOCK.id()), "Block mode must be the default");
        helper.assertTrue(error(() -> show.execute(context)) == null, "Showing a nearby region must work");
        show.setModeOption(PsitweaksModeOptions.VECTOR);
        time[0] = 5000.0;
        helper.assertTrue(error(() -> show.execute(context)) == null, "Long times must be clamped, not rejected");
        for (double invalid : new double[]{0, -20, 0.5}) {
            time[0] = invalid;
            helper.assertTrue(SpellRuntimeException.NON_POSITIVE_VALUE.equals(error(() -> show.execute(context))),
                    "Time must be at least one tick: " + invalid);
        }
        time[0] = null;
        matrix[0] = cube(vector(helper.absolutePos(new BlockPos(1, 1, 1))), 40);
        helper.assertTrue(SpellRuntimeException.OUTSIDE_RADIUS.equals(error(() -> show.execute(context))),
                "Every corner must be within the spell radius");
        matrix[0] = new MatrixValue(2, 4, new double[8]);
        helper.assertTrue(MassBlockBreakHelper.ERROR_INVALID_MATRIX.equals(error(() -> show.execute(context))),
                "Only 3x4 and 4x4 matrices are regions");
        matrix[0] = cube(vector(helper.absolutePos(new BlockPos(1, 1, 1))), 0);
        helper.assertTrue(MassBlockBreakHelper.ERROR_DEGENERATE_EDGES.equals(error(() -> show.execute(context))),
                "A flat region must fail");

        var saved = (PieceTrickShowRegion) create("trick_show_region");
        saved.setModeOption(PsitweaksModeOptions.VECTOR);
        var tag = new CompoundTag();
        saved.writeToNBT(tag);
        helper.assertTrue(((PieceTrickShowRegion) SpellPiece.createFromNBT(new Spell(), tag)).getModeOption().id()
                .equals(PsitweaksModeOptions.VECTOR.id()), "Vector mode lost on reload");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void blockItemGivesThePickBlockItemWithoutSource(GameTestHelper helper) throws Exception {
        var level = helper.getLevel();
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.FARMLAND);
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.WHEAT);
        helper.setBlock(new BlockPos(3, 0, 3), Blocks.STONE);
        var context = new SpellContext().setPlayer(fakeCaster(helper, new BlockPos(2, 0, 2), "block-item"));
        Object[] target = {BlockValue.snapshot(level, helper.absolutePos(new BlockPos(1, 1, 1)))};
        var blockItem = new PieceOperatorBlockItem(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return target[0]; }
        };
        var seeds = (SpellItemValue) blockItem.execute(context);
        helper.assertTrue(seeds.snapshot().is(Items.WHEAT_SEEDS) && seeds.snapshot().getCount() == 1 && seeds.source().isEmpty(),
                "A wheat crop must give wheat seeds without a source: " + seeds);
        target[0] = vector(helper.absolutePos(new BlockPos(1, 1, 1)));
        helper.assertTrue(((SpellItemValue) blockItem.execute(context)).snapshot().is(Items.WHEAT_SEEDS),
                "A Vector target must read the block there");
        target[0] = BlockValue.snapshot(level, helper.absolutePos(new BlockPos(2, 2, 2)));
        helper.assertTrue(((SpellItemValue) blockItem.execute(context)).isEmpty(), "Air must give an empty Item");
        var stone = BlockValue.snapshot(level, helper.absolutePos(new BlockPos(3, 0, 3)));
        helper.setBlock(new BlockPos(3, 0, 3), Blocks.DIRT);
        target[0] = stone;
        helper.assertTrue(((SpellItemValue) blockItem.execute(context)).snapshot().is(Items.STONE),
                "A Block read earlier must keep its own item after the world changed");

        // The item works as a filter, here for the ideaspace deposit.
        helper.setBlock(new BlockPos(3, 1, 1), Blocks.CHEST);
        BlockPos chestPos = helper.absolutePos(new BlockPos(3, 1, 1));
        var chest = (ChestBlockEntity) level.getBlockEntity(chestPos);
        chest.setItem(0, new ItemStack(Items.CARROT, 4));
        chest.setItem(1, new ItemStack(Items.WHEAT_SEEDS, 6));
        var depositor = fakeCaster(helper, new BlockPos(2, 1, 1), "block-item-filter");
        var depositContext = new SpellContext().setPlayer(depositor);
        var deposit = IdeaLogisticsGameTests.trick(true, BlockResourceModes.ITEM, param -> IdeaLogisticsGameTests.paramValue(param, chestPos, 64, seeds));
        deposit.execute(depositContext);
        var storage = IdeaStorageService.get(depositor.server, depositor.getUUID());
        helper.assertTrue(stored(storage, new ItemStack(Items.WHEAT_SEEDS)) == 6 && chest.getItem(0).getCount() == 4,
                "The block's item must filter like any Item");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void droppedItemsKeepTheirItemEntityAsSource(GameTestHelper helper) throws Exception {
        var level = helper.getLevel();
        var context = new SpellContext().setPlayer(fakeCaster(helper, new BlockPos(2, 1, 2), "dropped-items"));
        Vec3 at = Vec3.atCenterOf(helper.absolutePos(new BlockPos(1, 1, 1)));
        var apples = new ItemEntity(level, at.x, at.y, at.z, new ItemStack(Items.APPLE, 3));
        var carrots = new ItemEntity(level, at.x, at.y, at.z, new ItemStack(Items.CARROT, 2));
        var stand = new ArmorStand(level, at.x, at.y, at.z);
        var entities = EntityListWrapper.make(List.<Entity>of(stand, apples, carrots));
        Object[] list = {entities};
        var dropped = new PieceOperatorDroppedItems(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return list[0]; }
        };
        var items = (SpellItemListWrapper) dropped.execute(context);
        List<ItemEntity> order = new ArrayList<>();
        for (Entity entity : entities) {
            if (entity instanceof ItemEntity itemEntity) order.add(itemEntity);
        }
        helper.assertTrue(items.size() == 2, "Only the dropped items must become Items: " + items);
        for (int i = 0; i < 2; i++) {
            var value = items.get(i);
            var source = order.get(i);
            helper.assertTrue(value.source().orElse(null) instanceof ItemEntitySource itemSource
                            && itemSource.entityUuid().equals(source.getUUID()) && itemSource.entityId() == source.getId()
                            && ItemStack.matches(value.snapshot(), source.getItem()),
                    "Item " + i + " must keep the list order and point back at its item entity");
        }

        Object[] target = {apples};
        var held = new PieceSelectorHeldItem(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return target[0]; }
        };
        var heldList = new PieceSelectorHeldItems(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return target[0]; }
        };
        var heldValue = (SpellItemValue) held.execute(context);
        var heldValues = (SpellItemListWrapper) heldList.execute(context);
        helper.assertTrue(heldValue.snapshot().is(Items.APPLE) && heldValue.snapshot().getCount() == 3
                        && heldValue.source().orElse(null) instanceof ItemEntitySource source && source.entityUuid().equals(apples.getUUID()),
                "Selector: Held Item must read a dropped item's own stack");
        helper.assertTrue(heldValues.size() == 1 && heldValues.get(0).source().equals(heldValue.source()),
                "Selector: Held Items must list a dropped item's own stack");
        apples.discard();
        // Psi 1.20.1 does not reject removed entities (no IMMUNE_TARGET as on 1.21.1), so a picked up item reads as nothing.
        helper.assertTrue(error(() -> held.execute(context)) == null && ((SpellItemValue) held.execute(context)).isEmpty()
                        && error(() -> heldList.execute(context)) == null
                        && ((SpellItemListWrapper) heldList.execute(context)).size() == 0,
                "A picked up item entity must read as no item");
        helper.assertTrue(((SpellItemListWrapper) dropped.execute(context)).size() == 1,
                "A picked up item entity must drop out of the list");
        helper.succeed();
    }

    private static PieceOperatorBlockResourceBase resource(boolean amount, Object[] target, Object[] side, Object[] filter) {
        if (amount) {
            return new PieceOperatorBlockResourceAmount(new Spell()) {
                @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                    return param.name.equals(TARGET) ? target[0] : param.name.equals(DIRECTION) ? side[0] : filter[0];
                }
            };
        }
        return new PieceOperatorBlockFreeCapacity(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return param.name.equals(TARGET) ? target[0] : param.name.equals(DIRECTION) ? side[0] : filter[0];
            }
        };
    }

    private static double read(SpellPiece piece, SpellContext context) throws SpellRuntimeException {
        return (Double) piece.execute(context);
    }

    @GameTest(template = "connector_empty")
    public static void blockResourcesReadInventoriesTanksAndFaces(GameTestHelper helper) throws Exception {
        var level = helper.getLevel();
        var context = new SpellContext().setPlayer(fakeCaster(helper, new BlockPos(2, 1, 2), "resource-reader"));
        Object[] target = {null};
        Object[] side = {null};
        Object[] filter = {null};
        var amount = resource(true, target, side, filter);
        var free = resource(false, target, side, filter);

        helper.setBlock(new BlockPos(1, 1, 1), Blocks.CHEST);
        BlockPos chestPos = helper.absolutePos(new BlockPos(1, 1, 1));
        var chest = (ChestBlockEntity) level.getBlockEntity(chestPos);
        chest.setItem(0, new ItemStack(Items.APPLE, 10));
        chest.setItem(1, new ItemStack(Items.GOLDEN_APPLE, 5));
        chest.setItem(2, new ItemStack(Items.CARROT, 3));
        target[0] = vector(chestPos);
        Map<String, Double> amounts = new LinkedHashMap<>();
        amounts.put(null, 18.0);
        amounts.put("", 18.0);
        amounts.put("apple", 10.0);
        amounts.put("*apple", 15.0);
        amounts.put("minecraft:carrot", 3.0);
        amounts.put("minecraft:*", 18.0);
        amounts.put("other:*", 0.0);
        amounts.put("?pple", 10.0);
        for (var entry : amounts.entrySet()) {
            filter[0] = entry.getKey();
            helper.assertTrue(read(amount, context) == entry.getValue(), "Chest amount for " + entry.getKey() + ": " + read(amount, context));
        }
        Map<String, Double> capacities = Map.of("apple", 54.0 + 24 * 64, "minecraft:apple", 54.0 + 24 * 64,
                "minecraft:ender_pearl", 24.0 * 16, "missing:item", 0.0, "minecraft:air", 0.0, "INVALID ID", 0.0, "*apple", 0.0);
        for (var entry : capacities.entrySet()) {
            filter[0] = entry.getKey();
            helper.assertTrue(read(free, context) == entry.getValue(), "Chest free capacity for " + entry.getKey() + ": " + read(free, context));
        }
        long before = chest.getItem(0).getCount();
        helper.assertTrue(before == 10 && chest.getItem(3).isEmpty(), "Free capacity must only simulate");

        helper.setBlock(new BlockPos(3, 1, 1), Blocks.FURNACE);
        BlockPos furnacePos = helper.absolutePos(new BlockPos(3, 1, 1));
        var furnace = (FurnaceBlockEntity) level.getBlockEntity(furnacePos);
        furnace.setItem(0, new ItemStack(Items.RAW_IRON, 5));
        furnace.setItem(1, new ItemStack(Items.COAL, 3));
        furnace.setItem(2, new ItemStack(Items.IRON_INGOT, 2));
        target[0] = BlockValue.snapshot(level, furnacePos);
        filter[0] = null;
        Map<Vector3, Double> faces = new LinkedHashMap<>();
        faces.put(new Vector3(0, 1, 0), 5.0);
        faces.put(new Vector3(0, 0, -1), 3.0);
        faces.put(new Vector3(0, -0.9, 0.2), 5.0);
        for (var entry : faces.entrySet()) {
            side[0] = entry.getKey();
            helper.assertTrue(read(amount, context) == entry.getValue(), "Furnace face " + entry.getKey() + ": " + read(amount, context));
        }
        side[0] = null;
        helper.assertTrue(read(amount, context) == 10.0, "Without Direction the whole furnace must be read");
        filter[0] = "*iron*";
        helper.assertTrue(read(amount, context) == 7.0, "Wildcards must match inside the ID path");
        filter[0] = "minecraft:coal";
        helper.assertTrue(read(free, context) == 61.0, "Coal fits only on the fuel");
        side[0] = new Vector3(0, 1, 0);
        helper.assertTrue(read(free, context) == 0.0, "The top face takes only the input slot");
        side[0] = new Vector3(1, 0, 0);
        helper.assertTrue(read(free, context) == 61.0, "A side face takes the fuel slot");
        filter[0] = "raw_iron";
        helper.assertTrue(read(free, context) == 0.0, "Raw iron is no fuel");
        side[0] = null;
        helper.assertTrue(read(free, context) == 59.0, "Raw iron stacks onto the input");

        // Forge 1.20.1 gives vanilla cauldrons no fluid handler (NeoForge 1.21.1 does), so they read 0 like any block without one.
        helper.setBlock(new BlockPos(1, 1, 3), Blocks.CAULDRON);
        helper.setBlock(new BlockPos(3, 1, 3), Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
        amount.setModeOption(BlockResourceModes.FLUID);
        free.setModeOption(BlockResourceModes.FLUID);
        target[0] = vector(helper.absolutePos(new BlockPos(1, 1, 3)));
        filter[0] = null;
        helper.assertTrue(read(amount, context) == 0.0, "An empty cauldron holds no fluid");
        filter[0] = "water";
        helper.assertTrue(read(free, context) == 0.0, "A cauldron without a fluid handler takes no water");
        filter[0] = "minecraft:lava";
        helper.assertTrue(read(free, context) == 0.0, "A cauldron without a fluid handler takes no lava");
        target[0] = vector(helper.absolutePos(new BlockPos(3, 1, 3)));
        filter[0] = null;
        helper.assertTrue(read(amount, context) == 0.0, "A full water cauldron shows no fluid without a fluid handler");
        filter[0] = "lava";
        helper.assertTrue(read(amount, context) == 0.0, "The filter must apply to fluids");
        filter[0] = "water";
        helper.assertTrue(read(free, context) == 0.0 && read(amount, context) == 0.0, "A full cauldron takes nothing more");
        target[0] = vector(chestPos);
        helper.assertTrue(read(amount, context) == 0.0 && read(free, context) == 0.0, "A chest has no fluid storage");
        amount.setModeOption(BlockResourceModes.CHEMICAL);
        free.setModeOption(BlockResourceModes.CHEMICAL);
        filter[0] = "mekanism:hydrogen";
        helper.assertTrue(read(amount, context) == 0.0 && read(free, context) == 0.0, "A chest has no chemical storage");
        amount.setModeOption(BlockResourceModes.ENERGY);
        free.setModeOption(BlockResourceModes.ENERGY);
        helper.assertTrue(!amount.params.containsKey(PsitweaksSpellParams.STRING) && !free.params.containsKey(PsitweaksSpellParams.STRING)
                && read(amount, context) == 0.0 && read(free, context) == 0.0, "FE mode has no String and a chest has no FE");

        amount.setModeOption(BlockResourceModes.ITEM);
        target[0] = new BlockValue(Level.NETHER, chestPos, Blocks.CHEST.defaultBlockState());
        helper.assertTrue(read(amount, context) == 0.0, "A block in another dimension must read 0");
        target[0] = vector(chestPos);
        Consumer<PlayerInteractEvent.RightClickBlock> deny = event -> { if (event.getPos().equals(chestPos)) event.setCanceled(true); };
        MinecraftForge.EVENT_BUS.addListener(deny);
        String denied;
        try {
            denied = error(() -> amount.execute(context));
        } finally {
            MinecraftForge.EVENT_BUS.unregister(deny);
        }
        helper.assertTrue("psitweaks.spellerror.accessdenyed".equals(denied), "A protected block must fail");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void blockResourcesReadTheIdeaspaceConnectorThroughItsFaces(GameTestHelper helper) throws Exception {
        int oldStacks = PsitweaksConfig.COMMON.ideaStorageItemStacksPerType.get();
        long oldFluid = PsitweaksConfig.COMMON.ideaStorageMaxFluidPerType.get();
        long oldEnergy = PsitweaksConfig.COMMON.ideaStorageMaxEnergy.get();
        try {
            PsitweaksConfig.COMMON.ideaStorageItemStacksPerType.set(2);
            PsitweaksConfig.COMMON.ideaStorageMaxFluidPerType.set(4000L);
            PsitweaksConfig.COMMON.ideaStorageMaxEnergy.set(5000L);
            var context = new SpellContext().setPlayer(fakeCaster(helper, new BlockPos(2, 1, 2), "connector-reader"));
            var connector = IdeaLogisticsGameTests.place(helper, new BlockPos(1, 1, 1), UUID.randomUUID());
            connector.setResource(0, ConnectorResource.ENERGY);
            connector.setResource(1, IdeaLogisticsGameTests.water());
            connector.setResource(2, IdeaLogisticsGameTests.apple());
            var storage = connector.storage();
            storage.insertEnergy(1234, false);
            storage.insertFluid(new FluidStack(Fluids.WATER, 1), 1500);
            storage.insert(new ItemStack(Items.APPLE), 100);
            Object[] target = {vector(connector.getBlockPos())};
            Object[] side = {new Vector3(0, 1, 0)};
            Object[] filter = {null};
            var amount = resource(true, target, side, filter);
            var free = resource(false, target, side, filter);

            for (Object face : new Object[]{new Vector3(0, 1, 0), null}) {
                side[0] = face;
                String label = face == null ? "without Direction" : "through the top";
                amount.setModeOption(BlockResourceModes.ENERGY);
                free.setModeOption(BlockResourceModes.ENERGY);
                helper.assertTrue(read(amount, context) == 1234.0 && read(free, context) == 3766.0, "Connector FE " + label);
                amount.setModeOption(BlockResourceModes.FLUID);
                free.setModeOption(BlockResourceModes.FLUID);
                filter[0] = "water";
                helper.assertTrue(read(amount, context) == 1500.0 && read(free, context) == 2500.0, "Connector water " + label);
                amount.setModeOption(BlockResourceModes.ITEM);
                free.setModeOption(BlockResourceModes.ITEM);
                filter[0] = "apple";
                helper.assertTrue(read(amount, context) == 100.0, "Connector apples " + label);
                helper.assertTrue(read(free, context) == 28.0, "The connector's slots share one storage: " + read(free, context));
            }
            if (MekanismCompat.isMekanismLoaded()) BlockResourceChemicalChecks.run(helper, context, connector);

            connector.setSideMode(Direction.NORTH, ConnectorSideMode.DISABLED);
            side[0] = new Vector3(0, 0, -1);
            filter[0] = "apple";
            helper.assertTrue(read(amount, context) == 0.0 && read(free, context) == 0.0, "A disabled face must show and take no items");
            amount.setModeOption(BlockResourceModes.ENERGY);
            free.setModeOption(BlockResourceModes.ENERGY);
            helper.assertTrue(read(amount, context) == 0.0 && read(free, context) == 0.0, "A disabled face must show and take no FE");
            side[0] = new Vector3(0, -1, 0);
            helper.assertTrue(read(amount, context) == 1234.0, "Other faces keep working");
            connector.setSideMode(Direction.DOWN, ConnectorSideMode.INPUT);
            helper.assertTrue(read(amount, context) == 0.0 && read(free, context) == 3766.0,
                    "An input-only face shows no FE, which must not count as free: " + read(free, context));

            connector.setSideMode(Direction.DOWN, ConnectorSideMode.DISABLED);
            side[0] = null;
            helper.assertTrue(read(amount, context) == 1234.0 && read(free, context) == 3766.0,
                    "Without Direction the fullest face counts, not the disabled bottom");
            amount.setModeOption(BlockResourceModes.ITEM);
            free.setModeOption(BlockResourceModes.ITEM);
            helper.assertTrue(read(amount, context) == 100.0 && read(free, context) == 28.0,
                    "Items without Direction, bottom disabled");
        } finally {
            PsitweaksConfig.COMMON.ideaStorageItemStacksPerType.set(oldStacks);
            PsitweaksConfig.COMMON.ideaStorageMaxFluidPerType.set(oldFluid);
            PsitweaksConfig.COMMON.ideaStorageMaxEnergy.set(oldEnergy);
        }
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void blockResourceModesSwitchInputsAndSave(GameTestHelper helper) throws Exception {
        for (String id : new String[]{"operator_block_resource_amount", "operator_block_free_capacity"}) {
            var piece = (PieceOperatorBlockResourceBase) create(id);
            helper.assertTrue(piece.getModeOption().equals(BlockResourceModes.ITEM) && piece.params.size() == 3,
                    "Item mode must be the default: " + id);
            piece.paramSides.put(piece.params.get(TARGET), SpellParam.Side.LEFT);
            piece.paramSides.put(piece.params.get(DIRECTION), SpellParam.Side.TOP);
            piece.paramSides.put(piece.params.get(PsitweaksSpellParams.STRING), SpellParam.Side.RIGHT);
            piece.setModeOption(BlockResourceModes.FLUID);
            helper.assertTrue(piece.params.size() == 3 && piece.paramSides.get(piece.params.get(PsitweaksSpellParams.STRING)) == SpellParam.Side.RIGHT,
                    "Fluid mode keeps the String wire: " + id);
            piece.setModeOption(BlockResourceModes.ENERGY);
            helper.assertTrue(piece.params.size() == 2 && !piece.params.containsKey(PsitweaksSpellParams.STRING)
                            && piece.paramSides.get(piece.params.get(TARGET)) == SpellParam.Side.LEFT
                            && piece.paramSides.get(piece.params.get(DIRECTION)) == SpellParam.Side.TOP,
                    "FE mode drops the String and keeps the other wires: " + id);
            piece.setModeOption(BlockResourceModes.CHEMICAL);
            var tag = new CompoundTag();
            piece.writeToNBT(tag);
            var reloaded = (PieceOperatorBlockResourceBase) SpellPiece.createFromNBT(new Spell(), tag);
            helper.assertTrue(reloaded.getModeOption().equals(BlockResourceModes.CHEMICAL) && reloaded.params.size() == 3
                            && reloaded.paramSides.get(reloaded.params.get(TARGET)) == SpellParam.Side.LEFT,
                    "Chemical mode or its wires lost on reload: " + id);
            tag.putString(TAG_MODE, "unknown");
            helper.assertTrue(((PieceOperatorBlockResourceBase) SpellPiece.createFromNBT(new Spell(), tag)).getModeOption()
                    .equals(BlockResourceModes.ITEM), "Unknown modes must load as Item: " + id);
        }
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void itemListFiltersMatchAnyOfTheirItems(GameTestHelper helper) throws Exception {
        var apple = new ItemStack(Items.APPLE);
        var namedApple = named(new ItemStack(Items.APPLE), "Shiny");
        var list = SpellItemListWrapper.make(List.of(SpellItemValue.snapshot(namedApple), SpellItemValue.snapshot(new ItemStack(Items.BREAD, 3))));
        var loose = SpellItemFilters.of(list, false);
        var strict = SpellItemFilters.of(list, true);
        helper.assertTrue(loose.test(apple) && loose.test(namedApple) && loose.test(new ItemStack(Items.BREAD))
                && !loose.test(new ItemStack(Items.CARROT)) && !loose.test(ItemStack.EMPTY), "Item mode must match any listed item type");
        helper.assertTrue(strict.test(namedApple) && !strict.test(apple) && strict.test(new ItemStack(Items.BREAD, 64))
                && !strict.test(ItemStack.EMPTY), "Item (Strict) mode must also compare NBT, never the count");
        for (boolean mode : new boolean[]{false, true}) {
            helper.assertTrue(!SpellItemFilters.of(SpellItemListWrapper.EMPTY, mode).test(apple)
                    && !SpellItemFilters.of(SpellItemListWrapper.make(List.of(SpellItemValue.EMPTY)), mode).test(apple),
                    "An empty list must match nothing");
            helper.assertTrue(SpellItemFilters.ERROR_NULL_ITEM.equals(error(() -> SpellItemFilters.of(SpellItemValue.EMPTY, mode)))
                    && SpellItemFilters.ERROR_NULL_ITEM.equals(error(() -> SpellItemFilters.of("apple", mode))),
                    "An empty Item stays an error");
        }

        var level = helper.getLevel();
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.CHEST);
        BlockPos chestPos = helper.absolutePos(new BlockPos(1, 1, 1));
        var chest = (ChestBlockEntity) level.getBlockEntity(chestPos);
        chest.setItem(0, new ItemStack(Items.CARROT, 5));
        chest.setItem(1, new ItemStack(Items.APPLE, 4));
        chest.setItem(2, new ItemStack(Items.BREAD, 3));
        var player = fakeCaster(helper, new BlockPos(2, 1, 1), "list-filter");
        var context = new SpellContext().setPlayer(player);
        var wanted = SpellItemListWrapper.make(List.of(SpellItemValue.snapshot(new ItemStack(Items.BREAD)),
                SpellItemValue.snapshot(new ItemStack(Items.APPLE))));
        var pull = new PieceTrickPullItem(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return param.name.equals(PsitweaksSpellParams.MAX) ? null : IdeaLogisticsGameTests.paramValue(param, chestPos, 0, wanted);
            }
        };
        pull.setModeOption(PsitweaksModeOptions.ITEM);
        pull.execute(context);
        helper.assertTrue(chest.getItem(1).isEmpty() && chest.getItem(0).getCount() == 5 && chest.getItem(2).getCount() == 3,
                "Pull must move the first stack matching any listed item");
        pull.execute(context);
        helper.assertTrue(chest.getItem(2).isEmpty() && chest.getItem(0).getCount() == 5, "Pull must skip unlisted items");
        helper.assertTrue(error(() -> pull.execute(context)) != null && chest.getItem(0).getCount() == 5,
                "Pull must fail once no listed item is left");

        chest.setItem(1, new ItemStack(Items.APPLE, 4));
        chest.setItem(2, named(new ItemStack(Items.BREAD, 3), "Special"));
        // Operator: Strict Item turns the whole list into a strict filter.
        Object strictWanted = new PieceOperatorStrictItem(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return wanted; }
        }.execute(context);
        var deposit = IdeaLogisticsGameTests.trick(true, BlockResourceModes.ITEM,
                param -> IdeaLogisticsGameTests.paramValue(param, chestPos, 64, strictWanted));
        deposit.execute(context);
        var storage = IdeaStorageService.get(player.server, player.getUUID());
        helper.assertTrue(stored(storage, new ItemStack(Items.APPLE)) == 4 && chest.getItem(2).getCount() == 3
                && chest.getItem(0).getCount() == 5, "Strict deposit must take only exact matches of the list");

        var caster = IdeaStorageSlotGameTests.caster(helper, false, true);
        var slotContext = new SpellContext().setPlayer(caster);
        caster.getInventory().setItem(1, new ItemStack(Items.BREAD, 9));
        double[] count = {64};
        Object[] slotFilter = {wanted};
        PieceTrickIdeaStorageSlotBase depositSlot = IdeaStorageSlotGameTests.piece(true, count, slotFilter);
        depositSlot.execute(slotContext);
        var slotStorage = IdeaStorageService.get(caster.server, caster.getUUID());
        helper.assertTrue(caster.getInventory().getItem(1).isEmpty() && stored(slotStorage, new ItemStack(Items.BREAD)) == 9,
                "Slot deposit must accept an Item List filter");
        caster.getInventory().setItem(1, new ItemStack(Items.CARROT, 2));
        depositSlot.execute(slotContext);
        helper.assertTrue(caster.getInventory().getItem(1).getCount() == 2, "Slot deposit must skip unlisted items");
        helper.succeed();
    }
}
