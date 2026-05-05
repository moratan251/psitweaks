package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.moratan251.psitweaks.Psitweaks;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

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
                        'A', ingredientItem(item("alloy_psion")),
                        'U', ingredientItem("mekanism:ultimate_control_circuit")
                ),
                item("psionic_control_circuit"), 1));
        recipe(recipes, "echo_control_circuit", shaped(
                List.of("SSS", "ACA", "SSS"),
                Map.of(
                        'A', ingredientItem(item("alloy_psionic_echo")),
                        'C', ingredientItem(item("psionic_control_circuit")),
                        'S', ingredientItem(item("echo_sheet"))
                ),
                item("echo_control_circuit"), 1));
        recipe(recipes, "hypostasis_control_circuit", shaped(
                List.of("SSS", "ACA", "SSS"),
                Map.of(
                        'A', ingredientItem(item("alloy_hypostasis")),
                        'C', ingredientItem(item("echo_control_circuit")),
                        'S', ingredientItem(item("echo_sheet"))
                ),
                item("hypostasis_control_circuit"), 1));
        recipe(recipes, "magatama", shaped(
                List.of("JJJ", "JHJ", "JJJ"),
                Map.of(
                        'H', ingredientItem(item("hypostasis_gem")),
                        'J', ingredientItem(item("jade"))
                ),
                item("magatama"), 1));
        recipe(recipes, "psionic_echo", trickCrafting(
                ingredientItem("minecraft:echo_shard"),
                item("psionic_echo"),
                "psitweaks:trick_supreme_infusion",
                "psi:cad_assembly_psimetal"));
        recipe(recipes, "incomplete_heavy_psimetal_assembly", shaped(
                List.of("III", "  I"),
                Map.of('I', ingredientItem(item("heavy_psimetal"))),
                item("incomplete_heavy_psimetal_assembly"), 1));
        recipe(recipes, "cad_assembly_alloy_psion", shaped(
                List.of("AAA", "  A"),
                Map.of('A', ingredientItem(item("alloy_psion"))),
                item("cad_assembly_alloy_psion"), 1));
        recipe(recipes, "cad_assembly_chaotic_psimetal", shaped("equipment",
                List.of("AAA", " BA"),
                Map.of(
                        'A', ingredientItem(item("chaotic_psimetal")),
                        'B', ingredientItem(item("psionic_control_circuit"))
                ),
                item("cad_assembly_chaotic_psimetal"), 1));
        recipe(recipes, "cad_assembly_flashmetal", shaped("equipment",
                List.of("HHH", "FFF", " CF"),
                Map.of(
                        'C', ingredientItem(item("psionic_control_circuit")),
                        'F', ingredientItem(item("flashmetal")),
                        'H', ingredientItem("mekanism:hdpe_sheet")
                ),
                item("cad_assembly_flashmetal"), 1));
        recipe(recipes, "cad_assembly_heavy_psimetal_alpha", shaped("equipment",
                List.of("SNS", "CIC", "SCS"),
                Map.of(
                        'C', ingredientItem(item("echo_control_circuit")),
                        'I', ingredientItem(item("incomplete_heavy_psimetal_assembly")),
                        'N', ingredientItem("minecraft:nether_star"),
                        'S', ingredientItem(item("echo_sheet"))
                ),
                item("cad_assembly_heavy_psimetal_alpha"), 1));
        recipe(recipes, "cad_assembly_heavy_psimetal_beta", shaped("equipment",
                List.of("SCS", "PIP", "SCS"),
                Map.of(
                        'C', ingredientItem(item("echo_control_circuit")),
                        'I', ingredientItem(item("incomplete_heavy_psimetal_assembly")),
                        'P', ingredientItem("mekanism:pellet_plutonium"),
                        'S', ingredientItem(item("echo_sheet"))
                ),
                item("cad_assembly_heavy_psimetal_beta"), 1));
        recipe(recipes, "cad_assembly_psycheonic_metal_from_alpha", shaped("equipment",
                List.of("PCP", "AHA", "PCP"),
                Map.of(
                        'A', ingredientItem(item("pellet_americium")),
                        'C', ingredientItem(item("hypostasis_control_circuit")),
                        'H', ingredientItem(item("cad_assembly_heavy_psimetal_alpha")),
                        'P', ingredientItem(item("psycheonic_metal_ingot"))
                ),
                item("cad_assembly_psycheonic_metal"), 1));
        recipe(recipes, "cad_assembly_psycheonic_metal_from_beta", shaped("equipment",
                List.of("PCP", "AHA", "PCP"),
                Map.of(
                        'A', ingredientItem(item("pellet_americium")),
                        'C', ingredientItem(item("hypostasis_control_circuit")),
                        'H', ingredientItem(item("cad_assembly_heavy_psimetal_beta")),
                        'P', ingredientItem(item("psycheonic_metal_ingot"))
                ),
                item("cad_assembly_psycheonic_metal"), 1));
        recipe(recipes, "unrefined_flashmetal", shaped(
                List.of("BBB", "AAA", "BBB"),
                Map.of(
                        'A', ingredientItem(item("chaotic_psimetal")),
                        'B', ingredientItem("mekanism:ingot_refined_glowstone")
                ),
                item("unrefined_flashmetal"), 1));
        recipe(recipes, "heavy_psimetal", shapeless(
                List.of(
                        ingredientItem(item("heavy_psimetal_scrap")),
                        ingredientItem(item("heavy_psimetal_scrap")),
                        ingredientItem(item("heavy_psimetal_scrap")),
                        ingredientItem(item("heavy_psimetal_scrap")),
                        ingredientItem(item("flashmetal")),
                        ingredientItem(item("flashmetal")),
                        ingredientItem(item("flashmetal")),
                        ingredientItem(item("flashmetal"))
                ),
                item("heavy_psimetal"), 1));
        recipe(recipes, "psycheonic_metal_ingot", shaped(
                List.of("NNN", "NNN", "NNN"),
                Map.of('N', ingredientItem(item("psycheonic_metal_nugget"))),
                item("psycheonic_metal_ingot"), 1));
        recipe(recipes, "psycheonic_metal_nugget_from_ingot", shapeless(
                List.of(ingredientItem(item("psycheonic_metal_ingot"))),
                item("psycheonic_metal_nugget"), 9));
        recipe(recipes, "amethyst_shard_from_block", shapeless(
                List.of(ingredientItem("minecraft:amethyst_block")),
                "minecraft:amethyst_shard", 4));
        recipe(recipes, "antinite_ingot_smelting", cooking(
                "minecraft:smelting",
                ingredientItem(item("raw_antinite")),
                item("antinite_ingot"), 1.2D, 200));
        recipe(recipes, "antinite_ingot_blasting", cooking(
                "minecraft:blasting",
                ingredientItem(item("raw_antinite")),
                item("antinite_ingot"), 1.2D, 100));
        recipe(recipes, "antinite_ingot_from_dust_smelting", cooking(
                "minecraft:smelting",
                ingredientItem(item("antinite_dust")),
                item("antinite_ingot"), 0.3D, 200));
        recipe(recipes, "antinite_ingot_from_dust_blasting", cooking(
                "minecraft:blasting",
                ingredientItem(item("antinite_dust")),
                item("antinite_ingot"), 0.3D, 100));
        recipe(recipes, "cad_disassembler", shaped(
                List.of("PPP", "PAP", "PPP"),
                Map.of(
                        'P', ingredientItem("psi:psimetal"),
                        'A', ingredientItem("minecraft:anvil")
                ),
                block("cad_disassembler"), 1));
        recipe(recipes, "program_researcher", shaped(
                List.of("PCP", "CGC", "PCP"),
                Map.of(
                        'P', ingredientItem("psi:psimetal"),
                        'C', ingredientItem(item("psionic_control_circuit")),
                        'G', ingredientItem("psi:psigem")
                ),
                block("program_researcher"), 1));
        recipe(recipes, "material_mutator", shaped(
                List.of("APA", "CDC", "ABA"),
                Map.of(
                        'A', ingredientItem(item("alloy_hypostasis")),
                        'B', ingredientItem(item("hypostasis_control_circuit")),
                        'C', ingredientItem(item("antinite_ingot")),
                        'D', ingredientItem(block("spellmachinery_casing")),
                        'P', ingredientItem(item("program_material_mutation"))
                ),
                block("material_mutator"), 1));
        recipe(recipes, "psionic_generator", shaped(
                List.of(" B ", "ACA", "ATA"),
                Map.of(
                        'A', ingredientItem("psi:psimetal"),
                        'B', ingredientItem("minecraft:ender_pearl"),
                        'C', ingredientItem("mekanism:basic_control_circuit"),
                        'T', ingredientItem("mekanism:energy_tablet")
                ),
                block("psionic_generator"), 1));
        recipe(recipes, "program_blank", shaped(
                List.of("D", "P", "M"),
                Map.of(
                        'D', ingredientItem("psi:psidust"),
                        'P', ingredientItem("minecraft:paper"),
                        'M', ingredientItem("psi:psimetal")
                ),
                item("program_blank"), 1));
        recipe(recipes, "psimetal_bow", shaped("combat",
                List.of(" MS", "G S", " MS"),
                Map.of(
                        'M', ingredientItem("psi:psimetal"),
                        'G', ingredientItem("psi:psigem"),
                        'S', ingredientItem("minecraft:string")
                ),
                item("psimetal_bow"), 1));
        recipe(recipes, "auto_caster_tick", shaped("combat",
                List.of("ECE", "EFE", "EEE"),
                Map.of(
                        'E', ingredientItem("psi:ebony_psimetal"),
                        'F', ingredientItem(item("chaotic_factor")),
                        'C', ingredientItem("minecraft:clock")
                ),
                item("auto_caster_tick"), 1));
        recipe(recipes, "auto_caster_custom_tick", shaped("combat",
                List.of("ECE", "EFE", "EEE"),
                Map.of(
                        'E', ingredientItem(item("chaotic_psimetal")),
                        'F', ingredientItem(item("chaotic_factor")),
                        'C', ingredientItem("minecraft:clock")
                ),
                item("auto_caster_custom_tick"), 1));
        recipe(recipes, "moval_suit_helmet", shaped("equipment",
                List.of("EHE", "E E"),
                Map.of(
                        'E', ingredientItem("psi:ebony_psimetal"),
                        'H', ingredientItem(item("heavy_psimetal"))
                ),
                item("moval_suit_helmet"), 1));
        recipe(recipes, "moval_suit_chestplate", shaped("equipment",
                List.of("E E", "EHE", "EEE"),
                Map.of(
                        'E', ingredientItem("psi:ebony_psimetal"),
                        'H', ingredientItem(item("heavy_psimetal"))
                ),
                item("moval_suit_chestplate"), 1));
        recipe(recipes, "moval_suit_leggings", shaped("equipment",
                List.of("EHE", "E E", "E E"),
                Map.of(
                        'E', ingredientItem("psi:ebony_psimetal"),
                        'H', ingredientItem(item("heavy_psimetal"))
                ),
                item("moval_suit_leggings"), 1));
        recipe(recipes, "moval_suit_boots", shaped("equipment",
                List.of("E E", "EHE"),
                Map.of(
                        'E', ingredientItem("psi:ebony_psimetal"),
                        'H', ingredientItem(item("heavy_psimetal"))
                ),
                item("moval_suit_boots"), 1));
        recipe(recipes, "module_psyon_supplying_unit", shaped("equipment",
                List.of("AEA", "AMA", "SSS"),
                Map.of(
                        'A', ingredientItem(item("alloy_psion")),
                        'E', ingredientItem(item("chaotic_factor")),
                        'M', ingredientItem("mekanism:module_base"),
                        'S', ingredientItem(item("echo_sheet"))
                ),
                item("module_psyon_supplying_unit"), 1));
        recipe(recipes, "module_psyon_capacity_unit", shaped("equipment",
                List.of("AEA", "AMA", "SSS"),
                Map.of(
                        'A', ingredientItem(item("alloy_psion")),
                        'E', ingredientItem(item("antinite_ingot")),
                        'M', ingredientItem("mekanism:module_base"),
                        'S', ingredientItem(item("echo_sheet"))
                ),
                item("module_psyon_capacity_unit"), 1));
        recipe(recipes, "module_phenomenon_interference_enhancement_unit", shaped("equipment",
                List.of("AEA", "AMA", "NNN"),
                Map.of(
                        'A', ingredientItem(item("alloy_hypostasis")),
                        'E', ingredientItem(item("magicians_brain")),
                        'M', ingredientItem("mekanism:module_base"),
                        'N', ingredientItem(item("pellet_neptunium"))
                ),
                item("module_phenomenon_interference_enhancement_unit"), 1));
        recipe(recipes, "inline_caster", shaped("equipment",
                List.of(" G ", "PBP", " P "),
                Map.of(
                        'B', ingredientItem("psi:cad_assembly_psimetal"),
                        'G', ingredientItem("psi:psigem"),
                        'P', ingredientItem("psi:psimetal")
                ),
                item("inline_caster"), 1));
        recipe(recipes, "secondary_caster", shaped("equipment",
                List.of(" F ", "CBC", " T "),
                Map.of(
                        'B', ingredientItem("psi:cad_assembly_psimetal"),
                        'C', ingredientItem(item("psionic_control_circuit")),
                        'F', ingredientItem(item("flashmetal")),
                        'T', ingredientItem("psi:cad_socket_signaling")
                ),
                item("secondary_caster"), 1));
        recipe(recipes, "parallel_caster", shaped("equipment",
                List.of("FHF", "CBC", "FTF"),
                Map.of(
                        'B', ingredientItem("psi:cad_assembly_psimetal"),
                        'C', ingredientItem(item("echo_control_circuit")),
                        'F', ingredientItem(item("flashmetal")),
                        'H', ingredientItem(item("heavy_psimetal")),
                        'T', ingredientItem("psi:cad_socket_transmissive")
                ),
                item("parallel_caster"), 1));
        recipe(recipes, "antinite_block", shaped("building",
                List.of("III", "III", "III"),
                Map.of('I', ingredientItem(item("antinite_ingot"))),
                block("antinite_block"), 1));
        recipe(recipes, "antinite_ingot_from_block", shapeless(
                List.of(ingredientItem(block("antinite_block"))),
                item("antinite_ingot"), 9));
        recipe(recipes, "chaotic_psimetal_block", shaped("building",
                List.of("III", "III", "III"),
                Map.of('I', ingredientItem(item("chaotic_psimetal"))),
                block("chaotic_psimetal_block"), 1));
        recipe(recipes, "chaotic_psimetal_from_block", shapeless(
                List.of(ingredientItem(block("chaotic_psimetal_block"))),
                item("chaotic_psimetal"), 9));
        recipe(recipes, "flashmetal_block", shaped("building",
                List.of("III", "III", "III"),
                Map.of('I', ingredientItem(item("flashmetal"))),
                block("flashmetal_block"), 1));
        recipe(recipes, "flashmetal_from_block", shapeless(
                List.of(ingredientItem(block("flashmetal_block"))),
                item("flashmetal"), 9));
        recipe(recipes, "heavy_psimetal_block", shaped("building",
                List.of("III", "III", "III"),
                Map.of('I', ingredientItem(item("heavy_psimetal"))),
                block("heavy_psimetal_block"), 1));
        recipe(recipes, "heavy_psimetal_from_block", shapeless(
                List.of(ingredientItem(block("heavy_psimetal_block"))),
                item("heavy_psimetal"), 9));
        recipe(recipes, "plutonium_block", shaped("building",
                List.of("PPP", "PPP", "PPP"),
                Map.of('P', ingredientItem("mekanism:pellet_plutonium")),
                block("plutonium_block"), 1));
        recipe(recipes, "plutonium_pellet_from_block", shapeless(
                List.of(ingredientItem(block("plutonium_block"))),
                "mekanism:pellet_plutonium", 9));
        recipe(recipes, "polonium_block", shaped("building",
                List.of("PPP", "PPP", "PPP"),
                Map.of('P', ingredientItem("mekanism:pellet_polonium")),
                block("polonium_block"), 1));
        recipe(recipes, "polonium_pellet_from_block", shapeless(
                List.of(ingredientItem(block("polonium_block"))),
                "mekanism:pellet_polonium", 9));
        recipe(recipes, "raw_antinite_block", shaped("building",
                List.of("RRR", "RRR", "RRR"),
                Map.of('R', ingredientItem(item("raw_antinite"))),
                block("raw_antinite_block"), 1));
        recipe(recipes, "raw_antinite_from_block", shapeless(
                List.of(ingredientItem(block("raw_antinite_block"))),
                item("raw_antinite"), 9));
        recipe(recipes, "spellmachinery_casing", shaped("building",
                List.of("AHA", "HPH", "AHA"),
                Map.of(
                        'A', ingredientItem(item("alloy_hypostasis")),
                        'H', ingredientItem(item("magatama")),
                        'P', ingredientItem(item("psycheonic_metal_ingot"))
                ),
                block("spellmachinery_casing"), 1));
        recipe(recipes, "philosophers_stone", shaped(
                List.of("AEA", "ENE", "AEA"),
                Map.of(
                        'A', ingredientItem(item("antinite_ingot")),
                        'E', ingredientItem(item("alloy_psionic_echo")),
                        'N', ingredientItem("minecraft:nether_star")
                ),
                item("philosophers_stone"), 1));
        recipe(recipes, "portable_cad_assembler", shaped(
                List.of("CGC", "EAE", "CGC"),
                Map.of(
                        'C', ingredientItem(item("chaotic_psimetal")),
                        'G', ingredientItem("psi:psigem"),
                        'E', ingredientItem("minecraft:ender_pearl"),
                        'A', ingredientItem("psi:cad_assembler")
                ),
                item("portable_cad_assembler"), 1));
        recipe(recipes, "spell_magazine", shaped("tools",
                List.of("PBH"),
                Map.of(
                        'P', ingredientItem(item("chaotic_psimetal")),
                        'B', ingredientItem(item("flashmetal")),
                        'H', ingredientItem("psi:cad_socket_huge")
                ),
                item("spell_magazine"), 1));
        recipe(recipes, "curios_controller", shapeless("tools",
                List.of(
                        ingredientItem("psi:exosuit_controller"),
                        ingredientItem(item("chaotic_psimetal"))
                ),
                item("curios_controller"), 1));
        recipe(recipes, "flash_charm", shaped("combat",
                List.of(" F ", "FNF", " F "),
                Map.of(
                        'F', ingredientItem(item("flashmetal")),
                        'N', ingredientItem("minecraft:nether_wart")
                ),
                item("flash_charm"), 1));
        recipe(recipes, "sorcery_booster", shaped("tools",
                List.of("CDC", "BBB", "CCC"),
                Map.of(
                        'B', ingredientItem(item("magicians_brain")),
                        'C', ingredientItem(item("flashmetal")),
                        'D', ingredientItem(item("heavy_psimetal"))
                ),
                item("sorcery_booster"), 1));

        addSpellBulletRecipes(recipes);
        addSculkEroderRecipes(recipes);
        addProgramResearchRecipes(recipes);
        addPhilosophersStoneRecipes(recipes);
        recipe(recipes, "program_duplication", specialCrafting("psitweaks:crafting_special_program_duplication"));

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
                new SpellBulletVariant("", "psi:spell_bullet"),
                new SpellBulletVariant("_loop", "psi:spell_bullet_loop"),
                new SpellBulletVariant("_mine", "psi:spell_bullet_mine"),
                new SpellBulletVariant("_charge", "psi:spell_bullet_charge"),
                new SpellBulletVariant("_grenade", "psi:spell_bullet_grenade"),
                new SpellBulletVariant("_projectile", "psi:spell_bullet_projectile"),
                new SpellBulletVariant("_circle", "psi:spell_bullet_circle")
        );
        List<SpellBulletRecipeTier> tiers = List.of(
                new SpellBulletRecipeTier("advanced", "psi:psimetal", item("psionic_control_circuit")),
                new SpellBulletRecipeTier("resonant", item("chaotic_psimetal"), item("psionic_control_circuit")),
                new SpellBulletRecipeTier("sublimated", item("flashmetal"), item("psionic_control_circuit")),
                new SpellBulletRecipeTier("awakened", item("heavy_psimetal"), item("echo_control_circuit")),
                new SpellBulletRecipeTier("transcendent", item("psycheonic_metal_ingot"), item("echo_control_circuit"))
        );

        for (SpellBulletVariant variant : variants) {
            String previousItem = variant.psiItem();
            for (SpellBulletRecipeTier tier : tiers) {
                String id = tier.id() + "_spell_bullet" + variant.suffix();
                String result = item(id);

                recipe(recipes, id, shaped("equipment",
                        List.of("ABA", "BCB", "ABA"),
                        Map.of(
                                'A', ingredientItem(tier.material()),
                                'B', ingredientItem(previousItem),
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

    private static void addProgramResearchRecipes(Map<ResourceLocation, JsonObject> recipes) {
        programResearch(recipes, "program_cocytus", List.of(
                researchInput(item("program_blank"), 1),
                researchInput("minecraft:heart_of_the_sea", 1),
                researchInput("minecraft:blue_ice", 64),
                researchInput(item("psionic_echo"), 32),
                researchInput("minecraft:sculk_shrieker", 8)
        ), item("program_cocytus"), 500_000_000, 36_000);
        programResearch(recipes, "program_time_accelerate", List.of(
                researchInput(item("program_blank"), 1),
                researchInput("minecraft:clock", 8),
                researchInput("minecraft:redstone_block", 8),
                researchInput(item("chaotic_psimetal"), 8),
                researchInput("minecraft:powered_rail", 32)
        ), item("program_time_accelerate"), 10_000_000, 1_200);
        programResearch(recipes, "program_flight", List.of(
                researchInput(item("program_blank"), 1),
                researchInput("minecraft:phantom_membrane", 2),
                researchInput("minecraft:feather", 32),
                researchInput(item("chaotic_factor"), 8),
                researchInput("minecraft:nether_wart", 4)
        ), item("program_flight"), 10_000_000, 600);
        programResearch(recipes, "program_phonon_maser", List.of(
                researchInput(item("program_blank"), 1),
                researchInput("mekanism:laser", 1),
                researchInput("minecraft:amethyst_shard", 12),
                researchInput(item("flashmetal"), 8),
                researchInput("minecraft:note_block", 48)
        ), item("program_phonon_maser"), 40_000_000, 2_400);
        programResearch(recipes, "program_supreme_infusion", List.of(
                researchInput(item("program_blank"), 1),
                researchInput("minecraft:netherite_ingot", 1),
                researchInput(item("flashmetal"), 8),
                researchInput(item("alloy_psion"), 24),
                researchInput("minecraft:amethyst_block", 16)
        ), item("program_supreme_infusion"), 40_000_000, 2_400);
        programResearch(recipes, "program_molecular_divider", List.of(
                researchInput(item("program_blank"), 1),
                researchInput("psi:psigem", 32),
                researchInput("minecraft:quartz", 48),
                researchInput(item("heavy_psimetal"), 8),
                researchInput(item("echo_control_circuit"), 3)
        ), item("program_molecular_divider"), 80_000_000, 6_000);
        programResearch(recipes, "program_radiation_injection", List.of(
                researchInput(item("program_blank"), 1),
                researchInput("mekanism:ingot_uranium", 16),
                researchInput("mekanism:ingot_lead", 8),
                researchInput(item("chaotic_psimetal"), 8)
        ), item("program_radiation_injection"), 60_000_000, 3_600);
        programResearch(recipes, "program_radiation_filter", List.of(
                researchInput(item("program_blank"), 1),
                researchInput("mekanism:ingot_lead", 16),
                researchInput("mekanism:dust_fluorite", 16),
                researchInput(item("chaotic_psimetal"), 8)
        ), item("program_radiation_filter"), 60_000_000, 3_600);
        programResearch(recipes, "program_cure_radiation", List.of(
                researchInput(item("program_blank"), 1),
                researchInput("minecraft:golden_apple", 8),
                researchInput("mekanism:ingot_lead", 16),
                researchInput(item("chaotic_psimetal"), 8)
        ), item("program_cure_radiation"), 60_000_000, 3_600);
        programResearch(recipes, "program_guillotine", List.of(
                researchInput(item("program_blank"), 1),
                researchInput("minecraft:wither_skeleton_skull", 1),
                researchInput("minecraft:anvil", 3),
                researchInput("minecraft:rotten_flesh", 24),
                researchInput("minecraft:bone", 16),
                researchInput("psi:psimetal_sword", 1)
        ), item("program_guillotine"), 50_000_000, 3_000);
        programResearch(recipes, "program_active_air_mine", List.of(
                researchInput(item("program_blank"), 1),
                researchInput("minecraft:tnt", 8),
                researchInput("psi:psidust", 16),
                researchInput("psi:psigem", 2)
        ), item("program_active_air_mine"), 4_000_000, 400);
        programResearch(recipes, "program_die_flex", List.of(
                researchInput(item("program_blank"), 1),
                researchInput("minecraft:comparator", 4),
                researchInput("minecraft:redstone", 32),
                researchInput("psi:psidust", 24)
        ), item("program_die_flex"), 6_000_000, 600);
        programResearch(recipes, "program_material_mutation", List.of(
                researchInput(item("program_blank"), 1),
                researchCatalyst(item("philosophers_stone"), 1),
                researchInput("minecraft:sculk_catalyst", 1),
                researchInput(item("chaotic_factor"), 16),
                researchInput(item("antinite_ingot"), 32),
                researchInput(item("psionic_echo"), 16)
        ), item("program_material_mutation"), 250_000_000, 24_000);
    }

    private static void addPhilosophersStoneRecipes(Map<ResourceLocation, JsonObject> recipes) {
        recipe(recipes, "philosophers_stone/philosophers_stone_copper_to_iron", shapeless(
                List.of(ingredientItem(item("philosophers_stone")), ingredientTag("c:ingots/copper"),
                        ingredientTag("c:ingots/copper"), ingredientTag("c:ingots/copper"), ingredientTag("c:ingots/copper")),
                "minecraft:iron_ingot", 2));
        recipe(recipes, "philosophers_stone/philosophers_stone_iron_to_copper", shapeless(
                List.of(ingredientItem(item("philosophers_stone")), ingredientTag("c:ingots/iron"), ingredientTag("c:ingots/iron")),
                "minecraft:copper_ingot", 4));
        recipe(recipes, "philosophers_stone/philosophers_stone_iron_to_osmium", shapeless(
                List.of(ingredientItem(item("philosophers_stone")), ingredientTag("c:ingots/iron"), ingredientTag("c:ingots/iron"),
                        ingredientTag("c:ingots/iron"), ingredientTag("c:ingots/iron")),
                "mekanism:ingot_osmium", 2));
        recipe(recipes, "philosophers_stone/philosophers_stone_osmium_to_iron", shapeless(
                List.of(ingredientItem(item("philosophers_stone")), ingredientTag("c:ingots/osmium"), ingredientTag("c:ingots/osmium")),
                "minecraft:iron_ingot", 4));
        recipe(recipes, "philosophers_stone/philosophers_stone_osmium_to_gold", shapeless(
                List.of(ingredientItem(item("philosophers_stone")), ingredientTag("c:ingots/osmium"), ingredientTag("c:ingots/osmium"),
                        ingredientTag("c:ingots/osmium"), ingredientTag("c:ingots/osmium")),
                "minecraft:gold_ingot", 2));
        recipe(recipes, "philosophers_stone/philosophers_stone_gold_to_osmium", shapeless(
                List.of(ingredientItem(item("philosophers_stone")), ingredientTag("c:ingots/gold"), ingredientTag("c:ingots/gold")),
                "mekanism:ingot_osmium", 4));
        recipe(recipes, "philosophers_stone/philosophers_stone_gold_to_uranium", shapeless(
                List.of(ingredientItem(item("philosophers_stone")), ingredientTag("c:ingots/gold"), ingredientTag("c:ingots/gold"),
                        ingredientTag("c:ingots/gold"), ingredientTag("c:ingots/gold")),
                "mekanism:ingot_uranium", 2));
        recipe(recipes, "philosophers_stone/philosophers_stone_uranium_to_gold", shapeless(
                List.of(ingredientItem(item("philosophers_stone")), ingredientTag("c:ingots/uranium"), ingredientTag("c:ingots/uranium")),
                "minecraft:gold_ingot", 4));
        recipe(recipes, "philosophers_stone/philosophers_stone_iron_to_tin", shapeless(
                List.of(ingredientItem(item("philosophers_stone")), ingredientTag("c:ingots/iron"), ingredientTag("c:ingots/iron"),
                        ingredientTag("c:ingots/iron"), ingredientTag("c:ingots/iron"), ingredientTag("c:ingots/iron")),
                "mekanism:ingot_tin", 5));
        recipe(recipes, "philosophers_stone/philosophers_stone_tin_to_iron", shapeless(
                List.of(ingredientItem(item("philosophers_stone")), ingredientTag("c:ingots/tin"), ingredientTag("c:ingots/tin"),
                        ingredientTag("c:ingots/tin"), ingredientTag("c:ingots/tin"), ingredientTag("c:ingots/tin")),
                "minecraft:iron_ingot", 5));
        recipe(recipes, "philosophers_stone/philosophers_stone_osmium_to_lead", shapeless(
                List.of(ingredientItem(item("philosophers_stone")), ingredientTag("c:ingots/osmium"), ingredientTag("c:ingots/osmium"),
                        ingredientTag("c:ingots/osmium"), ingredientTag("c:ingots/osmium"), ingredientTag("c:ingots/osmium")),
                "mekanism:ingot_lead", 5));
        recipe(recipes, "philosophers_stone/philosophers_stone_lead_to_osmium", shapeless(
                List.of(ingredientItem(item("philosophers_stone")), ingredientTag("c:ingots/lead"), ingredientTag("c:ingots/lead"),
                        ingredientTag("c:ingots/lead"), ingredientTag("c:ingots/lead"), ingredientTag("c:ingots/lead")),
                "mekanism:ingot_osmium", 5));
        recipe(recipes, "philosophers_stone/philosophers_stone_coal_to_diamond", shapeless(
                List.of(ingredientItem(item("philosophers_stone")), ingredientItem("minecraft:coal_block"),
                        ingredientItem("minecraft:coal"), ingredientItem("minecraft:coal"), ingredientItem("minecraft:coal"),
                        ingredientItem("minecraft:coal"), ingredientItem("minecraft:coal"), ingredientItem("minecraft:coal"),
                        ingredientItem("minecraft:coal")),
                "minecraft:diamond", 1));
        recipe(recipes, "philosophers_stone/philosophers_stone_charcoal_to_diamond", shapeless(
                List.of(ingredientItem(item("philosophers_stone")), ingredientItem("mekanism:block_charcoal"),
                        ingredientItem("minecraft:charcoal"), ingredientItem("minecraft:charcoal"), ingredientItem("minecraft:charcoal"),
                        ingredientItem("minecraft:charcoal"), ingredientItem("minecraft:charcoal"), ingredientItem("minecraft:charcoal"),
                        ingredientItem("minecraft:charcoal")),
                "minecraft:diamond", 1));
        recipe(recipes, "philosophers_stone/philosophers_stone_diamond_to_coal", shapeless(
                List.of(ingredientItem(item("philosophers_stone")), ingredientItem("minecraft:diamond")),
                "minecraft:coal", 16));
        recipe(recipes, "philosophers_stone/philosophers_stone_ender_pearl_to_diamond", shapeless(
                List.of(ingredientItem(item("philosophers_stone")), ingredientItem("minecraft:ender_pearl"), ingredientItem("minecraft:ender_pearl"),
                        ingredientItem("minecraft:ender_pearl"), ingredientItem("minecraft:ender_pearl")),
                "minecraft:diamond", 2));
        recipe(recipes, "philosophers_stone/philosophers_stone_diamond_to_ender_pearl", shapeless(
                List.of(ingredientItem(item("philosophers_stone")), ingredientItem("minecraft:diamond"), ingredientItem("minecraft:diamond")),
                "minecraft:ender_pearl", 4));
        recipe(recipes, "philosophers_stone/philosophers_stone_blaze_rod_to_ender_pearl", shapeless(
                List.of(ingredientItem(item("philosophers_stone")), ingredientItem("minecraft:blaze_rod"), ingredientItem("minecraft:blaze_rod"),
                        ingredientItem("minecraft:blaze_rod"), ingredientItem("minecraft:blaze_rod")),
                "minecraft:ender_pearl", 2));
        recipe(recipes, "philosophers_stone/philosophers_stone_ender_pearl_to_blaze_rod", shapeless(
                List.of(ingredientItem(item("philosophers_stone")), ingredientItem("minecraft:ender_pearl"), ingredientItem("minecraft:ender_pearl")),
                "minecraft:blaze_rod", 4));
        recipe(recipes, "philosophers_stone/philosophers_stone_diamond_to_emerald", shapeless(
                List.of(ingredientItem(item("philosophers_stone")), ingredientItem("minecraft:diamond"), ingredientItem("minecraft:diamond"),
                        ingredientItem("minecraft:diamond"), ingredientItem("minecraft:diamond"), ingredientItem("minecraft:diamond")),
                "minecraft:emerald", 5));
        recipe(recipes, "philosophers_stone/philosophers_stone_emerald_to_diamond", shapeless(
                List.of(ingredientItem(item("philosophers_stone")), ingredientItem("minecraft:emerald"), ingredientItem("minecraft:emerald"),
                        ingredientItem("minecraft:emerald"), ingredientItem("minecraft:emerald"), ingredientItem("minecraft:emerald")),
                "minecraft:diamond", 5));
        recipe(recipes, "philosophers_stone/philosophers_stone_coal_to_quartz", shapeless(
                List.of(ingredientItem(item("philosophers_stone")), ingredientItem("minecraft:coal"), ingredientItem("minecraft:coal"),
                        ingredientItem("minecraft:coal"), ingredientItem("minecraft:coal"), ingredientItem("minecraft:coal")),
                "minecraft:quartz", 5));
        recipe(recipes, "philosophers_stone/philosophers_stone_quartz_to_coal", shapeless(
                List.of(ingredientItem(item("philosophers_stone")), ingredientItem("minecraft:quartz"), ingredientItem("minecraft:quartz"),
                        ingredientItem("minecraft:quartz"), ingredientItem("minecraft:quartz"), ingredientItem("minecraft:quartz")),
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

    private static void programResearch(Map<ResourceLocation, JsonObject> recipes, String id, List<JsonObject> inputs,
                                        String output, int energy, int time) {
        recipe(recipes, "program_research/" + id, programResearch(inputs, output, energy, time));
    }

    private static JsonObject programResearch(List<JsonObject> inputs, String output, int energy, int time) {
        JsonObject root = new JsonObject();
        JsonArray inputArray = new JsonArray();

        root.addProperty("type", "psitweaks:program_research");
        inputs.forEach(inputArray::add);
        root.add("inputs", inputArray);
        root.add("output", result(output, 1));
        root.addProperty("energy", energy);
        root.addProperty("time", time);

        return root;
    }

    private static JsonObject researchInput(String item, int count) {
        return researchInput(item, count, true);
    }

    private static JsonObject researchCatalyst(String item, int count) {
        return researchInput(item, count, false);
    }

    private static JsonObject researchInput(String item, int count, boolean consume) {
        JsonObject input = new JsonObject();
        input.add("ingredient", ingredientItem(item));
        input.addProperty("count", count);
        if (!consume) {
            input.addProperty("consume", false);
        }
        return input;
    }

    private static JsonObject specialCrafting(String type) {
        JsonObject root = new JsonObject();
        root.addProperty("type", type);
        root.addProperty("category", "misc");
        return root;
    }

    private static JsonObject ingredientItem(String item) {
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

    private static String item(String path) {
        return Psitweaks.MOD_ID + ":" + path;
    }

    private static String block(String path) {
        return Psitweaks.MOD_ID + ":" + path;
    }

    private record SpellBulletVariant(String suffix, String psiItem) {
    }

    private record SpellBulletRecipeTier(String id, String material, String circuit) {
    }
}
