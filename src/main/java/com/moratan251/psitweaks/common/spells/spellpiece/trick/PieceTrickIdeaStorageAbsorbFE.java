package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import vazkii.psi.api.spell.Spell;

public class PieceTrickIdeaStorageAbsorbFE extends PieceTrickIdeaStorageFEBase {
    public PieceTrickIdeaStorageAbsorbFE(Spell spell) {
        super(spell);
    }

    @Override
    protected boolean absorbs() {
        return true;
    }
}
