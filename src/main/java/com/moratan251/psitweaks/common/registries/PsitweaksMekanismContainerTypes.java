package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.tile.machine.MaterialMutatorBlockEntity;
import com.moratan251.psitweaks.common.tile.machine.PsionicGeneratorBlockEntity;
import com.moratan251.psitweaks.common.tile.machine.SculkEroderBlockEntity;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.neoforged.bus.api.IEventBus;

public final class PsitweaksMekanismContainerTypes {
    public static final ContainerTypeDeferredRegister CONTAINER_TYPES = new ContainerTypeDeferredRegister(Psitweaks.MOD_ID);

    public static final ContainerTypeRegistryObject<MekanismTileContainer<SculkEroderBlockEntity>> SCULK_ERODER =
            CONTAINER_TYPES.register(PsitweaksMekanismBlocks.SCULK_ERODER, SculkEroderBlockEntity.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<MaterialMutatorBlockEntity>> MATERIAL_MUTATOR =
            CONTAINER_TYPES.register(PsitweaksMekanismBlocks.MATERIAL_MUTATOR, MaterialMutatorBlockEntity.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<PsionicGeneratorBlockEntity>> PSIONIC_GENERATOR =
            CONTAINER_TYPES.register(PsitweaksMekanismBlocks.PSIONIC_GENERATOR, PsionicGeneratorBlockEntity.class);

    private PsitweaksMekanismContainerTypes() {
    }

    public static void register(IEventBus eventBus) {
        CONTAINER_TYPES.register(eventBus);
    }
}
