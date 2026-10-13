package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import vazkii.psi.api.spell.Spell;

public class PieceOperatorInsideRegion extends PieceOperatorRegionFilterBase {
    public PieceOperatorInsideRegion(Spell spell) {
        super(spell);
    }

    @Override
    protected boolean keepInside() {
        return true;
    }
}
