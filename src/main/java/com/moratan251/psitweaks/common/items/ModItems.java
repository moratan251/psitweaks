package com.moratan251.psitweaks.common.items;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.items.armor.*;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Psitweaks.MOD_ID);

   // public static final RegistryObject<Item> TEST_ITEM = ITEMS.register("test_item", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> MOVAL_SUIT_HELMET = ITEMS.register("moval_suit_helmet",
            () -> new ItemMovalSuitHelmet(ItemMovalSuitArmor.MOVAL_SUIT_MATERIAL,ArmorItem.Type.HELMET, new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MOVAL_SUIT_CHESTPLATE = ITEMS.register("moval_suit_chestplate",
            () -> new ItemMovalSuitChestplate(ItemMovalSuitArmor.MOVAL_SUIT_MATERIAL,ArmorItem.Type.CHESTPLATE, new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MOVAL_SUIT_LEGGINGS = ITEMS.register("moval_suit_leggings",
            () -> new ItemMovalSuitLeggings(ItemMovalSuitArmor.MOVAL_SUIT_MATERIAL,ArmorItem.Type.LEGGINGS, new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MOVAL_SUIT_BOOTS = ITEMS.register("moval_suit_boots",
            () -> new ItemMovalSuitBoots(ItemMovalSuitArmor.MOVAL_SUIT_MATERIAL,ArmorItem.Type.BOOTS, new Item.Properties().rarity(Rarity.RARE)));

    public static final RegistryObject<Item> ENRICHED_PSIGEM = ITEMS.register("enriched_psigem", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ALLOY_PSION = ITEMS.register("alloy_psion", () -> new Item(new Item.Properties()));




    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }


}

