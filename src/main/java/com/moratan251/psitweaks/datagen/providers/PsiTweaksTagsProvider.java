package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public class PsiTweaksTagsProvider implements DataProvider {
    private final PackOutput.PathProvider blockPathProvider;
    private final PackOutput.PathProvider chemicalPathProvider;
    private final PackOutput.PathProvider damageTypePathProvider;
    private final PackOutput.PathProvider itemPathProvider;
    private final PackOutput.PathProvider poiTypePathProvider;

    public PsiTweaksTagsProvider(PackOutput output) {
        this.blockPathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "tags/block");
        this.chemicalPathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "tags/mekanism/chemical");
        this.damageTypePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "tags/damage_type");
        this.itemPathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "tags/item");
        this.poiTypePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "tags/point_of_interest_type");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Map<ResourceLocation, JsonObject> blockTags = new LinkedHashMap<>();
        Map<ResourceLocation, JsonObject> chemicalTags = new LinkedHashMap<>();
        Map<ResourceLocation, JsonObject> damageTypeTags = new LinkedHashMap<>();
        Map<ResourceLocation, JsonObject> itemTags = new LinkedHashMap<>();
        Map<ResourceLocation, JsonObject> poiTypeTags = new LinkedHashMap<>();
        List<CompletableFuture<?>> futures = new ArrayList<>();

        addBlockTags(blockTags);
        addChemicalTags(chemicalTags);
        addDamageTypeTags(damageTypeTags);
        addItemTags(itemTags);
        addPoiTypeTags(poiTypeTags);
        saveTags(futures, output, blockPathProvider, blockTags);
        saveTags(futures, output, chemicalPathProvider, chemicalTags);
        saveTags(futures, output, damageTypePathProvider, damageTypeTags);
        saveTags(futures, output, itemPathProvider, itemTags);
        saveTags(futures, output, poiTypePathProvider, poiTypeTags);

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "PsiTweaks tags";
    }

    private static void addBlockTags(Map<ResourceLocation, JsonObject> tags) {
        for (ProductiveBeesDataProvider.GeneratedBee bee : ProductiveBeesDataProvider.bees()) {
            tag(tags, "psitweaks", "productivebees/flowers/" + bee.id(), block(bee.flowerBlock()));
        }

        tag(tags, "minecraft", "mineable/pickaxe",
                block(PsitweaksBlocks.CAD_DISASSEMBLER.get()),
                block(PsitweaksMekanismBlocks.PROGRAM_RESEARCHER.get()),
                block(PsitweaksMekanismBlocks.SCULK_ERODER.get()),
                block(PsitweaksMekanismBlocks.MATERIAL_MUTATOR.get()),
                block(PsitweaksMekanismBlocks.PSIONIC_GENERATOR.get()),
                block(PsitweaksBlocks.ORE_ANTINITE.get()),
                block(PsitweaksBlocks.ANTINITE_BLOCK.get()),
                block(PsitweaksBlocks.CHAOTIC_PSIMETAL_BLOCK.get()),
                block(PsitweaksBlocks.FLASHMETAL_BLOCK.get()),
                block(PsitweaksBlocks.HEAVY_PSIMETAL_BLOCK.get()),
                block(PsitweaksBlocks.PLUTONIUM_BLOCK.get()),
                block(PsitweaksBlocks.POLONIUM_BLOCK.get()),
                block(PsitweaksBlocks.RAW_ANTINITE_BLOCK.get()),
                block(PsitweaksBlocks.SPELLMACHINERY_CASING.get()),
                block(PsitweaksBlocks.PSYCHEONIC_METAL_BLOCK.get()),
                block(PsitweaksBlocks.HYPOSTASIS_GEM_BLOCK.get()),
                block(PsitweaksBlocks.PSYCHEONIC_METAL_CRUX.get()));
        tag(tags, "minecraft", "needs_stone_tool", block(PsitweaksBlocks.PSYCHEONIC_METAL_CRUX.get()));
        tag(tags, "minecraft", "needs_diamond_tool",
                block(PsitweaksBlocks.HEAVY_PSIMETAL_BLOCK.get()),
                block(PsitweaksBlocks.PSYCHEONIC_METAL_BLOCK.get()),
                block(PsitweaksBlocks.HYPOSTASIS_GEM_BLOCK.get()));
        tag(tags, "minecraft", "needs_iron_tool",
                block(PsitweaksMekanismBlocks.PROGRAM_RESEARCHER.get()),
                block(PsitweaksMekanismBlocks.SCULK_ERODER.get()),
                block(PsitweaksMekanismBlocks.MATERIAL_MUTATOR.get()),
                block(PsitweaksMekanismBlocks.PSIONIC_GENERATOR.get()),
                block(PsitweaksBlocks.CHAOTIC_PSIMETAL_BLOCK.get()),
                block(PsitweaksBlocks.FLASHMETAL_BLOCK.get()),
                block(PsitweaksBlocks.PLUTONIUM_BLOCK.get()),
                block(PsitweaksBlocks.POLONIUM_BLOCK.get()),
                block(PsitweaksBlocks.SPELLMACHINERY_CASING.get()));
        tag(tags, "neoforge", "needs_netherite_tool",
                block(PsitweaksBlocks.ORE_ANTINITE.get()),
                block(PsitweaksBlocks.ANTINITE_BLOCK.get()),
                block(PsitweaksBlocks.RAW_ANTINITE_BLOCK.get()));

        tag(tags, "c", "ores", tagRef("c:ores/antinite"));
        tag(tags, "c", "ores/antinite", block(PsitweaksBlocks.ORE_ANTINITE.get()));
        tag(tags, "c", "storage_blocks",
                tagRef("c:storage_blocks/antinite"),
                tagRef("c:storage_blocks/chaotic_psimetal"),
                tagRef("c:storage_blocks/flashmetal"),
                tagRef("c:storage_blocks/heavy_psimetal"),
                tagRef("c:storage_blocks/psycheonic_metal"),
                tagRef("c:storage_blocks/hypostasis_gem"),
                tagRef("c:storage_blocks/plutonium"),
                tagRef("c:storage_blocks/polonium"),
                tagRef("c:storage_blocks/raw_antinite"));
        tag(tags, "c", "storage_blocks/antinite", block(PsitweaksBlocks.ANTINITE_BLOCK.get()));
        tag(tags, "c", "storage_blocks/chaotic_psimetal", block(PsitweaksBlocks.CHAOTIC_PSIMETAL_BLOCK.get()));
        tag(tags, "c", "storage_blocks/flashmetal", block(PsitweaksBlocks.FLASHMETAL_BLOCK.get()));
        tag(tags, "c", "storage_blocks/heavy_psimetal", block(PsitweaksBlocks.HEAVY_PSIMETAL_BLOCK.get()));
        tag(tags, "c", "storage_blocks/psycheonic_metal", block(PsitweaksBlocks.PSYCHEONIC_METAL_BLOCK.get()));
        tag(tags, "c", "storage_blocks/hypostasis_gem", block(PsitweaksBlocks.HYPOSTASIS_GEM_BLOCK.get()));
        tag(tags, "c", "storage_blocks/plutonium", block(PsitweaksBlocks.PLUTONIUM_BLOCK.get()));
        tag(tags, "c", "storage_blocks/polonium", block(PsitweaksBlocks.POLONIUM_BLOCK.get()));
        tag(tags, "c", "storage_blocks/raw_antinite", block(PsitweaksBlocks.RAW_ANTINITE_BLOCK.get()));
    }

    private static void addChemicalTags(Map<ResourceLocation, JsonObject> tags) {
        tag(tags, "mekanism", "gaseous",
                chemical("gas_psionic_echo"),
                chemical("gas_peo_fuel"));
    }

    private static void addDamageTypeTags(Map<ResourceLocation, JsonObject> tags) {
        String meteorLine = damageType("meteor_line");
        tag(tags, "minecraft", "bypasses_armor", meteorLine);
        tag(tags, "minecraft", "bypasses_cooldown", meteorLine);
        tag(tags, "minecraft", "bypasses_effects", meteorLine);
        tag(tags, "minecraft", "bypasses_enchantments", meteorLine);
        tag(tags, "minecraft", "bypasses_invulnerability", meteorLine);
        tag(tags, "minecraft", "bypasses_resistance", meteorLine);
        tag(tags, "minecraft", "bypasses_shield", meteorLine);
        tag(tags, "minecraft", "no_knockback", meteorLine);
    }

    private static void addItemTags(Map<ResourceLocation, JsonObject> tags) {
        tag(tags, "c", "alloys", item(PsitweaksItems.ALLOY_PSION), item(PsitweaksItems.ALLOY_PSIONIC_ECHO), item(PsitweaksItems.ALLOY_HYPOSTASIS));
        tag(tags, "c", "circuits", item(PsitweaksItems.PSIONIC_CONTROL_CIRCUIT), item(PsitweaksItems.ECHO_CONTROL_CIRCUIT), item(PsitweaksItems.HYPOSTASIS_CONTROL_CIRCUIT));
        tag(tags, "c", "dusts", tagRef("c:dusts/antinite"));
        tag(tags, "c", "dusts/antinite", item(PsitweaksItems.ANTINITE_DUST));
        tag(tags, "c", "ingots",
                tagRef("c:ingots/antinite"),
                tagRef("c:ingots/chaotic_psimetal"),
                tagRef("c:ingots/flashmetal"),
                tagRef("c:ingots/heavy_psimetal"),
                tagRef("c:ingots/psycheonic_metal"));
        tag(tags, "c", "ingots/antinite", item(PsitweaksItems.ANTINITE_INGOT));
        tag(tags, "c", "ingots/chaotic_psimetal", item(PsitweaksItems.CHAOTIC_PSIMETAL));
        tag(tags, "c", "ingots/flashmetal", item(PsitweaksItems.FLASHMETAL));
        tag(tags, "c", "ingots/heavy_psimetal", item(PsitweaksItems.HEAVY_PSIMETAL));
        tag(tags, "c", "ingots/psycheonic_metal", item(PsitweaksItems.PSYCHEONIC_METAL_INGOT));
        tag(tags, "c", "nuggets",
                tagRef("c:nuggets/psimetal"),
                tagRef("c:nuggets/ivory_psimetal"),
                tagRef("c:nuggets/ebony_psimetal"),
                tagRef("c:nuggets/chaotic_psimetal"),
                tagRef("c:nuggets/flashmetal"),
                tagRef("c:nuggets/heavy_psimetal"),
                tagRef("c:nuggets/antinite"),
                tagRef("c:nuggets/psycheonic_metal"));
        tag(tags, "c", "nuggets/psimetal", item(PsitweaksItems.PSIMETAL_NUGGET));
        tag(tags, "c", "nuggets/ivory_psimetal", item(PsitweaksItems.IVORY_PSIMETAL_NUGGET));
        tag(tags, "c", "nuggets/ebony_psimetal", item(PsitweaksItems.EBONY_PSIMETAL_NUGGET));
        tag(tags, "c", "nuggets/chaotic_psimetal", item(PsitweaksItems.CHAOTIC_PSIMETAL_NUGGET));
        tag(tags, "c", "nuggets/flashmetal", item(PsitweaksItems.FLASHMETAL_NUGGET));
        tag(tags, "c", "nuggets/heavy_psimetal", item(PsitweaksItems.HEAVY_PSIMETAL_NUGGET));
        tag(tags, "c", "nuggets/antinite", item(PsitweaksItems.ANTINITE_NUGGET));
        tag(tags, "c", "nuggets/psycheonic_metal", item(PsitweaksItems.PSYCHEONIC_METAL_NUGGET));
        tag(tags, "c", "ores", tagRef("c:ores/antinite"));
        tag(tags, "c", "ores/antinite", item(PsitweaksBlocks.ORE_ANTINITE));
        tag(tags, "c", "raw_materials", tagRef("c:raw_materials/antinite"));
        tag(tags, "c", "raw_materials/antinite", item(PsitweaksItems.RAW_ANTINITE));
        tag(tags, "c", "storage_blocks",
                tagRef("c:storage_blocks/antinite"),
                tagRef("c:storage_blocks/chaotic_psimetal"),
                tagRef("c:storage_blocks/flashmetal"),
                tagRef("c:storage_blocks/heavy_psimetal"),
                tagRef("c:storage_blocks/psycheonic_metal"),
                tagRef("c:storage_blocks/hypostasis_gem"),
                tagRef("c:storage_blocks/plutonium"),
                tagRef("c:storage_blocks/polonium"),
                tagRef("c:storage_blocks/raw_antinite"));
        tag(tags, "c", "storage_blocks/antinite", item(PsitweaksBlocks.ANTINITE_BLOCK));
        tag(tags, "c", "storage_blocks/chaotic_psimetal", item(PsitweaksBlocks.CHAOTIC_PSIMETAL_BLOCK));
        tag(tags, "c", "storage_blocks/flashmetal", item(PsitweaksBlocks.FLASHMETAL_BLOCK));
        tag(tags, "c", "storage_blocks/heavy_psimetal", item(PsitweaksBlocks.HEAVY_PSIMETAL_BLOCK));
        tag(tags, "c", "storage_blocks/psycheonic_metal", item(PsitweaksBlocks.PSYCHEONIC_METAL_BLOCK));
        tag(tags, "c", "storage_blocks/hypostasis_gem", item(PsitweaksBlocks.HYPOSTASIS_GEM_BLOCK));
        tag(tags, "c", "storage_blocks/plutonium", item(PsitweaksBlocks.PLUTONIUM_BLOCK));
        tag(tags, "c", "storage_blocks/polonium", item(PsitweaksBlocks.POLONIUM_BLOCK));
        tag(tags, "c", "storage_blocks/raw_antinite", item(PsitweaksBlocks.RAW_ANTINITE_BLOCK));

//        tag(tags, "minecraft", "dirt",
//                "minecraft:dirt",
//                "minecraft:coarse_dirt",
//                "minecraft:rooted_dirt",
//                "minecraft:grass_block",
//                "minecraft:podzol",
//                "minecraft:mycelium");
        tag(tags, "minecraft", "head_armor", item(PsitweaksItems.MOVAL_SUIT_HELMET));
        tag(tags, "minecraft", "chest_armor", item(PsitweaksItems.MOVAL_SUIT_CHESTPLATE));
        tag(tags, "minecraft", "leg_armor", item(PsitweaksItems.MOVAL_SUIT_LEGGINGS));
        tag(tags, "minecraft", "foot_armor", item(PsitweaksItems.MOVAL_SUIT_BOOTS));
        tag(tags, "minecraft", "enchantable/head_armor", item(PsitweaksItems.MOVAL_SUIT_HELMET));
        tag(tags, "minecraft", "enchantable/chest_armor", item(PsitweaksItems.MOVAL_SUIT_CHESTPLATE));
        tag(tags, "minecraft", "enchantable/leg_armor", item(PsitweaksItems.MOVAL_SUIT_LEGGINGS));
        tag(tags, "minecraft", "enchantable/foot_armor", item(PsitweaksItems.MOVAL_SUIT_BOOTS));
        tag(tags, "minecraft", "enchantable/bow", item(PsitweaksItems.PSIMETAL_BOW));
        tag(tags, "curios", "charm", item(PsitweaksItems.FLASH_CHARM));
        tag(tags, "curios", "magic_calculation_area",
                item(PsitweaksItems.AUTO_CASTER_TICK),
                item(PsitweaksItems.AUTO_CASTER_CUSTOM_TICK),
                item(PsitweaksItems.INTERFERENCE_RANGE_EXTENDER),
                item(PsitweaksItems.THIRD_EYE_DEVICE),
                item(PsitweaksItems.SORCERY_BOOSTER));

        tag(tags, "psi", "assemblies",
                item(PsitweaksItems.CAD_ASSEMBLY_ALLOY_PSION),
                item(PsitweaksItems.CAD_ASSEMBLY_CHAOTIC_PSIMETAL),
                item(PsitweaksItems.CAD_ASSEMBLY_FLASHMETAL),
                item(PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_ALPHA),
                item(PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_BETA),
                item(PsitweaksItems.CAD_ASSEMBLY_PSYCHEONIC_METAL));
        tag(tags, "psi", "components",
                item(PsitweaksItems.CAD_ASSEMBLY_ALLOY_PSION),
                item(PsitweaksItems.CAD_ASSEMBLY_CHAOTIC_PSIMETAL),
                item(PsitweaksItems.CAD_ASSEMBLY_FLASHMETAL),
                item(PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_ALPHA),
                item(PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_BETA),
                item(PsitweaksItems.CAD_ASSEMBLY_PSYCHEONIC_METAL));

        tag(tags, "mekanism", "alloys", item(PsitweaksItems.ALLOY_PSION), item(PsitweaksItems.ALLOY_PSIONIC_ECHO), item(PsitweaksItems.ALLOY_HYPOSTASIS));
        tag(tags, "mekanism", "clumps", tagRef("mekanism:clumps/antinite"));
        tag(tags, "mekanism", "clumps/antinite", item(PsitweaksItems.CLUMP_ANTINITE));
        tag(tags, "mekanism", "crystals", tagRef("mekanism:crystals/antinite"));
        tag(tags, "mekanism", "crystals/antinite", item(PsitweaksItems.CRYSTAL_ANTINITE));
        tag(tags, "mekanism", "dirty_dusts", tagRef("mekanism:dirty_dusts/antinite"));
        tag(tags, "mekanism", "dirty_dusts/antinite", item(PsitweaksItems.DIRTY_DUST_ANTINITE));
        tag(tags, "mekanism", "shards", tagRef("mekanism:shards/antinite"));
        tag(tags, "mekanism", "shards/antinite", item(PsitweaksItems.SHARD_ANTINITE));
    }

    private static void addPoiTypeTags(Map<ResourceLocation, JsonObject> tags) {
        tag(tags, "minecraft", "acquirable_job_site", "psitweaks:cad_assembler");
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

    private static String block(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).toString();
    }

    private static String item(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).toString();
    }

    private static String chemical(String path) {
        return "psitweaks:" + path;
    }

    private static String damageType(String path) {
        return "psitweaks:" + path;
    }

    private static String tagRef(String id) {
        return "#" + id;
    }
}
