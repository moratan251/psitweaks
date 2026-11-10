package com.moratan251.psitweaks.common.compat;

import com.moratan251.psitweaks.common.entities.SpellGram.zone.ZoneArea;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

import java.util.function.Predicate;

/**
 * Create との連携の入口。Create のクラスは {@link CreateWashingIntegration} にだけ置き、Create がないときは読み込まない。
 */
public final class CreateCompat {
    private static final String CREATE_MOD_ID = "create";

    private CreateCompat() {
    }

    public static boolean isCreateLoaded() {
        return ModList.get().isLoaded(CREATE_MOD_ID);
    }

    /**
     * 範囲の中を 1 tick ぶん一括洗浄する（Create の扇風機の水と同じ）。落ちているアイテムと、デポ・ベルトの上のアイテムが対象。
     * {@code mayUse} が偽のブロック（保護された場所）のデポ・ベルトは洗わない。Create がなければ何もしない。
     */
    public static void washZone(ServerLevel level, ZoneArea area, Predicate<BlockPos> mayUse) {
        if (isCreateLoaded()) {
            CreateWashingIntegration.wash(level, area, mayUse);
        }
    }

    /**
     * クライアント側、毎 tick: ケース入りファンの水を当てたときと同じ一括洗浄のパーティクルを、範囲の中の落ちているアイテムと
     * デポ・ベルトの上のアイテムに出す（Create と同じく、出るかどうかは Create の側で毎回くじを引く）。Create がなければ何もしない。
     */
    public static void spawnWashingParticles(Level level, ZoneArea area) {
        if (isCreateLoaded()) {
            CreateWashingIntegration.spawnParticles(level, area);
        }
    }
}
