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
        Item ebonyPsimetal = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("psi", "ebony_psimetal"));
        Item ivoryPsimetal = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("psi", "ivory_psimetal"));
        Item loopCastBullet = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("psi", "spell_bullet_loop"));
        Item ultimateControlCircuit = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("mekanism", "ultimate_control_circuit"));


// ヘルメット
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MOVAL_SUIT_HELMET.get())
                .define('A', ModItems.ALLOY_PSION.get())
                .define('C', ModItems.PSIONIC_CONTROL_CIRCUIT.get())
                .define('E', ebonyPsimetal)
                .pattern("ACA")
                .pattern("E E")
                .unlockedBy("has_alloy_psion", has(ModItems.ALLOY_PSION.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "moval_suit_helmet"));

// チェスト
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MOVAL_SUIT_CHESTPLATE.get())
                .define('A', ModItems.ALLOY_PSION.get())
                .define('C', ModItems.PSIONIC_CONTROL_CIRCUIT.get())
                .define('E', ebonyPsimetal)
                .pattern("E E")
                .pattern("ACA")
                .pattern("EEE")
                .unlockedBy("has_alloy_psion", has(ModItems.ALLOY_PSION.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "moval_suit_chestplate"));

// レギンス
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MOVAL_SUIT_LEGGINGS.get())
                .define('F', ModItems.FLIGHT_CHIP.get())
                .define('A', ModItems.ALLOY_PSION.get())
                .define('C', ModItems.PSIONIC_CONTROL_CIRCUIT.get())
                .define('E', ebonyPsimetal)
                .pattern("ACA")
                .pattern("EFE")
                .pattern("E E")
                .unlockedBy("has_alloy_psion", has(ModItems.ALLOY_PSION.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "moval_suit_leggings"));


        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MOVAL_SUIT_LEGGINGS_IVORY.get())
                .define('F', ModItems.FLIGHT_CHIP.get())
                .define('A', ModItems.ALLOY_PSION.get())
                .define('C', ModItems.PSIONIC_CONTROL_CIRCUIT.get())
                .define('I', ivoryPsimetal)
                .pattern("ACA")
                .pattern("IFI")
                .pattern("I I")
                .unlockedBy("has_alloy_psion", has(ModItems.ALLOY_PSION.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "moval_suit_leggings_ivory"));

// ブーツ
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MOVAL_SUIT_BOOTS.get())
                .define('A', ModItems.ALLOY_PSION.get())
                .define('C', ModItems.PSIONIC_CONTROL_CIRCUIT.get())
                .define('E', ebonyPsimetal)
                .pattern("A A")
                .pattern("ECE")
                .unlockedBy("has_alloy_psion", has(ModItems.ALLOY_PSION.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "moval_suit_boots"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FLASH_RING.get())
                //.define('A', ModItems.ALLOY_PSION.get())
                .define('G', Items.GLOWSTONE_DUST)
                .define('E', ebonyPsimetal)
                .define('P', psiGem)
                .pattern(" E ")
                .pattern("EGE")
                .pattern(" P ")
                .unlockedBy("has_psigem", has(psiGem))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "flash_ring"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PSIONIC_CONTROL_CIRCUIT.get())
                .define('U', ultimateControlCircuit)
                .define('A', ModItems.ALLOY_PSION.get())
                .pattern("AUA")
                .unlockedBy("has_alloy_psion", has(ModItems.ALLOY_PSION.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "psionic_control_circuit"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FLIGHT_CHIP.get())
                .define('C', ModItems.PSIONIC_CONTROL_CIRCUIT.get())
                .define('A', ModItems.ALLOY_PSION.get())
                .define('L', loopCastBullet)
                .pattern("ALA")
                .pattern("LCL")
                .pattern("ALA")
                .unlockedBy("has_alloy_psion", has(ModItems.ALLOY_PSION.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "flight_chip"));


    }
}