package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.moratan251.psitweaks.Psitweaks;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public final class ProgramResearchRecipeProvider {
    private ProgramResearchRecipeProvider() {
    }

    static void addRecipes(Map<ResourceLocation, JsonObject> recipes) {
        research(recipes, "program_cocytus", List.of(
                input(item("program_blank"), 1),
                input("mekanism:pellet_antimatter", 8),
                input("minecraft:blue_ice", 64),
                input("minecraft:sculk_shrieker", 24),
                input("minecraft:heart_of_the_sea", 4),
                input(item("psycheonic_metal_ingot"), 64)
        ), item("program_cocytus"), 1_500_000L, 108_000);

        research(recipes, "program_time_accelerate", List.of(
                input(item("program_blank"), 1),
                input("minecraft:clock", 8),
                input("minecraft:redstone_block", 8),
                input(item("chaotic_psimetal"), 8),
                input("minecraft:powered_rail", 32)
        ), item("program_time_accelerate"), 10_000L, 1_200);

        research(recipes, "program_flight", List.of(
                input(item("program_blank"), 1),
                input("minecraft:phantom_membrane", 2),
                input("minecraft:feather", 32),
                input(item("chaotic_factor"), 8),
                input("minecraft:nether_wart", 4)
        ), item("program_flight"), 10_000L, 600);

        research(recipes, "program_phonon_maser", List.of(
                input(item("program_blank"), 1),
                input("mekanism:laser", 1),
                input("minecraft:amethyst_shard", 12),
                input(item("flashmetal"), 8),
                input("minecraft:note_block", 48)
        ), item("program_phonon_maser"), 40_000L, 2_400);

        research(recipes, "program_meteor_line", List.of(
                input(item("program_blank"), 1),
                input("minecraft:nether_star", 24),
                input("mekanism:pellet_antimatter", 16),
                input(item("alloy_hypostasis"), 48),
                input(item("psycheonic_metal_ingot"), 64),
                input(item("psycheonic_metal_ingot"), 64)
        ), item("program_meteor_line"), 1_500_000L, 288_000);

        research(recipes, "program_supreme_infusion", List.of(
                input(item("program_blank"), 1),
                input("minecraft:netherite_ingot", 1),
                input(item("flashmetal"), 8),
                input(item("alloy_psion"), 24),
                input("minecraft:amethyst_block", 16)
        ), item("program_supreme_infusion"), 40_000L, 2_400);

        research(recipes, "program_molecular_divider", List.of(
                input(item("program_blank"), 1),
                input("psi:psigem", 32),
                input("minecraft:quartz", 48),
                input(item("heavy_psimetal"), 8),
                input(item("echo_control_circuit"), 3)
        ), item("program_molecular_divider"), 80_000L, 6_000);

        research(recipes, "program_radiation_injection", List.of(
                input(item("program_blank"), 1),
                input("mekanism:ingot_uranium", 16),
                input("mekanism:ingot_lead", 8),
                input(item("chaotic_psimetal"), 8)
        ), item("program_radiation_injection"), 60_000L, 3_600);

        research(recipes, "program_radiation_filter", List.of(
                input(item("program_blank"), 1),
                input("mekanism:ingot_lead", 16),
                input("mekanism:dust_fluorite", 16),
                input(item("chaotic_psimetal"), 8)
        ), item("program_radiation_filter"), 60_000L, 3_600);

        research(recipes, "program_cure_radiation", List.of(
                input(item("program_blank"), 1),
                input("minecraft:golden_apple", 8),
                input("mekanism:ingot_lead", 16),
                input(item("chaotic_psimetal"), 8)
        ), item("program_cure_radiation"), 60_000L, 3_600);

        research(recipes, "program_guillotine", List.of(
                input(item("program_blank"), 1),
                input("minecraft:wither_skeleton_skull", 1),
                input("minecraft:anvil", 3),
                input("minecraft:rotten_flesh", 24),
                input("minecraft:bone", 16),
                input("psi:psimetal_sword", 1)
        ), item("program_guillotine"), 50_000L, 3_000);

        research(recipes, "program_active_air_mine", List.of(
                input(item("program_blank"), 1),
                input("minecraft:tnt", 8),
                input("psi:psidust", 16),
                input("psi:psigem", 2)
        ), item("program_active_air_mine"), 4_000L, 400);

        research(recipes, "program_die_flex", List.of(
                input(item("program_blank"), 1),
                input(item("psionic_control_circuit"), 16),
                input(item("chaotic_psimetal"), 16),
                input(item("chaotic_factor"), 16)
        ), item("program_die_flex"), 6_000L, 600);

        research(recipes, "program_jump_flex", List.of(
                input(item("program_die_flex"), 1),
                input(item("echo_control_circuit"), 16),
                input(item("flashmetal"), 24),
                input("mekanism:pellet_polonium", 4)
        ), item("program_jump_flex"), 100_000L, 54_000);

        research(recipes, "program_switch_flex", List.of(
                input(item("program_jump_flex"), 1),
                input(item("hypostasis_control_circuit"), 16),
                input(item("heavy_psimetal"), 32),
                input(item("pellet_americium"), 8)
        ), item("program_switch_flex"), 500_000L, 90_000);

        research(recipes, "program_material_mutation", List.of(
                input(item("program_blank"), 1),
                catalyst(item("philosophers_stone"), 1),
                input("minecraft:sculk_catalyst", 1),
                input(item("chaotic_factor"), 16),
                input(item("antinite_ingot"), 32),
                input(item("psionic_echo"), 16)
        ), item("program_material_mutation"), 250_000L, 24_000);

        research(recipes, "program_mass_block_break", List.of(
                input(item("program_blank"), 1),
                input("psi:psigem", 16),
                input(item("chaotic_psimetal"), 16),
                input("minecraft:tnt", 16)
        ), item("program_mass_block_break"), 10_000L, 1_200);
    }

    private static void research(Map<ResourceLocation, JsonObject> recipes, String id, List<JsonObject> inputs,
                                  String output, long energyPerTick, int time) {
        recipes.put(Psitweaks.location("program_research/" + id), research(inputs, output, energyPerTick, time));
    }

    private static JsonObject research(List<JsonObject> inputs, String output, long energyPerTick, int time) {
        JsonObject root = new JsonObject();
        JsonArray inputArray = new JsonArray();

        root.addProperty("type", "psitweaks:program_research");
        inputs.forEach(inputArray::add);
        root.add("inputs", inputArray);
        root.add("output", result(output));
        root.addProperty("energy_per_tick", energyPerTick);
        root.addProperty("time", time);

        return root;
    }

    private static JsonObject input(String item, int count) {
        return input(item, count, true);
    }

    private static JsonObject catalyst(String item, int count) {
        return input(item, count, false);
    }

    private static JsonObject input(String item, int count, boolean consume) {
        JsonObject input = new JsonObject();
        input.add("ingredient", ingredient(item));
        input.addProperty("count", count);
        if (!consume) {
            input.addProperty("consume", false);
        }
        return input;
    }

    private static JsonObject ingredient(String item) {
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", item);
        return ingredient;
    }

    private static JsonObject result(String item) {
        JsonObject result = new JsonObject();
        result.addProperty("id", item);
        result.addProperty("count", 1);
        return result;
    }

    private static String item(String id) {
        return Psitweaks.location(id).toString();
    }
}
