package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.blocks.transmitter.BlockTranscendentCable;
import com.moratan251.psitweaks.common.items.block.ItemBlockTranscendentCable;
import com.moratan251.psitweaks.common.items.block.ItemBlockTranscendentEnergyCube;
import com.moratan251.psitweaks.common.tile.machine.AutonomousCasterBlockEntity;
import com.moratan251.psitweaks.common.tile.machine.MaterialMutatorBlockEntity;
import com.moratan251.psitweaks.common.tile.machine.ProgramResearcherBlockEntity;
import com.moratan251.psitweaks.common.tile.machine.PsionicGeneratorBlockEntity;
import com.moratan251.psitweaks.common.tile.machine.SculkEroderBlockEntity;
import com.moratan251.psitweaks.common.tile.machine.TileEntityTranscendentEnergyCube;
import java.util.Map;
import mekanism.api.RelativeSide;
import mekanism.common.attachments.component.AttachedEjector;
import mekanism.common.attachments.component.AttachedSideConfig;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;

public final class PsitweaksMekanismBlocks {
    public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(Psitweaks.MOD_ID);
    private static final AttachedSideConfig PSIONIC_GENERATOR_SIDE_CONFIG = new AttachedSideConfig(Map.of(
            TransmissionType.ENERGY, new AttachedSideConfig.LightConfigInfo(Map.of(
                    RelativeSide.BACK, DataType.OUTPUT,
                    RelativeSide.LEFT, DataType.OUTPUT,
                    RelativeSide.RIGHT, DataType.OUTPUT,
                    RelativeSide.TOP, DataType.OUTPUT,
                    RelativeSide.BOTTOM, DataType.OUTPUT
            ), true)));

    public static final BlockRegistryObject<BlockTile.BlockTileModel<SculkEroderBlockEntity, BlockTypeTile<SculkEroderBlockEntity>>, ItemBlockTooltip<BlockTile.BlockTileModel<SculkEroderBlockEntity, BlockTypeTile<SculkEroderBlockEntity>>>> SCULK_ERODER =
            BLOCKS.register("sculk_eroder",
                    () -> new BlockTile.BlockTileModel<>(PsitweaksMekanismBlockTypes.SCULK_ERODER, properties -> properties),
                    (block, properties) -> new ItemBlockTooltip<>(block, true, withDefaultSideConfig(properties, AttachedSideConfig.ELECTRIC_MACHINE)));

    public static final BlockRegistryObject<BlockTile.BlockTileModel<ProgramResearcherBlockEntity, BlockTypeTile<ProgramResearcherBlockEntity>>, ItemBlockTooltip<BlockTile.BlockTileModel<ProgramResearcherBlockEntity, BlockTypeTile<ProgramResearcherBlockEntity>>>> PROGRAM_RESEARCHER =
            BLOCKS.register("program_researcher",
                    () -> new BlockTile.BlockTileModel<>(PsitweaksMekanismBlockTypes.PROGRAM_RESEARCHER, properties -> properties),
                    (block, properties) -> new ItemBlockTooltip<>(block, true, withDefaultSideConfig(properties, AttachedSideConfig.ELECTRIC_MACHINE)));

    public static final BlockRegistryObject<BlockTile.BlockTileModel<MaterialMutatorBlockEntity, BlockTypeTile<MaterialMutatorBlockEntity>>, ItemBlockTooltip<BlockTile.BlockTileModel<MaterialMutatorBlockEntity, BlockTypeTile<MaterialMutatorBlockEntity>>>> MATERIAL_MUTATOR =
            BLOCKS.register("material_mutator",
                    () -> new BlockTile.BlockTileModel<>(PsitweaksMekanismBlockTypes.MATERIAL_MUTATOR, properties -> properties),
                    (block, properties) -> new ItemBlockTooltip<>(block, true, withDefaultSideConfig(properties, AttachedSideConfig.ADVANCED_MACHINE)));

    public static final BlockRegistryObject<BlockTile.BlockTileModel<PsionicGeneratorBlockEntity, BlockTypeTile<PsionicGeneratorBlockEntity>>, ItemBlockTooltip<BlockTile.BlockTileModel<PsionicGeneratorBlockEntity, BlockTypeTile<PsionicGeneratorBlockEntity>>>> PSIONIC_GENERATOR =
            BLOCKS.register("psionic_generator",
                    () -> new BlockTile.BlockTileModel<>(PsitweaksMekanismBlockTypes.PSIONIC_GENERATOR, properties -> properties),
                    (block, properties) -> new ItemBlockTooltip<>(block, true, withDefaultSideConfig(properties, PSIONIC_GENERATOR_SIDE_CONFIG)));

    /** 自律詠唱機の初期側面設定: 左右・上・背面からアイテム搬入、底面はエコー補充スロット、気体は全面から搬入。前面は Caster の視点側なので空ける。 */
    private static final AttachedSideConfig AUTONOMOUS_CASTER_SIDE_CONFIG = new AttachedSideConfig(Map.of(
            TransmissionType.ITEM, new AttachedSideConfig.LightConfigInfo(AutonomousCasterBlockEntity.defaultItemSides(), false),
            TransmissionType.CHEMICAL, AttachedSideConfig.LightConfigInfo.INPUT_ONLY));

    public static final BlockRegistryObject<BlockTile.BlockTileModel<AutonomousCasterBlockEntity, BlockTypeTile<AutonomousCasterBlockEntity>>, ItemBlockTooltip<BlockTile.BlockTileModel<AutonomousCasterBlockEntity, BlockTypeTile<AutonomousCasterBlockEntity>>>> AUTONOMOUS_CASTER =
            BLOCKS.register("autonomous_caster",
                    () -> new BlockTile.BlockTileModel<>(PsitweaksMekanismBlockTypes.AUTONOMOUS_CASTER, properties -> properties),
                    (block, properties) -> new ItemBlockTooltip<>(block, true, withDefaultSideConfig(properties, AUTONOMOUS_CASTER_SIDE_CONFIG)));

    public static final BlockRegistryObject<BlockTranscendentCable, ItemBlockTranscendentCable> TRANSCENDENT_CABLE =
            BLOCKS.register("transcendent_universal_cable", BlockTranscendentCable::new, ItemBlockTranscendentCable::new);

    public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityTranscendentEnergyCube, BlockTypeTile<TileEntityTranscendentEnergyCube>>, ItemBlockTranscendentEnergyCube> TRANSCENDENT_ENERGY_CUBE =
            BLOCKS.register("transcendent_energy_cube",
                    () -> new BlockTile.BlockTileModel<>(PsitweaksMekanismBlockTypes.TRANSCENDENT_ENERGY_CUBE, properties -> properties),
                    ItemBlockTranscendentEnergyCube::new);

    private PsitweaksMekanismBlocks() {
    }

    private static Item.Properties withDefaultSideConfig(Item.Properties properties, AttachedSideConfig sideConfig) {
        return properties
                .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                .component(MekanismDataComponents.SIDE_CONFIG, sideConfig);
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
