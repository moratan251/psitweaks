package com.moratan251.psitweaks.client.guis;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FlashRingMenu extends AbstractContainerMenu {
    private final ItemStack ring;

    public FlashRingMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, inv.player.getItemInHand(InteractionHand.MAIN_HAND)); // 送信側でItemStackを渡す
    }

    public FlashRingMenu(int id, Inventory inv, ItemStack ring) {
        super(ModMenuTypes.FLASH_RING.get(), id);
        this.ring = ring;
        // Slot追加は必要ならここで
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        return null;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    public ItemStack getRing() {
        return ring;
    }
}