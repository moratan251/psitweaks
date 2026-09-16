package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import net.minecraft.resources.ResourceLocation;
import vazkii.psi.api.spell.Spell;

public class PieceSelectorIdeaStorageChemicalAmount extends PieceSelectorIdeaStorageAmountBase {
    public PieceSelectorIdeaStorageChemicalAmount(Spell spell) {
        super(spell);
    }

    @Override
    protected double storedAmount(PlayerIdeaStorage storage, ResourceLocation id) {
        return storage.chemicalAmountById(id);
    }
}
