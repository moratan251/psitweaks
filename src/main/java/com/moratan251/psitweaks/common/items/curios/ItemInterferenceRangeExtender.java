package com.moratan251.psitweaks.common.items.curios;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class ItemInterferenceRangeExtender extends Item implements ICurioItem {
    public ItemInterferenceRangeExtender(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return CuriosSlotChecker.MAGIC_CALCULATION_AREA.equals(slotContext.identifier());
    }
}

