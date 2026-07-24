package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

public class PsiTweaksLootTableProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public PsiTweaksLootTableProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "loot_table");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (PsitweaksDatagenBlocks.GeneratedBlock block : PsitweaksDatagenBlocks.blocks()) {
            JsonObject lootTable = switch (block.id()) {
                case "ore_antinite" -> oreDrop(block.id(), PsitweaksItems.RAW_ANTINITE);
                default -> dropSelf(block.id());
            };
            futures.add(DataProvider.saveStable(output, lootTable, pathProvider.json(Psitweaks.location("blocks/" + block.id()))));
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "PsiTweaks block loot tables";
    }

    private static JsonObject dropSelf(String blockId) {
        JsonObject table = blockTable(blockId);
        JsonObject pool = pool();
        JsonArray entries = new JsonArray();
        JsonArray conditions = new JsonArray();
        JsonObject survivesExplosion = new JsonObject();

        entries.add(itemEntry(block(blockId)));
        survivesExplosion.addProperty("condition", "minecraft:survives_explosion");
        conditions.add(survivesExplosion);
        pool.add("entries", entries);
        pool.add("conditions", conditions);
        addPool(table, pool);

        return table;
    }

    private static JsonObject oreDrop(String blockId, ItemLike item) {
        JsonObject table = blockTable(blockId);
        JsonObject pool = pool();
        JsonArray entries = new JsonArray();
        JsonObject alternatives = new JsonObject();
        JsonArray children = new JsonArray();

        alternatives.addProperty("type", "minecraft:alternatives");
        children.add(silkTouchEntry(blockId));
        children.add(fortuneEntry(item));
        alternatives.add("children", children);
        entries.add(alternatives);
        pool.add("entries", entries);
        addPool(table, pool);

        return table;
    }

    private static JsonObject blockTable(String blockId) {
        JsonObject table = new JsonObject();

        table.addProperty("type", "minecraft:block");
        table.add("pools", new JsonArray());
        table.addProperty("random_sequence", Psitweaks.location("blocks/" + blockId).toString());

        return table;
    }

    private static JsonObject pool() {
        JsonObject pool = new JsonObject();

        pool.addProperty("bonus_rolls", 0.0);
        pool.addProperty("name", "main");
        pool.addProperty("rolls", 1.0);

        return pool;
    }

    private static void addPool(JsonObject table, JsonObject pool) {
        table.getAsJsonArray("pools").add(pool);
    }

    private static JsonObject silkTouchEntry(String blockId) {
        JsonObject entry = itemEntry(block(blockId));
        JsonArray conditions = new JsonArray();
        JsonObject matchTool = new JsonObject();
        JsonObject predicate = new JsonObject();
        JsonObject predicates = new JsonObject();
        JsonArray enchantments = new JsonArray();
        JsonObject silkTouch = new JsonObject();
        JsonObject levels = new JsonObject();

        matchTool.addProperty("condition", "minecraft:match_tool");
        silkTouch.addProperty("enchantments", "minecraft:silk_touch");
        levels.addProperty("min", 1);
        silkTouch.add("levels", levels);
        enchantments.add(silkTouch);
        predicates.add("minecraft:enchantments", enchantments);
        predicate.add("predicates", predicates);
        matchTool.add("predicate", predicate);
        conditions.add(matchTool);
        entry.add("conditions", conditions);

        return entry;
    }

    private static JsonObject fortuneEntry(ItemLike item) {
        JsonObject entry = itemEntry(item);
        JsonArray functions = new JsonArray();
        JsonObject applyBonus = new JsonObject();
        JsonObject explosionDecay = new JsonObject();

        applyBonus.addProperty("function", "minecraft:apply_bonus");
        applyBonus.addProperty("enchantment", "minecraft:fortune");
        applyBonus.addProperty("formula", "minecraft:ore_drops");
        functions.add(applyBonus);
        explosionDecay.addProperty("function", "minecraft:explosion_decay");
        functions.add(explosionDecay);
        entry.add("functions", functions);

        return entry;
    }

    private static JsonObject itemEntry(String itemId) {
        JsonObject entry = new JsonObject();

        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", itemId);

        return entry;
    }

    private static JsonObject itemEntry(ItemLike item) {
        return itemEntry(BuiltInRegistries.ITEM.getKey(item.asItem()).toString());
    }

    private static String block(String path) {
        return Psitweaks.MOD_ID + ":" + path;
    }
}
