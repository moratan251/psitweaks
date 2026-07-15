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
            item("program_blank", "Blank Program", "空白のプログラム"),
            item("program_cocytus", "Program: Cocytus", "プログラム: コキュートス", "program_trick_cocytus"),
            item("program_time_accelerate", "Program: Time Accelerate", "プログラム: 時間加速", "program_trick_time_accelerate"),
            item("program_flight", "Program: Flight", "プログラム: 飛行", "program__trick_flight"),
            item("program_phonon_maser", "Program: Phonon Maser", "プログラム: フォノンメーザー", "program_trick_phonon_maser"),
            item("program_meteor_line", "Program: Meteor Line", "プログラム: 流星群", "program_trick_meteor_line"),
            item("program_supreme_infusion", "Program: Supreme Infusion", "プログラム: 超位注入", "program_trick_supreme_infusion"),
            item("program_molecular_divider", "Program: Molecular Divider", "プログラム: 分子ディバイダー", "program_trick_molecular_divider"),
            item("program_radiation_injection", "Program: Radiation Injection", "プログラム: 放射線注入", "program_trick_radiation_injection"),
            item("program_radiation_filter", "Program: Radiation Filter", "プログラム: 放射線フィルタ", "program_trick_radiation_filter"),
            item("program_cure_radiation", "Program: Cure Radiation", "プログラム: 放射線除去", "program_trick_cure_radiation"),
            item("program_guillotine", "Program: Guillotine", "プログラム: ギロチン", "program_trick_guillotine"),
            item("program_active_air_mine", "Program: Active Air Mine", "プログラム: 能動空中機雷", "program_trick_active_air_mine"),
            item("program_die_flex", "Program: Flexible Die", "プログラム: 柔軟停止", "program_trick_die_flex"),
            item("program_material_mutation", "Program: Material Mutation", "プログラム: 物質変成", "program_trick_material_mutation"),
            item("program_mass_block_break", "Program: Mass Block Break", "プログラム: 大規模ブロック破壊", "program_trick_mass_block_break"),
            item("philosophers_stone", "Philosopher's Stone", "賢者の石"),
            item("spell_magazine", "Spell Magazine", "スペルマガジン", "spell_magazine_huge"),
            item("portable_cad_assembler", "Portable CAD Assembler", "携帯型CAD組立機"),
            item("psimetal_bow", "Psimetal Bow", "サイメタルの弓"),
            item("curios_controller", "Curios Controller", "キュリオスコントローラ"),
            item("auto_caster_tick", "Auto Caster: tick", "術式自動詠唱デバイス: tick"),
            item("auto_caster_custom_tick", "Auto Caster: custom tick", "術式自動詠唱デバイス: カスタムtick"),
            item("flash_charm", "Flash Charm", "フラッシュチャーム"),
            item("interference_range_extender", "Interference Range Extender", "干渉距離延長デバイス"),
            item("third_eye_device", "Third-Eye Device", "サードアイデバイス"),
            item("sorcery_booster", "Sorcery Booster", "ソーサリーブースター"),
            item("moval_suit_helmet", "M.O.V.A.L. Suit Helmet", "M.O.V.A.L. スーツヘルメット"),
            item("moval_suit_chestplate", "M.O.V.A.L. Suit Chestplate", "M.O.V.A.L. スーツチェストプレート"),
            item("moval_suit_leggings", "M.O.V.A.L. Suit Leggings", "M.O.V.A.L. スーツレギンス"),
            item("moval_suit_boots", "M.O.V.A.L. Suit Boots", "M.O.V.A.L. スーツブーツ"),
            item("module_psyon_supplying_unit", "Psyon Supplying Unit", "サイオン供給ユニット"),
            item("module_psyon_capacity_unit", "Psyon Capacity Unit", "サイオン容量ユニット"),
            item("module_phenomenon_interference_enhancement_unit", "Phenomenon Interference Enhancement Unit", "事象干渉力増大ユニット"),
            activeItem("advanced_spell_bullet", "Advanced Spell Bullet", "改良術式弾"),
            activeItem("advanced_spell_bullet_loop", "Advanced Loopcast Spell Bullet", "ループ型改良術式弾"),
            activeItem("advanced_spell_bullet_mine", "Advanced Mine Spell Bullet", "地雷型改良術式弾"),
            activeItem("advanced_spell_bullet_charge", "Advanced Charge Spell Bullet", "チャージ型改良術式弾"),
            activeItem("advanced_spell_bullet_grenade", "Advanced Grenade Spell Bullet", "榴弾型改良術式弾"),
            activeItem("advanced_spell_bullet_projectile", "Advanced Projectile Spell Bullet", "発射型改良術式弾"),
            activeItem("advanced_spell_bullet_circle", "Advanced Circle Spell Bullet", "円形改良術式弾"),
            activeItem("resonant_spell_bullet", "Resonant Spell Bullet", "共鳴術式弾"),
            activeItem("resonant_spell_bullet_loop", "Resonant Loopcast Spell Bullet", "ループ型共鳴術式弾"),
            activeItem("resonant_spell_bullet_mine", "Resonant Mine Spell Bullet", "地雷型共鳴術式弾"),
            activeItem("resonant_spell_bullet_charge", "Resonant Charge Spell Bullet", "チャージ型共鳴術式弾"),
            activeItem("resonant_spell_bullet_grenade", "Resonant Grenade Spell Bullet", "榴弾型共鳴術式弾"),
            activeItem("resonant_spell_bullet_projectile", "Resonant Projectile Spell Bullet", "発射型共鳴術式弾"),
            activeItem("resonant_spell_bullet_circle", "Resonant Circle Spell Bullet", "円形共鳴術式弾"),
            activeItem("sublimated_spell_bullet", "Sublimated Spell Bullet", "昇華術式弾"),
            activeItem("sublimated_spell_bullet_loop", "Sublimated Loopcast Spell Bullet", "ループ型昇華術式弾"),
            activeItem("sublimated_spell_bullet_mine", "Sublimated Mine Spell Bullet", "地雷型昇華術式弾"),
            activeItem("sublimated_spell_bullet_charge", "Sublimated Charge Spell Bullet", "チャージ型昇華術式弾"),
            activeItem("sublimated_spell_bullet_grenade", "Sublimated Grenade Spell Bullet", "榴弾型昇華術式弾"),
            activeItem("sublimated_spell_bullet_projectile", "Sublimated Projectile Spell Bullet", "発射型昇華術式弾"),
            activeItem("sublimated_spell_bullet_circle", "Sublimated Circle Spell Bullet", "円形昇華術式弾"),
            activeItem("awakened_spell_bullet", "Awakened Spell Bullet", "覚醒術式弾"),
            activeItem("awakened_spell_bullet_loop", "Awakened Loopcast Spell Bullet", "ループ型覚醒術式弾"),
            activeItem("awakened_spell_bullet_mine", "Awakened Mine Spell Bullet", "地雷型覚醒術式弾"),
            activeItem("awakened_spell_bullet_charge", "Awakened Charge Spell Bullet", "チャージ型覚醒術式弾"),
            activeItem("awakened_spell_bullet_grenade", "Awakened Grenade Spell Bullet", "榴弾型覚醒術式弾"),
            activeItem("awakened_spell_bullet_projectile", "Awakened Projectile Spell Bullet", "発射型覚醒術式弾"),
            activeItem("awakened_spell_bullet_circle", "Awakened Circle Spell Bullet", "円形覚醒術式弾"),
            activeItem("transcendent_spell_bullet", "Transcendent Spell Bullet", "超越術式弾"),
            activeItem("transcendent_spell_bullet_loop", "Transcendent Loopcast Spell Bullet", "ループ型超越術式弾"),
            activeItem("transcendent_spell_bullet_mine", "Transcendent Mine Spell Bullet", "地雷型超越術式弾"),
            activeItem("transcendent_spell_bullet_charge", "Transcendent Charge Spell Bullet", "チャージ型超越術式弾"),
            activeItem("transcendent_spell_bullet_grenade", "Transcendent Grenade Spell Bullet", "榴弾型超越術式弾"),
            activeItem("transcendent_spell_bullet_projectile", "Transcendent Projectile Spell Bullet", "発射型超越術式弾"),
            activeItem("transcendent_spell_bullet_circle", "Transcendent Circle Spell Bullet", "円形超越術式弾"),
            item("cad_assembly_alloy_psion", "Psionic Alloy CAD Assembly", "サイオニック合金のCAD素体"),
            item("cad_assembly_chaotic_psimetal", "Chaotic Psimetal CAD Assembly", "カオティックサイメタルのCAD素体"),
            item("cad_assembly_flashmetal", "Flashmetal CAD Assembly", "フラッシュメタルのCAD素体"),
            item("cad_assembly_heavy_psimetal_alpha", "Heavy Psimetal CAD Assembly α", "ヘビーサイメタルのCAD素体 α型"),
            item("cad_assembly_heavy_psimetal_beta", "Heavy Psimetal CAD Assembly β", "ヘビーサイメタルのCAD素体 β型"),
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
        return new GeneratedItem(id, enUs, jaJp, "minecraft:item/generated", texture, false);
    }

    private static GeneratedItem activeItem(String id, String enUs, String jaJp) {
        return new GeneratedItem(id, enUs, jaJp, "minecraft:item/generated", id, true);
    }

    private static GeneratedItem modelItem(String id, String enUs, String jaJp, String parent) {
        return new GeneratedItem(id, enUs, jaJp, parent, null, false);
    }

    record GeneratedItem(String id, String enUs, String jaJp, String parent, String texture, boolean activeModel) {
    }
}
