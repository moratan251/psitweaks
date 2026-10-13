package com.moratan251.psitweaks.common.spells.param;

import com.moratan251.psitweaks.common.spells.wrapper.BlockListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.EnumPieceType;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.param.ParamSpecific;

public class ParamVectorListWrapper extends ParamSpecific<VectorListWrapper> {
    public ParamVectorListWrapper(String name, int color, boolean canDisable, boolean constant) {
        super(name, color, canDisable, constant);
    }

    @Override
    public Class<VectorListWrapper> getRequiredType() {
        return VectorListWrapper.class;
    }

    @Override
    public Component getRequiredTypeString() {
        return Component.translatable("psitweaks.datatype.vector_list");
    }

    /** A Block List is accepted too and read as its block positions, like a single Block on a Vector param. */
    @Override
    public boolean canAccept(SpellPiece piece) {
        if (super.canAccept(piece)) {
            return true;
        }
        if (requiresConstant() && piece.getPieceType() != EnumPieceType.CONSTANT) {
            return false;
        }
        Class<?> evaluationType = piece.getEvaluationType();
        return evaluationType != null && BlockListWrapper.class.isAssignableFrom(evaluationType);
    }
}
