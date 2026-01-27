package com.moratan251.psitweaks.common.items;

import com.moratan251.psitweaks.common.items.armor.*;
import com.moratan251.psitweaks.common.items.curios.ItemThirdEyeDevice;
import com.moratan251.psitweaks.common.items.curios.ItemAutoCasterSecond;
import com.moratan251.psitweaks.common.items.curios.ItemAutoCasterTick;
import com.moratan251.psitweaks.common.items.curios.ItemCuriosController;
//import com.moratan251.psitweaks.common.registries.PsitweaksModules;
import com.moratan251.psitweaks.common.registries.PsitweaksModules;
import mekanism.common.item.ItemModule;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemRegistryObject;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import vazkii.psi.common.item.component.ItemCADAssembly;


public class PsitweaksItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "psitweaks");

   // public static final RegistryObject<Item> TEST_ITEM = ITEMS.register("test_item", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> MOVAL_SUIT_HELMET = ITEMS.register("moval_suit_helmet",
            () -> new ItemMovalSuitHelmet(ItemMovalSuitArmor.MOVAL_SUIT_MATERIAL,ArmorItem.Type.HELMET, new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MOVAL_SUIT_CHESTPLATE = ITEMS.register("moval_suit_chestplate",
            () -> new ItemMovalSuitChestplate(ItemMovalSuitArmor.MOVAL_SUIT_MATERIAL,ArmorItem.Type.CHESTPLATE, new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MOVAL_SUIT_LEGGINGS = ITEMS.register("moval_suit_leggings",
            () -> new ItemMovalSuitLeggings(ItemMovalSuitArmor.MOVAL_SUIT_MATERIAL,ArmorItem.Type.LEGGINGS, new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MOVAL_SUIT_BOOTS = ITEMS.register("moval_suit_boots",
            () -> new ItemMovalSuitBoots(ItemMovalSuitArmor.MOVAL_SUIT_MATERIAL,ArmorItem.Type.BOOTS, new Item.Properties().rarity(Rarity.RARE)));

    public static final RegistryObject<Item> MOVAL_SUIT_LEGGINGS_IVORY = ITEMS.register("moval_suit_leggings_ivory",
            () -> new ItemMovalSuitLeggingsP(ItemMovalSuitArmor.MOVAL_SUIT_MATERIAL,ArmorItem.Type.LEGGINGS, new Item.Properties().rarity(Rarity.RARE)));



    //public static final RegistryObject<Item> PSYON_SUPPLY_RING = ITEMS.register("psyon_supply_ring",
     //       () -> new ItemPsyonSupplyRing(new Item.Properties().rarity(Rarity.RARE)));

    public static final RegistryObject<Item> FLASH_RING = ITEMS.register("flash_ring",
            () -> new ItemFlashRing(new Item.Properties().rarity(Rarity.RARE)));

    public static final RegistryObject<Item> PSIMETAL_BOW = ITEMS.register("psimetal_bow",
            () -> new ItemPsimetalBow(new Item.Properties()));




    public static final RegistryObject<Item> ALLOY_PSION = ITEMS.register("alloy_psion", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> PSIONIC_CONTROL_CIRCUIT = ITEMS.register("psionic_control_circuit", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> FLIGHT_CHIP = ITEMS.register("flight_chip", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> ENRICHED_PSIGEM = ITEMS.register("enriched_psigem", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ENRICHED_EBONY = ITEMS.register("enriched_ebony", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ENRICHED_IVORY = ITEMS.register("enriched_ivory", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PSIONIC_FACTOR = ITEMS.register("psionic_factor", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PSIONIC_FACTOR_IVORY = ITEMS.register("psionic_factor_ivory", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PSIONIC_FACTOR_EBONY = ITEMS.register("psionic_factor_ebony", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CHAOTIC_FACTOR = ITEMS.register("chaotic_factor", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CHAOTIC_PSIMETAL = ITEMS.register("chaotic_psimetal", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CAD_ASSEMBLY_ALLOY_PSION = ITEMS.register("cad_assembly_alloy_psion", () -> new ItemCADAssembly(new Item.Properties().rarity(Rarity.RARE),"cad_alloy_psion"));
    public static final RegistryObject<Item> CAD_ASSEMBLY_CHAOTIC_PSIMETAL = ITEMS.register("cad_assembly_chaotic_psimetal", () -> new ItemCADAssembly(new Item.Properties().rarity(Rarity.RARE),"cad_chaotic_psimetal"));

    //public static final RegistryObject<Item> CAD_ASSEMBLY_ALLOY_PSION = ITEMS.register("cad_assembly_alloy_psion", () -> new ItemCADAssemblyPsitweaks(new Item.Properties().rarity(Rarity.RARE),"cad_alloy_psion"));
    //public static final RegistryObject<Item> CAD_ASSEMBLY_CHAOTIC_PSIMETAL = ITEMS.register("cad_assembly_chaotic_psimetal", () -> new ItemCADAssemblyPsitweaks(new Item.Properties().rarity(Rarity.RARE),"cad_chaotic_psimetal"));


    public static final RegistryObject<Item> CURIOS_CONTROLLER = ITEMS.register("curios_controller", () -> new ItemCuriosController(new Item.Properties()));
    public static final RegistryObject<Item> AUTO_CASTER_SECOND = ITEMS.register("auto_caster_second", () -> new ItemAutoCasterSecond(new Item.Properties()));
    public static final RegistryObject<Item> AUTO_CASTER_TICK = ITEMS.register("auto_caster_tick", () -> new ItemAutoCasterTick(new Item.Properties()));
    public static final RegistryObject<Item> THIRD_EYE_DEVICE= ITEMS.register("third_eye_device", () -> new ItemThirdEyeDevice(new Item.Properties().rarity(Rarity.EPIC)));




    public static final ItemDeferredRegister MODULES = new ItemDeferredRegister("psitweaks");
    public static final ItemRegistryObject<ItemModule> MODULE_PSYON_SUPPLYING  = MODULES.registerModule(PsitweaksModules.PSYON_SUPPLYING_UNIT);;
/*
    static{

        MODULE_PSYON_SUPPLYING = MODULES.registerModule(PsitweaksModules.PSYON_SUPPLYING_UNIT);
    }

 */







    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);

    }


}



