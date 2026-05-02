package com.moratan251.psitweaks.common.tier;

import mekanism.api.math.FloatingLong;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum TranscendentEnergyCubeTier implements ITier, StringRepresentable {
    TRANSCENDENT;

    private static final FloatingLong MAX_ENERGY = FloatingLong.createConst(32_768_000_000L);
    private static final FloatingLong OUTPUT = FloatingLong.createConst(32_768_000L);

    @Override
    public BaseTier getBaseTier() {
        return BaseTier.ULTIMATE;
    }

    @Override
    public @NotNull String getSerializedName() {
        return "transcendent";
    }

    public FloatingLong getMaxEnergy() {
        return MAX_ENERGY;
    }

    public FloatingLong getOutput() {
        return OUTPUT;
    }
}
