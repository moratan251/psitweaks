package com.moratan251.psitweaks.datagen.providers;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public final class PsiTweaksTagsProvider {
    private PsiTweaksTagsProvider() {
    }

    public static final class Blocks extends BlockTagsProvider {
        public Blocks(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper fileHelper) {
            super(output, lookupProvider, Psitweaks.MOD_ID, fileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            add(blockTag("forge", "needs_netherite_tool"), entries(
                    "psitweaks:ore_antinite",
                    "psitweaks:antinite_block",
                    "psitweaks:raw_antinite_block"
            ));
            add(blockTag("forge", "ores"), entries("#forge:ores/antinite"));
            add(blockTag("forge", "ores/antinite"), entries("psitweaks:ore_antinite"));
            add(blockTag("forge", "storage_blocks"), entries(
                    "#forge:storage_blocks/antinite",
                    "#forge:storage_blocks/chaotic_psimetal",
                    "#forge:storage_blocks/flashmetal",
                    "#forge:storage_blocks/heavy_psimetal",
                    "#forge:storage_blocks/plutonium",
                    "#forge:storage_blocks/polonium",
                    "#forge:storage_blocks/raw_antinite"
            ));
            add(blockTag("forge", "storage_blocks/antinite"), entries("psitweaks:antinite_block"));
            add(blockTag("forge", "storage_blocks/chaotic_psimetal"), entries("psitweaks:chaotic_psimetal_block"));
            add(blockTag("forge", "storage_blocks/flashmetal"), entries("psitweaks:flashmetal_block"));
            add(blockTag("forge", "storage_blocks/heavy_psimetal"), entries("psitweaks:heavy_psimetal_block"));
            add(blockTag("forge", "storage_blocks/plutonium"), entries("psitweaks:plutonium_block"));
            add(blockTag("forge", "storage_blocks/polonium"), entries("psitweaks:polonium_block"));
            add(blockTag("forge", "storage_blocks/psimetal"), entries("psi:psimetal_block"));
            add(blockTag("forge", "storage_blocks/raw_antinite"), entries("psitweaks:raw_antinite_block"));

            add(BlockTags.MINEABLE_WITH_PICKAXE, entries(
                    "psi:cad_assembler",
                    "psi:psimetal_block",
                    "psi:ebony_psimetal_block",
                    "psi:ivory_psimetal_block",
                    "psi:psigem_block",
                    "psi:psidust_block",
                    "psi:psimetal_block",
                    "psi:black_psimetal_plate",
                    "psi:lit_black_psimetal_plate",
                    "psi:white_psimetal_plate",
                    "psi:lit_white_psimetal_plate",
                    "psi:programmer",
                    "psitweaks:cad_disassembler",
                    "psitweaks:program_researcher",
                    "psitweaks:ore_antinite",
                    "psitweaks:antinite_block",
                    "psitweaks:chaotic_psimetal_block",
                    "psitweaks:flashmetal_block",
                    "psitweaks:heavy_psimetal_block",
                    "psitweaks:plutonium_block",
                    "psitweaks:polonium_block",
                    "psitweaks:raw_antinite_block",
                    "psitweaks:spellmachinery_casing",
                    "psitweaks:sculk_eroder",
                    "psitweaks:material_mutator",
                    "psitweaks:psionic_generator"
            ));
            add(BlockTags.NEEDS_DIAMOND_TOOL, entries("psitweaks:heavy_psimetal_block"));
            add(BlockTags.NEEDS_IRON_TOOL, entries(
                    "psitweaks:chaotic_psimetal_block",
                    "psitweaks:flashmetal_block",
                    "psitweaks:plutonium_block",
                    "psitweaks:polonium_block",
                    "psitweaks:spellmachinery_casing"
            ));
        }

        private void add(TagKey<Block> tag, String[] values) {
            TagsProvider.TagAppender<Block> appender = tag(tag);
            addEntries(appender, Registries.BLOCK, values);
        }
    }

    public static final class Items extends ItemTagsProvider {
        public Items(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                     CompletableFuture<TagsProvider.TagLookup<Block>> blockTags, ExistingFileHelper fileHelper) {
            super(output, lookupProvider, blockTags, Psitweaks.MOD_ID, fileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            add(itemTag("curios", "charm"), entries("psitweaks:flash_charm"));
            add(itemTag("curios", "magic_calculation_area"), entries(
                    "psitweaks:auto_caster_tick",
                    "psitweaks:auto_caster_second",
                    "psitweaks:auto_caster_custom_tick",
                    "psitweaks:third_eye_device",
                    "psitweaks:sorcery_booster"
            ));
            add(itemTag("forge", "armors/boots"), entries("psitweaks:moval_suit_boots"));
            add(itemTag("forge", "armors/chestplates"), entries("psitweaks:moval_suit_chestplate"));
            add(itemTag("forge", "armors/helmets"), entries("psitweaks:moval_suit_helmet"));
            add(itemTag("forge", "armors/leggings"), entries(
                    "psitweaks:moval_suit_leggings",
                    "psitweaks:moval_suit_leggings_ivory"
            ));
            add(itemTag("forge", "circuits"), entries(
                    "psitweaks:psionic_control_circuit",
                    "psitweaks:echo_control_circuit",
                    "psitweaks:hypostasis_control_circuit"
            ));
            add(itemTag("forge", "dusts"), entries("#forge:dusts/antinite"));
            add(itemTag("forge", "dusts/antinite"), entries("psitweaks:antinite_dust"));
            add(itemTag("forge", "ingots"), entries(
                    "#forge:ingots/antinite",
                    "#forge:ingots/psycheonic_metal",
                    "#forge:ingots/chaotic_psimetal",
                    "#forge:ingots/flashmetal",
                    "#forge:ingots/heavy_psimetal"
            ));
            add(itemTag("forge", "ingots/antinite"), entries("psitweaks:antinite_ingot"));
            add(itemTag("forge", "ingots/chaotic_psimetal"), entries("psitweaks:chaotic_psimetal"));
            add(itemTag("forge", "ingots/flashmetal"), entries("psitweaks:flashmetal"));
            add(itemTag("forge", "ingots/heavy_psimetal"), entries("psitweaks:heavy_psimetal"));
            add(itemTag("forge", "ingots/psycheonic_metal"), entries("psitweaks:psycheonic_metal_ingot"));
            add(itemTag("forge", "nuggets/psycheonic_metal"), entries("psitweaks:psycheonic_metal_nugget"));
            add(itemTag("forge", "ores"), entries("#forge:ores/antinite"));
            add(itemTag("forge", "ores/antinite"), entries("psitweaks:ore_antinite"));
            add(itemTag("forge", "raw_materials"), entries("#forge:raw_materials/antinite"));
            add(itemTag("forge", "raw_materials/antinite"), entries("psitweaks:raw_antinite"));
            add(itemTag("forge", "storage_blocks"), entries(
                    "#forge:storage_blocks/antinite",
                    "#forge:storage_blocks/chaotic_psimetal",
                    "#forge:storage_blocks/flashmetal",
                    "#forge:storage_blocks/heavy_psimetal",
                    "#forge:storage_blocks/plutonium",
                    "#forge:storage_blocks/polonium",
                    "#forge:storage_blocks/raw_antinite"
            ));
            add(itemTag("forge", "storage_blocks/antinite"), entries("psitweaks:antinite_block"));
            add(itemTag("forge", "storage_blocks/chaotic_psimetal"), entries("psitweaks:chaotic_psimetal_block"));
            add(itemTag("forge", "storage_blocks/flashmetal"), entries("psitweaks:flashmetal_block"));
            add(itemTag("forge", "storage_blocks/heavy_psimetal"), entries("psitweaks:heavy_psimetal_block"));
            add(itemTag("forge", "storage_blocks/plutonium"), entries("psitweaks:plutonium_block"));
            add(itemTag("forge", "storage_blocks/polonium"), entries("psitweaks:polonium_block"));
            add(itemTag("forge", "storage_blocks/psimetal"), entries("psi:psimetal_block"));
            add(itemTag("forge", "storage_blocks/raw_antinite"), entries("psitweaks:raw_antinite_block"));
            add(itemTag("forge", "tools/bows"), entries("psitweaks:psimetal_bow"));
            add(itemTag("mekanism", "alloys"), entries(
                    "psitweaks:alloy_psion",
                    "psitweaks:alloy_psionic_echo",
                    "psitweaks:alloy_hypostasis"
            ));
            add(itemTag("mekanism", "clumps"), entries("#mekanism:clumps/antinite"));
            add(itemTag("mekanism", "clumps/antinite"), entries("psitweaks:clump_antinite"));
            add(itemTag("mekanism", "crystals"), entries("#mekanism:crystals/antinite"));
            add(itemTag("mekanism", "crystals/antinite"), entries("psitweaks:crystal_antinite"));
            add(itemTag("mekanism", "dirty_dusts"), entries("#mekanism:dirty_dusts/antinite"));
            add(itemTag("mekanism", "dirty_dusts/antinite"), entries("psitweaks:dirty_dust_antinite"));
            add(itemTag("mekanism", "shards"), entries("#mekanism:shards/antinite"));
            add(itemTag("mekanism", "shards/antinite"), entries("psitweaks:shard_antinite"));
            add(itemTag("psi", "assemblies"), entries(
                    "psitweaks:cad_assembly_alloy_psion",
                    "psitweaks:cad_assembly_chaotic_psimetal",
                    "psitweaks:cad_assembly_flashmetal",
                    "psitweaks:cad_assembly_heavy_psimetal_alpha",
                    "psitweaks:cad_assembly_heavy_psimetal_beta",
                    "psitweaks:cad_assembly_psycheonic_metal"
            ));
            add(itemTag("psi", "components"), entries(
                    "psitweaks:cad_assembly_alloy_psion",
                    "psitweaks:cad_assembly_chaotic_psimetal",
                    "psitweaks:cad_assembly_flashmetal",
                    "psitweaks:cad_assembly_heavy_psimetal_alpha",
                    "psitweaks:cad_assembly_heavy_psimetal_beta",
                    "psitweaks:cad_assembly_psycheonic_metal"
            ));
        }

        private void add(TagKey<Item> tag, String[] values) {
            TagsProvider.TagAppender<Item> appender = tag(tag);
            addEntries(appender, Registries.ITEM, values);
        }
    }

    public static final class Fluids extends FluidTagsProvider {
        public Fluids(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper fileHelper) {
            super(output, lookupProvider, Psitweaks.MOD_ID, fileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            addOptional(fluidTag("forge", "molten_antinite"), "psitweaks:molten_antinite");
            addOptional(fluidTag("forge", "molten_chaotic_psimetal"), "psitweaks:molten_chaotic_psimetal");
            addOptional(fluidTag("forge", "molten_ebony_psimetal"), "psitweaks:molten_ebony_psimetal");
            addOptional(fluidTag("forge", "molten_flashmetal"), "psitweaks:molten_flashmetal");
            addOptional(fluidTag("forge", "molten_heavy_psimetal"), "psitweaks:molten_heavy_psimetal");
            addOptional(fluidTag("forge", "molten_ivory_psimetal"), "psitweaks:molten_ivory_psimetal");
            addOptional(fluidTag("forge", "molten_psigem"), "psitweaks:molten_psigem");
            addOptional(fluidTag("forge", "molten_psimetal"), "psitweaks:molten_psimetal");
            addOptional(fluidTag("forge", "molten_psionic_echo"), "psitweaks:molten_psionic_echo");
            addOptional(fluidTag("forge", "molten_psycheonic_metal"), "psitweaks:molten_psycheonic_metal");
        }

        private void addOptional(TagKey<Fluid> tag, String value) {
            tag(tag).addOptional(location(value));
        }
    }

    public static final class Biomes extends BiomeTagsProvider {
        public Biomes(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper fileHelper) {
            super(output, lookupProvider, Psitweaks.MOD_ID, fileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            add(biomeTag("psitweaks", "has_antinite_ore"), entries(
                    "minecraft:end_highlands",
                    "minecraft:end_midlands",
                    "minecraft:small_end_islands",
                    "minecraft:end_barrens"
            ));
        }

        private void add(TagKey<Biome> tag, String[] values) {
            TagsProvider.TagAppender<Biome> appender = tag(tag);
            addEntries(appender, Registries.BIOME, values);
        }
    }

    public static final class PoiTypes extends PoiTypeTagsProvider {
        public PoiTypes(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper fileHelper) {
            super(output, lookupProvider, Psitweaks.MOD_ID, fileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            add(poiTag("minecraft", "acquirable_job_site"), entries("psitweaks:cad_assembler"));
        }

        private void add(TagKey<PoiType> tag, String[] values) {
            TagsProvider.TagAppender<PoiType> appender = tag(tag);
            addEntries(appender, Registries.POINT_OF_INTEREST_TYPE, values);
        }
    }

    private static <T> void addEntries(TagsProvider.TagAppender<T> appender, ResourceKey<? extends net.minecraft.core.Registry<T>> registry, String[] values) {
        for (String value : values) {
            if (value.startsWith("#")) {
                appender.addTag(TagKey.create(registry, location(value.substring(1))));
            } else {
                appender.add(ResourceKey.create(registry, location(value)));
            }
        }
    }

    private static String[] entries(String... values) {
        return values;
    }

    private static ResourceLocation location(String value) {
        String[] parts = value.split(":", 2);
        return ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
    }

    private static TagKey<Block> blockTag(String namespace, String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    private static TagKey<Item> itemTag(String namespace, String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    private static TagKey<Fluid> fluidTag(String namespace, String path) {
        return TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    private static TagKey<Biome> biomeTag(String namespace, String path) {
        return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    private static TagKey<PoiType> poiTag(String namespace, String path) {
        return TagKey.create(Registries.POINT_OF_INTEREST_TYPE, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

}

