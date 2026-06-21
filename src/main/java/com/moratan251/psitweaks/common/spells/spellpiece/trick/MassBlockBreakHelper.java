package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.MatrixOperations;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

public final class MassBlockBreakHelper {
    public static final String ERROR_INVALID_MATRIX = "psitweaks.spellerror.region_invalid_matrix";
    public static final String ERROR_DEGENERATE_EDGES = "psitweaks.spellerror.region_degenerate_edges";
    public static final String ERROR_REGION_TOO_LARGE = "psitweaks.spellerror.region_too_large";
    public static final int MAX_REGION_BLOCKS = 4096;

    private static final double EPSILON = 1e-12;

    private MassBlockBreakHelper() {
    }

    public static void breakBlocks(SpellContext context, Iterable<Vector3> positions, int maxBlocks) throws SpellRuntimeException {
        Level level = context.focalPoint.getCommandSenderWorld();
        if (!(level instanceof ServerLevel serverLevel) || !(context.caster instanceof ServerPlayer player)) {
            return;
        }

        List<BreakTarget> targets = collectBreakTargets(positions, maxBlocks);
        for (BreakTarget target : targets) {
            if (!context.isInRadius(target.vector())) {
                throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
            }
        }

        List<BlockState> brokenStates = new ArrayList<>();
        List<BlockPos> brokenPositions = new ArrayList<>();
        for (BreakTarget target : targets) {
            BlockState state = serverLevel.getBlockState(target.pos());
            if (breakBlock(serverLevel, player, target.pos())) {
                brokenStates.add(state);
                brokenPositions.add(target.pos());
            }
        }

        spawnRandomBlockParticles(serverLevel, brokenPositions, brokenStates, 10);
    }

    private static List<BreakTarget> collectBreakTargets(Iterable<Vector3> positions, int maxBlocks) throws SpellRuntimeException {
        if (positions == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }

        int limit = Math.min(Math.max(1, maxBlocks), MAX_REGION_BLOCKS);
        Set<BlockPos> seen = new LinkedHashSet<>();
        List<BreakTarget> targets = new ArrayList<>();
        for (Vector3 vector : positions) {
            if (vector == null) {
                throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
            }
            BlockPos pos = vector.toBlockPos();
            if (seen.add(pos)) {
                targets.add(new BreakTarget(pos, vector.copy()));
                if (targets.size() >= limit) {
                    break;
                }
            }
        }
        return targets;
    }

    public static List<BlockPos> collectRegionPositions(MatrixValue matrix) throws SpellRuntimeException {
        List<Target> targets = collectTargets(parseRegion(matrix));
        List<BlockPos> positions = new ArrayList<>(targets.size());
        for (Target target : targets) {
            positions.add(target.pos());
        }
        return List.copyOf(positions);
    }

    private static Region parseRegion(MatrixValue matrix) throws SpellRuntimeException {
        if ((matrix.rows() != 3 && matrix.rows() != 4) || matrix.cols() != 4) {
            throw new SpellRuntimeException(ERROR_INVALID_MATRIX);
        }

        Vector3 edge0 = new Vector3(matrix.get(0, 0), matrix.get(1, 0), matrix.get(2, 0));
        Vector3 edge1 = new Vector3(matrix.get(0, 1), matrix.get(1, 1), matrix.get(2, 1));
        Vector3 edge2 = new Vector3(matrix.get(0, 2), matrix.get(1, 2), matrix.get(2, 2));
        Vector3 start = new Vector3(matrix.get(0, 3), matrix.get(1, 3), matrix.get(2, 3));

        MatrixValue edgeMatrix = new MatrixValue(3, 3, new double[]{
                edge0.x, edge1.x, edge2.x,
                edge0.y, edge1.y, edge2.y,
                edge0.z, edge1.z, edge2.z
        });
        MatrixValue inverseMatrix;
        try {
            inverseMatrix = MatrixOperations.inverse(edgeMatrix);
        } catch (SpellRuntimeException e) {
            throw new SpellRuntimeException(ERROR_DEGENERATE_EDGES);
        }

        Vector3[] vertices = new Vector3[8];
        for (int i = 0; i < 8; i++) {
            double u = (i & 1) != 0 ? 1 : 0;
            double v = (i & 2) != 0 ? 1 : 0;
            double w = (i & 4) != 0 ? 1 : 0;
            vertices[i] = add(start, add(scale(edge0, u), add(scale(edge1, v), scale(edge2, w))));
        }

        return new Region(start, inverseMatrix, vertices);
    }

