package com.moratan251.psitweaks.common.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.moratan251.psitweaks.common.chemicals.PsitweaksChemicals;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class MekanismMaterialMutationRecipeHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private static final String MACHINE_RECIPE_DIRECTORY = "recipes/material_mutation/machine";
    private static final MachineRecipeReloadListener RELOAD_LISTENER = new MachineRecipeReloadListener();
    private static volatile Map<Item, MachineRecipe> recipesByInputItem = Map.of();

    private MekanismMaterialMutationRecipeHandler() {
    }

    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(RELOAD_LISTENER);
    }

    public static void registerClientReloadListeners(IEventBus modEventBus) {
        modEventBus.addListener(MekanismMaterialMutationRecipeHandler::onRegisterClientReloadListeners);
    }

    private static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(RELOAD_LISTENER);
    }

    @Nullable
    public static MachineRecipe findFirstMachineRecipe(ItemStack inputItem, ChemicalStack inputChemical) {
        if (inputItem.isEmpty() || !containsMachineRecipeChemical(inputChemical)) {
            return null;
        }
        MachineRecipe recipe = recipesByInputItem.get(inputItem.getItem());
        return recipe != null && inputItem.getCount() >= recipe.inputCount() ? recipe : null;
    }

    public static boolean containsMachineRecipeInput(ItemStack inputItem) {
        return !inputItem.isEmpty() && recipesByInputItem.containsKey(inputItem.getItem());
    }

    public static boolean containsMachineRecipeChemical(ChemicalStack inputChemical) {
        return !inputChemical.isEmpty()
                && inputChemical.is(getPsionicEchoChemical())
                && !recipesByInputItem.isEmpty();
    }

    public static Chemical getPsionicEchoChemical() {
        return PsitweaksChemicals.GAS_PSIONIC_ECHO.get();
    }

    public static Holder<Chemical> getPsionicEchoChemicalHolder() {
        return PsitweaksChemicals.GAS_PSIONIC_ECHO;
    }

    public static List<MachineRecipe> getAllMachineRecipes() {
        return List.copyOf(recipesByInputItem.values());
    }

    public record MachineRecipe(ResourceLocation id, Item input, int inputCount, ItemStack output, int time) {
    }

    private static final class MachineRecipeReloadListener extends SimpleJsonResourceReloadListener {
        private MachineRecipeReloadListener() {
            super(GSON, MACHINE_RECIPE_DIRECTORY);
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> jsonEntries, ResourceManager resourceManager,
                ProfilerFiller profiler) {
            Map<Item, MachineRecipe> loadedRecipes = new LinkedHashMap<>();
            for (Map.Entry<ResourceLocation, JsonElement> entry : jsonEntries.entrySet()) {
                ResourceLocation fileId = entry.getKey();
                JsonElement value = entry.getValue();
                if (!value.isJsonObject()) {
                    LOGGER.warn("Skipping material mutation machine recipe file {} because it is not a JSON object",
                            fileId);
                    continue;
                }
                JsonObject root = value.getAsJsonObject();
                @Nullable Item inputItem = parseInputItem(root, fileId);
                @Nullable ItemStack outputStack = MaterialMutationRecipeHandler.parseOutputStack(root, fileId);
                if (inputItem == null || outputStack == null || outputStack.isEmpty()) {
                    continue;
                }
                int inputCount = Math.max(
                        1,
                        GsonHelper.getAsInt(GsonHelper.getAsJsonObject(root, "input"), "count", 1)
                );
                int time = Math.max(1, GsonHelper.getAsInt(root, "time", 200));
                loadedRecipes.put(inputItem, new MachineRecipe(fileId, inputItem, inputCount, outputStack, time));
            }
            recipesByInputItem = Map.copyOf(loadedRecipes);
            LOGGER.info("Loaded {} machine material mutation entries", recipesByInputItem.size());
        }
    }

    @Nullable
    private static Item parseInputItem(JsonObject root, ResourceLocation fileId) {
        if (!GsonHelper.isValidNode(root, "input")) {
            LOGGER.warn("Skipping machine mutation recipe {} because 'input' is missing", fileId);
            return null;
        }
        String itemId = GsonHelper.getAsString(GsonHelper.getAsJsonObject(root, "input"), "item", "");
        ResourceLocation inputItemId = ResourceLocation.tryParse(itemId);
        if (inputItemId == null) {
            LOGGER.warn("Skipping machine mutation recipe {} because input item id is invalid: {}", fileId, itemId);
            return null;
        }
        Item inputItem = BuiltInRegistries.ITEM.get(inputItemId);
        if (inputItem == Items.AIR && !ResourceLocation.withDefaultNamespace("air").equals(inputItemId)) {
            LOGGER.warn("Skipping machine mutation recipe {} because input item '{}' was not found", fileId,
                    inputItemId);
            return null;
        }
        return inputItem;
    }
}
