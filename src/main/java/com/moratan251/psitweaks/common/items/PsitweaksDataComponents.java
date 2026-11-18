package com.moratan251.psitweaks.common.items;

import com.mojang.serialization.Codec;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.caster.AutonomousCasterStoredSettings;
import com.moratan251.psitweaks.common.caster.VirtualCad;
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

    /** The settings of an Autonomous Caster that was broken. Only set when they differ from a new machine's. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AutonomousCasterStoredSettings>> AUTONOMOUS_CASTER_SETTINGS =
            DATA_COMPONENTS.registerComponentType("autonomous_caster_settings", builder -> builder
                    .persistent(AutonomousCasterStoredSettings.CODEC)
                    .networkSynchronized(AutonomousCasterStoredSettings.STREAM_CODEC));

    /** The time and memory of a broken Autonomous Caster's internal CAD. Not set for a machine that never ran. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<VirtualCad.Memory>> AUTONOMOUS_CASTER_MEMORY =
            DATA_COMPONENTS.registerComponentType("autonomous_caster_memory", builder -> builder
                    .persistent(VirtualCad.Memory.CODEC)
                    .networkSynchronized(VirtualCad.Memory.STREAM_CODEC)
                    .cacheEncoding());

    /** 領域受電器の入（真）と切（偽）。付いていないものは入として扱う。 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ZONE_RECEIVER_ENABLED =
            DATA_COMPONENTS.registerComponentType("zone_receiver_enabled", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));

    /** 壊した領域給電機にためてあった FE。空の給電機には付けない。 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> ZONE_FEEDER_ENERGY =
            DATA_COMPONENTS.registerComponentType("zone_feeder_energy", builder -> builder
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG));

    private PsitweaksDataComponents() {
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}
