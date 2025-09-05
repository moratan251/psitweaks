package com.moratan251.psitweaks.common.items;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.common.item.component.ItemCADAssembly;

public class ItemCADAssemblyPsitweaks extends ItemCADAssembly {

    private final String model;

    public ItemCADAssemblyPsitweaks(Properties props, String model) {
        super(props, model);
        this.model = model;
    }

    @Override
    public ResourceLocation getCADModel(ItemStack stack, ItemStack cad) {
        return ResourceLocation.fromNamespaceAndPath("psitweaks", "item/" + this.model);
    }


    @Override
    public ResourceLocation getCadTexture(ItemStack stack, ItemStack cad) {
        return ResourceLocation.fromNamespaceAndPath("psitweaks", this.model);
    }



}
