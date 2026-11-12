package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import vazkii.psi.api.cad.RegenPsiEvent;

/**
 * コンフィグ {@code disableRegenCooldown}: Psi を使ったあとの回復の待ち時間をなくす。Psi は使ったときに待ち時間を
 * {@code PlayerData} に積み、毎 tick の回復の前に {@link RegenPsiEvent} を出すので、そこで待ち時間を 0 にして、その tick の
 * 回復を計算し直させる（{@link RegenPsiEvent#setRegenCooldown} が計算し直す）。使った次の tick から回復する。
 * Psi はクライアントでも回復を計算するので、両側で動かす（サーバーの値は Psi が同期する）。
 */
public final class RegenCooldownHandler {
    private RegenCooldownHandler() {
    }

    public static void onRegenPsi(RegenPsiEvent event) {
        if (event.getRegenCooldown() > 0 && PsitweaksConfig.COMMON.disableRegenCooldown.get()) {
            event.setRegenCooldown(0);
        }
    }
}
