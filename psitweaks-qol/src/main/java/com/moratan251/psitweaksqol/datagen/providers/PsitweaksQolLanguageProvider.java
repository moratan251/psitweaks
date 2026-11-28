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
        root.addProperty("gui.psitweaks_qol.spell_programmer.settings.english_names", switch (locale) {
            case "ja_jp" -> "英語名も表示する";
            default -> "Show English names";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.settings.english_names.tooltip", switch (locale) {
            case "ja_jp" -> "ピース一覧のツールチップに、英語名も表示します (英語名でも検索できます)";
            default -> "Also shows the English name in piece list tooltips (pieces can be searched by it)";
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

        root.addProperty("psitweaks_qol.snippet.button", switch (locale) {
            case "ja_jp" -> "スニペット";
            default -> "Snippets";
        });
        root.addProperty("psitweaks_qol.snippet.button.save", switch (locale) {
            case "ja_jp" -> "選んだピースに名前を付けて保存 (Ctrl + S)";
            default -> "Save the selected pieces with a name (Ctrl + S)";
        });
        root.addProperty("psitweaks_qol.snippet.button.list", switch (locale) {
            case "ja_jp" -> "保存したスニペットの一覧 (Ctrl + S)";
            default -> "List saved snippets (Ctrl + S)";
        });
        root.addProperty("psitweaks_qol.snippet.button.select_hint", switch (locale) {
            case "ja_jp" -> "Shift + ドラッグでピースを選んでから押すと保存";
            default -> "Select pieces with Shift + drag first to save them";
        });
        root.addProperty("psitweaks_qol.snippet.save.title", switch (locale) {
            case "ja_jp" -> "スニペットを保存 (%s×%s・%s 個)";
            default -> "Save snippet (%s×%s, %s pieces)";
        });
        root.addProperty("psitweaks_qol.snippet.save.name", switch (locale) {
            case "ja_jp" -> "名前";
            default -> "Name";
        });
        root.addProperty("psitweaks_qol.snippet.save.name_hint", switch (locale) {
            case "ja_jp" -> "名前を入力";
            default -> "Enter a name";
        });
        root.addProperty("psitweaks_qol.snippet.save.hint", switch (locale) {
            case "ja_jp" -> "Enter: 保存 / Esc: やめる";
            default -> "Enter: save / Esc: cancel";
        });
        root.addProperty("psitweaks_qol.snippet.save.exists", switch (locale) {
            case "ja_jp" -> "同じ名前があります。保存を 2 回押すと上書きします";
            default -> "This name is already used. Press Save twice to overwrite it.";
        });
        root.addProperty("psitweaks_qol.snippet.save.overwrite_confirm", switch (locale) {
            case "ja_jp" -> "もう一度押すと上書きします";
            default -> "Press again to overwrite it.";
        });
        root.addProperty("psitweaks_qol.snippet.save.button", switch (locale) {
            case "ja_jp" -> "保存";
            default -> "Save";
        });
        root.addProperty("psitweaks_qol.snippet.save.overwrite_button", switch (locale) {
            case "ja_jp" -> "上書き";
            default -> "Overwrite";
        });
        root.addProperty("psitweaks_qol.snippet.save.cancel", switch (locale) {
            case "ja_jp" -> "やめる";
            default -> "Cancel";
        });
        root.addProperty("psitweaks_qol.snippet.saved", switch (locale) {
            case "ja_jp" -> "スニペット「%s」を保存しました";
            default -> "Saved snippet \"%s\"";
        });
        root.addProperty("psitweaks_qol.snippet.save_failed", switch (locale) {
            case "ja_jp" -> "スニペットを保存できませんでした: %s";
            default -> "Could not save the snippet: %s";
        });
        root.addProperty("psitweaks_qol.snippet.deleted", switch (locale) {
            case "ja_jp" -> "スニペット「%s」を削除しました";
            default -> "Deleted snippet \"%s\"";
        });
        root.addProperty("psitweaks_qol.snippet.delete_failed", switch (locale) {
            case "ja_jp" -> "スニペットを削除できませんでした: %s";
            default -> "Could not delete the snippet: %s";
        });
        root.addProperty("psitweaks_qol.snippet.list.title", switch (locale) {
            case "ja_jp" -> "スニペット";
            default -> "Snippets";
        });
        root.addProperty("psitweaks_qol.snippet.list.count", switch (locale) {
            case "ja_jp" -> "%s 件";
            default -> "%s saved";
        });
        root.addProperty("psitweaks_qol.snippet.list.search", switch (locale) {
            case "ja_jp" -> "絞り込み";
            default -> "Filter";
        });
        root.addProperty("psitweaks_qol.snippet.list.search_hint", switch (locale) {
            case "ja_jp" -> "名前で絞り込み";
            default -> "Filter by name";
        });
        root.addProperty("psitweaks_qol.snippet.list.empty", switch (locale) {
            case "ja_jp" -> "保存したスニペットはありません。Shift + ドラッグでピースを選び、このボタンか Ctrl + S で保存できます。";
            default -> "No snippets yet. Select pieces with Shift + drag, then press this button or Ctrl + S to save them.";
        });
        root.addProperty("psitweaks_qol.snippet.list.no_match", switch (locale) {
            case "ja_jp" -> "一致するスニペットはありません。";
            default -> "No matching snippets.";
        });
        root.addProperty("psitweaks_qol.snippet.list.open_folder", switch (locale) {
            case "ja_jp" -> "フォルダを開く";
            default -> "Open folder";
        });
        root.addProperty("psitweaks_qol.snippet.list.delete_hint", switch (locale) {
            case "ja_jp" -> "Shift + クリックで削除";
            default -> "Shift + click to delete";
        });
        root.addProperty("psitweaks_qol.snippet.size", "%s×%s");
        root.addProperty("psitweaks_qol.snippet.info", switch (locale) {
            case "ja_jp" -> "%s×%s・%s 個のピース";
            default -> "%s×%s, %s pieces";
        });
        root.addProperty("psitweaks_qol.snippet.place.hint", switch (locale) {
            case "ja_jp" -> "「%s」を置く: 左クリック (Shift で続けて置く) / 右クリックか Esc でやめる";
            default -> "Placing \"%s\": left click (hold Shift to keep placing) / right click or Esc to stop";
        });
        root.addProperty("psitweaks_qol.snippet.unusable", switch (locale) {
            case "ja_jp" -> "未習得ピース %s 個";
            default -> "%s unlearned pieces";
        });
        root.addProperty("psitweaks_qol.paste.skipped", switch (locale) {
            case "ja_jp" -> "使えないピース %s 個を飛ばして貼り付けました";
            default -> "Pasted, skipping %s unusable pieces";
        });
        root.addProperty("psitweaks_qol.paste.nothing", switch (locale) {
            case "ja_jp" -> "使えないピースだけなので、%s 個とも貼り付けませんでした";
            default -> "Nothing pasted: all %s pieces are unusable";
        });
        root.addProperty("psitweaks_qol.paste.reason.unknown", switch (locale) {
            case "ja_jp" -> "見つからない (mod がない)";
            default -> "Unknown (mod missing)";
        });
        root.addProperty("psitweaks_qol.paste.reason.disabled", switch (locale) {
            case "ja_jp" -> "無効";
            default -> "Disabled";
        });
        root.addProperty("psitweaks_qol.paste.reason.locked", switch (locale) {
            case "ja_jp" -> "未解禁";
            default -> "Locked";
        });
        root.addProperty("psitweaks_qol.paste.reason.not_learned", switch (locale) {
            case "ja_jp" -> "未習得";
            default -> "Not learned";
        });

        return root;
    }
}
