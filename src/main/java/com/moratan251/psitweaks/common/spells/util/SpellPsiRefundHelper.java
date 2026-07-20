package com.moratan251.psitweaks.common.spells.util;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.ISocketable;
import vazkii.psi.api.spell.CompiledSpell;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.ISpellAcceptor;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.ItemCircleSpellBullet;

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

        ItemStack cad = PsiAPI.getPlayerCAD(context.caster);
        ItemStack spellContainer = getCastingSpellContainer(context, cad);
        int refundCost = ItemCAD.getRealCost(cad, spellContainer, remainingRawCost);
        if (isCircleSpellBullet(spellContainer)) {
            refundCost /= 20;
        }
        if (refundCost > 0) {
            PlayerDataHandler.get(context.caster).deductPsi(-refundCost, 0, true, true);
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

    private static ItemStack getCastingSpellContainer(SpellContext context, ItemStack cad) {
        ItemStack spellContainer = ItemStack.EMPTY;

        // Prefer the tool-selected spell container (e.g. exosuit/tools).
        if (context.tool != null && !context.tool.isEmpty() && ISocketable.isSocketable(context.tool)) {
            spellContainer = ISocketable.socketable(context.tool).getSelectedBullet();
        }

        // Flash Ring is not socketable and may not be treated as "container" by Psi.
        if (spellContainer.isEmpty() && context.tool != null && !context.tool.isEmpty()
                && ISpellAcceptor.isAcceptor(context.tool)) {
            spellContainer = context.tool;
        }

        // Fallback to the CAD's selected bullet.
        if (spellContainer.isEmpty() && !cad.isEmpty() && ISocketable.isSocketable(cad)) {
            spellContainer = ISocketable.socketable(cad).getSelectedBullet();
        }

        return spellContainer;
    }

    private static boolean isCircleSpellBullet(ItemStack spellContainer) {
        return !spellContainer.isEmpty() && spellContainer.getItem() instanceof ItemCircleSpellBullet;
    }
}
