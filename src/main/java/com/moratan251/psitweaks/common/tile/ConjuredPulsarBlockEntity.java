package com.moratan251.psitweaks.common.tile;

import com.moratan251.psitweaks.common.registries.PsitweaksBlockEntityTypes;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.psi.api.internal.PsiRenderHelper;
import vazkii.psi.common.client.PsiClientRuntime;
import vazkii.psi.api.cad.CADComponentLookup;
import vazkii.psi.common.block.BlockConjured;

public class ConjuredPulsarBlockEntity extends BlockEntity {
    private static final String TAG_COLORIZER = "colorizer";

    private ItemStack colorizer = ItemStack.EMPTY;

    public ConjuredPulsarBlockEntity(BlockPos pos, BlockState state) {
        super(PsitweaksBlockEntityTypes.CONJURED_PULSAR.get(), pos, state);
    }

    public ItemStack getColorizer() {
        return colorizer;
    }

    public void setColorizer(ItemStack colorizer) {
        this.colorizer = colorizer.copy();
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public void doParticles(RandomSource random) {
        if (level == null) {
            return;
        }

        int color = CADComponentLookup.color(level.registryAccess(), colorizer);
        float red = PsiRenderHelper.r(color) / 255.0F;
        float green = PsiRenderHelper.g(color) / 255.0F;
        float blue = PsiRenderHelper.b(color) / 255.0F;
        BlockState state = getBlockState();

        if (state.getValue(BlockConjured.LIGHT)) {
            if (random.nextBoolean()) {
                makeLightParticle(random, red, green, blue);
            }
            return;
        }
        if (!state.getValue(BlockConjured.SOLID)) {
            return;
        }

        boolean[] edges = new boolean[12];
        Arrays.fill(edges, true);
        if (state.getValue(BlockConjured.BLOCK_DOWN)) {
            removeEdges(edges, 0, 1, 2, 3);
        }
        if (state.getValue(BlockConjured.BLOCK_UP)) {
            removeEdges(edges, 4, 5, 6, 7);
        }
        if (state.getValue(BlockConjured.BLOCK_NORTH)) {
            removeEdges(edges, 3, 7, 8, 11);
        }
        if (state.getValue(BlockConjured.BLOCK_SOUTH)) {
            removeEdges(edges, 1, 5, 9, 10);
        }
        if (state.getValue(BlockConjured.BLOCK_EAST)) {
            removeEdges(edges, 2, 6, 10, 11);
        }
        if (state.getValue(BlockConjured.BLOCK_WEST)) {
            removeEdges(edges, 0, 4, 8, 9);
        }

        double x = worldPosition.getX();
        double y = worldPosition.getY();
        double z = worldPosition.getZ();
        makeSparkle(edges[0], red, green, blue, x, y, z, 0, 0, 1);
        makeSparkle(edges[1], red, green, blue, x, y, z + 1, 1, 0, 0);
        makeSparkle(edges[2], red, green, blue, x + 1, y, z, 0, 0, 1);
        makeSparkle(edges[3], red, green, blue, x, y, z, 1, 0, 0);
        makeSparkle(edges[4], red, green, blue, x, y + 1, z, 0, 0, 1);
        makeSparkle(edges[5], red, green, blue, x, y + 1, z + 1, 1, 0, 0);
        makeSparkle(edges[6], red, green, blue, x + 1, y + 1, z, 0, 0, 1);
        makeSparkle(edges[7], red, green, blue, x, y + 1, z, 1, 0, 0);
        makeSparkle(edges[8], red, green, blue, x, y, z, 0, 1, 0);
        makeSparkle(edges[9], red, green, blue, x, y, z + 1, 0, 1, 0);
        makeSparkle(edges[10], red, green, blue, x + 1, y, z + 1, 0, 1, 0);
        makeSparkle(edges[11], red, green, blue, x + 1, y, z, 0, 1, 0);
    }

    private void makeLightParticle(RandomSource random, float red, float green, float blue) {
        double width = 0.15;
        double height = 0.05;
        double x = worldPosition.getX() + 0.5 + (random.nextDouble() - 0.5) * width;
        double y = worldPosition.getY() + 0.25 + (random.nextDouble() - 0.5) * height;
        double z = worldPosition.getZ() + 0.5 + (random.nextDouble() - 0.5) * width;
        float size = 0.2F + random.nextFloat() * 0.1F;
        float motion = 0.01F + random.nextFloat() * 0.015F;
        PsiClientRuntime.wisp(level, x, y, z, red, green, blue, size, 0, motion, 0, 1.0F);
    }

    private void makeSparkle(boolean enabled, float red, float green, float blue,
                                    double x, double y, double z, double xLength, double yLength, double zLength) {
        if (!enabled) {
            return;
        }
        float scale = 0.1F;
        float xMotion = (float) (xLength * scale);
        float yMotion = (float) (yLength * scale);
        float zMotion = (float) (zLength * scale);
        PsiClientRuntime.sparkle(level, x, y, z, red, green, blue,
                xMotion, yMotion, zMotion, 2.75F, 15);
    }

    private static void removeEdges(boolean[] edges, int... indices) {
        for (int index : indices) {
            edges[index] = false;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (!colorizer.isEmpty()) {
            tag.put(TAG_COLORIZER, colorizer.save(provider, new CompoundTag()));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        readColorizer(tag, provider);
    }

    private void readColorizer(CompoundTag tag, HolderLookup.Provider provider) {
        colorizer = tag.contains(TAG_COLORIZER)
                ? ItemStack.parseOptional(provider, tag.getCompound(TAG_COLORIZER))
                : ItemStack.EMPTY;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, provider);
        return tag;
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider provider) {
        readColorizer(packet.getTag(), provider);
    }
}
