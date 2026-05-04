package com.moratan251.psitweaks.common.items.armor;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vazkii.psi.api.exosuit.IExosuitSensor;
import vazkii.psi.api.exosuit.ISensorHoldable;
import vazkii.psi.common.item.base.ModDataComponents;

public class ItemMovalSuitHelmet extends ItemMovalSuitArmor implements ISensorHoldable {
    public ItemMovalSuitHelmet(ArmorItem.Type type, Item.Properties properties) {
        super(type, properties);
    }

    @Override
    public String getEvent(ItemStack stack) {
        ItemStack sensor = getAttachedSensor(stack);
        if (!sensor.isEmpty() && sensor.getItem() instanceof IExosuitSensor exosuitSensor) {
            return exosuitSensor.getEventType(sensor);
        }
        return super.getEvent(stack);
    }

    @Override
    public int getCastCooldown(ItemStack stack) {
        return 40;
    }

    @Override
    public int getColor(ItemStack stack) {
        ItemStack sensor = getAttachedSensor(stack);
        if (!sensor.isEmpty() && sensor.getItem() instanceof IExosuitSensor exosuitSensor) {
            return exosuitSensor.getColor(sensor);
        }
        return super.getColor(stack);
    }

    @Override
    public ItemStack getAttachedSensor(ItemStack stack) {
        return new ItemStack(stack.getOrDefault(ModDataComponents.SENSOR, Items.AIR));
    }

    @Override
    public void attachSensor(ItemStack stack, ItemStack sensor) {
        stack.set(ModDataComponents.SENSOR, sensor.getItem());
    }
}
