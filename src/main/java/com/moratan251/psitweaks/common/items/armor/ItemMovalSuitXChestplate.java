package com.moratan251.psitweaks.common.items.armor;

import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;


public class ItemMovalSuitXChestplate extends ItemMovalSuitXArmor {
    private final ArmorMaterial material;

    public ItemMovalSuitXChestplate(ArmorMaterial material, Type type, Properties props) {

        super(material,type, props);
        this.material = material;
    }

    public String getEvent(ItemStack stack) {
        return "psi.event.damage";
    }

    public int getCastCooldown(ItemStack stack) {
        return 40;
    }

    /*
    @Override
    public int getMaxDamage(ItemStack stack) {
        return 2400;
    }

     */





    @Override
    public @NotNull ArmorMaterial getMaterial() {
        return material;
    }
}