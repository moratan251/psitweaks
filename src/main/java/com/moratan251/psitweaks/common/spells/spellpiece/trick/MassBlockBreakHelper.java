package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.MatrixOperations;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.common.core.handler.ConfigHandler;
import vazkii.psi.common.spell.trick.block.PieceTrickBreakBlock;
import org.slf4j.Logger;

public final class MassBlockBreakHelper {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String ERROR_INVALID_MATRIX = "psitweaks.spellerror.region_invalid_matrix";
    public static final String ERROR_DEGENERATE_EDGES = "psitweaks.spellerror.region_degenerate_edges";
    public static final String ERROR_REGION_TOO_LARGE = "psitweaks.spellerror.region_too_large";
    public static final int MAX_REGION_BLOCKS = 4096;
    public static final int MAX_SCAN_CANDIDATES = 65536;

    private static final int MAX_AXIS_SCAN = MAX_SCAN_CANDIDATES;
    private static final double EPSILON = 1e-12;

    private static final ThreadLocal<UUID> ACTIVE_BREAKER = new ThreadLocal<>();
    private static final ThreadLocal<MassBreakDrops> ACTIVE_DROPS = new ThreadLocal<>();

    private MassBlockBreakHelper() {
    }

    public static UUID getActiveBreaker() {
        return ACTIVE_BREAKER.get();
    }

    public static MassBreakDrops getActiveDrops() {
        return ACTIVE_DROPS.get();
    }

    public static final class MassBreakDrops {
        private final List<ItemStack> drops = new ArrayList<>();
        private int experience;

        public void addDrop(ItemStack stack) {
            if (stack != null && !stack.isEmpty()) {
                drops.add(stack);
            }
        }

        public void addExperience(int amount) {
            experience += amount;
        }

        public List<ItemStack> drops() {
            return drops;
        }

        public int experience() {
            return experience;
        }
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

        ItemStack tool = context.getHarvestTool().copy();
        if (tool.isEmpty()) {
            tool = PsiAPI.getPlayerCAD(player);
        }
        final ItemStack effectiveTool = tool;
        Predicate<BlockState> filter = state -> effectiveTool.isCorrectToolForDrops(state)
                || PieceTrickBreakBlock.canHarvest(ConfigHandler.COMMON.cadHarvestLevel.get(), state);

        List<BlockState> brokenStates = new ArrayList<>();
        List<BlockPos> brokenPositions = new ArrayList<>();

        ItemStack original = player.getMainHandItem();
        boolean previousHarvestCheck = PieceTrickBreakBlock.doingHarvestCheck.get();
        UUID previousBreaker = ACTIVE_BREAKER.get();
        MassBreakDrops previousDrops = ACTIVE_DROPS.get();
        MassBreakDrops loot = new MassBreakDrops();
        LOGGER.debug("[MassBlockBreakHelper] mass break start for {}", player.getName().getString());
        try {
            ACTIVE_BREAKER.set(player.getUUID());
            ACTIVE_DROPS.set(loot);
            PieceTrickBreakBlock.doingHarvestCheck.set(true);
            player.setItemInHand(InteractionHand.MAIN_HAND, effectiveTool);

            for (BreakTarget target : targets) {
                BlockPos pos = target.pos();
                BlockState state = serverLevel.getBlockState(pos);

                if (!serverLevel.hasChunkAt(pos)) {
                    continue;
                }
                if (state.isAir() || state.getDestroySpeed(serverLevel, pos) == -1.0F || !filter.test(state)) {
                    continue;
                }
                if (isUnbreakable(state)) {
                    continue;
                }
                if (!serverLevel.mayInteract(player, pos)) {
                    continue;
                }

                boolean destroyed = player.gameMode.destroyBlock(pos);
                if (destroyed) {
                    brokenStates.add(state);
                    brokenPositions.add(pos);
                }
            }
        } finally {
            PieceTrickBreakBlock.doingHarvestCheck.set(previousHarvestCheck);
            ACTIVE_BREAKER.set(previousBreaker);
            ACTIVE_DROPS.set(previousDrops);
            player.setItemInHand(InteractionHand.MAIN_HAND, original);
        }

        processAndGiveDrops(player, loot);
        LOGGER.debug("[MassBlockBreakHelper] mass break end: broken={}, collectedDrops={}, experience={}",
                brokenPositions.size(), loot.drops().size(), loot.experience());
        spawnRandomBlockParticles(serverLevel, brokenPositions, brokenStates, 10);
    }

