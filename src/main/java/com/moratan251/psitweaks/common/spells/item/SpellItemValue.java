package com.moratan251.psitweaks.common.spells.item;

import com.moratan251.psitweaks.api.value.ContextualValue;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.spell.SpellContext;

public record SpellItemValue(ItemStack snapshot, Optional<ItemSourceRef> source) implements ContextualValue {
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

    /**
     * Compares Item values using the spell-level contract shared by operators and list operations.
     */
    public static boolean equivalent(SpellItemValue left, SpellItemValue right) {
        if (left == right) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        if (left.isEmpty() || right.isEmpty()) {
            return left.isEmpty() && right.isEmpty();
        }
        return left.source().equals(right.source())
                && ItemStack.matches(left.snapshot, right.snapshot);
    }

    @Override
    public Optional<CompoundTag> getNbt(SpellContext context) {
        if (context == null || context.focalPoint == null || snapshot.isEmpty()) {
            return Optional.empty();
        }

        CompoundTag compound = snapshot.save(new CompoundTag());
        return compound.isEmpty() ? Optional.empty() : Optional.of(compound);
    }

    @Override
    public Set<ResourceLocation> getTagIds(SpellContext context) {
        if (snapshot.isEmpty()) {
            return Set.of();
        }

        return snapshot.getItem().builtInRegistryHolder().tags()
                .map(TagKey::location)
                .collect(Collectors.toUnmodifiableSet());
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
        result.append(", customName=").append(snapshot.hasCustomHoverName());
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
