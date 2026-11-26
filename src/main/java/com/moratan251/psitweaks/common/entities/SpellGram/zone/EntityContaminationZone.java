package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.compat.ZoneRadiationCompat;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.particles.PsitweaksParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * 作動式: 汚染領域（Contamination Zone、Mekanism があるときだけ）の範囲。{@link #INTERVAL} tick ごとに、範囲の中の生き物
 * （攻撃術式の共通ルールで選ぶ、{@link #followsAttackRules}）を Mekanism の放射線にさらす。場所に線源は置かない。
 * 範囲の中は、入れた威力 1 で {@link #BASE_DOSE_RATE} Sv/h、威力が {@link #POWER_PER_TENFOLD} 上がるごとに 10 倍になる線量率
 * （{@link #doseRateFor}）の場所として扱い、Mekanism が場所の線量率から生き物に足すのと同じく、1 秒ごとに 線量率 ÷ 3600 を
 * 被ばく量に足す（{@link #doseFor}）。上限はなく、中にいるあいだ増え続ける。防護（防具、作動式: 放射線フィルタ）の割り引き、
 * 被ばく量の減り方、ダメージは Mekanism 側で効く。
 * <p>
 * 同じ種類が重なったところでは、威力の一番高いものだけが効く。
 * <p>
 * 同じ術者の乱流領域と重なったブロックは「汚染流」になる: 照射は続けたまま、中の生き物に {@link #STORM_INTERVAL} tick ごとに
 * ダメージを与える（攻撃術式の共通ルール、「プレイヤーに安全」も照射と同じ、設定 {@code contaminated_stream.damageMultiplier}）。
 * 1 回の基本のダメージは 0.15 × 強さ（{@link SpellGramZone#STORM_DAMAGE_PER_POWER}）、強さは乱流の強さをこの領域の入れた威力 × 4
 * で頭打ちにしたもの（{@link #comboAttackPowerAt}、熱乱流・吹雪と同じ）。
 */
public class EntityContaminationZone extends SpellGramZone {
    /** 辺の色: 毒々しい黄緑。 */
    public static final int OUTLINE_COLOR = 0xC4FF1A;
    /** 乱流と重なったとき（汚染流）の辺の色と、渦の煙の色: 毒の緑。 */
    public static final int STORM_OUTLINE_COLOR = 0x47DB38;
    /** 汚染流のダメージの間隔（tick）。 */
    public static final int STORM_INTERVAL = 10;
    /** さらす間隔（tick）。 */
    public static final int INTERVAL = 20;
    /** 入れた威力 1 の範囲の中の線量率（Sv/h）。 */
    public static final double BASE_DOSE_RATE = 1.0;
    /** 線量率が 10 倍になる威力の差。 */
    public static final double POWER_PER_TENFOLD = 3.0;
    private static final double TICKS_PER_HOUR = 72000.0;
    /** 威力の既定値（置く前に設定しなかったとき、中の威力）。 */
    public static final float DEFAULT_POWER = 5.0F;
    private static final int MAX_MOTES_PER_TICK = 3;

    /** 中の威力（作動式に入れた威力 + 4、5〜14）。 */
    private float power = DEFAULT_POWER;
    /** 唱えた術式が「プレイヤーに安全」だったか（真ならプレイヤーをさらさない）。サーバー側だけ。 */
    private boolean safeToPlayers;

    public EntityContaminationZone(EntityType<? extends EntityContaminationZone> type, Level level) {
        super(type, level);
    }

    public EntityContaminationZone(Level level) {
        this(PsitweaksEntities.CONTAMINATION_ZONE.get(), level);
    }

    /** 置く前に呼ぶ。{@code power} は中の威力（作動式に入れた威力 + 4）。 */
    public void setPower(float power) {
        this.power = Math.max(0.0F, Math.min(power, MAX_EFFECT_POWER));
        SpellGramIndex.touch();
    }

    public float getPower() {
        return power;
    }

    /** 置く前に呼ぶ。唱えた術式が「プレイヤーに安全」か（放射線注入と同じく、攻撃術式の共通ルールに渡す）。 */
    public void setSafeToPlayers(boolean safeToPlayers) {
        this.safeToPlayers = safeToPlayers;
    }

    @Override
    protected com.moratan251.psitweaks.common.spells.combat.AttackRules.Options attackOptions() {
        return com.moratan251.psitweaks.common.spells.combat.AttackRules.Options.of(safeToPlayers);
    }

    /**
     * 中の威力 {@code power} の範囲の中の線量率（Sv/h）: 入れた威力（{@link #enteredPowerOf}、1〜10）が 1 で 1 Sv/h、3 上がる
     * ごとに 10 倍（4 で 10、7 で 100、10 で 1,000 Sv/h）。Mekanism のダメージは被ばく量の対数で決まるので、威力に対して
     * 指数で増やす。
     */
    public static double doseRateFor(float power) {
        return BASE_DOSE_RATE * Math.pow(10.0, (enteredPowerOf(power) - 1.0F) / POWER_PER_TENFOLD);
    }

    /** 中の威力 {@code power} の範囲が 1 回（{@link #INTERVAL} tick ぶん）で足す被ばく量（Sv）: 1 秒なら 線量率 ÷ 3600。 */
    public static double doseFor(float power) {
        return doseRateFor(power) * INTERVAL / TICKS_PER_HOUR;
    }

    @Override
    protected boolean followsAttackRules() {
        return true;
    }

    @Override
    protected int effectInterval() {
        return INTERVAL;
    }

    /** 同じ種類が重なったところでは、威力の一番高いものだけが効く。 */
    @Override
    protected double stackStrength() {
        return power;
    }

    @Override
    protected void affect(Entity target) {
        if (!(target instanceof LivingEntity living) || !MekanismCompat.isMekanismLoaded()
                || !ZoneRadiationCompat.isRadiationEnabled()) {
            return;
        }
        ZoneRadiationCompat.radiate(living, doseFor(power));
    }

    @Override
    public int getOutlineColor() {
        return isStorm() ? STORM_OUTLINE_COLOR : OUTLINE_COLOR;
    }

    @Override
    protected void onSpellGramTickServer() {
        super.onSpellGramTickServer();
        if (isEnded() || getArea().isEmpty() || !(level() instanceof ServerLevel level) || tickCount % STORM_INTERVAL != 0) {
            return;
        }
        refreshStorm();
        if (isStorm() && MekanismCompat.isMekanismLoaded() && ZoneRadiationCompat.isRadiationEnabled()) {
            // 汚染流: 同じ術者の乱流領域と重なったところ。
            ZoneAttacks.harmScaled(level, this, PsitweaksDamageTypes.CONTAMINATED_STREAM,
                    PsitweaksConfig.COMMON.contaminatedStreamDamageMultiplier.get(),
                    pos -> STORM_DAMAGE_PER_POWER * comboAttackPowerAt(pos, power,
                            EntityTurbulenceZone.PARTNER_ATTACK_POWER_PER_POWER), target -> true, target -> {
                    }, attackOptions());
        }
    }

    @Override
    protected void writeZoneSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeFloat(power);
    }

    @Override
    protected void readZoneSpawnData(RegistryFriendlyByteBuf buffer) {
        power = buffer.readFloat();
    }

    /** Mekanism の放射線の粒を、範囲の中にまばらに出す。乱流と重なったところ（汚染流）には、緑の煙と放射線の粒の渦も出す。 */
    @Override
    protected void spawnClientParticles() {
        if (!MekanismCompat.isMekanismLoaded()) {
            return;
        }
        if (isStorm()) {
            ParticleOptions smoke = ColorParticleOption.create(PsitweaksParticles.TINTED_CLOUD.get(),
                    ((STORM_OUTLINE_COLOR >> 16) & 0xFF) / 255.0F, ((STORM_OUTLINE_COLOR >> 8) & 0xFF) / 255.0F,
                    (STORM_OUTLINE_COLOR & 0xFF) / 255.0F);
            for (AABB storm : stormBoxes()) {
                int count = ZoneParticles.count(storm, 20.0, 6, 16);
                ZoneParticles.vortex(level(), random, storm, particleCount(count), smoke, 0.5, 0.04);
                ZoneParticles.vortex(level(), random, storm, particleCount(Math.max(1, count / 3)),
                        ZoneRadiationCompat.radiationParticle(), 0.45, 0.02);
            }
        }
        AABB box = getOutline();
        double volume = box.getXsize() * box.getYsize() * box.getZsize();
        int motes = particleCount(Math.min(MAX_MOTES_PER_TICK, Math.max(1.0, volume / 400.0)));
        if (motes <= 0) {
            return;
        }
        ParticleOptions radiation = ZoneRadiationCompat.radiationParticle();
        for (int i = 0; i < motes; i++) {
            double x = box.minX + random.nextDouble() * box.getXsize();
            double y = box.minY + random.nextDouble() * box.getYsize();
            double z = box.minZ + random.nextDouble() * box.getZsize();
            if (getArea().contains(BlockPos.containing(x, y, z))) {
                level().addParticle(radiation, x, y, z, 0.0, 0.0, 0.0);
            }
        }
    }
}
