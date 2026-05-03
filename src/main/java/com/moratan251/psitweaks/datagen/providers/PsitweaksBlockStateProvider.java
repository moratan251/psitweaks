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
            futures.add(DataProvider.saveStable(output, blockState(block.id()), blockStatePathProvider.json(id)));
            futures.add(DataProvider.saveStable(output, blockModel(block.texture()), blockModelPathProvider.json(id)));
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

    private static JsonObject blockItemModel(String id) {
        JsonObject root = new JsonObject();

        root.addProperty("parent", Psitweaks.location("block/" + id).toString());

        return root;
    }
}
