package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.SpellItemFilters;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.item.StrictItemFilter;
import com.moratan251.psitweaks.common.spells.param.ParamSpellItemFilter;
import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

/**
 * Makes item filter inputs compare data components too. An Item gives one template, an Item List one per non-empty
 * element; an empty Item is an error, as it is for the filter inputs themselves.
 */
public class PieceOperatorStrictItem extends PieceOperator {
    private SpellParam<SpellItemValue> item;

    public PieceOperatorStrictItem(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(item = new ParamSpellItemFilter(PsitweaksSpellParams.ITEM, PsitweaksSpellParams.ITEM_COLOR, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        // Read untyped: the input also takes an Item List or another strict Item.
        Object value = getParamValue(context, (SpellParam<?>) item);
        if (value instanceof StrictItemFilter filter) {
            return filter;
        }
        if (value instanceof SpellItemListWrapper list) {
            List<ItemStack> templates = new ArrayList<>(list.size());
            for (SpellItemValue element : list) {
                if (!element.isEmpty()) {
                    templates.add(element.snapshot());
                }
            }
            return new StrictItemFilter(templates);
        }
        if (value instanceof SpellItemValue single && !single.isEmpty()) {
            return new StrictItemFilter(List.of(single.snapshot()));
        }
        throw new SpellRuntimeException(SpellItemFilters.ERROR_NULL_ITEM);
    }

    @Override
    public Class<?> getEvaluationType() {
        return StrictItemFilter.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.item_strict");
    }
}
