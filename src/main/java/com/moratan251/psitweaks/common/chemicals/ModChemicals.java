package com.moratan251.psitweaks.common.chemicals;


import mekanism.api.chemical.infuse.InfuseType;
import mekanism.common.registration.impl.InfuseTypeDeferredRegister;
import mekanism.common.registration.impl.InfuseTypeRegistryObject;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModChemicals {

    public static final InfuseTypeDeferredRegister INFUSE_TYPES = new InfuseTypeDeferredRegister("psitweaks");

    public static final InfuseTypeRegistryObject<InfuseType> PSIGEM =
            INFUSE_TYPES.register("infuse_psigem", 0x747ed3);

    public static void register(IEventBus eventBus) {
        INFUSE_TYPES.register(eventBus);
    }
}
