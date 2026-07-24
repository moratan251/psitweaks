package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import vazkii.psi.common.item.base.ModItems;

public final class ProgramResearchRecipeProvider {
    private ProgramResearchRecipeProvider() {
    }

    static void addRecipes(Map<ResourceLocation, JsonObject> recipes) {
        research(recipes, "program_cocytus", List.of(
                input(PsitweaksItems.PROGRAM_BLANK, 1),
                inputId(mekanism("pellet_antimatter"), 8),
                input(Items.BLUE_ICE, 64),
                input(Items.SCULK_SHRIEKER, 24),
                input(Items.HEART_OF_THE_SEA, 4),
                input(PsitweaksItems.PSYCHEONIC_METAL_INGOT, 64)
        ), PsitweaksItems.PROGRAM_COCYTUS, 1_500_000L, 108_000);

        research(recipes, "program_time_accelerate", List.of(
                input(PsitweaksItems.PROGRAM_BLANK, 1),
                input(Items.CLOCK, 8),
                input(Items.REDSTONE_BLOCK, 8),
                input(PsitweaksItems.CHAOTIC_PSIMETAL, 8),
                input(Items.POWERED_RAIL, 32)
        ), PsitweaksItems.PROGRAM_TIME_ACCELERATE, 10_000L, 1_200);

        research(recipes, "program_flight", List.of(
                input(PsitweaksItems.PROGRAM_BLANK, 1),
                input(Items.PHANTOM_MEMBRANE, 2),
                input(Items.FEATHER, 32),
                input(PsitweaksItems.CHAOTIC_FACTOR, 8),
                input(Items.NETHER_WART, 4)
        ), PsitweaksItems.PROGRAM_FLIGHT, 10_000L, 600);

        research(recipes, "program_phonon_maser", List.of(
                input(PsitweaksItems.PROGRAM_BLANK, 1),
                inputId(mekanism("laser"), 1),
                input(Items.AMETHYST_SHARD, 12),
                input(PsitweaksItems.FLASHMETAL, 8),
                input(Items.NOTE_BLOCK, 48)
        ), PsitweaksItems.PROGRAM_PHONON_MASER, 40_000L, 2_400);

        research(recipes, "program_meteor_line", List.of(
                input(PsitweaksItems.PROGRAM_BLANK, 1),
                input(Items.NETHER_STAR, 24),
                inputId(mekanism("pellet_antimatter"), 16),
                input(PsitweaksItems.ALLOY_HYPOSTASIS, 48),
                input(PsitweaksItems.PSYCHEONIC_METAL_INGOT, 64),
                input(PsitweaksItems.PSYCHEONIC_METAL_INGOT, 64)
        ), PsitweaksItems.PROGRAM_METEOR_LINE, 1_500_000L, 288_000);

        research(recipes, "program_supreme_infusion", List.of(
                input(PsitweaksItems.PROGRAM_BLANK, 1),
                input(Items.NETHERITE_INGOT, 1),
                input(PsitweaksItems.FLASHMETAL, 8),
                input(PsitweaksItems.ALLOY_PSION, 24),
                input(Items.AMETHYST_BLOCK, 16)
        ), PsitweaksItems.PROGRAM_SUPREME_INFUSION, 40_000L, 2_400);

        research(recipes, "program_molecular_divider", List.of(
                input(PsitweaksItems.PROGRAM_BLANK, 1),
                input(ModItems.psigem.get(), 32),
                input(Items.QUARTZ, 48),
                input(PsitweaksItems.HEAVY_PSIMETAL, 8),
                input(PsitweaksItems.ECHO_CONTROL_CIRCUIT, 3)
        ), PsitweaksItems.PROGRAM_MOLECULAR_DIVIDER, 80_000L, 6_000);

        research(recipes, "program_radiation_injection", List.of(
                input(PsitweaksItems.PROGRAM_BLANK, 1),
                inputId(mekanism("ingot_uranium"), 16),
                inputId(mekanism("ingot_lead"), 8),
                input(PsitweaksItems.CHAOTIC_PSIMETAL, 8)
        ), PsitweaksItems.PROGRAM_RADIATION_INJECTION, 60_000L, 3_600);

        research(recipes, "program_radiation_filter", List.of(
                input(PsitweaksItems.PROGRAM_BLANK, 1),
                inputId(mekanism("ingot_lead"), 16),
                inputId(mekanism("dust_fluorite"), 16),
                input(PsitweaksItems.CHAOTIC_PSIMETAL, 8)
        ), PsitweaksItems.PROGRAM_RADIATION_FILTER, 60_000L, 3_600);

        research(recipes, "program_cure_radiation", List.of(
                input(PsitweaksItems.PROGRAM_BLANK, 1),
                input(Items.GOLDEN_APPLE, 8),
                inputId(mekanism("ingot_lead"), 16),
                input(PsitweaksItems.CHAOTIC_PSIMETAL, 8)
        ), PsitweaksItems.PROGRAM_CURE_RADIATION, 60_000L, 3_600);

        research(recipes, "program_guillotine", List.of(
                input(PsitweaksItems.PROGRAM_BLANK, 1),
                input(Items.WITHER_SKELETON_SKULL, 1),
                input(Items.ANVIL, 3),
                input(Items.ROTTEN_FLESH, 24),
                input(Items.BONE, 16),
                input(ModItems.psimetalSword.get(), 1)
        ), PsitweaksItems.PROGRAM_GUILLOTINE, 50_000L, 3_000);

        research(recipes, "program_active_air_mine", List.of(
                input(PsitweaksItems.PROGRAM_BLANK, 1),
                input(Items.TNT, 8),
                input(ModItems.psidust.get(), 16),
                input(ModItems.psigem.get(), 2)
        ), PsitweaksItems.PROGRAM_ACTIVE_AIR_MINE, 4_000L, 400);

        research(recipes, "program_die_flex", List.of(
                input(PsitweaksItems.PROGRAM_BLANK, 1),
                input(PsitweaksItems.PSIONIC_CONTROL_CIRCUIT, 16),
                input(PsitweaksItems.CHAOTIC_PSIMETAL, 16),
                input(PsitweaksItems.CHAOTIC_FACTOR, 16)
        ), PsitweaksItems.PROGRAM_DIE_FLEX, 6_000L, 600);

        research(recipes, "program_jump_flex", List.of(
                input(PsitweaksItems.PROGRAM_DIE_FLEX, 1),
                input(PsitweaksItems.ECHO_CONTROL_CIRCUIT, 16),
                input(PsitweaksItems.FLASHMETAL, 24),
                inputId(mekanism("pellet_polonium"), 4)
        ), PsitweaksItems.PROGRAM_JUMP_FLEX, 100_000L, 54_000);

        research(recipes, "program_switch_flex", List.of(
                input(PsitweaksItems.PROGRAM_JUMP_FLEX, 1),
                input(PsitweaksItems.HYPOSTASIS_CONTROL_CIRCUIT, 16),
                input(PsitweaksItems.HEAVY_PSIMETAL, 32),
                input(PsitweaksItems.PELLET_AMERICIUM, 8)
        ), PsitweaksItems.PROGRAM_SWITCH_FLEX, 500_000L, 90_000);

        research(recipes, "program_material_mutation", List.of(
                input(PsitweaksItems.PROGRAM_BLANK, 1),
                catalyst(PsitweaksItems.PHILOSOPHERS_STONE, 1),
                input(Items.SCULK_CATALYST, 1),
                input(PsitweaksItems.CHAOTIC_FACTOR, 16),
                input(PsitweaksItems.ANTINITE_INGOT, 32),
                input(PsitweaksItems.PSIONIC_ECHO, 16)
        ), PsitweaksItems.PROGRAM_MATERIAL_MUTATION, 250_000L, 24_000);

        research(recipes, "program_mass_block_break", List.of(
                input(PsitweaksItems.PROGRAM_BLANK, 1),
                input(ModItems.psigem.get(), 16),
                input(PsitweaksItems.CHAOTIC_PSIMETAL, 16),
                input(Items.TNT, 16)
        ), PsitweaksItems.PROGRAM_MASS_BLOCK_BREAK, 10_000L, 1_200);
    }

