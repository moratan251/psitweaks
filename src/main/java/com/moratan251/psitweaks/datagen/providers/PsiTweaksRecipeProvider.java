package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
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
                item("psionic_control_circuit"), 1));
        recipe(recipes, "echo_control_circuit", shaped(
                List.of("SSS", "ACA", "SSS"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.ALLOY_PSIONIC_ECHO),
                        'C', ingredientItem(PsitweaksItems.PSIONIC_CONTROL_CIRCUIT),
                        'S', ingredientItem(PsitweaksItems.ECHO_SHEET)
                ),
                item("echo_control_circuit"), 1));
        recipe(recipes, "hypostasis_control_circuit", shaped(
                List.of("SSS", "ACA", "SSS"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.ALLOY_HYPOSTASIS),
                        'C', ingredientItem(PsitweaksItems.ECHO_CONTROL_CIRCUIT),
                        'S', ingredientItem(PsitweaksItems.ECHO_SHEET)
                ),
                item("hypostasis_control_circuit"), 1));
        recipe(recipes, "magatama", shaped(
                List.of("JJJ", "JHJ", "JJJ"),
                Map.of(
                        'H', ingredientItem(PsitweaksItems.HYPOSTASIS_GEM),
                        'J', ingredientItem(PsitweaksItems.JADE)
                ),
                item("magatama"), 1));
        recipe(recipes, "psionic_echo", trickCrafting(
                ingredientItem(Items.ECHO_SHARD),
                item("psionic_echo"),
                "psitweaks:trick_supreme_infusion",
                "psi:cad_assembly_psimetal"));
        recipe(recipes, "incomplete_heavy_psimetal_assembly", shaped(
                List.of("III", "  I"),
                Map.of('I', ingredientItem(PsitweaksItems.HEAVY_PSIMETAL)),
                item("incomplete_heavy_psimetal_assembly"), 1));
        recipe(recipes, "cad_assembly_alloy_psion", shaped(
                List.of("AAA", "  A"),
                Map.of('A', ingredientItem(PsitweaksItems.ALLOY_PSION)),
                item("cad_assembly_alloy_psion"), 1));
        recipe(recipes, "cad_assembly_chaotic_psimetal", shaped("equipment",
                List.of("AAA", " BA"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.CHAOTIC_PSIMETAL),
                        'B', ingredientItem(PsitweaksItems.PSIONIC_CONTROL_CIRCUIT)
                ),
                item("cad_assembly_chaotic_psimetal"), 1));
        recipe(recipes, "cad_assembly_flashmetal", shaped("equipment",
                List.of("HHH", "FFF", " CF"),
                Map.of(
                        'C', ingredientItem(PsitweaksItems.PSIONIC_CONTROL_CIRCUIT),
                        'F', ingredientItem(PsitweaksItems.FLASHMETAL),
                        'H', ingredientItem(MekanismItems.HDPE_SHEET)
                ),
                item("cad_assembly_flashmetal"), 1));
        recipe(recipes, "cad_assembly_heavy_psimetal_alpha", shaped("equipment",
                List.of("SNS", "CIC", "SCS"),
                Map.of(
                        'C', ingredientItem(PsitweaksItems.ECHO_CONTROL_CIRCUIT),
                        'I', ingredientItem(PsitweaksItems.INCOMPLETE_HEAVY_PSIMETAL_ASSEMBLY),
                        'N', ingredientItem(Items.NETHER_STAR),
                        'S', ingredientItem(PsitweaksItems.ECHO_SHEET)
                ),
                item("cad_assembly_heavy_psimetal_alpha"), 1));
        recipe(recipes, "cad_assembly_heavy_psimetal_beta", shaped("equipment",
                List.of("SCS", "PIP", "SCS"),
                Map.of(
                        'C', ingredientItem(PsitweaksItems.ECHO_CONTROL_CIRCUIT),
                        'I', ingredientItem(PsitweaksItems.INCOMPLETE_HEAVY_PSIMETAL_ASSEMBLY),
                        'P', ingredientItem(MekanismItems.PLUTONIUM_PELLET),
                        'S', ingredientItem(PsitweaksItems.ECHO_SHEET)
                ),
                item("cad_assembly_heavy_psimetal_beta"), 1));
        recipe(recipes, "cad_assembly_psycheonic_metal_from_alpha", shaped("equipment",
                List.of("PCP", "AHA", "PCP"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.PELLET_AMERICIUM),
                        'C', ingredientItem(PsitweaksItems.HYPOSTASIS_CONTROL_CIRCUIT),
                        'H', ingredientItem(PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_ALPHA),
                        'P', ingredientItem(PsitweaksItems.PSYCHEONIC_METAL_INGOT)
                ),
                item("cad_assembly_psycheonic_metal"), 1));
        recipe(recipes, "cad_assembly_psycheonic_metal_from_beta", shaped("equipment",
                List.of("PCP", "AHA", "PCP"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.PELLET_AMERICIUM),
                        'C', ingredientItem(PsitweaksItems.HYPOSTASIS_CONTROL_CIRCUIT),
                        'H', ingredientItem(PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_BETA),
                        'P', ingredientItem(PsitweaksItems.PSYCHEONIC_METAL_INGOT)
                ),
                item("cad_assembly_psycheonic_metal"), 1));
        recipe(recipes, "unrefined_flashmetal", shaped(
                List.of("BBB", "AAA", "BBB"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.CHAOTIC_PSIMETAL),
                        'B', ingredientItem(MekanismItems.REFINED_GLOWSTONE_INGOT)
                ),
                item("unrefined_flashmetal"), 1));
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
                item("heavy_psimetal"), 1));
        recipe(recipes, "psycheonic_metal_ingot", shaped(
                List.of("NNN", "NNN", "NNN"),
                Map.of('N', ingredientItem(PsitweaksItems.PSYCHEONIC_METAL_NUGGET)),
                item("psycheonic_metal_ingot"), 1));
        recipe(recipes, "psycheonic_metal_nugget_from_ingot", shapeless(
                List.of(ingredientItem(PsitweaksItems.PSYCHEONIC_METAL_INGOT)),
                item("psycheonic_metal_nugget"), 9));
        recipe(recipes, "amethyst_shard_from_block", shapeless(
                List.of(ingredientItem(Blocks.AMETHYST_BLOCK)),
                "minecraft:amethyst_shard", 4));
        recipe(recipes, "antinite_ingot_smelting", cooking(
                "minecraft:smelting",
                ingredientItem(PsitweaksItems.RAW_ANTINITE),
                item("antinite_ingot"), 1.2D, 200));
        recipe(recipes, "antinite_ingot_blasting", cooking(
                "minecraft:blasting",
                ingredientItem(PsitweaksItems.RAW_ANTINITE),
                item("antinite_ingot"), 1.2D, 100));
        recipe(recipes, "antinite_ingot_from_dust_smelting", cooking(
                "minecraft:smelting",
                ingredientItem(PsitweaksItems.ANTINITE_DUST),
                item("antinite_ingot"), 0.3D, 200));
        recipe(recipes, "antinite_ingot_from_dust_blasting", cooking(
                "minecraft:blasting",
                ingredientItem(PsitweaksItems.ANTINITE_DUST),
                item("antinite_ingot"), 0.3D, 100));
        recipe(recipes, "cad_disassembler", shaped(
                List.of("PPP", "PAP", "PPP"),
                Map.of(
                        'P', ingredientItem(ModItems.psimetal.get()),
                        'A', ingredientItem(Blocks.ANVIL)
                ),
                block("cad_disassembler"), 1));
        recipe(recipes, "program_researcher", shaped(
                List.of("PCP", "CGC", "PCP"),
                Map.of(
                        'P', ingredientItem(ModItems.psimetal.get()),
                        'C', ingredientItem(PsitweaksItems.PSIONIC_CONTROL_CIRCUIT),
                        'G', ingredientItem(ModItems.psigem.get())
                ),
                block("program_researcher"), 1));
        recipe(recipes, "material_mutator", shaped(
                List.of("APA", "CDC", "ABA"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.ALLOY_HYPOSTASIS),
                        'B', ingredientItem(PsitweaksItems.HYPOSTASIS_CONTROL_CIRCUIT),
                        'C', ingredientItem(PsitweaksItems.ANTINITE_INGOT),
                        'D', ingredientItem(PsitweaksBlocks.SPELLMACHINERY_CASING),
                        'P', ingredientItem(PsitweaksItems.PROGRAM_MATERIAL_MUTATION)
                ),
                block("material_mutator"), 1));
        recipe(recipes, "sculk_eroder", shaped(
                List.of("ABA", "CDC", "ABA"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.ALLOY_PSIONIC_ECHO),
                        'B', ingredientItem(PsitweaksItems.ECHO_CONTROL_CIRCUIT),
                        'C', ingredientItem(Blocks.SCULK_CATALYST),
                        'D', ingredientItem(MekanismBlocks.STEEL_CASING)
                ),
                block("sculk_eroder"), 1));
        recipe(recipes, "psionic_generator", shaped(
                List.of(" B ", "ACA", "ATA"),
                Map.of(
                        'A', ingredientItem(ModItems.psimetal.get()),
                        'B', ingredientItem(Items.ENDER_PEARL),
                        'C', ingredientItem(MekanismItems.BASIC_CONTROL_CIRCUIT),
                        'T', ingredientItem(MekanismItems.ENERGY_TABLET)
                ),
                block("psionic_generator"), 1));
        recipe(recipes, "transcendent_universal_cable", shaped("redstone",
                List.of("APA", "PNP", "APA"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.ALLOY_HYPOSTASIS),
                        'N', ingredientItem(PsitweaksItems.PELLET_NEPTUNIUM),
                        'P', ingredientItem(MekanismBlocks.ULTIMATE_UNIVERSAL_CABLE)
                ),
                block("transcendent_universal_cable"), 4));
        recipe(recipes, "transcendent_energy_cube", shaped("redstone",
                List.of("AEA", "NCN", "AEA"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.ALLOY_HYPOSTASIS),
                        'C', ingredientItem(MekanismBlocks.ULTIMATE_ENERGY_CUBE),
                        'E', ingredientItem(MekanismItems.ENERGY_TABLET),
                        'N', ingredientItem(PsitweaksItems.PELLET_NEPTUNIUM)
                ),
                block("transcendent_energy_cube"), 1));
        recipe(recipes, "program_blank", shaped(
                List.of("D", "P", "M"),
                Map.of(
                        'D', ingredientItem(ModItems.psidust.get()),
                        'P', ingredientItem(Items.PAPER),
                        'M', ingredientItem(ModItems.psimetal.get())
                ),
                item("program_blank"), 1));
        recipe(recipes, "psimetal_bow", shaped("combat",
                List.of(" MS", "G S", " MS"),
                Map.of(
                        'M', ingredientItem(ModItems.psimetal.get()),
                        'G', ingredientItem(ModItems.psigem.get()),
                        'S', ingredientItem(Items.STRING)
                ),
                item("psimetal_bow"), 1));
        recipe(recipes, "auto_caster_tick", shaped("combat",
                List.of("ECE", "EFE", "EEE"),
                Map.of(
                        'E', ingredientItem(ModItems.ebonyPsimetal.get()),
                        'F', ingredientItem(PsitweaksItems.CHAOTIC_FACTOR),
                        'C', ingredientItem(Items.CLOCK)
                ),
                item("auto_caster_tick"), 1));
        recipe(recipes, "auto_caster_custom_tick", shaped("combat",
                List.of("ECE", "EFE", "EEE"),
                Map.of(
                        'E', ingredientItem(PsitweaksItems.CHAOTIC_PSIMETAL),
                        'F', ingredientItem(PsitweaksItems.CHAOTIC_FACTOR),
                        'C', ingredientItem(Items.CLOCK)
                ),
                item("auto_caster_custom_tick"), 1));
        recipe(recipes, "moval_suit_helmet", shaped("equipment",
                List.of("EHE", "E E"),
                Map.of(
                        'E', ingredientItem(ModItems.ebonyPsimetal.get()),
                        'H', ingredientItem(PsitweaksItems.HEAVY_PSIMETAL)
                ),
                item("moval_suit_helmet"), 1));
        recipe(recipes, "moval_suit_chestplate", shaped("equipment",
                List.of("E E", "EHE", "EEE"),
                Map.of(
                        'E', ingredientItem(ModItems.ebonyPsimetal.get()),
                        'H', ingredientItem(PsitweaksItems.HEAVY_PSIMETAL)
                ),
                item("moval_suit_chestplate"), 1));
        recipe(recipes, "moval_suit_leggings", shaped("equipment",
                List.of("EHE", "E E", "E E"),
                Map.of(
                        'E', ingredientItem(ModItems.ebonyPsimetal.get()),
                        'H', ingredientItem(PsitweaksItems.HEAVY_PSIMETAL)
                ),
                item("moval_suit_leggings"), 1));
        recipe(recipes, "moval_suit_boots", shaped("equipment",
                List.of("E E", "EHE"),
                Map.of(
                        'E', ingredientItem(ModItems.ebonyPsimetal.get()),
                        'H', ingredientItem(PsitweaksItems.HEAVY_PSIMETAL)
                ),
                item("moval_suit_boots"), 1));
        recipe(recipes, "module_psyon_supplying_unit", shaped("equipment",
                List.of("AEA", "AMA", "SSS"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.ALLOY_PSION),
                        'E', ingredientItem(PsitweaksItems.CHAOTIC_FACTOR),
                        'M', ingredientItem(MekanismItems.MODULE_BASE),
                        'S', ingredientItem(PsitweaksItems.ECHO_SHEET)
                ),
                item("module_psyon_supplying_unit"), 1));
        recipe(recipes, "module_psyon_capacity_unit", shaped("equipment",
                List.of("AEA", "AMA", "SSS"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.ALLOY_PSION),
                        'E', ingredientItem(PsitweaksItems.ANTINITE_INGOT),
                        'M', ingredientItem(MekanismItems.MODULE_BASE),
                        'S', ingredientItem(PsitweaksItems.ECHO_SHEET)
                ),
                item("module_psyon_capacity_unit"), 1));
        recipe(recipes, "module_phenomenon_interference_enhancement_unit", shaped("equipment",
                List.of("AEA", "AMA", "NNN"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.ALLOY_HYPOSTASIS),
                        'E', ingredientItem(PsitweaksItems.MAGICIANS_BRAIN),
                        'M', ingredientItem(MekanismItems.MODULE_BASE),
                        'N', ingredientItem(PsitweaksItems.PELLET_NEPTUNIUM)
                ),
                item("module_phenomenon_interference_enhancement_unit"), 1));
        recipe(recipes, "inline_caster", shaped("equipment",
                List.of(" G ", "PBP", " P "),
                Map.of(
                        'B', ingredientItem(ModItems.cadAssemblyPsimetal.get()),
                        'G', ingredientItem(ModItems.psigem.get()),
                        'P', ingredientItem(ModItems.psimetal.get())
                ),
                item("inline_caster"), 1));
        recipe(recipes, "secondary_caster", shaped("equipment",
                List.of(" F ", "CBC", " T "),
                Map.of(
                        'B', ingredientItem(ModItems.cadAssemblyPsimetal.get()),
                        'C', ingredientItem(PsitweaksItems.PSIONIC_CONTROL_CIRCUIT),
                        'F', ingredientItem(PsitweaksItems.FLASHMETAL),
                        'T', ingredientItem(ModItems.cadSocketSignaling.get())
                ),
                item("secondary_caster"), 1));
        recipe(recipes, "parallel_caster", shaped("equipment",
                List.of("FHF", "CBC", "FTF"),
                Map.of(
                        'B', ingredientItem(ModItems.cadAssemblyPsimetal.get()),
                        'C', ingredientItem(PsitweaksItems.ECHO_CONTROL_CIRCUIT),
                        'F', ingredientItem(PsitweaksItems.FLASHMETAL),
                        'H', ingredientItem(PsitweaksItems.HEAVY_PSIMETAL),
                        'T', ingredientItem(ModItems.cadSocketTransmissive.get())
                ),
                item("parallel_caster"), 1));
        recipe(recipes, "antinite_block", shaped("building",
                List.of("III", "III", "III"),
                Map.of('I', ingredientItem(PsitweaksItems.ANTINITE_INGOT)),
                block("antinite_block"), 1));
        recipe(recipes, "antinite_ingot_from_block", shapeless(
                List.of(ingredientItem(PsitweaksBlocks.ANTINITE_BLOCK)),
                item("antinite_ingot"), 9));
        recipe(recipes, "chaotic_psimetal_block", shaped("building",
                List.of("III", "III", "III"),
                Map.of('I', ingredientItem(PsitweaksItems.CHAOTIC_PSIMETAL)),
                block("chaotic_psimetal_block"), 1));
        recipe(recipes, "chaotic_psimetal_from_block", shapeless(
                List.of(ingredientItem(PsitweaksBlocks.CHAOTIC_PSIMETAL_BLOCK)),
                item("chaotic_psimetal"), 9));
        recipe(recipes, "flashmetal_block", shaped("building",
                List.of("III", "III", "III"),
                Map.of('I', ingredientItem(PsitweaksItems.FLASHMETAL)),
                block("flashmetal_block"), 1));
        recipe(recipes, "flashmetal_from_block", shapeless(
                List.of(ingredientItem(PsitweaksBlocks.FLASHMETAL_BLOCK)),
                item("flashmetal"), 9));
        recipe(recipes, "heavy_psimetal_block", shaped("building",
                List.of("III", "III", "III"),
                Map.of('I', ingredientItem(PsitweaksItems.HEAVY_PSIMETAL)),
                block("heavy_psimetal_block"), 1));
        recipe(recipes, "heavy_psimetal_from_block", shapeless(
                List.of(ingredientItem(PsitweaksBlocks.HEAVY_PSIMETAL_BLOCK)),
                item("heavy_psimetal"), 9));
        recipe(recipes, "plutonium_block", shaped("building",
                List.of("PPP", "PPP", "PPP"),
                Map.of('P', ingredientItem(MekanismItems.PLUTONIUM_PELLET)),
                block("plutonium_block"), 1));
        recipe(recipes, "plutonium_pellet_from_block", shapeless(
                List.of(ingredientItem(PsitweaksBlocks.PLUTONIUM_BLOCK)),
                "mekanism:pellet_plutonium", 9));
        recipe(recipes, "polonium_block", shaped("building",
                List.of("PPP", "PPP", "PPP"),
                Map.of('P', ingredientItem(MekanismItems.POLONIUM_PELLET)),
                block("polonium_block"), 1));
        recipe(recipes, "polonium_pellet_from_block", shapeless(
                List.of(ingredientItem(PsitweaksBlocks.POLONIUM_BLOCK)),
                "mekanism:pellet_polonium", 9));
        recipe(recipes, "raw_antinite_block", shaped("building",
                List.of("RRR", "RRR", "RRR"),
                Map.of('R', ingredientItem(PsitweaksItems.RAW_ANTINITE)),
                block("raw_antinite_block"), 1));
        recipe(recipes, "raw_antinite_from_block", shapeless(
                List.of(ingredientItem(PsitweaksBlocks.RAW_ANTINITE_BLOCK)),
                item("raw_antinite"), 9));
        recipe(recipes, "spellmachinery_casing", shaped("building",
                List.of("AHA", "HPH", "AHA"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.ALLOY_HYPOSTASIS),
                        'H', ingredientItem(PsitweaksItems.MAGATAMA),
                        'P', ingredientItem(PsitweaksItems.PSYCHEONIC_METAL_INGOT)
                ),
                block("spellmachinery_casing"), 1));
        recipe(recipes, "philosophers_stone", shaped(
                List.of("AEA", "ENE", "AEA"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.ANTINITE_INGOT),
                        'E', ingredientItem(PsitweaksItems.ALLOY_PSIONIC_ECHO),
                        'N', ingredientItem(Items.NETHER_STAR)
                ),
                item("philosophers_stone"), 1));
        recipe(recipes, "portable_cad_assembler", shaped(
                List.of("CGC", "EAE", "CGC"),
                Map.of(
                        'C', ingredientItem(PsitweaksItems.CHAOTIC_PSIMETAL),
                        'G', ingredientItem(ModItems.psigem.get()),
                        'E', ingredientItem(Items.ENDER_PEARL),
                        'A', ingredientItem(ModItems.cadAssemblerItem.get())
                ),
                item("portable_cad_assembler"), 1));
        recipe(recipes, "spell_magazine", shaped("tools",
                List.of("PBH"),
                Map.of(
                        'P', ingredientItem(PsitweaksItems.CHAOTIC_PSIMETAL),
                        'B', ingredientItem(PsitweaksItems.FLASHMETAL),
                        'H', ingredientItem(ModItems.cadSocketHuge.get())
                ),
                item("spell_magazine"), 1));
        recipe(recipes, "curios_controller", shapeless("tools",
                List.of(
                        ingredientItem(ModItems.exosuitController.get()),
                        ingredientItem(PsitweaksItems.CHAOTIC_PSIMETAL)
                ),
                item("curios_controller"), 1));
        recipe(recipes, "flash_charm", shaped("combat",
                List.of(" F ", "FNF", " F "),
                Map.of(
                        'F', ingredientItem(PsitweaksItems.FLASHMETAL),
                        'N', ingredientItem(Items.NETHER_WART)
                ),
                item("flash_charm"), 1));
        recipe(recipes, "third_eye_device", shaped("combat",
                List.of("ABA", "BCB", "ABA"),
                Map.of(
                        'A', ingredientItem(PsitweaksItems.PSYCHEONIC_METAL_INGOT),
                        'B', ingredientItem(MekanismItems.TELEPORTATION_CORE),
                        'C', ingredientItem(MekanismItems.ANTIMATTER_PELLET)
                ),
                item("third_eye_device"), 1));
        recipe(recipes, "sorcery_booster", shaped("tools",
                List.of("CDC", "BBB", "CCC"),
                Map.of(
                        'B', ingredientItem(PsitweaksItems.MAGICIANS_BRAIN),
                        'C', ingredientItem(PsitweaksItems.FLASHMETAL),
                        'D', ingredientItem(PsitweaksItems.HEAVY_PSIMETAL)
                ),
                item("sorcery_booster"), 1));

        addSpellBulletRecipes(recipes);
        addSculkEroderRecipes(recipes);
        ProgramResearchRecipeProvider.addRecipes(recipes);
        addPhilosophersStoneRecipes(recipes);
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
        recipe(recipes, "sculk_eroder/stone", sculkEroder(ingredientTag("c:stones"), "minecraft:sculk", 1));
        recipe(recipes, "sculk_eroder/dirt", sculkEroder(ingredientTag("minecraft:dirt"), "minecraft:sculk", 1));
        recipe(recipes, "sculk_eroder/sand", sculkEroder(ingredientTag("c:sands"), "minecraft:sculk", 1));
        recipe(recipes, "sculk_eroder/cobblestone", sculkEroder(ingredientTag("c:cobblestones"), "minecraft:sculk", 1));
    }

    private static void addPhilosophersStoneRecipes(Map<ResourceLocation, JsonObject> recipes) {
        recipe(recipes, "philosophers_stone/philosophers_stone_copper_to_iron", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientTag("c:ingots/copper"),
                        ingredientTag("c:ingots/copper"), ingredientTag("c:ingots/copper"), ingredientTag("c:ingots/copper")),
                "minecraft:iron_ingot", 2));
        recipe(recipes, "philosophers_stone/philosophers_stone_iron_to_copper", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientTag("c:ingots/iron"), ingredientTag("c:ingots/iron")),
                "minecraft:copper_ingot", 4));
        recipe(recipes, "philosophers_stone/philosophers_stone_iron_to_osmium", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientTag("c:ingots/iron"), ingredientTag("c:ingots/iron"),
                        ingredientTag("c:ingots/iron"), ingredientTag("c:ingots/iron")),
                "mekanism:ingot_osmium", 2));
        recipe(recipes, "philosophers_stone/philosophers_stone_osmium_to_iron", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientTag("c:ingots/osmium"), ingredientTag("c:ingots/osmium")),
                "minecraft:iron_ingot", 4));
        recipe(recipes, "philosophers_stone/philosophers_stone_osmium_to_gold", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientTag("c:ingots/osmium"), ingredientTag("c:ingots/osmium"),
                        ingredientTag("c:ingots/osmium"), ingredientTag("c:ingots/osmium")),
                "minecraft:gold_ingot", 2));
        recipe(recipes, "philosophers_stone/philosophers_stone_gold_to_osmium", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientTag("c:ingots/gold"), ingredientTag("c:ingots/gold")),
                "mekanism:ingot_osmium", 4));
        recipe(recipes, "philosophers_stone/philosophers_stone_gold_to_uranium", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientTag("c:ingots/gold"), ingredientTag("c:ingots/gold"),
                        ingredientTag("c:ingots/gold"), ingredientTag("c:ingots/gold")),
                "mekanism:ingot_uranium", 2));
        recipe(recipes, "philosophers_stone/philosophers_stone_uranium_to_gold", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientTag("c:ingots/uranium"), ingredientTag("c:ingots/uranium")),
                "minecraft:gold_ingot", 4));
        recipe(recipes, "philosophers_stone/philosophers_stone_iron_to_tin", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientTag("c:ingots/iron"), ingredientTag("c:ingots/iron"),
                        ingredientTag("c:ingots/iron"), ingredientTag("c:ingots/iron"), ingredientTag("c:ingots/iron")),
                "mekanism:ingot_tin", 5));
        recipe(recipes, "philosophers_stone/philosophers_stone_tin_to_iron", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientTag("c:ingots/tin"), ingredientTag("c:ingots/tin"),
                        ingredientTag("c:ingots/tin"), ingredientTag("c:ingots/tin"), ingredientTag("c:ingots/tin")),
                "minecraft:iron_ingot", 5));
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
                "minecraft:diamond", 1));
        recipe(recipes, "philosophers_stone/philosophers_stone_charcoal_to_diamond", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientItem(MekanismBlocks.CHARCOAL_BLOCK),
                        ingredientItem(Items.CHARCOAL), ingredientItem(Items.CHARCOAL), ingredientItem(Items.CHARCOAL),
                        ingredientItem(Items.CHARCOAL), ingredientItem(Items.CHARCOAL), ingredientItem(Items.CHARCOAL),
                        ingredientItem(Items.CHARCOAL)),
                "minecraft:diamond", 1));
        recipe(recipes, "philosophers_stone/philosophers_stone_diamond_to_coal", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientItem(Items.DIAMOND)),
                "minecraft:coal", 16));
        recipe(recipes, "philosophers_stone/philosophers_stone_ender_pearl_to_diamond", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientItem(Items.ENDER_PEARL), ingredientItem(Items.ENDER_PEARL),
                        ingredientItem(Items.ENDER_PEARL), ingredientItem(Items.ENDER_PEARL)),
                "minecraft:diamond", 2));
        recipe(recipes, "philosophers_stone/philosophers_stone_diamond_to_ender_pearl", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientItem(Items.DIAMOND), ingredientItem(Items.DIAMOND)),
                "minecraft:ender_pearl", 4));
        recipe(recipes, "philosophers_stone/philosophers_stone_blaze_rod_to_ender_pearl", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientItem(Items.BLAZE_ROD), ingredientItem(Items.BLAZE_ROD),
                        ingredientItem(Items.BLAZE_ROD), ingredientItem(Items.BLAZE_ROD)),
                "minecraft:ender_pearl", 2));
        recipe(recipes, "philosophers_stone/philosophers_stone_ender_pearl_to_blaze_rod", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientItem(Items.ENDER_PEARL), ingredientItem(Items.ENDER_PEARL)),
                "minecraft:blaze_rod", 4));
        recipe(recipes, "philosophers_stone/philosophers_stone_diamond_to_emerald", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientItem(Items.DIAMOND), ingredientItem(Items.DIAMOND),
                        ingredientItem(Items.DIAMOND), ingredientItem(Items.DIAMOND), ingredientItem(Items.DIAMOND)),
                "minecraft:emerald", 5));
        recipe(recipes, "philosophers_stone/philosophers_stone_emerald_to_diamond", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientItem(Items.EMERALD), ingredientItem(Items.EMERALD),
                        ingredientItem(Items.EMERALD), ingredientItem(Items.EMERALD), ingredientItem(Items.EMERALD)),
                "minecraft:diamond", 5));
        recipe(recipes, "philosophers_stone/philosophers_stone_coal_to_quartz", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientItem(Items.COAL), ingredientItem(Items.COAL),
                        ingredientItem(Items.COAL), ingredientItem(Items.COAL), ingredientItem(Items.COAL)),
                "minecraft:quartz", 5));
        recipe(recipes, "philosophers_stone/philosophers_stone_quartz_to_coal", shapeless(
                List.of(ingredientItem(PsitweaksItems.PHILOSOPHERS_STONE), ingredientItem(Items.QUARTZ), ingredientItem(Items.QUARTZ),
                        ingredientItem(Items.QUARTZ), ingredientItem(Items.QUARTZ), ingredientItem(Items.QUARTZ)),
                "minecraft:coal", 5));
    }

    private static JsonObject shaped(List<String> pattern, Map<Character, JsonObject> keys, String result, int count) {
        return shaped("misc", pattern, keys, result, count);
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

    private static JsonObject trickCrafting(JsonObject input, String output, String piece, String cadAssembly) {
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

    private static String itemId(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).toString();
    }

    private static String item(String path) {
        return Psitweaks.MOD_ID + ":" + path;
    }

    private static String block(String path) {
        return Psitweaks.MOD_ID + ":" + path;
    }

    private record SpellBulletVariant(String suffix, ItemLike psiItem) {
    }

    private record SpellBulletRecipeTier(String id, ItemLike material, ItemLike circuit) {
    }
}
