package com.moratan251.psitweaks.api.value;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.psi.api.internal.Vector3;

public class BlockValue extends Vector3 implements ContextualValue {
    private final BlockPos blockPos;
    private final ResourceKey<Level> dimension;
    private final BlockState state;
    private final ResourceLocation blockId;
    private final Set<ResourceLocation> tagIds;

    public BlockValue(ResourceKey<Level> dimension, BlockPos blockPos, BlockState state) {
        super(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        this.dimension = Objects.requireNonNull(dimension, "dimension");
        this.blockPos = Objects.requireNonNull(blockPos, "blockPos").immutable();
        this.state = Objects.requireNonNull(state, "state");
        this.blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        this.tagIds = state.getTags()
                .map(TagKey::location)
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(LinkedHashSet::new),
                        Set::copyOf
                ));
    }

    public static BlockValue snapshot(Level level, BlockPos blockPos) {
        Objects.requireNonNull(level, "level");
        BlockPos immutablePos = Objects.requireNonNull(blockPos, "blockPos").immutable();
        return new BlockValue(level.dimension(), immutablePos, level.getBlockState(immutablePos));
    }

    public BlockPos blockPos() {
        return blockPos;
    }

    public ResourceKey<Level> dimension() {
        return dimension;
    }

    public BlockState state() {
        return state;
    }

    public Block block() {
        return state.getBlock();
    }

    public ResourceLocation blockId() {
        return blockId;
    }

    public Set<ResourceLocation> tagIds() {
        return tagIds;
    }

    public boolean hasTag(ResourceLocation tagId) {
        return tagId != null && tagIds.contains(tagId);
    }

    public Vector3 positionVector() {
        return new Vector3(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    @Override
    public Vector3 copy() {
        return new Vector3(x, y, z);
    }

    @Override
    public String toString() {
        return "Block[id=\"" + blockId + "\", pos=" + blockPos.toShortString()
                + ", dimension=" + dimension.location() + ", tags=" + tagIds.size() + "]";
    }
}
