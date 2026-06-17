package com.moratan251.psitweaks.common.spells.param;

import net.minecraft.network.chat.Component;

public class ParamConstantString extends ParamString {
    public ParamConstantString(String name, int color, boolean canDisable) {
        super(name, color, canDisable, true);
    }

    @Override
    public Component getRequiredTypeString() {
        return super.getRequiredTypeString().copy()
                .append(" ")
                .append(Component.translatable("psimisc.constant"));
    }
}
