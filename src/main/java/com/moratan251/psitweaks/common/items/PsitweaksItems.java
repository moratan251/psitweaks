package com.moratan251.psitweaks.common.items;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.items.bullet.ItemPsitweaksChargeSpellBullet;
import com.moratan251.psitweaks.common.items.bullet.ItemPsitweaksCircleSpellBullet;
import com.moratan251.psitweaks.common.items.bullet.ItemPsitweaksGrenadeSpellBullet;
import com.moratan251.psitweaks.common.items.bullet.ItemPsitweaksLoopcastSpellBullet;
import com.moratan251.psitweaks.common.items.bullet.ItemPsitweaksMineSpellBullet;
import com.moratan251.psitweaks.common.items.bullet.ItemPsitweaksProjectileSpellBullet;
import com.moratan251.psitweaks.common.items.bullet.ItemPsitweaksSpellBullet;
import com.moratan251.psitweaks.common.items.bullet.SpellBulletTier;
import com.moratan251.psitweaks.common.items.curios.ItemAutoCasterCustomTick;
import com.moratan251.psitweaks.common.items.curios.ItemAutoCasterTick;
import com.moratan251.psitweaks.common.items.curios.ItemCuriosController;
import com.moratan251.psitweaks.common.items.curios.ItemFlashCharm;
import com.moratan251.psitweaks.common.items.curios.ItemInterferenceRangeExtender;
import com.moratan251.psitweaks.common.items.curios.ItemSorceryBooster;
import com.moratan251.psitweaks.common.items.curios.ItemThirdEyeDevice;
import com.moratan251.psitweaks.common.items.armor.ItemMovalSuitBoots;
import com.moratan251.psitweaks.common.items.armor.ItemMovalSuitChestplate;
import com.moratan251.psitweaks.common.items.armor.ItemMovalSuitHelmet;
import com.moratan251.psitweaks.common.items.armor.ItemMovalSuitLeggings;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.theillusivec4.curios.api.CuriosApi;
import vazkii.psi.common.item.component.ItemCADAssembly;

