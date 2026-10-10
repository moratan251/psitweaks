package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import vazkii.psi.api.spell.Spell;

public class PieceTrickIdeaStorageWithdrawSlot extends PieceTrickIdeaStorageSlotBase {
    public PieceTrickIdeaStorageWithdrawSlot(Spell spell) { super(spell); }
    @Override protected boolean deposit() { return false; }
}
