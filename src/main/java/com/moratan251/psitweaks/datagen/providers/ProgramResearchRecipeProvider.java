package com.moratan251.psitweaks.datagen.providers;

import com.moratan251.psitweaks.common.items.PsitweaksItems;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.Tag;
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

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_COCYTUS.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(Items.HEART_OF_THE_SEA, 1)
                .requires(Items.BLUE_ICE, 64)
                .requires(PsitweaksItems.PSIONIC_ECHO.get(), 32)
                .requires(Items.SCULK_SHRIEKER, 8)
                .energy(500000000)
                .time(36000)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_cocytus"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_TIME_ACCELERATE.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(Items.CLOCK, 16)
                .requires(Items.REDSTONE_BLOCK, 16)
                .requires(PsitweaksItems.PSIONIC_ECHO.get(), 1)
                .requires(Items.POWERED_RAIL, 48)
                .energy(10000000)
                .time(1200)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_time_accelerate"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_FLIGHT.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(Items.PHANTOM_MEMBRANE, 4)
                .requires(Items.FEATHER, 48)
                .requires(ModItems.psigem, 12)
                .requires(Items.NETHER_WART, 16)
                .energy(10000000)
                .time(600)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_flight"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_PHONON_MASER.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(MekanismBlocks.LASER, 1)
                .requires(Items.AMETHYST_SHARD, 12)
                .requires(PsitweaksItems.FLASHMETAL.get(), 12)
                .requires(Items.NOTE_BLOCK, 48)
                .energy(40000000)
                .time(2400)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_phonon_maser"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_SUPREME_INFUSION.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(Items.NETHERITE_INGOT, 1)
                .requires(PsitweaksItems.FLASHMETAL.get(), 8)
                .requires(PsitweaksItems.ALLOY_PSION.get(), 24)
                .requires(Items.AMETHYST_BLOCK, 16)
                .energy(40000000)
                .time(2400)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_supreme_infusion"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_MOLECULAR_DIVIDER.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(ModItems.psigem, 32)
                .requires(Items.QUARTZ, 48)
                .requires(PsitweaksItems.HEAVY_PSIMETAL.get(), 8)
                .requires(PsitweaksItems.ECHO_CONTROL_CIRCUIT.get(), 3)
                .energy(80000000)
                .time(6000)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_molecular_divider"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_RADIATION_INJECTION.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(uraniumIngot, 16)
                .requires(leadIngot, 8)
                .requires(PsitweaksItems.CHAOTIC_PSIMETAL.get(), 8)
                .energy(60000000)
                .time(3600)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_radiation_injection"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_GUILLOTINE.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(Items.WITHER_SKELETON_SKULL, 1)
                .requires(Items.ANVIL, 3)
                .requires(ModItems.psimetalSword, 1)
                .requires(ModItems.psimetalSword, 1)
                .requires(ModItems.psimetalAxe, 1)
                .requires(ModItems.psimetalAxe, 1)
                .energy(50000000)
                .time(3000)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_guillotine"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_ACTIVE_AIR_MINE.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(Items.TNT, 16)
                .requires(Items.AMETHYST_SHARD, 16)
                .requires(ModItems.psidust, 32)
                .requires(ModItems.psigem, 16)
                .energy(15000000)
                .time(1200)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_active_air_mine"));
    }
}
