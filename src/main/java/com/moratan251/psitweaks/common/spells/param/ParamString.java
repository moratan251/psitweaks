package com.moratan251.psitweaks.common.spells.param;

import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.param.ParamSpecific;

public class ParamString extends ParamSpecific<String> {
    public ParamString(String name, int color, boolean canDisable, boolean constant) {
        super(name, color, canDisable, constant);
    }

    @Override
    public Class<String> getRequiredType() {
        return String.class;
    }

    @Override
    public Component getRequiredTypeString() {
        return Component.translatable("psitweaks.datatype.string");
    }
}
