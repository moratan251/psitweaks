package com.moratan251.psitweaks.common.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.moratan251.psitweaks.common.chemicals.PsitweaksGases;
import com.moratan251.psitweaks.common.recipe.MaterialMutationInjectingRecipe;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.AddReloadListenerEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class MekanismMaterialMutationRecipeHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private static final String MACHINE_RECIPE_DIRECTORY = "material_mutation/machine";
    private static final long MACHINE_RECIPE_GAS_USAGE = 5L;
    private static final MachineRecipeReloadListener RELOAD_LISTENER = new MachineRecipeReloadListener();

    private static volatile List<MaterialMutationInjectingRecipe> machineRecipes = List.of();

    private MekanismMaterialMutationRecipeHandler() {
    }

    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(RELOAD_LISTENER);
    }

    public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(RELOAD_LISTENER);
    }

    @Nullable
    public static ItemStackGasToItemStackRecipe findFirstMachineRecipe(ItemStack inputItem, GasStack inputGas) {
        if (inputItem.isEmpty() || !containsMachineRecipeGas(inputGas)) {
            return null;
        }
        for (MaterialMutationInjectingRecipe recipe : machineRecipes) {
            if (recipe.test(inputItem, inputGas)) {
                return recipe;
            }
        }
        return null;
    }

    public static boolean containsMachineRecipe(ItemStack inputItem, Gas inputGas) {
        return containsMachineRecipeGas(inputGas) && containsMachineRecipeInput(inputItem);
    }

    public static boolean containsMachineRecipeInput(ItemStack inputItem) {
        if (inputItem.isEmpty()) {
            return false;
        }
        for (MaterialMutationInjectingRecipe recipe : machineRecipes) {
            if (recipe.getItemInput().testType(inputItem)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsMachineRecipeGas(GasStack inputGas) {
        return isPsionicEchoGas(inputGas) && !machineRecipes.isEmpty();
    }

    public static boolean containsMachineRecipeGas(@Nullable Gas inputGas) {
        return isPsionicEchoGas(inputGas) && !machineRecipes.isEmpty();
    }

    public static List<ItemStackGasToItemStackRecipe> getAllMachineRecipes() {
        return machineRecipes.stream().map(recipe -> (ItemStackGasToItemStackRecipe) recipe).toList();
    }

    private static boolean isPsionicEchoGas(@Nullable Gas gas) {
        return gas != null && gas == PsitweaksGases.PSIONIC_ECHO.get();
    }

    private static boolean isPsionicEchoGas(GasStack gasStack) {
        return !gasStack.isEmpty() && isPsionicEchoGas(gasStack.getType());
    }

    private static final class MachineRecipeReloadListener extends SimpleJsonResourceReloadListener {
        private MachineRecipeReloadListener() {
            super(GSON, MACHINE_RECIPE_DIRECTORY);
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> jsonEntries,
                             ResourceManager resourceManager,
                             ProfilerFiller profiler) {
            List<MaterialMutationInjectingRecipe> loadedRecipes = new ArrayList<>();
            for (Map.Entry<ResourceLocation, JsonElement> entry : jsonEntries.entrySet()) {
                ResourceLocation fileId = entry.getKey();
                JsonElement value = entry.getValue();
                if (!value.isJsonObject()) {
                    LOGGER.warn("Skipping machine material mutation recipe file {} because it is not a JSON object", fileId);
                    continue;
                }
                @Nullable MaterialMutationInjectingRecipe recipe = parseMachineRecipe(fileId, value.getAsJsonObject());
                if (recipe != null) {
                    loadedRecipes.add(recipe);
                }
            }
            machineRecipes = List.copyOf(loadedRecipes);
            LOGGER.info("Loaded {} machine material mutation entries", machineRecipes.size());
        }

        @Nullable
        private static MaterialMutationInjectingRecipe parseMachineRecipe(ResourceLocation recipeId, JsonObject root) {
            if (!GsonHelper.isValidNode(root, "input")) {
                LOGGER.warn("Skipping machine material mutation recipe {} because 'input' is missing", recipeId);
                return null;
            }
            if (!GsonHelper.isValidNode(root, "output")) {
                LOGGER.warn("Skipping machine material mutation recipe {} because 'output' is missing", recipeId);
                return null;
            }

            JsonObject inputObject = GsonHelper.getAsJsonObject(root, "input");
            JsonElement ingredientElement = GsonHelper.isValidNode(inputObject, "ingredient")
                    ? inputObject.get("ingredient")
                    : inputObject;
            Ingredient ingredient = Ingredient.fromJson(ingredientElement);
            if (ingredient.isEmpty()) {
                LOGGER.warn("Skipping machine material mutation recipe {} because input ingredient is empty", recipeId);
                return null;
            }

            int inputCount = Math.max(1, GsonHelper.getAsInt(inputObject, "count", 1));
            ItemStack output = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(root, "output"), true);
            if (output.isEmpty()) {
                LOGGER.warn("Skipping machine material mutation recipe {} because output is empty", recipeId);
                return null;
            }

            int processingTime = Math.max(1, GsonHelper.getAsInt(root, "time", 200));
            return new MaterialMutationInjectingRecipe(
                    recipeId,
                    IngredientCreatorAccess.item().from(ingredient, inputCount),
                    IngredientCreatorAccess.gas().from(PsitweaksGases.PSIONIC_ECHO.get(), MACHINE_RECIPE_GAS_USAGE),
                    output.copy(),
                    processingTime
            );
        }
    }
}
