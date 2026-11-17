package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * Keeps the n elements closest to the position, or the -n farthest for a negative n. Vector and Block Lists come out
 * closest first (farthest first for a negative n). An Entity List always takes Psi's own order.
 */
public class PieceOperatorListDistanceKeep extends PieceOperatorListDistanceBase {
    public PieceOperatorListDistanceKeep(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        double value = numberValue(context);
        List<?> sorted = sortedByDistance(context);
        int size = sorted.size();
        // Truncated toward zero like the other count inputs; NaN keeps nothing.
        long requested = Double.isNaN(value) ? 0L : (long) value;
        long magnitude = requested == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(requested);
        int count = (int) Math.min(magnitude, size);
        List<Object> kept = new ArrayList<>(count);
        if (requested >= 0) {
            kept.addAll(sorted.subList(0, count));
        } else {
            for (int i = size - 1; i >= size - count; i--) {
                kept.add(sorted.get(i));
            }
        }
        return makeList(kept);
    }

    @Override
    public Class<?> getEvaluationType() {
        return listType();
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable(getModeOption().listTranslationKey());
    }
}
