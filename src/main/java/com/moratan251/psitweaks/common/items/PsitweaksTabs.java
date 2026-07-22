package com.moratan251.psitweaks.common.items;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
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
                        output.accept(PsitweaksItems.PSIMETAL_NUGGET.get());
                        output.accept(PsitweaksItems.IVORY_PSIMETAL_NUGGET.get());
                        output.accept(PsitweaksItems.EBONY_PSIMETAL_NUGGET.get());
                        output.accept(PsitweaksItems.CHAOTIC_PSIMETAL.get());
                        output.accept(PsitweaksItems.CHAOTIC_PSIMETAL_NUGGET.get());
                        output.accept(PsitweaksItems.FLASHMETAL.get());
                        output.accept(PsitweaksItems.FLASHMETAL_NUGGET.get());
                        output.accept(PsitweaksItems.HEAVY_PSIMETAL.get());
                        output.accept(PsitweaksItems.HEAVY_PSIMETAL_NUGGET.get());
                        output.accept(PsitweaksItems.ANTINITE_INGOT.get());
                        output.accept(PsitweaksItems.ANTINITE_NUGGET.get());
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
                        output.accept(PsitweaksItems.PHILOSOPHERS_STONE.get());
                        output.accept(PsitweaksItems.SPELL_MAGAZINE.get());
                        output.accept(PsitweaksItems.PORTABLE_CAD_ASSEMBLER.get());
                        output.accept(PsitweaksItems.PSIMETAL_BOW.get());
                        output.accept(PsitweaksItems.CURIOS_CONTROLLER.get());
                        output.accept(PsitweaksItems.AUTO_CASTER_TICK.get());
                        output.accept(PsitweaksItems.AUTO_CASTER_CUSTOM_TICK.get());
                        output.accept(PsitweaksItems.FLASH_CHARM.get());
                        output.accept(PsitweaksItems.INTERFERENCE_RANGE_EXTENDER.get());
                        output.accept(PsitweaksItems.THIRD_EYE_DEVICE.get());
                        output.accept(PsitweaksItems.SORCERY_BOOSTER.get());
                        output.accept(PsitweaksItems.MOVAL_SUIT_HELMET.get());
                        output.accept(PsitweaksItems.MOVAL_SUIT_CHESTPLATE.get());
                        output.accept(PsitweaksItems.MOVAL_SUIT_LEGGINGS.get());
                        output.accept(PsitweaksItems.MOVAL_SUIT_BOOTS.get());
                        acceptSpellBullets(output);
                        acceptProgramItems(output);
                        output.accept(PsitweaksItems.CAD_ASSEMBLY_ALLOY_PSION.get());
                        output.accept(PsitweaksItems.CAD_ASSEMBLY_CHAOTIC_PSIMETAL.get());
                        output.accept(PsitweaksItems.CAD_ASSEMBLY_FLASHMETAL.get());
                        output.accept(PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_ALPHA.get());
                        output.accept(PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_BETA.get());
                        output.accept(PsitweaksItems.CAD_ASSEMBLY_PSYCHEONIC_METAL.get());
                        output.accept(PsitweaksItems.INCOMPLETE_HEAVY_PSIMETAL_ASSEMBLY.get());
                        output.accept(PsitweaksItems.INLINE_CASTER.get());
                        output.accept(PsitweaksItems.SECONDARY_CASTER.get());
                        output.accept(PsitweaksItems.PARALLEL_CASTER.get());
                        output.accept(PsitweaksBlocks.CAD_DISASSEMBLER.get());
                        MekanismCompat.addCreativeTabContents(output);
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

    private static void acceptSpellBullets(CreativeModeTab.Output output) {
        output.accept(PsitweaksItems.ADVANCED_SPELL_BULLET.get());
        output.accept(PsitweaksItems.ADVANCED_SPELL_BULLET_LOOP.get());
        output.accept(PsitweaksItems.ADVANCED_SPELL_BULLET_MINE.get());
        output.accept(PsitweaksItems.ADVANCED_SPELL_BULLET_CHARGE.get());
        output.accept(PsitweaksItems.ADVANCED_SPELL_BULLET_GRENADE.get());
        output.accept(PsitweaksItems.ADVANCED_SPELL_BULLET_PROJECTILE.get());
        output.accept(PsitweaksItems.ADVANCED_SPELL_BULLET_CIRCLE.get());
        output.accept(PsitweaksItems.RESONANT_SPELL_BULLET.get());
        output.accept(PsitweaksItems.RESONANT_SPELL_BULLET_LOOP.get());
        output.accept(PsitweaksItems.RESONANT_SPELL_BULLET_MINE.get());
        output.accept(PsitweaksItems.RESONANT_SPELL_BULLET_CHARGE.get());
        output.accept(PsitweaksItems.RESONANT_SPELL_BULLET_GRENADE.get());
        output.accept(PsitweaksItems.RESONANT_SPELL_BULLET_PROJECTILE.get());
        output.accept(PsitweaksItems.RESONANT_SPELL_BULLET_CIRCLE.get());
        output.accept(PsitweaksItems.SUBLIMATED_SPELL_BULLET.get());
        output.accept(PsitweaksItems.SUBLIMATED_SPELL_BULLET_LOOP.get());
        output.accept(PsitweaksItems.SUBLIMATED_SPELL_BULLET_MINE.get());
        output.accept(PsitweaksItems.SUBLIMATED_SPELL_BULLET_CHARGE.get());
        output.accept(PsitweaksItems.SUBLIMATED_SPELL_BULLET_GRENADE.get());
        output.accept(PsitweaksItems.SUBLIMATED_SPELL_BULLET_PROJECTILE.get());
        output.accept(PsitweaksItems.SUBLIMATED_SPELL_BULLET_CIRCLE.get());
        output.accept(PsitweaksItems.AWAKENED_SPELL_BULLET.get());
        output.accept(PsitweaksItems.AWAKENED_SPELL_BULLET_LOOP.get());
        output.accept(PsitweaksItems.AWAKENED_SPELL_BULLET_MINE.get());
        output.accept(PsitweaksItems.AWAKENED_SPELL_BULLET_CHARGE.get());
        output.accept(PsitweaksItems.AWAKENED_SPELL_BULLET_GRENADE.get());
        output.accept(PsitweaksItems.AWAKENED_SPELL_BULLET_PROJECTILE.get());
        output.accept(PsitweaksItems.AWAKENED_SPELL_BULLET_CIRCLE.get());
        output.accept(PsitweaksItems.TRANSCENDENT_SPELL_BULLET.get());
        output.accept(PsitweaksItems.TRANSCENDENT_SPELL_BULLET_LOOP.get());
        output.accept(PsitweaksItems.TRANSCENDENT_SPELL_BULLET_MINE.get());
        output.accept(PsitweaksItems.TRANSCENDENT_SPELL_BULLET_CHARGE.get());
        output.accept(PsitweaksItems.TRANSCENDENT_SPELL_BULLET_GRENADE.get());
        output.accept(PsitweaksItems.TRANSCENDENT_SPELL_BULLET_PROJECTILE.get());
        output.accept(PsitweaksItems.TRANSCENDENT_SPELL_BULLET_CIRCLE.get());
    }

    private static void acceptProgramItems(CreativeModeTab.Output output) {
        output.accept(PsitweaksItems.PROGRAM_BLANK.get());
        output.accept(PsitweaksItems.PROGRAM_COCYTUS.get());
        output.accept(PsitweaksItems.PROGRAM_TIME_ACCELERATE.get());
        output.accept(PsitweaksItems.PROGRAM_FLIGHT.get());
        output.accept(PsitweaksItems.PROGRAM_PHONON_MASER.get());
        output.accept(PsitweaksItems.PROGRAM_METEOR_LINE.get());
        output.accept(PsitweaksItems.PROGRAM_SUPREME_INFUSION.get());
        output.accept(PsitweaksItems.PROGRAM_MOLECULAR_DIVIDER.get());
        if (MekanismCompat.isMekanismLoaded()) {
            output.accept(PsitweaksItems.PROGRAM_RADIATION_INJECTION.get());
            output.accept(PsitweaksItems.PROGRAM_RADIATION_FILTER.get());
            output.accept(PsitweaksItems.PROGRAM_CURE_RADIATION.get());
        }
        output.accept(PsitweaksItems.PROGRAM_GUILLOTINE.get());
        output.accept(PsitweaksItems.PROGRAM_ACTIVE_AIR_MINE.get());
        output.accept(PsitweaksItems.PROGRAM_DIE_FLEX.get());
        output.accept(PsitweaksItems.PROGRAM_JUMP_FLEX.get());
        output.accept(PsitweaksItems.PROGRAM_SWITCH_FLEX.get());
        output.accept(PsitweaksItems.PROGRAM_MATERIAL_MUTATION.get());
        output.accept(PsitweaksItems.PROGRAM_MASS_BLOCK_BREAK.get());
    }
}
