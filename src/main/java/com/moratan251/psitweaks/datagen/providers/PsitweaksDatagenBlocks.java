package com.moratan251.psitweaks.datagen.providers;

import java.util.List;

final class PsitweaksDatagenBlocks {
    private static final List<GeneratedBlock> BLOCKS = List.of(
            block("cad_disassembler", "CAD Disassembler", "CAD分解台"),
            block("ore_antinite", "Antinite Ore", "アンティナイト鉱石", "antinite_ore"),
            block("antinite_block", "Antinite Block", "アンティナイトブロック"),
            block("chaotic_psimetal_block", "Chaotic Psimetal Block", "カオティックサイメタルブロック"),
            block("flashmetal_block", "Flashmetal Block", "フラッシュメタルブロック"),
            block("heavy_psimetal_block", "Heavy Psimetal Block", "ヘビーサイメタルブロック"),
            block("plutonium_block", "Plutonium Block", "プルトニウムブロック"),
            block("polonium_block", "Polonium Block", "ポロニウムブロック"),
            block("raw_antinite_block", "Raw Antinite Block", "アンティナイトの原石ブロック"),
            block("spellmachinery_casing", "Spellmachinery Casing", "魔導機構ケーシング")
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
        return new GeneratedBlock(id, enUs, jaJp, texture);
    }

    record GeneratedBlock(String id, String enUs, String jaJp, String texture) {
    }
}
