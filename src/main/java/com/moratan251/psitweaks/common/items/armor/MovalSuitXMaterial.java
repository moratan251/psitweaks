package com.moratan251.psitweaks.common.items.armor;


import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import vazkii.psi.api.material.PsimetalArmorMaterial;

public class MovalSuitXMaterial extends PsimetalArmorMaterial {


    public static final PsimetalArmorMaterial MOVAL_SUIT_X_MATERIAL;
    public MovalSuitXMaterial(ArmorItem.Type type, Item.Properties props) {
        super("moval_suit", 0, new int[]{8, 15, 12, 7}, 60,  SoundEvents.ARMOR_EQUIP_IRON, 6.0F, () -> Ingredient.EMPTY,0.1F);
    }

    static {
        MOVAL_SUIT_X_MATERIAL = new PsimetalArmorMaterial(
                "moval_suit", // 名前
                0,                // 耐久値倍率
                new int[]{8, 15, 12, 7}, // 防御力（頭、胸、脚、足）
                60,                // エンチャント適性
                SoundEvents.ARMOR_EQUIP_IRON,
                2.0F,
                () -> Ingredient.EMPTY,
                0.1F             // ノックバック耐性
        );

    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return 0;
    }

}