package com.moratan251.psitweaks.common.spells;

import net.minecraft.server.MinecraftServer;
import vazkii.psi.api.spell.SpellContext;

public final class SpellSafetyUtils {

    public static final String NBT_SAFE_TO_PLAYERS = "PsitweaksSafeToPlayers";

    private SpellSafetyUtils() {}

    /**
     * サーバーの pvp 設定が無効（false）なら true を返す。
     * true の場合、攻撃スペルはプレイヤーをダメージ対象から除外する。
     */
    public static boolean hasSafeToPlayers(SpellContext context) {
        if (context == null || context.caster == null) {
            return false;
        }
        MinecraftServer server = context.caster.level().getServer();
        if (server == null) {
            return false;
        }
        return !server.isPvpAllowed();
    }
}
