package com.moratan251.psitweaks.client.jei;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.recipe.ProgramResearchRecipe;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ProgramResearchJeiCategory implements IRecipeCategory<ProgramResearchJeiRecipe> {
    public static final RecipeType<ProgramResearchJeiRecipe> RECIPE_TYPE =
            RecipeType.create(Psitweaks.MOD_ID, "program_research", ProgramResearchJeiRecipe.class);

    private static final int WIDTH = 176;
    private static final int HEIGHT = 88;
    private static final int INPUT_START_X = 8;
    private static final int INPUT_START_Y = 12;
    private static final int OUTPUT_X = 148;
    private static final int OUTPUT_Y = 28;
    private static final int ARROW_X = 90;
    private static final int ARROW_Y = 28;
    private static final int TEXT_X = 8;
    private static final int ENERGY_Y = 68;
    private static final int TIME_Y = 79;

    private final IDrawable icon;
    private final IDrawable slotDrawable;
    private final IDrawable outputSlotDrawable;
    private final IDrawable arrowDrawable;

    public ProgramResearchJeiCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(PsitweaksMekanismBlocks.PROGRAM_RESEARCHER.get()));
        this.slotDrawable = guiHelper.getSlotDrawable();
        this.outputSlotDrawable = guiHelper.getOutputSlot();
        this.arrowDrawable = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<ProgramResearchJeiRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.psitweaks.program_research");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ProgramResearchJeiRecipe jeiRecipe, IFocusGroup focuses) {
        ProgramResearchRecipe recipe = jeiRecipe.recipe();
        List<ProgramResearchRecipe.RequiredInput> inputs = recipe.getInputs();
        for (int i = 0; i < ProgramResearchRecipe.MAX_INPUT_SLOTS; i++) {
            int x = INPUT_START_X + (i % 3) * 18;
            int y = INPUT_START_Y + (i / 3) * 18;

            var inputSlot = builder.addSlot(getInputRole(inputs, i), x, y)
                    .setBackground(slotDrawable, -1, -1);
            if (i < inputs.size()) {
                inputSlot.addItemStacks(toDisplayStacks(inputs.get(i)));
            }
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, OUTPUT_Y)
                .setBackground(outputSlotDrawable, -5, -5)
                .addItemStack(recipe.getOutput());
    }

    @Override
    public void draw(ProgramResearchJeiRecipe jeiRecipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        ProgramResearchRecipe recipe = jeiRecipe.recipe();
        arrowDrawable.draw(graphics, ARROW_X, ARROW_Y);

        Font font = Minecraft.getInstance().font;
        int totalSeconds = Math.max(1, (recipe.getTime() + 19) / 20);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        graphics.drawString(
                font,
                Component.translatable("jei.psitweaks.program_research.energy", recipe.getEnergy()),
                TEXT_X,
                ENERGY_Y,
                0x202020,
                false
        );
        graphics.drawString(
                font,
                Component.translatable("jei.psitweaks.program_research.time", minutes, seconds),
                TEXT_X,
                TIME_Y,
                0x202020,
                false
        );
    }

    private static RecipeIngredientRole getInputRole(List<ProgramResearchRecipe.RequiredInput> inputs, int index) {
        if (index < inputs.size() && !inputs.get(index).consume()) {
            return RecipeIngredientRole.CATALYST;
        }
        return RecipeIngredientRole.INPUT;
    }

    private static List<ItemStack> toDisplayStacks(ProgramResearchRecipe.RequiredInput input) {
        ItemStack[] stacks = input.ingredient().getItems();
        List<ItemStack> result = new ArrayList<>(stacks.length);
        for (ItemStack stack : stacks) {
            ItemStack copy = stack.copy();
            copy.setCount(input.count());
            result.add(copy);
        }
        return result;
    }
}
