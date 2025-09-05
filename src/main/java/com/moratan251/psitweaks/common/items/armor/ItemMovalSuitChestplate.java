package com.moratan251.psitweaks.common.items.armor;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;


public class ItemMovalSuitChestplate extends ItemMovalSuitArmor {
    private final ArmorMaterial material;

    public ItemMovalSuitChestplate(ArmorMaterial material, Type type, Properties props) {

        super(material,type, props);
        this.material = material;
    }

    public String getEvent(ItemStack stack) {
        return "psi.event.damage";
    }

    @Override
    public @NotNull ArmorMaterial getMaterial() {
        return material;
    }

    @Override
    public void onInventoryTick(ItemStack stack, Level level, Player player) {

    }
}