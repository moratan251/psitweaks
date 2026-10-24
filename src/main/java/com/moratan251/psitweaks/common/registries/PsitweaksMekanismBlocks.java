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
import java.util.function.Predicate;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.attachments.component.AttachedEjector;
import mekanism.common.attachments.component.AttachedSideConfig;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.chemical.ChemicalTanksBuilder;
import mekanism.common.attachments.containers.item.ItemSlotsBuilder;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;

public final class PsitweaksMekanismBlocks {
    public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(Psitweaks.MOD_ID);
    // Auto-eject out of the energy port on the back, as the tile defaults to.
    private static final AttachedSideConfig PSIONIC_GENERATOR_SIDE_CONFIG = new AttachedSideConfig(Map.of(
            TransmissionType.ENERGY, new AttachedSideConfig.LightConfigInfo(Map.of(
                    PsionicGeneratorBlockEntity.ENERGY_OUTPUT_SIDE, DataType.OUTPUT
            ), true)));

    /**
     * Machine items keep the machine's slots and tanks when it is broken. Mekanism reads them back (shift tooltip, an
     * item entity burning up) only through the item's registered layout, so each machine registers one matching its
     * tile; without it those crash ("No known containers for item"). The items only store the contents.
     */
    private static final Predicate<ItemStack> ANY_ITEM = stack -> true;
    private static final Predicate<ChemicalStack> ANY_CHEMICAL = stack -> true;

    public static final BlockRegistryObject<BlockTile.BlockTileModel<SculkEroderBlockEntity, BlockTypeTile<SculkEroderBlockEntity>>, ItemBlockTooltip<BlockTile.BlockTileModel<SculkEroderBlockEntity, BlockTypeTile<SculkEroderBlockEntity>>>> SCULK_ERODER =
            BLOCKS.register("sculk_eroder",
                    () -> new BlockTile.BlockTileModel<>(PsitweaksMekanismBlockTypes.SCULK_ERODER, properties -> properties),
                    (block, properties) -> new ItemBlockTooltip<>(block, true, withDefaultSideConfig(properties, AttachedSideConfig.ELECTRIC_MACHINE)))
                    // Input, output, energy: the slots of TileEntityElectricMachine.
                    .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                            .addInput(ANY_ITEM)
                            .addOutput()
                            .addEnergy()
                            .build()));

    public static final BlockRegistryObject<BlockTile.BlockTileModel<ProgramResearcherBlockEntity, BlockTypeTile<ProgramResearcherBlockEntity>>, ItemBlockTooltip<BlockTile.BlockTileModel<ProgramResearcherBlockEntity, BlockTypeTile<ProgramResearcherBlockEntity>>>> PROGRAM_RESEARCHER =
            BLOCKS.register("program_researcher",
                    () -> new BlockTile.BlockTileModel<>(PsitweaksMekanismBlockTypes.PROGRAM_RESEARCHER, properties -> properties),
                    (block, properties) -> new ItemBlockTooltip<>(block, true, withDefaultSideConfig(properties, AttachedSideConfig.ELECTRIC_MACHINE)))
                    // Nine inputs, output, energy: the slots of ProgramResearcherBlockEntity.
                    .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> {
                        ItemSlotsBuilder slots = ItemSlotsBuilder.builder();
                        for (int slot = 0; slot < ProgramResearcherBlockEntity.INPUT_SLOTS; slot++) {
                            slots.addInput(ANY_ITEM);
                        }
                        return slots.addOutput().addEnergy().build();
                    }));

    public static final BlockRegistryObject<BlockTile.BlockTileModel<MaterialMutatorBlockEntity, BlockTypeTile<MaterialMutatorBlockEntity>>, ItemBlockTooltip<BlockTile.BlockTileModel<MaterialMutatorBlockEntity, BlockTypeTile<MaterialMutatorBlockEntity>>>> MATERIAL_MUTATOR =
            BLOCKS.register("material_mutator",
                    () -> new BlockTile.BlockTileModel<>(PsitweaksMekanismBlockTypes.MATERIAL_MUTATOR, properties -> properties),
                    (block, properties) -> new ItemBlockTooltip<>(block, true, withDefaultSideConfig(properties, AttachedSideConfig.ADVANCED_MACHINE)))
                    // The Psionic Echo tank, and input, secondary (chemical item), output, energy: the slots of TileEntityAdvancedElectricMachine.
                    .forItemHolder(holder -> holder
                            .addAttachmentOnlyContainers(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                                    .addBasic(MaterialMutatorBlockEntity.CHEMICAL_TANK_CAPACITY, ANY_CHEMICAL)
                                    .build())
                            .addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                                    .addInput(ANY_ITEM)
                                    .addInput(ANY_ITEM)
                                    .addOutput()
                                    .addEnergy()
                                    .build()));

    public static final BlockRegistryObject<BlockTile.BlockTileModel<PsionicGeneratorBlockEntity, BlockTypeTile<PsionicGeneratorBlockEntity>>, ItemBlockTooltip<BlockTile.BlockTileModel<PsionicGeneratorBlockEntity, BlockTypeTile<PsionicGeneratorBlockEntity>>>> PSIONIC_GENERATOR =
            BLOCKS.register("psionic_generator",
                    // The glass chamber is see-through, so neighbours must keep rendering the faces behind it.
                    () -> new BlockTile.BlockTileModel<>(PsitweaksMekanismBlockTypes.PSIONIC_GENERATOR, BlockBehaviour.Properties::noOcclusion),
                    (block, properties) -> new ItemBlockTooltip<>(block, true, withDefaultSideConfig(properties, PSIONIC_GENERATOR_SIDE_CONFIG)))
                    // The one energy slot of PsionicGeneratorBlockEntity.
                    .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                            .addEnergy()
                            .build()));

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
                    ItemBlockTranscendentEnergyCube::new)
                    // Discharge and charge: the slots of TileEntityTranscendentEnergyCube, as on Mekanism's energy cubes.
                    .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                            .addEnergy()
                            .addDrainEnergy()
                            .build()));

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
