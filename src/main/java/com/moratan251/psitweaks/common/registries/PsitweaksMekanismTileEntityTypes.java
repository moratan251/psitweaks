package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.tile.machine.MaterialMutatorBlockEntity;
import com.moratan251.psitweaks.common.tile.machine.ProgramResearcherBlockEntity;
import com.moratan251.psitweaks.common.tile.machine.PsionicGeneratorBlockEntity;
import com.moratan251.psitweaks.common.tile.machine.SculkEroderBlockEntity;
import com.moratan251.psitweaks.common.tile.machine.TileEntityTranscendentEnergyCube;
import com.moratan251.psitweaks.common.tile.transmitter.TileEntityTranscendentCable;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.neoforged.bus.api.IEventBus;

public final class PsitweaksMekanismTileEntityTypes {
    public static final TileEntityTypeDeferredRegister TILE_ENTITY_TYPES = new TileEntityTypeDeferredRegister(Psitweaks.MOD_ID);

    public static final TileEntityTypeRegistryObject<SculkEroderBlockEntity> SCULK_ERODER =
            TILE_ENTITY_TYPES.mekBuilder(PsitweaksMekanismBlocks.SCULK_ERODER, SculkEroderBlockEntity::new)
                    .serverTicker((level, pos, state, tile) -> TileEntityMekanism.tickServer(level, pos, state, tile))
                    .clientTicker((level, pos, state, tile) -> TileEntityMekanism.tickClient(level, pos, state, tile))
                    .build();
    public static final TileEntityTypeRegistryObject<ProgramResearcherBlockEntity> PROGRAM_RESEARCHER =
            TILE_ENTITY_TYPES.mekBuilder(PsitweaksMekanismBlocks.PROGRAM_RESEARCHER, ProgramResearcherBlockEntity::new)
                    .serverTicker((level, pos, state, tile) -> TileEntityMekanism.tickServer(level, pos, state, tile))
                    .clientTicker((level, pos, state, tile) -> TileEntityMekanism.tickClient(level, pos, state, tile))
                    .build();
    public static final TileEntityTypeRegistryObject<MaterialMutatorBlockEntity> MATERIAL_MUTATOR =
            TILE_ENTITY_TYPES.mekBuilder(PsitweaksMekanismBlocks.MATERIAL_MUTATOR, MaterialMutatorBlockEntity::new)
                    .serverTicker((level, pos, state, tile) -> TileEntityMekanism.tickServer(level, pos, state, tile))
                    .clientTicker((level, pos, state, tile) -> TileEntityMekanism.tickClient(level, pos, state, tile))
                    .build();
    public static final TileEntityTypeRegistryObject<PsionicGeneratorBlockEntity> PSIONIC_GENERATOR =
            TILE_ENTITY_TYPES.mekBuilder(PsitweaksMekanismBlocks.PSIONIC_GENERATOR, PsionicGeneratorBlockEntity::new)
                    .serverTicker((level, pos, state, tile) -> TileEntityMekanism.tickServer(level, pos, state, tile))
                    .clientTicker((level, pos, state, tile) -> TileEntityMekanism.tickClient(level, pos, state, tile))
                    .build();
    public static final TileEntityTypeRegistryObject<TileEntityTranscendentCable> TRANSCENDENT_CABLE = registerTranscendentCable();
    public static final TileEntityTypeRegistryObject<TileEntityTranscendentEnergyCube> TRANSCENDENT_ENERGY_CUBE =
            TILE_ENTITY_TYPES.mekBuilder(PsitweaksMekanismBlocks.TRANSCENDENT_ENERGY_CUBE, TileEntityTranscendentEnergyCube::new)
                    .serverTicker((level, pos, state, tile) -> TileEntityMekanism.tickServer(level, pos, state, tile))
                    .clientTicker((level, pos, state, tile) -> TileEntityMekanism.tickClient(level, pos, state, tile))
                    .build();

    private PsitweaksMekanismTileEntityTypes() {
    }

    private static TileEntityTypeRegistryObject<TileEntityTranscendentCable> registerTranscendentCable() {
        var builder = TILE_ENTITY_TYPES.builder(PsitweaksMekanismBlocks.TRANSCENDENT_CABLE, TileEntityTranscendentCable::new);
        EnergyCompatUtils.addBlockCapabilities(builder);
        return builder
                .serverTicker((level, pos, state, tile) -> TileEntityTransmitter.tickServer(level, pos, state, tile))
                .withSimple(Capabilities.ALLOY_INTERACTION)
                .with(Capabilities.CONFIGURABLE, TileEntityTransmitter.CONFIGURABLE_PROVIDER)
                .build();
    }

    public static void register(IEventBus eventBus) {
        TILE_ENTITY_TYPES.register(eventBus);
    }
}
