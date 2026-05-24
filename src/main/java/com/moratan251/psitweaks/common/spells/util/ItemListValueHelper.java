package com.moratan251.psitweaks.common.spells.util;

import com.moratan251.psitweaks.common.spells.item.BlockInventorySlotSource;
import com.moratan251.psitweaks.common.spells.item.EntityEquipmentSource;
import com.moratan251.psitweaks.common.spells.item.EntityHandSource;
import com.moratan251.psitweaks.common.spells.item.EntityInventorySlotSource;
import com.moratan251.psitweaks.common.spells.item.ItemSourceRef;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

public final class ItemListValueHelper {
    private static final List<EquipmentSlot> ARMOR_SLOTS = List.of(
            EquipmentSlot.FEET,
            EquipmentSlot.LEGS,
            EquipmentSlot.CHEST,
            EquipmentSlot.HEAD
    );

    private ItemListValueHelper() {
    }

    public static SpellItemListWrapper fromEntityContainer(Entity entity, Container container) {
        if (entity == null || container == null) {
            return SpellItemListWrapper.EMPTY;
        }

        List<SpellItemValue> result = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            add(result, container.getItem(i), new EntityInventorySlotSource(entity.getUUID(), entity.getId(), i));
        }
        return SpellItemListWrapper.make(result);
    }

    public static SpellItemListWrapper fromEntityItemHandler(Entity entity, IItemHandler handler) {
        if (entity == null || handler == null) {
            return SpellItemListWrapper.EMPTY;
        }

        List<SpellItemValue> result = new ArrayList<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            add(result, handler.getStackInSlot(i), new EntityInventorySlotSource(entity.getUUID(), entity.getId(), i));
        }
        return SpellItemListWrapper.make(result);
    }

    public static SpellItemListWrapper fromLivingEquipment(LivingEntity living) {
        if (living == null) {
            return SpellItemListWrapper.EMPTY;
        }

        List<SpellItemValue> result = new ArrayList<>();
        add(result, living.getMainHandItem(), new EntityHandSource(living.getUUID(), InteractionHand.MAIN_HAND));
        add(result, living.getOffhandItem(), new EntityHandSource(living.getUUID(), InteractionHand.OFF_HAND));
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            add(result, living.getItemBySlot(slot), new EntityEquipmentSource(living.getUUID(), slot));
        }
        return SpellItemListWrapper.make(result);
    }

    public static SpellItemListWrapper fromBlockContainer(Level level, BlockPos pos, Container container) {
        if (level == null || pos == null || container == null) {
            return SpellItemListWrapper.EMPTY;
        }

        List<SpellItemValue> result = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            add(result, container.getItem(i), new BlockInventorySlotSource(level.dimension(), pos.immutable(), i));
        }
        return SpellItemListWrapper.make(result);
    }

    public static SpellItemListWrapper fromBlockItemHandler(Level level, BlockPos pos, IItemHandler handler) {
        if (level == null || pos == null || handler == null) {
            return SpellItemListWrapper.EMPTY;
        }

        List<SpellItemValue> result = new ArrayList<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            add(result, handler.getStackInSlot(i), new BlockInventorySlotSource(level.dimension(), pos.immutable(), i));
        }
        return SpellItemListWrapper.make(result);
    }

    private static void add(List<SpellItemValue> values, ItemStack stack, ItemSourceRef source) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        values.add(SpellItemValue.sourced(stack, source));
    }
}
