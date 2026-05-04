package com.moratan251.psitweaks.common.items.curios;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

public final class CuriosSlotChecker {
    public static final String MAGIC_CALCULATION_AREA = "magic_calculation_area";

    private CuriosSlotChecker() {
    }

    public static boolean hasItemInMagicSlot(Player player, Class<?> itemClass) {
        return !getAllItemsFromMagicSlot(player, itemClass).isEmpty();
    }

    public static ItemStack getItemFromMagicSlot(Player player, Class<?> itemClass) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(curios -> curios.getStacksHandler(MAGIC_CALCULATION_AREA))
                .map(slotInventory -> {
                    for (int slot = 0; slot < slotInventory.getSlots(); slot++) {
                        ItemStack stack = slotInventory.getStacks().getStackInSlot(slot);
                        if (!stack.isEmpty() && itemClass.isInstance(stack.getItem())) {
                            return stack;
                        }
                    }
                    return ItemStack.EMPTY;
                })
                .orElse(ItemStack.EMPTY);
    }

    public static ItemStack getItemFromSelectedMagicSlot(Player player, int slot) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(curios -> curios.getStacksHandler(MAGIC_CALCULATION_AREA))
                .map(slotInventory -> getStack(slotInventory, slot))
                .orElse(ItemStack.EMPTY);
    }

    public static List<ItemStack> getAllItemsFromMagicSlot(Player player, Class<?> itemClass) {
        List<ItemStack> items = new ArrayList<>();
        CuriosApi.getCuriosInventory(player).ifPresent(curios -> curios.getStacksHandler(MAGIC_CALCULATION_AREA)
                .ifPresent(slotInventory -> {
                    for (int slot = 0; slot < slotInventory.getSlots(); slot++) {
                        ItemStack stack = slotInventory.getStacks().getStackInSlot(slot);
                        if (!stack.isEmpty() && itemClass.isInstance(stack.getItem())) {
                            items.add(stack);
                        }
                    }
                }));
        return items;
    }

    public static void performActionIfItemExists(Player player, Class<?> itemClass, Runnable action) {
        if (hasItemInMagicSlot(player, itemClass)) {
            action.run();
        }
    }

    private static ItemStack getStack(ICurioStacksHandler slotInventory, int slot) {
        if (slot < 0 || slot >= slotInventory.getSlots()) {
            return ItemStack.EMPTY;
        }
        return slotInventory.getStacks().getStackInSlot(slot);
    }
}
