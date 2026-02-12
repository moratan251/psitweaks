package com.moratan251.psitweaks.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.moratan251.psitweaks.common.registries.PsitweaksRecipeSerializers;
import com.moratan251.psitweaks.common.registries.PsitweaksRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;

public class ProgramResearchRecipe implements Recipe<Container> {

    public static final int MAX_INPUT_SLOTS = 9;
    private static final int FLOW_INF = 1_000_000_000;

    private final ResourceLocation id;
    private final List<RequiredInput> inputs;
    private final ItemStack output;
    private final int energy;
    private final int time;

    public ProgramResearchRecipe(ResourceLocation id, List<RequiredInput> inputs, ItemStack output, int energy, int time) {
        this.id = id;
        this.inputs = List.copyOf(inputs);
        this.output = output.copy();
        this.energy = energy;
        this.time = time;
    }

    public List<RequiredInput> getInputs() {
        return inputs;
    }

    public ItemStack getOutput() {
        return output.copy();
    }

    public int getEnergy() {
        return energy;
    }

    public int getTime() {
        return time;
    }

    public int getEnergyCostForTick(int progress) {
        if (energy <= 0 || time <= 0) {
            return 0;
        }
        int base = energy / time;
        int remainder = energy % time;
        return progress < remainder ? base + 1 : base;
    }

    public @Nullable int[] createConsumptionPlan(IItemHandler inputInventory) {
        int slotCount = Math.min(inputInventory.getSlots(), MAX_INPUT_SLOTS);
        return createConsumptionPlan(inputInventory::getStackInSlot, slotCount);
    }

    private @Nullable int[] createConsumptionPlan(IntFunction<ItemStack> stackProvider, int slotCount) {
        if (inputs.isEmpty() || slotCount <= 0) {
            return null;
        }

        int requirementCount = inputs.size();
        int[] sourceCapacities = new int[slotCount];
        int totalAvailable = 0;
        for (int slot = 0; slot < slotCount; slot++) {
            ItemStack stack = stackProvider.apply(slot);
            int count = stack.isEmpty() ? 0 : stack.getCount();
            sourceCapacities[slot] = count;
            totalAvailable += count;
        }

        int totalRequired = inputs.stream().mapToInt(RequiredInput::count).sum();
        if (totalAvailable < totalRequired) {
            return null;
        }

        int source = 0;
        int slotStart = 1;
        int requirementStart = slotStart + slotCount;
        int sink = requirementStart + requirementCount;
        int[][] residual = new int[sink + 1][sink + 1];

        for (int slot = 0; slot < slotCount; slot++) {
            int slotNode = slotStart + slot;
            residual[source][slotNode] = sourceCapacities[slot];
        }

        for (int reqIndex = 0; reqIndex < requirementCount; reqIndex++) {
            RequiredInput requiredInput = inputs.get(reqIndex);
            int reqNode = requirementStart + reqIndex;
            residual[reqNode][sink] = requiredInput.count();

            for (int slot = 0; slot < slotCount; slot++) {
                ItemStack stack = stackProvider.apply(slot);
                if (requiredInput.matches(stack)) {
                    int slotNode = slotStart + slot;
                    residual[slotNode][reqNode] = FLOW_INF;
                }
            }
        }

        int flow = calculateMaxFlow(residual, source, sink);
        if (flow < totalRequired) {
            return null;
        }

        int[] consumedPerSlot = new int[slotCount];
        for (int slot = 0; slot < slotCount; slot++) {
            int slotNode = slotStart + slot;
            consumedPerSlot[slot] = sourceCapacities[slot] - residual[source][slotNode];
        }
        return consumedPerSlot;
    }

    private static int calculateMaxFlow(int[][] residual, int source, int sink) {
        int nodeCount = residual.length;
        int maxFlow = 0;
        int[] parent = new int[nodeCount];

        while (true) {
            Arrays.fill(parent, -1);
            parent[source] = source;

            int[] pathCapacity = new int[nodeCount];
            pathCapacity[source] = Integer.MAX_VALUE;

            ArrayDeque<Integer> queue = new ArrayDeque<>();
            queue.add(source);

            while (!queue.isEmpty() && parent[sink] == -1) {
                int current = queue.removeFirst();
                for (int next = 0; next < nodeCount; next++) {
                    if (parent[next] == -1 && residual[current][next] > 0) {
                        parent[next] = current;
                        pathCapacity[next] = Math.min(pathCapacity[current], residual[current][next]);
                        if (next == sink) {
                            break;
                        }
                        queue.addLast(next);
                    }
                }
            }

            if (parent[sink] == -1) {
                break;
            }

            int augment = pathCapacity[sink];
            maxFlow += augment;
            int node = sink;
            while (node != source) {
                int prev = parent[node];
                residual[prev][node] -= augment;
                residual[node][prev] += augment;
                node = prev;
            }
        }

        return maxFlow;
    }

