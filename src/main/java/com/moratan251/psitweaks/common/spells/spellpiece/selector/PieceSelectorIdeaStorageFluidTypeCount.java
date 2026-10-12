package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import vazkii.psi.api.spell.Spell;

/** Number of fluid types currently stored; component variants count as separate types, as in the storage limit. */
public class PieceSelectorIdeaStorageFluidTypeCount extends PieceSelectorIdeaStorageStatusBase {
    public PieceSelectorIdeaStorageFluidTypeCount(Spell spell) {
        super(spell);
    }

    @Override
    protected double status(PlayerIdeaStorage storage) {
        return storage.fluidTypeCount();
    }
}
