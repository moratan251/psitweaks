package com.moratan251.psitweaks.datagen.builders;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiConsumer;

public final class MaterialMutationRecipeBuilder {

    private MaterialMutationRecipeBuilder() {
    }

    public static TrickRecipeBuilder trick(Block inputBlock) {
        return new TrickRecipeBuilder(inputBlock);
    }

    public static MachineRecipeBuilder machine(ItemLike inputItem, ItemLike outputItem, int time) {
        return new MachineRecipeBuilder(inputItem.asItem(), outputItem.asItem(), time);
    }

    public static final class TrickRecipeBuilder {
        private final Block inputBlock;
        private int outputCount = 1;
        private boolean outputIsBlock = false;
        @Nullable
        private Item outputItem;
        @Nullable
        private Block outputBlock;

        private TrickRecipeBuilder(Block inputBlock) {
            this.inputBlock = Objects.requireNonNull(inputBlock, "inputBlock");
        }

        public TrickRecipeBuilder outputItem(ItemLike item, int count) {
            if (count < 1) {
                throw new IllegalArgumentException("Trick output count must be >= 1");
            }
            this.outputItem = Objects.requireNonNull(item, "item").asItem();
            this.outputBlock = null;
            this.outputIsBlock = false;
            this.outputCount = count;
            return this;
        }

        public TrickRecipeBuilder outputBlock(Block block, int count) {
            if (count < 1) {
                throw new IllegalArgumentException("Trick output count must be >= 1");
            }
            this.outputBlock = Objects.requireNonNull(block, "block");
            this.outputItem = null;
            this.outputIsBlock = true;
            this.outputCount = count;
            return this;
        }

        public void save(BiConsumer<ResourceLocation, JsonObject> consumer, ResourceLocation recipeId) {
            Objects.requireNonNull(consumer, "consumer");
            Objects.requireNonNull(recipeId, "recipeId");

            ResourceLocation inputBlockId = requireBlockId(inputBlock, "input block");
            if (outputItem == null && outputBlock == null) {
                throw new IllegalStateException("Trick recipe output is not configured: " + recipeId);
            }

            JsonObject mutation = new JsonObject();

            JsonObject inputObject = new JsonObject();
            inputObject.addProperty("block", inputBlockId.toString());
            mutation.add("input", inputObject);

            JsonObject outputObject = new JsonObject();
            if (outputIsBlock) {
                outputObject.addProperty("block", requireBlockId(outputBlock, "output block").toString());
            } else {
                outputObject.addProperty("item", requireItemId(outputItem, "output item").toString());
            }
            outputObject.addProperty("count", outputCount);
            mutation.add("output", outputObject);

            JsonArray mutations = new JsonArray();
            mutations.add(mutation);

            JsonObject root = new JsonObject();
            root.add("mutations", mutations);

            consumer.accept(recipeId, root);
        }
    }

    public static final class MachineRecipeBuilder {
        private final Item inputItem;
        private final Item outputItem;
        private final int time;
        private int outputCount = 1;

        private MachineRecipeBuilder(Item inputItem, Item outputItem, int time) {
            this.inputItem = Objects.requireNonNull(inputItem, "inputItem");
            this.outputItem = Objects.requireNonNull(outputItem, "outputItem");
            if (time < 1) {
                throw new IllegalArgumentException("Machine recipe time must be >= 1");
            }
            this.time = time;
        }

        public MachineRecipeBuilder outputCount(int outputCount) {
            if (outputCount < 1) {
                throw new IllegalArgumentException("Machine recipe output count must be >= 1");
            }
            this.outputCount = outputCount;
            return this;
        }

        public void save(BiConsumer<ResourceLocation, JsonObject> consumer, ResourceLocation recipeId) {
            Objects.requireNonNull(consumer, "consumer");
            Objects.requireNonNull(recipeId, "recipeId");

            JsonObject root = new JsonObject();

            JsonObject inputObject = new JsonObject();
            inputObject.addProperty("item", requireItemId(inputItem, "machine input item").toString());
            root.add("input", inputObject);

            JsonObject outputObject = new JsonObject();
            outputObject.addProperty("item", requireItemId(outputItem, "machine output item").toString());
            outputObject.addProperty("count", outputCount);
            root.add("output", outputObject);

            root.addProperty("time", time);
            consumer.accept(recipeId, root);
        }
    }

    private static ResourceLocation requireBlockId(@Nullable Block block, String fieldName) {
        ResourceLocation id = block == null ? null : ForgeRegistries.BLOCKS.getKey(block);
        if (id == null) {
            throw new IllegalStateException("Unregistered " + fieldName + " in material mutation recipe");
        }
        return id;
    }

    private static ResourceLocation requireItemId(@Nullable Item item, String fieldName) {
        ResourceLocation id = item == null ? null : ForgeRegistries.ITEMS.getKey(item);
        if (id == null) {
            throw new IllegalStateException("Unregistered " + fieldName + " in material mutation recipe");
        }
        return id;
    }
}
