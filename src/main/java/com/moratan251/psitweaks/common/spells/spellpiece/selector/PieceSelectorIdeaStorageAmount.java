package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.mode.BlockResourceModes;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import net.minecraft.resources.ResourceLocation;
import vazkii.psi.api.spell.Spell;

/**
 * Stored amount of one resource: items by ID with every NBT variant together (an Item input uses its ID), fluids and
 * chemicals in mB by ID, or the stored FE. A chemical ID adds up every Mekanism chemical kind registered under it.
 */
public class PieceSelectorIdeaStorageAmount extends PieceSelectorIdeaStorageResourceBase {
    public PieceSelectorIdeaStorageAmount(Spell spell) {
        super(spell);
    }

    @Override
    protected double read(PlayerIdeaStorage storage, PsitweaksModeOption kind, Object input) {
        if (kind == BlockResourceModes.ENERGY) {
            return storage.energy();
        }
        ResourceLocation id = idOf(input);
        if (id == null) {
            return 0.0D;
        }
        if (kind == BlockResourceModes.FLUID) {
            return storage.fluidAmountById(id);
        }
        if (kind == BlockResourceModes.CHEMICAL) {
            return storage.chemicalAmountById(id);
        }
        return storage.itemAmountById(id);
    }
}
