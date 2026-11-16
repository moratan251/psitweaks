package com.moratan251.psitweaks.common.blocks;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 壁に付けた魔法師の頭。当たり判定はバニラの壁の頭を高さ 10 に伸ばしたもの。
 */
public class MagiciansWallHeadBlock extends WallSkullBlock {
    public static final MapCodec<MagiciansWallHeadBlock> CODEC = simpleCodec(MagiciansWallHeadBlock::new);
    private static final Map<Direction, VoxelShape> SHAPES = Maps.newEnumMap(Map.of(
            Direction.NORTH, Block.box(4.0, 4.0, 8.0, 12.0, 14.0, 16.0),
            Direction.SOUTH, Block.box(4.0, 4.0, 0.0, 12.0, 14.0, 8.0),
            Direction.EAST, Block.box(0.0, 4.0, 4.0, 8.0, 14.0, 12.0),
            Direction.WEST, Block.box(8.0, 4.0, 4.0, 16.0, 14.0, 12.0)
    ));

    public MagiciansWallHeadBlock(BlockBehaviour.Properties properties) {
        super(PsitweaksSkullTypes.MAGICIAN, properties);
    }

    @Override
    public MapCodec<? extends WallSkullBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }
}
