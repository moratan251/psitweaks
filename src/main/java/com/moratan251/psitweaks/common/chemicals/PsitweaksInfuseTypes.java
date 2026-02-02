package com.moratan251.psitweaks.common.chemicals;


import mekanism.api.chemical.infuse.InfuseType;
import mekanism.common.registration.impl.InfuseTypeDeferredRegister;
import mekanism.common.registration.impl.InfuseTypeRegistryObject;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class PsitweaksInfuseTypes {

    public static final InfuseTypeDeferredRegister INFUSE_TYPES = new InfuseTypeDeferredRegister("psitweaks");

    public static final InfuseTypeRegistryObject<InfuseType> PSIGEM =
            INFUSE_TYPES.register("infuse_psigem", 0x747ed3);
    public static final InfuseTypeRegistryObject<InfuseType> EBONY =
            INFUSE_TYPES.register("infuse_ebony", 0x101010);
    public static final InfuseTypeRegistryObject<InfuseType> IVORY =
            INFUSE_TYPES.register("infuse_ivory", 0xf5f5f5);
    public static final InfuseTypeRegistryObject<InfuseType> CHAOTIC =
            INFUSE_TYPES.register("infuse_chaotic_factor", 0x7f7f7f);
    public static final InfuseTypeRegistryObject<InfuseType> PSIONIC_ECHO =
            INFUSE_TYPES.register("infuse_psionic_echo", 0x15044b);

    public static void register(IEventBus eventBus) {
        INFUSE_TYPES.register(eventBus);
    }
}
