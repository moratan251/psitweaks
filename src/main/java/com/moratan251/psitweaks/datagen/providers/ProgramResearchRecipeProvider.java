package com.moratan251.psitweaks.datagen.providers;

import com.moratan251.psitweaks.common.items.PsitweaksItems;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import vazkii.psi.common.item.base.ModItems;

import java.util.function.Consumer;

public final class ProgramResearchRecipeProvider {

    private ProgramResearchRecipeProvider() {
    }

    public static void addRecipes(Consumer<FinishedRecipe> consumer) {
        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_COCYTUS.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(Items.HEART_OF_THE_SEA, 1)
                .requires(Items.BLUE_ICE, 64)
                .requires(PsitweaksItems.PSIONIC_ECHO.get(), 32)
                .requires(Items.SCULK_SHRIEKER, 8)
                .energy(500000)
                .time(320)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_cocytus"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_TIME_ACCELERATE.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(Items.CLOCK, 16)
                .requires(Items.REDSTONE_BLOCK, 16)
                .requires(PsitweaksItems.PSIONIC_ECHO.get(), 1)
                .requires(Items.POWERED_RAIL, 48)
                .energy(300000)
                .time(360)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_time_accelerate"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_FLIGHT.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(Items.PHANTOM_MEMBRANE, 4)
                .requires(Items.FEATHER, 48)
                .requires(ModItems.psigem, 12)
                .requires(Items.NETHER_WART, 16)
                .energy(150000)
                .time(320)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_flight"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_PHONON_MASER.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(MekanismBlocks.LASER, 1)
                .requires(Items.AMETHYST_SHARD, 12)
                .requires(PsitweaksItems.FLASHMETAL.get(), 12)
                .requires(Items.NOTE_BLOCK, 48)
                .energy(100000)
                .time(420)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_phonon_maser"));

        ProgramResearchRecipeBuilder.research(PsitweaksItems.PROGRAM_MOLECULAR_DIVIDER.get())
                .requires(PsitweaksItems.PROGRAM_BLANK.get(), 1)
                .requires(ModItems.psigem, 32)
                .requires(Items.QUARTZ, 48)
                .requires(PsitweaksItems.HEAVY_PSIMETAL.get(), 8)
                .requires(PsitweaksItems.ECHO_CONTROL_CIRCUIT.get(), 3)
                .energy(200000)
                .time(480)
                .save(consumer, ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_molecular_divider"));
    }
}
