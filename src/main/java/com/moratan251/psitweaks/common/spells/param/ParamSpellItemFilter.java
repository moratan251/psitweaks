package com.moratan251.psitweaks.common.spells.param;

import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.EnumPieceType;
import vazkii.psi.api.spell.SpellPiece;

/**
 * Item filter input of the transfer tricks. Besides a single Item it accepts an Item List, which matches an item equal
 * to any of its elements. Read it with {@link com.moratan251.psitweaks.common.spells.item.SpellItemFilters}.
 */
public class ParamSpellItemFilter extends ParamSpellItemValue {
    public ParamSpellItemFilter(String name, int color, boolean canDisable, boolean constant) {
        super(name, color, canDisable, constant);
    }

    @Override
    public Component getRequiredTypeString() {
        return Component.translatable("psitweaks.datatype.item_or_item_list");
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
        return evaluationType != null && SpellItemListWrapper.class.isAssignableFrom(evaluationType);
    }
}
