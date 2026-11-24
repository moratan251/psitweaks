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
        JsonObject root = translations();
        if (!com.moratan251.psitweaks.common.compat.MekanismCompat.RADIATION_INJECTION_ENABLED) {
            // 作動式: 放射線注入を使えなくしているあいだは、ピース・解説書・設定・プログラムの文を出さない。
            root.keySet().removeIf(key -> key.contains("radiation_injection"));
        }
        return DataProvider.saveStable(output, root, pathProvider.json(Psitweaks.location(locale)));
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
        addBladePouchTranslations(root);
        addAdvancementTranslations(root);
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
        root.addProperty("block.psitweaks.magicians_head", switch (locale) {
            case "ja_jp" -> "魔法師の頭";
            default -> "Magician's Head";
        });
        root.addProperty("block.psitweaks.magicians_wall_head", switch (locale) {
            case "ja_jp" -> "壁に付けられた魔法師の頭";
            default -> "Magician's Wall Head";
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
        root.addProperty("entity.psitweaks.air_bullet", switch (locale) {
            case "ja_jp" -> "空気弾";
            default -> "Air Bullet";
        });
        root.addProperty("entity.psitweaks.psi_bullet", switch (locale) {
            case "ja_jp" -> "想子弾";
            default -> "Psi Bullet";
        });
        root.addProperty("entity.psitweaks.dancing_blade", switch (locale) {
            case "ja_jp" -> "舞う刃";
            default -> "Dancing Blade";
        });
        root.addProperty("entity.psitweaks.parade_decoy", switch (locale) {
            case "ja_jp" -> "仮装行列の分身";
            default -> "Parade Lookalike";
        });
        root.addProperty("entity.psitweaks.pseudo_meteor", switch (locale) {
            case "ja_jp" -> "疑似隕石";
            default -> "Pseudo Meteor";
        });
        root.addProperty("entity.psitweaks.soak_zone", switch (locale) {
            case "ja_jp" -> "湿潤領域";
            default -> "Soak Zone";
        });
        root.addProperty("entity.psitweaks.gravity_zone", switch (locale) {
            case "ja_jp" -> "加重領域";
            default -> "Gravity Zone";
        });
        root.addProperty("entity.psitweaks.heat_zone", switch (locale) {
            case "ja_jp" -> "加熱領域";
            default -> "Heat Zone";
        });
        root.addProperty("entity.psitweaks.freeze_zone", switch (locale) {
            case "ja_jp" -> "凍結領域";
            default -> "Freeze Zone";
        });
        root.addProperty("entity.psitweaks.turbulence_zone", switch (locale) {
            case "ja_jp" -> "乱流領域";
            default -> "Turbulence Zone";
        });
        root.addProperty("entity.psitweaks.interference_zone", switch (locale) {
            case "ja_jp" -> "領域干渉";
            default -> "Interference Zone";
        });
        root.addProperty("entity.psitweaks.flare_circle", switch (locale) {
            case "ja_jp" -> "ファイアサークル";
            default -> "Fire Circle";
        });
        root.addProperty("entity.psitweaks.ice_circle", switch (locale) {
            case "ja_jp" -> "アイスサークル";
            default -> "Ice Circle";
        });
        root.addProperty("entity.psitweaks.charged_zone", switch (locale) {
            case "ja_jp" -> "荷電領域";
            default -> "Charged Zone";
        });
        root.addProperty("entity.psitweaks.silence_zone", switch (locale) {
            case "ja_jp" -> "遮音領域";
            default -> "Silence Zone";
        });
        root.addProperty("entity.psitweaks.acceleration_zone", switch (locale) {
            case "ja_jp" -> "加速領域";
            default -> "Acceleration Zone";
        });
        root.addProperty("entity.psitweaks.deceleration_zone", switch (locale) {
            case "ja_jp" -> "減速領域";
            default -> "Deceleration Zone";
        });
        root.addProperty("entity.psitweaks.decontamination", switch (locale) {
            case "ja_jp" -> "除染";
            default -> "Decontamination";
        });
        root.addProperty("entity.psitweaks.contamination_zone", switch (locale) {
            case "ja_jp" -> "汚染領域";
            default -> "Contamination Zone";
        });
        root.addProperty("entity.psitweaks.meteor_line_night", switch (locale) {
            case "ja_jp" -> "流星群";
            default -> "Meteor Line";
        });
        root.addProperty("entity.psitweaks.micro_black_hole", switch (locale) {
            case "ja_jp" -> "マイクロブラックホール";
            default -> "Micro Black Hole";
        });
        root.addProperty("entity.psitweaks.chain_lightning", switch (locale) {
            case "ja_jp" -> "連鎖電光";
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
        addAttribute(root, "spell_damage_factor", "Phenomenon Interference", "事象干渉力");
        addModule(root, "psyon_supplying_unit", "Psyon Supplying Unit", "Increases Psi regeneration.", "サイオン供給ユニット", "ユーザーのPsi回復速度を増加させます.");
        addModule(root, "psyon_capacity_unit", "Psyon Capacity Unit", "Increases maximum Psi.", "サイオン容量ユニット", "ユーザーの最大Psi量を増加させます.");
        addModule(root, "phenomenon_interference_enhancement_unit", "Phenomenon Interference Enhancement Unit", "Increases the user's Phenomenon Interference.", "事象干渉力増大ユニット", "ユーザーの事象干渉力を高めます.");
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
        root.addProperty("psitweaks.spellparam.indiscriminate", switch (locale) {
            case "ja_jp" -> "無差別";
            default -> "Indiscriminate";
        });
        root.addProperty("psitweaks.spellparam.nbt_path", switch (locale) {
            case "ja_jp" -> "NBTパス";
            default -> "NBT Path";
        });
        root.addProperty("psitweaks.spellparam.fallback", switch (locale) {
            case "ja_jp" -> "既定値";
            default -> "Fallback";
        });
        root.addProperty("psitweaks.spellerror.parade_disguised", switch (locale) {
            case "ja_jp" -> "エラー: 対象の情報は偽装されています.";
            default -> "ERROR: The target's information is disguised.";
        });
        root.addProperty("psitweaks.spellerror.no_ground", switch (locale) {
            case "ja_jp" -> "指定位置の下に地面がありません";
            default -> "No ground below the position";
        });
        root.addProperty("psitweaks.spellerror.nbt_too_many_targets", switch (locale) {
            case "ja_jp" -> "1回の詠唱でNBTを読める対象の数を超えました";
            default -> "Too many targets had their NBT read in one cast";
        });
        root.addProperty("psitweaks.spellerror.wildcard_too_many_comparisons", switch (locale) {
            case "ja_jp" -> "1回の詠唱でパターンと文字を比べられる回数を超えました";
            default -> "Too many characters were compared with patterns in one cast";
        });
        root.addProperty("psitweaks.spellerror.value_list_size_mismatch", switch (locale) {
            case "ja_jp" -> "リストと基準値の要素数が違います";
            default -> "The List and the Values have different sizes";
        });
        root.addProperty("psitweaks.spellparam.values", switch (locale) {
            case "ja_jp" -> "基準値";
            default -> "Values";
        });
        root.addProperty("psitweaks.hud.spellgram_integrity", switch (locale) {
            case "ja_jp" -> "%s  %s";
            default -> "%s  %s";
        });
        // 領域のリソース: 使っている量 / 総量。2 つめの %s は「337 」「1.00 M」のように単位の接頭辞まで含む。
        root.addProperty("psitweaks.hud.zone_resource.heat", switch (locale) {
            case "ja_jp" -> "熱量: %s / %sJ/t";
            default -> "Heat: %s / %sJ/t";
        });
        root.addProperty("psitweaks.hud.zone_resource.cooling", switch (locale) {
            case "ja_jp" -> "冷却量: %s / %sJ/t";
            default -> "Cooling: %s / %sJ/t";
        });
        // 熱の受け渡し（文言は仮）: %s は「48.3 」「1.20 k」のように単位の接頭辞まで含む。
        root.addProperty("psitweaks.hud.zone_resource.carried", switch (locale) {
            case "ja_jp" -> "運搬: %sJ/t";
            default -> "Carried: %sJ/t";
        });
        root.addProperty("psitweaks.hud.zone_resource.received", switch (locale) {
            case "ja_jp" -> "受け取り: %sJ/t";
            default -> "Received: %sJ/t";
        });
        // 荷電領域が領域給電機から引いて配った FE。
        root.addProperty("psitweaks.hud.zone_resource.received_fe", switch (locale) {
            case "ja_jp" -> "受け取り: %sFE/t";
            default -> "Received: %sFE/t";
        });
        root.addProperty("psitweaks.hud.zone_resource.charge", switch (locale) {
            case "ja_jp" -> "充電能力: %s / %sFE/t";
            default -> "Charge: %s / %sFE/t";
        });
        root.addProperty("psitweaks.hud.zone_resource.wind", switch (locale) {
            case "ja_jp" -> "風量: %s / %s WU";
            default -> "Wind: %s / %s WU";
        });
        // 領域の強さ（リソースを持たない領域）。
        root.addProperty("psitweaks.hud.zone_acceleration", switch (locale) {
            case "ja_jp" -> "加速: ×%s";
            default -> "Acceleration: ×%s";
        });
        root.addProperty("psitweaks.hud.zone_deceleration", switch (locale) {
            case "ja_jp" -> "減速: 移動 −%s%%・飛び道具 ×%s/tick";
            default -> "Deceleration: movement −%s%%, projectiles ×%s/tick";
        });
        // 作動式: 除染: 使った量 / 除染能力（1 秒あたり）、半径の中の線源。
        root.addProperty("psitweaks.hud.decontamination.sources", switch (locale) {
            case "ja_jp" -> "除染能力: %s / %s Sv/h（毎秒）";
            default -> "Decontamination: %s / %s Sv/h a second";
        });
        root.addProperty("psitweaks.hud.decontamination.left", switch (locale) {
            case "ja_jp" -> "半径の中の線源: %s 個（計 %s Sv/h）";
            default -> "Sources in range: %s (%s Sv/h in all)";
        });
        root.addProperty("psitweaks.hud.zone_contamination", switch (locale) {
            case "ja_jp" -> "被ばく: 上限 %s mSv";
            default -> "Dose: up to %s mSv";
        });
        root.addProperty("psitweaks.spellerror.not_spellgram", switch (locale) {
            case "ja_jp" -> "エラー: 対象が常駐魔法式ではありません.";
            default -> "Error: The target is not a Resident SpellGram.";
        });
        root.addProperty("psitweaks.spellerror.not_own_spellgram", switch (locale) {
            case "ja_jp" -> "エラー: 自分の常駐魔法式ではありません.";
            default -> "Error: The target is not your own Resident SpellGram.";
        });
        root.addProperty("psitweaks.spellerror.not_autonomous_caster", switch (locale) {
            case "ja_jp" -> "エラー: 自律詠唱機からの詠唱ではありません.";
            default -> "Error: Not cast from an Autonomous Caster.";
        });
        root.addProperty("psitweaks.spellerror.no_jump_anchor", switch (locale) {
            case "ja_jp" -> "前方に一致するジャンプアンカーがありません";
            default -> "No matching Jump Anchor found ahead";
        });
        root.addProperty("psitweaks.spellerror.nullitem", switch (locale) {
            case "ja_jp" -> "エラー: 魔法の対象となるアイテムは存在しません.";
            default -> "Error: The target item of the spell does not exist.";
        });
        root.addProperty("psitweaks.spellerror.accessdenyed", switch (locale) {
            case "ja_jp" -> "エラー: 保護されたブロックにアクセスを試みました.";
            default -> "Error: Attempted to access a protected block.";
        });
        // 毎朝レビュー F-043: コードで使っているのに訳がなかったキー。
        root.addProperty("psitweaks.spellerror.not_spellgram_object", switch (locale) {
            case "ja_jp" -> "エラー: 対象が魔法式オブジェクトではありません.";
            default -> "Error: The target is not a SpellGram object.";
        });
        root.addProperty("psitweaks.spellerror.zone_empty", switch (locale) {
            case "ja_jp" -> "エラー: 領域の範囲にブロックがありません.";
            default -> "Error: The zone contains no blocks.";
        });
        root.addProperty("psitweaks.spellerror.failedToInteract", switch (locale) {
            case "ja_jp" -> "エラー: ブロックに作用できませんでした.";
            default -> "Error: Could not interact with the block.";
        });
        root.addProperty("psitweaks.event.custom_tick", switch (locale) {
            case "ja_jp" -> "§bイベント§7: カスタムtick";
            default -> "§bEvent§7: Custom Tick";
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
            case "ja_jp" -> "警告: CADメモリスロット%sのStringは%s文字から%s文字に切り捨てられました.";
            default -> "WARNING: String CAD memory value in slot %s was truncated from %s to %s characters.";
        });
        root.addProperty("psitweaks.spellwarning.position", switch (locale) {
            case "ja_jp" -> "警告 場所 [%s, %s].";
            default -> "Warning Position: [%s, %s].";
        });
        root.addProperty("psitweaks.spellwarning.target_fallen", switch (locale) {
            case "ja_jp" -> "警告: 対象は既に戦闘不能です.";
            default -> "WARNING: The target is already down.";
        });
        root.addProperty("psitweaks.spellwarning.radiation_disabled", switch (locale) {
            case "ja_jp" -> "警告: Mekanism の設定で放射線が無効なので, 領域を置きませんでした.";
            default -> "WARNING: Radiation is turned off in Mekanism's config, so the zone was not placed.";
        });
        root.addProperty("psitweaks.spellwarning.no_breeding_food", switch (locale) {
            case "ja_jp" -> "警告: %sを繁殖させる餌を持っていません.";
            default -> "WARNING: You have no food to breed %s.";
        });
        addPsitweaksCategoryBook(root);
        addProgrammingConceptsBook(root);
        addMachineTranslations(root);
        AutonomousCasterTranslations.add(root, locale);
        addSpellUnlockMessages(root);
        addCadDisassembler(root);
        addZoneFeeder(root);
        addZoneReceiver(root);
        addUtilityItemsBook(root);
        addMovalSuitBook(root);
        addMekanismIntegrationBook(root);
        addMaterialBookAdditions(root);
        addSpellBulletsBook(root);
        addEffect(root, "parade", "Parade", "仮装行列");
        addEffect(root, "flight", "Flight", "飛行");
        addEffect(root, "barrier", "Barrier", "障壁");
        addEffect(root, "hardening", "Hardening", "硬化");
        addEffect(root, "radiation_filter", "Radiation Filter", "放射線フィルタ");
        addEffect(root, "carbon_poisoning", "Carbon Poisoning", "炭素中毒");
        addEffect(root, "torpor", "Torpor", "鈍化");

        addSpellPieces(root);
        addSpellPiecesBook(root);
        addZoneMagicBook(root);
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
                "Shake the camera for ground shockwaves, pseudo meteor impacts and micro black hole collapses.",
                "地雷原の衝撃波, 疑似隕石の着弾, マイクロブラックホールの崩壊などで画面を揺らします.");
        addConfigValue(root, "psitweaks.configuration.effects.camera_shake_strength",
                "Camera Shake Strength", "画面の揺れの強さ",
                "Strength of the camera shake. 1.0 is the default.",
                "画面の揺れの強さです. 1.0が標準です.");
        addConfigValue(root, "psitweaks.configuration.effects.barrier_hud",
                "Barrier Shield Bar", "障壁シールドのバー",
                "Show the remaining shield and time of Trick: Barrier above the health bar.",
                "作動式: 障壁のシールド残量と残り時間を体力ゲージの上に表示します.");
        addConfigValue(root, "psitweaks.configuration.effects.spellgram_integrity_hud",
                "SpellGram Integrity", "魔法式オブジェクトの耐久値",
                "Show the name and remaining integrity of the SpellGram object whose core you aim at within 5 blocks, below the crosshair.",
                "5ブロック以内で照準を合わせた魔法式オブジェクトの核の, 名前と残りの耐久値をクロスヘアの下に表示します.");
        addConfigValue(root, "psitweaks.configuration.effects.meteor_line_night",
                "Meteor Line Night", "流星群の夜",
                "Darken the view while you stand in the night of Trick: Meteor Line.",
                "作動式: 流星群の夜の中にいる間, 画面を暗くします.");
        addConfigValue(root, "psitweaks.configuration.effects.zone_circle_quality",
                "Magic Circle Quality", "魔法陣の画質",
                "Image quality of the magic circle on the floor of zone spellgrams."
                        + " Lower uses less video memory, and the lines look a little blurry up close."
                        + " Smaller images are still used when the circle looks small.",
                "領域の床に出る魔法陣の画質です. "
                        + "低くするとビデオメモリの使用量が減り, 近くで見たときの線が少しぼやけます. "
                        + "遠くで小さく見えるときは, どの画質でも小さい画像を使います.");
        addConfigLabel(root, "psitweaks.configuration.effects.zone_circle_quality.high", "High (1024 px)", "高（1024 px）");
        addConfigLabel(root, "psitweaks.configuration.effects.zone_circle_quality.medium", "Medium (512 px)", "中（512 px）");
        addConfigLabel(root, "psitweaks.configuration.effects.zone_circle_quality.low", "Low (256 px)", "低（256 px）");
        addConfigValue(root, "psitweaks.configuration.effects.zone_particle_limit",
                "Zone Particle Limit", "領域のパーティクルの上限",
                "Maximum number of particles that all zone spellgrams together spawn per tick,"
                        + " such as the storms of Heat Storm and Blizzard."
                        + " When more are wanted, every zone spawns fewer at the same rate."
                        + " Zones far from the camera always spawn fewer. 0 turns them off.",
                "すべての領域魔法を合わせて, 1 tickに出すパーティクルの数の上限です（熱乱流や吹雪の渦など）. "
                        + "超えるときは, すべての領域で同じ割合で減らします. "
                        + "カメラから遠い領域は, いつも少なめに出します. 0にすると出しません.");
        addConfigLabel(root, "psitweaks.configuration.windows", "Window Positions", "ウィンドウの位置");
        addConfigLabel(root, "psitweaks.configuration.windows.autonomous_caster_settings",
                "Autonomous Caster Settings Window", "自律詠唱機の設定ウィンドウ");
        addConfigValue(root, "psitweaks.configuration.windows.x",
                "X Position", "X 座標",
                "Last x position of the window, relative to the machine screen.",
                "最後に閉じたときのウィンドウの X 座標です. 機械の画面の左上からの位置です.");
        addConfigValue(root, "psitweaks.configuration.windows.y",
                "Y Position", "Y 座標",
                "Last y position of the window, relative to the machine screen.",
                "最後に閉じたときのウィンドウの Y 座標です. 機械の画面の左上からの位置です.");
        addConfigValue(root, "psitweaks.configuration.windows.pinned",
                "Pinned", "ピン留め",
                "Open the window together with the machine screen.",
                "機械の画面を開いたときに, このウィンドウも開きます.");

        addConfigLabel(root, "psitweaks.configuration.spells", "Spell Settings", "スペル設定");
        addConfigValue(root, "psitweaks.configuration.spells.global_spell_power_multiplier",
                "Global Offensive Spell Multiplier", "攻撃術式の全体倍率",
                "Multiplier applied to all offensive spells added by PsiTweaks.",
                "PsiTweaksが追加する全攻撃術式に適用する倍率です.");
        addConfigLabel(root, "psitweaks.configuration.spells.unlocks",
                "Spell Unlock Settings", "スペル解禁設定");
        addConfigValue(root, "psitweaks.configuration.spells.unlocks.require_spell_unlocks",
                "Require Spell Unlocks", "スペル解禁を要求",
                "Require configured spells to be unlocked before they can be used in the Spell Programmer.",
                "対象スペルをスペルプログラマーで使用する前に, 解禁を必要とするか設定します.");

        addDamageMultiplierConfig(root, "molecular_divider", "Molecular Divider", "分子ディバイダー");
        addDamageMultiplierConfig(root, "phonon_maser", "Phonon Maser", "フォノンメーザー");
        addDamageMultiplierConfig(root, "aqua_cutter", "Aqua Cutter", "アクアカッター");
        addDamageMultiplierConfig(root, "dry_meteor", "Dry Meteor", "ドライミーティア");
        addDamageMultiplierConfig(root, "blaze_ball", "Blaze Ball", "ブレイズボール");
        addDamageMultiplierConfig(root, "active_air_mine", "Active Air Mine", "能動空中機雷");
        addDamageMultiplierConfig(root, "flare_circle", "Fire Circle", "ファイアサークル");
        addDamageMultiplierConfig(root, "ice_circle", "Ice Circle", "アイスサークル");
        addDamageMultiplierConfig(root, "heat_storm", "Heat Storm", "熱乱流");
        addDamageMultiplierConfig(root, "blizzard", "Blizzard", "吹雪");
        addDamageMultiplierConfig(root, "rainstorm", "Rainstorm", "暴風雨");
        addDamageMultiplierConfig(root, "thundercloud", "Thundercloud", "雷雲");
        addDamageMultiplierConfig(root, "contaminated_stream", "Contaminated Stream", "汚染流");
        addConfigLabel(root, "psitweaks.configuration.spells.gravity_zone", "Gravity Zone", "加重領域");
        addConfigValue(root, "psitweaks.configuration.spells.gravity_zone.contraption_multiplier", "Contraption Multiplier",
                "物理で動く構造物への倍率",
                "Multiplier for how hard a Gravity Zone pushes physics contraptions (such as Create Aeronautics airships). 0 disables it.",
                "加重領域が物理で動く構造物（Create Aeronautics の機体など）を押す強さの倍率です. 0 で無効になります.");
        addConfigLabel(root, "psitweaks.configuration.spells.spellgram", "SpellGram Objects", "魔法式オブジェクト");
        addConfigValue(root, "psitweaks.configuration.spells.spellgram.integrity_multiplier", "Integrity Multiplier", "耐久値の倍率",
                "Multiplier applied to the integrity of SpellGram objects.", "魔法式オブジェクトの耐久値に適用する倍率です.");
        addConfigValue(root, "psitweaks.configuration.spells.spellgram.max_objects_per_player", "Objects per Player", "プレイヤー1人が置ける数",
                "How many Resident SpellGrams (circles and zones together) one player can keep at once. Placing more ends the oldest.",
                "プレイヤー1人が同時に置ける常駐魔法式（サークルと領域を合わせた数）です. 超えて置くと古いものから消えます.");
        addConfigValue(root, "psitweaks.configuration.spells.spellgram.max_objects_per_autonomous_caster", "Objects per Autonomous Caster", "自律詠唱機1台が置ける数",
                "How many Resident SpellGrams (circles and zones together) one Autonomous Caster can keep at once, counted apart from its owner.",
                "自律詠唱機1台が同時に置ける常駐魔法式（サークルと領域を合わせた数）です. 持ち主のプレイヤーの数とは別に数えます.");
        addConfigLabel(root, "psitweaks.configuration.spells.nbt_pieces", "NBT Pieces", "NBT系のピース");
        addConfigValue(root, "psitweaks.configuration.spells.nbt_pieces.max_targets_per_cast", "Targets per Cast", "1回の詠唱で読める対象の数",
                "How many targets one cast can read the NBT of. Targets without NBT and targets read again in the same cast do not count. Reading more stops the spell with an error.",
                "1回の詠唱でNBTを読める対象の数です. NBTを持たない対象と, 同じ詠唱で読み直した対象は数えません. 超えると術式はエラーで止まります.");
        addConfigLabel(root, "psitweaks.configuration.spells.pattern_matching", "Pattern Matching", "パターンの照合");
        addConfigValue(root, "psitweaks.configuration.spells.pattern_matching.max_comparisons_per_cast", "Comparisons per Cast", "1回の詠唱で比べられる回数",
                "How many character comparisons one cast can make when matching text against patterns (* ? [abc]). Operator: Partial Match and the List Keep/Remove Matches, Name Matches and NBT Matches pieces count them; # tag patterns do not. Going past it stops the spell with an error.",
                "パターン（* ? [abc]）との照合で, 1回の詠唱が文字を比べられる回数です. 演算子: 部分一致, リスト一致抽出・除外, 表示名一致抽出・除外, NBT一致抽出・除外が数えます（# のタグの照合は数えません）. 超えると術式はエラーで止まります.");
        addDamageMultiplierConfig(root, "guillotine", "Guillotine", "ギロチン");
        addDamageMultiplierConfig(root, "chain_lightning", "Chain Lightning", "連鎖電光");
        addDamageMultiplierConfig(root, "mine_genesis", "Mine Genesis", "地雷原");
        addDamageMultiplierConfig(root, "burst", "Burst", "爆裂");
        addDamageMultiplierConfig(root, "air_bullet", "Air Bullet", "エア・ブリット");
        addDamageMultiplierConfig(root, "psi_bullet", "Psi Bullet", "サイ・ブリット");
        addDamageMultiplierConfig(root, "gram_demolition", "Gram Demolition", "術式解体");
        addDamageMultiplierConfig(root, "dancing_blades", "Dancing Blades", "ダンシングブレイズ");
        addDamageMultiplierConfig(root, "pseudo_meteor", "Pseudo Meteor", "疑似隕石");
        addDamageMultiplierConfig(root, "micro_black_hole", "Micro Black Hole", "マイクロブラックホール");
        addConfigLabel(root, "psitweaks.configuration.spells.barrier", "Barrier", "障壁");
        addConfigValue(root, "psitweaks.configuration.spells.barrier.shield_multiplier",
                "Shield Multiplier", "シールド量倍率",
                "Multiplier applied to the shield of Trick: Barrier (10 per point of Power).",
                "作動式: 障壁のシールド量 (威力1につき10) に適用する倍率です.");

        addConfigLabel(root, "psitweaks.configuration.spells.radiation_injection",
                "Radiation Injection", "放射線注入");
        addConfigValue(root, "psitweaks.configuration.spells.radiation_injection.radiation_multiplier",
                "Radiation Multiplier", "放射線量倍率",
                "Multiplier applied to the radiation amount caused by Radiation Injection.",
                "放射線注入による被ばく量に適用する倍率です.");

        addConfigLabel(root, "psitweaks.configuration.items", "Item Settings", "アイテム設定");
        addConfigLabel(root, "psitweaks.configuration.items.gravstringer",
                "Gravstringer", "グラヴストリンガー");
        addConfigValue(root, "psitweaks.configuration.items.gravstringer.base_damage",
                "Base Damage", "基礎ダメージ",
                "Base damage assigned to arrows fired by the Gravstringer.",
                "グラヴストリンガーが発射する矢に設定する基礎ダメージです.");
        addConfigLabel(root, "psitweaks.configuration.items.tunneler",
                "Tunneler", "トンネラー");
        addConfigValue(root, "psitweaks.configuration.items.tunneler.minimum_damage",
                "Minimum Damage", "最低ダメージ",
                "Minimum damage dealt when a Tunneler hits an entity.",
                "トンネラーがエンティティへ命中した際に与える最低ダメージです.");

        addConfigLabel(root, "psitweaks.configuration.psi", "Psi Behavior", "Psi基本挙動");
        addConfigValue(root, "psitweaks.configuration.psi.disable_damage_psi_deduction",
                "Disable Psi Loss on Damage", "被ダメージ時のPsi減少を無効化",
                "Prevent taking damage from reducing Psi.", "ダメージを受けた際のPsi減少を無効にします.");
        addConfigValue(root, "psitweaks.configuration.psi.disable_regen_cooldown",
                "Disable Psi Regeneration Cooldown", "Psi回復クールダウンを無効化",
                "Prevent Psi consumption from applying a regeneration cooldown.",
                "Psiを消費した際の回復クールダウンを無効にします.");

        addConfigLabel(root, "psitweaks.configuration.mekanism", "Mekanism Integration", "Mekanism連携");
        addConfigValue(root, "psitweaks.configuration.mekanism.gas_burning_generator_energy_capacity",
                "Gas-Burning Generator Energy Capacity", "ガス燃焼発電機の内部エネルギー容量",
                "Override the internal energy capacity in Joules. Set to -1 to use Mekanism's default behavior.",
                "内部エネルギー容量をJ単位で上書きします. -1にするとMekanismの標準挙動を使用します.");

        addConfigLabel(root, "psitweaks.configuration.idea_storage", "Ideaspace Storage", "イデアストレージ");
        addConfigValue(root, "psitweaks.configuration.idea_storage.max_item_types",
                "Max Item Types", "アイテム最大種類数",
                "Maximum number of distinct item types per player in the Ideaspace Storage.",
                "イデアストレージに格納できるアイテムの最大種類数です.");
        addConfigValue(root, "psitweaks.configuration.idea_storage.item_stacks_per_type",
                "Item Stacks per Type", "1種類あたりのスタック数係数",
                "Per-type item limit as a multiplier of the item's max stack size.",
                "アイテム1種類あたりの上限を, 最大スタック数に掛ける係数として設定します.");
        addConfigValue(root, "psitweaks.configuration.idea_storage.max_fluid_types",
                "Max Fluid Types", "液体最大種類数",
                "Maximum number of distinct fluid types per player in the Ideaspace Storage.",
                "イデアストレージに格納できる液体の最大種類数です.");
        addConfigValue(root, "psitweaks.configuration.idea_storage.max_fluid_per_type",
                "Max Fluid per Type", "液体1種類あたりの上限",
                "Maximum amount stored per fluid type. The config value uses internal mB units; the default is 1,048,576 B.",
                "液体1種類あたりに格納できる上限です. コンフィグ値は内部mB単位で, 既定値は1,048,576 Bです.");
        addConfigValue(root, "psitweaks.configuration.idea_storage.max_chemical_types",
                "Max Chemical Types", "Chemical最大種類数",
                "Maximum number of distinct chemical types per player in the Ideaspace Storage.",
                "イデアストレージに格納できるChemicalの最大種類数です.");
        addConfigValue(root, "psitweaks.configuration.idea_storage.max_energy",
                "Max FE", "FE上限", "Maximum FE stored per player.", "プレイヤーごとに保存できるFEの上限です.");
        addConfigValue(root, "psitweaks.configuration.idea_storage.max_chemical_per_type",
                "Max Chemical per Type", "Chemical 1種類あたりの上限",
                "Maximum amount stored per chemical type. The config value uses internal mB units; the default is 33,554,432 B.",
                "Chemical 1種類あたりに格納できる上限です. コンフィグ値は内部mB単位で, 既定値は33,554,432 Bです.");
    }

    private void addDamageMultiplierConfig(JsonObject root, String id, String enUsName, String jaJpName) {
        String key = "psitweaks.configuration.spells." + id;
        addConfigLabel(root, key, enUsName, jaJpName);
        addConfigValue(root, key + ".damage_multiplier", "Damage Multiplier", "ダメージ倍率",
                "Multiplier applied to " + enUsName + " damage.",
                jaJpName + "のダメージに適用する倍率です.");
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
            case "ja_jp" -> "%1$sは%2$sの連鎖電光に撃たれた";
            default -> "%1$s was struck by %2$s's chain lightning";
        });
        root.addProperty("death.attack.psitweaks.chain_lightning.player", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sの連鎖電光に撃たれた";
            default -> "%1$s was struck by %2$s's chain lightning";
        });
        root.addProperty("death.attack.psitweaks.chain_lightning.item", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sが%3$sで放った連鎖電光に撃たれた";
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
        root.addProperty("death.attack.psitweaks.air_bullet", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sのエア・ブリットに撃ち抜かれた";
            default -> "%1$s was shot through by %2$s's Air Bullet";
        });
        root.addProperty("death.attack.psitweaks.air_bullet.player", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sのエア・ブリットに撃ち抜かれた";
            default -> "%1$s was shot through by %2$s's Air Bullet";
        });
        root.addProperty("death.attack.psitweaks.air_bullet.item", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sが%3$sで放ったエア・ブリットに撃ち抜かれた";
            default -> "%1$s was shot through by Air Bullet cast by %2$s using %3$s";
        });
        root.addProperty("death.attack.psitweaks.psi_bullet", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sのサイ・ブリットに撃ち抜かれた";
            default -> "%1$s was shot by %2$s's Psi Bullet";
        });
        root.addProperty("death.attack.psitweaks.psi_bullet.player", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sのサイ・ブリットに撃ち抜かれた";
            default -> "%1$s was shot by %2$s's Psi Bullet";
        });
        root.addProperty("death.attack.psitweaks.psi_bullet.item", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sが%3$sで放ったサイ・ブリットに撃ち抜かれた";
            default -> "%1$s was shot by Psi Bullet cast by %2$s using %3$s";
        });
        root.addProperty("death.attack.psitweaks.gram_demolition", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sの術式解体に巻き込まれた";
            default -> "%1$s was caught in %2$s's Gram Demolition";
        });
        root.addProperty("death.attack.psitweaks.gram_demolition.player", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sの術式解体に巻き込まれた";
            default -> "%1$s was caught in %2$s's Gram Demolition";
        });
        root.addProperty("death.attack.psitweaks.gram_demolition.item", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sが%3$sで放った術式解体に巻き込まれた";
            default -> "%1$s was caught in Gram Demolition cast by %2$s using %3$s";
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
        root.addProperty("death.attack.psitweaks.pseudo_meteor", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sが呼んだ疑似隕石に押し潰された";
            default -> "%1$s was crushed by a pseudo meteor called down by %2$s";
        });
        root.addProperty("death.attack.psitweaks.pseudo_meteor.player", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sが呼んだ疑似隕石に押し潰された";
            default -> "%1$s was crushed by a pseudo meteor called down by %2$s";
        });
        root.addProperty("death.attack.psitweaks.pseudo_meteor.item", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sが%3$sで呼んだ疑似隕石に押し潰された";
            default -> "%1$s was crushed by a pseudo meteor %2$s called down using %3$s";
        });
        root.addProperty("death.attack.psitweaks.micro_black_hole", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sのマイクロブラックホールに押し潰された";
            default -> "%1$s was crushed by %2$s's Micro Black Hole";
        });
        root.addProperty("death.attack.psitweaks.micro_black_hole.player", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sのマイクロブラックホールに押し潰された";
            default -> "%1$s was crushed by %2$s's Micro Black Hole";
        });
        root.addProperty("death.attack.psitweaks.micro_black_hole.item", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sが%3$sで生み出したマイクロブラックホールに押し潰された";
            default -> "%1$s was crushed by a Micro Black Hole %2$s created using %3$s";
        });
        root.addProperty("death.attack.psitweaks.active_air_mine", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sの衝撃波に粉砕された";
            default -> "%1$s was shattered by %2$s's shockwave";
        });
        root.addProperty("death.attack.psitweaks.active_air_mine.player", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sの衝撃波に粉砕された";
            default -> "%1$s was shattered by %2$s's shockwave";
        });
        root.addProperty("death.attack.psitweaks.active_air_mine.item", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sが%3$sで放った衝撃波に粉砕された";
            default -> "%1$s was shattered by a shockwave %2$s unleashed using %3$s";
        });
        // 術者がいなくなった後の円は攻撃者がないので、基本の文は %2$s を使わない。
        root.addProperty("death.attack.psitweaks.flare_circle", switch (locale) {
            case "ja_jp" -> "%1$sはファイアサークルに焼かれた";
            default -> "%1$s was burned by a Fire Circle";
        });
        root.addProperty("death.attack.psitweaks.flare_circle.player", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sのファイアサークルに焼かれた";
            default -> "%1$s was burned by %2$s's Fire Circle";
        });
        root.addProperty("death.attack.psitweaks.flare_circle.item", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sが%3$sで張ったファイアサークルに焼かれた";
            default -> "%1$s was burned by a Fire Circle %2$s cast using %3$s";
        });
        root.addProperty("death.attack.psitweaks.ice_circle", switch (locale) {
            case "ja_jp" -> "%1$sはアイスサークルに凍らされた";
            default -> "%1$s was frozen by an Ice Circle";
        });
        root.addProperty("death.attack.psitweaks.ice_circle.player", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sのアイスサークルに凍らされた";
            default -> "%1$s was frozen by %2$s's Ice Circle";
        });
        root.addProperty("death.attack.psitweaks.ice_circle.item", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sが%3$sで張ったアイスサークルに凍らされた";
            default -> "%1$s was frozen by an Ice Circle %2$s cast using %3$s";
        });
        root.addProperty("death.attack.psitweaks.heat_storm", switch (locale) {
            case "ja_jp" -> "%1$sは熱乱流に焼かれた";
            default -> "%1$s was burned by a Heat Storm";
        });
        root.addProperty("death.attack.psitweaks.heat_storm.player", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sの熱乱流に焼かれた";
            default -> "%1$s was burned by %2$s's Heat Storm";
        });
        root.addProperty("death.attack.psitweaks.heat_storm.item", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sが%3$sで起こした熱乱流に焼かれた";
            default -> "%1$s was burned by a Heat Storm %2$s raised using %3$s";
        });
        root.addProperty("death.attack.psitweaks.blizzard", switch (locale) {
            case "ja_jp" -> "%1$sは吹雪に凍えた";
            default -> "%1$s froze in a Blizzard";
        });
        root.addProperty("death.attack.psitweaks.blizzard.player", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sの吹雪に凍えた";
            default -> "%1$s froze in %2$s's Blizzard";
        });
        root.addProperty("death.attack.psitweaks.blizzard.item", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sが%3$sで起こした吹雪に凍えた";
            default -> "%1$s froze in a Blizzard %2$s raised using %3$s";
        });
        root.addProperty("death.attack.psitweaks.thundercloud", switch (locale) {
            case "ja_jp" -> "%1$sは雷雲の雷に打たれた";
            default -> "%1$s was struck by a Thundercloud";
        });
        root.addProperty("death.attack.psitweaks.thundercloud.player", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sの雷雲の雷に打たれた";
            default -> "%1$s was struck by %2$s's Thundercloud";
        });
        root.addProperty("death.attack.psitweaks.thundercloud.item", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sが%3$sで起こした雷雲の雷に打たれた";
            default -> "%1$s was struck by a Thundercloud %2$s raised using %3$s";
        });
        root.addProperty("death.attack.psitweaks.rainstorm", switch (locale) {
            case "ja_jp" -> "%1$sは暴風雨に打たれて力尽きた";
            default -> "%1$s was beaten down by a Rainstorm";
        });
        root.addProperty("death.attack.psitweaks.rainstorm.player", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sの暴風雨に打たれて力尽きた";
            default -> "%1$s was beaten down by %2$s's Rainstorm";
        });
        root.addProperty("death.attack.psitweaks.rainstorm.item", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sが%3$sで起こした暴風雨に打たれて力尽きた";
            default -> "%1$s was beaten down by a Rainstorm %2$s raised using %3$s";
        });
        root.addProperty("death.attack.psitweaks.contaminated_stream", switch (locale) {
            case "ja_jp" -> "%1$sは汚染流に蝕まれた";
            default -> "%1$s was eaten away by a Contaminated Stream";
        });
        root.addProperty("death.attack.psitweaks.contaminated_stream.player", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sの汚染流に蝕まれた";
            default -> "%1$s was eaten away by %2$s's Contaminated Stream";
        });
        root.addProperty("death.attack.psitweaks.contaminated_stream.item", switch (locale) {
            case "ja_jp" -> "%1$sは%2$sが%3$sで起こした汚染流に蝕まれた";
            default -> "%1$s was eaten away by a Contaminated Stream %2$s raised using %3$s";
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
            {"psi.book.page.psitweaks_machine.psionic_generator", "A generator that links to its Mekanism owner and converts that player's Psi into energy while the owner is online.$(p)Use the GUI to enable the link and set the Psi consumed per tick. Higher consumption gives higher output and drains the owner faster.$(p)By default, generated energy auto-outputs from the back, up to 25600 FE/tick."},
            {"psi.book.page.psitweaks_machine.spellmachinery_casing", "A casing block used to execute advanced magic through machinery."},
            {"psi.book.page.psitweaks_material.alloy_hypostasis", "Made by infusing $(l:components/psitweaks_alloy_psionic_echo)$(o)$(item)Echo Alloy$(0)$(/l) with Hypostasis.$(p)Used for $(l:components/psitweaks_hypostasis_control_circuit)$(o)$(item)Hypostasis Control Circuits$(0)$(/l) and high-tier psionic machinery."},
            {"psi.book.page.psitweaks_material.alloy_psion", "Made by infusing Mekanism's Reinforced Alloy with Psigem.$(p)Used for $(l:components/psitweaks_psionic_control_circuit)$(o)$(item)Psionic Control Circuits$(0)$(/l), Psionic Alloy CAD assemblies, and the next alloy tier."},
            {"psi.book.page.psitweaks_material.alloy_psionic_echo", "Made by infusing $(l:components/psitweaks_alloy_psion)$(o)$(item)Psionic Alloy$(0)$(/l) with $(l:components/psitweaks_psionic_echo)Psionic Echo$(/l).$(p)Used for $(l:components/psitweaks_echo_control_circuit)$(o)$(item)Echo Control Circuits$(0)$(/l) and as the base for $(l:components/psitweaks_alloy_hypostasis)Hypostasis Alloy$(/l)."},
            {"psi.book.page.psitweaks_material.antinite_ingot", "Smelted from Antinite ore or recovered from Antinite blocks. The dust, shard, clump, crystal, and dirty dust forms are intermediate products in Mekanism-style ore processing.$(p)Used as a material for $(l:components/psitweaks_hypostasis_gem)Hypostasis Gems$(/l) and the $(l:items/psitweaks_philosophers_stone)$(o)$(item)Philosopher's Stone$(0)$(/l)."},
            {"psi.book.page.psitweaks_material.chaotic_factor", "$(item)Chaotic Factor$(0) starts with infusing an $(item)Ender Pearl$(0) with Psigem to make $(item)Psionic Factor$(0). Infuse that with Ivory or Ebony to make the aligned factor variants, then cross the opposite alignment to create $(item)Chaotic Factor$(0).$(p)Used as the infusion source for $(l:components/psitweaks_chaotic_psimetal)$(o)$(item)Chaotic Psimetal$(0)$(/l), the first major PsiTweaks metal above ordinary Psimetal."},
            {"psi.book.page.psitweaks_material.chaotic_psimetal", "Made by infusing ordinary $(item)Psimetal$(0) with $(l:components/psitweaks_chaotic_factor)Chaotic Factor$(/l).$(p)Used in recipes for Chaotic Psimetal CAD assemblies and $(l:components/psitweaks_unrefined_flashmetal)$(o)$(item)Unrefined Flashmetal$(0)$(/l)."},
            {"psi.book.page.psitweaks_material.dimensional_crystal", "Occasionally, casting a spell yields a $(item)Dimensional Crystal$(0). The chance increases in proportion to the amount of Psi the cast consumes.$(p)It cannot be crafted. If you want to gather them steadily, you need to make spellcasting part of your daily life."},
            {"psi.book.page.psitweaks_material.echo_control_circuit", "Crafted from $(l:components/psitweaks_psionic_control_circuit)$(o)$(item)Psionic Control Circuit$(0)$(/l), $(l:components/psitweaks_alloy_psionic_echo)$(o)$(item)Echo Alloy$(0)$(/l), and $(l:components/psitweaks_echo_sheet)HDΨE Sheets$(/l).$(p)Used in recipes for advanced spell bullets and machinery."},
            {"psi.book.page.psitweaks_material.echo_pellet", "A compact HDΨE material made from $(l:components/psitweaks_psionic_echo)Psionic Echo$(/l) processing.$(p)Used where recipes need condensed echo material rather than raw $(l:components/psitweaks_psionic_echo)Psionic Echo$(/l). The ΨE-O Fuel produced as a byproduct can be used as fuel for the Gas Generator."},
            {"psi.book.page.psitweaks_material.echo_sheet", "Made by enriching HDΨE material into a sheet form.$(p)Used heavily in Echo and Hypostasis Control Circuit recipes."},
            {"psi.book.page.psitweaks_material.flashmetal", "Made by enriching $(l:components/psitweaks_unrefined_flashmetal)$(o)$(item)Unrefined Flashmetal$(0)$(/l).$(p)Used for Flashmetal blocks, Flashmetal CAD assemblies, $(l:components/psitweaks_heavy_psimetal)Heavy Psimetal$(/l), and sublimated-tier spell bullets."},
            {"psi.book.page.psitweaks_material.graviton_factor", "$(item)Graviton Factor$(0) cannot be made by crafting or infusing. Drop a $(item)Quantum Factor$(0) as an item and let it fall 380 blocks; the strain of the fall rewrites it into a $(item)Graviton Factor$(0) in mid-air. The stack count is carried over as-is.$(p)Use Add Motion, a Gravity Zone or another mod's jetpack, or prepare a drop shaft from near the build limit, and collect it at the bottom."},
            {"psi.book.page.psitweaks_material.heavy_psimetal", "Crafted from $(l:components/psitweaks_heavy_psimetal_scrap)$(o)$(item)Heavy Psimetal Scrap$(0)$(/l) and $(l:components/psitweaks_flashmetal)$(o)$(item)Flashmetal$(0)$(/l).$(p)Used for stronger CAD assemblies, gear, and the $(l:components/psitweaks_psycheonic_metal_ingot)Psycheonic Metal chain$(/l)."},
            {"psi.book.page.psitweaks_material.heavy_psimetal_scrap", "Made by infusing Netherite Scrap with $(l:components/psitweaks_psionic_echo)Psionic Echo$(/l).$(p)Used with $(l:components/psitweaks_flashmetal)$(o)$(item)Flashmetal$(0)$(/l) to craft $(l:components/psitweaks_heavy_psimetal)$(o)$(item)Heavy Psimetal$(0)$(/l)."},
            {"psi.book.page.psitweaks_material.hypostasis_control_circuit", "Crafted from $(l:components/psitweaks_echo_control_circuit)$(o)$(item)Echo Control Circuit$(0)$(/l), $(l:components/psitweaks_alloy_hypostasis)$(o)$(item)Hypostasis Alloy$(0)$(/l), and $(l:components/psitweaks_echo_sheet)HDΨE Sheets$(/l).$(p)Used for the highest machinery."},
            {"psi.book.page.psitweaks_material.hypostasis_gem", "Made by applying material mutation to an $(item)Antinite Block$(0), either with the spell trick or the $(l:psitweaks_machines/material_mutator)Material Mutator$(/l).$(p)It can be enriched and converted into Hypostasis infusion, and is also used for $(l:components/psitweaks_magatama)$(o)$(item)Magatama$(0)$(/l), $(l:components/psitweaks_alloy_hypostasis)Hypostasis Alloy$(/l), and the $(l:components/psitweaks_psycheonic_metal_ingot)Psycheonic Metal chain$(/l)."},
            {"psi.book.page.psitweaks_material.jade", "Made by applying material mutation to an $(item)Emerald Block$(0), either with the spell trick or the $(l:psitweaks_machines/material_mutator)Material Mutator$(/l).$(p)Used as the main material for crafting $(l:components/psitweaks_magatama)$(o)$(item)Magatama$(0)$(/l)."},
            {"psi.book.page.psitweaks_material.magatama", "Crafted from $(l:components/psitweaks_jade)$(o)$(item)Jade$(0)$(/l) surrounding a $(l:components/psitweaks_hypostasis_gem)$(o)$(item)Hypostasis Gem$(0)$(/l).$(p)Used in $(l:psitweaks_machines/spellmachinery_casing)Spellmachinery Casings$(/l), making it a bridge between material mutation and advanced psionic machinery."},
            {"psi.book.page.psitweaks_material.pellet_americium", "Made by mutating a $(item)Plutonium Block$(0) with $(l:psitweaks_spell_pieces/trick_material_mutation)$(o)Trick: Material Mutation$(0)$(/l), or by processing it in the $(l:psitweaks_machines/material_mutator)$(o)Material Mutator$(0)$(/l).$(p)Used for Psycheonic Metal CAD Assemblies and other late-game psionic recipes."},
            {"psi.book.page.psitweaks_material.pellet_neptunium", "Made by mutating a $(item)Polonium Block$(0) with $(l:psitweaks_spell_pieces/trick_material_mutation)$(o)Trick: Material Mutation$(0)$(/l), or by processing it in the $(l:psitweaks_machines/material_mutator)$(o)Material Mutator$(0)$(/l).$(p)Used for the $(l:psitweaks_machines/transcendent_universal_cable)$(o)$(item)Transcendent Universal Cable$(0)$(/l) and the MekaSuit $(item)Phenomenon Interference Enhancement Unit$(0)."},
            {"psi.book.page.psitweaks_material.psionic_control_circuit", "Crafted from $(l:components/psitweaks_alloy_psion)$(o)$(item)Psionic Alloy$(0)$(/l) and a Mekanism Elite Control Circuit.$(p)Used in PsiTweaks machines, upgraded spell bullets, and similar recipes."},
            {"psi.book.page.psitweaks_material.psionic_echo", "Made by infusing an $(item)Echo Shard$(0) with Psionic Echo infusion, or by using the $(l:psitweaks_spell_pieces/trick_supreme_infusion)$(o)Trick: Supreme Infusion$(0)$(/l).$(p)Used as the base catalyst for $(l:components/psitweaks_alloy_psionic_echo)echo-tier alloys$(/l), HDΨE materials, gas processing, and advanced psionic machinery."},
            {"psi.book.page.psitweaks_material.psycheonic_metal_ingot", "Infuse $(l:components/psitweaks_heavy_psimetal)$(o)$(item)Heavy Psimetal$(0)$(/l) with Hypostasis to make $(item)Psycheonic Metal Nuggets$(0), then craft nine nuggets into an ingot.$(p)Used for $(l:items/psitweaks_spell_bullets)transcendent spell bullets$(/l), Psycheonic Metal CAD assemblies, and the highest psionic material tier."},
            {"psi.book.page.psitweaks_material.unrefined_flashmetal", "Crafted from $(l:components/psitweaks_chaotic_psimetal)$(o)$(item)Chaotic Psimetal$(0)$(/l) and Refined Glowstone.$(p)Enrich it to obtain usable $(l:components/psitweaks_flashmetal)$(o)$(item)Flashmetal$(0)$(/l)."},
            {"psi.book.page.psitweaks_mekanism_integration.1", "The $(item)Metallurgic Infuser$(0) can produce core Psi materials with PsiTweaks infuse types. Psigem infusion turns redstone into $(l:components/psidust)$(o)$(item)Psidust$(0)$(/l) and gold into $(item)Psimetal$(0).$(p)Ebony and Ivory infusion can make $(item)Ebony Substance$(0), $(item)Ivory Substance$(0), and their Psimetal variants. Later recipes extend this chain into $(l:components/psitweaks_chaotic_factor)psionic factors$(/l), $(l:components/psitweaks_chaotic_psimetal)$(o)$(item)Chaotic Psimetal$(0)$(/l), $(l:components/psitweaks_psionic_echo)Psionic Echo materials$(/l), and $(l:components/psitweaks_alloy_hypostasis)Hypostasis-tier metal$(/l)."},
            {"psi.book.page.psitweaks_mekanism_integration.2", "PsiTweaks adds three MekaSuit modules for spellcasters. The $(item)Psyon Supplying Unit$(0) and $(item)Psyon Capacity Unit$(0) install in the MekaSuit body armor and improve Psi regeneration and maximum Psi.$(p)The $(item)Phenomenon Interference Enhancement Unit$(0) installs in the MekaSuit helmet and increases Phenomenon Interference."},
            {"psi.book.page.psitweaks_research.0", "Some spell pieces added by $(thing)PsiTweaks$(0) must be unlocked through research before they can be used in spell programs.$(p)Research creates program items that correspond to those spell pieces. Right-clicking with a program unlocks its spell piece, and the program is not consumed."},
            {"psi.book.page.psitweaks_research.1", "Place the required ingredients in the $(l:psitweaks_machines/program_researcher)$(o)$(item)Program Research Table$(0)$(/l) and supply FE to craft a program.$(p)JEI lists each research recipe's required materials, energy usage per tick, and processing time."},
            {"psi.book.page.psitweaks_spell_bullets.0", "$(thing)PsiTweaks$(0) adds upgraded tiers above the ordinary $(l:items/spell_bullet)$(o)$(item)Spell Bullet$(0)$(/l): Advanced, Resonant, Sublimated, Awakened, and Transcendent.$(p)Every Psi spell bullet variant has upgraded tiers available."},
            {"psi.book.page.psitweaks_spell_bullets.1", "Upgrading a spell bullet requires four bullets from the previous tier, making it very expensive."},
            {"psi.book.page.psitweaks_spell_bullets.2", "Higher tiers greatly improve cost efficiency, making powerful spells easier to cast."},
            {"psi.book.page.psitweaks_spellpiece.selector_nearby_spellgram", "Gets SpellGram objects (Resident SpellGrams, the cores of Meteor Line nights and Micro Black Holes) around the specified coordinates. It is mainly used by tricks that control placed SpellGram objects."},
            {"psi.book.page.psitweaks_spellpiece.selector_stored_entity", "Gets an entity from the UUID stored in CAD memory. Only an entity within the spell's range is found."},
            {"psi.book.page.psitweaks_spellpiece.trick_blaze_ball", "Fires a ball of flame forward as an early-game offensive trick."},
            {"psi.book.page.psitweaks_spellpiece.trick_break_fortune", "Breaks the target block with Fortune."},
            {"psi.book.page.psitweaks_spellpiece.trick_break_silk", "Breaks the target block with Silk Touch."},
            {"psi.book.page.psitweaks_spellpiece.trick_cure_radiation", "Removes the target's radiation exposure."},
            {"psi.book.page.psitweaks_spellpiece.trick_die_flex", "Behaves similarly to Psi's Trick: Die and refunds the Psi cost of spell pieces that were not executed. With high-frequency casting, the client-side Psi display may temporarily desync."},
            {"psi.book.page.psitweaks_spellpiece.trick_dispel", "Removes effects from the target entity. This is the general-purpose dispel that does not distinguish between beneficial and harmful effects."},
            {"psi.book.page.psitweaks_spellpiece.trick_dispel_beneficial", "Removes only beneficial effects from the target entity. It is suited for stripping enhancements from hostile targets."},
            {"psi.book.page.psitweaks_spellpiece.trick_dispel_non_beneficial", "Removes non-beneficial effects from the target entity. Use it when you want to cleanse harmful effects while leaving allied buffs intact."},
            {"psi.book.page.psitweaks_spellpiece.trick_explode_no_destroy", "Creates an explosion that deals damage without destroying blocks. Dropped items and experience orbs are not erased by the blast."},
            {"psi.book.page.psitweaks_spellpiece.trick_flight", "Applies the Flight effect to the target. The Flight effect enables creative flight but consumes Psi every 0.5 seconds. The amount consumed varies based on the Flight effect level, the efficiency of the CAD used to apply the effect, and the spell bullet's cost multiplier. The base consumption at effect levels 1, 2, 3, 4, and 5 or higher is 1000 / 750 / 500 / 250 / 5 Psi every 0.5 seconds, respectively."},
            {"psi.book.page.psitweaks_spellpiece.trick_freeze_block", "Freezes the target block one stage. Water becomes ice, ice becomes packed ice, packed ice becomes blue ice, lava becomes magma block, and magma block becomes obsidian."},
            {"psi.book.page.psitweaks_spellpiece.trick_hardening", "Applies a hardening effect that limits large incoming damage to a fixed value. Maximum damage taken is capped at (level - 2)."},
            {"psi.book.page.psitweaks_spellpiece.trick_interact_block", "Acts on the target block as if right-clicked with the item in the caster's off hand."},
            {"psi.book.page.psitweaks_spellpiece.trick_material_mutation", "Breaks specific blocks and transmutes them into other items. The Material Mutator can perform this process using power and Vaporized Psionic Echo."},
            {"psi.book.page.psitweaks_spellpiece.trick_melt_block", "Melts the target block one stage. Ice, packed ice, and blue ice become water; obsidian, stone-like blocks, and cobblestone-like blocks become magma block; and magma block becomes lava."},
            {"psi.book.page.psitweaks_spellpiece.trick_radiation_injection", "Applies Mekanism radiation exposure to the target."},
            {"psi.book.page.psitweaks_spellpiece.trick_store_entity", "Stores the target entity's UUID in CAD memory."},
            {"psi.book.page.psitweaks_spellpiece.trick_supply_fe", "Supplies FE to the target block. When CAD Efficiency is 100, it supplies 80 FE per Psi."},
            {"psi.book.page.psitweaks_spellpiece.trick_supreme_infusion", "Infusion-converts Echo Shards into Psionic Echo."},
            {"psi.book.page.psitweaks_spellpiece.trick_time_accelerate", "Multiplies the target block's tick progression by (2 ^ power). The upper limit is 512x speed. Casting it again on the same block replaces both the rate and the remaining time."},
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
            {"psi.book.page.psitweaks_machine.psionic_generator", "所有者にリンクし, 所有者がオンラインの間, そのプレイヤーのPsiをエネルギーへ変換する発電機です.$(p)GUIからリンクの有効化とtickあたりのPsi消費量を設定します. 消費量を増やすほど出力も増えますが, 所有者のPsiも速く減ります.$(p)発電したFEは, 初期設定では背面から最大25600 FE/tickで自動出力します."},
            {"psi.book.page.psitweaks_machine.spellmachinery_casing", "高度な魔法を機械により実行するための筐体ブロックです."},
            {"psi.book.page.psitweaks_material.alloy_hypostasis", "$(l:components/psitweaks_alloy_psionic_echo)$(o)$(item)感応合金$(0)$(/l) にヒュポスタシスを注入して作ります.$(p)$(l:components/psitweaks_hypostasis_control_circuit)$(o)$(item)位格制御回路$(0)$(/l) や高 tier のサイオニック機械に使います."},
            {"psi.book.page.psitweaks_material.alloy_psion", "Mekanism の強化合金にサイジェムを注入して作ります.$(p)$(l:components/psitweaks_psionic_control_circuit)$(o)$(item)サイオニック制御回路$(0)$(/l), サイオニック合金 CAD 素体, 次 tier の合金素材に使います."},
            {"psi.book.page.psitweaks_material.alloy_psionic_echo", "$(l:components/psitweaks_alloy_psion)$(o)$(item)サイオニック合金$(0)$(/l) に$(l:components/psitweaks_psionic_echo)サイオニックエコー$(/l)を注入して作ります.$(p)$(l:components/psitweaks_echo_control_circuit)$(o)$(item)感応制御回路$(0)$(/l) や$(l:components/psitweaks_alloy_hypostasis)位格合金$(/l)の素材に使います."},
            {"psi.book.page.psitweaks_material.antinite_ingot", "アンティナイトの鉱石を精錬するか, アンティナイトブロックから戻して入手します. 粉, 欠片, 凝塊, 結晶, 汚れた粉は Mekanism 式鉱石処理の中間素材です.$(p)$(l:components/psitweaks_hypostasis_gem)ヒュポスタシスジェム$(/l)や$(l:items/psitweaks_philosophers_stone)賢者の石$(/l)の素材になります."},
            {"psi.book.page.psitweaks_material.chaotic_factor", "$(item)カオティック因子$(0) は, $(item)エンダーパール$(0) にサイジェムを注入して $(item)サイオニック因子$(0) を作るところから始まります. そこへアイボリーまたはエボニーを注入して偏向因子を作り, 反対属性を重ねると $(item)カオティック因子$(0) になります.$(p)通常のサイメタルを上回る最初の PsiTweaks 金属, $(l:components/psitweaks_chaotic_psimetal)$(o)$(item)カオティックサイメタル$(0)$(/l) の注入源です."},
            {"psi.book.page.psitweaks_material.chaotic_psimetal", "通常の $(item)サイメタル$(0) に$(l:components/psitweaks_chaotic_factor)カオティック因子$(/l)を注入して作ります.$(p)カオティックサイメタル CAD 素体や $(l:components/psitweaks_unrefined_flashmetal)$(o)$(item)未精製フラッシュメタル$(0)$(/l) のレシピに使います."},
            {"psi.book.page.psitweaks_material.dimensional_crystal", "術式の詠唱時に, まれに $(item)次元の結晶$(0) が手に入ります. 確率は詠唱が消費した Psi の量に比例して上がります.$(p)クラフトでは作れません. 安定して集めたいなら, 日常的に魔法を使って生活する必要があります."},
            {"psi.book.page.psitweaks_material.echo_control_circuit", "$(l:components/psitweaks_psionic_control_circuit)$(o)$(item)サイオニック制御回路$(0)$(/l), $(l:components/psitweaks_alloy_psionic_echo)$(o)$(item)感応合金$(0)$(/l), $(l:components/psitweaks_echo_sheet)HDΨE シート$(/l)からクラフトします.$(p) 上位の術式弾や機械のレシピに使います."},
            {"psi.book.page.psitweaks_material.echo_pellet", "$(l:components/psitweaks_psionic_echo)サイオニックエコー$(/l)加工から得られる圧縮 HDΨE 素材です.$(p)生の$(l:components/psitweaks_psionic_echo)サイオニックエコー$(/l)ではなく, 凝縮した感応素材を要求するレシピに使います. 副産物として得られるΨE-O燃料はガス発電機の燃料として使えます."},
            {"psi.book.page.psitweaks_material.echo_sheet", "HDΨE 素材をシート状に濃縮して作ります.$(p)感応制御回路と位格制御回路のレシピで多く使います."},
            {"psi.book.page.psitweaks_material.flashmetal", "$(l:components/psitweaks_unrefined_flashmetal)$(o)$(item)未精製フラッシュメタル$(0)$(/l) を濃縮して作ります.$(p)フラッシュメタルブロック, フラッシュメタル CAD 素体, $(l:components/psitweaks_heavy_psimetal)ヘビーサイメタル$(/l), 昇華 tier の術式弾に使います."},
            {"psi.book.page.psitweaks_material.graviton_factor", "$(item)グラヴィトン因子$(0) はクラフトや注入では作れません. $(item)クァンタム因子$(0) をアイテムとして落とし, 380ブロック落下させると, 落下の負荷によって空中で $(item)グラヴィトン因子$(0) へ書き換わります. スタック数はそのまま引き継がれます.$(p)運動追加や加重領域, 他modのジェットパックなどを使うか, 建築限界の高さ付近から落下用の縦穴を用意し, 底で回収しましょう."},
            {"psi.book.page.psitweaks_material.heavy_psimetal", "$(l:components/psitweaks_heavy_psimetal_scrap)$(o)$(item)ヘビーサイメタルの欠片$(0)$(/l) と $(l:components/psitweaks_flashmetal)$(o)$(item)フラッシュメタル$(0)$(/l) からクラフトします.$(p)より強い CAD 素体, 装備, $(l:components/psitweaks_psycheonic_metal_ingot)プシオニックメタル系列$(/l)に使います."},
            {"psi.book.page.psitweaks_material.heavy_psimetal_scrap", "ネザライトの欠片に$(l:components/psitweaks_psionic_echo)サイオニックエコー$(/l)を注入して作ります.$(p)$(l:components/psitweaks_flashmetal)$(o)$(item)フラッシュメタル$(0)$(/l) と組み合わせて $(l:components/psitweaks_heavy_psimetal)$(o)$(item)ヘビーサイメタル$(0)$(/l) を作ります."},
            {"psi.book.page.psitweaks_material.hypostasis_control_circuit", "$(l:components/psitweaks_echo_control_circuit)$(o)$(item)感応制御回路$(0)$(/l), $(l:components/psitweaks_alloy_hypostasis)$(o)$(item)位格合金$(0)$(/l), $(l:components/psitweaks_echo_sheet)HDΨE シート$(/l)からクラフトします.$(p)最上位の機械に使います."},
            {"psi.book.page.psitweaks_material.hypostasis_gem", "$(item)アンティナイトブロック$(0) に物質変成を作用させて作ります. 術式でも$(l:psitweaks_machines/material_mutator)物質変成機$(/l)でも入手できます.$(p)濃縮してヒュポスタシス注入へ変換できるほか, $(l:components/psitweaks_magatama)$(o)$(item)勾玉$(0)$(/l), $(l:components/psitweaks_alloy_hypostasis)位格合金$(/l), $(l:components/psitweaks_psycheonic_metal_ingot)プシオニックメタル系列$(/l)に使います."},
            {"psi.book.page.psitweaks_material.jade", "$(item)エメラルドブロック$(0) に物質変成を作用させて作ります. 術式でも$(l:psitweaks_machines/material_mutator)物質変成機$(/l)でも入手できます.$(p)$(l:components/psitweaks_magatama)$(o)$(item)勾玉$(0)$(/l) を作るための主素材です."},
            {"psi.book.page.psitweaks_material.magatama", "$(l:components/psitweaks_jade)$(o)$(item)翡翠$(0)$(/l) で $(l:components/psitweaks_hypostasis_gem)$(o)$(item)ヒュポスタシスジェム$(0)$(/l) を囲んでクラフトします.$(p)$(l:psitweaks_machines/spellmachinery_casing)魔導機構ケーシング$(/l)に使うため, 物質変成素材と上位サイオニック機械をつなぐ素材です."},
            {"psi.book.page.psitweaks_material.pellet_americium", "$(item)プルトニウムブロック$(0) を $(l:psitweaks_spell_pieces/trick_material_mutation)$(o)作動式: 物質変成$(0)$(/l) で変成するか, $(l:psitweaks_machines/material_mutator)$(o)物質変成機$(0)$(/l) で加工して作ります.$(p)プシオニックメタルCAD素体など, 終盤のサイオニックレシピに使います."},
            {"psi.book.page.psitweaks_material.pellet_neptunium", "$(item)ポロニウムブロック$(0) を $(l:psitweaks_spell_pieces/trick_material_mutation)$(o)作動式: 物質変成$(0)$(/l) で変成するか, $(l:psitweaks_machines/material_mutator)$(o)物質変成機$(0)$(/l) で加工して作ります.$(p)$(l:psitweaks_machines/transcendent_universal_cable)$(o)$(item)超越ユニバーサルケーブル$(0)$(/l) や MekaSuit の $(item)事象干渉力増大ユニット$(0) に使います."},
            {"psi.book.page.psitweaks_material.psionic_control_circuit", "$(l:components/psitweaks_alloy_psion)$(o)$(item)サイオニック合金$(0)$(/l) と Mekanism の精鋭制御回路からクラフトします.$(p)PsiTweaks の機械, 強化術式弾などのレシピに使います."},
            {"psi.book.page.psitweaks_material.psionic_echo", "$(item)残響の欠片$(0) にサイオニックエコーを注入するか, $(l:psitweaks_spell_pieces/trick_supreme_infusion)$(o)作動式: 超位注入$(0)$(/l)で作成します.$(p)$(l:components/psitweaks_alloy_psionic_echo)感応系合金$(/l), HDΨE 素材, 気体加工, 上位のサイオニック機械の基礎触媒です."},
            {"psi.book.page.psitweaks_material.psycheonic_metal_ingot", "$(l:components/psitweaks_heavy_psimetal)$(o)$(item)ヘビーサイメタル$(0)$(/l) にヒュポスタシスを注入して $(item)プシオニックメタルナゲット$(0) を作り, 9個クラフトするとインゴットになります.$(p)$(l:items/psitweaks_spell_bullets)超越 tier の術式弾$(/l), プシオニックメタル CAD 素体, 最上位のサイオニック素材に使います."},
            {"psi.book.page.psitweaks_material.unrefined_flashmetal", "$(l:components/psitweaks_chaotic_psimetal)$(o)$(item)カオティックサイメタル$(0)$(/l) と精製グロウストーンからクラフトします.$(p)濃縮すると使用可能な $(l:components/psitweaks_flashmetal)$(o)$(item)フラッシュメタル$(0)$(/l) になります."},
            {"psi.book.page.psitweaks_mekanism_integration.1", "$(item)冶金吹き込み機$(0) では, PsiTweaks の注入タイプを使って主要なPsi素材を作れます. サイジェム注入はレッドストーンを$(l:components/psidust)$(o)$(item)サイダスト$(0)$(/l)に, 金インゴットを$(item)サイメタル$(0)に変換します.$(p)エボニー注入とアイボリー注入では, $(item)エボニー基質$(0), $(item)アイボリー基質$(0), それぞれのサイメタル派生素材を作れます. さらに$(l:components/psitweaks_chaotic_factor)サイオニック因子$(/l), $(l:components/psitweaks_chaotic_psimetal)$(o)$(item)カオティックサイメタル$(0)$(/l), $(l:components/psitweaks_psionic_echo)サイオニックエコー素材$(/l), $(l:components/psitweaks_alloy_hypostasis)位格合金$(/l)へ発展します."},
            {"psi.book.page.psitweaks_mekanism_integration.2", "PsiTweaks は魔法師向けのMekaSuitモジュールを3種類追加します. $(item)サイオン供給ユニット$(0) と $(item)サイオン容量ユニット$(0) はMekaSuit胴体に装着し, Psi回復速度と最大Psi保有量を強化します.$(p)$(item)事象干渉力増大ユニット$(0) はMekaSuitヘルメットに装着し, 事象干渉力を高めます."},
            {"psi.book.page.psitweaks_research.0", "$(thing)PsiTweaks$(0) が追加する一部のスペルピースは, 術式プログラムで使う前に研究によるアンロックが必要です.$(p)研究では, それらのスペルピースに対応するプログラムアイテムを作成します. プログラムを右クリックで使用することでスペルピースをアンロックできます(プログラムは消費しません)."},
            {"psi.book.page.psitweaks_research.1", "$(l:psitweaks_machines/program_researcher)$(o)$(item)プログラム研究台$(0)$(/l) に必要素材を入れて FE を供給すると, プログラムをクラフトできます.$(p)各研究に必要な素材, tickあたりの消費電力, 処理時間は JEI で確認できます."},
            {"psi.book.page.psitweaks_spell_bullets.0", "$(thing)PsiTweaks$(0) は通常の$(l:items/spell_bullet)$(o)$(item)術式弾$(0)$(/l)より上位の強化ティアとして, 改良, 共鳴, 昇華, 覚醒, 超越を追加します.$(p)Psiの術式弾のバリエーション全てにそれぞれ上位Tierが用意されています."},
            {"psi.book.page.psitweaks_spell_bullets.1", "術式弾の強化は前段階の術式弾を4つ必要とするため, 非常に高コストです."},
            {"psi.book.page.psitweaks_spell_bullets.2", "上位のTierになるほどコスト効率が著しく改善し, 強力な魔法を簡単に発動できるようになります."},
            {"psi.book.page.psitweaks_spellpiece.selector_nearby_spellgram", "指定座標の周囲にある魔法式オブジェクト（常駐魔法式, 流星群の夜の核, マイクロブラックホール）を取得します. 設置済みの魔法式オブジェクトを制御する術式で主に使います."},
            {"psi.book.page.psitweaks_spellpiece.selector_stored_entity", "CADメモリに保存されたUUIDからエンティティを取得します. 術式の射程の中にいるエンティティだけを取得できます."},
            {"psi.book.page.psitweaks_spellpiece.trick_blaze_ball", "前方へ火の弾を放つ序盤用の攻撃術式です."},
            {"psi.book.page.psitweaks_spellpiece.trick_break_fortune", "対象ブロックを幸運付きで破壊します."},
            {"psi.book.page.psitweaks_spellpiece.trick_break_silk", "対象ブロックをシルクタッチ付きで破壊します."},
            {"psi.book.page.psitweaks_spellpiece.trick_cure_radiation", "対象の被ばく量を除去します."},
            {"psi.book.page.psitweaks_spellpiece.trick_die_flex", "Psi本体の停止と同様の動作を行い, 未実行分のスペルピースのPsiコストを返還します. 高頻度詠唱ではクライアント側のPsi表示が一時的にずれることがあります."},
            {"psi.book.page.psitweaks_spellpiece.trick_dispel", "対象エンティティからエフェクトを除去します. 良性・悪性を区別しない汎用版の解呪です."},
            {"psi.book.page.psitweaks_spellpiece.trick_dispel_beneficial", "対象エンティティから有益なエフェクトだけを除去します. 敵対対象の強化を剥がす用途に向いています."},
            {"psi.book.page.psitweaks_spellpiece.trick_dispel_non_beneficial", "対象エンティティから有益でないエフェクトを除去します. 味方のバフを残したまま悪性効果を消したい時に使います."},
            {"psi.book.page.psitweaks_spellpiece.trick_explode_no_destroy", "ブロックを破壊しない爆発を起こしてダメージを与えます. ドロップアイテムや経験値オーブは爆発で消滅しません."},
            {"psi.book.page.psitweaks_spellpiece.trick_flight", "対象に飛行エフェクトを付与します. 飛行エフェクトはクリエイティブ飛行を可能にしますが, 0.5秒ごとにPsiを消費します. 消費量は飛行エフェクトのレベル, 付与したCADの効率, 術式弾のコスト倍率によって変動し, エフェクトレベルが 1/2/3/4/5以上の際の基本消費量は 0.5秒ごとに 1000 / 750 / 500 / 250 / 5 です."},
            {"psi.book.page.psitweaks_spellpiece.trick_freeze_block", "対象ブロックを1段階凍結させます. 水は氷, 氷は氷塊, 氷塊は青氷, 溶岩はマグマブロック, マグマブロックは黒曜石になります."},
            {"psi.book.page.psitweaks_spellpiece.trick_hardening", "大きな被ダメージを一定値まで抑える硬化効果を付与します. 受ける最大ダメージを(レベル - 2)に抑えます."},
            {"psi.book.page.psitweaks_spellpiece.trick_interact_block", "対象ブロックに対して, 術者のオフハンドのアイテムで右クリックしたように作用します."},
            {"psi.book.page.psitweaks_spellpiece.trick_material_mutation", "特定のブロックを破壊して別のアイテムへ変成させます. 物質変成機はこの処理を電力と気化サイオニックエコーで実行できます."},
            {"psi.book.page.psitweaks_spellpiece.trick_melt_block", "対象ブロックを1段階溶解させます. 氷, 氷塊, 青氷は水に, 黒曜石, 石系, 丸石系はマグマブロックに, マグマブロックは溶岩になります."},
            {"psi.book.page.psitweaks_spellpiece.trick_radiation_injection", "対象へMekanismの放射線被ばくを付与します. "},
            {"psi.book.page.psitweaks_spellpiece.trick_store_entity", "対象エンティティのUUIDをCADメモリに保存します."},
            {"psi.book.page.psitweaks_spellpiece.trick_supply_fe", "対象ブロックへFEを供給します. 供給量はCADの効率が100のとき, 1psiあたり80FEです."},
            {"psi.book.page.psitweaks_spellpiece.trick_supreme_infusion", "残響の欠片をサイオニックエコーへ注入変換します."},
            {"psi.book.page.psitweaks_spellpiece.trick_time_accelerate", "対象ブロックのtick進行を (2 ^ 威力) 倍にします. 上限は512倍速まで. 同じブロックにかけ直すと, 倍率も残り時間も新しい詠唱のものになります."},
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
            case "ja_jp" -> "$(thing)イデアストレージ$(0)はプレイヤーごとに1つ用意される専用の仮想保管庫です. アイテム・液体・Mekanism の化学物質・FE を保管でき, ワールドにプレイヤー単位で保存されるため死亡しても失われません（種類数や容量の上限はコンフィグで変更可）.$(p)中身は$(l:psitweaks_spell_pieces/trick_idea_storage_view)$(o)作動式: イデアストレージ閲覧$(0)$(/l)で操作でき, 術式での転送・参照や$(l:psitweaks_spell_pieces/trick_ideaspace_connector)$(o)作動式: イデアコネクター$(0)$(/l)による外部機器連携にも対応します.";
            default -> "$(thing)Ideaspace Storage$(0) is a personal virtual storage for each player holding items, fluids, Mekanism chemicals, and FE. Saved per player in the world, it is never lost on death (limits are configurable).$(p)Access it via $(l:psitweaks_spell_pieces/trick_idea_storage_view)$(o)Trick: View Ideaspace Storage$(0)$(/l), automate it with transfer/selector pieces, or link pipes via $(l:psitweaks_spell_pieces/trick_ideaspace_connector)$(o)Trick: Ideaspace Connector$(0)$(/l).";
        });
        root.addProperty("psi.book.page.psitweaks_idea_storage.1", switch (locale) {
            case "ja_jp" -> "イデアストレージ系のスペルピースは, この$(item)プログラム: イデアストレージ$(0)を右クリックするだけでまとめて解禁されます（プログラムは消費されません）.$(p)プログラムの作り方は$(l:psitweaks/research)研究$(/l)を参照してください.";
            default -> "Right-clicking this $(item)Program: Ideaspace Storage$(0) unlocks all Ideaspace Storage spell pieces at once without consuming the program.$(p)See $(l:psitweaks/research)Research$(/l) for how to craft programs.";
        });
        root.addProperty("psi.book.entry.psitweaks_magician", switch (locale) {
            case "ja_jp" -> "魔法師";
            default -> "Magician";
        });
        root.addProperty("psi.book.page.psitweaks_magician.1", switch (locale) {
            case "ja_jp" -> "$(thing)魔法師$(0) は PsiTweaks が追加する村人の職業です. 無職の村人は, $(l:basics/cad_assembler)$(o)$(item)CAD組立機$(0)$(/l) を職業ブロックとして取得すると魔法師になります. 村人が到達できる場所に置いてください.";
            default -> "$(thing)Magicians$(0) are a villager profession added by PsiTweaks. An unemployed villager becomes one by claiming a $(l:basics/cad_assembler)$(o)$(item)CAD Assembler$(0)$(/l) as its job site; place it where a villager can reach it.";
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
                "$(thing)PsiTweaks$(0) adds String, Block, Item, and Matrix types to Psi spell programming.$(p)Values are grouped into Plain Values (world-independent: Number, Vector, String, Matrix; storable in CAD memory) and Contextual Values (world/target-dependent: Entity, Block, Item).",
                "$(thing)PsiTweaks$(0)は Psi のスペルプログラミングに String・Block・Item・Matrix 型を追加します.$(p)値はワールド状態に依存しない Plain Value（Number・Vector・String・Matrix 等, CAD メモリへ保存可能）と, 対象やワールドに依存する Contextual Value（Entity・Block・Item 等）に分かれます.");
        addLocalized(root, "psi.book.page.psitweaks_programming_types.1",
                "String holds text such as IDs, names, NBT paths, filter patterns, and messages.$(p)Runtime Strings cap at 8,192 characters (Constant: String pieces cap at 1,024 since they are saved in the spell), and pattern matching (* ? [abc]) enforces a per-cast comparison limit.",
                "String 型は ID・名前・NBT パス・検索条件・表示メッセージなどの文字列を扱います.$(p)実行時の上限は8,192文字（定数子: 文字列は術式内に保存するため1ピース最大1,024文字）で, パターン照合（* ? [abc]）にも1詠唱あたりの比較回数上限があります.");
        addLocalized(root, "psi.book.page.psitweaks_programming_types.2",
                "Block represents a world block (position, state, ID, tags, display name) and auto-converts with Vector on most inputs (Block List and Vector List likewise; Any inputs keep the original type).$(p)Item represents an ItemStack (type, count, NBT, display name) obtained from hands, slots, inventories, or dropped items.",
                "Block 型はワールド上のブロック（座標・状態・ID・タグ・表示名等）を扱い, 多くの入力で Vector と相互自動変換されます（Block List と Vector List も同様. Any 入力は元の型を維持）.$(p)Item 型は ItemStack（種類・個数・NBT・表示名等）を扱い, 手持ち・インベントリ・ドロップ等から取得できます.");
        addLocalized(root, "psi.book.page.psitweaks_programming_types.3",
                "Matrix is a Plain Value representing a 1×1 to 4×4 numeric matrix. Like Number and Vector, it can be stored in CAD memory and converted to text.$(p)Dedicated operators build, add, multiply, slice, transpose, and invert matrices, transform vectors, and define 3D regions.",
                "Matrix 型は 1×1〜4×4 の数値行列を表す Plain Value 型です. Number や Vector と同様に CAD メモリへの保存や文字列変換ができ, 専用演算子で行列の作成・加減乗算・行/列抽出・転置・行列式・逆行列・ベクトル変換や3D領域の定義を行えます.");

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
        root.addProperty("jei.psitweaks.idea_storage.craft_window_closed", switch (locale) {
            case "ja_jp" -> "先に左の「作業台を開く」でクラフト欄を開いてください";
            default -> "Open the crafting grid with the Open Crafting Table button on the left first";
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
        root.addProperty("sound_event.psitweaks.tile.machine.program_researcher", switch (locale) {
            case "ja_jp" -> "プログラム研究台が演算する";
            default -> "Program Research Table computes";
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
            case "ja_jp" -> "所有者の Psi を消費し, リンク中のみ FE に変換します";
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
            case "ja_jp" -> "所有者にリンクし, 所有者がオンラインのときにPsiをFEへ変換する発電機です.$(p)GUIでリンクの有効化とtickあたりのPsi消費量を設定できます. 発電したFEは, 初期設定では背面から最大25600 FE/tickで自動出力します.";
            default -> "A generator that links to its owner and converts that player's Psi into FE while the owner is online.$(p)Use the GUI to enable the link and set the Psi consumed per tick. By default, generated FE auto-outputs from the back, up to 25600 FE/tick.";
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
            case "ja_jp" -> "全術式の解禁を付与しました（%s/%s 件変更, 対象変更%s/%s人）";
            default -> "Granted all spell unlocks: %s/%s changes, players changed %s/%s";
        });
        root.addProperty("message.psitweaks.spell_unlock.command.all.revoke.single", switch (locale) {
            case "ja_jp" -> "%s から全術式の解禁を剥奪しました（%s/%s 件変更）";
            default -> "Revoked all spell unlocks from %s (%s/%s changed)";
        });
        root.addProperty("message.psitweaks.spell_unlock.command.all.revoke.multi", switch (locale) {
            case "ja_jp" -> "全術式の解禁を剥奪しました（%s/%s 件変更, 対象変更%s/%s人）";
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

    /** 作動式: ファイアサークル・アイスサークル・サークル追従の本のページ。 */
    private void addCircleBookPages(JsonObject root) {
        String timeEn = "$(p)Cost (min 50) and integrity scale with Time (constant 1–120 s, default 60). See $(l:psitweaks/resident_spellgrams)Resident SpellGrams$(/l) for integrity, limits, and management.";
        String timeJa = "$(p)「時間」（1〜120秒, 省略時60秒）に応じてコスト（最低50）と耐久値が決まります. 耐久値・設置上限・管理方法などは$(l:psitweaks/resident_spellgrams)常駐魔法式$(/l)を参照してください.";
        addBookPage(root, "trick_flare_circle.0",
                "Places a fire Resident SpellGram circle at Position, dealing fire damage every 0.5 s (scaling with Power and Phenomenon Interference) and igniting targets for 2 s (wet beings take damage without catching fire)." + timeEn,
                "「位置」に炎の常駐魔法式サークルを張ります. 中の生物に0.5秒ごと炎ダメージ（威力と事象干渉力で増加）を与え, 2秒間燃やします（濡れた生物は炎上せずダメージのみ）." + timeJa);
        addBookPage(root, "trick_flare_circle.1", circleRulesEn("Fire Circles"), circleRulesJa("ファイアサークル"));
        addBookPage(root, "trick_ice_circle.0",
                "Places an ice Resident SpellGram circle at Position, dealing freezing damage every 0.5 s (scaling with Power and Phenomenon Interference) and chilling targets." + timeEn,
                "「位置」に氷の常駐魔法式サークルを張り, 中の生物に0.5秒ごと凍結ダメージ（威力と事象干渉力で増加）と凍結を与えます." + timeJa);
        addBookPage(root, "trick_ice_circle.1",
                "Turns open water sources inside (same level and one block below) into Frosted Ice and keeps it from melting while covered (skips protected blocks and Heat Zones; melts normally once gone).$(p)While following via Trick: Circle Follow, freezes only on ground movement like Frost Walker—ice/land boats freeze water ahead, while floating boats do not.",
                "氷渡りのように範囲内（同じ高さと1段下）の開いた水源を薄氷に変えて維持します（離れるか消えると通常どおり融解. 保護ブロックや加熱領域内は対象外）.$(p)作動式: サークル追従中は氷渡り同様に移動時のみ足元1段下を凍らせ, 氷上・陸上のボートは先の水面を凍らせつつ, 水上ボートでは凍らせません.");
        addBookPage(root, "trick_ice_circle.2", circleRulesEn("Ice Circles"), circleRulesJa("アイスサークル"));
        addBookPage(root, "trick_circle_follow.0",
                "Makes your Target circles (Fire/Ice Circle; omits to your newest in this dimension, any distance) follow Follow Target every tick. Follow Target must be you or a non-living entity in range (boats, minecarts, items, etc.; not armor stands or entities ridden by others).$(p)Omit Follow Target to release the circles in place.",
                "「対象」の自分のサークル（省略時は同ディメンションの最新サークル, 距離不問）を毎 tick「追従先」へ追従させます. 「追従先」は術者自身か射程内の非生物エンティティ（ボート・トロッコ・アイテム等. 防具立てや他生物が乗っているものは不可）に限ります.$(p)「追従先」を省くと追従を解除してその場に残ります.");
        addBookPage(root, "trick_circle_follow.1",
                "Errors without changes if Target includes anything other than your own circles; zones cannot be moved.$(p)Moving consumes extra integrity (1 tick per block moved). Detaches in place if the Follow Target disappears, leaves the dimension or loaded chunks, moves over 32 blocks from you, or is ridden by another living entity.",
                "自分のサークル以外が対象に含まれるとエラーになります. 領域は動かせません.$(p)追従中は移動距離に応じて耐久値を追加消費し（1ブロックあたり1 tick 分）, 追従先の消滅・別ディメンション・未ロードチャンク・術者から32ブロック超過・他生物の騎乗のいずれかで追従を解除します.");
    }

    /** サークルの共通の決まり（重なり・領域干渉・移動）の本のページ。 */
    private static String circleRulesEn(String kind) {
        return "Where your own " + kind + " overlap, only the strongest hits (other kinds and other casters' circles hit separately). Does nothing while centered inside a stronger opposing Interference Zone, though time still passes.$(p)Move your circles with Trick: Circle Follow or Psi movement tricks (Add Motion, Blink, Mass Exodus; no integrity cost).";
    }

    private static String circleRulesJa(String kind) {
        return "自分の" + kind + "が重なる場合, 最も強いものだけが当たります（異種や他人のサークルは別々に命中）. 中心が勝てない他人の領域干渉内にある間は停止します（時間は経過）.$(p)自分のサークルは作動式: サークル追従や Psi の移動系作動式（運動追加・瞬間移動・大移動. 耐久消費なし）で動かせます.";
    }

    private void addSpellPieces(JsonObject root) {
        addSpellPiece(root, "trick_explode_no_destroy", "Trick: Antipersonnel Explode", "Cause an explosion that does not destroy blocks", "作動式: 対人爆発", "ブロックを破壊しない爆発を起こす");
        addSpellPiece(root, "trick_barrier", "Trick: Barrier", "Raises a shield that takes damage in the target's place for one minute", "作動式: 障壁", "対象に, 受けるダメージを1分間肩代わりするシールドを張る");
        addSpellPiece(root, "selector_barrier_amount", "Selector: Barrier Amount", "Gets the shield the target's Barrier has left. 0 when it has none.", "取得子: 障壁量", "対象の障壁のシールド残量を取得します. 障壁がなければ0を返します.");
        addSpellPiece(root, "trick_hardening", "Trick: Hardening", "When taking heavy damage, reduce it to a certain value", "作動式: 硬化", "大ダメージを受けた時, 一定値まで減少させる");
        addSpellPiece(root, "trick_parade", "Trick: Parade", "Conjures a lookalike of the caster that draws enemies' attacks and spells", "作動式: 仮装行列", "術者そっくりの分身を作り, 敵の攻撃と魔法を引きつける");
        addSpellPiece(root, "trick_flight", "Trick: Flight", "Applies the Flight effect to the target. Creative flight is enabled by continuously consuming Psi.", "作動式: 飛行", "対象に飛行エフェクトを付与します. Psiを消費し続けることでクリエイティブ飛行を可能にします.");
        addSpellPiece(root, "trick_interact_block", "Trick: Block Interact", "Right-click with the off-hand item on the target block", "作動式: ブロック作用", "座標のブロックに, オフハンドのアイテムで右クリックの動作を行う");
        addSpellPiece(root, "trick_freeze_block", "Trick: Block Freeze", "Freeze the block at the target position into its next colder form", "作動式: ブロック凍結", "指定座標のブロックを次の凍結段階に変化させる");
        addSpellPiece(root, "trick_melt_block", "Trick: Block Melt", "Melt the block at the target position into its hotter form", "作動式: ブロック溶解", "指定座標のブロックを溶解して高温の状態に変化させる");
        addSpellPiece(root, "trick_pulsar", "Trick: Conjure Pulsar", "Conjures a redstone emitting block in the given position", "作動式: 魔法パルサーブロック生成", "指定位置にレッドストーン信号を発するブロックを生成します.");
        addSpellPiece(root, "trick_pulsar_sequence", "Trick: Conjure Pulsar Sequence", "Conjures a sequence of redstone emitting blocks", "作動式: 魔法パルサーブロック生成(列)", "レッドストーン信号を発するブロックを連続して生成します.");
        addSpellPiece(root, "trick_pulsar_light", "Trick: Conjure Pulsar Light", "Conjures a redstone emitting light in the given position", "作動式: 魔法パルサー光源生成", "指定位置にレッドストーン信号を発する光を生成します.");
        addSpellPiece(root, "trick_break_fortune", "Trick: Break Block (Fortune)", "Break a block with Fortune applied", "作動式: ブロック破壊(幸運)", "幸運付きでブロックを破壊する");
        addSpellPiece(root, "trick_break_silk", "Trick: Break Block (Silk Touch)", "Break a block with Silk Touch applied", "作動式: ブロック破壊(シルクタッチ)", "シルクタッチ付きでブロックを破壊する");
        addSpellPiece(root, "trick_store_entity", "Trick: Store Entity", "Store the entity's UUID string in the CAD memory", "作動式: エンティティ保存", "エンティティのUUIDをStringとしてCADメモリに保存する");
        addSpellPiece(root, "selector_stored_entity", "Selector: Stored Entity", "Retrieve entities from the UUID stored in the CAD memory", "取得子: 保存されたエンティティ", "CADのメモリに保存されたUUIDからエンティティを取得する");
        addSpellPiece(root, "trick_store_value", "Trick: Store Value", "Store a Plain Value (Number, Vector, String ... ) in CAD memory. Strings are stored up to 128 characters.", "作動式: 値を保存", "Plain Value (Number, Vector, String ... )をCADメモリに保存する. Stringは最大128文字まで保存します.");
        addSpellPiece(root, "selector_stored_value", "Selector: Stored Value", "Retrieve a Plain Value from CAD memory", "取得子: 保存された値", "CADメモリに保存されたPlain Valueを取得する");
        addSpellPiece(root, "selector_nearby_spellgram", "Selector: Nearby SpellGram Object", "Retrieve SpellGram objects around the specified position", "取得子: 近くの魔法式オブジェクト", "指定座標の周囲にある魔法式オブジェクトを取得する");
        addSpellPiece(root, "selector_autonomous_caster_position", "Selector: Autonomous Caster Position", "Gets the center of the Autonomous Caster casting the spell", "取得子: 詠唱機位置", "詠唱している自律詠唱機のブロックの中心を取得します.");
        // 解説書の項目名だけ短くする（英語の名前は見出しがページの枠からはみ出すため）。ピース名はそのまま。
        root.addProperty("psitweaks.spellpiece.selector_autonomous_caster_position.book_title",
                locale.equals("ja_jp") ? "取得子: 詠唱機位置" : "Selector: Auto. Caster Position");
        addSpellPiece(root, "trick_dispel", "Trick: Dispel", "Remove effects from target entity", "作動式: 解呪", "対象からエフェクトを除去する");
        addSpellPiece(root, "trick_dispel_beneficial", "Trick: Dispel Beneficial", "Remove beneficial effects from target entity", "作動式: 良性解呪", "対象から有益なエフェクトを除去する");
        addSpellPiece(root, "trick_dispel_non_beneficial", "Trick: Dispel Non Beneficial", "Remove non beneficial effects from target entity", "作動式: 悪性解呪", "対象から有益でないエフェクトを除去する");
        addSpellPiece(root, "trick_cocytus", "Trick: Cocytus",
                "Blows a blizzard forward that freezes the minds of the mobs it catches forever",
                "作動式: コキュートス", "前方に吹雪を放ち, 巻き込んだモブの精神を永久に凍結させる");
        addSpellPiece(root, "trick_supply_fe", "Trick: FE Charge", "Supplies FE to blocks. Direction can be specified.", "作動式: FE供給", "ブロックにFEを供給します. 方向の指定が可能です.");
        addSpellPiece(root, "trick_time_accelerate", "Trick: Time Accelerate", "Accelerates the time of the target block", "作動式: 時間加速", "対象のブロックの時を加速させます.");
        addSpellPiece(root, "trick_phonon_maser", "Trick: Phonon Maser", "Vibrates ultrasonic waves to emit heat rays", "作動式: フォノンメーザー", "超音波を振動させ熱線を放出する");
        addSpellPiece(root, "trick_meteor_line", "Trick: Meteor Line", "Covers the position in a night and rains streaks of light from its stars on the creatures inside", "作動式: 流星群", "指定位置を夜で覆い, 星から光条を降らせて中の生物を穿つ");
        addSpellPiece(root, "trick_supreme_infusion", "Trick: Supreme Infusion", "Infuse Echo Shards into Psionic Echoes", "作動式: 超位注入", "残響の欠片に注入してサイオニックエコーにします");
        addSpellPiece(root, "trick_molecular_divider", "Trick: Molecular Divider", "divide the living creatures within the area", "作動式: 分子ディバイダー", "三点で作られた平面で生物を切断する");
        addSpellPiece(root, "trick_aqua_cutter", "Trick: Aqua Cutter", "Launches a water blade projectile that damages on hit", "作動式: アクアカッター", "水刃の発射体を前方に放ち, 命中した対象にダメージを与える");
        addSpellPiece(root, "trick_dry_meteor", "Trick: Dry Meteor", "Launches dry ice forward and deals freezing damage on hit", "作動式: ドライミーティア", "ドライアイスを前方に射出し, 命中した対象に凍結属性ダメージを与える");
        addSpellPiece(root, "trick_blaze_ball", "Trick: Blaze Ball", "Launches a fireball forward that deals fire damage on hit", "作動式: ブレイズボール", "前方へ火の玉を発射し, 命中した対象へ炎属性ダメージを与える");
        addSpellPiece(root, "trick_radiation_injection", "Trick: Radiation Injection", "Applies radiation exposure to the target", "作動式: 放射線注入", "対象を被ばくさせる");
        addSpellPiece(root, "trick_radiation_filter", "Trick: Radiation Filter", "Applies a radiation filter effect to the target", "作動式: 放射線フィルタ", "対象に放射線防護効果を付与する");
        addSpellPiece(root, "trick_cure_radiation", "Trick: Cure Radiation", "Removes radiation exposure from the target", "作動式: 放射線除去", "対象の被ばく量を除去する");
        addSpellPiece(root, "trick_guillotine", "Trick: Guillotine", "Deals a heavy slash to the target and drops a head on kill", "作動式: ギロチン", "対象に強力な斬撃ダメージを与え, 討伐時に頭をドロップさせる");
        addSpellPiece(root, "trick_chain_lightning", "Trick: Chain Lightning", "Discharges lightning from the focal point in the direction; it strikes the nearest hostile creature and branches to nearby ones", "作動式: 連鎖電光", "焦点から方向へ雷撃を放ち, 最も近い敵対的な生物に当てて近くの敵対的な生物へ枝分かれさせる");
        addSpellPiece(root, "trick_mine_genesis", "Trick: Mine Genesis", "Shakes the ground so a shockwave spreads out and throws up whatever stands on it", "作動式: 地雷原", "地面を振動させて衝撃波を広げ, 地上の生物を打ち上げる");
        addSpellPiece(root, "trick_burst", "Trick: Burst", "Vaporizes the liquid inside the target so it bursts from within", "作動式: 爆裂", "対象の体内の液体を気化させ, 内側から破裂させる");
        addSpellPiece(root, "trick_psi_bullet", "Trick: Psi Bullet",
                "Fires a bullet of psions that strikes the first creature or SpellGram object it hits",
                "作動式: サイ・ブリット", "想子の弾を撃ち出し, 最初に当たった生物か魔法式オブジェクトに当てる");
        addSpellPiece(root, "trick_gram_demolition", "Trick: Gram Demolition",
                "Blasts the SpellGram objects in front of you with psions, wearing their integrity away all at once",
                "作動式: 術式解体", "前方の魔法式オブジェクトに想子をぶつけ, 耐久値をまとめて削る");
        addSpellPiece(root, "trick_air_bullet", "Trick: Air Bullet", "Fires a bullet of compressed air that strikes the first creature it hits", "作動式: エア・ブリット", "圧縮した空気の弾を撃ち出し, 最初に直撃した生物に当てる");
        addSpellPiece(root, "trick_dancing_blades", "Trick: Dancing Blades", "Deploys the swords in your Blade Pouch as blades that strike nearby hostile creatures", "作動式: ダンシングブレイズ", "ブレードポーチの剣を刃として展開し, 近くの敵対的な生物を斬りつける");
        addSpellPiece(root, "trick_pseudo_meteor", "Trick: Pseudo Meteor", "Calls pseudo meteors down on the position and the hostile creatures around it", "作動式: 疑似隕石", "指定位置とその周りの敵対的な生物に疑似隕石を降らせる");
        addSpellPiece(root, "trick_micro_black_hole", "Trick: Micro Black Hole", "Creates a tiny black hole that pulls creatures in and swings them around, then collapses", "作動式: マイクロブラックホール", "生物を引き寄せて振り回す極小のブラックホールを生み出し, 最後に崩壊させる");
        addSpellPiece(root, "trick_active_air_mine", "Trick: Active Air Mine", "Deals magic damage to entities inside a spherical area around the target position", "作動式: 能動空中機雷", "指定座標を中心とした範囲内の生物にダメージを与える");
        addSpellPiece(root, "trick_flare_circle", "Trick: Fire Circle", "Places a fire Resident SpellGram circle that repeatedly deals fire damage around it", "作動式: ファイアサークル", "炎の常駐魔法式を設置して内部の生物に継続的な炎ダメージを与える");
        addSpellPiece(root, "trick_ice_circle", "Trick: Ice Circle", "Places an ice Resident SpellGram circle that repeatedly deals freeze damage around it and freezes the water in it", "作動式: アイスサークル", "氷の常駐魔法式を設置して内部の生物に継続的な凍結ダメージを与え, 水面を薄氷にする");
        addSpellPiece(root, "trick_die_flex", "Trick: Flexible Die", "Stops the spell when given a number whose absolute value is below 1, and refunds the Psi of the pieces not yet run. In spells cast every tick, the Psi display may briefly desync.", "作動式: 柔軟停止", "絶対値が1未満の数値を受け取ると停止し, 未実行分のPsiを返却します. 毎tick詠唱では, Psi表示が一時的にずれることがあります.");
        addSpellPiece(root, "trick_jump", "Trick: Jump", "If the target number is unset or its absolute value is less than 1, jumps forward to the next Jump Anchor with the same constant label.", "作動式: ジャンプ", "対象数値が未入力, または絶対値が1未満なら, 同じ定数ラベルを持つ次のジャンプアンカーまで前方にジャンプします.");
        addSpellPiece(root, "trick_jump_flex", "Trick: Flexible Jump", "Behaves like Trick: Jump and refunds the Psi of the pieces it skips.", "作動式: 柔軟ジャンプ", "作動式: ジャンプと同じ動作をし, 飛ばしたピースのPsiを返却します.");
        addSpellPiece(root, "trick_switch", "Trick: Switch", "Jumps forward to the next Jump Anchor with the same constant label as the input String. If no matching anchor exists, execution continues.", "作動式: スイッチ", "入力Stringと同じ定数ラベルを持つ次のジャンプアンカーまで前方にジャンプします. 一致するアンカーがない場合はそのまま次へ進みます.");
        addSpellPiece(root, "trick_switch_flex", "Trick: Flexible Switch", "Behaves like Trick: Switch and refunds the Psi of the pieces it skips.", "作動式: 柔軟スイッチ", "作動式: スイッチと同じ動作をし, 飛ばしたピースのPsiを返却します.");
        addSpellPiece(root, "jump_anchor", "Jump Anchor", "A no-op marker used as the destination for Trick: Jump and Trick: Switch. It can take an optional constant label.", "ジャンプアンカー", "作動式: ジャンプと作動式: スイッチの到達点になる, 何もしない目印です. 任意の定数ラベルを指定できます.");
        addSpellPiece(root, "safety", "Safety", "If the caster's Psi plus the CAD's stored Psi can't cover the cost, the spell fails without using any Psi.", "安全装置", "術者とCADのPsiを合わせても消費量に足りないとき, Psiを使わずに不発にします.");
        addSpellPiece(root, "trick_material_mutation", "Trick: Material Mutation", "Acts on a specific block, alters its material structure, and transmutes it into a different substance.", "作動式: 物質変成", "特定のブロックに作用して物質構造を改変し異なる物質に変成させる");
        addSpellPiece(root, "trick_physical_propulsion", "Trick: Physical Propulsion", "Raycasts from the specified position in the Ray direction; if it hits a Simulated Contraption, applies propulsion to it.", "作動式: 物理推進", "指定位置からRay方向へレイキャストし, Simulated Contraption に命中したならば推進力を与える");
        addSpellPiece(root, "trick_pull_item", "Trick: Pull Item", "Moves items from the specified face of the block at Position into the caster's inventory, up to one stack or the count given by Max.", "作動式: アイテム引き出し", "位置のブロックの指定面から, 術者のインベントリへアイテムを移します. 上限は1スタック, または「最大」で指定した個数です.");
        addSpellPiece(root, "trick_send_item", "Trick: Send Item", "Moves items from the caster's inventory into the specified face of the block at Position, up to one stack or the count given by Max. CADs are skipped.", "作動式: アイテム転送", "術者のインベントリから, 位置のブロックの指定面へアイテムを移します. 上限は1スタック, または「最大」で指定した個数です. CADは移しません.");
        addSpellPiece(root, "operator_tan", "Operator: Tangent", "tan(A)", "演算子: タンジェント", "tan(A)");
        addSpellPiece(root, "operator_atan", "Operator: Arc Tangent", "atan(A)", "演算子: アークタンジェント", "atan(A)");
        addSpellPiece(root, "operator_sinh", "Operator: Hyperbolic Sine", "sinh(A)", "演算子: ハイパボリックサイン", "sinh(A)");
        addSpellPiece(root, "operator_cosh", "Operator: Hyperbolic Cosine", "cosh(A)", "演算子: ハイパボリックコサイン", "cosh(A)");
        addSpellPiece(root, "operator_tanh", "Operator: Hyperbolic Tangent", "tanh(A)", "演算子: ハイパボリックタンジェント", "tanh(A)");
        addSpellPiece(root, "operator_greater_than", "Operator: Greater Than", "Outputs 1 if Value 1 is greater than Value 2, otherwise 0.", "演算子: ～より大きい", "値1が値2より大きいなら1, そうでなければ0を出力します.");
        addSpellPiece(root, "operator_greater_than_or_equal", "Operator: Greater Than or Equal", "Outputs 1 if Value 1 is greater than or equal to Value 2, otherwise 0.", "演算子: ～以上", "値1が値2以上なら1, そうでなければ0を出力します.");
        addSpellPiece(root, "operator_equal", "Operator: Equal", "Outputs 1 if Value 1 equals Value 2, otherwise 0.", "演算子: 等しい", "値1と値2が等しいなら1, そうでなければ0を出力します.");
        addSpellPiece(root, "constant_string", "Constant: String", "Outputs the entered string. Limited to 1024 characters.", "定数子: 文字列", "入力した文字列をそのまま出力します. 最大1024文字です.");
        addSpellPiece(root, "operator_format_string", "Operator: Format String", "Returns the entered text with Values 1 to 3 filled into {1}, {2}, and {3}.", "演算子: フォーマット文字列", "入力した文字列の {1}〜{3} に値1〜3を埋め込んで返します.");
        addSpellPiece(root, "operator_from_string", "Operator: From String", "Returns the input String as-is, or converts it into a Number or Vector for the selected mode.", "演算子: 文字列から変換", "選択中のモードに応じて String をそのまま返すか, Number または Vector に変換します.");
        addSpellPiece(root, "operator_list_from_string_list", "Operator: From String List", "Returns the input String List as-is, or converts it into a Number List or Vector List for the selected mode.", "演算子: 文字列リストから変換", "選択中のモードに応じて String List をそのまま返すか, Number List または Vector List に変換します.");
        addSpellPiece(root, "operator_number_list_to_vector", "Operator: Number List to Vector", "Converts a Number List with exactly three elements into a Vector.", "演算子: 数値リスト→ベクトル", "要素数3の Number List を Vector に変換します.");
        addSpellPiece(root, "operator_vector_to_number_list", "Operator: Vector to Number List", "Converts a Vector into a three-element Number List.", "演算子: ベクトル→数値リスト", "Vector を要素数3の Number List に変換します.");
        addSpellPiece(root, "operator_to_string", "Operator: To String", "Converts an Any input into a debug-display String.", "演算子: 文字列へ変換", "Any 入力をデバッグ表示相当の String に変換します.");
        addSpellPiece(root, "operator_list_to_string_list", "Operator: To String List", "Converts an Any input into a debug-display String List.", "演算子: 文字列リストへ変換", "Any 入力をデバッグ表示相当の String List に変換します.");
        addSpellPiece(root, "operator_get_id", "Operator: Registry ID", "Outputs the registry ID of a Contextual Value.", "演算子: レジストリID", "Contextual Value のレジストリIDを String として出力します.");
        addSpellPiece(root, "operator_get_id_list", "Operator: Registry ID List", "Outputs registry IDs from a Contextual Value List as a String List.", "演算子: レジストリIDリスト", "Contextual Value List の各要素のレジストリIDを String List として出力します.");
        addSpellPiece(root, "selector_display_name", "Selector: Display Name", "Outputs the localized display name of a Contextual Value.", "取得子: 表示名", "Contextual Value の表示名を術者の言語に応じた String として出力します.");
        addSpellPiece(root, "selector_display_name_list", "Selector: Display Name List", "Outputs localized display names from a Contextual Value List as a String List.", "取得子: 表示名リスト", "Contextual Value List の各要素の表示名を術者の言語に応じた String List として出力します.");
        addSpellPiece(root, "selector_effect_list", "Selector: Effect List", "Outputs the target Entity's active Mob Effect registry IDs as a String List.", "取得子: エフェクトリスト", "対象 Entity に付与されているエフェクトのレジストリ ID を String List として出力します.");
        addSpellPiece(root, "selector_block", "Selector: Block", "Returns the block at the given position as a Block value.", "取得子: ブロック", "指定座標にあるブロックを Block 型として返します.");
        addSpellPiece(root, "selector_block_list", "Selector: Block List", "Gets the blocks at the positions in a Vector List as a Block List.", "取得子: ブロックリスト", "Vector List の座標にあるブロックを Block List として取得します.");
        addSpellPiece(root, "operator_block_state", "Operator: Block State", "Outputs the saved block state of a Block value as a String. Example: minecraft:oak_stairs[facing=north]", "演算子: ブロックステート", "Block 値に保存されたブロックステートを文字列として出力します. 例: minecraft:oak_stairs[facing=north]");
        addSpellPiece(root, "operator_block_state_value", "Operator: Block State Value", "Outputs one saved block state property value from a Block value.", "演算子: ブロックステート値", "Block 値に保存されたブロックステートから指定したプロパティ値を String として出力します.");
        addSpellPiece(root, "operator_block_state_entries", "Operator: Block State Entries", "Outputs the saved block state properties of a Block value as a String List.", "演算子: ブロックステート項目", "Block 値に保存されたブロックステートのプロパティ項目を String List として出力します.");
        addSpellPiece(root, "operator_tag_list", "Operator: Tag List", "Outputs the registry tags of the target Contextual Value (Entity, Item, Block, etc.) as a String List.", "演算子: タグリスト", "対象の Contextual Value(Entity, Item, Block など)のレジストリタグを String List として出力します.");
        addSpellPiece(root, "operator_block_position", "Operator: Block Position", "Outputs the saved position of a Block value as a plain Vector.", "演算子: ブロック座標", "Block 値に保存された座標を通常の Vector として出力します.");
        addSpellPiece(root, "operator_block_position_list", "Operator: Block Position List", "Gets the positions of the blocks in a Block List as a Vector List.", "演算子: ブロック位置リスト", "Block List のブロックの座標を Vector List として取得します.");
        addSpellPiece(root, "selector_online_players", "Selector: Online Players", "Outputs the names of online players in the world as a String List.", "取得子: オンラインプレイヤー", "ワールド内のオンラインプレイヤー名を String List として取得します.");
        addSpellPiece(root, "selector_held_item", "Selector: Main-Hand Item", "Gets the target entity's main-hand item. For a dropped item, gets its stack.", "取得子: 手持ちアイテム", "対象エンティティのメインハンドのアイテムを取得します. ドロップアイテムの場合はそのスタックを取得します.");
        addSpellPiece(root, "selector_selected_slot_item", "Selector: Selected Slot Item", "Gets the item currently selected by this spell.", "取得子: 選択スロットアイテム", "この術式で選択しているアイテムを取得します.");
        addSpellPiece(root, "selector_entity_slot_item", "Selector: Entity Slot Item", "Gets the item in the target entity's zero-based internal inventory slot. Outputs an empty Item if the slot cannot be read.", "取得子: エンティティスロットアイテム", "対象 Entity の0始まり内部インベントリスロットにある Item を取得します. スロットを読めない場合は空の Item を出力します.");
        addSpellPiece(root, "selector_internal_slot_item", "Selector: Internal Slot Item", "Gets the item in a block's zero-based internal inventory slot. Outputs an empty Item if the slot cannot be read.", "取得子: 内部スロットアイテム", "指定ブロックの0始まり内部インベントリスロットにある Item を取得します. スロットを読めない場合は空の Item を出力します.");
        addSpellPiece(root, "operator_item_count", "Operator: Item Count", "Outputs the stack count of an Item value.", "演算子: アイテム個数", "Item 値のスタック個数を Number として出力します. 空の Item は0を出力します.");
        addSpellPiece(root, "operator_item_slot", "Operator: Item Slot", "Outputs the internal inventory slot number saved on an Item value. Outputs -1 if no slot number is available.", "演算子: アイテムスロット", "Item 値に保存された内部インベントリのスロット番号を Number として出力します. スロット番号を取得できない場合は-1です.");
        addSpellPiece(root, "operator_item_total_count", "Operator: Item Total Count", "Outputs the total count of matching items in an Item List.", "演算子: アイテム合計個数", "Item List 内の指定 ID と一致するアイテムの合計個数を Number として出力します.");
        addSpellPiece(root, "selector_held_items", "Selector: Carried Items", "Outputs the target entity's carried items as an Item List.", "取得子: 所持アイテム", "対象 Entity の所持アイテムを Item List として取得します.");
        addSpellPiece(root, "selector_internal_items", "Selector: Internal Items", "Outputs the target block's internal inventory as an Item List.", "取得子: 内部アイテム", "対象ブロックの内部インベントリを Item List として取得します.");
        addSpellPiece(root, "selector_indexed_element", "Selector: Indexed Element (PT)", "Outputs the element at a zero-based index from the selected List mode. Negative indexes count back from the end.", "取得子: インデックス要素 (PT)", "選択中の List モードから0始まりのインデックスにある要素を取得します. 負のインデックスは末尾から数えます.");
        addSpellPiece(root, "selector_nbt", "Selector: NBT", "Outputs the entries of the compound or list at the NBT Path (the whole NBT when unconnected) as a String List.", "取得子: NBT", "NBTパスにある複合タグかリストの中身を String List で出力します（未接続なら全体）.");
        addSpellPiece(root, "selector_nbt_keys", "Selector: NBT Keys", "Outputs the keys of the compound at the NBT Path, or the indexes of a list, as a String List.", "取得子: NBTキー", "NBTパスにある複合タグのキー, またはリストの番号を String List で出力します.");
        addSpellPiece(root, "selector_nbt_value", "Selector: NBT Value", "Outputs the value at the NBT Path as a String or a Number, chosen by the mode. Outputs the Fallback when nothing is found.", "取得子: NBT値", "NBTパスにある値を, モードに応じて文字列か数値で出力します. 見つからないときは既定値を出力します.");
        addSpellPiece(root, "selector_nbt_exists", "Selector: NBT Exists", "Outputs 1 if the NBT Path reaches anything in the target's NBT, otherwise 0.", "取得子: NBT存在", "対象のNBTにNBTパスで指す値があれば1, なければ0を出力します.");
        addSpellPiece(root, "operator_string_partial_match", "Operator: Partial Match", "Outputs 1 if part of String 1 matches the pattern in String 2, otherwise 0. * matches any text, ? one character, and [abc] one of a, b, or c.", "演算子: 部分一致", "文字列1の中に文字列2のパターンと一致する部分があれば1, なければ0を出力します. * は任意の文字列, ? は任意の1文字, [abc] は a・b・c のいずれかに一致します.");
        addSpellPiece(root, "operator_string_starts_with", "Operator: String Starts With", "Outputs 1 if String 1 starts with String 2, otherwise outputs 0.", "演算子: 先頭一致", "文字列1が文字列2で始まるなら1, そうでなければ0を出力します.");
        addSpellPiece(root, "operator_string_ends_with", "Operator: String Ends With", "Outputs 1 if String 1 ends with String 2, otherwise outputs 0.", "演算子: 末尾一致", "文字列1が文字列2で終わるなら1, そうでなければ0を出力します.");
        addSpellPiece(root, "operator_string_concat", "Operator: String Concat", "String 1 + String 2 + String 3", "演算子: 文字列結合", "文字列1 + 文字列2 + 文字列3");
        addSpellPiece(root, "operator_string_split", "Operator: String Split", "Splits String 1 with String 2 as a literal delimiter and outputs a String List.", "演算子: 文字列分割", "文字列1を文字列2のリテラル区切り文字で分割し, String List を出力します.");
        addSpellPiece(root, "operator_string_slice", "Operator: String Slice", "Returns the part of the String from Number A to Number B.", "演算子: 文字列スライス", "文字列を 数値A:数値B の範囲で切り出します.");
        addSpellPiece(root, "operator_string_length", "Operator: String Length", "Returns the number of characters in the input String as a Number.", "演算子: 文字列長", "入力した文字列の文字数を Number として返します.");
        addSpellPiece(root, "operator_string_replace", "Operator: String Replace", "Replaces parts of String 1 matching String 2 with String 3.", "演算子: 文字列置換", "入力した文字列1のうち, 文字列2と一致する部分を, 指定した文字列3へ置換する.");
        addSpellPiece(root, "operator_string_trim", "Operator: String Trim", "Removes leading and trailing whitespace from the input String.", "演算子: 文字列トリム", "入力した文字列の前後の空白を取り除きます.");
        addSpellPiece(root, "operator_string_list_join", "Operator: String List Join", "Joins the input String List with String 2 and outputs a String.", "演算子: 文字列リスト結合", "入力リストを文字列2で結合し, String を出力します.");
        addSpellPiece(root, "operator_player_name", "Operator: Player Name", "Outputs the player name if the Entity is a player, otherwise outputs an empty string.", "演算子: プレイヤーネーム", "Entityがプレイヤーならプレイヤー名を, そうでなければ空文字列を出力します.");
        addSpellPiece(root, "operator_list_search", "Operator: List Keep Matches", "Keeps only the List elements whose ID or string form matches the pattern. The whole string must match; * ? [abc] work. Start with # to compare tags.", "演算子: リスト一致抽出", "要素のIDまたは文字列が, パターンと一致するものだけを残します. 全体一致で, * ? [abc] を使えます. # で始めると, 要素のタグで比べます.");
        addSpellPiece(root, "operator_list_search_exclude", "Operator: List Remove Matches", "Removes the List elements whose ID or string form matches the pattern. The whole string must match; * ? [abc] work. Start with # to compare tags.", "演算子: リスト一致除外", "要素のIDまたは文字列が, パターンと一致するものを除きます. 全体一致で, * ? [abc] を使えます. # で始めると, 要素のタグで比べます.");
        addSpellPiece(root, "operator_list_name_search", "Operator: List Keep Name Matches", "Keeps only the List elements whose display name matches the pattern. The whole name must match; * ? [abc] work.", "演算子: 表示名一致抽出", "要素の表示名が, パターンと一致するものだけを残します. 全体一致で, * ? [abc] を使えます.");
        addSpellPiece(root, "operator_list_name_search_exclude", "Operator: List Remove Name Matches", "Removes the List elements whose display name matches the pattern. The whole name must match; * ? [abc] work.", "演算子: 表示名一致除外", "要素の表示名が, パターンと一致するものを除きます. 全体一致で, * ? [abc] を使えます.");
        addSpellPiece(root, "operator_list_nbt_search", "Operator: List Keep NBT Matches", "Keeps the List elements whose NBT value at the NBT Path matches the pattern. The whole value must match; * ? [abc] work.", "演算子: NBT一致抽出", "NBTパスの値がパターンと一致する要素だけを残します. 全体一致で, * ? [abc] を使えます.");
        addSpellPiece(root, "operator_list_nbt_search_exclude", "Operator: List Remove NBT Matches", "Removes the List elements whose NBT value at the NBT Path matches the pattern. The whole value must match; * ? [abc] work.", "演算子: NBT一致除外", "NBTパスの値がパターンと一致する要素を除きます. 全体一致で, * ? [abc] を使えます.");
        addSpellPiece(root, "operator_list_nbt_range", "Operator: List Keep NBT In Range", "Keeps the List elements whose number at the NBT Path is between Min and Max. An unconnected bound is open.", "演算子: NBT範囲抽出", "NBTパスの数値が最小以上・最大以下の要素だけを残します. 未接続の側は制限しません.");
        addSpellPiece(root, "operator_list_nbt_range_outside", "Operator: List Keep NBT Out Of Range", "Keeps the List elements whose number at the NBT Path is below Min or above Max.", "演算子: NBT範囲外抽出", "NBTパスの数値が最小より小さいか, 最大より大きい要素だけを残します.");
        addSpellPiece(root, "operator_random_element", "Operator: Random Element", "Outputs one random element from the input List.", "演算子: ランダム要素", "入力した List からランダムに1要素を出力します.");
        addSpellPiece(root, "operator_list_distance_keep", "Operator: List Keep By Distance", "Keeps as many elements as the Number, closest to the position first. A negative Number keeps the farthest ones.", "演算子: 距離順抽出", "位置から近い順に, 数値の分だけ要素を残します. 数値が負なら遠い順に残します.");
        addSpellPiece(root, "operator_list_distance_element", "Operator: Distance-Ranked Element", "Outputs the element at the index when the List is sorted closest first. 0 is the closest; negative numbers count from the farthest.", "演算子: 距離順要素", "位置から近い順に並べたときの, 指定番号の要素を出力します. 0が一番近く, 負の数は遠い方から数えます.");
        addSpellPiece(root, "selector_nbt_number_list", "Selector: NBT Number List", "Outputs one number per List element: the number at the NBT Path, or the Fallback (0 when unconnected).", "取得子: NBT数値リスト", "リストの要素ごとに, NBTパスにある数値（なければ既定値, 未接続なら0）を並べた Number List を出力します.");
        addSpellPiece(root, "operator_list_value_range", "Operator: List Keep In Value Range", "Keeps the List elements whose value in Values is between Min and Max. An unconnected bound is open.", "演算子: 基準値範囲抽出", "基準値が最小以上・最大以下の要素だけを残します. 未接続の側は制限しません.");
        addSpellPiece(root, "operator_list_value_keep", "Operator: List Keep By Value", "Keeps the Number elements with the smallest values. A negative Number keeps the largest ones.", "演算子: 基準値順抽出", "基準値の小さい順に, 数値の分だけ要素を残します. 数値が負なら大きい順に残します.");
        addSpellPiece(root, "operator_list_value_element", "Operator: Value-Ranked Element", "Outputs the element at the index when the List is sorted by value, smallest first. 0 is the smallest; negative numbers count from the largest.", "演算子: 基準値順要素", "基準値の小さい順に並べたときの, 指定番号の要素を出力します. 0が一番小さく, 負の数は大きい方から数えます.");
        addSpellPiece(root, "operator_list_add", "Operator: Add To List (PT)", "Adds up to three elements to the input List.", "リストへ追加 (PT)", "入力した List に最大3つの要素を追加します.");
        addSpellPiece(root, "operator_list_remove", "Operator: Remove From List (PT)", "Removes up to two elements from the input List.", "リストから削除 (PT)", "入力した List から最大2つの要素を削除します.");
        addSpellPiece(root, "operator_list_remove_indices", "Operator: Remove Indexed Elements", "Removes the elements at Number A and Number B from the input List.", "演算子: インデックス要素削除", "入力した List から数値Aと数値Bに対応するインデックスの要素を削除します.");
        addSpellPiece(root, "operator_list_insert", "Operator: List Insert", "Inserts the input element at the specified index in the input List.", "演算子: リストに挿入", "入力した List の指定インデックスへ要素を挿入します.");
        addSpellPiece(root, "operator_list_slice", "Operator: List Slice", "Returns the part of the List from Number A to Number B.", "演算子: リストスライス", "List を 数値A:数値B の範囲で切り出します.");
        addSpellPiece(root, "operator_list_size", "Operator: List Size (PT)", "Outputs the number of elements in the input List.", "演算子: リストサイズ (PT)", "入力した任意の List の要素数を出力します.");
        addSpellPiece(root, "operator_list_exclusion", "Operator: List Exclusion (PT)", "Removes the elements in List 2 from the input List 1.", "演算子: リスト除外 (PT)", "入力したリスト1からリスト2に含まれる要素を除外します.");
        addSpellPiece(root, "operator_list_intersection", "Operator: List Intersection (PT)", "Outputs the elements shared by the two input Lists.", "演算子: リスト共通部分 (PT)", "入力した2つの List に共通する要素を出力します.");
        addSpellPiece(root, "operator_list_concatenation", "Operator: List Concatenation (PT)", "Combines the two input Lists.", "演算子: リスト結合 (PT)", "入力した2つの List を結合します.");
        addSpellPiece(root, "operator_matrix_add", "Operator: Matrix Add", "Adds two or three matrices of the same size.", "演算子: 行列加算", "同じサイズの行列を2つまたは3つ加算します.");
        addSpellPiece(root, "operator_matrix_subtract", "Operator: Matrix Subtract", "Subtracts two or three matrices of the same size.", "演算子: 行列減算", "同じサイズの行列を2つまたは3つ減算します.");
        addSpellPiece(root, "operator_matrix_multiply", "Operator: Matrix Multiply", "Multiplies matrices with compatible dimensions.", "演算子: 行列積", "次元が整合する行列を乗算します.");
        addSpellPiece(root, "operator_matrix_scalar_multiply", "Operator: Matrix Scalar Multiply", "Multiplies a matrix by a number.", "演算子: 行列スカラー倍", "行列を数値でスカラー倍します.");
        addSpellPiece(root, "operator_matrix_transpose", "Operator: Matrix Transpose", "Transposes a matrix.", "演算子: 行列転置", "行列を転置します.");
        addSpellPiece(root, "operator_matrix_determinant", "Operator: Matrix Determinant", "Computes the determinant of a square matrix.", "演算子: 行列式", "正方行列の行列式を求めます.");
        addSpellPiece(root, "operator_matrix_inverse", "Operator: Matrix Inverse", "Computes the inverse of a square matrix.", "演算子: 逆行列", "正方行列の逆行列を求めます.");
        addSpellPiece(root, "operator_matrix_extract_row", "Operator: Matrix Extract Row", "Returns the specified row as a Number List.", "演算子: 行抽出", "指定した行を Number List として返します.");
        addSpellPiece(root, "operator_matrix_extract_column", "Operator: Matrix Extract Column", "Returns the specified column as a Number List.", "演算子: 列抽出", "指定した列を Number List として返します.");
        addSpellPiece(root, "operator_matrix_element", "Operator: Matrix Element", "Returns the element at (row, column).", "演算子: 行列要素", "(行, 列) の要素を返します.");
        addSpellPiece(root, "operator_matrix_row_count", "Operator: Matrix Row Count", "Returns the number of rows.", "演算子: 行数", "行列の行数を返します.");
        addSpellPiece(root, "operator_matrix_column_count", "Operator: Matrix Column Count", "Returns the number of columns.", "演算子: 列数", "行列の列数を返します.");
        addSpellPiece(root, "operator_matrix_multiply_vector", "Operator: Matrix Multiply Vector", "Multiplies a matrix by a column vector or Number List.", "演算子: 行列×ベクトル", "行列に列ベクトルまたは Number List を乗算します.");
        addSpellPiece(root, "operator_matrix_column_from_list", "Operator: Column Matrix From List", "Converts a Number List or Vector into a column matrix.", "演算子: 数値リスト→列行列", "Number List または Vector を列行列に変換します.");
        addSpellPiece(root, "operator_matrix_flatten", "Operator: Matrix Flatten", "Flattens a matrix into a Number List in row-major order.", "演算子: 行列平坦化", "行列を行優先の Number List に平坦化します.");
        addSpellPiece(root, "operator_matrix_identity", "Operator: Identity Matrix", "Creates an identity matrix of the given size.", "演算子: 単位行列", "指定次数の単位行列を作ります.");
        addSpellPiece(root, "operator_matrix_zero", "Operator: Zero Matrix", "Creates a zero matrix with the given dimensions.", "演算子: ゼロ行列", "指定サイズのゼロ行列を作ります.");
        addSpellPiece(root, "operator_matrix_diagonal", "Operator: Diagonal Matrix", "Creates a diagonal matrix from a Number List or Vector.", "演算子: 対角行列", "Number List または Vector から対角行列を作ります.");
        addSpellPiece(root, "operator_matrix_replace_column", "Operator: Matrix Replace Column", "Replaces a column, expanding the matrix with zeros as needed.", "演算子: 列置換", "列を置換し, 必要に応じてゼロで拡張します.");
        addSpellPiece(root, "operator_matrix_replace_row", "Operator: Matrix Replace Row", "Replaces a row, expanding the matrix with zeros as needed.", "演算子: 行置換", "行を置換し, 必要に応じてゼロで拡張します.");
        addSpellPiece(root, "operator_matrix_replace_element", "Operator: Matrix Replace Element", "Replaces the element at the zero-based [row, column] indices with the input number.", "演算子: 行列要素置換", "0始まりの [行, 列] で指定した成分を入力数値で置換します.");
        addSpellPiece(root, "operator_matrix_delete_row", "Operator: Matrix Delete Row", "Deletes the specified row from the matrix and shifts the remaining rows up.", "演算子: 行削除", "指定した行を行列から削除し, 残りの行を詰めます.");
        addSpellPiece(root, "operator_matrix_delete_column", "Operator: Matrix Delete Column", "Deletes the specified column from the matrix and shifts the remaining columns left.", "演算子: 列削除", "指定した列を行列から削除し, 残りの列を詰めます.");
        addSpellPiece(root, "operator_matrix_cuboid_region", "Operator: Matrix Cuboid Region", "Builds a 4x4 matrix for an axis-aligned cuboid from a center position and size. Whole-number sizes act as block counts on each axis. Number mode makes a cube from one size.", "演算子: 直方体領域", "中心位置とサイズから軸平行直方体を表す 4×4 行列を生成します. 整数サイズは各軸でブロック数として扱われます. Number モードでは1つの数値で立方体を作ります.");
        addSpellPiece(root, "operator_matrix_cuboid_corners", "Operator: Matrix Cuboid From Corners",
                "Builds a 4x4 matrix for the axis-aligned cuboid with two positions as opposite corners. Block mode includes both corner blocks.",
                "演算子: 2点直方体領域",
                "2つの位置を向かい合う角とする軸平行直方体の 4×4 行列を作ります. Block モードでは両方の角のブロックを含みます.");
        addSpellPiece(root, "operator_region_vector_list", "Operator: Region Vector List", "Returns block position vectors inside a matrix-defined region.", "演算子: 領域ベクトルリスト化", "行列で定義された領域内のブロック座標を Vector List として返します.");
        addSpellPiece(root, "operator_inside_region", "Operator: Inside Region", "Keeps only the elements of an Entity, Vector or Block List that are inside the matrix region. Choose the list type in the mode menu.", "演算子: 領域内", "Entity・Vector・Block の List のうち, 行列の領域内にあるものだけを残します. リストの種類はモードで選びます.");
        addSpellPiece(root, "operator_outside_region", "Operator: Outside Region", "Keeps only the elements of an Entity, Vector or Block List that are outside the matrix region. Choose the list type in the mode menu.", "演算子: 領域外", "Entity・Vector・Block の List のうち, 行列の領域外にあるものだけを残します. リストの種類はモードで選びます.");
        addSpellPiece(root, "operator_alive", "Operator: Alive", "Keeps only LivingEntity instances that are currently alive in the input Entity List.", "演算子: 生存", "入力した Entity List のうち, 生存中の LivingEntity のみを残します.");
        addSpellPiece(root, "operator_matrix_transform_vector", "Operator: Matrix Transform Vector", "Transforms a vector, or every vector of a Vector List, with a 3x3 or 4x4 matrix.", "演算子: 行列でベクトル変換", "3×3 または 4×4 行列でベクトル, または Vector List の各ベクトルを変換します.");
        addSpellPiece(root, "operator_matrix_linear_part", "Operator: Matrix Linear Part", "Extracts the top-left 3x3 submatrix.", "演算子: 行列線形部分", "左上の 3×3 部分行列を取り出します.");
        addSpellPiece(root, "operator_weak_raycast", "Operator: Vector Weak Raycast", "Raycasts from Position in the Ray direction and returns the coordinates of the hit block or fluid.", "演算子: ベクトルレイキャスト (弱)", "位置から Ray 方向へレイキャストし, 命中したブロックまたは流体の座標を返します.");
        addSpellPiece(root, "operator_weak_raycast_axis", "Operator: Vector Axis Weak Raycast", "Raycasts from Position in the Ray direction and returns the normal direction of the face of the first block or fluid hit.", "演算子: ベクトルレイキャスト方向 (弱)", "位置から Ray 方向へレイキャストし, 最初に命中したブロックまたは流体の面の法線方向を返します.");
        addSpellPiece(root, "operator_strong_raycast", "Operator: Vector Strong Raycast", "Raycasts from Position in the Ray direction, ignores blocks without a collision shape, and returns the coordinates of the hit block.", "演算子: ベクトルレイキャスト (強)", "位置から Ray 方向へレイキャストし, 当たり判定のないブロックを無視して命中したブロックの座標を返します.");
        addSpellPiece(root, "operator_strong_raycast_axis", "Operator: Vector Axis Strong Raycast", "Raycasts from Position in the Ray direction, ignores blocks without a collision shape, and returns the normal direction of the face of the hit block.", "演算子: ベクトルレイキャスト方向 (強)", "位置から Ray 方向へレイキャストし, 当たり判定のないブロックを無視して命中したブロックの面の法線方向を返します.");
        addSpellPiece(root, "macro_caster_raycast", "Macro: Caster Raycast", "Raycasts from the caster's position in the look direction and returns the coordinates of the hit block.", "マクロ: 術者レイキャスト", "術者の位置から視線方向へレイキャストし, 命中したブロックの座標を返します.");
        addSpellPiece(root, "macro_caster_raycast_axis", "Macro: Axis Caster Raycast", "Raycasts from the caster's position in the look direction and returns the normal direction of the hit face.", "マクロ: 術者レイキャスト方向", "術者の位置から視線方向へレイキャストし, 命中した面の法線方向を返します.");
        addSpellPiece(root, "macro_caster_weak_raycast", "Macro: Weak Caster Raycast", "Like Macro: Caster Raycast, but it also hits fluids such as water and lava.", "マクロ: 術者レイキャスト (弱)", "マクロ: 術者レイキャストと同じですが, 水や溶岩などの流体にも命中します.");
        addSpellPiece(root, "macro_caster_weak_raycast_axis", "Macro: Weak Axis Caster Raycast", "Like Macro: Axis Caster Raycast, but it also hits fluids such as water and lava.", "マクロ: 術者レイキャスト方向 (弱)", "マクロ: 術者レイキャスト方向と同じですが, 水や溶岩などの流体にも命中します.");
        addSpellPiece(root, "macro_caster_strong_raycast", "Macro: Strong Caster Raycast", "Like Macro: Caster Raycast, but blocks without a collision shape are ignored.", "マクロ: 術者レイキャスト (強)", "マクロ: 術者レイキャストと同じですが, 当たり判定のないブロックを無視します.");
        addSpellPiece(root, "macro_caster_strong_raycast_axis", "Macro: Strong Axis Caster Raycast", "Like Macro: Axis Caster Raycast, but blocks without a collision shape and fluids are ignored.", "マクロ: 術者レイキャスト方向 (強)", "マクロ: 術者レイキャスト方向と同じですが, 当たり判定のないブロックと流体を無視します.");
        addSpellPiece(root, "macro_caster_axial_offset", "Macro: Caster Axial Offset", "Offsets a position by Left/Right, Forward/Backward, and Up/Down relative to the caster's horizontal facing. Positive values move right, forward, and up. Pitch is ignored.", "マクロ: 術者方角平行移動", "術者の水平方向の向きを基準に, 位置を左右・前後・上下へ平行移動します. 正の値で右・前・上へ移動します. 視線の上下成分は無視されます.");
        addSpellPiece(root, "macro_caster_axial_rotation", "Macro: Caster Axial Rotation", "Rotates a direction vector from the caster's view frame (X = right, Y = up, Z = forward) into world space. Facing is rounded to the nearest horizontal cardinal direction. Pitch is ignored.", "マクロ: 術者方角回転", "方向ベクトルを術者の視点座標(X=右, Y=上, Z=前)からワールド座標へ回転します. 向きは最も近い水平4方角に丸められ, 視線の上下成分は無視されます.");
        addSpellPiece(root, "macro_caster_axial_offset_3d", "Macro: Caster Axial Offset (3D)", "Like Macro: Caster Axial Offset, but the facing also follows the pitch. When looking straight up or down, forward points vertically and the offset plane becomes horizontal.", "マクロ: 術者方角平行移動 (3D)", "マクロ: 術者方角平行移動と同じですが, 視線の上下も基準に含みます. 真上や真下を向いた時は前後が垂直になり, オフセット平面が水平になります.");
        addSpellPiece(root, "macro_caster_axial_rotation_3d", "Macro: Caster Axial Rotation (3D)", "Like Macro: Caster Axial Rotation, but the facing also follows the pitch (X = right, Y = view-up, Z = forward), including when looking straight up or down.", "マクロ: 術者方角回転 (3D)", "マクロ: 術者方角回転と同じですが, 視線の上下も基準に含みます. X=右, Y=視点上, Z=前で, 真上や真下を向いた時も視点に追随します.");
        addSpellPiece(root, "macro_face_axial_offset", "Macro: Face Axial Offset", "Offsets a position along axes aligned to the block face under the caster's crosshair. If Position is omitted, the hit block is used. Positive values move screen-right, into the block, and screen-up.", "マクロ: 面基準平行移動", "術者の視線が命中したブロック面を基準に, 位置を左右・前後・上下へ平行移動します. 位置を省略した場合は命中ブロックを使います. 正の値で画面右・ブロック内側・画面上へ移動します.");
        addSpellPiece(root, "macro_face_axial_rotation", "Macro: Face Axial Rotation", "Rotates a direction vector using the orientation of the block face hit by the caster's raycast as its reference.", "マクロ: 面基準回転", "方向ベクトルを, 術者の視線が命中したブロック面の方向を基準として回転させます.");
        addSpellPiece(root, "trick_mass_block_break", "Trick: Mass Block Break", "Breaks blocks at the coordinates in a Vector List (a Block List also works). Drops go into the caster's inventory; only overflow is dropped.", "作動式: 大規模ブロック破壊", "Vector List（Block List も可）の座標にあるブロックを破壊します. ドロップは術者のインベントリに入り, 入りきらない分だけ落ちます.");
        addSpellPiece(root, "selector_idea_storage_amount", "Selector: Ideaspace Storage - Amount", "Returns the stored amount of the resource chosen by the mode: items, fluids, chemicals or FE.", "取得子: イデアストレージ-貯蔵量", "モードで選んだ資源（アイテム・液体・化学物質・FE）の貯蔵量を取得します.");
        addSpellPiece(root, "selector_idea_storage_free_capacity", "Selector: Ideaspace Storage - Free Capacity", "Returns how much more of the resource chosen by the mode can be stored right now.", "取得子: イデアストレージ-空き容量", "モードで選んだ資源を, 今あとどれだけ格納できるかを取得します.");
        addSpellPiece(root, "selector_idea_storage_type_count", "Selector: Ideaspace Storage - Type Count", "Returns the number of stored types of the resource chosen by the mode.", "取得子: イデアストレージ-種類数", "モードで選んだ資源の, 格納中の種類数を取得します.");
        addSpellPiece(root, "selector_idea_storage_last_transfer", "Selector: Ideaspace Storage - Last Transfer Amount", "Returns the amount actually moved by the most recent Ideaspace transfer trick earlier in this cast.", "取得子: イデアストレージ-最終転送量", "この詠唱で直前に実行したイデア転送が, 実際に移した量を取得します.");
        addSpellPiece(root, "trick_idea_storage_view", "Trick: View Ideaspace Storage", "Opens a GUI to view and manage your Ideaspace Storage. Only works when cast by a player.", "作動式: イデアストレージ閲覧", "自分のイデアストレージを閲覧・出し入れするGUIを開きます. プレイヤーの詠唱時のみ有効です.");
    }

    /**
     * 領域魔法の本: Psitweaks の分類に項目「領域魔法」を立てて共通の決まりをまとめ、各領域のピースの
     * 項目はその領域だけの効き目にする。日本語は yomiyasu で推敲した。
     */
    private void addZoneMagicBook(JsonObject root) {
        addLocalized(root, "psi.book.entry.psitweaks_resident_spellgrams", "Resident SpellGrams", "常駐魔法式");
        addLocalized(root, "psi.book.page.psitweaks_resident_spellgrams.0",
                "$(thing)Resident SpellGrams$(0) are persistent entities—such as $(l:psitweaks/zone_magic)Zone Magic$(/l), circles, and Decontamination—that remain active where placed. They are immune to ordinary attacks and explosions and are not saved with chunks.$(p)Each has integrity scaled by Phenomenon Interference when placed and Time, decaying over time or from opposing spells like $(l:psitweaks_spell_pieces/trick_gram_demolition)Trick: Gram Demolition$(/l) and $(l:psitweaks_spell_pieces/trick_psi_bullet)Trick: Psi Bullet$(/l). One caster (or Autonomous Caster) may keep up to 32 combined (configurable; oldest ends first), and they vanish on logout or dimension change.",
                "$(thing)常駐魔法式$(0)は設置後も場に残って働き続けるエンティティです（$(l:psitweaks/zone_magic)領域魔法$(/l)・サークル系・除染など）. 通常の攻撃や爆発では壊れず, チャンクには保存されません.$(p)置いた時点の事象干渉力と時間に応じた耐久値を持ち, 時間経過や他者の$(l:psitweaks_spell_pieces/trick_gram_demolition)作動式: 術式解体$(/l)・$(l:psitweaks_spell_pieces/trick_psi_bullet)作動式: サイ・ブリット$(/l)などで削られて尽きると消滅します. 1人（自律詠唱機1台）につき合計32個まで設置でき（超過時は古いものから消滅, 設定変更可）, ログアウトや別ディメンション移動でも消えます.");
        addLocalized(root, "psi.book.page.psitweaks_resident_spellgrams.1",
                "Aim at a core within 5 blocks to see its name and remaining integrity (toggleable in client config).$(p)Manage your Resident SpellGrams with these pieces:$(li)$(l:psitweaks_spell_pieces/selector_own_spellgrams)Selector: Own Resident SpellGrams$(/l): get yours in the dimension as an Entity List$(li)$(l:psitweaks_spell_pieces/selector_spellgram_integrity)Selector: SpellGram Integrity$(/l) / $(l:psitweaks_spell_pieces/selector_spellgram_integrity_list)List$(/l): read remaining integrity$(li)$(l:psitweaks_spell_pieces/trick_extend_spellgram)Trick: Extend SpellGram$(/l): restore integrity$(li)$(l:psitweaks_spell_pieces/trick_recall_spellgram)Trick: Recall SpellGram$(/l): remove from any distance",
                "5ブロック以内で核に照準を合わせると名前と残り耐久値を確認できます（クライアント設定で切替可）.$(p)自分の常駐魔法式は次のスペルピースで管理できます.$(li)$(l:psitweaks_spell_pieces/selector_own_spellgrams)取得子: 自分の常駐魔法式$(/l): 同ディメンション内の自分の常駐魔法式を Entity List で取得$(li)$(l:psitweaks_spell_pieces/selector_spellgram_integrity)取得子: 魔法式耐久値$(/l) / $(l:psitweaks_spell_pieces/selector_spellgram_integrity_list)魔法式耐久値リスト$(/l): 残り耐久値を取得$(li)$(l:psitweaks_spell_pieces/trick_extend_spellgram)作動式: 魔法式延長$(/l): 耐久値を回復$(li)$(l:psitweaks_spell_pieces/trick_recall_spellgram)作動式: 魔法式回収$(/l): 距離に関係なく消去");
        addLocalized(root, "psi.book.entry.psitweaks_zone_magic", "Zone Magic", "領域魔法");
        // デバッグ用: 魔法式耐久値デバッガー。
        addLocalized(root, "psitweaks.debug.spellgram_integrity.amount", "Amount to remove: %s", "減らす量: %s");
        addLocalized(root, "psitweaks.debug.spellgram_integrity.no_target", "No SpellGram object in sight", "魔法式オブジェクトを見ていません");
        addLocalized(root, "psitweaks.debug.spellgram_integrity.reduced", "Removed %2$s integrity from %1$s (%3$s left)",
                "%1$s の耐久値を %2$s 減らしました（残り %3$s）");
        addLocalized(root, "psitweaks.debug.spellgram_integrity.destroyed", "Removed %2$s integrity from %1$s; it broke",
                "%1$s の耐久値を %2$s 減らし, 壊しました");
        addLocalized(root, "psitweaks.debug.spellgram_integrity.tooltip.amount", "Amount: %s", "減らす量: %s");
        addLocalized(root, "psitweaks.debug.spellgram_integrity.tooltip.use",
                "Right-click: remove integrity from the SpellGram object you look at (yours too)",
                "右クリック: 見ている魔法式オブジェクト（自分のものも）の耐久値を減らす");
        addLocalized(root, "psitweaks.debug.spellgram_integrity.tooltip.cycle", "Sneak + right-click: change the amount",
                "しゃがんで右クリック: 量を切り替える");
        addLocalized(root, "psitweaks.debug.spellgram_integrity.tooltip.rename", "Rename it to a number in an anvil to use that amount",
                "金床で名前を数にすると, その量を使う");
        addLocalized(root, "psi.book.page.psitweaks_zone_magic.0",
                "Zone Magic places $(l:psitweaks/resident_spellgrams)Resident SpellGrams$(/l) that keep working inside an area, such as Gravity, Heat, and Freeze Zones.$(p)Zone tricks are unlocked by research (Program: Zone Magic for the basic zones, and dedicated programs for specialized zones). See $(l:psitweaks/resident_spellgrams)Resident SpellGrams$(/l) for shared integrity, limit, and management rules, and each piece's entry for its specific effects.",
                "領域魔法は範囲内で効き続ける$(l:psitweaks/resident_spellgrams)常駐魔法式$(/l)で, 加重領域・加熱領域・凍結領域などがあります.$(p)研究で解禁でき, 基本的な領域はプログラム: 領域魔法, 一部の領域はそれぞれの専用プログラムで開きます. 耐久値・設置上限・管理方法は$(l:psitweaks/resident_spellgrams)常駐魔法式$(/l), 個別の効果は各ピースの項目を参照してください.");
        addLocalized(root, "psi.book.page.psitweaks_zone_magic.1",
                "Area is set by a Matrix (region matrix) and Size (constant, max 16); omitting the Matrix centers a cube on the caster. Use Trick: Show Region (Block mode) to preview it. Time is a constant up to 120 seconds (default 30; fixed at 30 for Gravity Zone's Vector mode).$(p)Zones that take Power require a constant of at least 1. Heat, Freeze, Turbulence, and Charged Zones accept Power above 10 to increase Cost and resource totals (Gravity, Acceleration, Deceleration, and Contamination Zones cap at 10).",
                "範囲は「行列」（領域行列）と「サイズ」（定数, 最大16）で決まり, 行列を省くと術者を中心にした立方体になります（作動式: 領域表示のブロックモードで確認可能）. 「時間」は最大120秒の定数です（省略時30秒, 加重領域のベクトルモードは30秒固定）.$(p)威力を持つ領域では1以上の定数が必要です. 加熱・凍結・乱流・荷電領域は10を超える威力でコストと総量だけを増やせます（加重・加速・減速・汚染領域は最大10）.");
        addLocalized(root, "psi.book.page.psitweaks_zone_magic.2",
                "Overlapping your own matching zones adds their resource totals (heat, cooling, FE, wind) and applies only the strongest of other effects (Gravity Zones add pushes up to Power 10). Overlapping your own Turbulence Zone upgrades Heat, Freeze, Soak, and Charged Zones into storms (see Trick: Turbulence Zone), while your own Heat and Freeze Zones cancel each other out.$(p)Where different casters' zones overlap, only the owner with the highest Phenomenon Interference when placed takes effect.",
                "自分の同じ種類の領域を重ねると, 資源の総量（熱・冷却・FE・風量）は合算され, その他の効果は最も強いものが効きます（加重領域は押す向きを合算, 最大威力10）. 自分の乱流領域を他領域に重ねると熱乱流・吹雪・暴風雨・雷雲へ強化され（詳しくは作動式: 乱流領域）, 自分の加熱領域と凍結領域が重なる部分は打ち消し合います.$(p)他人の領域と重なる場所では, 種類を問わず置いた時点の事象干渉力が高い持ち主の領域だけが効きます.");
        addLocalized(root, "psi.book.page.psitweaks_zone_magic.3",
                "Heat, cooling, FE, and wind are zone-wide totals determined by Cost and shared by everything inside (per-tick totals do not depend on Time).$(p)Processing speed for Create, Mekanism, and Applied Energistics 2 scales with Phenomenon Interference when placed, and dropped items do not despawn while processing. Create bulk processing only works where your own Turbulence Zone overlaps. With Create Aeronautics (Sable) installed, zones also affect blocks and machines on overlapping Simulated Contraptions.",
                "熱・冷却・FE・風量はコストで決まる領域全体の総量で, 範囲内の対象で分け合います（1 tick あたりの総量は時間によりません）.$(p)Create・Mekanism・Applied Energistics 2 への加工や供給の速さは置いた時点の事象干渉力で決まり, 加工中のドロップアイテムは消えません. Create の一括加工は自分の乱流領域が重なる場所でのみ働きます. Create Aeronautics (Sable) 導入時は, 範囲に重なった Simulated Contraption 上のブロックや機械にも効きます.");

        // 湿潤領域
        addBookPage(root, "trick_soak_zone.0",
                "Keeps living beings inside soaked (lasting 3 seconds after leaving), preventing fire and increasing Trick: Chain Lightning and Thundercloud bolt damage by 25%%. Also extinguishes fire, campfires, and candles, hardens concrete powder, and randomly moistens farmland and wets Sponges where block changes are allowed. See $(l:psitweaks/zone_magic)Zone Magic$(/l) for shared rules.",
                "範囲内の生物を濡らし続け（出たあとも3秒持続）, 炎上を防いで作動式: 連鎖電光や雷雲の雷撃ダメージを25%%増やします. 炎・焚き火・ろうそくを消し, コンクリートパウダーを固め, 耕地の保湿やスポンジの湿潤も行います（ブロック変更が可能な場所のみ）. 共通の決まりは$(l:psitweaks/zone_magic)領域魔法$(/l)を参照してください.");
        addBookPage(root, "trick_soak_zone.1",
                "With Create installed, bulk-washes dropped items and items on Depots and Mechanical Belts inside, but only where your own Turbulence Zone overlaps (a Rainstorm). Higher interference and turbulence speed up washing.",
                "Create 導入時は, 自分の乱流領域が重なる場所（暴風雨）に限り, ドロップアイテムやデポ・メカニカルベルト上のアイテムを一括洗浄します. 事象干渉力や乱流が強いほど速くなります.");

        // 加重領域
        addBookPage(root, "trick_gravity_zone.0",
                "Continuously pushes entities inside (including living beings, items, projectiles, minecarts, boats, and the caster) according to Power (constant 1–10). Power 4 roughly balances gravity, and upward push prevents fall damage. With Create Aeronautics (Sable), also pushes and tilts Simulated Contraptions; overlapping Gravity Zones combine pushes up to Power 10. See $(l:psitweaks/zone_magic)Zone Magic$(/l) for shared rules.",
                "範囲内のエンティティ（生物・アイテム・飛び道具・トロッコ・ボート・術者自身など）に, 「威力」（1〜10の定数）に応じた運動を毎 tick 与えます. 威力4前後で重力と釣り合い, 上向きに押している間は落下ダメージを受けません. Create Aeronautics (Sable) 導入時は Simulated Contraption も押し傾け, 加重領域の重なりは威力10を上限に押す向きを合算します. 共通の決まりは$(l:psitweaks/zone_magic)領域魔法$(/l)を参照してください.");
        addBookPage(root, "trick_gravity_zone.1",
                "Has two modes. Vector mode (default) pushes along Direction and lasts 30 seconds. Matrix mode pushes along the Matrix's Y axis (straight up if omitted; reversed with negative Power) and accepts a Time input. Use Operator: Matrix Set Y Axis to aim in any direction.",
                "2つのモードがあります. ベクトルモード（既定）は指定した「方向」へ押し, 30秒間持続します. 行列モードは行列の Y 軸方向（省略時は真上, 負の威力で逆向き）へ押し, 「時間」も指定できます. 演算子: Y軸方向指定で行列を回せば任意の向きへ押せます.");

        // 加熱領域
        addBookPage(root, "trick_heat_zone.0",
                "Ignites living beings inside according to Power (constant of at least 1), sparing the caster, pets, allies, and wet beings. Randomly melts Ice, Frosted Ice, and snow layers and dries Wet Sponges (Packed Ice, Blue Ice, and Snow Blocks are unaffected). See $(l:psitweaks/zone_magic)Zone Magic$(/l) for shared rules.",
                "「威力」（1以上の定数）に応じて範囲内の生物を炎上させます（術者・ペット・味方・濡れた生物は対象外）. ランダムに氷・薄氷や雪を溶かし, 濡れたスポンジを乾かします（氷塊・青氷・雪ブロックは対象外）. 共通の決まりは$(l:psitweaks/zone_magic)領域魔法$(/l)を参照してください.");
        addBookPage(root, "trick_heat_zone.1",
                "Produces a Cost-based heat total that heats Create Blaze Burners without fuel (nearest first) and shares remaining heat among Mekanism heat blocks, plus any heat transferred from touching or overlapping Freeze Zones of yours.$(p)With Create installed, overlapping your own Turbulence Zone (Heat Storm) bulk-smokes items on the ground, Depots, or Mechanical Belts; adding your own Charged Zone switches it to bulk-blasting.",
                "コストに応じた熱の総量を持ち, 燃料なしで Create のブレイズバーナーを近いものから加熱し, 残りの熱を Mekanism の熱ブロックへ分け与えます（隣接・重複する自分の凍結領域が運んだ熱も加算されます）.$(p)Create 導入時は, 自分の乱流領域が重なる場所（熱乱流）でアイテムを一括燻製し, さらに自分の荷電領域も重なると一括製錬に切り替わります.");

        // 凍結領域
        addBookPage(root, "trick_freeze_zone.0",
                "Freezes living beings inside like Powder Snow according to Power (constant of at least 1), slowing and damaging them once frozen; wet beings freeze twice as fast. Spares the caster, pets, allies, and beings immune to Powder Snow such as those in leather armor. Randomly freezes open water sources into ice. See $(l:psitweaks/zone_magic)Zone Magic$(/l) for shared rules.",
                "「威力」（1以上の定数）に応じて範囲内の生物を粉雪のように凍えさせ, 凍えきると鈍足と凍結ダメージを与えます（濡れた生物は半分の時間で凍えます）. 術者・ペット・味方・革の防具装備者など粉雪で凍えない相手は対象外です. 真上が空気の水源をランダムに氷へ変えます. 共通の決まりは$(l:psitweaks/zone_magic)領域魔法$(/l)を参照してください.");
        addBookPage(root, "trick_freeze_zone.1",
                "With Mekanism installed, cools heat-holding blocks inside (such as Resistive Heaters, Boiler Valves, and Fission Reactor Ports) using a Cost-based cooling total.$(p)When touching or overlapping your own Heat Zone (the overlap itself still cancels out), heat drawn from blocks hotter than ambient temperature is transferred into that Heat Zone's heat total.",
                "Mekanism 導入時は, コストに応じた冷却量で範囲内の熱を持つブロック（抵抗型発熱器・ボイラーバルブ・核分裂炉ポートなど）を冷やします.$(p)自分の加熱領域と隣接または重複している場合, 周囲温度より熱いブロックから奪った熱をその加熱領域へ運んで熱の総量に加えます（重なった部分は打ち消し合います）.");

        // 乱流領域
        addBookPage(root, "trick_turbulence_zone.0",
                "Blows a gale that sharply deflects non-magic projectiles shot by others (arrows, tridents, fireballs, etc.) according to Power (constant of at least 1), and quickly disperses lingering potion and dragon's breath clouds. See $(l:psitweaks/zone_magic)Zone Magic$(/l) for shared rules.",
                "暴風で他者の飛び道具（矢・トライデント・火の玉など. 魔法の弾は除く）の向きを大きく曲げ, 残留ポーションやドラゴンブレスの雲を素早く吹き散らします. 「威力」（1以上の定数）が大きいほど大きく曲がります. 共通の決まりは$(l:psitweaks/zone_magic)領域魔法$(/l)を参照してください.");
        addBookPage(root, "trick_turbulence_zone.1",
                "Overlapping your own zones upgrades them: Heat Zone becomes a Heat Storm (fire damage, extra Mekanism heat), Freeze Zone a Blizzard (freezing damage, faster freezing), Soak Zone a Rainstorm (water damage to vulnerable mobs), and Charged Zone a Thundercloud (random lightning, faster charging). With Create installed, these overlaps also enable bulk smoking (Heat), bulk blasting (Heat + Charged), and bulk washing (Soak), scaling with turbulence strength.",
                "自分の他領域に重ねると強化されます. 加熱領域とは熱乱流（火ダメージ・熱増加）, 凍結領域とは吹雪（凍結ダメージ・凍結加速）, 湿潤領域とは暴風雨（水に弱い生物へのダメージ）, 荷電領域とは雷雲（ランダム雷撃・充電加速）になります. Create 導入時はこれらの重なりでのみ一括加工（熱乱流で燻製, さらに荷電領域も重ねると製錬, 暴風雨で洗浄）も行われ, 乱流が強いほど速くなります.");
        addBookPage(root, "trick_turbulence_zone.2",
                "Generates Wind Units (WU) scaled by area, Power, and Phenomenon Interference when placed. Mekanism Generators Wind Generators (up to 10 WU each, working at any height even under roofs) and Immersive Engineering Windmills with a Kinetic Dynamo (up to 4 WU each, even with blocked fronts) share this wind to generate power.",
                "範囲の広さ・威力・事象干渉力に応じた風量（WU）を生み出します. Mekanism Generators の風力発電機（1台最大10 WU, 屋根の下や高度を問わず稼働）や, Immersive Engineering の動力ダイナモ付き風車（1台最大4 WU, 前面が塞がっていても稼働）で風量を分け合って発電できます.");

        // 領域干渉
        addBookPage(root, "trick_interference_zone.0",
                "Stops other casters with lower Phenomenon Interference from targeting blocks, positions, or entities inside (those inside cannot target themselves or center zones there). Does not affect equal/higher interference, your own or your Autonomous Casters' spells, or other mods' magic.$(p)Also blocks non-caster ender teleports (Endermen, Shulkers, Ender Pearls, Chorus Fruit) into or out of the area. See $(l:psitweaks/zone_magic)Zone Magic$(/l) for shared rules.",
                "置いた時点の自分の事象干渉力より低い他者は, 範囲内のブロック・位置・エンティティを対象にできず作動式が失敗します（範囲内の相手は自分自身も対象にできず, 領域の中心も置けません）. 同値以上の相手や自分・自分の自律詠唱機, 他 Mod の魔法は止めません.$(p)範囲内では術者以外のエンダー系テレポート（エンダーマン・シュルカー・エンダーパール・コーラスフルーツ）を内外ともに防ぎます. 共通の決まりは$(l:psitweaks/zone_magic)領域魔法$(/l)を参照してください.");

        // 荷電領域
        addBookPage(root, "trick_charged_zone.0",
                "Distributes a Cost-based FE capacity (Power: constant of at least 1) evenly to FE-accepting blocks, dropped items, FE entities, and players carrying a Zone Receiver inside. Randomly scrapes one oxidation stage off unwaxed copper blocks, and with Applied Energistics 2 processes dropped items like a Charger. See $(l:psitweaks/zone_magic)Zone Magic$(/l) for shared rules.",
                "「威力」（1以上の定数）とコストに応じた充電能力を, 範囲内の FE 対応ブロック・ドロップアイテム・FE 対応エンティティ・領域受電器を持つプレイヤーの持ち物へ均等に配ります. 錆止め以外の銅の錆をランダムに1段ずつ落とし, Applied Energistics 2 導入時はドロップアイテムをチャージャー同様に加工します. 共通の決まりは$(l:psitweaks/zone_magic)領域魔法$(/l)を参照してください.");
        addBookPage(root, "trick_charged_zone.1",
                "Overlapping your own Turbulence Zone creates a Thundercloud that strikes up to three living beings below at random (25%% stronger on wet targets) and increases FE output (with Create installed, adding your Heat Zone enables bulk blasting).$(p)If your own $(l:psitweaks_machines/zone_feeder)Zone Feeder$(/l) is inside, the zone draws stored FE from it whenever its own capacity is not enough (never charging the feeder itself).",
                "自分の乱流領域と重なる場所は雷雲となり, 下の生物へランダムに雷撃を落とし（濡れた相手には1.25倍）, FE 供給量も増えます（Create 導入時は, さらに加熱領域も重なると一括製錬になります）.$(p)範囲内に自分の$(l:psitweaks_machines/zone_feeder)領域給電機$(/l)があると, 充電能力で足りない分の FE を給電機から補って配ります（給電機自身には送りません）.");
        addBookPage(root, "trick_charged_zone.2",
                "Keep this in your inventory inside a Charged Zone to charge your FE items (armor first, then main hand, off hand, and remaining inventory). Right-click to toggle on or off. Players without one and other mobs' gear do not receive FE.",
                "持ち物に入れておくと, 荷電領域内で自分の FE 対応アイテム（防具・メインハンド・オフハンド・その他の順）に給電されます. 右クリックで入／切を切り替えられます. 持たないプレイヤーやほかの生物の装備には給電されません.");

        // 遮音領域
        addBookPage(root, "trick_silence_zone.0",
                "Silences sounds made inside (footsteps, blocks, mobs, and the caster; global sounds like music remain) and blocks vibrations like wool across, into, or out of the area.$(p)Sculk Sensors, Sculk Shriekers, and Wardens cannot detect blocked vibrations, and Shriekers inside or stepped on from inside will not shriek—though Wardens can still sniff out nearby targets. See $(l:psitweaks/zone_magic)Zone Magic$(/l) for shared rules.",
                "範囲内で鳴る音（足音・ブロック操作・モブの声・術者自身の音など. 音楽などは除く）を消し, 羊毛と同様に範囲を通る振動を遮断します.$(p)スカルクセンサー・スカルクシュリーカー・ウォーデンは遮られた振動に気づかず, 範囲内のシュリーカーや範囲内から踏んだシュリーカーも叫びません（においは消えないためウォーデンは近くで嗅ぎつけます）. 共通の決まりは$(l:psitweaks/zone_magic)領域魔法$(/l)を参照してください.");

        // 加速領域
        addBookPage(root, "trick_acceleration_zone.0",
                "Adds extra random ticks to growing plants inside (crops, saplings, Budding Amethyst, Bamboo, Sugar Cane, Nether Wart, etc.), accelerating growth to (Power + 5) times normal speed (Power: constant 1–10; follows randomTickSpeed; block entities and living beings are unaffected).$(p)Also speeds up random block changes of your own overlapping zones by the same factor. See $(l:psitweaks/zone_magic)Zone Magic$(/l) for shared rules.",
                "範囲内の育つもの（作物・苗木・芽生えたアメジスト・竹・サトウキビ・ネザーウォートなど）にランダムティックを上乗せし, 通常と合わせて（威力＋5）倍の速さで育てます（「威力」は1〜10の定数. randomTickSpeed に従い, ブロックエンティティと生き物には効きません）.$(p)自分の他領域と重なると, そのランダムなブロック変化も同じ倍率で速くなります. 共通の決まりは$(l:psitweaks/zone_magic)領域魔法$(/l)を参照してください.");

        // 減速領域
        addBookPage(root, "trick_deceleration_zone.0",
                "Applies Torpor to living beings inside, heavily reducing movement and flying speed (and players' attack speed) according to Power (constant 1–10); non-player mobs are also slowed every tick. Spares you, pets, allies, bosses, and other players when PvP is off. See $(l:psitweaks/zone_magic)Zone Magic$(/l) for shared rules.",
                "範囲内の生き物に鈍化を与え, 「威力」（1〜10の定数）に応じて移動・飛行速度（プレイヤーは攻撃速度も）を大きく下げます. プレイヤー以外の生き物は毎 tick 速度自体も落ちます. 術者・ペット・味方・PvP 無効時の他プレイヤー・ボスには効きません. 共通の決まりは$(l:psitweaks/zone_magic)領域魔法$(/l)を参照してください.");
        addBookPage(root, "trick_deceleration_zone.1",
                "Also slows projectiles inside every tick, sparing magic projectiles, projectiles from unaffected shooters, stuck arrows, Elytra rockets, fishing bobbers, items, minecarts, and boats. Does not cancel out Acceleration Zones.",
                "範囲内の飛び道具も毎 tick 大きく減速します（魔法の弾・対象外が放った飛び道具・刺さった矢・エリトラ用ロケット花火・浮き・アイテム・トロッコ・ボート等は対象外）. 加速領域とは打ち消し合いません.");

        // 作動式: 除染（領域ではない常駐魔法式）
        addBookPage(root, "trick_decontamination.0",
                "Places a non-zone $(l:psitweaks/resident_spellgrams)Resident SpellGram$(/l) core at Position that weakens Mekanism radiation sources within 32 blocks every second according to Cost (nearer and stronger sources weaken faster and vanish once weak enough). Does not reduce living beings' dose (use Trick: Cure Radiation).$(p)Takes Power (constant of at least 1) and Time (max 120 s, default 30).",
                "「位置」に除染の核（領域ではない$(l:psitweaks/resident_spellgrams)常駐魔法式$(/l)）を置き, 周囲32ブロック以内の Mekanism 放射線源をコストに応じて毎秒弱めます（近く強い線源ほど早く弱まり, 十分弱まると消えます）. 生き物の被ばく量は減らしません（作動式: 放射線除去を使用）.$(p)「威力」（1以上の定数）と「時間」（最大120秒, 省略時30秒）を指定します.");

        // 汚染領域
        addBookPage(root, "trick_contamination_zone.0",
                "Exposes living beings inside to radiation every second without leaving radiation sources, raising their dose up to a limit set by Power (constant 1–10); blocked by MekaSuit and Trick: Radiation Filter.$(p)Spares you, pets, allies, armor stands, and other players when PvP is off, and cannot be placed if Mekanism radiation is disabled. See $(l:psitweaks/zone_magic)Zone Magic$(/l) for shared rules.",
                "範囲内の生き物を毎秒放射線にさらします（放射線源は残しません）. 「威力」（1〜10の定数）に応じた上限まで被ばく量が上がり, MekaSuit や作動式: 放射線フィルタで防げます.$(p)術者・ペット・味方・防具立て・PvP 無効時の他プレイヤーには効かず, Mekanism 設定で放射線が無効なときは置けません. 共通の決まりは$(l:psitweaks/zone_magic)領域魔法$(/l)を参照してください.");
    }

    private void addSpellPiecesBook(JsonObject root) {
        addIdeaResourceLogistics(root);
        addIdeaSlotLogistics(root);
        addItemBlockMatrixPieces(root);
        addUtilitySpellPieces(root);
        addIdeaStorageSelectorPages(root);
        addBookPage(root, "trick_idea_storage_view.0",
                "Opens your personal Ideaspace Storage for items, fluids, Mekanism chemicals, and FE (transferred via Deposit/Withdraw FE mode; defaults to 256 item, 64 fluid, and 64 chemical types, configurable).$(p)Left-click the grid with a held item (right-click for 1) or Shift-click in your inventory to store. With an empty cursor, left-click withdraws up to a stack and right-click up to half (hold Shift to move directly to inventory).",
                "自分専用のイデアストレージ画面を開きます. アイテム・液体・Mekanism の化学物質・FE（FE モードの格納／取出で転送）を保管でき, 既定ではアイテム256種・液体64種・化学物質64種まで入ります（設定変更可）.$(p)カーソルで持って左クリック（右クリックで1個）またはインベントリから Shift クリックで格納し, 空の手で左クリックすると最大1スタック, 右クリックで半分（Shift 併用で直接インベントリへ）取り出せます.");
        addBookPage(root, "trick_idea_storage_view.1",
                "Right-click the grid with a fluid or chemical container to store or fill its contents (Shift + right-click repeats). With an empty cursor, left-clicking a stored fluid fills one stored empty bucket if supported.$(p)The side crafting button opens a 3x3 grid that auto-refills consumed ingredients from storage (Shift-click crafts roughly a stack; supports clearing to storage, search, sorting, and JEI/EMI recipe transfer).",
                "液体や化学物質の容器を持って右クリックすると中身を格納／充填できます（Shift＋右クリックで連続転送）. 空の手で液体を左クリックすると, 保存済みの空バケツ1個に汲み出せます.$(p)左の作業台ボタンで3×3クラフト欄が開き, 消費した材料は在庫から自動補充されます（Shift クリックでまとめて作成, 材料返却・検索・並べ替え・JEI/EMI 材料配置にも対応）.");
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
                "Wraps the target in a psionic shield worth $(thing)Power × 10$(0) points for one minute. While the shield lasts, damage the target takes comes off the shield instead of its health, and a honeycomb sphere flashes around it. Damage beyond what is left goes through, and the shield breaks.",
                "対象を $(thing)威力×10$(0) のシールドで1分間包みます. シールドがある間, 対象が受けるダメージは体力の代わりにシールドから差し引かれ, 対象の周りにハニカム模様の球が一瞬光ります. 残量を超えたダメージはそのまま通り, シールドは砕けます.");
        addBookPage(root, "trick_barrier.1",
                "Casting it again replaces the shield only if the new one is stronger. While shielded, the target does not catch fire. It does not absorb falling, drowning, poison and the like. A bar above your health shows the shield you have left and the seconds remaining.",
                "もう一度かけると, 新しいシールドのほうが多いときだけ置き換わります. シールドがある間, 対象は炎上しません. 落下, 溺れ, 毒などのダメージは防げません. 自分のシールドの残量と残り秒数は体力ゲージの上のバーに表示されます.");
        addBookPage(root, "selector_barrier_amount",
                "Outputs the shield points the target's $(thing)Trick: Barrier$(0) has left, as a Number. It outputs 0 when the target has no shield or is not a living entity.",
                "対象の $(thing)作動式: 障壁$(0) のシールド残量を Number として出力します. 対象にシールドがないときや, 生物でないときは 0 を出力します.");
        addBookPage(root, "trick_hardening", "Applies a hardening effect that limits large incoming damage to a fixed value. Maximum damage taken is capped at (level - 2).", "大きな被ダメージを一定値まで抑える硬化効果を付与します. 受ける最大ダメージを(レベル - 2)に抑えます.");
        addBookPage(root, "trick_parade.0",
                "Conjures a lookalike of the caster at the position, which wanders around it. It wavers away after $(thing)Time$(0) seconds or after taking $(thing)2 × Power$(0) hits. Casting it again replaces the current lookalike.",
                "指定位置に術者そっくりの分身を作り, その近くを歩き回らせます. 分身は $(thing)時間$(0) 秒たつか, $(thing)2×威力$(0) 回攻撃を受けると揺らいで消えます. もう一度唱えると, 今ある分身と入れ替わります.");
        addBookPage(root, "trick_parade.1",
                "While it stands, hostile creatures within 32 blocks that go for the caster go for the lookalike instead (bosses excepted). Other players no longer see the caster, and their spells aimed at the caster go to the lookalike and never reach the caster. Team mates are unaffected.",
                "分身がいる間, 術者を狙う32ブロック以内の敵対的な生物は分身を狙います (ボスを除く). 他のプレイヤーからは術者の姿が見えなくなり, 術者を狙った術式は分身に向かい, 術者には届きません. 同じチームの味方には影響しません.");
        addBookPage(root, "trick_flight", "Applies the Flight effect to the target. The Flight effect enables creative flight but consumes Psi every 0.5 seconds. The amount consumed varies based on the Flight effect level, the efficiency of the CAD used to apply the effect, and the spell bullet's cost multiplier. The base consumption at effect levels 1, 2, 3, 4, and 5 or higher is 1000 / 750 / 500 / 250 / 5 Psi every 0.5 seconds, respectively.", "対象に飛行エフェクトを付与します. 飛行エフェクトはクリエイティブ飛行を可能にしますが, 0.5秒ごとにPsiを消費します. 消費量は飛行エフェクトのレベル, 付与したCADの効率, 術式弾のコスト倍率によって変動し, エフェクトレベルが 1/2/3/4/5以上の際の基本消費量は 0.5秒ごとに 1000 / 750 / 500 / 250 / 5 です.");
        addBookPage(root, "trick_interact_block", "Acts on the target block as if right-clicked with the item in the caster's off hand.", "対象ブロックに対して, 術者のオフハンドのアイテムで右クリックしたように作用します.");
        addBookPage(root, "trick_freeze_block", "Freezes the target block one step: water to ice, ice to packed ice, packed ice to blue ice, lava to magma block, and magma block to obsidian.", "対象ブロックを1段階凍結させます. 水は氷, 氷は氷塊, 氷塊は青氷, 溶岩はマグマブロック, マグマブロックは黒曜石になります.");
        addBookPage(root, "trick_melt_block", "Melts the target block one step: ice, packed ice, and blue ice become water; obsidian, stone-like blocks, and cobblestone-like blocks become magma blocks; magma blocks become lava.", "対象ブロックを1段階溶解させます. 氷, 氷塊, 青氷は水に, 黒曜石, 石系, 丸石系はマグマブロックに, マグマブロックは溶岩になります.");
        addBookPage(root, "trick_pulsar", "Generates a magical Pulsar block at Position. The block emits a redstone signal with strength 15 in every direction and glows in the color of the caster's CAD. If Time is positive, it disappears after that many ticks; if Time is omitted or zero or less, it remains until broken.", "位置に魔法パルサーブロックを生成します. ブロックは全方向へ強度15のレッドストーン信号を出力し, 術者のCADの色で輝きます. 時間に正の値を指定するとそのtick数後に消滅し, 未指定または0以下では破壊されるまで残ります.");
        addBookPage(root, "trick_pulsar_sequence", "Constructs solid Pulsars in a straight line from Position in the direction of Target. The number generated is limited by the lesser of the length of the Target vector and Max. Each Pulsar emits a redstone signal with strength 15. If Time is positive, they disappear after that many ticks; if Time is omitted or zero or less, they remain until broken.", "位置を始点として, 対象の方向へ固体のパルサーを直線状に構築します. 生成数は対象ベクトルの長さと最大数の小さい方までです. 各パルサーは強度15のレッドストーン信号を出力します. 時間に正の値を指定するとそのtick数後に消滅し, 未指定または0以下では破壊されるまで残ります.");
        addBookPage(root, "trick_pulsar_light", "Generates a non-solid Pulsar light at Position. The light emits a redstone signal with strength 15 in every direction. Its color reflects the color of the caster's CAD. If Time is positive, it disappears after that many ticks; if Time is omitted or zero or less, it remains until broken.", "位置に非固体のパルサー光源を生成します. 光源は全方向へ強度15のレッドストーン信号を出力します. 光源色は術者のCADの色を反映します. 時間に正の値を指定するとそのtick数後に消滅し, 未指定または0以下では破壊されるまで残ります.");
        addBookPage(root, "trick_break_fortune", "Breaks the target block with Fortune.", "対象ブロックを幸運付きで破壊します.");
        addBookPage(root, "trick_break_silk", "Breaks the target block with Silk Touch.", "対象ブロックをシルクタッチ付きで破壊します.");
        addBookPage(root, "trick_store_entity", "Stores the target entity's UUID as a String value in CAD memory.", "対象エンティティのUUIDをString値としてCADメモリに保存します.");
        addBookPage(root, "selector_stored_entity", "Gets an entity from the UUID stored in CAD memory.", "CADメモリに保存されたUUIDからエンティティを取得します. ");
        addBookPage(root, "trick_store_value", "Stores a Plain Value in CAD memory. The memory slot uses Psi's standard numbering: 1 is the first slot. Number, Vector, and String values overwrite each other in the same slot. String values longer than 128 characters are truncated when stored, and a warning is shown in the caster's chat.", "Plain ValueをCADメモリに保存します. メモリ番号はPsi標準と同じく1が最初のスロットです. Number, Vector, Stringは同じスロットで互いに上書きされます. 128文字を超えるStringは保存時に切り捨てられ, 術者のチャット欄に警告が表示されます.");
        addBookPage(root, "selector_stored_value", "Gets a Plain Value from CAD memory. Use the mode button to choose String, Number, or Vector. String conversions are strict; Number and Vector cannot be converted directly.", "CADメモリからPlain Valueを取得します. モードボタンでString, Number, Vectorを選択します. String変換は厳格で, NumberとVectorは直接変換できません.");
        addBookPage(root, "selector_nearby_spellgram", "Gets SpellGram objects (Resident SpellGrams, the cores of Meteor Line nights and Micro Black Holes) around the specified coordinates. It is mainly used by tricks that control placed SpellGram objects.", "指定座標の周囲にある魔法式オブジェクト（常駐魔法式, 流星群の夜の核, マイクロブラックホール）を取得します. 設置済みの魔法式オブジェクトを制御する術式で主に使います.");
        addBookPage(root, "selector_autonomous_caster_position",
                "Outputs the center of the $(thing)Autonomous Caster$(0) block casting the spell, as a Vector. "
                        + "The Autonomous Caster's caster is in front of the machine, so the position of $(thing)Selector: Caster$(0) is the block in front. It fails when cast from anything but an Autonomous Caster.",
                "詠唱している$(thing)自律詠唱機$(0)のブロックの中心を Vector として出力します. "
                        + "自律詠唱機の詠唱主体は機械の前方に存在するので, $(thing)取得子: 術者$(0)の位置は前方のブロックになります. 自律詠唱機以外から詠唱するとエラーになります.");
        addBookPage(root, "trick_dispel", "Removes effects from the target entity. This is the general-purpose dispel that does not distinguish between beneficial and harmful effects.", "対象エンティティからエフェクトを除去します. 良性・悪性を区別しない汎用版の解呪です.");
        addBookPage(root, "trick_dispel_beneficial", "Removes only beneficial effects from the target entity. It is suited for stripping enhancements from hostile targets.", "対象エンティティから有益なエフェクトだけを除去します. 敵対対象の強化を剥がす用途に向いています.");
        addBookPage(root, "trick_dispel_non_beneficial", "Removes non-beneficial effects from the target entity. Use it when you want to cleanse harmful effects while leaving allied buffs intact.", "対象エンティティから有益でないエフェクトを除去します. 味方のバフを残したまま悪性効果を消したい時に使います.");
        addBookPage(root, "trick_cocytus.0",
                "Takes no input. Blows a blizzard the way the caster is looking, through the same cone in front of the caster as $(thing)Trick: Gram Demolition$(0) (blocks do not stop it), and freezes the minds of the mobs it catches forever. A frozen mob's AI stops and it is sealed in ice, unable to move or make a sound.",
                "入力はありません. 術者の視線の方向へ吹雪を放ち, $(thing)作動式: 術式解体$(0)と同じ前方の円錐の範囲 (ブロックは貫通します) にいるモブの精神を永久に凍結させます. 凍結したモブは AI が止まって氷に閉じ込められ, 動くことも音を立てることもなくなります.");
        addBookPage(root, "trick_cocytus.1",
                "Players, the caster, the caster's pets and allies, and bosses are not frozen. The blizzard itself deals no damage.$(br2)If something else turns a frozen mob's AI back on, the ice melts and the mob can make sounds again.",
                "プレイヤー, 術者自身とそのペット・味方, ボスは凍結しません. 吹雪そのものはダメージを与えません.$(br2)ほかの方法で AI が戻ると, 氷が溶けて再び音を立てるようになります.");
        addBookPage(root, "trick_supply_fe", "Supplies FE to the target block. When CAD Efficiency is 100, it supplies 80 FE per Psi.", "対象ブロックへFEを供給します. 供給量はCADの効率が100のとき, 1psiあたり80FEです.");
        addBookPage(root, "trick_pull_item.0",
                "Pulls up to one stack (capped by optional Max) from the specified face of the target block into your inventory; errors if there is no inventory, nothing to move, or the block is protected.$(p)Filter modes: String (ID wildcards, optional namespace, or # tag), Item (item type), and Item (Strict) (matches data components). Item modes also accept an Item List.",
                "位置のブロックのインベントリから指定面経由で最大1スタック（任意入力「最大」で上限変更可）を術者のインベントリへ引き出します（インベントリなし・対象なし・保護ブロックはエラー）.$(p)フィルタは String（ID ワイルドカード・名前空間省略・# タグ）, Item（アイテム種）, Item (Strict)（データコンポーネントまで一致）の3モードで, Item 系は Item List も受け付けます.");
        addBookPage(root, "trick_send_item.0", "Moves items from the caster's inventory into the inventory of the block at the target position, through the specified face. Only one stack is moved per cast; if the destination cannot hold it all, only what fits is moved. CADs are never moved.", "術者のインベントリから, 入力位置のブロックのインベントリへ, 入力方向の面を経由してアイテムを移動させます. 1回の実行で移動できるのは1スタックまでで, 移動先に入りきらない場合は入る分だけ移動します. CADは移動対象から除外されます.");
        addBookPage(root, "trick_send_item.1", "The filter modes and the optional Max input behave like Trick: Pull Item, including Item Lists in both Item modes. Casting fails with an error if the block has no inventory, no matching item is held, or the block is protected.", "フィルタのモードと任意入力の最大は作動式: アイテム引き出しと同様で, Item と Item (Strict) モードでは Item List も接続できます. インベントリが無い, 対象アイテムを所持していない, 保護されている場合はエラーになります.");
        addBookPage(root, "trick_time_accelerate", "Multiplies the target block's tick progression by (2 ^ power). The upper limit is 512x speed.", "対象ブロックのtick進行を (2 ^ 威力) 倍にします. 上限は512倍速まで.");
        addBookPage(root, "trick_phonon_maser", "Fires a high-power heat ray using ultrasonic vibration.", "超音波振動による高威力の熱線を放ちます.");
        addBookPage(root, "trick_meteor_line.0",
                "Spreads a sphere of night around the position, with a radius of $(thing)Radius$(0) blocks (16 to 32, 16 if left out). Stars light up in its upper half, and after one second they rain streaks of light for one second: at least one at every creature inside that the spell may harm, and the rest onto the ground.",
                "指定位置を中心に, $(thing)半径$(0) ブロック (16〜32, 省略時は16) の球の夜を広げます. 夜の上半分に星が灯り, 1秒後から1秒間, 星から光条が降り注ぎます. 光条は夜の中にいる攻撃できる生物それぞれに少なくとも1本ずつ向かい, 残りは地面に落ちます.");
        addBookPage(root, "trick_meteor_line.1",
                "A streak passes through blocks without breaking them, so a roof gives no cover, and deals lethal damage to whatever it touches. Creatures outside the night when a streak falls are not aimed at. The caster, their pets and allies are never hit. The night is a SpellGram object whose core sits at the top of the sphere. Its integrity does not wear away over time; if it runs out, the night shatters and its streaks vanish with it.",
                "光条はブロックを壊さずにすり抜けるので屋根の下にも届き, 触れたものに致死的なダメージを与えます. 光条が降る瞬間に夜の外にいる生物は狙われません. 術者とそのペット, 味方には当たりません. 夜は魔法式オブジェクトで, 核は球のてっぺんにあります. 耐久値は時間では減りません. 耐久値が尽きると夜は砕け散り, 光条も一緒に消えます.");
        addBookPage(root, "trick_supreme_infusion", "Infusion-converts Echo Shards into Psionic Echo.", "残響の欠片をサイオニックエコーへ注入変換します.");
        addBookPage(root, "trick_molecular_divider", "Cuts living beings along a plane defined by three points.", "三点で定義した平面で生物を切断します.");
        addBookPage(root, "trick_aqua_cutter",
                "Fires a water-blade projectile forward as an early-game offensive trick. A creature it hits stays wet for 10 seconds, as if in the rain: it does not catch fire, and $(thing)Trick: Chain Lightning$(0), $(thing)Trick: Burst$(0) and Thundercloud bolts deal it more. A burning creature takes 25%% more instead: the water turns to steam and puts out the fire, but the creature stays dry.",
                "前方へ水刃の発射体を放つ序盤用の攻撃術式です. 当てた相手は雨に打たれたように10秒間濡れ, その間は燃えず, $(thing)作動式: 連鎖電光$(0) や $(thing)作動式: 爆裂$(0), 雷雲の雷撃のダメージが増えます. 燃えている相手には水が蒸気になって25%%増しのダメージを与え, 火を消しますが, 濡れはしません.");
        addBookPage(root, "trick_dry_meteor", "Fires a dry ice projectile forward, dealing freezing damage to targets it hits. The dry ice sublimates into carbon dioxide, inflicting Carbon Poisoning that impairs the target's mobility and deals damage over time.", "前方にドライアイスの弾丸を発射し, 命中した相手に凍結ダメージを与えます. ドライアイスは二酸化炭素へと気化し対象を炭素中毒状態にして機動力を奪い継続ダメージを与えます.");
        addBookPage(root, "trick_blaze_ball", "Fires a ball of flame forward as an early-game offensive trick.", "前方へ火の弾を放つ序盤用の攻撃術式です.");
        addBookPage(root, "trick_active_air_mine", "Creates a spherical shockwave at the specified coordinates and damages living beings within range.", "指定座標に球状の衝撃波を作り, 範囲内の生物にダメージを与えます.");
        addCircleBookPages(root);
        addBookPage(root, "trick_die_flex", "Behaves similarly to Psi's Trick: Die and refunds the Psi cost of spell pieces that were not executed. With high-frequency casting, the client-side Psi display may temporarily desync.", "Psi本体の停止と同様の動作を行い, 未実行分のスペルピースのPsiコストを返還します. 高頻度詠唱ではクライアント側のPsi表示が一時的にずれることがあります.");
        addBookPage(root, "trick_jump", "Jumps forward to the next Jump Anchor with the same label when the target number is unset, or when its absolute value is less than 1. The label input is optional, but if used it must be a String constant. Empty labels only match empty-label anchors. It cannot jump to Jump Anchors behind it.", "対象数値が未入力, または絶対値が1未満なら, 同じラベルを持つ次のジャンプアンカーまで前方にジャンプします. ラベル入力は任意ですが, 使う場合はString定数である必要があります. 空ラベルは空ラベルのアンカーにのみ一致します. 後方のジャンプアンカーには飛ぶことができません.");
        addBookPage(root, "trick_jump_flex", "Uses the same conditions as Trick: Jump to jump to a Jump Anchor, then refunds the Psi cost of the spell pieces actually skipped by the jump. With high-frequency casting, the client-side Psi display may temporarily desync.", "作動式: ジャンプと同じ条件でジャンプアンカーまでジャンプし, ジャンプによって実際にスキップしたスペルピースのPsiコストを返却します. 高頻度詠唱ではクライアント側のPsi表示が一時的にずれることがあります.");
        addBookPage(root, "trick_switch", "Evaluates the input String and jumps forward to the next Jump Anchor whose constant label is equal to it. If no matching anchor exists ahead, it does not error and simply continues to the next spell piece, so place the default branch immediately after the Switch. It cannot jump to Jump Anchors behind it.", "入力Stringを評価し, それと等しい定数ラベルを持つ次のジャンプアンカーまで前方にジャンプします. 前方に一致するアンカーがない場合はエラーにならず, そのまま次のスペルピースへ進むため, default相当の処理はスイッチの直後に置いてください. 後方のジャンプアンカーには飛ぶことができません.");
        addBookPage(root, "trick_switch_flex", "Uses the same conditions as Trick: Switch to jump to a Jump Anchor, then refunds the Psi cost of the spell pieces actually skipped by the jump. If no matching anchor exists, execution continues and no Psi is refunded. With high-frequency casting, the client-side Psi display may temporarily desync.", "作動式: スイッチと同じ条件でジャンプアンカーまでジャンプし, ジャンプによって実際にスキップしたスペルピースのPsiコストを返却します. 一致するアンカーがない場合はそのまま実行を続け, Psiを返却しません. 高頻度詠唱ではクライアント側のPsi表示が一時的にずれることがあります.");
        addBookPage(root, "jump_anchor", "A marker used as the destination for Trick: Jump and Trick: Switch. This spell piece itself does nothing. You can input a constant String as its label.", "作動式: ジャンプと作動式: スイッチの到達点になる目印です. このスペルピース自体は何もしません. 定数文字列をラベルとして入力することができます.");
        addBookPage(root, "safety", "It works effectively no matter where it is placed within the program. Immediately before Psi is consumed, it compares the spell’s actual Psi consumption with the total of the caster’s current Psi and the Psi in the CAD battery. If the caster’s Psi is insufficient, the spell is canceled without consuming any Psi.", "プログラム内のどこに配置しても効果を発揮します. Psi消費直前に, 術式の実際のPsi消費量と, 術者の現在PsiおよびCADバッテリー内Psiの合計を比較します. 術者のPsi量が不足している場合はPsiを消費せずに術式を中止します.");
        addBookPage(root, "trick_radiation_injection", "Applies Mekanism radiation exposure to the target.", "対象へMekanismの放射線被ばくを付与します. ");
        addBookPage(root, "trick_radiation_filter", "Applies a radiation protection effect to the target.", "対象に放射線防護効果を付与します.");
        addBookPage(root, "trick_cure_radiation", "Removes the target's radiation exposure.", "対象の被ばく量を除去します.");
        addBookPage(root, "trick_guillotine", "Deals powerful slash damage to the target and makes it drop a head when killed.", "対象に強力な斬撃ダメージを与え, 討伐時に頭をドロップさせます.");
        addBookPage(root, "trick_chain_lightning.0",
                "Discharges lightning from the focal point along $(thing)Direction$(0), striking the nearest hostile creature within 24 blocks (discharges harmlessly into air if none is found). Chains to up to 3 hostile creatures within 8 blocks, then up to 2 more from each within 8 blocks without hitting the same target twice.",
                "焦点から$(thing)方向$(0)へ雷撃を放ち, 24ブロック以内で最も近い敵対的な生物に命中させます（対象がいなくても失敗せず空中へ放電）. 最初の対象から8ブロック以内の敵対生物最大3体, さらに各8ブロック以内の最大2体へ連鎖します（同一対象には1回のみ）.");
        addBookPage(root, "trick_chain_lightning.1",
                "Deals $(thing)9 + 9 × Power$(0) on the first hit, 40%% on the first chain, and 20%% on the second; wet creatures take 1.25x damage.$(p)Set optional $(thing)Indiscriminate$(0) to non-zero to also hit animals and villagers (still sparing you, allies, and pets; connect $(thing)Selector: Sneak Status$(0) to toggle).",
                "ダメージは初撃$(thing)9 + 9×威力$(0), 1段目40%%, 2段目20%%です. 濡れている生物には与えるダメージが1.25倍になります.$(p)$(thing)無差別$(0)（省略可）に0以外を入れると動物や村人も対象になります（術者・味方・ペットは除く. $(thing)取得子: スニーク状態$(0)で切替可）.");
        addBookPage(root, "trick_mine_genesis.0",
                "Strikes the ground at the position so a shockwave ring spreads out to $(thing)Radius$(0) blocks (2 to 12). Everything standing on the ground when the ring passes takes damage and is thrown into the air; anything already in the air is unharmed. The position must be within 3 blocks above the ground.",
                "指定位置の地面を打ち, $(thing)半径$(0) ブロック (2〜12) まで広がる衝撃波の輪を起こします. 輪が通過したときに地面に立っている生物はダメージを受けて宙へ打ち上げられ, すでに空中にいるものは無傷です. 位置は地面から3ブロック以内の高さである必要があります.");
        addBookPage(root, "trick_mine_genesis.1",
                "Damage is $(thing)16 + 12 × Power$(0) at the centre and falls to 40%% at the edge. Shields cannot block it, and a higher Power throws targets higher. The tremor only moves creatures; it never breaks blocks.",
                "ダメージは中心で $(thing)16 + 12×威力$(0), 外縁では40%%まで下がります. 盾では防げず, 威力が高いほど高く打ち上げます. 揺れが動かすのは生物だけで, ブロックは壊れません.");
        addBookPage(root, "trick_burst.0",
                "Vaporizes the liquid inside the target, dealing $(thing)36 + 30 × Power$(0) damage that ignores armor and shields. Works on bosses too. Wet creatures take 1.25x damage.",
                "対象の体内の液体を気化させ, 防具と盾を無視する $(thing)36 + 30×威力$(0) のダメージを与えます. ボスにも効きます. 濡れている生物には与えるダメージが1.25倍になります.");
        addBookPage(root, "trick_burst.1",
                "If the target dies from it, its body bursts and hits nearby hostile creatures for up to 40%% of the damage.",
                "この術式で対象が倒れると体が破裂し, 付近の敵対的な生物に最大でダメージの40%%を与えます.");
        addBookPage(root, "trick_psi_bullet",
                "Fires a bullet of psions from the position in the direction. The bullet's range is 24 blocks, and it stops at the first creature, SpellGram object or block it hits.$(br)"
                        + "A creature it hits takes $(thing)2 + Power$(0) damage. When it hits the core of a SpellGram object, it removes $(thing)8 + 2 × Power$(0) times the caster's Phenomenon Interference from that object's integrity. It flies through the caster's own SpellGram objects.",
                "指定位置から方向へ想子の弾を撃ち出します. 弾丸の射程は24ブロックで, 最初に当たった生物, 魔法式オブジェクト, ブロックで消えます.$(br)"
                        + "生物には $(thing)2 + 威力$(0) のダメージを与えます. 魔法式オブジェクトの核に当たると, その耐久値を $(thing)8 + 2×威力$(0) に術者の事象干渉力を掛けた分だけ減らします. 術者自身の魔法式オブジェクトはすり抜けます.");
        addBookPage(root, "trick_gram_demolition",
                "Takes no input. Blasts every SpellGram object whose core is within 16 blocks in front of the caster, inside a cone of 30 degrees around the line of sight, and removes $(thing)300$(0) times the caster's Phenomenon Interference from each one's integrity. Blocks do not stop it.$(br)"
                        + "Creatures in the same range take $(thing)6$(0) damage. The caster's own SpellGram objects are left alone.",
                "入力はありません. 術者の視線の先16ブロック以内で, 視線から30度の円錐の中に核がある魔法式オブジェクトすべての耐久値を, $(thing)300$(0) に術者の事象干渉力を掛けた分だけ減らします. ブロックは貫通します.$(br)"
                        + "同じ範囲の生物には $(thing)6$(0) のダメージを与えます. 術者自身の魔法式オブジェクトは削りません.");
        addBookPage(root, "trick_air_bullet",
                "Fires a bullet of compressed air from the position in the direction. The bullet's range is 24 blocks.$(br)"
                        + "A creature the bullet hits directly takes $(thing)4.5 + 4.5 × Power$(0) damage, and the bullet stops there. A graze from the pressure wave around the bullet deals a third of a direct hit's damage, and the bullet flies on.",
                "指定位置から方向へ圧縮した空気の弾を撃ち出します. 弾丸の射程は24ブロックです.$(br)"
                        + "弾が直撃した生物には $(thing)4.5 + 4.5×威力$(0) のダメージを与え, 弾はそこで消えます. 弾の周りの圧力波にかすったときのダメージは直撃の1/3で, 弾はそのまま飛び続けます.");
        addBookPage(root, "trick_dancing_blades.0",
                "Deploys all swords in your $(l:items/psitweaks_blade_pouch)$(o)$(item)Blade Pouch$(0)$(/l) around you as spectral blades. They dart at hostile creatures within 16 blocks, slash them, and return, fading after 3 attacks or 15 seconds.",
                "術者の$(l:items/psitweaks_blade_pouch)$(o)$(item)ブレードポーチ$(0)$(/l)に入っている剣をすべて術者の周囲に展開します. 刃は16ブロック以内の敵対的な生物へ飛びかかって斬りつけ, 術者のもとへ戻ります. 刃は3回の攻撃を行うか15秒の経過で消滅します.");
        addBookPage(root, "trick_dancing_blades.1",
                "Each slash deals the sword's attack damage scaled by Phenomenon Interference and applies enchantments without consuming durability. Recasting redeploys the blades.$(p)Set optional $(thing)Indiscriminate$(0) to non-zero to also target animals and villagers (sparing you, allies, and pets).",
                "1回の斬撃のダメージは, その剣で攻撃した際のダメージに事象干渉力を掛けたもので, エンチャント効果も働きます. 剣の耐久値は減りません. もう一度唱えると刃を再展開します.$(p)$(thing)無差別$(0) (省略可) に0以外を入れると, 刃は動物や村人を含むあらゆる生物を狙います（術者・味方・ペットは除く）.");
        addBookPage(root, "trick_pseudo_meteor.0",
                "Calls down $(thing)Number$(0) pseudo meteors (max 16) around the position, homing in on nearby hostile creatures in order or landing near the position. Explodes on impact for up to $(thing)25 + 20 × Power$(0) damage and 3 s of fire; passes through terrain without breaking blocks and is reduced by Blast Protection.$(p)Set optional $(thing)Indiscriminate$(0) to non-zero to also target animals and villagers (sparing you, allies, and pets).",
                "$(thing)数値$(0)個（上限16）の疑似隕石を指定位置の周囲へ順に降らせます. 近くの敵対的な生物を近い順に追尾し, 該当がいなければ指定位置周辺に落ちます. 着弾時に爆発し, 範囲内へ最大$(thing)25 + 20×威力$(0)ダメージと3秒の炎上を与えます. 地形をすり抜けてブロックを壊さず, 爆発耐性で軽減できます.$(p)$(thing)無差別$(0)（省略可）に0以外を入れると動物や村人も狙います（術者・味方・ペットは除く）.");
        addBookPage(root, "trick_micro_black_hole.0",
                "Creates a micro black hole at the position. For 10 seconds it drags creatures within $(thing)Radius$(0) blocks (16 to 32) and swings them around its centre at speed; a creature whipping past the centre may be flung out of range. In the last 1.5 seconds it swallows them, then collapses and crushes everything within three tenths of $(thing)Radius$(0) of the centre.",
                "指定位置にマイクロブラックホールを生み出します. 10秒間, $(thing)半径$(0) ブロック (16〜32) 以内の生物を引き寄せ, 中心のまわりで勢いよく振り回します. 中心のそばを回り込んだ勢いで, 範囲の外へ投げ飛ばされることもあります. 最後の1.5秒で中心へ呑み込み, 崩壊して中心から $(thing)半径$(0) の3割以内のものを押し潰します.");
        addBookPage(root, "trick_micro_black_hole.1",
                "The collapse deals $(thing)30 + 25 × Power$(0) damage that ignores armor and shields, falling to half at the edge. While being pulled, creatures build up no fall damage."
                        + " The black hole is a SpellGram object: it has integrity, which does not wear away over time. If its integrity runs out before the collapse, it vanishes without crushing anything.",
                "崩壊のダメージは防具と盾を無視する $(thing)30 + 25×威力$(0) で, 外縁では半分になります. 引き寄せられている間は落下ダメージが溜まりません. "
                        + "ブラックホールは魔法式オブジェクトで, 耐久値を持ちます. 耐久値は時間では減りません. 崩壊する前に耐久値が尽きると, 何も押し潰さずに消えます.");
        addBookPage(root, "trick_material_mutation", "Breaks specific blocks and transmutes them into other items. The Material Mutator can perform this process using power and Vaporized Psionic Echo.", "特定のブロックを破壊して別のアイテムへ変成させます. 物質変成機はこの処理を電力と気化サイオニックエコーで実行できます.");
        addBookPage(root, "trick_physical_propulsion", "Raycasts from the specified position in the Ray direction; if it hits a Simulated Contraption, applies propulsion to it.", "指定位置からRay方向へレイキャストし, Simulated Contraption に命中したならば推進力を与えます.");
        addBookPage(root, "operator_tan", "Returns the tangent of the target number.", "対象数値のタンジェントを返します.");
        addBookPage(root, "operator_atan", "Returns the arc tangent of the target number.", "対象数値のアークタンジェントを返します.");
        addBookPage(root, "operator_sinh", "Returns the hyperbolic sine of the target number.", "対象数値のハイパボリックサインを返します.");
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
        addBookPage(root, "selector_nbt", "Outputs the entries of the compound or list at the NBT Path as a String List: \"key:value\" for a compound, sorted by key, and \"index:value\" for a list, with values as SNBT. Unconnected or empty, the whole NBT is read. Any other value, a missing path or a target without NBT gives an empty List. Paths work as in Selector: NBT Value.", "NBTパスにある複合タグかリストの中身を String List で出力します. 複合タグは \"キー:値\" をキー順に, リストは \"番号:値\" を並べ, 値はSNBTです. NBTパスを接続しないか空文字にすると, NBT全体を読みます. それ以外の値, 存在しないパス, NBTのない対象は空のリストです. パスの書き方は 取得子: NBT値 と同じです.");
        addBookPage(root, "selector_nbt_keys", "Outputs the keys of the compound at the NBT Path, sorted, or the indexes \"0\", \"1\", ... of a list, as a String List. Unconnected or empty, the top-level keys are given. Any other value or a missing path gives an empty List. Paths work as in Selector: NBT Value.", "NBTパスにある複合タグのキーをキー順に, リストなら番号 \"0\", \"1\", ... を String List で出力します. NBTパスを接続しないか空文字にすると, 一番上のキーを出力します. それ以外の値や存在しないパスは空のリストです. パスの書き方は 取得子: NBT値 と同じです.");
        addBookPage(root, "selector_nbt_value",
                "Reads the target's NBT (Entity saved data, Item id/count/components, or Block entity) using command NBT path syntax (Inventory[0].id, [-1], [], [{...}]; keys with spaces are quoted automatically) and returns the first match. Reads up to 1,024 targets per cast by default.",
                "対象の NBT（エンティティ保存データ・アイテムの id/count/components・ブロックエンティティ）をコマンド準拠の NBT パス（Inventory[0].id・[-1]・[]・[{...}] 等. 空白キーは自動引用）で読み, 最初に一致した値を返します（1詠唱あたり既定1,024対象まで）.");
        addBookPage(root, "selector_nbt_value.1",
                "String mode returns strings as-is, numbers without type suffixes, and compounds/lists as SNBT. Number mode returns numeric values only (strings like \"5\" count as not found).$(p)Missing values or invalid paths never error, returning Fallback (or empty String / 0 if unset).",
                "文字列モードは文字列をそのまま, 数値は型記号を外し, 複合タグやリストは SNBT で出力します. 数値モードは数値のみ返し,\"5\" のような文字列は未検出扱いです.$(p)値がない場合や不正なパスでもエラーにならず, 既定値（未接続なら空文字または0）を返します.");
        addBookPage(root, "selector_nbt_exists", "Outputs 1 if the NBT Path reaches anything in the target's NBT, otherwise 0. Paths work as in Selector: NBT Value, so it also tells whether a list has an element or whether a filter such as Inventory[{Slot:0b}] matches. A target without NBT, such as a block without a block entity, gives 0.", "対象のNBTにNBTパスで指す値があれば1, なければ0を出力します. パスの書き方は 取得子: NBT値 と同じなので, リストに要素があるかや, Inventory[{Slot:0b}] のような絞り込みに一致するかも調べられます. ブロックエンティティのないブロックなど, NBTのない対象は0です.");
        addBookPage(root, "operator_string_partial_match", "Searches String 1 with String 2 and checks whether it contains matching text. * matches any text, ? matches one character, and [abc] matches one of the listed characters. Escape wildcard characters with a backslash. Matching is case-sensitive.", "文字列1を文字列2で検索してマッチする文字列を含むか判定します. * は任意の文字列, ? は任意の1文字, [abc] は a/b/c のいずれか1文字に一致します. ワイルドカード文字はバックスラッシュでエスケープできます. 比較は大文字小文字を区別します.");
        addBookPage(root, "operator_string_starts_with", "Outputs 1 if String 1 starts with String 2, otherwise outputs 0. The comparison is case-sensitive.", "文字列1が文字列2で始まるなら1, そうでなければ0を出力します. 比較は大文字小文字を区別します.");
        addBookPage(root, "operator_string_ends_with", "Outputs 1 if String 1 ends with String 2, otherwise outputs 0. The comparison is case-sensitive.", "文字列1が文字列2で終わるなら1, そうでなければ0を出力します. 比較は大文字小文字を区別します.");
        addBookPage(root, "operator_string_concat", "Concatenates String 1, String 2, and optional String 3 in that order.", "文字列1, 文字列2, 任意の文字列3をこの順に結合します.");
        addBookPage(root, "operator_string_split", "Splits String 1 with String 2 as the delimiter and outputs a String List. If String 2 is not connected, a comma is used. Empty fields are preserved.", "文字列1を文字列2の区切り文字で分割し, String List を出力します. 文字列2が未接続の場合はカンマを使います. 空要素は保持されます.");
        addBookPage(root, "operator_string_slice", "Returns a selected range of characters from the input String as a new String. If A or B is not connected, the beginning or end is used. Negative numbers count from the end, out-of-range values are clamped to the valid range, and an end position at or before the start returns an empty String. Decimal values are truncated toward zero.", "入力文字列の文字数を指定して範囲を切り出して新しい文字列として返します. AまたはBが未接続なら先頭または末尾を使います. 負数は末尾から数え, 範囲外は有効範囲に収め, 終了位置が開始位置以下なら空文字列を返します. 小数部分は0方向へ切り捨てます.");
        addBookPage(root, "operator_string_length", "Returns the number of characters in the input String.", "入力した String の文字数を Number として返します.");
        addBookPage(root, "operator_string_replace", "Replaces every literal, case-sensitive occurrence of String 2 in String 1 with String 3. String 2 is evaluated only as an exact match. If String 2 is empty, String 1 is returned unchanged. The result is limited by the runtime String length limit.", "文字列1のうち, 文字列2と大文字小文字を区別してリテラル一致するすべての部分を文字列3へ置換します. 文字列2は完全一致のみ判定します. 文字列2が空の場合は文字列1をそのまま返します. 結果は実行時のString文字数上限に従います.");
        addBookPage(root, "operator_string_trim", "Removes whitespace characters (such as spaces, tabs, and line breaks) from the beginning and end of the input String. Whitespace inside the String is preserved.", "入力した文字列の先頭と末尾にある空白（スペース, タブ, 改行など）を削除します. 文字列の途中にある空白は残ります.");
        addBookPage(root, "operator_string_list_join", "Joins the input String List with String 2 and outputs a String. If String 2 is not connected, a comma is used. Empty delimiters are allowed and concatenate the elements directly.", "入力リストを文字列2で結合し, String を出力します. 文字列2が未接続の場合はカンマを使います. 空の区切り文字は許可され, 要素を直接連結します.");
        addBookPage(root, "operator_player_name", "Outputs the player's name when the input Entity is a player. Non-player entities output an empty String.", "入力 Entity がプレイヤーならプレイヤー名を出力します. プレイヤーでない Entity は空文字列になります.");
        addBookPage(root, "operator_list_search",
                "Keeps elements in the selected List mode matching the pattern (full match with * ? [abc]; Entity, Item, and Block match by registry ID with optional namespace, others as Operator: To String).$(p)In Entity, Item, and Block modes, patterns starting with # match tags instead (e.g., #minecraft:logs).",
                "選択中の List モードからパターンに一致する要素を残します（全体一致で * ? [abc] 対応. Entity・Item・Block はレジストリ ID を名前空間省略可で照合し, その他は演算子: 文字列へ変換と同等）.$(p)Entity・Item・Block モードでは # 始まり（例: #minecraft:logs）でタグ照合になります.");
        addBookPage(root, "operator_list_search_exclude",
                "Removes elements in the selected List mode matching the pattern (full match with * ? [abc]; Entity, Item, and Block match by registry ID with optional namespace, others as Operator: To String).$(p)In Entity, Item, and Block modes, patterns starting with # match tags instead (e.g., #minecraft:logs).",
                "選択中の List モードからパターンに一致する要素を除きます（全体一致で * ? [abc] 対応. Entity・Item・Block はレジストリ ID を名前空間省略可で照合し, その他は演算子: 文字列へ変換と同等）.$(p)Entity・Item・Block モードでは # 始まり（例: #minecraft:logs）でタグ照合になります.");
        addBookPage(root, "operator_list_name_search", "Keeps only the elements of the selected List mode (Entity, Item or Block) whose display name matches the pattern. Names are the same as Selector: Display Name: a player's name, the custom name of an entity or item, and otherwise the name in the caster's language, so that last kind can give different results for casters with different languages. * matches any text, ? one character, and [abc] one of the listed characters. The whole name must match and case matters; use *Zombie* for a partial match.", "選択中の List モード (Entity, Item, Block) から, 表示名がパターンに一致する要素だけを残します. 表示名は 取得子: 表示名 と同じで, プレイヤーはプレイヤー名, 名前を付けた Entity や Item はその名前, それ以外は術者の言語での名前になります. 最後の場合は, 術者の言語によって結果が変わります. * は任意の文字列, ? は任意の1文字, [abc] は a/b/c のいずれか1文字に一致します. 比較は全体一致で大文字と小文字を区別し, 部分一致には *ゾンビ* のように前後に * を付けます.");
        addBookPage(root, "operator_list_name_search_exclude", "Removes the elements of the selected List mode (Entity, Item or Block) whose display name matches the pattern. Names are the same as Selector: Display Name: a player's name, the custom name of an entity or item, and otherwise the name in the caster's language, so that last kind can give different results for casters with different languages. * matches any text, ? one character, and [abc] one of the listed characters. The whole name must match and case matters; use *Zombie* for a partial match.", "選択中の List モード (Entity, Item, Block) から, 表示名がパターンに一致する要素を除きます. 表示名は 取得子: 表示名 と同じで, プレイヤーはプレイヤー名, 名前を付けた Entity や Item はその名前, それ以外は術者の言語での名前になります. 最後の場合は, 術者の言語によって結果が変わります. * は任意の文字列, ? は任意の1文字, [abc] は a/b/c のいずれか1文字に一致します. 比較は全体一致で大文字と小文字を区別し, 部分一致には *ゾンビ* のように前後に * を付けます.");
        addBookPage(root, "operator_list_nbt_search", "Keeps the elements of the selected List mode (Entity, Item or Block) whose NBT value at the NBT Path matches the pattern. Values become text as in the String mode of Selector: NBT Value, and the whole text must match (* ? [abc], case kept). If the path reaches several values, one match is enough; elements where it reaches nothing are not kept. Unlike Operator: List Keep Matches, there is no namespace rule and no # tags.", "選択中の List モード (Entity, Item, Block) から, NBTパスの値がパターンに一致する要素だけを残します. 値は 取得子: NBT値 の文字列モードと同じ形にして, 全体一致で比べます (* ? [abc], 大文字と小文字を区別). パスが複数の値に当たるときは1つ一致すれば十分で, 何にも当たらない要素は残しません. 演算子: リスト一致抽出 と違い, 名前空間の省略や # のタグはありません.");
        addBookPage(root, "operator_list_nbt_search_exclude", "Removes the elements of the selected List mode (Entity, Item or Block) whose NBT value at the NBT Path matches the pattern. Values become text as in the String mode of Selector: NBT Value, and the whole text must match (* ? [abc], case kept). If the path reaches several values, one match is enough; elements where it reaches nothing are kept. Unlike Operator: List Remove Matches, there is no namespace rule and no # tags.", "選択中の List モード (Entity, Item, Block) から, NBTパスの値がパターンに一致する要素を除きます. 値は 取得子: NBT値 の文字列モードと同じ形にして, 全体一致で比べます (* ? [abc], 大文字と小文字を区別). パスが複数の値に当たるときは1つ一致すれば十分で, 何にも当たらない要素は残します. 演算子: リスト一致除外 と違い, 名前空間の省略や # のタグはありません.");
        addBookPage(root, "operator_list_nbt_range", "Keeps the elements of the selected List mode (Entity, Item or Block) whose number at the NBT Path is between Min and Max, both included; an unconnected bound is open. Only number values count, as in the Number mode of Selector: NBT Value, so elements without one are dropped. If the path reaches several numbers, one in range is enough. To compare: at least a = Min a; at most a = Max a; equal to a = Min and Max both a.", "選択中の List モード (Entity, Item, Block) から, NBTパスの数値が最小以上かつ最大以下の要素だけを残します. 未接続の側は制限しません. 数値は 取得子: NBT値 の数値モードと同じで, 数値のない要素は残りません. パスが複数の数値に当たるときは, 1つが範囲内なら残します. 比べ方: a以上 = 最小a, a以下 = 最大a, aと等しい = 最小も最大もa.");
        addBookPage(root, "operator_list_nbt_range_outside", "Keeps the elements of the selected List mode whose number at the NBT Path is below Min or above Max; with neither connected, nothing is out of range. Elements without a number are dropped, as in Operator: List Keep NBT In Range. If the path reaches several numbers, one out of range is enough. To compare: greater than a = Max a; less than a = Min a; not equal to a = Min and Max both a.", "選択中の List モードから, NBTパスの数値が最小より小さいか最大より大きい要素だけを残します. どちらも未接続なら何も残りません. 数値のない要素は 演算子: NBT範囲抽出 と同じく残りません. パスが複数の数値に当たるときは, 1つが範囲外なら残します. 比べ方: aより大きい = 最大a, aより小さい = 最小a, aと等しくない = 最小も最大もa.");
        addBookPage(root, "operator_random_element", "Returns one random element from the input List.", "入力された List から要素を1つランダムに返します.");
        addBookPage(root, "operator_list_distance_keep", "Sorts the elements of the selected List mode (Entity, Vector or Block) by their distance from the position and keeps some of them. A positive Number keeps that many of the closest, a negative Number keeps the farthest, and 0 gives an empty List. Vector and Block Lists come out closest first (farthest first for a negative Number). An Entity List always keeps Psi's own order, so only which entities remain changes. Entities are measured at their feet, as in Operator: Closest to Point, and blocks at their center.", "選択中の List モード (Entity, Vector, Block) の要素を, 位置からの距離で並べて一部だけ残します. 数値が正なら近い方からその数だけ, 負なら遠い方から残し, 0 なら空の List になります. Vector と Block の List は近い順 (数値が負なら遠い順) に並びます. Entity の List は Psi の決まった順に並び直るので, どのエンティティが残るかだけが変わります. エンティティは 演算子: 最も近いエンティティ と同じく足元で, ブロックは中心で距離を測ります.");
        addBookPage(root, "operator_list_distance_element", "Sorts the elements of the selected List mode (Entity, Vector or Block) by their distance from the position, closest first, and returns the element at the Number. Counting starts at 0: 0 is the closest element and 1 the next. A negative Number counts from the farthest, so -1 is the farthest element. A Number outside the List is an error. Entities are measured at their feet, so 0 gives the same entity as Operator: Closest to Point; blocks are measured at their center.", "選択中の List モード (Entity, Vector, Block) の要素を位置から近い順に並べ, 数値の番号にある要素を返します. 番号は 0 始まりで, 0 が一番近い要素, 1 がその次です. 負の数は遠い方から数え, -1 が一番遠い要素です. List の範囲外の番号はエラーになります. エンティティは足元で距離を測るので, 0 は 演算子: 最も近いエンティティ と同じエンティティになります. ブロックは中心で測ります.");
        addBookPage(root, "selector_nbt_number_list", "Reads every element of the selected List mode (Entity, Item or Block) as Selector: NBT Value does in Number mode and outputs the numbers as a Number List in the List's order (an Entity List is in Psi's own order), so the two line up. An element without a number at the NBT Path gives the Fallback, or 0. Example: with the NBT Path Health, Operator: List Keep By Value with -1 keeps the entity with the most health.", "選択中の List モード (Entity, Item, Block) の要素を 取得子: NBT値 の数値モードと同じように読み, リストと同じ順の Number List にします (Entity の List は Psi の決まった順). そのためリストと要素が対応します. NBTパスに数値がない要素は既定値, 既定値もなければ0です. 例: NBTパスを Health にして 演算子: 基準値順抽出 に -1 を渡すと, 体力が一番多いエンティティが残ります.");
        addBookPage(root, "operator_list_value_range", "Keeps the elements of the selected List mode whose value is between Min and Max, both included. Values is a Number List with one value per element, in the same order, such as the output of Selector: NBT Number List. An unconnected Min or Max does not limit that side. The order of the List is kept. If the sizes differ, the spell fails with an error.", "選択中の List モードから, 基準値が最小以上かつ最大以下の要素だけを残します. 基準値は要素ごとに1つずつ同じ順に並んだ Number List で, 取得子: NBT数値リスト の出力などを使います. 最小や最大を接続しないと, その側は制限しません. リストの順は保ちます. 要素数が違うとエラーになります.");
        addBookPage(root, "operator_list_value_keep", "Sorts the elements of the selected List mode by Values and keeps some of them, like Operator: List Keep By Distance. A positive Number keeps that many with the smallest values, a negative Number the largest, and 0 gives an empty List. Lists other than Entity come out smallest first (largest first for a negative Number); an Entity List keeps Psi's own order. Equal values keep the List's order. If the sizes differ, the spell fails with an error.", "選択中の List モードの要素を基準値で並べ, 演算子: 距離順抽出 と同じように一部だけ残します. 数値が正なら基準値の小さい方からその数だけ, 負なら大きい方から残し, 0 なら空の List になります. Entity 以外の List は小さい順 (数値が負なら大きい順) に並び, Entity の List は Psi の決まった順に並び直ります. 同じ値の要素はリストの順を保ちます. 要素数が違うとエラーになります.");
        addBookPage(root, "operator_list_value_element", "Sorts the elements of the selected List mode by Values, smallest first, and returns the element at the Number. Counting starts at 0, and a negative Number counts from the largest, so -1 is the element with the largest value. Equal values keep the List's order. A Number outside the List, or Lists of different sizes, is an error.", "選択中の List モードの要素を基準値の小さい順に並べ, 数値の番号にある要素を返します. 番号は 0 始まりで, 負の数は大きい方から数え, -1 が基準値の一番大きい要素です. 同じ値の要素はリストの順を保ちます. 範囲外の番号や, 要素数の違いはエラーになります.");
        addBookPage(root, "operator_list_add", "Adds up to three elements to the List.", "List に要素を最大3つ追加します.");
        addBookPage(root, "operator_list_remove", "Removes up to two elements from the List.", "List から要素を最大2つ削除します.");
        addBookPage(root, "operator_list_remove_indices", "Removes the elements at Number A and Number B from the selected List mode. Negative indexes count from the end, so -1 removes the last element. Both indexes are resolved against the original List, and duplicate indexes remove only one element. An out-of-range index raises an out-of-bounds spell error.", "選択中の List モードから数値Aと数値Bに対応するインデックスの要素を削除します. 負数は末尾から数え, -1 は末尾要素を削除します. どちらのインデックスも元の List を基準に解決し, 同じインデックスの場合は1要素だけ削除します. 範囲外はインデックス範囲外エラーになります.");
        addBookPage(root, "operator_list_insert", "Inserts the input element before the specified index in the selected List mode. Index 0 inserts at the beginning and -1 inserts before the last element. Indexes above the List size insert at the end, while indexes below the negative range insert at the beginning.", "選択中の List モードの指定インデックス直前へ入力要素を挿入します. 0 は先頭へ, -1 は末尾要素の直前へ挿入します. List サイズより大きいインデックスは末尾へ, 負方向の範囲を超えたインデックスは先頭へ挿入します.");
        addBookPage(root, "operator_list_slice", "Returns a selected range of elements from the selected List mode as a new List. If A is omitted, the range starts at the beginning; if B is omitted, it continues to the end. Negative numbers count from the end, out-of-range values are clamped to the valid range, and an end position at or before the start returns an empty List. Decimal values are truncated toward zero.", "選択中の List モード に対しインデックスを指定して範囲を切り出して新しい List として返します. A を省略すると先頭から, B を省略すると末尾までが対象です. 負数は末尾から数え, 範囲外は有効範囲へ丸め, 終了位置が開始位置以前なら空の List を返します. 小数は0方向へ切り捨てます.");
        addBookPage(root, "operator_list_size", "Outputs the number of elements in the List.", "List の要素数を出力します.");
        addBookPage(root, "operator_list_exclusion", "Returns a new List by removing elements from List 1 when they are also present in List 2.", "入力したList 1 の要素のうち, List 2 にも含まれる要素を除外して新しいリストとして返します.");
        addBookPage(root, "operator_list_intersection", "Returns a new List containing elements that are present in both input Lists.", "入力した2つのリストでどちらにも含まれる要素を新しいリストとして返します.");
        addBookPage(root, "operator_list_concatenation", "Combines the two input Lists. For Contextual Value Lists, duplicate elements are removed.", "入力された2つのリストを結合します. Contextual Value List の場合, 重複した要素は削除されます.");
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
        addBookPage(root, "operator_matrix_multiply_vector", "Treats the Vector or Number List as a column vector and multiplies it by the matrix on the left.", "Vector または Number List を列ベクトルとして扱い, 左側の行列を掛けます.");
        addBookPage(root, "operator_matrix_column_from_list", "Converts a Number List or Vector into a column matrix. The input must contain 1 to 4 elements.", "Number List または Vector を列行列に変換します. 入力は1〜4要素である必要があります.");
        addBookPage(root, "operator_matrix_flatten", "Flattens the matrix into a Number List in row-major order.", "行列を行優先の順序で Number List に平坦化します.");
        addBookPage(root, "operator_matrix_identity", "Creates an identity matrix of the given size. The size must be 1 to 4.", "指定した次数の単位行列を作ります. 次数は1〜4である必要があります.");
        addBookPage(root, "operator_matrix_zero", "Creates a zero matrix. If the second size is omitted, a square matrix is returned.", "ゼロ行列を作ります. 第2のサイズを省略すると正方行列を返します.");
        addBookPage(root, "operator_matrix_diagonal", "Creates a diagonal matrix from a Number List or Vector. The input must contain 1 to 4 elements.", "Number List または Vector から対角行列を作ります. 入力は1〜4要素である必要があります.");
        addBookPage(root, "operator_matrix_replace_column", "Replaces the specified column with a Number List or Vector. Missing rows are filled with zero, and specifying a column beyond the current size expands the matrix with zeros.", "指定列を Number List または Vector で置換します. 足りない行は0で埋められ, 現在のサイズを超える列を指定するとゼロで拡張されます.");
        addBookPage(root, "operator_matrix_replace_row", "Replaces the specified row with a Number List or Vector. Missing columns are filled with zero, and specifying a row beyond the current size expands the matrix with zeros.", "指定行を Number List または Vector で置換します. 足りない列は0で埋められ, 現在のサイズを超える行を指定するとゼロで拡張されます.");
        addBookPage(root, "operator_matrix_replace_element", "Replaces the element at the zero-based [row, column] indices with the input number. The indices list must contain exactly two numbers.", "0始まりの [行, 列] で指定した成分を入力数値で置換します. 成分指定用のリストは必ず2つの数値を含む必要があります.");
        addBookPage(root, "operator_matrix_delete_row", "Deletes the row at the zero-based index from the matrix. The matrix must have at least 2 rows so that the result is not empty.", "0始まりのインデックスで指定した行を行列から削除します. 結果が空にならないよう, 元の行列は2行以上である必要があります.");
        addBookPage(root, "operator_matrix_delete_column", "Deletes the column at the zero-based index from the matrix. The matrix must have at least 2 columns so that the result is not empty.", "0始まりのインデックスで指定した列を行列から削除します. 結果が空にならないよう, 元の行列は2列以上である必要があります.");
        addBookPage(root, "operator_matrix_cuboid_region", "Builds a 4x4 matrix that defines an axis-aligned cuboid centered at the input position. Whole-number size components are treated as block counts around the block that contains the input position: odd counts are centered on that block, positive even counts include one extra block in the positive direction, and negative even counts include one extra block in the negative direction. Non-integer components keep continuous edge lengths. Pass the matrix to Operator: Region Vector List to turn the region into block coordinates before using Trick: Mass Block Break. In Number mode the Size is one number used for all three axes, so a Size of 5 makes a 5x5x5 cube.", "入力位置を中心とする軸平行直方体を表す 4×4 行列を生成します. 整数のサイズ成分はブロック数として扱われ, 入力位置が入っているブロックを基準にします. 奇数はそのブロックを中心に対称, 正の偶数は正方向へ1ブロック多く, 負の偶数は負方向へ1ブロック多く含みます. 非整数の成分は連続的な辺の長さとして扱われます. 作動式: 大規模ブロック破壊で使う前に, 演算子: 領域ベクトルリスト化へ渡して領域をブロック座標に変換します. Number モードではサイズを1つの数値で受け取り, 3軸すべてに使うので, サイズ5なら5×5×5の立方体になります.");
        addBookPage(root, "operator_matrix_cuboid_corners.0",
                "Builds an axis-aligned 4×4 cuboid matrix from opposite corners Vector A and Vector B (order does not matter). Block mode (default) includes both corner blocks; Vector mode uses the raw points (flat zero-thickness boxes error when used).$(p)Pass to zone tricks, Operator: Inside Region, or Operator: Region Vector List (up to 4,096 blocks).",
                "「ベクトル A」「ベクトル B」を向かい合う角（順不同）とする軸平行直方体の 4×4 行列を作ります. Block モード（既定）は両角のブロックを含む箱を作り, Vector モードは2点をそのまま角にします（同座標の軸があると厚さ0でエラー）.$(p)領域魔法の「行列」や演算子: 領域内, 演算子: 領域ベクトルリスト化（最大4,096ブロック）に渡して使えます.");
        addBookPage(root, "operator_region_vector_list", "Returns all block position vectors inside a 3D parallelepiped defined by a 3x4 or 4x4 matrix. The first three columns are the edge vectors and the fourth column is the start point. Vectors are returned in fixed local u, v, and w order. The operator does not inspect the world, so air, unloaded chunks, and unbreakable blocks are not filtered. The returned Vector List can be passed to Trick: Mass Block Break. Regions above the shared internal limit raise an error.", "3×4 または 4×4 の行列で定義された平行六面体の内部にあるすべてのブロック座標を Vector List として返します. 最初の3列は辺ベクトル, 第4列は開始点です. Vector は局所u, v, w軸の固定順序で返されます. ワールド状態は確認しないため, 空気, 未読み込みチャンク, 破壊不能ブロックも除外されません. 返した Vector List は作動式: 大規模ブロック破壊へ渡せます. 領域が共通の内部上限を超える場合はエラーになります.");
        String regionModesEn = "Choose the list type in the mode menu. Entity (default) tests the center of each bounding box, Vector tests each vector as given, and Block tests block positions, the same points Operator: Region Vector List returns.";
        String regionModesJa = "リストの種類はモードで選びます. Entity（既定）は当たり判定の中心, Vector はそのベクトル, Block はブロックの座標で判定します. Block の判定は 演算子: 領域ベクトルリスト化 が返す座標と同じ基準です.";
        addBookPage(root, "operator_inside_region.0", "Filters a list and returns only the elements inside the parallelepiped defined by a 3x4 or 4x4 matrix. The first three columns are the edge vectors and the fourth column is the start point. Points exactly on the boundary are counted as inside.", "3×4 または 4×4 の行列で定義された平行六面体の内部にある要素だけを残したリストを返します. 最初の3列は辺ベクトル, 第4列は開始点です. 境界面上にあるものも内部とみなします.");
        addBookPage(root, "operator_inside_region.1", regionModesEn, regionModesJa);
        addBookPage(root, "operator_outside_region.0", "Filters a list and returns only the elements outside the parallelepiped defined by a 3x4 or 4x4 matrix. The first three columns are the edge vectors and the fourth column is the start point. Points exactly on the boundary are counted as inside, so they are excluded.", "3×4 または 4×4 の行列で定義された平行六面体の外部にある要素だけを残したリストを返します. 最初の3列は辺ベクトル, 第4列は開始点です. 境界面上にあるものは内部とみなすため, 結果から除外されます.");
        addBookPage(root, "operator_outside_region.1", regionModesEn, regionModesJa);
        addBookPage(root, "operator_alive", "Filters the input Entity List and returns only LivingEntity instances that are currently alive.", "入力した Entity List をフィルターし, 現在生存中の LivingEntity のみを返します.");
        addBookPage(root, "operator_matrix_transform_vector.0", "Transforms a vector with a 3x3 or 4x4 matrix. A 3x3 matrix multiplies [x,y,z]. A 4x4 matrix treats the vector as a homogeneous point [x,y,z,1] and divides the result by w.", "3×3 または 4×4 行列でベクトルを変換します. 3×3 行列では [x,y,z] の列ベクトルとして扱います. 4×4 行列では入力された Vector を列ベクトル [x,y,z,1] として行列との積を計算し, 結果 [x',y',z',w'] の各成分を w' で除算した Vector を返します.");
        addBookPage(root, "operator_matrix_transform_vector.1",
                "Modes: Vector transforms one vector. Vector List transforms each vector of a Vector List and returns them in the same order;"
                        + " if any of them cannot be transformed, such as when w becomes 0, it is an error."
                        + " A Block List also works; its block positions are used.",
                "モード: Vector は1つのベクトルを変換します. Vector List は Vector List の各要素を変換し, 同じ順序で返します."
                        + " w が0になるなど, 変換できない要素が1つでもあるとエラーです."
                        + " Block List もつなぐことができ, ブロックの座標が使われます.");
        addBookPage(root, "operator_matrix_linear_part", "Extracts the top-left 3x3 part of the matrix. Matrices smaller than 3x3 cause an error.", "行列の左上 3×3 部分を取り出します. 3×3 より小さい行列ではエラーになります.");
        addBookPage(root, "operator_weak_raycast", "Raycasts from Position in the Ray direction and returns the position of the first block hit. Water and lava can stop the ray.", "位置から Ray 方向へレイキャストし, 最初に命中したブロック座標を返します. 水や溶岩でも停止します.");
        addBookPage(root, "operator_weak_raycast_axis", "Raycasts from Position in the Ray direction and returns the normal direction of the face of the first block or fluid hit.", "位置から Ray 方向へレイキャストし, 最初に命中したブロックまたは流体の面の法線方向を返します.");
        addBookPage(root, "operator_strong_raycast", "Raycasts from Position in the Ray direction and returns the position of the first block hit. Blocks without a collision shape are ignored.", "位置から Ray 方向へレイキャストし, 最初に命中したブロック座標を返します. 当たり判定のないブロックを無視します.");
        addBookPage(root, "operator_strong_raycast_axis", "Raycasts from Position in the Ray direction and returns the normal direction of the face of the first block with a collision shape hit.", "位置から Ray 方向へレイキャストし, 最初に命中した当たり判定を持つブロックの面の法線方向を返します.");
        addBookPage(root, "macro_caster_raycast", "Raycasts from the caster's position in the look direction and returns the coordinates of the hit block.", "術者の位置から視線方向へレイキャストし, 命中したブロック座標を返します.");
        addBookPage(root, "macro_caster_raycast_axis", "Raycasts from the caster's position in the look direction and returns the normal direction of the hit face.", "術者の位置から視線方向へレイキャストし, 命中した面の法線方向を返します.");
        addBookPage(root, "macro_caster_weak_raycast", "Raycasts from the caster's position in the look direction and returns the coordinates of the hit block or fluid.", "術者の位置から視線方向へレイキャストし, 命中したブロックや流体の座標を返します.");
        addBookPage(root, "macro_caster_weak_raycast_axis", "Raycasts from the caster's position in the look direction and returns the normal direction of the face of the hit block or fluid.", "術者の位置から視線方向へレイキャストし, 命中したブロックや流体の面の法線方向を返します.");
        addBookPage(root, "macro_caster_strong_raycast", "Raycasts from the caster's position in the look direction and returns the coordinates of the hit block with a collision shape.", "術者の位置から視線方向へレイキャストし, 命中した当たり判定のあるブロックの座標を返します.");
        addBookPage(root, "macro_caster_strong_raycast_axis", "Raycasts from the caster's position in the look direction and returns the normal direction of the face of the hit block with a collision shape.", "術者の位置から視線方向へレイキャストし, 命中した当たり判定のあるブロックの面の法線方向を返します.");
        addBookPage(root, "macro_caster_axial_offset", "Moves the given position by Left/Right, Forward/Backward, and Up/Down relative to the caster's horizontal facing. Facing is rounded to the nearest horizontal cardinal direction, and pitch is ignored. Positive values move right, forward, and up; negative values move the opposite way. Unconnected parameters are treated as 0. The mode button switches to vector input: a single vector whose components are distances in the caster's view frame (X = right, Y = up, Z = forward), equivalent to adding the result of Macro: Caster Axial Rotation to the position.", "指定した位置を, 術者の水平方向の向きを基準に左右・前後・上下へ平行移動します. 向きは最も近い水平4方角へ丸められ, 視線の上下成分は無視されます. 正の値で右・前・上へ, 負の値で逆へ移動します. 未接続のパラメータは 0 として扱われます. モードボタンでベクトル入力に切り替えると, 視点座標(X=右, Y=上, Z=前)の距離を成分とするベクトル1つで指定できます. これは位置にマクロ: 術者方角回転の結果を加算するのと同等です.");
        addBookPage(root, "macro_caster_axial_rotation", "Rotates the given direction vector from the caster's view frame into world space. The X component is treated as right, Y as up, and Z as forward relative to the caster's horizontal facing, which is rounded to the nearest cardinal direction; pitch is ignored. For example, (0, 0, 1) returns the caster's facing direction and (1, 0, 0) returns the direction to the caster's right. The vector's magnitude is preserved.", "指定した方向ベクトルを術者の視点座標からワールド座標へ回転します. X成分を右, Y成分を上, Z成分を前として, 最も近い水平4方角へ丸めた術者の向きを基準に変換します. 視線の上下成分は無視されます. 例えば (0, 0, 1) は術者の正面方向, (1, 0, 0) は術者の右方向を返します. ベクトルの大きさは保存されます.");
        addBookPage(root, "macro_caster_axial_offset_3d", "Like Macro: Caster Axial Offset, but the facing is rounded to the nearest of the six axis directions, including straight up and down. When looking straight up or down, forward points vertically and the Up/Down axis becomes horizontal, so offsets form a horizontal plane. This is useful for effects like 3x3 area mining, where the mined plane should become the floor or ceiling when looking up or down. The mode button switches to vector input: a single vector whose components are distances in the caster's view frame (X = right, Y = view-up, Z = forward), equivalent to adding the result of Macro: Caster Axial Rotation (3D) to the position.", "マクロ: 術者方角平行移動と同じですが, 向きは真上・真下を含む6方角の最も近い方向へ丸められます. 真上や真下を向いた時は前後が垂直になり, 上下の軸が水平になるため, オフセット平面が水平に張られます. 上や下を向いた時に掘削平面を床や天井にしたい 3×3 範囲破壊などに有用です. モードボタンでベクトル入力に切り替えると, 視点座標(X=右, Y=視点上, Z=前)の距離を成分とするベクトル1つで指定できます. これは位置にマクロ: 術者方角回転 (3D) の結果を加算するのと同等です.");
        addBookPage(root, "macro_caster_axial_rotation_3d", "Like Macro: Caster Axial Rotation, but the facing is rounded to the nearest of the six axis directions, including straight up and down. The X component is treated as right, Y as view-up, and Z as forward. When looking straight up or down, Z points vertically and Y becomes horizontal, matching the orientation of the view.", "マクロ: 術者方角回転と同じですが, 向きは真上・真下を含む6方角の最も近い方向へ丸められます. X成分を右, Y成分を視点上, Z成分を前として変換します. 真上や真下を向いた時はZが垂直, Yが水平になり, 視点の向きに一致します.");
        addBookPage(root, "macro_face_axial_offset.0",
                "Offsets a position relative to the block face hit by a raycast from the caster (defaults to that hit block if Position is omitted). Positive values move right, inward into the block, and up as viewed (top/bottom faces follow horizontal facing); Vector mode takes (X=right, Y=up, Z=inward). Errors if the raycast misses.",
                "術者のレイキャストが命中したブロック面を基準に位置ベクトルを移動します（「位置」省略時は命中ブロック座標を使用）. 正の値は面から見て右・ブロック内側・上方向へ動き（上下面では術者の水平向き基準）, ベクトル入力モードでは X=右・Y=上・Z=内側の1ベクトルで指定できます（未命中時はエラー）.");
        addBookPage(root, "macro_face_axial_offset.1",
                "When a spell uses multiple Face Axial Offset or Rotation pieces, only the first executed performs the raycast and shares its hit block and face with the rest (unaffected if the block breaks mid-spell; an explicit Position still takes precedence).",
                "同じプログラム内に面基準平行移動や面基準回転が複数ある場合, 最初の1回だけレイキャストを行い, 命中ブロックと面を後続ピースで共有します（途中でブロックを壊しても影響なし. 位置入力がある場合はそちらを優先）.");
        addBookPage(root, "macro_face_axial_rotation.0", "Rotates the specified direction vector according to the orientation of the block face hit by a raycast from the caster. X points right as the face is viewed, Y points toward the top as the face is viewed, and Z points inward into the block. On the top and bottom faces, the caster's horizontal facing determines the rotation within the face. The vector's magnitude is preserved. If the raycast misses, a null-vector error is raised.", "術者からのレイキャストが命中したブロック面の向きに応じて, 指定した方向ベクトルを回転します. Xは面を見た時の右, Yは面を見た時の上, Zはブロック内側方向です. 上面と下面では, 術者の水平向きによって面内の回転を決めます. ベクトルの大きさは保存されます. レイキャストが命中しなかった場合は null vector エラーになります.");
        addBookPage(root, "macro_face_axial_rotation.1", "Face Axial Offset and Face Axial Rotation in the same program share the raycast result of the first one executed. See page 2 of $(l:psitweaks_spell_pieces/macro_face_axial_offset)$(o)Macro: Face Axial Offset$(0)$(/l) for details.", "同じプログラムにある 面基準平行移動 と 面基準回転 は, 最初に実行されたピースのレイキャスト結果を共有します. 詳しくは $(l:psitweaks_spell_pieces/macro_face_axial_offset)$(o)マクロ: 面基準平行移動$(0)$(/l) の2ページ目を参照してください.");
        addBookPage(root, "trick_mass_block_break", "Breaks blocks at the coordinates in a Vector List (a Block List also works). Duplicate positions count once, and only the first positions up to the maximum block count (at most 4096) are processed. The maximum must be a positive integer constant. Drops go into the caster's inventory; overflow drops at the caster's position, or at the focal point if the task is interrupted.", "Vector List（Block List も可）の座標にあるブロックを破壊します. 重複した座標は1回だけ扱い, 先頭から破壊数の上限まで(最大4096)を処理します. 上限は正の整数定数である必要があります. ドロップは術者のインベントリに入り, 入りきらない分は術者の位置(タスクが中断されたときは焦点位置)に落ちます.");
    }

    private void addZoneFeeder(JsonObject root) {
        root.addProperty("message.psitweaks.zone_feeder.status", switch (locale) {
            case "ja_jp" -> "%s / %s FE・送り先の荷電領域: %s";
            default -> "%s / %s FE, Charged Zones drawing: %s";
        });
        root.addProperty("psi.book.page.psitweaks_machine.zone_feeder", switch (locale) {
            case "ja_jp" -> "荷電領域へ FE を送り込むブロックです. どの面からも FE を入出力でき（1 tick に究極ユニバーサルケーブル相当まで）, 壊してもためた FE は残ります.";
            default -> "Feeds FE into Charged Zones. Accepts and outputs FE from any side (up to Ultimate Universal Cable throughput per tick) and retains stored FE when broken.";
        });
        root.addProperty("tooltip.psitweaks.zone_feeder.stored", "%s / %s FE");
        root.addProperty("psi.book.page.psitweaks_machine.zone_feeder.1", switch (locale) {
            case "ja_jp" -> "置いた人の$(l:psitweaks_spell_pieces/trick_charged_zone)荷電領域$(/l)がこのブロックを範囲に含むと, 領域自身の充電能力で足りない分の FE をここから補って配ります. 供給中は光り, 素手で右クリックすると残量を確認できます.$(p)ケーブルは範囲の外から引いてください（範囲内のケーブルには領域からの FE が逆流します）.";
            default -> "When the owner's $(l:psitweaks_spell_pieces/trick_charged_zone)Charged Zone$(/l) covers this block, it draws stored FE here to cover demand beyond the zone's own capacity. Glows while feeding; right-click with an empty hand to check stored FE.$(p)Connect cables from outside the zone, as cables inside also receive the zone's FE.";
        });
    }

    private void addZoneReceiver(JsonObject root) {
        boolean japanese = "ja_jp".equals(locale);
        root.addProperty("message.psitweaks.zone_receiver.on", japanese ? "領域受電器: 入" : "Zone Receiver: On");
        root.addProperty("message.psitweaks.zone_receiver.off", japanese ? "領域受電器: 切" : "Zone Receiver: Off");
        root.addProperty("tooltip.psitweaks.zone_receiver.effect", japanese
                ? "持っていると, 荷電領域の中で持ち物の FE を受け取るアイテムに給電されます"
                : "While carried, Charged Zones send FE to the items you carry that take FE");
        root.addProperty("tooltip.psitweaks.zone_receiver.toggle", japanese
                ? "右クリックで入と切を切り替えます"
                : "Right-click to switch it on or off");
        root.addProperty("tooltip.psitweaks.zone_receiver.on", japanese ? "状態: 入" : "State: On");
        root.addProperty("tooltip.psitweaks.zone_receiver.off", japanese ? "状態: 切" : "State: Off");
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
            case "ja_jp" -> "手持ちで使える魔法構築機です. 右クリックすると, 魔法構築機と同じスペルプログラム画面を開くことができます.";
            default -> "A handheld Spell Programmer. Right-click to open the same spell programming screen as the Spell Programmer.";
        });
        root.addProperty("psi.book.page.psitweaks_item.portable_spell_programmer.1", switch (locale) {
            case "ja_jp" -> "メインハンドに携帯型魔法構築機を, オフハンドに術式弾やCADを持ち, スニーク右クリックすると, 編集中の魔法を登録します. CADなどでは, 現在選択している術式弾に登録されます.";
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
            case "ja_jp" -> "魔法演算領域スロットに装備し, 事象干渉力を30%%高めます.$(p)素材に $(item)魔法師の頭$(0) を使います.";
            default -> "Equips in the Magic Calculation Area slot and increases Phenomenon Interference by 30%%.$(p)It is made from a $(item)Magician's Head$(0).";
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
            case "ja_jp" -> "$(item)トンネラー$(0) は, $(item)グラヴストリンガー$(0) の専用弾です. ブロックを貫通し, 防御力や耐性も無効化します. 射手自身には命中しません.$(p)矢はmobに命中しても消滅せず貫通し, 同じmobには1秒のクールタイム後に再度命中します.";
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
            case "ja_jp" -> "ヘルメットは外装センサーに対応し, チェストプレートはダメージ時, レギンスはtick時, ブーツはジャンプ時に詠唱します.$(p)各部位は事象干渉力+10%%, Psi回復量+5, 最大Psi量+500を付与します.";
            default -> "The helmet accepts exosuit sensors, the chestplate casts on damage, the leggings cast on tick, and the boots cast on jump.$(p)Each piece grants +10%% Phenomenon Interference, +5 Psi regeneration, and +500 maximum Psi.";
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
            case "ja_jp" -> "PsiTweaks は魔法師向けのMekaSuitモジュールを3種類追加します. $(item)サイオン供給ユニット$(0) と $(item)サイオン容量ユニット$(0) はMekaSuit胴体に装着し, Psi回復速度と最大Psi保有量を強化します. これらはPsi本体のAttributeを使用します.$(p)$(item)事象干渉力増大ユニット$(0) はMekaSuitヘルメットに装着し, 事象干渉力を高めます.";
            default -> "PsiTweaks adds three MekaSuit modules for spellcasters. The $(item)Psyon Supplying Unit$(0) and $(item)Psyon Capacity Unit$(0) install in the MekaSuit body armor and improve Psi regeneration and maximum Psi. These bonuses use Psi's own attributes.$(p)The $(item)Phenomenon Interference Enhancement Unit$(0) installs in the MekaSuit helmet and increases Phenomenon Interference.";
        });
    }

    private void addMaterialBookAdditions(JsonObject root) {
        root.addProperty("psi.book.page.psitweaks_material.pellet_neptunium", switch (locale) {
            case "ja_jp" -> "$(item)ポロニウムブロック$(0) を $(l:psitweaks_spell_pieces/trick_material_mutation)$(o)作動式: 物質変成$(0)$(/l) で変成するか, $(l:psitweaks_machines/material_mutator)$(o)物質変成機$(0)$(/l) で加工して作成します.$(p)MekaSuit用の $(item)事象干渉力増大ユニット$(0) に使用します.";
            default -> "Made by mutating a $(item)Polonium Block$(0) with $(l:psitweaks_spell_pieces/trick_material_mutation)$(o)Trick: Material Mutation$(0)$(/l), or by processing it in the $(l:psitweaks_machines/material_mutator)$(o)Material Mutator$(0)$(/l).$(p)Used for the MekaSuit $(item)Phenomenon Interference Enhancement Unit$(0).";
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
            case "ja_jp" -> "術式弾の強化は前段階の術式弾を4つ必要とするため, 非常に高コストです.";
            default -> "Upgrading a spell bullet requires four bullets from the previous tier, making it very expensive.";
        });
        root.addProperty("psi.book.page.psitweaks_spell_bullets.2", switch (locale) {
            case "ja_jp" -> "上位のTierになるほどコスト効率が著しく改善し, 強力な魔法を簡単に発動できるようになります.";
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
                {"inherit_hint", "個別設定に切り替えると, 現在の共通設定をコピーします.", "Switching to individual settings copies the current shared settings."},
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
                {"redstone_hint", "%s面の動作条件です. 信号はどの面から受けてもブロック全体で共有し, 停止中はこの面の搬入・搬出をすべて止めます.",
                        "Condition for the %s face. A signal on any face counts for the whole block. While inactive, this face stops all input and output."},
                {"redstone_all.always", "全6面: 常時", "All: Always"},
                {"redstone_all.high", "全6面: 信号あり", "All: Powered"},
                {"redstone_all.low", "全6面: 信号なし", "All: Unpowered"},
                {"redstone_all.mixed", "全6面: 個別", "All: Mixed"},
                {"redstone_all_hint", "全6面の条件をまとめて切り替えます. 面ごとに違う場合は常時に揃えます.",
                        "Switches all six faces together. Mixed faces are reset to Always."},
                {"face.down", "下", "Down"}, {"face.up", "上", "Up"},
                {"face.north", "北", "North"}, {"face.south", "南", "South"},
                {"face.west", "西", "West"}, {"face.east", "東", "East"},
                {"help", "アイテムを持って左クリック, 液体・化学物質の容器を持って右クリックで公開枠に設定します. 資源は消費しません.",
                        "Click a slot with an item (left) or a fluid/chemical container (right) to set it. Nothing is consumed."},
                {"energy_hint", "公開枠にドラッグしてFEを設定", "Drag onto a published slot to assign FE"},
                {"template_too_large", "このアイテムは設定データが大きすぎます.", "This item's filter data is too large."},
                {"amount", "イデアストレージ内: %s", "In Ideaspace Storage: %s"},
                {"clear_hint", "何も持たずに右クリックで解除", "Right-click with an empty cursor to clear"},
                {"load_failed", "ストレージを読み込めません.", "Storage could not be loaded."},
                {"input_filters", "搬入フィルタ", "Input filter"},
                {"input_list", "搬入フィルタ（9枠）", "Input filter (9 slots)"},
                {"input_mode.any", "すべて許可", "Accept all"},
                {"input_mode.allow_list", "許可リスト", "Allow list"},
                {"input_mode.deny_list", "拒否リスト", "Deny list"},
                {"input_mode.existing", "在庫のある種類のみ", "Stored types only"},
                {"input_hint", "全6面の搬入に適用します. アイテムを持って左クリック, 液体・化学物質の容器を持って右クリックで登録します. 耐久値などのデータまで一致するものが対象で, FEは常に受け入れます.",
                        "Applies to input on all six faces. Click a slot with an item (left) or a fluid/chemical container (right) to register it. Only resources with identical data, such as durability, match. FE is always accepted."},
                {"stock_settings", "在庫条件", "Stock rules"},
                {"stock_common_title", "在庫条件(共通)", "Stock rules (Shared)"},
                {"stock_slot_title", "在庫条件(枠 %s)", "Stock rules (Slot %s)"},
                {"minimum_stock", "開始在庫", "Min. storage stock"},
                {"target_stock", "目標在庫", "Target stock"},
                {"minimum_stock_hint", "ストレージにこの量以上あるときだけ自動搬出します. 0で条件なし.",
                        "Auto export runs only while storage holds at least this amount. 0 = no condition."},
                {"target_stock_hint", "搬出先にある同じ資源がこの量になるまで自動搬出します.",
                        "Auto export tops the destination up to this amount of the same resource."},
                {"target_disabled", "無効", "Off"},
                {"stock_hint", "目標在庫: 空欄で無効", "Target stock: blank = off"},
                {"copy", "コピー", "Copy"},
                {"paste", "貼り付け", "Paste"},
                {"copy_hint", "イデアコネクターの設定をクリップボードにコピーします. Ctrl+Cでも操作可能です.",
                        "Copies this connector's settings to the clipboard. Ctrl+C also works."},
                {"paste_hint", "クリップボードからイデアコネクターの設定を貼り付けます. Ctrl+Vでも操作可能です.",
                        "Pastes connector settings from the clipboard. Ctrl+V also works."},
                {"clipboard.copied", "設定をクリップボードにコピーしました", "Copied the settings to the clipboard"},
                {"clipboard.copy_too_large", "設定データが大きすぎるためコピーできません", "The settings are too large to copy"},
                {"clipboard.pasted", "設定を貼り付けました", "Pasted the settings"},
                {"clipboard.pasted_skipped", "設定を貼り付けました. この環境で使えない資源 %s 件は空欄にしました", "Pasted the settings. %s resources unavailable here were left empty"},
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
                "作動式: イデアコネクター", "指定位置に, 自分のイデアストレージへ接続するイデアコネクターを設置します.");
        addBookPage(root, "trick_ideaspace_connector.0",
                "Places an Ideaspace Connector linked directly to your Ideaspace Storage at the input position (never expires; breaking drops no item and keeps storage intact). Incoming items, fluids, chemicals (with Mekanism), and FE go straight to storage; right-click to publish up to 9 resources for extraction. Only the owner can configure it, and it works offline while chunk-loaded.",
                "位置(Vector)に自分のイデアストレージと直結するイデアコネクターを設置します（時間経過で消えず, 破壊してもドロップなし・在庫は保持）. 搬入されたアイテム・液体・化学物質（Mekanism 導入時）・FE は直接ストレージに入り, 右クリックで最大9種類を公開して外部機器から搬出できます. 設定変更は設置者のみで, チャンク読込中ならオフラインでも稼働します.");
        addBookPage(root, "trick_ideaspace_connector.1",
                "All six faces default to I/O enabled with automatic export off. Face I/O, redstone mode (always / powered / unpowered), export rate/interval, and stock rules (minimum storage stock and destination target stock) can be overridden per published slot.$(p)The Input filter (all faces) restricts accepted resources via Accept all, Allow list, Deny list, or Stored types only (FE is always accepted).",
                "初期状態では全6面から搬入・搬出でき, 自動搬出は無効です. 面設定・動作条件（常時／信号あり／信号なし）・自動搬出の量と間隔・在庫条件（開始在庫・目標在庫）は共通設定を基本に公開枠ごとに上書きできます.$(p)「搬入フィルタ」（全6面共通）では, すべて許可／許可リスト／拒否リスト／在庫のある種類のみ（FE は常時許可）の4方式で受け入れを制限できます.");
        addBookPage(root, "trick_ideaspace_connector.2",
                "Use Copy / Paste (or Ctrl+C / Ctrl+V) on the main page to transfer published slots, face settings, export rates, stock rules, and input filters as JSON between connectors. Removing sections from the JSON leaves those parts unchanged; faces keep their world directions.",
                "メイン画面の「コピー」「貼り付け」（Ctrl+C / Ctrl+V）で, 公開枠・面設定・搬出量・在庫条件・搬入フィルタを JSON として別のコネクターへ複製できます. JSON から一部項目を削ればその箇所を変更せずに貼り付けられ, 面は方角（東西南北等）のまま適用されます.");
    }

    private void addIdeaStorageSelectorPages(JsonObject root) {
        addBookPage(root, "selector_idea_storage_last_transfer.0",
                "Returns the amount actually moved by the most recent Ideaspace transfer trick (Deposit, Withdraw, Slot Deposit/Withdraw in any mode) executed earlier in the same cast, or 0 if none ran, moved nothing, or failed.$(p)Because tricks run in reading order from top left, connect this selector only to tricks placed after the transfer.",
                "同じ詠唱内で直前に実行されたイデア転送作動式（格納・取出・スロット格納・スロット取出の全モード）が実際に移した量を Number で取得します（未実行・移動量0・エラー時は0）.$(p)作動式は左上から順に実行されるため, 転送より後ろに配置した作動式へ接続して使います.");
        addBookPage(root, "selector_idea_storage_amount.0",
                "Returns how much of a resource your Ideaspace Storage holds as a Number (Item: count, Fluid/Chemical: mB, FE: FE).$(p)Item mode takes an ID String or Item; Fluid and Chemical modes take an ID String (FE takes no input). Items and fluids with the same ID are summed across data components; invalid IDs, empty items, and missing stock return 0.",
                "自分のイデアストレージにある資源量を Number で取得します（モード: アイテム I＝個数, 液体 F・化学物質 C＝mB, FE＝FE）.$(p)アイテムは ID の String か Item, 液体・化学物質は ID の String を入力します（FE は入力なし）. アイテムと液体は同じ ID ならデータコンポーネント違いも合算し, 不正 ID・空 Item・在庫なしは0を返します.");
        addBookPage(root, "selector_idea_storage_free_capacity.0",
                "Returns how much more of a resource your Ideaspace Storage can accept as a Number (Item, Fluid, Chemical, or FE mode; returns 0 for a new type when the type limit is full).$(p)Item mode takes an ID String (no extra data) or Item (with its data components); Fluid and Chemical modes take an ID String (FE takes no input). Invalid inputs and Chemical mode without Mekanism return 0.",
                "自分のイデアストレージにあとどれだけ格納できるかを Number で取得します（モード: アイテム I・液体 F・化学物質 C・FE. 種類数上限に達している場合, 新規種類は0）.$(p)アイテムは ID の String（追加データなし）か Item（データ込み）, 液体・化学物質は ID の String を入力します（FE は入力なし）. 不正入力や Mekanism 未導入時の化学物質モードは0を返します.");
        addBookPage(root, "selector_idea_storage_type_count",
                "Returns the number of types of one resource kind currently stored in your Ideaspace Storage, as a Number."
                        + " The mode picks Item (I), Fluid (F) or Chemical (C)."
                        + " Items and fluids with different data components count as separate types, as in the storage's type limit.",
                "自分のイデアストレージに現在格納されている, ある種類の資源の種類数をNumberとして取得します. "
                        + "モードでアイテム（I）・液体（F）・化学物質（C）を選びます. "
                        + "アイテムと液体は, ストレージの種類数上限と同じく, データコンポーネントが異なるものを別の種類として数えます.");
    }

    private void addIdeaResourceLogistics(JsonObject root) {
        root.addProperty("psitweaks.spellerror.idea_storage_transfer_failed", switch (locale) {
            case "ja_jp" -> "外部機器とのイデア転送に失敗しました. 詳細はサーバーログを確認してください";
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
                    ? "位置のブロックの指定面から, モードで選んだ資源（アイテム・液体・化学物質・FE）を自分のイデアストレージへ格納します."
                    : "自分のイデアストレージから, モードで選んだ資源（アイテム・液体・化学物質・FE）を位置のブロックの指定面へ取り出します.";
            addSpellPiece(root, id, "Trick: Ideaspace Storage - " + (deposit ? "Deposit" : "Withdraw"), en,
                    "作動式: イデアストレージ-" + (deposit ? "格納" : "取出"), jp);
            if (!deposit) {
                addBookPage(root, id + ".0",
                        "Withdraws the chosen resource (Item I, Fluid F, Chemical C, or FE) from your Ideaspace Storage into the specified block face, up to Number items, Power * 1000 mB, or Power * 4000 FE (rounded down).$(p)Filters, block rules, and Exact Transfer (I=, F=, C=) match $(l:psitweaks_spell_pieces/trick_idea_storage_deposit)$(o)Trick: Ideaspace Storage - Deposit$(0)$(/l); multiple matching types are withdrawn in turn up to the limit.",
                        "自分のイデアストレージから位置のブロックの指定面へ, モードで選んだ資源（アイテム I・液体 F・化学物質 C・FE）を取り出します. 上限はアイテムが「数値」個, 液体・化学物質が威力×1000 mB, FE が威力×4000 FE です（端数切り捨て）.$(p)フィルタ・ブロック転送・完全転送（I=・F=・C=）の仕様は$(l:psitweaks_spell_pieces/trick_idea_storage_deposit)$(o)作動式: イデアストレージ-格納$(0)$(/l)と同じで, 複数種類が一致する場合は上限まで順に取り出します.");
                continue;
            }
            addBookPage(root, id + ".0",
                    "Deposits the chosen resource (Item I, Fluid F, Chemical C, or FE) from the specified block face into your Ideaspace Storage, up to Number items, Power * 1000 mB, or Power * 4000 FE (rounded down).$(p)Follows face I/O rules, errors on protected blocks, and skips unloaded chunks, your own Ideaspace Connector (except FE), and Chemical mode without Mekanism.",
                    "位置のブロックの指定面から, 選んだモードの資源（アイテム I・液体 F・化学物質 C・FE）を自分のイデアストレージへ格納します. 上限はアイテムが「数値」個, 液体・化学物質が威力×1000 mB, FE が威力×4000 FE です（端数切り捨て）.$(p)ブロックの面設定に従い, 保護ブロックではエラー, 未ロードチャンク・自分のイデアコネクター（FE 以外）・Mekanism 未導入時の化学物質モードでは作用しません.");
            addBookPage(root, id + ".1",
                    "The filter is optional (absent in FE mode). Fluid and Chemical modes match ID Strings with * and ? wildcards, optional namespace omission, or # tag matching.$(p)Item mode adapts to the connected type: String (ID/wildcard/#tag), Item (same type), Item List (any match), or Operator: Strict Item (matches data components such as enchantments and durability).",
                    "フィルタは省略可能です（FE モードはなし）. 液体・化学物質は資源 ID を String で照合し, *・?・名前空間省略・# タグ指定に対応します.$(p)アイテムモードは接続した値で照合方法が決まります：String（ID／ワイルドカード／# タグ）, Item（その種類）, Item List（いずれか）, 演算子: 厳密一致アイテム（エンチャントや耐久値等のデータまで一致）.");
            addBookPage(root, id + ".2",
                    "Exact Transfer modes (I=, F=, C=; white badge) move the exact full amount of a single resource type or nothing at all without raising an error.$(p)Different items, data components, fluids, or chemicals are never combined; the first qualifying type is moved (fluid deposits require a single full drain, while matching chemicals across tanks combine).",
                    "FE 以外は完全転送モード（I=・F=・C=, 白地バッジ）も選べます. 1種類の資源を指定量ちょうど移せる場合のみ実行し, 足りない場合は何も移さず終了します（エラーにはなりません）.$(p)別アイテムやデータ違いを合算せず, 条件を満たす最初の1種類を転送します（液体格納は1回の排出で全量が必要, 化学物質は同種の複数タンクを合算）.");
        }
    }

    private void addIdeaSlotLogistics(JsonObject root) {
        root.addProperty("psitweaks.spellerror.idea_storage_slot_transfer_failed", switch (locale) {
            case "ja_jp" -> "指定スロットとのイデア転送に失敗しました. 詳細はサーバーログを確認してください";
            default -> "Ideaspace transfer with the target slot failed. Check the server log for details";
        });
        String enSlot = "The target slot matches Psi's Trick: Place Block (default: hotbar slot right of the CAD; changeable via Trick: Switch Focus Slot or Trick: Slot Change (Inventory)). Skips the active CAD or casting tool slot, and errors if you have no CAD.";
        String jpSlot = "指定スロットは Psi の作動式: ブロック設置(単)等と共通です（既定は CAD の右隣で, 作動式: 指定スロット切替や Trick: Slot Change (Inventory) で変更可）. 使用中の CAD や詠唱中の道具があるスロットには作用せず, CAD 未所持時はエラーになります.";

        String depositEn = "Deposits the item in the spell's target slot into your Ideaspace Storage, up to the count given by Number.";
        String depositJp = "術式の指定スロットにあるアイテムを, 自分のイデアストレージへ格納します. 上限は「数値」で指定した個数です.";
        addSpellPiece(root, "trick_idea_storage_deposit_slot", "Trick: Ideaspace Storage - Slot Deposit", depositEn,
                "作動式: イデアストレージ-スロット格納", depositJp);
        addBookPage(root, "trick_idea_storage_deposit_slot.0",
                depositEn + " Moves as much as fits (fractions rounded down).$(p)The filter is optional (unconnected or empty matches any item) and uses the same matching rules as $(l:psitweaks_spell_pieces/trick_idea_storage_deposit)$(o)Trick: Ideaspace Storage - Deposit$(0)$(/l).",
                depositJp + " 空きが足りない場合は移せる分だけ移します（端数切り捨て）.$(p)フィルタは省略可能（未接続・空文字なら種類を問わず格納）で, 照合方法は$(l:psitweaks_spell_pieces/trick_idea_storage_deposit)$(o)作動式: イデアストレージ-格納$(0)$(/l)と同じです.");
        addBookPage(root, "trick_idea_storage_deposit_slot.1", enSlot, jpSlot);

        String withdrawEn = "Withdraws items from your Ideaspace Storage into the spell's target slot, up to the count given by Number.";
        String withdrawJp = "自分のイデアストレージから, 術式の指定スロットへアイテムを取り出します. 上限は「数値」で指定した個数です.";
        addSpellPiece(root, "trick_idea_storage_withdraw_slot", "Trick: Ideaspace Storage - Slot Withdraw", withdrawEn,
                "作動式: イデアストレージ-スロット取出", withdrawJp);
        addBookPage(root, "trick_idea_storage_withdraw_slot.0",
                withdrawEn + " Fills up to the item's max stack size (fractions rounded down).$(p)Occupied slots only top up with identical data components; empty slots take the first type matching the filter (does nothing if empty with no filter). Filter rules match $(l:psitweaks_spell_pieces/trick_idea_storage_deposit)$(o)Trick: Ideaspace Storage - Deposit$(0)$(/l).",
                withdrawJp + " 最大スタック数まで入ります（端数切り捨て）.$(p)既存アイテムがあるスロットにはデータまで同一のものを補充し, 空スロットにはフィルタに一致する最初の種類を取り出します（空＋フィルタなしは何もしません）. フィルタ照合は$(l:psitweaks_spell_pieces/trick_idea_storage_deposit)$(o)作動式: イデアストレージ-格納$(0)$(/l)と同じです.");
        addBookPage(root, "trick_idea_storage_withdraw_slot.1", enSlot, jpSlot);
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
                "ブロックの資源の読み取りに失敗しました. 詳細はサーバーログを確認してください");

        addSpellPiece(root, "operator_strict_item", "Operator: Strict Item",
                "Turns an Item or Item List into a filter that also compares data components.",
                "演算子: 厳密一致アイテム", "Item または Item List を, データコンポーネントまで比べるフィルタに変えます.");
        addBookPage(root, "operator_strict_item",
                "Turns an Item, or every item of an Item List, into a strict Item. Connect it to an item filter, such as the filter of"
                        + " Trick: Ideaspace Storage - Deposit/Withdraw, the Slot tricks, or the Item modes of"
                        + " Trick: Pull Item and Trick: Send Item, to match only items with the same data components, such as enchantments, durability and names."
                        + " The count is never compared. An empty Item is an error; empty elements of a list are skipped.",
                "Item, または Item List の各アイテムを, 厳密一致の Item に変えます. 作動式: イデアストレージ-格納・取出のフィルタ, スロット系, "
                        + "作動式: アイテム引き出し・アイテム転送のItemモードなどのフィルタにつなぐと, "
                        + "エンチャント・耐久値・名前などのデータコンポーネントまで一致するものだけが対象になります. "
                        + "個数は比べません. 空のItemはエラーで, リストの空の要素は無視します.");
        addSpellPiece(root, "operator_block_item", "Operator: Block Item",
                "Gets the item that Pick Block gives for the block, as an Item.",
                "演算子: ブロックのアイテム化", "ブロックをブロック選択した時のアイテムを, Item として取得します.");
        addBookPage(root, "operator_block_item",
                "Outputs the item that Pick Block gives for the block, as an Item: wheat seeds for a wheat crop, and an empty Item for air."
                        + " It works as an Item filter, for example in Trick: Ideaspace Storage - Slot Withdraw.",
                "ブロックをブロック選択した時のアイテムを Item として出力します. "
                        + "小麦の作物なら小麦の種, 空気なら空の Item です. "
                        + "Item フィルタとして作動式: イデアストレージ-スロット取出などに使えます.");

        addSpellPiece(root, "operator_block_resource_amount", "Operator: Block Resource Amount",
                "Outputs the items, fluids, chemicals or FE stored in a block, chosen by the mode. String narrows it down by ID, or by tag when it starts with #.",
                "演算子: ブロック資源量", "ブロック内のアイテム・液体・化学物質・FEの量を, モードに応じて出力します. 「文字列」でID（#で始めるとタグ）を絞り込めます.");
        addBookPage(root, "operator_block_resource_amount.0",
                "Outputs how much a block holds of the kind chosen in the mode menu: items (count), fluids or chemicals (mB), or FE."
                        + " Direction picks the face to read, following the block's per-face input and output; unconnected, the whole block is read.",
                "モードで選んだ種類について, ブロックが持つ量を出力します（アイテムは個数, 液体と化学物質はmB, FEはFE）. "
                        + "「方向」で読み取る面を選べ, 面ごとの入出力に従います. "
                        + "接続しない場合はブロック全体を読みます.");
        addBookPage(root, "operator_block_resource_amount.1",
                "The optional String filters by ID with * (any text) and ? (any single character); omit the namespace, such as \"minecraft:\", to match across all mods."
                        + " Start with # to match tags instead, such as #c:ingots."
                        + " Unconnected or empty, everything is counted."
                        + " Blocks with no such storage, in unloaded chunks or in other dimensions read as 0, as do chemicals without Mekanism."
                        + " A protected block fails with an error.",
                "任意の「文字列」は資源IDで絞り込み, *（任意の文字列）と?（任意の1文字）が使えます. "
                        + "「minecraft:」のような先頭部分を省くと, どのModでも後半部分だけで照合します. "
                        + "#で始めると, #c:ingotsのようにタグで照合します. "
                        + "接続しないか空文字にすると, すべてを数えます. "
                        + "該当する保管先がないブロック, 読み込まれていないチャンクや別のディメンションのブロック, Mekanismがない場合の化学物質は0です. "
                        + "保護されたブロックはエラーになります.");

        addSpellPiece(root, "operator_block_free_capacity", "Operator: Block Free Capacity",
                "Outputs how much more of one item, fluid or chemical ID, or FE, a block can accept now.",
                "演算子: ブロック空き容量", "指定IDのアイテム・液体・化学物質, またはFEを, ブロックがあとどれだけ受け入れられるかを出力します.");
        addBookPage(root, "operator_block_free_capacity.0",
                "Outputs how much more a block accepts right now, by simulating an insertion; nothing moves."
                        + " Everything but String works as in Operator: Block Resource Amount."
                        + " String takes one exact ID such as minecraft:coal: no wildcards, \"minecraft:\" may be omitted, and unknown IDs read as 0.",
                "ブロックが今あとどれだけ受け入れられるかを, 搬入を試算して出力します（実際には移しません）. "
                        + "「文字列」以外は演算子: ブロック資源量と同じです. "
                        + "「文字列」には minecraft:coal のようなIDを1つ指定します. "
                        + "ワイルドカードは使えず, 「minecraft:」は省略でき, 存在しないIDは0です.");
        addBookPage(root, "operator_block_free_capacity.1",
                "Items are simulated slot by slot, so machines whose slots share one pool may report more than really fits."
                        + " A block that accepts without limit reports a very large number."
                        + " Use it to keep machines stocked, for example by withdrawing fuel from your Ideaspace Storage only while a furnace has room for it.",
                "アイテムは1スロットずつ試算するため, スロットが容量を共有する機械では実際より多くなることがあります. "
                        + "上限なく受け入れるブロックは非常に大きな数になります. "
                        + "例えば, かまどに空きがある時だけイデアストレージから燃料を取り出すといった, 機械への補充に使えます.");

        addSpellPiece(root, "operator_dropped_items", "Operator: Dropped Items",
                "Turns the dropped items in an Entity List into an Item List. Other entities are skipped.",
                "演算子: ドロップアイテム", "Entity List のドロップアイテムを Item List に変換します. 他のエンティティは除外します.");
        addBookPage(root, "operator_dropped_items",
                "Turns the dropped items in an Entity List, such as from Selector: Nearby Items, into an Item List, keeping their order and skipping other entities.",
                "取得子: 近くのアイテムなどの Entity List から, ドロップアイテムを同じ順序で Item List に変換します. "
                        + "他のエンティティは除外します.");

        addSpellPiece(root, "operator_matrix_caster_basis", "Operator: Caster Basis Matrix",
                "Builds a 4x4 matrix from the caster's axis-rounded facing (columns: right, up, forward, origin), so regions can follow the caster.",
                "演算子: 術者基準行列", "術者の向きを軸に丸めた 4×4 行列（列: 右・上・前・原点）を作ります. 領域を術者の向きに合わせるのに使います.");
        addBookPage(root, "operator_matrix_caster_basis.0",
                "Builds a 4×4 matrix with the caster's axis-rounded right, up, and forward directions as columns and an origin (Position, or the caster's block if omitted). Modes: 2D (level forward), 3D (includes pitch), and Face (hit block face; errors on miss).",
                "術者の右・上・前をブロック軸に丸めて列に並べ, 4列目を原点（「位置」, 省略時は術者のブロック）とした 4×4 行列を作ります. モードは 2D（水平固定）・3D（視線の上下含む）・面基準（見ているブロック面, 未命中エラー）の3種です.");
        addBookPage(root, "operator_matrix_caster_basis.1",
                "Maps local (X=right, Y=up, Z=forward) to world space: left-multiply a region matrix around (0, 0, 0) via Operator: Matrix Multiply to orient it with the caster (e.g., building a forward tunnel for Trick: Mass Block Break).",
                "局所座標（X=右・Y=上・Z=前）をワールド座標へ写すため, (0, 0, 0) 基準の領域に左から演算子: 行列積で掛けると術者の向きへ追従させられます（例: 演算子: 直方体領域と組み合わせて前方の掘削領域を作成）.");

        addSpellPiece(root, "operator_matrix_rotation", "Operator: Rotation Matrix",
                "Builds a 4x4 matrix that rotates around Axis by Angle in radians, like Operator: Rotate Vector.",
                "演算子: 回転行列", "Axis（軸）の周りに Angle（ラジアン）だけ回す 4×4 行列を作ります. Operator: Rotate Vector と同じ回転です.");
        addBookPage(root, "operator_matrix_rotation.0",
                "Builds a 4x4 matrix that rotates around Axis, through the origin, by Angle in radians, the same rotation as Operator: Rotate Vector."
                        + " Quarter turns come out exact, so rotated regions keep their block positions."
                        + " A zero axis is an error.",
                "原点を通る Axis（軸）の周りに Angle（ラジアン）だけ回す 4×4 行列で, Operator: Rotate Vector と同じ回転です. "
                        + "90度単位の回転は誤差なく計算されるので, 回転した領域でもブロック座標がずれません. "
                        + "軸がゼロベクトルだとエラーです.");
        addBookPage(root, "operator_matrix_rotation.1",
                "To rotate around a point P instead, chain two Operator: Matrix Multiply pieces to get (translation by P) × (this matrix) × (translation by -P), making the translations with Operator: Translation Matrix.",
                "点Pの周りで回すには, 演算子: 平行移動行列と2つの演算子: 行列積で (Pの平行移動) × (この行列) × (-Pの平行移動) を作ります.");

        addSpellPiece(root, "operator_matrix_translation", "Operator: Translation Matrix",
                "Builds a 4x4 matrix that moves points by Offset.",
                "演算子: 平行移動行列", "点を「オフセット」だけ移動させる 4×4 行列を作ります.");
        addBookPage(root, "operator_matrix_translation",
                "Builds a 4x4 matrix that moves points by Offset."
                        + " Put it to the left of a region matrix in Operator: Matrix Multiply to move the region, or apply it with Operator: Matrix Transform Vector to move a position."
                        + " With Operator: Rotation Matrix, it rotates a region around any point.",
                "点を「オフセット」だけ移動させる 4×4 行列を作ります. "
                        + "演算子: 行列積で領域行列の左に置くと領域を移動でき, 演算子: 行列でベクトル変換で使うと座標を移動できます. "
                        + "演算子: 回転行列と組み合わせると, 任意の点の周りで領域を回転できます.");

        addSpellPiece(root, "operator_matrix_set_y_axis", "Operator: Matrix Set Y Axis",
                "Turns a region matrix around its center so that its Y axis points along Direction.",
                "演算子: Y軸方向指定", "領域行列をその中心のまわりに回し, Y 軸を「方向」へ向けます.");
        addBookPage(root, "operator_matrix_set_y_axis.0",
                "Rotates a 3×4 or 4×4 region matrix around its center by the smallest turn so its Y axis (column 2) points along Direction (size and center unchanged; exact for quarter turns; zero Direction errors).$(p)Useful for aiming Trick: Gravity Zone in Matrix mode (zones always apply within the axis-aligned box enclosing the turned matrix).",
                "領域行列（3×4 または 4×4）を中心のまわりに最小回転させ, Y 軸（2列目）を「方向」へ向けます（箱のサイズと中心は不変, 90度回転は誤差なし, ゼロベクトルはエラー）.$(p)作動式: 加重領域の行列モードなどで任意の向きへ押す領域を作るときに便利です（領域は回転後の箱を囲む軸平行範囲で効きます）.");

        addSpellPiece(root, "trick_soak_zone", "Trick: Soak Zone",
                "Places a Resident SpellGram zone that keeps living beings inside it soaked",
                "作動式: 湿潤領域", "範囲の中の生物を濡らし続ける常駐魔法式を置く");
        addSpellPiece(root, "trick_gravity_zone", "Trick: Gravity Zone",
                "Places a Resident SpellGram zone that keeps pushing entities inside it in the given direction",
                "作動式: 加重領域", "範囲の中のエンティティを指定した向きへ押し続ける常駐魔法式を置く");
        addSpellPiece(root, "trick_heat_zone", "Trick: Heat Zone",
                "Places a Resident SpellGram zone that sets living beings inside it on fire and melts ice",
                "作動式: 加熱領域", "範囲の中の生物に火をつけ, 氷を溶かす常駐魔法式を置く");
        addSpellPiece(root, "trick_freeze_zone", "Trick: Freeze Zone",
                "Places a Resident SpellGram zone that freezes living beings inside it and turns open water into ice",
                "作動式: 凍結領域", "範囲の中の生物を凍えさせ, 水を氷にする常駐魔法式を置く");
        addSpellPiece(root, "trick_turbulence_zone", "Trick: Turbulence Zone",
                "Places a Resident SpellGram zone where a gale turns projectiles aside and strengthens your other zones",
                "作動式: 乱流領域", "暴風で飛び道具をそらし, 重ねた自分の領域を強める常駐魔法式を置く");
        addSpellPiece(root, "selector_own_spellgrams", "Selector: Own Resident SpellGrams",
                "Lists your own Resident SpellGrams (circles and zones) in this dimension, in the order you placed them",
                "取得子: 自分の常駐魔法式", "このディメンションにある自分の常駐魔法式（サークルと領域）を, 置いた順のリストで返す");
        addBookPage(root, "selector_own_spellgrams",
                "Lists the Resident SpellGrams (circles and zones) you placed in this dimension, however far away they are, oldest first."
                        + " An Autonomous Caster gets only the ones it placed itself. The core of a Meteor Line night is not a Resident SpellGram and is not included.",
                "術者が置いた, このディメンションの常駐魔法式（サークルと領域）を, 距離に関係なく, 置いた順（古い順）のリストで返します. "
                        + "自律詠唱機は, その機械が置いたものだけを返します. 流星群の夜の核は常駐魔法式ではないので含みません.");
        addSpellPiece(root, "trick_recall_spellgram", "Trick: Recall SpellGram",
                "Takes back your own Resident SpellGrams",
                "作動式: 魔法式回収", "自分の常駐魔法式を消す");
        addBookPage(root, "trick_recall_spellgram",
                "Ends your own Resident SpellGrams given as the Target (an entity or a list, such as Selector: Own Resident SpellGrams), however far away they are."
                        + " If anything else is in the target, nothing is ended and the spell errors. Psi is not refunded.",
                "「対象」に入れた自分の常駐魔法式を, 距離に関係なく消します. エンティティでもリストでもよく, 取得子: 自分の常駐魔法式をそのまま渡せます. "
                        + "自分のもの以外や常駐魔法式でないもの（流星群の夜の核など）が混ざっているとエラーになり, 何も消しません. Psi は戻りません.");
        addSpellPiece(root, "selector_spellgram_integrity", "Selector: SpellGram Integrity",
                "Returns the current integrity of a SpellGram object",
                "取得子: 魔法式耐久値", "魔法式オブジェクトの今の耐久値を返す");
        addBookPage(root, "selector_spellgram_integrity",
                "Returns the current integrity of the Target, a SpellGram object (a Resident SpellGram, the core of a Meteor Line night or a Micro Black Hole), as a number."
                        + " Any other entity is an error. It is the integrity shown when you aim at the core (the display rounds it up),"
                        + " so a spell can tell how much is left before Trick: Extend SpellGram or before taking a SpellGram apart.",
                "「対象」に入れた魔法式オブジェクト（常駐魔法式, 流星群の夜の核, マイクロブラックホール）の, 今の耐久値を数値で返します. "
                        + "魔法式オブジェクトでないエンティティを入れるとエラーになります. "
                        + "値は核に照準を合わせたときに表示される耐久値と同じで（表示は切り上げ）, 作動式: 魔法式延長を使う前などに, 残りを術式で確かめられます.");
        addSpellPiece(root, "selector_spellgram_integrity_list", "Selector: SpellGram Integrity List",
                "Returns the current integrity of each SpellGram object in the List, as a Number List in the same order",
                "取得子: 魔法式耐久値リスト", "リストの魔法式オブジェクトそれぞれの今の耐久値を, 同じ順の Number List で返す");
        addBookPage(root, "selector_spellgram_integrity_list",
                "Returns the current integrity of every SpellGram object in the List as a Number List, in the List's order (an Entity List is in Psi's own order),"
                        + " so it can be the Values of Operator: Value-Ranked Element and the other value-ranked list operators. The numbers are the same as Selector: SpellGram Integrity."
                        + " Any element that is not a SpellGram object is an error. Example: with Selector: Own Resident SpellGrams as the List,"
                        + " Operator: Value-Ranked Element with 0 picks your SpellGram with the least integrity for Trick: Extend SpellGram.",
                "リストに入れた魔法式オブジェクトそれぞれの今の耐久値を, リストと同じ順（Entity の List は Psi の決まった順）の Number List で返します. "
                        + "演算子: 基準値順要素などの基準値にそのまま使えます. 値は取得子: 魔法式耐久値と同じで, 魔法式オブジェクトでない要素があるとエラーになります. "
                        + "例: 取得子: 自分の常駐魔法式をリストに入れて, 演算子: 基準値順要素に0を渡すと, 耐久値が一番少ない自分の常駐魔法式を作動式: 魔法式延長に渡せます.");
        addSpellPiece(root, "trick_extend_spellgram", "Trick: Extend SpellGram",
                "Restores the integrity of your own Resident SpellGram, so it lasts longer",
                "作動式: 魔法式延長", "自分の常駐魔法式の耐久値を回復し, 長く残す");
        addBookPage(root, "trick_extend_spellgram",
                "Restores the integrity of your own Resident SpellGram given as the Target, however far away it is. Its cost is 100 per Power (a constant of at most 100)."
                        + " It gives back the share of the integrity the object had when placed that this cost is of what the object cost when placed, so costly objects are harder to restore."
                        + " Integrity lost to spells comes back first. Integrity wears away at a steady rate, so anything restored beyond that makes the object last longer."
                        + " Integrity can be restored up to a cap: twice what the object would have had if placed with the longest Time (120 seconds), whatever Time it was placed with.",
                "「対象」に入れた自分の常駐魔法式の耐久値を, 距離に関係なく回復します. コストは威力（最大100の定数）×100です. "
                        + "置いたときのコストのうち, このコストが占める割合だけ, 置いたときの耐久値が戻るので, 多くコストを払ったものほど回復しにくくなります. "
                        + "術式で削られた分から先に戻ります. 耐久値は一定の速さで減るので, それを超えて戻した分だけ長く残ります. "
                        + "回復できる上限は, 領域でもサークルでも, 置いたときの「時間」によらず, 「時間」を最大（120秒）にして置いたときの耐久値の2倍です.");
        addSpellPiece(root, "trick_circle_follow", "Trick: Circle Follow",
                "Makes your own circles follow you or an entity that is not a living being; without a Follow Target, lets them go",
                "作動式: サークル追従", "自分のサークルを, 術者か生き物でないエンティティに追従させる. 追従先を入れなければ放す");
        addLocalized(root, "psitweaks.spellparam.follow_target", "Follow Target", "追従先");
        addLocalized(root, "psitweaks.spellerror.not_circle",
                "Error: The target is not a circle.", "エラー: 対象がサークルではありません.");
        addLocalized(root, "psitweaks.spellerror.invalid_follow_target",
                "Error: The follow target must be the caster or an entity that is not a living being.",
                "エラー: 追従先は術者か, 生き物でないエンティティにしてください.");
        addLocalized(root, "psitweaks.spellerror.follow_target_ridden",
                "Error: A living being other than the caster is riding the follow target.",
                "エラー: 追従先に術者以外の生き物が乗っています.");
        addSpellPiece(root, "trick_interference_zone", "Trick: Interference Zone",
                "Places a Resident SpellGram zone that keeps weaker casters' Psi spells out of it and stops others' ender teleports",
                "作動式: 領域干渉", "自分より事象干渉力の低い他者の魔法と, 術者以外のエンダー系のテレポートを, 範囲の中で止める常駐魔法式を置く");
        addSpellPiece(root, "trick_charged_zone", "Trick: Charged Zone",
                "Places a Resident SpellGram zone that sends FE to the blocks, items and carried things inside it",
                "作動式: 荷電領域", "範囲の中のブロック・持ち物・アイテムに FE を送る常駐魔法式を置く");
        addSpellPiece(root, "trick_silence_zone", "Trick: Silence Zone",
                "Places a Resident SpellGram zone where no sound is heard and no vibration gets through",
                "作動式: 遮音領域", "範囲の中の音を消し, 振動を通さない常駐魔法式を置く");
        addSpellPiece(root, "trick_acceleration_zone", "Trick: Acceleration Zone",
                "Places a Resident SpellGram zone that speeds up the random ticks of growing things inside it",
                "作動式: 加速領域", "範囲の中の育つもののランダムティックを速める常駐魔法式を置く");
        addSpellPiece(root, "trick_deceleration_zone", "Trick: Deceleration Zone",
                "Places a Resident SpellGram zone that slows the living beings and projectiles inside it",
                "作動式: 減速領域", "範囲の中の生き物と飛び道具を遅くする常駐魔法式を置く");
        addSpellPiece(root, "trick_decontamination", "Trick: Decontamination",
                "Places a Resident SpellGram core that clears the radiation sources within 32 blocks of it (needs Mekanism)",
                "作動式: 除染", "周り32ブロックの放射線源を取り除く常駐魔法式の核を置く（Mekanism が必要）");
        addSpellPiece(root, "trick_contamination_zone", "Trick: Contamination Zone",
                "Places a Resident SpellGram zone that exposes the living beings inside it to radiation, up to a limit (needs Mekanism)",
                "作動式: 汚染領域", "範囲の中の生き物を, 上限まで放射線にさらす常駐魔法式を置く（Mekanism が必要）");
        addSpellPiece(root, "trick_show_region", "Trick: Show Region",
                "Draws the 12 edges of a region matrix with particles in your CAD's color.",
                "作動式: 領域表示", "領域行列の12本の辺を, CADの色のパーティクルで描きます.");
        addBookPage(root, "trick_show_region.0",
                "Draws the outline of a region defined by a 3x4 or 4x4 matrix with particle trails in your CAD's color, to check it before working on it."
                        + " Block mode (default) wraps the blocks that Operator: Region Vector List picks from the region; Vector mode draws the parallelepiped exactly as defined.",
                "3×4 または 4×4 の行列で定義された領域の輪郭を, CADの色のパーティクルの軌跡で描きます. "
                        + "作業前の範囲確認に使います. "
                        + "Block モード（既定）は演算子: 領域ベクトルリスト化が選ぶブロックを囲むように, Vector モードは定義どおりの平行六面体を描きます.");
        addBookPage(root, "trick_show_region.1",
                "Time is an optional constant and works like the Time of Trick: Particle Trail: 20 by default, at most 1200, and it must be positive."
                        + " Every corner must be within the spell radius, or casting fails with an error."
                        + " Players near you see the particles too.",
                "「時間」は任意入力の定数で, Trick: Particle Trail の時間と同じく既定値20, 最大1200で, 正の数を指定します. "
                        + "すべての角が術式射程内にない場合はエラーになります. "
                        + "パーティクルは近くのプレイヤーにも見えます.");
    }

    private void addBladePouchTranslations(JsonObject root) {
        boolean japanese = "ja_jp".equals(locale);
        root.addProperty("tooltip.psitweaks.blade_pouch.empty", japanese ? "剣が入っていません" : "No swords inside");
        root.addProperty("tooltip.psitweaks.blade_pouch.count", japanese ? "剣: %s / %s" : "Swords: %s / %s");
        root.addProperty("tooltip.psitweaks.blade_pouch.broken", japanese ? "(破損: 展開されません)" : "(broken: not deployed)");
        root.addProperty("tooltip.psitweaks.blade_pouch.idle",
                japanese ? "待機中: 先に見つかる別のブレードポーチが使われます" : "Idle: another Blade Pouch found first is in use");
        root.addProperty("gui.psitweaks.blade_pouch.deployed", japanese ? "刃を展開中" : "Blades out");
        root.addProperty("psitweaks.spellerror.required_item_missing",
                japanese ? "エラー: 術式に必要なアイテムが存在しません." : "ERROR: An item the spell needs is missing.");
        root.addProperty("psi.book.page.psitweaks_item.blade_pouch.0", japanese
                ? "剣を8本まで入れておけるポーチです. 使うとGUIが開き, $(thing)#minecraft:swords$(0) の剣だけを入れられます.$(p)$(l:psitweaks_spell_pieces/trick_dancing_blades)$(o)作動式: ダンシングブレイズ$(0)$(/l) は, このポーチの剣をすべて展開して斬りつけます. 刃を展開している間は, ポーチの剣を出し入れできません."
                : "A pouch that holds up to 8 swords. Using it opens a GUI that only accepts items in $(thing)#minecraft:swords$(0).$(p)$(l:psitweaks_spell_pieces/trick_dancing_blades)$(o)Trick: Dancing Blades$(0)$(/l) deploys every sword in it. While the blades are out, swords cannot be put in or taken out.");
        root.addProperty("psi.book.page.psitweaks_item.blade_pouch.1", japanese
                ? "使われるポーチは1つだけです. フィルタカードと同じく, Curiosのスロット(どのスロットにも装備できます)を先に, 次にメインハンド, オフハンド, ホットバー, インベントリの順で探し, 最初に見つかったポーチを使います."
                : "Only one pouch is used at a time. As with the Filter Card, the Curios slots (it fits any slot) are searched first, then the main hand, the offhand, the hotbar and the inventory, and the first pouch found is used.");
    }

    /**
     * 1.0.0 advancements on Psi's tab; the tree itself is in {@link PsitweaksAdvancementProvider}.
     * Each line: id, English name, English description, Japanese name, Japanese description.
     */
    private void addAdvancementTranslations(JsonObject root) {
        addAdvancement(root, "alloy_psion_pickup", "One Small Step", "Obtain your first Psionic Alloy",
                "未知への一歩", "サイオニック合金を初めて手に入れる");
        addAdvancement(root, "alloy_psion_assembly_pickup", "Lightweight Champion", "Craft a Psionic Alloy CAD Assembly",
                "効率特化", "サイオニック合金のCAD素体を作る");
        addAdvancement(root, "psionic_control_circuit_pickup", "Perceptron", "Craft a Psionic Control Circuit",
                "パーセプトロン", "サイオニック制御回路を作る");
        // Mekanism only
        addAdvancement(root, "program_researcher_pickup", "Research and Development", "Craft a Program Research Table",
                "研究開発", "プログラム研究台を作る");
        // Mekanism Generators only, hidden
        addAdvancement(root, "phonon_maser_fusion_ignition", "Sonofusion", "Ignite a Fusion Reactor with Trick: Phonon Maser",
                "音響核融合", "作動式: フォノンメーザーで核融合炉を点火する");
        addAdvancement(root, "chaotic_psimetal_pickup", "Perfect Harmony", "Blend Ebony and Ivory into Psimetal",
                "陰陽混合", "エボニーとアイボリーの力をサイメタルに混ぜ合わせる");
        addAdvancement(root, "chaotic_psimetal_assembly_pickup", "Aufheben", "Craft a Chaotic Psimetal CAD Assembly",
                "アウフヘーベン", "カオティックサイメタルのCAD素体を作る");
        addAdvancement(root, "flashmetal_pickup", "In a Flash", "Mix Chaotic Psimetal with Refined Glowstone and refine it",
                "ピカイチ", "カオティックサイメタルと精製グロウストーンを混ぜ合わせて精製する");
        addAdvancement(root, "flashmetal_assembly_pickup", "Faster Than Thought", "Craft a Flashmetal CAD Assembly",
                "電光石火", "フラッシュメタルのCAD素体を作る");
        addAdvancement(root, "psionic_echo_pickup", "Power from Beyond", "Research Trick: Supreme Infusion and use it on an Echo Shard",
                "向こう側からの力", "研究で作動式: 超位注入を解禁し, 残響の欠片に使う");
        addAdvancement(root, "alloy_psionic_echo_pickup", "Good Vibrations", "Infuse Psionic Alloy with Psionic Echo",
                "反響を呼ぶ", "サイオニック合金にサイオニックエコーを注入する");
        addAdvancement(root, "echo_control_circuit_pickup", "Turing Test", "Craft an Echo Control Circuit",
                "チューリングテスト", "感応制御回路を作る");
        addAdvancement(root, "heavy_psimetal_pickup", "Heavy Metal", "Combine echo-infused Netherite Scrap with Flashmetal",
                "重鎮", "サイオニックエコーを注入したネザライトの欠片とフラッシュメタルを合わせる");
        addAdvancement(root, "heavy_psimetal_assembly_pickup", "It's All Greek to Me", "Craft a Heavy Psimetal CAD Assembly, α or β",
                "重みが違う", "ヘビーサイメタルのCAD素体を α型か β型で作る");
        addAdvancement(root, "antinite_pickup", "Two of a Kind", "Mine Antinite Ore on the outer End islands",
                "対を成す", "ジ・エンドの外側の島でアンティナイト鉱石を掘る");
        addAdvancement(root, "hypostasis_gem_pickup", "Awakening", "Research Trick: Material Mutation and use it on a Block of Antinite",
                "目覚め", "研究で作動式: 物質変成を解禁し, アンティナイトブロックに使う");
        addAdvancement(root, "alloy_hypostasis_pickup", "It Knows", "Infuse Echo Alloy with Hypostasis",
                "「それ」は認識する", "感応合金にヒュポスタシスを注入する");
        addAdvancement(root, "hypostasis_control_circuit_pickup", "Ascension is All You Need", "Craft a Hypostasis Control Circuit",
                "Ascension is All You Need", "位格制御回路を作る");
        addAdvancement(root, "psycheonic_metal_pickup", "Entelechy", "Infuse Heavy Psimetal with Hypostasis and craft an ingot from the nuggets",
                "エンテレケイア", "ヘビーサイメタルにヒュポスタシスを注入し, ナゲットからインゴットを作る");
        addAdvancement(root, "psycheonic_metal_assembly_pickup", "Body and Soul", "Craft a Psycheonic Metal CAD Assembly",
                "一心同体", "プシオニックメタルのCAD素体を作る");

        // Psi ships no Japanese for its own tab, so only ja_jp gets these; English stays Psi's.
        if ("ja_jp".equals(locale)) {
            root.addProperty("psi.desc", "第四の壁を壊すジョークです. お楽しみください.");
            addPsiAdvancementJapanese(root, "iron_cad_pickup", "ナイスバディ", "鉄のCAD素体を作る");
            addPsiAdvancementJapanese(root, "gold_assembly_pickup", "値千金", "金のCAD素体を作る");
            addPsiAdvancementJapanese(root, "encyclopaedia_psionica", "タブレットはつけで", "Psi のすべてを記した手引き書, Encyclopaedia Psionica を作る");
            addPsiAdvancementJapanese(root, "psidust", "クラフトできない…?", "初めてサイダストを作る");
            addPsiAdvancementJapanese(root, "psimetal_pickup", "注目の鋳塊", "初めてサイメタルを注入して作る");
            addPsiAdvancementJapanese(root, "psimetal_assembly_pickup", "微サイな進歩", "サイメタルのCAD素体を作る");
            addPsiAdvancementJapanese(root, "psigem_pickup", "思考の結晶", "ダイヤモンドに注入して初めてサイジェムを作る");
            addPsiAdvancementJapanese(root, "ebony_pickup", "成金", "石炭に注入して初めてエボニー実体を作る");
            addPsiAdvancementJapanese(root, "ebony_assembly_pickup", "ガチムチ", "エボニーサイメタルのCAD素体を作る");
            addPsiAdvancementJapanese(root, "ivory_pickup", "真価", "クォーツに注入して初めてアイボリー実体を作る");
            addPsiAdvancementJapanese(root, "ivory_assembly_pickup", "魅惑的", "アイボリーサイメタルのCAD素体を作る");
        }
    }

    private void addAdvancement(JsonObject root, String id, String enUs, String enUsDesc, String jaJp, String jaJpDesc) {
        String key = "advancement.psitweaks:" + id;
        addLocalized(root, key, enUs, jaJp);
        addLocalized(root, key + ".desc", enUsDesc, jaJpDesc);
    }

    private static void addPsiAdvancementJapanese(JsonObject root, String id, String jaJp, String jaJpDesc) {
        root.addProperty("advancement.psi:" + id, jaJp);
        root.addProperty("advancement.psi:" + id + ".desc", jaJpDesc);
    }

    /** 1.0.0 utility tricks: ender chest, crafting table, harvesting, shearing and breeding, and Operator: Connected Blocks. */
    private void addUtilitySpellPieces(JsonObject root) {
        addLocalized(root, "psitweaks.datatype.entity_or_entity_list", "Entity/Entity List", "Entity/Entity List");
        addLocalized(root, "psitweaks.spellerror.mass_harvest_too_many_pending", "Too many pending mass-harvest blocks",
                "保留中の一括収穫ブロック数が上限を超えています");

        addSpellPiece(root, "trick_ender_chest", "Trick: Ender Chest", "Opens the caster's ender chest.",
                "作動式: エンダーチェスト", "術者のエンダーチェストを開きます.");
        addBookPage(root, "trick_ender_chest",
                "Opens the caster's ender chest, with no ender chest block needed.",
                "エンダーチェストがなくても, 術者のエンダーチェストを開きます.");

        addSpellPiece(root, "trick_crafting_table", "Trick: Crafting Table", "Opens a 3x3 crafting grid.",
                "作動式: 作業台", "3×3のクラフト画面を開きます.");
        addBookPage(root, "trick_crafting_table",
                "Opens a 3x3 crafting grid, as if the caster had used a crafting table, with no crafting table needed.",
                "作業台がなくても, 作業台と同じ3×3のクラフト画面を開きます.");

        addSpellPiece(root, "trick_harvest", "Trick: Harvest",
                "Harvests the ripe crop at the position and replants it. The harvest goes into the caster's inventory.",
                "作動式: 収穫", "指定位置の熟した作物を収穫して植え直します. 収穫物は術者のインベントリに入ります.");
        addBookPage(root, "trick_harvest.0",
                "Harvests the ripe crop at the position and replants it with one of its seeds. The harvest goes into the"
                        + " caster's inventory. Cast from a Psimetal tool, the tool's Fortune applies.",
                "指定位置の熟した作物を収穫し, 種を1つ使って植え直します. 収穫物は術者のインベントリに入ります."
                        + " サイメタルの道具から唱えると, その道具の幸運が効きます.");
        addBookPage(root, "trick_harvest.1",
                "Sweet berry bushes and glow berries are only picked. Sugar cane, cactus and bamboo keep their lowest block,"
                        + " and melons and pumpkins are taken while the stem stays. Unripe crops are left alone.",
                "スイートベリーの低木とグロウベリーは実だけを摘みます."
                        + " サトウキビ, サボテン, 竹は一番下のブロックを残し, スイカとカボチャは茎を残して実だけを取ります."
                        + " 熟していない作物はそのままです.");

        addSpellPiece(root, "trick_harvest_sequence", "Trick: Harvest Sequence",
                "Harvests and replants the ripe crops in a line from the position in the direction of the target.",
                "作動式: 収穫(列)", "指定位置から対象の方向へ並ぶ熟した作物を収穫して植え直します.");
        addBookPage(root, "trick_harvest_sequence",
                "Harvests the crops in a line that starts at the position and runs $(thing)Max$(0) blocks in the direction of"
                        + " the target, the same way as $(thing)Trick: Harvest$(0).",
                "指定位置から対象の方向へ $(thing)最大$(0) ブロック並ぶ作物を, $(thing)作動式: 収穫$(0) と同じように収穫します.");

        addSpellPiece(root, "trick_mass_harvest", "Trick: Mass Harvest",
                "Harvests and replants the ripe crops at the coordinates in a Vector List (a Block List also works). The harvest goes into the caster's inventory.",
                "作動式: 大規模収穫", "Vector List（Block List も可）の座標にある熟した作物を収穫して植え直します. 収穫物は術者のインベントリに入ります.");
        addBookPage(root, "trick_mass_harvest.0",
                "Harvests the crops at the first $(thing)Max$(0) coordinates (at most 4096) in a Vector List (a Block List also"
                        + " works), the same way as $(thing)Trick: Harvest$(0).",
                "Vector List（Block List も可）の座標にある作物を, 先頭から $(thing)最大$(0) 個 (上限4096) まで, $(thing)作動式: 収穫$(0) と同じように収穫します.");
        addBookPage(root, "trick_mass_harvest.1",
                "If any position is out of range, the spell fails before harvesting anything. The crops are harvested a little"
                        + " at a time, and the caster gets the whole harvest at the end.",
                "座標が1つでも射程外にあると, 何も収穫せずにエラーになります."
                        + " 収穫は少しずつ進み, 収穫物は終わったときにまとめて術者に届きます.");

        addSpellPiece(root, "trick_shear", "Trick: Shear",
                "Shears the target as shears would. What comes off goes into the caster's inventory.",
                "作動式: 毛刈り", "対象をハサミで刈ったときと同じように刈ります. 刈り取ったものは術者のインベントリに入ります.");
        addBookPage(root, "trick_shear.0",
                "Shears the target, an Entity or every entity of an Entity List, as if the caster had used shears on it. Mobs"
                        + " from other mods that shears work on are sheared too.",
                "対象 (Entity, または Entity List の各エンティティ) を, 術者がハサミを使ったときと同じように刈ります."
                        + " ハサミが使える他Modの生物も刈れます.");
        addBookPage(root, "trick_shear.1",
                "What comes off goes into the caster's inventory. Targets that cannot be sheared right now are skipped, and if"
                        + " any target is out of range, the spell fails.",
                "刈り取ったものは術者のインベントリに入ります. 今は刈れない対象は飛ばし, 対象が1体でも射程外にいるとエラーになります.");

        addSpellPiece(root, "trick_breed", "Trick: Breed",
                "Feeds the target animals from the caster's inventory so that they fall in love.",
                "作動式: 繁殖", "術者のインベントリにある餌を対象の動物に与えて, 繁殖モードにします.");
        addBookPage(root, "trick_breed.0",
                "Feeds each target animal (an Entity, or every entity of an Entity List) a breeding food from the caster's"
                        + " inventory so that it falls in love. A warning names an animal the caster has no food for.",
                "対象の動物 (Entity, または Entity List の各エンティティ) に, 術者のインベントリにある餌を1つずつ与えて繁殖モードにします."
                        + " 餌を持っていない動物がいると, 警告で知らせます.");
        addBookPage(root, "trick_breed.1",
                "Babies, animals resting after breeding and others that cannot breed right now get no food. If any target is"
                        + " out of range, the spell fails.",
                "子どもや繁殖後の休憩中の動物など, 今は繁殖できない動物には餌を与えません."
                        + " 対象が1体でも射程外にいるとエラーになります.");

        addSpellPiece(root, "operator_connected_blocks", "Operator: Connected Blocks",
                "Lists the positions of the blocks of the same kind connected to the block at the position.",
                "演算子: 連結ブロック", "指定位置のブロックとつながっている同じ種類のブロックの座標を Vector List で返します.");
        addBookPage(root, "operator_connected_blocks.0",
                "Returns the coordinates of the blocks of the same kind connected to the block at the position (diagonally too)"
                        + " as a Vector List, nearest first, up to $(thing)Max$(0) blocks (at most 4096).",
                "指定位置のブロックとつながっている (斜めも含む) 同じ種類のブロックの座標を, 近い順に $(thing)最大$(0) 個 (上限4096) まで Vector List で返します.");
        addBookPage(root, "operator_connected_blocks.1",
                "Crops of any age and logs facing any way count as one kind. Blocks outside the spell's range are not"
                        + " followed.$(p)Pass the list to $(thing)Trick: Mass Block Break$(0) to fell a tree or mine out a vein of ore,"
                        + " or to $(thing)Trick: Mass Harvest$(0) to harvest a whole field.",
                "成長段階の違う作物や向きの違う原木も, 同じ種類として扱います."
                        + " 射程外のブロックはたどりません.$(p)$(thing)作動式: 大規模ブロック破壊$(0) に渡すと木の伐採や鉱脈の採掘に, $(thing)作動式: 大規模収穫$(0) に渡すと畑全体の収穫に使えます.");
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
                japanese ? "エラー: フィルタカードを持っていません." : "Error: You have no Filter Card.");
        root.addProperty("psitweaks.spellerror.filter_card_slot_empty",
                japanese ? "エラー: フィルタカードの %s 番の枠は空です." : "Error: Slot %s of the Filter Card is empty.");
        root.addProperty("psitweaks.spellerror.filter_card_slot_range",
                japanese ? "エラー: フィルタカードの番号は 1〜9 の整数で指定してください."
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
