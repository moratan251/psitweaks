package com.moratan251.psitweaks.datagen.providers;

import java.util.List;

final class PsitweaksDatagenBlocks {
    private static final List<GeneratedBlock> BLOCKS = List.of(
            block("cad_disassembler", "CAD Disassembler", "CAD分解台"),
            machineSingleTexture("program_researcher", "Program Research Table", "プログラム研究台"),
            machine("sculk_eroder", "Sculk Eroder", "スカルク侵食機", "sculk_eroder"),
            machine("material_mutator", "Material Mutator", "物質変成機", "material_mutator"),
            machine("psionic_generator", "Psi-Link Generator", "サイリンク発電機", "psi_link_generator"),
            block("transcendent_universal_cable", "Transcendent Universal Cable", "超越ユニバーサルケーブル"),
            block("transcendent_energy_cube", "Transcendent Energy Cube", "超越エネルギーキューブ"),
            block("ore_antinite", "Antinite Ore", "アンティナイト鉱石", "antinite_ore"),
            block("antinite_block", "Antinite Block", "アンティナイトブロック"),
            block("chaotic_psimetal_block", "Chaotic Psimetal Block", "カオティックサイメタルブロック"),
            block("flashmetal_block", "Flashmetal Block", "フラッシュメタルブロック"),
            block("heavy_psimetal_block", "Heavy Psimetal Block", "ヘビーサイメタルブロック"),
            block("plutonium_block", "Plutonium Block", "プルトニウムブロック"),
            block("polonium_block", "Polonium Block", "ポロニウムブロック"),
            block("raw_antinite_block", "Raw Antinite Block", "アンティナイトの原石ブロック"),
            block("spellmachinery_casing", "Spellmachinery Casing", "魔導機構ケーシング"),
            block("psycheonic_metal_block", "Psycheonic Metal Block", "プシオニックメタルブロック", "psycheonicmetal_block"),
            block("hypostasis_gem_block", "Hypostasis Gem Block", "ヒュポスタシスジェムブロック", "hypostasis_gem_block"),
            block("psycheonic_metal_crux", "Psycheonic Metal Crux", "プシオニックメタルの核", "pycheonicmetal_crux")
    );

    private PsitweaksDatagenBlocks() {
    }

    static List<GeneratedBlock> blocks() {
        return BLOCKS;
    }

    private static GeneratedBlock block(String id, String enUs, String jaJp) {
        return block(id, enUs, jaJp, id);
    }

    private static GeneratedBlock block(String id, String enUs, String jaJp, String texture) {
        return new GeneratedBlock(id, enUs, jaJp, texture, false, false);
    }

    private static GeneratedBlock machine(String id, String enUs, String jaJp, String texture) {
        return new GeneratedBlock(id, enUs, jaJp, texture, true, true);
    }

    private static GeneratedBlock machineSingleTexture(String id, String enUs, String jaJp) {
        return new GeneratedBlock(id, enUs, jaJp, id, true, false);
    }

    record GeneratedBlock(String id, String enUs, String jaJp, String texture, boolean machine, boolean directionalTextures) {
    }
}
