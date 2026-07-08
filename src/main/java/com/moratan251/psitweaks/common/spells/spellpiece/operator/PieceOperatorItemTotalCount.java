package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.param.ParamSpellItemListWrapper;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorItemTotalCount extends PsitweaksPieceOperator {
    private SpellParam<SpellItemListWrapper> list;
    private SpellParam<String> itemId;

    public PieceOperatorItemTotalCount(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(list = new ParamSpellItemListWrapper(SpellParam.GENERIC_NAME_LIST,
                PsitweaksSpellParams.ITEM_LIST_COLOR, false, false));
        addParam(itemId = new ParamString(PsitweaksSpellParams.STRING, PsitweaksSpellParams.STRING_COLOR,
                true, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        SpellItemListWrapper values = getNotNullParamValue(context, list);
        String targetId = getParamValueOrDefault(context, itemId, "");
        if (targetId == null) {
            targetId = "";
        }

        double total = 0.0D;
        for (SpellItemValue value : values) {
            if (value == null || value.isEmpty()) {
                continue;
            }

            ItemStack stack = value.copyStack();
            if (targetId.isEmpty() || targetId.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) {
                total += stack.getCount();
            }
        }
        return total;
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
