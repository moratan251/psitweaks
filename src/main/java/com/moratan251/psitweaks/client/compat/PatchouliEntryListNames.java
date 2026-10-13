package com.moratan251.psitweaks.client.compat;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;

/**
 * Patchouli のエントリ一覧ボタンだけで使う短縮名。
 * エントリ名キーに {@code .book_list} を付けたキーが現在の言語にあればそちらを表示し、
 * エントリ本文の見出し・検索・並び順は元の名前のまま残す。
 */
public final class PatchouliEntryListNames {
    private static final String KEY_PREFIX = "psitweaks.";
    private static final String LIST_SUFFIX = ".book_list";

    private PatchouliEntryListNames() {
    }

    public static MutableComponent listName(MutableComponent name) {
        if (name.getContents() instanceof TranslatableContents contents && contents.getKey().startsWith(KEY_PREFIX)) {
            String listKey = contents.getKey() + LIST_SUFFIX;
            if (I18n.exists(listKey)) {
                return Component.translatable(listKey);
            }
        }
        return name;
    }
}
