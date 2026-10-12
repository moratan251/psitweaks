package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.spell.Spell;

public class PieceSelectorIdeaStorageItemAmount extends PieceSelectorIdeaStorageItemBase {
    public PieceSelectorIdeaStorageItemAmount(Spell spell) {
        super(spell);
    }

    @Override
    protected double storedAmount(PlayerIdeaStorage storage, ResourceLocation id) {
        return storage.itemAmountById(id);
    }

    /** Item input uses only the item's ID, so every component variant is counted together. */
    @Override
    protected double itemValue(PlayerIdeaStorage storage, ItemStack stack) {
        return storage.itemAmountById(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }
}
