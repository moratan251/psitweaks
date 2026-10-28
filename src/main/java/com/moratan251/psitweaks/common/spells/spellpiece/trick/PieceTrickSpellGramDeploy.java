package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramProperty;
import org.jetbrains.annotations.Nullable;
import vazkii.psi.api.spell.Spell;

/**
 * 作動式: 魔法式展開（試作）。性質を持たない素の領域型を置く。威力は強度だけに効き、性質は性質付与の作動式で足す。
 */
public class PieceTrickSpellGramDeploy extends PieceTrickSpellGramRegionBase {
    private static final Stats STATS = new Stats(40.0D, 20.0D, 5.0D, 150.0D, 100.0D, 30.0D);

    public PieceTrickSpellGramDeploy(Spell spell) {
        super(spell);
    }

    @Override
    protected Stats stats() {
        return STATS;
    }

    @Override
    @Nullable
    protected SpellGramProperty property() {
        return null;
    }
}
