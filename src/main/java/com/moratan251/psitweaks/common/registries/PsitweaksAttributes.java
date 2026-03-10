package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PsitweaksAttributes {

    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, Psitweaks.MOD_ID);

    public static final RegistryObject<Attribute> PSI_MAX = ATTRIBUTES.register("psi_max",
            () -> new RangedAttribute("attribute.psitweaks.psi_max", 0.0D, -1024.0D, 1048576.0D)
                    .setSyncable(true));

    public static final RegistryObject<Attribute> PSI_REGEN = ATTRIBUTES.register("psi_regen",
            () -> new RangedAttribute("attribute.psitweaks.psi_regen", 0.0D, -1024.0D, 1048576.0D)
                    .setSyncable(true));

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }

    @Mod.EventBusSubscriber(modid = Psitweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class AttributeEvents {
        @SubscribeEvent
        public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
            event.add(EntityType.PLAYER, PSI_MAX.get());
            event.add(EntityType.PLAYER, PSI_REGEN.get());
        }
    }
}
