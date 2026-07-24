package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.items.PsitweaksMekanismItems;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import vazkii.psi.common.item.base.ModItems;

public class PsiTweaksRecipeProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public PsiTweaksRecipeProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Map<ResourceLocation, JsonObject> recipes = new LinkedHashMap<>();

        recipe(recipes, "psionic_control_circuit", shaped(
                List.of("AUA"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.ALLOY_PSION),
                        'U', ingredientItem(MekanismItems.ULTIMATE_CONTROL_CIRCUIT)
                ),
                PsitweaksItems.PSIONIC_CONTROL_CIRCUIT, 1));
        recipe(recipes, "echo_control_circuit", shaped(
                List.of("SSS", "ACA", "SSS"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.ALLOY_PSIONIC_ECHO),
                        'C', ingredientItem(PsitweaksItems.PSIONIC_CONTROL_CIRCUIT),
                        'S', ingredientItem(PsitweaksItems.ECHO_SHEET)
                ),
                PsitweaksItems.ECHO_CONTROL_CIRCUIT, 1));
        recipe(recipes, "hypostasis_control_circuit", shaped(
                List.of("SSS", "ACA", "SSS"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.ALLOY_HYPOSTASIS),
                        'C', ingredientItem(PsitweaksItems.ECHO_CONTROL_CIRCUIT),
                        'S', ingredientItem(PsitweaksItems.ECHO_SHEET)
                ),
                PsitweaksItems.HYPOSTASIS_CONTROL_CIRCUIT, 1));
        recipe(recipes, "magatama", shaped(
                List.of("JJJ", "JHJ", "JJJ"),
                Map.of(
                        'H', ingredientItem(PsitweaksItems.HYPOSTASIS_GEM),
                        'J', ingredientItem(PsitweaksItems.JADE)
                ),
                PsitweaksItems.MAGATAMA, 1));
        recipe(recipes, "psionic_echo", trickCrafting(
                ingredientItem(Items.ECHO_SHARD),
                PsitweaksItems.PSIONIC_ECHO,
                "psitweaks:trick_supreme_infusion",
                ModItems.cadAssemblyPsimetal.get()));
        recipe(recipes, "incomplete_heavy_psimetal_assembly", shaped(
                List.of("III", "  I"),
                Map.of('I', ingredientItem(PsitweaksItems.HEAVY_PSIMETAL)),
                PsitweaksItems.INCOMPLETE_HEAVY_PSIMETAL_ASSEMBLY, 1));
        recipe(recipes, "cad_assembly_alloy_psion", shaped(
                List.of("AAA", "  A"),
                Map.of('A', ingredientItem(PsitweaksItems.ALLOY_PSION)),
                PsitweaksItems.CAD_ASSEMBLY_ALLOY_PSION, 1));
        recipe(recipes, "cad_assembly_chaotic_psimetal", shaped("equipment",
                List.of("AAA", " BA"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.CHAOTIC_PSIMETAL),
                        'B', ingredientItem(PsitweaksItems.PSIONIC_CONTROL_CIRCUIT)
                ),
                PsitweaksItems.CAD_ASSEMBLY_CHAOTIC_PSIMETAL, 1));
        recipe(recipes, "cad_assembly_flashmetal", shaped("equipment",
                List.of("HHH", "FFF", " CF"),
                Map.of(
                        'C', ingredientItem(PsitweaksItems.PSIONIC_CONTROL_CIRCUIT),
                        'F', ingredientItem(PsitweaksItems.FLASHMETAL),
                        'H', ingredientItem(MekanismItems.HDPE_SHEET)
                ),
                PsitweaksItems.CAD_ASSEMBLY_FLASHMETAL, 1));
        recipe(recipes, "cad_assembly_heavy_psimetal_alpha", shaped("equipment",
                List.of("SNS", "CIC", "SCS"),
                Map.of(
                        'C', ingredientItem(PsitweaksItems.ECHO_CONTROL_CIRCUIT),
                        'I', ingredientItem(PsitweaksItems.INCOMPLETE_HEAVY_PSIMETAL_ASSEMBLY),
                        'N', ingredientItem(Items.NETHER_STAR),
                        'S', ingredientItem(PsitweaksItems.ECHO_SHEET)
                ),
                PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_ALPHA, 1));
        recipe(recipes, "cad_assembly_heavy_psimetal_beta", shaped("equipment",
                List.of("SCS", "PIP", "SCS"),
                Map.of(
                        'C', ingredientItem(PsitweaksItems.ECHO_CONTROL_CIRCUIT),
                        'I', ingredientItem(PsitweaksItems.INCOMPLETE_HEAVY_PSIMETAL_ASSEMBLY),
                        'P', ingredientItem(MekanismItems.PLUTONIUM_PELLET),
                        'S', ingredientItem(PsitweaksItems.ECHO_SHEET)
                ),
                PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_BETA, 1));
        recipe(recipes, "cad_assembly_psycheonic_metal_from_alpha", shaped("equipment",
                List.of("PCP", "AHA", "PCP"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.PELLET_AMERICIUM),
                        'C', ingredientItem(PsitweaksItems.HYPOSTASIS_CONTROL_CIRCUIT),
                        'H', ingredientItem(PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_ALPHA),
                        'P', ingredientItem(PsitweaksItems.PSYCHEONIC_METAL_INGOT)
                ),
                PsitweaksItems.CAD_ASSEMBLY_PSYCHEONIC_METAL, 1));
        recipe(recipes, "cad_assembly_psycheonic_metal_from_beta", shaped("equipment",
                List.of("PCP", "AHA", "PCP"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.PELLET_AMERICIUM),
                        'C', ingredientItem(PsitweaksItems.HYPOSTASIS_CONTROL_CIRCUIT),
                        'H', ingredientItem(PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_BETA),
                        'P', ingredientItem(PsitweaksItems.PSYCHEONIC_METAL_INGOT)
                ),
                PsitweaksItems.CAD_ASSEMBLY_PSYCHEONIC_METAL, 1));
        recipe(recipes, "unrefined_flashmetal", shaped(
                List.of("BBB", "AAA", "BBB"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.CHAOTIC_PSIMETAL),
                        'B', ingredientItem(MekanismItems.REFINED_GLOWSTONE_INGOT)
                ),
                PsitweaksItems.UNREFINED_FLASHMETAL, 1));
        recipe(recipes, "heavy_psimetal", shapeless(
                List.of(
                        ingredientItem(PsitweaksItems.HEAVY_PSIMETAL_SCRAP),
                        ingredientItem(PsitweaksItems.HEAVY_PSIMETAL_SCRAP),
                        ingredientItem(PsitweaksItems.HEAVY_PSIMETAL_SCRAP),
                        ingredientItem(PsitweaksItems.HEAVY_PSIMETAL_SCRAP),
                        ingredientItem(PsitweaksItems.FLASHMETAL),
                        ingredientItem(PsitweaksItems.FLASHMETAL),
                        ingredientItem(PsitweaksItems.FLASHMETAL),
                        ingredientItem(PsitweaksItems.FLASHMETAL)
                ),
                PsitweaksItems.HEAVY_PSIMETAL, 1));
        recipe(recipes, "psycheonic_metal_ingot", shaped(
                List.of("NNN", "NNN", "NNN"),
                Map.of('N', ingredientItem(PsitweaksItems.PSYCHEONIC_METAL_NUGGET)),
                PsitweaksItems.PSYCHEONIC_METAL_INGOT, 1));
        recipe(recipes, "psycheonic_metal_nugget_from_ingot", shapeless(
                List.of(ingredientItem(PsitweaksItems.PSYCHEONIC_METAL_INGOT)),
                PsitweaksItems.PSYCHEONIC_METAL_NUGGET, 9));
        addNuggetRecipes(recipes, "psimetal", PsitweaksItems.PSIMETAL_NUGGET, ModItems.psimetal.get());
        addNuggetRecipes(recipes, "ivory_psimetal", PsitweaksItems.IVORY_PSIMETAL_NUGGET, ModItems.ivoryPsimetal.get());
        addNuggetRecipes(recipes, "ebony_psimetal", PsitweaksItems.EBONY_PSIMETAL_NUGGET, ModItems.ebonyPsimetal.get());
        addNuggetRecipes(recipes, "chaotic_psimetal", PsitweaksItems.CHAOTIC_PSIMETAL_NUGGET, PsitweaksItems.CHAOTIC_PSIMETAL);
        addNuggetRecipes(recipes, "flashmetal", PsitweaksItems.FLASHMETAL_NUGGET, PsitweaksItems.FLASHMETAL);
        addNuggetRecipes(recipes, "heavy_psimetal", PsitweaksItems.HEAVY_PSIMETAL_NUGGET, PsitweaksItems.HEAVY_PSIMETAL);
        addNuggetRecipes(recipes, "antinite", PsitweaksItems.ANTINITE_NUGGET, PsitweaksItems.ANTINITE_INGOT);
        recipe(recipes, "amethyst_shard_from_block", shapeless(
                List.of(ingredientItem(Blocks.AMETHYST_BLOCK)),
                Items.AMETHYST_SHARD, 4));
        recipe(recipes, "antinite_ingot_smelting", cooking(
                "minecraft:smelting",
                ingredientItem(PsitweaksItems.RAW_ANTINITE),
                PsitweaksItems.ANTINITE_INGOT, 1.2D, 200));
        recipe(recipes, "antinite_ingot_blasting", cooking(
                "minecraft:blasting",
                ingredientItem(PsitweaksItems.RAW_ANTINITE),
                PsitweaksItems.ANTINITE_INGOT, 1.2D, 100));
        recipe(recipes, "antinite_ingot_from_dust_smelting", cooking(
                "minecraft:smelting",
                ingredientItem(PsitweaksItems.ANTINITE_DUST),
                PsitweaksItems.ANTINITE_INGOT, 0.3D, 200));
        recipe(recipes, "antinite_ingot_from_dust_blasting", cooking(
                "minecraft:blasting",
                ingredientItem(PsitweaksItems.ANTINITE_DUST),
                PsitweaksItems.ANTINITE_INGOT, 0.3D, 100));
        recipe(recipes, "cad_disassembler", shaped(
                List.of("PPP", "PAP", "PPP"),
                Map.of(
                        'P', ingredientItem(ModItems.psimetal.get()),
                        'A', ingredientItem(Blocks.ANVIL)
                ),
                PsitweaksBlocks.CAD_DISASSEMBLER.get(), 1));
        recipe(recipes, "program_researcher", shaped(
                List.of("PCP", "CGC", "PCP"),
                Map.of(
                        'P', ingredientItem(ModItems.psimetal.get()),
                        'C', ingredientItem(PsitweaksItems.PSIONIC_CONTROL_CIRCUIT),
                        'G', ingredientItem(ModItems.psigem.get())
                ),
                PsitweaksMekanismBlocks.PROGRAM_RESEARCHER.get(), 1));
        recipe(recipes, "material_mutator", shaped(
                List.of("APA", "CDC", "ABA"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.ALLOY_HYPOSTASIS),
                        'B', ingredientItem(PsitweaksItems.HYPOSTASIS_CONTROL_CIRCUIT),
                        'C', ingredientItem(PsitweaksItems.ANTINITE_INGOT),
                        'D', ingredientItem(PsitweaksBlocks.SPELLMACHINERY_CASING),
                        'P', ingredientItem(PsitweaksItems.PROGRAM_MATERIAL_MUTATION)
                ),
                PsitweaksMekanismBlocks.MATERIAL_MUTATOR.get(), 1));
        recipe(recipes, "sculk_eroder", shaped(
                List.of("ABA", "CDC", "ABA"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.ALLOY_PSIONIC_ECHO),
                        'B', ingredientItem(PsitweaksItems.ECHO_CONTROL_CIRCUIT),
                        'C', ingredientItem(Blocks.SCULK_CATALYST),
                        'D', ingredientItem(MekanismBlocks.STEEL_CASING)
                ),
                PsitweaksMekanismBlocks.SCULK_ERODER.get(), 1));
        recipe(recipes, "psionic_generator", shaped(
                List.of(" B ", "ACA", "ATA"),
                Map.of(
                        'A', ingredientItem(ModItems.psimetal.get()),
                        'B', ingredientItem(Items.ENDER_PEARL),
                        'C', ingredientItem(MekanismItems.BASIC_CONTROL_CIRCUIT),
                        'T', ingredientItem(MekanismItems.ENERGY_TABLET)
                ),
                PsitweaksMekanismBlocks.PSIONIC_GENERATOR.get(), 1));
        recipe(recipes, "transcendent_universal_cable", shaped("redstone",
                List.of("APA", "PNP", "APA"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.ALLOY_HYPOSTASIS),
                        'N', ingredientItem(PsitweaksItems.PELLET_NEPTUNIUM),
                        'P', ingredientItem(MekanismBlocks.ULTIMATE_UNIVERSAL_CABLE)
                ),
                PsitweaksMekanismBlocks.TRANSCENDENT_CABLE.get(), 4));
        recipe(recipes, "transcendent_energy_cube", shaped("redstone",
                List.of("AEA", "NCN", "AEA"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.ALLOY_HYPOSTASIS),
                        'C', ingredientItem(MekanismBlocks.ULTIMATE_ENERGY_CUBE),
                        'E', ingredientItem(MekanismItems.ENERGY_TABLET),
                        'N', ingredientItem(PsitweaksItems.PELLET_NEPTUNIUM)
                ),
                PsitweaksMekanismBlocks.TRANSCENDENT_ENERGY_CUBE.get(), 1));
        recipe(recipes, "program_blank", shaped(
                List.of("D", "P", "M"),
                Map.of(
                        'D', ingredientItem(ModItems.psidust.get()),
                        'P', ingredientItem(Items.PAPER),
                        'M', ingredientItem(ModItems.psimetal.get())
                ),
                PsitweaksItems.PROGRAM_BLANK, 1));
        recipe(recipes, "psimetal_bow", shaped("combat",
                List.of(" MS", "G S", " MS"),
                Map.of(
                        'M', ingredientItem(ModItems.psimetal.get()),
                        'G', ingredientItem(ModItems.psigem.get()),
                        'S', ingredientItem(Items.STRING)
                ),
                PsitweaksItems.PSIMETAL_BOW, 1));
        recipe(recipes, "auto_caster_tick", shaped("combat",
                List.of("ECE", "EFE", "EEE"),
                Map.of(
                        'E', ingredientItem(ModItems.ebonyPsimetal.get()),
                        'F', ingredientItem(PsitweaksItems.CHAOTIC_FACTOR),
                        'C', ingredientItem(Items.CLOCK)
                ),
                PsitweaksItems.AUTO_CASTER_TICK, 1));
        recipe(recipes, "auto_caster_custom_tick", shaped("combat",
                List.of("ECE", "EFE", "EEE"),
                Map.of(
                        'E', ingredientItem(PsitweaksItems.CHAOTIC_PSIMETAL),
                        'F', ingredientItem(PsitweaksItems.CHAOTIC_FACTOR),
                        'C', ingredientItem(Items.CLOCK)
                ),
                PsitweaksItems.AUTO_CASTER_CUSTOM_TICK, 1));
        recipe(recipes, "moval_suit_helmet", shaped("equipment",
                List.of("EHE", "E E"),
                Map.of(
                        'E', ingredientItem(ModItems.ebonyPsimetal.get()),
                        'H', ingredientItem(PsitweaksItems.HEAVY_PSIMETAL)
                ),
                PsitweaksItems.MOVAL_SUIT_HELMET, 1));
        recipe(recipes, "moval_suit_chestplate", shaped("equipment",
                List.of("E E", "EHE", "EEE"),
                Map.of(
                        'E', ingredientItem(ModItems.ebonyPsimetal.get()),
                        'H', ingredientItem(PsitweaksItems.HEAVY_PSIMETAL)
                ),
                PsitweaksItems.MOVAL_SUIT_CHESTPLATE, 1));
        recipe(recipes, "moval_suit_leggings", shaped("equipment",
                List.of("EHE", "E E", "E E"),
                Map.of(
                        'E', ingredientItem(ModItems.ebonyPsimetal.get()),
                        'H', ingredientItem(PsitweaksItems.HEAVY_PSIMETAL)
                ),
                PsitweaksItems.MOVAL_SUIT_LEGGINGS, 1));
        recipe(recipes, "moval_suit_boots", shaped("equipment",
                List.of("E E", "EHE"),
                Map.of(
                        'E', ingredientItem(ModItems.ebonyPsimetal.get()),
                        'H', ingredientItem(PsitweaksItems.HEAVY_PSIMETAL)
                ),
                PsitweaksItems.MOVAL_SUIT_BOOTS, 1));
        recipe(recipes, "module_psyon_supplying_unit", shaped("equipment",
                List.of("AEA", "AMA", "SSS"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.ALLOY_PSION),
                        'E', ingredientItem(PsitweaksItems.CHAOTIC_FACTOR),
                        'M', ingredientItem(MekanismItems.MODULE_BASE),
                        'S', ingredientItem(PsitweaksItems.ECHO_SHEET)
                ),
                PsitweaksMekanismItems.MODULE_PSYON_SUPPLYING, 1));
        recipe(recipes, "module_psyon_capacity_unit", shaped("equipment",
                List.of("AEA", "AMA", "SSS"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.ALLOY_PSION),
                        'E', ingredientItem(PsitweaksItems.ANTINITE_INGOT),
                        'M', ingredientItem(MekanismItems.MODULE_BASE),
                        'S', ingredientItem(PsitweaksItems.ECHO_SHEET)
                ),
                PsitweaksMekanismItems.MODULE_PSYON_CAPACITY, 1));
        recipe(recipes, "module_phenomenon_interference_enhancement_unit", shaped("equipment",
                List.of("AEA", "AMA", "NNN"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.ALLOY_HYPOSTASIS),
                        'E', ingredientItem(PsitweaksItems.MAGICIANS_BRAIN),
                        'M', ingredientItem(MekanismItems.MODULE_BASE),
                        'N', ingredientItem(PsitweaksItems.PELLET_NEPTUNIUM)
                ),
                PsitweaksMekanismItems.MODULE_PHENOMENON_INTERFERENCE_ENHANCEMENT, 1));
        recipe(recipes, "inline_caster", shaped("equipment",
                List.of(" G ", "PBP", " P "),
                Map.of(
                        'B', ingredientItem(ModItems.cadAssemblyPsimetal.get()),
                        'G', ingredientItem(ModItems.psigem.get()),
                        'P', ingredientItem(ModItems.psimetal.get())
                ),
                PsitweaksItems.INLINE_CASTER, 1));
        recipe(recipes, "secondary_caster", shaped("equipment",
                List.of(" F ", "CBC", " T "),
                Map.of(
                        'B', ingredientItem(ModItems.cadAssemblyPsimetal.get()),
                        'C', ingredientItem(PsitweaksItems.PSIONIC_CONTROL_CIRCUIT),
                        'F', ingredientItem(PsitweaksItems.FLASHMETAL),
                        'T', ingredientItem(ModItems.cadSocketSignaling.get())
                ),
                PsitweaksItems.SECONDARY_CASTER, 1));
        recipe(recipes, "parallel_caster", shaped("equipment",
                List.of("FHF", "CBC", "FTF"),
                Map.of(
                        'B', ingredientItem(ModItems.cadAssemblyPsimetal.get()),
                        'C', ingredientItem(PsitweaksItems.ECHO_CONTROL_CIRCUIT),
                        'F', ingredientItem(PsitweaksItems.FLASHMETAL),
                        'H', ingredientItem(PsitweaksItems.HEAVY_PSIMETAL),
                        'T', ingredientItem(ModItems.cadSocketTransmissive.get())
                ),
                PsitweaksItems.PARALLEL_CASTER, 1));
        recipe(recipes, "antinite_block", shaped("building",
                List.of("III", "III", "III"),
                Map.of('I', ingredientItem(PsitweaksItems.ANTINITE_INGOT)),
                PsitweaksBlocks.ANTINITE_BLOCK.get(), 1));
        recipe(recipes, "antinite_ingot_from_block", shapeless(
                List.of(ingredientItem(PsitweaksBlocks.ANTINITE_BLOCK)),
                PsitweaksItems.ANTINITE_INGOT, 9));
        recipe(recipes, "chaotic_psimetal_block", shaped("building",
                List.of("III", "III", "III"),
                Map.of('I', ingredientItem(PsitweaksItems.CHAOTIC_PSIMETAL)),
                PsitweaksBlocks.CHAOTIC_PSIMETAL_BLOCK.get(), 1));
        recipe(recipes, "chaotic_psimetal_from_block", shapeless(
                List.of(ingredientItem(PsitweaksBlocks.CHAOTIC_PSIMETAL_BLOCK)),
                PsitweaksItems.CHAOTIC_PSIMETAL, 9));
        recipe(recipes, "flashmetal_block", shaped("building",
                List.of("III", "III", "III"),
                Map.of('I', ingredientItem(PsitweaksItems.FLASHMETAL)),
                PsitweaksBlocks.FLASHMETAL_BLOCK.get(), 1));
        recipe(recipes, "flashmetal_from_block", shapeless(
                List.of(ingredientItem(PsitweaksBlocks.FLASHMETAL_BLOCK)),
                PsitweaksItems.FLASHMETAL, 9));
        recipe(recipes, "heavy_psimetal_block", shaped("building",
                List.of("III", "III", "III"),
                Map.of('I', ingredientItem(PsitweaksItems.HEAVY_PSIMETAL)),
                PsitweaksBlocks.HEAVY_PSIMETAL_BLOCK.get(), 1));
        recipe(recipes, "heavy_psimetal_from_block", shapeless(
                List.of(ingredientItem(PsitweaksBlocks.HEAVY_PSIMETAL_BLOCK)),
                PsitweaksItems.HEAVY_PSIMETAL, 9));
        recipe(recipes, "psycheonic_metal_block", shaped("building",
                List.of("III", "III", "III"),
                Map.of('I', ingredientItem(PsitweaksItems.PSYCHEONIC_METAL_INGOT)),
                PsitweaksBlocks.PSYCHEONIC_METAL_BLOCK.get(), 1));
        recipe(recipes, "psycheonic_metal_ingot_from_block", shapeless(
                List.of(ingredientItem(PsitweaksBlocks.PSYCHEONIC_METAL_BLOCK)),
                PsitweaksItems.PSYCHEONIC_METAL_INGOT, 9));
        recipe(recipes, "hypostasis_gem_block", shaped("building",
                List.of("GGG", "GGG", "GGG"),
                Map.of('G', ingredientItem(PsitweaksItems.HYPOSTASIS_GEM)),
                PsitweaksBlocks.HYPOSTASIS_GEM_BLOCK.get(), 1));
        recipe(recipes, "hypostasis_gem_from_block", shapeless(
                List.of(ingredientItem(PsitweaksBlocks.HYPOSTASIS_GEM_BLOCK)),
                PsitweaksItems.HYPOSTASIS_GEM, 9));
        recipe(recipes, "plutonium_block", shaped("building",
                List.of("PPP", "PPP", "PPP"),
                Map.of('P', ingredientItem(MekanismItems.PLUTONIUM_PELLET)),
                PsitweaksBlocks.PLUTONIUM_BLOCK.get(), 1));
        recipe(recipes, "plutonium_pellet_from_block", shapeless(
                List.of(ingredientItem(PsitweaksBlocks.PLUTONIUM_BLOCK)),
                "mekanism:pellet_plutonium", 9));
        recipe(recipes, "polonium_block", shaped("building",
                List.of("PPP", "PPP", "PPP"),
                Map.of('P', ingredientItem(MekanismItems.POLONIUM_PELLET)),
                PsitweaksBlocks.POLONIUM_BLOCK.get(), 1));
        recipe(recipes, "polonium_pellet_from_block", shapeless(
                List.of(ingredientItem(PsitweaksBlocks.POLONIUM_BLOCK)),
                "mekanism:pellet_polonium", 9));
        recipe(recipes, "raw_antinite_block", shaped("building",
                List.of("RRR", "RRR", "RRR"),
                Map.of('R', ingredientItem(PsitweaksItems.RAW_ANTINITE)),
                PsitweaksBlocks.RAW_ANTINITE_BLOCK.get(), 1));
        recipe(recipes, "raw_antinite_from_block", shapeless(
                List.of(ingredientItem(PsitweaksBlocks.RAW_ANTINITE_BLOCK)),
                PsitweaksItems.RAW_ANTINITE, 9));
        recipe(recipes, "spellmachinery_casing", shaped("building",
                List.of("AHA", "HPH", "AHA"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.ALLOY_HYPOSTASIS),
                        'H', ingredientItem(PsitweaksItems.MAGATAMA),
                        'P', ingredientItem(PsitweaksItems.PSYCHEONIC_METAL_INGOT)
                ),
                PsitweaksBlocks.SPELLMACHINERY_CASING.get(), 1));
        recipe(recipes, "philosophers_stone", shaped(
                List.of("AEA", "ENE", "AEA"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.ANTINITE_INGOT),
                        'E', ingredientItem(PsitweaksItems.ALLOY_PSIONIC_ECHO),
                        'N', ingredientItem(Items.NETHER_STAR)
                ),
                PsitweaksItems.PHILOSOPHERS_STONE, 1));
        recipe(recipes, "portable_cad_assembler", shaped(
                List.of("CGC", "EAE", "CGC"),
                Map.of(
                        'C', ingredientItem(PsitweaksItems.CHAOTIC_PSIMETAL),
                        'G', ingredientItem(ModItems.psigem.get()),
                        'E', ingredientItem(Items.ENDER_PEARL),
                        'A', ingredientItem(ModItems.cadAssemblerItem.get())
                ),
                PsitweaksItems.PORTABLE_CAD_ASSEMBLER, 1));
        recipe(recipes, "spell_magazine", shaped("tools",
                List.of("PBH"),
                Map.of(
                        'P', ingredientItem(PsitweaksItems.CHAOTIC_PSIMETAL),
                        'B', ingredientItem(PsitweaksItems.FLASHMETAL),
                        'H', ingredientItem(ModItems.cadSocketHuge.get())
                ),
                PsitweaksItems.SPELL_MAGAZINE, 1));
        recipe(recipes, "curios_controller", shapeless("tools",
                List.of(
                        ingredientItem(ModItems.exosuitController.get()),
                        ingredientItem(PsitweaksItems.CHAOTIC_PSIMETAL)
                ),
                PsitweaksItems.CURIOS_CONTROLLER, 1));
        recipe(recipes, "flash_charm", shaped("combat",
                List.of(" F ", "FNF", " F "),
                Map.of(
                        'F', ingredientItem(PsitweaksItems.FLASHMETAL),
                        'N', ingredientItem(Items.NETHER_WART)
                ),
                PsitweaksItems.FLASH_CHARM, 1));
        recipe(recipes, "interference_range_extender", shaped("combat",
                List.of("ABA", "BCB", "ABA"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.FLASHMETAL),
                        'B', ingredientItem(PsitweaksItems.PSIONIC_CONTROL_CIRCUIT),
                        'C', ingredientItem(MekanismItems.TELEPORTATION_CORE)
                ),
                PsitweaksItems.INTERFERENCE_RANGE_EXTENDER, 1));
        recipe(recipes, "third_eye_device", shaped("combat",
                List.of("ABA", "CDC", "ABA"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.PSYCHEONIC_METAL_INGOT),
                        'B', ingredientItem(Items.HEAVY_CORE),
                        'C', ingredientItem(MekanismItems.ANTIMATTER_PELLET),
                        'D', ingredientItem(PsitweaksItems.INTERFERENCE_RANGE_EXTENDER)
                ),
                PsitweaksItems.THIRD_EYE_DEVICE, 1));
        recipe(recipes, "sorcery_booster", shaped("tools",
                List.of("CDC", "BBB", "CCC"),
                Map.of(
                        'B', ingredientItem(PsitweaksItems.MAGICIANS_BRAIN),
                        'C', ingredientItem(PsitweaksItems.FLASHMETAL),
                        'D', ingredientItem(PsitweaksItems.HEAVY_PSIMETAL)
                ),
                PsitweaksItems.SORCERY_BOOSTER, 1));

        addSpellBulletRecipes(recipes);
        addSculkEroderRecipes(recipes);
        ProgramResearchRecipeProvider.addRecipes(recipes);
        addPhilosophersStoneRecipes(recipes);
        addMysticalAgricultureRecipes(recipes);
        addMysticalAgradditionsRecipes(recipes);
        recipe(recipes, "program_duplication", specialCrafting("psitweaks:crafting_special_program_duplication"));

        recipes.forEach(RecipeConditionHelper::addReferencedModConditions);
        CompletableFuture<?>[] futures = recipes.entrySet().stream()
                .map(entry -> DataProvider.saveStable(output, entry.getValue(), pathProvider.json(entry.getKey())))
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }

    @Override
    public String getName() {
        return "PsiTweaks recipes";
    }

    private static void recipe(Map<ResourceLocation, JsonObject> recipes, String id, JsonObject recipe) {
        recipes.put(Psitweaks.location(id), recipe);
    }

    private static void addNuggetRecipes(Map<ResourceLocation, JsonObject> recipes, String materialId,
                                         ItemLike nugget, ItemLike ingot) {
        recipe(recipes, materialId + "_from_nuggets", shaped(
                List.of("NNN", "NNN", "NNN"),
                Map.of('N', ingredientItem(nugget)),
                ingot, 1));
        recipe(recipes, materialId + "_nugget_from_ingot", shapeless(
                List.of(ingredientItem(ingot)),
                nugget, 9));
    }

    private static void addSpellBulletRecipes(Map<ResourceLocation, JsonObject> recipes) {
        List<SpellBulletVariant> variants = List.of(
                new SpellBulletVariant("", ModItems.spellBullet.get()),
                new SpellBulletVariant("_loop", ModItems.loopSpellBullet.get()),
                new SpellBulletVariant("_mine", ModItems.mineSpellBullet.get()),
                new SpellBulletVariant("_charge", ModItems.chargeSpellBullet.get()),
                new SpellBulletVariant("_grenade", ModItems.grenadeSpellBullet.get()),
                new SpellBulletVariant("_projectile", ModItems.projectileSpellBullet.get()),
                new SpellBulletVariant("_circle", ModItems.circleSpellBullet.get())
        );
        List<SpellBulletRecipeTier> tiers = List.of(
                new SpellBulletRecipeTier("advanced", ModItems.psimetal.get(), PsitweaksItems.PSIONIC_CONTROL_CIRCUIT),
                new SpellBulletRecipeTier("resonant", PsitweaksItems.CHAOTIC_PSIMETAL, PsitweaksItems.PSIONIC_CONTROL_CIRCUIT),
                new SpellBulletRecipeTier("sublimated", PsitweaksItems.FLASHMETAL, PsitweaksItems.PSIONIC_CONTROL_CIRCUIT),
                new SpellBulletRecipeTier("awakened", PsitweaksItems.HEAVY_PSIMETAL, PsitweaksItems.ECHO_CONTROL_CIRCUIT),
                new SpellBulletRecipeTier("transcendent", PsitweaksItems.PSYCHEONIC_METAL_INGOT, PsitweaksItems.HYPOSTASIS_CONTROL_CIRCUIT)
        );

        for (SpellBulletVariant variant : variants) {
            String previousItem = itemId(variant.psiItem());
            for (SpellBulletRecipeTier tier : tiers) {
                String id = tier.id() + "_spell_bullet" + variant.suffix();
                String result = item(id);

                recipe(recipes, id, shaped("equipment",
                        List.of("ABA", "BCB", "ABA"),
                        Map.of(
                                'A', ingredientItem(tier.material()),
                                'B', ingredientItemId(previousItem),
                                'C', ingredientItem(tier.circuit())
                        ),
                        result, 1));

                previousItem = result;
            }
        }
    }

    private static void addSculkEroderRecipes(Map<ResourceLocation, JsonObject> recipes) {
        recipe(recipes, "sculk_eroder/stone", sculkEroder(ingredientTag("c:stones"), Blocks.SCULK, 1));
        recipe(recipes, "sculk_eroder/dirt", sculkEroder(ingredientTag("minecraft:dirt"), Blocks.SCULK, 1));
        recipe(recipes, "sculk_eroder/sand", sculkEroder(ingredientTag("c:sands"), Blocks.SCULK, 1));
        recipe(recipes, "sculk_eroder/cobblestone", sculkEroder(ingredientTag("c:cobblestones"), Blocks.SCULK, 1));
    }

    private static void addPhilosophersStoneRecipes(Map<ResourceLocation, JsonObject> recipes) {
        recipe(recipes, "philosophers_stone/philosophers_stone_copper_to_iron", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientTag("c:ingots/copper"),
                        ingredientTag("c:ingots/copper"), ingredientTag("c:ingots/copper"), ingredientTag("c:ingots/copper")),
                Items.IRON_INGOT, 2));
        recipe(recipes, "philosophers_stone/philosophers_stone_iron_to_copper", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientTag("c:ingots/iron"), ingredientTag("c:ingots/iron")),
                Items.COPPER_INGOT, 4));
        recipe(recipes, "philosophers_stone/philosophers_stone_iron_to_osmium", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientTag("c:ingots/iron"), ingredientTag("c:ingots/iron"),
                        ingredientTag("c:ingots/iron"), ingredientTag("c:ingots/iron")),
                "mekanism:ingot_osmium", 2));
        recipe(recipes, "philosophers_stone/philosophers_stone_osmium_to_iron", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientTag("c:ingots/osmium"), ingredientTag("c:ingots/osmium")),
                Items.IRON_INGOT, 4));
        recipe(recipes, "philosophers_stone/philosophers_stone_osmium_to_gold", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientTag("c:ingots/osmium"), ingredientTag("c:ingots/osmium"),
                        ingredientTag("c:ingots/osmium"), ingredientTag("c:ingots/osmium")),
                Items.GOLD_INGOT, 2));
        recipe(recipes, "philosophers_stone/philosophers_stone_gold_to_osmium", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientTag("c:ingots/gold"), ingredientTag("c:ingots/gold")),
                "mekanism:ingot_osmium", 4));
        recipe(recipes, "philosophers_stone/philosophers_stone_gold_to_uranium", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientTag("c:ingots/gold"), ingredientTag("c:ingots/gold"),
                        ingredientTag("c:ingots/gold"), ingredientTag("c:ingots/gold")),
                "mekanism:ingot_uranium", 2));
        recipe(recipes, "philosophers_stone/philosophers_stone_uranium_to_gold", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientTag("c:ingots/uranium"), ingredientTag("c:ingots/uranium")),
                Items.GOLD_INGOT, 4));
        recipe(recipes, "philosophers_stone/philosophers_stone_iron_to_tin", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientTag("c:ingots/iron"), ingredientTag("c:ingots/iron"),
                        ingredientTag("c:ingots/iron"), ingredientTag("c:ingots/iron"), ingredientTag("c:ingots/iron")),
                "mekanism:ingot_tin", 5));
        recipe(recipes, "philosophers_stone/philosophers_stone_tin_to_iron", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientTag("c:ingots/tin"), ingredientTag("c:ingots/tin"),
                        ingredientTag("c:ingots/tin"), ingredientTag("c:ingots/tin"), ingredientTag("c:ingots/tin")),
                Items.IRON_INGOT, 5));
        recipe(recipes, "philosophers_stone/philosophers_stone_osmium_to_lead", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientTag("c:ingots/osmium"), ingredientTag("c:ingots/osmium"),
                        ingredientTag("c:ingots/osmium"), ingredientTag("c:ingots/osmium"), ingredientTag("c:ingots/osmium")),
                "mekanism:ingot_lead", 5));
        recipe(recipes, "philosophers_stone/philosophers_stone_lead_to_osmium", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientTag("c:ingots/lead"), ingredientTag("c:ingots/lead"),
                        ingredientTag("c:ingots/lead"), ingredientTag("c:ingots/lead"), ingredientTag("c:ingots/lead")),
                "mekanism:ingot_osmium", 5));
        recipe(recipes, "philosophers_stone/philosophers_stone_coal_to_diamond", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientItem(Blocks.COAL_BLOCK),
                        ingredientItem(Items.COAL), ingredientItem(Items.COAL), ingredientItem(Items.COAL),
                        ingredientItem(Items.COAL), ingredientItem(Items.COAL), ingredientItem(Items.COAL),
                        ingredientItem(Items.COAL)),
                Items.DIAMOND, 1));
        recipe(recipes, "philosophers_stone/philosophers_stone_charcoal_to_diamond", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientItem(MekanismBlocks.CHARCOAL_BLOCK),
                        ingredientItem(Items.CHARCOAL), ingredientItem(Items.CHARCOAL), ingredientItem(Items.CHARCOAL),
                        ingredientItem(Items.CHARCOAL), ingredientItem(Items.CHARCOAL), ingredientItem(Items.CHARCOAL),
                        ingredientItem(Items.CHARCOAL)),
                Items.DIAMOND, 1));
        recipe(recipes, "philosophers_stone/philosophers_stone_diamond_to_coal", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientItem(Items.DIAMOND)),
                Items.COAL, 16));
        recipe(recipes, "philosophers_stone/philosophers_stone_ender_pearl_to_diamond", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientItem(Items.ENDER_PEARL), ingredientItem(Items.ENDER_PEARL),
                        ingredientItem(Items.ENDER_PEARL), ingredientItem(Items.ENDER_PEARL)),
                Items.DIAMOND, 2));
        recipe(recipes, "philosophers_stone/philosophers_stone_diamond_to_ender_pearl", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientItem(Items.DIAMOND), ingredientItem(Items.DIAMOND)),
                Items.ENDER_PEARL, 4));
        recipe(recipes, "philosophers_stone/philosophers_stone_blaze_rod_to_ender_pearl", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientItem(Items.BLAZE_ROD), ingredientItem(Items.BLAZE_ROD),
                        ingredientItem(Items.BLAZE_ROD), ingredientItem(Items.BLAZE_ROD)),
                Items.ENDER_PEARL, 2));
        recipe(recipes, "philosophers_stone/philosophers_stone_ender_pearl_to_blaze_rod", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientItem(Items.ENDER_PEARL), ingredientItem(Items.ENDER_PEARL)),
                Items.BLAZE_ROD, 4));
        recipe(recipes, "philosophers_stone/philosophers_stone_diamond_to_emerald", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientItem(Items.DIAMOND), ingredientItem(Items.DIAMOND),
                        ingredientItem(Items.DIAMOND), ingredientItem(Items.DIAMOND), ingredientItem(Items.DIAMOND)),
                Items.EMERALD, 5));
        recipe(recipes, "philosophers_stone/philosophers_stone_emerald_to_diamond", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientItem(Items.EMERALD), ingredientItem(Items.EMERALD),
                        ingredientItem(Items.EMERALD), ingredientItem(Items.EMERALD), ingredientItem(Items.EMERALD)),
                Items.DIAMOND, 5));
        recipe(recipes, "philosophers_stone/philosophers_stone_coal_to_quartz", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientItem(Items.COAL), ingredientItem(Items.COAL),
                        ingredientItem(Items.COAL), ingredientItem(Items.COAL), ingredientItem(Items.COAL)),
                Items.QUARTZ, 5));
        recipe(recipes, "philosophers_stone/philosophers_stone_quartz_to_coal", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientItem(Items.QUARTZ), ingredientItem(Items.QUARTZ),
                        ingredientItem(Items.QUARTZ), ingredientItem(Items.QUARTZ), ingredientItem(Items.QUARTZ)),
                Items.COAL, 5));
    }

    private static void addMysticalAgricultureRecipes(Map<ResourceLocation, JsonObject> recipes) {
        addMysticalAgricultureEssenceRecipe(recipes, "psidust", List.of("EEE", "E E", "EEE"), ModItems.psidust.get(), 12);
        addMysticalAgricultureEssenceRecipe(recipes, "psimetal", List.of("EEE", "E E", "EEE"), ModItems.psimetal.get(), 4);
        addMysticalAgricultureEssenceRecipe(recipes, "ebony_psimetal", List.of("EEE", "E E", "EEE"), ModItems.ebonyPsimetal.get(), 4);
        addMysticalAgricultureEssenceRecipe(recipes, "ivory_psimetal", List.of("EEE", "E E", "EEE"), ModItems.ivoryPsimetal.get(), 4);
        addMysticalAgricultureEssenceRecipe(recipes, "psigem", List.of("EEE", "EEE", "EEE"), ModItems.psigem.get(), 1);
        addMysticalAgricultureEssenceRecipe(recipes, "chaotic_psimetal", List.of("EEE", "E E", "EEE"), PsitweaksItems.CHAOTIC_PSIMETAL, 2);
        addMysticalAgricultureEssenceRecipe(recipes, "flashmetal", List.of("EEE", "E E", "EEE"), PsitweaksItems.FLASHMETAL_NUGGET, 12);
        addMysticalAgricultureEssenceRecipe(recipes, "heavy_psimetal", List.of("EEE", "E E", "EEE"), PsitweaksItems.HEAVY_PSIMETAL_NUGGET, 6);
        addMysticalAgricultureEssenceRecipe(recipes, "antinite", List.of("EEE", "E E", "EEE"), PsitweaksItems.ANTINITE_INGOT, 1);
    }

    private static void addMysticalAgradditionsRecipes(Map<ResourceLocation, JsonObject> recipes) {
        JsonObject essenceRecipe = shaped(
                List.of("EEE", "EEE", "EEE"),
                Map.of('E', ingredientItemId("mysticalagriculture:psycheonic_metal_essence")),
                PsitweaksItems.PSYCHEONIC_METAL_NUGGET,
                3
        );
        RecipeConditionHelper.requireMod(essenceRecipe, "mysticalagradditions");
        recipe(recipes, "mystical_agradditions/essence/psycheonic_metal", essenceRecipe);

        JsonObject cruxRecipe = shaped(
                List.of("ABA", "BCB", "ABA"),
                Map.of(
                        'A', ingredientItemId("mysticalagradditions:insanium_essence"),
                        'B', ingredientItem(PsitweaksItems.PSYCHEONIC_METAL_INGOT),
                        'C', ingredientItem(Blocks.DIAMOND_BLOCK)
                ),
                PsitweaksBlocks.PSYCHEONIC_METAL_CRUX_ITEM,
                1
        );
        RecipeConditionHelper.requireMod(cruxRecipe, "mysticalagradditions");
        recipe(recipes, "mystical_agradditions/psycheonic_metal_crux", cruxRecipe);
    }

    private static void addMysticalAgricultureEssenceRecipe(Map<ResourceLocation, JsonObject> recipes,
                                                              String crop,
                                                              List<String> pattern,
                                                              ItemLike result,
                                                              int count) {
        JsonObject recipe = shaped(
                pattern,
                Map.of('E', ingredientItemId("mysticalagriculture:" + crop + "_essence")),
                result,
                count
        );
        RecipeConditionHelper.requireMod(recipe, "mysticalagriculture");
        recipe(recipes, "mystical_agriculture/essence/" + crop, recipe);
    }

    private static JsonObject shaped(List<String> pattern, Map<Character, JsonObject> keys, String result, int count) {
        return shaped("misc", pattern, keys, result, count);
    }

    private static JsonObject shaped(List<String> pattern, Map<Character, JsonObject> keys, ItemLike result, int count) {
        return shaped("misc", pattern, keys, itemId(result), count);
    }

    private static JsonObject shaped(String category, List<String> pattern, Map<Character, JsonObject> keys,
                                     ItemLike result, int count) {
        return shaped(category, pattern, keys, itemId(result), count);
    }

    private static JsonObject shaped(String category, List<String> pattern, Map<Character, JsonObject> keys, String result, int count) {
        JsonObject root = new JsonObject();
        JsonArray patternJson = new JsonArray();
        JsonObject keyJson = new JsonObject();

        root.addProperty("type", "minecraft:crafting_shaped");
        root.addProperty("category", category);
        for (String row : pattern) {
            patternJson.add(row);
        }
        root.add("pattern", patternJson);
        keys.forEach((key, ingredient) -> keyJson.add(String.valueOf(key), ingredient));
        root.add("key", keyJson);
        root.add("result", result(result, count));

        return root;
    }

    private static JsonObject shapeless(List<JsonObject> ingredients, String result, int count) {
        return shapeless("misc", ingredients, result, count);
    }

    private static JsonObject shapeless(List<JsonObject> ingredients, ItemLike result, int count) {
        return shapeless("misc", ingredients, itemId(result), count);
    }

    private static JsonObject shapeless(String category, List<JsonObject> ingredients, ItemLike result, int count) {
        return shapeless(category, ingredients, itemId(result), count);
    }

    private static JsonObject shapeless(String category, List<JsonObject> ingredients, String result, int count) {
        JsonObject root = new JsonObject();
        JsonArray ingredientsJson = new JsonArray();

        root.addProperty("type", "minecraft:crafting_shapeless");
        root.addProperty("category", category);
        ingredients.forEach(ingredientsJson::add);
        root.add("ingredients", ingredientsJson);
        root.add("result", result(result, count));

        return root;
    }

    private static JsonObject cooking(String type, JsonObject ingredient, String result, double experience, int cookingTime) {
        JsonObject root = new JsonObject();

        root.addProperty("type", type);
        root.addProperty("category", "misc");
        root.add("ingredient", ingredient);
        root.add("result", result(result, 1));
        root.addProperty("experience", experience);
        root.addProperty("cookingtime", cookingTime);

        return root;
    }

    private static JsonObject cooking(String type, JsonObject ingredient, ItemLike result, double experience,
                                      int cookingTime) {
        return cooking(type, ingredient, itemId(result), experience, cookingTime);
    }

    private static JsonObject trickCrafting(JsonObject input, ItemLike output, String piece, ItemLike cadAssembly) {
        JsonObject root = new JsonObject();

        root.addProperty("type", "psi:trick_crafting");
        root.add("cad", result(cadAssembly, 1));
        root.add("input", input);
        root.add("output", result(output, 1));
        root.addProperty("piece", piece);

        return root;
    }

    private static JsonObject sculkEroder(JsonObject inputIngredient, String output, int outputCount) {
        JsonObject root = new JsonObject();
        JsonObject input = new JsonObject();

        root.addProperty("type", "psitweaks:sculk_eroder");
        input.add("ingredient", inputIngredient);
        root.add("input", input);
        root.add("output", result(output, outputCount));

        return root;
    }

    private static JsonObject sculkEroder(JsonObject inputIngredient, ItemLike output, int outputCount) {
        return sculkEroder(inputIngredient, itemId(output), outputCount);
    }

    private static JsonObject specialCrafting(String type) {
        JsonObject root = new JsonObject();
        root.addProperty("type", type);
        root.addProperty("category", "misc");
        return root;
    }

    private static JsonObject ingredientItem(ItemLike item) {
        return ingredientItemId(itemId(item));
    }

    private static JsonObject ingredientItemId(String item) {
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", item);
        return ingredient;
    }

    private static JsonObject ingredientTag(String tag) {
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("tag", tag);
        return ingredient;
    }

    private static JsonObject result(String item, int count) {
        JsonObject result = new JsonObject();
        result.addProperty("count", count);
        result.addProperty("id", item);
        return result;
    }

    private static JsonObject result(ItemLike item, int count) {
        return result(itemId(item), count);
    }

    private static String itemId(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).toString();
    }

    private static String item(String path) {
        return Psitweaks.MOD_ID + ":" + path;
    }

    private record SpellBulletVariant(String suffix, ItemLike psiItem) {
    }

    private record SpellBulletRecipeTier(String id, ItemLike material, ItemLike circuit) {
    }
}