    @Override
    public boolean matches(Container container, Level level) {
        int slotCount = Math.min(container.getContainerSize(), MAX_INPUT_SLOTS);
        return createConsumptionPlan(container::getItem, slotCount) != null;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PsitweaksRecipeSerializers.PROGRAM_RESEARCH.get();
    }

    @Override
    public RecipeType<?> getType() {
        return PsitweaksRecipeTypes.PROGRAM_RESEARCH.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (RequiredInput input : inputs) {
            ingredients.add(input.ingredient());
        }
        return ingredients;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public record RequiredInput(Ingredient ingredient, int count) {

        public RequiredInput {
            if (count < 1) {
                throw new IllegalArgumentException("Program research input count must be >= 1");
            }
        }

        public boolean matches(ItemStack stack) {
            return !stack.isEmpty() && ingredient.test(stack);
        }
    }

    public static class Serializer implements RecipeSerializer<ProgramResearchRecipe> {

        @Override
        public ProgramResearchRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            JsonArray inputArray = GsonHelper.getAsJsonArray(json, "inputs");
            if (inputArray.size() == 0) {
                throw new IllegalArgumentException("Program research recipe has no inputs: " + recipeId);
            }
            if (inputArray.size() > MAX_INPUT_SLOTS) {
                throw new IllegalArgumentException("Program research recipe supports up to " + MAX_INPUT_SLOTS + " inputs: " + recipeId);
            }

            List<RequiredInput> inputs = new ArrayList<>(inputArray.size());
            for (int i = 0; i < inputArray.size(); i++) {
                JsonObject inputObject = GsonHelper.convertToJsonObject(inputArray.get(i), "inputs[" + i + "]");
                JsonElement ingredientElement = GsonHelper.isValidNode(inputObject, "ingredient")
                        ? inputObject.get("ingredient")
                        : inputObject;
                Ingredient ingredient = Ingredient.fromJson(ingredientElement);
                int inputCount = GsonHelper.getAsInt(inputObject, "count", 1);
                if (inputCount < 1) {
                    throw new IllegalArgumentException("Program research input count must be >= 1: " + recipeId);
                }
                inputs.add(new RequiredInput(ingredient, inputCount));
            }

            JsonObject outputObject = GsonHelper.getAsJsonObject(json, "output");
            ItemStack output = CraftingHelper.getItemStack(outputObject, true);
            if (output.isEmpty()) {
                throw new IllegalArgumentException("Program research output cannot be empty: " + recipeId);
            }

            int energy = GsonHelper.getAsInt(json, "energy", 0);
            if (energy < 0) {
                throw new IllegalArgumentException("Program research energy must be >= 0: " + recipeId);
            }

            int time = GsonHelper.getAsInt(json, "time", 200);
            if (time < 1) {
                throw new IllegalArgumentException("Program research time must be >= 1: " + recipeId);
            }

            return new ProgramResearchRecipe(recipeId, inputs, output, energy, time);
        }

        @Override
        public ProgramResearchRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            int inputSize = buffer.readVarInt();
            List<RequiredInput> inputs = new ArrayList<>(inputSize);
            for (int i = 0; i < inputSize; i++) {
                Ingredient ingredient = Ingredient.fromNetwork(buffer);
                int count = Math.max(1, buffer.readVarInt());
                inputs.add(new RequiredInput(ingredient, count));
            }
            ItemStack output = buffer.readItem();
            int energy = Math.max(0, buffer.readVarInt());
            int time = Math.max(1, buffer.readVarInt());
            return new ProgramResearchRecipe(recipeId, inputs, output, energy, time);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ProgramResearchRecipe recipe) {
            buffer.writeVarInt(recipe.inputs.size());
            for (RequiredInput input : recipe.inputs) {
                input.ingredient().toNetwork(buffer);
                buffer.writeVarInt(input.count());
            }
            buffer.writeItem(recipe.output);
            buffer.writeVarInt(recipe.energy);
            buffer.writeVarInt(recipe.time);
        }
    }
}
