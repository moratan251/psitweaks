package com.moratan251.psitweaks.common.spells.param;

import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.param.ParamSpecific;

public class ParamStringListWrapper extends ParamSpecific<StringListWrapper> {
    public ParamStringListWrapper(String name, int color, boolean canDisable, boolean constant) {
        super(name, color, canDisable, constant);
    }

    @Override
    public Class<StringListWrapper> getRequiredType() {
        return StringListWrapper.class;
    }

    @Override
    public Component getRequiredTypeString() {
        return Component.translatable("psitweaks.datatype.string_list");
    }
}
