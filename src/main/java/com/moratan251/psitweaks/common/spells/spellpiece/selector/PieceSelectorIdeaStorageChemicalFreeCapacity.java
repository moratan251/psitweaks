package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.compat.IdeaStorageLogisticsMekanism;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import net.minecraft.resources.ResourceLocation;
import vazkii.psi.api.spell.Spell;

/** mB of one chemical the storage accepts right now. Without Mekanism no chemical can enter, so it is always 0. */
public class PieceSelectorIdeaStorageChemicalFreeCapacity extends PieceSelectorIdeaStorageAmountBase {
    public PieceSelectorIdeaStorageChemicalFreeCapacity(Spell spell) {
        super(spell);
    }

    @Override
    protected double storedAmount(PlayerIdeaStorage storage, ResourceLocation id) {
        return MekanismCompat.isMekanismLoaded() ? IdeaStorageLogisticsMekanism.freeCapacity(storage, id) : 0.0D;
    }
}
