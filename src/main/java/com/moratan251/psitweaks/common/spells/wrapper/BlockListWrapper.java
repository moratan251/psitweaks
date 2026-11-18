package com.moratan251.psitweaks.common.spells.wrapper;

import com.moratan251.psitweaks.api.PsitweaksListLike;
import com.moratan251.psitweaks.api.value.BlockValue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A List of Blocks without equivalent duplicates. {@link BlockValue} is immutable, so the elements are shared rather
 * than copied: an element keeps its identity, which lets the NBT pieces reuse its NBT within a cast.
 */
public final class BlockListWrapper implements Iterable<BlockValue>, PsitweaksListLike {
    public static final BlockListWrapper EMPTY = new BlockListWrapper(List.of());

    private final List<BlockValue> list;

    private BlockListWrapper(List<BlockValue> list) {
        this.list = List.copyOf(list);
    }

    public static BlockListWrapper make(List<BlockValue> values) {
        if (values == null || values.isEmpty()) {
            return EMPTY;
        }

        List<BlockValue> result = new ArrayList<>(values.size());
        Set<EquivalenceKey> seen = new HashSet<>();
        for (BlockValue value : values) {
            if (value != null && seen.add(new EquivalenceKey(value.dimension(), value.blockPos(), value.state()))) {
                result.add(value);
            }
        }

        return result.isEmpty() ? EMPTY : new BlockListWrapper(result);
    }

    public int size() {
        return list.size();
    }

    public BlockValue get(int index) {
        return list.get(index);
    }

    public List<BlockValue> asList() {
        return list;
    }

    @Override
    public Iterator<BlockValue> iterator() {
        return list.iterator();
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

    /** The fields {@link #equivalent} compares. */
    private record EquivalenceKey(ResourceKey<Level> dimension, BlockPos pos, BlockState state) {
    }
}
