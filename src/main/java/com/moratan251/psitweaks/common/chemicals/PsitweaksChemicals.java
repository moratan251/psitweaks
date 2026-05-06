package com.moratan251.psitweaks.common.chemicals;

import com.moratan251.psitweaks.Psitweaks;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.common.registration.impl.ChemicalDeferredRegister;
import mekanism.common.registration.impl.DeferredChemical;
import mekanism.common.registration.impl.SlurryRegistryObject;
import net.neoforged.bus.api.IEventBus;

public final class PsitweaksChemicals {
    public static final ChemicalDeferredRegister CHEMICALS = new ChemicalDeferredRegister(Psitweaks.MOD_ID);

    public static final DeferredChemical<Chemical> INFUSE_PSIGEM = CHEMICALS.registerInfuse("infuse_psigem", 0x747ED3);
    public static final DeferredChemical<Chemical> INFUSE_EBONY = CHEMICALS.registerInfuse("infuse_ebony", 0x101010);
    public static final DeferredChemical<Chemical> INFUSE_IVORY = CHEMICALS.registerInfuse("infuse_ivory", 0xF5F5F5);
    public static final DeferredChemical<Chemical> INFUSE_CHAOTIC_FACTOR = CHEMICALS.registerInfuse("infuse_chaotic_factor", 0x7F7F7F);
    public static final DeferredChemical<Chemical> INFUSE_PSIONIC_ECHO = CHEMICALS.registerInfuse("infuse_psionic_echo", 0x15044B);
    public static final DeferredChemical<Chemical> INFUSE_HYPOSTASIS = CHEMICALS.registerInfuse("infuse_hypostasis", 0xFFB0B0);

    public static final DeferredChemical<Chemical> GAS_PSIONIC_ECHO = CHEMICALS.register(
            "gas_psionic_echo",
            () -> new Chemical(ChemicalBuilder.builder().tint(0x40006E))
    );
    public static final DeferredChemical<Chemical> GAS_PEO_FUEL = CHEMICALS.register(
            "gas_peo_fuel",
            () -> new Chemical(ChemicalBuilder.builder()
                    .tint(0x4D749B))
    );

    public static final SlurryRegistryObject<Chemical, Chemical> ANTINITE =
            CHEMICALS.registerSlurry("antinite", builder -> builder.tint(0xBBBB60));

    private PsitweaksChemicals() {
    }

    public static void register(IEventBus eventBus) {
        CHEMICALS.register(eventBus);
    }
}
