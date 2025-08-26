package com.moratan251.psitweaks.common.items.armor;

import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.radiation.item.RadiationShieldingHandler;
import mekanism.common.integration.gender.GenderCapabilityHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import vazkii.psi.api.exosuit.IPsiEventArmor;
import vazkii.psi.api.material.PsimetalArmorMaterial;
import vazkii.psi.common.item.armor.ItemPsimetalArmor;
import vazkii.psi.common.item.tool.IPsimetalTool;

import java.util.Objects;


public  class ItemMovalSuitArmor extends ItemPsimetalArmor implements IPsimetalTool, IPsiEventArmor{
    private final ArmorMaterial material;
    public static final PsimetalArmorMaterial MOVAL_SUIT_MATERIAL;


    public ItemMovalSuitArmor(ArmorMaterial material, Type type, Properties props) {
        super(type, material,props);
        this.material = material;

    }

    static {
        MOVAL_SUIT_MATERIAL = new PsimetalArmorMaterial(
                "moval_suit", // 名前
                60,                // 耐久値倍率（18 → 25 に変更）
                new int[]{3, 8, 6, 3}, // 防御力（頭、胸、脚、足）
                60,                // エンチャント適性
                SoundEvents.ARMOR_EQUIP_IRON,
                3.0F,
                () -> Ingredient.of(new ItemLike[]{(ItemLike) ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("psitweaks", "alloy_psion"))}),
                0.1F             // ノックバック耐性
        );

    }

    @Override
    public @NotNull ArmorMaterial getMaterial() {
        return material;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "psitweaks:textures/models/armor/moval_suit.png";
    }


}