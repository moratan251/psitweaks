package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramProperty;
import vazkii.psi.api.spell.Spell;

/** 作動式: 湿潤付与（試作）。自分の領域型に性質「湿潤」を足す。 */
public class PieceTrickSpellGramImbueWet extends PieceTrickSpellGramImbueBase {
    public PieceTrickSpellGramImbueWet(Spell spell) {
        super(spell);
    }

    @Override
    protected SpellGramProperty property() {
        return SpellGramProperty.WET;
    }
}
