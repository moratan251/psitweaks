package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import mekanism.api.math.MathUtils;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.util.ChemicalUtil;
import mekanism.generators.common.registries.GeneratorsBlockTypes;

public final class PsitweaksMekanismGeneratorTweaks {
    private PsitweaksMekanismGeneratorTweaks() {
    }

    public static void registerGeneratorTweaks() {
        GeneratorsBlockTypes.GAS_BURNING_GENERATOR.add(new AttributeEnergy(null,
                PsitweaksMekanismGeneratorTweaks::gasBurningGeneratorEnergyCapacity));
    }

    private static long gasBurningGeneratorEnergyCapacity() {
        long overrideCapacity = PsitweaksConfig.COMMON.gasBurningGeneratorEnergyCapacity.get();
        if (overrideCapacity > 0) {
            return overrideCapacity;
        }
        return MathUtils.multiplyClamped(1_000L, ChemicalUtil.hydrogenEnergyDensity());
    }
}
