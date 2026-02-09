package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.tile.machine.TileEntitySculkEroder;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraftforge.eventbus.api.IEventBus;

public class PsitweaksMekanismTileEntityTypes {

    public static final TileEntityTypeDeferredRegister TILE_ENTITY_TYPES = new TileEntityTypeDeferredRegister(Psitweaks.MOD_ID);

    public static final TileEntityTypeRegistryObject<TileEntitySculkEroder> SCULK_ERODER =
            TILE_ENTITY_TYPES.register(PsitweaksMekanismBlocks.SCULK_ERODER, TileEntitySculkEroder::new,
                    (level, pos, state, tile) -> TileEntityMekanism.tickServer(level, pos, state, tile),
                    (level, pos, state, tile) -> TileEntityMekanism.tickClient(level, pos, state, tile));

    private PsitweaksMekanismTileEntityTypes() {
    }

    public static void register(IEventBus eventBus) {
        TILE_ENTITY_TYPES.register(eventBus);
    }
}
