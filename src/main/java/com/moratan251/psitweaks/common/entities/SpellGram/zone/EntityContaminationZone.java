package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.compat.ZoneRadiationCompat;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * 作動式: 汚染領域（Contamination Zone、Mekanism があるときだけ）の範囲。{@link #INTERVAL} tick ごとに、範囲の中の生き物
 * （攻撃術式の共通ルールで選ぶ、{@link #followsAttackRules}）を Mekanism の放射線にさらす。場所に線源は置かない。
 * 被ばく量の上限は 中の威力 × {@link #DOSE_LIMIT_PER_POWER}（{@link #doseLimitFor}）で、1 秒に上限の
 * 1/{@link #RISE_STEPS} ずつ上げる。上限より多い相手には何もしない。防護（防具、作動式: 放射線フィルタ）の割り引きは
 * Mekanism 側で効く。
 * <p>
 * 同じ種類が重なったところでは、威力の一番高いものだけが効く。
 */
public class EntityContaminationZone extends SpellGramZone {
    /** 辺の色: 毒々しい黄緑。 */
    public static final int OUTLINE_COLOR = 0xC4FF1A;
    /** さらす間隔（tick）。 */
    public static final int INTERVAL = 20;
    /** 中の威力 1 あたりの、被ばく量の上限（Sv）: 0.1 mSv。 */
    public static final double DOSE_LIMIT_PER_POWER = 1.0E-4;
    /** 上限まで何回に分けて上げるか（1 回に上限の 1/5）。 */
    public static final int RISE_STEPS = 5;
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

    /** 中の威力 {@code power} の被ばく量の上限（Sv）: 中の威力 × 0.1 mSv（入れた威力 1 で 0.5 mSv、10 で 1.4 mSv）。 */
    public static double doseLimitFor(float power) {
        return Math.max(0.0F, power) * DOSE_LIMIT_PER_POWER;
    }

    /** 被ばく量 {@code dose}（Sv）の相手に 1 回で足す量（Sv）: 上限の 1/5、上限を超えない分だけ。上限以上なら 0。 */
    public static double doseStep(double dose, double limit) {
        return dose >= limit ? 0.0 : Math.min(limit / RISE_STEPS, limit - dose);
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
        double step = doseStep(ZoneRadiationCompat.exposure(living), doseLimitFor(power));
        if (step > 0.0) {
            ZoneRadiationCompat.radiate(living, step);
        }
    }

    @Override
    public int getOutlineColor() {
        return OUTLINE_COLOR;
    }

    /** 照準の表示: 被ばく量の上限（mSv）。 */
    @Override
    public List<Component> hudLines() {
        return List.of(Component.translatable("psitweaks.hud.zone_contamination", hudNumber(doseLimitFor(power) * 1000.0)));
    }

    @Override
    protected void writeZoneSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeFloat(power);
    }

    @Override
    protected void readZoneSpawnData(RegistryFriendlyByteBuf buffer) {
        power = buffer.readFloat();
    }

    /** Mekanism の放射線の粒を、範囲の中にまばらに出す。 */
    @Override
    protected void spawnClientParticles() {
        if (!MekanismCompat.isMekanismLoaded()) {
            return;
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
