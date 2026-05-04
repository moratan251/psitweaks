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
        addAttribute(root, "spell_damage_factor", "Spell Damage Factor", "術式ダメージ倍率");
        root.addProperty("curios.identifier.magic_calculation_area", switch (locale) {
            case "ja_jp" -> "魔法演算領域";
            default -> "Magic Calculation Area";
        });
        root.addProperty("tooltip.psitweaks.spell_magazine.bullets", switch (locale) {
            case "ja_jp" -> "装填済み術式弾: %s/%s";
            default -> "Loaded spell bullets: %s/%s";
        });
        root.addProperty("tooltip.psitweaks.auto_caster_custom_tick.interval", switch (locale) {
            case "ja_jp" -> "詠唱間隔: %s tick";
            default -> "Cast interval: %s ticks";
        });
        root.addProperty("gui.psitweaks.apply", switch (locale) {
            case "ja_jp" -> "適用";
            default -> "Apply";
        });
        root.addProperty("screen.psitweaks.auto_caster_custom_tick", switch (locale) {
            case "ja_jp" -> "オートキャスター設定";
            default -> "Auto Caster Settings";
        });
        root.addProperty("screen.psitweaks.auto_caster_custom_tick.interval", switch (locale) {
            case "ja_jp" -> "詠唱間隔 (tick)";
            default -> "Cast Interval (ticks)";
        });
        root.addProperty("screen.psitweaks.auto_caster_custom_tick.range", switch (locale) {
            case "ja_jp" -> "設定可能範囲: %s - %s";
            default -> "Range: %s - %s";
        });
        root.addProperty("screen.psitweaks.auto_caster_custom_tick.invalid", switch (locale) {
            case "ja_jp" -> "%s から %s の間で入力してください";
            default -> "Enter a value between %s and %s";
        });
        addCadDisassembler(root);
        addUtilityItemsBook(root);
        addMovalSuitBook(root);
        addSpellBulletsBook(root);
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

    private void addAttribute(JsonObject root, String id, String enUs, String jaJp) {
        root.addProperty("attribute.psitweaks." + id, switch (locale) {
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

    private void addCadDisassembler(JsonObject root) {
        root.addProperty("message.psitweaks.cad_disassembler.unsupported_tic_cad", switch (locale) {
            case "ja_jp" -> "このTiCCADは安全に分解できませんでした";
            default -> "Failed to safely disassemble this TiCCAD.";
        });
        root.addProperty("psi.book.page.psitweaks_machine.cad_disassembler", switch (locale) {
            case "ja_jp" -> "CADを解体して部品を回収するための作業台です. CADを持ってスニーク右クリックすると, 装着されている構成部品と装填済みの術式弾を取り出してCADを分解します.$(p)CAD素体を交換するときや, 古い構成から部品を回収したいときに使います.";
            default -> "A workbench for disassembling CADs and recovering their parts. Sneak-right-click it with a CAD to break the CAD into its installed components and loaded spell bullets.$(p)Use it when replacing CAD bodies or salvaging parts from old setups.";
        });
    }

    private void addUtilityItemsBook(JsonObject root) {
        root.addProperty("psi.book.entry.psitweaks_auto_casters", switch (locale) {
            case "ja_jp" -> "術式自動詠唱デバイス";
            default -> "Auto Casters";
        });
        root.addProperty("psi.book.page.psitweaks_item.philosophers_stone", switch (locale) {
            case "ja_jp" -> "再利用可能な触媒で, 金属, ダイヤモンド, 石炭やエンダーパールなどの相互変換レシピを提供します.";
            default -> "A reusable catalyst that provides interconversion recipes for metals, diamonds, coal, ender pearls, and other resources.";
        });
        root.addProperty("psi.book.page.psitweaks_item.spell_magazine", switch (locale) {
            case "ja_jp" -> "最大12個の術式弾を保持し, 使用すると装備中のCADの対応スロットと入れ替えます.$(p)複数の術式弾をまとめて切り替えるための携帯用ロードアウトです.";
            default -> "Stores up to twelve spell bullets and swaps them with the matching slots of your equipped CAD when used.$(p)Use it as a portable loadout carrier when you need to change many bullets at once.";
        });
        root.addProperty("psi.book.page.psitweaks_item.portable_cad_assembler", switch (locale) {
            case "ja_jp" -> "手持ちで使えるCAD組立機です. ブロックを設置せずに組立機画面を開けるため, 拠点外でも簡単にCAD部品や装填済み術式弾を調整できます.";
            default -> "A handheld CAD Assembler. Use it to open an assembler interface without placing the block, making it easy to adjust CAD parts or loaded spell bullets while away from base.";
        });
        root.addProperty("psi.book.page.psitweaks_item.auto_casters.0", switch (locale) {
            case "ja_jp" -> "術式自動詠唱デバイスは, Curiosスロットに装備可能であり, 自動で術式を詠唱する装備です.$(p)キュリオスコントローラを用いることで, サイメタル外装と同じ要領で術式弾を変更できます.";
            default -> "Auto Casters can be equipped in Curios slots and automatically cast spells.$(p)With the Curios Controller, you can change their spell bullets in the same way as psimetal exosuit armor.";
        });
        root.addProperty("psi.book.page.psitweaks_item.auto_casters.1", switch (locale) {
            case "ja_jp" -> "tick型は毎tick詠唱します. カスタムtick型は, アイテム使用で1から1200tickの範囲から詠唱間隔を設定できます.";
            default -> "The tick model fires every tick. The custom tick model lets you set an interval from 1 to 1200 ticks by using the item.";
        });
        root.addProperty("psi.book.page.psitweaks_item.curios_controller", switch (locale) {
            case "ja_jp" -> "魔法演算領域スロットに装備したソケット対応アイテムの選択術式弾スロットを操作します.$(p)術式自動詠唱デバイスを装備しているときに使う, Curios版の外装コントローラです.";
            default -> "Controls the selected bullet slot of socketable items equipped in Magic Calculation Area slots.$(p)Use it while Auto Casters are equipped as the Curios-side counterpart to the exosuit controller.";
        });
        root.addProperty("psi.book.page.psitweaks_item.flash_charm", switch (locale) {
            case "ja_jp" -> "装備者の盲目と暗闇を継続的に解除するCuriosチャームです.$(p)インベントリ内に持っているだけでも機能するため, ディープダークや視界妨害効果への対策になります.";
            default -> "A Curios charm that continually removes Blindness and Darkness from the wearer.$(p)It also works while carried in inventory, making it useful in the Deep Dark and against vision-disrupting effects.";
        });
        root.addProperty("psi.book.page.psitweaks_item.sorcery_booster", switch (locale) {
            case "ja_jp" -> "魔法演算領域スロットに装備し, 術式ダメージを30%増加させます.$(p)$(l:components/psitweaks_magicians_brain)$(o)$(item)魔法師の脳$(0)$(/l) を使う, 戦闘向けの魔法師強化装備です.";
            default -> "Equips in the Magic Calculation Area slot and increases spell damage by 30%.$(p)It is made from a $(l:components/psitweaks_magicians_brain)$(o)$(item)Magician's Brain$(0)$(/l), making it a combat-focused caster upgrade.";
        });
    }

    private void addMovalSuitBook(JsonObject root) {
        root.addProperty("psi.book.entry.psitweaks_moval_suit", switch (locale) {
            case "ja_jp" -> "M.O.V.A.L. スーツ";
            default -> "M.O.V.A.L. Suit";
        });
        root.addProperty("psi.book.page.psitweaks_item.moval_suit.0", switch (locale) {
            case "ja_jp" -> "$(thing)M.O.V.A.L. スーツ$(0) はPsiの外装防具を発展させた高性能な防具です. 防具として高い耐久性と防御性能を持ち, 装着した術式弾を特定の防具イベントで発動できます.";
            default -> "$(thing)M.O.V.A.L. Suit$(0) is a stronger suit derived from Psi's exosuit armor. It has high durability and protection, and it can cast installed spell bullets from specific armor events.";
        });
        root.addProperty("psi.book.page.psitweaks_item.moval_suit.1", switch (locale) {
            case "ja_jp" -> "ヘルメットは外装センサーに対応し, チェストプレートはダメージ時, レギンスはtick時, ブーツはジャンプ時に詠唱します.$(p)各部位は術式ダメージ+10%, Psi回復量+5, 最大Psi量+500を付与します. Psi回復量と最大Psi量はPsi本体のAttributeを使用します.";
            default -> "The helmet accepts exosuit sensors, the chestplate casts on damage, the leggings cast on tick, and the boots cast on jump.$(p)Each piece grants +10% spell damage, +5 Psi regeneration, and +500 maximum Psi. Psi regeneration and maximum Psi use Psi's own attributes.";
        });
    }

    private void addSpellBulletsBook(JsonObject root) {
        root.addProperty("psi.book.entry.psitweaks_spell_bullets", switch (locale) {
            case "ja_jp" -> "強化術式弾";
            default -> "Upgraded Spell Bullets";
        });
        root.addProperty("psi.book.page.psitweaks_spell_bullets.0", switch (locale) {
            case "ja_jp" -> "$(thing)PsiTweaks$(0) は通常の$(l:items/spell_bullet)$(o)$(item)術式弾$(0)$(/l)より上位の強化ティアとして, 改良, 共鳴, 昇華, 覚醒, 超越を追加します.$(p)Psiの術式弾のバリエーション全てにそれぞれ上位Tierが用意されています.";
            default -> "$(thing)PsiTweaks$(0) adds upgraded tiers above the ordinary $(l:items/spell_bullet)$(o)$(item)Spell Bullet$(0)$(/l): Advanced, Resonant, Sublimated, Awakened, and Transcendent.$(p)Every Psi spell bullet variant has upgraded tiers available.";
        });
        root.addProperty("psi.book.page.psitweaks_spell_bullets.1", switch (locale) {
            case "ja_jp" -> "術式弾の強化は前段階の術式弾を4つ必要とするため、非常に高コストです.";
            default -> "Upgrading a spell bullet requires four bullets from the previous tier, making it very expensive.";
        });
        root.addProperty("psi.book.page.psitweaks_spell_bullets.2", switch (locale) {
            case "ja_jp" -> "上位のTierになるほどコスト効率が著しく改善し、強力な魔法を簡単に発動できるようになります.";
            default -> "Higher tiers greatly improve cost efficiency, making powerful spells easier to cast.";
        });
    }
}
