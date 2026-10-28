package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramProperty;
import vazkii.psi.api.spell.Spell;

/** 作動式: 凍結領域（試作）。性質「凍結」の付いた領域型の魔法式オブジェクトを置く。 */
public class PieceTrickIceRegion extends PieceTrickSpellGramRegionBase {
    private static final Stats STATS = new Stats(100.0D, 60.0D, 5.0D, 400.0D, 300.0D, 30.0D);

    public PieceTrickIceRegion(Spell spell) {
        super(spell);
    }

    @Override
    protected Stats stats() {
        return STATS;
    }

    @Override
    protected SpellGramProperty property() {
        return SpellGramProperty.ICE;
    }
}
