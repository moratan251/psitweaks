package com.moratan251.psitweaks.common.menu.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import vazkii.psi.api.cad.ISocketable;
import vazkii.psi.common.item.ItemSpellBullet;

import java.util.function.Supplier;

/**
 * CAD(ItemCAD) の socket に直接紐づく Slot。
 * Containerはダミーで、get/set は ISocketable に委譲する。
 */
public class SlotBulletSocket extends Slot {

    private final Supplier<ISocketable> socketableSupplier;
    private final Supplier<Boolean> enabledSupplier;
    private final int socketIndex;

    public SlotBulletSocket(Container dummyContainer,
                            int dummyIndex,
                            int x, int y,
                            Supplier<ISocketable> socketableSupplier,
                            int socketIndex,
                            Supplier<Boolean> enabledSupplier) {
        super(dummyContainer, dummyIndex, x, y);
        this.socketableSupplier = socketableSupplier;
        this.socketIndex = socketIndex;
        this.enabledSupplier = enabledSupplier;
    }

    @Override
    public boolean isActive() {
        return enabledSupplier.get();
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return stack.isEmpty() || stack.getItem() instanceof ItemSpellBullet;
    }

    @Override
    public @NotNull ItemStack getItem() {
        ISocketable s = socketableSupplier.get();
        if (s == null) return ItemStack.EMPTY;
        return s.getBulletInSocket(socketIndex);
    }

    @Override
    public void set(@NotNull ItemStack stack) {
        ISocketable s = socketableSupplier.get();
        if (s != null) {
            s.setBulletInSocket(socketIndex, stack);
        }
        setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean mayPickup(@NotNull net.minecraft.world.entity.player.Player player) {
        return isActive();
    }
}