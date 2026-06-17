package com.moratan251.psitweaks.common.spells.param;

import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.param.ParamSpecific;

public class ParamSpellItemValue extends ParamSpecific<SpellItemValue> {
    public ParamSpellItemValue(String name, int color, boolean canDisable, boolean constant) {
        super(name, color, canDisable, constant);
    }

    @Override
    public Class<SpellItemValue> getRequiredType() {
        return SpellItemValue.class;
    }

    @Override
    public Component getRequiredTypeString() {
        return Component.translatable("psitweaks.datatype.item");
    }
}
