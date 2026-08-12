package com.moratan251.psitweaksqol.datagen.providers;

import com.moratan251.psitweaksqol.PsitweaksQol;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class PsitweaksQolLanguageProvider extends LanguageProvider {
    private final String locale;

    public PsitweaksQolLanguageProvider(PackOutput output, String locale) {
        super(output, PsitweaksQol.MOD_ID, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        add("gui.psitweaks_qol.spell_programmer.bookmarks.show", switch (locale) {
            case "ja_jp" -> "ブックマーク (Ctrl + クリック) したピースのみ表示";
            default -> "Show only spell pieces bookmarked with Ctrl + click";
        });
        add("gui.psitweaks_qol.spell_programmer.bookmarks.show_all", switch (locale) {
            case "ja_jp" -> "すべてのピースを表示";
            default -> "Show all spell pieces";
        });
        add("psitweaks_qol.gui.spell_piece_mode", switch (locale) {
            case "ja_jp" -> "モード: %s";
            default -> "Mode: %s";
        });
        add("psitweaks_qol.gui.spell_piece_mode.title", switch (locale) {
            case "ja_jp" -> "モード選択";
            default -> "Mode Select";
        });
        add("psitweaks_qol.gui.string_constant_input.empty", switch (locale) {
            case "ja_jp" -> "空文字列";
            default -> "Empty string";
        });
        add("psitweaks_qol.gui.string_constant_input.hint", switch (locale) {
            case "ja_jp" -> "Shift+Enter改行 / Enter閉";
            default -> "Shift+Enter newline / Enter closes";
        });
        add("psitweaks_qol.gui.string_constant_input.read_only", switch (locale) {
            case "ja_jp" -> "閲覧のみ";
            default -> "Read only";
        });
        add("psitweaks_qol.gui.string_constant_input.button.copy_all", switch (locale) {
            case "ja_jp" -> "全コピー";
            default -> "Copy All";
        });
        add("psitweaks_qol.gui.string_constant_input.button.clear_all", switch (locale) {
            case "ja_jp" -> "全削除";
            default -> "Clear";
        });
        add("psitweaks_qol.gui.string_constant_input.button.replace_all", switch (locale) {
            case "ja_jp" -> "貼付";
            default -> "Paste";
        });
    }
}
