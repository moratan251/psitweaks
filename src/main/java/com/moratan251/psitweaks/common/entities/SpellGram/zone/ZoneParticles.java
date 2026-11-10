package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * 領域の演出（クライアント側だけで呼ぶ）。乱流領域とその組み合わせ（熱乱流・吹雪・乱流＋湿潤）の竜巻。
 */
final class ZoneParticles {
    private ZoneParticles() {
    }

    /**
     * 箱 {@code box} の中に、縦の軸のまわりを回りながら昇る竜巻の粒を {@code count} 個出す。下が細く上が広い漏斗の形
     * （下は箱の幅の 15%、上は箱の幅いっぱい）。粒の速さは、回る向きに {@code swirl}、上向きに {@code rise}、軸へ少し寄せる。
     */
    static void vortex(Level level, RandomSource random, AABB box, int count, ParticleOptions particle,
                       double swirl, double rise) {
        double centerX = (box.minX + box.maxX) * 0.5;
        double centerZ = (box.minZ + box.maxZ) * 0.5;
        double maxRadius = Math.min(box.getXsize(), box.getZsize()) * 0.5;
        for (int i = 0; i < count; i++) {
            double height = random.nextDouble();
            double radius = maxRadius * (0.15 + 0.85 * height) * (0.75 + 0.25 * random.nextDouble());
            double angle = random.nextDouble() * Math.PI * 2.0;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double x = centerX + cos * radius;
            double y = box.minY + height * box.getYsize();
            double z = centerZ + sin * radius;
            // 回る向き（反時計回り）に、外へ飛ばされすぎないよう少し内向きを足す。
            double vx = -sin * swirl - cos * swirl * 0.15;
            double vz = cos * swirl - sin * swirl * 0.15;
            level.addParticle(particle, x, y, z, vx, rise, vz);
        }
    }

    /** 竜巻の粒の数: 箱の体積に合わせて、{@code min}〜{@code max} 個。 */
    static int count(AABB box, double volumePerParticle, int min, int max) {
        double volume = box.getXsize() * box.getYsize() * box.getZsize();
        return (int) Math.max(min, Math.min(max, volume / volumePerParticle));
    }
}