    private static void research(Map<ResourceLocation, JsonObject> recipes, String id, List<JsonObject> inputs,
                                   ItemLike output, long energyPerTick, int time) {
        recipes.put(Psitweaks.location("program_research/" + id), research(inputs, output, energyPerTick, time));
    }

    private static JsonObject research(List<JsonObject> inputs, ItemLike output, long energyPerTick, int time) {
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

    private static JsonObject input(ItemLike item, int count) {
        return input(item, count, true);
    }

    private static JsonObject inputId(ResourceLocation item, int count) {
        return input(item.toString(), count, true);
    }

    private static JsonObject catalyst(ItemLike item, int count) {
        return input(item, count, false);
    }

    private static JsonObject input(ItemLike item, int count, boolean consume) {
        return input(itemId(item), count, consume);
    }

    private static JsonObject input(String itemId, int count, boolean consume) {
        JsonObject input = new JsonObject();
        input.add("ingredient", ingredient(itemId));
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

    private static JsonObject result(ItemLike item) {
        JsonObject result = new JsonObject();
        result.addProperty("id", itemId(item));
        result.addProperty("count", 1);
        return result;
    }

    private static String itemId(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).toString();
    }

    private static ResourceLocation mekanism(String path) {
        return ResourceLocation.fromNamespaceAndPath("mekanism", path);
    }
}
