package com.moratan251.psitweaks.datagen.providers;

import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import vazkii.psi.common.block.base.ModBlocks;
import vazkii.psi.common.item.base.ModItems;

import java.util.function.Consumer;

public class PsiTweaksRecipeProvider extends RecipeProvider {
    public PsiTweaksRecipeProvider(PackOutput output) {
        super(output);
    }

    private static final TagKey<Item> FORGE_INGOTS_LEAD =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath("forge", "ingots/lead"));

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
        // PsiGem を取得
        Item psiGem = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("psi", "psigem"));
        Item psimetal = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("psi", "psimetal"));
        Item ebonyPsimetal = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("psi", "ebony_psimetal"));
        Item ivoryPsimetal = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("psi", "ivory_psimetal"));
        Item spellBullet = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("psi", "spell_bullet"));
        Item cadAssembler = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("psi", "cad_assembler"));
        Item ultimateControlCircuit = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("mekanism", "ultimate_control_circuit"));
        Item plutoniumPellet = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("mekanism", "pellet_plutonium"));
        Item teleportationCore = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("mekanism", "teleportation_core"));



// ヘルメット
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.MOVAL_SUIT_HELMET.get())
                .define('H', PsitweaksItems.HEAVY_PSIMETAL.get())
                .define('E', ebonyPsimetal)
                .pattern("EHE")
                .pattern("E E")
                .unlockedBy("has_heavy_psimetal", has(PsitweaksItems.HEAVY_PSIMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "moval_suit_helmet"));

// チェスト
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.MOVAL_SUIT_CHESTPLATE.get())
                .define('H', PsitweaksItems.HEAVY_PSIMETAL.get())
                .define('E', ebonyPsimetal)
                .pattern("E E")
                .pattern("EHE")
                .pattern("EEE")
                .unlockedBy("has_heavy_psimetal", has(PsitweaksItems.HEAVY_PSIMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "moval_suit_chestplate"));

// レギンス
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.MOVAL_SUIT_LEGGINGS.get())
                .define('A', PsitweaksItems.HEAVY_PSIMETAL.get())
                .define('E', ebonyPsimetal)
                .pattern("EAE")
                .pattern("E E")
                .pattern("E E")
                .unlockedBy("has_heavy_psimetal", has(PsitweaksItems.HEAVY_PSIMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "moval_suit_leggings"));


        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.MOVAL_SUIT_LEGGINGS_IVORY.get())
                .define('A', PsitweaksItems.HEAVY_PSIMETAL.get())
                .define('I', ivoryPsimetal)
                .pattern("IAI")
                .pattern("I I")
                .pattern("I I")
                .unlockedBy("has_heavy_psimetal", has(PsitweaksItems.HEAVY_PSIMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "moval_suit_leggings_ivory"));

