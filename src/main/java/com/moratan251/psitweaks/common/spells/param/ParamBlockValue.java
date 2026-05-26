package com.moratan251.psitweaks.common.spells.param;

import com.moratan251.psitweaks.api.value.BlockValue;
import com.moratan251.psitweaks.api.value.BlockValueHelper;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.param.ParamSpecific;

public class ParamBlockValue extends ParamSpecific<BlockValue> {
    public ParamBlockValue(String name, int color, boolean canDisable, boolean constant) {
        super(name, color, canDisable, constant);
    }

    @Override
    public Class<BlockValue> getRequiredType() {
        return BlockValue.class;
    }

    @Override
    public Component getRequiredTypeString() {
        return Component.translatable("psitweaks.datatype.block");
    }

    @Override
    public boolean canAccept(SpellPiece piece) {
        return BlockValueHelper.canAcceptBlockInput(piece, requiresConstant());
    }
}
