package com.moratan251.psitweaks.client.jei;

import com.moratan251.psitweaks.client.gui.machine.GuiMaterialMutator;
import com.moratan251.psitweaks.client.gui.machine.GuiProgramResearcher;
import com.moratan251.psitweaks.client.gui.machine.GuiSculkEroder;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import com.moratan251.psitweaks.common.registries.PsitweaksRecipeTypes;
import com.moratan251.psitweaks.common.tile.machine.MaterialMutatorBlockEntity;
import com.moratan251.psitweaks.common.tile.machine.SculkEroderBlockEntity;
import java.util.List;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.client.recipe_viewer.jei.machine.ItemStackChemicalToItemStackRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.ItemStackToItemStackRecipeCategory;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

final class PsitweaksMekanismJeiPlugin {
    private PsitweaksMekanismJeiPlugin() {
    }

    static void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ItemStackToItemStackRecipeCategory(
                registration.getJeiHelpers().getGuiHelper(),
                PsitweaksMekanismJeiRecipeTypes.SCULK_ERODER
        ));
        registration.addRecipeCategories(new ItemStackChemicalToItemStackRecipeCategory(
                registration.getJeiHelpers().getGuiHelper(),
                PsitweaksMekanismJeiRecipeTypes.MATERIAL_MUTATOR
        ));
        registration.addRecipeCategories(new ProgramResearchJeiCategory(
                registration.getJeiHelpers().getGuiHelper()
        ));
    }

    static void registerRecipes(IRecipeRegistration registration) {
        Level level = Minecraft.getInstance().level;
        List<RecipeHolder<ItemStackToItemStackRecipe>> recipes = level == null
                ? List.of()
                : SculkEroderBlockEntity.getAllSculkRecipeHolders(level);
        registration.addRecipes(sculkEroderRecipeType(), recipes);

        List<RecipeHolder<ItemStackChemicalToItemStackRecipe>> materialMutatorRecipes = level == null
                ? List.of()
                : MaterialMutatorBlockEntity.getAllMutationMachineRecipeHolders(level);
        registration.addRecipes(materialMutatorRecipeType(), materialMutatorRecipes);

        List<ProgramResearchJeiRecipe> programResearchRecipes = level == null
                ? List.of()
                : level.getRecipeManager().getAllRecipesFor(PsitweaksRecipeTypes.PROGRAM_RESEARCH.get()).stream()
                        .map(holder -> new ProgramResearchJeiRecipe(holder.id(), holder.value()))
                        .toList();
        registration.addRecipes(ProgramResearchJeiCategory.RECIPE_TYPE, programResearchRecipes);
    }

    static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(PsitweaksMekanismBlocks.SCULK_ERODER.get(), sculkEroderRecipeType());
        registration.addRecipeCatalyst(PsitweaksMekanismBlocks.MATERIAL_MUTATOR.get(), materialMutatorRecipeType());
        registration.addRecipeCatalyst(
                PsitweaksMekanismBlocks.PROGRAM_RESEARCHER.get(),
                ProgramResearchJeiCategory.RECIPE_TYPE
        );
    }

    static void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(GuiSculkEroder.class, 78, 34, 24, 17, sculkEroderRecipeType());
        registration.addRecipeClickArea(GuiMaterialMutator.class, 78, 34, 24, 17, materialMutatorRecipeType());
        registration.addRecipeClickArea(
                GuiProgramResearcher.class,
                104,
                38,
                24,
                17,
                ProgramResearchJeiCategory.RECIPE_TYPE
        );
    }

    private static RecipeType<RecipeHolder<ItemStackToItemStackRecipe>> sculkEroderRecipeType() {
        return MekanismJEI.holderRecipeType(PsitweaksMekanismJeiRecipeTypes.SCULK_ERODER);
    }

    private static RecipeType<RecipeHolder<ItemStackChemicalToItemStackRecipe>> materialMutatorRecipeType() {
        return MekanismJEI.holderRecipeType(PsitweaksMekanismJeiRecipeTypes.MATERIAL_MUTATOR);
    }
}
