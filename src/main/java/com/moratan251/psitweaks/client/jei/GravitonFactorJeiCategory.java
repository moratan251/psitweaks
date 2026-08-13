package com.moratan251.psitweaks.client.jei;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
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

public class GravitonFactorJeiCategory implements IRecipeCategory<GravitonFactorJeiRecipe> {
    public static final RecipeType<GravitonFactorJeiRecipe> RECIPE_TYPE =
            RecipeType.create(Psitweaks.MOD_ID, "graviton_factor_acquisition", GravitonFactorJeiRecipe.class);

    private static final int WIDTH = 176;
    private static final int HEIGHT = 60;
    private static final int INPUT_X = 32;
    private static final int INPUT_Y = 34;
    private static final int OUTPUT_X = 132;
    private static final int OUTPUT_Y = 34;
    private static final int ARROW_X = 76;
    private static final int ARROW_Y = 34;
    private static final int TEXT_Y = 8;

    private final IDrawable icon;
    private final IDrawable slotDrawable;
    private final IDrawable outputSlotDrawable;
    private final IDrawable arrowDrawable;

    public GravitonFactorJeiCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(PsitweaksItems.GRAVITON_FACTOR.get()));
        this.slotDrawable = guiHelper.getSlotDrawable();
        this.outputSlotDrawable = guiHelper.getOutputSlot();
        this.arrowDrawable = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<GravitonFactorJeiRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.psitweaks.graviton_factor_acquisition");
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
    public boolean needsRecipeBorder() {
        return false;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GravitonFactorJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X, INPUT_Y)
                .setBackground(slotDrawable, -1, -1)
                .addItemStack(recipe.input());

        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, OUTPUT_Y)
                .setBackground(outputSlotDrawable, -5, -5)
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(GravitonFactorJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        arrowDrawable.draw(graphics, ARROW_X, ARROW_Y);

        Font font = Minecraft.getInstance().font;
        Component description = Component.translatable("jei.psitweaks.graviton_factor_acquisition.fall");
        int textX = (WIDTH - font.width(description)) / 2;
        graphics.drawString(font, description, textX, TEXT_Y, 0x202020, false);
    }
}
