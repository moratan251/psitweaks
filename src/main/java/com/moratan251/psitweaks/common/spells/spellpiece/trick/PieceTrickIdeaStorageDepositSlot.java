package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import vazkii.psi.api.spell.Spell;

public class PieceTrickIdeaStorageDepositSlot extends PieceTrickIdeaStorageSlotBase {
    public PieceTrickIdeaStorageDepositSlot(Spell spell) { super(spell); }
    @Override protected boolean deposit() { return true; }
}
