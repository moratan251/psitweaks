package com.moratan251.psitweaks.common.entities.SpellGram;

import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.compat.ZoneRadiationCompat;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityInterferenceZone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 作動式: 除染（Decontamination、Mekanism があるときだけ）の核。領域ではない常駐魔法式で、置いた位置から半径
 * {@link #RADIUS} ブロックの中の Mekanism の放射線源を、{@link #INTERVAL} tick ごとに弱める。生き物の被ばく量には触れない。
 * <p>
 * 対象は中心のブロックの真ん中が半径の中にある線源。1 秒あたり 置いたときのコスト × {@link #SOURCE_PURIFICATION_PER_PSI} Sv/h ÷
 * 存続時間（秒）を、核に届いている量（{@link #reachingAmount}）に比例して配って引き、Mekanism の下限を下回るものは消す。使い切れ
 * なかった分は残りの線源へ 1 回だけ配り直す（{@link #allocateWeighted}）。弱めている線源から核へ白い光の粒が流れ込む。
 * <p>
 * 線源は Mekanism の線源の強さを直接減らす（{@link ZoneRadiationCompat#weakenSource}）。核のブロックが、勝てない他人の領域干渉の
 * 中にある間は何もしない（{@link #isSuppressed}、時間は進む）。
 */
public class EntityDecontamination extends SpellGramObject {
    /** 周りの汚染を取り除く半径（ブロック）。 */
    public static final double RADIUS = 32.0;
    /** 除染する間隔（tick）。 */
    public static final int INTERVAL = 20;
    /** 線源の除染能力: 置いたときのコスト 1 Psi あたり、存続時間全体で弱められる線源の強さ（Sv/h）。 */
    public static final double SOURCE_PURIFICATION_PER_PSI = 0.5;
    /** 光の粒を流す線源の数の上限（弱めた量の多い順）。 */
    private static final int MAX_STREAMED_SOURCES = 8;
    /** 線源 1 つから 1 回に流す光の粒の数。 */
    private static final int MOTES_PER_STREAM = 4;
    /** 流れ込む光の粒の速さ（ブロック / tick）。 */
    private static final double STREAM_SPEED = 0.25;

    /** 直前の間隔に弱めた線源の強さ（Sv/h、1 秒あたり）。照準の表示のためクライアントへ送る。 */
    private static final EntityDataAccessor<Float> DATA_SOURCE_USED =
            SynchedEntityData.defineId(EntityDecontamination.class, EntityDataSerializers.FLOAT);
    /** 半径の中の線源の数（除染したあと）。 */
    private static final EntityDataAccessor<Integer> DATA_SOURCE_COUNT =
            SynchedEntityData.defineId(EntityDecontamination.class, EntityDataSerializers.INT);
    /** 半径の中の線源の強さの合計（Sv/h、除染したあと）。 */
    private static final EntityDataAccessor<Float> DATA_SOURCES_LEFT =
            SynchedEntityData.defineId(EntityDecontamination.class, EntityDataSerializers.FLOAT);

    /** 術者の CAD の色（核の魔法陣の色）。 */
    private int color = 0xFFFFFF;
    /** 入れた威力（10 を超えた分はコスト＝能力だけを増やす）。照準の表示と記録のためだけに持つ。 */
    private float power = 1.0F;
    /** 置いた人の側と、置いた時点の事象干渉力（領域干渉と比べる）。サーバー側だけ。 */
    @Nullable
    private UUID ownerSide;
    private double placedInterference = 1.0;
    private long suppressedTick = Long.MIN_VALUE;
    private long suppressedVersion = -1L;
    private boolean suppressed;
    /** 線源の除染能力（1 秒あたり）。クライアントでは出現のデータで受け取る。 */
    private double clientSourceCapacity;
    /** 弱めた線源の強さの合計（Sv/h、GameTest 用）。 */
    private double sourcePurified;

    public EntityDecontamination(EntityType<? extends EntityDecontamination> type, Level level) {
        super(type, level);
    }

    public EntityDecontamination(Level level) {
        this(PsitweaksEntities.DECONTAMINATION.get(), level);
    }

    @Override
    public boolean isResident() {
        return true;
    }

    @Override
    protected void defineSpellGramSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SOURCE_USED, 0.0F);
        builder.define(DATA_SOURCE_COUNT, 0);
        builder.define(DATA_SOURCES_LEFT, 0.0F);
    }

    /** 置くときに呼ぶ。置いた人の側と、置いた時点の事象干渉力もここで決まる。 */
    @Override
    public void setCaster(@Nullable LivingEntity caster) {
        super.setCaster(caster);
        if (caster != null) {
            this.ownerSide = caster.getUUID();
            this.placedInterference = interferenceOf(caster);
        }
    }

    /** 置く前に呼ぶ。威力と、核の魔法陣の色（術者の CAD の色）。 */
    public void configure(float power, int color) {
        this.power = Math.max(0.0F, power);
        this.color = color & 0xFFFFFF;
        SpellGramIndex.touch();
    }

    public float getPower() {
        return power;
    }

    public int getColor() {
        return color;
    }

    /** 線源の除染能力（Sv/h、1 秒あたり）: 置いたときのコスト × 0.5 ÷ 存続時間（秒）。 */
    public double sourceCapacityPerSecond() {
        if (level().isClientSide) {
            return clientSourceCapacity;
        }
        return capacityPerSecond(getPlacementCost(), getLifetimeTicks(), SOURCE_PURIFICATION_PER_PSI);
    }

    /** 置いたときのコスト {@code placementCost}・存続時間 {@code lifetimeTicks} の、1 秒あたりの除染能力。時間によらない。 */
    public static double capacityPerSecond(float placementCost, int lifetimeTicks, double perPsi) {
        if (lifetimeTicks <= 0) {
            return 0.0;
        }
        return placementCost * perPsi / (lifetimeTicks / 20.0);
    }

    /**
     * 量 {@code amounts} のそれぞれから、合わせて {@code capacity} まで、重み {@code weights} に比例して引く量。引いたあと
     * {@code floor} を下回るものは全部引く（線源は消える）。残りが少なくて使い切れなかった分は、まだ残っているものへ 1 回だけ、
     * 同じく重みに比例して配り直す。
     */
    public static double[] allocateWeighted(double[] amounts, double[] weights, double capacity, double floor) {
        double[] taken = new double[amounts.length];
        double left = capacity;
        for (int pass = 0; pass < 2 && left > 0.0; pass++) {
            double total = 0.0;
            for (int i = 0; i < amounts.length; i++) {
                if (amounts[i] - taken[i] > 0.0) {
                    total += weights[i];
                }
            }
            if (total <= 0.0) {
                break;
            }
            double used = 0.0;
            for (int i = 0; i < amounts.length; i++) {
                double rest = amounts[i] - taken[i];
                if (rest <= 0.0) {
                    continue;
                }
                double share = left * weights[i] / total;
                double take = rest - share < floor ? rest : share;
                taken[i] += take;
                used += take;
            }
            left -= used;
        }
        return taken;
    }

    /**
     * 強さ {@code magnitude}（Sv/h）の線源が、位置 {@code core} に届かせている量（配る重み）: 強さ ÷ max(1, 線源のブロックの
     * 真ん中から core までの距離²)（Mekanism が場所の放射線の量を求める式と同じ形）。
     */
    public static double reachingAmount(double magnitude, BlockPos source, Vec3 core) {
        return magnitude / Math.max(1.0, Vec3.atCenterOf(source).distanceToSqr(core));
    }

    /**
     * 領域干渉に止められているか（サーバー側。クライアントではいつも偽）: 核のブロックで効いている他人の領域干渉のうち、置いた時点の
     * 事象干渉力がこの核の置いた時点の事象干渉力より高いもの、同じなら先に置いたものがあれば止められている（サークルと同じ）。
     */
    public boolean isSuppressed() {
        if (level().isClientSide) {
            return false;
        }
        long now = level().getGameTime();
        long version = SpellGramIndex.version();
        if (suppressedTick != now || suppressedVersion != version) {
            suppressedTick = now;
            suppressedVersion = version;
            suppressed = computeSuppressed();
        }
        return suppressed;
    }

    private boolean computeSuppressed() {
        BlockPos center = blockPosition();
        for (EntityInterferenceZone zone : SpellGramIndex.all(level(), EntityInterferenceZone.class)) {
            UUID zoneSide = zone.getOwnerSide();
            if (zone.isEnded() || zoneSide == null || zoneSide.equals(ownerSide)) {
                continue;
            }
            int interference = Double.compare(zone.getPlacedInterference(), placedInterference);
            boolean beaten = interference != 0 ? interference > 0 : zone.getId() < getId();
            if (beaten && zone.isInsideAndActive(center)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onSpellGramTickServer() {
        if (isEnded() || !(level() instanceof ServerLevel level) || tickCount % INTERVAL != 0
                || !MekanismCompat.isMekanismLoaded() || !ZoneRadiationCompat.isRadiationEnabled()) {
            return;
        }
        if (isSuppressed()) {
            entityData.set(DATA_SOURCE_USED, 0.0F);
            return;
        }
        purifySources(level);
    }

    private void purifySources(ServerLevel level) {
        Vec3 core = position();
        List<ZoneRadiationCompat.Source> sources = ZoneRadiationCompat.sourcesWithin(level, core, RADIUS);
        double[] magnitudes = new double[sources.size()];
        double[] weights = new double[sources.size()];
        for (int i = 0; i < magnitudes.length; i++) {
            magnitudes[i] = sources.get(i).magnitude();
            weights[i] = reachingAmount(magnitudes[i], sources.get(i).pos(), core);
        }
        double minimum = ZoneRadiationCompat.minMagnitude();
        double[] taken = allocateWeighted(magnitudes, weights, sourceCapacityPerSecond() * INTERVAL / 20.0, minimum);
        double used = 0.0;
        double left = 0.0;
        int remaining = 0;
        for (int i = 0; i < magnitudes.length; i++) {
            BlockPos pos = sources.get(i).pos();
            double rest = magnitudes[i] - taken[i];
            if (rest < minimum) {
                ZoneRadiationCompat.removeSource(level, pos);
            } else {
                if (taken[i] > 0.0) {
                    ZoneRadiationCompat.weakenSource(level, pos, taken[i]);
                }
                left += rest;
                remaining++;
            }
            used += taken[i];
        }
        streamMotes(level, sources, taken);
        sourcePurified += used;
        entityData.set(DATA_SOURCE_USED, (float) (used * 20.0 / INTERVAL));
        entityData.set(DATA_SOURCES_LEFT, (float) left);
        entityData.set(DATA_SOURCE_COUNT, remaining);
    }

    /** 弱めた量の多い線源（{@link #MAX_STREAMED_SOURCES} まで）から、核へ向かって白い光の粒を流す（見ている人に送る）。 */
    private void streamMotes(ServerLevel level, List<ZoneRadiationCompat.Source> sources, double[] taken) {
        Integer[] order = new Integer[sources.size()];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        Arrays.sort(order, (a, b) -> Double.compare(taken[b], taken[a]));
        Vec3 core = position();
        for (int n = 0; n < Math.min(MAX_STREAMED_SOURCES, order.length); n++) {
            if (taken[order[n]] <= 0.0) {
                break;
            }
            Vec3 from = Vec3.atCenterOf(sources.get(order[n]).pos());
            Vec3 toCore = core.subtract(from);
            double length = toCore.length();
            if (length < 1.0E-3) {
                continue;
            }
            Vec3 direction = toCore.scale(1.0 / length);
            for (int m = 0; m < MOTES_PER_STREAM; m++) {
                Vec3 at = from.add(toCore.scale(random.nextDouble()));
                level.sendParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, 0, direction.x, direction.y, direction.z,
                        STREAM_SPEED);
            }
        }
    }

    /** 弱めた線源の強さの合計（Sv/h、GameTest 用）。 */
    public double sourcePurified() {
        return sourcePurified;
    }

    /** 照準の表示: 線源の除染能力と使った量、半径の中の線源の数と強さの合計。 */
    @Override
    public List<Component> hudLines() {
        return List.of(
                Component.translatable("psitweaks.hud.decontamination.sources",
                        hudAmount(entityData.get(DATA_SOURCE_USED)), hudAmount(sourceCapacityPerSecond())),
                Component.translatable("psitweaks.hud.decontamination.left", entityData.get(DATA_SOURCE_COUNT),
                        hudAmount(entityData.get(DATA_SOURCES_LEFT))));
    }

    @Override
    protected void writeSpellGramSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(color);
        buffer.writeFloat(power);
        buffer.writeDouble(sourceCapacityPerSecond());
    }

    @Override
    protected void readSpellGramSpawnData(RegistryFriendlyByteBuf buffer) {
        color = buffer.readInt();
        power = buffer.readFloat();
        clientSourceCapacity = buffer.readDouble();
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide && !isRemoved() && random.nextInt(4) == 0) {
            // 核のまわりに、白い光の粒がときどきまたたく。
            level().addParticle(ParticleTypes.END_ROD, getX() + (random.nextDouble() - 0.5) * 0.8,
                    getY() + (random.nextDouble() - 0.5) * 0.8, getZ() + (random.nextDouble() - 0.5) * 0.8, 0.0, 0.01, 0.0);
        }
    }
}
