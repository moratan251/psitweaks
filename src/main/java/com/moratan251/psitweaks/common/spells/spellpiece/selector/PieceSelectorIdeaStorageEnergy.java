package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import vazkii.psi.api.spell.Spell;

public class PieceSelectorIdeaStorageEnergy extends PieceSelectorIdeaStorageStatusBase {
    public PieceSelectorIdeaStorageEnergy(Spell spell) {
        super(spell);
    }

    @Override
    protected double status(PlayerIdeaStorage storage) {
        return storage.energy();
    }
}
