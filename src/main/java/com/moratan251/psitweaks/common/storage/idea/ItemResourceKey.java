package com.moratan251.psitweaks.common.storage.idea;

import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/**
 * Item ID + Data Component 一式からなる不変の資源キー。
 * 内部の ItemStack は count=1 に正規化され、defensive copy で保持する。
 */
public final class ItemResourceKey {
    private final ItemStack template;

    private ItemResourceKey(ItemStack template) {
        this.template = template;
    }

    public static Optional<ItemResourceKey> of(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ItemResourceKey(stack.copyWithCount(1)));
    }

    public static Optional<ItemResourceKey> parse(HolderLookup.Provider registries, Tag tag) {
        return ItemStack.parse(registries, tag).flatMap(ItemResourceKey::of);
    }

    public Tag save(HolderLookup.Provider registries) {
        return template.save(registries);
    }

    public ItemStack template() {
        return template.copy();
    }

    public int getMaxStackSize() {
        return Math.max(1, template.getMaxStackSize());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ItemResourceKey other
                && ItemStack.isSameItemSameComponents(this.template, other.template);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashItemAndComponents(this.template);
    }

    @Override
    public String toString() {
        return template.getItem().toString();
    }
}
