package com.moratan251.psitweaks.common.items;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.cad.EnumCADComponent;
import vazkii.psi.api.cad.ICADAssembly;
import vazkii.psi.common.item.component.ItemCADAssembly;
import vazkii.psi.common.item.component.ItemCADComponent;
/*
public class ItemCADAssemblyPsitweaks extends ItemCADAssembly {

    private final String model;

    public ItemCADAssemblyPsitweaks(Properties props, String model) {
        super(props, model);
        this.model = model;
    }

    @Override
    public ResourceLocation getCADModel(ItemStack stack, ItemStack cad) {
        return ResourceLocation.fromNamespaceAndPath("psitweaks", "item/cad_alloy_psion");
    }


    @Override
    public ResourceLocation getCadTexture(ItemStack stack, ItemStack cad) {
        return ResourceLocation.fromNamespaceAndPath("psitweaks", "cad_alloy_psion");
    }



}

 */

public class ItemCADAssemblyPsitweaks extends ItemCADComponent implements ICADAssembly {
    private final String model;

    public ItemCADAssemblyPsitweaks(Item.Properties props, String model) {
        super(props);
        this.model = model;
    }

    public EnumCADComponent getComponentType(ItemStack stack) {
        return EnumCADComponent.ASSEMBLY;
    }

    public ResourceLocation getCADModel(ItemStack stack, ItemStack cad) {
        return ResourceLocation.fromNamespaceAndPath("psitweaks", "item/" + this.model);
    }

    public ResourceLocation getCadTexture(ItemStack stack, ItemStack cad) {
        return ResourceLocation.fromNamespaceAndPath("psitweaks", this.model);
    }
}

