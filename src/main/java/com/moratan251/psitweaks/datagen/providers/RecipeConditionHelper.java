package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

final class RecipeConditionHelper {
    private static final String CONDITIONS_KEY = "neoforge:conditions";
    private static final String MEKANISM = "mekanism";
    private static final String GENERATORS = "mekanismgenerators";

    private RecipeConditionHelper() {
    }

    static void addReferencedModConditions(ResourceLocation id, JsonObject recipe) {
        String json = recipe.toString();
        if (json.contains("\"mekanismgenerators:")) {
            requireMod(recipe, GENERATORS);
        } else if (json.contains("\"mekanism:")) {
            requireMod(recipe, MEKANISM);
        }

        String path = id.getPath();
        if (path.equals("program_researcher")
                || path.equals("material_mutator")
                || path.startsWith("sculk_eroder/")) {
            requireMod(recipe, MEKANISM);
        }
    }

    static JsonObject requireMod(JsonObject root, String modId) {
        conditions(root).add(modLoaded(modId));
        return root;
    }

    static JsonObject excludeMod(JsonObject root, String modId) {
        JsonObject not = new JsonObject();
        not.addProperty("type", "neoforge:not");
        not.add("value", modLoaded(modId));
        conditions(root).add(not);
        return root;
    }

    private static JsonArray conditions(JsonObject root) {
        if (root.has(CONDITIONS_KEY)) {
            return root.getAsJsonArray(CONDITIONS_KEY);
        }
        JsonArray conditions = new JsonArray();
        root.add(CONDITIONS_KEY, conditions);
        return conditions;
    }

    private static JsonObject modLoaded(String modId) {
        JsonObject condition = new JsonObject();
        condition.addProperty("type", "neoforge:mod_loaded");
        condition.addProperty("modid", modId);
        return condition;
    }
}
