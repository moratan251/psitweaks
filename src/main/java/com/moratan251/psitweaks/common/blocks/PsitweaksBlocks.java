package com.moratan251.psitweaks.common.blocks;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class PsitweaksBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "psitweaks");
    public static final DeferredRegister<Item> BLOCK_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "psitweaks");

    // CAD分解台
    public static final RegistryObject<Block> CAD_DISASSEMBLER = registerBlock("cad_disassembler",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(5.0f, 15.0f)
                    .sound(SoundType.METAL)
            ));

    // プログラム研究台
    public static final RegistryObject<Block> PROGRAM_RESEARCHER = registerBlock("program_researcher",
            () -> new ProgramResearcherBlock(BlockBehaviour.Properties.of()
                    .strength(5.0f, 15.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
            ));

    public static final RegistryObject<Block> ORE_ANTINITE = registerBlock("ore_antinite",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(10.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
            ));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        BLOCK_ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ITEMS.register(eventBus);
    }
}
