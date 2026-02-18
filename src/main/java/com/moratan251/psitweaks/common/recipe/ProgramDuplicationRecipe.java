package com.moratan251.psitweaks.common.recipe;

import com.moratan251.psitweaks.common.items.ItemPsitweaksProgram;
import com.moratan251.psitweaks.common.registries.PsitweaksRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class ProgramDuplicationRecipe extends CustomRecipe {

    public ProgramDuplicationRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return findMatch(container) != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        MatchData match = findMatch(container);
        if (match == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = match.programStack().copy();
        result.setCount(1);
        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> remainingItems = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        MatchData match = findMatch(container);
        if (match == null) {
            return remainingItems;
        }

        ItemStack keepSource = match.programStack().copy();
        keepSource.setCount(1);
        remainingItems.set(match.programSlot(), keepSource);
        return remainingItems;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PsitweaksRecipeSerializers.PROGRAM_DUPLICATION.get();
    }

    private MatchData findMatch(CraftingContainer container) {
        int blankCount = 0;
        int programCount = 0;
        int programSlot = -1;
        ItemStack programStack = ItemStack.EMPTY;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (ItemPsitweaksProgram.isBlankProgram(stack)) {
                blankCount++;
            } else if (ItemPsitweaksProgram.isNonBlankProgram(stack)) {
                programCount++;
                programSlot = i;
                programStack = stack;
            } else {
                return null;
            }

            if (blankCount > 1 || programCount > 1) {
                return null;
            }
        }

        if (blankCount != 1 || programCount != 1 || programStack.isEmpty()) {
            return null;
        }

        return new MatchData(programSlot, programStack);
    }

    private record MatchData(int programSlot, ItemStack programStack) {
    }
}
