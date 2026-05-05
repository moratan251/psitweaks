package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.tile.machine.MaterialMutatorBlockEntity;
import com.moratan251.psitweaks.common.tile.machine.PsionicGeneratorBlockEntity;
import com.moratan251.psitweaks.common.tile.machine.SculkEroderBlockEntity;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.neoforged.bus.api.IEventBus;

public final class PsitweaksMekanismBlocks {
    public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(Psitweaks.MOD_ID);

    public static final BlockRegistryObject<BlockTile.BlockTileModel<SculkEroderBlockEntity, BlockTypeTile<SculkEroderBlockEntity>>, ItemBlockTooltip<BlockTile.BlockTileModel<SculkEroderBlockEntity, BlockTypeTile<SculkEroderBlockEntity>>>> SCULK_ERODER =
            BLOCKS.register("sculk_eroder",
                    () -> new BlockTile.BlockTileModel<>(PsitweaksMekanismBlockTypes.SCULK_ERODER, properties -> properties),
                    ItemBlockTooltip::new);

    public static final BlockRegistryObject<BlockTile.BlockTileModel<MaterialMutatorBlockEntity, BlockTypeTile<MaterialMutatorBlockEntity>>, ItemBlockTooltip<BlockTile.BlockTileModel<MaterialMutatorBlockEntity, BlockTypeTile<MaterialMutatorBlockEntity>>>> MATERIAL_MUTATOR =
            BLOCKS.register("material_mutator",
                    () -> new BlockTile.BlockTileModel<>(PsitweaksMekanismBlockTypes.MATERIAL_MUTATOR, properties -> properties),
                    ItemBlockTooltip::new);

    public static final BlockRegistryObject<BlockTile.BlockTileModel<PsionicGeneratorBlockEntity, BlockTypeTile<PsionicGeneratorBlockEntity>>, ItemBlockTooltip<BlockTile.BlockTileModel<PsionicGeneratorBlockEntity, BlockTypeTile<PsionicGeneratorBlockEntity>>>> PSIONIC_GENERATOR =
            BLOCKS.register("psionic_generator",
                    () -> new BlockTile.BlockTileModel<>(PsitweaksMekanismBlockTypes.PSIONIC_GENERATOR, properties -> properties),
                    ItemBlockTooltip::new);

    private PsitweaksMekanismBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
