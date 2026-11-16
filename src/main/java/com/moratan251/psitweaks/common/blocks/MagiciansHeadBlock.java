package com.moratan251.psitweaks.common.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 床に置いた魔法師の頭。村人の頭は高さ 10 なので、当たり判定もほかの頭 (高さ 8) より高くする。
 */
public class MagiciansHeadBlock extends SkullBlock {
    public static final MapCodec<MagiciansHeadBlock> CODEC = simpleCodec(MagiciansHeadBlock::new);
    private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 10.0, 12.0);

    public MagiciansHeadBlock(BlockBehaviour.Properties properties) {
        super(PsitweaksSkullTypes.MAGICIAN, properties);
    }

    @Override
    public MapCodec<? extends SkullBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
