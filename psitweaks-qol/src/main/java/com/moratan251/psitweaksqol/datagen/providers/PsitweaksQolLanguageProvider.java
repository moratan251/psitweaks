package com.moratan251.psitweaksqol.datagen.providers;

import com.google.gson.JsonObject;
import com.moratan251.psitweaksqol.PsitweaksQol;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public class PsitweaksQolLanguageProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;
    private final String locale;

    public PsitweaksQolLanguageProvider(PackOutput output, String locale) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "lang");
        this.locale = locale;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return DataProvider.saveStable(output, translations(),
                pathProvider.json(ResourceLocation.fromNamespaceAndPath(PsitweaksQol.MOD_ID, locale)));
    }

    @Override
    public String getName() {
        return "Psitweaks QoL language " + locale;
    }

    private JsonObject translations() {
        JsonObject root = new JsonObject();

        root.addProperty("gui.psitweaks_qol.spell_programmer.tab.bookmark", switch (locale) {
            case "ja_jp" -> "ブックマーク (Ctrl + クリック) したピースのみ表示";
            default -> "Show only spell pieces bookmarked with Ctrl + click";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.tab.recent", switch (locale) {
            case "ja_jp" -> "最近使ったピースのみ表示 (新しい順)";
            default -> "Show only recently used spell pieces (newest first)";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.tab.selector", switch (locale) {
            case "ja_jp" -> "取得子のみ表示";
            default -> "Show only selectors";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.tab.operator", switch (locale) {
            case "ja_jp" -> "演算子のみ表示";
            default -> "Show only operators";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.tab.trick", switch (locale) {
            case "ja_jp" -> "作動式のみ表示";
            default -> "Show only tricks";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.tab.other", switch (locale) {
            case "ja_jp" -> "その他のピース (定数子、コネクタ、エラー抑止など) のみ表示";
            default -> "Show only other spell pieces (constants, connectors, error suppressors, etc.)";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.tab.show_all", switch (locale) {
            case "ja_jp" -> "すべてのピースを表示";
            default -> "Show all spell pieces";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.tab.selector.label", switch (locale) {
            case "ja_jp" -> "取";
            default -> "S";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.tab.operator.label", switch (locale) {
            case "ja_jp" -> "演";
            default -> "O";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.tab.trick.label", switch (locale) {
            case "ja_jp" -> "作";
            default -> "T";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.tab.other.label", switch (locale) {
            case "ja_jp" -> "他";
            default -> "…";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.settings", switch (locale) {
            case "ja_jp" -> "ピース一覧の設定";
            default -> "Piece list settings";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.settings.close", switch (locale) {
            case "ja_jp" -> "ピース一覧に戻る";
            default -> "Back to the piece list";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.settings.title", switch (locale) {
            case "ja_jp" -> "設定";
            default -> "Settings";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.settings.size", switch (locale) {
            case "ja_jp" -> "大きさ";
            default -> "Size";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.settings.size.small", switch (locale) {
            case "ja_jp" -> "小";
            default -> "S";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.settings.size.medium", switch (locale) {
            case "ja_jp" -> "中";
            default -> "M";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.settings.size.large", switch (locale) {
            case "ja_jp" -> "大";
            default -> "L";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.settings.size.tooltip", switch (locale) {
            case "ja_jp" -> "1 ページに %s 列 × %s 行 (%s 個)";
            default -> "%s columns × %s rows per page (%s pieces)";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.settings.keep_search", switch (locale) {
            case "ja_jp" -> "検索内容の保持";
            default -> "Keep search text";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.settings.keep_search.tooltip", switch (locale) {
            case "ja_jp" -> "ピース一覧を閉じても、検索欄に入力した文字を残します";
            default -> "Keeps the text in the search field after the piece list closes";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.settings.right_click_clear", switch (locale) {
            case "ja_jp" -> "右クリックで検索欄のクリア";
            default -> "Right-click clears search";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.settings.right_click_clear.tooltip", switch (locale) {
            case "ja_jp" -> "検索欄を右クリックすると、入力した文字を消します";
            default -> "Right-clicking the search field clears its text";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.settings.right_click_close", switch (locale) {
            case "ja_jp" -> "右クリックで一覧を閉じる";
            default -> "Right-click closes list";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.settings.right_click_close.tooltip", switch (locale) {
            case "ja_jp" -> "ピース一覧の検索欄以外を右クリックすると、一覧を閉じます";
            default -> "Right-clicking the piece list outside its search field closes it";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.settings.english_sort", switch (locale) {
            case "ja_jp" -> "英語名順に並べる";
            default -> "Sort by English name";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.settings.english_sort.tooltip", switch (locale) {
            case "ja_jp" -> "言語設定に関係なく、ピースを英語名の順に並べます";
            default -> "Sorts spell pieces by their English names regardless of the game language";
        });
        root.addProperty("psitweaks_qol.gui.spell_piece_mode", switch (locale) {
            case "ja_jp" -> "モード: %s";
            default -> "Mode: %s";
        });
        root.addProperty("psitweaks_qol.gui.spell_piece_mode.title", switch (locale) {
            case "ja_jp" -> "モード選択";
            default -> "Mode Select";
        });
        root.addProperty("psitweaks_qol.gui.string_constant_input.empty", switch (locale) {
            case "ja_jp" -> "空文字列";
            default -> "Empty string";
        });
        root.addProperty("psitweaks_qol.gui.string_constant_input.hint", switch (locale) {
            case "ja_jp" -> "Shift+Enter改行 / Enter閉";
            default -> "Shift+Enter newline / Enter closes";
        });
        root.addProperty("psitweaks_qol.gui.string_constant_input.read_only", switch (locale) {
            case "ja_jp" -> "閲覧のみ";
            default -> "Read only";
        });
        root.addProperty("psitweaks_qol.gui.string_constant_input.button.copy_all", switch (locale) {
            case "ja_jp" -> "全コピー";
            default -> "Copy All";
        });
        root.addProperty("psitweaks_qol.gui.string_constant_input.button.clear_all", switch (locale) {
            case "ja_jp" -> "全削除";
            default -> "Clear";
        });
        root.addProperty("psitweaks_qol.gui.string_constant_input.button.replace_all", switch (locale) {
            case "ja_jp" -> "貼付";
            default -> "Paste";
        });
        root.addProperty("psitweaks_qol.gui.spell_name_from_trick", switch (locale) {
            case "ja_jp" -> "作動式の名前を術式名にする";
            default -> "Name Spell After Trick";
        });
        root.addProperty("psitweaks_qol.gui.spell_name_from_trick.single", switch (locale) {
            case "ja_jp" -> "術式名を「%s」にします";
            default -> "Renames the spell to \"%s\"";
        });
        root.addProperty("psitweaks_qol.gui.spell_name_from_trick.multiple", switch (locale) {
            case "ja_jp" -> "押したあと、名前にする作動式をクリックで選びます";
            default -> "Then click the trick to take the name from";
        });
        root.addProperty("psitweaks_qol.gui.spell_name_from_trick.none", switch (locale) {
            case "ja_jp" -> "この術式には作動式がありません";
            default -> "This spell has no tricks";
        });
        root.addProperty("psitweaks_qol.gui.spell_name_from_trick.pick", switch (locale) {
            case "ja_jp" -> "術式名にする作動式をクリック (Esc: 中止)";
            default -> "Click a trick to name the spell (Esc: cancel)";
        });

        return root;
    }
}
