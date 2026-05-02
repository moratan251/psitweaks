package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PsiTweaksLootTableProvider implements DataProvider {
    private static final List<NbtCopy> COMMON_MACHINE_NBT_PATHS = List.of(
            new NbtCopy("componentSecurity.owner", "owner"),
            new NbtCopy("componentSecurity.securityMode", "securityMode"),
            new NbtCopy("componentUpgrade", "componentUpgrade"),
            new NbtCopy("componentConfig", "componentConfig"),
            new NbtCopy("componentEjector", "componentEjector"),
            new NbtCopy("controlType", "controlType"),
            new NbtCopy("EnergyContainers", "EnergyContainers")
    );

    private final PackOutput.PathProvider pathProvider;

    public PsiTweaksLootTableProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "loot_tables");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        dropSelf(futures, output, PsitweaksBlocks.CAD_DISASSEMBLER.get());
        dropSelf(futures, output, PsitweaksBlocks.PROGRAM_RESEARCHER.get());
        save(futures, output, PsitweaksBlocks.ORE_ANTINITE.get(), oreDrop(PsitweaksBlocks.ORE_ANTINITE.get(), PsitweaksItems.RAW_ANTINITE.get()));
        dropSelf(futures, output, PsitweaksBlocks.ANTINITE_BLOCK.get());
        dropSelf(futures, output, PsitweaksBlocks.CHAOTIC_PSIMETAL_BLOCK.get());
        dropSelf(futures, output, PsitweaksBlocks.FLASHMETAL_BLOCK.get());
        dropSelf(futures, output, PsitweaksBlocks.HEAVY_PSIMETAL_BLOCK.get());
        dropSelf(futures, output, PsitweaksBlocks.PLUTONIUM_BLOCK.get());
        dropSelf(futures, output, PsitweaksBlocks.POLONIUM_BLOCK.get());
        dropSelf(futures, output, PsitweaksBlocks.RAW_ANTINITE_BLOCK.get());
        dropSelf(futures, output, PsitweaksBlocks.SPELLMACHINERY_CASING.get());

        save(futures, output, PsitweaksMekanismBlocks.SCULK_ERODER.getBlock(), machineDrop(
                PsitweaksMekanismBlocks.SCULK_ERODER.getBlock(),
                nbtPaths("Items")
        ));
        save(futures, output, PsitweaksMekanismBlocks.MATERIAL_MUTATOR.getBlock(), machineDrop(
                PsitweaksMekanismBlocks.MATERIAL_MUTATOR.getBlock(),
                nbtPaths("GasTanks", "Items")
        ));
        save(futures, output, PsitweaksMekanismBlocks.PSIONIC_GENERATOR.getBlock(), machineDrop(
                PsitweaksMekanismBlocks.PSIONIC_GENERATOR.getBlock(),
                List.of(
                        new NbtCopy("componentSecurity.owner", "owner"),
                        new NbtCopy("componentSecurity.securityMode", "securityMode"),
                        new NbtCopy("componentUpgrade", "componentUpgrade"),
                        new NbtCopy("controlType", "controlType"),
                        new NbtCopy("EnergyContainers", "EnergyContainers"),
                        new NbtCopy("Items", "Items"),
                        new NbtCopy("linkActive", "linkActive"),
                        new NbtCopy("consumePsiPerTick", "consumePsiPerTick"),
                        new NbtCopy("lastOwnerUUID", "lastOwnerUUID")
                )
        ));
        save(futures, output, PsitweaksMekanismBlocks.TRANSCENDENT_ENERGY_CUBE.getBlock(), machineDrop(
                PsitweaksMekanismBlocks.TRANSCENDENT_ENERGY_CUBE.getBlock(),
                nbtPaths("Items")
        ));

        dropSelf(futures, output, requiredBlock("psi", "psimetal_block"));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "PsiTweaks Block Loot Tables";
    }

    private void dropSelf(List<CompletableFuture<?>> futures, CachedOutput output, Block block) {
        JsonObject table = blockTable();
        JsonObject pool = pool(1);
        JsonArray entries = new JsonArray();
        entries.add(itemEntry(name(block)));
        pool.add("entries", entries);
        JsonArray conditions = new JsonArray();
        JsonObject survivesExplosion = new JsonObject();
        survivesExplosion.addProperty("condition", "minecraft:survives_explosion");
        conditions.add(survivesExplosion);
        pool.add("conditions", conditions);
        addPool(table, pool);
        save(futures, output, block, table);
    }

    private static JsonObject oreDrop(Block block, Item item) {
        JsonObject table = blockTable();
        JsonObject pool = pool(1);
        JsonArray entries = new JsonArray();
        JsonObject alternatives = new JsonObject();
        alternatives.addProperty("type", "minecraft:alternatives");
        JsonArray children = new JsonArray();

        JsonObject silkTouchEntry = itemEntry(name(block));
        JsonArray silkTouchConditions = new JsonArray();
        JsonObject matchTool = new JsonObject();
        matchTool.addProperty("condition", "minecraft:match_tool");
        JsonObject predicate = new JsonObject();
        JsonArray enchantments = new JsonArray();
        JsonObject silkTouch = new JsonObject();
        silkTouch.addProperty("enchantment", "minecraft:silk_touch");
        JsonObject levels = new JsonObject();
        levels.addProperty("min", 1);
        silkTouch.add("levels", levels);
        enchantments.add(silkTouch);
        predicate.add("enchantments", enchantments);
        matchTool.add("predicate", predicate);
        silkTouchConditions.add(matchTool);
        silkTouchEntry.add("conditions", silkTouchConditions);
        children.add(silkTouchEntry);

        JsonObject rawEntry = itemEntry(name(item));
        JsonArray functions = new JsonArray();
        JsonObject applyBonus = new JsonObject();
        applyBonus.addProperty("function", "minecraft:apply_bonus");
        applyBonus.addProperty("enchantment", "minecraft:fortune");
        applyBonus.addProperty("formula", "minecraft:ore_drops");
        functions.add(applyBonus);
        JsonObject explosionDecay = new JsonObject();
        explosionDecay.addProperty("function", "minecraft:explosion_decay");
        functions.add(explosionDecay);
        rawEntry.add("functions", functions);
        children.add(rawEntry);

        alternatives.add("children", children);
        entries.add(alternatives);
        pool.add("entries", entries);
        addPool(table, pool);
        return table;
    }

    private static JsonObject machineDrop(Block block, List<NbtCopy> nbtPaths) {
        JsonObject table = blockTable();
        JsonObject pool = pool(1.0);
        pool.addProperty("bonus_rolls", 0.0);
        pool.addProperty("name", "main");
        JsonArray entries = new JsonArray();
        JsonObject entry = itemEntry(name(block));
        JsonArray functions = new JsonArray();

        JsonObject copyName = new JsonObject();
        copyName.addProperty("function", "minecraft:copy_name");
        copyName.addProperty("source", "block_entity");
        functions.add(copyName);

        JsonObject copyNbt = new JsonObject();
        copyNbt.addProperty("function", "minecraft:copy_nbt");
        copyNbt.addProperty("source", "block_entity");
        JsonArray ops = new JsonArray();
        for (NbtCopy path : nbtPaths) {
            JsonObject op = new JsonObject();
            op.addProperty("op", "replace");
            op.addProperty("source", path.source());
            op.addProperty("target", "mekData." + path.target());
            ops.add(op);
        }
        copyNbt.add("ops", ops);
        functions.add(copyNbt);

        entry.add("functions", functions);
        entries.add(entry);
        pool.add("entries", entries);
        addPool(table, pool);
        table.addProperty("random_sequence", block.getLootTable().toString());
        return table;
    }

    private void save(List<CompletableFuture<?>> futures, CachedOutput output, Block block, JsonObject table) {
        Path path = pathProvider.json(block.getLootTable());
        futures.add(DataProvider.saveStable(output, table, path));
    }

    private static JsonObject blockTable() {
        JsonObject table = new JsonObject();
        table.addProperty("type", "minecraft:block");
        table.add("pools", new JsonArray());
        return table;
    }

    private static JsonObject pool(Number rolls) {
        JsonObject pool = new JsonObject();
        pool.addProperty("rolls", rolls);
        return pool;
    }

    private static void addPool(JsonObject table, JsonObject pool) {
        table.getAsJsonArray("pools").add(pool);
    }

    private static JsonObject itemEntry(String itemName) {
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", itemName);
        return entry;
    }

    private static String name(Block block) {
        ResourceLocation location = ForgeRegistries.BLOCKS.getKey(block);
        if (location == null) {
            throw new IllegalStateException("Missing block registry name: " + block);
        }
        return location.toString();
    }

    private static String name(Item item) {
        ResourceLocation location = ForgeRegistries.ITEMS.getKey(item);
        if (location == null) {
            throw new IllegalStateException("Missing item registry name: " + item);
        }
        return location.toString();
    }

    private static List<NbtCopy> nbtPaths(String... additionalPaths) {
        return java.util.stream.Stream.concat(
                COMMON_MACHINE_NBT_PATHS.stream(),
                java.util.Arrays.stream(additionalPaths).map(path -> new NbtCopy(path, path))
        ).toList();
    }

    private static Block requiredBlock(String namespace, String path) {
        Block block = ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath(namespace, path));
        if (block == null) {
            throw new IllegalStateException("Missing block: " + namespace + ":" + path);
        }
        return block;
    }

    private record NbtCopy(String source, String target) {
    }
}
