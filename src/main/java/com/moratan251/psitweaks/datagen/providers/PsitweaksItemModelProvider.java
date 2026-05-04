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

public class PsitweaksItemModelProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public PsitweaksItemModelProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (PsitweaksDatagenItems.GeneratedItem item : PsitweaksDatagenItems.items()) {
            futures.add(DataProvider.saveStable(output, itemModel(item), pathProvider.json(Psitweaks.location(item.id()))));
            if (item.activeModel()) {
                futures.add(DataProvider.saveStable(
                        output,
                        activeItemModel(item),
                        pathProvider.json(Psitweaks.location(item.id() + "_active"))
                ));
            }
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "PsiTweaks item models";
    }

    private static JsonObject itemModel(PsitweaksDatagenItems.GeneratedItem item) {
        JsonObject root = new JsonObject();

        root.addProperty("parent", item.parent());
        if (item.texture() != null) {
            JsonObject textures = new JsonObject();
            textures.addProperty("layer0", ResourceLocation.fromNamespaceAndPath(Psitweaks.MOD_ID, "item/" + item.texture()).toString());
            root.add("textures", textures);
        }
        if (item.activeModel()) {
            JsonObject override = new JsonObject();
            JsonObject predicate = new JsonObject();
            JsonArray overrides = new JsonArray();

            predicate.addProperty(Psitweaks.location("active").toString(), 1.0F);
            override.add("predicate", predicate);
            override.addProperty("model", Psitweaks.location("item/" + item.id() + "_active").toString());
            overrides.add(override);
            root.add("overrides", overrides);
        }

        return root;
    }

    private static JsonObject activeItemModel(PsitweaksDatagenItems.GeneratedItem item) {
        JsonObject root = new JsonObject();
        JsonObject textures = new JsonObject();

        root.addProperty("parent", item.parent());
        textures.addProperty("layer0", ResourceLocation.fromNamespaceAndPath(Psitweaks.MOD_ID, "item/" + item.texture() + "_active").toString());
        root.add("textures", textures);

        return root;
    }
}
