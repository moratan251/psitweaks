package com.moratan251.psitweaks.client.gui;

import java.util.Arrays;

/** Capacity-aware matching of ingredient slots to stock entries, independent of Minecraft. */
final class IdeaStorageIngredientAssignment {
    private IdeaStorageIngredientAssignment() {
    }

    static int[] assign(int[][] candidates, long[] counts) {
        int[] assignment = new int[candidates.length];
        Arrays.fill(assignment, -1);
        int[][] owners = new int[counts.length][candidates.length];
        int[] used = new int[counts.length];
        for (int slot = 0; slot < candidates.length; slot++) {
            assignSlot(slot, candidates, counts, assignment, owners, used, new boolean[counts.length]);
        }
        return assignment;
    }

    private static boolean assignSlot(int slot, int[][] candidates, long[] counts, int[] assignment,
                                      int[][] owners, int[] used, boolean[] visited) {
        for (int entry : candidates[slot]) {
            if (visited[entry] || counts[entry] <= 0) {
                continue;
            }
            visited[entry] = true;
            if (used[entry] < counts[entry]) {
                owners[entry][used[entry]++] = slot;
                assignment[slot] = entry;
                return true;
            }
            // Free capacity by moving an earlier ingredient to another compatible entry.
            for (int index = 0; index < used[entry]; index++) {
                if (assignSlot(owners[entry][index], candidates, counts, assignment, owners, used, visited)) {
                    owners[entry][index] = slot;
                    assignment[slot] = entry;
                    return true;
                }
            }
        }
        return false;
    }
}
