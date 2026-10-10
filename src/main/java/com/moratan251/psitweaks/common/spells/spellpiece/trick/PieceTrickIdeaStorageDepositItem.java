package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.storage.connector.ConnectorResource.Kind;
import vazkii.psi.api.spell.Spell;

public class PieceTrickIdeaStorageDepositItem extends PieceTrickIdeaStorageItemBase {
    public PieceTrickIdeaStorageDepositItem(Spell spell) { super(spell); }
    @Override protected Kind kind() { return Kind.ITEM; }
    @Override protected boolean deposit() { return true; }
}
