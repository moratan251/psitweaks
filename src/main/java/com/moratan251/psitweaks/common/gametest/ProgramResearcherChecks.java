package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.recipe.ProgramResearchRecipe;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import com.moratan251.psitweaks.common.tile.machine.ProgramResearcherBlockEntity;
import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Mekanism-only checks; call only when Mekanism is loaded. */
final class ProgramResearcherChecks {
    private static final BlockPos RESEARCHER = new BlockPos(1, 1, 2);
    private static final BlockPos RECEIVER = new BlockPos(3, 1, 2);
    private static final String RECIPE = "program_research/program_burst";
    private static final String UPDATE_KEY = "researching";

    private ProgramResearcherChecks() {
    }

    /**
     * An idle researcher sends no program. Once it works on a recipe, the block update carries the recipe's output,
     * and a client copy of the researcher reads it back.
     */
    static void checkSendsResearchingProgram(GameTestHelper helper) {
        Block block = PsitweaksMekanismBlocks.PROGRAM_RESEARCHER.get();
        helper.setBlock(RESEARCHER, block.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
        helper.setBlock(RECEIVER, block.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
        ProgramResearcherBlockEntity researcher = helper.getBlockEntity(RESEARCHER);
        ProgramResearcherBlockEntity receiver = helper.getBlockEntity(RECEIVER);
        HolderLookup.Provider registries = helper.getLevel().registryAccess();
        helper.assertTrue(researcher.getResearchingItem() == Items.AIR
                        && !researcher.getReducedUpdateTag(registries).contains(UPDATE_KEY),
                "an idle researcher sends " + researcher.getResearchingItem());

        ProgramResearchRecipe recipe = (ProgramResearchRecipe) helper.getLevel().getRecipeManager()
                .byKey(Psitweaks.location(RECIPE)).orElseThrow().value();
        List<IInventorySlot> slots = researcher.getInventorySlots(null);
        int slot = 0;
        for (ProgramResearchRecipe.RequiredInput input : recipe.getInputs()) {
            slots.get(slot++).setStack(input.ingredient().getItems()[0].copyWithCount(input.count()));
        }
        researcher.getEnergyContainer().setEnergy(researcher.getEnergyContainer().getMaxEnergy());
        Item expected = recipe.getOutput().getItem();

        helper.runAfterDelay(5, () -> {
            helper.assertTrue(researcher.getResearchingItem() == expected,
                    "researching " + researcher.getResearchingItem() + ", expected " + expected);
            CompoundTag update = researcher.getReducedUpdateTag(registries);
            helper.assertTrue(update.contains(UPDATE_KEY), "the block update has no researched program: " + update);
            receiver.handleUpdateTag(update, registries);
            helper.assertTrue(receiver.getResearchingItem() == expected,
                    "a client copy read " + receiver.getResearchingItem() + ", expected " + expected);
            helper.succeed();
        });
    }
}
