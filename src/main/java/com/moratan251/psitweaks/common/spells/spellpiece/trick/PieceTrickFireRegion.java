package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramProperty;
import vazkii.psi.api.spell.Spell;

/** 作動式: 灼熱領域（試作）。性質「灼熱」の付いた領域型の魔法式オブジェクトを置く。 */
public class PieceTrickFireRegion extends PieceTrickSpellGramRegionBase {
    private static final Stats STATS = new Stats(100.0D, 60.0D, 5.0D, 400.0D, 300.0D, 30.0D);

    public PieceTrickFireRegion(Spell spell) {
        super(spell);
    }

    @Override
    protected Stats stats() {
        return STATS;
    }

    @Override
    protected SpellGramProperty property() {
        return SpellGramProperty.FIRE;
    }
}
