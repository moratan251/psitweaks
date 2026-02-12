package com.moratan251.psitweaks.common.items;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.items.armor.*;
import com.moratan251.psitweaks.common.items.bullet.*;
import com.moratan251.psitweaks.common.items.curios.ItemThirdEyeDevice;
import com.moratan251.psitweaks.common.items.curios.ItemAutoCasterSecond;
import com.moratan251.psitweaks.common.items.curios.ItemAutoCasterTick;
import com.moratan251.psitweaks.common.items.curios.ItemCuriosController;
import com.moratan251.psitweaks.common.items.curios.ItemFlashCharm;
//import com.moratan251.psitweaks.common.registries.PsitweaksModules;
import com.moratan251.psitweaks.common.registries.PsitweaksModules;
import mekanism.common.item.ItemModule;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemRegistryObject;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
    public static final RegistryObject<Item> ALLOY_PSIONIC_ECHO = ITEMS.register("alloy_psionic_echo", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> PSIONIC_ECHO = ITEMS.register("psionic_echo", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> PROGRAM_BLANK = ITEMS.register("program_blank", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PROGRAM_COCYTUS = ITEMS.register("program_cocytus", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1)));
    public static final RegistryObject<Item> PROGRAM_TIME_ACCELERATE = ITEMS.register("program_time_accelerate", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1)));
    public static final RegistryObject<Item> PROGRAM_FLIGHT = ITEMS.register("program_flight", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1)));
    public static final RegistryObject<Item> PROGRAM_PHONON_MASER = ITEMS.register("program_phonon_maser", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1)));
    public static final RegistryObject<Item> PROGRAM_SUPREME_INFUSION = ITEMS.register("program_supreme_infusion", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1)));
    public static final RegistryObject<Item> PROGRAM_MOLECULAR_DIVIDER = ITEMS.register("program_molecular_divider", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1)));
    public static final RegistryObject<Item> PROGRAM_RADIATION_INJECTION = ITEMS.register("program_radiation_injection", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1)));
    public static final RegistryObject<Item> HEAVY_PSIMETAL_SCRAP = ITEMS.register("heavy_psimetal_scrap", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> PSIONIC_CONTROL_CIRCUIT = ITEMS.register("psionic_control_circuit", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> ECHO_CONTROL_CIRCUIT = ITEMS.register("echo_control_circuit", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> ENRICHED_PSIGEM = ITEMS.register("enriched_psigem", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ENRICHED_EBONY = ITEMS.register("enriched_ebony", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ENRICHED_IVORY = ITEMS.register("enriched_ivory", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ENRICHED_ECHO = ITEMS.register("enriched_echo", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PSIONIC_FACTOR = ITEMS.register("psionic_factor", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PSIONIC_FACTOR_IVORY = ITEMS.register("psionic_factor_ivory", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PSIONIC_FACTOR_EBONY = ITEMS.register("psionic_factor_ebony", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CHAOTIC_FACTOR = ITEMS.register("chaotic_factor", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CHAOTIC_PSIMETAL = ITEMS.register("chaotic_psimetal", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> UNREFINED_FLASHMETAL = ITEMS.register("unrefined_flashmetal", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FLASHMETAL = ITEMS.register("flashmetal", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HEAVY_PSIMETAL = ITEMS.register("heavy_psimetal", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ECHO_PELLET = ITEMS.register("echo_pellet", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> ECHO_SHEET = ITEMS.register("echo_sheet", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));




    public static final RegistryObject<Item> CAD_ASSEMBLY_ALLOY_PSION = ITEMS.register("cad_assembly_alloy_psion", () -> new ItemCADAssembly(new Item.Properties().rarity(Rarity.RARE),"cad_alloy_psion"));
    public static final RegistryObject<Item> CAD_ASSEMBLY_CHAOTIC_PSIMETAL = ITEMS.register("cad_assembly_chaotic_psimetal", () -> new ItemCADAssembly(new Item.Properties().rarity(Rarity.RARE),"cad_chaotic_psimetal"));
    public static final RegistryObject<Item> CAD_ASSEMBLY_FLASHMETAL = ITEMS.register("cad_assembly_flashmetal", () -> new ItemCADAssembly(new Item.Properties().rarity(Rarity.RARE),"cad_flashmetal"));
    public static final RegistryObject<Item> CAD_ASSEMBLY_HEAVY_PSIMETAL_ALPHA = ITEMS.register("cad_assembly_heavy_psimetal_alpha", () -> new ItemCADAssembly(new Item.Properties().rarity(Rarity.RARE),"cad_heavy_psimetal_alpha"));
    public static final RegistryObject<Item> CAD_ASSEMBLY_HEAVY_PSIMETAL_BETA = ITEMS.register("cad_assembly_heavy_psimetal_beta", () -> new ItemCADAssembly(new Item.Properties().rarity(Rarity.RARE),"cad_heavy_psimetal_beta"));
    public static final RegistryObject<Item> INCOMPLETE_HEAVY_PSIMETAL_ASSEMBLY = ITEMS.register("incomplete_heavy_psimetal_assembly", () -> new Item(new Item.Properties().stacksTo(1)));

    //public static final RegistryObject<Item> CAD_ASSEMBLY_ALLOY_PSION = ITEMS.register("cad_assembly_alloy_psion", () -> new ItemCADAssemblyPsitweaks(new Item.Properties().rarity(Rarity.RARE),"cad_alloy_psion"));
    //public static final RegistryObject<Item> CAD_ASSEMBLY_CHAOTIC_PSIMETAL = ITEMS.register("cad_assembly_chaotic_psimetal", () -> new ItemCADAssemblyPsitweaks(new Item.Properties().rarity(Rarity.RARE),"cad_chaotic_psimetal"));


    public static final RegistryObject<Item> CURIOS_CONTROLLER = ITEMS.register("curios_controller", () -> new ItemCuriosController(new Item.Properties()));
    public static final RegistryObject<Item> AUTO_CASTER_SECOND = ITEMS.register("auto_caster_second", () -> new ItemAutoCasterSecond(new Item.Properties()));
    public static final RegistryObject<Item> AUTO_CASTER_TICK = ITEMS.register("auto_caster_tick", () -> new ItemAutoCasterTick(new Item.Properties()));
    public static final RegistryObject<Item> THIRD_EYE_DEVICE= ITEMS.register("third_eye_device", () -> new ItemThirdEyeDevice(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1)));
    public static final RegistryObject<Item> FLASH_CHARM = ITEMS.register("flash_charm", () -> new ItemFlashCharm(new Item.Properties()));

    public static final RegistryObject<Item> ADVANCED_SPELL_BULLET = ITEMS.register("advanced_spell_bullet", () -> new ItemAdvancedSpellBullet(new Item.Properties()));
    public static final RegistryObject<Item> ADVANCED_SPELL_BULLET_LOOP = ITEMS.register("advanced_spell_bullet_loop", () -> new ItemAdvancedLoopcastSpellBullet(new Item.Properties()));
    public static final RegistryObject<Item> ADVANCED_SPELL_BULLET_MINE = ITEMS.register("advanced_spell_bullet_mine", () -> new ItemAdvancedMineSpellBullet(new Item.Properties()));
    public static final RegistryObject<Item> ADVANCED_SPELL_BULLET_CHARGE = ITEMS.register("advanced_spell_bullet_charge", () -> new ItemAdvancedChargeSpellBullet(new Item.Properties()));
    public static final RegistryObject<Item> ADVANCED_SPELL_BULLET_GRENADE = ITEMS.register("advanced_spell_bullet_grenade", () -> new ItemAdvancedGrenadeSpellBullet(new Item.Properties()));
    public static final RegistryObject<Item> ADVANCED_SPELL_BULLET_PROJECTILE = ITEMS.register("advanced_spell_bullet_projectile", () -> new ItemAdvancedProjectileSpellBullet(new Item.Properties()));
    public static final RegistryObject<Item> ADVANCED_SPELL_BULLET_CIRCLE = ITEMS.register("advanced_spell_bullet_circle", () -> new ItemAdvancedCircleSpellBullet(new Item.Properties()));

    public static final RegistryObject<Item> RESONANT_SPELL_BULLET = ITEMS.register("resonant_spell_bullet", () -> new ItemResonantSpellBullet(new Item.Properties()));
    public static final RegistryObject<Item> RESONANT_SPELL_BULLET_LOOP = ITEMS.register("resonant_spell_bullet_loop", () -> new ItemResonantLoopcastSpellBullet(new Item.Properties()));
    public static final RegistryObject<Item> RESONANT_SPELL_BULLET_MINE = ITEMS.register("resonant_spell_bullet_mine", () -> new ItemResonantMineSpellBullet(new Item.Properties()));
    public static final RegistryObject<Item> RESONANT_SPELL_BULLET_CHARGE = ITEMS.register("resonant_spell_bullet_charge", () -> new ItemResonantChargeSpellBullet(new Item.Properties()));
    public static final RegistryObject<Item> RESONANT_SPELL_BULLET_GRENADE = ITEMS.register("resonant_spell_bullet_grenade", () -> new ItemResonantGrenadeSpellBullet(new Item.Properties()));
    public static final RegistryObject<Item> RESONANT_SPELL_BULLET_PROJECTILE = ITEMS.register("resonant_spell_bullet_projectile", () -> new ItemResonantProjectileSpellBullet(new Item.Properties()));
    public static final RegistryObject<Item> RESONANT_SPELL_BULLET_CIRCLE = ITEMS.register("resonant_spell_bullet_circle", () -> new ItemResonantCircleSpellBullet(new Item.Properties()));

    public static final RegistryObject<Item> SUBLIMATED_SPELL_BULLET = ITEMS.register("sublimated_spell_bullet", () -> new ItemSublimatedSpellBullet(new Item.Properties()));
    public static final RegistryObject<Item> SUBLIMATED_SPELL_BULLET_LOOP = ITEMS.register("sublimated_spell_bullet_loop", () -> new ItemSublimatedLoopcastSpellBullet(new Item.Properties()));
    public static final RegistryObject<Item> SUBLIMATED_SPELL_BULLET_MINE = ITEMS.register("sublimated_spell_bullet_mine", () -> new ItemSublimatedMineSpellBullet(new Item.Properties()));
    public static final RegistryObject<Item> SUBLIMATED_SPELL_BULLET_CHARGE = ITEMS.register("sublimated_spell_bullet_charge", () -> new ItemSublimatedChargeSpellBullet(new Item.Properties()));
    public static final RegistryObject<Item> SUBLIMATED_SPELL_BULLET_GRENADE = ITEMS.register("sublimated_spell_bullet_grenade", () -> new ItemSublimatedGrenadeSpellBullet(new Item.Properties()));
    public static final RegistryObject<Item> SUBLIMATED_SPELL_BULLET_PROJECTILE = ITEMS.register("sublimated_spell_bullet_projectile", () -> new ItemSublimatedProjectileSpellBullet(new Item.Properties()));
    public static final RegistryObject<Item> SUBLIMATED_SPELL_BULLET_CIRCLE = ITEMS.register("sublimated_spell_bullet_circle", () -> new ItemSublimatedCircleSpellBullet(new Item.Properties()));

    public static final RegistryObject<Item> AWAKENED_SPELL_BULLET = ITEMS.register("awakened_spell_bullet", () -> new ItemAwakenedSpellBullet(new Item.Properties()));
    public static final RegistryObject<Item> AWAKENED_SPELL_BULLET_LOOP = ITEMS.register("awakened_spell_bullet_loop", () -> new ItemAwakenedLoopcastSpellBullet(new Item.Properties()));
    public static final RegistryObject<Item> AWAKENED_SPELL_BULLET_MINE = ITEMS.register("awakened_spell_bullet_mine", () -> new ItemAwakenedMineSpellBullet(new Item.Properties()));
    public static final RegistryObject<Item> AWAKENED_SPELL_BULLET_CHARGE = ITEMS.register("awakened_spell_bullet_charge", () -> new ItemAwakenedChargeSpellBullet(new Item.Properties()));
    public static final RegistryObject<Item> AWAKENED_SPELL_BULLET_GRENADE = ITEMS.register("awakened_spell_bullet_grenade", () -> new ItemAwakenedGrenadeSpellBullet(new Item.Properties()));
    public static final RegistryObject<Item> AWAKENED_SPELL_BULLET_PROJECTILE = ITEMS.register("awakened_spell_bullet_projectile", () -> new ItemAwakenedProjectileSpellBullet(new Item.Properties()));
    public static final RegistryObject<Item> AWAKENED_SPELL_BULLET_CIRCLE = ITEMS.register("awakened_spell_bullet_circle", () -> new ItemAwakenedCircleSpellBullet(new Item.Properties()));


    public static final RegistryObject<Item> PORTABLE_CAD_ASSEMBLER = ITEMS.register("portable_cad_assembler",
            () -> new ItemPortableCADAssembler(new Item.Properties()));




    public static final ItemDeferredRegister MODULES = new ItemDeferredRegister(Psitweaks.MOD_ID);
    public static final ItemRegistryObject<ItemModule> MODULE_PSYON_SUPPLYING  = MODULES.registerModule(PsitweaksModules.PSYON_SUPPLYING_UNIT);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        MODULES.register(eventBus);
    }


}



