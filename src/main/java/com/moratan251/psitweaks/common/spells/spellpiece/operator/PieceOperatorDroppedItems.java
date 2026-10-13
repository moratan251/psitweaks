package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.util.ItemListValueHelper;
import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamEntityListWrapper;
import vazkii.psi.api.spell.piece.PieceOperator;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

/**
 * Turns the dropped item entities of an Entity List (such as Psi's Selector: Nearby Items) into an Item List. Every
 * value keeps its item entity as the source, so the ideaspace collect trick can take that very stack.
 */
public class PieceOperatorDroppedItems extends PieceOperator {
    private SpellParam<EntityListWrapper> list;

    public PieceOperatorDroppedItems(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(list = new ParamEntityListWrapper(SpellParam.GENERIC_NAME_LIST, SpellParam.BLUE, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        return ItemListValueHelper.fromItemEntities(getNotNullParamValue(context, list));
    }

    @Override
    public Class<?> getEvaluationType() {
        return SpellItemListWrapper.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.item_list");
    }
}
