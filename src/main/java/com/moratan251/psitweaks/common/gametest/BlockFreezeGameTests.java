package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickFreezeBlock;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;

/** Trick: Block Freeze turns source water into ice and source lava into magma, and leaves flowing water and lava alone. */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BlockFreezeGameTests {
    private BlockFreezeGameTests() {
    }

    @GameTest(template = "connector_empty")
    public static void freezeBlockSkipsFlowingLiquids(GameTestHelper helper) throws Exception {
        FakePlayer player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "freeze-flowing"));
        player.moveTo(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(2, 1, 0))));
        SpellContext context = new SpellContext().setPlayer(player);

        // Each liquid is placed and frozen in the same tick, before the flowing ones spread or dry up.
        assertFreezes(helper, context, new BlockPos(1, 1, 1), Blocks.WATER, 0, Blocks.ICE);
        assertFreezes(helper, context, new BlockPos(3, 1, 1), Blocks.WATER, 1, Blocks.WATER);
        assertFreezes(helper, context, new BlockPos(1, 1, 3), Blocks.LAVA, 0, Blocks.MAGMA_BLOCK);
        assertFreezes(helper, context, new BlockPos(3, 1, 3), Blocks.LAVA, 2, Blocks.LAVA);
        helper.succeed();
    }

    private static void assertFreezes(GameTestHelper helper, SpellContext context, BlockPos pos, Block liquid, int level,
                                      Block expected) throws Exception {
        helper.setBlock(pos, liquid.defaultBlockState().setValue(LiquidBlock.LEVEL, level));
        BlockPos absolute = helper.absolutePos(pos);
        Vector3 center = new Vector3(absolute.getX() + 0.5, absolute.getY() + 0.5, absolute.getZ() + 0.5);
        new PieceTrickFreezeBlock(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return center;
            }
        }.execute(context);
        helper.assertBlockPresent(expected, pos);
        helper.setBlock(pos, Blocks.AIR);
    }
}
