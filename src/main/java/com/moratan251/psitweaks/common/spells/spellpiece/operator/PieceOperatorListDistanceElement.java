package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import java.util.List;
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
        List<?> sorted = sortedByDistance(context);
        int size = sorted.size();
        long index = Double.isNaN(value) ? 0L : (long) value;
        long normalized = index < 0 ? size + index : index;
        if (normalized < 0 || normalized >= size) {
            throw new SpellRuntimeException("psi.spellerror.out_of_bounds");
        }
        return sorted.get((int) normalized);
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
