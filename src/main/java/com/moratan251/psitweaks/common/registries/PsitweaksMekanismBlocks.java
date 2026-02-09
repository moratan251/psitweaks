package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.tile.machine.TileEntitySculkEroder;
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

    private PsitweaksMekanismBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
