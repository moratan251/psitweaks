package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonObject;
import com.moratan251.psitweaks.Psitweaks;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public class PsitweaksLanguageProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;
    private final String locale;

    public PsitweaksLanguageProvider(PackOutput output, String locale) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "lang");
        this.locale = locale;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return DataProvider.saveStable(output, translations(), pathProvider.json(Psitweaks.location(locale)));
    }

    @Override
    public String getName() {
        return "PsiTweaks language " + locale;
    }

    private JsonObject translations() {
        JsonObject root = new JsonObject();

        root.addProperty("creativetabs.psitweaks", "Psi: Tweaks and Additions");
        for (PsitweaksDatagenBlocks.GeneratedBlock block : PsitweaksDatagenBlocks.blocks()) {
            root.addProperty("block.psitweaks." + block.id(), switch (locale) {
                case "ja_jp" -> block.jaJp();
                default -> block.enUs();
            });
        }
        for (PsitweaksDatagenItems.GeneratedItem item : PsitweaksDatagenItems.items()) {
            root.addProperty("item.psitweaks." + item.id(), switch (locale) {
                case "ja_jp" -> item.jaJp();
                default -> item.enUs();
            });
        }
        addChemical(root, "infuse_psigem", "Psigem", "サイジェム");
        addChemical(root, "infuse_ebony", "Ebony", "黒檀");
        addChemical(root, "infuse_ivory", "Ivory", "白象牙");
        addChemical(root, "infuse_chaotic_factor", "Chaotic Factor", "混沌因子");
        addChemical(root, "infuse_psionic_echo", "Psionic Echo", "サイオニックエコー");
        addChemical(root, "infuse_hypostasis", "Hypostasis", "ヒュポスタシス");
        addChemical(root, "gas_psionic_echo", "Psionic Echo", "サイオニックエコー");
        addChemical(root, "gas_peo_fuel", "PEO Fuel", "PEO燃料");
        addChemical(root, "dirty_antinite", "Dirty Antinite Slurry", "汚れたアンティナイトの懸濁液");
        addChemical(root, "clean_antinite", "Clean Antinite Slurry", "純粋なアンティナイトの懸濁液");
        root.addProperty("tooltip.psitweaks.spell_magazine.bullets", switch (locale) {
            case "ja_jp" -> "装填済み術式弾: %s/%s";
            default -> "Loaded spell bullets: %s/%s";
        });
        addEffect(root, "parade", "Parade", "仮想行列");
        addEffect(root, "flight", "Flight", "飛行");
        addEffect(root, "barrier", "Barrier", "障壁");
        addEffect(root, "hardening", "Hardening", "硬化");
        addEffect(root, "radiation_filter", "Radiation Filter", "放射線フィルタ");

        addSpellPiece(
                root,
                "trick_supreme_infusion",
                "Trick: Supreme Infusion",
                "Infuse Echo Shards into Psionic Echoes",
                "作動式: 超位注入",
                "残響の欠片に注入してサイオニックエコーにします"
        );
        addSpellPiece(
                root,
                "trick_barrier",
                "Trick: Barrier",
                "Reduce damage taken",
                "作動式: 障壁",
                "受けるダメージを減少させる"
        );
        addSpellPiece(
                root,
                "trick_hardening",
                "Trick: Hardening",
                "When taking heavy damage, reduce it to a certain value",
                "作動式: 硬化",
                "大ダメージを受けた時、一定値まで減少させる"
        );
        addSpellPiece(
                root,
                "trick_parade",
                "Trick: Parade",
                "Avoid damage with a certain probability",
                "作動式: パレード",
                "確率で被ダメージを回避する"
        );
        addSpellPiece(
                root,
                "trick_radiation_filter",
                "Trick: Radiation Filter",
                "Applies a radiation filter effect to the target",
                "作動式: 放射線フィルタ",
                "対象に放射線防護効果を付与する"
        );
        addSpellPiece(
                root,
                "trick_flight",
                "Trick: Flight",
                "Enabling creative flight",
                "作動式: 飛行",
                "クリエイティブ飛行を可能にする"
        );
        addBookPage(
                root,
                "trick_barrier",
                "Applies a barrier effect that reduces incoming damage. It reduces damage taken by (level * 4).",
                "被ダメージを軽減する障壁効果を付与します.(レベル * 4)だけ被ダメージを減少させます."
        );
        addBookPage(
                root,
                "trick_hardening",
                "Applies a hardening effect that limits large incoming damage to a fixed value. Maximum damage taken is capped at (level - 2).",
                "大きな被ダメージを一定値まで抑える硬化効果を付与します. 受ける最大ダメージを(レベル - 2)に抑えます."
        );
        addBookPage(
                root,
                "trick_parade",
                "Applies an effect that evades attacks by chance. It evades attacks with a (62.5 + 7.5 * level)% chance.",
                "確率で攻撃を回避する効果を付与します. (62.5 + 7.5 * レベル) % で攻撃を回避します."
        );
        addBookPage(
                root,
                "trick_flight",
                "Gives the target an effect that enables creative flight.",
                "対象にクリエイティブ飛行を可能にする効果を与えます. "
        );
        addBookPage(
                root,
                "trick_radiation_filter",
                "Applies a radiation protection effect to the target, protecting them from radiation.",
                "対象に放射線防護効果を付与し、放射線の影響から身を守ります."
        );

        return root;
    }

    private void addEffect(JsonObject root, String id, String enUs, String jaJp) {
        root.addProperty("effect.psitweaks." + id, switch (locale) {
            case "ja_jp" -> jaJp;
            default -> enUs;
        });
    }

    private void addChemical(JsonObject root, String id, String enUs, String jaJp) {
        root.addProperty("chemical.psitweaks." + id, switch (locale) {
            case "ja_jp" -> jaJp;
            default -> enUs;
        });
    }

    private void addSpellPiece(JsonObject root, String id, String enUs, String enUsDesc, String jaJp, String jaJpDesc) {
        root.addProperty("psitweaks.spellpiece." + id, switch (locale) {
            case "ja_jp" -> jaJp;
            default -> enUs;
        });
        root.addProperty("psitweaks.spellpiece." + id + ".desc", switch (locale) {
            case "ja_jp" -> jaJpDesc;
            default -> enUsDesc;
        });
    }

    private void addBookPage(JsonObject root, String id, String enUs, String jaJp) {
        root.addProperty("psi.book.page.psitweaks_spellpiece." + id, switch (locale) {
            case "ja_jp" -> jaJp;
            default -> enUs;
        });
    }
}
