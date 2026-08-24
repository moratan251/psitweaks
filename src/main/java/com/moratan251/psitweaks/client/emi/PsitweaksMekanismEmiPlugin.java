package com.moratan251.psitweaks.client.emi;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.client.jei.PsitweaksMekanismJeiRecipeTypes;
import com.moratan251.psitweaks.common.recipe.ProgramResearchRecipe;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import com.moratan251.psitweaks.common.registries.PsitweaksRecipeTypes;
import com.moratan251.psitweaks.common.tile.machine.MaterialMutatorBlockEntity;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import java.util.LinkedHashMap;
import java.util.Map;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.client.recipe_viewer.emi.MekanismEmi;
import mekanism.client.recipe_viewer.emi.recipe.ItemStackChemicalToItemStackEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.ItemStackToItemStackEmiRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

/** Mekanism型を参照するEMI登録を任意依存境界の内側へ隔離する。 */
final class PsitweaksMekanismEmiPlugin {
    private PsitweaksMekanismEmiPlugin() {
    }

    static void register(EmiRegistry registry) {
        registerSculkEroder(registry);
        registerMaterialMutator(registry);
        registerProgramResearch(registry);
    }

    private static void registerSculkEroder(EmiRegistry registry) {
        Map<ResourceLocation, ItemStackToItemStackRecipe> recipes = new LinkedHashMap<>();
        registry.getRecipeManager().getAllRecipesFor(PsitweaksRecipeTypes.SCULK_ERODER.get())
                .forEach(holder -> recipes.put(holder.id(), holder.value().asMekanismRecipe()));
        MekanismEmi.addCategoryAndRecipes(
                registry,
                PsitweaksMekanismJeiRecipeTypes.SCULK_ERODER,
                (category, id, recipe) -> new ItemStackToItemStackEmiRecipe(
                        category, new RecipeHolder<>(id, recipe)),
                recipes
        );
    }

    private static void registerMaterialMutator(EmiRegistry registry) {
        Map<ResourceLocation, ItemStackChemicalToItemStackRecipe> recipes = new LinkedHashMap<>();
        MaterialMutatorBlockEntity.getAllMutationMachineRecipeHolders()
                .forEach(holder -> recipes.put(holder.id(), holder.value()));
        MekanismEmi.addCategoryAndRecipes(
                registry,
                PsitweaksMekanismJeiRecipeTypes.MATERIAL_MUTATOR,
                (category, id, recipe) -> new ItemStackChemicalToItemStackEmiRecipe(
                        category, new RecipeHolder<>(id, recipe)),
                recipes
        );
    }

    private static void registerProgramResearch(EmiRegistry registry) {
        EmiRecipeCategory category = new PsitweaksEmiRecipeCategory(
                Psitweaks.location("program_research"),
                PsitweaksMekanismBlocks.PROGRAM_RESEARCHER.get(),
                "jei.psitweaks.program_research"
        );
        registry.addCategory(category);
        registry.addWorkstation(category, EmiStack.of(PsitweaksMekanismBlocks.PROGRAM_RESEARCHER.get()));
        for (RecipeHolder<ProgramResearchRecipe> holder
                : registry.getRecipeManager().getAllRecipesFor(PsitweaksRecipeTypes.PROGRAM_RESEARCH.get())) {
            registry.addRecipe(new ProgramResearchEmiRecipe(category, holder));
        }
    }
}
