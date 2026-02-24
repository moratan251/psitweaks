package com.moratan251.psitweaks.client.jei;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.client.gui.machine.GuiProgramResearcher;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.handler.MaterialMutationRecipeHandler;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import com.moratan251.psitweaks.common.registries.PsitweaksRecipeTypes;
import com.moratan251.psitweaks.common.recipe.ProgramResearchRecipe;
import com.moratan251.psitweaks.common.tile.machine.TileEntityMaterialMutator;
import com.moratan251.psitweaks.common.tile.machine.TileEntitySculkEroder;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.machine.ItemStackGasToItemStackRecipeCategory;
import mekanism.client.jei.machine.ItemStackToItemStackRecipeCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
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
        registration.addRecipeCategories(new ItemStackToItemStackRecipeCategory(
                registration.getJeiHelpers().getGuiHelper(),
                PsitweaksMekanismJeiRecipeTypes.SCULK_ERODER,
                PsitweaksMekanismBlocks.SCULK_ERODER
        ));
        registration.addRecipeCategories(new ItemStackGasToItemStackRecipeCategory(
                registration.getJeiHelpers().getGuiHelper(),
                PsitweaksMekanismJeiRecipeTypes.MATERIAL_MUTATOR,
                PsitweaksMekanismBlocks.MATERIAL_MUTATOR
        ));
        registration.addRecipeCategories(new ProgramResearchJeiCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new MaterialMutationJeiCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeType<ItemStackToItemStackRecipe> recipeType = MekanismJEI.recipeType(PsitweaksMekanismJeiRecipeTypes.SCULK_ERODER);
        Level level = Minecraft.getInstance().level;
        List<ItemStackToItemStackRecipe> recipes = level == null ? List.of() : TileEntitySculkEroder.getAllSculkRecipes(level);
        registration.addRecipes(recipeType, recipes);

        RecipeType<ItemStackGasToItemStackRecipe> materialMutatorRecipeType = MekanismJEI.recipeType(PsitweaksMekanismJeiRecipeTypes.MATERIAL_MUTATOR);
        List<ItemStackGasToItemStackRecipe> materialMutatorRecipes = level == null ? List.of() : TileEntityMaterialMutator.getAllMutationMachineRecipes(level);
        registration.addRecipes(materialMutatorRecipeType, materialMutatorRecipes);

        List<ProgramResearchRecipe> programResearchRecipes = level == null
                ? List.of()
                : level.getRecipeManager().getAllRecipesFor(PsitweaksRecipeTypes.PROGRAM_RESEARCH.get());
        registration.addRecipes(ProgramResearchJeiCategory.RECIPE_TYPE, programResearchRecipes);

        registration.addRecipes(MaterialMutationJeiCategory.RECIPE_TYPE, getMaterialMutationJeiRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        RecipeType<ItemStackToItemStackRecipe> recipeType = MekanismJEI.recipeType(PsitweaksMekanismJeiRecipeTypes.SCULK_ERODER);
        RecipeType<ItemStackGasToItemStackRecipe> materialMutatorRecipeType = MekanismJEI.recipeType(PsitweaksMekanismJeiRecipeTypes.MATERIAL_MUTATOR);
        registration.addRecipeCatalyst(PsitweaksMekanismBlocks.SCULK_ERODER.getBlock(), recipeType);
        registration.addRecipeCatalyst(PsitweaksMekanismBlocks.MATERIAL_MUTATOR.getBlock(), materialMutatorRecipeType);
        registration.addRecipeCatalyst(PsitweaksBlocks.PROGRAM_RESEARCHER.get(), ProgramResearchJeiCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(PsitweaksItems.PROGRAM_MATERIAL_MUTATION.get(), MaterialMutationJeiCategory.RECIPE_TYPE);
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
