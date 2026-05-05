package com.moratan251.psitweaks.client.jei;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.client.gui.machine.GuiMaterialMutator;
import com.moratan251.psitweaks.client.gui.machine.GuiSculkEroder;
import com.moratan251.psitweaks.common.handler.MaterialMutationRecipeHandler;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import com.moratan251.psitweaks.common.tile.machine.MaterialMutatorBlockEntity;
import com.moratan251.psitweaks.common.tile.machine.SculkEroderBlockEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.client.recipe_viewer.jei.machine.ItemStackChemicalToItemStackRecipeCategory;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
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
        registration.addRecipeCategories(new ItemStackToItemStackRecipeCategory(
                registration.getJeiHelpers().getGuiHelper(),
                PsitweaksMekanismJeiRecipeTypes.SCULK_ERODER
        ));
        registration.addRecipeCategories(new ItemStackChemicalToItemStackRecipeCategory(
                registration.getJeiHelpers().getGuiHelper(),
                PsitweaksMekanismJeiRecipeTypes.MATERIAL_MUTATOR
        ));
        registration.addRecipeCategories(new MaterialMutationJeiCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Level level = Minecraft.getInstance().level;
        List<RecipeHolder<ItemStackToItemStackRecipe>> recipes = level == null
                ? List.of()
                : SculkEroderBlockEntity.getAllSculkRecipeHolders(level);
        registration.addRecipes(sculkEroderRecipeType(), recipes);

        List<RecipeHolder<ItemStackChemicalToItemStackRecipe>> materialMutatorRecipes = level == null
                ? List.of()
                : MaterialMutatorBlockEntity.getAllMutationMachineRecipeHolders(level);
        registration.addRecipes(materialMutatorRecipeType(), materialMutatorRecipes);

        registration.addRecipes(MaterialMutationJeiCategory.RECIPE_TYPE, getMaterialMutationJeiRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(PsitweaksMekanismBlocks.SCULK_ERODER.get(), sculkEroderRecipeType());
        registration.addRecipeCatalyst(PsitweaksMekanismBlocks.MATERIAL_MUTATOR.get(), materialMutatorRecipeType());
        registration.addRecipeCatalyst(PsitweaksItems.PROGRAM_MATERIAL_MUTATION.get(), MaterialMutationJeiCategory.RECIPE_TYPE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(GuiSculkEroder.class, 78, 34, 24, 17, sculkEroderRecipeType());
        registration.addRecipeClickArea(GuiMaterialMutator.class, 78, 34, 24, 17, materialMutatorRecipeType());
    }

    private static RecipeType<RecipeHolder<ItemStackToItemStackRecipe>> sculkEroderRecipeType() {
        return MekanismJEI.holderRecipeType(PsitweaksMekanismJeiRecipeTypes.SCULK_ERODER);
    }

    private static RecipeType<RecipeHolder<ItemStackChemicalToItemStackRecipe>> materialMutatorRecipeType() {
        return MekanismJEI.holderRecipeType(PsitweaksMekanismJeiRecipeTypes.MATERIAL_MUTATOR);
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
