package com.moratan251.psitweaks.common.items.armor;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PsitweaksArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, Psitweaks.MOD_ID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> MOVAL_SUIT = ARMOR_MATERIALS.register(
            "moval_suit",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), defense -> {
                        defense.put(ArmorItem.Type.BOOTS, 3);
                        defense.put(ArmorItem.Type.LEGGINGS, 6);
                        defense.put(ArmorItem.Type.CHESTPLATE, 8);
                        defense.put(ArmorItem.Type.HELMET, 3);
                        defense.put(ArmorItem.Type.BODY, 8);
                    }),
                    60,
                    SoundEvents.ARMOR_EQUIP_IRON,
                    () -> Ingredient.of(PsitweaksItems.ALLOY_PSION.get()),
                    List.of(new ArmorMaterial.Layer(Psitweaks.location("moval_suit"))),
                    3.0F,
                    0.1F
            )
    );

    private PsitweaksArmorMaterials() {
    }

    public static void register(IEventBus eventBus) {
        ARMOR_MATERIALS.register(eventBus);
    }
}
