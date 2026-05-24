package com.moratan251.psitweaks.common.spells.item;

import java.util.Locale;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

public record SpellItemValue(ItemStack snapshot, Optional<ItemSourceRef> source) {
    public static final SpellItemValue EMPTY = new SpellItemValue(ItemStack.EMPTY, Optional.empty());

    public SpellItemValue {
        snapshot = snapshot == null ? ItemStack.EMPTY : snapshot.copy();
        source = source == null ? Optional.empty() : source;
    }

    public static SpellItemValue snapshot(ItemStack stack) {
        return new SpellItemValue(stack, Optional.empty());
    }

    public static SpellItemValue sourced(ItemStack stack, ItemSourceRef source) {
        return new SpellItemValue(stack, Optional.ofNullable(source));
    }

    @Override
    public ItemStack snapshot() {
        return snapshot.copy();
    }

    public ItemStack copyStack() {
        return snapshot.copy();
    }

    public boolean isEmpty() {
        return snapshot.isEmpty();
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "Item[empty]";
        }

        StringBuilder result = new StringBuilder("Item[name=\"");
        result.append(snapshot.getHoverName().getString()).append('"');
        result.append(", count=").append(snapshot.getCount());
        if (snapshot.isDamageableItem()) {
            result.append(", damage=").append(snapshot.getDamageValue()).append('/').append(snapshot.getMaxDamage());
        }
        result.append(", source=").append(source.map(SpellItemValue::describeSource).orElse("none"));
        result.append(", customName=").append(snapshot.has(DataComponents.CUSTOM_NAME));
        result.append(']');
        return result.toString();
    }

    private static String describeSource(ItemSourceRef source) {
        if (source instanceof EntityHandSource handSource) {
            return handSource.hand().name().toLowerCase(Locale.ROOT);
        }
        if (source instanceof EntityEquipmentSource equipmentSource) {
            return "equipment:" + equipmentSource.slot().name().toLowerCase(Locale.ROOT);
        }
        if (source instanceof EntityInventorySlotSource inventorySlotSource) {
            return "entity_inventory:" + inventorySlotSource.slot();
        }
        if (source instanceof ItemEntitySource) {
            return "item_entity";
        }
        if (source instanceof BlockInventorySlotSource) {
            return "block_inventory";
        }
        return "unknown";
    }
}
