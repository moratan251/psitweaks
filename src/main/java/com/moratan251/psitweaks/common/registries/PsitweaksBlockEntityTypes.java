package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.tile.machine.ProgramResearcherBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PsitweaksBlockEntityTypes {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Psitweaks.MOD_ID);

    public static final RegistryObject<BlockEntityType<ProgramResearcherBlockEntity>> PROGRAM_RESEARCHER =
            BLOCK_ENTITY_TYPES.register("program_researcher",
                    () -> BlockEntityType.Builder.of(ProgramResearcherBlockEntity::new,
                            PsitweaksBlocks.PROGRAM_RESEARCHER.get()).build(null));

    private PsitweaksBlockEntityTypes() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