public final class PsitweaksItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Psitweaks.MOD_ID);

    public static final DeferredItem<Item> ALLOY_PSION = ITEMS.registerSimpleItem(
            "alloy_psion",
            new Item.Properties().rarity(Rarity.RARE)
    );
    public static final DeferredItem<Item> ALLOY_PSIONIC_ECHO = ITEMS.registerSimpleItem(
            "alloy_psionic_echo",
            new Item.Properties().rarity(Rarity.EPIC)
    );
    public static final DeferredItem<Item> ALLOY_HYPOSTASIS = ITEMS.registerSimpleItem(
            "alloy_hypostasis",
            new Item.Properties().rarity(Rarity.EPIC)
    );
    public static final DeferredItem<Item> PSIONIC_ECHO = ITEMS.registerSimpleItem(
            "psionic_echo",
            new Item.Properties().rarity(Rarity.RARE)
    );
    public static final DeferredItem<Item> HEAVY_PSIMETAL_SCRAP = ITEMS.registerSimpleItem(
            "heavy_psimetal_scrap",
            new Item.Properties()
    );
    public static final DeferredItem<Item> PSIONIC_CONTROL_CIRCUIT = ITEMS.registerSimpleItem(
            "psionic_control_circuit",
            new Item.Properties().rarity(Rarity.RARE)
    );
    public static final DeferredItem<Item> ECHO_CONTROL_CIRCUIT = ITEMS.registerSimpleItem(
            "echo_control_circuit",
            new Item.Properties().rarity(Rarity.EPIC)
    );
    public static final DeferredItem<Item> HYPOSTASIS_CONTROL_CIRCUIT = ITEMS.registerSimpleItem(
            "hypostasis_control_circuit",
            new Item.Properties().rarity(Rarity.EPIC)
    );
    public static final DeferredItem<Item> ENRICHED_PSIGEM = ITEMS.registerSimpleItem(
            "enriched_psigem",
            new Item.Properties()
    );
    public static final DeferredItem<Item> ENRICHED_EBONY = ITEMS.registerSimpleItem(
            "enriched_ebony",
            new Item.Properties()
    );
    public static final DeferredItem<Item> ENRICHED_IVORY = ITEMS.registerSimpleItem(
            "enriched_ivory",
            new Item.Properties()
    );
    public static final DeferredItem<Item> ENRICHED_ECHO = ITEMS.registerSimpleItem(
            "enriched_echo",
            new Item.Properties()
    );
    public static final DeferredItem<Item> PSIONIC_FACTOR = ITEMS.registerSimpleItem(
            "psionic_factor",
            new Item.Properties()
    );
    public static final DeferredItem<Item> PSIONIC_FACTOR_IVORY = ITEMS.registerSimpleItem(
            "psionic_factor_ivory",
            new Item.Properties()
    );
    public static final DeferredItem<Item> PSIONIC_FACTOR_EBONY = ITEMS.registerSimpleItem(
            "psionic_factor_ebony",
            new Item.Properties()
    );
    public static final DeferredItem<Item> CHAOTIC_FACTOR = ITEMS.registerSimpleItem(
            "chaotic_factor",
            new Item.Properties()
    );
    public static final DeferredItem<Item> CHAOTIC_PSIMETAL = ITEMS.registerSimpleItem(
            "chaotic_psimetal",
            new Item.Properties()
    );
    public static final DeferredItem<Item> UNREFINED_FLASHMETAL = ITEMS.registerSimpleItem(
            "unrefined_flashmetal",
            new Item.Properties()
    );
    public static final DeferredItem<Item> FLASHMETAL = ITEMS.registerSimpleItem(
            "flashmetal",
            new Item.Properties()
    );
    public static final DeferredItem<Item> HEAVY_PSIMETAL = ITEMS.registerSimpleItem(
            "heavy_psimetal",
            new Item.Properties()
    );
    public static final DeferredItem<Item> RAW_ANTINITE = ITEMS.registerSimpleItem(
            "raw_antinite",
            new Item.Properties()
    );
    public static final DeferredItem<Item> SHARD_ANTINITE = ITEMS.registerSimpleItem(
            "shard_antinite",
            new Item.Properties()
    );
    public static final DeferredItem<Item> CRYSTAL_ANTINITE = ITEMS.registerSimpleItem(
            "crystal_antinite",
            new Item.Properties()
    );
    public static final DeferredItem<Item> CLUMP_ANTINITE = ITEMS.registerSimpleItem(
            "clump_antinite",
            new Item.Properties()
    );
    public static final DeferredItem<Item> DIRTY_DUST_ANTINITE = ITEMS.registerSimpleItem(
            "dirty_dust_antinite",
            new Item.Properties()
    );
    public static final DeferredItem<Item> ANTINITE_DUST = ITEMS.registerSimpleItem(
            "antinite_dust",
            new Item.Properties()
    );
    public static final DeferredItem<Item> ANTINITE_INGOT = ITEMS.registerSimpleItem(
            "antinite_ingot",
            new Item.Properties()
    );
    public static final DeferredItem<Item> PSYCHEONIC_METAL_INGOT = ITEMS.registerSimpleItem(
            "psycheonic_metal_ingot",
            new Item.Properties()
    );
    public static final DeferredItem<Item> PSYCHEONIC_METAL_NUGGET = ITEMS.registerSimpleItem(
            "psycheonic_metal_nugget",
            new Item.Properties()
    );
    public static final DeferredItem<Item> ENRICHED_HYPOSTASIS = ITEMS.registerSimpleItem(
            "enriched_hypostasis",
            new Item.Properties()
    );
    public static final DeferredItem<Item> JADE = ITEMS.registerSimpleItem(
            "jade",
            new Item.Properties()
    );
    public static final DeferredItem<Item> MAGATAMA = ITEMS.registerSimpleItem(
            "magatama",
            new Item.Properties()
    );
    public static final DeferredItem<Item> ECHO_PELLET = ITEMS.registerSimpleItem(
            "echo_pellet",
            new Item.Properties().rarity(Rarity.UNCOMMON)
    );
    public static final DeferredItem<Item> PELLET_NEPTUNIUM = ITEMS.registerSimpleItem(
            "pellet_neptunium",
            new Item.Properties().rarity(Rarity.RARE)
    );
    public static final DeferredItem<Item> PELLET_AMERICIUM = ITEMS.registerSimpleItem(
            "pellet_americium",
            new Item.Properties().rarity(Rarity.RARE)
    );
    public static final DeferredItem<Item> HYPOSTASIS_GEM = ITEMS.registerSimpleItem(
            "hypostasis_gem",
            new Item.Properties()
    );
    public static final DeferredItem<Item> ECHO_SHEET = ITEMS.registerSimpleItem(
            "echo_sheet",
            new Item.Properties().rarity(Rarity.UNCOMMON)
    );
    public static final DeferredItem<Item> MAGICIANS_BRAIN = ITEMS.registerSimpleItem(
            "magicians_brain",
            new Item.Properties().rarity(Rarity.UNCOMMON)
    );
    public static final DeferredItem<ItemPsitweaksProgram> PROGRAM_BLANK = registerProgram("program_blank", true);
    public static final DeferredItem<ItemPsitweaksProgram> PROGRAM_COCYTUS = registerProgram("program_cocytus", false);
    public static final DeferredItem<ItemPsitweaksProgram> PROGRAM_TIME_ACCELERATE = registerProgram("program_time_accelerate", false);
    public static final DeferredItem<ItemPsitweaksProgram> PROGRAM_FLIGHT = registerProgram("program_flight", false);
    public static final DeferredItem<ItemPsitweaksProgram> PROGRAM_PHONON_MASER = registerProgram("program_phonon_maser", false);
    public static final DeferredItem<ItemPsitweaksProgram> PROGRAM_METEOR_LINE = registerProgram("program_meteor_line", false);
    public static final DeferredItem<ItemPsitweaksProgram> PROGRAM_SUPREME_INFUSION = registerProgram("program_supreme_infusion", false);
    public static final DeferredItem<ItemPsitweaksProgram> PROGRAM_MOLECULAR_DIVIDER = registerProgram("program_molecular_divider", false);
    public static final DeferredItem<ItemPsitweaksProgram> PROGRAM_RADIATION_INJECTION =
            registerMekanismProgram("program_radiation_injection");
    public static final DeferredItem<ItemPsitweaksProgram> PROGRAM_RADIATION_FILTER =
            registerMekanismProgram("program_radiation_filter");
    public static final DeferredItem<ItemPsitweaksProgram> PROGRAM_CURE_RADIATION =
            registerMekanismProgram("program_cure_radiation");
    public static final DeferredItem<ItemPsitweaksProgram> PROGRAM_GUILLOTINE = registerProgram("program_guillotine", false);
    public static final DeferredItem<ItemPsitweaksProgram> PROGRAM_ACTIVE_AIR_MINE = registerProgram("program_active_air_mine", false);
    public static final DeferredItem<ItemPsitweaksProgram> PROGRAM_DIE_FLEX = registerProgram("program_die_flex", false);
    public static final DeferredItem<ItemPsitweaksProgram> PROGRAM_JUMP_FLEX = registerProgram("program_jump_flex", false);
    public static final DeferredItem<ItemPsitweaksProgram> PROGRAM_SWITCH_FLEX = registerProgram("program_switch_flex", false);
    public static final DeferredItem<ItemPsitweaksProgram> PROGRAM_MATERIAL_MUTATION = registerProgram("program_material_mutation", false);
    public static final DeferredItem<ItemPsitweaksProgram> PROGRAM_MASS_BLOCK_BREAK = registerProgram("program_mass_block_break", false);
    public static final DeferredItem<ItemPhilosophersStone> PHILOSOPHERS_STONE = ITEMS.register(
            "philosophers_stone",
            () -> new ItemPhilosophersStone(new Item.Properties().rarity(Rarity.RARE))
    );
    public static final DeferredItem<ItemPsimetalBow> PSIMETAL_BOW = ITEMS.register(
            "psimetal_bow",
            () -> new ItemPsimetalBow(new Item.Properties().rarity(Rarity.RARE))
    );
    public static final DeferredItem<ItemCuriosController> CURIOS_CONTROLLER = ITEMS.register(
            "curios_controller",
            () -> new ItemCuriosController(new Item.Properties().rarity(Rarity.RARE))
    );
    public static final DeferredItem<ItemAutoCasterTick> AUTO_CASTER_TICK = ITEMS.register(
            "auto_caster_tick",
            () -> new ItemAutoCasterTick(new Item.Properties())
    );
    public static final DeferredItem<ItemAutoCasterCustomTick> AUTO_CASTER_CUSTOM_TICK = ITEMS.register(
            "auto_caster_custom_tick",
            () -> new ItemAutoCasterCustomTick(new Item.Properties())
    );
    public static final DeferredItem<ItemSpellMagazine> SPELL_MAGAZINE = ITEMS.register(
            "spell_magazine",
            () -> new ItemSpellMagazine(new Item.Properties().rarity(Rarity.RARE))
    );
    public static final DeferredItem<ItemPortableCADAssembler> PORTABLE_CAD_ASSEMBLER = ITEMS.register(
            "portable_cad_assembler",
            () -> new ItemPortableCADAssembler(new Item.Properties().rarity(Rarity.RARE))
    );
    public static final DeferredItem<ItemFlashCharm> FLASH_CHARM = ITEMS.register(
            "flash_charm",
            () -> new ItemFlashCharm(new Item.Properties())
    );
    public static final DeferredItem<ItemInterferenceRangeExtender> INTERFERENCE_RANGE_EXTENDER = ITEMS.register(
            "interference_range_extender",
            () -> new ItemInterferenceRangeExtender(new Item.Properties())
    );
    public static final DeferredItem<ItemThirdEyeDevice> THIRD_EYE_DEVICE = ITEMS.register(
            "third_eye_device",
            () -> new ItemThirdEyeDevice(new Item.Properties())
    );
    public static final DeferredItem<ItemSorceryBooster> SORCERY_BOOSTER = ITEMS.register(
            "sorcery_booster",
            () -> new ItemSorceryBooster(new Item.Properties())
    );
    public static final DeferredItem<ItemMovalSuitHelmet> MOVAL_SUIT_HELMET = ITEMS.register(
            "moval_suit_helmet",
            () -> new ItemMovalSuitHelmet(ArmorItem.Type.HELMET, movalSuitProperties(ArmorItem.Type.HELMET))
    );
    public static final DeferredItem<ItemMovalSuitChestplate> MOVAL_SUIT_CHESTPLATE = ITEMS.register(
            "moval_suit_chestplate",
            () -> new ItemMovalSuitChestplate(ArmorItem.Type.CHESTPLATE, movalSuitProperties(ArmorItem.Type.CHESTPLATE))
    );
    public static final DeferredItem<ItemMovalSuitLeggings> MOVAL_SUIT_LEGGINGS = ITEMS.register(
            "moval_suit_leggings",
            () -> new ItemMovalSuitLeggings(ArmorItem.Type.LEGGINGS, movalSuitProperties(ArmorItem.Type.LEGGINGS))
    );
    public static final DeferredItem<ItemMovalSuitBoots> MOVAL_SUIT_BOOTS = ITEMS.register(
            "moval_suit_boots",
            () -> new ItemMovalSuitBoots(ArmorItem.Type.BOOTS, movalSuitProperties(ArmorItem.Type.BOOTS))
    );
    public static final DeferredItem<ItemPsitweaksSpellBullet> ADVANCED_SPELL_BULLET = registerSpellBullet(
            "advanced_spell_bullet",
            SpellBulletTier.ADVANCED
    );
    public static final DeferredItem<ItemPsitweaksLoopcastSpellBullet> ADVANCED_SPELL_BULLET_LOOP = registerLoopcastSpellBullet(
            "advanced_spell_bullet_loop",
            SpellBulletTier.ADVANCED
    );
    public static final DeferredItem<ItemPsitweaksMineSpellBullet> ADVANCED_SPELL_BULLET_MINE = registerMineSpellBullet(
            "advanced_spell_bullet_mine",
            SpellBulletTier.ADVANCED
    );
    public static final DeferredItem<ItemPsitweaksChargeSpellBullet> ADVANCED_SPELL_BULLET_CHARGE = registerChargeSpellBullet(
            "advanced_spell_bullet_charge",
            SpellBulletTier.ADVANCED
    );
    public static final DeferredItem<ItemPsitweaksGrenadeSpellBullet> ADVANCED_SPELL_BULLET_GRENADE = registerGrenadeSpellBullet(
            "advanced_spell_bullet_grenade",
            SpellBulletTier.ADVANCED
    );
    public static final DeferredItem<ItemPsitweaksProjectileSpellBullet> ADVANCED_SPELL_BULLET_PROJECTILE = registerProjectileSpellBullet(
            "advanced_spell_bullet_projectile",
            SpellBulletTier.ADVANCED
    );
    public static final DeferredItem<ItemPsitweaksCircleSpellBullet> ADVANCED_SPELL_BULLET_CIRCLE = registerCircleSpellBullet(
            "advanced_spell_bullet_circle",
            SpellBulletTier.ADVANCED
    );
    public static final DeferredItem<ItemPsitweaksSpellBullet> RESONANT_SPELL_BULLET = registerSpellBullet(
            "resonant_spell_bullet",
            SpellBulletTier.RESONANT
    );
    public static final DeferredItem<ItemPsitweaksLoopcastSpellBullet> RESONANT_SPELL_BULLET_LOOP = registerLoopcastSpellBullet(
            "resonant_spell_bullet_loop",
            SpellBulletTier.RESONANT
    );
    public static final DeferredItem<ItemPsitweaksMineSpellBullet> RESONANT_SPELL_BULLET_MINE = registerMineSpellBullet(
            "resonant_spell_bullet_mine",
            SpellBulletTier.RESONANT
    );
    public static final DeferredItem<ItemPsitweaksChargeSpellBullet> RESONANT_SPELL_BULLET_CHARGE = registerChargeSpellBullet(
            "resonant_spell_bullet_charge",
            SpellBulletTier.RESONANT
    );
    public static final DeferredItem<ItemPsitweaksGrenadeSpellBullet> RESONANT_SPELL_BULLET_GRENADE = registerGrenadeSpellBullet(
            "resonant_spell_bullet_grenade",
            SpellBulletTier.RESONANT
    );
    public static final DeferredItem<ItemPsitweaksProjectileSpellBullet> RESONANT_SPELL_BULLET_PROJECTILE = registerProjectileSpellBullet(
            "resonant_spell_bullet_projectile",
            SpellBulletTier.RESONANT
    );
    public static final DeferredItem<ItemPsitweaksCircleSpellBullet> RESONANT_SPELL_BULLET_CIRCLE = registerCircleSpellBullet(
            "resonant_spell_bullet_circle",
            SpellBulletTier.RESONANT
    );
    public static final DeferredItem<ItemPsitweaksSpellBullet> SUBLIMATED_SPELL_BULLET = registerSpellBullet(
            "sublimated_spell_bullet",
            SpellBulletTier.SUBLIMATED
    );
    public static final DeferredItem<ItemPsitweaksLoopcastSpellBullet> SUBLIMATED_SPELL_BULLET_LOOP = registerLoopcastSpellBullet(
            "sublimated_spell_bullet_loop",
            SpellBulletTier.SUBLIMATED
    );
    public static final DeferredItem<ItemPsitweaksMineSpellBullet> SUBLIMATED_SPELL_BULLET_MINE = registerMineSpellBullet(
            "sublimated_spell_bullet_mine",
            SpellBulletTier.SUBLIMATED
    );
    public static final DeferredItem<ItemPsitweaksChargeSpellBullet> SUBLIMATED_SPELL_BULLET_CHARGE = registerChargeSpellBullet(
            "sublimated_spell_bullet_charge",
            SpellBulletTier.SUBLIMATED
    );
    public static final DeferredItem<ItemPsitweaksGrenadeSpellBullet> SUBLIMATED_SPELL_BULLET_GRENADE = registerGrenadeSpellBullet(
            "sublimated_spell_bullet_grenade",
            SpellBulletTier.SUBLIMATED
    );
    public static final DeferredItem<ItemPsitweaksProjectileSpellBullet> SUBLIMATED_SPELL_BULLET_PROJECTILE = registerProjectileSpellBullet(
            "sublimated_spell_bullet_projectile",
            SpellBulletTier.SUBLIMATED
    );
    public static final DeferredItem<ItemPsitweaksCircleSpellBullet> SUBLIMATED_SPELL_BULLET_CIRCLE = registerCircleSpellBullet(
            "sublimated_spell_bullet_circle",
            SpellBulletTier.SUBLIMATED
    );
    public static final DeferredItem<ItemPsitweaksSpellBullet> AWAKENED_SPELL_BULLET = registerSpellBullet(
            "awakened_spell_bullet",
            SpellBulletTier.AWAKENED
    );
    public static final DeferredItem<ItemPsitweaksLoopcastSpellBullet> AWAKENED_SPELL_BULLET_LOOP = registerLoopcastSpellBullet(
            "awakened_spell_bullet_loop",
            SpellBulletTier.AWAKENED
    );
    public static final DeferredItem<ItemPsitweaksMineSpellBullet> AWAKENED_SPELL_BULLET_MINE = registerMineSpellBullet(
            "awakened_spell_bullet_mine",
            SpellBulletTier.AWAKENED
    );
    public static final DeferredItem<ItemPsitweaksChargeSpellBullet> AWAKENED_SPELL_BULLET_CHARGE = registerChargeSpellBullet(
            "awakened_spell_bullet_charge",
            SpellBulletTier.AWAKENED
    );
    public static final DeferredItem<ItemPsitweaksGrenadeSpellBullet> AWAKENED_SPELL_BULLET_GRENADE = registerGrenadeSpellBullet(
            "awakened_spell_bullet_grenade",
            SpellBulletTier.AWAKENED
    );
    public static final DeferredItem<ItemPsitweaksProjectileSpellBullet> AWAKENED_SPELL_BULLET_PROJECTILE = registerProjectileSpellBullet(
            "awakened_spell_bullet_projectile",
            SpellBulletTier.AWAKENED
    );
    public static final DeferredItem<ItemPsitweaksCircleSpellBullet> AWAKENED_SPELL_BULLET_CIRCLE = registerCircleSpellBullet(
            "awakened_spell_bullet_circle",
            SpellBulletTier.AWAKENED
    );
    public static final DeferredItem<ItemPsitweaksSpellBullet> TRANSCENDENT_SPELL_BULLET = registerSpellBullet(
            "transcendent_spell_bullet",
            SpellBulletTier.TRANSCENDENT
    );
    public static final DeferredItem<ItemPsitweaksLoopcastSpellBullet> TRANSCENDENT_SPELL_BULLET_LOOP = registerLoopcastSpellBullet(
            "transcendent_spell_bullet_loop",
            SpellBulletTier.TRANSCENDENT
    );
    public static final DeferredItem<ItemPsitweaksMineSpellBullet> TRANSCENDENT_SPELL_BULLET_MINE = registerMineSpellBullet(
            "transcendent_spell_bullet_mine",
            SpellBulletTier.TRANSCENDENT
    );
    public static final DeferredItem<ItemPsitweaksChargeSpellBullet> TRANSCENDENT_SPELL_BULLET_CHARGE = registerChargeSpellBullet(
            "transcendent_spell_bullet_charge",
            SpellBulletTier.TRANSCENDENT
    );
    public static final DeferredItem<ItemPsitweaksGrenadeSpellBullet> TRANSCENDENT_SPELL_BULLET_GRENADE = registerGrenadeSpellBullet(
            "transcendent_spell_bullet_grenade",
            SpellBulletTier.TRANSCENDENT
    );
    public static final DeferredItem<ItemPsitweaksProjectileSpellBullet> TRANSCENDENT_SPELL_BULLET_PROJECTILE = registerProjectileSpellBullet(
            "transcendent_spell_bullet_projectile",
            SpellBulletTier.TRANSCENDENT
    );
    public static final DeferredItem<ItemPsitweaksCircleSpellBullet> TRANSCENDENT_SPELL_BULLET_CIRCLE = registerCircleSpellBullet(
            "transcendent_spell_bullet_circle",
            SpellBulletTier.TRANSCENDENT
    );
    public static final DeferredItem<ItemCADAssembly> CAD_ASSEMBLY_ALLOY_PSION = registerCadAssembly(
            "cad_assembly_alloy_psion",
            Rarity.RARE,
            "cad_alloy_psion"
    );
    public static final DeferredItem<ItemCADAssembly> CAD_ASSEMBLY_CHAOTIC_PSIMETAL = registerCadAssembly(
            "cad_assembly_chaotic_psimetal",
            Rarity.RARE,
            "cad_chaotic_psimetal"
    );
    public static final DeferredItem<ItemCADAssembly> CAD_ASSEMBLY_FLASHMETAL = registerCadAssembly(
            "cad_assembly_flashmetal",
            Rarity.RARE,
            "cad_flashmetal"
    );
    public static final DeferredItem<ItemCADAssembly> CAD_ASSEMBLY_HEAVY_PSIMETAL_ALPHA = registerCadAssembly(
            "cad_assembly_heavy_psimetal_alpha",
            Rarity.RARE,
            "cad_heavy_psimetal_alpha"
    );
    public static final DeferredItem<ItemCADAssembly> CAD_ASSEMBLY_HEAVY_PSIMETAL_BETA = registerCadAssembly(
            "cad_assembly_heavy_psimetal_beta",
            Rarity.RARE,
            "cad_heavy_psimetal_beta"
    );
    public static final DeferredItem<ItemCADAssembly> CAD_ASSEMBLY_PSYCHEONIC_METAL = registerCadAssembly(
            "cad_assembly_psycheonic_metal",
            Rarity.EPIC,
            "cad_psycheonicmetal"
    );
    public static final DeferredItem<Item> INCOMPLETE_HEAVY_PSIMETAL_ASSEMBLY = ITEMS.registerSimpleItem(
            "incomplete_heavy_psimetal_assembly",
            new Item.Properties().stacksTo(1)
    );
    public static final DeferredItem<ItemInlineCaster> INLINE_CASTER = ITEMS.register(
            "inline_caster",
            () -> new ItemInlineCaster(new Item.Properties())
    );
    public static final DeferredItem<ItemSecondaryCaster> SECONDARY_CASTER = ITEMS.register(
            "secondary_caster",
            () -> new ItemSecondaryCaster(new Item.Properties())
    );
    public static final DeferredItem<ItemParallelCaster> PARALLEL_CASTER = ITEMS.register(
            "parallel_caster",
            () -> new ItemParallelCaster(new Item.Properties())
    );

    private PsitweaksItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static void registerCurioItems() {
        CuriosApi.registerCurio(AUTO_CASTER_TICK.get(), AUTO_CASTER_TICK.get());
        CuriosApi.registerCurio(AUTO_CASTER_CUSTOM_TICK.get(), AUTO_CASTER_CUSTOM_TICK.get());
        CuriosApi.registerCurio(FLASH_CHARM.get(), FLASH_CHARM.get());
        CuriosApi.registerCurio(INTERFERENCE_RANGE_EXTENDER.get(), INTERFERENCE_RANGE_EXTENDER.get());
        CuriosApi.registerCurio(THIRD_EYE_DEVICE.get(), THIRD_EYE_DEVICE.get());
        CuriosApi.registerCurio(SORCERY_BOOSTER.get(), SORCERY_BOOSTER.get());
    }

    private static DeferredItem<ItemCADAssembly> registerCadAssembly(String id, Rarity rarity, String model) {
        return ITEMS.register(id, () -> new ItemCADAssembly(new Item.Properties().rarity(rarity), model));
    }

    private static DeferredItem<ItemPsitweaksProgram> registerProgram(String id, boolean blankProgram) {
        return ITEMS.register(id, () -> new ItemPsitweaksProgram(programProperties(blankProgram), blankProgram));
    }

    private static Item.Properties programProperties(boolean blankProgram) {
        if (blankProgram) {
            return new Item.Properties();
        }
        return new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1);
    }

    private static Item.Properties movalSuitProperties(ArmorItem.Type type) {
        return new Item.Properties()
                .rarity(Rarity.RARE)
                .durability(type.getDurability(60));
    }

    private static DeferredItem<ItemPsitweaksProgram> registerMekanismProgram(String id) {
        return MekanismCompat.isMekanismLoaded() ? registerProgram(id, false) : null;
    }

    private static DeferredItem<ItemPsitweaksSpellBullet> registerSpellBullet(String id, SpellBulletTier tier) {
        return ITEMS.register(id, () -> new ItemPsitweaksSpellBullet(new Item.Properties(), tier));
    }

    private static DeferredItem<ItemPsitweaksLoopcastSpellBullet> registerLoopcastSpellBullet(String id, SpellBulletTier tier) {
        return ITEMS.register(id, () -> new ItemPsitweaksLoopcastSpellBullet(new Item.Properties(), tier));
    }

    private static DeferredItem<ItemPsitweaksMineSpellBullet> registerMineSpellBullet(String id, SpellBulletTier tier) {
        return ITEMS.register(id, () -> new ItemPsitweaksMineSpellBullet(new Item.Properties(), tier));
    }

    private static DeferredItem<ItemPsitweaksChargeSpellBullet> registerChargeSpellBullet(String id, SpellBulletTier tier) {
        return ITEMS.register(id, () -> new ItemPsitweaksChargeSpellBullet(new Item.Properties(), tier));
    }

    private static DeferredItem<ItemPsitweaksGrenadeSpellBullet> registerGrenadeSpellBullet(String id, SpellBulletTier tier) {
        return ITEMS.register(id, () -> new ItemPsitweaksGrenadeSpellBullet(new Item.Properties(), tier));
    }

    private static DeferredItem<ItemPsitweaksProjectileSpellBullet> registerProjectileSpellBullet(String id, SpellBulletTier tier) {
        return ITEMS.register(id, () -> new ItemPsitweaksProjectileSpellBullet(new Item.Properties(), tier));
    }

    private static DeferredItem<ItemPsitweaksCircleSpellBullet> registerCircleSpellBullet(String id, SpellBulletTier tier) {
        return ITEMS.register(id, () -> new ItemPsitweaksCircleSpellBullet(new Item.Properties(), tier));
    }
}
