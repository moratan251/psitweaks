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

    private void addProgrammingConceptsBook(JsonObject root) {
        addLocalized(root, "psi.book.entry.psitweaks_programming_types",
                "Types Added by PsiTweaks",
                "PsiTweaks 追加の型");
        addLocalized(root, "psi.book.page.psitweaks_programming_types.0",
                "$(thing)PsiTweaks$(0) adds new value types to Psi spell programming. The main additions are String, Block, Item, and Matrix.$(p)These types allow spells to work with text, block information, item information, and numerical grids.",
                "$(thing)PsiTweaks$(0) は, Psi のスペルプログラミングに新しい型を追加します. 主な追加型は String, Block, Item, Matrix です.$(p)これらの型を使うことで, 文字列, ブロック情報, アイテム情報, 数値の格子をスペル内で扱えるようになります.");
        addLocalized(root, "psi.book.page.psitweaks_programming_types.1",
                "String is the type used for text. It can hold IDs, names, NBT paths, search conditions, and messages for display.$(p)For example, a spell can obtain a block or item ID as a String and test whether it matches a particular ID.$(p)String values are limited to 8192 characters at runtime. Note that Constant: String pieces can only store 1024 characters inside the spell, because the value is saved with the spell itself.",
                "String 型は文字列を扱うための型です. ID, 名前, NBTパス, 検索条件, 表示用メッセージなどをスペル内で扱うために使用します.$(p)例えば, ブロックやアイテムのIDを String として取得し, 特定のIDと一致するかを調べることができます.$(p)実行時の String 値は 8192 文字までに制限されています. なお, 定数子: 文字列 は術式に値を保存するため, 1つのピースあたり最大1024文字までしか入力できません.");
        addLocalized(root, "psi.book.page.psitweaks_programming_types.2",
                "Block represents a block in the world. It provides information such as position, dimension, block state, registry ID, tags, and display name.$(p)A Vector represents coordinates, while a Block represents information about the block at those coordinates.",
                "Block 型はワールド上のブロックを扱うための型です. ブロックの座標, ディメンション, ブロック状態, レジストリID, タグ, 表示名などの情報を扱えます.$(p)Vector が「座標」を表すのに対し, Block は「その座標に存在するブロックの情報」を表します.");
        addLocalized(root, "psi.book.page.psitweaks_programming_types.3",
                "Because both relate to positions, many inputs convert Block and Vector automatically.$(p)A Block connected to a Vector input uses its position. A Vector connected to a Block input reads the block at that position when the spell runs.$(p)Any inputs, such as Operator: To String, preserve the original type.",
                "Block と Vector はどちらも座標に関係するため, 多くの入力で自動変換されます.$(p)Block を Vector 入力へ接続すると座標として扱われます. Vector を Block 入力へ接続すると, 実行時にその座標のブロックを読み取ります.$(p)演算子: 文字列へ変換 などの Any 入力では, 元の型がそのまま扱われます.");
        addLocalized(root, "psi.book.page.psitweaks_programming_types.4",
                "Item represents an ItemStack. It is used to inspect item type, stack count, NBT, display name, and similar data.$(p)Item values can be obtained from held items, selected inventory slots, or inventories inside blocks.$(p)Each Item value also records its source, so spells can tell where the item came from.",
                "Item 型は ItemStack を扱うための型です. アイテムの種類, 個数, NBT, 表示名などを調べるために使用します.$(p)Item 型は, 手に持っているアイテム, 指定スロットのアイテム, ブロック内部のインベントリにあるアイテムなどから取得できます.$(p)Item 型の値は取得元を source として保持しており, スペル内でそのアイテムがどこから来たかを判別できます.");
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
                "Psi often uses Entity List. PsiTweaks also allows lists of values other than Entity.$(p)The main additional list types are String List, Number List, Vector List, Item List, and Block List.",
                "Psi では Entity List がよく使われますが, PsiTweaks では Entity 以外のリストも扱えるようになります.$(p)追加される主なリストには, String List, Number List, Vector List, Item List, Block List があります.");
        addLocalized(root, "psi.book.page.psitweaks_lists.1",
                "A List groups multiple values of the same type.$(p)For example, multiple strings can be handled as a String List, while multiple items can be handled as an Item List.$(p)This makes searching, filtering, comparing, and counting multiple targets easier within a spell.",
                "List は同じ種類の値を複数まとめて扱うための型です.$(p)例えば, 複数の文字列を String List として扱ったり, 複数のアイテムを Item List として扱ったりできます.$(p)これにより, 複数対象の検索, 抽出, 比較, 集計などをスペル内で行いやすくなります.");
        addLocalized(root, "psi.book.page.psitweaks_lists.2",
                "PsiTweaks adds operators for working with Lists.$(p)They can retrieve elements or list size, add, remove, insert, slice, search, exclude, intersect, and concatenate values.$(p)List operations make it easier to build spells that process several targets together.",
                "PsiTweaks には List を操作するための演算子が追加されています.$(p)要素の取得, 要素数の取得, 追加, 削除, 挿入, 切り取り, 検索, 除外, 交差, 結合などを行えます.$(p)List 操作を使うことで, 複数の対象をまとめて処理するスペルを作りやすくなります.");

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
        addLocalized(root, "psi.book.page.psitweaks_mode_selection.3",
                "A mode-selectable spell piece can have a different meaning in each mode even though its icon is unchanged.$(p)This matters especially for Lists, because values such as String List and Item List behave differently. Check the current mode before connecting the piece.",
                "モード選択スペルピースでは, 同じ見た目のピースでもモードによって意味が変わります.$(p)特に List 系では, String List と Item List のように扱う値の性質が大きく異なります. スペルを組むときは, 現在のモードを確認してから接続してください.");
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
        addSpellPiece(root, "trick_jump", "Trick: Jump", "If the target number is unset or its absolute value is less than 1, jumps forward to the next Jump Anchor with the same constant label.", "作動式: ジャンプ", "対象数値が未入力、または絶対値が1未満なら、同じ定数ラベルを持つ次のジャンプアンカーまで前方にジャンプします。");
        addSpellPiece(root, "trick_switch", "Trick: Switch", "Jumps forward to the next Jump Anchor with the same constant label as the input String. If no matching anchor exists, execution continues.", "作動式: スイッチ", "入力Stringと同じ定数ラベルを持つ次のジャンプアンカーまで前方にジャンプします。一致するアンカーがない場合はそのまま次へ進みます。");
        addSpellPiece(root, "jump_anchor", "Jump Anchor", "A no-op marker used as the destination for Trick: Jump and Trick: Switch. It can take an optional constant label.", "ジャンプアンカー", "作動式: ジャンプと作動式: スイッチの到達点になる、何もしない目印です。任意の定数ラベルを指定できます。");
        addSpellPiece(root, "trick_material_mutation", "Trick: Material Mutation", "Acts on a specific block, alters its material structure, and transmutes it into a different substance.", "作動式: 物質変成", "特定のブロックに作用して物質構造を改変し異なる物質に変成させる");
        addSpellPiece(root, "trick_physical_propulsion", "Trick: Physical Propulsion", "Raycasts from the specified position in the Ray direction; if it hits a Simulated Contraption, applies propulsion to it.", "作動式: 物理推進", "指定位置からRay方向へレイキャストし、Simulated Contraption に命中したならば推進力を与える");
        addSpellPiece(root, "operator_tan", "Operator: Tangent", "tan(A)", "演算子: タンジェント", "tan(A)");
        addSpellPiece(root, "operator_atan", "Operator: Arc Tangent", "atan(A)", "演算子: アークタンジェント", "atan(A)");
        addSpellPiece(root, "operator_sinh", "Operator: Hyperbolic Sine", "sinh(A)", "演算子: ハイパボリックサイン", "sinh(A)");
        addSpellPiece(root, "operator_cosh", "Operator: Hyperbolic Cosine", "cosh(A)", "演算子: ハイパボリックコサイン", "cosh(A)");
        addSpellPiece(root, "operator_tanh", "Operator: Hyperbolic Tangent", "tanh(A)", "演算子: ハイパボリックタンジェント", "tanh(A)");
        addSpellPiece(root, "operator_greater_than", "Operator: Greater Than", "Outputs 1 if Value 1 is greater than Value 2, otherwise 0. Number inputs are accepted.", "演算子: ～より大きい", "値1が値2より大きいなら1、そうでなければ0を出力します。Number を入力できます。");
        addSpellPiece(root, "operator_greater_than_or_equal", "Operator: Greater Than or Equal", "Outputs 1 if Value 1 is greater than or equal to Value 2, otherwise 0. Number inputs are accepted.", "演算子: ～以上", "値1が値2以上なら1、そうでなければ0を出力します。Number を入力できます。");
        addSpellPiece(root, "operator_equal", "Operator: Equal", "Outputs 1 if Value 1 equals Value 2, otherwise 0.", "演算子: 等しい", "値1と値2が等しいなら1、そうでなければ0を出力します。");
        addSpellPiece(root, "constant_string", "Constant: String", "Outputs the entered string. Limited to 1024 characters.", "定数子: 文字列", "入力した文字列をそのまま出力します。最大1024文字です。");
        addSpellPiece(root, "operator_format_string", "Operator: Format String", "Returns the string entered in the text window. Optional input values are embedded into {1}, {2}, and {3} in the text.", "演算子: フォーマット文字列", "テキストウィンドウで入力された文字列を返します。 任意の入力された値はテキスト中の{1}, {2}, {3}へ埋め込まれます。");
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
        addSpellPiece(root, "selector_block", "Selector: Block", "Returns the block at the given position as a Block value.", "取得子: ブロック", "指定座標にあるブロックを Block 型として返します。");
        addSpellPiece(root, "selector_block_list", "Selector: Block List", "Gets the blocks at the positions in a Vector List as a Block List.", "取得子: ブロックリスト", "Vector List の座標にあるブロックを Block List として取得します。");
        addSpellPiece(root, "operator_block_state", "Operator: Block State", "Outputs the saved block state of a Block value as a String. Example: minecraft:oak_stairs[facing=north]", "演算子: ブロックステート", "Block 値に保存されたブロックステートを文字列として出力します。例: minecraft:oak_stairs[facing=north]");
        addSpellPiece(root, "operator_block_state_value", "Operator: Block State Value", "Outputs one saved block state property value from a Block value.", "演算子: ブロックステート値", "Block 値に保存されたブロックステートから指定したプロパティ値を String として出力します。");
        addSpellPiece(root, "operator_block_state_entries", "Operator: Block State Entries", "Outputs the saved block state properties of a Block value as a String List.", "演算子: ブロックステート項目", "Block 値に保存されたブロックステートのプロパティ項目を String List として出力します。");
        addSpellPiece(root, "operator_tag_list", "Operator: Tag List", "Outputs the registry tags of the target Contextual Value (Entity, Item, Block, etc.) as a String List.", "演算子: タグリスト", "対象の Contextual Value(Entity、Item、Block など)のレジストリタグを String List として出力します。");
        addSpellPiece(root, "operator_block_position", "Operator: Block Position", "Outputs the saved position of a Block value as a plain Vector.", "演算子: ブロック座標", "Block 値に保存された座標を通常の Vector として出力します。");
        addSpellPiece(root, "operator_block_position_list", "Operator: Block Position List", "Gets the positions of the blocks in a Block List as a Vector List.", "演算子: ブロック位置リスト", "Block List のブロックの座標を Vector List として取得します。");
        addSpellPiece(root, "selector_online_players", "Selector: Online Players", "Outputs the names of online players in the world as a String List.", "取得子: オンラインプレイヤー", "ワールド内のオンラインプレイヤー名を String List として取得します。");
        addSpellPiece(root, "selector_held_item", "Selector: Main-Hand Item", "Gets the target entity's main-hand item.", "取得子: 手持ちアイテム", "対象エンティティのメインハンドのアイテムを取得します。");
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
        addSpellPiece(root, "operator_string_partial_match", "Operator: Partial Match", "Outputs 1 if searching String 1 with String 2 finds matching text, otherwise outputs 0. String 2 accepts wildcards: * matches any text, ? matches one character, and [abc] matches one listed character.", "演算子: 部分一致", "文字列1が文字列2での検索にマッチする文字列を含むなら1、そうでなければ0を出力します。文字列2ではワイルドカードを使えます: * は任意の文字列, ? は任意の1文字, [abc] は a/b/c のいずれか1文字に一致します。");
        addSpellPiece(root, "operator_string_starts_with", "Operator: String Starts With", "Outputs 1 if String 1 starts with String 2, otherwise outputs 0.", "演算子: 先頭一致", "文字列1が文字列2で始まるなら1、そうでなければ0を出力します。");
        addSpellPiece(root, "operator_string_ends_with", "Operator: String Ends With", "Outputs 1 if String 1 ends with String 2, otherwise outputs 0.", "演算子: 末尾一致", "文字列1が文字列2で終わるなら1、そうでなければ0を出力します。");
        addSpellPiece(root, "operator_string_concat", "Operator: String Concat", "String 1 + String 2 + String 3", "演算子: 文字列結合", "文字列1 + 文字列2 + 文字列3");
        addSpellPiece(root, "operator_string_split", "Operator: String Split", "Splits String 1 with String 2 as a literal delimiter and outputs a String List.", "演算子: 文字列分割", "文字列1を文字列2のリテラル区切り文字で分割し、String List を出力します。");
        addSpellPiece(root, "operator_string_slice", "Operator: String Slice", "Slices the input String from Number A to Number B and returns a new String.", "演算子: 文字列スライス", "入力した文字列の文字を 数値A:数値B でスライスした新しい 文字列 を返します。");
        addSpellPiece(root, "operator_string_length", "Operator: String Length", "Returns the number of characters in the input String as a Number.", "演算子: 文字列長", "入力した文字列の文字数を Number として返します。");
        addSpellPiece(root, "operator_string_replace", "Operator: String Replace", "Replaces parts of String 1 matching String 2 with String 3.", "演算子: 文字列置換", "入力した文字列1のうち、文字列2と一致する部分を、指定した文字列3へ置換する。");
        addSpellPiece(root, "operator_string_trim", "Operator: String Trim", "Removes leading and trailing whitespace from the input String.", "演算子: 文字列トリム", "入力した文字列の前後の空白を取り除きます。");
        addSpellPiece(root, "operator_string_list_join", "Operator: String List Join", "Joins the input String List with String 2 and outputs a String.", "演算子: 文字列リスト結合", "入力リストを文字列2で結合し、String を出力します。");
        addSpellPiece(root, "operator_player_name", "Operator: Player Name", "Outputs the player name if the Entity is a player, otherwise outputs an empty string.", "演算子: プレイヤーネーム", "Entityがプレイヤーならプレイヤー名を、そうでなければ空文字列を出力します。");
        addSpellPiece(root, "operator_list_search", "Operator: List Search", "Keeps only the input List elements whose string form matches the input search string.", "演算子: リスト検索", "リストの要素を文字列化したもののうち、入力した文字列で検索してマッチするもののみを残します。");
        addSpellPiece(root, "operator_list_search_exclude", "Operator: List Search Exclude", "Removes the input List elements whose string form matches the input search string.", "演算子: リスト検索除外", "リストの要素を文字列化したもののうち、入力した文字列で検索してマッチするものを除外します。");
        addSpellPiece(root, "operator_random_element", "Operator: Random Element", "Outputs one random element from the input List.", "演算子: ランダム要素", "入力した List からランダムに1要素を出力します。");
        addSpellPiece(root, "operator_list_add", "Operator: Add To List (PT)", "Adds up to three elements to the input List.", "リストへ追加 (PT)", "入力した List に最大3つの要素を追加します。");
        addSpellPiece(root, "operator_list_remove", "Operator: Remove From List (PT)", "Removes up to two elements from the input List.", "リストから削除 (PT)", "入力した List から最大2つの要素を削除します。");
        addSpellPiece(root, "operator_list_remove_indices", "Operator: Remove Indexed Elements", "Removes the elements at Number A and Number B from the input List.", "演算子: インデックス要素削除", "入力した List から数値Aと数値Bに対応するインデックスの要素を削除します。");
        addSpellPiece(root, "operator_list_insert", "Operator: List Insert", "Inserts the input element at the specified index in the input List.", "演算子: リストに挿入", "入力した List の指定インデックスへ要素を挿入します。");
        addSpellPiece(root, "operator_list_slice", "Operator: List Slice", "Slices the input List from Number A to Number B and returns a new List.", "演算子: リストスライス", "入力した List の要素を 数値A:数値B でスライスした新しい List を返します。");
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
        addSpellPiece(root, "operator_matrix_cuboid_region", "Operator: Matrix Cuboid Region", "Builds a 4x4 matrix for an axis-aligned cuboid from a center position and signed size. Whole-number sizes select that many blocks per axis.", "演算子: 直方体領域", "中心位置と符号付きサイズから軸平行直方体を表す 4×4 行列を生成します。整数サイズは各軸で含めるブロック数になります。");
        addSpellPiece(root, "operator_region_vector_list", "Operator: Region Vector List", "Returns block position vectors inside a matrix-defined region.", "演算子: 領域ベクトルリスト化", "行列で定義された領域内のブロック座標を Vector List として返します。");
        addSpellPiece(root, "operator_matrix_transform_vector", "Operator: Matrix Transform Vector", "Transforms a vector with a 3x3 or 4x4 matrix.", "演算子: 行列でベクトル変換", "3×3 または 4×4 行列でベクトルを変換します。");
        addSpellPiece(root, "operator_matrix_linear_part", "Operator: Matrix Linear Part", "Extracts the top-left 3x3 submatrix.", "演算子: 行列線形部分", "左上の 3×3 部分行列を取り出します。");
        addSpellPiece(root, "trick_mass_block_break", "Trick: Mass Block Break", "Breaks blocks at the coordinates in a Vector List. The maximum block count must be a positive integer constant.", "作動式: 大規模ブロック破壊", "Vector List の座標にあるブロックを破壊します。破壊数の上限には正の整数定数を入力する必要があります。");
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
        addBookPage(root, "trick_store_value", "Stores a Plain Value in CAD memory. The memory slot uses Psi's standard numbering: 1 is the first slot. Number, Vector, and String values overwrite each other in the same slot. String values longer than 128 characters are truncated when stored, and a warning is shown in the caster's chat.", "Plain ValueをCADメモリに保存します. メモリ番号はPsi標準と同じく1が最初のスロットです. Number, Vector, Stringは同じスロットで互いに上書きされます. 128文字を超えるStringは保存時に切り捨てられ, 術者のチャット欄に警告が表示されます.");
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
        addBookPage(root, "trick_jump", "Jumps forward to the next Jump Anchor with the same label when the target number is unset, or when its absolute value is less than 1. The label input is optional, but if used it must be a String constant. Empty labels only match empty-label anchors. With no target input, it acts as an unconditional forward jump. It cannot jump to Jump Anchors behind it.", "対象数値が未入力、または絶対値が1未満なら, 同じラベルを持つ次のジャンプアンカーまで前方にジャンプします. ラベル入力は任意ですが, 使う場合はString定数である必要があります. 空ラベルは空ラベルのアンカーにのみ一致します. 対象入力なしの場合は無条件の前方ジャンプとして動作します. 後方のジャンプアンカーには飛ぶことができません.");
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
        addBookPage(root, "operator_greater_than", "Compares Value 1 and Value 2 as Numbers. It outputs 1 when Value 1 is greater than Value 2, otherwise 0.", "値1と値2を Number として比較します. 値1が値2より大きいなら1, そうでなければ0を出力します.");
        addBookPage(root, "operator_greater_than_or_equal", "Compares Value 1 and Value 2 as Numbers. It outputs 1 when Value 1 is greater than or equal to Value 2, otherwise 0.", "値1と値2を Number として比較します. 値1が値2以上なら1, そうでなければ0を出力します.");
        addBookPage(root, "operator_equal", "Compares Value 1 and Value 2 as Any inputs. It outputs 1 when both values are equal, otherwise 0. Numbers compare by numeric value, Vectors compare by coordinates, Item values compare their item data and source, and Block values compare dimension, position, and block state. A Block compared with a Vector uses coordinate comparison.", "値1と値2を Any 入力として比較します. 2つの値が等しいなら1, そうでなければ0を出力します. Number は数値, Vector は座標, Item はアイテムデータと source, Block はディメンション, 座標, ブロック状態を比較します. Block と Vector の比較は座標比較です.");
        addBookPage(root, "constant_string", "Outputs the entered string as a String value. The value is saved with the spell and is limited to 1024 characters.", "入力した文字列をString値として出力します. 値は術式に保存され, 最大1024文字です.");
        addBookPage(root, "operator_format_string", "Outputs a String by replacing {1}, {2}, and {3} in the saved template with up to three optional Any inputs. The template uses the same input window as Constant: String and is limited to 1024 characters. Unconnected inputs become empty text. Contextual Values such as Entity, Item, and Block use the same localized display name as Selector: Display Name. Contextual Value Lists use the same localized names as Selector: Display Name List and are joined with commas. Other values use the normal String conversion. The final output is capped by the runtime String limit.", "保存したテンプレート中の {1}, {2}, {3} を最大3つの任意 Any 入力で置換して String を出力します. テンプレートは 定数子: 文字列 と同じ入力ウィンドウで編集し, 最大1024文字です. 未接続の入力は空文字になります. Entity, Item, Block などの Contextual Value は 取得子: 表示名 と同じ現在言語表示名を使います. Contextual Value List は 取得子: 表示名リスト と同じ現在言語表示名へ変換し, コンマ区切りで埋め込みます. それ以外の値は通常の String 変換を使います. 最終出力は実行時 String 上限で制限されます.");
        addBookPage(root, "operator_from_string", "Converts the String input for the selected Plain mode. String mode returns the input as-is. Number mode parses finite numeric text and outputs 0 on invalid input. Vector mode accepts Vector[x,y,z], Vector(x,y,z), [x,y,z], or (x,y,z), case-insensitive for Vector, and outputs the zero vector when parsing fails. Left-click the spell piece to select the mode.", "選択中の Plain モードに応じて String 入力を変換します. String モードは入力をそのまま返します. Number モードは有限の数値文字列を解析し, 無効な入力では0を出力します. Vector モードは Vector[x,y,z], Vector(x,y,z), [x,y,z], (x,y,z) を受け付けます. Vector は大文字小文字を区別せず, 解析失敗時はゼロベクトルを出力します. スペルピースを左クリックでモードを選択できます.");
        addBookPage(root, "operator_list_from_string_list", "Converts a String List for the selected Plain mode. String mode returns the input list as-is. Number mode parses each entry as finite numeric text, so invalid entries become 0. Vector mode converts only entries matching Vector[x,y,z], Vector(x,y,z), [x,y,z], or (x,y,z), case-insensitive for Vector, and skips invalid entries. Left-click the spell piece to select the mode.", "選択中の Plain モードに応じて String List を変換します. String モードは入力リストをそのまま返します. Number モードは各要素を有限の数値文字列として解析し, 無効な要素は0になります. Vector モードは Vector[x,y,z], Vector(x,y,z), [x,y,z], (x,y,z) に一致する要素のみ変換し, 無効な要素は無視します. Vector は大文字小文字を区別しません. スペルピースを左クリックでモードを選択できます.");
        addBookPage(root, "operator_number_list_to_vector", "Converts a Number List with exactly three elements into a Vector. The list order is X, Y, Z. If the list does not contain exactly three elements, the spell raises a runtime error.", "要素数3の Number List を Vector に変換します. リストの順序は X, Y, Z です. 要素数が3ではない場合は実行時エラーになります.");
        addBookPage(root, "operator_vector_to_number_list", "Converts a Vector into a Number List with three elements in X, Y, Z order.", "Vector を X, Y, Z の順で3要素の Number List に変換します.");
        addBookPage(root, "operator_to_string", "Converts an Any input into a String using the same display-oriented text form as Trick: Debug. Contextual values such as Entity, Item, and Block therefore use their debug display text rather than registry IDs. String values are returned as-is. List values convert each element by the same rules and join them with commas.", "Any 入力を 作動式: デバッグ と同じ表示向けの文字列表現で String に変換します. Entity, Item, Block などの Contextual Value もレジストリIDではなくデバッグ表示相当の文字列になります. String はそのまま返します. List は各要素を同じ規則で変換し, コンマ区切りで結合します.");
        addBookPage(root, "operator_list_to_string_list", "Converts an Any input into a String List using the same display-oriented text form as Trick: Debug. Contextual values such as Entity, Item, and Block therefore use their debug display text rather than registry IDs. List inputs, including Block List, convert each element by the same rules while preserving order. Non-list inputs become a one-element String List.", "Any 入力を 作動式: デバッグ と同じ表示向けの文字列表現で String List に変換します. Entity, Item, Block などの Contextual Value もレジストリIDではなくデバッグ表示相当の文字列になります. Block List を含む List は入力順を維持して各要素を同じ規則で変換します. List 以外は1要素の String List になります.");
        addBookPage(root, "operator_get_id", "Outputs the registry ID of a Contextual Value as a String. Entity inputs output the EntityType ID, Item inputs output the Item ID, and Block inputs output the Block ID. Unsupported contextual values output an empty String.", "Contextual Value のレジストリIDを String として出力します. Entity 入力は EntityType ID, Item 入力は Item ID, Block 入力は Block ID を出力します. 対応していない contextual value は空の String を出力します.");
        addBookPage(root, "operator_get_id_list", "Outputs registry IDs from a Contextual Value List as a String List. Entity List, Item List, and Block List preserve their input order. Vector List input is converted to Block List by reading the blocks at those world coordinates. Unsupported or empty elements become empty strings.", "Contextual Value List から各要素のレジストリIDを String List として出力します. Entity List, Item List, Block List は入力順を維持します. Vector List 入力はそのワールド座標のブロックを読む Block List として変換されます. 対応していない要素や空要素は空文字列になります.");
        addBookPage(root, "selector_display_name", "Outputs the display name of a Contextual Value as a String in the caster's current language. Players and custom-named entities keep their visible names; other entities use their EntityType name, Items use their hover name, and Blocks use their block name. If the caster language cannot be read, English is used. Missing translations fall back to English, then to the translation key itself.", "Contextual Value の表示名を, 術者の現在言語に応じた String として出力します. プレイヤーやカスタム名付き Entity は表示されている名前を返し, 通常の Entity は EntityType の名前, Item は hover name, Block はブロック名を返します. 術者の言語を取得できない場合は English を使います. 翻訳がない場合は English, それもなければ翻訳キーそのものを返します.");
        addBookPage(root, "selector_display_name_list", "Outputs display names from a Contextual Value List as a String List while preserving input order. Entity List, Item List, and Block List are supported. Vector List input is converted to Block List by reading the blocks at those world coordinates. Each element uses the same language fallback as Selector: Display Name.", "Contextual Value List から各要素の表示名を String List として出力し, 入力順を維持します. Entity List, Item List, Block List に対応します. Vector List 入力はそのワールド座標のブロックを読む Block List として変換されます. 各要素は 取得子: 表示名 と同じ言語 fallback を使います.");
        addBookPage(root, "selector_block", "Outputs the block at the target position as a Block value. Block is a read-only snapshot containing the saved position, dimension, block state, registry ID, and block tags. Spell pieces that require a Block can also accept a Vector through automatic conversion, so use this when you want to explicitly keep the value as a Block, such as when showing Block information with Trick: Debug.", "対象座標にあるブロックを Block 型として出力します. Block は保存座標, ディメンション, ブロック状態, レジストリID, ブロックタグを持つ読み取り用スナップショットです. Block 型を必要とするスペルピースにも Vector型 が自動変換されそのまま接続できますが, 作動式: デバッグ に Block としての情報を表示させたい場合など Block型 であることを明示的に示したい場合に用います.");
        addBookPage(root, "selector_block_list", "Gets the blocks at the positions in a Vector List as a Block List. Use this when you want to handle positions as blocks, such as when searching a list by block type.", "Vector List の座標にあるブロックを Block List として取得します. ブロックの種類でリストを検索したいなど, 座標をブロックとして扱いたい場合に使います.");
        addBookPage(root, "operator_block_state", "Outputs the block state saved in a Block value as a String, using Minecraft's command-style form such as minecraft:oak_stairs[facing=north,half=bottom]. The value comes from the Block snapshot and does not change if the world changes later.", "Block 値に保存されたブロックステートを String として出力します. 出力は minecraft:oak_stairs[facing=north,half=bottom] のような Minecraft のコマンド風表記です. 値は Block のスナップショットから取得され, 後からワールドが変化しても変わりません.");
        addBookPage(root, "operator_block_state_value", "Outputs one property value from the block state saved in a Block value. Use the String input to name a property such as facing or waterlogged. If the property does not exist, it outputs an empty String.", "Block 値に保存されたブロックステートから指定 property の値を1つ出力します. String 入力には facing や waterlogged などの property 名を指定します. property が存在しない場合は空の String を出力します.");
        addBookPage(root, "operator_block_state_entries", "Outputs the properties saved in a Block value's block state as a String List. Each entry uses property:value form, such as facing:north or waterlogged:false. Blocks with no state properties output an empty String List.", "Block 値に保存されたブロックステートのプロパティを String List として出力します. 各要素は facing:north や waterlogged:false のような property:value 形式です. state property を持たないブロックは空の String List を出力します.");
        addBookPage(root, "operator_tag_list", "Outputs registry tags from a Contextual Value as a String List. Entries use namespace:path form without #. Entity inputs return EntityType tags, not scoreboard tags.", "Contextual Value からレジストリタグを String List として出力します. 各要素は # を付けない namespace:path 形式です. Entity 入力は EntityType のタグを返し, スコアボードタグは含みません.");
        addBookPage(root, "operator_block_position", "Outputs the position saved in a Block as a plain Vector. A Block can usually be connected directly to spell pieces that require a Vector, but use this when a Vector value is explicitly needed, such as when checking coordinates with Trick: Debug.", "Block に保存された座標を通常の Vector として出力します. 基本的に Vector を必要とするスペルピースには Block 型をそのまま接続できますが、作動式: デバッグ に接続して座標を確認する際など Vector型 が明示的に必要な場合に使います.");
        addBookPage(root, "operator_block_position_list", "Gets the positions saved in a Block List as a Vector List.", "Block List に保存された座標を Vector List として取得します.");
        addBookPage(root, "selector_online_players", "Outputs the names of all online players in the caster's current world as a String List.", "術者の現在ワールドにいるオンラインプレイヤー全員の名前を String List として出力します.");
        addBookPage(root, "selector_held_item", "Outputs the target living entity's main-hand ItemStack as an Item value. Item is a read-only snapshot containing item data such as kind, count, durability, and name. Note that Psi's Selector: Nearby Items returns dropped item entities, which are different from the Item type added by PsiTweaks.", "対象 LivingEntity のメインハンドの ItemStack を Item 型として出力します. Item型は, アイテムの種類, 個数, 耐久値, 名前などを持つ読み取り用の値です. Psi の 取得子: 近くのアイテム で取得できるドロップアイテムはエンティティであり, PsiTweaksで追加されるItem型とは異なることに注意してください.");
        addBookPage(root, "selector_selected_slot_item", "Outputs the ItemStack in the slot selected by the spell as an Item value. This is the same slot used by Psi pieces such as Trick: Place Block, and is affected by Trick: Switch Target Slot. Empty slots output an empty Item.", "術式で選択しているスロットにある ItemStack を Item 型として出力します. スロットは Psi の 作動式: ブロック設置 などで扱うものと同じであり, 作動式: 指定スロット切替 の影響を受けます. 空スロットは空の Item を出力します.");
        addBookPage(root, "selector_entity_slot_item", "Outputs the Item in the target Entity's zero-based inventory slot. Players use their player inventory; other entities use their item handler when available. Unreadable, empty, negative, or out-of-range slots output an empty Item.", "対象 Entity の0始まりのインベントリスロットにある Item を出力します. Player はプレイヤーインベントリを使用し, その他の Entity は利用可能な ItemHandler を使用します. 読み取れないスロット, 空スロット, 負の番号, 範囲外の番号は空の Item を出力します.");
        addBookPage(root, "selector_internal_slot_item", "Outputs the Item in the target block's zero-based internal inventory slot. Item handlers are used first, with Container as a fallback. Blocks without a readable inventory and empty, negative, or out-of-range slots output an empty Item.", "対象ブロックの0始まりの内部インベントリスロットにある Item を出力します. ItemHandler を優先して使用し, 利用できない場合は Container を使用します. 読み取り可能なインベントリがないブロック, 空スロット, 負の番号, 範囲外の番号は空の Item を出力します.");
        addBookPage(root, "operator_item_count", "Outputs the stack count of an Item value as a Number. Empty Item values output 0.", "Item 値のスタック個数を Number として出力します. 空の Item は0を出力します.");
        addBookPage(root, "operator_item_slot", "Outputs the zero-based inventory slot number saved in an Item value. Items obtained from entity or block inventory slots retain this source slot. Items without an inventory-slot source, including empty Items, hand or equipment Items, and dropped item entities, output -1.", "Item 値に保存された0始まりのインベントリスロット番号を出力します. Entity またはブロックのインベントリスロットから取得した Item は取得元のスロット番号を保持します. 空の Item, 手や防具から取得した Item, ドロップアイテムなど, インベントリスロット由来でない Item は -1 を出力します.");
        addBookPage(root, "operator_item_total_count", "Outputs the total stack count from an Item List. If the String input is empty or not connected, all Items are counted. Otherwise only Items whose registry ID exactly matches the String are counted. Invalid IDs simply match nothing and output 0.", "Item List からスタック個数の合計を Number として出力します. String 入力が空または未接続ならすべての Item を数えます. それ以外の場合は String と registry ID が完全一致する Item だけを数えます. 無効な ID は何にも一致せず0を出力します.");
        addBookPage(root, "selector_held_items", "Outputs carried items from the target Entity as an Item List. Player inventories are read slot by slot; other entities use their item handler when available, or their hands and armor slots as a fallback. Empty inventories output an empty Item List.", "対象 Entity の所持アイテムを Item List として出力します. Player はインベントリをスロット単位で読み取り, その他の Entity は ItemHandler があればそれを使い, なければ手持ちと防具スロットを読み取ります. 空の場合は空の Item List を出力します.");
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
        addBookPage(root, "operator_list_search", "Filters the selected List mode by comparing the String input with each element's search text. Entity and contextual values such as Item and Block compare by registry ID. Other values use the same text form as Operator: To String. The String input accepts wildcards: * matches any text, ? matches one character, and [abc] matches one of the listed characters. Without wildcards, matching is exact.", "選択中の List モードを, 各要素の検索用文字列で絞り込みます. Entity と Item/Block などの Contextual Value はレジストリIDで比較します. それ以外の値は 演算子: 文字列へ変換 と同じ文字列で比較します. String入力ではワイルドカードを利用でき, * は任意の文字列, ? は任意の1文字, [abc] は a/b/c のいずれか1文字に一致します. ワイルドカードなしでは完全一致です.");
        addBookPage(root, "operator_list_search_exclude", "Filters the selected List mode by removing elements whose search text matches the input wildcard. Entity and contextual values such as Item and Block compare by registry ID. Other values use the same text form as Operator: To String. * matches any text, ? matches one character, and [abc] matches one of the listed characters. Without wildcards, matching is exact.", "選択中の List モードから, 検索用文字列が入力ワイルドカードに一致する要素を除外します. Entity と Item/Block などの Contextual Value はレジストリIDで比較します. それ以外の値は 演算子: 文字列へ変換 と同じ文字列で比較します. * は任意の文字列, ? は任意の1文字, [abc] は a/b/c のいずれか1文字に一致します. ワイルドカードなしでは完全一致です.");
        addBookPage(root, "operator_random_element", "Returns one random element from the input List.", "入力された List から要素を1つランダムに返します.");
        addBookPage(root, "operator_list_add", "Adds up to three input elements to the input List.", "入力された List に最大3つの入力要素を追加します.");
        addBookPage(root, "operator_list_remove", "Removes up to two input elements from the input List.", "入力された List から最大2つの入力要素を削除します.");
        addBookPage(root, "operator_list_remove_indices", "Removes the elements at Number A and Number B from the selected List mode. Negative indexes count from the end, so -1 removes the last element. Both indexes are resolved against the original List, and duplicate indexes remove only one element. An out-of-range index raises an out-of-bounds spell error.", "選択中の List モードから数値Aと数値Bに対応するインデックスの要素を削除します. 負数は末尾から数え, -1 は末尾要素を削除します. どちらのインデックスも元の List を基準に解決し, 同じインデックスの場合は1要素だけ削除します. 範囲外はインデックス範囲外エラーになります.");
        addBookPage(root, "operator_list_insert", "Inserts the input element before the specified index in the selected List mode. Index 0 inserts at the beginning and -1 inserts before the last element. Indexes above the List size insert at the end, while indexes below the negative range insert at the beginning.", "選択中の List モードの指定インデックス直前へ入力要素を挿入します. 0 は先頭へ, -1 は末尾要素の直前へ挿入します. List サイズより大きいインデックスは末尾へ, 負方向の範囲を超えたインデックスは先頭へ挿入します.");
        addBookPage(root, "operator_list_slice", "Returns a selected range of elements from the selected List mode as a new List. If A is omitted, the range starts at the beginning; if B is omitted, it continues to the end. Negative numbers count from the end, out-of-range values are clamped to the valid range, and an end position at or before the start returns an empty List. Decimal values are truncated toward zero.", "選択中の List モード に対しインデックスを指定して範囲を切り出して新しい List として返します. A を省略すると先頭から, B を省略すると末尾までが対象です. 負数は末尾から数え, 範囲外は有効範囲へ丸め, 終了位置が開始位置以前なら空の List を返します. 小数は0方向へ切り捨てます.");
        addBookPage(root, "operator_list_size", "Outputs the number of elements in any input List.", "入力された任意の List 入力に含まれる要素数を出力します.");
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
        addBookPage(root, "operator_matrix_cuboid_region", "Builds a 4x4 matrix that defines an axis-aligned cuboid centered at the input position. Whole-number size components are treated as signed block counts: odd counts are centered on the input position, positive even counts include one extra block in the positive direction, and negative even counts include one extra block in the negative direction. Non-integer components keep continuous edge lengths. Pass the matrix to Operator: Region Vector List to turn the region into block coordinates before using Trick: Mass Block Break.", "入力位置を中心とする軸平行直方体を表す 4×4 行列を生成します. 整数のサイズ成分は符号付きブロック数として扱われます. 奇数は入力位置を中心に対称, 正の偶数は正方向へ1ブロック多く, 負の偶数は負方向へ1ブロック多く含みます. 非整数の成分は連続的な辺の長さとして扱われます. 作動式: 大規模ブロック破壊で使う前に、演算子: 領域ベクトルリスト化へ渡して領域をブロック座標に変換します.");
        addBookPage(root, "operator_region_vector_list", "Returns all block position vectors inside a 3D parallelepiped defined by a 3x4 or 4x4 matrix. The first three columns are the edge vectors and the fourth column is the start point. Vectors are returned in fixed local u, v, and w order. The operator does not inspect the world, so air, unloaded chunks, and unbreakable blocks are not filtered. The returned Vector List can be passed to Trick: Mass Block Break. Regions above the shared internal limit raise an error.", "3×4 または 4×4 の行列で定義された3次元の平行六面体内部にあるすべてのブロック座標を Vector List として返します. 最初の3列は辺ベクトル、第4列は開始点です. Vector は局所u, v, w軸の固定順序で返されます. ワールド状態は確認しないため、空気、未読み込みチャンク、破壊不能ブロックも除外されません. 返した Vector List は作動式: 大規模ブロック破壊へ渡せます. 領域が共通の内部上限を超える場合はエラーになります.");
        addBookPage(root, "operator_matrix_transform_vector", "Transforms a vector with a 3x3 or 4x4 matrix. A 3x3 matrix multiplies [x,y,z]. A 4x4 matrix treats the vector as a homogeneous point [x,y,z,1] and divides the result by w.", "3×3 または 4×4 行列でベクトルを変換します. 3×3 行列では [x,y,z] の列ベクトルとして扱います. 4×4 行列では入力された Vector を列ベクトル [x,y,z,1] として行列との積を計算し、結果 [x',y',z',w'] の各成分を w' で除算した Vector を返します.");
        addBookPage(root, "operator_matrix_linear_part", "Extracts the top-left 3x3 submatrix from the input matrix. If the input is 3x3 or larger, the first 3 rows and 3 columns are returned as a new 3x3 matrix.", "入力された行列の左上 3×3 部分行列を取り出します. 入力が 3×3 以上であれば、最初の3行3列を新しい 3×3 行列として返します.");
        addBookPage(root, "trick_mass_block_break", "Breaks blocks at the coordinates in a Vector List. Vectors are treated as block coordinates, duplicate block positions are ignored after their first occurrence, and the remaining order is preserved. The maximum block count input must be a positive integer constant. Only positions that can be processed up to the configured maximum are range-checked and broken. Dropped items are merged and gathered at the caster's position. The processed positions must fit within the shared internal limit.", "Vector List の座標にあるブロックを破壊します. Vector はブロック座標として扱われ、重複したブロック座標は最初の1回だけ使われ、残りの順序は維持されます. 破壊数の上限入力は正の整数定数である必要があります. 設定した最大数まで実際に処理される座標だけが範囲チェックと破壊の対象になります. ドロップアイテムはまとめられて術者の位置に集められます. 処理対象は共通の内部上限内に収まる必要があります.");
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
