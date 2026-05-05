package com.moratan251.psitweaks.common.recipe;

import com.moratan251.psitweaks.common.items.ItemPsitweaksProgram;
import com.moratan251.psitweaks.common.registries.PsitweaksRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class ProgramDuplicationRecipe extends CustomRecipe {
    public ProgramDuplicationRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return findMatch(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        MatchData match = findMatch(input);
        if (match == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = match.programStack().copy();
        result.setCount(1);
        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remainingItems = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        MatchData match = findMatch(input);
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

    private MatchData findMatch(CraftingInput input) {
        int blankCount = 0;
        int programCount = 0;
        int programSlot = -1;
        ItemStack programStack = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
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
