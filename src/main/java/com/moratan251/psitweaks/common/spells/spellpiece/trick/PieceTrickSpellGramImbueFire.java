package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramProperty;
import vazkii.psi.api.spell.Spell;

/** 作動式: 灼熱付与（試作）。自分の領域型に性質「灼熱」を足す。 */
public class PieceTrickSpellGramImbueFire extends PieceTrickSpellGramImbueBase {
    public PieceTrickSpellGramImbueFire(Spell spell) {
        super(spell);
    }

    @Override
    protected SpellGramProperty property() {
        return SpellGramProperty.FIRE;
    }
}
