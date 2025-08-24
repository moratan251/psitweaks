package com.moratan251.psitweaks.common.items.armor;
/*

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;

public enum MovalSuitMaterial implements ArmorMaterial {
    MOVAL_SUIT("psitweaks:moval_suit", 25, new int[]{7, 12, 15, 6}, 60,
            SoundEvents.ARMOR_EQUIP_DIAMOND, 4.0F, 0.15F,
            () -> Ingredient.of(Items.DIAMOND));

    private final String name;
    private final int durabilityMultiplier;
    private final int[] slotProtections;
    private final int enchantability;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;

    private static final int[] HEALTH_PER_SLOT = new int[]{13, 15, 16, 11};

    MovalSuitMaterial(String name, int durabilityMultiplier, int[] slotProtections,
                      int enchantability, SoundEvent equipSound,
                      float toughness, float knockbackResistance,
                      Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.slotProtections = slotProtections;
        this.enchantability = enchantability;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return HEALTH_PER_SLOT[type.getSlot().getIndex()] * this.durabilityMultiplier;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return this.slotProtections[type.getSlot().getIndex()];
    }

    @Override
    public int getEnchantmentValue() {
        return enchantability;
    }

    @Override
    public SoundEvent getEquipSound() {
        return equipSound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient.get();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public float getToughness() {
        return toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return knockbackResistance;
    }
}
*/

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.psi.api.material.PsimetalArmorMaterial;
import vazkii.psi.common.item.armor.ItemPsimetalArmor;

import net.minecraft.world.item.Item;

public class MovalSuitMaterial extends PsimetalArmorMaterial {


    public static final PsimetalArmorMaterial MOVAL_SUIT_MATERIAL;
    public MovalSuitMaterial(ArmorItem.Type type, Item.Properties props) {
        super("moval_suit", 54, new int[]{8, 15, 12, 7}, 60,  SoundEvents.ARMOR_EQUIP_IRON, 6.0F, () -> Ingredient.EMPTY,0.1F);
    }

    static {
        MOVAL_SUIT_MATERIAL = new PsimetalArmorMaterial(
                "moval_suit", // 名前
                54,                // 耐久値倍率（18 → 25 に変更）
                new int[]{8, 15, 12, 7}, // 防御力（頭、胸、脚、足）
                60,                // エンチャント適性
                SoundEvents.ARMOR_EQUIP_IRON,
                2.0F,
                () -> Ingredient.EMPTY,
                0.1F             // ノックバック耐性
        );

    }

}



/*
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;

public enum MovalSuitMaterial implements ArmorMaterial {
    MOVAL_SUIT("psitweaks:moval_suit", 25,
            new int[]{8, 15, 12, 6}, // helmet=8, chest=15, leggings=12, boots=6
            60, SoundEvents.ARMOR_EQUIP_DIAMOND,
            4.0F, 0.15F,
            () -> Ingredient.of(Items.DIAMOND));

    // ---- 以下は ArmorMaterial の実装 ----
    private final String name;
    private final int durabilityMultiplier;
    private final int[] slotProtections;
    private final int enchantability;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;

    private static final int[] HEALTH_PER_SLOT = new int[]{13, 15, 16, 11};

    MovalSuitMaterial(String name, int durabilityMultiplier, int[] slotProtections,
                      int enchantability, SoundEvent equipSound,
                      float toughness, float knockbackResistance,
                      Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.slotProtections = slotProtections;
        this.enchantability = enchantability;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return HEALTH_PER_SLOT[type.getSlot().getIndex()] * durabilityMultiplier;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return slotProtections[type.getSlot().getIndex()];
    }

    @Override
    public int getEnchantmentValue() { return enchantability; }

    @Override
    public SoundEvent getEquipSound() { return equipSound; }

    @Override
    public Ingredient getRepairIngredient() { return repairIngredient.get(); }

    @Override
    public String getName() { return name; }

    @Override
    public float getToughness() { return toughness; }

    @Override
    public float getKnockbackResistance() { return knockbackResistance; }
}

 */