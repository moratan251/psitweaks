package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.common.items.CarriedItems;
import com.moratan251.psitweaks.common.items.ItemZoneReceiver;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

/**
 * プレイヤー 1 人の持ち物の FE アイテムをまとめた、荷電領域の受け手 1 つぶんの窓口。受け取った FE を、防具 → 選んでいるメインハンド →
 * オフハンド → 残りの持ち物 → Curios のスロットの順に詰める。引き出しはできない。作った時点で満タンのアイテムは含めない。
 */
public final class CarriedEnergyStorage implements IEnergyStorage {
    private final List<IEnergyStorage> storages;

    private CarriedEnergyStorage(List<IEnergyStorage> storages) {
        this.storages = storages;
    }

    /** 入の領域受電器を持つ {@code player} の窓口。受電器がないか、FE を受け取れるアイテムが 1 つもなければ null。 */
    @Nullable
    public static CarriedEnergyStorage of(Player player) {
        if (!ItemZoneReceiver.isCarriedBy(player)) {
            return null;
        }
        Inventory inventory = player.getInventory();
        List<IEnergyStorage> storages = new ArrayList<>();
        inventory.armor.forEach(stack -> addStorage(storages, stack));
        addStorage(storages, inventory.getSelected());
        inventory.offhand.forEach(stack -> addStorage(storages, stack));
        for (int i = 0; i < inventory.items.size(); i++) {
            if (i != inventory.selected) {
                addStorage(storages, inventory.items.get(i));
            }
        }
        CarriedItems.curios(player).forEach(stack -> addStorage(storages, stack));
        return storages.isEmpty() ? null : new CarriedEnergyStorage(storages);
    }

    private static void addStorage(List<IEnergyStorage> storages, ItemStack stack) {
        IEnergyStorage storage = stack.isEmpty() ? null : stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (EntityChargedZone.accepts(storage)) {
            storages.add(storage);
        }
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        int remaining = Math.max(0, toReceive);
        for (IEnergyStorage storage : storages) {
            if (remaining <= 0) {
                break;
            }
            remaining -= storage.receiveEnergy(remaining, simulate);
        }
        return Math.max(0, toReceive) - remaining;
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        long total = 0L;
        for (IEnergyStorage storage : storages) {
            total += storage.getEnergyStored();
        }
        return (int) Math.min(total, Integer.MAX_VALUE);
    }

    @Override
    public int getMaxEnergyStored() {
        long total = 0L;
        for (IEnergyStorage storage : storages) {
            total += storage.getMaxEnergyStored();
        }
        return (int) Math.min(total, Integer.MAX_VALUE);
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        for (IEnergyStorage storage : storages) {
            if (storage.canReceive()) {
                return true;
            }
        }
        return false;
    }
}
