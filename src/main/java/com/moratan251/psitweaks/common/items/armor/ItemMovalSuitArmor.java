package com.moratan251.psitweaks.common.items.armor;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.common.item.armor.ItemPsimetalArmor;

public class ItemMovalSuitArmor extends ItemPsimetalArmor {
    private static final ResourceLocation LAYER_1 = Psitweaks.location("textures/models/armor/moval_suit_layer_1.png");
    private static final ResourceLocation LAYER_2 = Psitweaks.location("textures/models/armor/moval_suit_layer_2.png");

    protected ItemMovalSuitArmor(ArmorItem.Type type, Item.Properties properties) {
        super(type, PsitweaksArmorMaterials.MOVAL_SUIT, properties);
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return slot == EquipmentSlot.LEGS ? LAYER_2 : LAYER_1;
    }
}
