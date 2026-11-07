package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.handler.MaterialMutationRecipeHandler;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickFreezeBlock;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickInteractBlock;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickMaterialMutation;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickMeltBlock;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * Tricks that use or change a block must stay within the spell's range, must not load far chunks, and must ask the
 * events protection mods listen to (right-click for using a block, break for changing it).
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SpellProtectionGameTests {
    private static final String TEMPLATE = "connector_empty";
    private static final String ACCESS_DENIED = "psitweaks.spellerror.accessdenyed";

    private SpellProtectionGameTests() {
    }

    private interface CastAction {
        void run() throws SpellRuntimeException;
    }

    private static FakePlayer caster(GameTestHelper helper, String name) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        player.moveTo(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(2, 1, 0))));
        return player;
    }

    private static Vector3 center(BlockPos absolute) {
        return new Vector3(absolute.getX() + 0.5, absolute.getY() + 0.5, absolute.getZ() + 0.5);
    }

    private static String castError(CastAction action) {
        try {
            action.run();
            return null;
        } catch (SpellRuntimeException failure) {
            return failure.getMessage();
        }
    }

    private static PieceTrickInteractBlock interact(Vector3 position) {
        return new PieceTrickInteractBlock(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return position; }
        };
    }

    private static PieceTrickFreezeBlock freeze(Vector3 position) {
        return new PieceTrickFreezeBlock(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return position; }
        };
    }

    private static PieceTrickMeltBlock melt(Vector3 position) {
        return new PieceTrickMeltBlock(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return position; }
        };
    }

    private static PieceTrickMaterialMutation mutate(Vector3 position) {
        return new PieceTrickMaterialMutation(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return position; }
        };
    }

    /** F-019: Trick: Interact Block keeps to the spell range and asks the right-click event first. */
    @GameTest(template = TEMPLATE)
    public static void interactBlockKeepsToTheRangeAndAsksProtection(GameTestHelper helper) throws Exception {
        var player = caster(helper, "protected-interact");
        var context = new SpellContext().setPlayer(player);
        BlockPos trapdoor = new BlockPos(2, 1, 2);
        helper.setBlock(trapdoor, Blocks.OAK_TRAPDOOR);
        BlockPos absolute = helper.absolutePos(trapdoor);
        var piece = interact(center(absolute));

        Consumer<PlayerInteractEvent.RightClickBlock> deny = event -> {
            if (event.getPos().equals(absolute)) {
                event.setCanceled(true);
            }
        };
        NeoForge.EVENT_BUS.addListener(deny);
        String denied;
        try {
            denied = castError(() -> piece.execute(context));
        } finally {
            NeoForge.EVENT_BUS.unregister(deny);
        }
        helper.assertTrue(ACCESS_DENIED.equals(denied), "A protected block should refuse the spell, got " + denied);
        helper.assertBlockState(trapdoor, state -> !state.getValue(TrapDoorBlock.OPEN), () -> "A protected trapdoor was opened");

        piece.execute(context);
        helper.assertBlockState(trapdoor, state -> state.getValue(TrapDoorBlock.OPEN), () -> "The trapdoor did not open once allowed");

        var far = interact(center(absolute.offset(64, 0, 0)));
        helper.assertTrue(SpellRuntimeException.OUTSIDE_RADIUS.equals(castError(() -> far.execute(context))),
                "A block outside the spell range should be out of reach");
        helper.succeed();
    }

    /** F-021: Freeze, Melt and Material Mutation ask the break event, which protection mods listen to. */
    @GameTest(template = TEMPLATE)
    public static void changingTricksAskTheBreakEvent(GameTestHelper helper) throws Exception {
        var player = caster(helper, "protected-changer");
        var context = new SpellContext().setPlayer(player);
        BlockPos water = new BlockPos(1, 1, 2);
        BlockPos stone = new BlockPos(2, 1, 2);
        BlockPos mutable = new BlockPos(3, 1, 2);
        Block mutationInput = null;
        for (Map.Entry<Block, ItemStack> entry : MaterialMutationRecipeHandler.getAllMutationOutputs().entrySet()) {
            if (!entry.getValue().isEmpty() && entry.getKey().defaultBlockState().getDestroySpeed(helper.getLevel(), BlockPos.ZERO) >= 0
                    && !entry.getKey().defaultBlockState().isAir()) {
                mutationInput = entry.getKey();
                break;
            }
        }
        helper.assertTrue(mutationInput != null, "No Material Mutation recipe is loaded");
        helper.setBlock(water, Blocks.WATER);
        helper.setBlock(stone, Blocks.STONE);
        helper.setBlock(mutable, mutationInput);
        BlockPos waterAbs = helper.absolutePos(water);
        BlockPos stoneAbs = helper.absolutePos(stone);
        BlockPos mutableAbs = helper.absolutePos(mutable);

        Consumer<BlockEvent.BreakEvent> deny = event -> {
            if (event.getPos().equals(waterAbs) || event.getPos().equals(stoneAbs) || event.getPos().equals(mutableAbs)) {
                event.setCanceled(true);
            }
        };
        NeoForge.EVENT_BUS.addListener(deny);
        try {
            freeze(center(waterAbs)).execute(context);
            melt(center(stoneAbs)).execute(context);
            mutate(center(mutableAbs)).execute(context);
        } finally {
            NeoForge.EVENT_BUS.unregister(deny);
        }
        Block input = mutationInput;
        helper.assertBlockPresent(Blocks.WATER, water);
        helper.assertBlockPresent(Blocks.STONE, stone);
        helper.assertBlockPresent(input, mutable);

        player.getAbilities().mayBuild = false;
        freeze(center(waterAbs)).execute(context);
        helper.assertBlockPresent(Blocks.WATER, water);
        player.getAbilities().mayBuild = true;

        freeze(center(waterAbs)).execute(context);
        melt(center(stoneAbs)).execute(context);
        mutate(center(mutableAbs)).execute(context);
        helper.assertBlockPresent(Blocks.ICE, water);
        helper.assertBlockPresent(Blocks.MAGMA_BLOCK, stone);
        helper.assertBlockNotPresent(input, mutable);
        boolean dropped = !helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(mutableAbs).inflate(1.5)).isEmpty();
        helper.assertTrue(dropped, "Material Mutation dropped nothing once allowed");
        helper.succeed();
    }
}
