package com.moratan251.psitweaks.common.storage.idea;

final class IdeaStorageCapacity {

    private IdeaStorageCapacity() {
    }

    static long simulateInsert(boolean loadFailed, boolean existing, int typeCount, int maxTypes,
                               long currentAmount, long totalAmount, long perTypeLimit, long totalLimit,
                               long requestedAmount) {
        if (loadFailed || requestedAmount <= 0 || currentAmount < 0 || totalAmount < 0
                || perTypeLimit <= 0 || totalLimit <= 0 || maxTypes <= 0) {
            return 0;
        }
        if (!existing && typeCount >= maxTypes) {
            return 0;
        }
        long typeFree = currentAmount >= perTypeLimit ? 0 : perTypeLimit - currentAmount;
        long totalFree = totalAmount >= totalLimit ? 0 : totalLimit - totalAmount;
        return Math.min(requestedAmount, Math.min(typeFree, totalFree));
    }
}
