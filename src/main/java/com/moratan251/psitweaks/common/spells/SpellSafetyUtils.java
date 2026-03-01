package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import vazkii.psi.api.spell.SpellContext;

public final class SpellSafetyUtils {

    public static final String NBT_SAFE_TO_PLAYERS = "PsitweaksSafeToPlayers";

    private SpellSafetyUtils() {}

    /**
     * Configの safeToPlayers が有効なら true を返す。
     * 攻撃スペルはプレイヤーをダメージ対象から除外する。
     */
    public static boolean hasSafeToPlayers(SpellContext context) {
        return PsitweaksConfig.COMMON.safeToPlayers.get();
    }
}
