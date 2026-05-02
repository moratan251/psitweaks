package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.blocks.transmitter.BlockTranscendentCable;
import com.moratan251.psitweaks.common.items.block.ItemBlockTranscendentEnergyCube;
import com.moratan251.psitweaks.common.items.block.ItemBlockTranscendentCable;
import com.moratan251.psitweaks.common.tile.machine.TileEntityMaterialMutator;
import com.moratan251.psitweaks.common.tile.machine.TileEntityPsionicGenerator;
import com.moratan251.psitweaks.common.tile.machine.TileEntitySculkEroder;
import com.moratan251.psitweaks.common.tile.machine.TileEntityTranscendentEnergyCube;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.item.block.machine.ItemBlockMachine;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraftforge.eventbus.api.IEventBus;

public class PsitweaksMekanismBlocks {

    public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(Psitweaks.MOD_ID);

    public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntitySculkEroder, BlockTypeTile<TileEntitySculkEroder>>, ItemBlockMachine> SCULK_ERODER =
            BLOCKS.register("sculk_eroder",
                    () -> new BlockTile.BlockTileModel<>(PsitweaksMekanismBlockTypes.SCULK_ERODER, properties -> properties),
                    ItemBlockMachine::new);

    public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityMaterialMutator, BlockTypeTile<TileEntityMaterialMutator>>, ItemBlockMachine> MATERIAL_MUTATOR =
            BLOCKS.register("material_mutator",
                    () -> new BlockTile.BlockTileModel<>(PsitweaksMekanismBlockTypes.MATERIAL_MUTATOR, properties -> properties),
                    ItemBlockMachine::new);

    public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityPsionicGenerator, BlockTypeTile<TileEntityPsionicGenerator>>, ItemBlockMachine> PSIONIC_GENERATOR =
            BLOCKS.register("psionic_generator",
                    () -> new BlockTile.BlockTileModel<>(PsitweaksMekanismBlockTypes.PSIONIC_GENERATOR, properties -> properties),
                    ItemBlockMachine::new);

    public static final BlockRegistryObject<BlockTranscendentCable, ItemBlockTranscendentCable> TRANSCENDENT_CABLE =
            BLOCKS.register("transcendent_universal_cable", BlockTranscendentCable::new, ItemBlockTranscendentCable::new);

    public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityTranscendentEnergyCube, BlockTypeTile<TileEntityTranscendentEnergyCube>>, ItemBlockTranscendentEnergyCube> TRANSCENDENT_ENERGY_CUBE =
            BLOCKS.register("transcendent_energy_cube",
                    () -> new BlockTile.BlockTileModel<>(PsitweaksMekanismBlockTypes.TRANSCENDENT_ENERGY_CUBE, properties -> properties),
                    ItemBlockTranscendentEnergyCube::new);

    private PsitweaksMekanismBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
