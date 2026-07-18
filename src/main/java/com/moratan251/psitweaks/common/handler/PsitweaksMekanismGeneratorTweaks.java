package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import mekanism.api.math.FloatingLong;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.config.MekanismConfig;
import mekanism.generators.common.registries.GeneratorsBlockTypes;

public final class PsitweaksMekanismGeneratorTweaks {
    private PsitweaksMekanismGeneratorTweaks() {
    }

    public static void registerGeneratorTweaks() {
        GeneratorsBlockTypes.GAS_BURNING_GENERATOR.add(new AttributeEnergy(null,
                PsitweaksMekanismGeneratorTweaks::gasBurningGeneratorEnergyCapacity));
    }

    private static FloatingLong gasBurningGeneratorEnergyCapacity() {
        long overrideCapacity = PsitweaksConfig.COMMON.gasBurningGeneratorEnergyCapacity.get();
        if (overrideCapacity > 0) {
            return FloatingLong.createConst(overrideCapacity);
        }
        return MekanismConfig.general.FROM_H2.get().multiply(1_000L);
    }
}
