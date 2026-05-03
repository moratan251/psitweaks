package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonObject;
import com.moratan251.psitweaks.Psitweaks;
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
        CompletableFuture<?>[] futures = PsitweaksDatagenItems.items().stream()
                .map(item -> DataProvider.saveStable(output, itemModel(item), pathProvider.json(Psitweaks.location(item.id()))))
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
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

        return root;
    }
}
