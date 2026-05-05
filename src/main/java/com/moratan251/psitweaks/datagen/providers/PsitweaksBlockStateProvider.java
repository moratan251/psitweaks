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
import net.minecraft.resources.ResourceLocation;

public class PsitweaksBlockStateProvider implements DataProvider {
    private final PackOutput.PathProvider blockStatePathProvider;
    private final PackOutput.PathProvider blockModelPathProvider;
    private final PackOutput.PathProvider itemModelPathProvider;

    public PsitweaksBlockStateProvider(PackOutput output) {
        this.blockStatePathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        this.blockModelPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/block");
        this.itemModelPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (PsitweaksDatagenBlocks.GeneratedBlock block : PsitweaksDatagenBlocks.blocks()) {
            if ("transcendent_universal_cable".equals(block.id())) {
                ResourceLocation id = Psitweaks.location(block.id());
                ResourceLocation model = Psitweaks.location("block/transmitter/small/universal_cable/transcendent");
                futures.add(DataProvider.saveStable(output, simpleBlockState(model), blockStatePathProvider.json(id)));
                futures.add(DataProvider.saveStable(output,
                        transcendentCableBlockModel(),
                        blockModelPathProvider.json(Psitweaks.location("transmitter/small/universal_cable/transcendent"))));
                futures.add(DataProvider.saveStable(output, parentItemModel(model), itemModelPathProvider.json(id)));
                continue;
            }
            if ("transcendent_energy_cube".equals(block.id())) {
                ResourceLocation id = Psitweaks.location(block.id());
                ResourceLocation model = Psitweaks.location("block/energy_cube/transcendent");
                futures.add(DataProvider.saveStable(output, energyCubeBlockState(model), blockStatePathProvider.json(id)));
                futures.add(DataProvider.saveStable(output,
                        energyCubeBlockModel(),
                        blockModelPathProvider.json(Psitweaks.location("energy_cube/transcendent"))));
                futures.add(DataProvider.saveStable(output, energyCubeItemModel(), itemModelPathProvider.json(id)));
                continue;
            }
            ResourceLocation id = Psitweaks.location(block.id());
            futures.add(DataProvider.saveStable(output,
                    block.machine() ? machineBlockState(block.id()) : blockState(block.id()),
                    blockStatePathProvider.json(id)));
            futures.add(DataProvider.saveStable(output,
                    block.machine() ? machineBlockModel(block.texture(), false, block.directionalTextures()) : blockModel(block.texture()),
                    blockModelPathProvider.json(id)));
            if (block.machine()) {
                futures.add(DataProvider.saveStable(output,
                        machineBlockModel(block.texture(), true, block.directionalTextures()),
                        blockModelPathProvider.json(Psitweaks.location(block.id() + "_active"))));
            }
            futures.add(DataProvider.saveStable(output, blockItemModel(block.id()), itemModelPathProvider.json(id)));
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "PsiTweaks block states and models";
    }

    private static JsonObject blockState(String id) {
        return simpleBlockState(Psitweaks.location("block/" + id));
    }

    private static JsonObject simpleBlockState(ResourceLocation model) {
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();
        JsonObject defaultVariant = new JsonObject();

        defaultVariant.addProperty("model", model.toString());
        variants.add("", defaultVariant);
        root.add("variants", variants);

        return root;
    }

    private static JsonObject blockModel(String texture) {
        JsonObject root = new JsonObject();
        JsonObject textures = new JsonObject();

        root.addProperty("parent", "minecraft:block/cube_all");
        textures.addProperty("all", Psitweaks.location("block/" + texture).toString());
        root.add("textures", textures);

        return root;
    }

    private static JsonObject machineBlockState(String id) {
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();

        addMachineVariant(variants, "active=false,facing=north", id, false, 0);
        addMachineVariant(variants, "active=false,facing=east", id, false, 90);
        addMachineVariant(variants, "active=false,facing=south", id, false, 180);
        addMachineVariant(variants, "active=false,facing=west", id, false, 270);
        addMachineVariant(variants, "active=true,facing=north", id, true, 0);
        addMachineVariant(variants, "active=true,facing=east", id, true, 90);
        addMachineVariant(variants, "active=true,facing=south", id, true, 180);
        addMachineVariant(variants, "active=true,facing=west", id, true, 270);
        root.add("variants", variants);

        return root;
    }

    private static void addMachineVariant(JsonObject variants, String key, String id, boolean active, int yRotation) {
        JsonObject variant = new JsonObject();

        variant.addProperty("model", Psitweaks.location("block/" + id + (active ? "_active" : "")).toString());
        if (yRotation != 0) {
            variant.addProperty("y", yRotation);
        }
        variants.add(key, variant);
    }

    private static JsonObject energyCubeBlockState(ResourceLocation model) {
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();

        addCubeVariant(variants, "facing=up", model, -90, null);
        addCubeVariant(variants, "facing=down", model, 90, null);
        addCubeVariant(variants, "facing=north", model, null, null);
        addCubeVariant(variants, "facing=south", model, null, 180);
        addCubeVariant(variants, "facing=east", model, null, 90);
        addCubeVariant(variants, "facing=west", model, null, -90);
        root.add("variants", variants);

        return root;
    }

    private static void addCubeVariant(JsonObject variants, String key, ResourceLocation model, Integer xRotation, Integer yRotation) {
        JsonObject variant = new JsonObject();

        variant.addProperty("model", model.toString());
        if (xRotation != null) {
            variant.addProperty("x", xRotation);
        }
        if (yRotation != null) {
            variant.addProperty("y", yRotation);
        }
        variants.add(key, variant);
    }

    private static JsonObject machineBlockModel(String texture, boolean active, boolean directionalTextures) {
        if (!directionalTextures) {
            return blockModel(texture);
        }

        JsonObject root = new JsonObject();
        JsonObject textures = new JsonObject();
        String base = "block/" + texture + "/";
        String front = Psitweaks.location(base + (active ? "front_active" : "front")).toString();

        root.addProperty("parent", "minecraft:block/cube");
        textures.addProperty("particle", front);
        textures.addProperty("down", Psitweaks.location(base + "bottom").toString());
        textures.addProperty("up", Psitweaks.location(base + "top").toString());
        textures.addProperty("north", front);
        textures.addProperty("east", Psitweaks.location(base + "right").toString());
        textures.addProperty("south", Psitweaks.location(base + "back").toString());
        textures.addProperty("west", Psitweaks.location(base + "left").toString());
        root.add("textures", textures);

        return root;
    }

    private static JsonObject blockItemModel(String id) {
        return parentItemModel(Psitweaks.location("block/" + id));
    }

    private static JsonObject parentItemModel(ResourceLocation parent) {
        JsonObject root = new JsonObject();

        root.addProperty("parent", parent.toString());

        return root;
    }

    private static JsonObject transcendentCableBlockModel() {
        JsonObject root = new JsonObject();
        JsonObject textures = new JsonObject();

        root.addProperty("parent", "mekanism:block/transmitter/small/small");
        textures.addProperty("side", Psitweaks.location("block/models/multipart/transcendent_universal_cable_vertical").toString());
        textures.addProperty("center_down", Psitweaks.location("block/models/multipart/transcendent_universal_cable").toString());
        textures.addProperty("side_opaque", Psitweaks.location("block/models/multipart/opaque/transcendent_universal_cable_vertical").toString());
        textures.addProperty("center_opaque", Psitweaks.location("block/models/multipart/transcendent_universal_cable").toString());
        root.add("textures", textures);

        return root;
    }

    private static JsonObject energyCubeBlockModel() {
        JsonObject root = new JsonObject();
        JsonObject textures = new JsonObject();

        root.addProperty("parent", "mekanism:block/energy_cube/base");
        textures.addProperty("corner", "mekanism:block/models/energy_cube_transcendent_corner");
        root.add("textures", textures);

        return root;
    }

    private static JsonObject energyCubeItemModel() {
        JsonObject root = new JsonObject();
        JsonObject display = new JsonObject();

        root.addProperty("parent", "builtin/entity");
        addDisplayTransform(display, "gui",
                vector(30, 315, 0),
                vector(0, 0, 0),
                vector(0.625, 0.625, 0.625));
        addDisplayTransform(display, "ground",
                vector(0, 90, 0),
                vector(0, 2, 0),
                vector(0.25, 0.25, 0.25));
        addDisplayTransform(display, "thirdperson_righthand",
                vector(75, 135, 0),
                vector(0, 2.5, 0),
                vector(0.375, 0.375, 0.375));
        addDisplayTransform(display, "firstperson_righthand",
                vector(0, 135, 0),
                vector(0, 0, 0),
                vector(0.4, 0.4, 0.4));
        addDisplayTransform(display, "thirdperson_lefthand",
                vector(75, 135, 0),
                vector(0, 2.5, 0),
                vector(0.375, 0.375, 0.375));
        addDisplayTransform(display, "firstperson_lefthand",
                vector(0, 135, 0),
                vector(0, 0, 0),
                vector(0.4, 0.4, 0.4));
        root.add("display", display);

        return root;
    }

    private static void addDisplayTransform(JsonObject display, String key, JsonArray rotation, JsonArray translation, JsonArray scale) {
        JsonObject transform = new JsonObject();

        transform.add("rotation", rotation);
        transform.add("translation", translation);
        transform.add("scale", scale);
        display.add(key, transform);
    }

    private static JsonArray vector(double x, double y, double z) {
        JsonArray vector = new JsonArray();

        vector.add(x);
        vector.add(y);
        vector.add(z);

        return vector;
    }
}
