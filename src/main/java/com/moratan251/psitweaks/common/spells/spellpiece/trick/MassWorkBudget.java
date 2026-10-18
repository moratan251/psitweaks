package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import javax.annotation.Nullable;
import net.minecraft.server.MinecraftServer;

/**
 * The time per server tick that the queued work of the mass tricks shares, so that together they cannot slow the
 * server down: the first scheduler to work in a tick opens the budget, and the others use what is left of it.
 */
final class MassWorkBudget {
    private static final long BUDGET_NS = 4_000_000L; // 4ms per tick shared across all tasks

    @Nullable
    private static MinecraftServer budgetServer;
    private static int budgetTick;
    private static long deadline;

    private MassWorkBudget() {
    }

    /** The {@link System#nanoTime()} at which the work of this tick must stop. */
    static long deadline(MinecraftServer server) {
        int tick = server.getTickCount();
        if (server != budgetServer || tick != budgetTick) {
            budgetServer = server;
            budgetTick = tick;
            deadline = System.nanoTime() + BUDGET_NS;
        }
        return deadline;
    }
}
