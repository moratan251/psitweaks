package com.moratan251.psitweaks.datagen.providers;

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
            ResourceLocation id = Psitweaks.location(block.id());
            futures.add(DataProvider.saveStable(output,
                    block.machine() ? machineBlockState(block.id()) : blockState(block.id()),
                    blockStatePathProvider.json(id)));
            futures.add(DataProvider.saveStable(output,
                    block.machine() ? machineBlockModel(block.texture(), false) : blockModel(block.texture()),
                    blockModelPathProvider.json(id)));
            if (block.machine()) {
                futures.add(DataProvider.saveStable(output,
                        machineBlockModel(block.texture(), true),
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
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();
        JsonObject defaultVariant = new JsonObject();

        defaultVariant.addProperty("model", Psitweaks.location("block/" + id).toString());
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

    private static JsonObject machineBlockModel(String texture, boolean active) {
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
        JsonObject root = new JsonObject();

        root.addProperty("parent", Psitweaks.location("block/" + id).toString());

        return root;
    }
}
