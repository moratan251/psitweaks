package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vazkii.psi.api.spell.Spell;

/**
 * How many more of one item the storage accepts right now: the per-type cap minus the stock, or 0 for a new type
 * while every type slot is taken. Capacity belongs to one exact item, so an ID means the item without extra data
 * components and an Item input keeps its own components.
 */
public class PieceSelectorIdeaStorageItemFreeCapacity extends PieceSelectorIdeaStorageItemBase {
    public PieceSelectorIdeaStorageItemFreeCapacity(Spell spell) {
        super(spell);
    }

    @Override
    protected double storedAmount(PlayerIdeaStorage storage, ResourceLocation id) {
        return BuiltInRegistries.ITEM.getOptional(id)
                .filter(item -> item != Items.AIR)
                .map(item -> (double) storage.simulateInsert(new ItemStack(item), Long.MAX_VALUE))
                .orElse(0.0D);
    }

    @Override
    protected double itemValue(PlayerIdeaStorage storage, ItemStack stack) {
        return storage.simulateInsert(stack.copyWithCount(1), Long.MAX_VALUE);
    }
}
