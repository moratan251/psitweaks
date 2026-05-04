package com.moratan251.psitweaks.common.items.curios;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.moratan251.psitweaks.common.attributes.PsitweaksAttributes;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class ItemSorceryBooster extends Item implements ICurioItem {
    private static final double SPELL_DAMAGE_BONUS = 0.3D;

    public ItemSorceryBooster(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return CuriosSlotChecker.MAGIC_CALCULATION_AREA.equals(slotContext.identifier());
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext,
                                                                                ResourceLocation id,
                                                                                ItemStack stack) {
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = HashMultimap.create();
        if (CuriosSlotChecker.MAGIC_CALCULATION_AREA.equals(slotContext.identifier())) {
            modifiers.put(
                    PsitweaksAttributes.SPELL_DAMAGE_FACTOR,
                    new AttributeModifier(id, SPELL_DAMAGE_BONUS, AttributeModifier.Operation.ADD_VALUE)
            );
        }
        return modifiers;
    }
}
