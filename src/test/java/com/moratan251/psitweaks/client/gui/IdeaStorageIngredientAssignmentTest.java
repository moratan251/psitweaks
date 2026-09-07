package com.moratan251.psitweaks.client.gui;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;

class IdeaStorageIngredientAssignmentTest {
    @Test
    void reservesOakForTheOakOnlyIngredient() {
        assertArrayEquals(new int[] {1, 0}, IdeaStorageIngredientAssignment.assign(
                new int[][] {{0, 1}, {0}}, new long[] {1, 1}));
    }

    @Test
    void reassignsAChainOfOverlappingIngredients() {
        assertArrayEquals(new int[] {1, 2, 0}, IdeaStorageIngredientAssignment.assign(
                new int[][] {{0, 1}, {1, 2}, {0}}, new long[] {1, 1, 1}));
    }

    @Test
    void respectsCapacityAndReportsRealShortages() {
        int[] result = IdeaStorageIngredientAssignment.assign(
                new int[][] {{0}, {0}, {0}, {}, {1}}, new long[] {2, 0});
        assertArrayEquals(new int[] {0, 0, -1, -1, -1}, result);
    }

    @Test
    void preservesCandidatePreferenceWhenStockAllowsIt() {
        long[] counts = {Long.MAX_VALUE, 1};
        assertArrayEquals(new int[] {0, 0, 1}, IdeaStorageIngredientAssignment.assign(
                new int[][] {{0, 1}, {0, 1}, {1, 0}}, counts));
        assertArrayEquals(new long[] {Long.MAX_VALUE, 1}, counts);
    }

    @Test
    void movesOneOwnerOfAResourceWithMultipleUnits() {
        assertArrayEquals(new int[] {1, 0, 0}, IdeaStorageIngredientAssignment.assign(
                new int[][] {{0, 1}, {0}, {0}}, new long[] {2, 1}));
    }

    @Test
    void matchesExhaustiveSearchOnSmallInventories() {
        Random random = new Random(251);
        for (int trial = 0; trial < 2000; trial++) {
            int[][] candidates = new int[1 + random.nextInt(6)][];
            long[] counts = {random.nextInt(3), random.nextInt(3), random.nextInt(3)};
            for (int slot = 0; slot < candidates.length; slot++) {
                int mask = random.nextInt(8);
                final int choices = mask;
                candidates[slot] = java.util.stream.IntStream.range(0, 3)
                        .filter(entry -> (choices & (1 << entry)) != 0).toArray();
            }
            int[] result = IdeaStorageIngredientAssignment.assign(candidates, counts);
            long[] consumed = new long[counts.length];
            for (int slot = 0; slot < result.length; slot++) {
                final int assigned = result[slot];
                if (assigned >= 0) {
                    assertTrue(Arrays.stream(candidates[slot]).anyMatch(entry -> entry == assigned));
                    consumed[assigned]++;
                }
            }
            for (int entry = 0; entry < counts.length; entry++) {
                assertTrue(consumed[entry] <= counts[entry]);
            }
            assertEquals(bestCount(candidates, counts.clone(), 0),
                    (int) Arrays.stream(result).filter(entry -> entry >= 0).count(), "trial " + trial);
        }
    }

    private static int bestCount(int[][] candidates, long[] remaining, int slot) {
        if (slot == candidates.length) {
            return 0;
        }
        int best = bestCount(candidates, remaining, slot + 1);
        for (int entry : candidates[slot]) {
            if (remaining[entry] > 0) {
                remaining[entry]--;
                best = Math.max(best, 1 + bestCount(candidates, remaining, slot + 1));
                remaining[entry]++;
            }
        }
        return best;
    }
}
