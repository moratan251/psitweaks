package com.moratan251.psitweaks.common.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.chemicals.PsitweaksGases;
import com.moratan251.psitweaks.common.recipe.MaterialMutationInjectingRecipe;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Psitweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MaterialMutationRecipeHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private static final String TRICK_RECIPE_DIRECTORY = "recipes/material_mutation/trick";
    private static final String MACHINE_RECIPE_DIRECTORY = "recipes/material_mutation/machine";
    private static final long MACHINE_RECIPE_GAS_USAGE = 5L;
    private static final TrickRecipeReloadListener TRICK_RELOAD_LISTENER = new TrickRecipeReloadListener();
    private static final MachineRecipeReloadListener MACHINE_RELOAD_LISTENER = new MachineRecipeReloadListener();

    private static volatile Map<Block, ItemStack> trickRecipesByInputBlock = Map.of();
    private static volatile List<MaterialMutationInjectingRecipe> machineRecipes = List.of();

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(TRICK_RELOAD_LISTENER);
        event.addListener(MACHINE_RELOAD_LISTENER);
    }

    public static ItemStack getMutationOutput(Block inputBlock) {
        ItemStack output = trickRecipesByInputBlock.get(inputBlock);
        return output == null ? ItemStack.EMPTY : output.copy();
    }

    public static Map<Block, ItemStack> getAllMutationOutputs() {
        return trickRecipesByInputBlock;
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

    public static boolean containsMachineRecipe(ItemStack inputItem, GasStack inputGas) {
        return findFirstMachineRecipe(inputItem, inputGas) != null;
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

    @Mod.EventBusSubscriber(modid = Psitweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientReloadEvents {
        @SubscribeEvent
        public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(TRICK_RELOAD_LISTENER);
            event.registerReloadListener(MACHINE_RELOAD_LISTENER);
        }
    }

    private static class TrickRecipeReloadListener extends SimpleJsonResourceReloadListener {
        private TrickRecipeReloadListener() {
            super(GSON, TRICK_RECIPE_DIRECTORY);
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> jsonEntries,
                             ResourceManager resourceManager,
                             ProfilerFiller profiler) {
            Map<Block, ItemStack> loadedRecipes = new LinkedHashMap<>();

            for (Map.Entry<ResourceLocation, JsonElement> entry : jsonEntries.entrySet()) {
                ResourceLocation fileId = entry.getKey();
                JsonElement value = entry.getValue();
                if (!value.isJsonObject()) {
                    LOGGER.warn("Skipping material mutation recipe file {} because it is not a JSON object", fileId);
                    continue;
                }

                JsonObject root = value.getAsJsonObject();
                if (!GsonHelper.isValidNode(root, "mutations")) {
                    LOGGER.warn("Skipping material mutation recipe file {} because 'mutations' array is missing", fileId);
                    continue;
                }

                for (JsonElement mutationElement : GsonHelper.getAsJsonArray(root, "mutations")) {
                    if (!mutationElement.isJsonObject()) {
                        LOGGER.warn("Skipping malformed mutation entry in {}: expected object", fileId);
                        continue;
                    }

                    JsonObject mutation = mutationElement.getAsJsonObject();
                    @Nullable Block inputBlock = parseInputBlock(mutation, fileId);
                    @Nullable ItemStack outputStack = parseOutputStack(mutation, fileId);

                    if (inputBlock == null || outputStack == null || outputStack.isEmpty()) {
                        continue;
                    }

                    loadedRecipes.put(inputBlock, outputStack);
                }
            }

            trickRecipesByInputBlock = Map.copyOf(loadedRecipes);
            LOGGER.info("Loaded {} trick material mutation entries", trickRecipesByInputBlock.size());
        }

        @Nullable
        private static Block parseInputBlock(JsonObject mutation, ResourceLocation fileId) {
            if (!GsonHelper.isValidNode(mutation, "input")) {
                LOGGER.warn("Skipping mutation entry in {} because 'input' is missing", fileId);
                return null;
            }

            JsonObject input = GsonHelper.getAsJsonObject(mutation, "input");
            String blockId = GsonHelper.getAsString(input, "block", "");
            ResourceLocation inputBlockId = ResourceLocation.tryParse(blockId);
            if (inputBlockId == null) {
                LOGGER.warn("Skipping mutation entry in {} because input block id is invalid: {}", fileId, blockId);
                return null;
            }

            Block inputBlock = ForgeRegistries.BLOCKS.getValue(inputBlockId);
            if (inputBlock == null) {
                LOGGER.warn("Skipping mutation entry in {} because input block '{}' was not found", fileId, inputBlockId);
                return null;
            }

            return inputBlock;
        }

        @Nullable
        private static ItemStack parseOutputStack(JsonObject mutation, ResourceLocation fileId) {
            if (!GsonHelper.isValidNode(mutation, "output")) {
                LOGGER.warn("Skipping mutation entry in {} because 'output' is missing", fileId);
                return null;
            }

            JsonObject output = GsonHelper.getAsJsonObject(mutation, "output");
            int count = Math.max(1, GsonHelper.getAsInt(output, "count", 1));

            if (GsonHelper.isValidNode(output, "item")) {
                String itemId = GsonHelper.getAsString(output, "item", "");
                ResourceLocation outputItemId = ResourceLocation.tryParse(itemId);
                if (outputItemId == null) {
                    LOGGER.warn("Skipping mutation entry in {} because output item id is invalid: {}", fileId, itemId);
                    return null;
                }

                Item outputItem = ForgeRegistries.ITEMS.getValue(outputItemId);
                if (outputItem == null) {
                    LOGGER.warn("Skipping mutation entry in {} because output item '{}' was not found", fileId, outputItemId);
                    return null;
                }

                return new ItemStack(outputItem, count);
            }

            if (GsonHelper.isValidNode(output, "block")) {
                String blockId = GsonHelper.getAsString(output, "block", "");
                ResourceLocation outputBlockId = ResourceLocation.tryParse(blockId);
                if (outputBlockId == null) {
                    LOGGER.warn("Skipping mutation entry in {} because output block id is invalid: {}", fileId, blockId);
                    return null;
                }

                Block outputBlock = ForgeRegistries.BLOCKS.getValue(outputBlockId);
                if (outputBlock == null) {
                    LOGGER.warn("Skipping mutation entry in {} because output block '{}' was not found", fileId, outputBlockId);
                    return null;
                }

                Item outputItem = outputBlock.asItem();
                if (outputItem == Items.AIR) {
                    LOGGER.warn("Skipping mutation entry in {} because output block '{}' has no item form", fileId, outputBlockId);
                    return null;
                }

                return new ItemStack(outputItem, count);
            }

            LOGGER.warn("Skipping mutation entry in {} because output requires either 'item' or 'block'", fileId);
            return null;
        }
    }

    private static class MachineRecipeReloadListener extends SimpleJsonResourceReloadListener {
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

                JsonObject root = value.getAsJsonObject();
                @Nullable MaterialMutationInjectingRecipe recipe = parseMachineRecipe(fileId, root);
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
            JsonElement ingredientElement = GsonHelper.isValidNode(inputObject, "ingredient") ? inputObject.get("ingredient") : inputObject;
            Ingredient ingredient = Ingredient.fromJson(ingredientElement);
            if (ingredient.isEmpty()) {
                LOGGER.warn("Skipping machine material mutation recipe {} because input ingredient is empty", recipeId);
                return null;
            }

            int inputCount = Math.max(1, GsonHelper.getAsInt(inputObject, "count", 1));
            JsonObject outputObject = GsonHelper.getAsJsonObject(root, "output");
            ItemStack output = CraftingHelper.getItemStack(outputObject, true);
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
