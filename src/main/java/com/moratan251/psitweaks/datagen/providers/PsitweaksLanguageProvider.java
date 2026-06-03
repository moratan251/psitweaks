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
        addDeathMessages(root);
        addChemical(root, "infuse_psigem", "Psigem", "サイジェム");
        addChemical(root, "infuse_ebony", "Ebony", "エボニー");
        addChemical(root, "infuse_ivory", "Ivory", "アイボリー");
        addChemical(root, "infuse_chaotic_factor", "Chaotic Factor", "混沌因子");
        addChemical(root, "infuse_psionic_echo", "Psionic Echo", "サイオニックエコー");
        addChemical(root, "infuse_hypostasis", "Hypostasis Gem", "ヒュポスタシスジェム");
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
        root.addProperty("psitweaks.datatype.item", switch (locale) {
            case "ja_jp" -> "Item";
            default -> "Item";
        });
        root.addProperty("psitweaks.datatype.block", switch (locale) {
            case "ja_jp" -> "Block";
            default -> "Block";
        });
        root.addProperty("psitweaks.datatype.contextual_value", switch (locale) {
            case "ja_jp" -> "Contextual Value";
            default -> "Contextual Value";
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
        root.addProperty("psitweaks.datatype.number_or_vector", switch (locale) {
            case "ja_jp" -> "Number / Vector";
            default -> "Number / Vector";
        });
        root.addProperty("psitweaks.datatype.plain_value", switch (locale) {
            case "ja_jp" -> "Plain Value";
            default -> "Plain Value";
        });
        root.addProperty("psitweaks.gui.spell_piece_mode", switch (locale) {
            case "ja_jp" -> "モード: %s";
            default -> "Mode: %s";
        });
        root.addProperty("psitweaks.gui.spell_piece_mode.title", switch (locale) {
            case "ja_jp" -> "モード選択";
            default -> "Mode Select";
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
        root.addProperty("psitweaks.spellparam.item", switch (locale) {
            case "ja_jp" -> "Item";
            default -> "Item";
        });
        root.addProperty("psitweaks.spellparam.tag", switch (locale) {
            case "ja_jp" -> "タグ";
            default -> "Tag";
        });
        root.addProperty("psitweaks.spellparam.label", switch (locale) {
            case "ja_jp" -> "ラベル";
            default -> "Label";
        });
        root.addProperty("psitweaks.spellerror.no_jump_anchor", switch (locale) {
            case "ja_jp" -> "前方に一致するジャンプアンカーがありません";
            default -> "No matching Jump Anchor found ahead";
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
        root.addProperty("psitweaks.gui.string_constant_input.empty", switch (locale) {
            case "ja_jp" -> "空文字列";
            default -> "Empty string";
        });
        root.addProperty("psitweaks.gui.string_constant_input.hint", switch (locale) {
            case "ja_jp" -> "Shift+Enter改行 / Enter閉";
            default -> "Shift+Enter newline / Enter closes";
        });
        root.addProperty("psitweaks.gui.string_constant_input.read_only", switch (locale) {
            case "ja_jp" -> "閲覧のみ";
            default -> "Read only";
        });
        root.addProperty("psitweaks.gui.string_constant_input.button.copy_all", switch (locale) {
            case "ja_jp" -> "全コピー";
            default -> "Copy All";
        });
        root.addProperty("psitweaks.gui.string_constant_input.button.clear_all", switch (locale) {
            case "ja_jp" -> "全削除";
            default -> "Clear";
        });
        root.addProperty("psitweaks.gui.string_constant_input.button.replace_all", switch (locale) {
            case "ja_jp" -> "貼付";
            default -> "Paste";
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
        addLegacyPatchouliBookTranslations(root);

        return root;
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
            {"psi.book.entry.psitweaks_machines", "Overview"},
            {"psi.book.entry.psitweaks_magician", "Magician"},
            {"psi.book.entry.psitweaks_mekanism_integration", "Mekanism Integration"},
            {"psi.book.entry.psitweaks_moval_suit", "M.O.V.A.L. Suit"},
            {"psi.book.entry.psitweaks_overview", "What PsiTweaks Adds"},
            {"psi.book.entry.psitweaks_research", "Research"},
            {"psi.book.entry.psitweaks_spell_bullets", "Upgraded Spell Bullets"},
            {"psi.book.page.psitweaks_cad_and_gear.0", "Several PsiTweaks items make spellcasting easier to carry, automate, or specialize. Some are direct CAD tools, while others support bullet handling or improve a caster's combat role."},
            {"psi.book.page.psitweaks_cad_and_gear.1", "These recipes cover portable assembly, inline casting, spell storage, and caster support gear."},
            {"psi.book.page.psitweaks_cad_and_gear.2", "PsiTweaks also adds powerful CAD assemblies made from expensive ingots. When ordinary Psi equipment no longer keeps up, use them together with high-tier spell bullets and similar upgrades."},
            {"psi.book.page.psitweaks_cad_and_gear.3", "$(thing)PsiTweaks$(0) adds five CAD material lines: $(l:components/psitweaks_alloy_psion)Psionic Alloy$(/l), $(l:components/psitweaks_chaotic_psimetal)Chaotic Psimetal$(/l), $(l:components/psitweaks_flashmetal)Flashmetal$(/l), $(l:components/psitweaks_heavy_psimetal)Heavy Psimetal$(/l), and $(l:components/psitweaks_psycheonic_metal_ingot)Psycheonic Metal$(/l).$(p)Psionic Alloy CADs have excellent Efficiency but very small Potency. The other CAD assemblies improve in Efficiency and Potency as their materials move into later progression."},
            {"psi.book.page.psitweaks_item.auto_casters.0", "Auto Casters can be equipped in Curios slots and automatically cast spells.$(p)With the Curios Controller, you can change their spell bullets in the same way as psimetal exosuit armor."},
            {"psi.book.page.psitweaks_item.auto_casters.1", "The tick model fires every tick, the second model fires every 0.9 seconds, and the custom tick model lets you set an interval from 1 to 1200 ticks by using the item."},
            {"psi.book.page.psitweaks_item.blank_program", "Used as the base item for program research. Put it into the $(l:psitweaks_machines/program_researcher)$(o)$(item)Program Research Table$(0)$(/l) with the required materials to create a written program.$(p)It can also be crafted with an existing written program item to duplicate that program."},
            {"psi.book.page.psitweaks_item.curios_controller", "Controls the selected bullet slot of socketable items equipped in Magic Calculation Area slots.$(p)Use it while Auto Casters are equipped as the Curios-side counterpart to the exosuit controller."},
            {"psi.book.page.psitweaks_item.flash_charm", "A Curios charm that continually removes Blindness and Darkness from the wearer.$(p)It also works while carried in inventory, making it useful in the Deep Dark and against vision-disrupting effects."},
            {"psi.book.page.psitweaks_item.flash_ring", "A tool that can build and cast spells on the spot.$(p)Sneak-use it to open the programming screen."},
            {"psi.book.page.psitweaks_item.inline_casters.0", "Inline, Secondary, and Parallel Casters are handheld spellcasting tools with their own bullet sockets. They still require the caster to have a CAD, but the selected bullet is stored in the caster item itself."},
            {"psi.book.page.psitweaks_item.inline_casters.1", "The Inline Caster has one slot, the Secondary Caster has five, and the Parallel Caster has eleven.$(p)Use them when you want to carry many spell bullets without constantly changing the CAD."},
            {"psi.book.page.psitweaks_item.moval_suit.0", "The $(item)M.O.V.A.L. Suit$(0) is a high-grade Psi armor set crafted with $(l:components/psitweaks_heavy_psimetal)$(o)$(item)Heavy Psimetal$(0)$(/l) and $(item)Ebony Psimetal$(0). Each equipped piece supports spellcasters by increasing spell damage, Psi regeneration, and maximum Psi.$(p)Use it when you want armor that improves both survival and sustained spellcasting instead of only adding protection."},
            {"psi.book.page.psitweaks_item.moval_suit.1", "Like Psi's exosuit armor, M.O.V.A.L. Suit pieces can trigger spells from armor events. The helmet accepts exosuit sensors, the normal leggings cast on tick, the Ivory leggings cast on second, and the boots cast on jump.$(p)Each piece grants +10% spell damage, +5 Psi regeneration, and +500 maximum Psi."},
            {"psi.book.page.psitweaks_item.philosophers_stone", "A reusable catalyst that provides interconversion recipes for metals, diamonds, coal, ender pearls, and other resources."},
            {"psi.book.page.psitweaks_item.portable_cad_assembler", "A handheld CAD Assembler. Use it to open an assembler interface without placing the block, making it easy to adjust CAD parts or loaded spell bullets while away from base."},
            {"psi.book.page.psitweaks_item.sorcery_booster", "Equips in the Magic Calculation Area slot and increases spell damage by 30%.$(p)It is made from a $(l:components/psitweaks_magicians_brain)$(o)$(item)Magician's Brain$(0)$(/l), making it a combat-focused caster upgrade."},
            {"psi.book.page.psitweaks_item.spell_magazine", "Stores up to twelve spell bullets and swaps them with the matching slots of your equipped CAD when used.$(p)Use it as a portable loadout carrier when you need to change many bullets at once."},
            {"psi.book.page.psitweaks_item.third_eye_device", "Equips in the Magic Calculation Area slot and removes Psi's normal spell radius check for the caster.$(p)This lets spells target positions far beyond ordinary CAD range limits, so use it carefully."},
            {"psi.book.page.psitweaks_machine.cad_disassembler", "A workbench for disassembling CADs and recovering their parts. Sneak-right-click it with a CAD to break the CAD into its installed components and loaded spell bullets.$(p)Use it when replacing CAD bodies or salvaging parts from old setups."},
            {"psi.book.page.psitweaks_machine.material_mutator", "A Mekanism injecting-style machine that performs material mutation with $(item)Psionic Echo Gas$(0) and energy.$(p)It automates mutations that can also be produced by $(l:psitweaks_spell_pieces/trick_material_mutation)$(o)Trick: Material Mutation$(0)$(/l), such as $(l:components/psitweaks_jade)Jade$(/l) and $(l:components/psitweaks_hypostasis_gem)Hypostasis Gems$(/l)."},
            {"psi.book.page.psitweaks_machine.program_researcher", "A powered research table for producing PsiTweaks program items. Put the required ingredients in the input slots and supply FE; completed research outputs the program item.$(p)JEI shows each research recipe's energy cost and time."},
            {"psi.book.page.psitweaks_machine.psionic_generator", "A generator that links to its Mekanism owner and converts that player's Psi into energy while the owner is online.$(p)Use the GUI to enable the link and set the Psi consumed per tick. Higher consumption gives higher output and drains the owner faster."},
            {"psi.book.page.psitweaks_machine.sculk_eroder", "A machine that corrodes stone, dirt, sand, and related block items into Sculk outputs.$(p)Use it when you need Sculk materials without relying on natural spread."},
            {"psi.book.page.psitweaks_machine.spellmachinery_casing", "A casing block used to execute advanced magic through machinery."},
            {"psi.book.page.psitweaks_machine.transcendent_energy_cube", "This is simply an upgraded version of Mekanism's Energy Cube. It has 128 times the capacity and output performance of the Ultimate Energy Cube, making it suitable for buffering fission reactor-class power."},
            {"psi.book.page.psitweaks_machine.transcendent_universal_cable", "This is simply an upgraded version of Mekanism's Universal Cable. It has 128 times the performance of the Ultimate Cable, making it suitable for fusion reactor-class power output."},
            {"psi.book.page.psitweaks_machines.0", "PsiTweaks adds machines that connect spellcraft with automation and industrial processing. Some machines are pure psionic tools, while others integrate with Mekanism-style energy and materials."},
            {"psi.book.page.psitweaks_machines.1", "These machines are the main entry points for research, mutation, Sculk processing, and Psi-to-energy conversion."},
            {"psi.book.page.psitweaks_machines.2", "The $(l:psitweaks_machines/psionic_generator)$(o)$(item)Psi-Link Generator$(0)$(/l) links to its owner. When enabled, loaded, and the owner is online, it consumes that player's Psi and produces energy for the machine's buffer.$(p)The rate is set in the GUI. More Psi per tick means more power, but it also drains the owner faster."},
            {"psi.book.page.psitweaks_magician.0", "$(thing)Magicians$(0) are a villager profession added by PsiTweaks. They represent villagers who work with Psi machinery and are connected to several caster-focused materials and upgrades."},
            {"psi.book.page.psitweaks_magician.1", "An unemployed villager can become a $(thing)Magician$(0) by claiming a $(l:basics/cad_assembler)$(o)$(item)CAD Assembler$(0)$(/l) as its job site.$(p)Place the assembler where a villager can reach it, just like other villager workstation blocks."},
            {"psi.book.page.psitweaks_magician.2", "A Magician is also the source of the $(l:components/psitweaks_magicians_brain)$(o)$(item)Magician's Brain$(0)$(/l). It drops when a Magician villager is killed by $(l:psitweaks_spell_pieces/trick_guillotine)$(o)Trick: Guillotine$(0)$(/l), and is used in caster-focused recipes."},
            {"psi.book.page.psitweaks_material.alloy_hypostasis", "Made by infusing $(l:components/psitweaks_alloy_psionic_echo)$(o)$(item)Echo Alloy$(0)$(/l) with Hypostasis.$(p)Used for $(l:components/psitweaks_hypostasis_control_circuit)$(o)$(item)Hypostasis Control Circuits$(0)$(/l) and high-tier psionic machinery."},
            {"psi.book.page.psitweaks_material.alloy_psion", "Made by infusing Mekanism's Atomic Alloy with Psigem.$(p)Used for $(l:components/psitweaks_psionic_control_circuit)$(o)$(item)Psionic Control Circuits$(0)$(/l), Psionic Alloy CAD assemblies, and the next alloy tier."},
            {"psi.book.page.psitweaks_material.alloy_psionic_echo", "Made by infusing $(l:components/psitweaks_alloy_psion)$(o)$(item)Psionic Alloy$(0)$(/l) with $(l:components/psitweaks_psionic_echo)Psionic Echo$(/l).$(p)Used for $(l:components/psitweaks_echo_control_circuit)$(o)$(item)Echo Control Circuits$(0)$(/l) and as the base for $(l:components/psitweaks_alloy_hypostasis)Hypostasis Alloy$(/l)."},
            {"psi.book.page.psitweaks_material.antinite_ingot", "Smelted from Antinite ore or recovered from Antinite blocks. The dust, shard, clump, crystal, and dirty dust forms are intermediate products in Mekanism-style ore processing.$(p)Used as a material for $(l:components/psitweaks_hypostasis_gem)Hypostasis Gems$(/l) and the $(l:items/psitweaks_philosophers_stone)$(o)$(item)Philosopher's Stone$(0)$(/l)."},
            {"psi.book.page.psitweaks_material.chaotic_factor", "$(item)Chaotic Factor$(0) starts with infusing an $(item)Ender Pearl$(0) with Psigem to make $(item)Psionic Factor$(0). Infuse that with Ivory or Ebony to make the aligned factor variants, then cross the opposite alignment to create $(item)Chaotic Factor$(0).$(p)Used as the infusion source for $(l:components/psitweaks_chaotic_psimetal)$(o)$(item)Chaotic Psimetal$(0)$(/l), the first major PsiTweaks metal above ordinary Psimetal."},
            {"psi.book.page.psitweaks_material.chaotic_psimetal", "Made by infusing ordinary $(item)Psimetal$(0) with $(l:components/psitweaks_chaotic_factor)Chaotic Factor$(/l).$(p)Used in recipes for Chaotic Psimetal CAD assemblies and $(l:components/psitweaks_unrefined_flashmetal)$(o)$(item)Unrefined Flashmetal$(0)$(/l)."},
            {"psi.book.page.psitweaks_material.echo_control_circuit", "Crafted from $(l:components/psitweaks_psionic_control_circuit)$(o)$(item)Psionic Control Circuit$(0)$(/l), $(l:components/psitweaks_alloy_psionic_echo)$(o)$(item)Echo Alloy$(0)$(/l), and $(l:components/psitweaks_echo_sheet)HDΨE Sheets$(/l).$(p)Used in recipes for advanced spell bullets and machinery."},
            {"psi.book.page.psitweaks_material.echo_pellet", "A compact HDΨE material made from $(l:components/psitweaks_psionic_echo)Psionic Echo$(/l) processing.$(p)Used where recipes need condensed echo material rather than raw $(l:components/psitweaks_psionic_echo)Psionic Echo$(/l). The ΨE-O Fuel produced as a byproduct can be used as fuel for the Gas Generator."},
            {"psi.book.page.psitweaks_material.echo_sheet", "Made by enriching HDΨE material into a sheet form.$(p)Used heavily in Echo and Hypostasis Control Circuit recipes."},
            {"psi.book.page.psitweaks_material.flashmetal", "Made by enriching $(l:components/psitweaks_unrefined_flashmetal)$(o)$(item)Unrefined Flashmetal$(0)$(/l).$(p)Used for Flashmetal blocks, Flashmetal CAD assemblies, $(l:components/psitweaks_heavy_psimetal)Heavy Psimetal$(/l), and sublimated-tier spell bullets."},
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
            {"psi.book.page.psitweaks_mekanism_integration.0", "$(thing)PsiTweaks$(0) ties $(thing)Psi$(0) into $(thing)Mekanism$(0) progression. Its integration covers Metallurgic Infusing recipes for Psi materials, MekaSuit modules that improve caster stats, and machines that exchange Psi, FE, and psionic resources.$(p)Use JEI as the exact recipe reference; this entry explains what each group is for."},
            {"psi.book.page.psitweaks_mekanism_integration.1", "The $(item)Metallurgic Infuser$(0) can produce core Psi materials with PsiTweaks infuse types. Psigem infusion turns redstone into $(l:components/psidust)$(o)$(item)Psidust$(0)$(/l) and gold into $(item)Psimetal$(0).$(p)Ebony and Ivory infusion can make $(item)Ebony Substance$(0), $(item)Ivory Substance$(0), and their Psimetal variants. Later recipes extend this chain into $(l:components/psitweaks_chaotic_factor)psionic factors$(/l), $(l:components/psitweaks_chaotic_psimetal)$(o)$(item)Chaotic Psimetal$(0)$(/l), $(l:components/psitweaks_psionic_echo)Psionic Echo materials$(/l), and $(l:components/psitweaks_alloy_hypostasis)Hypostasis-tier metal$(/l)."},
            {"psi.book.page.psitweaks_mekanism_integration.2", "PsiTweaks adds three MekaSuit modules for spellcasters. The $(item)Psyon Supplying Unit$(0) and $(item)Psyon Capacity Unit$(0) install in the MekaSuit body armor and improve Psi regeneration and maximum Psi.$(p)The $(item)Phenomenon Interference Enhancement Unit$(0) installs in the MekaSuit helmet and increases spell damage."},
            {"psi.book.page.psitweaks_overview.0", "$(thing)PsiTweaks$(0) is an expansion for $(thing)Psi$(0). It adds many elements, including industrialized material production, new gear, and new spells.$(p)The mod is intended for setups where Psi remains powerful through the late game as both combat and utility, rather than ending at ordinary CADs and basic spell bullets."},
            {"psi.book.page.psitweaks_overview.2", "Major additions include higher-tier spell bullets, new CAD assemblies and casting support tools, new spells, processing machines, and generators.$(p)Most systems are built to bridge Psi with industrial environments and to strengthen Psi's late-game capabilities."},
            {"psi.book.page.psitweaks_research.0", "Some spell pieces added by $(thing)PsiTweaks$(0) must be unlocked through research before they can be used in spell programs.$(p)Research creates program items that correspond to those spell pieces. Right-clicking with a program unlocks its spell piece, and the program is not consumed."},
            {"psi.book.page.psitweaks_research.1", "Place the required ingredients in the $(l:psitweaks_machines/program_researcher)$(o)$(item)Program Research Table$(0)$(/l) and supply FE to craft a program.$(p)JEI lists each research recipe's required materials, energy cost, and processing time."},
            {"psi.book.page.psitweaks_spell_bullets.0", "$(thing)PsiTweaks$(0) adds upgraded tiers above the ordinary $(l:items/spell_bullet)$(o)$(item)Spell Bullet$(0)$(/l): Advanced, Resonant, Sublimated, Awakened, and Transcendent.$(p)Every Psi spell bullet variant has upgraded tiers available."},
            {"psi.book.page.psitweaks_spell_bullets.1", "Upgrading a spell bullet requires four bullets from the previous tier, making it very expensive."},
            {"psi.book.page.psitweaks_spell_bullets.2", "Higher tiers greatly improve cost efficiency, making powerful spells easier to cast."},
            {"psi.book.page.psitweaks_spellpiece.selector_nearby_spellgram", "Gets SpellGram objects around the specified coordinates. It is mainly used by tricks that control placed SpellGram objects."},
            {"psi.book.page.psitweaks_spellpiece.selector_stored_entity", "Gets an entity from the UUID stored in CAD memory."},
            {"psi.book.page.psitweaks_spellpiece.trick_active_air_mine", "Creates a spherical shockwave at the specified coordinates and damages living beings within range. This is an area attack detonated at a chosen location."},
            {"psi.book.page.psitweaks_spellpiece.trick_aqua_cutter", "Fires a water-blade projectile forward as an early-game offensive trick."},
            {"psi.book.page.psitweaks_spellpiece.trick_barrier", "Applies a barrier effect that reduces incoming damage. It reduces damage taken by (level * 4)."},
            {"psi.book.page.psitweaks_spellpiece.trick_blaze_ball", "Fires a ball of flame forward as an early-game offensive trick."},
            {"psi.book.page.psitweaks_spellpiece.trick_break_fortune", "Breaks the target block with Fortune."},
            {"psi.book.page.psitweaks_spellpiece.trick_break_silk", "Breaks the target block with Silk Touch."},
            {"psi.book.page.psitweaks_spellpiece.trick_cocytus", "Permanently freezes the mind of the target mob. Rather than merely dealing damage, this very powerful control trick prevents action."},
            {"psi.book.page.psitweaks_spellpiece.trick_cure_radiation", "Removes the target's radiation exposure."},
            {"psi.book.page.psitweaks_spellpiece.trick_die_flex", "Behaves similarly to Psi's Trick: Die and refunds the Psi cost of spell pieces that were not executed. With high-frequency casting, the client-side Psi display may temporarily desync."},
            {"psi.book.page.psitweaks_spellpiece.trick_dispel", "Removes effects from the target entity. This is the general-purpose dispel that does not distinguish between beneficial and harmful effects."},
            {"psi.book.page.psitweaks_spellpiece.trick_dispel_beneficial", "Removes only beneficial effects from the target entity. It is suited for stripping enhancements from hostile targets."},
            {"psi.book.page.psitweaks_spellpiece.trick_dispel_non_beneficial", "Removes non-beneficial effects from the target entity. Use it when you want to cleanse harmful effects while leaving allied buffs intact."},
            {"psi.book.page.psitweaks_spellpiece.trick_explode_no_destroy", "Creates an explosion that deals damage without destroying blocks. Be careful: dropped items and similar entities can still be erased."},
            {"psi.book.page.psitweaks_spellpiece.trick_flare_circle", "Places a fire SpellGram Circle that continuously deals fire damage to living beings inside it. Once placed, the circle remains for 60 seconds."},
            {"psi.book.page.psitweaks_spellpiece.trick_flight", "Gives the target an effect that enables creative flight."},
            {"psi.book.page.psitweaks_spellpiece.trick_freeze_block", "Freezes the target block one stage. Water becomes ice, ice becomes packed ice, packed ice becomes blue ice, lava becomes magma block, and magma block becomes obsidian."},
            {"psi.book.page.psitweaks_spellpiece.trick_guillotine", "Deals powerful slash damage to the target and makes it drop a head when killed. This is a single-target offensive trick."},
            {"psi.book.page.psitweaks_spellpiece.trick_hardening", "Applies a hardening effect that limits large incoming damage to a fixed value. Maximum damage taken is capped at (level - 2)."},
            {"psi.book.page.psitweaks_spellpiece.trick_ice_circle", "Places an ice SpellGram Circle that continuously deals freezing damage to living beings inside it. Like Fire Circle, it is suited for area control."},
            {"psi.book.page.psitweaks_spellpiece.trick_interact_block", "Acts on the target block as if right-clicked with the item in the caster's off hand."},
            {"psi.book.page.psitweaks_spellpiece.trick_material_mutation", "Breaks specific blocks and transmutes them into other items. The Material Mutator can perform this process using power and Vaporized Psionic Echo."},
            {"psi.book.page.psitweaks_spellpiece.trick_melt_block", "Melts the target block one stage. Ice, packed ice, and blue ice become water; obsidian, stone-like blocks, and cobblestone-like blocks become magma block; and magma block becomes lava."},
            {"psi.book.page.psitweaks_spellpiece.trick_molecular_divider", "Cuts living beings along a plane defined by three points. It is a high-power area attack trick."},
            {"psi.book.page.psitweaks_spellpiece.trick_parade", "Applies an effect that evades attacks by chance. It evades attacks with a (62.5 + 7.5 * level)% chance."},
            {"psi.book.page.psitweaks_spellpiece.trick_phonon_maser", "Fires a high-power heat ray using ultrasonic vibration. It is a powerful offensive trick."},
            {"psi.book.page.psitweaks_spellpiece.trick_radiation_filter", "Applies a radiation protection effect to the target, protecting them from radiation."},
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
            {"psi.book.entry.psitweaks_machines", "概要"},
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
            {"psi.book.page.cadAssembler.0", "$(item)CAD組立機(0)は$(thing)Psi$(0)の中核であり, 二つの重要な機能を果たします. $(p)第一に, 構成部品から$(thing)CAD$(0)を組み立てます. 次に, $(o)空でない$() $(item)術式弾$(0)を$(thing)CAD$(0)に装填します.（$(l:items/spell_bullet)$(o)$(thing)術式弾$(0)$(/l)を保持する他のアイテム, 例えば$(l:items/tools)$(o)$ (thing)サイメタルツール$(0)$(/l)など）に$(o)空でない$(0)$(item)術式弾$(0)$を装填する."},
            {"psi.book.page.cadAssembler.1", "CAD組立機のクラフト"},
            {"psi.book.page.cadAssembler.2", "最も簡素な魔法使用可能CAD"},
            {"psi.book.page.cadAssembler.3", "A $(thing)CAD$(0) is built from up to five components; the simplest $(thing)CAD$(0) only uses one component, an $(l:components/assembly)$(o)$(item)Assembly$(0)$(/l), though this is only useful for crafting $(l:components/psidust)$(o)$(item)Psidust$(0)$(/l).$(p)A $(thing)CAD$(0) capable of casting $(thing)Spells$(0) requires a $(l:components/core)$(o)$(item)Core$(0)$(/l) and a $(l:components/socket)$(o)$(item)Socket$(0)$(/l) as well.$(p)Adding a $(l:components/battery)$(o)$(item)Battery$(0)$(/l) slightly increases a user's maximum $(thing)Psi energy$(0), and adding a $(l:components/colorizer)$(o)$(item)Colorizer$(0)$(/l) changes the color of cast $(thing)Spells$(0), which is purely cosmetic."},
            {"psi.book.page.cadAssembler.4", "Once a $(thing)CAD$(0) is created, it can be placed in the leftmost panel of the $(item)CAD Assembler$(0).$(p)When placed there, the slots below open; $(item)Spell Bullets$(0) can be placed in these slots to be loaded into the $(thing)CAD$(0).$(p)Once the $(l:items/spell_bullet)$(o)$(thing)Bullets$(0)$(/l) are loaded, the $(thing)CAD$(0) is removed and held, and the $(thing)Psi master keybind$(0) ($(k:psimisc.keybind)) is held, the $(l:items/spell_bullet)$(o)$(thing)Bullets$(0)$(/l)' $(thing)Spells$(0) will be displayed on a radial menu, ready to be selected and cast."},
            {"psi.book.page.cadAssembler.5", "A CAD with one bullet loaded"},
            {"psi.book.page.introduction.0", "$(thing)Psi$(0)へようこそ！ 宇宙で最も偉大なスペルプログラムベースの魔法技術MODです！$(p)$(thing)Psi$(0)は（魔法科高校の劣等生シリーズにインスパイアされた）MODで, あなたの命令を実行する$(thing)魔法$(0)$(o)を創造・発動して意のままに操るmodです.$(p)魔法を操る魔法師への道へ踏み出すには, まず$(l:components/psidust)$(o)$(item)サイダスト$(0)$(/l)という素材が必要です――ただし, これを単純にクラフトできるものではありません."},
            {"psi.book.page.introduction.1", "代わりに, まず$(l:basics/cad_assembler)$(o)$(item)CAD組立機(0)$(/l)と$(l:components/assembly#iron)$(o)$(item)鉄のCAD素体$(0)$(/l)を作成してください. (p)$(l:basics/cad_assembler)$(o)$(item)CAD組立機$(0)$(/l)を配置し, 開いて$(l:components/assembly#iron)$(o)$ (item)鉄のCAD素体$(0)$(/l)を挿入し, 非常に簡素な$(thing)術式補助演算機(0)（略して$(thing)CAD$(0)）を構築します. (p)そこから地面に$(item)レッドストーンダスト$(0)を数個落とし, 新しく作成した$(thing)CAD$(0)をダストに向けて, $(k:use)を発動して$(l:components/psidust)$(o)$(item)サイダスト$(0)$(/l)を生成します."},
            {"psi.book.page.psitweaks_cad_and_gear.0", "PsiTweaks には, 術式の携行, 自動化, 専門化を助けるアイテムが複数あります. CADに直接関わる道具もあれば, 術式弾の管理や魔法師の戦闘能力を支える装備もあります."},
            {"psi.book.page.psitweaks_cad_and_gear.1", "これらのレシピは, 携帯型組立, インライン詠唱, 術式保存, 魔法師支援装備の入口です."},
            {"psi.book.page.psitweaks_cad_and_gear.2", "PsiTweaks は, 高コストなインゴットを素材とした強力なCAD素体を追加します. 通常のPsi装備では物足りなくなったら, 高位術式弾などと組み合わせて使ってください."},
            {"psi.book.page.psitweaks_cad_and_gear.3", "$(thing)PsiTweaks$(0) はCAD素材として, $(l:components/psitweaks_alloy_psion)サイオニック合金$(/l), $(l:components/psitweaks_chaotic_psimetal)カオティックサイメタル$(/l), $(l:components/psitweaks_flashmetal)フラッシュメタル$(/l), $(l:components/psitweaks_heavy_psimetal)ヘビーサイメタル$(/l), $(l:components/psitweaks_psycheonic_metal_ingot)プシオニックメタル$(/l) の5系統を追加します.$(p)サイオニック合金CADは効率が非常に優れる一方で, 規模は非常に小さいCADです. 他のCAD素体は, 後半の素材ほど効率と規模が相応に強化されています."},
            {"psi.book.page.psitweaks_item.auto_casters.0", "術式自動詠唱デバイスは, Curiosスロットに装備可能であり, 自動で術式を詠唱する装備です. $(p) キュリオスコントローラを用いることで、サイメタル外装と同じ要領で術式弾を変更できます."},
            {"psi.book.page.psitweaks_item.auto_casters.1", "tick型は毎tick, セカンド型は0.9秒ごとに詠唱します. カスタムtick型は, アイテム使用で1から1200tickの範囲から詠唱間隔を設定できます."},
            {"psi.book.page.psitweaks_item.blank_program", "プログラム研究の基礎素材です. $(l:psitweaks_machines/program_researcher)$(o)$(item)プログラム研究台$(0)$(/l) に必要素材と一緒に入れることで, 記入済みのプログラムを作成できます.$(p)既存の記入済みプログラムアイテムとクラフトすると, そのプログラムを複製することもできます."},
            {"psi.book.page.psitweaks_item.curios_controller", "魔法演算領域スロットに装備したソケット対応アイテムの選択術式弾スロットを操作します.$(p)術式自動詠唱デバイスを装備しているときに使う, Curios版の外装コントローラです."},
            {"psi.book.page.psitweaks_item.flash_charm", "装備者の盲目と暗闇を継続的に解除するCuriosチャームです.$(p)インベントリ内に持っているだけでも機能するため, ディープダークや視界妨害効果への対策になります."},
            {"psi.book.page.psitweaks_item.flash_ring", "その場で術式を組み立てて詠唱が可能なツールです.$(p)スニーク使用でプログラム画面を開くことができます."},
            {"psi.book.page.psitweaks_item.inline_casters.0", "インライン, セカンダリ, パラレルキャスターは, 自身に術式弾スロットを持つ手持ち詠唱具です. 詠唱には通常通りCADが必要ですが, 選択中の術式弾はキャスター側に保存されます."},
            {"psi.book.page.psitweaks_item.inline_casters.1", "インラインキャスターは1スロット, セカンダリキャスターは5スロット, パラレルキャスターは11スロットです.$(p)CADを頻繁に差し替えず, 多数の術式弾を持ち替えたいときに使います."},
            {"psi.book.page.psitweaks_item.moval_suit.0", "$(item)M.O.V.A.L. スーツ$(0) は, $(l:components/psitweaks_heavy_psimetal)$(o)$(item)ヘビーサイメタル$(0)$(/l) と $(item)エボニーサイメタル$(0) を使って製作する高性能なPsi系防具です. 各部位を装備するごとに, 術式ダメージ, Psi回復速度, 最大Psiを強化します.$(p)単なる防御力だけでなく, 継続的な詠唱能力も伸ばしたいときに使う装備です."},
            {"psi.book.page.psitweaks_item.moval_suit.1", "Psiの外装防具と同様に, M.O.V.A.L. スーツの各部位は防具イベントから術式を発動できます. ヘルメットは外装センサーに対応し, 通常レギンスはtick時, Ivory版レギンスはsecond時, ブーツはjump時に詠唱します.$(p)各部位は術式ダメージ+10%, Psi回復速度+5, 最大Psi+500を付与します."},
            {"psi.book.page.psitweaks_item.philosophers_stone", "再利用可能な触媒で, 金属, ダイヤモンド, 石炭やエンダーパールなどの相互変換レシピを提供します."},
            {"psi.book.page.psitweaks_item.portable_cad_assembler", "手持ちで使えるCAD組立機です. ブロックを設置せずに組立機画面を開けるため, 拠点外でも簡単にCAD部品や装填済み術式弾を調整できます."},
            {"psi.book.page.psitweaks_item.sorcery_booster", "魔法演算領域スロットに装備し, 術式ダメージを30%増加させます.$(p)$(l:components/psitweaks_magicians_brain)$(o)$(item)魔法師の脳$(0)$(/l) を使う, 戦闘向けの魔法師強化装備です."},
            {"psi.book.page.psitweaks_item.spell_magazine", "最大12個の術式弾を保持し, 使用すると装備中のCADの対応スロットと入れ替えます.$(p)複数の術式弾をまとめて切り替えるための携帯用ロードアウトです."},
            {"psi.book.page.psitweaks_item.third_eye_device", "魔法演算領域スロットに装備し, 術者に対する通常の術式射程チェックを無効化します.$(p)通常のCAD射程を大きく超える位置を対象にできるため, 扱いには注意してください."},
            {"psi.book.page.psitweaks_machine.cad_disassembler", "CADを解体して部品を回収するための作業台です. CADを持ってスニーク右クリックすると, 装着されている構成部品と装填済みの術式弾を取り出してCADを分解します.$(p)CAD素体を交換するときや, 古い構成から部品を回収したいときに使います."},
            {"psi.book.page.psitweaks_machine.material_mutator", "$(item)気化サイオニックエコー$(0) と電力を使い, 物質変成を実行するMekanism注入形式の機械です.$(p)$(l:psitweaks_spell_pieces/trick_material_mutation)$(o)作動式: 物質変成$(0)$(/l)でも得られる$(l:components/psitweaks_jade)翡翠$(/l)や$(l:components/psitweaks_hypostasis_gem)ヒュポスタシスジェム$(/l)などの変成を自動化できます."},
            {"psi.book.page.psitweaks_machine.program_researcher", "PsiTweaksのプログラムアイテムを作成するための電力式研究台です. 必要素材を入力スロットに入れてFEを供給すると, 研究完了時にプログラムを出力します.$(p)各研究の消費電力と時間はJEIで確認できます."},
            {"psi.book.page.psitweaks_machine.psionic_generator", "所有者にリンクし, 所有者がオンラインの間, そのプレイヤーのPsiをエネルギーへ変換する発電機です.$(p)GUIからリンクの有効化とtickあたりのPsi消費量を設定します. 消費量を増やすほど出力も増えますが, 所有者のPsiも速く減ります."},
            {"psi.book.page.psitweaks_machine.sculk_eroder", "石, 土, 砂系などのブロックアイテムをスカルク系の出力へ侵食加工する機械です.$(p)自然なスカルク伝播に頼らず, スカルク素材を得たいときに使います."},
            {"psi.book.page.psitweaks_machine.spellmachinery_casing", "高度な魔法を機械により実行するための筐体ブロックです."},
            {"psi.book.page.psitweaks_machine.transcendent_energy_cube", "これは単純に, MekanismのEnergy Cubeの強化版です. 究極エネルギーキューブの128倍の容量と出力性能を持ち, 核分裂炉級の電力を蓄えるのに向きます."},
            {"psi.book.page.psitweaks_machine.transcendent_universal_cable", "これは単純に, MekanismのUniversal Cableの強化版です. 究極ケーブルの128倍の性能を持ち, 核融合炉級の電力出力を扱うのに向きます."},
            {"psi.book.page.psitweaks_machines.0", "PsiTweaks は, 術式工学と自動化・工業処理をつなぐ機械を追加します. 純粋なサイオニック装置もあれば, Mekanism形式のエネルギーや素材処理と連携する機械もあります."},
            {"psi.book.page.psitweaks_machines.1", "これらの機械は, 研究, 物質変成, スカルク処理, Psiからエネルギーへの変換の入口になります."},
            {"psi.book.page.psitweaks_machines.2", "$(l:psitweaks_machines/psionic_generator)$(o)$(item)サイリンク発電機$(0)$(/l) は Mekanism の所有者にリンクします. 有効化され, チャンクが読み込まれ, 所有者がオンラインのとき, そのプレイヤーのPsiを消費して内部バッファへエネルギーを生成します.$(p)発電レートはGUIで設定できます. tickあたりのPsi消費を増やすほど出力も増えますが, 所有者のPsiも速く減ります."},
            {"psi.book.page.psitweaks_magician.0", "$(thing)魔法師$(0) は PsiTweaks が追加する村人の職業です. Psi機械を扱う村人であり, 魔法師向けの素材や強化装備と関係します."},
            {"psi.book.page.psitweaks_magician.1", "無職の村人は, $(l:basics/cad_assembler)$(o)$(item)CAD組立機$(0)$(/l) を職業ブロックとして取得すると $(thing)魔法師$(0) になります.$(p)ほかの村人作業台と同じように, 村人が到達できる場所にCAD組立機を置いてください."},
            {"psi.book.page.psitweaks_magician.2", "魔法師は $(l:components/psitweaks_magicians_brain)$(o)$(item)魔法師の脳$(0)$(/l) の入手元でもあります. 魔法師の村人を $(l:psitweaks_spell_pieces/trick_guillotine)$(o)作動式: ギロチン$(0)$(/l) で倒したときにドロップし, 魔法師向けのレシピに使います."},
            {"psi.book.page.psitweaks_material.alloy_hypostasis", "$(l:components/psitweaks_alloy_psionic_echo)$(o)$(item)感応合金$(0)$(/l) にヒュポスタシスを注入して作ります.$(p)$(l:components/psitweaks_hypostasis_control_circuit)$(o)$(item)位格制御回路$(0)$(/l) や高 tier のサイオニック機械に使います."},
            {"psi.book.page.psitweaks_material.alloy_psion", "Mekanism の原子合金にサイジェムを注入して作ります.$(p)$(l:components/psitweaks_psionic_control_circuit)$(o)$(item)サイオニック制御回路$(0)$(/l), サイオニック合金 CAD 素体, 次 tier の合金素材に使います."},
            {"psi.book.page.psitweaks_material.alloy_psionic_echo", "$(l:components/psitweaks_alloy_psion)$(o)$(item)サイオニック合金$(0)$(/l) に$(l:components/psitweaks_psionic_echo)サイオニックエコー$(/l)を注入して作ります.$(p)$(l:components/psitweaks_echo_control_circuit)$(o)$(item)感応制御回路$(0)$(/l) や$(l:components/psitweaks_alloy_hypostasis)位格合金$(/l)の素材に使います."},
            {"psi.book.page.psitweaks_material.antinite_ingot", "アンティナイトの鉱石を精錬するか, アンティナイトブロックから戻して入手します. 粉, 欠片, 凝塊, 結晶, 汚れた粉は Mekanism 式鉱石処理の中間素材です.$(p)$(l:components/psitweaks_hypostasis_gem)ヒュポスタシスジェム$(/l)や$(l:items/psitweaks_philosophers_stone)賢者の石$(/l)の素材になります."},
            {"psi.book.page.psitweaks_material.chaotic_factor", "$(item)カオティック因子$(0) は, $(item)エンダーパール$(0) にサイジェムを注入して $(item)サイオニック因子$(0) を作るところから始まります. そこへアイボリーまたはエボニーを注入して偏向因子を作り, 反対属性を重ねると $(item)カオティック因子$(0) になります.$(p)通常のサイメタルを上回る最初の PsiTweaks 金属, $(l:components/psitweaks_chaotic_psimetal)$(o)$(item)カオティックサイメタル$(0)$(/l) の注入源です."},
            {"psi.book.page.psitweaks_material.chaotic_psimetal", "通常の $(item)サイメタル$(0) に$(l:components/psitweaks_chaotic_factor)カオティック因子$(/l)を注入して作ります.$(p)カオティックサイメタル CAD 素体や $(l:components/psitweaks_unrefined_flashmetal)$(o)$(item)未精製フラッシュメタル$(0)$(/l) のレシピに使います."},
            {"psi.book.page.psitweaks_material.echo_control_circuit", "$(l:components/psitweaks_psionic_control_circuit)$(o)$(item)サイオニック制御回路$(0)$(/l), $(l:components/psitweaks_alloy_psionic_echo)$(o)$(item)感応合金$(0)$(/l), $(l:components/psitweaks_echo_sheet)HDΨE シート$(/l)からクラフトします.$(p) 上位の術式弾や機械のレシピに使います."},
            {"psi.book.page.psitweaks_material.echo_pellet", "$(l:components/psitweaks_psionic_echo)サイオニックエコー$(/l)加工から得られる圧縮 HDΨE 素材です.$(p)生の$(l:components/psitweaks_psionic_echo)サイオニックエコー$(/l)ではなく, 凝縮した感応素材を要求するレシピに使います. 副産物として得られるΨE-O燃料はガス発電機の燃料として使えます."},
            {"psi.book.page.psitweaks_material.echo_sheet", "HDΨE 素材をシート状に濃縮して作ります.$(p)感応制御回路と位格制御回路のレシピで多く使います."},
            {"psi.book.page.psitweaks_material.flashmetal", "$(l:components/psitweaks_unrefined_flashmetal)$(o)$(item)未精製フラッシュメタル$(0)$(/l) を濃縮して作ります.$(p)フラッシュメタルブロック, フラッシュメタル CAD 素体, $(l:components/psitweaks_heavy_psimetal)ヘビーサイメタル$(/l), 昇華 tier の術式弾に使います."},
            {"psi.book.page.psitweaks_material.heavy_psimetal", "$(l:components/psitweaks_heavy_psimetal_scrap)$(o)$(item)ヘビーサイメタルの欠片$(0)$(/l) と $(l:components/psitweaks_flashmetal)$(o)$(item)フラッシュメタル$(0)$(/l) からクラフトします.$(p)より強い CAD 素体, 装備, $(l:components/psitweaks_psycheonic_metal_ingot)プシオニックメタル系列$(/l)に使います."},
            {"psi.book.page.psitweaks_material.heavy_psimetal_scrap", "ネザライトの欠片に$(l:components/psitweaks_psionic_echo)サイオニックエコー$(/l)を注入して作ります.$(p)$(l:components/psitweaks_flashmetal)$(o)$(item)フラッシュメタル$(0)$(/l) と組み合わせて $(l:components/psitweaks_heavy_psimetal)$(o)$(item)ヘビーサイメタル$(0)$(/l) を作ります."},
            {"psi.book.page.psitweaks_material.hypostasis_control_circuit", "$(l:components/psitweaks_echo_control_circuit)$(o)$(item)感応制御回路$(0)$(/l), $(l:components/psitweaks_alloy_hypostasis)$(o)$(item)位格合金$(0)$(/l), $(l:components/psitweaks_echo_sheet)HDΨE シート$(/l)からクラフトします.$(p)最上位の機械に使います."},
            {"psi.book.page.psitweaks_material.hypostasis_gem", "$(item)アンティナイトブロック$(0) に物質変成を作用させて作ります. 術式でも$(l:psitweaks_machines/material_mutator)物質変成機$(/l)でも入手できます.$(p)濃縮してヒュポスタシス注入へ変換できるほか, $(l:components/psitweaks_magatama)$(o)$(item)勾玉$(0)$(/l), $(l:components/psitweaks_alloy_hypostasis)位格合金$(/l), $(l:components/psitweaks_psycheonic_metal_ingot)プシオニックメタル系列$(/l)に使います."},
            {"psi.book.page.psitweaks_material.jade", "$(item)エメラルドブロック$(0) に物質変成を作用させて作ります. 術式でも$(l:psitweaks_machines/material_mutator)物質変成機$(/l)でも入手できます.$(p)$(l:components/psitweaks_magatama)$(o)$(item)勾玉$(0)$(/l) を作るための主素材です."},
            {"psi.book.page.psitweaks_material.magatama", "$(l:components/psitweaks_jade)$(o)$(item)翡翠$(0)$(/l) で $(l:components/psitweaks_hypostasis_gem)$(o)$(item)ヒュポスタシスジェム$(0)$(/l) を囲んでクラフトします.$(p)$(l:psitweaks_machines/spellmachinery_casing)スペル機械筐体$(/l)に使うため, 物質変成素材と上位サイオニック機械をつなぐ素材です."},
            {"psi.book.page.psitweaks_material.magicians_brain", "魔法師の村人を $(l:psitweaks_spell_pieces/trick_guillotine)$(o)作動式: ギロチン$(0)$(/l) で倒したときにドロップします.$(p)$(l:items/psitweaks_sorcery_booster)$(o)$(item)ソーサリーブースター$(0)$(/l) と事象干渉力増強ユニットの素材に使います."},
            {"psi.book.page.psitweaks_material.pellet_americium", "$(item)プルトニウムブロック$(0) を $(l:psitweaks_spell_pieces/trick_material_mutation)$(o)作動式: 物質変成$(0)$(/l) で変成するか, $(l:psitweaks_machines/material_mutator)$(o)物質変成機$(0)$(/l) で加工して作ります.$(p)プシオニックメタルCAD素体など, 終盤のサイオニックレシピに使います."},
            {"psi.book.page.psitweaks_material.pellet_neptunium", "$(item)ポロニウムブロック$(0) を $(l:psitweaks_spell_pieces/trick_material_mutation)$(o)作動式: 物質変成$(0)$(/l) で変成するか, $(l:psitweaks_machines/material_mutator)$(o)物質変成機$(0)$(/l) で加工して作ります.$(p)$(l:psitweaks_machines/transcendent_universal_cable)$(o)$(item)超越ユニバーサルケーブル$(0)$(/l) や MekaSuit の $(item)事象干渉力増大ユニット$(0) に使います."},
            {"psi.book.page.psitweaks_material.psionic_control_circuit", "$(l:components/psitweaks_alloy_psion)$(o)$(item)サイオニック合金$(0)$(/l) と Mekanism の究極制御回路からクラフトします.$(p)PsiTweaks の機械, 強化術式弾などのレシピに使います."},
            {"psi.book.page.psitweaks_material.psionic_echo", "$(item)残響の欠片$(0) にサイオニックエコーを注入するか, $(l:psitweaks_spell_pieces/trick_supreme_infusion)$(o)作動式: 超位注入$(0)$(/l)で作成します.$(p)$(l:components/psitweaks_alloy_psionic_echo)感応系合金$(/l), HDΨE 素材, 気体加工, 上位のサイオニック機械の基礎触媒です."},
            {"psi.book.page.psitweaks_material.psycheonic_metal_ingot", "$(l:components/psitweaks_heavy_psimetal)$(o)$(item)ヘビーサイメタル$(0)$(/l) にヒュポスタシスを注入して $(item)プシオニックメタルナゲット$(0) を作り, 9個クラフトするとインゴットになります.$(p)$(l:items/psitweaks_spell_bullets)超越 tier の術式弾$(/l), プシオニックメタル CAD 素体, 最上位のサイオニック素材に使います."},
            {"psi.book.page.psitweaks_material.unrefined_flashmetal", "$(l:components/psitweaks_chaotic_psimetal)$(o)$(item)カオティックサイメタル$(0)$(/l) と精製グロウストーンからクラフトします.$(p)濃縮すると使用可能な $(l:components/psitweaks_flashmetal)$(o)$(item)フラッシュメタル$(0)$(/l) になります."},
            {"psi.book.page.psitweaks_mekanism_integration.0", "$(thing)PsiTweaks$(0) は $(thing)Psi$(0) を $(thing)Mekanism$(0) の進行へ接続します. 連携要素には, Psi素材を作る冶金吹き込みレシピ, 魔法師能力を伸ばすMekaSuitモジュール, Psi・FE・サイオニック資源を扱う機械があります.$(p)正確なレシピはJEIで確認してください. この項目では各要素の役割をまとめます."},
            {"psi.book.page.psitweaks_mekanism_integration.1", "$(item)冶金吹き込み機$(0) では, PsiTweaks の注入タイプを使って主要なPsi素材を作れます. サイジェム注入はレッドストーンを$(l:components/psidust)$(o)$(item)サイダスト$(0)$(/l)に, 金インゴットを$(item)サイメタル$(0)に変換します.$(p)エボニー注入とアイボリー注入では, $(item)エボニー基質$(0), $(item)アイボリー基質$(0), それぞれのサイメタル派生素材を作れます. さらに$(l:components/psitweaks_chaotic_factor)サイオニック因子$(/l), $(l:components/psitweaks_chaotic_psimetal)$(o)$(item)カオティックサイメタル$(0)$(/l), $(l:components/psitweaks_psionic_echo)サイオニックエコー素材$(/l), $(l:components/psitweaks_alloy_hypostasis)位格合金$(/l)へ発展します."},
            {"psi.book.page.psitweaks_mekanism_integration.2", "PsiTweaks は魔法師向けのMekaSuitモジュールを3種類追加します. $(item)サイオン供給ユニット$(0) と $(item)サイオン容量ユニット$(0) はMekaSuit胴体に装着し, Psi回復速度と最大Psi保有量を強化します.$(p)$(item)事象干渉力増大ユニット$(0) はMekaSuitヘルメットに装着し, 術式ダメージを増加させます."},
            {"psi.book.page.psitweaks_overview.0", "$(thing)PsiTweaks$(0) は $(thing)Psi$(0) の拡張modです. 素材生成の工業化や, 新しい装備, 新しい術式など多数の要素を追加します.$(p)通常のCADや基本術式弾で終わらず, Psiを戦闘やユーティリティとして終盤まで強力に扱う構成を想定しています."},
            {"psi.book.page.psitweaks_overview.2", "主な追加要素は, 高位術式弾, 新しいCAD素体や詠唱補助具,新しい魔法, 加工機械や発電機です.$(p)多くのシステムは, Psiと工業環境の橋渡しや, Psiの終盤での能力強化などを目的として作られています."},
            {"psi.book.page.psitweaks_research.0", "$(thing)PsiTweaks$(0) が追加する一部のスペルピースは, 術式プログラムで使う前に研究によるアンロックが必要です.$(p)研究では, それらのスペルピースに対応するプログラムアイテムを作成します. プログラムを右クリックで使用することでスペルピースをアンロックできます(プログラムは消費しません)."},
            {"psi.book.page.psitweaks_research.1", "$(l:psitweaks_machines/program_researcher)$(o)$(item)プログラム研究台$(0)$(/l) に必要素材を入れて FE を供給すると, プログラムをクラフトできます.$(p)各研究に必要な素材, 消費電力, 処理時間は JEI で確認できます."},
            {"psi.book.page.psitweaks_spell_bullets.0", "$(thing)PsiTweaks$(0) は通常の$(l:items/spell_bullet)$(o)$(item)術式弾$(0)$(/l)より上位の強化ティアとして, 改良, 共鳴, 昇華, 覚醒, 超越を追加します.$(p)Psiの術式弾のバリエーション全てにそれぞれ上位Tierが用意されています."},
            {"psi.book.page.psitweaks_spell_bullets.1", "術式弾の強化は前段階の術式弾を4つ必要とするため、非常に高コストです."},
            {"psi.book.page.psitweaks_spell_bullets.2", "上位のTierになるほどコスト効率が著しく改善し、強力な魔法を簡単に発動できるようになります."},
            {"psi.book.page.psitweaks_spellpiece.selector_nearby_spellgram", "指定座標の周囲にある魔法式オブジェクトを取得します. 設置済みの魔法式オブジェクトを制御する術式で主に使います."},
            {"psi.book.page.psitweaks_spellpiece.selector_stored_entity", "CADメモリに保存されたUUIDからエンティティを取得します. "},
            {"psi.book.page.psitweaks_spellpiece.trick_active_air_mine", "指定座標に球状の衝撃波を作り, 範囲内の生物にダメージを与えます. 場所を指定して起爆する範囲攻撃です."},
            {"psi.book.page.psitweaks_spellpiece.trick_aqua_cutter", "前方へ水刃の発射体を放つ序盤用の攻撃術式です."},
            {"psi.book.page.psitweaks_spellpiece.trick_barrier", "被ダメージを軽減する障壁効果を付与します.(レベル * 4)だけ被ダメージを減少させます."},
            {"psi.book.page.psitweaks_spellpiece.trick_blaze_ball", "前方へ火の弾を放つ序盤用の攻撃術式です."},
            {"psi.book.page.psitweaks_spellpiece.trick_break_fortune", "対象ブロックを幸運付きで破壊します."},
            {"psi.book.page.psitweaks_spellpiece.trick_break_silk", "対象ブロックをシルクタッチ付きで破壊します."},
            {"psi.book.page.psitweaks_spellpiece.trick_cocytus", "対象モブの精神を永久に凍結させます. 単なるダメージではなく行動を封じる, 非常に強力な制御術式です."},
            {"psi.book.page.psitweaks_spellpiece.trick_cure_radiation", "対象の被ばく量を除去します."},
            {"psi.book.page.psitweaks_spellpiece.trick_die_flex", "Psi本体の停止と同様の動作を行い, 未実行分のスペルピースのPsiコストを返還します. 高頻度詠唱ではクライアント側のPsi表示が一時的にずれることがあります."},
            {"psi.book.page.psitweaks_spellpiece.trick_dispel", "対象エンティティからエフェクトを除去します. 良性・悪性を区別しない汎用版の解呪です."},
            {"psi.book.page.psitweaks_spellpiece.trick_dispel_beneficial", "対象エンティティから有益なエフェクトだけを除去します. 敵対対象の強化を剥がす用途に向いています."},
            {"psi.book.page.psitweaks_spellpiece.trick_dispel_non_beneficial", "対象エンティティから有益でないエフェクトを除去します. 味方のバフを残したまま悪性効果を消したい時に使います."},
            {"psi.book.page.psitweaks_spellpiece.trick_explode_no_destroy", "ブロックを破壊しない爆発を起こしてダメージを与えます. ドロップアイテムなどは消滅するので注意してください."},
            {"psi.book.page.psitweaks_spellpiece.trick_flare_circle", "炎の魔法式サークルを設置し, 内部の生物に継続的な炎ダメージを与えます. 一度設置したサークルは60秒残り続けます."},
            {"psi.book.page.psitweaks_spellpiece.trick_flight", "対象にクリエイティブ飛行を可能にする効果を与えます. "},
            {"psi.book.page.psitweaks_spellpiece.trick_freeze_block", "対象ブロックを1段階凍結させます. 水は氷, 氷は氷塊, 氷塊は青氷, 溶岩はマグマブロック, マグマブロックは黒曜石になります."},
            {"psi.book.page.psitweaks_spellpiece.trick_guillotine", "対象に強力な斬撃ダメージを与え, 討伐時に頭をドロップさせます. 単体対象の攻撃術式です."},
            {"psi.book.page.psitweaks_spellpiece.trick_hardening", "大きな被ダメージを一定値まで抑える硬化効果を付与します. 受ける最大ダメージを(レベル - 2)に抑えます."},
            {"psi.book.page.psitweaks_spellpiece.trick_ice_circle", "氷の魔法式サークルを設置し, 内部の生物に継続的な凍結ダメージを与えます. ファイアサークルと同じく領域制圧に向いています."},
            {"psi.book.page.psitweaks_spellpiece.trick_interact_block", "対象ブロックに対して, 術者のオフハンドのアイテムで右クリックしたように作用します."},
            {"psi.book.page.psitweaks_spellpiece.trick_material_mutation", "特定のブロックを破壊して別のアイテムへ変成させます. 物質変成機はこの処理を電力と気化サイオニックエコーで実行できます."},
            {"psi.book.page.psitweaks_spellpiece.trick_melt_block", "対象ブロックを1段階溶解させます. 氷, 氷塊, 青氷は水に, 黒曜石, 石系, 丸石系はマグマブロックに, マグマブロックは溶岩になります."},
            {"psi.book.page.psitweaks_spellpiece.trick_molecular_divider", "三点で定義した平面で生物を切断します. 高威力の範囲攻撃術式です."},
            {"psi.book.page.psitweaks_spellpiece.trick_parade", "確率で攻撃を回避する効果を付与します. (62.5 + 7.5 * レベル) % で攻撃を回避します."},
            {"psi.book.page.psitweaks_spellpiece.trick_phonon_maser", "超音波振動による高威力の熱線を放ちます. 攻撃用の強力な術式です."},
            {"psi.book.page.psitweaks_spellpiece.trick_radiation_filter", "対象に放射線防護効果を付与し、放射線の影響から身を守ります."},
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
            case "ja_jp" -> "$(thing)PsiTweaks$(0) は $(thing)Psi$(0) の拡張modです. 素材生成の工業化や, 新しい装備, 新しい術式など多数の要素を追加します.$(p)通常のCADや基本術式弾で終わらず, Psiを戦闘やユーティリティとして終盤まで強力に扱う構成を想定しています.";
            default -> "$(thing)PsiTweaks$(0) is an expansion for $(thing)Psi$(0). It adds many elements, including industrialized material production, new gear, and new spells.$(p)The mod is intended for setups where Psi remains powerful through the late game as both combat and utility, rather than ending at ordinary CADs and basic spell bullets.";
        });
        root.addProperty("psi.book.page.psitweaks_overview.2", switch (locale) {
            case "ja_jp" -> "主な追加要素は, 高位術式弾, 新しいCAD素体や詠唱補助具, 新しい魔法, 加工機械や発電機です.$(p)多くのシステムは, Psiと工業環境の橋渡しや, Psiの終盤での能力強化などを目的として作られています.";
            default -> "Major additions include higher-tier spell bullets, new CAD assemblies and casting support tools, new spells, processing machines, and generators.$(p)Most systems are built to bridge Psi with industrial environments and to strengthen Psi's late-game capabilities.";
        });
        root.addProperty("psi.book.entry.psitweaks_changes", switch (locale) {
            case "ja_jp" -> "PsiTweaksによる変更";
            default -> "Changes by PsiTweaks";
        });
        root.addProperty("psi.book.page.psitweaks_changes.0", switch (locale) {
            case "ja_jp" -> "$(thing)Psi: Tweaks And Additions$(0) を導入することによって QOL を改善するためのいくつかの調整が加えられます.$(p)ダメージを受けた際に Psi量 が減少しなくなり, 詠唱時の Psi回復クールタイムも撤廃されます.$(p)スペルプログラム画面では, 現在の設定言語に関わらず英語でスペルピースを検索することができます.";
            default -> "Installing $(thing)Psi: Tweaks And Additions$(0) applies several adjustments to improve QOL.$(p)Psi is no longer reduced when you take damage, and the Psi regeneration cooldown after casting is removed.$(p)In the Spell Programmer screen, spell pieces can be searched in English regardless of the current language setting.";
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
        addSpellPiece(root, "trick_freeze_block", "Trick: Block Freeze", "Freeze the block at the target position into its next colder form", "作動式: ブロック凍結", "指定座標のブロックを次の凍結段階に変化させる");
        addSpellPiece(root, "trick_melt_block", "Trick: Block Melt", "Melt the block at the target position into its hotter form", "作動式: ブロック溶解", "指定座標のブロックを溶解して高温の状態に変化させる");
        addSpellPiece(root, "trick_break_fortune", "Trick: Break Block (Fortune)", "Break a block with Fortune applied", "作動式: ブロック破壊(幸運)", "幸運付きでブロックを破壊する");
        addSpellPiece(root, "trick_break_silk", "Trick: Break Block (Silk Touch)", "Break a block with Silk Touch applied", "作動式: ブロック破壊(シルクタッチ)", "シルクタッチ付きでブロックを破壊する");
        addSpellPiece(root, "trick_store_entity", "Trick: Store Entity", "Store the entity's UUID string in the CAD memory", "作動式: エンティティ保存", "エンティティのUUIDをStringとしてCADメモリに保存する");
        addSpellPiece(root, "selector_stored_entity", "Selector: Stored Entity", "Retrieve entities from the UUID stored in the CAD memory", "取得子: 保存されたエンティティ", "CADのメモリに保存されたUUIDからエンティティを取得する");
        addSpellPiece(root, "trick_store_value", "Trick: Store Value", "Store a Plain Value in CAD memory", "作動式: 値を保存", "Plain ValueをCADメモリに保存する");
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
        addSpellPiece(root, "trick_jump", "Trick: Jump", "If the target number's absolute value is less than 1, jumps forward to the next Jump Anchor with the same constant label.", "作動式: ジャンプ", "対象数値の絶対値が1未満なら、同じ定数ラベルを持つ次のジャンプアンカーまで前方にジャンプします。");
        addSpellPiece(root, "trick_switch", "Trick: Switch", "Jumps forward to the next Jump Anchor with the same constant label as the input String. If no matching anchor exists, execution continues.", "作動式: スイッチ", "入力Stringと同じ定数ラベルを持つ次のジャンプアンカーまで前方にジャンプします。一致するアンカーがない場合はそのまま次へ進みます。");
        addSpellPiece(root, "jump_anchor", "Jump Anchor", "A no-op marker used as the destination for Trick: Jump and Trick: Switch. It can take an optional constant label.", "ジャンプアンカー", "作動式: ジャンプと作動式: スイッチの到達点になる、何もしない目印です。任意の定数ラベルを指定できます。");
        addSpellPiece(root, "trick_material_mutation", "Trick: Material Mutation", "Acts on a specific block, alters its material structure, and transmutes it into a different substance.", "作動式: 物質変成", "特定のブロックに作用して物質構造を改変し異なる物質に変成させる");
        addSpellPiece(root, "trick_physical_propulsion", "Trick: Physical Propulsion", "Raycasts from the specified position in the Ray direction; if it hits a Simulated Contraption, applies propulsion to it.", "作動式: 物理推進", "指定位置からRay方向へレイキャストし、Simulated Contraption に命中したならば推進力を与える");
        addSpellPiece(root, "operator_tan", "Operator: Tangent", "tan(A)", "演算子: タンジェント", "tan(A)");
        addSpellPiece(root, "operator_atan", "Operator: Arc Tangent", "atan(A)", "演算子: アークタンジェント", "atan(A)");
        addSpellPiece(root, "operator_sinh", "Operator: Hyperbolic Sine", "sinh(A)", "演算子: ハイパボリックサイン", "sinh(A)");
        addSpellPiece(root, "operator_cosh", "Operator: Hyperbolic Cosine", "cosh(A)", "演算子: ハイパボリックコサイン", "cosh(A)");
        addSpellPiece(root, "operator_tanh", "Operator: Hyperbolic Tangent", "tanh(A)", "演算子: ハイパボリックタンジェント", "tanh(A)");
        addSpellPiece(root, "operator_greater_than", "Operator: Greater Than", "Outputs 1 if Value 1 is greater than Value 2, otherwise 0. Number and Vector inputs are accepted.", "演算子: ～より大きい", "値1が値2より大きいなら1、そうでなければ0を出力します。Number と Vector を入力できます。");
        addSpellPiece(root, "operator_greater_than_or_equal", "Operator: Greater Than or Equal", "Outputs 1 if Value 1 is greater than or equal to Value 2, otherwise 0. Number and Vector inputs are accepted.", "演算子: ～以上", "値1が値2以上なら1、そうでなければ0を出力します。Number と Vector を入力できます。");
        addSpellPiece(root, "operator_equal", "Operator: Equal", "Outputs 1 if Value 1 equals Value 2, otherwise 0. Block values compare by dimension, position, and block state.", "演算子: 等しい", "値1と値2が等しいなら1、そうでなければ0を出力します。Block 値はディメンション、座標、ブロック状態で比較します。");
        addSpellPiece(root, "constant_string", "Constant: String", "Outputs the entered string. Limited to 1000 characters.", "定数子: 文字列", "入力した文字列をそのまま出力します。最大1000文字です。");
        addSpellPiece(root, "operator_from_string", "Operator: From String", "Returns the input String as-is, or converts it into a Number or Vector for the selected mode.", "演算子: 文字列から変換", "選択中のモードに応じて String をそのまま返すか、Number または Vector に変換します。");
        addSpellPiece(root, "operator_list_from_string_list", "Operator: From String List", "Returns the input String List as-is, or converts it into a Number List or Vector List for the selected mode.", "演算子: 文字列リストから変換", "選択中のモードに応じて String List をそのまま返すか、Number List または Vector List に変換します。");
        addSpellPiece(root, "operator_to_string", "Operator: To String", "Converts an Any input into a String. Entity, Item, and Block values output registry IDs.", "演算子: 文字列へ変換", "Any 入力を String に変換します。Entity、Item、Block はレジストリIDを出力します。");
        addSpellPiece(root, "operator_list_to_string_list", "Operator: To String List", "Converts an Any input into a String List. Entity, Item, and Block values output registry IDs.", "演算子: 文字列リストへ変換", "Any 入力を String List に変換します。Entity、Item、Block はレジストリIDを出力します。");
        addSpellPiece(root, "selector_block", "Selector: Block", "Outputs a Block snapshot at the given position.", "取得子: ブロック", "指定座標にあるブロックを Block 型のスナップショットとして出力します。");
        addSpellPiece(root, "operator_block_id", "Operator: Block ID", "Outputs the registry ID of a Block value. Example: minecraft:stone", "演算子: ブロックID", "Block 値のレジストリIDを文字列として出力します。例: minecraft:stone");
        addSpellPiece(root, "operator_block_state", "Operator: Block State", "Outputs the saved block state of a Block value as a String. Example: minecraft:oak_stairs[facing=north]", "演算子: ブロックステート", "Block 値に保存されたブロックステートを文字列として出力します。例: minecraft:oak_stairs[facing=north]");
        addSpellPiece(root, "operator_block_state_entries", "Operator: Block State Entries", "Outputs the saved block state properties of a Block value as a String List.", "演算子: ブロックステート項目", "Block 値に保存されたブロックステートのプロパティ項目を String List として出力します。");
        addSpellPiece(root, "operator_tag_list", "Operator: Tag List", "Outputs registry tags for the target Entity, Item, Block, or other contextual value as a String List.", "演算子: タグリスト", "対象の Entity、Item、Block などのレジストリタグを String List として出力します。");
        addSpellPiece(root, "operator_block_position", "Operator: Block Position", "Outputs the saved position of a Block value as a plain Vector.", "演算子: ブロック座標", "Block 値に保存された座標を通常の Vector として出力します。");
        addSpellPiece(root, "selector_online_players", "Selector: Online Players", "Outputs the names of online players in the world as a String List.", "取得子: オンラインプレイヤー", "ワールド内のオンラインプレイヤー名を String List として取得します。");
        addSpellPiece(root, "selector_held_item", "Selector: Main-Hand Item", "Gets the target entity's main-hand item.", "取得子: 手持ちアイテム", "対象エンティティのメインハンドのアイテムを取得します。");
        addSpellPiece(root, "selector_selected_slot_item", "Selector: Selected Slot Item", "Gets the item in the caster's selected target slot.", "取得子: 選択スロットアイテム", "術者の選択対象スロットにあるアイテムを取得します。");
        addSpellPiece(root, "operator_item_count", "Operator: Item Count", "Outputs the stack count of an Item value.", "演算子: アイテム個数", "Item 値のスタック個数を Number として出力します。空の Item は0を出力します。");
        addSpellPiece(root, "selector_held_items", "Selector: Carried Items", "Outputs the target entity's carried items as an Item List.", "取得子: 所持アイテム", "対象 Entity の所持アイテムを Item List として取得します。");
        addSpellPiece(root, "selector_internal_items", "Selector: Internal Items", "Outputs the target block's internal inventory as an Item List.", "取得子: 内部アイテム", "対象ブロックの内部インベントリを Item List として取得します。");
        addSpellPiece(root, "selector_indexed_element", "Selector: Indexed Element", "Outputs the element at a zero-based index from the selected List mode. Negative indexes count back from the end.", "取得子: インデックス要素", "選択中の List モードから0始まりのインデックスにある要素を取得します。負のインデックスは末尾から数えます。");
        addSpellPiece(root, "selector_nbt", "Selector: NBT", "Outputs the selected target's top-level NBT as key:value strings. Entity mode omits the entity id.", "取得子: NBT", "選択モードの対象のNBTトップレベルを key:value 形式の String List として出力します。Entity モードではエンティティIDを含みません。");
        addSpellPiece(root, "selector_nbt_keys", "Selector: NBT Keys", "Outputs the selected target's top-level NBT keys as a String List. Entity mode omits the entity id.", "取得子: NBTキー", "選択モードの対象のNBTトップレベルキーを String List として出力します。Entity モードではエンティティIDを含みません。");
        addSpellPiece(root, "selector_nbt_value", "Selector: NBT Value", "Outputs the selected target's NBT value matching the String key or NBT path. Missing paths output an empty string.", "取得子: NBT値", "選択モードの対象のNBTから String キーまたはNBTパスに一致する値を出力します。一致しない場合は空文字列です。");
        addSpellPiece(root, "operator_string_partial_match", "Operator: Partial Match", "Outputs 1 if searching String 1 with String 2 finds matching text, otherwise outputs 0.", "演算子: 部分一致", "文字列1が文字列2での検索にマッチする文字列を含むなら1、そうでなければ0を出力します。");
        addSpellPiece(root, "operator_string_concat", "Operator: String Concat", "String 1 + String 2 + String 3", "演算子: 文字列結合", "文字列1 + 文字列2 + 文字列3");
        addSpellPiece(root, "operator_string_split", "Operator: String Split", "Splits String 1 with String 2 as a literal delimiter and outputs a String List.", "演算子: 文字列分割", "文字列1を文字列2のリテラル区切り文字で分割し、String List を出力します。");
        addSpellPiece(root, "operator_string_list_join", "Operator: String List Join", "Joins the input String List with String 2 and outputs a String.", "演算子: 文字列リスト結合", "入力リストを文字列2で結合し、String を出力します。");
        addSpellPiece(root, "operator_player_name", "Operator: Player Name", "Outputs the player name if the Entity is a player, otherwise outputs an empty string.", "演算子: プレイヤーネーム", "Entityがプレイヤーならプレイヤー名を、そうでなければ空文字列を出力します。");
        addSpellPiece(root, "operator_list_search", "Operator: List Search", "Keeps only elements whose string form matches the wildcard.", "演算子: リスト検索", "文字列化した値がワイルドカードに一致する要素だけを残します。");
        addSpellPiece(root, "operator_list_search_exclude", "Operator: List Search Exclude", "Removes elements whose string form matches the wildcard.", "演算子: リスト検索除外", "文字列化した値がワイルドカードに一致する要素を除外します。");
        addSpellPiece(root, "operator_random_element", "Operator: Random Element", "Outputs one random element from the selected List mode.", "演算子: ランダム要素", "選択中の List モードからランダムに1要素を出力します。");
        addSpellPiece(root, "operator_list_add", "Operator: List Add", "Adds up to three elements to the selected List mode.", "演算子: リスト追加", "選択中の List モードに最大3つの要素を追加します。");
        addSpellPiece(root, "operator_list_remove", "Operator: List Remove", "Removes up to three elements from the selected List mode.", "演算子: リスト削除", "選択中の List モードから最大3つの要素を削除します。");
        addSpellPiece(root, "operator_list_size", "Operator: List Size", "Outputs the size of any registered List input.", "演算子: リストサイズ", "登録済みの任意の List 入力の要素数を出力します。");
        addSpellPiece(root, "operator_list_exclusion", "Operator: List Exclusion", "Outputs the elements of List 1 that are not present in List 2 for the selected List mode.", "演算子: リスト除外", "選択中の List モードでリスト1からリスト2に含まれる要素を除外します。");
        addSpellPiece(root, "operator_list_intersection", "Operator: List Intersection", "Outputs the elements shared by two lists for the selected List mode.", "演算子: リスト共通部分", "選択中の List モードで2つのリストに共通する要素を出力します。");
        addSpellPiece(root, "operator_list_concatenation", "Operator: List Concatenation", "Combines two lists for the selected List mode.", "演算子: リスト結合", "選択中の List モードで2つのリストを結合します。");
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
        addBookPage(root, "trick_freeze_block", "Freezes the target block one step: water to ice, ice to packed ice, packed ice to blue ice, lava to magma block, and magma block to obsidian.", "対象ブロックを1段階凍結させます. 水は氷, 氷は氷塊, 氷塊は青氷, 溶岩はマグマブロック, マグマブロックは黒曜石になります.");
        addBookPage(root, "trick_melt_block", "Melts the target block one step: ice, packed ice, and blue ice become water; obsidian, stone-like blocks, and cobblestone-like blocks become magma blocks; magma blocks become lava.", "対象ブロックを1段階溶解させます. 氷, 氷塊, 青氷は水に, 黒曜石, 石系, 丸石系はマグマブロックに, マグマブロックは溶岩になります.");
        addBookPage(root, "trick_break_fortune", "Breaks the target block with Fortune.", "対象ブロックを幸運付きで破壊します.");
        addBookPage(root, "trick_break_silk", "Breaks the target block with Silk Touch.", "対象ブロックをシルクタッチ付きで破壊します.");
        addBookPage(root, "trick_store_entity", "Stores the target entity's UUID as a String value in CAD memory.", "対象エンティティのUUIDをString値としてCADメモリに保存します.");
        addBookPage(root, "selector_stored_entity", "Gets an entity from the UUID stored in CAD memory.", "CADメモリに保存されたUUIDからエンティティを取得します. ");
        addBookPage(root, "trick_store_value", "Stores a Plain Value in CAD memory. The memory slot uses Psi's standard numbering: 1 is the first slot. Number, Vector, and String values overwrite each other in the same slot.", "Plain ValueをCADメモリに保存します. メモリ番号はPsi標準と同じく1が最初のスロットです. Number, Vector, Stringは同じスロットで互いに上書きされます.");
        addBookPage(root, "selector_stored_value", "Gets a Plain Value from CAD memory. Use the mode button to choose String, Number, or Vector. String conversions are strict; Number and Vector cannot be converted directly.", "CADメモリからPlain Valueを取得します. モードボタンでString, Number, Vectorを選択します. String変換は厳格で, NumberとVectorは直接変換できません.");
        addBookPage(root, "selector_nearby_spellgram", "Gets SpellGram objects around the specified coordinates. It is mainly used by tricks that control placed SpellGram objects.", "指定座標の周囲にある魔法式オブジェクトを取得します. 設置済みの魔法式オブジェクトを制御する術式で主に使います.");
        addBookPage(root, "trick_dispel", "Removes effects from the target entity. This is the general-purpose dispel that does not distinguish between beneficial and harmful effects.", "対象エンティティからエフェクトを除去します. 良性・悪性を区別しない汎用版の解呪です.");
        addBookPage(root, "trick_dispel_beneficial", "Removes only beneficial effects from the target entity. It is suited for stripping enhancements from hostile targets.", "対象エンティティから有益なエフェクトだけを除去します. 敵対対象の強化を剥がす用途に向いています.");
        addBookPage(root, "trick_dispel_non_beneficial", "Removes non-beneficial effects from the target entity. Use it when you want to cleanse harmful effects while leaving allied buffs intact.", "対象エンティティから有益でないエフェクトを除去します. 味方のバフを残したまま悪性効果を消したい時に使います.");
        addBookPage(root, "trick_cocytus", "Permanently freezes the mind of the target mob. Rather than merely dealing damage, this very powerful control trick prevents action.", "対象モブの精神を永久に凍結させます. 単なるダメージではなく行動を封じる, 非常に強力な制御術式です.");
        addBookPage(root, "trick_supply_fe", "Supplies FE to the target block. When CAD Efficiency is 100, it supplies 20 FE per Psi.", "対象ブロックへFEを供給します. 供給量はCADの効率が100のとき、1psiあたり20FEです.");
        addBookPage(root, "trick_time_accelerate", "Multiplies the target block's tick progression by (2 ^ power). The upper limit is 512x speed.", "対象ブロックのtick進行を (2 ^ 威力) 倍にします.上限は512倍速まで.");
        addBookPage(root, "trick_phonon_maser", "Fires a high-power heat ray using ultrasonic vibration. It is a powerful offensive trick.", "超音波振動による高威力の熱線を放ちます. 攻撃用の強力な術式です.");
        addBookPage(root, "trick_meteor_line", "Creates a beam from the specified position in the direction of the Ray vector, dealing special and lethally massive damage to living beings along its path.", "指定位置からRayベクトル方向へ光線を生み出し、経路上の生物に特殊かつ致死的な大ダメージを与えます。");
        addBookPage(root, "trick_supreme_infusion", "Infusion-converts Echo Shards into Psionic Echo.", "残響の欠片をサイオニックエコーへ注入変換します.");
        addBookPage(root, "trick_molecular_divider", "Cuts living beings along a plane defined by three points. It is a high-power area attack trick.", "三点で定義した平面で生物を切断します. 高威力の範囲攻撃術式です.");
        addBookPage(root, "trick_aqua_cutter", "Fires a water-blade projectile forward as an early-game offensive trick.", "前方へ水刃の発射体を放つ序盤用の攻撃術式です.");
        addBookPage(root, "trick_blaze_ball", "Fires a ball of flame forward as an early-game offensive trick.", "前方へ火の弾を放つ序盤用の攻撃術式です.");
        addBookPage(root, "trick_active_air_mine", "Creates a spherical shockwave at the specified coordinates and damages living beings within range. This is an area attack detonated at a chosen location.", "指定座標に球状の衝撃波を作り, 範囲内の生物にダメージを与えます. 場所を指定して起爆する範囲攻撃です.");
        addBookPage(root, "trick_flare_circle", "Places a fire SpellGram Circle that continuously deals fire damage to living beings inside it. Once placed, the circle remains for 60 seconds.", "炎の魔法式サークルを設置し, 内部の生物に継続的な炎ダメージを与えます. 一度設置したサークルは60秒残り続けます.");
        addBookPage(root, "trick_ice_circle", "Places an ice SpellGram Circle that continuously deals freezing damage to living beings inside it. Like Fire Circle, it is suited for area control.", "氷の魔法式サークルを設置し, 内部の生物に継続的な凍結ダメージを与えます. ファイアサークルと同じく領域制圧に向いています.");
        addBookPage(root, "trick_set_spellgram_follow_target", "Sets the follow target entity of a SpellGram object. For example, by setting the Fire Circle to follow the caster, it will continue to follow the caster and keep burning surrounding enemies.", "魔法式オブジェクトの追従対象エンティティを設定します. 例えば、ファイアサークルの追従対象を術者に設定することで、術者に追従し続け周囲の敵を燃やし続けます.");
        addBookPage(root, "trick_die_flex", "Behaves similarly to Psi's Trick: Die and refunds the Psi cost of spell pieces that were not executed. With high-frequency casting, the client-side Psi display may temporarily desync.", "Psi本体の停止と同様の動作を行い, 未実行分のスペルピースのPsiコストを返還します. 高頻度詠唱ではクライアント側のPsi表示が一時的にずれることがあります.");
        addBookPage(root, "trick_jump", "When the target number's absolute value is less than 1, jumps forward to the next Jump Anchor with the same label. The label input is optional, but if used it must be a String constant. Empty labels only match empty-label anchors. Put a Number constant of 0 into the target to use it as an unconditional forward jump. It cannot jump to Jump Anchors behind it.", "対象数値の絶対値が1未満なら, 同じラベルを持つ次のジャンプアンカーまで前方にジャンプします. ラベル入力は任意ですが, 使う場合はString定数である必要があります. 空ラベルは空ラベルのアンカーにのみ一致します. 対象にNumber定数の0を入れると無条件の前方ジャンプとして使えます. 後方のジャンプアンカーには飛ぶことができません.");
        addBookPage(root, "trick_switch", "Evaluates the input String and jumps forward to the next Jump Anchor whose constant label is equal to it. If no matching anchor exists ahead, it does not error and simply continues to the next spell piece, so place the default branch immediately after the Switch. It cannot jump to Jump Anchors behind it.", "入力Stringを評価し, それと等しい定数ラベルを持つ次のジャンプアンカーまで前方にジャンプします. 前方に一致するアンカーがない場合はエラーにならず, そのまま次のスペルピースへ進むため, default相当の処理はスイッチの直後に置いてください.  後方のジャンプアンカーには飛ぶことができません.");
        addBookPage(root, "jump_anchor", "A marker used as the destination for Trick: Jump and Trick: Switch. This spell piece itself does nothing. You can input a constant String as its label.", "作動式: ジャンプと作動式: スイッチの到達点になる目印です.  このスペルピース自体は何もしません. 定数文字列をラベルとして入力することができます.");
        addBookPage(root, "trick_radiation_injection", "Applies Mekanism radiation exposure to the target.", "対象へMekanismの放射線被ばくを付与します. ");
        addBookPage(root, "trick_radiation_filter", "Applies a radiation protection effect to the target, protecting them from radiation.", "対象に放射線防護効果を付与し、放射線の影響から身を守ります.");
        addBookPage(root, "trick_cure_radiation", "Removes the target's radiation exposure.", "対象の被ばく量を除去します.");
        addBookPage(root, "trick_guillotine", "Deals powerful slash damage to the target and makes it drop a head when killed. This is a single-target offensive trick.", "対象に強力な斬撃ダメージを与え, 討伐時に頭をドロップさせます. 単体対象の攻撃術式です.");
        addBookPage(root, "trick_material_mutation", "Breaks specific blocks and transmutes them into other items. The Material Mutator can perform this process using power and Vaporized Psionic Echo.", "特定のブロックを破壊して別のアイテムへ変成させます. 物質変成機はこの処理を電力と気化サイオニックエコーで実行できます.");
        addBookPage(root, "trick_physical_propulsion", "Raycasts from the specified position in the Ray direction; if it hits a Simulated Contraption, applies propulsion to it.", "指定位置からRay方向へレイキャストし、Simulated Contraption に命中したならば推進力を与えます.");
        addBookPage(root, "operator_tan", "Returns the tangent of the target number.", "対象数値のタンジェントを返します.");
        addBookPage(root, "operator_atan", "Returns the arc tangent of the target number.", "対象数値のアークタンジェントを返します.");
        addBookPage(root, "operator_sinh", "Returns the hyperbolic sine of the target number.", "対象数値のハイパボリックサインを返します. ");
        addBookPage(root, "operator_cosh", "Returns the hyperbolic cosine of the target number.", "対象数値のハイパボリックコサインを返します.");
        addBookPage(root, "operator_tanh", "Returns the hyperbolic tangent of the target number.", "対象数値のハイパボリックタンジェントを返します.");
        addBookPage(root, "operator_greater_than", "Compares Value 1 and Value 2. It outputs 1 when Value 1 is greater than Value 2, otherwise 0. Each input accepts either a Number or a Vector; Vector inputs are compared by magnitude.", "値1と値2を比較します. 値1が値2より大きいなら1, そうでなければ0を出力します. 各入力は Number または Vector を受け付け, Vector は大きさに変換して比較します.");
        addBookPage(root, "operator_greater_than_or_equal", "Compares Value 1 and Value 2. It outputs 1 when Value 1 is greater than or equal to Value 2, otherwise 0. Each input accepts either a Number or a Vector; Vector inputs are compared by magnitude.", "値1と値2を比較します. 値1が値2以上なら1, そうでなければ0を出力します. 各入力は Number または Vector を受け付け, Vector は大きさに変換して比較します.");
        addBookPage(root, "operator_equal", "Compares Value 1 and Value 2 as Any inputs. It outputs 1 when both values are equal, otherwise 0. Numbers compare by numeric value, Vectors compare by coordinates, Item values compare their item data and source, and Block values compare dimension, position, and block state. A Block compared with a Vector uses coordinate comparison.", "値1と値2を Any 入力として比較します. 2つの値が等しいなら1, そうでなければ0を出力します. Number は数値, Vector は座標, Item はアイテムデータと source, Block はディメンション, 座標, ブロック状態を比較します. Block と Vector の比較は座標比較です.");
        addBookPage(root, "constant_string", "Outputs the entered string as a String value. The value is saved with the spell and is limited to 1000 characters.", "入力した文字列をString値として出力します. 値は術式に保存され, 最大1000文字です.");
        addBookPage(root, "operator_from_string", "Converts the String input for the selected Plain mode. String mode returns the input as-is. Number mode parses finite numeric text and outputs 0 on invalid input. Vector mode accepts Vector[x,y,z], Vector(x,y,z), [x,y,z], or (x,y,z), case-insensitive for Vector, and outputs the zero vector when parsing fails. Left-click the spell piece to select the mode.", "選択中の Plain モードに応じて String 入力を変換します. String モードは入力をそのまま返します. Number モードは有限の数値文字列を解析し, 無効な入力では0を出力します. Vector モードは Vector[x,y,z], Vector(x,y,z), [x,y,z], (x,y,z) を受け付けます. Vector は大文字小文字を区別せず, 解析失敗時はゼロベクトルを出力します. スペルピースを左クリックでモードを選択できます.");
        addBookPage(root, "operator_list_from_string_list", "Converts a String List for the selected Plain mode. String mode returns the input list as-is. Number mode parses each entry as finite numeric text, so invalid entries become 0. Vector mode converts only entries matching Vector[x,y,z], Vector(x,y,z), [x,y,z], or (x,y,z), case-insensitive for Vector, and skips invalid entries. Left-click the spell piece to select the mode.", "選択中の Plain モードに応じて String List を変換します. String モードは入力リストをそのまま返します. Number モードは各要素を有限の数値文字列として解析し, 無効な要素は0になります. Vector モードは Vector[x,y,z], Vector(x,y,z), [x,y,z], (x,y,z) に一致する要素のみ変換し, 無効な要素は無視します. Vector は大文字小文字を区別しません. スペルピースを左クリックでモードを選択できます.");
        addBookPage(root, "operator_to_string", "Converts an Any input into a String. Entity, Item, and Block values output registry IDs, Number and Vector values use the same text form shown by Trick: Debug, and String values are returned as-is. List values convert each element by the same rules and join them with commas.", "Any 入力を String に変換します. Entity, Item, Block はレジストリIDを出力し, Number と Vector は 作動式: デバッグ と同じ文字列表現を使い, String はそのまま返します. List は各要素を同じ規則で変換し, コンマ区切りで結合します.");
        addBookPage(root, "operator_list_to_string_list", "Converts an Any input into a String List. Entity, Item, and Block values output registry IDs, Number and Vector values use the same text form shown by Trick: Debug, and String values are returned as-is. List inputs, including Block List, convert each element by the same rules while preserving order. Non-list inputs become a one-element String List.", "Any 入力を String List に変換します. Entity, Item, Block はレジストリIDを出力し, Number と Vector は 作動式: デバッグ と同じ文字列表現を使い, String はそのまま返します. Block List を含む List は入力順を維持して各要素を同じ規則で変換します. List 以外は1要素の String List になります.");
        addBookPage(root, "selector_block", "Outputs the block at the target position as a Block value. Block is a read-only snapshot containing the saved position, dimension, block state, registry ID, and block tags. Because Block extends Vector, it can also be connected to Vector inputs and used as its saved position.", "対象座標にあるブロックを Block 型として出力します. Block は保存座標, ディメンション, ブロック状態, レジストリID, ブロックタグを持つ読み取り用スナップショットです. Block は Vector を継承しているため, Vector 入力にも接続でき, 保存座標として扱えます.");
        addBookPage(root, "operator_block_id", "Outputs the registry ID stored in a Block value as a String, such as minecraft:stone. The ID comes from the Block snapshot and does not change if the world changes later.", "Block 値に保存されたレジストリIDを String として出力します. 例: minecraft:stone. ID は Block のスナップショットから取得され, 後からワールドが変化しても変わりません.");
        addBookPage(root, "operator_block_state", "Outputs the block state saved in a Block value as a String, using Minecraft's command-style form such as minecraft:oak_stairs[facing=north,half=bottom]. The value comes from the Block snapshot and does not change if the world changes later.", "Block 値に保存されたブロックステートを String として出力します. 出力は minecraft:oak_stairs[facing=north,half=bottom] のような Minecraft のコマンド風表記です. 値は Block のスナップショットから取得され, 後からワールドが変化しても変わりません.");
        addBookPage(root, "operator_block_state_entries", "Outputs the properties saved in a Block value's block state as a String List. Each entry uses property:value form, such as facing:north or waterlogged:false. Blocks with no state properties output an empty String List.", "Block 値に保存されたブロックステートのプロパティを String List として出力します. 各要素は facing:north や waterlogged:false のような property:value 形式です. state property を持たないブロックは空の String List を出力します.");
        addBookPage(root, "operator_tag_list", "Outputs registry tags from an Entity, Item, Block, or supported contextual value as a String List. Entries use namespace:path form without #. Entity inputs return EntityType tags, not scoreboard tags.", "Entity, Item, Block, または対応する contextual value からレジストリタグを String List として出力します. 各要素は # を付けない namespace:path 形式です. Entity 入力は EntityType のタグを返し, スコアボードタグは含みません.");
        addBookPage(root, "operator_block_position", "Outputs the saved Block position as a plain Vector. Use this when a spell should intentionally drop Block metadata and keep only coordinates.", "Block に保存された座標を通常の Vector として出力します. Block のメタデータを意図的に捨て, 座標だけを残したい場合に使います.");
        addBookPage(root, "selector_online_players", "Outputs the names of all online players in the caster's current world as a String List.", "術者の現在ワールドにいるオンラインプレイヤー全員の名前を String List として出力します.");
        addBookPage(root, "selector_held_item", "Outputs the target living entity's main-hand ItemStack as an Item value. Item is a read-only snapshot containing item data such as kind, count, durability, and name. Note that Psi's Selector: Nearby Items returns dropped item entities, which are different from the Item type added by PsiTweaks.", "対象 LivingEntity のメインハンドの ItemStack を Item 型として出力します. Item型は, アイテムの種類, 個数, 耐久値, 名前などを持つ読み取り用の値です. Psi の 取得子: 近くのアイテム で取得できるドロップアイテムはエンティティであり, PsiTweaksで追加されるItem型とは異なることに注意してください.");
        addBookPage(root, "selector_selected_slot_item", "Outputs the ItemStack in the caster's selected target slot as an Item value. The slot is resolved by Psi's target slot context, so Trick: Change Slot and Trick: Switch Target Slot affect it. Empty slots output an empty Item.", "術者の選択対象スロットにある ItemStack を Item 型として出力します. スロットは Psi の target slot context で解決されるため, 作動式: スロット変更 や 作動式: ターゲットスロット切替 の影響を受けます. 空スロットは空の Item を出力します.");
        addBookPage(root, "operator_item_count", "Outputs the stack count of an Item value as a Number. Empty Item values output 0.", "Item 値のスタック個数を Number として出力します. 空の Item は0を出力します.");
        addBookPage(root, "selector_held_items", "Outputs carried items from the target Entity as an Item List. Player inventories are read slot by slot; other entities use their item handler when available, or their hands and armor slots as a fallback. Empty inventories output an empty Item List.", "対象 Entity の所持アイテムを Item List として出力します. Player はインベントリをスロット単位で読み取り, その他の Entity は ItemHandler があればそれを使い, なければ手持ちと防具スロットを読み取ります. 空の場合は空の Item List を出力します.");
        addBookPage(root, "selector_internal_items", "Outputs items from the target block's internal inventory as an Item List. Item handlers and containers are read slot by slot. Blocks without readable inventories output an empty Item List.", "対象ブロックの内部インベントリを Item List として出力します. ItemHandler と Container をスロット単位で読み取ります. 読み取り可能なインベントリがない場合は空の Item List を出力します.");
        addBookPage(root, "selector_indexed_element", "Outputs the element at a zero-based index from the selected List mode. Negative indexes count back from the end: -1 returns the last element, -2 returns the element before it. Available modes come from Psitweaks list adapters. Out-of-range indexes produce the same out-of-bounds spell error as Psi's Indexed Element.", "選択中の List モードから, 0始まりのインデックスにある要素を出力します. 負のインデックスは末尾から数え, -1 は末尾要素, -2 はその1つ前の要素を返します. 利用可能なモードは Psitweaks の list adapter から追加されます. 範囲外のインデックスは Psi 本体の Indexed Element と同じ範囲外エラーになります.");
        addBookPage(root, "selector_nbt", "Outputs the selected target's top-level NBT as key:value strings in a String List. Use the mode button to choose Entity, Item, Block, or another supported contextual target. Block mode reads BlockEntity NBT; blocks without a BlockEntity output an empty list. Values use SNBT text. Entity mode intentionally omits the entity id because entity type IDs have a dedicated selector.", "選択モードの対象のNBTトップレベルを key:value 形式の String List として出力します. モードボタンで Entity, Item, Block, または対応する別の contextual target を選択します. Block モードでは BlockEntity のNBTを読み取り, BlockEntity がないブロックは空リストになります. 値はSNBT文字列です. Entity モードではエンティティIDは専用の取得子があるため意図的に含めません.");
        addBookPage(root, "selector_nbt_keys", "Outputs the selected target's top-level NBT keys as a String List. Use the mode button to choose Entity, Item, Block, or another supported contextual target. Block mode reads BlockEntity NBT; blocks without a BlockEntity output an empty list. Entity mode intentionally omits the entity id because entity type IDs have a dedicated selector.", "選択モードの対象のNBTトップレベルキーを String List として出力します. モードボタンで Entity, Item, Block, または対応する別の contextual target を選択します. Block モードでは BlockEntity のNBTを読み取り, BlockEntity がないブロックは空リストになります. Entity モードではエンティティIDは専用の取得子があるため意図的に含めません.");
        addBookPage(root, "selector_nbt_value", "Outputs the selected target's NBT value matching the String key or NBT path as SNBT text. Top-level key matches are checked before path parsing, so existing key use stays compatible. Use paths such as Inventory[0].id or components.\"minecraft:custom_name\" for nested values. Use the mode button to choose Entity, Item, Block, or another supported contextual target. Block mode reads BlockEntity NBT. Missing paths, invalid paths, blocks without a BlockEntity, and the omitted Entity id key output an empty String.", "選択モードの対象のNBTから StringキーまたはNBTパスに一致する値をSNBT文字列として出力します. トップレベルキーの完全一致を先に確認するため, 既存のキー指定はそのまま使えます. 深い値には Inventory[0].id や components.\"minecraft:custom_name\" のようなパスを指定できます. モードボタンで Entity, Item, Block, または対応する別の contextual target を選択します. Block モードでは BlockEntity のNBTを読み取ります. パスが存在しない場合, 不正なパス, BlockEntity がないブロック, 省略対象の Entity のidキーは空文字列になります.");
        addBookPage(root, "operator_string_partial_match", "Searches String 1 with String 2 and checks whether it contains matching text. * matches any text, ? matches one character, and [abc] matches one of the listed characters. Escape wildcard characters with a backslash. Matching is case-sensitive.", "文字列1を文字列2で検索してマッチする文字列を含むか判定します. * は任意の文字列, ? は任意の1文字, [abc] は a/b/c のいずれか1文字に一致します. ワイルドカード文字はバックスラッシュでエスケープできます. 比較は大文字小文字を区別します.");
        addBookPage(root, "operator_string_concat", "Concatenates String 1, String 2, and optional String 3 in that order. If String 3 is not connected, it is treated as an empty string.", "文字列1, 文字列2, 任意の文字列3をこの順に結合します. 文字列3が未接続の場合は空文字列として扱います.");
        addBookPage(root, "operator_string_split", "Splits String 1 with String 2 as a literal delimiter and outputs a String List. If String 2 is not connected, a comma is used. Empty fields are preserved. Empty delimiters are invalid.", "文字列1を文字列2のリテラル区切り文字で分割し, String List を出力します. 文字列2が未接続の場合はカンマを使います. 空要素は保持されます. 空の区切り文字は使用できません.");
        addBookPage(root, "operator_string_list_join", "Joins the input String List with String 2 and outputs a String. If String 2 is not connected, a comma is used. Empty delimiters are allowed and concatenate the elements directly.", "入力リストを文字列2で結合し, String を出力します. 文字列2が未接続の場合はカンマを使います. 空の区切り文字は許可され, 要素を直接連結します.");
        addBookPage(root, "operator_player_name", "Outputs the player's name when the input Entity is a player. Non-player entities output an empty String.", "入力 Entity がプレイヤーならプレイヤー名を出力します. プレイヤーでない Entity は空文字列になります.");
        addBookPage(root, "operator_list_search", "Filters the selected List mode by comparing the String input with each element converted by Operator: To String. Entity, Item, and Block values compare by registry ID. The String input accepts wildcards: * matches any text, ? matches one character, and [abc] matches one of the listed characters. Without wildcards, matching is exact.", "選択中の List モードを, 各要素を 演算子: 文字列へ変換 と同じ規則で文字列化した値で絞り込みます. Entity, Item, Block はレジストリIDで比較します. String入力ではワイルドカードを利用でき, * は任意の文字列, ? は任意の1文字, [abc] は a/b/c のいずれか1文字に一致します. ワイルドカードなしでは完全一致です.");
        addBookPage(root, "operator_list_search_exclude", "Filters the selected List mode by removing elements whose String form matches the input wildcard. Entity, Item, and Block values compare by registry ID. * matches any text, ? matches one character, and [abc] matches one of the listed characters. Without wildcards, matching is exact.", "選択中の List モードから, 文字列化した値が入力ワイルドカードに一致する要素を除外します. Entity, Item, Block はレジストリIDで比較します. * は任意の文字列, ? は任意の1文字, [abc] は a/b/c のいずれか1文字に一致します. ワイルドカードなしでは完全一致です.");
        addBookPage(root, "operator_random_element", "Chooses one random element from the selected List mode. Available modes come from Psitweaks list adapters. Empty lists output the adapter-defined empty element value.", "選択中の List モードからランダムに1要素を選びます. 利用可能なモードは Psitweaks の list adapter から追加されます. 空リストの場合は adapter が定義した空要素値を出力します.");
        addBookPage(root, "operator_list_add", "Adds up to three element inputs to the selected List mode. Order, duplicate handling, and empty-list defaults are defined by that mode's Psitweaks list adapter.", "選択中の List モードに最大3つの要素入力を追加します. 順序, 重複の扱い, 空リストの初期値はそのモードの Psitweaks list adapter が定義します.");
        addBookPage(root, "operator_list_remove", "Removes up to three element inputs from the selected List mode. Matching and duplicate handling are defined by that mode's Psitweaks list adapter.", "選択中の List モードから最大3つの要素入力を削除します. 一致判定と重複の扱いはそのモードの Psitweaks list adapter が定義します.");
        addBookPage(root, "operator_list_size", "Outputs the number of elements in any registered List input. This accepts Psitweaks List types, Psi Entity List, and custom List types registered through the Psitweaks list adapter API.", "登録済みの任意の List 入力に含まれる要素数を出力します. Psitweaks の List 型, Psi の Entity List, および Psitweaks のリスト adapter API に登録された独自 List 型を受け付けます.");
        addBookPage(root, "operator_list_exclusion", "Returns List 1 with elements that are present in List 2 removed for the selected List mode. Matching, ordering, and duplicate behavior are defined by that mode's Psitweaks list adapter.", "選択中の List モードで List 1 から List 2 に含まれる要素を除外して返します. 一致判定, 順序, 重複の扱いはそのモードの Psitweaks list adapter が定義します.");
        addBookPage(root, "operator_list_intersection", "Returns elements from List 1 that are also present in List 2 for the selected List mode. Matching, ordering, and duplicate behavior are defined by that mode's Psitweaks list adapter.", "選択中の List モードで List 1 のうち List 2 にも含まれる要素を返します. 一致判定, 順序, 重複の扱いはそのモードの Psitweaks list adapter が定義します.");
        addBookPage(root, "operator_list_concatenation", "Combines two Lists for the selected List mode. Ordering and duplicate behavior are defined by that mode's Psitweaks list adapter.", "選択中の List モードで2つの List を結合します. 順序と重複の扱いはそのモードの Psitweaks list adapter が定義します.");
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
