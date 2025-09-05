package com.moratan251.psitweaks.datagen.providers;

import com.moratan251.psitweaks.common.items.PsitweaksItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import vazkii.psi.common.item.base.ModItems;

import java.util.function.Consumer;

public class PsiTweaksRecipeProvider extends RecipeProvider {
    public PsiTweaksRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
        // PsiGem を取得
        Item psiGem = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("psi", "psigem"));
        Item psimetal = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("psi", "psimetal"));
        Item ebonyPsimetal = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("psi", "ebony_psimetal"));
        Item ivoryPsimetal = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("psi", "ivory_psimetal"));
        Item loopCastBullet = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("psi", "spell_bullet_loop"));
        Item ultimateControlCircuit = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("mekanism", "ultimate_control_circuit"));



// ヘルメット
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.MOVAL_SUIT_HELMET.get())
                .define('A', PsitweaksItems.ALLOY_PSION.get())
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .define('E', ebonyPsimetal)
                .pattern("ACA")
                .pattern("E E")
                .unlockedBy("has_alloy_psion", has(PsitweaksItems.ALLOY_PSION.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "moval_suit_helmet"));

// チェスト
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.MOVAL_SUIT_CHESTPLATE.get())
                .define('A', PsitweaksItems.ALLOY_PSION.get())
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .define('E', ebonyPsimetal)
                .pattern("E E")
                .pattern("ACA")
                .pattern("EEE")
                .unlockedBy("has_alloy_psion", has(PsitweaksItems.ALLOY_PSION.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "moval_suit_chestplate"));

// レギンス
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.MOVAL_SUIT_LEGGINGS.get())
                .define('A', PsitweaksItems.ALLOY_PSION.get())
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .define('E', ebonyPsimetal)
                .pattern("ACA")
                .pattern("E E")
                .pattern("E E")
                .unlockedBy("has_alloy_psion", has(PsitweaksItems.ALLOY_PSION.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "moval_suit_leggings"));


        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.MOVAL_SUIT_LEGGINGS_IVORY.get())
                .define('A', PsitweaksItems.ALLOY_PSION.get())
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .define('I', ivoryPsimetal)
                .pattern("ACA")
                .pattern("I I")
                .pattern("I I")
                .unlockedBy("has_alloy_psion", has(PsitweaksItems.ALLOY_PSION.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "moval_suit_leggings_ivory"));

// ブーツ
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.MOVAL_SUIT_BOOTS.get())
                .define('A', PsitweaksItems.ALLOY_PSION.get())
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .define('E', ebonyPsimetal)
                .pattern("A A")
                .pattern("ECE")
                .unlockedBy("has_alloy_psion", has(PsitweaksItems.ALLOY_PSION.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "moval_suit_boots"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .define('U', ultimateControlCircuit)
                .define('A', PsitweaksItems.ALLOY_PSION.get())
                .pattern("AUA")
                .unlockedBy("has_alloy_psion", has(PsitweaksItems.ALLOY_PSION.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "psionic_control_circuit"));
/*
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PsitweaksItems.FLIGHT_CHIP.get())
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .define('A', PsitweaksItems.ALLOY_PSION.get())
                .define('L', loopCastBullet)
                .pattern("ALA")
                .pattern("LCL")
                .pattern("ALA")
                .unlockedBy("has_alloy_psion", has(PsitweaksItems.ALLOY_PSION.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "flight_chip"));

 */

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PsitweaksItems.CAD_ASSEMBLY_ALLOY_PSION.get())
                .define('A', PsitweaksItems.ALLOY_PSION.get())
                .pattern("AAA")
                .pattern("  A")
                .unlockedBy("has_alloy_psion", has(PsitweaksItems.ALLOY_PSION.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "cad_assembly_alloy_psion"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.PSIMETAL_BOW.get())
                .define('M', psimetal)
                .define('G', psiGem)
                .define('S', Items.STRING)
                .pattern(" MS")
                .pattern("G S")
                .pattern(" MS")
                .unlockedBy("has_alloy_psion", has(PsitweaksItems.ALLOY_PSION.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "psimetal_bow"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.FLASH_RING.get())
                .define('A', ebonyPsimetal)
                .define('B', psiGem)
                .define('C', Items.GLOWSTONE_DUST)
                .pattern(" A ")
                .pattern("ABA")
                .pattern(" C ")
                .unlockedBy("has_psigem", has(psiGem))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "flash_ring"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.AUTO_CASTER_SECOND.get())
                .define('I', ivoryPsimetal)
                .define('F', PsitweaksItems.CHAOTIC_FACTOR.get())
                .define('C', Items.CLOCK)
                .pattern("ICI")
                .pattern("IFI")
                .pattern("III")
                .unlockedBy("has_chaotic_factor", has(PsitweaksItems.CHAOTIC_FACTOR.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "auto_caster_second"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.AUTO_CASTER_TICK.get())
                .define('E', ebonyPsimetal)
                .define('F', PsitweaksItems.CHAOTIC_FACTOR.get())
                .define('C', Items.CLOCK)
                .pattern("ECE")
                .pattern("EFE")
                .pattern("EEE")
                .unlockedBy("has_chaotic_factor", has(PsitweaksItems.CHAOTIC_FACTOR.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "auto_caster_tick"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.CAD_ASSEMBLY_CHAOTIC_PSIMETAL.get())
                .define('A', PsitweaksItems.CHAOTIC_PSIMETAL.get())
                .define('B', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .pattern("AAA")
                .pattern(" BA")
                .unlockedBy("has_chaotic_factor", has(PsitweaksItems.CHAOTIC_FACTOR.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "cad_assembly_chaotic_psimetal"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, PsitweaksItems.CURIOS_CONTROLLER.get(), 1)
                .requires(ModItems.exosuitController)
                .requires(PsitweaksItems.CHAOTIC_PSIMETAL.get())
                .unlockedBy("has_exosuit_controller", has(ModItems.exosuitController))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "curios_controller"));


    }
}