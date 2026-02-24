package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.tile.machine.TileEntityMaterialMutator;
import com.moratan251.psitweaks.common.tile.machine.TileEntitySculkEroder;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.minecraftforge.eventbus.api.IEventBus;

public class PsitweaksMekanismContainerTypes {

    public static final ContainerTypeDeferredRegister CONTAINER_TYPES = new ContainerTypeDeferredRegister(Psitweaks.MOD_ID);

    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntitySculkEroder>> SCULK_ERODER =
            CONTAINER_TYPES.register(PsitweaksMekanismBlocks.SCULK_ERODER, TileEntitySculkEroder.class);

    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityMaterialMutator>> MATERIAL_MUTATOR =
            CONTAINER_TYPES.register(PsitweaksMekanismBlocks.MATERIAL_MUTATOR, TileEntityMaterialMutator.class);

    private PsitweaksMekanismContainerTypes() {
    }

    public static void register(IEventBus eventBus) {
        CONTAINER_TYPES.register(eventBus);
    }
}
