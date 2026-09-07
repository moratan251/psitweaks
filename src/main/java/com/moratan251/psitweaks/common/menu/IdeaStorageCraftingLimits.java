package com.moratan251.psitweaks.common.menu;

final class IdeaStorageCraftingLimits {

    private IdeaStorageCraftingLimits() {
    }

    static int calculateShiftCraftLimit(int amountPerCraft, int maxStackSize) {
        if (amountPerCraft <= 0 || maxStackSize <= 0) {
            return 0;
        }
        return Math.max(1, maxStackSize / amountPerCraft);
    }
}
