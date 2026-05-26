package com.moratan251.psitweaks.common.spells.param;

import com.moratan251.psitweaks.common.spells.wrapper.BlockListWrapper;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.param.ParamSpecific;

public class ParamBlockListWrapper extends ParamSpecific<BlockListWrapper> {
    public ParamBlockListWrapper(String name, int color, boolean canDisable, boolean constant) {
        super(name, color, canDisable, constant);
    }

    @Override
    public Class<BlockListWrapper> getRequiredType() {
        return BlockListWrapper.class;
    }

    @Override
    public Component getRequiredTypeString() {
        return Component.translatable("psitweaks.datatype.block_list");
    }
}
