package com.moratan251.psitweaks.client.jei;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.client.gui.machine.GuiSculkEroder;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import com.moratan251.psitweaks.common.tile.machine.SculkEroderBlockEntity;
import java.util.List;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.client.recipe_viewer.jei.machine.ItemStackToItemStackRecipeCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

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
                PsitweaksMekanismJeiRecipeTypes.SCULK_ERODER
        ));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Level level = Minecraft.getInstance().level;
        List<RecipeHolder<ItemStackToItemStackRecipe>> recipes = level == null
                ? List.of()
                : SculkEroderBlockEntity.getAllSculkRecipeHolders(level);
        registration.addRecipes(sculkEroderRecipeType(), recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(PsitweaksMekanismBlocks.SCULK_ERODER.get(), sculkEroderRecipeType());
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(GuiSculkEroder.class, 78, 34, 24, 17, sculkEroderRecipeType());
    }

    private static RecipeType<RecipeHolder<ItemStackToItemStackRecipe>> sculkEroderRecipeType() {
        return MekanismJEI.holderRecipeType(PsitweaksMekanismJeiRecipeTypes.SCULK_ERODER);
    }
}
