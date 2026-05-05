package com.moratan251.psitweaks.common.blocks;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public final class PsitweaksBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Psitweaks.MOD_ID);
    public static final DeferredRegister.Items BLOCK_ITEMS = DeferredRegister.createItems(Psitweaks.MOD_ID);

    public static final DeferredBlock<Block> CAD_DISASSEMBLER = registerSimpleBlock(
            "cad_disassembler",
            BlockBehaviour.Properties.of()
                    .strength(5.0F, 15.0F)
                    .sound(SoundType.METAL)
    );
    public static final DeferredBlock<ProgramResearcherBlock> PROGRAM_RESEARCHER = registerBlock(
            "program_researcher",
            () -> new ProgramResearcherBlock(machineProperties())
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

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> blockSupplier) {
        DeferredBlock<T> block = BLOCKS.register(name, blockSupplier);
        BLOCK_ITEMS.registerSimpleBlockItem(block, new Item.Properties());
        return block;
    }

    private static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.of()
                .strength(5.0F, 15.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops();
    }
}
