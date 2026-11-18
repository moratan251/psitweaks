package com.moratan251.psitweaks.client.particles;

import com.moratan251.psitweaks.client.config.PsitweaksClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * 領域魔法の中の演出の粒の数の調整（クライアント側だけ）。領域を何個も置いたときに粒が増えすぎないようにする。
 * <ul>
 *   <li>距離: カメラから範囲の箱までが {@link #NEAR_DISTANCE} ブロックまではそのまま、{@link #FAR_DISTANCE} ブロックで
 *   {@link #FAR_DENSITY} 倍まで減らす。</li>
 *   <li>上限: すべての領域を合わせて 1 tick に出す粒の数を、クライアント設定 {@code effects.zoneParticleLimit} までにする。
 *   前の tick に出そうとした数（距離で減らしたあと）が上限を超えていたら、すべての領域を同じ割合で減らす。</li>
 *   <li>ビデオ設定の「パーティクルの表示」: 「最少」なら出さない、「少し」なら半分にする（バニラがそこからさらに 3 分の 1 を間引く）。</li>
 * </ul>
 */
public final class ZoneParticleBudget {
    /** カメラから範囲の箱までがこの距離までなら、距離では減らさない。 */
    static final double NEAR_DISTANCE = 16.0;
    /** カメラから範囲の箱までがこの距離より遠ければ、{@link #FAR_DENSITY} 倍にする（あいだは直線で変える）。 */
    static final double FAR_DISTANCE = 40.0;
    static final double FAR_DENSITY = 0.3;
    /** ビデオ設定の「パーティクルの表示」が「少し」のときの倍率。 */
    static final double DECREASED_DENSITY = 0.5;

    /** 割合を決めた tick（ゲーム内時刻）。 */
    private static long tick = Long.MIN_VALUE;
    /** この tick に、すべての領域が出そうとした粒の数（距離で減らしたあと、上限で減らす前）。 */
    private static double requested;
    /** この tick の、上限による倍率（前の tick に出そうとした数から決める）。 */
    private static double limitScale = 1.0;

    private ZoneParticleBudget() {
    }

    /**
     * 範囲 {@code box} の、カメラからの距離による倍率（0〜1）。「パーティクルの表示」が「最少」なら 0 で、
     * 演出の準備もしない。
     */
    public static double distanceFactor(AABB box) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.particles().get() == ParticleStatus.MINIMAL) {
            return 0.0;
        }
        double distance = Math.sqrt(box.distanceToSqr(minecraft.gameRenderer.getMainCamera().getPosition()));
        if (distance <= NEAR_DISTANCE) {
            return 1.0;
        }
        if (distance >= FAR_DISTANCE) {
            return FAR_DENSITY;
        }
        return Mth.lerp((distance - NEAR_DISTANCE) / (FAR_DISTANCE - NEAR_DISTANCE), 1.0, FAR_DENSITY);
    }

    /** この tick の、すべての領域に共通の倍率: 上限による倍率 ×「パーティクルの表示」による倍率。 */
    public static double sharedFactor(Level level) {
        rollover(level);
        double setting = Minecraft.getInstance().options.particles().get() == ParticleStatus.DECREASED ? DECREASED_DENSITY : 1.0;
        return limitScale * setting;
    }

    /** 領域が出そうとした粒の数（距離で減らしたあと）を足す。次の tick の上限による倍率に使う。 */
    public static void request(double count) {
        requested += count;
    }

    /** tick が変わったら、前の tick に出そうとした数から上限による倍率を決め直す。 */
    private static void rollover(Level level) {
        long now = level.getGameTime();
        if (now == tick) {
            return;
        }
        int limit = PsitweaksClientConfig.CLIENT.zoneParticleLimit.get();
        // 前の tick に領域の演出がなかった（しばらく見ていなかった）ときは、古い数を使わない。
        double previous = now == tick + 1 ? requested : 0.0;
        if (limit <= 0) {
            limitScale = 0.0;
        } else {
            limitScale = previous <= limit ? 1.0 : limit / previous;
        }
        requested = 0.0;
        tick = now;
    }
}
