package com.moratan251.psitweaks.common.chemicals;

import mekanism.api.chemical.slurry.Slurry;
import mekanism.common.registration.impl.SlurryDeferredRegister;
import mekanism.common.registration.impl.SlurryRegistryObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class PsitweaksSlurries {

    private static final TagKey<Item> ANTINITE_ORES =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath("forge", "ores/antinite"));

    public static final SlurryDeferredRegister SLURRIES = new SlurryDeferredRegister("psitweaks");

    public static final SlurryRegistryObject<Slurry, Slurry> ANTINITE =
            SLURRIES.register("antinite", builder -> builder.tint(0xbbbb60).ore(ANTINITE_ORES));

    public static void register(IEventBus eventBus) {
        SLURRIES.register(eventBus);
    }
}
