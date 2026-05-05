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
        root.addProperty("entity.minecraft.villager.psitweaks.spellcaster", switch (locale) {
            case "ja_jp" -> "魔法師";
            default -> "Magician";
        });
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
        addPsitweaksCategoryBook(root);
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

        addSpellPieces(root);
        addSpellPiecesBook(root);

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
            case "ja_jp" -> "$(thing)PsiTweaks$(0) は $(thing)Psi$(0) の拡張modです. 素材生成の工業化や, 新しい装備, 新しい術式など多数の要素を追加します.$(p)通常のCADや基本術式弾で終わらず, Psiを戦闘やユーティリティとして終盤まで強力に扱う構成を想定しています.";
            default -> "$(thing)PsiTweaks$(0) is an expansion for $(thing)Psi$(0). It adds many elements, including industrialized material production, new gear, and new spells.$(p)The mod is intended for setups where Psi remains powerful through the late game as both combat and utility, rather than ending at ordinary CADs and basic spell bullets.";
        });
        root.addProperty("psi.book.page.psitweaks_overview.2", switch (locale) {
            case "ja_jp" -> "主な追加要素は, 高位術式弾, 新しいCAD素体や詠唱補助具, 新しい魔法, 加工機械や発電機です.$(p)多くのシステムは, Psiと工業環境の橋渡しや, Psiの終盤での能力強化などを目的として作られています.";
            default -> "Major additions include higher-tier spell bullets, new CAD assemblies and casting support tools, new spells, processing machines, and generators.$(p)Most systems are built to bridge Psi with industrial environments and to strengthen Psi's late-game capabilities.";
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
            case "ja_jp" -> "$(l:psitweaks_machines/program_researcher)$(o)$(item)プログラム研究台$(0)$(/l) に必要素材を入れて FE を供給すると, プログラムをクラフトできます.$(p)各研究に必要な素材, 消費電力, 処理時間は JEI で確認できます.";
            default -> "Place the required ingredients in the $(l:psitweaks_machines/program_researcher)$(o)$(item)Program Research Table$(0)$(/l) and supply FE to craft a program.$(p)JEI lists each research recipe's required materials, energy cost, and processing time.";
        });
        root.addProperty("psi.book.entry.psitweaks_magician", switch (locale) {
            case "ja_jp" -> "魔法師";
            default -> "Magician";
        });
        root.addProperty("psi.book.page.psitweaks_magician.0", switch (locale) {
            case "ja_jp" -> "$(thing)魔法師$(0) は PsiTweaks が追加する村人の職業です. Psi機械を扱う村人であり, 魔法師向けの素材や強化装備と関係します.";
            default -> "$(thing)Magicians$(0) are a villager profession added by PsiTweaks. They represent villagers who work with Psi machinery and are connected to several caster-focused materials and upgrades.";
        });
        root.addProperty("psi.book.page.psitweaks_magician.1", switch (locale) {
            case "ja_jp" -> "無職の村人は, $(l:basics/cad_assembler)$(o)$(item)CAD組立機$(0)$(/l) を職業ブロックとして取得すると $(thing)魔法師$(0) になります.$(p)ほかの村人作業台と同じように, 村人が到達できる場所にCAD組立機を置いてください.";
            default -> "An unemployed villager can become a $(thing)Magician$(0) by claiming a $(l:basics/cad_assembler)$(o)$(item)CAD Assembler$(0)$(/l) as its job site.$(p)Place the assembler where a villager can reach it, just like other villager workstation blocks.";
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
        root.addProperty("psi.book.page.psitweaks_cad_and_gear.1", switch (locale) {
            case "ja_jp" -> "これらのレシピは, 携帯型組立, インライン詠唱, 術式保存, 魔法師支援装備の入口です.";
            default -> "These recipes cover portable assembly, inline casting, spell storage, and caster support gear.";
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
        root.addProperty("jei.psitweaks.program_research.energy", switch (locale) {
            case "ja_jp" -> "消費電力: %s FE";
            default -> "Energy: %s FE";
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
            case "ja_jp" -> "PsiTweaks のプログラムアイテムを作成するための電力式研究台です. 入力スロットに必要素材を入れてFEを供給すると, 研究完了時にプログラムを出力します.$(p)各研究の素材, 消費電力, 時間はJEIのプログラム研究レシピで確認できます.";
            default -> "A powered research table for producing PsiTweaks program items. Put the required ingredients in the input slots and supply FE; completed research outputs the program item.$(p)JEI shows each research recipe's energy cost and time.";
        });
        root.addProperty("psi.book.page.psitweaks_machine.sculk_eroder", switch (locale) {
            case "ja_jp" -> "石, 土, 砂などのブロックアイテムをスカルクへ侵食加工する機械です.$(p)自然発生やスカルクカタリストに頼らず, スカルク素材を安定して作りたいときに使います.";
            default -> "A machine that corrodes stone, dirt, sand, and related block items into Sculk outputs.$(p)Use it when you need Sculk materials without relying on natural spread.";
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
            case "ja_jp" -> "$(item)究極ユニバーサルケーブル$(0) の128倍の転送容量を持つ上位ケーブルです.$(p)核融合炉級の電力出力など, 大量のFEを一度に扱う設備に向きます.";
            default -> "An upgraded cable with 128 times the transfer capacity of $(item)Ultimate Universal Cable$(0).$(p)Use it for high-throughput FE lines such as fusion-reactor-class power output.";
        });
        root.addProperty("psi.book.page.psitweaks_machine.transcendent_energy_cube", switch (locale) {
            case "ja_jp" -> "$(item)究極エネルギーキューブ$(0) の128倍の蓄電容量と出力を持つ上位キューブです.$(p)大規模発電や高負荷機械群のバッファとして使えます.";
            default -> "An upgraded energy cube with 128 times the storage and output of $(item)Ultimate Energy Cube$(0).$(p)Use it as a buffer for large generators or high-load machine arrays.";
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
    }

    private void addBookPage(JsonObject root, String id, String enUs, String jaJp) {
        root.addProperty("psi.book.page.psitweaks_spellpiece." + id, switch (locale) {
            case "ja_jp" -> jaJp;
            default -> enUs;
        });
    }

    private void addSpellPieces(JsonObject root) {
        addSpellPiece(root, "trick_explode_no_destroy", "Trick: Antipersonnel Explode", "Cause an explosion that does not destroy blocks", "作動式: 対人爆発", "ブロックを破壊しない爆発を起こす");
        addSpellPiece(root, "trick_barrier", "Trick: Barrier", "Reduce damage taken", "作動式: 障壁", "受けるダメージを減少させる");
        addSpellPiece(root, "trick_hardening", "Trick: Hardening", "When taking heavy damage, reduce it to a certain value", "作動式: 硬化", "大ダメージを受けた時、一定値まで減少させる");
        addSpellPiece(root, "trick_parade", "Trick: Parade", "Avoid damage with a certain probability", "作動式: パレード", "確率で被ダメージを回避する");
        addSpellPiece(root, "trick_flight", "Trick: Flight", "Enabling creative flight", "作動式: 飛行", "クリエイティブ飛行を可能にする");
        addSpellPiece(root, "trick_interact_block", "Trick: Block Interact", "Right-click with the off-hand item on the target block", "作動式: ブロック作用", "座標のブロックに、オフハンドのアイテムで右クリックの動作を行う");
        addSpellPiece(root, "trick_break_fortune", "Trick: Break Block (Fortune)", "Break a block with Fortune applied", "作動式: ブロック破壊(幸運)", "幸運付きでブロックを破壊する");
        addSpellPiece(root, "trick_break_silk", "Trick: Break Block (Silk Touch)", "Break a block with Silk Touch applied", "作動式: ブロック破壊(シルクタッチ)", "シルクタッチ付きでブロックを破壊する");
        addSpellPiece(root, "trick_store_entity", "Trick: Store Entity", "Store the entity's UUID in the CAD memory", "作動式: エンティティ保存", "CADのメモリにエンティティのUUIDを保存する");
        addSpellPiece(root, "selector_stored_entity", "Selector: Stored Entity", "Retrieve entities from the UUID stored in the CAD memory", "取得子: 保存されたエンティティ", "CADのメモリに保存されたUUIDからエンティティを取得する");
        addSpellPiece(root, "selector_nearby_spellgram", "Selector: Nearby SpellGram Object", "Retrieve SpellGram objects around the specified position", "取得子: 近くの魔法式オブジェクト", "指定座標の周囲にある魔法式オブジェクトを取得する");
        addSpellPiece(root, "trick_dispel", "Trick: Dispel", "Remove effects from target entity", "作動式: 解呪", "対象からエフェクトを除去する");
        addSpellPiece(root, "trick_dispel_beneficial", "Trick: Dispel Beneficial", "Remove beneficial effects from target entity", "作動式: 良性解呪", "対象から有益なエフェクトを除去する");
        addSpellPiece(root, "trick_dispel_non_beneficial", "Trick: Dispel Non Beneficial", "Remove non beneficial effects from target entity", "作動式: 悪性解呪", "対象から有益でないエフェクトを除去する");
        addSpellPiece(root, "trick_cocytus", "Trick: Cocytus", "Permanently freeze the target mob's mind", "作動式: コキュートス", "対象のモブの精神を永久に凍結させる");
        addSpellPiece(root, "trick_supply_fe", "Trick: FE Charge", "Supplies FE to blocks. Direction can be specified.", "作動式: FE供給", "ブロックにFEを供給します。方向の指定が可能です。");
        addSpellPiece(root, "trick_time_accelerate", "Trick: Time Accelerate", "Accelerates the time of the target block", "作動式: 時間加速", "対象のブロックの時を加速させます。");
        addSpellPiece(root, "trick_phonon_maser", "Trick: Phonon Maser", "Vibrates ultrasonic waves to emit heat rays", "作動式: フォノンメーザー", "超音波を振動させ熱線を放出する");
        addSpellPiece(root, "trick_supreme_infusion", "Trick: Supreme Infusion", "Infuse Echo Shards into Psionic Echoes", "作動式: 超位注入", "残響の欠片に注入してサイオニックエコーにします");
        addSpellPiece(root, "trick_molecular_divider", "Trick: Molecular Divider", "divide the living creatures within the area", "作動式: 分子ディバイダー", "三点で作られた平面で生物を切断する");
        addSpellPiece(root, "trick_aqua_cutter", "Trick: Aqua Cutter", "Launches a water blade projectile that damages on hit", "作動式: アクアカッター", "水刃の発射体を前方に放ち、命中した対象にダメージを与える");
        addSpellPiece(root, "trick_blaze_ball", "Trick: Blaze Ball", "Launches a fireball forward that deals fire damage on hit", "作動式: ブレイズボール", "前方へ火の玉を発射し、命中した対象へ炎属性ダメージを与える");
        addSpellPiece(root, "trick_radiation_injection", "Trick: Radiation Injection", "Applies radiation exposure to the target", "作動式: 放射線注入", "対象を被ばくさせる");
        addSpellPiece(root, "trick_radiation_filter", "Trick: Radiation Filter", "Applies a radiation filter effect to the target", "作動式: 放射線フィルタ", "対象に放射線防護効果を付与する");
        addSpellPiece(root, "trick_cure_radiation", "Trick: Cure Radiation", "Removes radiation exposure from the target", "作動式: 放射線除去", "対象の被ばく量を除去する");
        addSpellPiece(root, "trick_guillotine", "Trick: Guillotine", "Deals a heavy slash to the target and drops a head on kill", "作動式: ギロチン", "対象に強力な斬撃ダメージを与え、討伐時に頭をドロップさせる");
        addSpellPiece(root, "trick_active_air_mine", "Trick: Active Air Mine", "Deals magic damage to entities inside a spherical area around the target position", "作動式: 能動空中機雷", "指定座標を中心とした範囲内の生物にダメージを与える");
        addSpellPiece(root, "trick_flare_circle", "Trick: Fire Circle", "Places a flare Spell Gram Circle that repeatedly deals fire damage around it", "作動式: ファイアサークル", "炎の魔法式オブジェクトを設置して内部の生物に継続的な炎ダメージを与える");
        addSpellPiece(root, "trick_ice_circle", "Trick: Ice Circle", "Places an ice Spell Gram Circle that repeatedly deals freeze damage around it", "作動式: アイスサークル", "氷の魔法式オブジェクトを設置して内部の生物に継続的な凍結ダメージを与える");
        addSpellPiece(root, "trick_set_spellgram_follow_target", "Trick: Set SpellGram Follow Target", "Sets the follow target entity for a SpellGram object", "作動式: 魔法式追従", "魔法式オブジェクトの追従対象エンティティを設定する");
        addSpellPiece(root, "trick_die_flex", "Trick: Flexible Die", "Stops execution when given a number whose absolute value is less than 1, and refunds Psi cost for skipped pieces. When used in spells that cast every tick, client-side Psi display may temporarily desync.", "作動式: 柔軟停止", "絶対値が1未満の数値を受け取ると術式を停止し、未実行分のPsi消費を返却する。毎tick詠唱する術式に組み込むと、クライアント側のPsi量表示が同期ずれする場合があります");
        addSpellPiece(root, "trick_material_mutation", "Trick: Material Mutation", "Acts on a specific block, alters its material structure, and transmutes it into a different substance.", "作動式: 物質変成", "特定のブロックに作用して物質構造を改変し異なる物質に変成させる");
    }

    private void addSpellPiecesBook(JsonObject root) {
        root.addProperty("psi.book.category.psitweaks_spell_pieces", switch (locale) {
            case "ja_jp" -> "追加術式";
            default -> "Additional Spell Pieces";
        });
        root.addProperty("psi.book.category.psitweaks_spell_pieces.desc", switch (locale) {
            case "ja_jp" -> "PsiTweaks が追加するスペルピースです. 攻撃, 工業処理, 補助用途のものが多く, 設定によっては研究解禁が必要になります.";
            default -> "Spell pieces added by PsiTweaks. Many are offensive, industrial, or utility-focused, and some require research depending on the config.";
        });

        addBookPage(root, "trick_explode_no_destroy", "Creates an explosion that deals damage without destroying blocks. Be careful: dropped items and similar entities can still be erased.", "ブロックを破壊しない爆発を起こしてダメージを与えます. ドロップアイテムなどは消滅するので注意してください.");
        addBookPage(root, "trick_barrier", "Applies a barrier effect that reduces incoming damage. It reduces damage taken by (level * 4).", "被ダメージを軽減する障壁効果を付与します.(レベル * 4)だけ被ダメージを減少させます.");
        addBookPage(root, "trick_hardening", "Applies a hardening effect that limits large incoming damage to a fixed value. Maximum damage taken is capped at (level - 2).", "大きな被ダメージを一定値まで抑える硬化効果を付与します. 受ける最大ダメージを(レベル - 2)に抑えます.");
        addBookPage(root, "trick_parade", "Applies an effect that evades attacks by chance. It evades attacks with a (62.5 + 7.5 * level)% chance.", "確率で攻撃を回避する効果を付与します. (62.5 + 7.5 * レベル) % で攻撃を回避します.");
        addBookPage(root, "trick_flight", "Gives the target an effect that enables creative flight.", "対象にクリエイティブ飛行を可能にする効果を与えます. ");
        addBookPage(root, "trick_interact_block", "Acts on the target block as if right-clicked with the item in the caster's off hand.", "対象ブロックに対して, 術者のオフハンドのアイテムで右クリックしたように作用します.");
        addBookPage(root, "trick_break_fortune", "Breaks the target block with Fortune.", "対象ブロックを幸運付きで破壊します.");
        addBookPage(root, "trick_break_silk", "Breaks the target block with Silk Touch.", "対象ブロックをシルクタッチ付きで破壊します.");
        addBookPage(root, "trick_store_entity", "Stores the target entity's UUID in CAD memory.", "対象エンティティのUUIDをCADメモリに保存します.");
        addBookPage(root, "selector_stored_entity", "Gets an entity from the UUID stored in CAD memory.", "CADメモリに保存されたUUIDからエンティティを取得します. ");
        addBookPage(root, "selector_nearby_spellgram", "Gets SpellGram objects around the specified coordinates. It is mainly used by tricks that control placed SpellGram objects.", "指定座標の周囲にある魔法式オブジェクトを取得します. 設置済みの魔法式オブジェクトを制御する術式で主に使います.");
        addBookPage(root, "trick_dispel", "Removes effects from the target entity. This is the general-purpose dispel that does not distinguish between beneficial and harmful effects.", "対象エンティティからエフェクトを除去します. 良性・悪性を区別しない汎用版の解呪です.");
        addBookPage(root, "trick_dispel_beneficial", "Removes only beneficial effects from the target entity. It is suited for stripping enhancements from hostile targets.", "対象エンティティから有益なエフェクトだけを除去します. 敵対対象の強化を剥がす用途に向いています.");
        addBookPage(root, "trick_dispel_non_beneficial", "Removes non-beneficial effects from the target entity. Use it when you want to cleanse harmful effects while leaving allied buffs intact.", "対象エンティティから有益でないエフェクトを除去します. 味方のバフを残したまま悪性効果を消したい時に使います.");
        addBookPage(root, "trick_cocytus", "Permanently freezes the mind of the target mob. Rather than merely dealing damage, this very powerful control trick prevents action.", "対象モブの精神を永久に凍結させます. 単なるダメージではなく行動を封じる, 非常に強力な制御術式です.");
        addBookPage(root, "trick_supply_fe", "Supplies FE to the target block. When CAD Efficiency is 100, it supplies 20 FE per Psi.", "対象ブロックへFEを供給します. 供給量はCADの効率が100のとき、1psiあたり20FEです.");
        addBookPage(root, "trick_time_accelerate", "Multiplies the target block's tick progression by (2 ^ power). The upper limit is 512x speed.", "対象ブロックのtick進行を (2 ^ 威力) 倍にします.上限は512倍速まで.");
        addBookPage(root, "trick_phonon_maser", "Fires a high-power heat ray using ultrasonic vibration. It is a powerful offensive trick.", "超音波振動による高威力の熱線を放ちます. 攻撃用の強力な術式です.");
        addBookPage(root, "trick_supreme_infusion", "Infusion-converts Echo Shards into Psionic Echo.", "残響の欠片をサイオニックエコーへ注入変換します.");
        addBookPage(root, "trick_molecular_divider", "Cuts living beings along a plane defined by three points. It is a high-power area attack trick.", "三点で定義した平面で生物を切断します. 高威力の範囲攻撃術式です.");
        addBookPage(root, "trick_aqua_cutter", "Fires a water-blade projectile forward as an early-game offensive trick.", "前方へ水刃の発射体を放つ序盤用の攻撃術式です.");
        addBookPage(root, "trick_blaze_ball", "Fires a ball of flame forward as an early-game offensive trick.", "前方へ火の弾を放つ序盤用の攻撃術式です.");
        addBookPage(root, "trick_active_air_mine", "Creates a spherical shockwave at the specified coordinates and damages living beings within range. This is an area attack detonated at a chosen location.", "指定座標に球状の衝撃波を作り, 範囲内の生物にダメージを与えます. 場所を指定して起爆する範囲攻撃です.");
        addBookPage(root, "trick_flare_circle", "Places a fire SpellGram Circle that continuously deals fire damage to living beings inside it. Once placed, the circle remains for 60 seconds.", "炎の魔法式サークルを設置し, 内部の生物に継続的な炎ダメージを与えます. 一度設置したサークルは60秒残り続けます.");
        addBookPage(root, "trick_ice_circle", "Places an ice SpellGram Circle that continuously deals freezing damage to living beings inside it. Like Fire Circle, it is suited for area control.", "氷の魔法式サークルを設置し, 内部の生物に継続的な凍結ダメージを与えます. ファイアサークルと同じく領域制圧に向いています.");
        addBookPage(root, "trick_set_spellgram_follow_target", "Sets the follow target entity of a SpellGram object. For example, by setting the Fire Circle to follow the caster, it will continue to follow the caster and keep burning surrounding enemies.", "魔法式オブジェクトの追従対象エンティティを設定します. 例えば、ファイアサークルの追従対象を術者に設定することで、術者に追従し続け周囲の敵を燃やし続けます.");
        addBookPage(root, "trick_die_flex", "Behaves similarly to Psi's Trick: Die and refunds the Psi cost of spell pieces that were not executed. With high-frequency casting, the client-side Psi display may temporarily desync.", "Psi本体の停止と同様の動作を行い, 未実行分のスペルピースのPsiコストを返還します. 高頻度詠唱ではクライアント側のPsi表示が一時的にずれることがあります.");
        addBookPage(root, "trick_radiation_injection", "Applies Mekanism radiation exposure to the target.", "対象へMekanismの放射線被ばくを付与します. ");
        addBookPage(root, "trick_radiation_filter", "Applies a radiation protection effect to the target, protecting them from radiation.", "対象に放射線防護効果を付与し、放射線の影響から身を守ります.");
        addBookPage(root, "trick_cure_radiation", "Removes the target's radiation exposure.", "対象の被ばく量を除去します.");
        addBookPage(root, "trick_guillotine", "Deals powerful slash damage to the target and makes it drop a head when killed. This is a single-target offensive trick.", "対象に強力な斬撃ダメージを与え, 討伐時に頭をドロップさせます. 単体対象の攻撃術式です.");
        addBookPage(root, "trick_material_mutation", "Breaks specific blocks and transmutes them into other items. The Material Mutator can perform this process using power and Vaporized Psionic Echo.", "特定のブロックを破壊して別のアイテムへ変成させます. 物質変成機はこの処理を電力と気化サイオニックエコーで実行できます.");
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
        root.addProperty("psi.book.page.psitweaks_item.third_eye_device", switch (locale) {
            case "ja_jp" -> "魔法演算領域スロットに装備し, 術者に対する通常の術式射程チェックを無効化します.$(p)通常のCAD射程を大きく超える位置を対象にできるため, 扱いには注意してください.";
            default -> "Equips in the Magic Calculation Area slot and removes Psi's normal spell radius check for the caster.$(p)This lets spells target positions far beyond ordinary CAD range limits, so use it carefully.";
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
            case "ja_jp" -> "$(thing)PsiTweaks$(0) は $(thing)Psi$(0) を $(thing)Mekanism$(0) の進行へ接続します. 連携要素には, Psi素材を作る冶金吹き込みレシピ, 魔法師能力を伸ばすMekaSuitモジュール, Psi・FE・サイオニック資源を扱う機械があります.$(p)正確なレシピはJEIで確認してください. この項目では各要素の役割をまとめます.";
            default -> "$(thing)PsiTweaks$(0) ties $(thing)Psi$(0) into $(thing)Mekanism$(0) progression. Its integration covers Metallurgic Infusing recipes for Psi materials, MekaSuit modules that improve caster stats, and machines that exchange Psi, FE, and psionic resources.$(p)Use JEI as the exact recipe reference; this entry explains what each group is for.";
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
}
