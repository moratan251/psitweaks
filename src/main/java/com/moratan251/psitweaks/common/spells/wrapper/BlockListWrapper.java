package com.moratan251.psitweaks.common.spells.wrapper;

import com.moratan251.psitweaks.api.PsitweaksListLike;
import com.moratan251.psitweaks.api.value.BlockValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public final class BlockListWrapper implements Iterable<BlockValue>, PsitweaksListLike {
    public static final BlockListWrapper EMPTY = new BlockListWrapper(List.of());

    private final List<BlockValue> list;

    private BlockListWrapper(List<BlockValue> list) {
        this.list = copyValues(list);
    }

    public static BlockListWrapper make(List<BlockValue> values) {
        if (values == null || values.isEmpty()) {
            return EMPTY;
        }

        List<BlockValue> result = new ArrayList<>(values.size());
        for (BlockValue value : values) {
            if (value != null && !containsEquivalent(result, value)) {
                result.add(copyBlock(value));
            }
        }

        return result.isEmpty() ? EMPTY : new BlockListWrapper(result);
    }

    public int size() {
        return list.size();
    }

    public BlockValue get(int index) {
        return copyBlock(list.get(index));
    }

    public List<BlockValue> asList() {
        return Collections.unmodifiableList(copyValues(list));
    }

    @Override
    public Iterator<BlockValue> iterator() {
        return asList().iterator();
    }

    @Override
    public String toString() {
        return list.toString();
    }

    public static boolean equivalent(BlockValue left, BlockValue right) {
        return left != null && right != null
                && Objects.equals(left.dimension(), right.dimension())
                && Objects.equals(left.blockPos(), right.blockPos())
                && Objects.equals(left.state(), right.state());
    }

    private static boolean containsEquivalent(List<BlockValue> values, BlockValue candidate) {
        for (BlockValue value : values) {
            if (equivalent(value, candidate)) {
                return true;
            }
        }
        return false;
    }

    private static List<BlockValue> copyValues(List<BlockValue> values) {
        List<BlockValue> result = new ArrayList<>(values.size());
        for (BlockValue value : values) {
            if (value != null) {
                result.add(copyBlock(value));
            }
        }
        return List.copyOf(result);
    }

    private static BlockValue copyBlock(BlockValue value) {
        return new BlockValue(value.dimension(), value.blockPos(), value.state());
    }
}