    private static List<Target> collectTargets(Region region) throws SpellRuntimeException {
        double minX = region.start().x;
        double minY = region.start().y;
        double minZ = region.start().z;
        double maxX = region.start().x;
        double maxY = region.start().y;
        double maxZ = region.start().z;
        for (Vector3 vertex : region.vertices()) {
            minX = Math.min(minX, vertex.x);
            minY = Math.min(minY, vertex.y);
            minZ = Math.min(minZ, vertex.z);
            maxX = Math.max(maxX, vertex.x);
            maxY = Math.max(maxY, vertex.y);
            maxZ = Math.max(maxZ, vertex.z);
        }

        int minBX = (int) Math.floor(minX);
        int minBY = (int) Math.floor(minY);
        int minBZ = (int) Math.floor(minZ);
        int maxBX = (int) Math.ceil(maxX);
        int maxBY = (int) Math.ceil(maxY);
        int maxBZ = (int) Math.ceil(maxZ);

        List<Target> targets = new ArrayList<>();
        MatrixValue inverseMatrix = region.inverseMatrix();
        for (int x = minBX; x <= maxBX; x++) {
            for (int y = minBY; y <= maxBY; y++) {
                for (int z = minBZ; z <= maxBZ; z++) {
                    double dx = x - region.start().x;
                    double dy = y - region.start().y;
                    double dz = z - region.start().z;
                    double u = inverseMatrix.get(0, 0) * dx + inverseMatrix.get(0, 1) * dy + inverseMatrix.get(0, 2) * dz;
                    double v = inverseMatrix.get(1, 0) * dx + inverseMatrix.get(1, 1) * dy + inverseMatrix.get(1, 2) * dz;
                    double w = inverseMatrix.get(2, 0) * dx + inverseMatrix.get(2, 1) * dy + inverseMatrix.get(2, 2) * dz;
                    if (u >= -EPSILON && u <= 1 + EPSILON && v >= -EPSILON && v <= 1 + EPSILON && w >= -EPSILON && w <= 1 + EPSILON) {
                        targets.add(new Target(new BlockPos(x, y, z), u, v, w));
                        if (targets.size() > MAX_REGION_BLOCKS) {
                            throw new SpellRuntimeException(ERROR_REGION_TOO_LARGE);
                        }
                    }
                }
            }
        }

        targets.sort(Comparator.comparingDouble(Target::u)
                .thenComparingDouble(Target::v)
                .thenComparingDouble(Target::w));
        return targets;
    }

    private static void spawnRandomBlockParticles(ServerLevel level, List<BlockPos> positions, List<BlockState> states, int count) {
        int total = positions.size();
        if (total == 0) {
            return;
        }
        int particleCount = Math.min(count, total);
        List<Integer> indices = new ArrayList<>(total);
        for (int i = 0; i < total; i++) {
            indices.add(i);
        }
        Collections.shuffle(indices, new Random(level.random.nextLong()));
        for (int i = 0; i < particleCount; i++) {
            int idx = indices.get(i);
            spawnParticleAt(level, positions.get(idx), states.get(idx));
        }
    }

    private static boolean breakBlock(ServerLevel level, ServerPlayer player, BlockPos pos) {
        if (!level.hasChunkAt(pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getDestroySpeed(level, pos) == -1.0F) {
            return false;
        }
        if (!level.mayInteract(player, pos)) {
            return false;
        }
        if (isUnbreakable(state)) {
            return false;
        }

        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, state, player);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
            return false;
        }

        ItemStack tool = PsiAPI.getPlayerCAD(player);
        List<ItemStack> drops = Block.getDrops(state, level, pos, level.getBlockEntity(pos), player, tool);
        for (ItemStack drop : drops) {
            ItemEntity item = new ItemEntity(level, player.getX(), player.getY() + 0.5, player.getZ(), drop);
            item.setPickUpDelay(10);
            level.addFreshEntity(item);
        }

        level.removeBlock(pos, false);
        return true;
    }

    private static boolean isUnbreakable(BlockState state) {
        return state.is(Blocks.BEDROCK)
                || state.is(Blocks.COMMAND_BLOCK)
                || state.is(Blocks.CHAIN_COMMAND_BLOCK)
                || state.is(Blocks.REPEATING_COMMAND_BLOCK)
                || state.is(Blocks.STRUCTURE_BLOCK)
                || state.is(Blocks.JIGSAW)
                || state.is(Blocks.END_PORTAL_FRAME)
                || state.is(Blocks.BARRIER)
                || state.is(Blocks.SPAWNER);
    }

    private static void spawnParticleAt(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.isAir()) {
            return;
        }
        if (!level.hasChunkAt(pos)) {
            return;
        }
        level.getServer().getPlayerList().broadcastAll(
                new ClientboundLevelEventPacket(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state), false)
        );
    }

    private record BreakTarget(BlockPos pos, Vector3 vector) {
    }

    private record Region(Vector3 start, MatrixValue inverseMatrix, Vector3[] vertices) {
    }

    private record Target(BlockPos pos, double u, double v, double w) {
    }

    private static Vector3 scale(Vector3 vector, double scalar) {
        return new Vector3(vector.x * scalar, vector.y * scalar, vector.z * scalar);
    }

    private static Vector3 add(Vector3 a, Vector3 b) {
        return new Vector3(a.x + b.x, a.y + b.y, a.z + b.z);
    }
}
