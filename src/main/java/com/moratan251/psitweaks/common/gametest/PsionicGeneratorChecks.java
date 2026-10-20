package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import com.moratan251.psitweaks.common.tile.machine.PsionicGeneratorBlockEntity;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Mekanism-only checks; call only when Mekanism is loaded. */
final class PsionicGeneratorChecks {
    private static final BlockPos GENERATOR = new BlockPos(2, 1, 2);
    private static final int TICKS = 20;
    /** 6400 FE/tick in Mekanism's joules (2.5 J per FE). */
    private static final long EJECT_RATE_JOULES = 16_000L;

    private PsionicGeneratorChecks() {
    }

    /**
     * Facing north, the generator ejects into a receiver on its back (south) at up to 6400 FE/tick and into none of
     * the receivers on its side or top. A generator placed without its item has no side config from the item, and
     * must still eject.
     */
    static void checkEjectsOutOfItsBack(GameTestHelper helper, boolean fromItem) {
        Block block = PsitweaksMekanismBlocks.PSIONIC_GENERATOR.get();
        helper.setBlock(GENERATOR, block.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
        PsionicGeneratorBlockEntity generator = helper.getBlockEntity(GENERATOR);
        if (fromItem) {
            generator.applyComponentsFromItemStack(new ItemStack(block));
        }
        long stored = PsionicGeneratorBlockEntity.getEnergyCapacityJoules();
        generator.getEnergyContainer().setEnergy(stored);
        IEnergyContainer back = placeReceiver(helper, GENERATOR.south(), Direction.SOUTH);
        IEnergyContainer side = placeReceiver(helper, GENERATOR.east(), Direction.EAST);
        IEnergyContainer top = placeReceiver(helper, GENERATOR.above(), Direction.UP);

        helper.runAfterDelay(TICKS, () -> {
            long ejected = stored - generator.getEnergyContainer().getEnergy();
            helper.assertTrue(ejected > 0 && back.getEnergy() == ejected,
                    "ejected " + ejected + " J, the receiver on the back got " + back.getEnergy() + " J");
            helper.assertTrue(ejected <= (TICKS + 1) * EJECT_RATE_JOULES,
                    "ejected " + ejected + " J in " + TICKS + " ticks, over 6400 FE/tick");
            helper.assertTrue(side.getEnergy() == 0 && top.getEnergy() == 0,
                    "ejected into the side (" + side.getEnergy() + " J) or the top (" + top.getEnergy() + " J)");
            helper.succeed();
        });
    }

    /** An energy cube with its default config: input on every side but its front, which faces away. */
    private static IEnergyContainer placeReceiver(GameTestHelper helper, BlockPos pos, Direction away) {
        Block cube = PsitweaksMekanismBlocks.TRANSCENDENT_ENERGY_CUBE.get();
        helper.setBlock(pos, cube.defaultBlockState().setValue(BlockStateProperties.FACING, away));
        TileEntityMekanism tile = helper.getBlockEntity(pos);
        tile.applyComponentsFromItemStack(new ItemStack(cube));
        return tile.getEnergyContainers(null).getFirst();
    }
}
