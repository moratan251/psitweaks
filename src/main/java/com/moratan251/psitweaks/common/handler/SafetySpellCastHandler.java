package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.common.spells.spellpiece.etc.PieceSafety;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.spell.PreSpellCastEvent;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellPiece;

public final class SafetySpellCastHandler {
    public static final String ERROR_INSUFFICIENT_PSI = "psitweaks.spellerror.safety_insufficient_psi";

    private SafetySpellCastHandler() {
    }

    public static void onPreSpellCast(PreSpellCastEvent event) {
        if (event.isCanceled() || !containsSafety(event.getSpell())) {
            return;
        }

        long availablePsi = Math.max(0, event.getPlayerData().getAvailablePsi());
        ItemStack cad = event.getCad();
        if (!cad.isEmpty() && cad.getItem() instanceof ICAD cadItem) {
            availablePsi += Math.max(0, cadItem.getStoredPsi(cad));
        }

        long cost = Math.max(0, event.getCost());
        if (availablePsi >= cost) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationMessage(event.getPlayer().level().isClientSide
                ? ""
                : ERROR_INSUFFICIENT_PSI);
    }

    private static boolean containsSafety(Spell spell) {
        if (spell == null || spell.grid == null || spell.grid.gridData == null) {
            return false;
        }

        for (SpellPiece[] row : spell.grid.gridData) {
            if (row == null) {
                continue;
            }
            for (SpellPiece piece : row) {
                if (piece instanceof PieceSafety) {
                    return true;
                }
            }
        }
        return false;
    }
}
