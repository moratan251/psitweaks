package com.moratan251.psitweaks.common.spells.util;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import vazkii.psi.api.spell.CompiledSpell;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;

public final class SpellPsiRefundHelper {
    private SpellPsiRefundHelper() {
    }

    public static List<CompiledSpell.Action> snapshotActions(SpellContext context) {
        if (context.actions == null || context.actions.isEmpty()) {
            return List.of();
        }
        return List.copyOf(context.actions);
    }

    public static void refundRemainingActions(SpellContext context) {
        refundActions(context, snapshotActions(context));
    }

    public static void refundRemovedActions(SpellContext context, List<CompiledSpell.Action> actionsBeforeJump) {
        if (actionsBeforeJump.isEmpty()) {
            return;
        }

        Set<CompiledSpell.Action> remainingActions = Collections.newSetFromMap(new IdentityHashMap<>());
        if (context.actions != null) {
            remainingActions.addAll(context.actions);
        }

        refundActions(context, actionsBeforeJump.stream()
                .filter(action -> !remainingActions.contains(action))
                .toList());
    }

    private static void refundActions(SpellContext context, List<CompiledSpell.Action> actions) {
        if (context.caster == null || actions.isEmpty()) {
            return;
        }

        int remainingRawCost = calculateRawCost(actions);
        if (remainingRawCost <= 0) {
            return;
        }

        SpellPsiRefundLedger ledger = SpellPsiRefundLedger.get(context);
        if (ledger != null) {
            ledger.refundRawCost(context.caster, remainingRawCost);
        }
    }

    private static int calculateRawCost(List<CompiledSpell.Action> actions) {
        long total = 0L;

        for (CompiledSpell.Action action : actions) {
            SpellMetadata pieceMetadata = new SpellMetadata();
            try {
                action.piece.addToMetadata(pieceMetadata);
            } catch (SpellCompilationException ignored) {
                continue;
            }

            total += pieceMetadata.getStat(EnumSpellStat.COST);
            if (total >= Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            if (total <= Integer.MIN_VALUE) {
                return Integer.MIN_VALUE;
            }
        }

        return Math.max((int) total, 0);
    }
}
