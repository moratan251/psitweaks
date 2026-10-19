package com.moratan251.psitweaks.common.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

/**
 * Finds the items a player carries for spells that read one item of a kind (Filter Card, Blade Pouch): the Curios slots
 * first (in Curios' slot order), then the main hand, the offhand, the hotbar and the main inventory. The first one found
 * is the one in use. The order is the same on the server and the client.
 */
public final class CarriedItems {
    private CarriedItems() {
    }

    /** The matching stacks the player carries, in lookup order. The returned stacks are the live stacks, not copies. */
    public static List<ItemStack> find(Player player, Predicate<ItemStack> matches) {
        List<ItemStack> found = new ArrayList<>();
        Set<ItemStack> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        collectCurios(player, matches, found, seen);

        Inventory inventory = player.getInventory();
        add(matches, found, seen, player.getMainHandItem());
        add(matches, found, seen, player.getOffhandItem());
        for (int slot = 0; slot < inventory.items.size(); slot++) {
            add(matches, found, seen, inventory.items.get(slot));
        }
        return found;
    }

    private static void collectCurios(Player player, Predicate<ItemStack> matches, List<ItemStack> found, Set<ItemStack> seen) {
        CuriosApi.getCuriosInventory(player).ifPresent(curios -> {
            List<Map.Entry<String, ICurioStacksHandler>> handlers = new ArrayList<>(curios.getCurios().entrySet());
            // The map order is not guaranteed to match between server and client; sort like the Curios screen does.
            handlers.sort(Comparator
                    .comparingInt((Map.Entry<String, ICurioStacksHandler> entry) -> slotOrder(player, entry.getKey()))
                    .thenComparing(Map.Entry::getKey));
            for (Map.Entry<String, ICurioStacksHandler> entry : handlers) {
                ICurioStacksHandler handler = entry.getValue();
                NonNullList<Boolean> activeStates = handler.getActiveStates();
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    if (slot < activeStates.size() && !activeStates.get(slot)) {
                        continue;
                    }
                    add(matches, found, seen, handler.getStacks().getStackInSlot(slot));
                }
            }
        });
    }

    private static int slotOrder(Player player, String identifier) {
        return CuriosApi.getSlot(identifier, player.level()).map(ISlotType::getOrder).orElse(Integer.MAX_VALUE);
    }

    private static void add(Predicate<ItemStack> matches, List<ItemStack> found, Set<ItemStack> seen, ItemStack stack) {
        if (!stack.isEmpty() && matches.test(stack) && seen.add(stack)) {
            found.add(stack);
        }
    }
}
