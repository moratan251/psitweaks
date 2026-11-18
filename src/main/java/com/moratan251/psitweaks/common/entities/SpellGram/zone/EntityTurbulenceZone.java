package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.common.caster.CasterIds;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import net.minecraft.core.BlockPos;
import com.moratan251.psitweaks.common.particles.PsitweaksParticles;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import com.moratan251.psitweaks.common.compat.ImmersiveEngineeringCompat;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.compat.MekanismGameplayCompat;
import com.moratan251.psitweaks.common.compat.SableRangeCompat;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 作動式: 乱流領域（仮）の範囲。中に暴風が吹き荒れる。運ぶのは加重領域の役目なので、乱流は「乱して散らす」ものと、ほかの領域を
 * 強める触媒にする。
 * <ul>
 *   <li>他者の飛び道具（矢・トライデント・雪玉・火の玉など）は、入ってきたときに向きをランダムに曲げられる（威力で大きく）。
 *   魔法の弾（Psi・Ars Nouveau・Iron's Spells 'n Spellbooks）はそらさない。</li>
 *   <li>残留ポーションやドラゴンブレスの雲を吹き散らす（早く縮んで消える）。</li>
 *   <li>風量（{@link #windAmount}、単位 WU、置いた時点の事象干渉力で増える）を、範囲の中の風を受ける機械（Mekanism の風力発電機と
 *   IE の風車）の台数で分ける（{@link #generatorWind}）。受け取った風は発電に足す（{@code WindGeneratorTurbulenceMixin}・
 *   {@code WindmillTurbulenceMixin}）。どちらの mod もなくても値を持つ。</li>
 *   <li>同じ術者のほかの領域と重なると、重なったブロックで強める: 加熱領域は熱乱流、凍結領域は吹雪、湿潤領域は暴風雨、
 *   荷電領域は雷雲（{@link #blowsFor}、それぞれの領域が見る）。Create の一括加工（湿潤領域の一括洗浄、加熱領域の一括燻製、
 *   加熱領域と荷電領域の重なりの一括製錬）は、乱流が重なったところでしか起きない。</li>
 * </ul>
 * 仕様の全体と数値の根拠は {@code docs/1.21.1/spellgram-zones.md}。
 */
public class EntityTurbulenceZone extends SpellGramZone {
    /** 威力 1 あたりの、飛び道具を曲げる角度（度、仮）。威力 10 で最大 90 度。 */
    public static final double DEFLECT_DEGREES_PER_POWER = 9.0;
    /** 1 WU あたりに足す、風力発電機 1 台の発電量（J/t、15 FE/t、WU の定義）。乱流の風は人工のものなので高さによらない。 */
    public static final double WIND_JOULES_PER_WU = 37.5;
    /** 1 WU を FE/t にした値（{@link #WIND_JOULES_PER_WU} を Mekanism の 2.5 J = 1 FE で割ったもの）。IE の風車に足す発電量。 */
    public static final double WIND_FE_PER_WU = 15.0;
    /**
     * 風量 = 床の広さ × 威力 ÷ この値 × 風の倍率（WU、{@link #windFactor}）。干渉力 1 なら、サイズ 12・威力 10 で 6 WU、
     * サイズ 16・威力 10 で約 10.7 WU。コスト（広さ × 威力 × 2 / 5）と合わせて、コスト 1 Psi あたりの風は
     * 6.25 WU·tick（干渉力 1、時間によらない）。
     */
    public static final double WIND_FOOTPRINT_DIVISOR = 240.0;
    /** 組み合わせの攻撃の強さの上限。 */
    public static final float MAX_ATTACK_POWER = 40.0F;
    /** 組み合わせの攻撃の強さ 1 に要る、乱流領域の 1 秒あたりのコスト。 */
    public static final float ATTACK_COST_PER_SECOND = 3.0F;
    /** 熱乱流・吹雪の攻撃の強さの上限を決める、加熱領域・凍結領域の威力 1 あたりの強さ。 */
    public static final float PARTNER_ATTACK_POWER_PER_POWER = 4.0F;
    /** 雷雲の攻撃の強さの上限を決める、荷電領域の威力 1 あたりの強さ。 */
    public static final float CHARGED_ATTACK_POWER_PER_POWER = 4.0F;
    /** 範囲の中の風力発電機の風量を計算し直す間隔（tick）。 */
    public static final int WIND_COUNT_INTERVAL = 20;
    /** 雲が 1 tick に縮む半径（仮）。 */
    private static final float CLOUD_SHRINK_PER_TICK = 0.15F;
    /** そらさない魔法の弾の mod。 */
    private static final Set<String> SPELL_PROJECTILE_MODS = Set.of("psi", "psitweaks", "ars_nouveau", "irons_spellbooks");

    /** 辺の色: 風の薄緑。 */
    public static final int OUTLINE_COLOR = 0x9CF2C4;

    /** 1 か所あたりの強さ（そらす角度・組み合わせの上乗せ・風力の倍率）に使う威力（{@link #MAX_EFFECT_POWER} まで）。 */
    private float power;
    /** 入れた威力（10 より上もそのまま。重なりの強さと、風力の上乗せを許す台数に使う）。 */
    private float rawPower;
    /**
     * 風を受ける機械ごとの、この領域から受ける風量（機械のブロックエンティティの位置 → 風量と上限）。{@link #WIND_COUNT_INTERVAL}
     * tick ごとに計算し直す。サーバー側だけ。
     */
    private Map<BlockPos, WindShare> generatorWind = Map.of();

    /** 風を受ける機械 1 台が、この領域から受ける風量 {@code units} と、重なった乱流から合わせて受け取れる上限 {@code cap}（WU）。 */
    private record WindShare(double units, double cap) {
    }
    /** もう曲げた飛び道具（1 つにつき 1 回だけ曲げる）。サーバー側だけ。 */
    private final Set<UUID> deflected = new HashSet<>();

    public EntityTurbulenceZone(EntityType<? extends EntityTurbulenceZone> type, Level level) {
        super(type, level);
    }

    public EntityTurbulenceZone(Level level) {
        this(PsitweaksEntities.TURBULENCE_ZONE.get(), level);
    }

    /** 置く前に呼ぶ。 */
    public void configureWind(float power) {
        this.rawPower = Math.max(0.0F, power);
        this.power = Math.min(rawPower, MAX_EFFECT_POWER);
        SpellGramIndex.touch();
    }

    /** 同じ種類が重なったところでは、威力の一番高いもの（10 より上も比べる）だけが効く。 */
    @Override
    protected double stackStrength() {
        return rawPower;
    }

    public float getPower() {
        return power;
    }

    /** 組み合わせ（熱乱流・吹雪・暴風雨・雷雲）のダメージに使う、この乱流の強さ（{@link #attackPower(float, int)}）。 */
    public float attackPower() {
        return attackPower(getPlacementCost(), getLifetimeTicks());
    }

    /**
     * 乱流の攻撃の強さ: 1 秒あたりのコスト ÷ {@link #ATTACK_COST_PER_SECOND}（{@link #MAX_ATTACK_POWER} まで）。
     * ダメージはコストに比例する。熱量・風量などのリソースには関わらない。
     */
    public static float attackPower(float placementCost, int lifetimeTicks) {
        if (lifetimeTicks <= 0) {
            return 0.0F;
        }
        return Math.min(MAX_ATTACK_POWER, placementCost * 20.0F / lifetimeTicks / ATTACK_COST_PER_SECOND);
    }

    /** 乱流の攻撃の強さを、相方の領域の威力（{@link #MAX_EFFECT_POWER} まで）× {@code perPower} で頭打ちにした強さ。 */
    public static float limitedAttackPower(float attackPower, float partnerPower, float perPower) {
        return Math.min(attackPower, Math.min(partnerPower, MAX_EFFECT_POWER) * perPower);
    }

    /**
     * {@code level} のブロック {@code pos} の風を受ける機械（風力発電機・風車）が、吹いている乱流から受け取る風量（WU、サーバー側、
     * 術者は問わない）。吹いていないか、風を受ける機械でなければ 0。各乱流は自分の風量を範囲の中の機械で分け
     * （{@link #generatorWind}）、重なった乱流から受け取る WU は足し算。ただし 1 台が受け取れるのは合わせて機械の種類ごとの上限
     * （風力発電機は {@link #MAX_WU_PER_GENERATOR}、風車は {@link #MAX_WU_PER_WINDMILL}）まで。
     * 発電への足し方は {@code WindGeneratorTurbulenceMixin}（1 WU ごとに 37.5 J/t、高さによらない）と
     * {@code WindmillTurbulenceMixin}（1 WU ごとに 15 FE/t、天気・帆羽によらない）。
     * <p>
     * 機体（Sable のサブレベル、Create Aeronautics の機体など）の上の機械は、ブロックの本当の位置（機体用の空間）ではなく、
     * 今見えている世界の位置で範囲を見る。風量の表はブロックエンティティの本当の位置で引く（{@link #computeGeneratorWind} と同じ）。
     */
    public static double windUnitsAt(Level level, BlockPos pos) {
        List<EntityTurbulenceZone> zones = SpellGramIndex.all(level, EntityTurbulenceZone.class);
        if (zones.isEmpty()) {
            return 0.0;
        }
        BlockPos at = SableRangeCompat.worldBlockOf(level, pos);
        double units = 0.0;
        double cap = 0.0;
        for (EntityTurbulenceZone zone : zones) {
            if (!zone.isEnded() && zone.isInsideAndActive(at)) {
                WindShare share = zone.generatorWind.get(pos);
                if (share != null) {
                    units += share.units();
                    cap = share.cap();
                }
            }
        }
        return Math.min(cap, units);
    }

    /** 風力発電機 1 台が、重なった乱流から合わせて受け取れる WU（375 J/t = 150 FE/t ぶん）。 */
    public static final double MAX_WU_PER_GENERATOR = 10.0;
    /** IE の風車 1 台が、重なった乱流から合わせて受け取れる WU（60 FE/t ぶん）。 */
    public static final double MAX_WU_PER_WINDMILL = 4.0;

    /** {@code blockEntity} が風を受ける機械なら、1 台が受け取れる WU の上限。そうでなければ 0。 */
    private static double windCapOf(BlockEntity blockEntity) {
        if (MekanismGameplayCompat.isWindGenerator(blockEntity)) {
            return MAX_WU_PER_GENERATOR;
        }
        return ImmersiveEngineeringCompat.isWindReceivingWindmill(blockEntity) ? MAX_WU_PER_WINDMILL : 0.0;
    }

    /**
     * 風量（WU）: 範囲の床の広さ（輪郭の箱の X × Z）× 入れた威力 ÷ {@link #WIND_FOOTPRINT_DIVISOR} × {@link #windFactor}。
     * Mekanism がなくても持つ。威力が 10 を超えた分も風量に入る（1 台が受け取れる量は 10 WU のまま、受け取れる台数が増える）。
     */
    public double windAmount() {
        AABB box = getOutline();
        return box.getXsize() * box.getZsize() * rawPower / WIND_FOOTPRINT_DIVISOR * windFactor();
    }

    /**
     * 風の倍率: 置いた時点の事象干渉力（上限 4）。干渉力 1 で 1 倍（コスト 1 Psi あたりの発電の上乗せは 25 ÷ 64 J/t）、2 で 2 倍。
     * 加熱・荷電の総量（干渉力 2 で基準、上限 2 倍）とは基準が違う。上限は速さ（{@link SpellGramZone#machineSpeedFor}）が
     * 頭打ちになる干渉力にそろえ、速さから戻す。
     */
    public double windFactor() {
        return getMachineSpeed() / MACHINE_SPEED_PER_INTERFERENCE;
    }


    /** 照準の表示: 風量（WU）。Mekanism がなくても出す。 */
    @Override
    protected double resourcePerTick() {
        return windAmount();
    }

    @Override
    public String resourceKey() {
        return "psitweaks.hud.zone_resource.wind";
    }

    /**
     * 照準の表示: 風を受ける機械に渡している WU。1 台が受け取れるのは重なった乱流と合わせて種類ごとの上限までなので、合わせて
     * 上限を超える機械では、この領域の分を「上限 ÷ 合計」倍にして数える。
     * 風を受ける機械がなければ 0。
     */
    @Override
    protected double resourceUsedPerTick() {
        double used = 0.0;
        for (Map.Entry<BlockPos, WindShare> entry : generatorWind.entrySet()) {
            double total = 0.0;
            for (EntityTurbulenceZone zone : SpellGramIndex.all(level(), EntityTurbulenceZone.class)) {
                if (!zone.isEnded()) {
                    WindShare share = zone.generatorWind.get(entry.getKey());
                    total += share == null ? 0.0 : share.units();
                }
            }
            double units = entry.getValue().units();
            double cap = entry.getValue().cap();
            used += total > cap ? units * cap / total : units;
        }
        return used;
    }

    /** 風を受ける機械 {@code pos}（ブロックエンティティの位置）がこの領域から受ける風量（WU、GameTest 用）。 */
    public double generatorWindAt(BlockPos pos) {
        WindShare share = generatorWind.get(pos);
        return share == null ? 0.0 : share.units();
    }

    /**
     * 風を受ける機械ごとの風量を計算し直す: 範囲の中の風を受ける機械（Mekanism の風力発電機と IE の風車）の台数で風量を均等に
     * 割る。1 台がこの領域から受けるのは種類ごとの上限まで（{@link #windCapOf}）で、上限に届いた台の余りはほかの台で分け直す
     * （上限の小さい台から順に配る）。全員が届いて余った風は使われない。機械の範囲の判定は世界の位置（機体の上の機械は、見えている
     * 世界の位置）。
     */
    private void computeGeneratorWind() {
        Map<BlockPos, Double> caps = new java.util.LinkedHashMap<>();
        for (ZoneRegion region : regions()) {
            for (BlockEntity blockEntity : machinesIn(region)) {
                BlockPos pos = blockEntity.getBlockPos();
                if (caps.containsKey(pos) || !isInsideAndActive(region.worldPos(pos))) {
                    continue;
                }
                double cap = windCapOf(blockEntity);
                if (cap > 0.0) {
                    caps.put(pos, cap);
                }
            }
        }
        if (caps.isEmpty()) {
            generatorWind = Map.of();
            return;
        }
        List<Map.Entry<BlockPos, Double>> byCap = new java.util.ArrayList<>(caps.entrySet());
        byCap.sort(Map.Entry.comparingByValue());
        double left = windAmount();
        int remaining = byCap.size();
        Map<BlockPos, WindShare> wind = new HashMap<>();
        for (Map.Entry<BlockPos, Double> entry : byCap) {
            double share = Math.min(entry.getValue(), left / remaining);
            wind.put(entry.getKey(), new WindShare(share, entry.getValue()));
            left -= share;
            remaining--;
        }
        generatorWind = wind;
    }

    /**
     * {@code level} のブロック {@code pos} に乱流領域の風が吹いているか（サーバー側、術者は問わない）。機体の上のブロックは、
     * 今見えている世界の位置で見る。
     */
    public static boolean blowsAt(Level level, BlockPos pos) {
        BlockPos at = SableRangeCompat.worldBlockOf(level, pos);
        for (EntityTurbulenceZone zone : SpellGramIndex.all(level, EntityTurbulenceZone.class)) {
            if (!zone.isEnded() && zone.isInsideAndActive(at)) {
                return true;
            }
        }
        return false;
    }

    /**
     * クライアント側の見た目用: ブロック {@code pos} が、どれかの乱流領域の範囲に入っているか（奪い合い・領域干渉は見ない）。
     * 機体の上のブロックは見ない。
     */
    public static boolean coversOnClient(Level level, BlockPos pos) {
        AABB near = new AABB(pos).inflate(com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickZoneBase.MAX_SIZE);
        for (EntityTurbulenceZone zone : level.getEntitiesOfClass(EntityTurbulenceZone.class, near)) {
            if (!zone.isEnded() && zone.getArea().contains(pos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 術者 {@code casterId} の乱流領域の風が、ブロック {@code pos} に吹いているか（サーバー側）。重ねたときの強化
     * （熱乱流・吹雪・暴風雨・雷雲と、Create の一括加工）は、同じ術者の領域どうしだけ。
     */
    public static boolean blowsFor(Level level, BlockPos pos, @Nullable UUID casterId) {
        for (SpellGramObject object : SpellGramIndex.ofCaster(level, casterId)) {
            if (object instanceof EntityTurbulenceZone zone && !zone.isEnded() && zone.isInsideAndActive(pos)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOutlineColor() {
        return OUTLINE_COLOR;
    }

    /** 中のエンティティを運ばない。効くのは飛び道具と雲だけ（{@link #onSpellGramTickServer}）。 */
    @Override
    protected boolean affects(Entity entity) {
        return false;
    }

    @Override
    protected void affect(Entity target) {
    }

    @Override
    protected void onSpellGramTickServer() {
        super.onSpellGramTickServer();
        if (isEnded() || getArea().isEmpty() || !(level() instanceof ServerLevel level)) {
            return;
        }
        if (tickCount % WIND_COUNT_INTERVAL == 1 && (MekanismCompat.isGeneratorsLoaded() || ImmersiveEngineeringCompat.isLoaded())) {
            computeGeneratorWind();
        }
        List<Entity> inside = level.getEntities(this, getArea().bounds(),
                entity -> (entity instanceof Projectile || entity instanceof AreaEffectCloud) && entity.isAlive()
                        && isInsideAndActive(BlockPos.containing(entity.getBoundingBox().getCenter())));
        for (Entity entity : inside) {
            if (entity instanceof Projectile projectile) {
                deflect(level, projectile);
            } else if (entity instanceof AreaEffectCloud cloud && cloud.getType() == EntityType.AREA_EFFECT_CLOUD) {
                disperse(cloud);
            }
        }
    }

    /** 他者の飛び道具の向きを、入ってきたときに 1 回だけランダムに曲げる（速さは変えない）。 */
    private void deflect(ServerLevel level, Projectile projectile) {
        if (isSpellProjectile(projectile) || CasterIds.matches(projectile.getOwner(), getCasterUuid())
                || !deflected.add(projectile.getUUID())) {
            return;
        }
        Vec3 velocity = projectile.getDeltaMovement();
        double speed = velocity.length();
        if (speed < 1.0E-3) {
            return;
        }
        double maxAngle = Math.toRadians(Math.min(90.0, power * DEFLECT_DEGREES_PER_POWER));
        double angle = maxAngle * (0.5 + 0.5 * random.nextDouble());
        // 進む向きに垂直なランダムな向きへ、angle だけ倒す。
        Vec3 forward = velocity.scale(1.0 / speed);
        Vec3 side = forward.cross(new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()));
        if (side.lengthSqr() < 1.0E-6) {
            return;
        }
        side = side.normalize();
        Vec3 turned = forward.scale(Math.cos(angle)).add(side.scale(Math.sin(angle))).scale(speed);
        projectile.setDeltaMovement(turned);
        projectile.hasImpulse = true;
        float yaw = (float) (Mth.atan2(turned.x, turned.z) * Mth.RAD_TO_DEG);
        float pitch = (float) (Mth.atan2(turned.y, turned.horizontalDistance()) * Mth.RAD_TO_DEG);
        projectile.setYRot(yaw);
        projectile.setXRot(pitch);
        level.sendParticles(ParticleTypes.CLOUD, projectile.getX(), projectile.getY(), projectile.getZ(), 4,
                0.1, 0.1, 0.1, 0.02);
    }

    private static boolean isSpellProjectile(Projectile projectile) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(projectile.getType());
        return SPELL_PROJECTILE_MODS.contains(id.getNamespace());
    }

    /** 残留ポーション・ドラゴンブレスの雲を早く縮める。 */
    private static void disperse(AreaEffectCloud cloud) {
        float radius = cloud.getRadius() - CLOUD_SHRINK_PER_TICK;
        if (radius <= 0.5F) {
            cloud.discard();
        } else {
            cloud.setRadius(radius);
        }
    }

    @Override
    protected void onEnd(EndReason reason) {
        super.onEnd(reason);
        deflected.clear();
    }

    /** 竜巻の煙の色: 熱乱流（オレンジ）・乱流＋湿潤（青）・吹雪（水色）・雷雲（暗い紫がかった灰色）。 */
    private static final float[] HEAT_SMOKE = {1.0F, 0.55F, 0.16F};
    private static final float[] SOAK_SMOKE = {0.25F, 0.5F, 1.0F};
    private static final float[] FREEZE_SMOKE = {0.62F, 0.92F, 1.0F};
    private static final float[] CHARGE_SMOKE = {0.42F, 0.4F, 0.56F};
    /** 煙の色の割合（{@link #stormShare}）を測り直す間隔（tick）。あいだは前の結果を使う。 */
    private static final int STORM_SHARE_REFRESH_TICKS = 10;

    /** 煙の色の割合（熱乱流・乱流＋湿潤・吹雪・雷雲の順、クライアント側）。まだ測っていなければ null。 */
    @org.jetbrains.annotations.Nullable
    private double[] stormShares;
    private int stormSharesTick;

    @Override
    protected void spawnClientParticles() {
        if (power <= 0.0F) {
            return;
        }
        // 竜巻: 下が細く上が広い漏斗の形で、威力が大きいほど速く回る。
        AABB box = getOutline();
        double swirl = 0.25 + 0.035 * power;
        int count = ZoneParticles.count(box, 40.0, 4, 14);
        // 同じ術者のほかの領域と重なっているとき（熱乱流・乱流＋湿潤・吹雪）: 重なっている体積の割合だけ、煙をその色にする。
        if (stormShares == null || tickCount - stormSharesTick >= STORM_SHARE_REFRESH_TICKS) {
            stormShares = new double[]{stormShare(box, EntityHeatZone.class), stormShare(box, EntitySoakZone.class),
                    stormShare(box, EntityFreezeZone.class), stormShare(box, EntityChargedZone.class)};
            stormSharesTick = tickCount;
        }
        double heat = stormShares[0];
        double soak = stormShares[1];
        double freeze = stormShares[2];
        double charge = stormShares[3];
        double total = heat + soak + freeze + charge;
        if (total > 1.0) {
            heat /= total;
            soak /= total;
            freeze /= total;
            charge /= total;
        }
        int heatCount = (int) Math.round(count * heat);
        int soakCount = (int) Math.round(count * soak);
        int freezeCount = (int) Math.round(count * freeze);
        int chargeCount = (int) Math.round(count * charge);
        int white = Math.max(0, count - heatCount - soakCount - freezeCount - chargeCount);
        ZoneParticles.vortex(level(), random, box, particleCount(white), ParticleTypes.CLOUD, swirl, 0.06);
        tintedVortex(box, heatCount, HEAT_SMOKE, swirl, 0.08);
        tintedVortex(box, soakCount, SOAK_SMOKE, swirl, 0.05);
        tintedVortex(box, freezeCount, FREEZE_SMOKE, swirl, 0.04);
        tintedVortex(box, chargeCount, CHARGE_SMOKE, swirl, 0.03);
        if (random.nextInt(3) == 0) {
            ZoneParticles.vortex(level(), random, box, particleCount(2), ParticleTypes.WHITE_SMOKE, swirl * 1.2, 0.04);
        }
    }

    private void tintedVortex(AABB box, int count, float[] color, double swirl, double rise) {
        int spawned = particleCount(count);
        if (spawned > 0) {
            ZoneParticles.vortex(level(), random, box, spawned,
                    ColorParticleOption.create(PsitweaksParticles.TINTED_CLOUD.get(), color[0], color[1], color[2]), swirl, rise);
        }
    }

    /**
     * クライアント側の演出用: 範囲 {@code box} のうち、乱流と重なって強まっている（{@link SpellGramZone#isStorm}）
     * {@code type} の領域と重なっている体積の割合（0〜1）。
     */
    private double stormShare(AABB box, Class<? extends SpellGramZone> type) {
        double volume = box.getXsize() * box.getYsize() * box.getZsize();
        if (volume <= 0.0) {
            return 0.0;
        }
        double overlapped = 0.0;
        for (SpellGramZone zone : level().getEntitiesOfClass(type, box.inflate(1.0))) {
            if (zone.isStorm()) {
                AABB overlap = ZoneAttacks.overlap(box, zone.getOutline());
                if (overlap != null) {
                    overlapped += overlap.getXsize() * overlap.getYsize() * overlap.getZsize();
                }
            }
        }
        return Math.min(1.0, overlapped / volume);
    }

    @Override
    protected void writeZoneSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeFloat(power);
    }

    @Override
    protected void readZoneSpawnData(RegistryFriendlyByteBuf buffer) {
        power = buffer.readFloat();
    }
}
