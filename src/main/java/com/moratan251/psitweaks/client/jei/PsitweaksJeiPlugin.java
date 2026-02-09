package com.moratan251.psitweaks.client.jei;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import com.moratan251.psitweaks.common.tile.machine.TileEntitySculkEroder;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.machine.ItemStackToItemStackRecipeCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.List;

@JeiPlugin
public class PsitweaksJeiPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_ID = Psitweaks.location("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ItemStackToItemStackRecipeCategory(
                registration.getJeiHelpers().getGuiHelper(),
                PsitweaksMekanismJeiRecipeTypes.SCULK_ERODER,
                PsitweaksMekanismBlocks.SCULK_ERODER
        ));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeType<ItemStackToItemStackRecipe> recipeType = MekanismJEI.recipeType(PsitweaksMekanismJeiRecipeTypes.SCULK_ERODER);
        Level level = Minecraft.getInstance().level;
        List<ItemStackToItemStackRecipe> recipes = level == null ? List.of() : TileEntitySculkEroder.getAllSculkRecipes(level);
        registration.addRecipes(recipeType, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        RecipeType<ItemStackToItemStackRecipe> recipeType = MekanismJEI.recipeType(PsitweaksMekanismJeiRecipeTypes.SCULK_ERODER);
        registration.addRecipeCatalyst(PsitweaksMekanismBlocks.SCULK_ERODER.getBlock(), recipeType);
    }
}
