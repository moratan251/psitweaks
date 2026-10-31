package com.moratan251.psitweaks.common.items;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.caster.AutonomousCasterUpgrade;
import com.moratan251.psitweaks.common.registries.PsitweaksModules;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import mekanism.api.gear.ModuleData;
import mekanism.common.item.ItemModule;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PsitweaksMekanismItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Psitweaks.MOD_ID);

    public static final DeferredItem<ItemModule> MODULE_PSYON_SUPPLYING = registerModule(
            "module_psyon_supplying_unit",
            () -> PsitweaksModules.PSYON_SUPPLYING_UNIT
    );
    public static final DeferredItem<ItemModule> MODULE_PSYON_CAPACITY = registerModule(
            "module_psyon_capacity_unit",
            () -> PsitweaksModules.PSYON_CAPACITY_UNIT
    );
    public static final DeferredItem<ItemModule> MODULE_PHENOMENON_INTERFERENCE_ENHANCEMENT = registerModule(
            "module_phenomenon_interference_enhancement_unit",
            () -> PsitweaksModules.PHENOMENON_INTERFERENCE_ENHANCEMENT_UNIT
    );
    /** 自律詠唱機のアップグレード。種類ごとに1アイテム。 */
    public static final Map<AutonomousCasterUpgrade, DeferredItem<ItemAutonomousCasterUpgrade>> AUTONOMOUS_CASTER_UPGRADES =
            registerAutonomousCasterUpgrades();

    private PsitweaksMekanismItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static DeferredItem<ItemAutonomousCasterUpgrade> autonomousCasterUpgrade(AutonomousCasterUpgrade upgrade) {
        return AUTONOMOUS_CASTER_UPGRADES.get(upgrade);
    }

    private static Map<AutonomousCasterUpgrade, DeferredItem<ItemAutonomousCasterUpgrade>> registerAutonomousCasterUpgrades() {
        Map<AutonomousCasterUpgrade, DeferredItem<ItemAutonomousCasterUpgrade>> items = new EnumMap<>(AutonomousCasterUpgrade.class);
        for (AutonomousCasterUpgrade upgrade : AutonomousCasterUpgrade.values()) {
            items.put(upgrade, ITEMS.register(upgrade.itemId(),
                    () -> new ItemAutonomousCasterUpgrade(upgrade, new Item.Properties().rarity(Rarity.UNCOMMON))));
        }
        return Collections.unmodifiableMap(items);
    }

    private static DeferredItem<ItemModule> registerModule(String id, Supplier<Holder<ModuleData<?>>> module) {
        return ITEMS.register(id, () -> new ItemModule(module, new Item.Properties().rarity(Rarity.RARE)));
    }
}