    private static void processAndGiveDrops(ServerPlayer player, MassBreakDrops loot) {
        if (loot == null || loot.drops().isEmpty() && loot.experience() <= 0) {
            return;
        }

        List<ItemStack> overflow = new ArrayList<>();
        for (ItemStack stack : loot.drops()) {
            player.getInventory().add(stack);
            if (!stack.isEmpty()) {
                overflow.add(stack);
            }
        }

        List<ItemStack> merged = mergeStacks(overflow);
        for (ItemStack stack : merged) {
            player.drop(stack, false);
        }

        if (loot.experience() > 0) {
            player.giveExperiencePoints(loot.experience());
        }

        LOGGER.debug("[MassBlockBreakHelper] gave drops: overflowBeforeMerge={}, overflowAfterMerge={}, experience={}",
                overflow.size(), merged.size(), loot.experience());
    }

    private static List<ItemStack> mergeStacks(List<ItemStack> stacks) {
        List<ItemStack> merged = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }

            ItemStack remaining = stack.copy();
            for (ItemStack target : merged) {
                if (ItemStack.isSameItemSameComponents(target, remaining)) {
                    int space = target.getMaxStackSize() - target.getCount();
                    int transfer = Math.min(remaining.getCount(), space);
                    if (transfer <= 0) {
                        continue;
                    }
                    target.grow(transfer);
                    remaining.shrink(transfer);
                    if (remaining.isEmpty()) {
                        break;
                    }
                }
            }

            if (!remaining.isEmpty()) {
                merged.add(remaining);
            }
        }
        return merged;
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
            validateFiniteVector(vertices[i]);
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

        ScanBounds bounds = createScanBounds(minX, minY, minZ, maxX, maxY, maxZ);

        List<Target> targets = new ArrayList<>();
        MatrixValue inverseMatrix = region.inverseMatrix();
        for (long x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (long y = bounds.minY(); y <= bounds.maxY(); y++) {
                for (long z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                    double dx = x - region.start().x;
                    double dy = y - region.start().y;
                    double dz = z - region.start().z;
                    double u = inverseMatrix.get(0, 0) * dx + inverseMatrix.get(0, 1) * dy + inverseMatrix.get(0, 2) * dz;
                    double v = inverseMatrix.get(1, 0) * dx + inverseMatrix.get(1, 1) * dy + inverseMatrix.get(1, 2) * dz;
                    double w = inverseMatrix.get(2, 0) * dx + inverseMatrix.get(2, 1) * dy + inverseMatrix.get(2, 2) * dz;
                    if (u >= -EPSILON && u <= 1 + EPSILON && v >= -EPSILON && v <= 1 + EPSILON && w >= -EPSILON && w <= 1 + EPSILON) {
                        targets.add(new Target(new BlockPos((int) x, (int) y, (int) z), u, v, w));
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

    private static ScanBounds createScanBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) throws SpellRuntimeException {
        int minBX = floorToBlock(minX);
        int minBY = floorToBlock(minY);
        int minBZ = floorToBlock(minZ);
        int maxBX = ceilToBlock(maxX);
        int maxBY = ceilToBlock(maxY);
        int maxBZ = ceilToBlock(maxZ);

        long sizeX = (long) maxBX - minBX + 1;
        long sizeY = (long) maxBY - minBY + 1;
        long sizeZ = (long) maxBZ - minBZ + 1;
        if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0
                || sizeX > MAX_AXIS_SCAN || sizeY > MAX_AXIS_SCAN || sizeZ > MAX_AXIS_SCAN) {
            throw new SpellRuntimeException(ERROR_REGION_TOO_LARGE);
        }

        long sizeXY = checkedScanProduct(sizeX, sizeY);
        checkedScanProduct(sizeXY, sizeZ);
        return new ScanBounds(minBX, minBY, minBZ, maxBX, maxBY, maxBZ);
    }

    private static long checkedScanProduct(long a, long b) throws SpellRuntimeException {
        if (a > MAX_SCAN_CANDIDATES / b) {
            throw new SpellRuntimeException(ERROR_REGION_TOO_LARGE);
        }
        return a * b;
    }

    private static int floorToBlock(double value) throws SpellRuntimeException {
        return blockCoordinate(Math.floor(value));
    }

    private static int ceilToBlock(double value) throws SpellRuntimeException {
        return blockCoordinate(Math.ceil(value));
    }

    private static int blockCoordinate(double value) throws SpellRuntimeException {
        if (!Double.isFinite(value) || value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new SpellRuntimeException(ERROR_REGION_TOO_LARGE);
        }
        return (int) value;
    }

    private static void validateFiniteVector(Vector3 vector) throws SpellRuntimeException {
        if (!Double.isFinite(vector.x) || !Double.isFinite(vector.y) || !Double.isFinite(vector.z)) {
            throw new SpellRuntimeException(ERROR_REGION_TOO_LARGE);
        }
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

    private record ScanBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
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
