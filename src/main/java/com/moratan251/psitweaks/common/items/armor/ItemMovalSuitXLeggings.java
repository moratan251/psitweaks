package com.moratan251.psitweaks.common.items.armor;

import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemMovalSuitXLeggings extends ItemMovalSuitXArmor {

    private  final ArmorMaterial material;

    public ItemMovalSuitXLeggings(ArmorMaterial material, Type type, Properties props) {
        super(material,type, props);
        this.material = material;
    }

    /*
    @Override
    public int getMaxDamage(ItemStack stack) {
        return 2400;
    }

     */



    @Override
    public float getToughness() {
        return 4.0f;
    }

    public String getEvent(ItemStack stack) {
        return "psi.event.tick";
    }

    public int getCastCooldown(ItemStack stack) {
        return 0;
    }

    public float getCastVolume() {
        return 0.0F;
    }


    @Override
    public @NotNull ArmorMaterial getMaterial() {
        return material;
    }
}