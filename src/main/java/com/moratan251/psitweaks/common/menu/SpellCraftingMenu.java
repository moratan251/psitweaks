package com.moratan251.psitweaks.common.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;

/**
 * The 3x3 crafting screen of Trick: Crafting Table. It stays open without a crafting table block nearby; the client
 * sees a plain {@link CraftingMenu}, since the menu type is the vanilla one.
 * <p>
 * The access must point at a real level: {@link CraftingMenu} updates the result slot and hands the grid back to the
 * player through it, and {@link ContainerLevelAccess#NULL} would skip both.
 */
public class SpellCraftingMenu extends CraftingMenu {
    public SpellCraftingMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(containerId, playerInventory, access);
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive();
    }
}
