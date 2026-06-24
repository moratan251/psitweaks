package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.mojang.logging.LogUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import vazkii.psi.api.spell.SpellRuntimeException;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.common.spell.trick.block.PieceTrickBreakBlock;

public final class MassBlockBreakScheduler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long BUDGET_NS = 4_000_000L; // 4ms per tick shared across all tasks
    private static final int CHECK_INTERVAL = 32;

    private static final ArrayDeque<PendingBreak> ACTIVE_BREAKS = new ArrayDeque<>();
    private static final Map<UUID, Integer> QUEUED_BLOCK_COUNTS = new HashMap<>();
    private static final int MAX_QUEUED_BLOCKS_PER_PLAYER = 16384;

    private MassBlockBreakScheduler() {
    }

    public static void schedule(ServerPlayer player,
                                ServerLevel level,
                                Vector3 focalPoint,
                                double maxRadiusSq,
                                List<BlockPos> positions,
                                ItemStack effectiveTool,
                                Predicate<BlockState> filter) throws SpellRuntimeException {
        if (positions.isEmpty()) {
            return;
        }

        UUID uuid = player.getUUID();
        int current = QUEUED_BLOCK_COUNTS.getOrDefault(uuid, 0);
        if (current + positions.size() > MAX_QUEUED_BLOCKS_PER_PLAYER) {
            throw new SpellRuntimeException(MassBlockBreakHelper.ERROR_TOO_MANY_PENDING_BLOCKS);
        }

        QUEUED_BLOCK_COUNTS.put(uuid, current + positions.size());
        ACTIVE_BREAKS.addLast(new PendingBreak(player, level, focalPoint, maxRadiusSq, positions, effectiveTool, filter));
        LOGGER.debug("[MassBlockBreakScheduler] scheduled {} blocks for {} (queue size={})",
                positions.size(), player.getName().getString(), ACTIVE_BREAKS.size());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE_BREAKS.isEmpty()) {
            return;
        }

        long deadline = System.nanoTime() + BUDGET_NS;
        while (!ACTIVE_BREAKS.isEmpty() && System.nanoTime() < deadline) {
            PendingBreak task = ACTIVE_BREAKS.peekFirst();

            if (!task.isValid()) {
                LOGGER.debug("[MassBlockBreakScheduler] task became invalid for {}, flushing remaining drops", task.playerName);
                flushToWorld(task);
                releaseQueuedBlocks(task);
                ACTIVE_BREAKS.pollFirst();
                continue;
            }

            boolean finished = switch (task.state) {
                case BREAKING -> tickBreaking(task, deadline);
                case GIVING_DROPS -> tickGivingDrops(task, deadline);
                case DROPPING -> tickDropping(task, deadline);
            };

            if (finished) {
                finish(task);
                releaseQueuedBlocks(task);
                ACTIVE_BREAKS.pollFirst();
            } else {
                // Round-robin: move to the tail so other tasks get a turn next tick.
                ACTIVE_BREAKS.pollFirst();
                ACTIVE_BREAKS.addLast(task);
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        if (ACTIVE_BREAKS.isEmpty()) {
            return;
        }
        LOGGER.info("[MassBlockBreakScheduler] flushing {} active breaks on server stop", ACTIVE_BREAKS.size());
        for (PendingBreak task : new ArrayList<>(ACTIVE_BREAKS)) {
            flushToWorld(task);
        }
        ACTIVE_BREAKS.clear();
        QUEUED_BLOCK_COUNTS.clear();
    }

    private static boolean tickBreaking(PendingBreak task, long deadline) {
        ServerPlayer player = (ServerPlayer) task.level.getPlayerByUUID(task.playerUuid);
        if (player == null) {
            return true;
        }

        ItemStack original = player.getMainHandItem();
        long tickStart = System.nanoTime();

        Boolean previousHarvestCheck = PieceTrickBreakBlock.doingHarvestCheck.get();
        MassBlockBreakHelper.runWithBreak(task.playerUuid, task.loot, () -> {
            PieceTrickBreakBlock.doingHarvestCheck.set(true);
            player.setItemInHand(InteractionHand.MAIN_HAND, task.effectiveTool);
            try {
                int checked = 0;
                while (!task.positions.isEmpty() && System.nanoTime() < deadline) {
                    BlockPos pos = task.positions.poll();
                    BlockState state = task.level.getBlockState(pos);

                    if (!task.level.hasChunkAt(pos)) {
                        continue;
                    }
                    if (state.isAir() || state.getDestroySpeed(task.level, pos) == -1.0F || !task.filter.test(state)) {
                        continue;
                    }
                    if (MassBlockBreakHelper.isUnbreakable(state)) {
                        continue;
                    }
                    if (!task.level.mayInteract(player, pos)) {
                        continue;
                    }
                    if (!MassBlockBreakHelper.isInRadius(task.focalPoint, task.maxRadiusSq, pos)) {
                        continue;
                    }

                    if (player.gameMode.destroyBlock(pos)) {
                        task.brokenPositions.add(pos);
                        task.brokenStates.add(state);
                    }

                    checked++;
                    if ((checked & (CHECK_INTERVAL - 1)) == 0) {
                        if (System.nanoTime() >= deadline) {
                            break;
                        }
                    }
                }
            } finally {
                player.setItemInHand(InteractionHand.MAIN_HAND, original);
                PieceTrickBreakBlock.doingHarvestCheck.set(previousHarvestCheck);
            }
        });

        task.totalBreakNs += System.nanoTime() - tickStart;

        if (task.positions.isEmpty()) {
            task.state = State.GIVING_DROPS;
        }
        return false;
    }

    private static boolean tickGivingDrops(PendingBreak task, long deadline) {
        ServerPlayer player = (ServerPlayer) task.level.getPlayerByUUID(task.playerUuid);
        if (player == null) {
            return true;
        }

        List<ItemStack> drops = task.loot.drops();
        while (task.dropIndex < drops.size() && System.nanoTime() < deadline) {
            ItemStack stack = drops.get(task.dropIndex++);
            player.getInventory().add(stack);
            if (!stack.isEmpty()) {
                mergeInto(task.mergedOverflow, stack);
            }

            if ((task.dropIndex & (CHECK_INTERVAL - 1)) == 0) {
                if (System.nanoTime() >= deadline) {
                    return false;
                }
            }
        }

        if (task.dropIndex < drops.size()) {
            return false;
        }

        task.state = State.DROPPING;
        return false;
    }

    private static boolean tickDropping(PendingBreak task, long deadline) {
        ServerPlayer player = (ServerPlayer) task.level.getPlayerByUUID(task.playerUuid);
        if (player == null) {
            return true;
        }

        List<ItemStack> merged = task.mergedOverflow;
        while (task.mergedDropIndex < merged.size() && System.nanoTime() < deadline) {
            player.drop(merged.get(task.mergedDropIndex++), false);
        }

        if (task.mergedDropIndex < merged.size()) {
            return false;
        }

        if (task.loot.experience() > 0) {
            player.giveExperiencePoints(task.loot.experience());
        }

        task.mergedOverflowCount = merged.size();
        return true;
    }

    private static void flushToWorld(PendingBreak task) {
        if (task.level.isClientSide()) {
            return;
        }

        List<ItemStack> flush = new ArrayList<>();
        if (task.state == State.DROPPING) {
            // Only drop overflow that has not already been spawned by player.drop().
            flush.addAll(task.mergedOverflow.subList(task.mergedDropIndex, task.mergedOverflow.size()));
        } else {
            flush.addAll(task.mergedOverflow);
        }

        List<ItemStack> drops = task.loot.drops();
        for (int i = task.dropIndex; i < drops.size(); i++) {
            ItemStack stack = drops.get(i);
            if (!stack.isEmpty()) {
                mergeInto(flush, stack.copy());
            }
        }

        BlockPos dropPos = task.focalPoint.toBlockPos();
        for (ItemStack stack : flush) {
            Block.popResource(task.level, dropPos, stack);
        }

        if (task.loot.experience() > 0) {
            task.level.addFreshEntity(new ExperienceOrb(task.level,
                    dropPos.getX() + 0.5, dropPos.getY() + 0.5, dropPos.getZ() + 0.5,
                    task.loot.experience()));
        }
    }

    private static void releaseQueuedBlocks(PendingBreak task) {
        int remaining = QUEUED_BLOCK_COUNTS.getOrDefault(task.playerUuid, 0) - task.scheduledBlockCount;
        if (remaining <= 0) {
            QUEUED_BLOCK_COUNTS.remove(task.playerUuid);
        } else {
            QUEUED_BLOCK_COUNTS.put(task.playerUuid, remaining);
        }
    }

    private static void mergeInto(List<ItemStack> merged, ItemStack remaining) {
        if (remaining.isEmpty()) {
            return;
        }
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
                    return;
                }
            }
        }
        if (!remaining.isEmpty()) {
            merged.add(remaining);
        }
    }

    private static void finish(PendingBreak task) {
        ServerPlayer player = (ServerPlayer) task.level.getPlayerByUUID(task.playerUuid);
        if (player != null) {
            MassBlockBreakHelper.spawnRandomBlockParticles(task.level, task.brokenPositions, task.brokenStates, 10);
        }

        double breakMs = task.totalBreakNs / 1_000_000.0;
        double blocksPerMs = task.brokenPositions.isEmpty() ? 0.0
                : task.brokenPositions.size() / Math.max(breakMs, 0.001);
        LOGGER.info("[MassBlockBreakScheduler] finished for {}: blocks={}, breakTimeMs={}, blocksPerMs={}, overflowAfterMerge={}",
                task.playerName, task.brokenPositions.size(), String.format("%.3f", breakMs), String.format("%.2f", blocksPerMs), task.mergedOverflowCount);
    }

    private enum State {
        BREAKING,
        GIVING_DROPS,
        DROPPING
    }

    private static final class PendingBreak {
        final UUID playerUuid;
        final String playerName;
        final ServerLevel level;
        final Vector3 focalPoint;
        final double maxRadiusSq;
        final ArrayDeque<BlockPos> positions;
        final ItemStack effectiveTool;
        final Predicate<BlockState> filter;
        final MassBlockBreakHelper.MassBreakDrops loot = new MassBlockBreakHelper.MassBreakDrops();
        final List<BlockPos> brokenPositions = new ArrayList<>();
        final List<BlockState> brokenStates = new ArrayList<>();
        final List<ItemStack> mergedOverflow = new ArrayList<>();

        State state = State.BREAKING;
        int dropIndex = 0;
        int mergedDropIndex = 0;
        long totalBreakNs = 0;
        int mergedOverflowCount = 0;
        final int scheduledBlockCount;

        PendingBreak(Player player,
                     ServerLevel level,
                     Vector3 focalPoint,
                     double maxRadiusSq,
                     List<BlockPos> positions,
                     ItemStack effectiveTool,
                     Predicate<BlockState> filter) {
            this.playerUuid = player.getUUID();
            this.playerName = player.getName().getString();
            this.level = level;
            this.focalPoint = focalPoint;
            this.maxRadiusSq = maxRadiusSq;
            this.positions = new ArrayDeque<>(positions);
            this.effectiveTool = effectiveTool;
            this.filter = filter;
            this.scheduledBlockCount = positions.size();
        }

        boolean isValid() {
            ServerPlayer player = (ServerPlayer) level.getPlayerByUUID(playerUuid);
            return player != null && !player.isRemoved() && player.level() == level;
        }
    }
}
