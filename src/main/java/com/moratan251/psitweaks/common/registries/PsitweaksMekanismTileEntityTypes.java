package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.tile.machine.TileEntityMaterialMutator;
import com.moratan251.psitweaks.common.tile.machine.TileEntitySculkEroder;
import com.moratan251.psitweaks.common.tile.transmitter.TileEntityTranscendentCable;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraftforge.eventbus.api.IEventBus;

public class PsitweaksMekanismTileEntityTypes {

    public static final TileEntityTypeDeferredRegister TILE_ENTITY_TYPES = new TileEntityTypeDeferredRegister(Psitweaks.MOD_ID);

    public static final TileEntityTypeRegistryObject<TileEntitySculkEroder> SCULK_ERODER =
            TILE_ENTITY_TYPES.register(PsitweaksMekanismBlocks.SCULK_ERODER, TileEntitySculkEroder::new,
                    (level, pos, state, tile) -> TileEntityMekanism.tickServer(level, pos, state, tile),
                    (level, pos, state, tile) -> TileEntityMekanism.tickClient(level, pos, state, tile));

    public static final TileEntityTypeRegistryObject<TileEntityMaterialMutator> MATERIAL_MUTATOR =
            TILE_ENTITY_TYPES.register(PsitweaksMekanismBlocks.MATERIAL_MUTATOR, TileEntityMaterialMutator::new,
                    (level, pos, state, tile) -> TileEntityMekanism.tickServer(level, pos, state, tile),
                    (level, pos, state, tile) -> TileEntityMekanism.tickClient(level, pos, state, tile));

    public static final TileEntityTypeRegistryObject<TileEntityTranscendentCable> TRANSCENDENT_CABLE =
            TILE_ENTITY_TYPES.builder(PsitweaksMekanismBlocks.TRANSCENDENT_CABLE, TileEntityTranscendentCable::new)
                    .serverTicker((level, pos, state, tile) -> TileEntityTransmitter.tickServer(level, pos, state, tile))
                    .build();

    private PsitweaksMekanismTileEntityTypes() {
    }

    public static void register(IEventBus eventBus) {
        TILE_ENTITY_TYPES.register(eventBus);
    }
}
