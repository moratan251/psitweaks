package com.moratan251.psitweaks.datagen.providers;

import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import mekanism.common.registries.MekanismItems;
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
        Item spellBullet = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("psi", "spell_bullet"));
        Item ultimateControlCircuit = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("mekanism", "ultimate_control_circuit"));
        Item plutoniumPellet = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("mekanism", "pellet_plutonium"));
        Item teleportationCore = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("mekanism", "teleportation_core"));



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

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PsitweaksItems.UNREFINED_FLASHMETAL.get())
                .define('A', PsitweaksItems.CHAOTIC_PSIMETAL.get())
                .define('B', MekanismItems.REFINED_GLOWSTONE_INGOT)
                .pattern("BBB")
                .pattern("AAA")
                .pattern("BBB")
                .unlockedBy("has_chaotic_psimetal", has(PsitweaksItems.CHAOTIC_PSIMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "unrefined_flashmetal"));
/*
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MekanismItems.TELEPORTATION_CORE)
                .define('A', Items.GOLD_INGOT)
                .define('B', Items.ENDER_PEARL)
                .define('C', Items.DIAMOND)
                .define('D', MekanismItems.ATOMIC_ALLOY)
                .pattern("BDB")
                .pattern("ACA")
                .pattern("BDB")
                .unlockedBy("ender_pearl", has(Items.ENDER_PEARL))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "unrefined_flashmetal"));

 */

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.THIRD_EYE_DEVICE.get())
                .define('A', PsitweaksItems.CHAOTIC_PSIMETAL.get())
                .define('B', plutoniumPellet)
                .define('C', teleportationCore)
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_chaotic_psimetal", has(PsitweaksItems.CHAOTIC_PSIMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "third_eye_device"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.ADVANCED_SPELL_BULLET.get())
                .define('A', psimetal)
                .define('B', spellBullet)
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_psimetal", has(psimetal))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "advanced_spell_bullet"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.ADVANCED_SPELL_BULLET_LOOP.get())
                .define('A', psimetal)
                .define('B', ModItems.loopSpellBullet)
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_psimetal", has(psimetal))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "advanced_spell_bullet_loop"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.ADVANCED_SPELL_BULLET_MINE.get())
                .define('A', psimetal)
                .define('B', ModItems.mineSpellBullet)
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_psimetal", has(psimetal))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "advanced_spell_bullet_mine"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.ADVANCED_SPELL_BULLET_CHARGE.get())
                .define('A', psimetal)
                .define('B', ModItems.chargeSpellBullet)
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_psimetal", has(psimetal))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "advanced_spell_bullet_charge"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.ADVANCED_SPELL_BULLET_GRENADE.get())
                .define('A', psimetal)
                .define('B', ModItems.grenadeSpellBullet)
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_psimetal", has(psimetal))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "advanced_spell_bullet_grenade"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.ADVANCED_SPELL_BULLET_PROJECTILE.get())
                .define('A', psimetal)
                .define('B', ModItems.projectileSpellBullet)
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_psimetal", has(psimetal))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "advanced_spell_bullet_projectile"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.ADVANCED_SPELL_BULLET_CIRCLE.get())
                .define('A', psimetal)
                .define('B', ModItems.circleSpellBullet)
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_psimetal", has(psimetal))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "advanced_spell_bullet_circle"));


        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.RESONANT_SPELL_BULLET.get())
                .define('A', PsitweaksItems.CHAOTIC_PSIMETAL.get())
                .define('B', PsitweaksItems.ADVANCED_SPELL_BULLET.get())
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_chaotic_psimetal", has(PsitweaksItems.CHAOTIC_PSIMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "resonant_spell_bullet"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.RESONANT_SPELL_BULLET_LOOP.get())
                .define('A', PsitweaksItems.CHAOTIC_PSIMETAL.get())
                .define('B', PsitweaksItems.ADVANCED_SPELL_BULLET_LOOP.get())
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_chaotic_psimetal", has(PsitweaksItems.CHAOTIC_PSIMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "resonant_spell_bullet_loop"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.RESONANT_SPELL_BULLET_MINE.get())
                .define('A', PsitweaksItems.CHAOTIC_PSIMETAL.get())
                .define('B', PsitweaksItems.ADVANCED_SPELL_BULLET_MINE.get())
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_chaotic_psimetal", has(PsitweaksItems.CHAOTIC_PSIMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "resonant_spell_bullet_mine"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.RESONANT_SPELL_BULLET_CHARGE.get())
                .define('A', PsitweaksItems.CHAOTIC_PSIMETAL.get())
                .define('B', PsitweaksItems.ADVANCED_SPELL_BULLET_CHARGE.get())
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_psimetal", has(psimetal))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "resonant_spell_bullet_charge"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.RESONANT_SPELL_BULLET_GRENADE.get())
                .define('A', PsitweaksItems.CHAOTIC_PSIMETAL.get())
                .define('B', PsitweaksItems.ADVANCED_SPELL_BULLET_GRENADE.get())
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_chaotic_psimetal", has(PsitweaksItems.CHAOTIC_PSIMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "resonant_spell_bullet_grenade"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.RESONANT_SPELL_BULLET_PROJECTILE.get())
                .define('A', PsitweaksItems.CHAOTIC_PSIMETAL.get())
                .define('B', PsitweaksItems.ADVANCED_SPELL_BULLET_PROJECTILE.get())
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_chaotic_psimetal", has(PsitweaksItems.CHAOTIC_PSIMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "resonant_spell_bullet_projectile"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.RESONANT_SPELL_BULLET_CIRCLE.get())
                .define('A', PsitweaksItems.CHAOTIC_PSIMETAL.get())
                .define('B', PsitweaksItems.ADVANCED_SPELL_BULLET_CIRCLE.get())
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_chaotic_psimetal", has(PsitweaksItems.CHAOTIC_PSIMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "resonant_spell_bullet_circle"));


        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.SUBLIMATED_SPELL_BULLET.get())
                .define('A', PsitweaksItems.FLASHMETAL.get())
                .define('B', PsitweaksItems.RESONANT_SPELL_BULLET.get())
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_flashmetal", has(PsitweaksItems.FLASHMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "sublimated_spell_bullet"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.SUBLIMATED_SPELL_BULLET_LOOP.get())
                .define('A', PsitweaksItems.FLASHMETAL.get())
                .define('B', PsitweaksItems.RESONANT_SPELL_BULLET_LOOP.get())
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_psimetal", has(psimetal))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "sublimated_spell_bullet_loop"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.SUBLIMATED_SPELL_BULLET_MINE.get())
                .define('A', PsitweaksItems.FLASHMETAL.get())
                .define('B', PsitweaksItems.RESONANT_SPELL_BULLET_MINE.get())
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_flashmetal", has(PsitweaksItems.FLASHMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "sublimated_spell_bullet_mine"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.SUBLIMATED_SPELL_BULLET_CHARGE.get())
                .define('A', PsitweaksItems.FLASHMETAL.get())
                .define('B', PsitweaksItems.RESONANT_SPELL_BULLET_CHARGE.get())
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_flashmetal", has(PsitweaksItems.FLASHMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "sublimated_spell_bullet_charge"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.SUBLIMATED_SPELL_BULLET_GRENADE.get())
                .define('A', PsitweaksItems.FLASHMETAL.get())
                .define('B', PsitweaksItems.RESONANT_SPELL_BULLET_GRENADE.get())
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_flashmetal", has(PsitweaksItems.FLASHMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "sublimated_spell_bullet_grenade"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.SUBLIMATED_SPELL_BULLET_PROJECTILE.get())
                .define('A', PsitweaksItems.FLASHMETAL.get())
                .define('B', PsitweaksItems.RESONANT_SPELL_BULLET_PROJECTILE.get())
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_flashmetal", has(PsitweaksItems.FLASHMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "sublimated_spell_bullet_projectile"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.SUBLIMATED_SPELL_BULLET_CIRCLE.get())
                .define('A', PsitweaksItems.FLASHMETAL.get())
                .define('B', PsitweaksItems.RESONANT_SPELL_BULLET_CIRCLE.get())
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_flashmetal", has(PsitweaksItems.FLASHMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "sublimated_spell_bullet_circle"));


        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.CAD_ASSEMBLY_FLASHMETAL.get())
                .define('F', PsitweaksItems.FLASHMETAL.get())
                .define('H', MekanismItems.HDPE_SHEET)
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .pattern("HHH")
                .pattern("FFF")
                .pattern(" CF")
                .unlockedBy("has_flashmetal", has(PsitweaksItems.FLASHMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "cad_assembly_flashmetal"));


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PsitweaksBlocks.CAD_DISASSEMBLER.get())
                .define('P', ModItems.psimetal)
                .define('A', Items.ANVIL)
                .pattern("PPP")
                .pattern("PAP")
                .pattern("PPP")
                .unlockedBy("has_psimetal", has(ModItems.psimetal))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "cad_disassembler"));


        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, PsitweaksItems.CURIOS_CONTROLLER.get(), 1)
                .requires(ModItems.exosuitController)
                .requires(PsitweaksItems.CHAOTIC_PSIMETAL.get())
                .unlockedBy("has_exosuit_controller", has(ModItems.exosuitController))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "curios_controller"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, PsitweaksItems.HEAVY_PSIMETAL.get(), 1)
                .requires(PsitweaksItems.FLASHMETAL.get())
                .requires(PsitweaksItems.HEAVY_PSIMETAL_SCRAP.get())
                .requires(PsitweaksItems.FLASHMETAL.get())
                .requires(PsitweaksItems.HEAVY_PSIMETAL_SCRAP.get())
                .requires(PsitweaksItems.FLASHMETAL.get())
                .requires(PsitweaksItems.HEAVY_PSIMETAL_SCRAP.get())
                .requires(PsitweaksItems.FLASHMETAL.get())
                .requires(PsitweaksItems.HEAVY_PSIMETAL_SCRAP.get())
                .unlockedBy("has_flashmetal", has(PsitweaksItems.FLASHMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "heavy_psimetal"));


    }
}