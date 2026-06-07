package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.BlockInventorySlotSource;
import com.moratan251.psitweaks.common.spells.item.EntityInventorySlotSource;
import com.moratan251.psitweaks.common.spells.item.ItemSourceRef;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.param.ParamSpellItemValue;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorItemSlot extends PieceOperator {
    private static final double MISSING_SLOT = -1.0D;

    private SpellParam<SpellItemValue> item;

    public PieceOperatorItemSlot(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(item = new ParamSpellItemValue(PsitweaksSpellParams.ITEM, PsitweaksSpellParams.ITEM_COLOR,
                false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        SpellItemValue value = getNotNullParamValue(context, item);
        if (value.isEmpty()) {
            return MISSING_SLOT;
        }
        return value.source()
                .map(PieceOperatorItemSlot::slotOf)
                .orElse(MISSING_SLOT);
    }

    private static double slotOf(ItemSourceRef source) {
        if (source instanceof EntityInventorySlotSource entitySlot) {
            return entitySlot.slot();
        }
        if (source instanceof BlockInventorySlotSource blockSlot) {
            return blockSlot.slot();
        }
        return MISSING_SLOT;
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psi.datatype.number");
    }
}
