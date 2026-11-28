package com.moratan251.psitweaks.common.spells.spellpiece.operator;

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
        return makeList(keepByDistance(context, value));
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
