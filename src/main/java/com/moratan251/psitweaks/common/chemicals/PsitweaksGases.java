package com.moratan251.psitweaks.common.chemicals;


import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.common.registration.impl.GasDeferredRegister;
import mekanism.common.registration.impl.GasRegistryObject;
import mekanism.common.registration.impl.InfuseTypeDeferredRegister;
import mekanism.common.registration.impl.InfuseTypeRegistryObject;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class PsitweaksGases {

    public static final GasDeferredRegister GASES = new GasDeferredRegister("psitweaks");

    public static final GasRegistryObject<Gas> PSIONIC_ECHO =
            GASES.register("gas_psionic_echo", 0x40006e);


    public static void register(IEventBus eventBus) {
        GASES.register(eventBus);
    }
}
