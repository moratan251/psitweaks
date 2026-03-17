package com.moratan251.psitweaks.common.items.curios;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.moratan251.psitweaks.common.attributes.PsitweaksAttributes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.UUID;

public class ItemSorceryBooster extends Item implements ICurioItem {

    private static final String MAGIC_CALCULATION_AREA_SLOT = "magic_calculation_area";
    private static final String ATTRIBUTE_NAME = "psitweaks.sorcery_booster.spell_damage_factor";
    private static final double SPELL_DAMAGE_BONUS = 0.3D;

    public ItemSorceryBooster(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return MAGIC_CALCULATION_AREA_SLOT.equals(slotContext.identifier());
    }

    @Override
    public @NotNull Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();

        if (MAGIC_CALCULATION_AREA_SLOT.equals(slotContext.identifier())) {
            modifiers.put(
                    PsitweaksAttributes.SPELL_DAMAGE_FACTOR.get(),
                    new AttributeModifier(uuid, ATTRIBUTE_NAME, SPELL_DAMAGE_BONUS, AttributeModifier.Operation.ADDITION)
            );
        }

        return modifiers;
    }
}
