package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.storage.connector.ConnectorResource.Kind;
import vazkii.psi.api.spell.Spell;

public class PieceTrickIdeaStorageWithdrawChemical extends PieceTrickIdeaStorageResourceBase {
    public PieceTrickIdeaStorageWithdrawChemical(Spell spell) { super(spell); }
    @Override protected Kind kind() { return Kind.CHEMICAL; }
    @Override protected boolean deposit() { return false; }
}
