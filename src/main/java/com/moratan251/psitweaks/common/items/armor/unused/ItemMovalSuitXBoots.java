package com.moratan251.psitweaks.common.items.armor.unused;

import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemMovalSuitXBoots extends ItemMovalSuitXArmor {
    private final ArmorMaterial material;

    public ItemMovalSuitXBoots(ArmorMaterial material, Type type, Properties props) {
        super(material, type, props);
        this.material = material;
    }

    public String getEvent(ItemStack stack) {
        return "psi.event.jump";
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