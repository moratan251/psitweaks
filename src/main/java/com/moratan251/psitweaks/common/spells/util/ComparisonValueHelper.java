package com.moratan251.psitweaks.common.spells.util;

import com.moratan251.psitweaks.api.value.BlockValue;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.wrapper.NumberListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

public final class ComparisonValueHelper {
    private ComparisonValueHelper() {
    }

    public static boolean equalsValue(Object left, Object right) {
        if (left == right) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
            return Double.compare(leftNumber.doubleValue(), rightNumber.doubleValue()) == 0;
        }
        if (left instanceof BlockValue leftBlock && right instanceof BlockValue rightBlock) {
            return blockEquals(leftBlock, rightBlock);
        }
        if (left instanceof Vector3 leftVector && right instanceof Vector3 rightVector) {
            return leftVector.equals(rightVector);
        }
        if (left instanceof SpellItemValue leftItem && right instanceof SpellItemValue rightItem) {
            return itemEquals(leftItem, rightItem);
        }
        if (left instanceof StringListWrapper leftList && right instanceof StringListWrapper rightList) {
            return leftList.asList().equals(rightList.asList());
        }
        if (left instanceof NumberListWrapper leftList && right instanceof NumberListWrapper rightList) {
            return leftList.asList().equals(rightList.asList());
        }
        if (left instanceof VectorListWrapper leftList && right instanceof VectorListWrapper rightList) {
            return leftList.asList().equals(rightList.asList());
        }
        if (left instanceof SpellItemListWrapper leftList && right instanceof SpellItemListWrapper rightList) {
            return itemListEquals(leftList.asList(), rightList.asList());
        }
        if (left instanceof EntityListWrapper leftList && right instanceof EntityListWrapper rightList) {
            return entityListEquals(leftList, rightList);
        }
        return Objects.equals(left, right);
    }

    private static boolean itemEquals(SpellItemValue left, SpellItemValue right) {
        if (left.isEmpty() || right.isEmpty()) {
            return left.isEmpty() && right.isEmpty();
        }
        return left.source().equals(right.source()) && ItemStack.matches(left.copyStack(), right.copyStack());
    }

    private static boolean blockEquals(BlockValue left, BlockValue right) {
        return Objects.equals(left.dimension(), right.dimension())
                && Objects.equals(left.blockPos(), right.blockPos())
                && Objects.equals(left.state(), right.state());
    }

    private static boolean itemListEquals(List<SpellItemValue> left, List<SpellItemValue> right) {
        if (left.size() != right.size()) {
            return false;
        }
        for (int i = 0; i < left.size(); i++) {
            if (!itemEquals(left.get(i), right.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean entityListEquals(EntityListWrapper left, EntityListWrapper right) {
        Iterator<Entity> leftIterator = left.iterator();
        Iterator<Entity> rightIterator = right.iterator();
        while (leftIterator.hasNext() && rightIterator.hasNext()) {
            if (!Objects.equals(leftIterator.next(), rightIterator.next())) {
                return false;
            }
        }
        return !leftIterator.hasNext() && !rightIterator.hasNext();
    }
}
