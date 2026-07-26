package com.moratan251.psitweaks.datagen.providers;

import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.datagen.builders.ProgramResearchRecipeBuilder;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.psi.common.item.base.ModItems;

import java.util.Objects;
import java.util.function.Consumer;

public final class ProgramResearchRecipeProvider {

    private ProgramResearchRecipeProvider() {
    }

    public static void addRecipes(Consumer<FinishedRecipe> consumer) {
        Item uraniumIngot = Objects.requireNonNull(
                ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("mekanism", "ingot_uranium")),
                "Missing item: mekanism:ingot_uranium"
        );
        Item leadIngot = Objects.requireNonNull(
                ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("mekanism", "ingot_lead")),
                "Missing item: mekanism:ingot_lead"
        );
        Item radiationShieldingUnit = Objects.requireNonNull(
                ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("mekanism", "module_radiation_shielding_unit")),
                "Missing item: mekanism:module_radiation_shielding_unit"
        );

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_COCYTUS.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(MekanismItems.ANTIMATTER_PELLET, 8)
                .requires(Items.BLUE_ICE, 64)
                .requires(Items.SCULK_SHRIEKER, 24)
                .requires(Items.HEART_OF_THE_SEA, 4)
                .requires(PsitweaksItems.PSYCHEONIC_METAL_INGOT.get(), 64)
                .energyPerTick(1_500_000L)
                .time(108_000)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_cocytus"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_TIME_ACCELERATE.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(Items.CLOCK, 8)
                .requires(Items.REDSTONE_BLOCK, 8)
                .requires(PsitweaksItems.CHAOTIC_PSIMETAL.get(), 8)
                .requires(Items.POWERED_RAIL, 32)
                .energyPerTick(10_000L)
                .time(1200)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_time_accelerate"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_FLIGHT.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(PsitweaksItems.ADVANCED_SPELL_BULLET_LOOP.get(), 16)
                .requires(PsitweaksItems.SUBLIMATED_SPELL_BULLET_LOOP.get(), 1)
                .requires(Items.FEATHER, 32)
                .requires(PsitweaksItems.CHAOTIC_FACTOR.get(), 8)
                .energyPerTick(10_000L)
                .time(600)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_flight"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_PHONON_MASER.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(MekanismBlocks.LASER, 1)
                .requires(Items.AMETHYST_SHARD, 12)
                .requires(PsitweaksItems.FLASHMETAL.get(), 8)
                .requires(Items.NOTE_BLOCK, 48)
                .energyPerTick(40_000L)
                .time(2400)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_phonon_maser"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_METEOR_LINE.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(Items.NETHER_STAR, 24)
                .requires(MekanismItems.ANTIMATTER_PELLET, 16)
                .requires(PsitweaksItems.ALLOY_HYPOSTASIS.get(), 48)
                .requires(PsitweaksItems.PSYCHEONIC_METAL_INGOT.get(), 64)
                .requires(PsitweaksItems.PSYCHEONIC_METAL_INGOT.get(), 64)
                .energyPerTick(1_500_000L)
                .time(288_000)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_meteor_line"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_SUPREME_INFUSION.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(Items.NETHERITE_INGOT, 1)
                .requires(PsitweaksItems.FLASHMETAL.get(), 8)
                .requires(PsitweaksItems.ALLOY_PSION.get(), 24)
                .requires(Items.AMETHYST_BLOCK, 16)
                .energyPerTick(40_000L)
                .time(2400)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_supreme_infusion"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_MOLECULAR_DIVIDER.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(ModItems.psigem, 32)
                .requires(Items.QUARTZ, 48)
                .requires(PsitweaksItems.HEAVY_PSIMETAL.get(), 8)
                .requires(PsitweaksItems.ECHO_CONTROL_CIRCUIT.get(), 3)
                .energyPerTick(80_000L)
                .time(6000)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_molecular_divider"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_RADIATION_INJECTION.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(uraniumIngot, 16)
                .requires(leadIngot, 8)
                .requires(PsitweaksItems.CHAOTIC_PSIMETAL.get(), 8)
                .energyPerTick(60_000L)
                .time(3600)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_radiation_injection"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_RADIATION_FILTER.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(leadIngot, 16)
                .requires(MekanismItems.FLUORITE_DUST, 16)
                .requires(PsitweaksItems.CHAOTIC_PSIMETAL.get(), 8)
                .energyPerTick(60_000L)
                .time(3600)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_radiation_filter"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_CURE_RADIATION.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(Items.GOLDEN_APPLE, 8)
                .requires(leadIngot, 16)
                .requires(PsitweaksItems.CHAOTIC_PSIMETAL.get(), 8)
                .energyPerTick(60_000L)
                .time(3600)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_cure_radiation"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_GUILLOTINE.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(Items.WITHER_SKELETON_SKULL, 1)
                .requires(Items.ANVIL, 3)
                .requires(Items.ROTTEN_FLESH, 24)
                .requires(Items.BONE, 16)
                .requires(ModItems.psimetalSword, 1)
                .energyPerTick(50_000L)
                .time(3000)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_guillotine"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_ACTIVE_AIR_MINE.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(Items.TNT, 8)
                .requires(ModItems.psidust, 16)
                .requires(ModItems.psigem, 2)
                .energyPerTick(4_000L)
                .time(400)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_active_air_mine"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_DIE_FLEX.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get(), 16)
                .requires(PsitweaksItems.CHAOTIC_PSIMETAL.get(), 16)
                .requires(PsitweaksItems.CHAOTIC_FACTOR.get(), 16)
                .energyPerTick(6_000L)
                .time(600)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_die_flex"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_JUMP_FLEX.get())
                .requires(PsitweaksItems.PROGRAM_DIE_FLEX.get(), 1)
                .requires(PsitweaksItems.ECHO_CONTROL_CIRCUIT.get(), 16)
                .requires(PsitweaksItems.FLASHMETAL.get(), 24)
                .requires(MekanismItems.POLONIUM_PELLET, 4)
                .energyPerTick(100_000L)
                .time(54_000)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_jump_flex"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_SWITCH_FLEX.get())
                .requires(PsitweaksItems.PROGRAM_JUMP_FLEX.get(), 1)
                .requires(PsitweaksItems.HYPOSTASIS_CONTROL_CIRCUIT.get(), 16)
                .requires(PsitweaksItems.HEAVY_PSIMETAL.get(), 32)
                .requires(PsitweaksItems.PELLET_AMERICIUM.get(), 8)
                .energyPerTick(500_000L)
                .time(90_000)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_switch_flex"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_MATERIAL_MUTATION.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requiresCatalyst(PsitweaksItems.PHILOSOPHERS_STONE.get(), 1)
                .requires(Items.SCULK_CATALYST, 1)
                .requires(PsitweaksItems.CHAOTIC_FACTOR.get(), 16)
                .requires(PsitweaksItems.ANTINITE_INGOT.get(), 32)
                .requires(PsitweaksItems.PSIONIC_ECHO.get(), 16)
                .energyPerTick(250_000L)
                .time(24000)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_material_mutation"));
        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_MASS_BLOCK_BREAK.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(ModItems.psigem, 16)
                .requires(PsitweaksItems.CHAOTIC_PSIMETAL.get(), 16)
                .requires(Items.TNT, 16)
                .energyPerTick(10_000L)
                .time(1200)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_mass_block_break"));
    }
}
