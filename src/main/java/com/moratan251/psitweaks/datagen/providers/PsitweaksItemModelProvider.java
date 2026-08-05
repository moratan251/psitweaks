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
        futures.add(DataProvider.saveStable(output, bowPullingModel("psimetal_bow_pulling_0"), pathProvider.json(Psitweaks.location("psimetal_bow_pulling_0"))));
        futures.add(DataProvider.saveStable(output, bowPullingModel("psimetal_bow_pulling_1"), pathProvider.json(Psitweaks.location("psimetal_bow_pulling_1"))));
        futures.add(DataProvider.saveStable(output, bowPullingModel("psimetal_bow_pulling_2"), pathProvider.json(Psitweaks.location("psimetal_bow_pulling_2"))));
        futures.add(DataProvider.saveStable(output, bowPullingModel("gravstringer_pulling_0"), pathProvider.json(Psitweaks.location("gravstringer_pulling_0"))));
        futures.add(DataProvider.saveStable(output, bowPullingModel("gravstringer_pulling_1"), pathProvider.json(Psitweaks.location("gravstringer_pulling_1"))));
        futures.add(DataProvider.saveStable(output, bowPullingModel("gravstringer_pulling_2"), pathProvider.json(Psitweaks.location("gravstringer_pulling_2"))));
        futures.add(DataProvider.saveStable(output, bowPullingModel("gravstringer_pulling_0_tunneler"), pathProvider.json(Psitweaks.location("gravstringer_pulling_0_tunneler"))));
        futures.add(DataProvider.saveStable(output, bowPullingModel("gravstringer_pulling_1_tunneler"), pathProvider.json(Psitweaks.location("gravstringer_pulling_1_tunneler"))));
        futures.add(DataProvider.saveStable(output, bowPullingModel("gravstringer_pulling_2_tunneler"), pathProvider.json(Psitweaks.location("gravstringer_pulling_2_tunneler"))));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "PsiTweaks item models";
    }

    private static JsonObject itemModel(PsitweaksDatagenItems.GeneratedItem item) {
        if (isBow(item.id())) {
            return bowModel(item);
        }

        JsonObject root = new JsonObject();

        root.addProperty("parent", item.parent());
        if (item.texture() != null) {
            JsonObject textures = new JsonObject();
            String texturePath = item.texture().startsWith("block/")
                    ? item.texture()
                    : "item/" + item.texture();
            textures.addProperty("layer0", ResourceLocation.fromNamespaceAndPath(Psitweaks.MOD_ID, texturePath).toString());
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

    private static boolean isBow(String id) {
        return "psimetal_bow".equals(id) || "gravstringer".equals(id);
    }

    private static JsonObject bowModel(PsitweaksDatagenItems.GeneratedItem item) {
        JsonObject root = generatedModel("minecraft:item/bow", item.texture());
        JsonArray overrides = new JsonArray();

        overrides.add(bowOverride(1.0F, null, item.id() + "_pulling_0"));
        overrides.add(bowOverride(1.0F, 0.65F, item.id() + "_pulling_1"));
        overrides.add(bowOverride(1.0F, 0.9F, item.id() + "_pulling_2"));
        if ("gravstringer".equals(item.id())) {
            // 通常オーバーライドの後に置くことで、トンネラー装填時はこちらが優先される
            overrides.add(tunnelerBowOverride(null, "gravstringer_pulling_0_tunneler"));
            overrides.add(tunnelerBowOverride(0.65F, "gravstringer_pulling_1_tunneler"));
            overrides.add(tunnelerBowOverride(0.9F, "gravstringer_pulling_2_tunneler"));
        }
        root.add("overrides", overrides);

        return root;
    }

    private static JsonObject tunnelerBowOverride(Float pull, String model) {
        JsonObject override = new JsonObject();
        JsonObject predicate = new JsonObject();

        predicate.addProperty(Psitweaks.location("tunneler_loaded").toString(), 1.0F);
        predicate.addProperty(ResourceLocation.withDefaultNamespace("pulling").toString(), 1.0F);
        if (pull != null) {
            predicate.addProperty(ResourceLocation.withDefaultNamespace("pull").toString(), pull);
        }
        override.add("predicate", predicate);
        override.addProperty("model", Psitweaks.location("item/" + model).toString());

        return override;
    }

    private static JsonObject bowPullingModel(String id) {
        return generatedModel("minecraft:item/bow", id);
    }

    private static JsonObject bowOverride(float pulling, Float pull, String model) {
        JsonObject override = new JsonObject();
        JsonObject predicate = new JsonObject();

        predicate.addProperty(ResourceLocation.withDefaultNamespace("pulling").toString(), pulling);
        if (pull != null) {
            predicate.addProperty(ResourceLocation.withDefaultNamespace("pull").toString(), pull);
        }
        override.add("predicate", predicate);
        override.addProperty("model", Psitweaks.location("item/" + model).toString());

        return override;
    }

    private static JsonObject generatedModel(String parent, String texture) {
        JsonObject root = new JsonObject();
        JsonObject textures = new JsonObject();

        root.addProperty("parent", parent);
        textures.addProperty("layer0", ResourceLocation.fromNamespaceAndPath(Psitweaks.MOD_ID, "item/" + texture).toString());
        root.add("textures", textures);

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
