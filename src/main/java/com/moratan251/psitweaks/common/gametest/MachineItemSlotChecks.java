package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.common.handler.MekanismMaterialMutationRecipeHandler;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import mekanism.api.Upgrade;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.item.AttachedItems;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.text.BooleanStateDisplay;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Mekanism-only checks; call only when Mekanism is loaded. */
final class MachineItemSlotChecks {
    private static final long STORED_ENERGY = 1_000L;
    private static final long STORED_CHEMICAL = 500L;

    private MachineItemSlotChecks() {
    }

    /**
     * A machine's item keeps its slots, and Mekanism's shift tooltip reads them through the item's registered slot
     * layout. The layout must exist and match the machine's own slots and tanks.
     */
    static void checkMachineItemsDescribeTheirSlots(GameTestHelper helper) {
        check(helper, PsitweaksMekanismBlocks.SCULK_ERODER.get(), PsitweaksMekanismBlocks.SCULK_ERODER, new BlockPos(1, 1, 1));
        check(helper, PsitweaksMekanismBlocks.PROGRAM_RESEARCHER.get(), PsitweaksMekanismBlocks.PROGRAM_RESEARCHER, new BlockPos(3, 1, 1));
        check(helper, PsitweaksMekanismBlocks.MATERIAL_MUTATOR.get(), PsitweaksMekanismBlocks.MATERIAL_MUTATOR, new BlockPos(1, 1, 3));
        check(helper, PsitweaksMekanismBlocks.PSIONIC_GENERATOR.get(), PsitweaksMekanismBlocks.PSIONIC_GENERATOR, new BlockPos(3, 1, 3));
        check(helper, PsitweaksMekanismBlocks.TRANSCENDENT_ENERGY_CUBE.get(), PsitweaksMekanismBlocks.TRANSCENDENT_ENERGY_CUBE, new BlockPos(2, 1, 2));
        check(helper, PsitweaksMekanismBlocks.AUTONOMOUS_CASTER.get(), PsitweaksMekanismBlocks.AUTONOMOUS_CASTER, new BlockPos(2, 1, 4));
    }

    /**
     * Breaking a machine keeps its contents in the dropped item, as with Mekanism's own machines: the item reads them
     * without crashing, spills them when it burns up, and gives them back when placed again.
     */
    static void checkBrokenMachinesKeepTheirContents(GameTestHelper helper) {
        breakAndRestore(helper, PsitweaksMekanismBlocks.SCULK_ERODER.get());
        breakAndRestore(helper, PsitweaksMekanismBlocks.PROGRAM_RESEARCHER.get());
        breakAndRestore(helper, PsitweaksMekanismBlocks.MATERIAL_MUTATOR.get());
        breakAndRestore(helper, PsitweaksMekanismBlocks.PSIONIC_GENERATOR.get());
        breakAndRestore(helper, PsitweaksMekanismBlocks.TRANSCENDENT_ENERGY_CUBE.get());
        breakAndRestore(helper, PsitweaksMekanismBlocks.AUTONOMOUS_CASTER.get());
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
        int tanks = tile.getChemicalTanks(null).size();
        int describedTanks = ContainerType.CHEMICAL.getContainerCount(new ItemStack(item));
        helper.assertTrue(describedTanks == tanks, item + " describes " + describedTanks + " tanks, its machine has " + tanks);
    }

    private static void breakAndRestore(GameTestHelper helper, Block block) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = new BlockPos(1, 1, 1);
        BlockPos absolute = helper.absolutePos(pos);
        helper.setBlock(pos, block);
        TileEntityMekanism tile = (TileEntityMekanism) helper.getBlockEntity(pos);
        ItemStack stored = fillFirstSlot(helper, tile, block);
        // The Autonomous Caster runs on Psionic Echo alone and has no energy container.
        boolean powered = !tile.getEnergyContainers(null).isEmpty();
        if (powered) {
            tile.getEnergyContainers(null).getFirst().setEnergy(STORED_ENERGY);
        }
        boolean muffled = tile.supportsUpgrades() && tile.getComponent().supports(Upgrade.MUFFLING);
        if (muffled) {
            tile.getComponent().addUpgrades(Upgrade.MUFFLING, 1);
        }
        List<IChemicalTank> tanks = tile.getChemicalTanks(null);
        if (!tanks.isEmpty()) {
            tanks.getFirst().setStack(new ChemicalStack(MekanismMaterialMutationRecipeHandler.getPsionicEchoChemicalHolder(), STORED_CHEMICAL));
        }

        level.destroyBlock(absolute, true, new FakePlayer(level, new GameProfile(UUID.randomUUID(), "machine-breaker")));
        List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(absolute).inflate(2));
        helper.assertTrue(drops.size() == 1 && drops.getFirst().getItem().is(block.asItem()),
                block + " must drop only itself, got " + drops.stream().map(ItemEntity::getItem).toList());
        ItemStack dropped = drops.getFirst().getItem().copy();
        drops.forEach(ItemEntity::discard);
        helper.assertTrue(BooleanStateDisplay.YesNo.hasInventory(dropped) == BooleanStateDisplay.YesNo.of(true, true),
                block + " item lost its contents");

        // Burning up spills the stored item (and installed upgrades) instead of crashing.
        ItemEntity burning = new ItemEntity(level, absolute.getX() + 0.5, absolute.getY() + 0.5, absolute.getZ() + 0.5, dropped.copy());
        burning.hurt(level.damageSources().lava(), Float.MAX_VALUE);
        List<ItemEntity> spilled = level.getEntitiesOfClass(ItemEntity.class, new AABB(absolute).inflate(2));
        helper.assertTrue(burning.isRemoved() && spilled.stream().anyMatch(entity -> ItemStack.isSameItem(entity.getItem(), stored)),
                block + " item spilled " + spilled.stream().map(ItemEntity::getItem).toList() + " without " + stored);
        spilled.forEach(ItemEntity::discard);

        // Placing it again gives everything back.
        helper.setBlock(pos, block);
        TileEntityMekanism placed = (TileEntityMekanism) helper.getBlockEntity(pos);
        placed.applyComponentsFromItemStack(dropped);
        helper.assertTrue(placed.getInventorySlots(null).stream().anyMatch(slot -> ItemStack.isSameItem(slot.getStack(), stored)),
                block + " lost its item when placed again");
        if (powered) {
            helper.assertTrue(placed.getEnergyContainers(null).getFirst().getEnergy() == STORED_ENERGY, block + " lost its energy");
        }
        if (muffled) {
            helper.assertTrue(placed.getComponent().getUpgrades(Upgrade.MUFFLING) == 1, block + " lost its upgrade");
        }
        if (!tanks.isEmpty()) {
            helper.assertTrue(placed.getChemicalTanks(null).getFirst().getStored() == STORED_CHEMICAL, block + " lost its chemical");
        }
        helper.setBlock(pos, Blocks.AIR);
    }

    /** Puts a stone, or an energy tablet for charge-only machines, into the first slot that takes it. */
    private static ItemStack fillFirstSlot(GameTestHelper helper, TileEntityMekanism tile, Block block) {
        for (ItemStack candidate : List.of(new ItemStack(Items.STONE), new ItemStack(MekanismItems.ENERGY_TABLET.get()))) {
            for (IInventorySlot slot : tile.getInventorySlots(null)) {
                if (slot.isItemValid(candidate)) {
                    slot.setStack(candidate.copy());
                    return candidate;
                }
            }
        }
        helper.fail(block + " takes neither a stone nor an energy tablet");
        return ItemStack.EMPTY;
    }
}
