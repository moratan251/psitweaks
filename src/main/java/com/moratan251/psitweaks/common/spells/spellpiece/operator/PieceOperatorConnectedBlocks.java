package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.spellpiece.trick.MassBlockBreakHelper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellHelpers;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceOperator;

/**
 * 演算子: 連結ブロック。
 * 位置のブロックから、面・辺・角で隣り合う同じブロックをたどり、その座標を位置に近いものから順に Vector List で返す。
 * 状態(作物の成長段階や原木の向きなど)は区別しない。射程外の座標はたどらず、最大数(上限4096)で打ち切る。
 * 位置が空気なら空のリスト。
 */
public class PieceOperatorConnectedBlocks extends PieceOperator {
    /** The most positions returned, the most Trick: Mass Block Break and Trick: Mass Harvest take. */
    public static final int MAX_BLOCKS = MassBlockBreakHelper.MAX_REGION_BLOCKS;

    private SpellParam<Vector3> position;
    private SpellParam<Number> maxBlocks;

    public PieceOperatorConnectedBlocks(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(maxBlocks = new ParamNumber(SpellParam.GENERIC_NAME_MAX, SpellParam.RED, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = SpellHelpers.getVector3(this, context, position, true, false);
        double maxVal = getNotNullParamValue(context, maxBlocks).doubleValue();
        if (maxVal < 1) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }
        return VectorListWrapper.make(collect(context, positionVal.toBlockPos(), (int) Math.min(maxVal, MAX_BLOCKS)));
    }

    /** A breadth-first walk from {@code start}, so the nearest blocks come first and a limit cuts off the farthest. */
    static List<Vector3> collect(SpellContext context, BlockPos start, int limit) {
        List<Vector3> result = new ArrayList<>();
        Level level = context.focalPoint.level();
        if (!isWalkable(context, level, start)) {
            return result;
        }
        BlockState startState = level.getBlockState(start);
        if (startState.isAir()) {
            return result;
        }
        Block block = startState.getBlock();
        // Packed positions: the walk looks at up to 26 neighbors of each of 4096 blocks within one tick.
        LongOpenHashSet seen = new LongOpenHashSet();
        LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
        seen.add(start.asLong());
        queue.enqueue(start.asLong());
        BlockPos.MutableBlockPos next = new BlockPos.MutableBlockPos();
        while (!queue.isEmpty() && result.size() < limit) {
            long pos = queue.dequeueLong();
            int x = BlockPos.getX(pos);
            int y = BlockPos.getY(pos);
            int z = BlockPos.getZ(pos);
            result.add(new Vector3(x, y, z));
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        next.set(x + dx, y + dy, z + dz);
                        if (seen.add(next.asLong()) && isWalkable(context, level, next) && level.getBlockState(next).is(block)) {
                            queue.enqueue(next.asLong());
                        }
                    }
                }
            }
        }
        return result;
    }

    private static boolean isWalkable(SpellContext context, Level level, BlockPos pos) {
        // The tricks that take the list check the block corners against the radius, so check the same points here.
        return context.isInRadius(pos.getX(), pos.getY(), pos.getZ()) && !level.isOutsideBuildHeight(pos) && level.isLoaded(pos);
    }

    @Override
    public Class<?> getEvaluationType() {
        return VectorListWrapper.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.vector_list");
    }
}
