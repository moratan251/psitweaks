package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * Harvests the crops of Trick: Mass Harvest over the following server ticks, in the time per tick it shares with
 * Trick: Mass Block Break ({@link MassWorkBudget}), so that a large field does not stall the server. The caster gets
 * the whole harvest after the last crop. If the caster dies, leaves or changes dimension first (or, for an autonomous
 * caster, its machine is removed or unloaded), the crops not reached stay as they are and the harvest the caster has
 * not got drops at the focal point.
 */
public final class MassHarvestScheduler {
    public static final String ERROR_TOO_MANY_PENDING = "psitweaks.spellerror.mass_harvest_too_many_pending";
    /** The most positions one player may have waiting, as for Trick: Mass Block Break. */
    public static final int MAX_QUEUED_PER_PLAYER = 16384;

    private static final ArrayDeque<PendingHarvest> ACTIVE = new ArrayDeque<>();
    private static final Map<UUID, Integer> QUEUED = new HashMap<>();

    private MassHarvestScheduler() {
    }

    /** Queues the positions for {@code harvest}; {@code dropPos} is where the harvest drops if the caster is gone. */
    public static void schedule(HarvestHelper.Harvest harvest, List<BlockPos> positions, BlockPos dropPos)
            throws SpellRuntimeException {
        if (positions.isEmpty()) {
            return;
        }
        UUID casterId = harvest.casterId();
        int queued = QUEUED.getOrDefault(casterId, 0);
        if (queued + positions.size() > MAX_QUEUED_PER_PLAYER) {
            throw new SpellRuntimeException(ERROR_TOO_MANY_PENDING);
        }
        QUEUED.put(casterId, queued + positions.size());
        ACTIVE.addLast(new PendingHarvest(casterId, harvest, new ArrayDeque<>(positions), dropPos.immutable()));
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE.isEmpty()) {
            return;
        }
        long deadline = MassWorkBudget.deadline(event.getServer());
        while (!ACTIVE.isEmpty() && System.nanoTime() < deadline) {
            PendingHarvest task = ACTIVE.pollFirst();
            if (!task.harvest().isCasterPresent()) {
                task.harvest().abandon(task.dropPos());
                release(task);
                continue;
            }
            while (!task.positions().isEmpty() && System.nanoTime() < deadline) {
                task.harvest().harvest(task.positions().poll());
            }
            // A large harvest is handed over across ticks too.
            if (task.positions().isEmpty() && task.harvest().finish(deadline)) {
                release(task);
            } else {
                // Round-robin: other harvests go first next tick.
                ACTIVE.addLast(task);
            }
        }
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        // The crops not reached stay ripe, and what was harvested is not lost.
        for (PendingHarvest task : ACTIVE) {
            // A machine does not tick again to move the harvest from its caster into its slots.
            if (task.harvest().isCasterPresent() && !task.harvest().isCastByMachine()) {
                task.harvest().finish();
            } else {
                task.harvest().abandon(task.dropPos());
            }
        }
        ACTIVE.clear();
        QUEUED.clear();
    }

    private static void release(PendingHarvest task) {
        int remaining = QUEUED.getOrDefault(task.casterId(), 0) - task.scheduled();
        if (remaining <= 0) {
            QUEUED.remove(task.casterId());
        } else {
            QUEUED.put(task.casterId(), remaining);
        }
    }

    private record PendingHarvest(UUID casterId, HarvestHelper.Harvest harvest, ArrayDeque<BlockPos> positions,
                                  BlockPos dropPos, int scheduled) {
        PendingHarvest(UUID casterId, HarvestHelper.Harvest harvest, ArrayDeque<BlockPos> positions, BlockPos dropPos) {
            this(casterId, harvest, positions, dropPos, positions.size());
        }
    }
}
