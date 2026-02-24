package com.moratan251.psitweaks.client.jei;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class MaterialMutationJeiCategory implements IRecipeCategory<MaterialMutationJeiRecipe> {

    public static final RecipeType<MaterialMutationJeiRecipe> RECIPE_TYPE =
            RecipeType.create(Psitweaks.MOD_ID, "material_mutation", MaterialMutationJeiRecipe.class);

    private static final int WIDTH = 116;
    private static final int HEIGHT = 36;
    private static final int INPUT_X = 20;
    private static final int INPUT_Y = 10;
    private static final int OUTPUT_X = 80;
    private static final int OUTPUT_Y = 10;
    private static final int ARROW_X = 48;
    private static final int ARROW_Y = 10;

    private final IDrawable icon;
    private final IDrawable slotDrawable;
    private final IDrawable outputSlotDrawable;
    private final IDrawable arrowDrawable;

    public MaterialMutationJeiCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(PsitweaksItems.PROGRAM_MATERIAL_MUTATION.get()));
        this.slotDrawable = guiHelper.getSlotDrawable();
        this.outputSlotDrawable = guiHelper.getOutputSlot();
        this.arrowDrawable = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<MaterialMutationJeiRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.psitweaks.material_mutation");
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
    public void setRecipe(IRecipeLayoutBuilder builder, MaterialMutationJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X, INPUT_Y)
                .setBackground(slotDrawable, -1, -1)
                .addItemStack(recipe.input());

        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, OUTPUT_Y)
                .setBackground(outputSlotDrawable, -5, -5)
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(MaterialMutationJeiRecipe recipe, mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView,
                     net.minecraft.client.gui.GuiGraphics graphics, double mouseX, double mouseY) {
        arrowDrawable.draw(graphics, ARROW_X, ARROW_Y);
    }
}
