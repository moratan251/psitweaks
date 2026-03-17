package com.moratan251.psitweaks.common.items.armor;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.attributes.PsitweaksAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Psitweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArmorSpellDamageAttributeHandler {
    private static final String SPELL_DAMAGE_ATTRIBUTE_NAME = "psitweaks.armor_spell_damage";
    private static final String PSI_REGEN_ATTRIBUTE_NAME = "psitweaks.armor.psi_regen_bonus";
    private static final String MAX_PSI_ATTRIBUTE_NAME = "psitweaks.armor.max_psi_bonus";
    private static final double PSI_EXOSUIT_BONUS = 0.05D;
    private static final double MOVAL_SUIT_BONUS = 0.10D;
    private static final double PSI_EXOSUIT_PSI_REGEN_BONUS = 2.0D;
    private static final double PSI_EXOSUIT_MAX_PSI_BONUS = 250.0D;
    private static final double MOVAL_SUIT_PSI_REGEN_BONUS = 5.0D;
    private static final double MOVAL_SUIT_MAX_PSI_BONUS = 500.0D;
    private static final Map<EquipmentSlot, UUID> SPELL_DAMAGE_MODIFIER_IDS = Map.of(
            EquipmentSlot.HEAD, UUID.fromString("7fd7092d-74f3-49ae-a8f2-2d93ffca7d55"),
            EquipmentSlot.CHEST, UUID.fromString("9f76d7ff-82df-4b89-b60e-a455968f7db3"),
            EquipmentSlot.LEGS, UUID.fromString("9dd06bda-a20d-4312-a820-c6ce5dcacbc4"),
            EquipmentSlot.FEET, UUID.fromString("f4f68031-d65c-4550-b666-9adb9ee35a44")
    );
    private static final Map<EquipmentSlot, UUID> PSI_REGEN_MODIFIER_IDS = Map.of(
            EquipmentSlot.HEAD, UUID.fromString("67bded6e-2456-4624-a98b-19ca5027752f"),
            EquipmentSlot.CHEST, UUID.fromString("ec7d618f-bafe-4288-b45d-99a9cdb22f67"),
            EquipmentSlot.LEGS, UUID.fromString("ff6e14c7-f09b-48b8-bd79-dcd4b3c323ca"),
            EquipmentSlot.FEET, UUID.fromString("3c43fce8-1a76-49fc-83db-ab350d7741e1")
    );
    private static final Map<EquipmentSlot, UUID> MAX_PSI_MODIFIER_IDS = Map.of(
            EquipmentSlot.HEAD, UUID.fromString("5b7e1434-f6c6-40ba-8387-4cdca6654b25"),
            EquipmentSlot.CHEST, UUID.fromString("744359ab-56fd-478a-a6fe-f9f1d44eb303"),
            EquipmentSlot.LEGS, UUID.fromString("ed193b80-b4b3-4f4e-8244-64d1101fd729"),
            EquipmentSlot.FEET, UUID.fromString("787e5e32-cb7d-412e-a6a2-7af65870477e")
    );

    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof ArmorItem armorItem)) {
            return;
        }

        EquipmentSlot slot = event.getSlotType();
        if (slot != armorItem.getType().getSlot()) {
            return;
        }

        boolean isMovalSuit = stack.getItem() instanceof ItemMovalSuitArmor;
        boolean isPsiExosuit = isPsiExosuitArmor(stack);

        addSpellDamageModifier(event, slot, getSpellDamageBonus(isMovalSuit, isPsiExosuit));
        addPsiRegenModifier(event, slot, getPsiRegenBonus(isMovalSuit, isPsiExosuit));
        addMaxPsiModifier(event, slot, getMaxPsiBonus(isMovalSuit, isPsiExosuit));
    }

    private static void addSpellDamageModifier(ItemAttributeModifierEvent event, EquipmentSlot slot, double bonus) {
        if (bonus <= 0.0D) {
            return;
        }

        UUID modifierId = SPELL_DAMAGE_MODIFIER_IDS.get(slot);
        if (modifierId == null) {
            return;
        }

        event.addModifier(
                PsitweaksAttributes.SPELL_DAMAGE_FACTOR.get(),
                new AttributeModifier(modifierId, SPELL_DAMAGE_ATTRIBUTE_NAME, bonus, AttributeModifier.Operation.ADDITION)
        );
    }

    private static void addPsiRegenModifier(ItemAttributeModifierEvent event, EquipmentSlot slot, double bonus) {
        if (bonus == 0.0D) {
            return;
        }

        UUID modifierId = PSI_REGEN_MODIFIER_IDS.get(slot);
        if (modifierId == null) {
            return;
        }

        event.addModifier(
                PsitweaksAttributes.PSI_REGEN_BONUS.get(),
                new AttributeModifier(modifierId, PSI_REGEN_ATTRIBUTE_NAME, bonus, AttributeModifier.Operation.ADDITION)
        );
    }

    private static void addMaxPsiModifier(ItemAttributeModifierEvent event, EquipmentSlot slot, double bonus) {
        if (bonus == 0.0D) {
            return;
        }

        UUID modifierId = MAX_PSI_MODIFIER_IDS.get(slot);
        if (modifierId == null) {
            return;
        }

        event.addModifier(
                PsitweaksAttributes.MAX_PSI_BONUS.get(),
                new AttributeModifier(modifierId, MAX_PSI_ATTRIBUTE_NAME, bonus, AttributeModifier.Operation.ADDITION)
        );
    }

    private static double getSpellDamageBonus(boolean isMovalSuit, boolean isPsiExosuit) {
        if (isMovalSuit) {
            return MOVAL_SUIT_BONUS;
        }

        if (isPsiExosuit) {
            return PSI_EXOSUIT_BONUS;
        }

        return 0.0D;
    }

    private static double getPsiRegenBonus(boolean isMovalSuit, boolean isPsiExosuit) {
        if (isMovalSuit) {
            return MOVAL_SUIT_PSI_REGEN_BONUS;
        }

        if (isPsiExosuit) {
            return PSI_EXOSUIT_PSI_REGEN_BONUS;
        }

        return 0.0D;
    }

    private static double getMaxPsiBonus(boolean isMovalSuit, boolean isPsiExosuit) {
        if (isMovalSuit) {
            return MOVAL_SUIT_MAX_PSI_BONUS;
        }

        if (isPsiExosuit) {
            return PSI_EXOSUIT_MAX_PSI_BONUS;
        }

        return 0.0D;
    }

    private static boolean isPsiExosuitArmor(ItemStack stack) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return itemId != null && itemId.getNamespace().equals("psi") && itemId.getPath().startsWith("psimetal_exosuit_");
    }
}
