package com.moratan251.psitweaks.common.spells;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * A spell piece that needs an item the caster carries. The piece names the item once in {@link #getRequiredItem()},
 * and both errors below show that item's name, so every such piece shares the same two lang entries.
 */
public interface RequiredItemPiece {
    /** The caster carries no such item. Argument: the item's name. */
    String ERROR_REQUIRED_ITEM_MISSING = "psitweaks.spellerror.required_item_missing";
    /** The item is carried but holds nothing the piece can use, such as an empty pouch. Argument: the item's name. */
    String ERROR_REQUIRED_ITEM_EMPTY = "psitweaks.spellerror.required_item_empty";

    /** The item the piece needs. */
    ItemLike getRequiredItem();

    /** The name the errors show; a translatable component, so each player reads it in their own language. */
    default Component getRequiredItemName() {
        return getRequiredItem().asItem().getDescription();
    }

    default SpellRuntimeException requiredItemMissing() {
        return new SpellRuntimeException(ERROR_REQUIRED_ITEM_MISSING, getRequiredItemName());
    }

    default SpellRuntimeException requiredItemEmpty() {
        return new SpellRuntimeException(ERROR_REQUIRED_ITEM_EMPTY, getRequiredItemName());
    }
}
