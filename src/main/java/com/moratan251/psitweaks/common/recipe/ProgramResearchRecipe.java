package com.moratan251.psitweaks.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.moratan251.psitweaks.common.registries.PsitweaksRecipeSerializers;
import com.moratan251.psitweaks.common.registries.PsitweaksRecipeTypes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class ProgramResearchRecipe implements Recipe<MachineRecipeInput> {
    public static final int MAX_INPUT_SLOTS = 9;
    private static final int FLOW_INF = 1_000_000_000;

    private final List<RequiredInput> inputs;
    private final ItemStack output;
    private final int energy;
    private final int time;

    public ProgramResearchRecipe(List<RequiredInput> inputs, ItemStack output, int energy, int time) {
        this.inputs = List.copyOf(inputs);
        this.output = output.copy();
        this.energy = Math.max(0, energy);
        this.time = Math.max(1, time);
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
        if (energy <= 0) {
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
        ItemStack[] availableStacks = new ItemStack[slotCount];
        int[] sourceCapacities = new int[slotCount];
        int totalAvailable = 0;
        for (int slot = 0; slot < slotCount; slot++) {
            ItemStack stack = stackProvider.apply(slot);
            availableStacks[slot] = stack;
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
            residual[source][slotStart + slot] = sourceCapacities[slot];
        }

        for (int reqIndex = 0; reqIndex < requirementCount; reqIndex++) {
            RequiredInput requiredInput = inputs.get(reqIndex);
            int reqNode = requirementStart + reqIndex;
            residual[reqNode][sink] = requiredInput.count();

            for (int slot = 0; slot < slotCount; slot++) {
                if (requiredInput.matches(availableStacks[slot])) {
                    residual[slotStart + slot][reqNode] = FLOW_INF;
                }
            }
        }

        if (calculateMaxFlow(residual, source, sink) < totalRequired) {
            return null;
        }

        int[] consumedPerSlot = new int[slotCount];
        for (int slot = 0; slot < slotCount; slot++) {
            int consumed = 0;
            for (int reqIndex = 0; reqIndex < requirementCount; reqIndex++) {
                RequiredInput requiredInput = inputs.get(reqIndex);
                if (requiredInput.consume()) {
                    consumed += residual[requirementStart + reqIndex][slotStart + slot];
                }
            }
            consumedPerSlot[slot] = consumed;
        }
        return consumedPerSlot;
    }

    private static int calculateMaxFlow(int[][] residual, int source, int sink) {
        int maxFlow = 0;
        int[] parent = new int[residual.length];

        while (true) {
            Arrays.fill(parent, -1);
            parent[source] = source;
            int[] pathCapacity = new int[residual.length];
            pathCapacity[source] = Integer.MAX_VALUE;
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            queue.add(source);

            while (!queue.isEmpty() && parent[sink] == -1) {
                int current = queue.removeFirst();
                for (int next = 0; next < residual.length; next++) {
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
    public boolean matches(MachineRecipeInput input, Level level) {
        int slotCount = Math.min(input.size(), MAX_INPUT_SLOTS);
        return createConsumptionPlan(input::getItem, slotCount) != null;
    }

    @Override
    public ItemStack assemble(MachineRecipeInput input, HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output.copy();
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

    public record RequiredInput(Ingredient ingredient, int count, boolean consume) {
        private static final Codec<RequiredInput> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(RequiredInput::ingredient),
                Codec.INT.optionalFieldOf("count", 1).forGetter(RequiredInput::count),
                Codec.BOOL.optionalFieldOf("consume", true).forGetter(RequiredInput::consume)
        ).apply(instance, RequiredInput::new));

        public RequiredInput {
            count = Math.max(1, count);
        }

        public boolean matches(ItemStack stack) {
            return !stack.isEmpty() && ingredient.test(stack);
        }
    }

    public static class Serializer implements RecipeSerializer<ProgramResearchRecipe> {
        private static final MapCodec<ProgramResearchRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                RequiredInput.CODEC.listOf(1, MAX_INPUT_SLOTS).fieldOf("inputs").forGetter(ProgramResearchRecipe::getInputs),
                ItemStack.STRICT_CODEC.fieldOf("output").forGetter(recipe -> recipe.output),
                Codec.INT.optionalFieldOf("energy", 0).forGetter(ProgramResearchRecipe::getEnergy),
                Codec.INT.optionalFieldOf("time", 200).forGetter(ProgramResearchRecipe::getTime)
        ).apply(instance, ProgramResearchRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, ProgramResearchRecipe> STREAM_CODEC =
                StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        @Override
        public MapCodec<ProgramResearchRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ProgramResearchRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static ProgramResearchRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            int inputSize = buf.readVarInt();
            List<RequiredInput> inputs = new ArrayList<>(inputSize);
            for (int i = 0; i < inputSize; i++) {
                Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                int count = Math.max(1, buf.readVarInt());
                boolean consume = buf.readBoolean();
                inputs.add(new RequiredInput(ingredient, count, consume));
            }
            ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
            int energy = Math.max(0, buf.readVarInt());
            int time = Math.max(1, buf.readVarInt());
            return new ProgramResearchRecipe(inputs, output, energy, time);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, ProgramResearchRecipe recipe) {
            buf.writeVarInt(recipe.inputs.size());
            for (RequiredInput input : recipe.inputs) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, input.ingredient());
                buf.writeVarInt(input.count());
                buf.writeBoolean(input.consume());
            }
            ItemStack.STREAM_CODEC.encode(buf, recipe.output);
            buf.writeVarInt(recipe.energy);
            buf.writeVarInt(recipe.time);
        }
    }
}
