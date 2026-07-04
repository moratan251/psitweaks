package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.spells.spellpiece.trick.MassBlockBreakHelper.DropEntry;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.MassBlockBreakHelper.StackAccumulator;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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
        PieceTrickBreakBlock.doingHarvestCheck.set(true);
        try {
            int checked = 0;
            while (!task.positions.isEmpty() && System.nanoTime() < deadline) {
                BlockPos pos = task.positions.poll();
                if (task.level.isOutsideBuildHeight(pos) || !task.level.hasChunkAt(pos)) {
                    continue;
                }

                BlockState state = task.level.getBlockState(pos);
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

                player.setItemInHand(InteractionHand.MAIN_HAND, createBreakTool(task.effectiveTool));
                if (MassBlockBreakHelper.withActiveBreak(task.playerUuid, task.level, pos, task.loot, () -> player.gameMode.destroyBlock(pos))) {
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

        task.totalBreakNs += System.nanoTime() - tickStart;

        if (task.positions.isEmpty()) {
            task.state = State.GIVING_DROPS;
        }
        return false;
    }

    private static ItemStack createBreakTool(ItemStack effectiveTool) {
        ItemStack tool = effectiveTool.copy();
        if (tool.isDamageableItem()) {
            tool.set(DataComponents.UNBREAKABLE, new Unbreakable(false));
        }
        return tool;
    }

    private static boolean tickGivingDrops(PendingBreak task, long deadline) {
        ServerPlayer player = (ServerPlayer) task.level.getPlayerByUUID(task.playerUuid);
        if (player == null) {
            return true;
        }

        List<DropEntry> drops = dropEntries(task);
        int checked = 0;
        while (task.dropIndex < drops.size() && System.nanoTime() < deadline) {
            DropEntry entry = drops.get(task.dropIndex);
            long remaining = entry.count() - task.dropEntryOffset;
            if (remaining <= 0) {
                task.dropIndex++;
                task.dropEntryOffset = 0;
                continue;
            }

            int stackCount = (int) Math.min(remaining, entry.maxStackSize());
            ItemStack stack = entry.copyStack(stackCount);
            task.dropEntryOffset += stackCount;

            player.getInventory().add(stack);
            if (!stack.isEmpty()) {
                task.overflow.add(stack);
            }
            if (task.dropEntryOffset >= entry.count()) {
                task.dropIndex++;
                task.dropEntryOffset = 0;
            }

            checked++;
            if ((checked & (CHECK_INTERVAL - 1)) == 0 && System.nanoTime() >= deadline) {
                return false;
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

        List<DropEntry> overflow = overflowEntries(task);
        int checked = 0;
        while (task.overflowDropIndex < overflow.size() && System.nanoTime() < deadline) {
            DropEntry entry = overflow.get(task.overflowDropIndex);
            long remaining = entry.count() - task.overflowEntryOffset;
            if (remaining <= 0) {
                task.overflowDropIndex++;
                task.overflowEntryOffset = 0;
                continue;
            }

            int stackCount = (int) Math.min(remaining, entry.maxStackSize());
            player.drop(entry.copyStack(stackCount), false);
            task.overflowEntryOffset += stackCount;
            task.overflowStackCount++;
            if (task.overflowEntryOffset >= entry.count()) {
                task.overflowDropIndex++;
                task.overflowEntryOffset = 0;
            }

            checked++;
            if ((checked & (CHECK_INTERVAL - 1)) == 0 && System.nanoTime() >= deadline) {
                return false;
            }
        }

        if (task.overflowDropIndex < overflow.size()) {
            return false;
        }

        awardExperience(task.level, player.position(), task.loot.experience());
        return true;
    }

    private static void flushToWorld(PendingBreak task) {
        if (task.level.isClientSide()) {
            return;
        }

        StackAccumulator flush = new StackAccumulator();
        if (task.state == State.DROPPING) {
            addRemaining(flush, overflowEntries(task), task.overflowDropIndex, task.overflowEntryOffset);
        } else {
            addRemaining(flush, task.overflow.entries(), 0, 0);
            addRemaining(flush, dropEntries(task), task.dropIndex, task.dropEntryOffset);
        }

        BlockPos dropPos = task.focalPoint.toBlockPos();
        dropAt(task.level, dropPos, flush.entries());
        awardExperience(task.level, Vec3.atCenterOf(dropPos), task.loot.experience());
    }

    private static void awardExperience(ServerLevel level, Vec3 position, int experience) {
        if (experience > 0) {
            ExperienceOrb.award(level, position, experience);
        }
    }

    private static List<DropEntry> dropEntries(PendingBreak task) {
        if (task.dropEntries == null) {
            task.dropEntries = task.loot.dropEntries();
        }
        return task.dropEntries;
    }

    private static List<DropEntry> overflowEntries(PendingBreak task) {
        if (task.overflowEntries == null) {
            task.overflowEntries = task.overflow.entries();
        }
        return task.overflowEntries;
    }

    private static void addRemaining(StackAccumulator target, List<DropEntry> entries, int startIndex, long firstOffset) {
        for (int i = startIndex; i < entries.size(); i++) {
            DropEntry entry = entries.get(i);
            long count = entry.count();
            if (i == startIndex) {
                count -= firstOffset;
            }
            if (count > 0) {
                target.add(entry.template(), count);
            }
        }
    }

    private static void dropAt(ServerLevel level, BlockPos pos, List<DropEntry> entries) {
        for (DropEntry entry : entries) {
            long remaining = entry.count();
            while (remaining > 0) {
                int stackCount = (int) Math.min(remaining, entry.maxStackSize());
                Block.popResource(level, pos, entry.copyStack(stackCount));
                remaining -= stackCount;
            }
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


    private static void finish(PendingBreak task) {
        ServerPlayer player = (ServerPlayer) task.level.getPlayerByUUID(task.playerUuid);
        if (player != null) {
            MassBlockBreakHelper.spawnRandomBlockParticles(task.level, task.brokenPositions, task.brokenStates, 10);
        }

        double breakMs = task.totalBreakNs / 1_000_000.0;
        double blocksPerMs = task.brokenPositions.isEmpty() ? 0.0
                : task.brokenPositions.size() / Math.max(breakMs, 0.001);
        LOGGER.info("[MassBlockBreakScheduler] finished for {}: blocks={}, breakTimeMs={}, blocksPerMs={}, overflowStacks={}",
                task.playerName, task.brokenPositions.size(), String.format("%.3f", breakMs), String.format("%.2f", blocksPerMs), task.overflowStackCount);
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
        final StackAccumulator overflow = new StackAccumulator();

        State state = State.BREAKING;
        List<DropEntry> dropEntries;
        int dropIndex = 0;
        long dropEntryOffset = 0;
        List<DropEntry> overflowEntries;
        int overflowDropIndex = 0;
        long overflowEntryOffset = 0;
        long totalBreakNs = 0;
        int overflowStackCount = 0;
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
