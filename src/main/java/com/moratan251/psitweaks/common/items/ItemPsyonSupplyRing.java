package com.moratan251.psitweaks.common.items;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class ItemPsyonSupplyRing extends Item implements ICurioItem {
    public ItemPsyonSupplyRing(Properties props) {
        super(props);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            if (!player.level().isClientSide) {
                // サーバー側ロジック（例：効果を与える etc.）
            }
        }
    }


    public void onEquip(SlotContext slotContext, ItemStack stack) { }


    public void onUnequip(SlotContext slotContext, ItemStack stack) { }
}