// ブーツ
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.MOVAL_SUIT_BOOTS.get())
                .define('A', PsitweaksItems.HEAVY_PSIMETAL.get())
                .define('E', ebonyPsimetal)
                .pattern("E E")
                .pattern("EAE")
                .unlockedBy("has_heavy_psimetal", has(PsitweaksItems.HEAVY_PSIMETAL.get()))
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
                .define('A', PsitweaksItems.HEAVY_PSIMETAL.get())
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

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.AWAKENED_SPELL_BULLET.get())
                .define('A', PsitweaksItems.HEAVY_PSIMETAL.get())
                .define('B', PsitweaksItems.SUBLIMATED_SPELL_BULLET.get())
                .define('C', PsitweaksItems.ECHO_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_heavy_psimetal", has(PsitweaksItems.HEAVY_PSIMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "awakened_spell_bullet"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.AWAKENED_SPELL_BULLET_LOOP.get())
                .define('A', PsitweaksItems.HEAVY_PSIMETAL.get())
                .define('B', PsitweaksItems.SUBLIMATED_SPELL_BULLET_LOOP.get())
                .define('C', PsitweaksItems.ECHO_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_heavy_psimetal", has(PsitweaksItems.HEAVY_PSIMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "awakened_spell_bullet_loop"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.AWAKENED_SPELL_BULLET_MINE.get())
                .define('A', PsitweaksItems.HEAVY_PSIMETAL.get())
                .define('B', PsitweaksItems.SUBLIMATED_SPELL_BULLET_MINE.get())
                .define('C', PsitweaksItems.ECHO_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_heavy_psimetal", has(PsitweaksItems.HEAVY_PSIMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "awakened_spell_bullet_mine"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.AWAKENED_SPELL_BULLET_CHARGE.get())
                .define('A', PsitweaksItems.HEAVY_PSIMETAL.get())
                .define('B', PsitweaksItems.SUBLIMATED_SPELL_BULLET_CHARGE.get())
                .define('C', PsitweaksItems.ECHO_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_heavy_psimetal", has(PsitweaksItems.HEAVY_PSIMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "awakened_spell_bullet_charge"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.AWAKENED_SPELL_BULLET_GRENADE.get())
                .define('A', PsitweaksItems.HEAVY_PSIMETAL.get())
                .define('B', PsitweaksItems.SUBLIMATED_SPELL_BULLET_GRENADE.get())
                .define('C', PsitweaksItems.ECHO_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_heavy_psimetal", has(PsitweaksItems.HEAVY_PSIMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "awakened_spell_bullet_grenade"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.AWAKENED_SPELL_BULLET_PROJECTILE.get())
                .define('A', PsitweaksItems.HEAVY_PSIMETAL.get())
                .define('B', PsitweaksItems.SUBLIMATED_SPELL_BULLET_PROJECTILE.get())
                .define('C', PsitweaksItems.ECHO_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_heavy_psimetal", has(PsitweaksItems.HEAVY_PSIMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "awakened_spell_bullet_projectile"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.AWAKENED_SPELL_BULLET_CIRCLE.get())
                .define('A', PsitweaksItems.HEAVY_PSIMETAL.get())
                .define('B', PsitweaksItems.SUBLIMATED_SPELL_BULLET_CIRCLE.get())
                .define('C', PsitweaksItems.ECHO_CONTROL_CIRCUIT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .unlockedBy("has_heavy_psimetal", has(PsitweaksItems.HEAVY_PSIMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "awakened_spell_bullet_circle"));




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

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PsitweaksBlocks.PROGRAM_RESEARCHER.get())
                .define('P', ModItems.psimetal)
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .define('G', ModItems.psigem)
                .pattern("PCP")
                .pattern("CGC")
                .pattern("PCP")
                .unlockedBy("has_chaotic_psimetal", has(PsitweaksItems.CHAOTIC_PSIMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_researcher"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PsitweaksItems.PROGRAM_BLANK.get())
                .define('P', Items.PAPER)
                .define('M', psimetal)
                .define('D', ModItems.psidust)
                .pattern("D")
                .pattern("P")
                .pattern("M")
                .unlockedBy("has_psimetal", has(psimetal))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_blank"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PsitweaksItems.ECHO_CONTROL_CIRCUIT.get())
                .define('C', PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get())
                .define('S', PsitweaksItems.ECHO_SHEET.get())
                .define('A', PsitweaksItems.ALLOY_PSIONIC_ECHO.get())
                .pattern("SSS")
                .pattern("ACA")
                .pattern("SSS")
                .unlockedBy("has_alloy_psionic_echo", has(PsitweaksItems.ALLOY_PSIONIC_ECHO.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "echo_control_circuit"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PsitweaksItems.INCOMPLETE_HEAVY_PSIMETAL_ASSEMBLY.get())
                .define('I', PsitweaksItems.HEAVY_PSIMETAL.get())
                .pattern("III")
                .pattern("  I")
                .unlockedBy("has_heavy_psimetal", has(PsitweaksItems.HEAVY_PSIMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "incomplete_heavy_psimetal_assembly"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_ALPHA.get())
                .define('I', PsitweaksItems.INCOMPLETE_HEAVY_PSIMETAL_ASSEMBLY.get())
                .define('S', PsitweaksItems.ECHO_SHEET.get())
                .define('C', PsitweaksItems.ECHO_CONTROL_CIRCUIT.get())
                .define('N', Items.NETHER_STAR)
                .pattern("SNS")
                .pattern("CIC")
                .pattern("SCS")
                .unlockedBy("has_echo_sheet", has(PsitweaksItems.ECHO_SHEET.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "cad_assembly_heavy_psimetal_alpha"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_BETA.get())
                .define('I', PsitweaksItems.INCOMPLETE_HEAVY_PSIMETAL_ASSEMBLY.get())
                .define('S', PsitweaksItems.ECHO_SHEET.get())
                .define('C', PsitweaksItems.ECHO_CONTROL_CIRCUIT.get())
                .define('P', MekanismItems.PLUTONIUM_PELLET)
                .pattern("SCS")
                .pattern("PIP")
                .pattern("SCS")
                .unlockedBy("has_echo_sheet", has(PsitweaksItems.ECHO_SHEET.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "cad_assembly_heavy_psimetal_beta"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, PsitweaksItems.FLASH_CHARM.get())
                .define('F', PsitweaksItems.FLASHMETAL.get())
                .define('N', Items.NETHER_WART)
                .pattern(" F ")
                .pattern("FNF")
                .pattern(" F ")
                .unlockedBy("has_flashmetal", has(PsitweaksItems.FLASHMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "flash_charm"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PsitweaksItems.PORTABLE_CAD_ASSEMBLER.get())
                .define('C', PsitweaksItems.CHAOTIC_PSIMETAL.get())
                .define('G', ModItems.psigem)
                .define('E', Items.ENDER_PEARL)
                .define('A', ModBlocks.cadAssembler)
                .pattern("CGC")
                .pattern("EAE")
                .pattern("CGC")
                .unlockedBy("has_chaotic_psimetal", has(PsitweaksItems.CHAOTIC_PSIMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "portable_cad_assembler"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MekanismBlocks.QIO_DRIVE_ARRAY)
                .define('P', MekanismTags.Items.PERSONAL_STORAGE)
                .define('G', Tags.Items.GLASS_PANES)
                .define('E', Tags.Items.ENDER_PEARLS)
                .define('C', MekanismItems.ULTIMATE_CONTROL_CIRCUIT)
                .define('T', MekanismItems.TELEPORTATION_CORE)
                .pattern("TGT")
                .pattern("CPC")
                .pattern("TET")
                .unlockedBy("has_teleportation_core", has(MekanismItems.TELEPORTATION_CORE))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "qio_drive_array"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MekanismBlocks.QIO_DASHBOARD)
                .define('L', FORGE_INGOTS_LEAD)
                .define('G', Tags.Items.GLASS_PANES)
                .define('E', Tags.Items.ENDER_PEARLS)
                .define('T', MekanismItems.TELEPORTATION_CORE)
                .pattern("LEL")
                .pattern("EGE")
                .pattern("LTL")
                .unlockedBy("has_teleportation_core", has(MekanismItems.TELEPORTATION_CORE))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "qio_dashboard"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MekanismBlocks.QIO_EXPORTER)
                .define('L', FORGE_INGOTS_LEAD)
                .define('P', Items.PISTON)
                .define('E', Tags.Items.ENDER_PEARLS)
                .define('T', MekanismItems.TELEPORTATION_CORE)
                .define('C', MekanismItems.ULTIMATE_CONTROL_CIRCUIT)
                .pattern("LTL")
                .pattern("ECE")
                .pattern(" P ")
                .unlockedBy("has_teleportation_core", has(MekanismItems.TELEPORTATION_CORE))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "qio_exporter"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MekanismBlocks.QIO_IMPORTER)
                .define('L', FORGE_INGOTS_LEAD)
                .define('P', Items.STICKY_PISTON)
                .define('E', Tags.Items.ENDER_PEARLS)
                .define('T', MekanismItems.TELEPORTATION_CORE)
                .define('C', MekanismItems.ULTIMATE_CONTROL_CIRCUIT)
                .pattern("LTL")
                .pattern("ECE")
                .pattern(" P ")
                .unlockedBy("has_teleportation_core", has(MekanismItems.TELEPORTATION_CORE))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "qio_importer"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MekanismBlocks.QIO_REDSTONE_ADAPTER)
                .define('A', Items.REDSTONE_TORCH)
                .define('R', Items.REDSTONE)
                .define('E', Tags.Items.ENDER_PEARLS)
                .define('T', MekanismItems.TELEPORTATION_CORE)
                .define('C', MekanismItems.ULTIMATE_CONTROL_CIRCUIT)
                .pattern("EAE")
                .pattern("CRC")
                .pattern("ETE")
                .unlockedBy("has_teleportation_core", has(MekanismItems.TELEPORTATION_CORE))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "qio_redstone_adapter"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MekanismItems.BASE_QIO_DRIVE)
                .define('L', FORGE_INGOTS_LEAD)
                .define('E', Tags.Items.ENDER_PEARLS)
                .define('C', MekanismItems.ULTIMATE_CONTROL_CIRCUIT)
                .pattern("LCL")
                .pattern("CEC")
                .pattern("LCL")
                .unlockedBy("has_lead_ingot", has(FORGE_INGOTS_LEAD))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "base_qio_drive"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MekanismItems.HYPER_DENSE_QIO_DRIVE)
                .define('D', MekanismItems.BASE_QIO_DRIVE)
                .define('T', MekanismItems.TELEPORTATION_CORE)
                .define('P', MekanismItems.PLUTONIUM_PELLET)
                .pattern("PDP")
                .pattern("DTD")
                .pattern("PDP")
                .unlockedBy("has_teleportation_core", has(MekanismItems.TELEPORTATION_CORE))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "hyper_dense_qio_drive"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MekanismItems.TIME_DILATING_QIO_DRIVE)
                .define('D', MekanismItems.HYPER_DENSE_QIO_DRIVE)
                .define('T', MekanismItems.POLONIUM_PELLET)
                .define('P', MekanismItems.PLUTONIUM_PELLET)
                .pattern("PDP")
                .pattern("DTD")
                .pattern("PDP")
                .unlockedBy("has_polonium_pellet", has(MekanismItems.POLONIUM_PELLET))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "time_dilating_qio_drive"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MekanismItems.SUPERMASSIVE_QIO_DRIVE)
                .define('D', MekanismItems.TIME_DILATING_QIO_DRIVE)
                .define('A', MekanismItems.ANTIMATTER_PELLET)
                .define('P', MekanismItems.POLONIUM_PELLET)
                .pattern("PDP")
                .pattern("DAD")
                .pattern("PDP")
                .unlockedBy("has_polonium_pellet", has(MekanismItems.POLONIUM_PELLET))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "supermassive_qio_drive"));



        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, PsitweaksItems.CURIOS_CONTROLLER.get(), 1)
                .requires(ModItems.exosuitController)
                .requires(PsitweaksItems.CHAOTIC_PSIMETAL.get())
                .unlockedBy("has_exosuit_controller", has(ModItems.exosuitController))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "curios_controller"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, PsitweaksItems.HEAVY_PSIMETAL.get(), 1)
                .requires(PsitweaksItems.HEAVY_PSIMETAL_SCRAP.get())
                .requires(PsitweaksItems.HEAVY_PSIMETAL_SCRAP.get())
                .requires(PsitweaksItems.HEAVY_PSIMETAL_SCRAP.get())
                .requires(PsitweaksItems.HEAVY_PSIMETAL_SCRAP.get())
                .requires(PsitweaksItems.FLASHMETAL.get())
                .requires(PsitweaksItems.FLASHMETAL.get())
                .requires(PsitweaksItems.FLASHMETAL.get())
                .requires(PsitweaksItems.FLASHMETAL.get())
                .unlockedBy("has_flashmetal", has(PsitweaksItems.FLASHMETAL.get()))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "heavy_psimetal"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.AMETHYST_SHARD, 4)
                .requires(Items.AMETHYST_BLOCK)
                .unlockedBy("has_amethyst_block", has(Items.AMETHYST_BLOCK))
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "amethyst_shard_from_block"));

        PsiTweaksSmeltryRecipeProvider.addRecipes(consumer);
        ProgramResearchRecipeProvider.addRecipes(consumer);
    }
}
