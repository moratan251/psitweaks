package com.moratan251.psitweaks.common.tile.machine;

import net.neoforged.neoforge.energy.EnergyStorage;

class MutableEnergyStorage extends EnergyStorage {
    private final Runnable onChanged;

    MutableEnergyStorage(int capacity, int maxReceive, int maxExtract, Runnable onChanged) {
        super(capacity, maxReceive, maxExtract);
        this.onChanged = onChanged;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = super.receiveEnergy(maxReceive, simulate);
        if (received > 0 && !simulate) {
            onChanged.run();
        }
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = super.extractEnergy(maxExtract, simulate);
        if (extracted > 0 && !simulate) {
            onChanged.run();
        }
        return extracted;
    }

    int consumeEnergy(int amount) {
        int consumed = Math.min(amount, this.energy);
        if (consumed > 0) {
            this.energy -= consumed;
            onChanged.run();
        }
        return consumed;
    }

    int addEnergy(int amount) {
        int accepted = Math.min(amount, this.capacity - this.energy);
        if (accepted > 0) {
            this.energy += accepted;
            onChanged.run();
        }
        return accepted;
    }

    void setEnergy(int amount) {
        this.energy = Math.max(0, Math.min(this.capacity, amount));
        onChanged.run();
    }
}
