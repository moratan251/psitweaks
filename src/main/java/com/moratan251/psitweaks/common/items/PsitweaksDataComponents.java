package com.moratan251.psitweaks.common.items;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PsitweaksDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Psitweaks.MOD_ID);

    /**
     * Template items registered on a Filter Card. These are filter samples, not stored items, so they use their own
     * component instead of {@code minecraft:container} and are never exposed as a real inventory.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> FILTER_CARD_TEMPLATES =
            DATA_COMPONENTS.registerComponentType("filter_card_templates", builder -> builder
                    .persistent(ItemContainerContents.CODEC)
                    .networkSynchronized(ItemContainerContents.STREAM_CODEC)
                    .cacheEncoding());

    /** The internal Psi of an Autonomous Caster that was broken, put back when the machine is placed again. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> AUTONOMOUS_CASTER_PSI =
            DATA_COMPONENTS.registerComponentType("autonomous_caster_psi", builder -> builder
                    .persistent(ExtraCodecs.NON_NEGATIVE_INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    private PsitweaksDataComponents() {
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}
