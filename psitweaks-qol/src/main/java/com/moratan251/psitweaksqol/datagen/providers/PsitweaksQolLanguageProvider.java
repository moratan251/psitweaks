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

        root.addProperty("gui.psitweaks_qol.spell_programmer.bookmarks.show", switch (locale) {
            case "ja_jp" -> "ブックマーク (Ctrl + クリック) したピースのみ表示";
            default -> "Show only spell pieces bookmarked with Ctrl + click";
        });
        root.addProperty("gui.psitweaks_qol.spell_programmer.bookmarks.show_all", switch (locale) {
            case "ja_jp" -> "すべてのピースを表示";
            default -> "Show all spell pieces";
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
