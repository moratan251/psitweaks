package com.moratan251.psitweaks.common.spells.param;

import net.minecraft.network.chat.Component;

/**
 * Search input of the ideaspace item logistics. The wired value decides how it matches: a String is a wildcard pattern
 * over item IDs, an Item or Item List matches item types, and a strict Item also compares data components. Read it
 * untyped with {@link com.moratan251.psitweaks.common.spells.item.SpellItemFilters#search}.
 */
public class ParamItemSearch extends ParamSpellItemFilter {
    public ParamItemSearch(String name, int color, boolean canDisable, boolean constant) {
        super(name, color, canDisable, constant);
    }

    @Override
    public Component getRequiredTypeString() {
        return Component.translatable("psitweaks.datatype.item_search");
    }

    @Override
    protected boolean acceptsExtra(Class<?> evaluationType) {
        return String.class.isAssignableFrom(evaluationType) || super.acceptsExtra(evaluationType);
    }
}
