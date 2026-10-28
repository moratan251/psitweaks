package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramProperty;
import vazkii.psi.api.spell.Spell;

/** 作動式: 湿潤領域（試作）。性質「湿潤」の付いた領域型の魔法式オブジェクトを置く。 */
public class PieceTrickWetRegion extends PieceTrickSpellGramRegionBase {
    private static final Stats STATS = new Stats(100.0D, 60.0D, 5.0D, 400.0D, 300.0D, 30.0D);

    public PieceTrickWetRegion(Spell spell) {
        super(spell);
    }

    @Override
    protected Stats stats() {
        return STATS;
    }

    @Override
    protected SpellGramProperty property() {
        return SpellGramProperty.WET;
    }
}
