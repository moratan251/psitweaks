package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.common.effects.FlightPsiCostProfile;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickFlight;
import vazkii.psi.api.spell.PreSpellCastEvent;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellPiece;

public final class FlightPsiCostCaptureHandler {
    private FlightPsiCostCaptureHandler() {
    }

    public static void onPreSpellCast(PreSpellCastEvent event) {
        if (event.isCanceled()
                || event.getPlayer().level().isClientSide
                || event.getContext() == null
                || !containsFlightPiece(event.getSpell())) {
            return;
        }

        event.getContext().customData.put(
                FlightPsiCostProfile.CONTEXT_KEY,
                FlightPsiCostProfile.capture(event.getCad(), event.getBullet())
        );
    }

    private static boolean containsFlightPiece(Spell spell) {
        if (spell == null || spell.grid == null || spell.grid.gridData == null) {
            return false;
        }

        for (SpellPiece[] row : spell.grid.gridData) {
            if (row == null) {
                continue;
            }
            for (SpellPiece piece : row) {
                if (piece instanceof PieceTrickFlight) {
                    return true;
                }
            }
        }
        return false;
    }
}
