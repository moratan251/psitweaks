package com.moratan251.psitweaks.common.storage.idea;

import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/**
 * Item ID + NBT + ForgeCaps 一式からなる不変の資源キー。
 * 内部の ItemStack は count=1 に正規化され、defensive copy で保持する。
 */
public final class ItemResourceKey {
    private final ItemStack template;
    private final CompoundTag identity;

    private ItemResourceKey(ItemStack template) {
        this.template = template;
        this.identity = template.save(new CompoundTag());
    }

    public static Optional<ItemResourceKey> of(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ItemResourceKey(stack.copyWithCount(1)));
    }

    public static Optional<ItemResourceKey> parse(Tag tag) {
        return tag instanceof CompoundTag compound ? ItemResourceKey.of(ItemStack.of(compound)) : Optional.empty();
    }

    public Tag save() {
        return identity.copy();
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
                && identity.equals(other.identity);
    }

    @Override
    public int hashCode() {
        return identity.hashCode();
    }

    @Override
    public String toString() {
        return template.getItem().toString();
    }
}
