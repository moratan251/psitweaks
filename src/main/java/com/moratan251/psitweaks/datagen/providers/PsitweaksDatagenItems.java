package com.moratan251.psitweaks.datagen.providers;

import java.util.List;

final class PsitweaksDatagenItems {
    private static final List<GeneratedItem> ITEMS = List.of(
            item("alloy_psion", "Psionic Alloy", "サイオニック合金"),
            item("alloy_psionic_echo", "Echo Alloy", "感応合金"),
            item("alloy_hypostasis", "Hypostasis Alloy", "位格合金"),
            item("psionic_echo", "Psionic Echo", "サイオニックエコー"),
            item("heavy_psimetal_scrap", "Heavy Psimetal Scrap", "ヘビーサイメタルの欠片"),
            item("psionic_control_circuit", "Psionic Control Circuit", "サイオニック制御回路"),
            item("echo_control_circuit", "Echo Control Circuit", "感応制御回路"),
            item("hypostasis_control_circuit", "Hypostasis Control Circuit", "位格制御回路"),
            item("enriched_psigem", "Enriched Psigem", "濃縮サイジェム"),
            item("enriched_ebony", "Enriched Ebony", "濃縮エボニー"),
            item("enriched_ivory", "Enriched Ivory", "濃縮アイボリー"),
            item("enriched_echo", "Enriched Echo", "濃縮エコー "),
            item("psionic_factor", "Psionic Factor", "サイオニック因子"),
            item("psionic_factor_ivory", "Ivory Psionic Factor", "偏陽サイオニック因子"),
            item("psionic_factor_ebony", "Ebony Psionic Factor", "偏陰サイオニック因子"),
            item("chaotic_factor", "Chaotic Factor", "カオティック因子"),
            item("chaotic_psimetal", "Chaotic Psimetal Ingot", "カオティックサイメタルインゴット"),
            item("unrefined_flashmetal", " Unrefined Flashmetal", "未精製フラッシュメタル"),
            item("flashmetal", "Flashmetal Ingot", "フラッシュメタルインゴット"),
            item("heavy_psimetal", "Heavy Psimetal Ingot", "ヘビーサイメタルインゴット"),
            item("raw_antinite", "Raw Antinite", "アンティナイトの原石"),
            item("shard_antinite", "Antinite Shard", "アンティナイトの欠片"),
            item("crystal_antinite", "Antinite Crystal", "アンティナイトの結晶"),
            item("clump_antinite", "Antinite Clump", "アンティナイトの凝塊"),
            item("dirty_dust_antinite", "Dirty Antinite Dust", "汚れたアンティナイトの粉"),
            item("antinite_dust", "Antinite Dust", "アンティナイトの粉", "dust_antinite"),
            item("antinite_ingot", "Antinite Ingot", "アンティナイトインゴット"),
            item("psycheonic_metal_ingot", "Psycheonic Metal Ingot", "プシオニックメタルインゴット", "psycheonic_metal"),
            item("psycheonic_metal_nugget", "Psycheonic Metal Nugget", "プシオニックメタルナゲット", "psycheonic_nugget"),
            item("enriched_hypostasis", "Enriched Hypostasis Gem", "濃縮ヒュポスタシスジェム"),
            item("jade", "Jade", "翡翠"),
            item("magatama", "Magatama", "勾玉"),
            item("echo_pellet", "HDΨE Pellet", "HDΨEペレット"),
            item("pellet_neptunium", "Neptunium Pellet", "ネプツニウムペレット"),
            item("pellet_americium", "Americium Pellet", "アメリシウムペレット"),
            item("hypostasis_gem", "Hypostasis Gem", "ヒュポスタシスジェム"),
            item("echo_sheet", "HDΨE Sheet", "HDΨEシート"),
            item("magicians_brain", "Magician's Brain", "魔法師の脳"),
            item("cad_assembly_alloy_psion", "Psionic Alloy CAD Assembly", "サイオニック合金のCAD素体"),
            item("cad_assembly_chaotic_psimetal", "Chaotic Psimetal CAD Assembly", "カオティックサイメタルのCAD素体"),
            item("cad_assembly_flashmetal", "Flashmetal CAD Assembly", "フラッシュメタルのCAD素体"),
            item("cad_assembly_heavy_psimetal_alpha", "Heavy Psimetal CAD Assembly Alpha", "ヘビーサイメタルのCAD素体 α型"),
            item("cad_assembly_heavy_psimetal_beta", "Heavy Psimetal CAD Assembly Beta", "ヘビーサイメタルのCAD素体 β型"),
            item("cad_assembly_psycheonic_metal", "Psycheonic Metal CAD Assembly", "プシオニックメタルのCAD素体", "cad_assembly_psycheonicmetal"),
            item("incomplete_heavy_psimetal_assembly", "Incomplete Heavy Psimetal CAD Assembly", "未完成ヘビーサイメタルCAD素体"),
            modelItem("inline_caster", "Inline Caster", "インラインキャスター", "psi:item/cad_inline_1"),
            modelItem("secondary_caster", "Secondary Caster", "セカンダリキャスター", "psi:item/cad_inline_2"),
            modelItem("parallel_caster", "Parallel Caster", "パラレルキャスター", "psi:item/cad_inline_3")
    );

    private PsitweaksDatagenItems() {
    }

    static List<GeneratedItem> items() {
        return ITEMS;
    }

    private static GeneratedItem item(String id, String enUs, String jaJp) {
        return item(id, enUs, jaJp, id);
    }

    private static GeneratedItem item(String id, String enUs, String jaJp, String texture) {
        return new GeneratedItem(id, enUs, jaJp, "minecraft:item/generated", texture);
    }

    private static GeneratedItem modelItem(String id, String enUs, String jaJp, String parent) {
        return new GeneratedItem(id, enUs, jaJp, parent, null);
    }

    record GeneratedItem(String id, String enUs, String jaJp, String parent, String texture) {
    }
}
