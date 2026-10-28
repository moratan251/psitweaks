package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramProperty;
import vazkii.psi.api.spell.Spell;

/** 作動式: 凍結付与（試作）。自分の領域型に性質「凍結」を足す。 */
public class PieceTrickSpellGramImbueIce extends PieceTrickSpellGramImbueBase {
    public PieceTrickSpellGramImbueIce(Spell spell) {
        super(spell);
    }

    @Override
    protected SpellGramProperty property() {
        return SpellGramProperty.ICE;
    }
}
