package com.moratan251.psitweaks.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import net.minecraft.util.StringRepresentable;

public enum TranscendentEnergyCubeTier implements ITier, StringRepresentable {
    TRANSCENDENT;

    private static final long MAX_ENERGY = 32_768_000_000L;
    private static final long OUTPUT = 32_768_000L;

    @Override
    public BaseTier getBaseTier() {
        return BaseTier.ULTIMATE;
    }

    @Override
    public String getSerializedName() {
        return "transcendent";
    }

    public long getMaxEnergy() {
        return MAX_ENERGY;
    }

    public long getOutput() {
        return OUTPUT;
    }
}
