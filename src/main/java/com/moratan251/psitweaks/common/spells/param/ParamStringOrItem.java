package com.moratan251.psitweaks.common.spells.param;

import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.EnumPieceType;
import vazkii.psi.api.spell.SpellPiece;

/** Resource input of the ideaspace selectors in Item mode: an item ID String or an Item value, read untyped. */
public class ParamStringOrItem extends ParamSpellItemValue {
    public ParamStringOrItem(String name, int color, boolean canDisable, boolean constant) {
        super(name, color, canDisable, constant);
    }

    @Override
    public Component getRequiredTypeString() {
        return Component.translatable("psitweaks.datatype.string_or_item");
    }

    @Override
    public boolean canAccept(SpellPiece piece) {
        if (super.canAccept(piece)) {
            return true;
        }
        if (requiresConstant() && piece.getPieceType() != EnumPieceType.CONSTANT) {
            return false;
        }
        Class<?> evaluationType = piece.getEvaluationType();
        return evaluationType != null && String.class.isAssignableFrom(evaluationType);
    }
}
