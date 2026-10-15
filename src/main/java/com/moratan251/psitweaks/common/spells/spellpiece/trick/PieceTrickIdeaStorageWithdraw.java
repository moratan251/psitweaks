package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import vazkii.psi.api.spell.Spell;

/** Moves a resource from the caster's Ideaspace Storage into the block face; FE mode supplies FE. */
public class PieceTrickIdeaStorageWithdraw extends PieceTrickIdeaStorageResourceBase {
    public PieceTrickIdeaStorageWithdraw(Spell spell) { super(spell); }
    @Override protected boolean deposit() { return false; }
}
