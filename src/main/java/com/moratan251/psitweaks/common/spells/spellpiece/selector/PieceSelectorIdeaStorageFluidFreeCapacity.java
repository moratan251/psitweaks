package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import vazkii.psi.api.spell.Spell;

/** mB of one fluid (without extra data components) the storage accepts right now; 0 for a new type with no free slot. */
public class PieceSelectorIdeaStorageFluidFreeCapacity extends PieceSelectorIdeaStorageAmountBase {
    public PieceSelectorIdeaStorageFluidFreeCapacity(Spell spell) {
        super(spell);
    }

    @Override
    protected double storedAmount(PlayerIdeaStorage storage, ResourceLocation id) {
        return BuiltInRegistries.FLUID.getOptional(id)
                .filter(fluid -> fluid != Fluids.EMPTY)
                .map(fluid -> (double) storage.simulateInsertFluid(new FluidStack(fluid, 1), Long.MAX_VALUE))
                .orElse(0.0D);
    }
}
