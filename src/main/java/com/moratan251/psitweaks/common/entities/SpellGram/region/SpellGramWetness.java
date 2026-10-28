package com.moratan251.psitweaks.common.entities.SpellGram.region;

import net.minecraft.world.entity.Entity;

/**
 * 「濡れ」の共通判定（試作）。水中か雨の中にいるか、湿潤の領域で濡れてから少しの間を濡れとする。
 * 連鎖電光などの「濡れた相手に強い」術式はここを見る。
 */
public final class SpellGramWetness {
    private static final String NBT_WET_UNTIL = "psitweaks:spellgram_wet_until";

    private SpellGramWetness() {
    }

    public static boolean isWet(Entity entity) {
        if (entity.isInWaterOrRain()) {
            return true;
        }
        long until = entity.getPersistentData().getLong(NBT_WET_UNTIL);
        return until > entity.level().getGameTime();
    }

    /** {@code ticks} の間、濡れた状態にする。すでに長く濡れているなら短くしない。 */
    public static void soak(Entity entity, int ticks) {
        long until = entity.level().getGameTime() + ticks;
        if (until > entity.getPersistentData().getLong(NBT_WET_UNTIL)) {
            entity.getPersistentData().putLong(NBT_WET_UNTIL, until);
        }
    }
}
