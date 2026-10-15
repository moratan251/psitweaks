package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.compat.IdeaStorageLogisticsMekanism;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.mode.BlockResourceModes;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import vazkii.psi.api.spell.Spell;

/**
 * How much more of one resource the storage accepts right now: the per-type cap minus the stock, or 0 for a new type
 * while every type slot is taken. Capacity belongs to one exact resource, so an ID means it without extra data
 * components and an Item input keeps its own components. FE mode is the capacity minus the stored FE; chemicals read
 * 0 without Mekanism.
 */
public class PieceSelectorIdeaStorageFreeCapacity extends PieceSelectorIdeaStorageResourceBase {
    public PieceSelectorIdeaStorageFreeCapacity(Spell spell) {
        super(spell);
    }

    @Override
    protected double read(PlayerIdeaStorage storage, PsitweaksModeOption kind, Object input) {
        if (kind == BlockResourceModes.ENERGY) {
            return storage.insertEnergy(Long.MAX_VALUE, true);
        }
        if (kind == BlockResourceModes.ITEM && input instanceof SpellItemValue item) {
            return item.isEmpty() ? 0.0D : storage.simulateInsert(item.snapshot().copyWithCount(1), Long.MAX_VALUE);
        }
        ResourceLocation id = idOf(input);
        if (id == null) {
            return 0.0D;
        }
        if (kind == BlockResourceModes.FLUID) {
            return BuiltInRegistries.FLUID.getOptional(id)
                    .filter(fluid -> fluid != Fluids.EMPTY)
                    .map(fluid -> (double) storage.simulateInsertFluid(new FluidStack(fluid, 1), Long.MAX_VALUE))
                    .orElse(0.0D);
        }
        if (kind == BlockResourceModes.CHEMICAL) {
            return MekanismCompat.isMekanismLoaded() ? IdeaStorageLogisticsMekanism.freeCapacity(storage, id) : 0.0D;
        }
        return BuiltInRegistries.ITEM.getOptional(id)
                .filter(item -> item != Items.AIR)
                .map(item -> (double) storage.simulateInsert(new ItemStack(item), Long.MAX_VALUE))
                .orElse(0.0D);
    }
}
