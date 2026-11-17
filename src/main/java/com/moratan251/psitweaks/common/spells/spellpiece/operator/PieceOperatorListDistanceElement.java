package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.util.RankedSelection;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * The element at index n when the List is sorted closest first. Index 0 is the closest element and negative indexes
 * count from the farthest (-1 is the farthest). An index outside the List is an error, as for Selector: Indexed Element.
 */
public class PieceOperatorListDistanceElement extends PieceOperatorListDistanceBase {
    public PieceOperatorListDistanceElement(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        double value = numberValue(context);
        return RankedSelection.element(sortedByDistance(context), value);
    }

    @Override
    public Class<?> getEvaluationType() {
        return elementType();
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable(getModeOption().elementTranslationKey());
    }
}
