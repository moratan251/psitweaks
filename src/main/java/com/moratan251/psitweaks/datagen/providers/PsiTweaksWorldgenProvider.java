package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.moratan251.psitweaks.Psitweaks;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public class PsiTweaksWorldgenProvider implements DataProvider {
    private final PackOutput.PathProvider configuredFeaturePathProvider;
    private final PackOutput.PathProvider placedFeaturePathProvider;
    private final PackOutput.PathProvider biomeModifierPathProvider;
    private final PackOutput.PathProvider biomeTagPathProvider;

    public PsiTweaksWorldgenProvider(PackOutput output) {
        this.configuredFeaturePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "worldgen/configured_feature");
        this.placedFeaturePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "worldgen/placed_feature");
        this.biomeModifierPathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "neoforge/biome_modifier");
        this.biomeTagPathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "tags/worldgen/biome");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        futures.add(DataProvider.saveStable(
                output,
                configuredAntiniteOre(),
                configuredFeaturePathProvider.json(Psitweaks.location("ore_antinite"))
        ));
        futures.add(DataProvider.saveStable(
                output,
                placedAntiniteOre(),
                placedFeaturePathProvider.json(Psitweaks.location("ore_antinite"))
        ));
        futures.add(DataProvider.saveStable(
                output,
                addAntiniteOreBiomeModifier(),
                biomeModifierPathProvider.json(Psitweaks.location("add_ore_antinite"))
        ));
        futures.add(DataProvider.saveStable(
                output,
                hasAntiniteOreBiomeTag(),
                biomeTagPathProvider.json(Psitweaks.location("has_antinite_ore"))
        ));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "PsiTweaks worldgen";
    }

    private static JsonObject configuredAntiniteOre() {
        JsonObject root = new JsonObject();
        JsonObject config = new JsonObject();
        JsonArray targets = new JsonArray();
        JsonObject targetEntry = new JsonObject();
        JsonObject target = new JsonObject();
        JsonObject state = new JsonObject();

        root.addProperty("type", "minecraft:ore");
        config.addProperty("size", 4);
        config.addProperty("discard_chance_on_air_exposure", 0.0);
        target.addProperty("predicate_type", "minecraft:block_match");
        target.addProperty("block", "minecraft:end_stone");
        state.addProperty("Name", Psitweaks.location("ore_antinite").toString());
        targetEntry.add("target", target);
        targetEntry.add("state", state);
        targets.add(targetEntry);
        config.add("targets", targets);
        root.add("config", config);

        return root;
    }

    private static JsonObject placedAntiniteOre() {
        JsonObject root = new JsonObject();
        JsonArray placement = new JsonArray();

        JsonObject count = new JsonObject();
        count.addProperty("type", "minecraft:count");
        count.addProperty("count", 1);
        placement.add(count);

        JsonObject inSquare = new JsonObject();
        inSquare.addProperty("type", "minecraft:in_square");
        placement.add(inSquare);

        JsonObject heightRange = new JsonObject();
        JsonObject height = new JsonObject();
        JsonObject minInclusive = new JsonObject();
        JsonObject maxInclusive = new JsonObject();
        heightRange.addProperty("type", "minecraft:height_range");
        height.addProperty("type", "minecraft:uniform");
        minInclusive.addProperty("above_bottom", 0);
        maxInclusive.addProperty("below_top", 0);
        height.add("min_inclusive", minInclusive);
        height.add("max_inclusive", maxInclusive);
        heightRange.add("height", height);
        placement.add(heightRange);

        JsonObject biome = new JsonObject();
        biome.addProperty("type", "minecraft:biome");
        placement.add(biome);

        root.addProperty("feature", Psitweaks.location("ore_antinite").toString());
        root.add("placement", placement);

        return root;
    }

    private static JsonObject addAntiniteOreBiomeModifier() {
        JsonObject root = new JsonObject();

        root.addProperty("type", "neoforge:add_features");
        root.addProperty("biomes", "#psitweaks:has_antinite_ore");
        root.addProperty("features", Psitweaks.location("ore_antinite").toString());
        root.addProperty("step", "underground_ores");

        return root;
    }

    private static JsonObject hasAntiniteOreBiomeTag() {
        JsonObject root = new JsonObject();
        JsonArray values = new JsonArray();

        values.add("minecraft:end_highlands");
        values.add("minecraft:end_midlands");
        values.add("minecraft:small_end_islands");
        values.add("minecraft:end_barrens");
        root.add("values", values);

        return root;
    }
}
