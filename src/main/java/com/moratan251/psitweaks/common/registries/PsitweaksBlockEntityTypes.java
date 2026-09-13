package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.tile.ConjuredPulsarBlockEntity;
import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PsitweaksBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Psitweaks.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConjuredPulsarBlockEntity>> CONJURED_PULSAR =
            BLOCK_ENTITY_TYPES.register("conjured_pulsar", () -> BlockEntityType.Builder.of(
                    ConjuredPulsarBlockEntity::new,
                    PsitweaksBlocks.CONJURED_PULSAR.get(),
                    PsitweaksBlocks.CONJURED_PULSAR_LIGHT.get()
            ).build(null));

    private PsitweaksBlockEntityTypes() {
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IdeaspaceConnectorBlockEntity>> IDEASPACE_CONNECTOR =
            BLOCK_ENTITY_TYPES.register("ideaspace_connector", () -> BlockEntityType.Builder.of(
                    IdeaspaceConnectorBlockEntity::new, PsitweaksBlocks.IDEASPACE_CONNECTOR.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
