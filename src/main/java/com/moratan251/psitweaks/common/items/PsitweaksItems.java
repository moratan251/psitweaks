package com.moratan251.psitweaks.common.items;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

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
    public static final DeferredItem<Item> INCOMPLETE_HEAVY_PSIMETAL_ASSEMBLY = ITEMS.registerSimpleItem(
            "incomplete_heavy_psimetal_assembly",
            new Item.Properties().stacksTo(1)
    );

    private PsitweaksItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
