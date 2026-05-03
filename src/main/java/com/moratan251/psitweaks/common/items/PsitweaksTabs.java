package com.moratan251.psitweaks.common.items;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PsitweaksTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(
            Registries.CREATIVE_MODE_TAB,
            Psitweaks.MOD_ID
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PSITWEAKS_TAB = TABS.register(
            "psitweaks",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetabs.psitweaks"))
                    .icon(() -> PsitweaksItems.ALLOY_PSION.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(PsitweaksItems.ENRICHED_PSIGEM.get());
                        output.accept(PsitweaksItems.ENRICHED_EBONY.get());
                        output.accept(PsitweaksItems.ENRICHED_IVORY.get());
                        output.accept(PsitweaksItems.ENRICHED_ECHO.get());
                        output.accept(PsitweaksItems.ENRICHED_HYPOSTASIS.get());
                        output.accept(PsitweaksItems.ALLOY_PSION.get());
                        output.accept(PsitweaksItems.ALLOY_PSIONIC_ECHO.get());
                        output.accept(PsitweaksItems.ALLOY_HYPOSTASIS.get());
                        output.accept(PsitweaksItems.PSIONIC_ECHO.get());
                        output.accept(PsitweaksItems.HEAVY_PSIMETAL_SCRAP.get());
                        output.accept(PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get());
                        output.accept(PsitweaksItems.ECHO_CONTROL_CIRCUIT.get());
                        output.accept(PsitweaksItems.HYPOSTASIS_CONTROL_CIRCUIT.get());
                        output.accept(PsitweaksItems.PSIONIC_FACTOR.get());
                        output.accept(PsitweaksItems.PSIONIC_FACTOR_IVORY.get());
                        output.accept(PsitweaksItems.PSIONIC_FACTOR_EBONY.get());
                        output.accept(PsitweaksItems.CHAOTIC_FACTOR.get());
                        output.accept(PsitweaksItems.CHAOTIC_PSIMETAL.get());
                        output.accept(PsitweaksItems.FLASHMETAL.get());
                        output.accept(PsitweaksItems.HEAVY_PSIMETAL.get());
                        output.accept(PsitweaksItems.ANTINITE_INGOT.get());
                        output.accept(PsitweaksItems.PSYCHEONIC_METAL_INGOT.get());
                        output.accept(PsitweaksItems.PSYCHEONIC_METAL_NUGGET.get());
                        output.accept(PsitweaksItems.UNREFINED_FLASHMETAL.get());
                        output.accept(PsitweaksItems.RAW_ANTINITE.get());
                        output.accept(PsitweaksItems.CRYSTAL_ANTINITE.get());
                        output.accept(PsitweaksItems.SHARD_ANTINITE.get());
                        output.accept(PsitweaksItems.CLUMP_ANTINITE.get());
                        output.accept(PsitweaksItems.DIRTY_DUST_ANTINITE.get());
                        output.accept(PsitweaksItems.ANTINITE_DUST.get());
                        output.accept(PsitweaksItems.ECHO_PELLET.get());
                        output.accept(PsitweaksItems.PELLET_NEPTUNIUM.get());
                        output.accept(PsitweaksItems.PELLET_AMERICIUM.get());
                        output.accept(PsitweaksItems.HYPOSTASIS_GEM.get());
                        output.accept(PsitweaksItems.JADE.get());
                        output.accept(PsitweaksItems.MAGATAMA.get());
                        output.accept(PsitweaksItems.ECHO_SHEET.get());
                        output.accept(PsitweaksItems.MAGICIANS_BRAIN.get());
                        output.accept(PsitweaksItems.INCOMPLETE_HEAVY_PSIMETAL_ASSEMBLY.get());
                        output.accept(PsitweaksBlocks.CAD_DISASSEMBLER.get());
                        output.accept(PsitweaksBlocks.ORE_ANTINITE.get());
                        output.accept(PsitweaksBlocks.ANTINITE_BLOCK.get());
                        output.accept(PsitweaksBlocks.CHAOTIC_PSIMETAL_BLOCK.get());
                        output.accept(PsitweaksBlocks.FLASHMETAL_BLOCK.get());
                        output.accept(PsitweaksBlocks.HEAVY_PSIMETAL_BLOCK.get());
                        output.accept(PsitweaksBlocks.PLUTONIUM_BLOCK.get());
                        output.accept(PsitweaksBlocks.POLONIUM_BLOCK.get());
                        output.accept(PsitweaksBlocks.RAW_ANTINITE_BLOCK.get());
                        output.accept(PsitweaksBlocks.SPELLMACHINERY_CASING.get());
                    })
                    .build()
    );

    private PsitweaksTabs() {
    }

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}
