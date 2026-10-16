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

        addConfigTranslations(root);
        addConnectorTranslations(root);
        addFilterCardTranslations(root);
        root.addProperty("creativetabs.psitweaks", "Psi: Tweaks and Additions");
        addMysticalAgricultureTranslations(root);
        for (PsitweaksDatagenBlocks.GeneratedBlock block : PsitweaksDatagenBlocks.blocks()) {
            root.addProperty("block.psitweaks." + block.id(), switch (locale) {
                case "ja_jp" -> block.jaJp();
                default -> block.enUs();
            });
        }
        root.addProperty("block.psitweaks.conjuredpulsar", switch (locale) {
            case "ja_jp" -> "魔法パルサーブロック";
            default -> "Conjured Pulsar";
        });
        root.addProperty("block.psitweaks.conjuredpulsarlight", switch (locale) {
            case "ja_jp" -> "魔法パルサー光源";
            default -> "Conjured Pulsar Light";
        });
        for (PsitweaksDatagenItems.GeneratedItem item : PsitweaksDatagenItems.items()) {
            root.addProperty("item.psitweaks." + item.id(), switch (locale) {
                case "ja_jp" -> item.jaJp();
                default -> item.enUs();
            });
        }
        root.addProperty("item.psitweaks.dry_ice_projectile", switch (locale) {
            case "ja_jp" -> "ドライアイスの飛翔体";
            default -> "Dry Ice Projectile";
        });
        for (ProductiveBeesDataProvider.GeneratedBee bee : ProductiveBeesDataProvider.bees()) {
            root.addProperty("entity.productivebees." + bee.id() + "_bee", switch (locale) {
                case "ja_jp" -> bee.jaJp();
                default -> bee.enUs();
            });
        }
        root.addProperty("entity.minecraft.villager.psitweaks.spellcaster", switch (locale) {
            case "ja_jp" -> "魔法師";
            default -> "Magician";
        });
        root.addProperty("entity.psitweaks.ground_shockwave", switch (locale) {
            case "ja_jp" -> "地雷原の衝撃波";
            default -> "Ground Shockwave";
        });
        root.addProperty("entity.psitweaks.kamaitachi_blade", switch (locale) {
            case "ja_jp" -> "鎌鼬の刃";
            default -> "Kamaitachi Blade";
        });
        root.addProperty("entity.psitweaks.dancing_blade", switch (locale) {
            case "ja_jp" -> "舞う刃";
            default -> "Dancing Blade";
        });
        root.addProperty("entity.psitweaks.starfall_meteor", switch (locale) {
            case "ja_jp" -> "スターフォールの流星";
            default -> "Starfall Meteor";
        });
        root.addProperty("entity.psitweaks.gravity_well", switch (locale) {
            case "ja_jp" -> "重力の井戸";
            default -> "Gravity Well";
        });
        root.addProperty("entity.psitweaks.chain_lightning", switch (locale) {
            case "ja_jp" -> "連鎖雷撃";
            default -> "Chain Lightning";
        });
        addDeathMessages(root);
        root.addProperty("item.psitweaks.gravstringer.mode.normal", switch (locale) {
            case "ja_jp" -> "通常モード";
            default -> "Normal Mode";
        });
        root.addProperty("item.psitweaks.gravstringer.mode.slow", switch (locale) {
            case "ja_jp" -> "低速モード";
            default -> "Slow Mode";
        });
        root.addProperty("item.psitweaks.gravstringer.mode.inertialess", switch (locale) {
            case "ja_jp" -> "無慣性モード";
            default -> "Inertialess Mode";
        });
        root.addProperty("item.psitweaks.gravstringer.mode.hybrid", switch (locale) {
            case "ja_jp" -> "ハイブリッドモード";
            default -> "Hybrid Mode";
        });
        root.addProperty("item.psitweaks.gravstringer.current_mode", switch (locale) {
            case "ja_jp" -> "モード: %s";
            default -> "Mode: %s";
        });
        addChemical(root, "infuse_psigem", "Psigem", "サイジェム");
        addChemical(root, "infuse_ebony", "Ebony", "エボニー");
        addChemical(root, "infuse_ivory", "Ivory", "アイボリー");
        addChemical(root, "infuse_chaotic_factor", "Chaotic Factor", "混沌因子");
        addChemical(root, "infuse_psionic_echo", "Psionic Echo", "サイオニックエコー");
        addChemical(root, "infuse_hypostasis", "Hypostasis Gem", "ヒュポスタシスジェム");
        addChemical(root, "infuse_quantum", "Quantum", "量子");
        addChemical(root, "infuse_graviton", "Graviton", "重力子");
        addChemical(root, "gas_psionic_echo", "Psionic Echo Gas", "気化サイオニックエコー");
        addChemical(root, "gas_peo_fuel", "ΨE-O Fuel", "ΨE-O 燃料");
        addChemical(root, "dirty_antinite", "Dirty Antinite Slurry", "汚れたアンティナイトの懸濁液");
        addChemical(root, "clean_antinite", "Clean Antinite Slurry", "純粋なアンティナイトの懸濁液");
        addAttribute(root, "spell_damage_factor", "Spell Damage Factor", "術式ダメージ倍率");
        addModule(root, "psyon_supplying_unit", "Psyon Supplying Unit", "Increases Psi regeneration.", "サイオン供給ユニット", "ユーザーのPsi回復速度を増加させます.");
        addModule(root, "psyon_capacity_unit", "Psyon Capacity Unit", "Increases maximum Psi.", "サイオン容量ユニット", "ユーザーの最大Psi量を増加させます.");
        addModule(root, "phenomenon_interference_enhancement_unit", "Phenomenon Interference Enhancement Unit", "Increases spell damage.", "事象干渉力増大ユニット", "ユーザーの術式の威力を増大させます.");
        root.addProperty("curios.identifier.magic_calculation_area", switch (locale) {
            case "ja_jp" -> "魔法演算領域";
            default -> "Magic Calculation Area";
        });
        root.addProperty("tooltip.psitweaks.spell_magazine.bullets", switch (locale) {
            case "ja_jp" -> "装填済み術式弾: %s/%s";
            default -> "Loaded spell bullets: %s/%s";
        });
        root.addProperty("tooltip.psitweaks.cad_battery_reactive.bonus", switch (locale) {
            case "ja_jp" -> "CAD所持時: Psi再生速度 +10";
            default -> "While your CAD: +10 Psi Regen";
        });
        root.addProperty("tooltip.psitweaks.cad_battery_quantum.bonus", switch (locale) {
            case "ja_jp" -> "CAD所持時: 最大Psi +1500";
            default -> "While your CAD: +1500 Max Psi";
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
        root.addProperty("psitweaks.spellparam.spellgram", switch (locale) {
            case "ja_jp" -> "魔法式";
            default -> "SpellGram";
        });
        root.addProperty("psitweaks.datatype.string", switch (locale) {
            case "ja_jp" -> "String";
            default -> "String";
        });
        root.addProperty("psitweaks.datatype.string_list", switch (locale) {
            case "ja_jp" -> "String List";
            default -> "String List";
        });
        root.addProperty("psitweaks.datatype.number_list", switch (locale) {
            case "ja_jp" -> "Number List";
            default -> "Number List";
        });
        root.addProperty("psitweaks.datatype.vector_list", switch (locale) {
            case "ja_jp" -> "Vector List";
            default -> "Vector List";
        });
        root.addProperty("psitweaks.datatype.vector_or_number_list", switch (locale) {
            case "ja_jp" -> "Vector/Number List";
            default -> "Vector/Number List";
        });
        root.addProperty("psitweaks.datatype.matrix", switch (locale) {
            case "ja_jp" -> "Matrix";
            default -> "Matrix";
        });
        root.addProperty("psitweaks.datatype.matrix_list", switch (locale) {
            case "ja_jp" -> "Matrix List";
            default -> "Matrix List";
        });
        root.addProperty("psitweaks.datatype.item", switch (locale) {
            case "ja_jp" -> "Item";
            default -> "Item";
        });
        root.addProperty("psitweaks.datatype.item_strict", switch (locale) {
            case "ja_jp" -> "Item (Strict)";
            default -> "Item (Strict)";
        });
        root.addProperty("psitweaks.datatype.item_search", switch (locale) {
            case "ja_jp" -> "String/Item/Item List";
            default -> "String/Item/Item List";
        });
        root.addProperty("psitweaks.datatype.string_or_item", switch (locale) {
            case "ja_jp" -> "String/Item";
            default -> "String/Item";
        });
        root.addProperty("psitweaks.datatype.block", switch (locale) {
            case "ja_jp" -> "Block";
            default -> "Block";
        });
        root.addProperty("psitweaks.datatype.contextual_value", switch (locale) {
            case "ja_jp" -> "Contextual Value";
            default -> "Contextual Value";
        });
        root.addProperty("psitweaks.datatype.contextual_value_list", switch (locale) {
            case "ja_jp" -> "Contextual Value List";
            default -> "Contextual Value List";
        });
        root.addProperty("psitweaks.datatype.block_list", switch (locale) {
            case "ja_jp" -> "Block List";
            default -> "Block List";
        });
        root.addProperty("psitweaks.datatype.item_list", switch (locale) {
            case "ja_jp" -> "Item List";
            default -> "Item List";
        });
        root.addProperty("psitweaks.datatype.any_list", switch (locale) {
            case "ja_jp" -> "Any List";
            default -> "Any List";
        });
        root.addProperty("psitweaks.datatype.plain_value", switch (locale) {
            case "ja_jp" -> "Plain Value";
            default -> "Plain Value";
        });
        root.addProperty("psitweaks.spellparam.string", switch (locale) {
            case "ja_jp" -> "文字列";
            default -> "String";
        });
        root.addProperty("psitweaks.spellparam.string1", switch (locale) {
            case "ja_jp" -> "文字列1";
            default -> "String 1";
        });
        root.addProperty("psitweaks.spellparam.string2", switch (locale) {
            case "ja_jp" -> "文字列2";
            default -> "String 2";
        });
        root.addProperty("psitweaks.spellparam.string3", switch (locale) {
            case "ja_jp" -> "文字列3";
            default -> "String 3";
        });
        root.addProperty("psitweaks.spellparam.value1", switch (locale) {
            case "ja_jp" -> "値1";
            default -> "Value 1";
        });
        root.addProperty("psitweaks.spellparam.value2", switch (locale) {
            case "ja_jp" -> "値2";
            default -> "Value 2";
        });
        root.addProperty("psitweaks.spellparam.value3", switch (locale) {
            case "ja_jp" -> "値3";
            default -> "Value 3";
        });
        root.addProperty("psitweaks.spellparam.element1", switch (locale) {
            case "ja_jp" -> "要素1";
            default -> "Element 1";
        });
        root.addProperty("psitweaks.spellparam.element2", switch (locale) {
            case "ja_jp" -> "要素2";
            default -> "Element 2";
        });
        root.addProperty("psitweaks.spellparam.element3", switch (locale) {
            case "ja_jp" -> "要素3";
            default -> "Element 3";
        });
        root.addProperty("psitweaks.spellparam.matrix1", switch (locale) {
            case "ja_jp" -> "行列1";
            default -> "Matrix 1";
        });
        root.addProperty("psitweaks.spellparam.matrix2", switch (locale) {
            case "ja_jp" -> "行列2";
            default -> "Matrix 2";
        });
        root.addProperty("psitweaks.spellparam.matrix3", switch (locale) {
            case "ja_jp" -> "行列3";
            default -> "Matrix 3";
        });
        root.addProperty("psitweaks.spellparam.matrix", switch (locale) {
            case "ja_jp" -> "行列";
            default -> "Matrix";
        });
        root.addProperty("psitweaks.spellparam.vector_or_number_list", switch (locale) {
            case "ja_jp" -> "数列";
            default -> "Array";
        });
        root.addProperty("psitweaks.spellparam.size", switch (locale) {
            case "ja_jp" -> "サイズ";
            default -> "Size";
        });
        root.addProperty("psitweaks.spellparam.indices", switch (locale) {
            case "ja_jp" -> "成分";
            default -> "Indices";
        });
        root.addProperty("psitweaks.spellparam.item", switch (locale) {
            case "ja_jp" -> "Item";
            default -> "Item";
        });
        root.addProperty("psitweaks.spellparam.tag", switch (locale) {
            case "ja_jp" -> "タグ";
            default -> "Tag";
        });
        root.addProperty("psitweaks.spellparam.filter", switch (locale) {
            case "ja_jp" -> "フィルタ";
            default -> "Filter";
        });
        root.addProperty("psitweaks.spellparam.resource", switch (locale) {
            case "ja_jp" -> "資源";
            default -> "Resource";
        });
        root.addProperty("psitweaks.spellparam.leftright", switch (locale) {
            case "ja_jp" -> "左右";
            default -> "Left/Right";
        });
        root.addProperty("psitweaks.spellparam.forwardbackward", switch (locale) {
            case "ja_jp" -> "前後";
            default -> "Forward/Backward";
        });
        root.addProperty("psitweaks.spellparam.updown", switch (locale) {
            case "ja_jp" -> "上下";
            default -> "Up/Down";
        });
        root.addProperty("psitweaks.spellparam.offset", switch (locale) {
            case "ja_jp" -> "オフセット";
            default -> "Offset";
        });
        root.addProperty("psitweaks.spellparam.max", switch (locale) {
            case "ja_jp" -> "最大";
            default -> "Max";
        });
        root.addProperty("psitweaks.spellparam.label", switch (locale) {
            case "ja_jp" -> "ラベル";
            default -> "Label";
        });
        root.addProperty("psitweaks.spellparam.chains", switch (locale) {
            case "ja_jp" -> "連鎖数";
            default -> "Chains";
        });
        root.addProperty("psitweaks.spellparam.count", switch (locale) {
            case "ja_jp" -> "数";
            default -> "Count";
        });
        root.addProperty("psitweaks.spellerror.no_ground", switch (locale) {
            case "ja_jp" -> "指定位置の下に地面がありません";
            default -> "No ground below the position";
        });
        root.addProperty("psitweaks.spellerror.no_jump_anchor", switch (locale) {
            case "ja_jp" -> "前方に一致するジャンプアンカーがありません";
            default -> "No matching Jump Anchor found ahead";
        });
        root.addProperty("psitweaks.spellerror.nullitem", switch (locale) {
            case "ja_jp" -> "エラー: 魔法の対象となるアイテムは存在しません。";
            default -> "Error: The target item of the spell does not exist.";
        });
        root.addProperty("psitweaks.spellerror.accessdenyed", switch (locale) {
            case "ja_jp" -> "エラー: 保護されたブロックにアクセスを試みました。";
            default -> "Error: Attempted to access a protected block.";
        });
        root.addProperty("psitweaks.spellerror.safety_insufficient_psi", switch (locale) {
            case "ja_jp" -> "安全装置が作動しました: Psiが不足しています";
            default -> "Safety activated: not enough Psi";
        });
        root.addProperty("psitweaks.spellerror.plain_memory_type", switch (locale) {
            case "ja_jp" -> "保存された値を選択中の型として読み取れません";
            default -> "Stored value cannot be read as the selected type";
        });
        root.addProperty("psitweaks.spellerror.plain_memory_parse", switch (locale) {
            case "ja_jp" -> "保存された文字列を選択中の型へ変換できません";
            default -> "Stored string cannot be converted to the selected type";
        });
        root.addProperty("psitweaks.spellerror.empty_delimiter", switch (locale) {
            case "ja_jp" -> "区切り文字が空です";
            default -> "Delimiter cannot be empty";
        });
        root.addProperty("psitweaks.spellerror.expected_three_numbers", switch (locale) {
            case "ja_jp" -> "Number List の要素数は3である必要があります";
            default -> "Number List must contain exactly 3 elements";
        });
        root.addProperty("psitweaks.spellerror.matrix_incompatible_sizes", switch (locale) {
            case "ja_jp" -> "行列のサイズが整合しません";
            default -> "Matrix dimensions are incompatible";
        });
        root.addProperty("psitweaks.spellerror.matrix_not_square", switch (locale) {
            case "ja_jp" -> "行列は正方行列である必要があります";
            default -> "Matrix must be square";
        });
        root.addProperty("psitweaks.spellerror.matrix_singular", switch (locale) {
            case "ja_jp" -> "行列が特異であり逆行列を求められません";
            default -> "Matrix is singular and cannot be inverted";
        });
        root.addProperty("psitweaks.spellerror.matrix_out_of_bounds", switch (locale) {
            case "ja_jp" -> "行列のインデックスが範囲外です";
            default -> "Matrix index is out of bounds";
        });
        root.addProperty("psitweaks.spellerror.matrix_too_large", switch (locale) {
            case "ja_jp" -> "行列またはリストが最大サイズ4を超えています";
            default -> "Matrix or list exceeds the maximum size of 4";
        });
        root.addProperty("psitweaks.spellerror.matrix_invalid_dimension", switch (locale) {
            case "ja_jp" -> "行列の次元は1〜4である必要があります";
            default -> "Matrix dimension must be between 1 and 4";
        });
        root.addProperty("psitweaks.spellerror.matrix_invalid_transform", switch (locale) {
            case "ja_jp" -> "変換行列は3×3または4×4である必要があります";
            default -> "Transform matrix must be 3x3 or 4x4";
        });
        root.addProperty("psitweaks.spellerror.matrix_non_finite_result", switch (locale) {
            case "ja_jp" -> "行列演算の結果が有限値ではありません";
            default -> "Matrix operation produced a non-finite result";
        });
        root.addProperty("psitweaks.spellerror.matrix_zero_w", switch (locale) {
            case "ja_jp" -> "変換後のw成分が0です";
            default -> "Transformed w component is zero";
        });
        root.addProperty("psitweaks.spellerror.region_invalid_matrix", switch (locale) {
            case "ja_jp" -> "領域演算には3×4または4×4の行列が必要です";
            default -> "Region operations require a 3x4 or 4x4 matrix";
        });
        root.addProperty("psitweaks.spellerror.region_degenerate_edges", switch (locale) {
            case "ja_jp" -> "3つの辺ベクトルが退化しています";
            default -> "The three edge vectors are degenerate";
        });
        root.addProperty("psitweaks.spellerror.region_too_large", switch (locale) {
            case "ja_jp" -> "領域に含まれるブロックが多すぎます";
            default -> "The region contains too many blocks";
        });
        root.addProperty("psitweaks.spellerror.mass_break_too_many_pending", switch (locale) {
            case "ja_jp" -> "保留中の一括破壊ブロック数が上限を超えています";
            default -> "Too many pending mass-break blocks";
        });
        root.addProperty("psitweaks.spellwarning.cad_memory_string_truncated", switch (locale) {
            case "ja_jp" -> "CADメモリスロット%sのStringは%s文字から%s文字に切り捨てられました";
            default -> "String CAD memory value in slot %s was truncated from %s to %s characters";
        });
        addPsitweaksCategoryBook(root);
        addProgrammingConceptsBook(root);
        addMachineTranslations(root);
        addSpellUnlockMessages(root);
        addCadDisassembler(root);
        addUtilityItemsBook(root);
        addMovalSuitBook(root);
        addMekanismIntegrationBook(root);
        addMaterialBookAdditions(root);
        addSpellBulletsBook(root);
        addEffect(root, "parade", "Parade", "仮想行列");
        addEffect(root, "flight", "Flight", "飛行");
        addEffect(root, "barrier", "Barrier", "障壁");
        addEffect(root, "hardening", "Hardening", "硬化");
        addEffect(root, "radiation_filter", "Radiation Filter", "放射線フィルタ");
        addEffect(root, "carbon_poisoning", "Carbon Poisoning", "炭素中毒");

        addSpellPieces(root);
        addSpellPiecesBook(root);
        addLegacyPatchouliBookTranslations(root);

        return root;
    }

    private void addMysticalAgricultureTranslations(JsonObject root) {
        String[][] crops = {
                {"psidust", "Psidust", "サイダスト"},
                {"psimetal", "Psimetal", "サイメタル"},
                {"ebony_psimetal", "Ebony Psimetal", "エボニーサイメタル"},
                {"ivory_psimetal", "Ivory Psimetal", "アイボリーサイメタル"},
                {"psigem", "Psigem", "サイジェム"},
                {"chaotic_psimetal", "Chaotic Psimetal", "カオティックサイメタル"},
                {"flashmetal", "Flashmetal", "フラッシュメタル"},
                {"heavy_psimetal", "Heavy Psimetal", "ヘビーサイメタル"},
                {"psycheonic_metal", "Psycheonic Metal", "プシオニックメタル"},
                {"antinite", "Antinite", "アンティナイト"}
        };
        for (String[] crop : crops) {
            root.addProperty("crop.psitweaks." + crop[0], locale.equals("ja_jp") ? crop[2] : crop[1]);
        }
    }

    private void addConfigTranslations(JsonObject root) {
        addConfigLabel(root, "psitweaks.configuration.title",
                "Psi: Tweaks and Additions Configuration", "Psi: Tweaks and Additions 設定");
        addConfigLabel(root, "psitweaks.configuration.section.psitweaks.common.toml",
                "Common Configuration", "共通設定");
        addConfigLabel(root, "psitweaks.configuration.section.psitweaks.common.toml.title",
                "Common Configuration", "共通設定");
        addConfigLabel(root, "psitweaks.configuration.section.psitweaks.client.toml",
                "Client Configuration", "クライアント設定");
        addConfigLabel(root, "psitweaks.configuration.section.psitweaks.client.toml.title",
                "Client Configuration", "クライアント設定");
        addConfigLabel(root, "psitweaks.configuration.effects", "Spell Effects", "術式エフェクト");
        addConfigValue(root, "psitweaks.configuration.effects.camera_shake",
                "Camera Shake", "画面の揺れ",
                "Shake the camera for ground shockwaves, meteor impacts and gravity collapses.",
                "地雷原の衝撃波、流星の着弾、重力崩壊などで画面を揺らします。");
        addConfigValue(root, "psitweaks.configuration.effects.camera_shake_strength",
                "Camera Shake Strength", "画面の揺れの強さ",
                "Strength of the camera shake. 1.0 is the default.",
                "画面の揺れの強さです。1.0が標準です。");
        addConfigValue(root, "psitweaks.configuration.effects.barrier_hud",
                "Barrier Shield Bar", "障壁シールドのバー",
                "Show the remaining shield of Trick: Barrier above the health bar.",
                "作動式: 障壁のシールド残量を体力ゲージの上に表示します。");

        addConfigLabel(root, "psitweaks.configuration.spells", "Spell Settings", "スペル設定");
        addConfigValue(root, "psitweaks.configuration.spells.global_spell_power_multiplier",
                "Global Offensive Spell Multiplier", "攻撃術式の全体倍率",
                "Multiplier applied to all offensive spells added by PsiTweaks.",
                "PsiTweaksが追加する全攻撃術式に適用する倍率です。");
        addConfigLabel(root, "psitweaks.configuration.spells.unlocks",
                "Spell Unlock Settings", "スペル解禁設定");
        addConfigValue(root, "psitweaks.configuration.spells.unlocks.require_spell_unlocks",
                "Require Spell Unlocks", "スペル解禁を要求",
                "Require configured spells to be unlocked before they can be used in the Spell Programmer.",
                "対象スペルをスペルプログラマーで使用する前に、解禁を必要とするか設定します。");

        addDamageMultiplierConfig(root, "molecular_divider", "Molecular Divider", "分子ディバイダー");
        addDamageMultiplierConfig(root, "phonon_maser", "Phonon Maser", "フォノンメーザー");
        addDamageMultiplierConfig(root, "aqua_cutter", "Aqua Cutter", "アクアカッター");
        addDamageMultiplierConfig(root, "dry_meteor", "Dry Meteor", "ドライミーティア");
        addDamageMultiplierConfig(root, "blaze_ball", "Blaze Ball", "ブレイズボール");
        addDamageMultiplierConfig(root, "active_air_mine", "Active Air Mine", "能動空中機雷");
        addDamageMultiplierConfig(root, "flare_circle", "Flare Circle", "フレアサークル");
        addDamageMultiplierConfig(root, "ice_circle", "Ice Circle", "アイスサークル");
        addDamageMultiplierConfig(root, "guillotine", "Guillotine", "ギロチン");
        addDamageMultiplierConfig(root, "chain_lightning", "Chain Lightning", "連鎖雷撃");
        addDamageMultiplierConfig(root, "mine_genesis", "Mine Genesis", "地雷原");
        addDamageMultiplierConfig(root, "burst", "Burst", "爆裂");
        addDamageMultiplierConfig(root, "kamaitachi", "Kamaitachi", "鎌鼬");
        addDamageMultiplierConfig(root, "dancing_blades", "Dancing Blades", "ダンシングブレイズ");
        addDamageMultiplierConfig(root, "starfall", "Starfall", "スターフォール");
        addDamageMultiplierConfig(root, "gravity_collapse", "Gravity Collapse", "重力崩壊");
        addConfigLabel(root, "psitweaks.configuration.spells.barrier", "Barrier", "障壁");
        addConfigValue(root, "psitweaks.configuration.spells.barrier.shield_multiplier",
                "Shield Multiplier", "シールド量倍率",
                "Multiplier applied to the shield of Trick: Barrier (10 per point of Power).",
                "作動式: 障壁のシールド量 (威力1につき10) に適用する倍率です。");

        addConfigLabel(root, "psitweaks.configuration.spells.radiation_injection",
                "Radiation Injection", "放射線注入");
        addConfigValue(root, "psitweaks.configuration.spells.radiation_injection.radiation_multiplier",
                "Radiation Multiplier", "放射線量倍率",
                "Multiplier applied to the radiation amount caused by Radiation Injection.",
                "放射線注入による被ばく量に適用する倍率です。");

        addConfigLabel(root, "psitweaks.configuration.items", "Item Settings", "アイテム設定");
        addConfigLabel(root, "psitweaks.configuration.items.gravstringer",
                "Gravstringer", "グラヴストリンガー");
        addConfigValue(root, "psitweaks.configuration.items.gravstringer.base_damage",
                "Base Damage", "基礎ダメージ",
                "Base damage assigned to arrows fired by the Gravstringer.",
                "グラヴストリンガーが発射する矢に設定する基礎ダメージです。");
        addConfigLabel(root, "psitweaks.configuration.items.tunneler",
                "Tunneler", "トンネラー");
        addConfigValue(root, "psitweaks.configuration.items.tunneler.minimum_damage",
                "Minimum Damage", "最低ダメージ",
                "Minimum damage dealt when a Tunneler hits an entity.",
                "トンネラーがエンティティへ命中した際に与える最低ダメージです。");

        addConfigLabel(root, "psitweaks.configuration.psi", "Psi Behavior", "Psi基本挙動");
        addConfigValue(root, "psitweaks.configuration.psi.disable_damage_psi_deduction",
                "Disable Psi Loss on Damage", "被ダメージ時のPsi減少を無効化",
                "Prevent taking damage from reducing Psi.", "ダメージを受けた際のPsi減少を無効にします。");
        addConfigValue(root, "psitweaks.configuration.psi.disable_regen_cooldown",
                "Disable Psi Regeneration Cooldown", "Psi回復クールダウンを無効化",
                "Prevent Psi consumption from applying a regeneration cooldown.",
                "Psiを消費した際の回復クールダウンを無効にします。");

        addConfigLabel(root, "psitweaks.configuration.mekanism", "Mekanism Integration", "Mekanism連携");
        addConfigValue(root, "psitweaks.configuration.mekanism.gas_burning_generator_energy_capacity",
                "Gas-Burning Generator Energy Capacity", "ガス燃焼発電機の内部エネルギー容量",
                "Override the internal energy capacity in Joules. Set to -1 to use Mekanism's default behavior.",
                "内部エネルギー容量をJ単位で上書きします。-1にするとMekanismの標準挙動を使用します。");

        addConfigLabel(root, "psitweaks.configuration.idea_storage", "Ideaspace Storage", "イデアストレージ");
        addConfigValue(root, "psitweaks.configuration.idea_storage.max_item_types",
                "Max Item Types", "アイテム最大種類数",
                "Maximum number of distinct item types per player in the Ideaspace Storage.",
                "イデアストレージに格納できるアイテムの最大種類数です。");
        addConfigValue(root, "psitweaks.configuration.idea_storage.item_stacks_per_type",
                "Item Stacks per Type", "1種類あたりのスタック数係数",
                "Per-type item limit as a multiplier of the item's max stack size.",
                "アイテム1種類あたりの上限を、最大スタック数に掛ける係数として設定します。");
        addConfigValue(root, "psitweaks.configuration.idea_storage.max_fluid_types",
                "Max Fluid Types", "液体最大種類数",
                "Maximum number of distinct fluid types per player in the Ideaspace Storage.",
                "イデアストレージに格納できる液体の最大種類数です。");
        addConfigValue(root, "psitweaks.configuration.idea_storage.max_fluid_per_type",
                "Max Fluid per Type", "液体1種類あたりの上限",
                "Maximum amount stored per fluid type. The config value uses internal mB units; the default is 1,048,576 B.",
                "液体1種類あたりに格納できる上限です。コンフィグ値は内部mB単位で、既定値は1,048,576 Bです。");
        addConfigValue(root, "psitweaks.configuration.idea_storage.max_chemical_types",
                "Max Chemical Types", "Chemical最大種類数",
                "Maximum number of distinct chemical types per player in the Ideaspace Storage.",
                "イデアストレージに格納できるChemicalの最大種類数です。");
        addConfigValue(root, "psitweaks.configuration.idea_storage.max_energy",
                "Max FE", "FE上限", "Maximum FE stored per player.", "プレイヤーごとに保存できるFEの上限です。");
        addConfigValue(root, "psitweaks.configuration.idea_storage.max_chemical_per_type",
                "Max Chemical per Type", "Chemical 1種類あたりの上限",
                "Maximum amount stored per chemical type. The config value uses internal mB units; the default is 33,554,432 B.",
                "Chemical 1種類あたりに格納できる上限です。コンフィグ値は内部mB単位で、既定値は33,554,432 Bです。");
    }

    private void addDamageMultiplierConfig(JsonObject root, String id, String enUsName, String jaJpName) {
        String key = "psitweaks.configuration.spells." + id;
        addConfigLabel(root, key, enUsName, jaJpName);
        addConfigValue(root, key + ".damage_multiplier", "Damage Multiplier", "ダメージ倍率",
                "Multiplier applied to " + enUsName + " damage.",
                jaJpName + "のダメージに適用する倍率です。");
    }

    private void addConfigValue(JsonObject root, String key, String enUs, String jaJp,
                                String enUsTooltip, String jaJpTooltip) {
        addConfigLabel(root, key, enUs, jaJp);
        addConfigLabel(root, key + ".tooltip", enUsTooltip, jaJpTooltip);
    }

    private void addConfigLabel(JsonObject root, String key, String enUs, String jaJp) {
        root.addProperty(key, switch (locale) {
            case "ja_jp" -> jaJp;
            default -> enUs;
        });
    }

    private void addLegacyPatchouliBookTranslations(JsonObject root) {
        String[][] translations = switch (locale) {
            case "ja_jp" -> LEGACY_PATCHOULI_BOOK_JA_JP;
            default -> LEGACY_PATCHOULI_BOOK_EN_US;
        };
        for (String[] entry : translations) {
            root.addProperty(entry[0], entry[1]);
        }
    }

    private void addDeathMessages(JsonObject root) {
        root.addProperty("death.attack.psitweaks.meteor_line", switch (locale) {
            case "ja_jp" -> "%1$sは光に透過された";
            default -> "%1$s was permeated by light";
        });
        root.addProperty("death.attack.psitweaks.meteor_line.player", switch (locale) {
            case "ja_jp" -> "%1$sは光に透過された";
            default -> "%1$s was permeated by light";
        });
        root.addProperty("death.attack.psitweaks.meteor_line.item", switch (locale) {
            case "ja_jp" -> "%1$sは光に透過された";
            default -> "%1$s was permeated by light";
        });
        root.addProperty("death.attack.psitweaks.carbon_poisoning", switch (locale) {
            case "ja_jp" -> "%1$sは炭素中毒に屈した";
            default -> "%1$s succumbed to carbon poisoning";
        });
        root.addProperty("death.attack.psitweaks.tunneler", switch (locale) {
            case "ja_jp" -> "%1$sはトンネル効果を体感した";
            default -> "%1$s experienced the tunneling effect";
        });
        root.addProperty("death.attack.psitweaks.tunneler.player", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sにトンネル効果を体感させられた";
            default -> "%1$s was made to experience the tunneling effect by %2$s";
        });
        root.addProperty("death.attack.psitweaks.tunneler.item", switch (locale) {
            case "ja_jp" -> "%1$sはトンネル効果を体感した";
            default -> "%1$s experienced the tunneling effect";
        });
        // Combat tricks: the source always has the caster (or the spell entity) as attacker, so %2$s is filled.
        root.addProperty("death.attack.psitweaks.chain_lightning", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sの連鎖雷撃に撃たれた";
            default -> "%1$s was struck by %2$s's chain lightning";
        });
        root.addProperty("death.attack.psitweaks.chain_lightning.player", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sの連鎖雷撃に撃たれた";
            default -> "%1$s was struck by %2$s's chain lightning";
        });
        root.addProperty("death.attack.psitweaks.chain_lightning.item", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sが%3$sで放った連鎖雷撃に撃たれた";
            default -> "%1$s was struck by chain lightning cast by %2$s using %3$s";
        });
        root.addProperty("death.attack.psitweaks.mine_genesis", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sの地雷原に吹き飛ばされた";
            default -> "%1$s was blown off their feet by %2$s's Mine Genesis";
        });
        root.addProperty("death.attack.psitweaks.mine_genesis.player", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sの地雷原に吹き飛ばされた";
            default -> "%1$s was blown off their feet by %2$s's Mine Genesis";
        });
        root.addProperty("death.attack.psitweaks.mine_genesis.item", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sが%3$sで起こした地雷原に吹き飛ばされた";
            default -> "%1$s was blown off their feet by Mine Genesis cast by %2$s using %3$s";
        });
        root.addProperty("death.attack.psitweaks.burst", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sの爆裂で内側から弾け飛んだ";
            default -> "%1$s was blown apart from within by %2$s";
        });
        root.addProperty("death.attack.psitweaks.burst.player", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sの爆裂で内側から弾け飛んだ";
            default -> "%1$s was blown apart from within by %2$s";
        });
        root.addProperty("death.attack.psitweaks.burst.item", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sが%3$sで放った爆裂で内側から弾け飛んだ";
            default -> "%1$s was blown apart from within by %2$s using %3$s";
        });
        root.addProperty("death.attack.psitweaks.kamaitachi", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sの鎌鼬に切り裂かれた";
            default -> "%1$s was sliced by %2$s's Kamaitachi";
        });
        root.addProperty("death.attack.psitweaks.kamaitachi.player", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sの鎌鼬に切り裂かれた";
            default -> "%1$s was sliced by %2$s's Kamaitachi";
        });
        root.addProperty("death.attack.psitweaks.kamaitachi.item", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sが%3$sで放った鎌鼬に切り裂かれた";
            default -> "%1$s was sliced by Kamaitachi cast by %2$s using %3$s";
        });
        root.addProperty("death.attack.psitweaks.dancing_blades", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sの舞う刃に斬り伏せられた";
            default -> "%1$s was cut down by %2$s's dancing blades";
        });
        root.addProperty("death.attack.psitweaks.dancing_blades.player", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sの舞う刃に斬り伏せられた";
            default -> "%1$s was cut down by %2$s's dancing blades";
        });
        root.addProperty("death.attack.psitweaks.dancing_blades.item", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sが%3$sで呼んだ舞う刃に斬り伏せられた";
            default -> "%1$s was cut down by dancing blades summoned by %2$s using %3$s";
        });
        root.addProperty("death.attack.psitweaks.starfall", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sが呼んだ流星に押し潰された";
            default -> "%1$s was crushed by a meteor called down by %2$s";
        });
        root.addProperty("death.attack.psitweaks.starfall.player", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sが呼んだ流星に押し潰された";
            default -> "%1$s was crushed by a meteor called down by %2$s";
        });
        root.addProperty("death.attack.psitweaks.starfall.item", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sが%3$sで呼んだ流星に押し潰された";
            default -> "%1$s was crushed by a meteor %2$s called down using %3$s";
        });
        root.addProperty("death.attack.psitweaks.gravity_collapse", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sの重力崩壊に押し潰された";
            default -> "%1$s was crushed by %2$s's gravity collapse";
        });
        root.addProperty("death.attack.psitweaks.gravity_collapse.player", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sの重力崩壊に押し潰された";
            default -> "%1$s was crushed by %2$s's gravity collapse";
        });
        root.addProperty("death.attack.psitweaks.gravity_collapse.item", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sが%3$sで起こした重力崩壊に押し潰された";
            default -> "%1$s was crushed by a gravity collapse %2$s caused using %3$s";
        });
    }

    private static final String[][] LEGACY_PATCHOULI_BOOK_EN_US = {
            {"psi.book.category.psitweaks", "PsiTweaks"},
            {"psi.book.category.psitweaks.desc", "$(thing)PsiTweaks$(0) expands Psi with stronger spell bullets, CAD parts, machinery, Mekanism integration, and late-game psionic materials."},
            {"psi.book.category.psitweaks_machines", "Machines"},
            {"psi.book.category.psitweaks_machines.desc", "Machine blocks and infrastructure added by PsiTweaks."},
            {"psi.book.category.psitweaks_spell_pieces", "Additional Spell Pieces"},
            {"psi.book.category.psitweaks_spell_pieces.desc", "Spell pieces added by PsiTweaks. Many are offensive, industrial, or utility-focused, and some require research depending on the config."},
            {"psi.book.entry.psitweaks_auto_casters", "Auto Casters"},
            {"psi.book.entry.psitweaks_cad_and_gear", "CADs and Gear"},
            {"psi.book.entry.psitweaks_inline_casters", "Inline Casters"},
            {"psi.book.entry.psitweaks_magician", "Magician"},
            {"psi.book.entry.psitweaks_mekanism_integration", "Mekanism Integration"},
            {"psi.book.entry.psitweaks_moval_suit", "M.O.V.A.L. Suit"},
            {"psi.book.entry.psitweaks_overview", "What PsiTweaks Adds"},
            {"psi.book.entry.psitweaks_research", "Research"},
            {"psi.book.entry.psitweaks_spell_bullets", "Upgraded Spell Bullets"},
            {"psi.book.page.psitweaks_cad_and_gear.0", "Several PsiTweaks items make spellcasting easier to carry, automate, or specialize. Some are direct CAD tools, while others support bullet handling or improve a caster's combat role."},
            {"psi.book.page.psitweaks_cad_and_gear.2", "PsiTweaks also adds powerful CAD assemblies made from expensive ingots. When ordinary Psi equipment no longer keeps up, use them together with high-tier spell bullets and similar upgrades."},
            {"psi.book.page.psitweaks_cad_and_gear.3", "$(thing)PsiTweaks$(0) adds five CAD material lines: $(l:components/psitweaks_alloy_psion)Psionic Alloy$(/l), $(l:components/psitweaks_chaotic_psimetal)Chaotic Psimetal$(/l), $(l:components/psitweaks_flashmetal)Flashmetal$(/l), $(l:components/psitweaks_heavy_psimetal)Heavy Psimetal$(/l), and $(l:components/psitweaks_psycheonic_metal_ingot)Psycheonic Metal$(/l).$(p)Psionic Alloy CADs have excellent Efficiency but very small Potency. The other CAD assemblies improve in Efficiency and Potency as their materials move into later progression."},
            {"psi.book.page.psitweaks_item.blank_program", "Used as the base item for program research. Put it into the $(l:psitweaks_machines/program_researcher)$(o)$(item)Program Research Table$(0)$(/l) with the required materials to create a written program.$(p)It can also be crafted with an existing written program item to duplicate that program."},
            {"psi.book.page.psitweaks_item.flash_charm", "A Curios charm that continually removes Blindness and Darkness from the wearer.$(p)It also works while carried in inventory, making it useful in the Deep Dark and against vision-disrupting effects."},
            {"psi.book.page.psitweaks_item.inline_casters.0", "Inline, Secondary, and Parallel Casters are handheld spellcasting tools with their own bullet sockets. They still require the caster to have a CAD, but the selected bullet is stored in the caster item itself."},
            {"psi.book.page.psitweaks_item.inline_casters.1", "The Inline Caster has one slot, the Secondary Caster has five, and the Parallel Caster has eleven.$(p)Use them when you want to carry many spell bullets without constantly changing the CAD."},
            {"psi.book.page.psitweaks_item.philosophers_stone", "A reusable catalyst that provides interconversion recipes for metals, diamonds, coal, ender pearls, and other resources."},
            {"psi.book.page.psitweaks_item.portable_cad_assembler", "A handheld CAD Assembler. Use it to open an assembler interface without placing the block, making it easy to adjust CAD parts or loaded spell bullets while away from base."},
            {"psi.book.page.psitweaks_item.third_eye_device", "Equips in the Magic Calculation Area slot and removes Psi's normal spell radius check for the caster.$(p)This lets spells target positions far beyond ordinary CAD range limits, so use it carefully."},
            {"psi.book.page.psitweaks_machine.material_mutator", "A Mekanism injecting-style machine that performs material mutation with $(item)Psionic Echo Gas$(0) and energy.$(p)It automates mutations that can also be produced by $(l:psitweaks_spell_pieces/trick_material_mutation)$(o)Trick: Material Mutation$(0)$(/l), such as $(l:components/psitweaks_jade)Jade$(/l) and $(l:components/psitweaks_hypostasis_gem)Hypostasis Gems$(/l)."},
            {"psi.book.page.psitweaks_machine.program_researcher", "A powered research table for producing PsiTweaks program items. Put the required ingredients in the input slots and supply FE; completed research outputs the program item.$(p)JEI shows each research recipe's energy usage per tick and time."},
            {"psi.book.page.psitweaks_machine.psionic_generator", "A generator that links to its Mekanism owner and converts that player's Psi into energy while the owner is online.$(p)Use the GUI to enable the link and set the Psi consumed per tick. Higher consumption gives higher output and drains the owner faster."},
            {"psi.book.page.psitweaks_machine.spellmachinery_casing", "A casing block used to execute advanced magic through machinery."},
            {"psi.book.page.psitweaks_magician.2", "A Magician is also the source of the $(l:components/psitweaks_magicians_brain)$(o)$(item)Magician's Brain$(0)$(/l). It drops when a Magician villager is killed by $(l:psitweaks_spell_pieces/trick_guillotine)$(o)Trick: Guillotine$(0)$(/l), and is used in caster-focused recipes."},
            {"psi.book.page.psitweaks_material.alloy_hypostasis", "Made by infusing $(l:components/psitweaks_alloy_psionic_echo)$(o)$(item)Echo Alloy$(0)$(/l) with Hypostasis.$(p)Used for $(l:components/psitweaks_hypostasis_control_circuit)$(o)$(item)Hypostasis Control Circuits$(0)$(/l) and high-tier psionic machinery."},
            {"psi.book.page.psitweaks_material.alloy_psion", "Made by infusing Mekanism's Atomic Alloy with Psigem.$(p)Used for $(l:components/psitweaks_psionic_control_circuit)$(o)$(item)Psionic Control Circuits$(0)$(/l), Psionic Alloy CAD assemblies, and the next alloy tier."},
            {"psi.book.page.psitweaks_material.alloy_psionic_echo", "Made by infusing $(l:components/psitweaks_alloy_psion)$(o)$(item)Psionic Alloy$(0)$(/l) with $(l:components/psitweaks_psionic_echo)Psionic Echo$(/l).$(p)Used for $(l:components/psitweaks_echo_control_circuit)$(o)$(item)Echo Control Circuits$(0)$(/l) and as the base for $(l:components/psitweaks_alloy_hypostasis)Hypostasis Alloy$(/l)."},
            {"psi.book.page.psitweaks_material.antinite_ingot", "Smelted from Antinite ore or recovered from Antinite blocks. The dust, shard, clump, crystal, and dirty dust forms are intermediate products in Mekanism-style ore processing.$(p)Used as a material for $(l:components/psitweaks_hypostasis_gem)Hypostasis Gems$(/l) and the $(l:items/psitweaks_philosophers_stone)$(o)$(item)Philosopher's Stone$(0)$(/l)."},
            {"psi.book.page.psitweaks_material.chaotic_factor", "$(item)Chaotic Factor$(0) starts with infusing an $(item)Ender Pearl$(0) with Psigem to make $(item)Psionic Factor$(0). Infuse that with Ivory or Ebony to make the aligned factor variants, then cross the opposite alignment to create $(item)Chaotic Factor$(0).$(p)Used as the infusion source for $(l:components/psitweaks_chaotic_psimetal)$(o)$(item)Chaotic Psimetal$(0)$(/l), the first major PsiTweaks metal above ordinary Psimetal."},
            {"psi.book.page.psitweaks_material.chaotic_psimetal", "Made by infusing ordinary $(item)Psimetal$(0) with $(l:components/psitweaks_chaotic_factor)Chaotic Factor$(/l).$(p)Used in recipes for Chaotic Psimetal CAD assemblies and $(l:components/psitweaks_unrefined_flashmetal)$(o)$(item)Unrefined Flashmetal$(0)$(/l)."},
            {"psi.book.page.psitweaks_material.dimensional_crystal", "Occasionally, casting a spell yields a $(item)Dimensional Crystal$(0). The chance increases in proportion to the amount of Psi the cast consumes.$(p)It cannot be crafted. If you want to gather them steadily, you need to make spellcasting part of your daily life."},
            {"psi.book.page.psitweaks_material.echo_control_circuit", "Crafted from $(l:components/psitweaks_psionic_control_circuit)$(o)$(item)Psionic Control Circuit$(0)$(/l), $(l:components/psitweaks_alloy_psionic_echo)$(o)$(item)Echo Alloy$(0)$(/l), and $(l:components/psitweaks_echo_sheet)HDΨE Sheets$(/l).$(p)Used in recipes for advanced spell bullets and machinery."},
            {"psi.book.page.psitweaks_material.echo_pellet", "A compact HDΨE material made from $(l:components/psitweaks_psionic_echo)Psionic Echo$(/l) processing.$(p)Used where recipes need condensed echo material rather than raw $(l:components/psitweaks_psionic_echo)Psionic Echo$(/l). The ΨE-O Fuel produced as a byproduct can be used as fuel for the Gas Generator."},
            {"psi.book.page.psitweaks_material.echo_sheet", "Made by enriching HDΨE material into a sheet form.$(p)Used heavily in Echo and Hypostasis Control Circuit recipes."},
            {"psi.book.page.psitweaks_material.flashmetal", "Made by enriching $(l:components/psitweaks_unrefined_flashmetal)$(o)$(item)Unrefined Flashmetal$(0)$(/l).$(p)Used for Flashmetal blocks, Flashmetal CAD assemblies, $(l:components/psitweaks_heavy_psimetal)Heavy Psimetal$(/l), and sublimated-tier spell bullets."},
            {"psi.book.page.psitweaks_material.graviton_factor", "$(item)Graviton Factor$(0) cannot be made by crafting or infusing. Drop a $(item)Quantum Factor$(0) as an item and let it fall 380 blocks; the strain of the fall rewrites it into a $(item)Graviton Factor$(0) in mid-air. The stack count is carried over as-is.$(p)Use a jetpack or flight spell, or prepare a drop shaft from near the build limit, and collect it at the bottom."},
            {"psi.book.page.psitweaks_material.heavy_psimetal", "Crafted from $(l:components/psitweaks_heavy_psimetal_scrap)$(o)$(item)Heavy Psimetal Scrap$(0)$(/l) and $(l:components/psitweaks_flashmetal)$(o)$(item)Flashmetal$(0)$(/l).$(p)Used for stronger CAD assemblies, gear, and the $(l:components/psitweaks_psycheonic_metal_ingot)Psycheonic Metal chain$(/l)."},
            {"psi.book.page.psitweaks_material.heavy_psimetal_scrap", "Made by infusing Netherite Scrap with $(l:components/psitweaks_psionic_echo)Psionic Echo$(/l).$(p)Used with $(l:components/psitweaks_flashmetal)$(o)$(item)Flashmetal$(0)$(/l) to craft $(l:components/psitweaks_heavy_psimetal)$(o)$(item)Heavy Psimetal$(0)$(/l)."},
            {"psi.book.page.psitweaks_material.hypostasis_control_circuit", "Crafted from $(l:components/psitweaks_echo_control_circuit)$(o)$(item)Echo Control Circuit$(0)$(/l), $(l:components/psitweaks_alloy_hypostasis)$(o)$(item)Hypostasis Alloy$(0)$(/l), and $(l:components/psitweaks_echo_sheet)HDΨE Sheets$(/l).$(p)Used for the highest machinery."},
            {"psi.book.page.psitweaks_material.hypostasis_gem", "Made by applying material mutation to an $(item)Antinite Block$(0), either with the spell trick or the $(l:psitweaks_machines/material_mutator)Material Mutator$(/l).$(p)It can be enriched and converted into Hypostasis infusion, and is also used for $(l:components/psitweaks_magatama)$(o)$(item)Magatama$(0)$(/l), $(l:components/psitweaks_alloy_hypostasis)Hypostasis Alloy$(/l), and the $(l:components/psitweaks_psycheonic_metal_ingot)Psycheonic Metal chain$(/l)."},
            {"psi.book.page.psitweaks_material.jade", "Made by applying material mutation to an $(item)Emerald Block$(0), either with the spell trick or the $(l:psitweaks_machines/material_mutator)Material Mutator$(/l).$(p)Used as the main material for crafting $(l:components/psitweaks_magatama)$(o)$(item)Magatama$(0)$(/l)."},
            {"psi.book.page.psitweaks_material.magatama", "Crafted from $(l:components/psitweaks_jade)$(o)$(item)Jade$(0)$(/l) surrounding a $(l:components/psitweaks_hypostasis_gem)$(o)$(item)Hypostasis Gem$(0)$(/l).$(p)Used in $(l:psitweaks_machines/spellmachinery_casing)Spellmachinery Casings$(/l), making it a bridge between material mutation and advanced psionic machinery."},
            {"psi.book.page.psitweaks_material.magicians_brain", "Dropped when a Magician villager is killed by $(l:psitweaks_spell_pieces/trick_guillotine)$(o)Trick: Guillotine$(0)$(/l).$(p)Used for the $(l:items/psitweaks_sorcery_booster)$(o)$(item)Sorcery Booster$(0)$(/l) and the Phenomenon Interference Enhancement Unit module."},
            {"psi.book.page.psitweaks_material.pellet_americium", "Made by mutating a $(item)Plutonium Block$(0) with $(l:psitweaks_spell_pieces/trick_material_mutation)$(o)Trick: Material Mutation$(0)$(/l), or by processing it in the $(l:psitweaks_machines/material_mutator)$(o)Material Mutator$(0)$(/l).$(p)Used for Psycheonic Metal CAD Assemblies and other late-game psionic recipes."},
            {"psi.book.page.psitweaks_material.pellet_neptunium", "Made by mutating a $(item)Polonium Block$(0) with $(l:psitweaks_spell_pieces/trick_material_mutation)$(o)Trick: Material Mutation$(0)$(/l), or by processing it in the $(l:psitweaks_machines/material_mutator)$(o)Material Mutator$(0)$(/l).$(p)Used for the $(l:psitweaks_machines/transcendent_universal_cable)$(o)$(item)Transcendent Universal Cable$(0)$(/l) and the MekaSuit $(item)Phenomenon Interference Enhancement Unit$(0)."},
            {"psi.book.page.psitweaks_material.psionic_control_circuit", "Crafted from $(l:components/psitweaks_alloy_psion)$(o)$(item)Psionic Alloy$(0)$(/l) and a Mekanism Ultimate Control Circuit.$(p)Used in PsiTweaks machines, upgraded spell bullets, and similar recipes."},
            {"psi.book.page.psitweaks_material.psionic_echo", "Made by infusing an $(item)Echo Shard$(0) with Psionic Echo infusion, or by using the $(l:psitweaks_spell_pieces/trick_supreme_infusion)$(o)Trick: Supreme Infusion$(0)$(/l).$(p)Used as the base catalyst for $(l:components/psitweaks_alloy_psionic_echo)echo-tier alloys$(/l), HDΨE materials, gas processing, and advanced psionic machinery."},
            {"psi.book.page.psitweaks_material.psycheonic_metal_ingot", "Infuse $(l:components/psitweaks_heavy_psimetal)$(o)$(item)Heavy Psimetal$(0)$(/l) with Hypostasis to make $(item)Psycheonic Metal Nuggets$(0), then craft nine nuggets into an ingot.$(p)Used for $(l:items/psitweaks_spell_bullets)transcendent spell bullets$(/l), Psycheonic Metal CAD assemblies, and the highest psionic material tier."},
            {"psi.book.page.psitweaks_material.unrefined_flashmetal", "Crafted from $(l:components/psitweaks_chaotic_psimetal)$(o)$(item)Chaotic Psimetal$(0)$(/l) and Refined Glowstone.$(p)Enrich it to obtain usable $(l:components/psitweaks_flashmetal)$(o)$(item)Flashmetal$(0)$(/l)."},
            {"psi.book.page.psitweaks_mekanism_integration.1", "The $(item)Metallurgic Infuser$(0) can produce core Psi materials with PsiTweaks infuse types. Psigem infusion turns redstone into $(l:components/psidust)$(o)$(item)Psidust$(0)$(/l) and gold into $(item)Psimetal$(0).$(p)Ebony and Ivory infusion can make $(item)Ebony Substance$(0), $(item)Ivory Substance$(0), and their Psimetal variants. Later recipes extend this chain into $(l:components/psitweaks_chaotic_factor)psionic factors$(/l), $(l:components/psitweaks_chaotic_psimetal)$(o)$(item)Chaotic Psimetal$(0)$(/l), $(l:components/psitweaks_psionic_echo)Psionic Echo materials$(/l), and $(l:components/psitweaks_alloy_hypostasis)Hypostasis-tier metal$(/l)."},
            {"psi.book.page.psitweaks_mekanism_integration.2", "PsiTweaks adds three MekaSuit modules for spellcasters. The $(item)Psyon Supplying Unit$(0) and $(item)Psyon Capacity Unit$(0) install in the MekaSuit body armor and improve Psi regeneration and maximum Psi.$(p)The $(item)Phenomenon Interference Enhancement Unit$(0) installs in the MekaSuit helmet and increases spell damage."},
            {"psi.book.page.psitweaks_research.0", "Some spell pieces added by $(thing)PsiTweaks$(0) must be unlocked through research before they can be used in spell programs.$(p)Research creates program items that correspond to those spell pieces. Right-clicking with a program unlocks its spell piece, and the program is not consumed."},
            {"psi.book.page.psitweaks_research.1", "Place the required ingredients in the $(l:psitweaks_machines/program_researcher)$(o)$(item)Program Research Table$(0)$(/l) and supply FE to craft a program.$(p)JEI lists each research recipe's required materials, energy usage per tick, and processing time."},
            {"psi.book.page.psitweaks_spell_bullets.0", "$(thing)PsiTweaks$(0) adds upgraded tiers above the ordinary $(l:items/spell_bullet)$(o)$(item)Spell Bullet$(0)$(/l): Advanced, Resonant, Sublimated, Awakened, and Transcendent.$(p)Every Psi spell bullet variant has upgraded tiers available."},
            {"psi.book.page.psitweaks_spell_bullets.1", "Upgrading a spell bullet requires four bullets from the previous tier, making it very expensive."},
            {"psi.book.page.psitweaks_spell_bullets.2", "Higher tiers greatly improve cost efficiency, making powerful spells easier to cast."},
            {"psi.book.page.psitweaks_spellpiece.selector_nearby_spellgram", "Gets SpellGram objects around the specified coordinates. It is mainly used by tricks that control placed SpellGram objects."},
            {"psi.book.page.psitweaks_spellpiece.selector_stored_entity", "Gets an entity from the UUID stored in CAD memory."},
            {"psi.book.page.psitweaks_spellpiece.trick_aqua_cutter", "Fires a water-blade projectile forward as an early-game offensive trick."},
            {"psi.book.page.psitweaks_spellpiece.trick_blaze_ball", "Fires a ball of flame forward as an early-game offensive trick."},
            {"psi.book.page.psitweaks_spellpiece.trick_break_fortune", "Breaks the target block with Fortune."},
            {"psi.book.page.psitweaks_spellpiece.trick_break_silk", "Breaks the target block with Silk Touch."},
            {"psi.book.page.psitweaks_spellpiece.trick_cure_radiation", "Removes the target's radiation exposure."},
            {"psi.book.page.psitweaks_spellpiece.trick_die_flex", "Behaves similarly to Psi's Trick: Die and refunds the Psi cost of spell pieces that were not executed. With high-frequency casting, the client-side Psi display may temporarily desync."},
            {"psi.book.page.psitweaks_spellpiece.trick_dispel", "Removes effects from the target entity. This is the general-purpose dispel that does not distinguish between beneficial and harmful effects."},
            {"psi.book.page.psitweaks_spellpiece.trick_dispel_beneficial", "Removes only beneficial effects from the target entity. It is suited for stripping enhancements from hostile targets."},
            {"psi.book.page.psitweaks_spellpiece.trick_dispel_non_beneficial", "Removes non-beneficial effects from the target entity. Use it when you want to cleanse harmful effects while leaving allied buffs intact."},
            {"psi.book.page.psitweaks_spellpiece.trick_explode_no_destroy", "Creates an explosion that deals damage without destroying blocks. Dropped items and experience orbs are not erased by the blast."},
            {"psi.book.page.psitweaks_spellpiece.trick_flare_circle", "Places a fire SpellGram Circle that continuously deals fire damage to living beings inside it. Once placed, the circle remains for 60 seconds."},
            {"psi.book.page.psitweaks_spellpiece.trick_flight", "Applies the Flight effect to the target. The Flight effect enables creative flight but consumes Psi every 0.5 seconds. The amount consumed varies based on the Flight effect level, the efficiency of the CAD used to apply the effect, and the spell bullet's cost multiplier. The base consumption at effect levels 1, 2, 3, 4, and 5 or higher is 1000 / 750 / 500 / 250 / 5 Psi every 0.5 seconds, respectively."},
            {"psi.book.page.psitweaks_spellpiece.trick_freeze_block", "Freezes the target block one stage. Water becomes ice, ice becomes packed ice, packed ice becomes blue ice, lava becomes magma block, and magma block becomes obsidian."},
            {"psi.book.page.psitweaks_spellpiece.trick_hardening", "Applies a hardening effect that limits large incoming damage to a fixed value. Maximum damage taken is capped at (level - 2)."},
            {"psi.book.page.psitweaks_spellpiece.trick_ice_circle", "Places an ice SpellGram Circle that continuously deals freezing damage to living beings inside it. Like Fire Circle, it is suited for area control."},
            {"psi.book.page.psitweaks_spellpiece.trick_interact_block", "Acts on the target block as if right-clicked with the item in the caster's off hand."},
            {"psi.book.page.psitweaks_spellpiece.trick_material_mutation", "Breaks specific blocks and transmutes them into other items. The Material Mutator can perform this process using power and Vaporized Psionic Echo."},
            {"psi.book.page.psitweaks_spellpiece.trick_melt_block", "Melts the target block one stage. Ice, packed ice, and blue ice become water; obsidian, stone-like blocks, and cobblestone-like blocks become magma block; and magma block becomes lava."},
            {"psi.book.page.psitweaks_spellpiece.trick_parade", "Applies an effect that evades attacks by chance. It evades attacks with a (62.5 + 7.5 * level)%% chance."},
            {"psi.book.page.psitweaks_spellpiece.trick_radiation_injection", "Applies Mekanism radiation exposure to the target."},
            {"psi.book.page.psitweaks_spellpiece.trick_set_spellgram_follow_target", "Sets the follow target entity of a SpellGram object."},
            {"psi.book.page.psitweaks_spellpiece.trick_store_entity", "Stores the target entity's UUID in CAD memory."},
            {"psi.book.page.psitweaks_spellpiece.trick_supply_fe", "Supplies FE to the target block. When CAD Efficiency is 100, it supplies 20 FE per Psi."},
            {"psi.book.page.psitweaks_spellpiece.trick_supreme_infusion", "Infusion-converts Echo Shards into Psionic Echo."},
            {"psi.book.page.psitweaks_spellpiece.trick_time_accelerate", "Multiplies the target block's tick progression by (2 ^ power). The upper limit is 512x speed."},
            {"psi.book.title.psitweaks_material.chaotic_psimetal", "Chaotic Psimetal Ingot"},
            {"psi.book.title.psitweaks_material.psycheonic_metal_ingot", "Psycheonic Metal Ingot"},
            {"psi.book.title.psitweaks_mekanism_integration.infusing", "Metallurgic Infusing"},
            {"psi.book.title.psitweaks_mekanism_integration.mekasuit", "MekaSuit Modules"}
    };

    private static final String[][] LEGACY_PATCHOULI_BOOK_JA_JP = {
            {"psi.book.category.basics", "基礎"},
            {"psi.book.category.basics.desc", "$(thing)Psi$(0)を初めてご利用になる方は, こちらのすべての項目を必ずお読みになることをお勧めします. いずれも重要な情報が含まれています."},
            {"psi.book.category.psitweaks", "PsiTweaks"},
            {"psi.book.category.psitweaks.desc", "$(thing)PsiTweaks$(0) は, 高位術式弾, CAD部品, 機械, Mekanism連携, そして終盤向けのサイオニック素材を追加して, $(thing)Psi$(0) の進行を拡張します."},
            {"psi.book.category.psitweaks_machines", "機械"},
            {"psi.book.category.psitweaks_machines.desc", "PsiTweaks が追加する機械ブロックと基盤設備です."},
            {"psi.book.category.psitweaks_spell_pieces", "追加術式"},
            {"psi.book.category.psitweaks_spell_pieces.desc", "PsiTweaks が追加するスペルピースです. 攻撃, 工業処理, 補助用途のものが多く, 設定によっては研究解禁が必要になります."},
            {"psi.book.entry.cadAssembler", "CAD組立機"},
            {"psi.book.entry.introduction", "導入"},
            {"psi.book.entry.psitweaks_auto_casters", "術式自動詠唱デバイス"},
            {"psi.book.entry.psitweaks_cad_and_gear", "CADと装備"},
            {"psi.book.entry.psitweaks_inline_casters", "インラインキャスター"},
            {"psi.book.entry.psitweaks_magician", "魔法師"},
            {"psi.book.entry.psitweaks_mekanism_integration", "Mekanism連携"},
            {"psi.book.entry.psitweaks_moval_suit", "M.O.V.A.L. スーツ"},
            {"psi.book.entry.psitweaks_overview", "PsiTweaksの追加要素"},
            {"psi.book.entry.psitweaks_research", "研究"},
            {"psi.book.entry.psitweaks_spell_bullets", "強化術式弾"},
            {"psi.book.entry.spellProgrammer", "Spell Programmer"},
            {"psi.book.entry.tutorial1", "Tutorial (1): Writing A Spell"},
            {"psi.book.entry.vectorPrimer", "A Primer On Vectors"},
            {"psi.book.landing_text", "$(thing)Psi$(0) はあなたの創意工夫によって限界が決まる$(thing)魔法$(0)作成 mod です. 本書には, 熟練の魔法師になるために必要な知識がすべて収録されています.$(p)(本書は作成中です. 旧チュートリアル記事は「レガシーエントリー」でご覧いただけます.)"},
            {"psi.book.name", "サイオニカ魔法大全"},
            {"psi.book.page.cadAssembler.0", "$(item)CAD組立機$(0)は$(thing)Psi$(0)の中核であり, 二つの重要な機能を果たします.$(p)第一に, 構成部品から$(thing)CAD$(0)を組み立てます. 第二に, $(o)空でない$() $(item)術式弾$(0)を$(thing)CAD$(0)や, $(l:items/tools)$(o)$(thing)サイメタルツール$(0)$(/l)など$(l:items/spell_bullet)$(o)$(thing)術式弾$(0)$(/l)を保持する他のアイテムに装填します."},
            {"psi.book.page.cadAssembler.1", "CAD組立機のクラフト"},
            {"psi.book.page.cadAssembler.2", "最も簡素な魔法使用可能CAD"},
            {"psi.book.page.cadAssembler.3", "A $(thing)CAD$(0) is built from up to five components; the simplest $(thing)CAD$(0) only uses one component, an $(l:components/assembly)$(o)$(item)Assembly$(0)$(/l), though this is only useful for crafting $(l:components/psidust)$(o)$(item)Psidust$(0)$(/l).$(p)A $(thing)CAD$(0) capable of casting $(thing)Spells$(0) requires a $(l:components/core)$(o)$(item)Core$(0)$(/l) and a $(l:components/socket)$(o)$(item)Socket$(0)$(/l) as well.$(p)Adding a $(l:components/battery)$(o)$(item)Battery$(0)$(/l) slightly increases a user's maximum $(thing)Psi energy$(0), and adding a $(l:components/colorizer)$(o)$(item)Colorizer$(0)$(/l) changes the color of cast $(thing)Spells$(0), which is purely cosmetic."},
            {"psi.book.page.cadAssembler.4", "Once a $(thing)CAD$(0) is created, it can be placed in the leftmost panel of the $(item)CAD Assembler$(0).$(p)When placed there, the slots below open; $(item)Spell Bullets$(0) can be placed in these slots to be loaded into the $(thing)CAD$(0).$(p)Once the $(l:items/spell_bullet)$(o)$(thing)Bullets$(0)$(/l) are loaded, the $(thing)CAD$(0) is removed and held, and the $(thing)Psi master keybind$(0) ($(k:psimisc.keybind)) is held, the $(l:items/spell_bullet)$(o)$(thing)Bullets$(0)$(/l)' $(thing)Spells$(0) will be displayed on a radial menu, ready to be selected and cast."},
            {"psi.book.page.cadAssembler.5", "A CAD with one bullet loaded"},
            {"psi.book.page.introduction.0", "$(thing)Psi$(0)へようこそ！ 宇宙で最も偉大なスペルプログラムベースの魔法技術MODです！$(p)$(thing)Psi$(0)は（魔法科高校の劣等生シリーズにインスパイアされた）MODで, あなたの命令を実行する$(thing)魔法$(0)$(o)を創造・発動して意のままに操るmodです.$(p)魔法を操る魔法師への道へ踏み出すには, まず$(l:components/psidust)$(o)$(item)サイダスト$(0)$(/l)という素材が必要です――ただし, これを単純にクラフトできるものではありません."},
            {"psi.book.page.introduction.1", "代わりに, まず$(l:basics/cad_assembler)$(o)$(item)CAD組立機$(0)$(/l)と$(l:components/assembly#iron)$(o)$(item)鉄のCAD素体$(0)$(/l)を作成してください.$(p)$(l:basics/cad_assembler)$(o)$(item)CAD組立機$(0)$(/l)を配置し, 開いて$(l:components/assembly#iron)$(o)$(item)鉄のCAD素体$(0)$(/l)を挿入し, 非常に簡素な$(thing)術式補助演算機$(0)（略して$(thing)CAD$(0)）を構築します.$(p)そこから地面に$(item)レッドストーンダスト$(0)を数個落とし, 新しく作成した$(thing)CAD$(0)をダストに向けて, $(k:use)を発動して$(l:components/psidust)$(o)$(item)サイダスト$(0)$(/l)を生成します."},
            {"psi.book.page.psitweaks_cad_and_gear.0", "PsiTweaks には, 術式の携行, 自動化, 専門化を助けるアイテムが複数あります. CADに直接関わる道具もあれば, 術式弾の管理や魔法師の戦闘能力を支える装備もあります."},
            {"psi.book.page.psitweaks_cad_and_gear.2", "PsiTweaks は, 高コストなインゴットを素材とした強力なCAD素体を追加します. 通常のPsi装備では物足りなくなったら, 高位術式弾などと組み合わせて使ってください."},
            {"psi.book.page.psitweaks_cad_and_gear.3", "$(thing)PsiTweaks$(0) はCAD素材として, $(l:components/psitweaks_alloy_psion)サイオニック合金$(/l), $(l:components/psitweaks_chaotic_psimetal)カオティックサイメタル$(/l), $(l:components/psitweaks_flashmetal)フラッシュメタル$(/l), $(l:components/psitweaks_heavy_psimetal)ヘビーサイメタル$(/l), $(l:components/psitweaks_psycheonic_metal_ingot)プシオニックメタル$(/l) の5系統を追加します.$(p)サイオニック合金CADは効率が非常に優れる一方で, 規模は非常に小さいCADです. 他のCAD素体は, 後半の素材ほど効率と規模が相応に強化されています."},
            {"psi.book.page.psitweaks_item.blank_program", "プログラム研究の基礎素材です. $(l:psitweaks_machines/program_researcher)$(o)$(item)プログラム研究台$(0)$(/l) に必要素材と一緒に入れることで, 記入済みのプログラムを作成できます.$(p)既存の記入済みプログラムアイテムとクラフトすると, そのプログラムを複製することもできます."},
            {"psi.book.page.psitweaks_item.flash_charm", "装備者の盲目と暗闇を継続的に解除するCuriosチャームです.$(p)インベントリ内に持っているだけでも機能するため, ディープダークや視界妨害効果への対策になります."},
            {"psi.book.page.psitweaks_item.inline_casters.0", "インライン, セカンダリ, パラレルキャスターは, 自身に術式弾スロットを持つ手持ち詠唱具です. 詠唱には通常通りCADが必要ですが, 選択中の術式弾はキャスター側に保存されます."},
            {"psi.book.page.psitweaks_item.inline_casters.1", "インラインキャスターは1スロット, セカンダリキャスターは5スロット, パラレルキャスターは11スロットです.$(p)CADを頻繁に差し替えず, 多数の術式弾を持ち替えたいときに使います."},
            {"psi.book.page.psitweaks_item.philosophers_stone", "再利用可能な触媒で, 金属, ダイヤモンド, 石炭やエンダーパールなどの相互変換レシピを提供します."},
            {"psi.book.page.psitweaks_item.portable_cad_assembler", "手持ちで使えるCAD組立機です. ブロックを設置せずに組立機画面を開けるため, 拠点外でも簡単にCAD部品や装填済み術式弾を調整できます."},
            {"psi.book.page.psitweaks_item.third_eye_device", "魔法演算領域スロットに装備し, 術者に対する通常の術式射程チェックを無効化します.$(p)通常のCAD射程を大きく超える位置を対象にできるため, 扱いには注意してください."},
            {"psi.book.page.psitweaks_machine.material_mutator", "$(item)気化サイオニックエコー$(0) と電力を使い, 物質変成を実行するMekanism注入形式の機械です.$(p)$(l:psitweaks_spell_pieces/trick_material_mutation)$(o)作動式: 物質変成$(0)$(/l)でも得られる$(l:components/psitweaks_jade)翡翠$(/l)や$(l:components/psitweaks_hypostasis_gem)ヒュポスタシスジェム$(/l)などの変成を自動化できます."},
            {"psi.book.page.psitweaks_machine.program_researcher", "PsiTweaksのプログラムアイテムを作成するための電力式研究台です. 必要素材を入力スロットに入れてFEを供給すると, 研究完了時にプログラムを出力します.$(p)各研究のtickあたりの消費電力と時間はJEIで確認できます."},
            {"psi.book.page.psitweaks_machine.psionic_generator", "所有者にリンクし, 所有者がオンラインの間, そのプレイヤーのPsiをエネルギーへ変換する発電機です.$(p)GUIからリンクの有効化とtickあたりのPsi消費量を設定します. 消費量を増やすほど出力も増えますが, 所有者のPsiも速く減ります."},
            {"psi.book.page.psitweaks_machine.spellmachinery_casing", "高度な魔法を機械により実行するための筐体ブロックです."},
            {"psi.book.page.psitweaks_magician.2", "魔法師は $(l:components/psitweaks_magicians_brain)$(o)$(item)魔法師の脳$(0)$(/l) の入手元でもあります. 魔法師の村人を $(l:psitweaks_spell_pieces/trick_guillotine)$(o)作動式: ギロチン$(0)$(/l) で倒したときにドロップし, 魔法師向けのレシピに使います."},
            {"psi.book.page.psitweaks_material.alloy_hypostasis", "$(l:components/psitweaks_alloy_psionic_echo)$(o)$(item)感応合金$(0)$(/l) にヒュポスタシスを注入して作ります.$(p)$(l:components/psitweaks_hypostasis_control_circuit)$(o)$(item)位格制御回路$(0)$(/l) や高 tier のサイオニック機械に使います."},
            {"psi.book.page.psitweaks_material.alloy_psion", "Mekanism の原子合金にサイジェムを注入して作ります.$(p)$(l:components/psitweaks_psionic_control_circuit)$(o)$(item)サイオニック制御回路$(0)$(/l), サイオニック合金 CAD 素体, 次 tier の合金素材に使います."},
            {"psi.book.page.psitweaks_material.alloy_psionic_echo", "$(l:components/psitweaks_alloy_psion)$(o)$(item)サイオニック合金$(0)$(/l) に$(l:components/psitweaks_psionic_echo)サイオニックエコー$(/l)を注入して作ります.$(p)$(l:components/psitweaks_echo_control_circuit)$(o)$(item)感応制御回路$(0)$(/l) や$(l:components/psitweaks_alloy_hypostasis)位格合金$(/l)の素材に使います."},
            {"psi.book.page.psitweaks_material.antinite_ingot", "アンティナイトの鉱石を精錬するか, アンティナイトブロックから戻して入手します. 粉, 欠片, 凝塊, 結晶, 汚れた粉は Mekanism 式鉱石処理の中間素材です.$(p)$(l:components/psitweaks_hypostasis_gem)ヒュポスタシスジェム$(/l)や$(l:items/psitweaks_philosophers_stone)賢者の石$(/l)の素材になります."},
            {"psi.book.page.psitweaks_material.chaotic_factor", "$(item)カオティック因子$(0) は, $(item)エンダーパール$(0) にサイジェムを注入して $(item)サイオニック因子$(0) を作るところから始まります. そこへアイボリーまたはエボニーを注入して偏向因子を作り, 反対属性を重ねると $(item)カオティック因子$(0) になります.$(p)通常のサイメタルを上回る最初の PsiTweaks 金属, $(l:components/psitweaks_chaotic_psimetal)$(o)$(item)カオティックサイメタル$(0)$(/l) の注入源です."},
            {"psi.book.page.psitweaks_material.chaotic_psimetal", "通常の $(item)サイメタル$(0) に$(l:components/psitweaks_chaotic_factor)カオティック因子$(/l)を注入して作ります.$(p)カオティックサイメタル CAD 素体や $(l:components/psitweaks_unrefined_flashmetal)$(o)$(item)未精製フラッシュメタル$(0)$(/l) のレシピに使います."},
            {"psi.book.page.psitweaks_material.dimensional_crystal", "術式の詠唱時に, まれに $(item)次元の結晶$(0) が手に入ります. 確率は詠唱が消費した Psi の量に比例して上がります.$(p)クラフトでは作れません. 安定して集めたいなら, 日常的に魔法を使って生活する必要があります."},
            {"psi.book.page.psitweaks_material.echo_control_circuit", "$(l:components/psitweaks_psionic_control_circuit)$(o)$(item)サイオニック制御回路$(0)$(/l), $(l:components/psitweaks_alloy_psionic_echo)$(o)$(item)感応合金$(0)$(/l), $(l:components/psitweaks_echo_sheet)HDΨE シート$(/l)からクラフトします.$(p) 上位の術式弾や機械のレシピに使います."},
            {"psi.book.page.psitweaks_material.echo_pellet", "$(l:components/psitweaks_psionic_echo)サイオニックエコー$(/l)加工から得られる圧縮 HDΨE 素材です.$(p)生の$(l:components/psitweaks_psionic_echo)サイオニックエコー$(/l)ではなく, 凝縮した感応素材を要求するレシピに使います. 副産物として得られるΨE-O燃料はガス発電機の燃料として使えます."},
            {"psi.book.page.psitweaks_material.echo_sheet", "HDΨE 素材をシート状に濃縮して作ります.$(p)感応制御回路と位格制御回路のレシピで多く使います."},
            {"psi.book.page.psitweaks_material.flashmetal", "$(l:components/psitweaks_unrefined_flashmetal)$(o)$(item)未精製フラッシュメタル$(0)$(/l) を濃縮して作ります.$(p)フラッシュメタルブロック, フラッシュメタル CAD 素体, $(l:components/psitweaks_heavy_psimetal)ヘビーサイメタル$(/l), 昇華 tier の術式弾に使います."},
            {"psi.book.page.psitweaks_material.graviton_factor", "$(item)グラヴィトン因子$(0) はクラフトや注入では作れません. $(item)クァンタム因子$(0) をアイテムとして落とし, 380ブロック落下させると, 落下の負荷によって空中で $(item)グラヴィトン因子$(0) へ書き換わります. スタック数はそのまま引き継がれます.$(p)ジェットパックや飛行魔法などを使うか, 建築限界の高さ付近から落下用の縦穴を用意し, 底で回収しましょう."},
            {"psi.book.page.psitweaks_material.heavy_psimetal", "$(l:components/psitweaks_heavy_psimetal_scrap)$(o)$(item)ヘビーサイメタルの欠片$(0)$(/l) と $(l:components/psitweaks_flashmetal)$(o)$(item)フラッシュメタル$(0)$(/l) からクラフトします.$(p)より強い CAD 素体, 装備, $(l:components/psitweaks_psycheonic_metal_ingot)プシオニックメタル系列$(/l)に使います."},
            {"psi.book.page.psitweaks_material.heavy_psimetal_scrap", "ネザライトの欠片に$(l:components/psitweaks_psionic_echo)サイオニックエコー$(/l)を注入して作ります.$(p)$(l:components/psitweaks_flashmetal)$(o)$(item)フラッシュメタル$(0)$(/l) と組み合わせて $(l:components/psitweaks_heavy_psimetal)$(o)$(item)ヘビーサイメタル$(0)$(/l) を作ります."},
            {"psi.book.page.psitweaks_material.hypostasis_control_circuit", "$(l:components/psitweaks_echo_control_circuit)$(o)$(item)感応制御回路$(0)$(/l), $(l:components/psitweaks_alloy_hypostasis)$(o)$(item)位格合金$(0)$(/l), $(l:components/psitweaks_echo_sheet)HDΨE シート$(/l)からクラフトします.$(p)最上位の機械に使います."},
            {"psi.book.page.psitweaks_material.hypostasis_gem", "$(item)アンティナイトブロック$(0) に物質変成を作用させて作ります. 術式でも$(l:psitweaks_machines/material_mutator)物質変成機$(/l)でも入手できます.$(p)濃縮してヒュポスタシス注入へ変換できるほか, $(l:components/psitweaks_magatama)$(o)$(item)勾玉$(0)$(/l), $(l:components/psitweaks_alloy_hypostasis)位格合金$(/l), $(l:components/psitweaks_psycheonic_metal_ingot)プシオニックメタル系列$(/l)に使います."},
            {"psi.book.page.psitweaks_material.jade", "$(item)エメラルドブロック$(0) に物質変成を作用させて作ります. 術式でも$(l:psitweaks_machines/material_mutator)物質変成機$(/l)でも入手できます.$(p)$(l:components/psitweaks_magatama)$(o)$(item)勾玉$(0)$(/l) を作るための主素材です."},
            {"psi.book.page.psitweaks_material.magatama", "$(l:components/psitweaks_jade)$(o)$(item)翡翠$(0)$(/l) で $(l:components/psitweaks_hypostasis_gem)$(o)$(item)ヒュポスタシスジェム$(0)$(/l) を囲んでクラフトします.$(p)$(l:psitweaks_machines/spellmachinery_casing)魔導機構ケーシング$(/l)に使うため, 物質変成素材と上位サイオニック機械をつなぐ素材です."},
            {"psi.book.page.psitweaks_material.magicians_brain", "魔法師の村人を $(l:psitweaks_spell_pieces/trick_guillotine)$(o)作動式: ギロチン$(0)$(/l) で倒したときにドロップします.$(p)$(l:items/psitweaks_sorcery_booster)$(o)$(item)ソーサリーブースター$(0)$(/l) と事象干渉力増大ユニットの素材に使います."},
            {"psi.book.page.psitweaks_material.pellet_americium", "$(item)プルトニウムブロック$(0) を $(l:psitweaks_spell_pieces/trick_material_mutation)$(o)作動式: 物質変成$(0)$(/l) で変成するか, $(l:psitweaks_machines/material_mutator)$(o)物質変成機$(0)$(/l) で加工して作ります.$(p)プシオニックメタルCAD素体など, 終盤のサイオニックレシピに使います."},
            {"psi.book.page.psitweaks_material.pellet_neptunium", "$(item)ポロニウムブロック$(0) を $(l:psitweaks_spell_pieces/trick_material_mutation)$(o)作動式: 物質変成$(0)$(/l) で変成するか, $(l:psitweaks_machines/material_mutator)$(o)物質変成機$(0)$(/l) で加工して作ります.$(p)$(l:psitweaks_machines/transcendent_universal_cable)$(o)$(item)超越ユニバーサルケーブル$(0)$(/l) や MekaSuit の $(item)事象干渉力増大ユニット$(0) に使います."},
            {"psi.book.page.psitweaks_material.psionic_control_circuit", "$(l:components/psitweaks_alloy_psion)$(o)$(item)サイオニック合金$(0)$(/l) と Mekanism の究極制御回路からクラフトします.$(p)PsiTweaks の機械, 強化術式弾などのレシピに使います."},
            {"psi.book.page.psitweaks_material.psionic_echo", "$(item)残響の欠片$(0) にサイオニックエコーを注入するか, $(l:psitweaks_spell_pieces/trick_supreme_infusion)$(o)作動式: 超位注入$(0)$(/l)で作成します.$(p)$(l:components/psitweaks_alloy_psionic_echo)感応系合金$(/l), HDΨE 素材, 気体加工, 上位のサイオニック機械の基礎触媒です."},
            {"psi.book.page.psitweaks_material.psycheonic_metal_ingot", "$(l:components/psitweaks_heavy_psimetal)$(o)$(item)ヘビーサイメタル$(0)$(/l) にヒュポスタシスを注入して $(item)プシオニックメタルナゲット$(0) を作り, 9個クラフトするとインゴットになります.$(p)$(l:items/psitweaks_spell_bullets)超越 tier の術式弾$(/l), プシオニックメタル CAD 素体, 最上位のサイオニック素材に使います."},
            {"psi.book.page.psitweaks_material.unrefined_flashmetal", "$(l:components/psitweaks_chaotic_psimetal)$(o)$(item)カオティックサイメタル$(0)$(/l) と精製グロウストーンからクラフトします.$(p)濃縮すると使用可能な $(l:components/psitweaks_flashmetal)$(o)$(item)フラッシュメタル$(0)$(/l) になります."},
            {"psi.book.page.psitweaks_mekanism_integration.1", "$(item)冶金吹き込み機$(0) では, PsiTweaks の注入タイプを使って主要なPsi素材を作れます. サイジェム注入はレッドストーンを$(l:components/psidust)$(o)$(item)サイダスト$(0)$(/l)に, 金インゴットを$(item)サイメタル$(0)に変換します.$(p)エボニー注入とアイボリー注入では, $(item)エボニー基質$(0), $(item)アイボリー基質$(0), それぞれのサイメタル派生素材を作れます. さらに$(l:components/psitweaks_chaotic_factor)サイオニック因子$(/l), $(l:components/psitweaks_chaotic_psimetal)$(o)$(item)カオティックサイメタル$(0)$(/l), $(l:components/psitweaks_psionic_echo)サイオニックエコー素材$(/l), $(l:components/psitweaks_alloy_hypostasis)位格合金$(/l)へ発展します."},
            {"psi.book.page.psitweaks_mekanism_integration.2", "PsiTweaks は魔法師向けのMekaSuitモジュールを3種類追加します. $(item)サイオン供給ユニット$(0) と $(item)サイオン容量ユニット$(0) はMekaSuit胴体に装着し, Psi回復速度と最大Psi保有量を強化します.$(p)$(item)事象干渉力増大ユニット$(0) はMekaSuitヘルメットに装着し, 術式ダメージを増加させます."},
            {"psi.book.page.psitweaks_research.0", "$(thing)PsiTweaks$(0) が追加する一部のスペルピースは, 術式プログラムで使う前に研究によるアンロックが必要です.$(p)研究では, それらのスペルピースに対応するプログラムアイテムを作成します. プログラムを右クリックで使用することでスペルピースをアンロックできます(プログラムは消費しません)."},
            {"psi.book.page.psitweaks_research.1", "$(l:psitweaks_machines/program_researcher)$(o)$(item)プログラム研究台$(0)$(/l) に必要素材を入れて FE を供給すると, プログラムをクラフトできます.$(p)各研究に必要な素材, tickあたりの消費電力, 処理時間は JEI で確認できます."},
            {"psi.book.page.psitweaks_spell_bullets.0", "$(thing)PsiTweaks$(0) は通常の$(l:items/spell_bullet)$(o)$(item)術式弾$(0)$(/l)より上位の強化ティアとして, 改良, 共鳴, 昇華, 覚醒, 超越を追加します.$(p)Psiの術式弾のバリエーション全てにそれぞれ上位Tierが用意されています."},
            {"psi.book.page.psitweaks_spell_bullets.1", "術式弾の強化は前段階の術式弾を4つ必要とするため、非常に高コストです."},
            {"psi.book.page.psitweaks_spell_bullets.2", "上位のTierになるほどコスト効率が著しく改善し、強力な魔法を簡単に発動できるようになります."},
            {"psi.book.page.psitweaks_spellpiece.selector_nearby_spellgram", "指定座標の周囲にある魔法式オブジェクトを取得します. 設置済みの魔法式オブジェクトを制御する術式で主に使います."},
            {"psi.book.page.psitweaks_spellpiece.selector_stored_entity", "CADメモリに保存されたUUIDからエンティティを取得します. "},
            {"psi.book.page.psitweaks_spellpiece.trick_aqua_cutter", "前方へ水刃の発射体を放つ序盤用の攻撃術式です."},
            {"psi.book.page.psitweaks_spellpiece.trick_blaze_ball", "前方へ火の弾を放つ序盤用の攻撃術式です."},
            {"psi.book.page.psitweaks_spellpiece.trick_break_fortune", "対象ブロックを幸運付きで破壊します."},
            {"psi.book.page.psitweaks_spellpiece.trick_break_silk", "対象ブロックをシルクタッチ付きで破壊します."},
            {"psi.book.page.psitweaks_spellpiece.trick_cure_radiation", "対象の被ばく量を除去します."},
            {"psi.book.page.psitweaks_spellpiece.trick_die_flex", "Psi本体の停止と同様の動作を行い, 未実行分のスペルピースのPsiコストを返還します. 高頻度詠唱ではクライアント側のPsi表示が一時的にずれることがあります."},
            {"psi.book.page.psitweaks_spellpiece.trick_dispel", "対象エンティティからエフェクトを除去します. 良性・悪性を区別しない汎用版の解呪です."},
            {"psi.book.page.psitweaks_spellpiece.trick_dispel_beneficial", "対象エンティティから有益なエフェクトだけを除去します. 敵対対象の強化を剥がす用途に向いています."},
            {"psi.book.page.psitweaks_spellpiece.trick_dispel_non_beneficial", "対象エンティティから有益でないエフェクトを除去します. 味方のバフを残したまま悪性効果を消したい時に使います."},
            {"psi.book.page.psitweaks_spellpiece.trick_explode_no_destroy", "ブロックを破壊しない爆発を起こしてダメージを与えます. ドロップアイテムや経験値オーブは爆発で消滅しません."},
            {"psi.book.page.psitweaks_spellpiece.trick_flare_circle", "炎の魔法式サークルを設置し, 内部の生物に継続的な炎ダメージを与えます. 一度設置したサークルは60秒残り続けます."},
            {"psi.book.page.psitweaks_spellpiece.trick_flight", "対象に飛行エフェクトを付与します. 飛行エフェクトはクリエイティブ飛行を可能にしますが, 0.5秒ごとにPsiを消費します. 消費量は飛行エフェクトのレベル, 付与したCADの効率, 術式弾のコスト倍率によって変動し, エフェクトレベルが 1/2/3/4/5以上の際の基本消費量は 0.5秒ごとに 1000 / 750 / 500 / 250 / 5 です."},
            {"psi.book.page.psitweaks_spellpiece.trick_freeze_block", "対象ブロックを1段階凍結させます. 水は氷, 氷は氷塊, 氷塊は青氷, 溶岩はマグマブロック, マグマブロックは黒曜石になります."},
            {"psi.book.page.psitweaks_spellpiece.trick_hardening", "大きな被ダメージを一定値まで抑える硬化効果を付与します. 受ける最大ダメージを(レベル - 2)に抑えます."},
            {"psi.book.page.psitweaks_spellpiece.trick_ice_circle", "氷の魔法式サークルを設置し, 内部の生物に継続的な凍結ダメージを与えます. ファイアサークルと同じく領域制圧に向いています."},
            {"psi.book.page.psitweaks_spellpiece.trick_interact_block", "対象ブロックに対して, 術者のオフハンドのアイテムで右クリックしたように作用します."},
            {"psi.book.page.psitweaks_spellpiece.trick_material_mutation", "特定のブロックを破壊して別のアイテムへ変成させます. 物質変成機はこの処理を電力と気化サイオニックエコーで実行できます."},
            {"psi.book.page.psitweaks_spellpiece.trick_melt_block", "対象ブロックを1段階溶解させます. 氷, 氷塊, 青氷は水に, 黒曜石, 石系, 丸石系はマグマブロックに, マグマブロックは溶岩になります."},
            {"psi.book.page.psitweaks_spellpiece.trick_parade", "確率で攻撃を回避する効果を付与します. (62.5 + 7.5 * レベル) %% で攻撃を回避します."},
            {"psi.book.page.psitweaks_spellpiece.trick_radiation_injection", "対象へMekanismの放射線被ばくを付与します. "},
            {"psi.book.page.psitweaks_spellpiece.trick_set_spellgram_follow_target", "魔法式オブジェクトの追従対象エンティティを設定します."},
            {"psi.book.page.psitweaks_spellpiece.trick_store_entity", "対象エンティティのUUIDをCADメモリに保存します."},
            {"psi.book.page.psitweaks_spellpiece.trick_supply_fe", "対象ブロックへFEを供給します. 供給量はCADの効率が100のとき、1psiあたり20FEです."},
            {"psi.book.page.psitweaks_spellpiece.trick_supreme_infusion", "残響の欠片をサイオニックエコーへ注入変換します."},
            {"psi.book.page.psitweaks_spellpiece.trick_time_accelerate", "対象ブロックのtick進行を (2 ^ 威力) 倍にします.上限は512倍速まで."},
            {"psi.book.page.spellProgrammer.0", "If the $(l:basics/cad_assembler)$(o)$(item)CAD Assembler$(0)$(/l) is the heart of $(thing)Psi$(0), then the $(item)Spell Programmer$(0) is the brains of the mod. It's where $(thing)Spells$(0) are written and compiled, and eventually copied into $(item)Spell Bullets$(0) to be cast.$(p)When placed and opened ($(k:use)), it displays a large 9x9 grid; see $(l:basics/tutorial_1)the tutorial entries$(/l) for more in-depth knowledge on using this grid."},
            {"psi.book.page.spellProgrammer.1", "Hour Of Code"},
            {"psi.book.page.vectorPrimer.0", "$(thing)Psi$(0) uses the concept of a Vector extensively. Therefore, if you haven't the foggiest idea what a vector is, I strongly recommend you watch the video below.$(p)The explanation in the next few pages is $(l)simplified$() for beginners. Don't take it as definitive."},
            {"psi.book.page.vectorPrimer.1", "In the world of $(thing)Psi$(0) (and indeed in a $(thing)Minecraft$(0) world), all vectors are three-dimensional. In essence, they're just lists of three coordinates: $(o)x$(), $(o)y$(), $(o)z$().$(p)The $(o)x$()-coordinate represents east when positive and west when negative, the $(o)y$()-direction up and down, and the $(o)z$()-direction south and north.$(p)If this doesn't make sense yet, open the debug screen (F3) and run around, paying attention to the row labeled \"XYZ:\".$(br)You'll understand."},
            {"psi.book.page.vectorPrimer.10", "(Most directions aren't so nice, usually looking like \"36.86 degrees north of west, 22.62 degrees below the horizon.\")$(p)Note that the only vector without a direction is [0, 0, 0] (the $(l)zero vector$()), since you have to be going somewhere $(o)else$() to have a direction.$(p)Note that the direction of a position vector is almost as meaningless as its magnitude-- most $(thing)Spells$(0) don't need to know \"where should I go to get away from bedrock at world spawn.\""},
            {"psi.book.page.vectorPrimer.11", "As a matter of fact, you can reconstruct any vector, given only its magnitude and direction, into a list of three numbers (which are known as the $(l)components$() of the vector).$(p)For example, the direction \"up\" and the magnitude 1 correspond to the vector [0, 1, 0].$(p)This isn't as surprising as it may seem: after all, if someone tells you which direction to go, and how far, you should know they want you to be."},
            {"psi.book.page.vectorPrimer.12", "There are several simple ways to manipulate position and offset vectors.$(p)First, we can add a position and an offset vector to get another position vector, as we did earlier with the grass block example.$(p)On the other hand, we can of course $(o)subtract$() two position vectors to get the offset vector representing the offset from one to the other:$(br)[$(o)x$(), $(o)y$()+1, $(o)z$()] − [$(o)x$(), $(o)y$(), $(o)z$()] = [0, 1, 0]."},
            {"psi.book.page.vectorPrimer.13", "Perhaps more interestingly, we can add two offset vectors, to get a single offset representing their combination.$(p)Adding this offset to a position vector would be equivalent to first adding one of its components to that position vector, then adding the other."},
            {"psi.book.page.vectorPrimer.14", "And last, but most certainly not least, of the simple operations: we can $(l)scale$() a vector, by multiplying it by a number.$(p)Note that we're multiplying it by a number, and $(o)not$() another vector.$(p)If we wish to scale an vector [$(o)p$(), $(o)q$(), $(o)r$()] by a factor $(o)n$(), we simply multiply each of the vector's components by $(o)n$():$(br)$(o)n$()·[$(o)p$(), $(o)q$(), $(o)r$()] = [$(o)n$()·$(o)p$(), $(o)n$()·$(o)q$(), $(o)n$()·$(o)r$()]."},
            {"psi.book.page.vectorPrimer.15", "This final operation relates nicely indeed to the concepts of magnitude and direction.$(p)When you scale a vector by a number $(o)n$(), you:$(li)multiply its magnitude by the absolute value of $(o)n$(), and$(li)don't change its direction if $(o)n$() is positive, but reverse its direction if $(o)n$() is negative.$(p)If $(o)n$()=0, then of course the resulting vector is the zero vector."},
            {"psi.book.page.vectorPrimer.16", "On the other hand, if we set $(o)n$()=-1, then we get a vector with the same magnitude (the absolute value of -1 is 1), but pointing the opposite way (since -1 is negative)!$(p)This vector is known as the $(l)negative$() of the original, and when the two are added we get the zero vector.$(p)This makes sense, since if we go in a direction, then go in the opposite direction for the same distance, our net movement is zero."},
            {"psi.book.page.vectorPrimer.17", "If, instead of multiplying, we $(o)divide$() a (nonzero) vector by its magnitude, we get a vector with magnitude 1 (since anything divided by itself is 1), but the same direction (since magnitudes are always positive).$(p)This is an important and well-known operation, known as $(l)normalizing$() a vector; the vector that results (and, in fact, any vector with magnitude 1) is called a $(l)unit vector$()."},
            {"psi.book.page.vectorPrimer.18", "Unit vectors have a fixed magnitude, so they only represent direction.$(p)Many $(thing)Spell Pieces$(0) related to direction return unit vectors, like $(piece)Operator: Vector Axis Raycast$(0) and $(piece)Operator: Entity Look$(0).$(p)Indeed, there are $(thing)Operators$(0) to do $(o)most$() of the vector operations laid out in this article, usually with self-explanatory names."},
            {"psi.book.page.vectorPrimer.19", "The operations and their corresponding $(thing)Operators$(0) are as follows:$(li)Negating is $(piece)Operator: Vector Negate$(0);$(li)Normalizing is $(piece)Operator: Vector Normalize$(0);$(li)Scaling is $(piece)Operator: Vector Multiply$(0) and $(piece)Operator: Vector Divide$(0);$(li)Taking the magnitude is $(piece)Operator: Vector Magnitude$(0);$(li)Adding is $(piece)Operator: Vector Sum$(0);$(li)Subtracting is $(piece)Operator: Vector Subtract$(0)."},
            {"psi.book.page.vectorPrimer.2", "That list of three numbers on the debug screen is, in fact, the first type of vector you'll meet: a $(l)position vector$().$(p)A position vector simply represents the location of a block, or an entity, or perhaps some empty space in the world. A single fixed location, represented by a list [$(o)x$(), $(o)y$(), $(o)z$()].$(p)However, not all vectors represent positions-- and it's important to note that $(l)any three numbers in a list constitute a vector$()."},
            {"psi.book.page.vectorPrimer.20", "Finally: vectors are still lists of three numbers. Don't lose sight of that.$(p)In a $(l:basics/spell_programmer)$(o)$(item)Spell Programmer$(0)$(/l), they can be constructed from up to three numbers with $(piece)Operator: Vector Construct$(0).$(p)Conversely, a vector can also be broken back down into numbers with $(piece)Operator: Extract X$(0), $(piece)Operator: Extract Y$(0), and $(piece)Operator: Extract Z$(0)."},
            {"psi.book.page.vectorPrimer.21", "Congratulations on making it through this tutorial!$(p)Again, this is just an introduction to vectors-- I've not said anything about dot or cross products, or vector projections, for example.$(p)But this should be more than enough to put together some quite interesting $(thing)Spells$(0) already.$(p)That's all-- now go forth and spellsling!"},
            {"psi.book.page.vectorPrimer.3", "An interesting fact about vectors is that they're extremely easy to add.$(p)For example, say we have a grass block at some location, which we choose to represent as [$(o)x$(), $(o)y$(), $(o)z$()].$(p)If we wish to add another vector (e.g. [0, 1, 0]) to this one, all we would have to do is add corresponding numbers:$(br)our vector sum would be [$(o)x$()+0, $(o)y$()+1, $(o)z$()+0], or just [$(o)x$(), $(o)y$()+1, $(o)z$()]."},
            {"psi.book.page.vectorPrimer.4", "Since the $(o)y$()-component of our new vector has increased by one, and positive-$(o)y$() means up, this new vector simply represents the block above our grass block.$(p)The vector [0, 1, 0] represents the $(o)difference$() in position between our original and new vector, and it's our second type of vector: an $(l)offset vector$()."},
            {"psi.book.page.vectorPrimer.5", "Offset vectors are what most spellslingers spend the majority of their time manipulating, so a mastery over them is key.$(p)Usually, a mage starts with a single position vector, then adds, subtracts, or otherwise combines offset vectors with this position vector in order to target their desired point in the world."},
            {"psi.book.page.vectorPrimer.6", "It's important to note that the idea \"offset\" and \"position\" vectors is conceptual; the terms are unique to the terminology of this tablet.$(p)Again: all $(thing)Psi$(0) vectors are just lists of three numbers, and there's nothing stopping you from pretending the distinction doesn't exist.$(p)Indeed, in some contexts outside the scope of this book, the distinction doesn't even make $(o)sense$()."},
            {"psi.book.page.vectorPrimer.7", "Offset vectors have $(l)magnitudes$().$(p)You can think of an offset vector's magnitude as its \"length,\" or the distance between a position in the world, and that same position when the offset vector is added to it.$(p)For example, our earlier offset vector [0, 1, 0] simply moved the position one block-length up, so it had a length, and therefore a magnitude, of 1."},
            {"psi.book.page.vectorPrimer.8", "Since a distance is always positive, so are vectors' magnitudes.$(p)Consider the vector [0, -3, 0] as an example: it represents down, three blocks-- yet the total distance moved is three blocks, and the \"down\" bit doesn't matter.$(p)Therefore, the magnitude of this vector is $(l)positive$() 3."},
            {"psi.book.page.vectorPrimer.9", "Almost all vectors also have $(l)directions$().$(p)An offset vector's direction is, well, the direction something would move if it followed the vector in a straight line.$(p)For example, the direction of [0, 1, 0] is simply straight up.$(p)The vector [1, 0, -1], on the other hand, represents one block east and one block north, so its direction is just straight ahead, due northeast."},
            {"psi.book.subtitle", "魔法学入門"},
            {"psi.book.title.psitweaks_material.chaotic_psimetal", "カオティックサイメタル"},
            {"psi.book.title.psitweaks_material.psycheonic_metal_ingot", "プシオニックメタル"},
            {"psi.book.title.psitweaks_mekanism_integration.infusing", "冶金吹き込み"},
            {"psi.book.title.psitweaks_mekanism_integration.mekasuit", "MekaSuitモジュール"}
    };

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

    private void addModule(JsonObject root, String id, String enUs, String enUsDesc, String jaJp, String jaJpDesc) {
        root.addProperty("module.psitweaks." + id, switch (locale) {
            case "ja_jp" -> jaJp;
            default -> enUs;
        });
        root.addProperty("description.psitweaks." + id, switch (locale) {
            case "ja_jp" -> jaJpDesc;
            default -> enUsDesc;
        });
    }

    private void addPsitweaksCategoryBook(JsonObject root) {
        root.addProperty("psi.book.category.psitweaks", switch (locale) {
            case "ja_jp" -> "PsiTweaks";
            default -> "PsiTweaks";
        });
        root.addProperty("psi.book.category.psitweaks.desc", switch (locale) {
            case "ja_jp" -> "$(thing)PsiTweaks$(0) は, 高位術式弾, CAD部品, 機械, Mekanism連携, そして終盤向けのサイオニック素材を追加して, $(thing)Psi$(0) の進行を拡張します.";
            default -> "$(thing)PsiTweaks$(0) expands Psi with stronger spell bullets, CAD parts, machinery, Mekanism integration, and late-game psionic materials.";
        });
        root.addProperty("psi.book.entry.psitweaks_overview", switch (locale) {
            case "ja_jp" -> "PsiTweaksの追加要素";
            default -> "What PsiTweaks Adds";
        });
        root.addProperty("psi.book.page.psitweaks_overview.0", switch (locale) {
            case "ja_jp" -> "$(thing)PsiTweaks$(0) は $(thing)Psi$(0) の拡張modです. 高位術式弾, 新しいCAD素体や詠唱補助具, 新しい術式, 加工機械や発電機などを追加します.$(p)Psiを工業環境とつなぎ, 通常のCADや基本術式弾で終わらず, 終盤まで戦闘とユーティリティの両面で使えるようにすることを目指しています.";
            default -> "$(thing)PsiTweaks$(0) is an expansion for $(thing)Psi$(0). It adds higher-tier spell bullets, new CAD assemblies and casting support tools, new spells, processing machines, and generators.$(p)It ties Psi into industrial setups and keeps it strong for combat and utility through the late game, beyond ordinary CADs and basic spell bullets.";
        });
        root.addProperty("psi.book.entry.psitweaks_changes", switch (locale) {
            case "ja_jp" -> "PsiTweaksによる変更";
            default -> "Changes by PsiTweaks";
        });
        root.addProperty("psi.book.page.psitweaks_changes.0", switch (locale) {
            case "ja_jp" -> "$(thing)Psi: Tweaks And Additions$(0) を導入することによって QOL を改善するためのいくつかの調整が加えられます.$(p)ダメージを受けた際に Psi量 が減少しなくなり, 詠唱時の Psi回復クールタイムも撤廃されます.$(p)スペルプログラム画面では, 現在の設定言語に関わらず英語でスペルピースを検索することができます.";
            default -> "Installing $(thing)Psi: Tweaks And Additions$(0) applies several adjustments to improve QOL.$(p)Psi is no longer reduced when you take damage, and the Psi regeneration cooldown after casting is removed.$(p)In the Spell Programmer screen, spell pieces can be searched in English regardless of the current language setting.";
        });
        root.addProperty("psi.book.page.psitweaks_changes.1", switch (locale) {
            case "ja_jp" -> "$(p)スペルプログラム画面では, Shiftを押しながらグリッドを左ドラッグすると, 複数のスペルピースを範囲選択できます. 選択されたスペルピースは青くハイライトされ, コピーされる範囲は青い枠で示されます.$(p)Ctrlを押しながらスペルピースを左クリックすると, そのピースを選択に追加したり, 選択から外したりできます. 複数選択中は Ctrl+C でコピー, Ctrl+X で切り取り, Ctrl+V で貼り付けを行えます. 貼り付けは, Psiで現在選択されているセルをコピー範囲の左上として行われます.";
            default -> "In the Spell Programmer screen, hold Shift and left-drag across the grid to select multiple spell pieces. Selected spell pieces are highlighted in blue, and the copied range is shown with a blue outline.$(p)Hold Ctrl and left-click a spell piece to add it to or remove it from the selection. While multiple pieces are selected, use Ctrl+C to copy, Ctrl+X to cut, and Ctrl+V to paste. Pasting uses the cell currently selected in Psi as the upper-left corner of the copied range.";
        });
        root.addProperty("psi.book.entry.psitweaks_research", switch (locale) {
            case "ja_jp" -> "研究";
            default -> "Research";
        });
        root.addProperty("psi.book.page.psitweaks_research.0", switch (locale) {
            case "ja_jp" -> "$(thing)PsiTweaks$(0) が追加する一部のスペルピースは, 術式プログラムで使う前に研究によるアンロックが必要です.$(p)研究では, それらのスペルピースに対応するプログラムアイテムを作成します. プログラムを右クリックで使用することでスペルピースをアンロックできます(プログラムは消費しません).";
            default -> "Some spell pieces added by $(thing)PsiTweaks$(0) must be unlocked through research before they can be used in spell programs.$(p)Research creates program items that correspond to those spell pieces. Right-clicking with a program unlocks its spell piece, and the program is not consumed.";
        });
        root.addProperty("psi.book.page.psitweaks_research.1", switch (locale) {
            case "ja_jp" -> "$(l:psitweaks_machines/program_researcher)$(o)$(item)プログラム研究台$(0)$(/l) に必要素材を入れて FE を供給すると, プログラムをクラフトできます.$(p)各研究に必要な素材, tickあたりの消費電力, 処理時間は JEI で確認できます.";
            default -> "Place the required ingredients in the $(l:psitweaks_machines/program_researcher)$(o)$(item)Program Research Table$(0)$(/l) and supply FE to craft a program.$(p)JEI lists each research recipe's required materials, energy usage per tick, and processing time.";
        });
        root.addProperty("psi.book.page.psitweaks_research.no_mekanism", switch (locale) {
            case "ja_jp" -> "$(thing)Mekanism$(0) が導入されていない場合, 空白のプログラムと各スペルピースに対応する素材を作業台で不定形クラフトするとプログラムを作成できます.$(p)必要な素材はJEIのクラフトレシピで確認できます.";
            default -> "When $(thing)Mekanism$(0) is not installed, craft a Blank Program together with the materials for the corresponding spell piece in any arrangement to create its program.$(p)Check JEI for each crafting recipe's required materials.";
        });
        root.addProperty("psi.book.entry.psitweaks_idea_storage", switch (locale) {
            case "ja_jp" -> "イデアストレージ";
            default -> "Ideaspace Storage";
        });
        root.addProperty("psi.book.page.psitweaks_idea_storage.0", switch (locale) {
            case "ja_jp" -> "$(thing)イデアストレージ$(0) は, プレイヤーごとに1つずつ用意される専用の保管庫です. アイテム, 液体, Mekanismの化学物質, FEをまとめて保管できます.$(p)中身はプレイヤー単位でワールドに保存され, 持ち物とは別に管理されるので, 死亡しても失われません. 保管できる種類数や1種類あたりの上限はコンフィグで変更できます.";
            default -> "$(thing)Ideaspace Storage$(0) is a personal storage space that every player has. It holds items, fluids, Mekanism chemicals, and FE together.$(p)Its contents are saved in the world for each player, separately from your inventory, so they are not lost when you die. The number of types and the per-type limits can be changed in the config.";
        });
        root.addProperty("psi.book.page.psitweaks_idea_storage.1", switch (locale) {
            case "ja_jp" -> "中身は $(l:psitweaks_spell_pieces/trick_idea_storage_view)$(o)作動式: イデアストレージ閲覧$(0)$(/l) の画面で確認・出し入れできます.$(p)術式からは, ブロックや指定スロットと資源・FEを格納・取出する作動式と, 貯蔵量・空き容量・種類数を調べる取得子で操作します.$(p)$(l:psitweaks_spell_pieces/trick_ideaspace_connector)$(o)作動式: イデアコネクター$(0)$(/l) を設置すると, パイプなどの外部機器ともやり取りできます.";
            default -> "Use the screen opened by $(l:psitweaks_spell_pieces/trick_idea_storage_view)$(o)Trick: View Ideaspace Storage$(0)$(/l) to check its contents and move things in and out.$(p)Spells work with it through tricks that deposit and withdraw resources and FE with blocks or the target slot, and selectors that read amounts, free capacity and type counts.$(p)Place a $(l:psitweaks_spell_pieces/trick_ideaspace_connector)$(o)Trick: Ideaspace Connector$(0)$(/l) to exchange resources with pipes and other external devices.";
        });
        root.addProperty("psi.book.page.psitweaks_idea_storage.2", switch (locale) {
            case "ja_jp" -> "イデアストレージ系のスペルピースは, この $(item)プログラム: イデアストレージ$(0) を右クリックで使用するだけで, すべてまとめて解禁されます. プログラムは消費されません.$(p)プログラムの作り方は $(l:psitweaks/research)研究$(/l) を参照してください.";
            default -> "Using this $(item)Program: Ideaspace Storage$(0) with right-click unlocks every Ideaspace Storage spell piece at once. The program is not consumed.$(p)See $(l:psitweaks/research)Research$(/l) for how to make programs.";
        });
        root.addProperty("psi.book.entry.psitweaks_magician", switch (locale) {
            case "ja_jp" -> "魔法師";
            default -> "Magician";
        });
        root.addProperty("psi.book.page.psitweaks_magician.1", switch (locale) {
            case "ja_jp" -> "$(thing)魔法師$(0) は PsiTweaks が追加する村人の職業です. 無職の村人は, $(l:basics/cad_assembler)$(o)$(item)CAD組立機$(0)$(/l) を職業ブロックとして取得すると魔法師になります. 村人が到達できる場所に置いてください.";
            default -> "$(thing)Magicians$(0) are a villager profession added by PsiTweaks. An unemployed villager becomes one by claiming a $(l:basics/cad_assembler)$(o)$(item)CAD Assembler$(0)$(/l) as its job site; place it where a villager can reach it.";
        });
        root.addProperty("psi.book.page.psitweaks_magician.2", switch (locale) {
            case "ja_jp" -> "魔法師は $(l:components/psitweaks_magicians_brain)$(o)$(item)魔法師の脳$(0)$(/l) の入手元でもあります. 魔法師の村人を $(l:psitweaks_spell_pieces/trick_guillotine)$(o)作動式: ギロチン$(0)$(/l) で倒したときにドロップし, 魔法師向けのレシピに使います.";
            default -> "A Magician is also the source of the $(l:components/psitweaks_magicians_brain)$(o)$(item)Magician's Brain$(0)$(/l). It drops when a Magician villager is killed by $(l:psitweaks_spell_pieces/trick_guillotine)$(o)Trick: Guillotine$(0)$(/l), and is used in caster-focused recipes.";
        });
        root.addProperty("psi.book.entry.psitweaks_cad_and_gear", switch (locale) {
            case "ja_jp" -> "CADと装備";
            default -> "CADs and Gear";
        });
        root.addProperty("psi.book.page.psitweaks_cad_and_gear.0", switch (locale) {
            case "ja_jp" -> "PsiTweaks には, 術式の携行, 自動化, 専門化を助けるアイテムが複数あります. CADに直接関わる道具もあれば, 術式弾の管理や魔法師の戦闘能力を支える装備もあります.";
            default -> "Several PsiTweaks items make spellcasting easier to carry, automate, or specialize. Some are direct CAD tools, while others support bullet handling or improve a caster's combat role.";
        });
        root.addProperty("psi.book.page.psitweaks_cad_and_gear.2", switch (locale) {
            case "ja_jp" -> "PsiTweaks は, 高コストなインゴットを素材とした強力なCAD素体を追加します. 通常のPsi装備では物足りなくなったら, 高位術式弾などと組み合わせて使ってください.";
            default -> "PsiTweaks also adds powerful CAD assemblies made from expensive ingots. When ordinary Psi equipment no longer keeps up, use them together with high-tier spell bullets and similar upgrades.";
        });
        root.addProperty("psi.book.page.psitweaks_cad_and_gear.3", switch (locale) {
            case "ja_jp" -> "$(thing)PsiTweaks$(0) はCAD素材として, $(l:components/psitweaks_alloy_psion)サイオニック合金$(/l), $(l:components/psitweaks_chaotic_psimetal)カオティックサイメタル$(/l), $(l:components/psitweaks_flashmetal)フラッシュメタル$(/l), $(l:components/psitweaks_heavy_psimetal)ヘビーサイメタル$(/l), $(l:components/psitweaks_psycheonic_metal_ingot)プシオニックメタル$(/l) の5系統を追加します.$(p)サイオニック合金CADは効率が非常に優れる一方で, 規模は非常に小さいCADです. 他のCAD素体は, 後半の素材ほど効率と規模が相応に強化されています.";
            default -> "$(thing)PsiTweaks$(0) adds five CAD material lines: $(l:components/psitweaks_alloy_psion)Psionic Alloy$(/l), $(l:components/psitweaks_chaotic_psimetal)Chaotic Psimetal$(/l), $(l:components/psitweaks_flashmetal)Flashmetal$(/l), $(l:components/psitweaks_heavy_psimetal)Heavy Psimetal$(/l), and $(l:components/psitweaks_psycheonic_metal_ingot)Psycheonic Metal$(/l).$(p)Psionic Alloy CADs have excellent Efficiency but very small Potency. The other CAD assemblies improve in Efficiency and Potency as their materials move into later progression.";
        });
    }

    private void addProgrammingConceptsBook(JsonObject root) {
        addLocalized(root, "psi.book.entry.psitweaks_programming_types",
                "Types Added by PsiTweaks",
                "PsiTweaks 追加の型");
        addLocalized(root, "psi.book.page.psitweaks_programming_types.0",
                "$(thing)PsiTweaks$(0) adds new value types to Psi spell programming. The main additions are String, Block, Item, and Matrix.",
                "$(thing)PsiTweaks$(0) は, Psi のスペルプログラミングに新しい型を追加します. 主な追加型は String, Block, Item, Matrix です.");
        addLocalized(root, "psi.book.page.psitweaks_programming_types.1",
                "String is the type used for text. It can hold IDs, names, NBT paths, search conditions, and messages for display.$(p)For example, a spell can obtain a block or item ID as a String and test whether it matches a particular ID.$(p)String values are limited to 8192 characters at runtime. Note that Constant: String pieces can only store 1024 characters inside the spell, because the value is saved with the spell itself.",
                "String 型は文字列を扱うための型です. ID, 名前, NBTパス, 検索条件, 表示用メッセージなどをスペル内で扱うために使用します.$(p)例えば, ブロックやアイテムのIDを String として取得し, 特定のIDと一致するかを調べることができます.$(p)実行時の String 値は 8192 文字までに制限されています. なお, 定数子: 文字列 は術式に値を保存するため, 1つのピースあたり最大1024文字までしか入力できません.");
        addLocalized(root, "psi.book.page.psitweaks_programming_types.2",
                "Block represents a block in the world. It provides information such as position, dimension, block state, registry ID, tags, and display name.$(p)A Vector represents coordinates, while a Block represents information about the block at those coordinates.",
                "Block 型はワールド上のブロックを扱うための型です. ブロックの座標, ディメンション, ブロック状態, レジストリID, タグ, 表示名などの情報を扱えます.$(p)Vector が「座標」を表すのに対し, Block は「その座標に存在するブロックの情報」を表します.");
        addLocalized(root, "psi.book.page.psitweaks_programming_types.3",
                "Since both relate to positions, many inputs convert Block and Vector automatically.$(p)A Block connected to a Vector input uses its position, as does each block of a Block List in a Vector List input. A Vector connected to a Block input reads the block there when the spell runs.$(p)Any inputs, such as Operator: To String, keep the original type.",
                "Block と Vector はどちらも座標に関係するため, 多くの入力で自動変換されます.$(p)Block を Vector 入力へ接続すると座標として扱われます (Block List と Vector List も同様). Vector を Block 入力へ接続すると, 実行時にその座標のブロックを読み取ります.$(p)演算子: 文字列へ変換 などの Any 入力では, 元の型がそのまま扱われます.");
        addLocalized(root, "psi.book.page.psitweaks_programming_types.4",
                "Item represents an ItemStack, for inspecting its type, count, NBT, display name and so on. It can be read from held items, inventory slots, block inventories or dropped items.",
                "Item 型は ItemStack を扱う型で, アイテムの種類, 個数, NBT, 表示名などを調べるために使います. 手持ちのアイテム, 指定スロットのアイテム, ブロック内のインベントリ, ドロップアイテムなどから取得できます.");
        addLocalized(root, "psi.book.page.psitweaks_programming_types.5",
                "PsiTweaks divides values into Plain Values and Contextual Values.$(p)Plain Values do not depend on world state, such as Number, Vector, and String.$(p)Contextual Values depend on a world or target context, such as Entity, Block, and Item.",
                "PsiTweaks の値は Plain Value と Contextual Value に分けられます.$(p)Plain Value はワールドの状態に依存しない値で, Number, Vector, String などが該当します.$(p)Contextual Value はワールドや対象に依存する値で, Entity, Block, Item などが該当します.");
        addLocalized(root, "psi.book.page.psitweaks_programming_types.6",
                "Matrix is a Plain Value type that represents a two-dimensional grid of numbers. It can be stored in CAD memory and converted to a String, just like Number or Vector.$(p)Matrices support sizes from 1×1 up to 4×4. The dedicated matrix operators allow you to build matrices from Number Lists or Vectors, add and multiply matrices, extract rows and columns, compute transposes, determinants, and inverses, and transform vectors with 3×3 or 4×4 matrices.",
                "Matrix 型は数値の2次元配列（行列）を表す Plain Value 型です. Number や Vector と同様に, CAD メモリへの保存や文字列への変換が可能です.$(p)1×1 から 4×4 までのサイズに対応しています. 専用の行列演算子を使うと, Number List や Vector から行列を作ったり, 行列の加算や乗算, 行や列の取り出し, 転置・行列式・逆行列の計算, 3×3 や 4×4 行列によるベクトル変換などが行えます.");

        addLocalized(root, "psi.book.entry.psitweaks_lists",
                "About Lists",
                "List について");
        addLocalized(root, "psi.book.page.psitweaks_lists.0",
                "A List groups multiple values of the same type.$(p)Psi mostly uses Entity List; PsiTweaks also supports String List, Number List, Vector List, Item List, and Block List.",
                "List は同じ種類の値を複数まとめて扱う型です.$(p)Psi では Entity List がよく使われますが, PsiTweaks では String List, Number List, Vector List, Item List, Block List も扱えるようになります.");
        addLocalized(root, "psi.book.page.psitweaks_lists.2",
                "PsiTweaks adds operators for working with Lists.$(p)They can retrieve elements or list size, add, remove, insert, slice, search, exclude, intersect, and concatenate values.",
                "PsiTweaks には List を操作するための演算子が追加されています.$(p)要素の取得, 要素数の取得, 追加, 削除, 挿入, 切り取り, 検索, 除外, 交差, 結合などを行えます.");

        addLocalized(root, "psi.book.entry.psitweaks_mode_selection",
                "Mode-Selectable Spell Pieces",
                "モード選択スペルピース");
        addLocalized(root, "psi.book.page.psitweaks_mode_selection.0",
                "Some PsiTweaks spell pieces support mode selection.$(p)A mode-selectable spell piece can switch the type it handles without changing to a different piece.",
                "一部の PsiTweaks のスペルピースには, モード選択機能があります.$(p)モード選択に対応したスペルピースは, 同じピースでも扱う型を切り替えることができます.");
        addLocalized(root, "psi.book.page.psitweaks_mode_selection.1",
                "For example, List operators can switch between String List, Number List, Vector List, Item List, and Block List.$(p)The selected mode changes the input and output types of operations such as Add to List and List Concatenation.",
                "例えば List 操作系のスペルピースでは, String List, Number List, Vector List, Item List, Block List などをモードで切り替えて使用します.$(p)同じ「リストへ追加」や「リスト結合」でも, 選択しているモードによって入力と出力の型が変わります.");
        addLocalized(root, "psi.book.page.psitweaks_mode_selection.2",
                "To choose a mode, left-click the spell piece in the Spell Programmer.$(p)The current mode appears on the piece and in its tooltip. If a connection fails, check that the piece mode matches the type of the connected value.",
                "モードは Spell Programmer 上で対象のピースを左クリックして選択します.$(p)現在のモードはピース上の表示やツールチップで確認できます. 接続できない場合は, ピースのモードと接続先の型が一致しているか確認してください.");
    }

    private void addLocalized(JsonObject root, String key, String enUs, String jaJp) {
        root.addProperty(key, switch (locale) {
            case "ja_jp" -> jaJp;
            default -> enUs;
        });
    }

    private void addMachineTranslations(JsonObject root) {
        root.addProperty("psi.book.category.psitweaks_machines", switch (locale) {
            case "ja_jp" -> "PsiTweaks機械";
            default -> "PsiTweaks Machines";
        });
        root.addProperty("psi.book.category.psitweaks_machines.desc", switch (locale) {
            case "ja_jp" -> "PsiTweaks が追加する機械です. Psi, FE, Mekanism 資源を扱う加工・研究装置が含まれます.";
            default -> "Machines added by PsiTweaks. They cover processing and research workflows involving Psi, FE, and Mekanism resources.";
        });
        root.addProperty("container.psitweaks.program_researcher", switch (locale) {
            case "ja_jp" -> "プログラム研究台";
            default -> "Program Research Table";
        });
        root.addProperty("container.psitweaks.sculk_eroder", switch (locale) {
            case "ja_jp" -> "スカルク侵食機";
            default -> "Sculk Eroder";
        });
        root.addProperty("container.psitweaks.material_mutator", switch (locale) {
            case "ja_jp" -> "物質変成機";
            default -> "Material Mutator";
        });
        root.addProperty("container.psitweaks.psionic_generator", switch (locale) {
            case "ja_jp" -> "サイリンク発電機";
            default -> "Psi-Link Generator";
        });
        root.addProperty("container.psitweaks.transcendent_energy_cube", switch (locale) {
            case "ja_jp" -> "超越エネルギーキューブ";
            default -> "Transcendent Energy Cube";
        });
        root.addProperty("container.psitweaks.idea_storage", switch (locale) {
            case "ja_jp" -> "イデアストレージ";
            default -> "Ideaspace Storage";
        });
        root.addProperty("gui.psitweaks.idea_storage.search", switch (locale) {
            case "ja_jp" -> "検索...";
            default -> "Search...";
        });
        root.addProperty("gui.psitweaks.idea_storage.load_failed", switch (locale) {
            case "ja_jp" -> "ストレージデータを読み込めません(新しい形式のデータです)";
            default -> "Storage data could not be loaded (newer data format)";
        });
        root.addProperty("gui.psitweaks.idea_storage.craft_window", switch (locale) {
            case "ja_jp" -> "作業台を開く";
            default -> "Open Crafting Table";
        });
        root.addProperty("gui.psitweaks.idea_storage.craft_clear", switch (locale) {
            case "ja_jp" -> "クラフトグリッドをストレージへ戻す";
            default -> "Return Crafting Grid to Storage";
        });
        root.addProperty("gui.psitweaks.idea_storage.rows_add", switch (locale) {
            case "ja_jp" -> "表示行数を増やす";
            default -> "Increase Grid Rows";
        });
        root.addProperty("gui.psitweaks.idea_storage.rows_remove", switch (locale) {
            case "ja_jp" -> "表示行数を減らす";
            default -> "Decrease Grid Rows";
        });
        root.addProperty("gui.psitweaks.idea_storage.sort", switch (locale) {
            case "ja_jp" -> "ソート: %s";
            default -> "Sort: %s";
        });
        root.addProperty("gui.psitweaks.idea_storage.sort.port", switch (locale) {
            case "ja_jp" -> "ポート番号順";
            default -> "Port Number";
        });
        root.addProperty("gui.psitweaks.idea_storage.sort.item_id", switch (locale) {
            case "ja_jp" -> "資源ID順";
            default -> "Resource ID";
        });
        root.addProperty("gui.psitweaks.idea_storage.sort.mod_id", switch (locale) {
            case "ja_jp" -> "Mod ID順";
            default -> "Mod ID";
        });
        root.addProperty("gui.psitweaks.idea_storage.sort.count", switch (locale) {
            case "ja_jp" -> "保管量順";
            default -> "Stored Amount";
        });
        root.addProperty("gui.psitweaks.idea_storage.info.energy", switch (locale) {
            case "ja_jp" -> "電力: %s / %s FE";
            default -> "Energy: %s / %s FE";
        });
        root.addProperty("gui.psitweaks.idea_storage.info.item_types", switch (locale) {
            case "ja_jp" -> "アイテム: %s/%s 種類";
            default -> "Items: %s/%s types";
        });
        root.addProperty("gui.psitweaks.idea_storage.info.fluid_types", switch (locale) {
            case "ja_jp" -> "液体: %s/%s 種類";
            default -> "Fluids: %s/%s types";
        });
        root.addProperty("gui.psitweaks.idea_storage.info.chemical_types", switch (locale) {
            case "ja_jp" -> "化学物質: %s/%s 種類";
            default -> "Chemicals: %s/%s types";
        });
        root.addProperty("gui.psitweaks.idea_storage.energy", switch (locale) {
            case "ja_jp" -> "FE";
            default -> "FE";
        });
        root.addProperty("gui.psitweaks.idea_storage.energy_amount", switch (locale) {
            case "ja_jp" -> "内容量: %s FE";
            default -> "Stored: %s FE";
        });
        root.addProperty("gui.psitweaks.idea_storage.fluid_amount", switch (locale) {
            case "ja_jp" -> "%s B";
            default -> "%s B";
        });
        root.addProperty("gui.psitweaks.idea_storage.chemical_amount", switch (locale) {
            case "ja_jp" -> "%s B";
            default -> "%s B";
        });
        root.addProperty("gui.psitweaks.idea_storage.item_amount", switch (locale) {
            case "ja_jp" -> "%s 個";
            default -> "%s items";
        });
        root.addProperty("gui.psitweaks.idea_storage.transfer_missing", switch (locale) {
            case "ja_jp" -> "材料が足りません";
            default -> "Not enough ingredients";
        });
        root.addProperty("description.psitweaks.program_researcher", switch (locale) {
            case "ja_jp" -> "素材と電力からプログラムアイテムを研究します";
            default -> "Researches program items from materials and power";
        });
        root.addProperty("description.psitweaks.sculk_eroder", switch (locale) {
            case "ja_jp" -> "石・土・砂系のブロックをスカルクへ侵食加工します";
            default -> "Corrodes stone, dirt, and sand type blocks into Sculk";
        });
        root.addProperty("description.psitweaks.material_mutator", switch (locale) {
            case "ja_jp" -> "電力と気化サイオニックエコーで物質変成を行います";
            default -> "Performs material mutation using FE and vaporized Psionic Echo";
        });
        root.addProperty("description.psitweaks.psionic_generator", switch (locale) {
            case "ja_jp" -> "所有者の Psi を消費し、リンク中のみ FE に変換します";
            default -> "Consumes the owner's Psi and converts it into FE while linked";
        });
        root.addProperty("description.psitweaks.transcendent_universal_cable", switch (locale) {
            case "ja_jp" -> "究極ユニバーサルケーブルの128倍の容量を持つエネルギーケーブルです";
            default -> "An energy cable with 128 times the capacity of Ultimate Universal Cable";
        });
        root.addProperty("description.psitweaks.transcendent_energy_cube", switch (locale) {
            case "ja_jp" -> "究極エネルギーキューブの128倍の容量と出力を持つ蓄電ブロックです";
            default -> "An energy cube with 128 times the storage and output of Ultimate Energy Cube";
        });
        root.addProperty("jei.psitweaks.program_research", switch (locale) {
            case "ja_jp" -> "プログラム研究";
            default -> "Program Research";
        });
        root.addProperty("jei.psitweaks.material_mutation", switch (locale) {
            case "ja_jp" -> "物質変成";
            default -> "Material Mutation";
        });
        root.addProperty("jei.psitweaks.graviton_factor_acquisition", switch (locale) {
            case "ja_jp" -> "落下変換";
            default -> "Fall Conversion";
        });
        root.addProperty("jei.psitweaks.graviton_factor_acquisition.fall", switch (locale) {
            case "ja_jp" -> "アイテム状態で380ブロック落下";
            default -> "Fall 380 blocks as an item";
        });
        root.addProperty("jei.psitweaks.program_research.energy", switch (locale) {
            case "ja_jp" -> "消費電力: %s FE/t";
            default -> "Energy usage: %s FE/t";
        });
        root.addProperty("jei.psitweaks.program_research.time", switch (locale) {
            case "ja_jp" -> "研究時間: %s分 %s秒";
            default -> "Time: %s min %s sec";
        });
        root.addProperty("gui.psitweaks.psionic_generator.toggle", switch (locale) {
            case "ja_jp" -> "切替";
            default -> "Toggle";
        });
        root.addProperty("gui.psitweaks.psionic_generator.owner", switch (locale) {
            case "ja_jp" -> "所有者: %s";
            default -> "Owner: %s";
        });
        root.addProperty("gui.psitweaks.psionic_generator.owner_online", switch (locale) {
            case "ja_jp" -> "オンライン";
            default -> "Online";
        });
        root.addProperty("gui.psitweaks.psionic_generator.owner_offline", switch (locale) {
            case "ja_jp" -> "オフライン";
            default -> "Offline";
        });
        root.addProperty("gui.psitweaks.psionic_generator.link_on", switch (locale) {
            case "ja_jp" -> "有効";
            default -> "Enabled";
        });
        root.addProperty("gui.psitweaks.psionic_generator.link_off", switch (locale) {
            case "ja_jp" -> "無効";
            default -> "Disabled";
        });
        root.addProperty("gui.psitweaks.psionic_generator.summary_status", "%1$s / %2$s");
        root.addProperty("gui.psitweaks.psionic_generator.summary_psi", "Psi: %1$s/%2$s");
        root.addProperty("gui.psitweaks.psionic_generator.psi", "Psi: %s / %s");
        root.addProperty("gui.psitweaks.psionic_generator.consume", switch (locale) {
            case "ja_jp" -> "消費: %s Psi/t";
            default -> "Consume: %s Psi/t";
        });
        root.addProperty("gui.psitweaks.psionic_generator.generate", switch (locale) {
            case "ja_jp" -> "発電: %1$s / %2$s FE/t";
            default -> "Producing: %1$s / %2$s FE/t";
        });
        root.addProperty("psi.book.page.psitweaks_machine.program_researcher", switch (locale) {
            case "ja_jp" -> "PsiTweaks のプログラムアイテムを作成するための電力式研究台です. 入力スロットに必要素材を入れてFEを供給すると, 研究完了時にプログラムを出力します.$(p)各研究の素材, tickあたりの消費電力, 時間はJEIのプログラム研究レシピで確認できます.";
            default -> "A powered research table for producing PsiTweaks program items. Put the required ingredients in the input slots and supply FE; completed research outputs the program item.$(p)JEI shows each research recipe's energy usage per tick and time.";
        });
        root.addProperty("psi.book.page.psitweaks_machine.sculk_eroder", switch (locale) {
            case "ja_jp" -> "石, 土, 砂系などのブロックアイテムをスカルク系の出力へ侵食加工する機械です.";
            default -> "A machine that corrodes stone, dirt, sand, and related block items into Sculk outputs.";
        });
        root.addProperty("psi.book.page.psitweaks_machine.material_mutator", switch (locale) {
            case "ja_jp" -> "$(item)気化サイオニックエコー$(0) とFEを使って物質変成を行う機械です.$(p)$(l:psitweaks_spell_pieces/trick_material_mutation)$(o)作動式: 物質変成$(0)$(/l) でも行える変成を自動化し, 翡翠やヒュポスタシスジェムなどを加工できます.";
            default -> "A machine that performs material mutation with $(item)Psionic Echo Gas$(0) and FE.$(p)It automates mutations that can also be produced by $(l:psitweaks_spell_pieces/trick_material_mutation)$(o)Trick: Material Mutation$(0)$(/l), such as Jade and Hypostasis Gems.";
        });
        root.addProperty("psi.book.page.psitweaks_machine.psionic_generator", switch (locale) {
            case "ja_jp" -> "所有者にリンクし, 所有者がオンラインのときにPsiをFEへ変換する発電機です.$(p)GUIでリンクの有効化とtickあたりのPsi消費量を設定できます. 発電したFEは正面以外の面へ最大6400 FE/tickで自動出力します.";
            default -> "A generator that links to its owner and converts that player's Psi into FE while the owner is online.$(p)Use the GUI to enable the link and set the Psi consumed per tick. Generated FE auto-outputs from every side except the front, up to 6400 FE/tick.";
        });
        root.addProperty("psi.book.page.psitweaks_machine.transcendent_universal_cable", switch (locale) {
            case "ja_jp" -> "Mekanism のユニバーサルケーブルの強化版です. 究極ケーブルの128倍の性能を持ち, 核融合炉級の電力出力を扱うのに向きます.";
            default -> "An upgraded version of Mekanism's Universal Cable. It has 128 times the performance of the Ultimate Cable, making it suitable for fusion reactor-class power output.";
        });
        root.addProperty("psi.book.page.psitweaks_machine.transcendent_energy_cube", switch (locale) {
            case "ja_jp" -> "Mekanism のエネルギーキューブの強化版です. 究極エネルギーキューブの128倍の容量と出力性能を持ち, 核分裂炉級の電力を蓄えるのに向きます.";
            default -> "An upgraded version of Mekanism's Energy Cube. It has 128 times the capacity and output of the Ultimate Energy Cube, making it suitable for buffering fission reactor-class power.";
        });
        root.addProperty("psi.book.page.psitweaks_item.blank_program", switch (locale) {
            case "ja_jp" -> "プログラム研究の基礎になる空白のプログラムです. $(l:psitweaks_machines/program_researcher)$(o)$(item)プログラム研究台$(0)$(/l) に素材と一緒に入れることで, 記述済みプログラムを作成できます.$(p)既存のプログラムとクラフトすると, そのプログラムを複製できます.";
            default -> "Used as the base item for program research. Put it into the $(l:psitweaks_machines/program_researcher)$(o)$(item)Program Research Table$(0)$(/l) with the required materials to create a written program.$(p)It can also be crafted with an existing written program item to duplicate that program.";
        });
    }

    private void addSpellUnlockMessages(JsonObject root) {
        root.addProperty("message.psitweaks.spell_unlock.unlocked", switch (locale) {
            case "ja_jp" -> "%s を解禁しました";
            default -> "Unlocked %s";
        });
        root.addProperty("message.psitweaks.spell_unlock.already", switch (locale) {
            case "ja_jp" -> "%s は既に解禁済みです";
            default -> "%s is already unlocked";
        });
        root.addProperty("message.psitweaks.spell_unlock.command.grant.single", switch (locale) {
            case "ja_jp" -> "%s の解禁を %s に付与しました";
            default -> "Granted %s unlock to %s";
        });
        root.addProperty("message.psitweaks.spell_unlock.command.grant.multi", switch (locale) {
            case "ja_jp" -> "%s の解禁を %s/%s 人に付与しました";
            default -> "Granted %s unlock to %s/%s players";
        });
        root.addProperty("message.psitweaks.spell_unlock.command.revoke.single", switch (locale) {
            case "ja_jp" -> "%s の解禁を %s から剥奪しました";
            default -> "Revoked %s unlock from %s";
        });
        root.addProperty("message.psitweaks.spell_unlock.command.revoke.multi", switch (locale) {
            case "ja_jp" -> "%s の解禁を %s/%s 人から剥奪しました";
            default -> "Revoked %s unlock from %s/%s players";
        });
        root.addProperty("message.psitweaks.spell_unlock.command.status.unlocked", switch (locale) {
            case "ja_jp" -> "%s は %s を解禁済みです";
            default -> "%s has %s unlocked";
        });
        root.addProperty("message.psitweaks.spell_unlock.command.status.locked", switch (locale) {
            case "ja_jp" -> "%s は %s が未解禁です";
            default -> "%s has %s locked";
        });
        root.addProperty("message.psitweaks.spell_unlock.command.no_change", switch (locale) {
            case "ja_jp" -> "%s の解禁状態は変更されませんでした";
            default -> "No %s unlock status was changed";
        });
        root.addProperty("message.psitweaks.spell_unlock.command.unknown", switch (locale) {
            case "ja_jp" -> "不明な術式解禁 ID です: %s";
            default -> "Unknown spell unlock: %s";
        });
        root.addProperty("message.psitweaks.spell_unlock.command.all.no_change", switch (locale) {
            case "ja_jp" -> "全術式の解禁状態は変更されませんでした（対象%s人・術式%s個）";
            default -> "No all-spell unlock status was changed (%s player(s), %s spell(s))";
        });
        root.addProperty("message.psitweaks.spell_unlock.command.all.grant.single", switch (locale) {
            case "ja_jp" -> "%s に全術式の解禁を付与しました（%s/%s 件変更）";
            default -> "Granted all spell unlocks to %s (%s/%s changed)";
        });
        root.addProperty("message.psitweaks.spell_unlock.command.all.grant.multi", switch (locale) {
            case "ja_jp" -> "全術式の解禁を付与しました（%s/%s 件変更、対象変更%s/%s人）";
            default -> "Granted all spell unlocks: %s/%s changes, players changed %s/%s";
        });
        root.addProperty("message.psitweaks.spell_unlock.command.all.revoke.single", switch (locale) {
            case "ja_jp" -> "%s から全術式の解禁を剥奪しました（%s/%s 件変更）";
            default -> "Revoked all spell unlocks from %s (%s/%s changed)";
        });
        root.addProperty("message.psitweaks.spell_unlock.command.all.revoke.multi", switch (locale) {
            case "ja_jp" -> "全術式の解禁を剥奪しました（%s/%s 件変更、対象変更%s/%s人）";
            default -> "Revoked all spell unlocks: %s/%s changes, players changed %s/%s";
        });
        root.addProperty("message.psitweaks.spell_unlock.command.all.status", switch (locale) {
            case "ja_jp" -> "%s は術式を %s/%s 解禁済みです";
            default -> "%s has %s/%s spell unlocks";
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
        // Patchouli のエントリ一覧だけで使う短縮名。ピース名やエントリ本文の見出しは変えない。
        String name = locale.equals("ja_jp") ? jaJp : enUs;
        String prefix = locale.equals("ja_jp") ? JA_IDEA_STORAGE_PREFIX : EN_IDEA_STORAGE_PREFIX;
        String listPrefix = locale.equals("ja_jp") ? JA_IDEA_STORAGE_LIST_PREFIX : EN_IDEA_STORAGE_LIST_PREFIX;
        if (name.contains(prefix)) {
            root.addProperty("psitweaks.spellpiece." + id + ".book_list", name.replace(prefix, listPrefix));
        }
    }

    private static final String JA_IDEA_STORAGE_PREFIX = "イデアストレージ-";
    private static final String JA_IDEA_STORAGE_LIST_PREFIX = "IS-";
    private static final String EN_IDEA_STORAGE_PREFIX = "Ideaspace Storage - ";
    private static final String EN_IDEA_STORAGE_LIST_PREFIX = "IS - ";

    private void addBookPage(JsonObject root, String id, String enUs, String jaJp) {
        root.addProperty("psi.book.page.psitweaks_spellpiece." + id, switch (locale) {
            case "ja_jp" -> jaJp;
            default -> enUs;
        });
    }

    private void addSpellPieces(JsonObject root) {
        addSpellPiece(root, "trick_explode_no_destroy", "Trick: Antipersonnel Explode", "Cause an explosion that does not destroy blocks", "作動式: 対人爆発", "ブロックを破壊しない爆発を起こす");
        addSpellPiece(root, "trick_barrier", "Trick: Barrier", "Raises a shield that takes damage in the target's place", "作動式: 障壁", "対象に、受けるダメージを肩代わりするシールドを張る");
        addSpellPiece(root, "trick_hardening", "Trick: Hardening", "When taking heavy damage, reduce it to a certain value", "作動式: 硬化", "大ダメージを受けた時、一定値まで減少させる");
        addSpellPiece(root, "trick_parade", "Trick: Parade", "Avoid damage with a certain probability", "作動式: パレード", "確率で被ダメージを回避する");
        addSpellPiece(root, "trick_flight", "Trick: Flight", "Applies the Flight effect to the target. Creative flight is enabled by continuously consuming Psi.", "作動式: 飛行", "対象に飛行エフェクトを付与します。Psiを消費し続けることでクリエイティブ飛行を可能にします。");
        addSpellPiece(root, "trick_interact_block", "Trick: Block Interact", "Right-click with the off-hand item on the target block", "作動式: ブロック作用", "座標のブロックに、オフハンドのアイテムで右クリックの動作を行う");
        addSpellPiece(root, "trick_freeze_block", "Trick: Block Freeze", "Freeze the block at the target position into its next colder form", "作動式: ブロック凍結", "指定座標のブロックを次の凍結段階に変化させる");
        addSpellPiece(root, "trick_melt_block", "Trick: Block Melt", "Melt the block at the target position into its hotter form", "作動式: ブロック溶解", "指定座標のブロックを溶解して高温の状態に変化させる");
        addSpellPiece(root, "trick_pulsar", "Trick: Conjure Pulsar", "Conjures a redstone emitting block in the given position", "作動式: 魔法パルサーブロック生成", "指定位置にレッドストーン信号を発するブロックを生成します。");
        addSpellPiece(root, "trick_pulsar_sequence", "Trick: Conjure Pulsar Sequence", "Conjures a sequence of redstone emitting blocks", "作動式: 魔法パルサーブロック生成(列)", "レッドストーン信号を発するブロックを連続して生成します。");
        addSpellPiece(root, "trick_pulsar_light", "Trick: Conjure Pulsar Light", "Conjures a redstone emitting light in the given position", "作動式: 魔法パルサー光源生成", "指定位置にレッドストーン信号を発する光を生成します。");
        addSpellPiece(root, "trick_break_fortune", "Trick: Break Block (Fortune)", "Break a block with Fortune applied", "作動式: ブロック破壊(幸運)", "幸運付きでブロックを破壊する");
        addSpellPiece(root, "trick_break_silk", "Trick: Break Block (Silk Touch)", "Break a block with Silk Touch applied", "作動式: ブロック破壊(シルクタッチ)", "シルクタッチ付きでブロックを破壊する");
        addSpellPiece(root, "trick_store_entity", "Trick: Store Entity", "Store the entity's UUID string in the CAD memory", "作動式: エンティティ保存", "エンティティのUUIDをStringとしてCADメモリに保存する");
        addSpellPiece(root, "selector_stored_entity", "Selector: Stored Entity", "Retrieve entities from the UUID stored in the CAD memory", "取得子: 保存されたエンティティ", "CADのメモリに保存されたUUIDからエンティティを取得する");
        addSpellPiece(root, "trick_store_value", "Trick: Store Value", "Store a Plain Value (Number, Vector, String ... ) in CAD memory. Strings are stored up to 128 characters.", "作動式: 値を保存", "Plain Value (Number, Vector, String ... )をCADメモリに保存する。Stringは最大128文字まで保存します。");
        addSpellPiece(root, "selector_stored_value", "Selector: Stored Value", "Retrieve a Plain Value from CAD memory", "取得子: 保存された値", "CADメモリに保存されたPlain Valueを取得する");
        addSpellPiece(root, "selector_nearby_spellgram", "Selector: Nearby SpellGram Object", "Retrieve SpellGram objects around the specified position", "取得子: 近くの魔法式オブジェクト", "指定座標の周囲にある魔法式オブジェクトを取得する");
        addSpellPiece(root, "trick_dispel", "Trick: Dispel", "Remove effects from target entity", "作動式: 解呪", "対象からエフェクトを除去する");
        addSpellPiece(root, "trick_dispel_beneficial", "Trick: Dispel Beneficial", "Remove beneficial effects from target entity", "作動式: 良性解呪", "対象から有益なエフェクトを除去する");
        addSpellPiece(root, "trick_dispel_non_beneficial", "Trick: Dispel Non Beneficial", "Remove non beneficial effects from target entity", "作動式: 悪性解呪", "対象から有益でないエフェクトを除去する");
        addSpellPiece(root, "trick_cocytus", "Trick: Cocytus", "Permanently freeze the target mob's mind", "作動式: コキュートス", "対象のモブの精神を永久に凍結させる");
        addSpellPiece(root, "trick_supply_fe", "Trick: FE Charge", "Supplies FE to blocks. Direction can be specified.", "作動式: FE供給", "ブロックにFEを供給します。方向の指定が可能です。");
        addSpellPiece(root, "trick_time_accelerate", "Trick: Time Accelerate", "Accelerates the time of the target block", "作動式: 時間加速", "対象のブロックの時を加速させます。");
        addSpellPiece(root, "trick_phonon_maser", "Trick: Phonon Maser", "Vibrates ultrasonic waves to emit heat rays", "作動式: フォノンメーザー", "超音波を振動させ熱線を放出する");
        addSpellPiece(root, "trick_meteor_line", "Trick: Meteor Line", "Creates a line that permeates light and pierces living beings on its path", "作動式: 流星群", "光を透過させるラインを作り出し経路上の生物を穿つ");
        addSpellPiece(root, "trick_supreme_infusion", "Trick: Supreme Infusion", "Infuse Echo Shards into Psionic Echoes", "作動式: 超位注入", "残響の欠片に注入してサイオニックエコーにします");
        addSpellPiece(root, "trick_molecular_divider", "Trick: Molecular Divider", "divide the living creatures within the area", "作動式: 分子ディバイダー", "三点で作られた平面で生物を切断する");
        addSpellPiece(root, "trick_aqua_cutter", "Trick: Aqua Cutter", "Launches a water blade projectile that damages on hit", "作動式: アクアカッター", "水刃の発射体を前方に放ち、命中した対象にダメージを与える");
        addSpellPiece(root, "trick_dry_meteor", "Trick: Dry Meteor", "Launches dry ice forward and deals freezing damage on hit", "作動式: ドライミーティア", "ドライアイスを前方に射出し、命中した対象に凍結属性ダメージを与える");
        addSpellPiece(root, "trick_blaze_ball", "Trick: Blaze Ball", "Launches a fireball forward that deals fire damage on hit", "作動式: ブレイズボール", "前方へ火の玉を発射し、命中した対象へ炎属性ダメージを与える");
        addSpellPiece(root, "trick_radiation_injection", "Trick: Radiation Injection", "Applies radiation exposure to the target", "作動式: 放射線注入", "対象を被ばくさせる");
        addSpellPiece(root, "trick_radiation_filter", "Trick: Radiation Filter", "Applies a radiation filter effect to the target", "作動式: 放射線フィルタ", "対象に放射線防護効果を付与する");
        addSpellPiece(root, "trick_cure_radiation", "Trick: Cure Radiation", "Removes radiation exposure from the target", "作動式: 放射線除去", "対象の被ばく量を除去する");
        addSpellPiece(root, "trick_guillotine", "Trick: Guillotine", "Deals a heavy slash to the target and drops a head on kill", "作動式: ギロチン", "対象に強力な斬撃ダメージを与え、討伐時に頭をドロップさせる");
        addSpellPiece(root, "trick_chain_lightning", "Trick: Chain Lightning", "Strikes the target with lightning that jumps to nearby hostile creatures", "作動式: 連鎖雷撃", "対象に雷撃を放ち、近くの敵対的な生物へ連鎖させる");
        addSpellPiece(root, "trick_mine_genesis", "Trick: Mine Genesis", "Shakes the ground so a shockwave spreads out and throws up whatever stands on it", "作動式: 地雷原", "地面を振動させて衝撃波を広げ、地上の生物を打ち上げる");
        addSpellPiece(root, "trick_burst", "Trick: Burst", "Vaporizes the liquid inside the target so it bursts from within", "作動式: 爆裂", "対象の体内の液体を気化させ、内側から破裂させる");
        addSpellPiece(root, "trick_kamaitachi", "Trick: Kamaitachi", "Fires a crescent blade of wind that cuts through everything in its path", "作動式: 鎌鼬", "三日月形の風の刃を飛ばし、通り道の生物をまとめて切り裂く");
        addSpellPiece(root, "trick_dancing_blades", "Trick: Dancing Blades", "Summons blades that circle the caster and strike nearby hostile creatures", "作動式: ダンシングブレイズ", "術者の周りを舞い、近くの敵対的な生物を斬りつける刃を呼び出す");
        addSpellPiece(root, "trick_starfall", "Trick: Starfall", "Calls meteors down around the position", "作動式: スターフォール", "指定位置の周りに流星を降らせる");
        addSpellPiece(root, "trick_gravity_collapse", "Trick: Gravity Collapse", "Opens a gravity well that pulls creatures in, then collapses", "作動式: 重力崩壊", "生物を引き寄せる重力の井戸を開き、最後に崩壊させる");
        addSpellPiece(root, "trick_active_air_mine", "Trick: Active Air Mine", "Deals magic damage to entities inside a spherical area around the target position", "作動式: 能動空中機雷", "指定座標を中心とした範囲内の生物にダメージを与える");
        addSpellPiece(root, "trick_flare_circle", "Trick: Fire Circle", "Places a flare Spell Gram Circle that repeatedly deals fire damage around it", "作動式: ファイアサークル", "炎の魔法式オブジェクトを設置して内部の生物に継続的な炎ダメージを与える");
        addSpellPiece(root, "trick_ice_circle", "Trick: Ice Circle", "Places an ice Spell Gram Circle that repeatedly deals freeze damage around it", "作動式: アイスサークル", "氷の魔法式オブジェクトを設置して内部の生物に継続的な凍結ダメージを与える");
        addSpellPiece(root, "trick_set_spellgram_follow_target", "Trick: Set SpellGram Follow Target", "Sets the follow target entity for a SpellGram object", "作動式: 魔法式追従", "魔法式オブジェクトの追従対象エンティティを設定する");
        addSpellPiece(root, "trick_die_flex", "Trick: Flexible Die", "Stops the spell when given a number whose absolute value is below 1, and refunds the Psi of the pieces not yet run. In spells cast every tick, the Psi display may briefly desync.", "作動式: 柔軟停止", "絶対値が1未満の数値を受け取ると停止し、未実行分のPsiを返却します。毎tick詠唱では、Psi表示が一時的にずれることがあります。");
        addSpellPiece(root, "trick_jump", "Trick: Jump", "If the target number is unset or its absolute value is less than 1, jumps forward to the next Jump Anchor with the same constant label.", "作動式: ジャンプ", "対象数値が未入力、または絶対値が1未満なら、同じ定数ラベルを持つ次のジャンプアンカーまで前方にジャンプします。");
        addSpellPiece(root, "trick_jump_flex", "Trick: Flexible Jump", "Behaves like Trick: Jump and refunds the Psi of the pieces it skips.", "作動式: 柔軟ジャンプ", "作動式: ジャンプと同じ動作をし、飛ばしたピースのPsiを返却します。");
        addSpellPiece(root, "trick_switch", "Trick: Switch", "Jumps forward to the next Jump Anchor with the same constant label as the input String. If no matching anchor exists, execution continues.", "作動式: スイッチ", "入力Stringと同じ定数ラベルを持つ次のジャンプアンカーまで前方にジャンプします。一致するアンカーがない場合はそのまま次へ進みます。");
        addSpellPiece(root, "trick_switch_flex", "Trick: Flexible Switch", "Behaves like Trick: Switch and refunds the Psi of the pieces it skips.", "作動式: 柔軟スイッチ", "作動式: スイッチと同じ動作をし、飛ばしたピースのPsiを返却します。");
        addSpellPiece(root, "jump_anchor", "Jump Anchor", "A no-op marker used as the destination for Trick: Jump and Trick: Switch. It can take an optional constant label.", "ジャンプアンカー", "作動式: ジャンプと作動式: スイッチの到達点になる、何もしない目印です。任意の定数ラベルを指定できます。");
        addSpellPiece(root, "safety", "Safety", "If the caster's Psi plus the CAD's stored Psi can't cover the cost, the spell fails without using any Psi.", "安全装置", "術者とCADのPsiを合わせても消費量に足りないとき、Psiを使わずに不発にします。");
        addSpellPiece(root, "trick_material_mutation", "Trick: Material Mutation", "Acts on a specific block, alters its material structure, and transmutes it into a different substance.", "作動式: 物質変成", "特定のブロックに作用して物質構造を改変し異なる物質に変成させる");
        addSpellPiece(root, "trick_physical_propulsion", "Trick: Physical Propulsion", "Raycasts from the specified position in the Ray direction; if it hits a Simulated Contraption, applies propulsion to it.", "作動式: 物理推進", "指定位置からRay方向へレイキャストし、Simulated Contraption に命中したならば推進力を与える");
        addSpellPiece(root, "trick_pull_item", "Trick: Pull Item", "Moves items from the specified face of the block at Position into the caster's inventory, up to one stack or the count given by Max.", "作動式: アイテム引き出し", "位置のブロックの指定面から、術者のインベントリへアイテムを移します。上限は1スタック、または「最大」で指定した個数です。");
        addSpellPiece(root, "trick_send_item", "Trick: Send Item", "Moves items from the caster's inventory into the specified face of the block at Position, up to one stack or the count given by Max. CADs are skipped.", "作動式: アイテム転送", "術者のインベントリから、位置のブロックの指定面へアイテムを移します。上限は1スタック、または「最大」で指定した個数です。CADは移しません。");
        addSpellPiece(root, "operator_tan", "Operator: Tangent", "tan(A)", "演算子: タンジェント", "tan(A)");
        addSpellPiece(root, "operator_atan", "Operator: Arc Tangent", "atan(A)", "演算子: アークタンジェント", "atan(A)");
        addSpellPiece(root, "operator_sinh", "Operator: Hyperbolic Sine", "sinh(A)", "演算子: ハイパボリックサイン", "sinh(A)");
        addSpellPiece(root, "operator_cosh", "Operator: Hyperbolic Cosine", "cosh(A)", "演算子: ハイパボリックコサイン", "cosh(A)");
        addSpellPiece(root, "operator_tanh", "Operator: Hyperbolic Tangent", "tanh(A)", "演算子: ハイパボリックタンジェント", "tanh(A)");
        addSpellPiece(root, "operator_greater_than", "Operator: Greater Than", "Outputs 1 if Value 1 is greater than Value 2, otherwise 0.", "演算子: ～より大きい", "値1が値2より大きいなら1、そうでなければ0を出力します。");
        addSpellPiece(root, "operator_greater_than_or_equal", "Operator: Greater Than or Equal", "Outputs 1 if Value 1 is greater than or equal to Value 2, otherwise 0.", "演算子: ～以上", "値1が値2以上なら1、そうでなければ0を出力します。");
        addSpellPiece(root, "operator_equal", "Operator: Equal", "Outputs 1 if Value 1 equals Value 2, otherwise 0.", "演算子: 等しい", "値1と値2が等しいなら1、そうでなければ0を出力します。");
        addSpellPiece(root, "constant_string", "Constant: String", "Outputs the entered string. Limited to 1024 characters.", "定数子: 文字列", "入力した文字列をそのまま出力します。最大1024文字です。");
        addSpellPiece(root, "operator_format_string", "Operator: Format String", "Returns the entered text with Values 1 to 3 filled into {1}, {2}, and {3}.", "演算子: フォーマット文字列", "入力した文字列の {1}〜{3} に値1〜3を埋め込んで返します。");
        addSpellPiece(root, "operator_from_string", "Operator: From String", "Returns the input String as-is, or converts it into a Number or Vector for the selected mode.", "演算子: 文字列から変換", "選択中のモードに応じて String をそのまま返すか、Number または Vector に変換します。");
        addSpellPiece(root, "operator_list_from_string_list", "Operator: From String List", "Returns the input String List as-is, or converts it into a Number List or Vector List for the selected mode.", "演算子: 文字列リストから変換", "選択中のモードに応じて String List をそのまま返すか、Number List または Vector List に変換します。");
        addSpellPiece(root, "operator_number_list_to_vector", "Operator: Number List to Vector", "Converts a Number List with exactly three elements into a Vector.", "演算子: 数値リスト→ベクトル", "要素数3の Number List を Vector に変換します。");
        addSpellPiece(root, "operator_vector_to_number_list", "Operator: Vector to Number List", "Converts a Vector into a three-element Number List.", "演算子: ベクトル→数値リスト", "Vector を要素数3の Number List に変換します。");
        addSpellPiece(root, "operator_to_string", "Operator: To String", "Converts an Any input into a debug-display String.", "演算子: 文字列へ変換", "Any 入力をデバッグ表示相当の String に変換します。");
        addSpellPiece(root, "operator_list_to_string_list", "Operator: To String List", "Converts an Any input into a debug-display String List.", "演算子: 文字列リストへ変換", "Any 入力をデバッグ表示相当の String List に変換します。");
        addSpellPiece(root, "operator_get_id", "Operator: Registry ID", "Outputs the registry ID of a Contextual Value.", "演算子: レジストリID", "Contextual Value のレジストリIDを String として出力します。");
        addSpellPiece(root, "operator_get_id_list", "Operator: Registry ID List", "Outputs registry IDs from a Contextual Value List as a String List.", "演算子: レジストリIDリスト", "Contextual Value List の各要素のレジストリIDを String List として出力します。");
        addSpellPiece(root, "selector_display_name", "Selector: Display Name", "Outputs the localized display name of a Contextual Value.", "取得子: 表示名", "Contextual Value の表示名を術者の言語に応じた String として出力します。");
        addSpellPiece(root, "selector_display_name_list", "Selector: Display Name List", "Outputs localized display names from a Contextual Value List as a String List.", "取得子: 表示名リスト", "Contextual Value List の各要素の表示名を術者の言語に応じた String List として出力します。");
        addSpellPiece(root, "selector_effect_list", "Selector: Effect List", "Outputs the target Entity's active Mob Effect registry IDs as a String List.", "取得子: エフェクトリスト", "対象 Entity に付与されているエフェクトのレジストリ ID を String List として出力します。");
        addSpellPiece(root, "selector_block", "Selector: Block", "Returns the block at the given position as a Block value.", "取得子: ブロック", "指定座標にあるブロックを Block 型として返します。");
        addSpellPiece(root, "selector_block_list", "Selector: Block List", "Gets the blocks at the positions in a Vector List as a Block List.", "取得子: ブロックリスト", "Vector List の座標にあるブロックを Block List として取得します。");
        addSpellPiece(root, "operator_block_state", "Operator: Block State", "Outputs the saved block state of a Block value as a String. Example: minecraft:oak_stairs[facing=north]", "演算子: ブロックステート", "Block 値に保存されたブロックステートを文字列として出力します。例: minecraft:oak_stairs[facing=north]");
        addSpellPiece(root, "operator_block_state_value", "Operator: Block State Value", "Outputs one saved block state property value from a Block value.", "演算子: ブロックステート値", "Block 値に保存されたブロックステートから指定したプロパティ値を String として出力します。");
        addSpellPiece(root, "operator_block_state_entries", "Operator: Block State Entries", "Outputs the saved block state properties of a Block value as a String List.", "演算子: ブロックステート項目", "Block 値に保存されたブロックステートのプロパティ項目を String List として出力します。");
        addSpellPiece(root, "operator_tag_list", "Operator: Tag List", "Outputs the registry tags of the target Contextual Value (Entity, Item, Block, etc.) as a String List.", "演算子: タグリスト", "対象の Contextual Value(Entity、Item、Block など)のレジストリタグを String List として出力します。");
        addSpellPiece(root, "operator_block_position", "Operator: Block Position", "Outputs the saved position of a Block value as a plain Vector.", "演算子: ブロック座標", "Block 値に保存された座標を通常の Vector として出力します。");
        addSpellPiece(root, "operator_block_position_list", "Operator: Block Position List", "Gets the positions of the blocks in a Block List as a Vector List.", "演算子: ブロック位置リスト", "Block List のブロックの座標を Vector List として取得します。");
        addSpellPiece(root, "selector_online_players", "Selector: Online Players", "Outputs the names of online players in the world as a String List.", "取得子: オンラインプレイヤー", "ワールド内のオンラインプレイヤー名を String List として取得します。");
        addSpellPiece(root, "selector_held_item", "Selector: Main-Hand Item", "Gets the target entity's main-hand item. For a dropped item, gets its stack.", "取得子: 手持ちアイテム", "対象エンティティのメインハンドのアイテムを取得します。ドロップアイテムの場合はそのスタックを取得します。");
        addSpellPiece(root, "selector_selected_slot_item", "Selector: Selected Slot Item", "Gets the item currently selected by this spell.", "取得子: 選択スロットアイテム", "この術式で選択しているアイテムを取得します。");
        addSpellPiece(root, "selector_entity_slot_item", "Selector: Entity Slot Item", "Gets the item in the target entity's zero-based internal inventory slot. Outputs an empty Item if the slot cannot be read.", "取得子: エンティティスロットアイテム", "対象 Entity の0始まり内部インベントリスロットにある Item を取得します。スロットを読めない場合は空の Item を出力します。");
        addSpellPiece(root, "selector_internal_slot_item", "Selector: Internal Slot Item", "Gets the item in a block's zero-based internal inventory slot. Outputs an empty Item if the slot cannot be read.", "取得子: 内部スロットアイテム", "指定ブロックの0始まり内部インベントリスロットにある Item を取得します。スロットを読めない場合は空の Item を出力します。");
        addSpellPiece(root, "operator_item_count", "Operator: Item Count", "Outputs the stack count of an Item value.", "演算子: アイテム個数", "Item 値のスタック個数を Number として出力します。空の Item は0を出力します。");
        addSpellPiece(root, "operator_item_slot", "Operator: Item Slot", "Outputs the internal inventory slot number saved on an Item value. Outputs -1 if no slot number is available.", "演算子: アイテムスロット", "Item 値に保存された内部インベントリのスロット番号を Number として出力します。スロット番号を取得できない場合は-1です。");
        addSpellPiece(root, "operator_item_total_count", "Operator: Item Total Count", "Outputs the total count of matching items in an Item List.", "演算子: アイテム合計個数", "Item List 内の指定 ID と一致するアイテムの合計個数を Number として出力します。");
        addSpellPiece(root, "selector_held_items", "Selector: Carried Items", "Outputs the target entity's carried items as an Item List.", "取得子: 所持アイテム", "対象 Entity の所持アイテムを Item List として取得します。");
        addSpellPiece(root, "selector_internal_items", "Selector: Internal Items", "Outputs the target block's internal inventory as an Item List.", "取得子: 内部アイテム", "対象ブロックの内部インベントリを Item List として取得します。");
        addSpellPiece(root, "selector_indexed_element", "Selector: Indexed Element (PT)", "Outputs the element at a zero-based index from the selected List mode. Negative indexes count back from the end.", "取得子: インデックス要素 (PT)", "選択中の List モードから0始まりのインデックスにある要素を取得します。負のインデックスは末尾から数えます。");
        addSpellPiece(root, "selector_nbt", "Selector: NBT", "Outputs the target Contextual Value's top-level NBT as key:value strings.", "取得子: NBT", "対象 Contextual Value のNBTトップレベルを key:value 形式の String List として出力します。");
        addSpellPiece(root, "selector_nbt_keys", "Selector: NBT Keys", "Outputs the target Contextual Value's top-level NBT keys as a String List.", "取得子: NBTキー", "対象 Contextual Value のNBTトップレベルキーを String List として出力します。");
        addSpellPiece(root, "selector_nbt_value", "Selector: NBT Value", "Outputs the target Contextual Value's NBT value matching the String key or NBT path. Missing paths output an empty string.", "取得子: NBT値", "対象 Contextual Value のNBTから String キーまたはNBTパスに一致する値を出力します。一致しない場合は空文字列です。");
        addSpellPiece(root, "operator_string_partial_match", "Operator: Partial Match", "Outputs 1 if part of String 1 matches the pattern in String 2, otherwise 0. * matches any text, ? one character, and [abc] one of a, b, or c.", "演算子: 部分一致", "文字列1の中に文字列2のパターンと一致する部分があれば1、なければ0を出力します。* は任意の文字列、? は任意の1文字、[abc] は a・b・c のいずれかに一致します。");
        addSpellPiece(root, "operator_string_starts_with", "Operator: String Starts With", "Outputs 1 if String 1 starts with String 2, otherwise outputs 0.", "演算子: 先頭一致", "文字列1が文字列2で始まるなら1、そうでなければ0を出力します。");
        addSpellPiece(root, "operator_string_ends_with", "Operator: String Ends With", "Outputs 1 if String 1 ends with String 2, otherwise outputs 0.", "演算子: 末尾一致", "文字列1が文字列2で終わるなら1、そうでなければ0を出力します。");
        addSpellPiece(root, "operator_string_concat", "Operator: String Concat", "String 1 + String 2 + String 3", "演算子: 文字列結合", "文字列1 + 文字列2 + 文字列3");
        addSpellPiece(root, "operator_string_split", "Operator: String Split", "Splits String 1 with String 2 as a literal delimiter and outputs a String List.", "演算子: 文字列分割", "文字列1を文字列2のリテラル区切り文字で分割し、String List を出力します。");
        addSpellPiece(root, "operator_string_slice", "Operator: String Slice", "Returns the part of the String from Number A to Number B.", "演算子: 文字列スライス", "文字列を 数値A:数値B の範囲で切り出します。");
        addSpellPiece(root, "operator_string_length", "Operator: String Length", "Returns the number of characters in the input String as a Number.", "演算子: 文字列長", "入力した文字列の文字数を Number として返します。");
        addSpellPiece(root, "operator_string_replace", "Operator: String Replace", "Replaces parts of String 1 matching String 2 with String 3.", "演算子: 文字列置換", "入力した文字列1のうち、文字列2と一致する部分を、指定した文字列3へ置換する。");
        addSpellPiece(root, "operator_string_trim", "Operator: String Trim", "Removes leading and trailing whitespace from the input String.", "演算子: 文字列トリム", "入力した文字列の前後の空白を取り除きます。");
        addSpellPiece(root, "operator_string_list_join", "Operator: String List Join", "Joins the input String List with String 2 and outputs a String.", "演算子: 文字列リスト結合", "入力リストを文字列2で結合し、String を出力します。");
        addSpellPiece(root, "operator_player_name", "Operator: Player Name", "Outputs the player name if the Entity is a player, otherwise outputs an empty string.", "演算子: プレイヤーネーム", "Entityがプレイヤーならプレイヤー名を、そうでなければ空文字列を出力します。");
        addSpellPiece(root, "operator_list_search", "Operator: List Keep Matches", "Keeps only the List elements whose ID or string form matches the pattern. The whole string must match; * ? [abc] work.", "演算子: リスト一致抽出", "要素のIDまたは文字列が、パターンと一致するものだけを残します。全体一致で、* ? [abc] を使えます。");
        addSpellPiece(root, "operator_list_search_exclude", "Operator: List Remove Matches", "Removes the List elements whose ID or string form matches the pattern. The whole string must match; * ? [abc] work.", "演算子: リスト一致除外", "要素のIDまたは文字列が、パターンと一致するものを除きます。全体一致で、* ? [abc] を使えます。");
        addSpellPiece(root, "operator_random_element", "Operator: Random Element", "Outputs one random element from the input List.", "演算子: ランダム要素", "入力した List からランダムに1要素を出力します。");
        addSpellPiece(root, "operator_list_add", "Operator: Add To List (PT)", "Adds up to three elements to the input List.", "リストへ追加 (PT)", "入力した List に最大3つの要素を追加します。");
        addSpellPiece(root, "operator_list_remove", "Operator: Remove From List (PT)", "Removes up to two elements from the input List.", "リストから削除 (PT)", "入力した List から最大2つの要素を削除します。");
        addSpellPiece(root, "operator_list_remove_indices", "Operator: Remove Indexed Elements", "Removes the elements at Number A and Number B from the input List.", "演算子: インデックス要素削除", "入力した List から数値Aと数値Bに対応するインデックスの要素を削除します。");
        addSpellPiece(root, "operator_list_insert", "Operator: List Insert", "Inserts the input element at the specified index in the input List.", "演算子: リストに挿入", "入力した List の指定インデックスへ要素を挿入します。");
        addSpellPiece(root, "operator_list_slice", "Operator: List Slice", "Returns the part of the List from Number A to Number B.", "演算子: リストスライス", "List を 数値A:数値B の範囲で切り出します。");
        addSpellPiece(root, "operator_list_size", "Operator: List Size (PT)", "Outputs the number of elements in the input List.", "演算子: リストサイズ (PT)", "入力した任意の List の要素数を出力します。");
        addSpellPiece(root, "operator_list_exclusion", "Operator: List Exclusion (PT)", "Removes the elements in List 2 from the input List 1.", "演算子: リスト除外 (PT)", "入力したリスト1からリスト2に含まれる要素を除外します。");
        addSpellPiece(root, "operator_list_intersection", "Operator: List Intersection (PT)", "Outputs the elements shared by the two input Lists.", "演算子: リスト共通部分 (PT)", "入力した2つの List に共通する要素を出力します。");
        addSpellPiece(root, "operator_list_concatenation", "Operator: List Concatenation (PT)", "Combines the two input Lists.", "演算子: リスト結合 (PT)", "入力した2つの List を結合します。");
        addSpellPiece(root, "operator_matrix_add", "Operator: Matrix Add", "Adds two or three matrices of the same size.", "演算子: 行列加算", "同じサイズの行列を2つまたは3つ加算します。");
        addSpellPiece(root, "operator_matrix_subtract", "Operator: Matrix Subtract", "Subtracts two or three matrices of the same size.", "演算子: 行列減算", "同じサイズの行列を2つまたは3つ減算します。");
        addSpellPiece(root, "operator_matrix_multiply", "Operator: Matrix Multiply", "Multiplies matrices with compatible dimensions.", "演算子: 行列積", "次元が整合する行列を乗算します。");
        addSpellPiece(root, "operator_matrix_scalar_multiply", "Operator: Matrix Scalar Multiply", "Multiplies a matrix by a number.", "演算子: 行列スカラー倍", "行列を数値でスカラー倍します。");
        addSpellPiece(root, "operator_matrix_transpose", "Operator: Matrix Transpose", "Transposes a matrix.", "演算子: 行列転置", "行列を転置します。");
        addSpellPiece(root, "operator_matrix_determinant", "Operator: Matrix Determinant", "Computes the determinant of a square matrix.", "演算子: 行列式", "正方行列の行列式を求めます。");
        addSpellPiece(root, "operator_matrix_inverse", "Operator: Matrix Inverse", "Computes the inverse of a square matrix.", "演算子: 逆行列", "正方行列の逆行列を求めます。");
        addSpellPiece(root, "operator_matrix_extract_row", "Operator: Matrix Extract Row", "Returns the specified row as a Number List.", "演算子: 行抽出", "指定した行を Number List として返します。");
        addSpellPiece(root, "operator_matrix_extract_column", "Operator: Matrix Extract Column", "Returns the specified column as a Number List.", "演算子: 列抽出", "指定した列を Number List として返します。");
        addSpellPiece(root, "operator_matrix_element", "Operator: Matrix Element", "Returns the element at (row, column).", "演算子: 行列要素", "(行, 列) の要素を返します。");
        addSpellPiece(root, "operator_matrix_row_count", "Operator: Matrix Row Count", "Returns the number of rows.", "演算子: 行数", "行列の行数を返します。");
        addSpellPiece(root, "operator_matrix_column_count", "Operator: Matrix Column Count", "Returns the number of columns.", "演算子: 列数", "行列の列数を返します。");
        addSpellPiece(root, "operator_matrix_multiply_vector", "Operator: Matrix Multiply Vector", "Multiplies a matrix by a column vector or Number List.", "演算子: 行列×ベクトル", "行列に列ベクトルまたは Number List を乗算します。");
        addSpellPiece(root, "operator_matrix_column_from_list", "Operator: Column Matrix From List", "Converts a Number List or Vector into a column matrix.", "演算子: 数値リスト→列行列", "Number List または Vector を列行列に変換します。");
        addSpellPiece(root, "operator_matrix_flatten", "Operator: Matrix Flatten", "Flattens a matrix into a Number List in row-major order.", "演算子: 行列平坦化", "行列を行優先の Number List に平坦化します。");
        addSpellPiece(root, "operator_matrix_identity", "Operator: Identity Matrix", "Creates an identity matrix of the given size.", "演算子: 単位行列", "指定次数の単位行列を作ります。");
        addSpellPiece(root, "operator_matrix_zero", "Operator: Zero Matrix", "Creates a zero matrix with the given dimensions.", "演算子: ゼロ行列", "指定サイズのゼロ行列を作ります。");
        addSpellPiece(root, "operator_matrix_diagonal", "Operator: Diagonal Matrix", "Creates a diagonal matrix from a Number List or Vector.", "演算子: 対角行列", "Number List または Vector から対角行列を作ります。");
        addSpellPiece(root, "operator_matrix_replace_column", "Operator: Matrix Replace Column", "Replaces a column, expanding the matrix with zeros as needed.", "演算子: 列置換", "列を置換し、必要に応じてゼロで拡張します。");
        addSpellPiece(root, "operator_matrix_replace_row", "Operator: Matrix Replace Row", "Replaces a row, expanding the matrix with zeros as needed.", "演算子: 行置換", "行を置換し、必要に応じてゼロで拡張します。");
        addSpellPiece(root, "operator_matrix_replace_element", "Operator: Matrix Replace Element", "Replaces the element at the zero-based [row, column] indices with the input number.", "演算子: 行列要素置換", "0始まりの [行, 列] で指定した成分を入力数値で置換します。");
        addSpellPiece(root, "operator_matrix_delete_row", "Operator: Matrix Delete Row", "Deletes the specified row from the matrix and shifts the remaining rows up.", "演算子: 行削除", "指定した行を行列から削除し、残りの行を詰めます。");
        addSpellPiece(root, "operator_matrix_delete_column", "Operator: Matrix Delete Column", "Deletes the specified column from the matrix and shifts the remaining columns left.", "演算子: 列削除", "指定した列を行列から削除し、残りの列を詰めます。");
        addSpellPiece(root, "operator_matrix_cuboid_region", "Operator: Matrix Cuboid Region", "Builds a 4x4 matrix for an axis-aligned cuboid from a center position and size. Whole-number sizes act as block counts on each axis.", "演算子: 直方体領域", "中心位置とサイズから軸平行直方体を表す 4×4 行列を生成します。整数サイズは各軸でブロック数として扱われます。");
        addSpellPiece(root, "operator_region_vector_list", "Operator: Region Vector List", "Returns block position vectors inside a matrix-defined region.", "演算子: 領域ベクトルリスト化", "行列で定義された領域内のブロック座標を Vector List として返します。");
        addSpellPiece(root, "operator_inside_region", "Operator: Inside Region", "Keeps only the elements of an Entity, Vector or Block List that are inside the matrix region. Choose the list type in the mode menu.", "演算子: 領域内", "Entity・Vector・Block の List のうち、行列の領域内にあるものだけを残します。リストの種類はモードで選びます。");
        addSpellPiece(root, "operator_outside_region", "Operator: Outside Region", "Keeps only the elements of an Entity, Vector or Block List that are outside the matrix region. Choose the list type in the mode menu.", "演算子: 領域外", "Entity・Vector・Block の List のうち、行列の領域外にあるものだけを残します。リストの種類はモードで選びます。");
        addSpellPiece(root, "operator_alive", "Operator: Alive", "Keeps only LivingEntity instances that are currently alive in the input Entity List.", "演算子: 生存", "入力した Entity List のうち、生存中の LivingEntity のみを残します。");
        addSpellPiece(root, "operator_matrix_transform_vector", "Operator: Matrix Transform Vector", "Transforms a vector, or every vector of a Vector List, with a 3x3 or 4x4 matrix.", "演算子: 行列でベクトル変換", "3×3 または 4×4 行列でベクトル、または Vector List の各ベクトルを変換します。");
        addSpellPiece(root, "operator_matrix_linear_part", "Operator: Matrix Linear Part", "Extracts the top-left 3x3 submatrix.", "演算子: 行列線形部分", "左上の 3×3 部分行列を取り出します。");
        addSpellPiece(root, "operator_weak_raycast", "Operator: Vector Weak Raycast", "Raycasts from Position in the Ray direction and returns the coordinates of the hit block or fluid.", "演算子: ベクトルレイキャスト (弱)", "位置から Ray 方向へレイキャストし、命中したブロックまたは流体の座標を返します。");
        addSpellPiece(root, "operator_weak_raycast_axis", "Operator: Vector Axis Weak Raycast", "Raycasts from Position in the Ray direction and returns the normal direction of the face of the first block or fluid hit.", "演算子: ベクトルレイキャスト方向 (弱)", "位置から Ray 方向へレイキャストし、最初に命中したブロックまたは流体の面の法線方向を返します。");
        addSpellPiece(root, "operator_strong_raycast", "Operator: Vector Strong Raycast", "Raycasts from Position in the Ray direction, ignores blocks without a collision shape, and returns the coordinates of the hit block.", "演算子: ベクトルレイキャスト (強)", "位置から Ray 方向へレイキャストし、当たり判定のないブロックを無視して命中したブロックの座標を返します。");
        addSpellPiece(root, "operator_strong_raycast_axis", "Operator: Vector Axis Strong Raycast", "Raycasts from Position in the Ray direction, ignores blocks without a collision shape, and returns the normal direction of the face of the hit block.", "演算子: ベクトルレイキャスト方向 (強)", "位置から Ray 方向へレイキャストし、当たり判定のないブロックを無視して命中したブロックの面の法線方向を返します。");
        addSpellPiece(root, "macro_caster_raycast", "Macro: Caster Raycast", "Raycasts from the caster's position in the look direction and returns the coordinates of the hit block.", "マクロ: 術者レイキャスト", "術者の位置から視線方向へレイキャストし、命中したブロックの座標を返します。");
        addSpellPiece(root, "macro_caster_raycast_axis", "Macro: Axis Caster Raycast", "Raycasts from the caster's position in the look direction and returns the normal direction of the hit face.", "マクロ: 術者レイキャスト方向", "術者の位置から視線方向へレイキャストし、命中した面の法線方向を返します。");
        addSpellPiece(root, "macro_caster_weak_raycast", "Macro: Weak Caster Raycast", "Like Macro: Caster Raycast, but it also hits fluids such as water and lava.", "マクロ: 術者レイキャスト (弱)", "マクロ: 術者レイキャストと同じですが、水や溶岩などの流体にも命中します。");
        addSpellPiece(root, "macro_caster_weak_raycast_axis", "Macro: Weak Axis Caster Raycast", "Like Macro: Axis Caster Raycast, but it also hits fluids such as water and lava.", "マクロ: 術者レイキャスト方向 (弱)", "マクロ: 術者レイキャスト方向と同じですが、水や溶岩などの流体にも命中します。");
        addSpellPiece(root, "macro_caster_strong_raycast", "Macro: Strong Caster Raycast", "Like Macro: Caster Raycast, but blocks without a collision shape are ignored.", "マクロ: 術者レイキャスト (強)", "マクロ: 術者レイキャストと同じですが、当たり判定のないブロックを無視します。");
        addSpellPiece(root, "macro_caster_strong_raycast_axis", "Macro: Strong Axis Caster Raycast", "Like Macro: Axis Caster Raycast, but blocks without a collision shape and fluids are ignored.", "マクロ: 術者レイキャスト方向 (強)", "マクロ: 術者レイキャスト方向と同じですが、当たり判定のないブロックと流体を無視します。");
        addSpellPiece(root, "macro_caster_axial_offset", "Macro: Caster Axial Offset", "Offsets a position by Left/Right, Forward/Backward, and Up/Down relative to the caster's horizontal facing. Positive values move right, forward, and up. Pitch is ignored.", "マクロ: 術者方角平行移動", "術者の水平方向の向きを基準に、位置を左右・前後・上下へ平行移動します。正の値で右・前・上へ移動します。視線の上下成分は無視されます。");
        addSpellPiece(root, "macro_caster_axial_rotation", "Macro: Caster Axial Rotation", "Rotates a direction vector from the caster's view frame (X = right, Y = up, Z = forward) into world space. Facing is rounded to the nearest horizontal cardinal direction. Pitch is ignored.", "マクロ: 術者方角回転", "方向ベクトルを術者の視点座標(X=右、Y=上、Z=前)からワールド座標へ回転します。向きは最も近い水平4方角に丸められ、視線の上下成分は無視されます。");
        addSpellPiece(root, "macro_caster_axial_offset_3d", "Macro: Caster Axial Offset (3D)", "Like Macro: Caster Axial Offset, but the facing also follows the pitch. When looking straight up or down, forward points vertically and the offset plane becomes horizontal.", "マクロ: 術者方角平行移動 (3D)", "マクロ: 術者方角平行移動と同じですが、視線の上下も基準に含みます。真上や真下を向いた時は前後が垂直になり、オフセット平面が水平になります。");
        addSpellPiece(root, "macro_caster_axial_rotation_3d", "Macro: Caster Axial Rotation (3D)", "Like Macro: Caster Axial Rotation, but the facing also follows the pitch (X = right, Y = view-up, Z = forward), including when looking straight up or down.", "マクロ: 術者方角回転 (3D)", "マクロ: 術者方角回転と同じですが、視線の上下も基準に含みます。X=右、Y=視点上、Z=前で、真上や真下を向いた時も視点に追随します。");
        addSpellPiece(root, "macro_face_axial_offset", "Macro: Face Axial Offset", "Offsets a position along axes aligned to the block face under the caster's crosshair. If Position is omitted, the hit block is used. Positive values move screen-right, into the block, and screen-up.", "マクロ: 面基準平行移動", "術者の視線が命中したブロック面を基準に、位置を左右・前後・上下へ平行移動します。位置を省略した場合は命中ブロックを使います。正の値で画面右・ブロック内側・画面上へ移動します。");
        addSpellPiece(root, "macro_face_axial_rotation", "Macro: Face Axial Rotation", "Rotates a direction vector using the orientation of the block face hit by the caster's raycast as its reference.", "マクロ: 面基準回転", "方向ベクトルを、術者の視線が命中したブロック面の方向を基準として回転させます。");
        addSpellPiece(root, "trick_mass_block_break", "Trick: Mass Block Break", "Breaks blocks at the coordinates in a Vector List (a Block List also works). Drops go into the caster's inventory; only overflow is dropped.", "作動式: 大規模ブロック破壊", "Vector List（Block List も可）の座標にあるブロックを破壊します。ドロップは術者のインベントリに入り、入りきらない分だけ落ちます。");
        addSpellPiece(root, "selector_idea_storage_amount", "Selector: Ideaspace Storage - Amount", "Returns the stored amount of the resource chosen by the mode: items, fluids, chemicals or FE.", "取得子: イデアストレージ-貯蔵量", "モードで選んだ資源（アイテム・液体・化学物質・FE）の貯蔵量を取得します。");
        addSpellPiece(root, "selector_idea_storage_free_capacity", "Selector: Ideaspace Storage - Free Capacity", "Returns how much more of the resource chosen by the mode can be stored right now.", "取得子: イデアストレージ-空き容量", "モードで選んだ資源を、今あとどれだけ格納できるかを取得します。");
        addSpellPiece(root, "selector_idea_storage_type_count", "Selector: Ideaspace Storage - Type Count", "Returns the number of stored types of the resource chosen by the mode.", "取得子: イデアストレージ-種類数", "モードで選んだ資源の、格納中の種類数を取得します。");
        addSpellPiece(root, "selector_idea_storage_last_transfer", "Selector: Ideaspace Storage - Last Transfer Amount", "Returns the amount actually moved by the most recent Ideaspace transfer trick earlier in this cast.", "取得子: イデアストレージ-最終転送量", "この詠唱で直前に実行したイデア転送が、実際に移した量を取得します。");
        addSpellPiece(root, "trick_idea_storage_view", "Trick: View Ideaspace Storage", "Opens a GUI to view and manage your Ideaspace Storage. Only works when cast by a player.", "作動式: イデアストレージ閲覧", "自分のイデアストレージを閲覧・出し入れするGUIを開きます。プレイヤーの詠唱時のみ有効です。");
    }

    private void addSpellPiecesBook(JsonObject root) {
        addIdeaResourceLogistics(root);
        addIdeaSlotLogistics(root);
        addItemBlockMatrixPieces(root);
        addIdeaStorageSelectorPages(root);
        addBookPage(root, "trick_idea_storage_view.0",
                "Opens your personal Ideaspace Storage. Items, fluids, Mekanism chemicals, and FE can each be stored. FE is transferred by the FE mode of the Deposit and Withdraw tricks; its balance appears in the storage grid, and the information button also shows capacity.$(p)By default, it can store 256 item types, 64 fluid types, and 64 chemical types. Capacity limits can be changed in the config.",
                "自分専用のイデアストレージを開きます。アイテム・液体・Mekanismの化学物質・FEを保管できます。FEは格納・取出の作動式のFEモードで転送し、一覧のスロットで残量、情報ボタンで上限も確認できます。$(p)標準設定ではアイテム256種類、液体64種類、化学物質64種類を保管できます。保存上限はコンフィグで変更できます。");
        addBookPage(root, "trick_idea_storage_view.1",
                "To store items, hold a stack on the cursor and left-click the storage grid, or Shift-click a stack in your inventory. Right-click while holding a normal item to store just one. Fluid and chemical containers transfer their contents instead.$(p)With an empty cursor, left-click a stored item to withdraw up to one stack, or right-click for up to half its maximum stack size. Hold Shift to send the withdrawn items directly to your inventory.",
                "アイテムをカーソルで持ち、ストレージ欄を左クリックすると格納します。インベントリ内のアイテムをShiftクリックしても格納できます。通常アイテムを持って右クリックすると1個だけ格納します。液体・化学物質の容器は中身を移します。$(p)カーソルが空のとき、保存済みアイテムを左クリックすると最大1スタック、右クリックすると最大スタック数の半分まで取り出します。Shiftを押しながら操作すると、直接インベントリへ取り出します。");
        addBookPage(root, "trick_idea_storage_view.2",
                "Hold a fluid or chemical container on the cursor and right-click the storage grid to transfer its contents. Right-click the desired stored resource to fill a compatible container; if it cannot be filled, its contents may be deposited instead.$(p)Shift-right-click repeats the transfer in the same direction. With an empty cursor, left-click a stored fluid to fill one empty bucket from storage, if that fluid supports buckets.",
                "液体や化学物質の容器をカーソルで持ち、ストレージ欄を右クリックすると中身を移します。保存済みの資源を右クリックすると、対応する容器への充填を試みます。充填できない場合は、容器の中身を格納することがあります。$(p)Shift＋右クリックでは、同じ方向への転送を繰り返します。カーソルが空の状態で液体を左クリックすると、保存済みの空バケツ1個に充填できます。バケツに対応する液体に限ります。");
        addBookPage(root, "trick_idea_storage_view.3",
                "The side crafting-table button opens a 3x3 crafting grid. Consumed ingredients are replenished from storage when available. Shift-click the result to craft roughly one stack at a time.$(p)The clear button returns ingredients to storage if they all fit. Search, sorting, and the + / - buttons help organize the view. JEI and EMI can also place recipe ingredients while the crafting grid is open.",
                "左側の作業台ボタンで3×3のクラフト欄を開きます。消費した材料は、ストレージに在庫があれば自動補充されます。完成品をShiftクリックすると、約1スタック分をまとめて作成します。$(p)材料返却ボタンは、全量を格納できる場合に材料をストレージへ戻します。検索・並べ替え・＋／－ボタンで表示を調整できます。クラフト欄を開いている間は、JEI・EMIからの材料配置にも対応します。");
        root.addProperty("psi.book.category.psitweaks_spell_pieces", switch (locale) {
            case "ja_jp" -> "追加術式";
            default -> "Additional Spell Pieces";
        });
        root.addProperty("psi.book.category.psitweaks_spell_pieces.desc", switch (locale) {
            case "ja_jp" -> "PsiTweaks が追加するスペルピースです. 攻撃, 工業処理, 補助用途のものが多く, 設定によっては研究解禁が必要になります.";
            default -> "Spell pieces added by PsiTweaks. Many are offensive, industrial, or utility-focused, and some require research depending on the config.";
        });

        addBookPage(root, "trick_explode_no_destroy", "Creates an explosion that deals damage without destroying blocks. Dropped items and experience orbs are not erased by the blast.", "ブロックを破壊しない爆発を起こしてダメージを与えます. ドロップアイテムや経験値オーブは爆発で消滅しません.");
        addBookPage(root, "trick_barrier.0",
                "Wraps the target in a psionic shield worth $(thing)Power × 10$(0) points for $(thing)Time$(0) seconds. While the shield lasts, damage the target takes comes off the shield instead of its health, and a honeycomb sphere flashes around it. Damage beyond what is left goes through, and the shield breaks.",
                "対象を $(thing)威力×10$(0) のシールドで $(thing)時間$(0) 秒間包みます. シールドがある間、対象が受けるダメージは体力の代わりにシールドから差し引かれ、対象の周りにハニカム模様の球が一瞬光ります. 残量を超えたダメージはそのまま通り、シールドは砕けます.");
        addBookPage(root, "trick_barrier.1",
                "Power and Time must be positive integers. Casting it again replaces the current shield. It does not absorb starvation, drowning, suffocation, falling, freezing, withering, the damage of being on fire, the void or /kill. A bar above your health shows the shield you have left.",
                "威力と時間には正の整数を指定します. もう一度かけると今のシールドと置き換わります. 空腹、溺れ、窒息、落下、凍結、衰弱、炎上中の継続ダメージ、奈落、/kill は防げません. 自分のシールドの残量は体力ゲージの上のバーに表示されます.");
        addBookPage(root, "trick_hardening", "Applies a hardening effect that limits large incoming damage to a fixed value. Maximum damage taken is capped at (level - 2).", "大きな被ダメージを一定値まで抑える硬化効果を付与します. 受ける最大ダメージを(レベル - 2)に抑えます.");
        addBookPage(root, "trick_parade", "Applies an effect that evades attacks by chance. It evades attacks with a (62.5 + 7.5 * level)%% chance.", "確率で攻撃を回避する効果を付与します. (62.5 + 7.5 * レベル) %% で攻撃を回避します.");
        addBookPage(root, "trick_flight", "Applies the Flight effect to the target. The Flight effect enables creative flight but consumes Psi every 0.5 seconds. The amount consumed varies based on the Flight effect level, the efficiency of the CAD used to apply the effect, and the spell bullet's cost multiplier. The base consumption at effect levels 1, 2, 3, 4, and 5 or higher is 1000 / 750 / 500 / 250 / 5 Psi every 0.5 seconds, respectively.", "対象に飛行エフェクトを付与します. 飛行エフェクトはクリエイティブ飛行を可能にしますが, 0.5秒ごとにPsiを消費します. 消費量は飛行エフェクトのレベル, 付与したCADの効率, 術式弾のコスト倍率によって変動し, エフェクトレベルが 1/2/3/4/5以上の際の基本消費量は 0.5秒ごとに 1000 / 750 / 500 / 250 / 5 です.");
        addBookPage(root, "trick_interact_block", "Acts on the target block as if right-clicked with the item in the caster's off hand.", "対象ブロックに対して, 術者のオフハンドのアイテムで右クリックしたように作用します.");
        addBookPage(root, "trick_freeze_block", "Freezes the target block one step: water to ice, ice to packed ice, packed ice to blue ice, lava to magma block, and magma block to obsidian.", "対象ブロックを1段階凍結させます. 水は氷, 氷は氷塊, 氷塊は青氷, 溶岩はマグマブロック, マグマブロックは黒曜石になります.");
        addBookPage(root, "trick_melt_block", "Melts the target block one step: ice, packed ice, and blue ice become water; obsidian, stone-like blocks, and cobblestone-like blocks become magma blocks; magma blocks become lava.", "対象ブロックを1段階溶解させます. 氷, 氷塊, 青氷は水に, 黒曜石, 石系, 丸石系はマグマブロックに, マグマブロックは溶岩になります.");
        addBookPage(root, "trick_pulsar", "Generates a magical Pulsar block at Position. The block emits a redstone signal with strength 15 in every direction and glows in the color of the caster's CAD. If Time is positive, it disappears after that many ticks; if Time is omitted or zero or less, it remains until broken.", "位置に魔法パルサーブロックを生成します. ブロックは全方向へ強度15のレッドストーン信号を出力し, 術者のCADの色で輝きます. 時間に正の値を指定するとそのtick数後に消滅し, 未指定または0以下では破壊されるまで残ります.");
        addBookPage(root, "trick_pulsar_sequence", "Constructs solid Pulsars in a straight line from Position in the direction of Target. The number generated is limited by the lesser of the length of the Target vector and Max. Each Pulsar emits a redstone signal with strength 15. If Time is positive, they disappear after that many ticks; if Time is omitted or zero or less, they remain until broken.", "位置を始点として, 対象の方向へ固体のパルサーを直線状に構築します. 生成数は対象ベクトルの長さと最大数の小さい方までです. 各パルサーは強度15のレッドストーン信号を出力します. 時間に正の値を指定するとそのtick数後に消滅し, 未指定または0以下では破壊されるまで残ります.");
        addBookPage(root, "trick_pulsar_light", "Generates a non-solid Pulsar light at Position. The light emits a redstone signal with strength 15 in every direction. Its color reflects the color of the caster's CAD. If Time is positive, it disappears after that many ticks; if Time is omitted or zero or less, it remains until broken.", "位置に非固体のパルサー光源を生成します.  光源は全方向へ強度15のレッドストーン信号を出力します. 光源色は術者のCADの色を反映します. 時間に正の値を指定するとそのtick数後に消滅し, 未指定または0以下では破壊されるまで残ります.");
        addBookPage(root, "trick_break_fortune", "Breaks the target block with Fortune.", "対象ブロックを幸運付きで破壊します.");
        addBookPage(root, "trick_break_silk", "Breaks the target block with Silk Touch.", "対象ブロックをシルクタッチ付きで破壊します.");
        addBookPage(root, "trick_store_entity", "Stores the target entity's UUID as a String value in CAD memory.", "対象エンティティのUUIDをString値としてCADメモリに保存します.");
        addBookPage(root, "selector_stored_entity", "Gets an entity from the UUID stored in CAD memory.", "CADメモリに保存されたUUIDからエンティティを取得します. ");
        addBookPage(root, "trick_store_value", "Stores a Plain Value in CAD memory. The memory slot uses Psi's standard numbering: 1 is the first slot. Number, Vector, and String values overwrite each other in the same slot. String values longer than 128 characters are truncated when stored, and a warning is shown in the caster's chat.", "Plain ValueをCADメモリに保存します. メモリ番号はPsi標準と同じく1が最初のスロットです. Number, Vector, Stringは同じスロットで互いに上書きされます. 128文字を超えるStringは保存時に切り捨てられ, 術者のチャット欄に警告が表示されます.");
        addBookPage(root, "selector_stored_value", "Gets a Plain Value from CAD memory. Use the mode button to choose String, Number, or Vector. String conversions are strict; Number and Vector cannot be converted directly.", "CADメモリからPlain Valueを取得します. モードボタンでString, Number, Vectorを選択します. String変換は厳格で, NumberとVectorは直接変換できません.");
        addBookPage(root, "selector_nearby_spellgram", "Gets SpellGram objects around the specified coordinates. It is mainly used by tricks that control placed SpellGram objects.", "指定座標の周囲にある魔法式オブジェクトを取得します. 設置済みの魔法式オブジェクトを制御する術式で主に使います.");
        addBookPage(root, "trick_dispel", "Removes effects from the target entity. This is the general-purpose dispel that does not distinguish between beneficial and harmful effects.", "対象エンティティからエフェクトを除去します. 良性・悪性を区別しない汎用版の解呪です.");
        addBookPage(root, "trick_dispel_beneficial", "Removes only beneficial effects from the target entity. It is suited for stripping enhancements from hostile targets.", "対象エンティティから有益なエフェクトだけを除去します. 敵対対象の強化を剥がす用途に向いています.");
        addBookPage(root, "trick_dispel_non_beneficial", "Removes non-beneficial effects from the target entity. Use it when you want to cleanse harmful effects while leaving allied buffs intact.", "対象エンティティから有益でないエフェクトを除去します. 味方のバフを残したまま悪性効果を消したい時に使います.");
        addBookPage(root, "trick_cocytus", "Permanently freezes the mind of the target mob, leaving it unable to act.", "対象モブの精神を永久に凍結させ, 行動できなくします.");
        addBookPage(root, "trick_supply_fe", "Supplies FE to the target block. When CAD Efficiency is 100, it supplies 20 FE per Psi.", "対象ブロックへFEを供給します. 供給量はCADの効率が100のとき、1psiあたり20FEです.");
        addBookPage(root, "trick_pull_item.0", "Moves items from the inventory of the block at the target position, through the specified face, into the caster's inventory. Only one stack is moved per cast, as much of it as fits. Casting fails with an error if the block has no inventory, nothing can be moved, or the block is protected.", "入力位置のブロックのインベントリから, 入力方向の面を経由してアイテムを術者のインベントリへ移動させます. 1回の実行で移動できるのは1スタックまでで, 術者のインベントリに入りきらない場合は入る分だけ移動します. インベントリが無い, 移動できるものが無い, 保護されている場合はエラーになります.");
        addBookPage(root, "trick_pull_item.1", "In String mode the filter matches item IDs with wildcard support (the namespace may be omitted), Item mode matches the item type only, and Item (Strict) mode also compares data components such as enchantments and durability. In both Item modes, the filter can also be an Item List, matching any of its items. The optional Max input caps the number of items moved.", "StringモードはアイテムIDをワイルドカードで絞り込みます(名前空間は省略可能), Itemモードはアイテム種のみ, Item (Strict)モードはエンチャントや耐久値などのデータコンポーネントまで一致を要求します. Item と Item (Strict) モードでは Item List も接続でき, そのいずれかに一致するアイテムを移動します. 任意入力の最大で移動数に上限を設けられます.");
        addBookPage(root, "trick_send_item.0", "Moves items from the caster's inventory into the inventory of the block at the target position, through the specified face. Only one stack is moved per cast; if the destination cannot hold it all, only what fits is moved. CADs are never moved.", "術者のインベントリから, 入力位置のブロックのインベントリへ, 入力方向の面を経由してアイテムを移動させます. 1回の実行で移動できるのは1スタックまでで, 移動先に入りきらない場合は入る分だけ移動します. CADは移動対象から除外されます.");
        addBookPage(root, "trick_send_item.1", "The filter modes and the optional Max input behave like Trick: Pull Item, including Item Lists in both Item modes. Casting fails with an error if the block has no inventory, no matching item is held, or the block is protected.", "フィルタのモードと任意入力の最大は作動式: アイテム引き出しと同様で, Item と Item (Strict) モードでは Item List も接続できます. インベントリが無い, 対象アイテムを所持していない, 保護されている場合はエラーになります.");
        addBookPage(root, "trick_time_accelerate", "Multiplies the target block's tick progression by (2 ^ power). The upper limit is 512x speed.", "対象ブロックのtick進行を (2 ^ 威力) 倍にします.上限は512倍速まで.");
        addBookPage(root, "trick_phonon_maser", "Fires a high-power heat ray using ultrasonic vibration.", "超音波振動による高威力の熱線を放ちます.");
        addBookPage(root, "trick_meteor_line", "Creates a beam from the specified position in the direction of the Ray vector, dealing special and lethally massive damage to living beings along its path.", "指定位置からRayベクトル方向へ光線を生み出し、経路上の生物に特殊かつ致死的な大ダメージを与えます。");
        addBookPage(root, "trick_supreme_infusion", "Infusion-converts Echo Shards into Psionic Echo.", "残響の欠片をサイオニックエコーへ注入変換します.");
        addBookPage(root, "trick_molecular_divider", "Cuts living beings along a plane defined by three points.", "三点で定義した平面で生物を切断します.");
        addBookPage(root, "trick_aqua_cutter", "Fires a water-blade projectile forward as an early-game offensive trick.", "前方へ水刃の発射体を放つ序盤用の攻撃術式です.");
        addBookPage(root, "trick_dry_meteor", "Fires a dry ice projectile forward, dealing freezing damage to targets it hits. The dry ice sublimates into carbon dioxide, inflicting Carbon Poisoning that impairs the target's mobility and deals damage over time.", "前方にドライアイスの弾丸を発射し, 命中した相手に凍結ダメージを与えます. ドライアイスは二酸化炭素へと気化し対象を炭素中毒状態にして機動力を奪い継続ダメージを与えます.");
        addBookPage(root, "trick_blaze_ball", "Fires a ball of flame forward as an early-game offensive trick.", "前方へ火の弾を放つ序盤用の攻撃術式です.");
        addBookPage(root, "trick_active_air_mine", "Creates a spherical shockwave at the specified coordinates and damages living beings within range.", "指定座標に球状の衝撃波を作り, 範囲内の生物にダメージを与えます.");
        addBookPage(root, "trick_flare_circle", "Places a fire SpellGram Circle that continuously deals fire damage to living beings inside it. Once placed, the circle remains for 60 seconds.", "炎の魔法式サークルを設置し, 内部の生物に継続的な炎ダメージを与えます. 一度設置したサークルは60秒残り続けます.");
        addBookPage(root, "trick_ice_circle", "Places an ice SpellGram Circle that continuously deals freezing damage to living beings inside it. Like Fire Circle, it is suited for area control.", "氷の魔法式サークルを設置し, 内部の生物に継続的な凍結ダメージを与えます. ファイアサークルと同じく領域制圧に向いています.");
        addBookPage(root, "trick_set_spellgram_follow_target", "Sets the follow target entity of a SpellGram object. For example, by setting the Fire Circle to follow the caster, it will continue to follow the caster and keep burning surrounding enemies.", "魔法式オブジェクトの追従対象エンティティを設定します. 例えば、ファイアサークルの追従対象を術者に設定することで、術者に追従し続け周囲の敵を燃やし続けます.");
        addBookPage(root, "trick_die_flex", "Behaves similarly to Psi's Trick: Die and refunds the Psi cost of spell pieces that were not executed. With high-frequency casting, the client-side Psi display may temporarily desync.", "Psi本体の停止と同様の動作を行い, 未実行分のスペルピースのPsiコストを返還します. 高頻度詠唱ではクライアント側のPsi表示が一時的にずれることがあります.");
        addBookPage(root, "trick_jump", "Jumps forward to the next Jump Anchor with the same label when the target number is unset, or when its absolute value is less than 1. The label input is optional, but if used it must be a String constant. Empty labels only match empty-label anchors. It cannot jump to Jump Anchors behind it.", "対象数値が未入力、または絶対値が1未満なら, 同じラベルを持つ次のジャンプアンカーまで前方にジャンプします. ラベル入力は任意ですが, 使う場合はString定数である必要があります. 空ラベルは空ラベルのアンカーにのみ一致します. 後方のジャンプアンカーには飛ぶことができません.");
        addBookPage(root, "trick_jump_flex", "Uses the same conditions as Trick: Jump to jump to a Jump Anchor, then refunds the Psi cost of the spell pieces actually skipped by the jump. With high-frequency casting, the client-side Psi display may temporarily desync.", "作動式: ジャンプと同じ条件でジャンプアンカーまでジャンプし, ジャンプによって実際にスキップしたスペルピースのPsiコストを返却します. 高頻度詠唱ではクライアント側のPsi表示が一時的にずれることがあります.");
        addBookPage(root, "trick_switch", "Evaluates the input String and jumps forward to the next Jump Anchor whose constant label is equal to it. If no matching anchor exists ahead, it does not error and simply continues to the next spell piece, so place the default branch immediately after the Switch. It cannot jump to Jump Anchors behind it.", "入力Stringを評価し, それと等しい定数ラベルを持つ次のジャンプアンカーまで前方にジャンプします. 前方に一致するアンカーがない場合はエラーにならず, そのまま次のスペルピースへ進むため, default相当の処理はスイッチの直後に置いてください.  後方のジャンプアンカーには飛ぶことができません.");
        addBookPage(root, "trick_switch_flex", "Uses the same conditions as Trick: Switch to jump to a Jump Anchor, then refunds the Psi cost of the spell pieces actually skipped by the jump. If no matching anchor exists, execution continues and no Psi is refunded. With high-frequency casting, the client-side Psi display may temporarily desync.", "作動式: スイッチと同じ条件でジャンプアンカーまでジャンプし, ジャンプによって実際にスキップしたスペルピースのPsiコストを返却します. 一致するアンカーがない場合はそのまま実行を続け, Psiを返却しません. 高頻度詠唱ではクライアント側のPsi表示が一時的にずれることがあります.");
        addBookPage(root, "jump_anchor", "A marker used as the destination for Trick: Jump and Trick: Switch. This spell piece itself does nothing. You can input a constant String as its label.", "作動式: ジャンプと作動式: スイッチの到達点になる目印です.  このスペルピース自体は何もしません. 定数文字列をラベルとして入力することができます.");
        addBookPage(root, "safety", "It works effectively no matter where it is placed within the program. Immediately before Psi is consumed, it compares the spell’s actual Psi consumption with the total of the caster’s current Psi and the Psi in the CAD battery. If the caster’s Psi is insufficient, the spell is canceled without consuming any Psi.", "プログラム内のどこに配置しても効果を発揮します. Psi消費直前に、術式の実際のPsi消費量と、術者の現在PsiおよびCADバッテリー内Psiの合計を比較します. 術者のPsi量が不足している場合はPsiを消費せずに術式を中止します.");
        addBookPage(root, "trick_radiation_injection", "Applies Mekanism radiation exposure to the target.", "対象へMekanismの放射線被ばくを付与します. ");
        addBookPage(root, "trick_radiation_filter", "Applies a radiation protection effect to the target.", "対象に放射線防護効果を付与します.");
        addBookPage(root, "trick_cure_radiation", "Removes the target's radiation exposure.", "対象の被ばく量を除去します.");
        addBookPage(root, "trick_guillotine", "Deals powerful slash damage to the target and makes it drop a head when killed.", "対象に強力な斬撃ダメージを与え, 討伐時に頭をドロップさせます.");
        addBookPage(root, "trick_chain_lightning.0",
                "Strikes the target with a bolt of lightning from the caster's hand. The bolt then jumps up to $(thing)Chains$(0) times (at most 8), each time to the nearest hostile creature it has not hit yet, within 6 blocks of the last target and in its line of sight.",
                "術者の手元から対象へ雷撃を放ちます. 雷撃はさらに最大 $(thing)連鎖数$(0) 回 (上限8) まで、直前の対象から6ブロック以内で見通せる、まだ当たっていない最も近い敵対的な生物へ飛び移ります.");
        addBookPage(root, "trick_chain_lightning.1",
                "The first hit deals $(thing)3 + 3 × Power$(0) damage, and each jump deals 15%% less than the one before. Creatures in water or rain take 50%% more. The first target can be anything, but the bolt only jumps to hostile creatures (monsters and anything attacking you), so allies and animals are left alone.",
                "最初の一撃のダメージは $(thing)3 + 3×威力$(0) で、飛び移るたびに直前より15%%下がります. 水中や雨の中にいる生物には50%%増しになります. 最初の対象は何でも構いませんが、連鎖先は敵対的な生物 (モンスターや術者を攻撃している生物) に限られるため、味方や動物は巻き込みません.");
        addBookPage(root, "trick_mine_genesis.0",
                "Strikes the ground at the position so a shockwave ring spreads out to $(thing)Radius$(0) blocks (2 to 12). Everything standing on the ground when the ring passes takes damage and is thrown into the air; anything already in the air is unharmed. The position must be within 3 blocks above the ground.",
                "指定位置の地面を打ち、$(thing)半径$(0) ブロック (2〜12) まで広がる衝撃波の輪を起こします. 輪が通過したときに地面に立っている生物はダメージを受けて宙へ打ち上げられ、すでに空中にいるものは無傷です. 位置は地面から3ブロック以内の高さである必要があります.");
        addBookPage(root, "trick_mine_genesis.1",
                "Damage is $(thing)4 + 3 × Power$(0) at the centre and falls to 70%% at the edge. Shields cannot block it, and a higher Power throws targets higher. The tremor only moves creatures; it never breaks blocks.",
                "ダメージは中心で $(thing)4 + 3×威力$(0)、外縁では70%%まで下がります. 盾では防げず、威力が高いほど高く打ち上げます. 揺れが動かすのは生物だけで、ブロックは壊れません.");
        addBookPage(root, "trick_burst.0",
                "Vaporizes the liquid inside the target, dealing $(thing)6 + 5 × Power$(0) damage that ignores armor and shields. Creatures with little liquid inside them, such as skeletons, golems and blazes, take only a quarter of the damage.",
                "対象の体内の液体を気化させ、防具と盾を無視する $(thing)6 + 5×威力$(0) のダメージを与えます. スケルトン、ゴーレム、ブレイズのように体内の液体が少ない生物には、ダメージが4分の1になります.");
        addBookPage(root, "trick_burst.1",
                "If the target dies from it, its body bursts and hits hostile creatures within 3 blocks for up to 40%% of the damage. The creatures that resist it are listed in the entity type tag $(thing)#psitweaks:burst_resistant$(0), which modpacks can change.",
                "この術式で対象が倒れると体が破裂し、3ブロック以内の敵対的な生物に最大でダメージの40%%を与えます. ダメージが下がる生物はエンティティタグ $(thing)#psitweaks:burst_resistant$(0) で決まっており、Modパックで変更できます.");
        addBookPage(root, "trick_kamaitachi.0",
                "Fires a crescent blade of wind from the position in the direction. It flies 24 blocks, cutting every creature in its path once, and breaks apart when it hits a solid block.",
                "指定位置から方向へ三日月形の風の刃を放ちます. 刃は24ブロック飛び、通り道の生物をそれぞれ一度ずつ切り裂き、固いブロックに当たると砕けます.");
        addBookPage(root, "trick_kamaitachi.1",
                "Each cut deals $(thing)3 + 3 × Power$(0) damage. The blade is $(thing)1.5 + 0.25 × Power$(0) blocks wide (at most 4) and reaches 0.7 blocks above and below its path.",
                "1回の斬撃のダメージは $(thing)3 + 3×威力$(0) です. 刃の幅は $(thing)1.5 + 0.25×威力$(0) ブロック (最大4) で、軌道の上下0.7ブロックまで届きます.");
        addBookPage(root, "trick_dancing_blades.0",
                "Summons $(thing)Count$(0) spectral blades (at most 8) that circle the caster. Each blade darts at a hostile creature it can see within 16 blocks, cuts it and comes back. A blade fades after two cuts, and all of them fade after 5 seconds.",
                "術者の周りを回る幻影の刃を $(thing)数$(0) 本 (上限8) 呼び出します. 刃は16ブロック以内の見通せる敵対的な生物へ飛びかかって斬りつけ、術者のもとへ戻ります. 刃は2回斬ると消え、5秒経つとすべて消えます.");
        addBookPage(root, "trick_dancing_blades.1",
                "Each cut deals $(thing)2 + 2 × Power$(0) damage. When there are several targets the blades spread out over them. Casting it again replaces the blades you already have.",
                "1回の斬撃のダメージは $(thing)2 + 2×威力$(0) です. 敵が複数いると、刃は狙いを分散させます. もう一度唱えると、今ある刃と入れ替わります.");
        addBookPage(root, "trick_starfall.0",
                "Calls $(thing)Count$(0) meteors (at most 12) down around the position. A ring on the ground marks each landing spot; after one second the meteors fall one after another, the first at the centre and the rest within 4 blocks of it.",
                "指定位置の周りに $(thing)数$(0) 個 (上限12) の流星を降らせます. 着弾点は地面の輪で示され、1秒後から流星が順に落ちてきます. 最初の1つは中心に、残りは中心から4ブロック以内に落ちます.");
        addBookPage(root, "trick_starfall.1",
                "Each meteor explodes on landing, dealing up to $(thing)5 + 4 × Power$(0) damage within 2.5 blocks (half at the edge) and setting targets on fire for 3 seconds. Meteors pass through terrain and never break blocks. Blast Protection reduces the damage.",
                "流星は着弾と同時に爆発し、2.5ブロック以内に最大 $(thing)5 + 4×威力$(0) のダメージ (外縁では半分) を与え、対象を3秒間燃やします. 流星は地形をすり抜け、ブロックを壊しません. 爆発耐性でダメージを軽減できます.");
        addBookPage(root, "trick_gravity_collapse.0",
                "Opens a gravity well at the position. For 3 seconds it drags creatures within $(thing)Radius$(0) blocks (3 to 10) towards its centre, then collapses and crushes everything close to the centre.",
                "指定位置に重力の井戸を開きます. 3秒間 $(thing)半径$(0) ブロック (3〜10) 以内の生物を中心へ引き寄せた後、崩壊して中心付近のものを押し潰します.");
        addBookPage(root, "trick_gravity_collapse.1",
                "The collapse deals $(thing)6 + 5 × Power$(0) damage that ignores armor and shields, falling to half at the edge. It reaches 60%% of the radius, but never less than 3 blocks. While being pulled, creatures build up no fall damage.",
                "崩壊のダメージは防具と盾を無視する $(thing)6 + 5×威力$(0) で、外縁では半分になります. 崩壊が届くのは半径の60%%ですが、3ブロックより狭くはなりません. 引き寄せられている間は落下ダメージが溜まりません.");
        addBookPage(root, "trick_material_mutation", "Breaks specific blocks and transmutes them into other items. The Material Mutator can perform this process using power and Vaporized Psionic Echo.", "特定のブロックを破壊して別のアイテムへ変成させます. 物質変成機はこの処理を電力と気化サイオニックエコーで実行できます.");
        addBookPage(root, "trick_physical_propulsion", "Raycasts from the specified position in the Ray direction; if it hits a Simulated Contraption, applies propulsion to it.", "指定位置からRay方向へレイキャストし、Simulated Contraption に命中したならば推進力を与えます.");
        addBookPage(root, "operator_tan", "Returns the tangent of the target number.", "対象数値のタンジェントを返します.");
        addBookPage(root, "operator_atan", "Returns the arc tangent of the target number.", "対象数値のアークタンジェントを返します.");
        addBookPage(root, "operator_sinh", "Returns the hyperbolic sine of the target number.", "対象数値のハイパボリックサインを返します. ");
        addBookPage(root, "operator_cosh", "Returns the hyperbolic cosine of the target number.", "対象数値のハイパボリックコサインを返します.");
        addBookPage(root, "operator_tanh", "Returns the hyperbolic tangent of the target number.", "対象数値のハイパボリックタンジェントを返します.");
        addBookPage(root, "operator_greater_than", "Compares Value 1 and Value 2 as Numbers. It outputs 1 when Value 1 is greater than Value 2, otherwise 0.", "値1と値2を Number として比較します. 値1が値2より大きいなら1, そうでなければ0を出力します.");
        addBookPage(root, "operator_greater_than_or_equal", "Compares Value 1 and Value 2 as Numbers. It outputs 1 when Value 1 is greater than or equal to Value 2, otherwise 0.", "値1と値2を Number として比較します. 値1が値2以上なら1, そうでなければ0を出力します.");
        addBookPage(root, "operator_equal", "Compares Value 1 and Value 2 as Any inputs. It outputs 1 when both values are equal, otherwise 0. Numbers compare by numeric value, Vectors compare by coordinates, Item values compare their item data and source, and Block values compare dimension, position, and block state. A Block compared with a Vector uses coordinate comparison.", "値1と値2を Any 入力として比較します. 2つの値が等しいなら1, そうでなければ0を出力します. Number は数値, Vector は座標, Item はアイテムデータと source, Block はディメンション, 座標, ブロック状態を比較します. Block と Vector の比較は座標比較です.");
        addBookPage(root, "constant_string", "Outputs the entered string as a String value. The value is saved with the spell and is limited to 1024 characters.", "入力した文字列をString値として出力します. 値は術式に保存され, 最大1024文字です.");
        addBookPage(root, "operator_format_string", "Outputs a String by replacing {1}, {2}, and {3} in the saved template with up to three optional Any inputs. The template uses the same input window as Constant: String and is limited to 1024 characters. Unconnected inputs become empty text. Contextual Values such as Entity, Item, and Block use the same localized display name as Selector: Display Name. Contextual Value Lists use the same localized names as Selector: Display Name List and are joined with commas. Other values use the normal String conversion. The final output is capped by the runtime String limit.", "保存したテンプレート中の {1}, {2}, {3} を最大3つの任意 Any 入力で置換して String を出力します. テンプレートは 定数子: 文字列 と同じ入力ウィンドウで編集し, 最大1024文字です. 未接続の入力は空文字になります. Entity, Item, Block などの Contextual Value は 取得子: 表示名 と同じ現在言語表示名を使います. Contextual Value List は 取得子: 表示名リスト と同じ現在言語表示名へ変換し, コンマ区切りで埋め込みます. それ以外の値は通常の String 変換を使います. 最終出力は実行時 String 上限で制限されます.");
        addBookPage(root, "operator_from_string", "Converts the String input for the selected Plain mode. String mode returns the input as-is. Number mode parses finite numeric text and outputs 0 on invalid input. Vector mode accepts Vector[x,y,z], Vector(x,y,z), [x,y,z], or (x,y,z), case-insensitive for Vector, and outputs the zero vector when parsing fails.", "選択中の Plain モードに応じて String 入力を変換します. String モードは入力をそのまま返します. Number モードは有限の数値文字列を解析し, 無効な入力では0を出力します. Vector モードは Vector[x,y,z], Vector(x,y,z), [x,y,z], (x,y,z) を受け付けます. Vector は大文字小文字を区別せず, 解析失敗時はゼロベクトルを出力します.");
        addBookPage(root, "operator_list_from_string_list", "Converts a String List for the selected Plain mode. String mode returns the input list as-is. Number mode parses each entry as finite numeric text, so invalid entries become 0. Vector mode converts only entries matching Vector[x,y,z], Vector(x,y,z), [x,y,z], or (x,y,z), case-insensitive for Vector, and skips invalid entries.", "選択中の Plain モードに応じて String List を変換します. String モードは入力リストをそのまま返します. Number モードは各要素を有限の数値文字列として解析し, 無効な要素は0になります. Vector モードは Vector[x,y,z], Vector(x,y,z), [x,y,z], (x,y,z) に一致する要素のみ変換し, 無効な要素は無視します. Vector は大文字小文字を区別しません.");
        addBookPage(root, "operator_number_list_to_vector", "Converts a Number List with exactly three elements into a Vector. The list order is X, Y, Z. If the list does not contain exactly three elements, the spell raises a runtime error.", "要素数3の Number List を Vector に変換します. リストの順序は X, Y, Z です. 要素数が3ではない場合は実行時エラーになります.");
        addBookPage(root, "operator_vector_to_number_list", "Converts a Vector into a Number List with three elements in X, Y, Z order.", "Vector を X, Y, Z の順で3要素の Number List に変換します.");
        addBookPage(root, "operator_to_string", "Converts an Any input into a String using the same display-oriented text form as Trick: Debug. Contextual values such as Entity, Item, and Block therefore use their debug display text rather than registry IDs. String values are returned as-is. List values convert each element by the same rules and join them with commas.", "Any 入力を 作動式: デバッグ と同じ表示向けの文字列表現で String に変換します. Entity, Item, Block などの Contextual Value もレジストリIDではなくデバッグ表示相当の文字列になります. String はそのまま返します. List は各要素を同じ規則で変換し, コンマ区切りで結合します.");
        addBookPage(root, "operator_list_to_string_list", "Converts an Any input into a String List using the same display-oriented text form as Trick: Debug. Contextual values such as Entity, Item, and Block therefore use their debug display text rather than registry IDs. List inputs, including Block List, convert each element by the same rules while preserving order. Non-list inputs become a one-element String List.", "Any 入力を 作動式: デバッグ と同じ表示向けの文字列表現で String List に変換します. Entity, Item, Block などの Contextual Value もレジストリIDではなくデバッグ表示相当の文字列になります. Block List を含む List は入力順を維持して各要素を同じ規則で変換します. List 以外は1要素の String List になります.");
        addBookPage(root, "operator_get_id", "Outputs the registry ID of a Contextual Value as a String. Entity inputs output the EntityType ID, Item inputs output the Item ID, and Block inputs output the Block ID. Unsupported contextual values output an empty String.", "Contextual Value のレジストリIDを String として出力します. Entity 入力は EntityType ID, Item 入力は Item ID, Block 入力は Block ID を出力します. 対応していない contextual value は空の String を出力します.");
        addBookPage(root, "operator_get_id_list", "Outputs registry IDs from a Contextual Value List as a String List. Entity List, Item List, and Block List preserve their input order. Vector List input is converted to Block List by reading the blocks at those world coordinates. Unsupported or empty elements become empty strings.", "Contextual Value List から各要素のレジストリIDを String List として出力します. Entity List, Item List, Block List は入力順を維持します. Vector List 入力はそのワールド座標のブロックを読む Block List として変換されます. 対応していない要素や空要素は空文字列になります.");
        addBookPage(root, "selector_display_name", "Outputs the display name of a Contextual Value as a String in the caster's current language. Players and custom-named entities keep their visible names; other entities use their EntityType name, Items use their hover name, and Blocks use their block name. If the caster's language or a translation is missing, English is used, then the translation key itself.", "Contextual Value の表示名を, 術者の現在言語に応じた String として出力します. プレイヤーやカスタム名付き Entity は表示されている名前を返し, 通常の Entity は EntityType の名前, Item は hover name, Block はブロック名を返します. 術者の言語や翻訳が見つからなければ English, それもなければ翻訳キーを返します.");
        addBookPage(root, "selector_display_name_list", "Outputs display names from a Contextual Value List as a String List while preserving input order. Entity List, Item List, and Block List are supported. Vector List input is converted to Block List by reading the blocks at those world coordinates. Each element uses the same language fallback as Selector: Display Name.", "Contextual Value List から各要素の表示名を String List として出力し, 入力順を維持します. Entity List, Item List, Block List に対応します. Vector List 入力はそのワールド座標のブロックを読む Block List として変換されます. 各要素は 取得子: 表示名 と同じ言語 fallback を使います.");
        addBookPage(root, "selector_effect_list", "Outputs the registry ID of each active Mob Effect on the target Entity as a String List, including the namespace (for example, minecraft:speed). Non-living entities and living entities without active effects output an empty String List.", "対象 Entity に付与されている各 Mob Effect のレジストリ ID を, namespace を含む String List として出力します（例: minecraft:speed）. LivingEntity ではない Entity と, 有効なエフェクトがない LivingEntity は空の String List を出力します.");
        addBookPage(root, "selector_block", "Outputs the block at the target position as a Block value. Block is a read-only snapshot containing the saved position, dimension, block state, registry ID, and block tags. Pieces that take a Block also accept a Vector directly, so this is mainly for viewing Block information with Trick: Debug.", "対象座標にあるブロックを Block 型として出力します. Block は保存座標, ディメンション, ブロック状態, レジストリID, ブロックタグを持つ読み取り用スナップショットです. Block を受け取るピースには Vector をそのまま接続できるので, 主に 作動式: デバッグ で Block の情報を見たいときに使います.");
        addBookPage(root, "selector_block_list", "Gets the blocks at the positions in a Vector List as a Block List. Use this when you want to handle positions as blocks, such as when searching a list by block type.", "Vector List の座標にあるブロックを Block List として取得します. ブロックの種類でリストを検索したいなど, 座標をブロックとして扱いたい場合に使います.");
        addBookPage(root, "operator_block_state", "Outputs the block state saved in a Block value as a String, using Minecraft's command-style form such as minecraft:oak_stairs[facing=north,half=bottom]. The value comes from the Block snapshot and does not change if the world changes later.", "Block 値に保存されたブロックステートを String として出力します. 出力は minecraft:oak_stairs[facing=north,half=bottom] のような Minecraft のコマンド風表記です. 値は Block のスナップショットから取得され, 後からワールドが変化しても変わりません.");
        addBookPage(root, "operator_block_state_value", "Outputs one property value from the block state saved in a Block value. Use the String input to name a property such as facing or waterlogged. If the property does not exist, it outputs an empty String.", "Block 値に保存されたブロックステートから指定 property の値を1つ出力します. String 入力には facing や waterlogged などの property 名を指定します. property が存在しない場合は空の String を出力します.");
        addBookPage(root, "operator_block_state_entries", "Outputs the properties saved in a Block value's block state as a String List. Each entry uses property:value form, such as facing:north or waterlogged:false. Blocks with no state properties output an empty String List.", "Block 値に保存されたブロックステートのプロパティを String List として出力します. 各要素は facing:north や waterlogged:false のような property:value 形式です. state property を持たないブロックは空の String List を出力します.");
        addBookPage(root, "operator_tag_list", "Outputs registry tags from a Contextual Value as a String List. Entries use namespace:path form without #. Entity inputs return EntityType tags, not scoreboard tags.", "Contextual Value からレジストリタグを String List として出力します. 各要素は # を付けない namespace:path 形式です. Entity 入力は EntityType のタグを返し, スコアボードタグは含みません.");
        addBookPage(root, "operator_block_position", "Outputs the position saved in a Block as a Vector. Pieces that take a Vector also accept a Block directly, so this is mainly for checking coordinates with Trick: Debug.", "Block に保存された座標を Vector として出力します. Vector を受け取るピースには Block をそのまま接続できるので, 主に 作動式: デバッグ で座標を見たいときに使います.");
        addBookPage(root, "operator_block_position_list", "Gets the positions saved in a Block List as a Vector List. A Block List can also be connected directly to Vector List inputs, which then use these positions.", "Block List に保存された座標を Vector List として取得します. Vector List の入力には Block List を直接つなぐこともでき, その場合も同じ座標が使われます.");
        addBookPage(root, "selector_online_players", "Outputs the names of all online players in the caster's current world as a String List.", "術者の現在ワールドにいるオンラインプレイヤー全員の名前を String List として出力します.");
        addBookPage(root, "selector_held_item", "Outputs the target living entity's main-hand ItemStack as an Item value: a read-only snapshot of its kind, count, durability, name and so on. A dropped item, such as from Psi's Selector: Nearby Items, gives its own stack.", "対象 LivingEntity のメインハンドの ItemStack を Item 型として出力します. Item型は, アイテムの種類, 個数, 耐久値, 名前などを持つ読み取り用の値です. Psi の 取得子: 近くのアイテム などで得たドロップアイテムでは, そのスタックを出力します.");
        addBookPage(root, "selector_selected_slot_item", "Outputs the ItemStack in the slot selected by the spell as an Item value. This is the same slot used by Psi pieces such as Trick: Place Block, and is affected by Trick: Switch Target Slot. Empty slots output an empty Item.", "術式で選択しているスロットにある ItemStack を Item 型として出力します. スロットは Psi の 作動式: ブロック設置 などで扱うものと同じであり, 作動式: 指定スロット切替 の影響を受けます. 空スロットは空の Item を出力します.");
        addBookPage(root, "selector_entity_slot_item", "Outputs the Item in the target Entity's zero-based inventory slot. Players use their player inventory; other entities use their item handler when available. Unreadable, empty, negative, or out-of-range slots output an empty Item.", "対象 Entity の0始まりのインベントリスロットにある Item を出力します. Player はプレイヤーインベントリを使用し, その他の Entity は利用可能な ItemHandler を使用します. 読み取れないスロット, 空スロット, 負の番号, 範囲外の番号は空の Item を出力します.");
        addBookPage(root, "selector_internal_slot_item", "Outputs the Item in the target block's zero-based internal inventory slot. Item handlers are used first, with Container as a fallback. Blocks without a readable inventory and empty, negative, or out-of-range slots output an empty Item.", "対象ブロックの0始まりの内部インベントリスロットにある Item を出力します. ItemHandler を優先して使用し, 利用できない場合は Container を使用します. 読み取り可能なインベントリがないブロック, 空スロット, 負の番号, 範囲外の番号は空の Item を出力します.");
        addBookPage(root, "operator_item_count", "Outputs the stack count of an Item value as a Number. Empty Item values output 0.", "Item 値のスタック個数を Number として出力します. 空の Item は0を出力します.");
        addBookPage(root, "operator_item_slot", "Outputs the zero-based inventory slot number saved in an Item value. Items obtained from entity or block inventory slots retain this source slot. Items without an inventory-slot source, including empty Items, hand or equipment Items, and dropped item entities, output -1.", "Item 値に保存された0始まりのインベントリスロット番号を出力します. Entity またはブロックのインベントリスロットから取得した Item は取得元のスロット番号を保持します. 空の Item, 手や防具から取得した Item, ドロップアイテムなど, インベントリスロット由来でない Item は -1 を出力します.");
        addBookPage(root, "operator_item_total_count", "Outputs the total stack count from an Item List. If the String input is empty or not connected, all Items are counted. Otherwise only Items whose registry ID exactly matches the String are counted. Invalid IDs simply match nothing and output 0.", "Item List からスタック個数の合計を Number として出力します. String 入力が空または未接続ならすべての Item を数えます. それ以外の場合は String と registry ID が完全一致する Item だけを数えます. 無効な ID は何にも一致せず0を出力します.");
        addBookPage(root, "selector_held_items", "Outputs carried items from the target Entity as an Item List. Player inventories are read slot by slot; other entities use their item handler when available, or their hands and armor slots as a fallback. Empty inventories output an empty Item List, and a dropped item a list of its own stack.", "対象 Entity の所持アイテムを Item List として出力します. Player はインベントリをスロット単位で読み取り, その他の Entity は ItemHandler があればそれを使い, なければ手持ちと防具スロットを読み取ります. 空の場合は空の Item List を, ドロップアイテムの場合はそのスタックだけのリストを出力します.");
        addBookPage(root, "selector_internal_items", "Outputs items from the target block's internal inventory as an Item List. Item handlers and containers are read slot by slot. Blocks without readable inventories output an empty Item List.", "対象ブロックの内部インベントリを Item List として出力します. ItemHandler と Container をスロット単位で読み取ります. 読み取り可能なインベントリがない場合は空の Item List を出力します.");
        addBookPage(root, "selector_indexed_element", "Outputs the element at a zero-based index from the input List. Negative indexes count back from the end: -1 returns the last element, and -2 returns the element before it. Out-of-range indexes produce the same out-of-bounds spell error as Psi's Indexed Element.", "入力された List から, 0始まりのインデックスにある要素を出力します. 負のインデックスは末尾から数え, -1 は末尾要素, -2 はその1つ前の要素を返します. 範囲外のインデックスは Psi 本体の Indexed Element と同じ範囲外エラーになります.");
        addBookPage(root, "selector_nbt", "Outputs the target Contextual Value's top-level NBT as key:value strings in a String List. Entity inputs include their entity id key like other top-level NBT keys. Block inputs, including Vector inputs converted to Block values, read BlockEntity NBT; blocks without a BlockEntity output an empty list. Values use SNBT text.", "対象 Contextual Value のNBTトップレベルを key:value 形式の String List として出力します. Entity 入力ではエンティティIDキーも他のトップレベルNBTキーと同様に含まれます. Block 入力は, Vector 入力から変換された Block も含めて BlockEntity のNBTを読み取り, BlockEntity がないブロックは空リストになります. 値はSNBT文字列です.");
        addBookPage(root, "selector_nbt_keys", "Outputs the target Contextual Value's top-level NBT keys as a String List. Entity inputs include the entity id key. Block inputs, including Vector inputs converted to Block values, read BlockEntity NBT; blocks without a BlockEntity output an empty list.", "対象 Contextual Value のNBTトップレベルキーを String List として出力します. Entity 入力ではエンティティIDキーも含まれます. Block 入力は, Vector 入力から変換された Block も含めて BlockEntity のNBTを読み取り, BlockEntity がないブロックは空リストになります.");
        addBookPage(root, "selector_nbt_value", "Outputs the target Contextual Value's NBT value matching the String key or NBT path as SNBT text. Top-level exact key matches are checked first, including the entity id key on Entity inputs. Use paths such as Inventory[0].id or components.\"minecraft:custom_name\" for nested values. Block inputs, including Vector inputs converted to Block values, read BlockEntity NBT. Missing paths, invalid paths, and blocks without a BlockEntity output an empty String.", "対象 Contextual Value のNBTから StringキーまたはNBTパスに一致する値をSNBT文字列として出力します. Entity 入力のエンティティIDキーを含めて, トップレベルキーの完全一致を先に確認します. 深い値には Inventory[0].id や components.\"minecraft:custom_name\" のようなパスを指定できます. Block 入力は, Vector 入力から変換された Block も含めて BlockEntity のNBTを読み取ります. パスが存在しない場合, 不正なパス, BlockEntity がないブロックは空文字列になります.");
        addBookPage(root, "operator_string_partial_match", "Searches String 1 with String 2 and checks whether it contains matching text. * matches any text, ? matches one character, and [abc] matches one of the listed characters. Escape wildcard characters with a backslash. Matching is case-sensitive.", "文字列1を文字列2で検索してマッチする文字列を含むか判定します. * は任意の文字列, ? は任意の1文字, [abc] は a/b/c のいずれか1文字に一致します. ワイルドカード文字はバックスラッシュでエスケープできます. 比較は大文字小文字を区別します.");
        addBookPage(root, "operator_string_starts_with", "Outputs 1 if String 1 starts with String 2, otherwise outputs 0. The comparison is case-sensitive.", "文字列1が文字列2で始まるなら1, そうでなければ0を出力します. 比較は大文字小文字を区別します.");
        addBookPage(root, "operator_string_ends_with", "Outputs 1 if String 1 ends with String 2, otherwise outputs 0. The comparison is case-sensitive.", "文字列1が文字列2で終わるなら1, そうでなければ0を出力します. 比較は大文字小文字を区別します.");
        addBookPage(root, "operator_string_concat", "Concatenates String 1, String 2, and optional String 3 in that order.", "文字列1, 文字列2, 任意の文字列3をこの順に結合します. ");
        addBookPage(root, "operator_string_split", "Splits String 1 with String 2 as the delimiter and outputs a String List. If String 2 is not connected, a comma is used. Empty fields are preserved.", "文字列1を文字列2の区切り文字で分割し, String List を出力します. 文字列2が未接続の場合はカンマを使います. 空要素は保持されます.");
        addBookPage(root, "operator_string_slice", "Returns a selected range of characters from the input String as a new String. If A or B is not connected, the beginning or end is used. Negative numbers count from the end, out-of-range values are clamped to the valid range, and an end position at or before the start returns an empty String. Decimal values are truncated toward zero.", "入力文字列の文字数を指定して範囲を切り出して新しい文字列として返します. AまたはBが未接続なら先頭または末尾を使います. 負数は末尾から数え, 範囲外は有効範囲に収め, 終了位置が開始位置以下なら空文字列を返します. 小数部分は0方向へ切り捨てます.");
        addBookPage(root, "operator_string_length", "Returns the number of characters in the input String.", "入力した String の文字数を Number として返します. ");
        addBookPage(root, "operator_string_replace", "Replaces every literal, case-sensitive occurrence of String 2 in String 1 with String 3. String 2 is evaluated only as an exact match. If String 2 is empty, String 1 is returned unchanged. The result is limited by the runtime String length limit.", "文字列1のうち, 文字列2と大文字小文字を区別してリテラル一致するすべての部分を文字列3へ置換します. 文字列2は完全一致のみ判定します. 文字列2が空の場合は文字列1をそのまま返します. 結果は実行時のString文字数上限に従います.");
        addBookPage(root, "operator_string_trim", "Removes whitespace characters (such as spaces, tabs, and line breaks) from the beginning and end of the input String. Whitespace inside the String is preserved.", "入力した文字列の先頭と末尾にある空白（スペース, タブ, 改行など）を削除します. 文字列の途中にある空白は残ります.");
        addBookPage(root, "operator_string_list_join", "Joins the input String List with String 2 and outputs a String. If String 2 is not connected, a comma is used. Empty delimiters are allowed and concatenate the elements directly.", "入力リストを文字列2で結合し, String を出力します. 文字列2が未接続の場合はカンマを使います. 空の区切り文字は許可され, 要素を直接連結します.");
        addBookPage(root, "operator_player_name", "Outputs the player's name when the input Entity is a player. Non-player entities output an empty String.", "入力 Entity がプレイヤーならプレイヤー名を出力します. プレイヤーでない Entity は空文字列になります.");
        addBookPage(root, "operator_list_search", "Keeps only the elements of the selected List mode that match the pattern. Entity and contextual values such as Item and Block compare by registry ID; other values use the same text as Operator: To String. If the pattern has no namespace such as \"minecraft:\", only the rest of the ID is compared. * matches any text, ? one character, and [abc] one of the listed characters. The whole text must match; use *zombie* for a partial match.", "選択中の List モードから, パターンに一致する要素だけを残します. Entity と Item/Block などの Contextual Value はレジストリIDで, それ以外は 演算子: 文字列へ変換 と同じ文字列で比較します. 「minecraft:」のような先頭部分を省くと, どのModでも後半部分だけで照合します. * は任意の文字列, ? は任意の1文字, [abc] は a/b/c のいずれか1文字に一致します. 比較は全体一致で, 部分一致には *zombie* のように前後に * を付けます.");
        addBookPage(root, "operator_list_search_exclude", "Removes the elements of the selected List mode that match the pattern. Entity and contextual values such as Item and Block compare by registry ID; other values use the same text as Operator: To String. If the pattern has no namespace such as \"minecraft:\", only the rest of the ID is compared. * matches any text, ? one character, and [abc] one of the listed characters. The whole text must match; use *zombie* for a partial match.", "選択中の List モードから, パターンに一致する要素を除きます. Entity と Item/Block などの Contextual Value はレジストリIDで, それ以外は 演算子: 文字列へ変換 と同じ文字列で比較します. 「minecraft:」のような先頭部分を省くと, どのModでも後半部分だけで照合します. * は任意の文字列, ? は任意の1文字, [abc] は a/b/c のいずれか1文字に一致します. 比較は全体一致で, 部分一致には *zombie* のように前後に * を付けます.");
        addBookPage(root, "operator_random_element", "Returns one random element from the input List.", "入力された List から要素を1つランダムに返します.");
        addBookPage(root, "operator_list_add", "Adds up to three elements to the List.", "List に要素を最大3つ追加します.");
        addBookPage(root, "operator_list_remove", "Removes up to two elements from the List.", "List から要素を最大2つ削除します.");
        addBookPage(root, "operator_list_remove_indices", "Removes the elements at Number A and Number B from the selected List mode. Negative indexes count from the end, so -1 removes the last element. Both indexes are resolved against the original List, and duplicate indexes remove only one element. An out-of-range index raises an out-of-bounds spell error.", "選択中の List モードから数値Aと数値Bに対応するインデックスの要素を削除します. 負数は末尾から数え, -1 は末尾要素を削除します. どちらのインデックスも元の List を基準に解決し, 同じインデックスの場合は1要素だけ削除します. 範囲外はインデックス範囲外エラーになります.");
        addBookPage(root, "operator_list_insert", "Inserts the input element before the specified index in the selected List mode. Index 0 inserts at the beginning and -1 inserts before the last element. Indexes above the List size insert at the end, while indexes below the negative range insert at the beginning.", "選択中の List モードの指定インデックス直前へ入力要素を挿入します. 0 は先頭へ, -1 は末尾要素の直前へ挿入します. List サイズより大きいインデックスは末尾へ, 負方向の範囲を超えたインデックスは先頭へ挿入します.");
        addBookPage(root, "operator_list_slice", "Returns a selected range of elements from the selected List mode as a new List. If A is omitted, the range starts at the beginning; if B is omitted, it continues to the end. Negative numbers count from the end, out-of-range values are clamped to the valid range, and an end position at or before the start returns an empty List. Decimal values are truncated toward zero.", "選択中の List モード に対しインデックスを指定して範囲を切り出して新しい List として返します. A を省略すると先頭から, B を省略すると末尾までが対象です. 負数は末尾から数え, 範囲外は有効範囲へ丸め, 終了位置が開始位置以前なら空の List を返します. 小数は0方向へ切り捨てます.");
        addBookPage(root, "operator_list_size", "Outputs the number of elements in the List.", "List の要素数を出力します.");
        addBookPage(root, "operator_list_exclusion", "Returns a new List by removing elements from List 1 when they are also present in List 2.", "入力したList 1 の要素のうち、List 2 にも含まれる要素を除外して新しいリストとして返します.");
        addBookPage(root, "operator_list_intersection", "Returns a new List containing elements that are present in both input Lists.", "入力した2つのリストでどちらにも含まれる要素を新しいリストとして返します.");
        addBookPage(root, "operator_list_concatenation", "Combines the two input Lists. For Contextual Value Lists, duplicate elements are removed.", "入力された2つのリストを結合します. Contextual Value List の場合、重複した要素は削除されます.");
        addBookPage(root, "operator_matrix_add", "Adds matrices of the same size. The optional third matrix is added after the second.", "同じサイズの行列を加算します. 任意の第3行列は第2行列の後に加算されます.");
        addBookPage(root, "operator_matrix_subtract", "Subtracts matrices of the same size. The optional third matrix is subtracted after the second.", "同じサイズの行列を減算します. 任意の第3行列は第2行列の後に減算されます.");
        addBookPage(root, "operator_matrix_multiply", "Multiplies matrices from left to right. The columns of each matrix must match the rows of the next.", "左から右へ行列を乗算します. 各行列の列数が次の行列の行数と一致する必要があります.");
        addBookPage(root, "operator_matrix_scalar_multiply", "Multiplies every element of the matrix by the input number.", "行列のすべての要素を入力数値で乗算します.");
        addBookPage(root, "operator_matrix_transpose", "Swaps rows and columns of the matrix.", "行列の行と列を入れ替えます.");
        addBookPage(root, "operator_matrix_determinant", "Computes the determinant. The matrix must be square.", "行列式を求めます. 行列は正方行列である必要があります.");
        addBookPage(root, "operator_matrix_inverse", "Computes the inverse matrix. The matrix must be square and have a non-zero determinant.", "逆行列を求めます. 行列は正方かつ行列式が0でない必要があります.");
        addBookPage(root, "operator_matrix_extract_row", "Returns the row at the zero-based index as a Number List.", "0始まりのインデックスで指定した行を Number List として返します.");
        addBookPage(root, "operator_matrix_extract_column", "Returns the column at the zero-based index as a Number List.", "0始まりのインデックスで指定した列を Number List として返します.");
        addBookPage(root, "operator_matrix_element", "Returns the element at (row, column) using zero-based indexes.", "0始まりのインデックスで (行, 列) の要素を返します.");
        addBookPage(root, "operator_matrix_row_count", "Returns the number of rows as a Number.", "行数を Number として返します.");
        addBookPage(root, "operator_matrix_column_count", "Returns the number of columns as a Number.", "列数を Number として返します.");
        addBookPage(root, "operator_matrix_multiply_vector", "Treats the Vector or Number List as a column vector and multiplies it by the matrix on the left.", "Vector または Number List を列ベクトルとして扱い、左側の行列を掛けます.");
        addBookPage(root, "operator_matrix_column_from_list", "Converts a Number List or Vector into a column matrix. The input must contain 1 to 4 elements.", "Number List または Vector を列行列に変換します. 入力は1〜4要素である必要があります.");
        addBookPage(root, "operator_matrix_flatten", "Flattens the matrix into a Number List in row-major order.", "行列を行優先の順序で Number List に平坦化します.");
        addBookPage(root, "operator_matrix_identity", "Creates an identity matrix of the given size. The size must be 1 to 4.", "指定した次数の単位行列を作ります. 次数は1〜4である必要があります.");
        addBookPage(root, "operator_matrix_zero", "Creates a zero matrix. If the second size is omitted, a square matrix is returned.", "ゼロ行列を作ります. 第2のサイズを省略すると正方行列を返します.");
        addBookPage(root, "operator_matrix_diagonal", "Creates a diagonal matrix from a Number List or Vector. The input must contain 1 to 4 elements.", "Number List または Vector から対角行列を作ります. 入力は1〜4要素である必要があります.");
        addBookPage(root, "operator_matrix_replace_column", "Replaces the specified column with a Number List or Vector. Missing rows are filled with zero, and specifying a column beyond the current size expands the matrix with zeros.", "指定列を Number List または Vector で置換します. 足りない行は0で埋められ、現在のサイズを超える列を指定するとゼロで拡張されます.");
        addBookPage(root, "operator_matrix_replace_row", "Replaces the specified row with a Number List or Vector. Missing columns are filled with zero, and specifying a row beyond the current size expands the matrix with zeros.", "指定行を Number List または Vector で置換します. 足りない列は0で埋められ、現在のサイズを超える行を指定するとゼロで拡張されます.");
        addBookPage(root, "operator_matrix_replace_element", "Replaces the element at the zero-based [row, column] indices with the input number. The indices list must contain exactly two numbers.", "0始まりの [行, 列] で指定した成分を入力数値で置換します. 成分指定用のリストは必ず2つの数値を含む必要があります.");
        addBookPage(root, "operator_matrix_delete_row", "Deletes the row at the zero-based index from the matrix. The matrix must have at least 2 rows so that the result is not empty.", "0始まりのインデックスで指定した行を行列から削除します. 結果が空にならないよう、元の行列は2行以上である必要があります.");
        addBookPage(root, "operator_matrix_delete_column", "Deletes the column at the zero-based index from the matrix. The matrix must have at least 2 columns so that the result is not empty.", "0始まりのインデックスで指定した列を行列から削除します. 結果が空にならないよう、元の行列は2列以上である必要があります.");
        addBookPage(root, "operator_matrix_cuboid_region", "Builds a 4x4 matrix that defines an axis-aligned cuboid centered at the input position. Whole-number size components are treated as block counts: odd counts are centered on the input position, positive even counts include one extra block in the positive direction, and negative even counts include one extra block in the negative direction. Non-integer components keep continuous edge lengths. Pass the matrix to Operator: Region Vector List to turn the region into block coordinates before using Trick: Mass Block Break.", "入力位置を中心とする軸平行直方体を表す 4×4 行列を生成します. 整数のサイズ成分はブロック数として扱われます. 奇数は入力位置を中心に対称, 正の偶数は正方向へ1ブロック多く, 負の偶数は負方向へ1ブロック多く含みます. 非整数の成分は連続的な辺の長さとして扱われます. 作動式: 大規模ブロック破壊で使う前に、演算子: 領域ベクトルリスト化へ渡して領域をブロック座標に変換します.");
        addBookPage(root, "operator_region_vector_list", "Returns all block position vectors inside a 3D parallelepiped defined by a 3x4 or 4x4 matrix. The first three columns are the edge vectors and the fourth column is the start point. Vectors are returned in fixed local u, v, and w order. The operator does not inspect the world, so air, unloaded chunks, and unbreakable blocks are not filtered. The returned Vector List can be passed to Trick: Mass Block Break. Regions above the shared internal limit raise an error.", "3×4 または 4×4 の行列で定義された平行六面体の内部にあるすべてのブロック座標を Vector List として返します. 最初の3列は辺ベクトル、第4列は開始点です. Vector は局所u, v, w軸の固定順序で返されます. ワールド状態は確認しないため、空気、未読み込みチャンク、破壊不能ブロックも除外されません. 返した Vector List は作動式: 大規模ブロック破壊へ渡せます. 領域が共通の内部上限を超える場合はエラーになります.");
        String regionModesEn = "Choose the list type in the mode menu. Entity (default) tests the center of each bounding box, Vector tests each vector as given, and Block tests block positions, the same points Operator: Region Vector List returns.";
        String regionModesJa = "リストの種類はモードで選びます. Entity（既定）は当たり判定の中心、Vector はそのベクトル、Block はブロックの座標で判定します. Block の判定は 演算子: 領域ベクトルリスト化 が返す座標と同じ基準です.";
        addBookPage(root, "operator_inside_region.0", "Filters a list and returns only the elements inside the parallelepiped defined by a 3x4 or 4x4 matrix. The first three columns are the edge vectors and the fourth column is the start point. Points exactly on the boundary are counted as inside.", "3×4 または 4×4 の行列で定義された平行六面体の内部にある要素だけを残したリストを返します. 最初の3列は辺ベクトル、第4列は開始点です. 境界面上にあるものも内部とみなします.");
        addBookPage(root, "operator_inside_region.1", regionModesEn, regionModesJa);
        addBookPage(root, "operator_outside_region.0", "Filters a list and returns only the elements outside the parallelepiped defined by a 3x4 or 4x4 matrix. The first three columns are the edge vectors and the fourth column is the start point. Points exactly on the boundary are counted as inside, so they are excluded.", "3×4 または 4×4 の行列で定義された平行六面体の外部にある要素だけを残したリストを返します. 最初の3列は辺ベクトル、第4列は開始点です. 境界面上にあるものは内部とみなすため、結果から除外されます.");
        addBookPage(root, "operator_outside_region.1", regionModesEn, regionModesJa);
        addBookPage(root, "operator_alive", "Filters the input Entity List and returns only LivingEntity instances that are currently alive.", "入力した Entity List をフィルターし、現在生存中の LivingEntity のみを返します.");
        addBookPage(root, "operator_matrix_transform_vector.0", "Transforms a vector with a 3x3 or 4x4 matrix. A 3x3 matrix multiplies [x,y,z]. A 4x4 matrix treats the vector as a homogeneous point [x,y,z,1] and divides the result by w.", "3×3 または 4×4 行列でベクトルを変換します. 3×3 行列では [x,y,z] の列ベクトルとして扱います. 4×4 行列では入力された Vector を列ベクトル [x,y,z,1] として行列との積を計算し、結果 [x',y',z',w'] の各成分を w' で除算した Vector を返します.");
        addBookPage(root, "operator_matrix_transform_vector.1",
                "Modes: Vector transforms one vector. Vector List transforms each vector of a Vector List and returns them in the same order;"
                        + " if any of them cannot be transformed, such as when w becomes 0, it is an error."
                        + " A Block List also works; its block positions are used.",
                "モード: Vector は1つのベクトルを変換します. Vector List は Vector List の各要素を変換し、同じ順序で返します."
                        + " w が0になるなど、変換できない要素が1つでもあるとエラーです."
                        + " Block List もつなぐことができ、ブロックの座標が使われます.");
        addBookPage(root, "operator_matrix_linear_part", "Extracts the top-left 3x3 part of the matrix. Matrices smaller than 3x3 cause an error.", "行列の左上 3×3 部分を取り出します. 3×3 より小さい行列ではエラーになります.");
        addBookPage(root, "operator_weak_raycast", "Raycasts from Position in the Ray direction and returns the position of the first block hit. Water and lava can stop the ray.", "位置から Ray 方向へレイキャストし、最初に命中したブロック座標を返します. 水や溶岩でも停止します.");
        addBookPage(root, "operator_weak_raycast_axis", "Raycasts from Position in the Ray direction and returns the normal direction of the face of the first block or fluid hit.", "位置から Ray 方向へレイキャストし、最初に命中したブロックまたは流体の面の法線方向を返します.");
        addBookPage(root, "operator_strong_raycast", "Raycasts from Position in the Ray direction and returns the position of the first block hit. Blocks without a collision shape are ignored.", "位置から Ray 方向へレイキャストし、最初に命中したブロック座標を返します. 当たり判定のないブロックを無視します.");
        addBookPage(root, "operator_strong_raycast_axis", "Raycasts from Position in the Ray direction and returns the normal direction of the face of the first block with a collision shape hit.", "位置から Ray 方向へレイキャストし、最初に命中した当たり判定を持つブロックの面の法線方向を返します.");
        addBookPage(root, "macro_caster_raycast", "Raycasts from the caster's position in the look direction and returns the coordinates of the hit block.", "術者の位置から視線方向へレイキャストし、命中したブロック座標を返します. ");
        addBookPage(root, "macro_caster_raycast_axis", "Raycasts from the caster's position in the look direction and returns the normal direction of the hit face.", "術者の位置から視線方向へレイキャストし、命中した面の法線方向を返します.");
        addBookPage(root, "macro_caster_weak_raycast", "Raycasts from the caster's position in the look direction and returns the coordinates of the hit block or fluid.", "術者の位置から視線方向へレイキャストし、命中したブロックや流体の座標を返します. ");
        addBookPage(root, "macro_caster_weak_raycast_axis", "Raycasts from the caster's position in the look direction and returns the normal direction of the face of the hit block or fluid.", "術者の位置から視線方向へレイキャストし、命中したブロックや流体の面の法線方向を返します.");
        addBookPage(root, "macro_caster_strong_raycast", "Raycasts from the caster's position in the look direction and returns the coordinates of the hit block with a collision shape.", "術者の位置から視線方向へレイキャストし、命中した当たり判定のあるブロックの座標を返します.");
        addBookPage(root, "macro_caster_strong_raycast_axis", "Raycasts from the caster's position in the look direction and returns the normal direction of the face of the hit block with a collision shape.", "術者の位置から視線方向へレイキャストし、命中した当たり判定のあるブロックの面の法線方向を返します.");
        addBookPage(root, "macro_caster_axial_offset", "Moves the given position by Left/Right, Forward/Backward, and Up/Down relative to the caster's horizontal facing. Facing is rounded to the nearest horizontal cardinal direction, and pitch is ignored. Positive values move right, forward, and up; negative values move the opposite way. Unconnected parameters are treated as 0. The mode button switches to vector input: a single vector whose components are distances in the caster's view frame (X = right, Y = up, Z = forward), equivalent to adding the result of Macro: Caster Axial Rotation to the position.", "指定した位置を、術者の水平方向の向きを基準に左右・前後・上下へ平行移動します. 向きは最も近い水平4方角へ丸められ、視線の上下成分は無視されます. 正の値で右・前・上へ, 負の値で逆へ移動します. 未接続のパラメータは 0 として扱われます. モードボタンでベクトル入力に切り替えると、視点座標(X=右、Y=上、Z=前)の距離を成分とするベクトル1つで指定できます. これは位置にマクロ: 術者方角回転の結果を加算するのと同等です.");
        addBookPage(root, "macro_caster_axial_rotation", "Rotates the given direction vector from the caster's view frame into world space. The X component is treated as right, Y as up, and Z as forward relative to the caster's horizontal facing, which is rounded to the nearest cardinal direction; pitch is ignored. For example, (0, 0, 1) returns the caster's facing direction and (1, 0, 0) returns the direction to the caster's right. The vector's magnitude is preserved.", "指定した方向ベクトルを術者の視点座標からワールド座標へ回転します. X成分を右、Y成分を上、Z成分を前として、最も近い水平4方角へ丸めた術者の向きを基準に変換します. 視線の上下成分は無視されます. 例えば (0, 0, 1) は術者の正面方向、(1, 0, 0) は術者の右方向を返します. ベクトルの大きさは保存されます.");
        addBookPage(root, "macro_caster_axial_offset_3d", "Like Macro: Caster Axial Offset, but the facing is rounded to the nearest of the six axis directions, including straight up and down. When looking straight up or down, forward points vertically and the Up/Down axis becomes horizontal, so offsets form a horizontal plane. This is useful for effects like 3x3 area mining, where the mined plane should become the floor or ceiling when looking up or down. The mode button switches to vector input: a single vector whose components are distances in the caster's view frame (X = right, Y = view-up, Z = forward), equivalent to adding the result of Macro: Caster Axial Rotation (3D) to the position.", "マクロ: 術者方角平行移動と同じですが、向きは真上・真下を含む6方角の最も近い方向へ丸められます. 真上や真下を向いた時は前後が垂直になり、上下の軸が水平になるため、オフセット平面が水平に張られます. 上や下を向いた時に掘削平面を床や天井にしたい 3×3 範囲破壊などに有用です. モードボタンでベクトル入力に切り替えると、視点座標(X=右、Y=視点上、Z=前)の距離を成分とするベクトル1つで指定できます. これは位置にマクロ: 術者方角回転 (3D) の結果を加算するのと同等です.");
        addBookPage(root, "macro_caster_axial_rotation_3d", "Like Macro: Caster Axial Rotation, but the facing is rounded to the nearest of the six axis directions, including straight up and down. The X component is treated as right, Y as view-up, and Z as forward. When looking straight up or down, Z points vertically and Y becomes horizontal, matching the orientation of the view.", "マクロ: 術者方角回転と同じですが、向きは真上・真下を含む6方角の最も近い方向へ丸められます. X成分を右、Y成分を視点上、Z成分を前として変換します. 真上や真下を向いた時はZが垂直、Yが水平になり、視点の向きに一致します.");
        addBookPage(root, "macro_face_axial_offset.0", "Moves a position vector relative to the block face hit by a raycast from the caster. Position is optional; when omitted, the block position hit by that same raycast is used. Positive Left/Right moves to the right as the face is viewed, positive Forward/Backward moves inward into the block, and positive Up/Down moves toward the top as the face is viewed; negative values move in the opposite directions. On the top and bottom faces, the caster's horizontal facing changes where Up/Down and Left/Right lead. The mode button switches to vector input, allowing the distances to be specified with one vector whose components are X = right, Y = up, and Z = inward into the block. If the raycast misses, a null-vector error is raised.", "術者からのレイキャストが命中したブロック面を基準に位置ベクトルを移動します. 位置入力は任意で、省略時はその同じレイキャストが命中したブロック座標を使います. 左右の正の値は面を見た時の右へ、前後の正の値はブロック内側方向へ、上下の正の値は面を見た時の上へ移動し、負の値は逆方向です. 上面と下面では、術者の水平向きによって上下、左右の行き先が変わります. モードボタンでベクトル入力に切り替えると、X=右、Y=上、Z=ブロック内側の距離を成分とするベクトル1つで指定できます. レイキャストが命中しなかった場合は null vector エラーになります.");
        addBookPage(root, "macro_face_axial_offset.1", "When a program contains multiple Face Axial Offsets or Face Axial Rotations, only the first piece executed performs a raycast. The hit block and face are saved and used by all later instances of either piece, so breaking the block partway through the spell does not affect them. If Position is supplied to Face Axial Offset, that input takes precedence.", "1つのプログラムに面基準平行移動または面基準回転が複数存在するとき、最初に実行されたピースだけがレイキャストを行います. 命中ブロックと面は保存され、以降のこれらのピースで使用されるため、術式途中でブロックが破壊されても影響を受けません（面基準平行移動に位置を入力した場合はそちらが優先されます）.");
        addBookPage(root, "macro_face_axial_rotation.0", "Rotates the specified direction vector according to the orientation of the block face hit by a raycast from the caster. X points right as the face is viewed, Y points toward the top as the face is viewed, and Z points inward into the block. On the top and bottom faces, the caster's horizontal facing determines the rotation within the face. The vector's magnitude is preserved. If the raycast misses, a null-vector error is raised.", "術者からのレイキャストが命中したブロック面の向きに応じて、指定した方向ベクトルを回転します. Xは面を見た時の右、Yは面を見た時の上、Zはブロック内側方向です. 上面と下面では、術者の水平向きによって面内の回転を決めます. ベクトルの大きさは保存されます. レイキャストが命中しなかった場合は null vector エラーになります.");
        addBookPage(root, "macro_face_axial_rotation.1", "Face Axial Offset and Face Axial Rotation in the same program share the raycast result of the first one executed. See page 2 of $(l:psitweaks_spell_pieces/macro_face_axial_offset)$(o)Macro: Face Axial Offset$(0)$(/l) for details.", "同じプログラムにある 面基準平行移動 と 面基準回転 は, 最初に実行されたピースのレイキャスト結果を共有します. 詳しくは $(l:psitweaks_spell_pieces/macro_face_axial_offset)$(o)マクロ: 面基準平行移動$(0)$(/l) の2ページ目を参照してください.");
        addBookPage(root, "trick_mass_block_break", "Breaks blocks at the coordinates in a Vector List (a Block List also works). Duplicate positions count once, and only the first positions up to the maximum block count (at most 4096) are processed. The maximum must be a positive integer constant. Drops go into the caster's inventory; overflow drops at the caster's position, or at the focal point if the task is interrupted.", "Vector List（Block List も可）の座標にあるブロックを破壊します. 重複した座標は1回だけ扱い, 先頭から破壊数の上限まで(最大4096)を処理します. 上限は正の整数定数である必要があります. ドロップは術者のインベントリに入り, 入りきらない分は術者の位置(タスクが中断されたときは焦点位置)に落ちます.");
    }

    private void addCadDisassembler(JsonObject root) {
        root.addProperty("message.psitweaks.cad_disassembler.unsupported_tic_cad", switch (locale) {
            case "ja_jp" -> "このTiCCADは安全に分解できませんでした";
            default -> "Failed to safely disassemble this TiCCAD.";
        });
        root.addProperty("psi.book.page.psitweaks_machine.cad_disassembler", switch (locale) {
            case "ja_jp" -> "CADを解体して部品を回収するための作業台です. CADを持ってスニーク右クリックすると, 装着されている構成部品と装填済みの術式弾を取り出してCADを分解します.";
            default -> "A workbench for disassembling CADs and recovering their parts. Sneak-right-click it with a CAD to break the CAD into its installed components and loaded spell bullets.";
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
            case "ja_jp" -> "最大12個の術式弾を保持し, 使用すると装備中のCADの対応スロットと入れ替えます.";
            default -> "Stores up to twelve spell bullets and swaps them with the matching slots of your equipped CAD when used.";
        });
        root.addProperty("psi.book.page.psitweaks_item.portable_cad_assembler", switch (locale) {
            case "ja_jp" -> "手持ちで使えるCAD組立機です. ブロックを設置せずに組立機画面を開けるため, 拠点外でも簡単にCAD部品や装填済み術式弾を調整できます.";
            default -> "A handheld CAD Assembler. Use it to open an assembler interface without placing the block, making it easy to adjust CAD parts or loaded spell bullets while away from base.";
        });
        root.addProperty("message.psitweaks.portable_spell_programmer.no_target", switch (locale) {
            case "ja_jp" -> "オフハンドに書き込み可能なアイテムがありません";
            default -> "There is no writable item in your offhand.";
        });
        root.addProperty("message.psitweaks.portable_spell_programmer.empty_spell", switch (locale) {
            case "ja_jp" -> "術式が空です";
            default -> "The spell is empty.";
        });
        root.addProperty("message.psitweaks.portable_spell_programmer.no_selected_bullet", switch (locale) {
            case "ja_jp" -> "選択中のスロットに書き込み可能な術式弾がありません";
            default -> "The selected slot has no writable spell bullet.";
        });
        root.addProperty("message.psitweaks.portable_spell_programmer.compile_failed", switch (locale) {
            case "ja_jp" -> "術式をコンパイルできません";
            default -> "The spell cannot be compiled.";
        });
        root.addProperty("psi.book.page.psitweaks_item.portable_spell_programmer.0", switch (locale) {
            case "ja_jp" -> "手持ちで使える魔法構築機です。右クリックすると、魔法構築機と同じスペルプログラム画面を開くことができます。";
            default -> "A handheld Spell Programmer. Right-click to open the same spell programming screen as the Spell Programmer.";
        });
        root.addProperty("psi.book.page.psitweaks_item.portable_spell_programmer.1", switch (locale) {
            case "ja_jp" -> "メインハンドに携帯型魔法構築機を、オフハンドに術式弾やCADを持ち、スニーク右クリックすると、編集中の魔法を登録します。CADなどでは、現在選択している術式弾に登録されます。";
            default -> "Hold the Portable Spell Programmer in your main hand and a spell bullet or CAD in your offhand, then sneak-right-click to register the spell you are editing. For a CAD or another socketable device, this writes to its currently selected spell bullet.";
        });
        root.addProperty("psi.book.page.psitweaks_item.auto_casters.0", switch (locale) {
            case "ja_jp" -> "術式自動詠唱デバイスは, Curiosスロットに装備して自動で術式を詠唱する装備です.$(p)術式弾は キュリオスコントローラ で変更できます.";
            default -> "Auto Casters are worn in Curios slots and cast spells automatically.$(p)Change their spell bullets with the Curios Controller.";
        });
        root.addProperty("psi.book.page.psitweaks_item.auto_casters.1", switch (locale) {
            case "ja_jp" -> "tick型は毎tick詠唱します. カスタムtick型は, アイテム使用で1から1200tickの範囲から詠唱間隔を設定できます.";
            default -> "The tick model fires every tick. The custom tick model lets you set an interval from 1 to 1200 ticks by using the item.";
        });
        root.addProperty("psi.book.page.psitweaks_item.curios_controller", switch (locale) {
            case "ja_jp" -> "魔法演算領域スロットに装備した術式自動詠唱デバイスなどの選択術式弾スロットを, 外装コントローラと同じ要領で切り替えます.";
            default -> "Switches the selected bullet slot of Auto Casters and other socketable items in Magic Calculation Area slots, the same way the Exosuit Controller does.";
        });
        root.addProperty("psi.book.page.psitweaks_item.flash_charm", switch (locale) {
            case "ja_jp" -> "装備者の盲目と暗闇を継続的に解除するCuriosチャームです.$(p)インベントリ内に持っているだけでも機能するため, ディープダークや視界妨害効果への対策になります.";
            default -> "A Curios charm that continually removes Blindness and Darkness from the wearer.$(p)It also works while carried in inventory, making it useful in the Deep Dark and against vision-disrupting effects.";
        });
        root.addProperty("psi.book.page.psitweaks_item.interference_range_extender", switch (locale) {
            case "ja_jp" -> "魔法演算領域スロットに装備し, Psi術式とレイキャストの有効射程を64ブロックへ拡張します.$(p)サードアイデバイスより作りやすい下位装備で, 両方を装備した場合はサードアイデバイスの効果が優先されます.";
            default -> "Equips in the Magic Calculation Area slot and extends Psi spell and raycast range to 64 blocks.$(p)It is a lower-tier alternative to the Third-Eye Device; when both are equipped, the Third-Eye Device takes priority.";
        });
        root.addProperty("psi.book.page.psitweaks_item.third_eye_device", switch (locale) {
            case "ja_jp" -> "魔法演算領域スロットに装備し, 術者に対する通常の術式射程チェックを無効化します.$(p)通常のCAD射程を大きく超える位置を対象にできるため, 扱いには注意してください.";
            default -> "Equips in the Magic Calculation Area slot and removes Psi's normal spell radius check for the caster.$(p)This lets spells target positions far beyond ordinary CAD range limits, so use it carefully.";
        });
        root.addProperty("psi.book.page.psitweaks_item.sorcery_booster", switch (locale) {
            case "ja_jp" -> "魔法演算領域スロットに装備し, 術式ダメージを30%%増加させます.$(p)素材に $(l:components/psitweaks_magicians_brain)$(o)$(item)魔法師の脳$(0)$(/l) を使います.";
            default -> "Equips in the Magic Calculation Area slot and increases spell damage by 30%%.$(p)It is made from a $(l:components/psitweaks_magicians_brain)$(o)$(item)Magician's Brain$(0)$(/l).";
        });
        root.addProperty("psi.book.page.psitweaks_item.gravstringer.0", switch (locale) {
            case "ja_jp" -> "$(item)グラヴストリンガー$(0) は, サイメタルの弓の上位互換となる弓です. 矢のダメージと引き絞り速度が大幅に強化され, 耐久値を持たず, 11個の術式弾ソケットを備えます.$(p)専用弾の $(item)トンネラー$(0) を射出することが可能です.";
            default -> "The $(item)Gravstringer$(0) is an upgraded version of the Psimetal Bow. Arrow damage and draw speed are greatly improved, it has no durability, and it provides 11 spell bullet sockets.$(p)It can fire its dedicated $(item)Tunneler$(0) ammunition.";
        });
        root.addProperty("psi.book.page.psitweaks_item.gravstringer.1", switch (locale) {
            case "ja_jp" -> "スニーク+左クリックで射撃モードを切り替えます. 射撃モードはトンネラーを射出する際のみ影響します.$(li)通常モード: 5秒間重力の影響を受けず直進する高速の矢を3本放ちます.$(li)低速モード: 非常に低速で重力の影響を受けない矢を3本発射します. 60秒で消滅します.$(li)ハイブリッドモード: 中央に通常, 左右に低速の矢を放ちます.$(p)詠唱した術式弾は中央の矢にのみ付与されます.";
            default -> "Sneak + left-click cycles the firing mode. Firing modes only apply when firing Tunnelers.$(li)Normal: fires 3 high-speed arrows that fly straight, unaffected by gravity for 5 seconds.$(li)Slow: fires 3 extremely slow arrows that are unaffected by gravity. They expire after 60 seconds.$(li)Hybrid: a volley with a Normal arrow in the center and Slow arrows on both sides.$(p)The cast spell bullet is applied only to the center arrow.";
        });
        root.addProperty("psi.book.page.psitweaks_item.tunneler.0", switch (locale) {
            case "ja_jp" -> "$(item)トンネラー$(0) は, $(item)グラヴストリンガー$(0) の専用弾です. ブロックを貫通し、防御力や耐性も無効化します. 射手自身には命中しません.$(p)矢はmobに命中しても消滅せず貫通し, 同じmobには1秒のクールタイム後に再度命中します.";
            default -> "The $(item)Tunneler$(0) is the dedicated ammunition of the $(item)Gravstringer$(0). It pierces through blocks and nullifies armor and resistances. It never strikes its shooter.$(p)The arrow pierces through mobs instead of disappearing, striking the same target again after a 1-second cooldown.";
        });
    }

    private void addMovalSuitBook(JsonObject root) {
        root.addProperty("psi.book.entry.psitweaks_moval_suit", switch (locale) {
            case "ja_jp" -> "M.O.V.A.L. スーツ";
            default -> "M.O.V.A.L. Suit";
        });
        root.addProperty("psi.book.page.psitweaks_item.moval_suit.0", switch (locale) {
            case "ja_jp" -> "$(item)M.O.V.A.L. スーツ$(0) は, $(l:components/psitweaks_heavy_psimetal)$(o)$(item)ヘビーサイメタル$(0)$(/l) と $(item)エボニーサイメタル$(0) を使って製作する高性能なPsi系防具です.";
            default -> "The $(item)M.O.V.A.L. Suit$(0) is a high-grade Psi armor set crafted with $(l:components/psitweaks_heavy_psimetal)$(o)$(item)Heavy Psimetal$(0)$(/l) and $(item)Ebony Psimetal$(0).";
        });
        root.addProperty("psi.book.page.psitweaks_item.moval_suit.1", switch (locale) {
            case "ja_jp" -> "ヘルメットは外装センサーに対応し, チェストプレートはダメージ時, レギンスはtick時, ブーツはジャンプ時に詠唱します.$(p)各部位は術式ダメージ+10%%, Psi回復量+5, 最大Psi量+500を付与します.";
            default -> "The helmet accepts exosuit sensors, the chestplate casts on damage, the leggings cast on tick, and the boots cast on jump.$(p)Each piece grants +10%% spell damage, +5 Psi regeneration, and +500 maximum Psi.";
        });
    }

    private void addMekanismIntegrationBook(JsonObject root) {
        root.addProperty("psi.book.entry.psitweaks_mekanism_integration", switch (locale) {
            case "ja_jp" -> "Mekanism連携";
            default -> "Mekanism Integration";
        });
        root.addProperty("psi.book.title.psitweaks_mekanism_integration.infusing", switch (locale) {
            case "ja_jp" -> "冶金吹き込み";
            default -> "Metallurgic Infusing";
        });
        root.addProperty("psi.book.title.psitweaks_mekanism_integration.mekasuit", switch (locale) {
            case "ja_jp" -> "MekaSuitモジュール";
            default -> "MekaSuit Modules";
        });
        root.addProperty("psi.book.page.psitweaks_mekanism_integration.0", switch (locale) {
            case "ja_jp" -> "$(thing)PsiTweaks$(0) は $(thing)Psi$(0) を $(thing)Mekanism$(0) の進行へ接続します. 連携要素には, Psi素材を作る冶金吹き込みレシピ, 魔法師能力を伸ばすMekaSuitモジュール, Psi・FE・サイオニック資源を扱う機械があります.$(p)正確なレシピはJEIで確認してください.";
            default -> "$(thing)PsiTweaks$(0) ties $(thing)Psi$(0) into $(thing)Mekanism$(0) progression. Its integration covers Metallurgic Infusing recipes for Psi materials, MekaSuit modules that improve caster stats, and machines that exchange Psi, FE, and psionic resources.$(p)See JEI for exact recipes.";
        });
        root.addProperty("psi.book.page.psitweaks_mekanism_integration.1", switch (locale) {
            case "ja_jp" -> "$(item)冶金吹き込み機$(0) では, PsiTweaks の注入タイプを使って主要なPsi素材を作れます. サイジェム注入はレッドストーンを$(l:components/psidust)$(o)$(item)サイダスト$(0)$(/l)に, 金インゴットを$(item)サイメタル$(0)に変換します.$(p)エボニー注入とアイボリー注入では, $(item)エボニー基質$(0), $(item)アイボリー基質$(0), それぞれのサイメタル派生素材を作れます. さらに$(l:components/psitweaks_chaotic_factor)サイオニック因子$(/l), $(l:components/psitweaks_chaotic_psimetal)$(o)$(item)カオティックサイメタル$(0)$(/l), $(l:components/psitweaks_psionic_echo)サイオニックエコー素材$(/l), $(l:components/psitweaks_alloy_hypostasis)位格合金$(/l)へ発展します.";
            default -> "The $(item)Metallurgic Infuser$(0) can produce core Psi materials with PsiTweaks infuse types. Psigem infusion turns redstone into $(l:components/psidust)$(o)$(item)Psidust$(0)$(/l) and gold into $(item)Psimetal$(0).$(p)Ebony and Ivory infusion can make $(item)Ebony Substance$(0), $(item)Ivory Substance$(0), and their Psimetal variants. Later recipes extend this chain into $(l:components/psitweaks_chaotic_factor)psionic factors$(/l), $(l:components/psitweaks_chaotic_psimetal)$(o)$(item)Chaotic Psimetal$(0)$(/l), $(l:components/psitweaks_psionic_echo)Psionic Echo materials$(/l), and $(l:components/psitweaks_alloy_hypostasis)Hypostasis-tier metal$(/l).";
        });
        root.addProperty("psi.book.page.psitweaks_mekanism_integration.2", switch (locale) {
            case "ja_jp" -> "PsiTweaks は魔法師向けのMekaSuitモジュールを3種類追加します. $(item)サイオン供給ユニット$(0) と $(item)サイオン容量ユニット$(0) はMekaSuit胴体に装着し, Psi回復速度と最大Psi保有量を強化します. これらはPsi本体のAttributeを使用します.$(p)$(item)事象干渉力増大ユニット$(0) はMekaSuitヘルメットに装着し, 術式ダメージを増加させます.";
            default -> "PsiTweaks adds three MekaSuit modules for spellcasters. The $(item)Psyon Supplying Unit$(0) and $(item)Psyon Capacity Unit$(0) install in the MekaSuit body armor and improve Psi regeneration and maximum Psi. These bonuses use Psi's own attributes.$(p)The $(item)Phenomenon Interference Enhancement Unit$(0) installs in the MekaSuit helmet and increases spell damage.";
        });
    }

    private void addMaterialBookAdditions(JsonObject root) {
        root.addProperty("psi.book.page.psitweaks_material.pellet_neptunium", switch (locale) {
            case "ja_jp" -> "$(item)ポロニウムブロック$(0) を $(l:psitweaks_spell_pieces/trick_material_mutation)$(o)作動式: 物質変成$(0)$(/l) で変成するか, $(l:psitweaks_machines/material_mutator)$(o)物質変成機$(0)$(/l) で加工して作成します.$(p)MekaSuit用の $(item)事象干渉力増大ユニット$(0) に使用します.";
            default -> "Made by mutating a $(item)Polonium Block$(0) with $(l:psitweaks_spell_pieces/trick_material_mutation)$(o)Trick: Material Mutation$(0)$(/l), or by processing it in the $(l:psitweaks_machines/material_mutator)$(o)Material Mutator$(0)$(/l).$(p)Used for the MekaSuit $(item)Phenomenon Interference Enhancement Unit$(0).";
        });
        root.addProperty("psi.book.page.psitweaks_material.magicians_brain", switch (locale) {
            case "ja_jp" -> "$(l:psitweaks_spell_pieces/trick_guillotine)$(o)作動式: ギロチン$(0)$(/l) で魔法師の村人を倒したときに入手できます.$(p)$(l:items/psitweaks_sorcery_booster)$(o)$(item)ソーサリーブースター$(0)$(/l) と $(item)事象干渉力増大ユニット$(0) に使用します.";
            default -> "Dropped when a Magician villager is killed by $(l:psitweaks_spell_pieces/trick_guillotine)$(o)Trick: Guillotine$(0)$(/l).$(p)Used for the $(l:items/psitweaks_sorcery_booster)$(o)$(item)Sorcery Booster$(0)$(/l) and the Phenomenon Interference Enhancement Unit module.";
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

    private void addConnectorTranslations(JsonObject root) {
        boolean japanese = "ja_jp".equals(locale);
        root.addProperty("block.psitweaks.ideaspace_connector", japanese ? "イデアコネクター" : "Ideaspace Connector");
        String[][] labels = {
                {"published", "公開する資源（9枠）", "Published resources (9 slots)"},
                {"settings", "搬入出設定(共通)", "I/O settings (Shared)"},
                {"export_settings", "自動搬出量・間隔", "Export rate"},
                {"export_common_title", "搬出量・間隔(共通)", "Export rate (Shared)"},
                {"export_slot_title", "搬出量・間隔(枠 %s)", "Export rate (Slot %s)"},
                {"export_amount", "1回の量", "Amount"},
                {"export_interval", "間隔 (tick)", "Interval (tick)"},
                {"export_tick_hint", "20 tick = 1秒", "20 ticks = 1 second"},
                {"export_amount_hint", "設定範囲: 1 ～ %s", "Range: 1 - %s"},
                {"export_interval_hint", "設定範囲: 1 ～ %s", "Range: 1 - %s"},
                {"export_type.0", "アイテム", "Items"},
                {"export_type.1", "液体 (mB)", "Fluid (mB)"},
                {"export_type.2", "化学物質 (mB)", "Chem. (mB)"},
                {"export_type.3", "FE", "FE"},
                {"slot_settings", "搬入出設定(公開枠 %s)", "I/O settings (Slot %s)"},
                {"use_common", "共通設定を使用", "Use shared settings"},
                {"use_individual", "個別設定を使用", "Use individual settings"},
                {"inherit_hint", "個別設定に切り替えると、現在の共通設定をコピーします。", "Switching to individual settings copies the current shared settings."},
                {"back", "戻る", "Back"},
                {"faces", "面ごとの入出力・自動搬出", "Connections / auto output"},
                {"mode.both", "入出力", "In / Out"},
                {"mode.input", "搬入のみ", "Input"},
                {"mode.output", "搬出のみ", "Output"},
                {"mode.disabled", "無効", "Disabled"},
                {"auto_on", "自動: 有効", "Auto: ON"},
                {"auto_off", "自動: 無効", "Auto: OFF"},
                {"redstone_title", "レッドストーン", "Redstone"},
                {"redstone.always", "常時動作", "Always active"},
                {"redstone.high", "信号ありで動作", "When powered"},
                {"redstone.low", "信号なしで動作", "When unpowered"},
                {"redstone_hint", "%s面の動作条件です。信号はどの面から受けてもブロック全体で共有し、停止中はこの面の搬入・搬出をすべて止めます。",
                        "Condition for the %s face. A signal on any face counts for the whole block. While inactive, this face stops all input and output."},
                {"redstone_all.always", "全6面: 常時", "All: Always"},
                {"redstone_all.high", "全6面: 信号あり", "All: Powered"},
                {"redstone_all.low", "全6面: 信号なし", "All: Unpowered"},
                {"redstone_all.mixed", "全6面: 個別", "All: Mixed"},
                {"redstone_all_hint", "全6面の条件をまとめて切り替えます。面ごとに違う場合は常時に揃えます。",
                        "Switches all six faces together. Mixed faces are reset to Always."},
                {"face.down", "下", "Down"}, {"face.up", "上", "Up"},
                {"face.north", "北", "North"}, {"face.south", "南", "South"},
                {"face.west", "西", "West"}, {"face.east", "東", "East"},
                {"help", "アイテムを持って左クリック、液体・化学物質の容器を持って右クリックで公開枠に設定します。資源は消費しません。",
                        "Click a slot with an item (left) or a fluid/chemical container (right) to set it. Nothing is consumed."},
                {"energy_hint", "公開枠にドラッグしてFEを設定", "Drag onto a published slot to assign FE"},
                {"template_too_large", "このアイテムは設定データが大きすぎます。", "This item's filter data is too large."},
                {"amount", "イデアストレージ内: %s", "In Ideaspace Storage: %s"},
                {"clear_hint", "何も持たずに右クリックで解除", "Right-click with an empty cursor to clear"},
                {"load_failed", "ストレージを読み込めません。", "Storage could not be loaded."},
                {"input_filters", "搬入フィルタ", "Input filter"},
                {"input_list", "搬入フィルタ（9枠）", "Input filter (9 slots)"},
                {"input_mode.any", "すべて許可", "Accept all"},
                {"input_mode.allow_list", "許可リスト", "Allow list"},
                {"input_mode.deny_list", "拒否リスト", "Deny list"},
                {"input_mode.existing", "在庫のある種類のみ", "Stored types only"},
                {"input_hint", "全6面の搬入に適用します。アイテムを持って左クリック、液体・化学物質の容器を持って右クリックで登録します。耐久値などのデータまで一致するものが対象で、FEは常に受け入れます。",
                        "Applies to input on all six faces. Click a slot with an item (left) or a fluid/chemical container (right) to register it. Only resources with identical data, such as durability, match. FE is always accepted."},
                {"stock_settings", "在庫条件", "Stock rules"},
                {"stock_common_title", "在庫条件(共通)", "Stock rules (Shared)"},
                {"stock_slot_title", "在庫条件(枠 %s)", "Stock rules (Slot %s)"},
                {"minimum_stock", "開始在庫", "Min. storage stock"},
                {"target_stock", "目標在庫", "Target stock"},
                {"minimum_stock_hint", "ストレージにこの量以上あるときだけ自動搬出します。0で条件なし。",
                        "Auto export runs only while storage holds at least this amount. 0 = no condition."},
                {"target_stock_hint", "搬出先にある同じ資源がこの量になるまで自動搬出します。",
                        "Auto export tops the destination up to this amount of the same resource."},
                {"target_disabled", "無効", "Off"},
                {"stock_hint", "目標在庫: 空欄で無効", "Target stock: blank = off"},
                {"copy", "コピー", "Copy"},
                {"paste", "貼り付け", "Paste"},
                {"copy_hint", "イデアコネクターの設定をクリップボードにコピーします。Ctrl+Cでも操作可能です。",
                        "Copies this connector's settings to the clipboard. Ctrl+C also works."},
                {"paste_hint", "クリップボードからイデアコネクターの設定を貼り付けます。Ctrl+Vでも操作可能です。",
                        "Pastes connector settings from the clipboard. Ctrl+V also works."},
                {"clipboard.copied", "設定をクリップボードにコピーしました", "Copied the settings to the clipboard"},
                {"clipboard.copy_too_large", "設定データが大きすぎるためコピーできません", "The settings are too large to copy"},
                {"clipboard.pasted", "設定を貼り付けました", "Pasted the settings"},
                {"clipboard.pasted_skipped", "設定を貼り付けました。この環境で使えない資源 %s 件は空欄にしました", "Pasted the settings. %s resources unavailable here were left empty"},
                {"clipboard.empty", "クリップボードが空です", "The clipboard is empty"},
                {"clipboard.too_large", "クリップボードのデータが大きすぎます", "The clipboard content is too large"},
                {"clipboard.invalid_json", "クリップボードの内容をJSONとして読み込めません", "The clipboard does not contain valid JSON"},
                {"clipboard.not_connector", "イデアコネクターの設定ではありません", "This is not Ideaspace Connector settings"},
                {"clipboard.newer_version", "新しい形式の設定のため読み込めません", "These settings use a newer format"},
                {"clipboard.invalid_value", "設定値が正しくないため何も変更していません: %s", "Nothing changed because of an invalid value: %s"},
                {"clipboard.no_settings", "貼り付ける設定がありません", "There are no settings to paste"}
        };
        for (String[] label : labels) root.addProperty("gui.psitweaks.connector." + label[0], label[japanese ? 1 : 2]);
        addSpellPiece(root, "trick_ideaspace_connector", "Trick: Ideaspace Connector",
                "Places an Ideaspace Connector linked to your Ideaspace Storage at the specified position.",
                "作動式: イデアコネクター", "指定位置に、自分のイデアストレージへ接続するイデアコネクターを設置します。");
        addBookPage(root, "trick_ideaspace_connector.0",
                "Places an Ideaspace Connector at the input position (Vector). Its appearance uses only conjured-block particles in the CAD color. It does not expire; breaking it drops no block item and leaves your stored resources intact.",
                "位置(Vector)の座標にイデアコネクターを設置します。外観はCADの色を使った魔法ブロックのパーティクルのみです。時間経過では消えず、破壊時にブロックアイテムは落ちません。ストレージの中身は保持されます。");
        addBookPage(root, "trick_ideaspace_connector.1",
                "Incoming items, fluids, chemicals and FE enter the owner's Ideaspace Storage directly. Right-click to configure up to nine published resources. External devices can extract only these resources. Configuration consumes nothing; the connector holds no inventory. Only the owner can configure it, and it works while the owner is offline if its chunk is loaded. Chemicals require Mekanism.",
                "搬入されたアイテム・液体・化学物質・FEは、設置者のイデアストレージに直接収納されます。右クリックの設定画面で最大9種類を公開し、外部機器から搬出できます。設定に資源は消費せず、ブロック内に在庫は持ちません。設定変更は設置者のみ可能です。チャンクが読み込まれていれば設置者がオフラインでも利用できます。化学物質にはMekanismが必要です。");
        addBookPage(root, "trick_ideaspace_connector.2",
                "All six faces initially allow input and output. Each published slot can override the shared face settings. Automatic output is initially off. Export rate settings control the amount per attempt and interval for each resource type, shared by all six faces of each slot. Defaults are one stack, 1,000 mB of fluid or chemical, and 16,000 FE every 5 ticks. Transfers are limited to available stock and destination capacity.",
                "初期状態では全6面から搬入・搬出できます。面設定は共通設定を基本に、公開枠ごとに個別設定で上書きできます。自動搬出は初期状態では無効です。「自動搬出量・間隔」で資源別の1回量と間隔を設定でき、各枠の6面で共用します。初期値は5tickごとに最大1スタック、液体・化学物質1,000mB、16,000FEです。在庫や移動先の空きに応じ、移動できる分だけ搬出します。");
        addBookPage(root, "trick_ideaspace_connector.3",
                "Each face can run always, when powered, or when unpowered. Signals received on any face are shared by the whole connector. An inactive face blocks external input, extraction and automatic output for items, fluids, chemicals and FE. Conditions follow the shared or individual slot settings. The All button changes all six faces of the current profile together. Defaults to always active.",
                "各面の動作条件を「常時動作」「信号ありで動作」「信号なしで動作」から選べます。どの面から受けた信号もブロック全体で共有します。停止中の面は、アイテム・液体・化学物質・FEの外部からの搬入・搬出と自動搬出を停止します。条件は共通設定を基本に、公開枠ごとに上書きできます。「全6面」ボタンで編集中の設定の6面を一括変更できます。初期値は常時動作です。");
        addBookPage(root, "trick_ideaspace_connector.4",
                "The Input filter limits which resources are accepted. It applies to all six faces and has four modes: Accept all, Allow list, Deny list, and Stored types only. The nine list slots are separate from the published slots, match only resources with identical data such as durability, and never consume resources. Stored types only accepts types the storage holds at least one of, so a type that runs out is no longer accepted. FE is always accepted. Defaults to Accept all.",
                "「搬入フィルタ」で、受け入れる資源を制限できます。全6面に適用され、方式は「すべて許可」「許可リスト」「拒否リスト」「在庫のある種類のみ」の4つです。リスト用の9枠は公開枠とは別で、耐久値などのデータまで一致するものだけが対象です。登録しても資源は消費しません。「在庫のある種類のみ」は、ストレージに1以上ある種類だけを受け入れます。在庫が0になった種類は受け入れなくなります。FEは常に受け入れます。初期値は「すべて許可」です。");
        addBookPage(root, "trick_ideaspace_connector.5",
                "Stock rules adjust when and how much automatic export sends. Min. storage stock exports only while the storage holds at least that amount. It does not keep a reserve, so a single export may drop below it. Target stock counts the same resource visible through the connected face of the destination and exports only what is missing (blank = off). Both can be combined and follow the shared or per-slot settings. They do not apply when other devices extract from the connector.",
                "自動搬出の「在庫条件」で、搬出のタイミングと量を調整できます。「開始在庫」を設定すると、ストレージにその量以上あるときだけ搬出します。残量を確保する機能ではないので、1回の搬出で下回ることがあります。「目標在庫」を設定すると、搬出先の接続面から見える同じ資源を数え、目標に足りない分だけ搬出します（空欄で無効）。両方を併用でき、共通設定と公開枠ごとの個別設定に従います。他の機器がコネクターから引き出す場合には適用されません。");
        addBookPage(root, "trick_ideaspace_connector.6",
                "Copy on the main page puts the published slots, I/O settings, export rates, stock rules and input filters on the clipboard as JSON. Paste on another connector applies them; Ctrl+C and Ctrl+V also work. Deleting the faces, export, slots or input section from the JSON leaves that part unchanged. Faces keep their absolute directions, and resources unavailable in the world become empty.",
                "メイン画面の「コピー」で、公開枠・搬入出設定・搬出量・在庫条件・搬入フィルタをJSONとしてクリップボードに保存し、別のコネクターで「貼り付け」すると同じ設定にできます。Ctrl+C / Ctrl+Vでも操作できます。JSONから faces・export・slots・input の項目を消すと、その部分は変更しません。面は東西南北などの向きのまま貼り付け、存在しない資源は空欄になります。");
    }

    private void addIdeaStorageSelectorPages(JsonObject root) {
        String enFree = " the per-type limit minus the stock, or 0 for a new type while every type slot is in use.";
        String jpFree = "1種類あたりの上限から在庫を引いた値で、種類数が上限に達しているときに新しい種類を指定すると0です。";
        String enModes = " The mode picks the resource kind: Item (I), Fluid (F), Chemical (C) or FE.";
        String jpModes = "資源の種類はモードで選びます（アイテム I・液体 F・化学物質 C・FE）。";
        addBookPage(root, "selector_idea_storage_last_transfer.0",
                "Returns the amount actually moved by the most recent Ideaspace transfer trick that ran earlier in the same cast, as a Number."
                        + " The units match the storage amount selectors: items are counted one by one, fluids and chemicals in mB, and FE in FE."
                        + " Returns 0 if no such trick has run yet.",
                "同じ詠唱の中で、それまでに実行された最後のイデア転送の作動式が実際に移した量をNumberとして取得します。"
                        + "単位は貯蔵量の取得子と同じで、アイテムは個数、液体・化学物質はmB、FEはFEです。まだ実行されていなければ0です。");
        addBookPage(root, "selector_idea_storage_last_transfer.1",
                "Counted tricks: Deposit and Withdraw in every mode, FE included, and Slot Deposit/Withdraw."
                        + " A trick that moves nothing (an Exact Transfer that falls short included) or stops with an error records 0."
                        + " Tricks run in reading order, row by row from the top left, so connect this selector only to tricks placed after the transfer.",
                "対象は、すべてのモード（FEを含む）の格納・取出と、スロット格納・取出の作動式です。"
                        + "何も移さなかった場合（量が足りなかった完全転送を含む）や、エラーで止まった場合は0が記録されます。"
                        + "作動式は左上から1行ずつ順に実行されるため、この取得子は転送より後にある作動式だけに接続してください。");
        addBookPage(root, "selector_idea_storage_amount.0",
                "Returns how much of one resource your Ideaspace Storage holds, as a Number." + enModes
                        + " Items are counted one by one, fluids and chemicals in mB, and FE in FE.",
                "自分のイデアストレージにある資源の量をNumberとして取得します。" + jpModes
                        + "アイテムは個数、液体・化学物質はmB、FEはFE単位です。");
        addBookPage(root, "selector_idea_storage_amount.1",
                "Item mode takes an item ID as a String (e.g. minecraft:stone) or an Item, which uses that item's ID."
                        + " Fluid and Chemical modes take an ID as a String (e.g. minecraft:water, mekanism:hydrogen). FE mode has no input."
                        + " Items and fluids with different data components are counted together when their IDs match."
                        + " Invalid IDs, empty Item values and missing stock return 0.",
                "アイテムモードはアイテムIDのString（例: minecraft:stone）かItemを受け取り、ItemではそのアイテムのIDを使います。"
                        + "液体・化学物質モードはIDをString（例: minecraft:water、mekanism:hydrogen）で受け取ります。FEモードに入力はありません。"
                        + "アイテムと液体は、同じIDならデータコンポーネントが異なるものも合算します。"
                        + "不正なID・空のItem・在庫なしの場合は0を返します。");
        addBookPage(root, "selector_idea_storage_free_capacity.0",
                "Returns how much more of one resource your Ideaspace Storage can accept now, as a Number." + enModes
                        + " For items, fluids and chemicals it is" + enFree + " In FE mode it is the FE capacity minus the stored FE.",
                "自分のイデアストレージに、資源を今あとどれだけ格納できるかをNumberとして取得します。" + jpModes
                        + "アイテム・液体・化学物質では、" + jpFree + "FEモードでは、FEの上限から蓄電量を引いた値です。");
        addBookPage(root, "selector_idea_storage_free_capacity.1",
                "Item mode takes an item ID as a String, meaning the item without extra data components, or an Item, which keeps its data components."
                        + " Fluid and Chemical modes take an ID as a String; the fluid is taken without extra data components. FE mode has no input."
                        + " Invalid IDs and empty Item values return 0, and Chemical mode always returns 0 without Mekanism.",
                "アイテムモードはアイテムIDのString（追加のデータコンポーネントなし）か、Item（データコンポーネントを含む）を受け取ります。"
                        + "液体・化学物質モードはIDをStringで受け取り、液体は追加のデータコンポーネントがないものとして扱います。FEモードに入力はありません。"
                        + "不正なID・空のItemは0で、Mekanismが導入されていない場合、化学物質モードは常に0です。");
        addBookPage(root, "selector_idea_storage_type_count",
                "Returns the number of types of one resource kind currently stored in your Ideaspace Storage, as a Number."
                        + " The mode picks Item (I), Fluid (F) or Chemical (C)."
                        + " Items and fluids with different data components count as separate types, as in the storage's type limit.",
                "自分のイデアストレージに現在格納されている、ある種類の資源の種類数をNumberとして取得します。"
                        + "モードでアイテム（I）・液体（F）・化学物質（C）を選びます。"
                        + "アイテムと液体は、ストレージの種類数上限と同じく、データコンポーネントが異なるものを別の種類として数えます。");
    }

    private void addIdeaResourceLogistics(JsonObject root) {
        root.addProperty("psitweaks.spellerror.idea_storage_transfer_failed", switch (locale) {
            case "ja_jp" -> "外部機器とのイデア転送に失敗しました。詳細はサーバーログを確認してください";
            default -> "Ideaspace transfer with the external device failed. Check the server log for details";
        });
        // Exact-transfer labels of the mode menu (IdeaStorageResourceModes); the plain kinds share the block resource labels.
        String[][] modes = {
                {"resource_item_exact", "Item / Exact", "アイテム / 完全転送"},
                {"resource_fluid_exact", "Fluid / Exact", "液体 / 完全転送"},
                {"resource_chemical_exact", "Chemical / Exact", "化学物質 / 完全転送"}};
        for (String[] mode : modes) {
            addLocalized(root, "psitweaks.mode." + mode[0], mode[1], mode[2]);
        }
        for (boolean deposit : new boolean[] {true, false}) {
            String id = deposit ? "trick_idea_storage_deposit" : "trick_idea_storage_withdraw";
            String en = deposit
                    ? "Deposits the resource chosen by the mode (items, fluids, chemicals or FE) from the specified face of the block at Position into your Ideaspace Storage."
                    : "Withdraws the resource chosen by the mode (items, fluids, chemicals or FE) from your Ideaspace Storage into the specified face of the block at Position.";
            String jp = deposit
                    ? "位置のブロックの指定面から、モードで選んだ資源（アイテム・液体・化学物質・FE）を自分のイデアストレージへ格納します。"
                    : "自分のイデアストレージから、モードで選んだ資源（アイテム・液体・化学物質・FE）を位置のブロックの指定面へ取り出します。";
            addSpellPiece(root, id, "Trick: Ideaspace Storage - " + (deposit ? "Deposit" : "Withdraw"), en,
                    "作動式: イデアストレージ-" + (deposit ? "格納" : "取出"), jp);
            addBookPage(root, id + ".0",
                    (deposit
                            ? "Deposits the resource chosen by the mode from the specified face of the block at Position into your Ideaspace Storage."
                            : "Withdraws the resource chosen by the mode from your Ideaspace Storage into the specified face of the block at Position.")
                            + " The modes are Item (I), Fluid (F), Chemical (C) and FE, and the first three also have Exact Transfer (I=, F=, C=)."
                            + " Partial Transfer moves as much as it can: items up to the count given by Number, fluids and chemicals up to Power * 1000 mB,"
                            + " and FE up to Power * 4000 FE. The amount must be positive; fractions are rounded down.",
                    (deposit
                            ? "位置のブロックの指定面から、モードで選んだ資源を自分のイデアストレージへ格納します。"
                            : "自分のイデアストレージから、モードで選んだ資源を位置のブロックの指定面へ取り出します。")
                            + "モードはアイテム（I）・液体（F）・化学物質（C）・FEで、FE以外は完全転送（I=・F=・C=）も選べます。"
                            + "部分転送では移せる分だけ移します。上限はアイテムが「数値」の個数、液体・化学物質が威力×1000mB、FEが威力×4000FEです。"
                            + "量には正の数を指定し、端数は切り捨てます。");
            if (!deposit) {
                // Filters, block transfers and Exact Transfer are shared with Deposit, so Withdraw links there instead of repeating them.
                addBookPage(root, id + ".1",
                        "Filters, block transfers and Exact Transfer work as in $(l:psitweaks_spell_pieces/trick_idea_storage_deposit)$(o)Trick: Ideaspace Storage - Deposit$(0)$(/l)."
                                + " When several types match, they are withdrawn in turn until the limit is reached.",
                        "フィルタ、ブロックとの転送、完全転送は $(l:psitweaks_spell_pieces/trick_idea_storage_deposit)$(o)作動式: イデアストレージ-格納$(0)$(/l) と同じです。"
                                + "複数の種類が一致する場合は、上限に達するまで順に取り出します。");
                continue;
            }
            addBookPage(root, id + ".1",
                    "The filter is optional; with no filter or an empty String, everything of the chosen kind is a target."
                            + " In Fluid and Chemical modes the filter is a String matched against resource IDs, supporting * (any text) and ? (any single character)."
                            + " Omit the namespace, such as \"minecraft:\", to match the rest of the ID across all mods (e.g. *water for water, heavy water and more)."
                            + " FE mode has no filter.",
                    "フィルタは省略でき、接続しないか空文字にすると、選んだ種類のすべてが対象です。"
                            + "液体・化学物質モードのフィルタはStringで、資源IDで照合し、*（任意の文字列）と?（任意の1文字）が使えます。"
                            + "「minecraft:」のような先頭部分を省くと、どのModでも後半部分だけで照合します（例: *water で水や重水など）。"
                            + "FEモードにフィルタはありません。");
            addBookPage(root, id + ".2",
                    "In Item mode, what you connect to the filter decides how it matches: a String matches item IDs as above (e.g. *_ore for every mod's ores),"
                            + " an Item matches that item type, an Item List matches any of its items, and the output of Operator: Strict Item"
                            + " matches only items with the same data components, such as enchantments and durability.",
                    "アイテムモードでは、フィルタにつないだ値で照合方法が決まります。Stringは上と同じくアイテムIDで照合し（例: *_ore で各Modの鉱石）、"
                            + "Itemはそのアイテムの種類に、Item Listはそのいずれかに一致します。演算子: 厳密一致アイテムの出力をつなぐと、"
                            + "エンチャントや耐久値などのデータコンポーネントまで一致するものだけが対象です。");
            addBookPage(root, id + ".3",
                    "Follows the block's per-face input and output. For items, fluids and chemicals, casting fails with an error if the block is protected,"
                            + " and nothing happens on blocks in unloaded chunks or on your own Ideaspace Connector."
                            + " FE mode does nothing where you may not interact. Chemical mode requires Mekanism and does nothing without it.",
                    "ブロックの面ごとの入出力に従います。アイテム・液体・化学物質では、保護されたブロックを対象にするとエラーになり、"
                            + "読み込まれていないチャンクのブロックや、自分のイデアコネクターには作用しません。"
                            + "FEモードは、操作できない場所では何もしません。化学物質モードにはMekanismが必要で、導入されていない場合は何もしません。");
            addBookPage(root, id + ".4",
                    "Exact Transfer (I=, F=, C=; the badge turns white): moves exactly the full amount of one resource type, or nothing at all."
                            + " Both sides are simulated first, including the storage's per-type and type limits."
                            + " Different items, the same item with different data components, and different fluids or chemicals are never combined;"
                            + " the first type that fits is moved. When depositing a fluid, the block must release the amount in a single drain,"
                            + " while the same chemical in several tanks of the block adds up. A shortfall is not an error.",
                    "完全転送（I=・F=・C=。バッジが白地になります）: 1種類の資源を全量ちょうど移すか、まったく移しません。"
                            + "移動元・移動先と、ストレージの1種類あたりの上限・種類数上限を先に試算します。"
                            + "別のアイテムやデータコンポーネント違い、別の液体・化学物質を合算することはなく、条件を満たす最初の種類を移します。"
                            + "液体の格納では1回の排出で全量を出せる必要があり、化学物質はブロックの複数のタンクにある同じものを合算します。"
                            + "量が足りなくてもエラーにはなりません。");
        }
    }

    private void addIdeaSlotLogistics(JsonObject root) {
        root.addProperty("psitweaks.spellerror.idea_storage_slot_transfer_failed", switch (locale) {
            case "ja_jp" -> "指定スロットとのイデア転送に失敗しました。詳細はサーバーログを確認してください";
            default -> "Ideaspace transfer with the target slot failed. Check the server log for details";
        });
        String enInputs = " The filter is optional. Number must be positive; fractions of an item are rounded down.";
        String jpInputs = "フィルタは省略できます。数値には正の数を指定し、1個未満の端数は切り捨てます。";
        String enSlot = "The target slot is the same one used by Psi pieces such as Trick: Place Block. By default it is the hotbar slot to the right of your CAD,"
                + " and it can be changed with Trick: Switch Focus Slot (Hotbar) or Trick: Slot Change (Inventory)."
                + " Has no effect on the slot holding the CAD in use or the tool casting the spell. Casting without a CAD fails with an error.";
        String jpSlot = "指定スロットは、Psiの作動式: ブロック設置(単)などが使うスロットと同じです。既定ではCADの右隣のホットバーで、"
                + "作動式: 指定スロット切替やTrick: Slot Change (Inventory)で変更できます。"
                + "使用中のCADや、術式を詠唱している道具があるスロットには作用しません。CADを持っていないとエラーになります。";

        String depositEn = "Deposits the item in the spell's target slot into your Ideaspace Storage, up to the count given by Number.";
        String depositJp = "術式の指定スロットにあるアイテムを、自分のイデアストレージへ格納します。上限は「数値」で指定した個数です。";
        addSpellPiece(root, "trick_idea_storage_deposit_slot", "Trick: Ideaspace Storage - Slot Deposit", depositEn,
                "作動式: イデアストレージ-スロット格納", depositJp);
        addBookPage(root, "trick_idea_storage_deposit_slot.0",
                depositEn + " If storage space runs short, moves as much as possible." + enInputs,
                depositJp + "ストレージの空きが足りない場合は、移せる分だけ移します。" + jpInputs);
        addBookPage(root, "trick_idea_storage_deposit_slot.1",
                "With no filter or an empty String, the item in the slot is deposited whatever it is; otherwise nothing happens unless it matches."
                        + " The filter matches as in Trick: Ideaspace Storage - Deposit: a String matches item IDs with * (any text) and ? (any single character),"
                        + " an Item matches that item type, an Item List any of its items, and the output of Operator: Strict Item also compares data components.",
                "フィルタを接続しないか空文字にすると、スロットのアイテムを種類を問わず格納します。"
                        + "フィルタに一致しない場合は何もしません。"
                        + "照合方法は作動式: イデアストレージ-格納と同じで、Stringは*（任意の文字列）と?（任意の1文字）を使ってアイテムIDで照合し、"
                        + "Itemはその種類に、Item Listはそのいずれかに一致し、演算子: 厳密一致アイテムの出力はデータコンポーネントまで比べます。");
        addBookPage(root, "trick_idea_storage_deposit_slot.2", enSlot, jpSlot);

        String withdrawEn = "Withdraws items from your Ideaspace Storage into the spell's target slot, up to the count given by Number.";
        String withdrawJp = "自分のイデアストレージから、術式の指定スロットへアイテムを取り出します。上限は「数値」で指定した個数です。";
        addSpellPiece(root, "trick_idea_storage_withdraw_slot", "Trick: Ideaspace Storage - Slot Withdraw", withdrawEn,
                "作動式: イデアストレージ-スロット取出", withdrawJp);
        addBookPage(root, "trick_idea_storage_withdraw_slot.0",
                withdrawEn + " The slot is filled up to the item's max stack size. If stock runs short, moves as much as possible." + enInputs,
                withdrawJp + "スロットには最大スタック数まで入ります。在庫が足りない場合は、移せる分だけ移します。" + jpInputs);
        addBookPage(root, "trick_idea_storage_withdraw_slot.1",
                "If the slot holds an item, only the same item with identical data components is added; with no filter or an empty String, the slot is simply topped up."
                        + " If the slot is empty, the first stored type matching the filter is withdrawn. An empty slot with no filter does nothing."
                        + " The filter matches as in Trick: Ideaspace Storage - Slot Deposit.",
                "スロットにアイテムがある場合は、データコンポーネントまで同じアイテムだけを補充します。フィルタを接続しないか空文字にすると、そのまま補充します。"
                        + "スロットが空の場合は、フィルタに一致する最初の種類を取り出します。スロットが空でフィルタもない場合は何もしません。"
                        + "フィルタの照合方法は、作動式: イデアストレージ-スロット格納と同じです。");
        addBookPage(root, "trick_idea_storage_withdraw_slot.2", enSlot, jpSlot);
    }

    /** Pieces that put the Item, Block and Matrix types to work: block resources, item collecting and region matrices. */
    private void addItemBlockMatrixPieces(JsonObject root) {
        // Mode labels of Operator: Caster Basis Matrix and the block resource operators.
        String[][] modes = {
                {"caster_basis_2d", "2D (Horizontal)", "2D（水平）"},
                {"caster_basis_3d", "3D (With Pitch)", "3D（上下を含む）"},
                {"caster_basis_face", "Face", "面基準"},
                {"resource_item", "Item", "アイテム"},
                {"resource_fluid", "Fluid", "液体"},
                {"resource_chemical", "Chemical", "化学物質"},
                {"resource_energy", "FE", "FE"}};
        for (String[] mode : modes) {
            addLocalized(root, "psitweaks.mode." + mode[0], mode[1], mode[2]);
        }
        addLocalized(root, "psitweaks.datatype.item_or_item_list", "Item/Item List", "Item/Item List");
        addLocalized(root, "psitweaks.spellerror.block_resource_read_failed",
                "Reading the block's resources failed. Check the server log for details",
                "ブロックの資源の読み取りに失敗しました。詳細はサーバーログを確認してください");

        addSpellPiece(root, "operator_strict_item", "Operator: Strict Item",
                "Turns an Item or Item List into a filter that also compares data components.",
                "演算子: 厳密一致アイテム", "Item または Item List を、データコンポーネントまで比べるフィルタに変えます。");
        addBookPage(root, "operator_strict_item",
                "Turns an Item, or every item of an Item List, into a strict Item. Connect it to an item filter, such as the filter of"
                        + " Trick: Ideaspace Storage - Deposit/Withdraw, the Slot tricks, or the Item modes of"
                        + " Trick: Pull Item and Trick: Send Item, to match only items with the same data components, such as enchantments, durability and names."
                        + " The count is never compared. An empty Item is an error; empty elements of a list are skipped.",
                "Item、または Item List の各アイテムを、厳密一致の Item に変えます。作動式: イデアストレージ-格納・取出のフィルタ、スロット系、"
                        + "作動式: アイテム引き出し・アイテム転送のItemモードなどのフィルタにつなぐと、"
                        + "エンチャント・耐久値・名前などのデータコンポーネントまで一致するものだけが対象になります。"
                        + "個数は比べません。空のItemはエラーで、リストの空の要素は無視します。");
        addSpellPiece(root, "operator_block_item", "Operator: Block Item",
                "Gets the item that Pick Block gives for the block, as an Item.",
                "演算子: ブロックのアイテム化", "ブロックをブロック選択した時のアイテムを、Item として取得します。");
        addBookPage(root, "operator_block_item",
                "Outputs the item that Pick Block gives for the block, as an Item: wheat seeds for a wheat crop, and an empty Item for air."
                        + " It works as an Item filter, for example in Trick: Ideaspace Storage - Slot Withdraw.",
                "ブロックをブロック選択した時のアイテムを Item として出力します。"
                        + "小麦の作物なら小麦の種、空気なら空の Item です。"
                        + "Item フィルタとして作動式: イデアストレージ-スロット取出などに使えます。");

        addSpellPiece(root, "operator_block_resource_amount", "Operator: Block Resource Amount",
                "Outputs the items, fluids, chemicals or FE stored in a block, chosen by the mode. String narrows it down by ID.",
                "演算子: ブロック資源量", "ブロック内のアイテム・液体・化学物質・FEの量を、モードに応じて出力します。「文字列」でIDを絞り込めます。");
        addBookPage(root, "operator_block_resource_amount.0",
                "Outputs how much a block holds of the kind chosen in the mode menu: items (count), fluids or chemicals (mB), or FE."
                        + " Direction picks the face to read, following the block's per-face input and output; unconnected, the whole block is read.",
                "モードで選んだ種類について、ブロックが持つ量を出力します（アイテムは個数、液体と化学物質はmB、FEはFE）。"
                        + "「方向」で読み取る面を選べ、面ごとの入出力に従います。"
                        + "接続しない場合はブロック全体を読みます。");
        addBookPage(root, "operator_block_resource_amount.1",
                "The optional String filters by ID with * (any text) and ? (any single character); omit the namespace, such as \"minecraft:\", to match across all mods."
                        + " Unconnected or empty, everything is counted."
                        + " Blocks with no such storage, in unloaded chunks or in other dimensions read as 0, as do chemicals without Mekanism."
                        + " A protected block fails with an error.",
                "任意の「文字列」は資源IDで絞り込み、*（任意の文字列）と?（任意の1文字）が使えます。"
                        + "「minecraft:」のような先頭部分を省くと、どのModでも後半部分だけで照合します。"
                        + "接続しないか空文字にすると、すべてを数えます。"
                        + "該当する保管先がないブロック、読み込まれていないチャンクや別のディメンションのブロック、Mekanismがない場合の化学物質は0です。"
                        + "保護されたブロックはエラーになります。");

        addSpellPiece(root, "operator_block_free_capacity", "Operator: Block Free Capacity",
                "Outputs how much more of one item, fluid or chemical ID, or FE, a block can accept now.",
                "演算子: ブロック空き容量", "指定IDのアイテム・液体・化学物質、またはFEを、ブロックがあとどれだけ受け入れられるかを出力します。");
        addBookPage(root, "operator_block_free_capacity.0",
                "Outputs how much more a block accepts right now, by simulating an insertion; nothing moves."
                        + " Everything but String works as in Operator: Block Resource Amount."
                        + " String takes one exact ID such as minecraft:coal: no wildcards, \"minecraft:\" may be omitted, and unknown IDs read as 0.",
                "ブロックが今あとどれだけ受け入れられるかを、搬入を試算して出力します（実際には移しません）。"
                        + "「文字列」以外は演算子: ブロック資源量と同じです。"
                        + "「文字列」には minecraft:coal のようなIDを1つ指定します。"
                        + "ワイルドカードは使えず、「minecraft:」は省略でき、存在しないIDは0です。");
        addBookPage(root, "operator_block_free_capacity.1",
                "Items are simulated slot by slot, so machines whose slots share one pool may report more than really fits."
                        + " A block that accepts without limit reports a very large number."
                        + " Use it to keep machines stocked, for example by withdrawing fuel from your Ideaspace Storage only while a furnace has room for it.",
                "アイテムは1スロットずつ試算するため、スロットが容量を共有する機械では実際より多くなることがあります。"
                        + "上限なく受け入れるブロックは非常に大きな数になります。"
                        + "例えば、かまどに空きがある時だけイデアストレージから燃料を取り出すといった、機械への補充に使えます。");

        addSpellPiece(root, "operator_dropped_items", "Operator: Dropped Items",
                "Turns the dropped items in an Entity List into an Item List. Other entities are skipped.",
                "演算子: ドロップアイテム", "Entity List のドロップアイテムを Item List に変換します。他のエンティティは除外します。");
        addBookPage(root, "operator_dropped_items",
                "Turns the dropped items in an Entity List, such as from Selector: Nearby Items, into an Item List, keeping their order and skipping other entities.",
                "取得子: 近くのアイテムなどの Entity List から、ドロップアイテムを同じ順序で Item List に変換します。"
                        + "他のエンティティは除外します。");

        addSpellPiece(root, "operator_matrix_caster_basis", "Operator: Caster Basis Matrix",
                "Builds a 4x4 matrix from the caster's axis-rounded facing (columns: right, up, forward, origin), so regions can follow the caster.",
                "演算子: 術者基準行列", "術者の向きを軸に丸めた 4×4 行列（列: 右・上・前・原点）を作ります。領域を術者の向きに合わせるのに使います。");
        addBookPage(root, "operator_matrix_caster_basis.0",
                "Builds a 4x4 matrix whose columns are the caster's right, up and forward directions, each rounded to a block axis, and an origin: Position, or the caster's block if unconnected."
                        + " It maps a local point (X = right, Y = up, Z = forward) to the world, so regions built around (0, 0, 0) can follow the caster.",
                "術者の右・上・前の方向をブロックの軸に丸めて列に並べ、4列目を原点（「位置」、未接続なら術者のいるブロック）とした 4×4 行列です。"
                        + "局所座標（X=右、Y=上、Z=前）の点をワールド座標へ写すので、(0, 0, 0) を基準に作った領域を術者に追従させられます。");
        addBookPage(root, "operator_matrix_caster_basis.1",
                "Each mode uses the same frame as a macro: 2D (default) as Macro: Caster Axial Rotation, so forward is always level; 3D as Macro: Caster Axial Rotation (3D), which follows the pitch too; Face as Macro: Face Axial Rotation, using the face the caster looks at."
                        + " In Face mode the default origin is that block, and a miss is an error.",
                "各モードはマクロと同じ基準を使います。"
                        + "2D（既定）はマクロ: 術者方角回転と同じで、前は常に水平です。"
                        + "3D はマクロ: 術者方角回転 (3D) と同じで、視線の上下にも追従します。"
                        + "面基準はマクロ: 面基準回転と同じく、術者が見ているブロック面を使い、原点の既定値はそのブロックです。"
                        + "命中しない場合はエラーになります。");
        addBookPage(root, "operator_matrix_caster_basis.2",
                "Example: build Operator: Matrix Cuboid Region with Position (0, 1, 3) and Size (3, 3, 5), and multiply it by this matrix from the left with Operator: Matrix Multiply."
                        + " The result is a 3x3 tunnel five blocks deep ahead of the caster: Trick: Show Region previews it, and Operator: Region Vector List and Trick: Mass Block Break dig it.",
                "例: 演算子: 直方体領域を「位置」(0, 1, 3)、「サイズ」(3, 3, 5) で作り、演算子: 行列積でこの行列を左から掛けます。"
                        + "結果は術者の正面にある 3×3、奥行き5ブロックのトンネル状の領域です。"
                        + "作動式: 領域表示で確認でき、演算子: 領域ベクトルリスト化と作動式: 大規模ブロック破壊で掘れます。");

        addSpellPiece(root, "operator_matrix_rotation", "Operator: Rotation Matrix",
                "Builds a 4x4 matrix that rotates around Axis by Angle in radians, like Operator: Rotate Vector.",
                "演算子: 回転行列", "Axis（軸）の周りに Angle（ラジアン）だけ回す 4×4 行列を作ります。Operator: Rotate Vector と同じ回転です。");
        addBookPage(root, "operator_matrix_rotation.0",
                "Builds a 4x4 matrix that rotates around Axis, through the origin, by Angle in radians, the same rotation as Operator: Rotate Vector."
                        + " Quarter turns come out exact, so rotated regions keep their block positions."
                        + " A zero axis is an error.",
                "原点を通る Axis（軸）の周りに Angle（ラジアン）だけ回す 4×4 行列で、Operator: Rotate Vector と同じ回転です。"
                        + "90度単位の回転は誤差なく計算されるので、回転した領域でもブロック座標がずれません。"
                        + "軸がゼロベクトルだとエラーです。");
        addBookPage(root, "operator_matrix_rotation.1",
                "To rotate around a point P instead, chain two Operator: Matrix Multiply pieces to get (translation by P) × (this matrix) × (translation by -P), making the translations with Operator: Translation Matrix.",
                "点Pの周りで回すには、演算子: 平行移動行列と2つの演算子: 行列積で (Pの平行移動) × (この行列) × (-Pの平行移動) を作ります。");

        addSpellPiece(root, "operator_matrix_translation", "Operator: Translation Matrix",
                "Builds a 4x4 matrix that moves points by Offset.",
                "演算子: 平行移動行列", "点を「オフセット」だけ移動させる 4×4 行列を作ります。");
        addBookPage(root, "operator_matrix_translation",
                "Builds a 4x4 matrix that moves points by Offset."
                        + " Put it to the left of a region matrix in Operator: Matrix Multiply to move the region, or apply it with Operator: Matrix Transform Vector to move a position."
                        + " With Operator: Rotation Matrix, it rotates a region around any point.",
                "点を「オフセット」だけ移動させる 4×4 行列を作ります。"
                        + "演算子: 行列積で領域行列の左に置くと領域を移動でき、演算子: 行列でベクトル変換で使うと座標を移動できます。"
                        + "演算子: 回転行列と組み合わせると、任意の点の周りで領域を回転できます。");

        addSpellPiece(root, "trick_show_region", "Trick: Show Region",
                "Draws the 12 edges of a region matrix with particles in your CAD's color.",
                "作動式: 領域表示", "領域行列の12本の辺を、CADの色のパーティクルで描きます。");
        addBookPage(root, "trick_show_region.0",
                "Draws the outline of a region defined by a 3x4 or 4x4 matrix with particle trails in your CAD's color, to check it before working on it."
                        + " Block mode (default) wraps the blocks that Operator: Region Vector List picks from the region; Vector mode draws the parallelepiped exactly as defined.",
                "3×4 または 4×4 の行列で定義された領域の輪郭を、CADの色のパーティクルの軌跡で描きます。"
                        + "作業前の範囲確認に使います。"
                        + "Block モード（既定）は演算子: 領域ベクトルリスト化が選ぶブロックを囲むように、Vector モードは定義どおりの平行六面体を描きます。");
        addBookPage(root, "trick_show_region.1",
                "Time is an optional constant and works like the Time of Trick: Particle Trail: 20 by default, at most 1200, and it must be positive."
                        + " Every corner must be within the spell radius, or casting fails with an error."
                        + " Players near you see the particles too.",
                "「時間」は任意入力の定数で、Trick: Particle Trail の時間と同じく既定値20、最大1200で、正の数を指定します。"
                        + "すべての角が術式射程内にない場合はエラーになります。"
                        + "パーティクルは近くのプレイヤーにも見えます。");
    }

    private void addFilterCardTranslations(JsonObject root) {
        boolean japanese = "ja_jp".equals(locale);
        root.addProperty("tooltip.psitweaks.filter_card.slot", "%s: %s");
        root.addProperty("tooltip.psitweaks.filter_card.empty",
                japanese ? "アイテムが登録されていません" : "No items registered");
        root.addProperty("tooltip.psitweaks.filter_card.any_slot", japanese ? "どこでも" : "Any");
        root.addProperty("tooltip.psitweaks.filter_card.idle",
                japanese ? "待機中: 先に見つかる別のフィルタカードが使われます" : "Idle: another Filter Card found first is in use");
        root.addProperty("psitweaks.spellerror.no_filter_card",
                japanese ? "エラー: フィルタカードを持っていません。" : "Error: You have no Filter Card.");
        root.addProperty("psitweaks.spellerror.filter_card_slot_empty",
                japanese ? "エラー: フィルタカードの %s 番の枠は空です。" : "Error: Slot %s of the Filter Card is empty.");
        root.addProperty("psitweaks.spellerror.filter_card_slot_range",
                japanese ? "エラー: フィルタカードの番号は 1〜9 の整数で指定してください。"
                        : "Error: The Filter Card slot must be an integer from 1 to 9.");
        addSpellPiece(root, "selector_filter_card", "Selector: Filter Card",
                "Gets the item registered in the given slot (1-9) of the Filter Card in use",
                "取得子: フィルタカード", "使用中のフィルタカードの指定した番号(1〜9)に登録したアイテムを取得する");
        addBookPage(root, "selector_filter_card",
                "Outputs the item registered in the given slot (1 to 9) of the caster's Filter Card in use, as an Item value with a count of 1. Connect it to Operator: Strict Item to also match data such as enchantments and names.$(p)If the caster has no Filter Card, or the slot is empty, the spell stops with an error.",
                "術者が使用中のフィルタカードの, 指定した番号(1〜9)に登録したアイテムを, 個数1の Item として出力します. エンチャントや名前などのデータまで一致させるには, 演算子: 厳密一致アイテム につなぎます.$(p)フィルタカードを持っていないときや, 指定した枠が空のときはエラーで止まります.");
        root.addProperty("psi.book.page.psitweaks_item.filter_card.0", japanese
                ? "術式のフィルタに使う見本アイテムを, 9枠まで登録できるカードです. 使うとGUIが開き, 枠をアイテムでクリックすると個数1の写しを登録します. 元のアイテムは消費しません. 空の手でクリックするかシフトクリックで枠を空にします.$(p)取得子: フィルタカード に番号(1〜9)を入力すると, その枠のアイテムを取得できます."
                : "A card that holds up to 9 sample items for spell filters. Using it opens a GUI; clicking a slot with an item registers a copy with a count of 1 without using up the item. Click with an empty hand or shift-click to clear a slot.$(p)Selector: Filter Card reads the item in the slot whose number (1 to 9) you give it.");
        root.addProperty("psi.book.page.psitweaks_item.filter_card.1", japanese
                ? "使われるカードは1枚だけです. Curiosのスロット(どのスロットにも装備できます)を先に, 次にメインハンド, オフハンド, ホットバー, インベントリの順で探し, 最初に見つかったカードを使います.$(p)2枚以上持っているとき, 使われていないカードは見た目が変わります."
                : "Only one card is used at a time. The Curios slots (it fits any slot) are searched first, then the main hand, the offhand, the hotbar and the inventory, and the first card found is used.$(p)When you carry two or more cards, the unused ones look different.");
    }
}
