package com.moratan251.psitweaks.common.storage.idea;

import net.neoforged.neoforge.energy.IEnergyStorage;

/** Synchronous FE transfers. Account only for the amount the block actually transfers. */
public final class IdeaStorageEnergyTransfer {
    private IdeaStorageEnergyTransfer() {
    }

    public static int amountForPower(double power) {
        if (!Double.isFinite(power) || power <= 0) {
            return 0;
        }
        return (int) Math.min(Integer.MAX_VALUE, power * 4000.0D);
    }

    public static int absorb(PlayerIdeaStorage storage, IEnergyStorage source, int requested) {
        if (requested <= 0 || !source.canExtract()) return 0;
        int room = (int) storage.insertEnergy(requested, true);
        int planned = Math.min(room, source.extractEnergy(room, true));
        if (planned <= 0) return 0;
        int moved = source.extractEnergy(planned, false);
        storage.insertEnergy(moved, false);
        return moved;
    }

    public static int supply(PlayerIdeaStorage storage, IEnergyStorage target, int requested) {
        if (requested <= 0 || !target.canReceive()) return 0;
        int available = (int) storage.extractEnergy(requested, true);
        int planned = Math.min(available, target.receiveEnergy(available, true));
        if (planned <= 0) return 0;
        int moved = target.receiveEnergy(planned, false);
        storage.extractEnergy(moved, false);
        return moved;
    }
}
