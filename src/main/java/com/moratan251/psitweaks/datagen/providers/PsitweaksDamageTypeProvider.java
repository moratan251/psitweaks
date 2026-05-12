package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonObject;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public class PsitweaksDamageTypeProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public PsitweaksDamageTypeProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "damage_type");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Map<ResourceLocation, JsonObject> damageTypes = new LinkedHashMap<>();
        List<CompletableFuture<?>> futures = new ArrayList<>();

        addDamageTypes(damageTypes);
        damageTypes.forEach((id, damageType) -> futures.add(DataProvider.saveStable(output, damageType, pathProvider.json(id))));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "PsiTweaks damage types";
    }

    private static void addDamageTypes(Map<ResourceLocation, JsonObject> damageTypes) {
        damageTypes.put(PsitweaksDamageTypes.METEOR_LINE.location(), damageType("psitweaks.meteor_line", "never", 0.0F));
    }

    private static JsonObject damageType(String messageId, String scaling, float exhaustion) {
        JsonObject root = new JsonObject();
        root.addProperty("exhaustion", exhaustion);
        root.addProperty("message_id", messageId);
        root.addProperty("scaling", scaling);
        return root;
    }
}
