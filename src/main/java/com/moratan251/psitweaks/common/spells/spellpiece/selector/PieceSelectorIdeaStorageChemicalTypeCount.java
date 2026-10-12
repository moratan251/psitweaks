package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import vazkii.psi.api.spell.Spell;

/** Number of chemical types currently stored, one per chemical ID, as counted against the storage limit. */
public class PieceSelectorIdeaStorageChemicalTypeCount extends PieceSelectorIdeaStorageStatusBase {
    public PieceSelectorIdeaStorageChemicalTypeCount(Spell spell) {
        super(spell);
    }

    @Override
    protected double status(PlayerIdeaStorage storage) {
        return storage.chemicalTypeCount();
    }
}
