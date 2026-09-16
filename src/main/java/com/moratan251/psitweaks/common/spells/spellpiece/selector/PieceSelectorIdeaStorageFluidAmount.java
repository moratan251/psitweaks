package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import net.minecraft.resources.ResourceLocation;
import vazkii.psi.api.spell.Spell;

public class PieceSelectorIdeaStorageFluidAmount extends PieceSelectorIdeaStorageAmountBase {
    public PieceSelectorIdeaStorageFluidAmount(Spell spell) {
        super(spell);
    }

    @Override
    protected double storedAmount(PlayerIdeaStorage storage, ResourceLocation id) {
        return storage.fluidAmountById(id);
    }
}
