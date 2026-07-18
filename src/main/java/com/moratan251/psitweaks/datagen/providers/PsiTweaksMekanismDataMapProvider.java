package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonObject;
import com.moratan251.psitweaks.Psitweaks;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public class PsiTweaksMekanismDataMapProvider implements DataProvider {
    private final PackOutput.PathProvider chemicalDataMapPathProvider;

    public PsiTweaksMekanismDataMapProvider(PackOutput output) {
        this.chemicalDataMapPathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "data_maps/mekanism/chemical");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Map<ResourceLocation, JsonObject> dataMaps = new LinkedHashMap<>();

        addChemicalDataMaps(dataMaps);
        return CompletableFuture.allOf(dataMaps.entrySet().stream()
                .map(entry -> DataProvider.saveStable(output, entry.getValue(), chemicalDataMapPathProvider.json(entry.getKey())))
                .toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "PsiTweaks Mekanism data maps";
    }

    private static void addChemicalDataMaps(Map<ResourceLocation, JsonObject> dataMaps) {
        JsonObject fuelValues = new JsonObject();
        fuelValues.add(chemical("gas_peo_fuel"), chemicalFuel(240, 540_000L));
        dataMaps.put(ResourceLocation.fromNamespaceAndPath("mekanism", "chemical_attribute_fuel"), dataMap(fuelValues));

        JsonObject solidTagValues = new JsonObject();
        solidTagValues.add(chemical("dirty_antinite"), chemicalSolidTag("c:ores/antinite"));
        solidTagValues.add(chemical("clean_antinite"), chemicalSolidTag("c:ores/antinite"));
        dataMaps.put(ResourceLocation.fromNamespaceAndPath(Psitweaks.MOD_ID, "chemical_solid_tag"), dataMap(solidTagValues));
    }

    private static JsonObject dataMap(JsonObject values) {
        JsonObject root = new JsonObject();
        root.add("values", values);
        return root;
    }

    private static JsonObject chemicalFuel(int burnTime, long energy) {
        JsonObject value = new JsonObject();
        value.addProperty("burn_time", burnTime);
        value.addProperty("energy", energy);
        return value;
    }

    private static JsonObject chemicalSolidTag(String representation) {
        JsonObject value = new JsonObject();
        value.addProperty("representation", representation);
        return value;
    }

    private static String chemical(String path) {
        return Psitweaks.MOD_ID + ":" + path;
    }
}
