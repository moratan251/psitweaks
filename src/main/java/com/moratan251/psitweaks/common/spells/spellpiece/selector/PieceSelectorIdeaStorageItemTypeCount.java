package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import vazkii.psi.api.spell.Spell;

/** Number of item types currently stored; component variants count as separate types, as in the storage limit. */
public class PieceSelectorIdeaStorageItemTypeCount extends PieceSelectorIdeaStorageStatusBase {
    public PieceSelectorIdeaStorageItemTypeCount(Spell spell) {
        super(spell);
    }

    @Override
    protected double status(PlayerIdeaStorage storage) {
        return storage.itemTypeCount();
    }
}
