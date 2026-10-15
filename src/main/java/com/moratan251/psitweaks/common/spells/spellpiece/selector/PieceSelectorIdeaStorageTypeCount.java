package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.mode.BlockResourceModes;
import com.moratan251.psitweaks.common.spells.mode.IdeaStorageResourceModes;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import java.util.List;
import vazkii.psi.api.spell.Spell;

/**
 * Number of stored types of one resource kind, as counted against the storage limit: item and fluid variants with
 * different NBT count separately, chemicals once per Mekanism chemical kind and ID.
 */
public class PieceSelectorIdeaStorageTypeCount extends PieceSelectorIdeaStorageResourceBase {
    public PieceSelectorIdeaStorageTypeCount(Spell spell) {
        super(spell);
    }

    @Override
    protected boolean takesResource() {
        return false;
    }

    @Override
    public List<PsitweaksModeOption> getAvailableModeOptions() {
        return IdeaStorageResourceModes.TYPE_COUNT;
    }

    @Override
    protected double read(PlayerIdeaStorage storage, PsitweaksModeOption kind, Object input) {
        if (kind == BlockResourceModes.FLUID) {
            return storage.fluidTypeCount();
        }
        if (kind == BlockResourceModes.CHEMICAL) {
            return storage.chemicalTypeCount();
        }
        return storage.itemTypeCount();
    }
}
