package com.moratan251.psitweaks.common.storage.idea;

import net.minecraftforge.energy.IEnergyStorage;

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
        if (requested <= 0) return 0;
        try (var transfer = storage.beginEnergyTransfer()) {
            if (transfer == null || !source.canExtract()) return 0;
            int room = (int) transfer.insert(requested, true);
            if (room <= 0) return 0;
            int planned = bounded(source.extractEnergy(room, true), room);
            if (planned <= 0) return 0;
            int moved = bounded(source.extractEnergy(planned, false), planned);
            transfer.restore(moved);
            return moved;
        }
    }

    public static int supply(PlayerIdeaStorage storage, IEnergyStorage target, int requested) {
        if (requested <= 0) return 0;
        try (var transfer = storage.beginEnergyTransfer()) {
            if (transfer == null || !target.canReceive()) return 0;
            int available = (int) transfer.extract(requested, true);
            if (available <= 0) return 0;
            int planned = bounded(target.receiveEnergy(available, true), available);
            if (planned <= 0) return 0;
            int withdrawn = (int) transfer.extract(planned, false);
            int accepted = 0;
            try {
                accepted = bounded(target.receiveEnergy(withdrawn, false), withdrawn);
                return accepted;
            } finally {
                transfer.restore(withdrawn - accepted);
            }
        }
    }

    private static int bounded(int amount, int maximum) { return Math.max(0, Math.min(amount, maximum)); }
}
