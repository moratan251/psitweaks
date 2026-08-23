package com.moratan251.psitweaks.client.gui;

import com.moratan251.psitweaks.common.network.MessageIdeaStorageSync;
import java.util.Comparator;
import net.minecraft.core.registries.BuiltInRegistries;
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
            Comparator.comparing(entry -> itemKey(entry).toString())),
    MOD_ID("mod_id", new ItemStack(Items.BOOK),
            Comparator.comparing((MessageIdeaStorageSync.Entry entry) -> itemKey(entry).getNamespace())
                    .thenComparing(entry -> itemKey(entry).toString())),
    COUNT("count", new ItemStack(Items.HOPPER),
            Comparator.comparingLong(MessageIdeaStorageSync.Entry::count).reversed());

    private final String id;
    private final ItemStack icon;
    /** PORT では null(ソートせず元順を維持)。 */
    private final Comparator<MessageIdeaStorageSync.Entry> comparator;

    IdeaStorageSortMode(String id, ItemStack icon, Comparator<MessageIdeaStorageSync.Entry> comparator) {
        this.id = id;
        this.icon = icon;
        this.comparator = comparator;
    }

    private static net.minecraft.resources.ResourceLocation itemKey(MessageIdeaStorageSync.Entry entry) {
        return BuiltInRegistries.ITEM.getKey(entry.template().getItem());
    }

    public Component displayName() {
        return Component.translatable("gui.psitweaks.idea_storage.sort." + id);
    }

    public ItemStack icon() {
        return icon;
    }

    public Comparator<MessageIdeaStorageSync.Entry> comparator() {
        return comparator;
    }

    public IdeaStorageSortMode next() {
        IdeaStorageSortMode[] modes = values();
        return modes[(ordinal() + 1) % modes.length];
    }
}
