package com.moratan251.psitweaks.common.spells.spellpiece.etc;

import java.util.HashSet;
import java.util.Set;
import vazkii.psi.api.spell.CompiledSpell;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellGrid;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellRuntimeException;

public final class JumpAnchorHelper {
    private JumpAnchorHelper() {
    }

    public static int findMatchingAnchorOrder(Spell spell, SpellPiece source, String targetLabel)
            throws SpellCompilationException {
        int currentOrder = orderOf(source);
        int maxOrder = SpellGrid.GRID_SIZE * SpellGrid.GRID_SIZE;
        for (int order = currentOrder + 1; order < maxOrder; order++) {
            SpellPiece piece = pieceAtOrder(spell, order);
            if (piece instanceof PieceJumpAnchor anchor && anchor.matchesConfiguredLabel(targetLabel)) {
                return order;
            }
        }
        return -1;
    }

    public static boolean jumpToMatchingAnchor(SpellContext context, SpellPiece source, String targetLabel)
            throws SpellRuntimeException {
        if (context.actions == null || context.actions.isEmpty()) {
            return false;
        }

        int currentOrder = orderOf(source);
        int anchorOrder = findMatchingAnchorOrder(context, targetLabel, currentOrder);
        if (anchorOrder < 0) {
            return false;
        }

        Set<SpellPiece> requiredPieces = collectRequiredPieces(context, currentOrder, anchorOrder);
        removeSkippedActions(context, currentOrder, anchorOrder, requiredPieces);
        return true;
    }

    private static void removeSkippedActions(SpellContext context, int currentOrder, int anchorOrder,
            Set<SpellPiece> requiredPieces) {
        for (int i = context.actions.size() - 1; i >= 0; i--) {
            CompiledSpell.Action action = context.actions.get(i);
            int actionOrder = orderOf(action.piece);
            if (isBetween(actionOrder, currentOrder, anchorOrder) && !requiredPieces.contains(action.piece)) {
                context.actions.remove(i);
            }
        }
    }

    private static Set<SpellPiece> collectRequiredPieces(SpellContext context, int currentOrder, int anchorOrder)
            throws SpellRuntimeException {
        Set<SpellPiece> requiredPieces = new HashSet<>();
        for (CompiledSpell.Action action : context.actions) {
            int actionOrder = orderOf(action.piece);
            if (!isBetween(actionOrder, currentOrder, anchorOrder)) {
                collectRequiredPiece(context, action.piece, requiredPieces);
            }
        }
        return requiredPieces;
    }

    private static void collectRequiredPiece(SpellContext context, SpellPiece piece, Set<SpellPiece> requiredPieces)
            throws SpellRuntimeException {
        if (piece == null || !requiredPieces.add(piece)) {
            return;
        }

        if (context.cspell != null) {
            CompiledSpell.CatchHandler handler = context.cspell.errorHandlers.get(piece);
            if (handler != null) {
                collectRequiredPiece(context, handler.handlerPiece, requiredPieces);
            }
        }

        for (SpellParam<?> param : piece.paramSides.keySet()) {
            SpellParam.Side side = piece.paramSides.get(param);
            if (side == null || !side.isEnabled()) {
                continue;
            }
            collectRequiredPiece(context, getDependency(piece, side), requiredPieces);
        }
    }

    private static SpellPiece getDependency(SpellPiece piece, SpellParam.Side side) throws SpellRuntimeException {
        try {
            return piece.spell.grid.getPieceAtSideWithRedirections(piece.x, piece.y, side);
        } catch (SpellCompilationException exception) {
            throw new SpellRuntimeException(exception.getMessage());
        }
    }

    private static boolean isBetween(int order, int currentOrder, int anchorOrder) {
        return order > currentOrder && order < anchorOrder;
    }

    private static int findMatchingAnchorOrder(SpellContext context, String targetLabel, int currentOrder)
            throws SpellRuntimeException {
        int anchorOrder = -1;
        for (CompiledSpell.Action action : context.actions) {
            if (action.piece instanceof PieceJumpAnchor anchor) {
                int actionOrder = orderOf(anchor);
                if (actionOrder > currentOrder
                        && (anchorOrder < 0 || actionOrder < anchorOrder)
                        && matchesAnchor(anchor, targetLabel)) {
                    anchorOrder = actionOrder;
                }
            }
        }
        return anchorOrder;
    }

    private static boolean matchesAnchor(PieceJumpAnchor anchor, String targetLabel) throws SpellRuntimeException {
        try {
            return anchor.matchesConfiguredLabel(targetLabel);
        } catch (SpellCompilationException exception) {
            throw new SpellRuntimeException("psitweaks.spellerror.no_jump_anchor");
        }
    }

    private static SpellPiece pieceAtOrder(Spell spell, int order) {
        int x = order % SpellGrid.GRID_SIZE;
        int y = order / SpellGrid.GRID_SIZE;
        return spell.grid.gridData[x][y];
    }

    private static int orderOf(SpellPiece piece) {
        return piece.y * SpellGrid.GRID_SIZE + piece.x;
    }
}
