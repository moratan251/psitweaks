package com.moratan251.psitweaks.common.spells.item;

import java.util.List;
import net.minecraft.world.item.ItemStack;

/**
 * Output of Operator: Strict Item. Item filter inputs read it as "the same item with the same data components" instead
 * of matching the item type only. The templates are copies with a count of 1 and are never empty.
 */
public record StrictItemFilter(List<ItemStack> templates) {
    public StrictItemFilter {
        templates = templates.stream().map(stack -> stack.copyWithCount(1)).toList();
    }

    public boolean test(ItemStack stack) {
        for (ItemStack template : templates) {
            if (ItemStack.isSameItemSameComponents(stack, template)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Strict" + templates;
    }
}
