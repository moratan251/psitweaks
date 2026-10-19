package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import java.util.ArrayList;
import java.util.List;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.item.AttachedItems;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.text.BooleanStateDisplay;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

/** Mekanism-only checks; call only when Mekanism is loaded. */
final class MachineItemSlotChecks {
    private MachineItemSlotChecks() {
    }

    /**
     * A broken machine's item keeps its slots, and Mekanism's shift tooltip reads them through the item's registered
     * slot layout. The layout must exist and match the machine's own slots.
     */
    static void checkMachineItemsDescribeTheirSlots(GameTestHelper helper) {
        check(helper, PsitweaksMekanismBlocks.SCULK_ERODER.get(), PsitweaksMekanismBlocks.SCULK_ERODER, new BlockPos(1, 1, 1));
        check(helper, PsitweaksMekanismBlocks.PROGRAM_RESEARCHER.get(), PsitweaksMekanismBlocks.PROGRAM_RESEARCHER, new BlockPos(3, 1, 1));
        check(helper, PsitweaksMekanismBlocks.MATERIAL_MUTATOR.get(), PsitweaksMekanismBlocks.MATERIAL_MUTATOR, new BlockPos(1, 1, 3));
        check(helper, PsitweaksMekanismBlocks.PSIONIC_GENERATOR.get(), PsitweaksMekanismBlocks.PSIONIC_GENERATOR, new BlockPos(3, 1, 3));
    }

    private static void check(GameTestHelper helper, Block block, ItemLike item, BlockPos pos) {
        helper.setBlock(pos, block);
        if (!(helper.getBlockEntity(pos) instanceof TileEntityMekanism tile)) {
            helper.fail("No Mekanism tile for " + block);
            return;
        }
        int slots = tile.getInventorySlots(null).size();
        List<ItemStack> contents = new ArrayList<>();
        contents.add(new ItemStack(Items.STONE));
        while (contents.size() < slots) {
            contents.add(ItemStack.EMPTY);
        }
        ItemStack stack = new ItemStack(item);
        stack.set(MekanismDataComponents.ATTACHED_ITEMS, new AttachedItems(contents));
        int described = ContainerType.ITEM.getAttachmentContainersIfPresent(stack).size();
        helper.assertTrue(described == slots, item + " describes " + described + " slots, its machine has " + slots);
        helper.assertTrue(BooleanStateDisplay.YesNo.hasInventory(stack) == BooleanStateDisplay.YesNo.of(true, true),
                item + " must report its stored contents");
    }
}
