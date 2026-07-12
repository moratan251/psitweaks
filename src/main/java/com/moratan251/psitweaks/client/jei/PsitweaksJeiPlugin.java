package com.moratan251.psitweaks.client.jei;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.handler.MaterialMutationRecipeHandler;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

@JeiPlugin
public class PsitweaksJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_ID = Psitweaks.location("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        if (MekanismCompat.isMekanismLoaded()) {
            PsitweaksMekanismJeiPlugin.registerCategories(registration);
        }
        registration.addRecipeCategories(new MaterialMutationJeiCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (MekanismCompat.isMekanismLoaded()) {
            PsitweaksMekanismJeiPlugin.registerRecipes(registration);
        }
        registration.addRecipes(MaterialMutationJeiCategory.RECIPE_TYPE, getMaterialMutationJeiRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        if (MekanismCompat.isMekanismLoaded()) {
            PsitweaksMekanismJeiPlugin.registerRecipeCatalysts(registration);
        }
        registration.addRecipeCatalyst(PsitweaksItems.PROGRAM_MATERIAL_MUTATION.get(), MaterialMutationJeiCategory.RECIPE_TYPE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        if (MekanismCompat.isMekanismLoaded()) {
            PsitweaksMekanismJeiPlugin.registerGuiHandlers(registration);
        }
    }

    private static List<MaterialMutationJeiRecipe> getMaterialMutationJeiRecipes() {
        List<MaterialMutationJeiRecipe> recipes = new ArrayList<>();
        for (Map.Entry<Block, ItemStack> entry : MaterialMutationRecipeHandler.getAllMutationOutputs().entrySet()) {
            ItemStack output = entry.getValue();
            if (output.isEmpty()) {
                continue;
            }

            ItemStack input = new ItemStack(entry.getKey());
            if (input.isEmpty() || input.is(Items.AIR)) {
                continue;
            }
            recipes.add(new MaterialMutationJeiRecipe(input, output));
        }
        return recipes;
    }
}
