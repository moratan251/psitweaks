package com.moratan251.psitweaks.datagen.providers;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.items.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class PsiTweaksRecipeProvider extends RecipeProvider {
    public PsiTweaksRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
        // PsiGem を取得
        Item psiGem = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("psi", "psigem"));
        if (psiGem == null) return;

// ヘルメット
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MOVAL_SUIT_HELMET.get())
                .define('S', Items.NETHER_STAR)
                .define('I', Items.NETHERITE_INGOT)
                .define('G', psiGem)
                .pattern("GSG")
                .pattern("I I")
                .unlockedBy("has_psigem", has(psiGem))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "moval_suit_helmet"));

// チェスト
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MOVAL_SUIT_CHESTPLATE.get())
                .define('S', Items.NETHER_STAR)
                .define('I', Items.NETHERITE_INGOT)
                .define('G', psiGem)
                .pattern("I I")
                .pattern("GSG")
                .pattern("III")
                .unlockedBy("has_psigem", has(psiGem))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "moval_suit_chestplate"));

// レギンス
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MOVAL_SUIT_LEGGINGS.get())
                .define('S', Items.NETHER_STAR)
                .define('I', Items.NETHERITE_INGOT)
                .define('G', psiGem)
                .pattern("GSG")
                .pattern("I I")
                .pattern("I I")
                .unlockedBy("has_psigem", has(psiGem))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "moval_suit_leggings"));

// ブーツ
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MOVAL_SUIT_BOOTS.get())
                .define('S', Items.NETHER_STAR)
                .define('I', Items.NETHERITE_INGOT)
                .define('G', psiGem)
                .pattern("G G")
                .pattern("ISI")
                .unlockedBy("has_psigem", has(psiGem))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "moval_suit_boots"));
    }
}