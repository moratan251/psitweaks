package com.moratan251.psitweaks.common.chemicals;


import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasBuilder;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.math.FloatingLong;
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

    public static final GasRegistryObject<Gas> PEO_FUEL =
            GASES.register("gas_peo_fuel", () -> new Gas(GasBuilder.builder()
                    .tint(0x4d749b)
                    .with(new GasAttributes.Fuel(
                            1,    // burnTicks: 1mBあたりの燃焼tick数
                            FloatingLong.create(64000)  // energyDensity: 1mBあたりの総エネルギー量（FE）
                    ))
            ));






    public static void register(IEventBus eventBus) {
        GASES.register(eventBus);
    }
}
