package com.moratan251.psitweaks.common.storage.idea;

import net.minecraft.nbt.*;

/** Forge 1.20.1 item identity limits, shared by admission and S2C synchronization. */
public final class IdeaStorageNbtLimits {
    public static final int MAX_RECORD_BYTES = 64 * 1024 * 1024;
    // Leave space for the resource ID, amount and kind in a synchronization record.
    public static final long MAX_ITEM_NBT_BYTES = MAX_RECORD_BYTES - 32L;

    private IdeaStorageNbtLimits() { }

    public static boolean fitsItem(CompoundTag tag) {
        try {
            accountItem(tag, MAX_ITEM_NBT_BYTES);
            return true;
        } catch (RuntimeException invalid) {
            return false;
        }
    }

    /** Mirrors Forge's NbtIo/TagType accounting without serializing or copying array contents. */
    static long accountItem(CompoundTag tag, long quota) {
        NbtAccounter budget = new NbtAccounter(quota);
        budget.accountBytes(7); // root type, empty UTF name, Forge object allocation
        accountTag(tag, 0, budget);
        return budget.getUsage();
    }

    private static void accountTag(Tag tag, int depth, NbtAccounter budget) {
        if (tag instanceof CompoundTag compound) {
            checkDepth(depth);
            budget.accountBytes(48);
            for (String name : compound.getAllKeys()) {
                budget.accountBytes(2); // Forge readNamedTagType
                accountUtf(name, budget);
                budget.accountBytes(28L + 2L * name.length() + 4 + 36);
                accountTag(compound.get(name), depth + 1, budget);
            }
            budget.accountBytes(2); // end marker
        } else if (tag instanceof ListTag list) {
            checkDepth(depth);
            budget.accountBytes(37L + 4L * list.size());
            for (Tag child : list) accountTag(child, depth + 1, budget);
        } else if (tag instanceof StringTag string) {
            budget.accountBytes(36);
            accountUtf(string.getAsString(), budget);
        } else if (tag instanceof ByteArrayTag array) {
            budget.accountBytes(24L + array.getAsByteArray().length);
        } else if (tag instanceof IntArrayTag array) {
            budget.accountBytes(24L + 4L * array.getAsIntArray().length);
        } else if (tag instanceof LongArrayTag array) {
            budget.accountBytes(24L + 8L * array.getAsLongArray().length);
        } else {
            budget.accountBytes(tag.sizeInBytes());
        }
    }

    private static void accountUtf(String text, NbtAccounter budget) {
        long before = budget.getUsage();
        budget.readUTF(text);
        if (budget.getUsage() - before > 65535L + 2) {
            throw new IllegalArgumentException("NBT string exceeds modified UTF limit");
        }
    }

    private static void checkDepth(int depth) {
        if (depth > 512) throw new IllegalArgumentException("NBT depth exceeds 512");
    }
}
