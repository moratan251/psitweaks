package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import vazkii.psi.api.spell.Spell;

/** Moves a resource from the block face into the caster's Ideaspace Storage; FE mode absorbs FE. */
public class PieceTrickIdeaStorageDeposit extends PieceTrickIdeaStorageResourceBase {
    public PieceTrickIdeaStorageDeposit(Spell spell) { super(spell); }
    @Override protected boolean deposit() { return true; }
}
