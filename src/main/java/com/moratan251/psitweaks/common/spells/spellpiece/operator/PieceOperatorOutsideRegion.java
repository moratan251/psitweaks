package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import vazkii.psi.api.spell.Spell;

public class PieceOperatorOutsideRegion extends PieceOperatorRegionFilterBase {
    public PieceOperatorOutsideRegion(Spell spell) {
        super(spell);
    }

    @Override
    protected boolean keepInside() {
        return false;
    }
}
