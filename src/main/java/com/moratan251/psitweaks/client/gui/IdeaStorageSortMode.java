package com.moratan251.psitweaks.client.gui;

import java.util.Comparator;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * イデアストレージ仮想グリッドの表示順。クライアント側のみで完結し、ストレージ内部順(= sync リスト順)は変更しない。
 * PORT がデフォルト(ポート番号順 = sync リストの元順)。
 */
public enum IdeaStorageSortMode {
    PORT("port", new ItemStack(Items.ENDER_CHEST), null),
    ITEM_ID("item_id", new ItemStack(Items.NAME_TAG),
            Comparator.comparing(entry -> entry.resourceId().toString())),
    MOD_ID("mod_id", new ItemStack(Items.BOOK),
            Comparator.comparing((IdeaStorageDisplayEntry entry) -> entry.resourceId().getNamespace())
                    .thenComparing(entry -> entry.resourceId().toString())),
    COUNT("count", new ItemStack(Items.HOPPER),
            Comparator.comparing(IdeaStorageDisplayEntry::amountInDisplayUnits).reversed());

    private final String id;
    private final ItemStack icon;
    /** PORT では null(ソートせず元順を維持)。 */
    private final Comparator<IdeaStorageDisplayEntry> comparator;

    IdeaStorageSortMode(String id, ItemStack icon, Comparator<IdeaStorageDisplayEntry> comparator) {
        this.id = id;
        this.icon = icon;
        this.comparator = comparator;
    }

    public Component displayName() {
        return Component.translatable("gui.psitweaks.idea_storage.sort." + id);
    }

    public ItemStack icon() {
        return icon;
    }

    public Comparator<IdeaStorageDisplayEntry> comparator() {
        return comparator;
    }

    public IdeaStorageSortMode next() {
        IdeaStorageSortMode[] modes = values();
        return modes[(ordinal() + 1) % modes.length];
    }
}
