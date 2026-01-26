package com.moratan251.psitweaks.common.items;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.cad.EnumCADComponent;
import vazkii.psi.api.cad.ICADAssembly;
import vazkii.psi.common.item.component.ItemCADComponent;

public class ItemCADAssemblyPsitweaks extends ItemCADComponent implements ICADAssembly {
    private final String model;

    public ItemCADAssemblyPsitweaks(Item.Properties props, String model) {
        super(props);
        this.model = model;
    }

    @Override
    public EnumCADComponent getComponentType(ItemStack stack) {
        return EnumCADComponent.ASSEMBLY;
    }

    @Override
    public ResourceLocation getCADModel(ItemStack stack, ItemStack cad) {
        // assets/psitweaks/models/item/<model>.json を参照したいので "item/<model>"
        return ResourceLocation.fromNamespaceAndPath("psitweaks", "item/" + this.model);
    }

    @Override
    public ResourceLocation getCadTexture(ItemStack stack, ItemStack cad) {
        // ここは実際に Psi 側がどう使うか次第だが、少なくとも textures/item/<model>.png の形に寄せる
        return ResourceLocation.fromNamespaceAndPath("psitweaks", "item/" + this.model);
    }
}
