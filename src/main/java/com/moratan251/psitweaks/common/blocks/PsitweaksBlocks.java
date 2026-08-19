package com.moratan251.psitweaks.common.blocks;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PsitweaksBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Psitweaks.MOD_ID);
    public static final DeferredRegister.Items BLOCK_ITEMS = DeferredRegister.createItems(Psitweaks.MOD_ID);

    public static final DeferredBlock<ConjuredPulsarBlock> CONJURED_PULSAR = BLOCKS.register(
            "conjuredpulsar",
            () -> new ConjuredPulsarBlock(conjuredPulsarProperties(), true, false)
    );
    public static final DeferredBlock<ConjuredPulsarBlock> CONJURED_PULSAR_LIGHT = BLOCKS.register(
            "conjuredpulsarlight",
            () -> new ConjuredPulsarBlock(conjuredPulsarProperties(), false, true)
    );

    public static final DeferredBlock<Block> CAD_DISASSEMBLER = registerSimpleBlock(
            "cad_disassembler",
            BlockBehaviour.Properties.of()
                    .strength(5.0F, 15.0F)
                    .sound(SoundType.METAL)
    );
    public static final DeferredBlock<Block> ORE_ANTINITE = registerSimpleBlock(
            "ore_antinite",
            BlockBehaviour.Properties.of()
                    .strength(10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
    );
    public static final DeferredBlock<Block> ANTINITE_BLOCK = registerSimpleBlock(
            "antinite_block",
            BlockBehaviour.Properties.of()
                    .strength(10.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
    );
    public static final DeferredBlock<Block> CHAOTIC_PSIMETAL_BLOCK = registerSimpleBlock(
            "chaotic_psimetal_block",
            BlockBehaviour.Properties.of()
                    .strength(5.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
    );
    public static final DeferredBlock<Block> FLASHMETAL_BLOCK = registerSimpleBlock(
            "flashmetal_block",
            BlockBehaviour.Properties.of()
                    .strength(5.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
    );
    public static final DeferredBlock<Block> HEAVY_PSIMETAL_BLOCK = registerSimpleBlock(
            "heavy_psimetal_block",
            BlockBehaviour.Properties.of()
                    .strength(10.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
    );
    public static final DeferredBlock<Block> PLUTONIUM_BLOCK = registerSimpleBlock(
            "plutonium_block",
            BlockBehaviour.Properties.of()
                    .strength(5.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
    );
    public static final DeferredBlock<Block> POLONIUM_BLOCK = registerSimpleBlock(
            "polonium_block",
            BlockBehaviour.Properties.of()
                    .strength(5.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
    );
    public static final DeferredBlock<Block> RAW_ANTINITE_BLOCK = registerSimpleBlock(
            "raw_antinite_block",
            BlockBehaviour.Properties.of()
                    .strength(10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
    );
    public static final DeferredBlock<Block> SPELLMACHINERY_CASING = registerSimpleBlock(
            "spellmachinery_casing",
            BlockBehaviour.Properties.of()
                    .strength(8.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
    );
    public static final DeferredBlock<Block> PSYCHEONIC_METAL_BLOCK = registerSimpleBlock(
            "psycheonic_metal_block",
            BlockBehaviour.Properties.of()
                    .strength(10.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
    );
    public static final DeferredBlock<Block> HYPOSTASIS_GEM_BLOCK = registerSimpleBlock(
            "hypostasis_gem_block",
            BlockBehaviour.Properties.of()
                    .strength(5.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
    );
    public static final DeferredBlock<Block> PSYCHEONIC_METAL_CRUX = BLOCKS.register(
            "psycheonic_metal_crux",
            () -> new Block(BlockBehaviour.Properties.of()
                    .sound(SoundType.STONE)
                    .strength(5.0F, 10.0F))
    );
    public static final DeferredItem<BlockItem> PSYCHEONIC_METAL_CRUX_ITEM = BLOCK_ITEMS.register(
            "psycheonic_metal_crux",
            () -> new BlockItem(PSYCHEONIC_METAL_CRUX.get(), new Item.Properties())
    );

    private PsitweaksBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ITEMS.register(eventBus);
    }

    private static DeferredBlock<Block> registerSimpleBlock(String name, BlockBehaviour.Properties properties) {
        DeferredBlock<Block> block = BLOCKS.registerSimpleBlock(name, properties);
        BLOCK_ITEMS.registerSimpleBlockItem(block, new Item.Properties());
        return block;
    }

    private static BlockBehaviour.Properties conjuredPulsarProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.NONE)
                .instrument(NoteBlockInstrument.HAT)
                .strength(0.3F)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .noLootTable()
                .lightLevel(state -> state.getValue(ConjuredPulsarBlock.LIGHT) ? 15 : 0)
                .isValidSpawn((state, level, pos, entityType) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false);
    }

}
