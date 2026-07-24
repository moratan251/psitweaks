package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickDieFlex;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickJumpFlex;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickSwitchFlex;
import com.moratan251.psitweaks.common.spells.util.SpellPsiRefundLedger;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.spell.PreSpellCastEvent;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellPiece;

public final class SpellPsiRefundCaptureHandler {
    private SpellPsiRefundCaptureHandler() {
    }

    public static void onPreSpellCast(PreSpellCastEvent event) {
        if (event.isCanceled()
                || event.getPlayer().level().isClientSide
                || event.getContext() == null
                || !containsRefundPiece(event.getSpell())) {
            return;
        }

        int playerPsi = Math.max(event.getPlayerData().getAvailablePsi(), 0);
        int cadPsi = getStoredPsi(event.getCad());
        SpellPsiRefundLedger ledger = new SpellPsiRefundLedger(
                event.getCad(),
                event.getBullet(),
                event.getCost(),
                playerPsi,
                cadPsi);
        event.getContext().customData.put(SpellPsiRefundLedger.CONTEXT_KEY, ledger);
    }

    private static int getStoredPsi(ItemStack cad) {
        if (!cad.isEmpty() && cad.getItem() instanceof ICAD cadItem) {
            return Math.max(cadItem.getStoredPsi(cad), 0);
        }
        return 0;
    }

    private static boolean containsRefundPiece(Spell spell) {
        if (spell == null || spell.grid == null || spell.grid.gridData == null) {
            return false;
        }

        for (SpellPiece[] row : spell.grid.gridData) {
            if (row == null) {
                continue;
            }
            for (SpellPiece piece : row) {
                if (piece instanceof PieceTrickDieFlex
                        || piece instanceof PieceTrickJumpFlex
                        || piece instanceof PieceTrickSwitchFlex) {
                    return true;
                }
            }
        }
        return false;
    }
}
