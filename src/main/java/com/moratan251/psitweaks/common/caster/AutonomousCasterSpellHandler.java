package com.moratan251.psitweaks.common.caster;

import vazkii.psi.api.spell.PreSpellCastEvent;

/**
 * 自律詠唱機の専用 Caster が詠唱するときだけ働く PreSpellCastEvent handler。
 * <p>
 * 他の handler がコストを書き換えた後の最終コストで判定するため、PsiEvents へは最後に登録する。
 * 内部 Psi を超えるコストは overload させずに取り消す。
 */
public final class AutonomousCasterSpellHandler {
    private AutonomousCasterSpellHandler() {
    }

    public static void onPreSpellCast(PreSpellCastEvent event) {
        if (event.isCanceled() || !(event.getPlayer() instanceof AutonomousCasterPlayer caster)) {
            return;
        }
        if (caster.level().isClientSide || !caster.isAttached()) {
            event.setCanceled(true);
            event.setCancellationMessage("");
            return;
        }

        int cost = Math.max(0, event.getCost());
        if (cost <= caster.availablePsi()) {
            return;
        }

        event.setCanceled(true);
        // 取り消し理由は機械側で必要量つきのメッセージにして作動ログへ残す。
        event.setCancellationMessage("");
        caster.reportInsufficientPsi(cost);
    }
}
