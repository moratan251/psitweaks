package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.tile.machine.MaterialMutatorBlockEntity;
import com.moratan251.psitweaks.common.tile.machine.PsionicGeneratorBlockEntity;
import com.moratan251.psitweaks.common.tile.machine.SculkEroderBlockEntity;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import net.neoforged.bus.api.IEventBus;

public final class PsitweaksMekanismTileEntityTypes {
    public static final TileEntityTypeDeferredRegister TILE_ENTITY_TYPES = new TileEntityTypeDeferredRegister(Psitweaks.MOD_ID);

    public static final TileEntityTypeRegistryObject<SculkEroderBlockEntity> SCULK_ERODER =
            TILE_ENTITY_TYPES.mekBuilder(PsitweaksMekanismBlocks.SCULK_ERODER, SculkEroderBlockEntity::new)
                    .build();
    public static final TileEntityTypeRegistryObject<MaterialMutatorBlockEntity> MATERIAL_MUTATOR =
            TILE_ENTITY_TYPES.mekBuilder(PsitweaksMekanismBlocks.MATERIAL_MUTATOR, MaterialMutatorBlockEntity::new)
                    .build();
    public static final TileEntityTypeRegistryObject<PsionicGeneratorBlockEntity> PSIONIC_GENERATOR =
            TILE_ENTITY_TYPES.mekBuilder(PsitweaksMekanismBlocks.PSIONIC_GENERATOR, PsionicGeneratorBlockEntity::new)
                    .build();

    private PsitweaksMekanismTileEntityTypes() {
    }

    public static void register(IEventBus eventBus) {
        TILE_ENTITY_TYPES.register(eventBus);
    }
}
