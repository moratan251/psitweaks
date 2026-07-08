package com.moratan251.psitweaks.common.spells;

import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

final class SpellItemDataUtils {
    private SpellItemDataUtils() {
    }

    static CompoundTag getCustomData(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getTag() == null) {
            return new CompoundTag();
        }
        return stack.getTag().copy();
    }

    static void updateCustomData(ItemStack stack, Consumer<CompoundTag> updater) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        updater.accept(tag);
        stack.setTag(tag);
    }
}