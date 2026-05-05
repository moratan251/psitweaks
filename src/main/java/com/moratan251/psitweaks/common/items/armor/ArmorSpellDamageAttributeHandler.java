package com.moratan251.psitweaks.common.items.armor;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.attributes.PsitweaksAttributes;
import com.moratan251.psitweaks.common.registries.PsitweaksModules;
import java.util.EnumMap;
import java.util.Map;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.common.registration.impl.ModuleRegistryObject;
import mekanism.common.registries.MekanismItems;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import vazkii.psi.common.attribute.base.ModAttributes;
import vazkii.psi.common.item.tool.IPsimetalTool;

public final class ArmorSpellDamageAttributeHandler {
    private static final double MOVAL_SUIT_SPELL_DAMAGE_BONUS = 0.10D;
    private static final double MOVAL_SUIT_PSI_REGEN_BONUS = 5.0D;
    private static final double MOVAL_SUIT_MAX_PSI_BONUS = 500.0D;
    private static final double MEKASUIT_SPELL_DAMAGE_BONUS = 0.15D;
    private static final double MEKASUIT_PSI_REGEN_BONUS = 5.0D;
    private static final double MEKASUIT_MAX_PSI_BONUS = 1_000.0D;

    private static final Map<EquipmentSlot, ResourceLocation> SPELL_DAMAGE_IDS = modifierIds("moval_suit_spell_damage");
    private static final Map<EquipmentSlot, ResourceLocation> PSI_REGEN_IDS = modifierIds("moval_suit_psi_regen");
    private static final Map<EquipmentSlot, ResourceLocation> MAX_PSI_IDS = modifierIds("moval_suit_max_psi");
    private static final ResourceLocation MEKASUIT_SPELL_DAMAGE_ID = Psitweaks.location("mekasuit_spell_damage");
    private static final ResourceLocation MEKASUIT_PSI_REGEN_ID = Psitweaks.location("mekasuit_psi_regen");
    private static final ResourceLocation MEKASUIT_MAX_PSI_ID = Psitweaks.location("mekasuit_max_psi");

    private ArmorSpellDamageAttributeHandler() {
    }

    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        if (event.getItemStack().getItem() instanceof ItemMovalSuitArmor armor) {
            addMovalSuitModifiers(event, armor);
            return;
        }

        addMekaSuitModifiers(event);
    }

    private static void addMovalSuitModifiers(ItemAttributeModifierEvent event, ItemMovalSuitArmor armor) {
        if (!IPsimetalTool.isEnabled(event.getItemStack())) {
            return;
        }

        EquipmentSlot slot = armor.getEquipmentSlot();
        EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(slot);

        addModifier(event, PsitweaksAttributes.SPELL_DAMAGE_FACTOR, SPELL_DAMAGE_IDS.get(slot), MOVAL_SUIT_SPELL_DAMAGE_BONUS, group);
        addModifier(event, ModAttributes.REGEN, PSI_REGEN_IDS.get(slot), MOVAL_SUIT_PSI_REGEN_BONUS, group);
        addModifier(event, ModAttributes.TOTAL_PSI, MAX_PSI_IDS.get(slot), MOVAL_SUIT_MAX_PSI_BONUS, group);
    }

    private static void addMekaSuitModifiers(ItemAttributeModifierEvent event) {
        if (event.getItemStack().is(MekanismItems.MEKASUIT_HELMET.get())) {
            addMekaSuitHelmetModifiers(event);
        } else if (event.getItemStack().is(MekanismItems.MEKASUIT_BODYARMOR.get())) {
            addMekaSuitBodyarmorModifiers(event);
        }
    }

    private static void addMekaSuitHelmetModifiers(ItemAttributeModifierEvent event) {
        int installedCount = getInstalledModuleCount(event, PsitweaksModules.PHENOMENON_INTERFERENCE_ENHANCEMENT_UNIT);
        if (installedCount > 0) {
            addModifier(event, PsitweaksAttributes.SPELL_DAMAGE_FACTOR, MEKASUIT_SPELL_DAMAGE_ID,
                    installedCount * MEKASUIT_SPELL_DAMAGE_BONUS, EquipmentSlotGroup.HEAD);
        }
    }

    private static void addMekaSuitBodyarmorModifiers(ItemAttributeModifierEvent event) {
        int regenCount = getInstalledModuleCount(event, PsitweaksModules.PSYON_SUPPLYING_UNIT);
        if (regenCount > 0) {
            addModifier(event, ModAttributes.REGEN, MEKASUIT_PSI_REGEN_ID,
                    regenCount * MEKASUIT_PSI_REGEN_BONUS, EquipmentSlotGroup.CHEST);
        }

        int maxPsiCount = getInstalledModuleCount(event, PsitweaksModules.PSYON_CAPACITY_UNIT);
        if (maxPsiCount > 0) {
            addModifier(event, ModAttributes.TOTAL_PSI, MEKASUIT_MAX_PSI_ID,
                    maxPsiCount * MEKASUIT_MAX_PSI_BONUS, EquipmentSlotGroup.CHEST);
        }
    }

    private static <MODULE extends ICustomModule<MODULE>> int getInstalledModuleCount(
            ItemAttributeModifierEvent event,
            ModuleRegistryObject<MODULE> moduleType
    ) {
        IModule<MODULE> module = IModuleHelper.INSTANCE.getModule(event.getItemStack(), moduleType);
        return module == null || !module.isEnabled() ? 0 : module.getInstalledCount();
    }

    private static void addModifier(ItemAttributeModifierEvent event, Holder<Attribute> attribute,
                                    ResourceLocation id, double amount, EquipmentSlotGroup group) {
        event.addModifier(attribute, new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE), group);
    }

    private static Map<EquipmentSlot, ResourceLocation> modifierIds(String prefix) {
        EnumMap<EquipmentSlot, ResourceLocation> ids = new EnumMap<>(EquipmentSlot.class);
        ids.put(EquipmentSlot.HEAD, Psitweaks.location(prefix + "_head"));
        ids.put(EquipmentSlot.CHEST, Psitweaks.location(prefix + "_chest"));
        ids.put(EquipmentSlot.LEGS, Psitweaks.location(prefix + "_legs"));
        ids.put(EquipmentSlot.FEET, Psitweaks.location(prefix + "_feet"));
        return ids;
    }
}
