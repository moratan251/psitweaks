package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import vazkii.psi.api.spell.Spell;

public class PieceTrickIdeaStorageSupplyFE extends PieceTrickIdeaStorageFEBase {
    public PieceTrickIdeaStorageSupplyFE(Spell spell) {
        super(spell);
    }

    @Override
    protected boolean absorbs() {
        return false;
    }
}
