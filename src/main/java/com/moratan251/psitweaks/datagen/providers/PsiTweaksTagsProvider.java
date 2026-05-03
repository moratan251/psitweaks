package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public class PsiTweaksTagsProvider implements DataProvider {
    private final PackOutput.PathProvider blockPathProvider;
    private final PackOutput.PathProvider itemPathProvider;

    public PsiTweaksTagsProvider(PackOutput output) {
        this.blockPathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "tags/block");
        this.itemPathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "tags/item");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Map<ResourceLocation, JsonObject> blockTags = new LinkedHashMap<>();
        Map<ResourceLocation, JsonObject> itemTags = new LinkedHashMap<>();
        List<CompletableFuture<?>> futures = new ArrayList<>();

        addBlockTags(blockTags);
        addItemTags(itemTags);
        saveTags(futures, output, blockPathProvider, blockTags);
        saveTags(futures, output, itemPathProvider, itemTags);

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "PsiTweaks tags";
    }

    private static void addBlockTags(Map<ResourceLocation, JsonObject> tags) {
        tag(tags, "minecraft", "mineable/pickaxe",
                block("cad_disassembler"),
                block("ore_antinite"),
                block("antinite_block"),
                block("chaotic_psimetal_block"),
                block("flashmetal_block"),
                block("heavy_psimetal_block"),
                block("plutonium_block"),
                block("polonium_block"),
                block("raw_antinite_block"),
                block("spellmachinery_casing"));
        tag(tags, "minecraft", "needs_diamond_tool", block("heavy_psimetal_block"));
        tag(tags, "minecraft", "needs_iron_tool",
                block("chaotic_psimetal_block"),
                block("flashmetal_block"),
                block("plutonium_block"),
                block("polonium_block"),
                block("spellmachinery_casing"));
        tag(tags, "neoforge", "needs_netherite_tool",
                block("ore_antinite"),
                block("antinite_block"),
                block("raw_antinite_block"));

        tag(tags, "c", "ores", tagRef("c:ores/antinite"));
        tag(tags, "c", "ores/antinite", block("ore_antinite"));
        tag(tags, "c", "storage_blocks",
                tagRef("c:storage_blocks/antinite"),
                tagRef("c:storage_blocks/chaotic_psimetal"),
                tagRef("c:storage_blocks/flashmetal"),
                tagRef("c:storage_blocks/heavy_psimetal"),
                tagRef("c:storage_blocks/plutonium"),
                tagRef("c:storage_blocks/polonium"),
                tagRef("c:storage_blocks/raw_antinite"));
        tag(tags, "c", "storage_blocks/antinite", block("antinite_block"));
        tag(tags, "c", "storage_blocks/chaotic_psimetal", block("chaotic_psimetal_block"));
        tag(tags, "c", "storage_blocks/flashmetal", block("flashmetal_block"));
        tag(tags, "c", "storage_blocks/heavy_psimetal", block("heavy_psimetal_block"));
        tag(tags, "c", "storage_blocks/plutonium", block("plutonium_block"));
        tag(tags, "c", "storage_blocks/polonium", block("polonium_block"));
        tag(tags, "c", "storage_blocks/raw_antinite", block("raw_antinite_block"));
    }

    private static void addItemTags(Map<ResourceLocation, JsonObject> tags) {
        tag(tags, "c", "alloys", item("alloy_psion"), item("alloy_psionic_echo"), item("alloy_hypostasis"));
        tag(tags, "c", "circuits", item("psionic_control_circuit"), item("echo_control_circuit"), item("hypostasis_control_circuit"));
        tag(tags, "c", "dusts", tagRef("c:dusts/antinite"));
        tag(tags, "c", "dusts/antinite", item("antinite_dust"));
        tag(tags, "c", "ingots",
                tagRef("c:ingots/antinite"),
                tagRef("c:ingots/chaotic_psimetal"),
                tagRef("c:ingots/flashmetal"),
                tagRef("c:ingots/heavy_psimetal"),
                tagRef("c:ingots/psycheonic_metal"));
        tag(tags, "c", "ingots/antinite", item("antinite_ingot"));
        tag(tags, "c", "ingots/chaotic_psimetal", item("chaotic_psimetal"));
        tag(tags, "c", "ingots/flashmetal", item("flashmetal"));
        tag(tags, "c", "ingots/heavy_psimetal", item("heavy_psimetal"));
        tag(tags, "c", "ingots/psycheonic_metal", item("psycheonic_metal_ingot"));
        tag(tags, "c", "nuggets", tagRef("c:nuggets/psycheonic_metal"));
        tag(tags, "c", "nuggets/psycheonic_metal", item("psycheonic_metal_nugget"));
        tag(tags, "c", "ores", tagRef("c:ores/antinite"));
        tag(tags, "c", "ores/antinite", item("ore_antinite"));
        tag(tags, "c", "raw_materials", tagRef("c:raw_materials/antinite"));
        tag(tags, "c", "raw_materials/antinite", item("raw_antinite"));
        tag(tags, "c", "storage_blocks",
                tagRef("c:storage_blocks/antinite"),
                tagRef("c:storage_blocks/chaotic_psimetal"),
                tagRef("c:storage_blocks/flashmetal"),
                tagRef("c:storage_blocks/heavy_psimetal"),
                tagRef("c:storage_blocks/plutonium"),
                tagRef("c:storage_blocks/polonium"),
                tagRef("c:storage_blocks/raw_antinite"));
        tag(tags, "c", "storage_blocks/antinite", item("antinite_block"));
        tag(tags, "c", "storage_blocks/chaotic_psimetal", item("chaotic_psimetal_block"));
        tag(tags, "c", "storage_blocks/flashmetal", item("flashmetal_block"));
        tag(tags, "c", "storage_blocks/heavy_psimetal", item("heavy_psimetal_block"));
        tag(tags, "c", "storage_blocks/plutonium", item("plutonium_block"));
        tag(tags, "c", "storage_blocks/polonium", item("polonium_block"));
        tag(tags, "c", "storage_blocks/raw_antinite", item("raw_antinite_block"));

        tag(tags, "psi", "assemblies",
                item("cad_assembly_alloy_psion"),
                item("cad_assembly_chaotic_psimetal"),
                item("cad_assembly_flashmetal"),
                item("cad_assembly_heavy_psimetal_alpha"),
                item("cad_assembly_heavy_psimetal_beta"),
                item("cad_assembly_psycheonic_metal"));
        tag(tags, "psi", "components",
                item("cad_assembly_alloy_psion"),
                item("cad_assembly_chaotic_psimetal"),
                item("cad_assembly_flashmetal"),
                item("cad_assembly_heavy_psimetal_alpha"),
                item("cad_assembly_heavy_psimetal_beta"),
                item("cad_assembly_psycheonic_metal"));

        tag(tags, "mekanism", "alloys", item("alloy_psion"), item("alloy_psionic_echo"), item("alloy_hypostasis"));
        tag(tags, "mekanism", "clumps", tagRef("mekanism:clumps/antinite"));
        tag(tags, "mekanism", "clumps/antinite", item("clump_antinite"));
        tag(tags, "mekanism", "crystals", tagRef("mekanism:crystals/antinite"));
        tag(tags, "mekanism", "crystals/antinite", item("crystal_antinite"));
        tag(tags, "mekanism", "dirty_dusts", tagRef("mekanism:dirty_dusts/antinite"));
        tag(tags, "mekanism", "dirty_dusts/antinite", item("dirty_dust_antinite"));
        tag(tags, "mekanism", "shards", tagRef("mekanism:shards/antinite"));
        tag(tags, "mekanism", "shards/antinite", item("shard_antinite"));
    }

    private static void saveTags(List<CompletableFuture<?>> futures, CachedOutput output,
                                 PackOutput.PathProvider pathProvider, Map<ResourceLocation, JsonObject> tags) {
        tags.forEach((id, tag) -> futures.add(DataProvider.saveStable(output, tag, pathProvider.json(id))));
    }

    private static void tag(Map<ResourceLocation, JsonObject> tags, String namespace, String path, String... values) {
        JsonObject root = new JsonObject();
        JsonArray jsonValues = new JsonArray();
        for (String value : values) {
            jsonValues.add(value);
        }
        root.add("values", jsonValues);
        tags.put(ResourceLocation.fromNamespaceAndPath(namespace, path), root);
    }

    private static String block(String path) {
        return "psitweaks:" + path;
    }

    private static String item(String path) {
        return "psitweaks:" + path;
    }

    private static String tagRef(String id) {
        return "#" + id;
    }
}
