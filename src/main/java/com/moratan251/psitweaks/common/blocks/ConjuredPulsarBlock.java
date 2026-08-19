package com.moratan251.psitweaks.common.blocks;

import com.moratan251.psitweaks.common.tile.ConjuredPulsarBlockEntity;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.psi.common.Psi;
import vazkii.psi.common.block.BlockConjured;

public class ConjuredPulsarBlock extends BlockConjured {
    public ConjuredPulsarBlock(BlockBehaviour.Properties properties, boolean solid, boolean light) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(SOLID, solid)
                .setValue(LIGHT, light));
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 15;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ConjuredPulsarBlockEntity pulsar) {
            pulsar.doParticles(random);
        }
    }

    @Nullable
    @Override
    public Integer getBeaconColorMultiplier(BlockState state, LevelReader level, BlockPos pos, BlockPos beaconPos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ConjuredPulsarBlockEntity pulsar) {
            return Psi.proxy.getColorForColorizer(pulsar.getColorizer());
        }
        return null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConjuredPulsarBlockEntity(pos, state);
    }
}
