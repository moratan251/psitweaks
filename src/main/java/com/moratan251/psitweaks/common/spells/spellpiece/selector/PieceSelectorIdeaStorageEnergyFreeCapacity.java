package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import vazkii.psi.api.spell.Spell;

/** FE the storage accepts right now (capacity minus stored FE, never negative). */
public class PieceSelectorIdeaStorageEnergyFreeCapacity extends PieceSelectorIdeaStorageStatusBase {
    public PieceSelectorIdeaStorageEnergyFreeCapacity(Spell spell) {
        super(spell);
    }

    @Override
    protected double status(PlayerIdeaStorage storage) {
        return storage.insertEnergy(Long.MAX_VALUE, true);
    }
}
