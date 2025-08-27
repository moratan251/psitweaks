package com.moratan251.psitweaks.common.items.armor;

import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemMovalSuitLeggingsP extends ItemMovalSuitLeggings{

    private  final ArmorMaterial material;

    public ItemMovalSuitLeggingsP(ArmorMaterial material, Type type, Properties props) {
        super(material,type, props);
        this.material = material;
    }


    @Override
    public String getEvent(ItemStack stack) {
        return "psitweaks.event.second";
    }



    @Override
    public @NotNull ArmorMaterial getMaterial() {
        return material;
    }
}