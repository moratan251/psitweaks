package com.moratan251.psitweaks.client.jei;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.client.gui.machine.GuiProgramResearcher;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.handler.MaterialMutationRecipeHandler;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.registries.PsitweaksRecipeTypes;
import com.moratan251.psitweaks.common.recipe.ProgramResearchRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JeiPlugin
public class PsitweaksJeiPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_ID = Psitweaks.location("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ProgramResearchJeiCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new MaterialMutationJeiCategory(registration.getJeiHelpers().getGuiHelper()));
        if (MekanismCompat.isMekanismLoaded()) {
            MekanismJeiIntegration.registerCategories(registration);
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Level level = Minecraft.getInstance().level;
        List<ProgramResearchRecipe> programResearchRecipes = level == null
                ? List.of()
                : level.getRecipeManager().getAllRecipesFor(PsitweaksRecipeTypes.PROGRAM_RESEARCH.get());
        registration.addRecipes(ProgramResearchJeiCategory.RECIPE_TYPE, programResearchRecipes);
        registration.addRecipes(MaterialMutationJeiCategory.RECIPE_TYPE, getMaterialMutationJeiRecipes());
        if (MekanismCompat.isMekanismLoaded()) {
            MekanismJeiIntegration.registerRecipes(registration, level);
        }
        registration.addIngredientInfo(
                new ItemStack(PsitweaksItems.MAGICIANS_BRAIN.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("description.psitweaks.magicians_brain")
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(PsitweaksBlocks.PROGRAM_RESEARCHER.get(), ProgramResearchJeiCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(PsitweaksItems.PROGRAM_MATERIAL_MUTATION.get(), MaterialMutationJeiCategory.RECIPE_TYPE);
        if (MekanismCompat.isMekanismLoaded()) {
            MekanismJeiIntegration.registerRecipeCatalysts(registration);
        }
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(
                GuiProgramResearcher.class,
                104,
                34,
                24,
                16,
                ProgramResearchJeiCategory.RECIPE_TYPE
        );
    }

    private static List<MaterialMutationJeiRecipe> getMaterialMutationJeiRecipes() {
        List<MaterialMutationJeiRecipe> recipes = new ArrayList<>();
        for (Map.Entry<Block, ItemStack> entry : MaterialMutationRecipeHandler.getAllMutationOutputs().entrySet()) {
            ItemStack output = entry.getValue();
            if (!output.isEmpty()) {
                ItemStack input = new ItemStack(entry.getKey());
                if (!input.isEmpty() && !input.is(Items.AIR)) {
                    recipes.add(new MaterialMutationJeiRecipe(input, output));
                }
            }
        }
        return recipes;
    }

}
