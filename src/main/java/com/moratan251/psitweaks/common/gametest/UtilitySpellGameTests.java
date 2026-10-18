package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.menu.SpellCraftingMenu;
import com.moratan251.psitweaks.common.spells.SpellWarning;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorConnectedBlocks;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.HarvestHelper;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.MassHarvestScheduler;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickBreed;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickCraftingTable;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickEnderChest;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickHarvest;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickHarvestSequence;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickMassHarvest;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickShear;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slf4j.Logger;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

/**
 * The utility tricks of 1.0.0: the screens they open, what they harvest, shear and breed, and Operator: Connected Blocks.
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class UtilitySpellGameTests {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TEMPLATE = "connector_empty";
    private static final String POSITION = SpellParam.GENERIC_NAME_POSITION;
    private static final String TARGET = SpellParam.GENERIC_NAME_TARGET;
    private static final String MAX = SpellParam.GENERIC_NAME_MAX;
    private static final String LIST = SpellParam.GENERIC_NAME_LIST;

    private interface CompileAction {
        void run() throws SpellCompilationException;
    }

    private interface CastAction {
        void run() throws SpellRuntimeException;
    }

    private static FakePlayer caster(GameTestHelper helper, BlockPos relative, String name) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        player.moveTo(Vec3.atBottomCenterOf(helper.absolutePos(relative)));
        return player;
    }

    /** A plain ServerPlayer whose packets go nowhere: a FakePlayer never opens a screen. */
    private static ServerPlayer screenPlayer(GameTestHelper helper, String name) {
        var level = helper.getLevel();
        var profile = new GameProfile(UUID.randomUUID(), name);
        var player = new ServerPlayer(level.getServer(), level, profile, ClientInformation.createDefault());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        player.connection = new ServerGamePacketListenerImpl(level.getServer(), connection, player,
                CommonListenerCookie.createInitial(profile, false));
        player.moveTo(Vec3.atBottomCenterOf(helper.absolutePos(BlockPos.ZERO)));
        return player;
    }

    private static Vector3 corner(BlockPos absolute) {
        return new Vector3(absolute.getX(), absolute.getY(), absolute.getZ());
    }

    private static Vector3 center(BlockPos absolute) {
        return new Vector3(absolute.getX() + 0.5, absolute.getY() + 0.5, absolute.getZ() + 0.5);
    }

    private static int[] stats(SpellPiece piece) throws SpellCompilationException {
        var metadata = new SpellMetadata();
        piece.addToMetadata(metadata);
        return new int[]{metadata.getStat(EnumSpellStat.POTENCY), metadata.getStat(EnumSpellStat.COST),
                metadata.getStat(EnumSpellStat.COMPLEXITY), metadata.getStat(EnumSpellStat.PROJECTION)};
    }

    /** The error key compiling fails with, or null when it compiles. */
    private static String compileError(CompileAction action) {
        try {
            action.run();
            return null;
        } catch (SpellCompilationException failure) {
            return failure.getMessage();
        }
    }

    /** The error key casting fails with, or null when it succeeds. */
    private static String castError(CastAction action) {
        try {
            action.run();
            return null;
        } catch (SpellRuntimeException failure) {
            return failure.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T evaluation(Map<String, ?> values, SpellParam<?> param) {
        Object value = values.get(param.name);
        return (T) (value instanceof Number number ? Double.valueOf(number.doubleValue()) : value);
    }

    // Pieces whose parameters read fixed values, both when cast and when compiled.

    private static PieceTrickHarvest harvest(Vector3 position) {
        return new PieceTrickHarvest(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return position; }
        };
    }

    private static PieceTrickHarvestSequence harvestSequence(Map<String, ?> values) {
        return new PieceTrickHarvestSequence(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return values.get(param.name); }
            @Override public <T> T getParamEvaluation(SpellParam<?> param) { return evaluation(values, param); }
        };
    }

    private static PieceTrickMassHarvest massHarvest(Map<String, ?> values) {
        return new PieceTrickMassHarvest(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return values.get(param.name); }
            @Override public <T> T getParamEvaluation(SpellParam<?> param) { return evaluation(values, param); }
        };
    }

    private static PieceTrickShear shear(Object target) {
        return new PieceTrickShear(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return target; }
        };
    }

    private static PieceTrickBreed breed(Object target) {
        return new PieceTrickBreed(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return target; }
        };
    }

    private static PieceOperatorConnectedBlocks connectedBlocks(Map<String, ?> values) {
        return new PieceOperatorConnectedBlocks(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return values.get(param.name); }
        };
    }

    private static BlockState ripe(Block crop) {
        return ((CropBlock) crop).getStateForAge(((CropBlock) crop).getMaxAge());
    }

    private static boolean between(int value, int min, int max) {
        return value >= min && value <= max;
    }

    @GameTest(template = TEMPLATE)
    public static void enderChestTrickOpensTheCastersEnderChestAnywhere(GameTestHelper helper) throws Exception {
        var player = screenPlayer(helper, "ender-chest");
        player.getEnderChestInventory().setItem(0, new ItemStack(Items.DIAMOND));
        var piece = new PieceTrickEnderChest(new Spell());
        piece.execute(new SpellContext().setPlayer(player));
        helper.assertTrue(player.containerMenu instanceof ChestMenu menu && menu.getContainer() == player.getEnderChestInventory()
                && menu.getSlot(0).getItem().is(Items.DIAMOND), "The caster's own ender chest must open, got " + player.containerMenu);
        // No ender chest block is involved, so walking away keeps it open.
        player.moveTo(player.getX() + 100, player.getY(), player.getZ());
        helper.assertTrue(player.containerMenu.stillValid(player), "The ender chest screen must stay open away from any block");
        player.closeContainer();

        var machine = caster(helper, BlockPos.ZERO, "ender-chest-machine");
        piece.execute(new SpellContext().setPlayer(machine));
        helper.assertTrue(machine.containerMenu == machine.inventoryMenu, "A fake player must not open a screen");
        int[] stats = stats(piece);
        helper.assertTrue(stats[0] == 0 && stats[1] == 0 && stats[2] == 1 && stats[3] == 0,
                "Ender Chest must have complexity 1 only, got potency/cost/complexity/projection " + Arrays.toString(stats));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void craftingTableTrickCraftsAnywhereAndHandsTheGridBack(GameTestHelper helper) throws Exception {
        var player = screenPlayer(helper, "crafting-table");
        var piece = new PieceTrickCraftingTable(new Spell());
        piece.execute(new SpellContext().setPlayer(player));
        helper.assertTrue(player.containerMenu instanceof SpellCraftingMenu, "A crafting screen must open, got " + player.containerMenu);
        var menu = (CraftingMenu) player.containerMenu;
        menu.getSlot(1).set(new ItemStack(Items.OAK_LOG));
        ItemStack result = menu.getSlot(0).getItem();
        helper.assertTrue(result.is(Items.OAK_PLANKS) && result.getCount() == 4, "An oak log must show 4 oak planks, got " + result);
        player.moveTo(player.getX() + 100, player.getY(), player.getZ());
        helper.assertTrue(menu.stillValid(player), "The crafting screen must stay open away from any crafting table");
        player.closeContainer();
        helper.assertTrue(player.getInventory().countItem(Items.OAK_LOG) == 1, "Closing must hand the grid back to the caster");
        int[] stats = stats(piece);
        helper.assertTrue(stats[0] == 0 && stats[1] == 0 && stats[2] == 1 && stats[3] == 0,
                "Crafting Table must have complexity 1 only, got " + Arrays.toString(stats));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void harvestReplantsRipeCropsAndKeepsOneSeed(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(2, 0, 2), "harvester");
        var context = new SpellContext().setPlayer(player);
        BlockPos wheat = new BlockPos(0, 1, 0);
        BlockPos unripe = new BlockPos(1, 1, 0);
        BlockPos carrots = new BlockPos(2, 1, 0);
        BlockPos wart = new BlockPos(3, 1, 0);
        BlockPos cocoa = new BlockPos(4, 1, 1);
        BlockPos berries = new BlockPos(0, 1, 2);
        BlockPos glowBerries = new BlockPos(1, 2, 2);
        BlockPos bamboo = new BlockPos(2, 1, 2);
        BlockPos stem = new BlockPos(3, 1, 4);
        BlockPos melon = new BlockPos(4, 1, 4);
        BlockPos looseMelon = new BlockPos(0, 1, 4);
        helper.setBlock(wheat.below(), Blocks.FARMLAND);
        helper.setBlock(wheat, ripe(Blocks.WHEAT));
        helper.setBlock(unripe.below(), Blocks.FARMLAND);
        helper.setBlock(unripe, ((CropBlock) Blocks.WHEAT).getStateForAge(3));
        helper.setBlock(carrots.below(), Blocks.FARMLAND);
        helper.setBlock(carrots, ripe(Blocks.CARROTS));
        helper.setBlock(wart.below(), Blocks.SOUL_SAND);
        helper.setBlock(wart, Blocks.NETHER_WART.defaultBlockState().setValue(NetherWartBlock.AGE, NetherWartBlock.MAX_AGE));
        helper.setBlock(cocoa.north(), Blocks.JUNGLE_LOG);
        helper.setBlock(cocoa, Blocks.COCOA.defaultBlockState().setValue(CocoaBlock.AGE, CocoaBlock.MAX_AGE)
                .setValue(CocoaBlock.FACING, Direction.NORTH));
        helper.setBlock(berries.below(), Blocks.GRASS_BLOCK);
        helper.setBlock(berries, Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, SweetBerryBushBlock.MAX_AGE));
        helper.setBlock(glowBerries.above(), Blocks.STONE);
        helper.setBlock(glowBerries, Blocks.CAVE_VINES.defaultBlockState().setValue(CaveVines.BERRIES, true));
        helper.setBlock(bamboo.below(), Blocks.DIRT);
        for (int i = 0; i < 3; i++) {
            helper.setBlock(bamboo.above(i), Blocks.BAMBOO);
        }
        helper.setBlock(stem.below(), Blocks.FARMLAND);
        helper.setBlock(melon.below(), Blocks.DIRT);
        helper.setBlock(melon, Blocks.MELON);
        helper.setBlock(stem, Blocks.ATTACHED_MELON_STEM.defaultBlockState().setValue(AttachedStemBlock.FACING, Direction.EAST));
        helper.setBlock(looseMelon.below(), Blocks.DIRT);
        helper.setBlock(looseMelon, Blocks.MELON);

        // The middle of the bamboo: everything above the lowest block goes.
        for (BlockPos pos : List.of(wheat, unripe, carrots, wart, cocoa, berries, glowBerries, bamboo.above(), melon, looseMelon)) {
            harvest(center(helper.absolutePos(pos))).execute(context);
        }

        helper.assertBlockState(wheat, state -> state.is(Blocks.WHEAT) && state.getValue(CropBlock.AGE) == 0, () -> "Wheat must be replanted");
        helper.assertBlockState(unripe, state -> state.is(Blocks.WHEAT) && state.getValue(CropBlock.AGE) == 3, () -> "Unripe wheat must stay");
        helper.assertBlockState(carrots, state -> state.is(Blocks.CARROTS) && state.getValue(CropBlock.AGE) == 0, () -> "Carrots must be replanted");
        helper.assertBlockState(wart, state -> state.is(Blocks.NETHER_WART) && state.getValue(NetherWartBlock.AGE) == 0, () -> "Nether wart must be replanted");
        helper.assertBlockState(cocoa, state -> state.is(Blocks.COCOA) && state.getValue(CocoaBlock.AGE) == 0
                && state.getValue(CocoaBlock.FACING) == Direction.NORTH, () -> "Cocoa must be replanted facing its log");
        helper.assertBlockState(berries, state -> state.is(Blocks.SWEET_BERRY_BUSH) && state.getValue(SweetBerryBushBlock.AGE) == 1,
                () -> "The sweet berry bush must be picked");
        helper.assertBlockState(glowBerries, state -> state.is(Blocks.CAVE_VINES) && !state.getValue(CaveVines.BERRIES),
                () -> "The glow berries must be picked");
        helper.assertBlockPresent(Blocks.BAMBOO, bamboo);
        helper.assertBlockPresent(Blocks.AIR, bamboo.above());
        helper.assertBlockPresent(Blocks.AIR, bamboo.above(2));
        helper.assertBlockPresent(Blocks.AIR, melon);
        helper.assertBlockPresent(Blocks.MELON_STEM, stem);
        helper.assertBlockPresent(Blocks.MELON, looseMelon);

        Inventory inventory = player.getInventory();
        helper.assertTrue(inventory.countItem(Items.WHEAT) == 1 && between(inventory.countItem(Items.WHEAT_SEEDS), 0, 3),
                "Wheat: 1 wheat and the seeds but one, got " + inventory.countItem(Items.WHEAT) + "/" + inventory.countItem(Items.WHEAT_SEEDS));
        helper.assertTrue(between(inventory.countItem(Items.CARROT), 1, 4), "Carrots: 2-5 but the replanted one, got "
                + inventory.countItem(Items.CARROT));
        helper.assertTrue(between(inventory.countItem(Items.NETHER_WART), 1, 3), "Nether wart: 2-4 but the replanted one");
        helper.assertTrue(between(inventory.countItem(Items.COCOA_BEANS), 1, 2), "Cocoa: 2-3 beans but the replanted one");
        helper.assertTrue(between(inventory.countItem(Items.SWEET_BERRIES), 2, 3), "A full bush gives 2-3 sweet berries");
        helper.assertTrue(inventory.countItem(Items.GLOW_BERRIES) == 1, "Glow berries give one");
        helper.assertTrue(inventory.countItem(Items.BAMBOO) == 2, "Two bamboo above the lowest block");
        helper.assertTrue(between(inventory.countItem(Items.MELON_SLICE), 3, 7) && inventory.countItem(Items.MELON) == 0,
                "The grown melon gives slices and the loose melon nothing");
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(8)).isEmpty(),
                "Nothing may drop while the inventory has room");

        var outside = harvest(center(helper.absolutePos(wheat)).add(40, 0, 0));
        helper.assertTrue(SpellRuntimeException.OUTSIDE_RADIUS.equals(castError(() -> outside.execute(context))),
                "A position out of range must fail");
        int[] stats = stats(harvest(null));
        helper.assertTrue(stats[0] == 20 && stats[1] == 40, "Harvest: potency 20, cost 40, got " + Arrays.toString(stats));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void harvestLeavesCropsTheCasterMayNotBreak(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(2, 0, 2), "protected-harvester");
        var context = new SpellContext().setPlayer(player);
        BlockPos wheat = new BlockPos(1, 1, 1);
        helper.setBlock(wheat.below(), Blocks.FARMLAND);
        helper.setBlock(wheat, ripe(Blocks.WHEAT));
        BlockPos absolute = helper.absolutePos(wheat);
        var piece = harvest(center(absolute));

        Consumer<BlockEvent.BreakEvent> deny = event -> { if (event.getPos().equals(absolute)) event.setCanceled(true); };
        NeoForge.EVENT_BUS.addListener(deny);
        try {
            piece.execute(context);
        } finally {
            NeoForge.EVENT_BUS.unregister(deny);
        }
        helper.assertBlockState(wheat, state -> state.getValue(CropBlock.AGE) == 7, () -> "A protected crop must stay ripe");
        player.getAbilities().mayBuild = false;
        piece.execute(context);
        helper.assertBlockState(wheat, state -> state.getValue(CropBlock.AGE) == 7, () -> "Adventure mode must not harvest");
        helper.assertTrue(player.getInventory().isEmpty(), "Nothing may be harvested");
        player.getAbilities().mayBuild = true;
        piece.execute(context);
        helper.assertBlockState(wheat, state -> state.getValue(CropBlock.AGE) == 0, () -> "The crop must be harvested once allowed");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void harvestDropsWhatDoesNotFitEvenInCreative(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(2, 0, 2), "full-harvester");
        // Inventory.add would delete the overflow of a creative player (PIT-029).
        player.getAbilities().instabuild = true;
        for (int slot = 0; slot < player.getInventory().items.size(); slot++) {
            player.getInventory().items.set(slot, new ItemStack(Items.STONE, 64));
        }
        BlockPos wheat = new BlockPos(1, 1, 1);
        helper.setBlock(wheat.below(), Blocks.FARMLAND);
        helper.setBlock(wheat, ripe(Blocks.WHEAT));
        harvest(center(helper.absolutePos(wheat))).execute(new SpellContext().setPlayer(player));
        var dropped = helper.getLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(4));
        helper.assertTrue(dropped.stream().anyMatch(item -> item.getItem().is(Items.WHEAT)),
                "The wheat must be dropped at the caster, got " + dropped);
        helper.killAllEntitiesOfClass(ItemEntity.class);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void harvestSequenceFollowsTheLineAndSkipsBlocksOutOfRange(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(0, 0, 3), "row-harvester");
        var context = new SpellContext().setPlayer(player);
        List<BlockPos> row = new ArrayList<>();
        for (int x = 0; x < 4; x++) {
            BlockPos pos = new BlockPos(x, 1, 1);
            row.add(pos);
            helper.setBlock(pos.below(), Blocks.FARMLAND);
            helper.setBlock(pos, ripe(Blocks.WHEAT));
        }
        Vector3 start = center(helper.absolutePos(row.get(0)));
        harvestSequence(Map.of(POSITION, start, TARGET, new Vector3(1, 0, 0), MAX, 3.0)).execute(context);
        for (int i = 0; i < 4; i++) {
            int expected = i < 3 ? 0 : 7;
            helper.assertBlockState(row.get(i), state -> state.getValue(CropBlock.AGE) == expected,
                    () -> "Only the first three crops of the row may be harvested");
        }

        // From 31.5 blocks away only the first crop is in range; the rest are skipped without an error.
        for (BlockPos pos : row) {
            helper.setBlock(pos, ripe(Blocks.WHEAT));
        }
        player.moveTo(start.x - 31.5, start.y, start.z);
        helper.assertTrue(castError(() -> harvestSequence(Map.of(POSITION, start, TARGET, new Vector3(1, 0, 0), MAX, 3.0))
                .execute(context)) == null, "Blocks out of range must be skipped, not fail");
        helper.assertBlockState(row.get(0), state -> state.getValue(CropBlock.AGE) == 0, () -> "The crop in range must be harvested");
        helper.assertBlockState(row.get(1), state -> state.getValue(CropBlock.AGE) == 7, () -> "A crop out of range must stay");

        int[] stats = stats(harvestSequence(Map.of(MAX, 3.0)));
        helper.assertTrue(stats[0] == 60 && stats[1] == 104, "Harvest Sequence of 3: potency 60, cost 104, got "
                + Arrays.toString(stats));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void massHarvestTakesDistinctPositionsAndFailsOutsideTheRadius(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(2, 0, 3), "mass-harvester");
        var context = new SpellContext().setPlayer(player);
        List<BlockPos> crops = new ArrayList<>();
        for (int x = 0; x < 3; x++) {
            BlockPos pos = new BlockPos(x, 1, 1);
            crops.add(pos);
            helper.setBlock(pos.below(), Blocks.FARMLAND);
            helper.setBlock(pos, ripe(Blocks.WHEAT));
        }
        Vector3 first = corner(helper.absolutePos(crops.get(0)));
        Vector3 second = corner(helper.absolutePos(crops.get(1)));
        Vector3 third = corner(helper.absolutePos(crops.get(2)));
        // The duplicate counts once, so a maximum of 2 takes the first and the second crop.
        massHarvest(Map.of(LIST, VectorListWrapper.make(List.of(first, first, second, third)), MAX, 2.0)).execute(context);
        helper.assertBlockState(crops.get(0), state -> state.getValue(CropBlock.AGE) == 7, () -> "The harvest must wait for the next tick");

        var outOfRange = massHarvest(Map.of(LIST, VectorListWrapper.make(List.of(third, first.copy().add(40, 0, 0))), MAX, 2.0));
        helper.assertTrue(SpellRuntimeException.OUTSIDE_RADIUS.equals(castError(() -> outOfRange.execute(context))),
                "A position out of range must fail the spell");

        int[] stats = stats(massHarvest(Map.of(MAX, 100.0)));
        helper.assertTrue(stats[0] == 200 && stats[1] == 250, "Mass Harvest of 100: potency 200, cost 250, got "
                + Arrays.toString(stats));
        for (double invalid : new double[]{0.0, 2.5, -1.0}) {
            helper.assertTrue(SpellCompilationException.NON_POSITIVE_INTEGER.equals(compileError(() -> stats(massHarvest(Map.of(MAX, invalid))))),
                    "Max " + invalid + " must not compile");
        }
        helper.succeedWhen(() -> {
            helper.assertBlockState(crops.get(0), state -> state.getValue(CropBlock.AGE) == 0, () -> "The first crop must be harvested");
            helper.assertBlockState(crops.get(1), state -> state.getValue(CropBlock.AGE) == 0, () -> "The second crop must be harvested");
            helper.assertBlockState(crops.get(2), state -> state.getValue(CropBlock.AGE) == 7,
                    () -> "Neither the maximum nor the failed cast may harvest the third crop");
            helper.assertTrue(player.getInventory().countItem(Items.WHEAT) == 2, "The caster must get the harvest at the end");
        });
    }

    @GameTest(template = TEMPLATE)
    public static void massHarvestDropsTheHarvestSoFarWhenTheCasterIsGone(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(2, 0, 3), "vanishing-harvester");
        BlockPos reached = new BlockPos(1, 1, 1);
        BlockPos notReached = new BlockPos(3, 1, 1);
        for (BlockPos pos : List.of(reached, notReached)) {
            helper.setBlock(pos.below(), Blocks.FARMLAND);
            helper.setBlock(pos, ripe(Blocks.WHEAT));
        }
        var harvest = HarvestHelper.begin(new SpellContext().setPlayer(player));
        helper.assertTrue(harvest != null && harvest.harvest(helper.absolutePos(reached)), "The first crop must be harvested at once");
        BlockPos focalPoint = helper.absolutePos(new BlockPos(2, 1, 3));
        MassHarvestScheduler.schedule(harvest, List.of(helper.absolutePos(notReached)), focalPoint);
        // A fake player cannot die, so take its health away directly.
        player.setHealth(0);
        helper.succeedWhen(() -> {
            var dropped = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(focalPoint).inflate(2));
            helper.assertTrue(dropped.stream().anyMatch(item -> item.getItem().is(Items.WHEAT)),
                    "The harvest so far must drop at the focal point");
            helper.assertBlockState(notReached, state -> state.getValue(CropBlock.AGE) == 7, () -> "The crop not reached must stay ripe");
            helper.assertTrue(player.getInventory().isEmpty(), "Nothing may go to the caster that is gone");
            helper.killAllEntitiesOfClass(ItemEntity.class);
        });
    }

    @GameTest(template = TEMPLATE)
    public static void harvestHandOverStopsAtTheDeadline(GameTestHelper helper) {
        var player = caster(helper, new BlockPos(2, 0, 2), "patient-harvester");
        BlockPos wheat = new BlockPos(1, 1, 1);
        helper.setBlock(wheat.below(), Blocks.FARMLAND);
        helper.setBlock(wheat, ripe(Blocks.WHEAT));
        var harvest = HarvestHelper.begin(new SpellContext().setPlayer(player));
        helper.assertTrue(harvest != null && harvest.harvest(helper.absolutePos(wheat)), "The wheat must be harvested");
        helper.assertTrue(!harvest.finish(System.nanoTime() - 1) && player.getInventory().isEmpty(),
                "Nothing may be handed over once the time of the tick is up");
        helper.assertTrue(harvest.finish(Long.MAX_VALUE) && player.getInventory().countItem(Items.WHEAT) == 1,
                "The rest must be handed over later");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void massHarvestLimitsThePendingBlocksPerPlayer(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(2, 0, 2), "queued-harvester");
        // High in the air, where nothing grows.
        player.moveTo(player.getX(), player.getY() + 150, player.getZ());
        var context = new SpellContext().setPlayer(player);
        BlockPos center = player.blockPosition();
        List<Vector3> cube = new ArrayList<>();
        for (int x = -8; x < 8; x++) {
            for (int y = -8; y < 8; y++) {
                for (int z = -8; z < 8; z++) {
                    cube.add(corner(center.offset(x, y, z)));
                }
            }
        }
        var piece = massHarvest(Map.of(LIST, VectorListWrapper.make(cube), MAX, (double) PieceTrickMassHarvest.MAX_POSITIONS));
        int casts = MassHarvestScheduler.MAX_QUEUED_PER_PLAYER / PieceTrickMassHarvest.MAX_POSITIONS;
        for (int i = 0; i < casts; i++) {
            int cast = i + 1;
            helper.assertTrue(castError(() -> piece.execute(context)) == null, "Cast " + cast + " must be queued");
        }
        helper.assertTrue(MassHarvestScheduler.ERROR_TOO_MANY_PENDING.equals(castError(() -> piece.execute(context))),
                "One cast more than the queue holds must fail");
        helper.succeedWhen(() -> helper.assertTrue(castError(() -> piece.execute(context)) == null,
                "The queue must free up once its positions are done"));
    }

    /**
     * Times the largest Mass Harvest done all at once, on a 5x5 field ripened again and again. The trick itself spreads
     * the work over ticks; the log tells the cost per crop, and the bound only catches a slowdown that grows with size.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void harvestingTheMaximumIsTimed(GameTestHelper helper) {
        var player = caster(helper, new BlockPos(2, 0, 2), "timed-harvester");
        List<BlockPos> field = new ArrayList<>();
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                BlockPos pos = helper.absolutePos(new BlockPos(x, 1, z));
                field.add(pos);
                helper.getLevel().setBlock(pos.below(), Blocks.FARMLAND.defaultBlockState(), 3);
            }
        }
        var harvest = HarvestHelper.begin(new SpellContext().setPlayer(player));
        helper.assertTrue(harvest != null, "A fake player must be able to harvest");
        int rounds = (PieceTrickMassHarvest.MAX_POSITIONS + field.size() - 1) / field.size();
        long harvestNanos = 0;
        long lastRoundNanos = 0;
        for (int round = 0; round < rounds; round++) {
            for (BlockPos pos : field) {
                helper.getLevel().setBlock(pos, ripe(Blocks.WHEAT), 2);
            }
            long start = System.nanoTime();
            for (BlockPos pos : field) {
                harvest.harvest(pos);
            }
            lastRoundNanos = System.nanoTime() - start;
            harvestNanos += lastRoundNanos;
        }
        long start = System.nanoTime();
        harvest.finish();
        long finishNanos = System.nanoTime() - start;
        double millis = (harvestNanos + finishNanos) / 1.0E6;
        LOGGER.info("Harvesting {} crops took {} ms ({} us per crop in the last round), handing them over {} ms",
                harvest.harvested(), String.format("%.1f", harvestNanos / 1.0E6),
                String.format("%.1f", lastRoundNanos / 1.0E3 / field.size()), String.format("%.1f", finishNanos / 1.0E6));
        helper.assertTrue(harvest.harvested() == rounds * field.size(), "Every ripe crop must be harvested, got " + harvest.harvested());
        helper.assertTrue(millis < 2000, "Harvesting " + harvest.harvested() + " crops took " + millis + " ms");
        helper.killAllEntitiesOfClass(ItemEntity.class);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void shearCutsShearablesIntoTheCastersInventory(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(2, 0, 2), "shearer");
        var context = new SpellContext().setPlayer(player);
        Sheep woolly = helper.spawnWithNoFreeWill(EntityType.SHEEP, new BlockPos(0, 0, 0));
        Sheep bare = helper.spawnWithNoFreeWill(EntityType.SHEEP, new BlockPos(4, 0, 0));
        bare.setSheared(true);
        Cow cow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(0, 0, 4));
        var mooshroom = helper.spawnWithNoFreeWill(EntityType.MOOSHROOM, new BlockPos(4, 0, 4));
        shear(EntityListWrapper.make(List.of(woolly, bare, cow, mooshroom))).execute(context);
        helper.assertTrue(woolly.isSheared() && between(player.getInventory().countItem(Items.WHITE_WOOL), 1, 3),
                "The woolly sheep must give its wool to the caster, got " + player.getInventory().countItem(Items.WHITE_WOOL));
        helper.assertTrue(mooshroom.isRemoved() && player.getInventory().countItem(Items.RED_MUSHROOM) == 5,
                "The mooshroom must turn into a cow and give 5 mushrooms");
        helper.assertTrue(!cow.isRemoved(), "A cow cannot be sheared");

        // A single Entity works too, and protection stops it.
        Sheep guarded = helper.spawnWithNoFreeWill(EntityType.SHEEP, new BlockPos(2, 0, 4));
        Consumer<PlayerInteractEvent.EntityInteract> deny = event -> { if (event.getTarget() == guarded) event.setCanceled(true); };
        NeoForge.EVENT_BUS.addListener(deny);
        try {
            shear(guarded).execute(context);
        } finally {
            NeoForge.EVENT_BUS.unregister(deny);
        }
        helper.assertTrue(!guarded.isSheared(), "A protected sheep must keep its wool");
        shear(guarded).execute(context);
        helper.assertTrue(guarded.isSheared(), "A single sheep must be sheared");

        Sheep far = helper.spawnWithNoFreeWill(EntityType.SHEEP, new BlockPos(2, 0, 0));
        player.moveTo(player.getX() + 40, player.getY(), player.getZ());
        helper.assertTrue(SpellRuntimeException.OUTSIDE_RADIUS.equals(castError(() -> shear(far).execute(context))),
                "A target out of range must fail");
        helper.assertTrue(!far.isSheared(), "A failed cast must shear nothing");
        int[] stats = stats(shear(null));
        helper.assertTrue(stats[0] == 20 && stats[1] == 40, "Shear: potency 20, cost 40, got " + Arrays.toString(stats));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void breedFeedsAnimalsFromTheInventory(GameTestHelper helper) throws Exception {
        List<Component> messages = new ArrayList<>();
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "breeder")) {
            @Override
            public void sendSystemMessage(Component message) {
                messages.add(message);
            }
        };
        player.moveTo(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(2, 0, 2))));
        var context = new SpellContext().setPlayer(player);
        Inventory inventory = player.getInventory();
        inventory.setItem(0, new ItemStack(Items.WHEAT, 2));
        inventory.setItem(5, new ItemStack(Items.TROPICAL_FISH_BUCKET));
        inventory.setItem(6, new ItemStack(Items.HAY_BLOCK));
        inventory.setItem(7, new ItemStack(Items.BEEF));
        inventory.setItem(8, new ItemStack(Items.SUGAR));
        inventory.setItem(Inventory.SLOT_OFFHAND, new ItemStack(Items.GOLDEN_CARROT));

        Cow cow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(0, 0, 0));
        Cow otherCow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(1, 0, 0));
        Cow calf = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(2, 0, 0));
        calf.setAge(-24000);
        var wildWolf = helper.spawnWithNoFreeWill(EntityType.WOLF, new BlockPos(3, 0, 0));
        var pet = helper.spawnWithNoFreeWill(EntityType.WOLF, new BlockPos(4, 0, 0));
        pet.setTame(true, false);
        var wildHorse = helper.spawnWithNoFreeWill(EntityType.HORSE, new BlockPos(0, 0, 4));
        var horse = helper.spawnWithNoFreeWill(EntityType.HORSE, new BlockPos(1, 0, 4));
        horse.setTamed(true);
        var llama = helper.spawnWithNoFreeWill(EntityType.LLAMA, new BlockPos(2, 0, 4));
        llama.setTamed(true);
        var axolotl = helper.spawnWithNoFreeWill(EntityType.AXOLOTL, new BlockPos(3, 0, 4));
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(4, 0, 4));
        List<Entity> animals = List.of(cow, otherCow, calf, wildWolf, pet, wildHorse, horse, llama, axolotl, pig);
        breed(EntityListWrapper.make(animals)).execute(context);

        helper.assertTrue(cow.isInLove() && otherCow.isInLove() && inventory.countItem(Items.WHEAT) == 0,
                "Both cows must eat a wheat each and fall in love");
        helper.assertTrue(!calf.isInLove() && !wildWolf.isInLove() && !wildHorse.isInLove(),
                "A calf, an untamed wolf and an untamed horse must be skipped");
        helper.assertTrue(pet.isInLove() && inventory.countItem(Items.BEEF) == 0, "The tamed wolf must eat the beef");
        helper.assertTrue(horse.isInLove() && inventory.getItem(Inventory.SLOT_OFFHAND).isEmpty() && inventory.countItem(Items.SUGAR) == 1,
                "The tamed horse must take the golden carrot from the off hand, not the sugar");
        helper.assertTrue(llama.isInLove() && inventory.countItem(Items.HAY_BLOCK) == 0, "The tamed llama must eat the hay bale");
        helper.assertTrue(axolotl.isInLove() && inventory.getItem(5).is(Items.WATER_BUCKET),
                "The axolotl must eat the fish and leave the water bucket, got " + inventory.getItem(5));
        helper.assertTrue(!pig.isInLove() && messages.size() == 1
                        && messages.get(0).getContents() instanceof TranslatableContents contents
                        && SpellWarning.NO_BREEDING_FOOD.equals(contents.getKey()),
                "The hungry pig must be named in one warning, got " + messages);

        // In creative mode no food is used; protection skips the animal without a warning.
        Cow creativeCow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(0, 0, 2));
        Cow guardedCow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(4, 0, 2));
        inventory.setItem(0, new ItemStack(Items.WHEAT, 1));
        player.getAbilities().instabuild = true;
        messages.clear();
        Consumer<PlayerInteractEvent.EntityInteract> deny = event -> { if (event.getTarget() == guardedCow) event.setCanceled(true); };
        NeoForge.EVENT_BUS.addListener(deny);
        try {
            breed(EntityListWrapper.make(List.of(creativeCow, guardedCow))).execute(context);
        } finally {
            NeoForge.EVENT_BUS.unregister(deny);
        }
        helper.assertTrue(creativeCow.isInLove() && !guardedCow.isInLove() && inventory.countItem(Items.WHEAT) == 1 && messages.isEmpty(),
                "Creative must keep the wheat and protection must skip the cow quietly");
        int[] stats = stats(breed(null));
        helper.assertTrue(stats[0] == 20 && stats[1] == 40, "Breed: potency 20, cost 40, got " + Arrays.toString(stats));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void connectedBlocksFollowsFacesEdgesAndCorners(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(2, 0, 4), "vein-finder");
        var context = new SpellContext().setPlayer(player);
        BlockPos a = new BlockPos(0, 0, 0);
        BlockPos b = new BlockPos(1, 0, 0);
        BlockPos c = new BlockPos(2, 1, 1);
        BlockPos apart = new BlockPos(4, 3, 3);
        helper.setBlock(a, Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
        helper.setBlock(b, Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
        helper.setBlock(c, Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
        helper.setBlock(apart, Blocks.OAK_LOG);
        helper.setBlock(a.above(), Blocks.DIRT);
        Vector3 start = center(helper.absolutePos(a));

        Object all = connectedBlocks(Map.of(POSITION, start, MAX, 10.0)).execute(context);
        List<Vector3> expected = List.of(corner(helper.absolutePos(a)), corner(helper.absolutePos(b)), corner(helper.absolutePos(c)));
        helper.assertTrue(all instanceof VectorListWrapper list && toList(list).equals(expected),
                "Logs of any axis touching by a face or a corner must be found nearest first, got " + all);
        Object two = connectedBlocks(Map.of(POSITION, start, MAX, 2.0)).execute(context);
        helper.assertTrue(two instanceof VectorListWrapper list && toList(list).equals(expected.subList(0, 2)),
                "The maximum must cut off the farthest block, got " + two);
        Object air = connectedBlocks(Map.of(POSITION, center(helper.absolutePos(new BlockPos(3, 2, 0))), MAX, 10.0)).execute(context);
        helper.assertTrue(air instanceof VectorListWrapper list && list.size() == 0, "Air must give an empty list");
        helper.assertTrue(SpellRuntimeException.NON_POSITIVE_VALUE.equals(castError(() ->
                connectedBlocks(Map.of(POSITION, start, MAX, 0.0)).execute(context))), "A maximum below 1 must fail");

        // From 30.5 blocks away the corner block is out of range and is not followed.
        BlockPos origin = helper.absolutePos(a);
        player.moveTo(origin.getX() - 30.5, origin.getY(), origin.getZ());
        Object near = connectedBlocks(Map.of(POSITION, corner(origin), MAX, 10.0)).execute(context);
        helper.assertTrue(near instanceof VectorListWrapper list && toList(list).equals(expected.subList(0, 2)),
                "A block out of range must not be followed, got " + near);
        helper.succeed();
    }

    private static List<Vector3> toList(VectorListWrapper list) {
        List<Vector3> result = new ArrayList<>();
        list.forEach(result::add);
        return result;
    }
}
