package com.moratan251.psitweaks.common.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

/**
 * Contents and lookup of Blade Pouches, the sword holders Trick: Dancing Blades draws its blades from. The swords are
 * kept in the pouch's {@code minecraft:container} component. Only one pouch is in use: the first one found, in the
 * same order as Filter Cards ({@link CarriedItems}).
 */
public final class BladePouches {
    public static final int SLOT_COUNT = 8;

    private BladePouches() {
    }

    public static boolean isPouch(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemBladePouch;
    }

    /** What a pouch accepts: anything in {@code #minecraft:swords}. */
    public static boolean isSword(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ItemTags.SWORDS);
    }

    /** Copies of the pouch's slots, always {@link #SLOT_COUNT} entries; empty slots are {@link ItemStack#EMPTY}. */
    public static NonNullList<ItemStack> contents(ItemStack pouch) {
        NonNullList<ItemStack> contents = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        pouch.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(contents);
        return contents;
    }

    /** Copies of the swords in the pouch, in slot order, without the empty slots. */
    public static List<ItemStack> swords(ItemStack pouch) {
        List<ItemStack> swords = new ArrayList<>();
        for (ItemStack stack : contents(pouch)) {
            if (isSword(stack)) {
                swords.add(stack);
            }
        }
        return swords;
    }

    /**
     * Whether a sword has no durability left. Vanilla swords break away before that, but a psimetal sword stays as a
     * broken tool that neither hits hard nor casts, and a blade (which strikes with a fresh copy) must not revive it.
     */
    public static boolean isBroken(ItemStack sword) {
        return sword.isDamageableItem() && sword.getDamageValue() >= sword.getMaxDamage();
    }

    /** The swords Trick: Dancing Blades deploys: those in the pouch that are not broken. */
    public static List<ItemStack> deployableSwords(ItemStack pouch) {
        List<ItemStack> swords = swords(pouch);
        swords.removeIf(BladePouches::isBroken);
        return swords;
    }

    /** Stores the slots. Entries past {@link #SLOT_COUNT} are ignored. */
    public static void setContents(ItemStack pouch, List<ItemStack> contents) {
        List<ItemStack> normalized = new ArrayList<>(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) {
            normalized.add(i < contents.size() ? contents.get(i).copy() : ItemStack.EMPTY);
        }
        ItemContainerContents stored = ItemContainerContents.fromItems(normalized);
        if (stored.equals(ItemContainerContents.EMPTY)) {
            pouch.remove(DataComponents.CONTAINER);
        } else {
            pouch.set(DataComponents.CONTAINER, stored);
        }
    }

    /**
     * True for a pouch the player carries that is not the pouch in use. The comparison is by identity, so a copy (a
     * creative tab entry, a recipe viewer, the cursor stack) is never idle.
     */
    public static boolean isIdle(Player player, ItemStack stack) {
        if (!isPouch(stack)) {
            return false;
        }
        List<ItemStack> pouches = CarriedItems.find(player, BladePouches::isPouch);
        for (int i = 1; i < pouches.size(); i++) {
            if (pouches.get(i) == stack) {
                return true;
            }
        }
        return false;
    }

    /** The pouch spells draw from, or empty when the player carries none. */
    public static Optional<ItemStack> findActive(Player player) {
        List<ItemStack> pouches = CarriedItems.find(player, BladePouches::isPouch);
        return pouches.isEmpty() ? Optional.empty() : Optional.of(pouches.get(0));
    }
}
