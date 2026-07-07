package com.moratan251.psitweaks.common.spells.param;

import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.param.ParamSpecific;

public class ParamSpellItemListWrapper extends ParamSpecific<SpellItemListWrapper> {
    public ParamSpellItemListWrapper(String name, int color, boolean canDisable, boolean constant) {
        super(name, color, canDisable, constant);
    }

    @Override
    public Class<SpellItemListWrapper> getRequiredType() {
        return SpellItemListWrapper.class;
    }

    @Override
    public Component getRequiredTypeString() {
        return Component.translatable("psitweaks.datatype.item_list");
    }
}
