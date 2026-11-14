package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.common.compat.CreateCompat;
import com.moratan251.psitweaks.common.compat.MekanismGameplayCompat;
import com.moratan251.psitweaks.common.compat.SableRangeCompat;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.spells.SpellBlockProtection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * 作動式: 加熱領域（仮）の範囲。中の生物に火をつける（中にいる間はバニラの炎上と同じ毎秒 1、出てからも 威力 × 0.6 秒は
 * 燃える。濡れている生物は燃えない、{@code CombatStatuses}。{@link #affect}）。術者自身は燃やさない（{@link #sparesCaster}）。
 * <p>
 * 範囲の中の氷と薄氷を水に戻す（ネザーのような水の置けないディメンションでは消える。氷塊・青氷はそのまま）。
 * <p>
 * Create があるときは、範囲の中の落ちているアイテムと、デポ・ベルトの上のアイテムを一括燻製する（Create のケース入りファンと
 * 同じで上限なし、速さは置いた時点の事象干渉力、{@link SpellGramZone#machineSpeedFor}）。
 * <p>
 * 熱の総量（2026-11-14 ユーザー）: 領域は 1 tick あたり決まった量の熱を持つ（{@link #heatTotalPerTick}: 置いたときのコスト ×
 * {@link #HEAT_PER_PSI} ÷ 存続時間 × 速さ）。燃料の代わりになるもの（消耗品・エネルギー）はこの総量から払う: Create の
 * ブレイズバーナーの加熱（1 台 {@link #BURNER_HEAT_PER_TICK}）と、残りを Mekanism の熱を受け取るブロックで均等に分ける。熱は
 * 威力 × 100 K までしか上げない（核分裂炉が損傷する 1200 K を越えさせない）。
 * <p>
 * 同じ術者の凍結領域と 1 つの直方体を二分すると、氷炎地獄の「灼熱」側になる（{@link InfernoZones}、隠し要素）。
 * 灼熱の間は、Create の加工が一括燻製から一括製錬に変わり（2026-11-11 ユーザー）、熱の総量は相手の凍結領域のコストの分も
 * 足したものになる。極寒側が Mekanism の機械から奪った熱は、次の tick に灼熱側の Mekanism の機械へ送る（{@link #acceptMovedHeat}、
 * バーナーには使わない。2026-11-15 ユーザー、案 A: 前の「熱の総量 4 倍」を置き換えた）。
 * <p>
 * Create のブレイズバーナーは、熱の総量で払えるぶん（{@link #BURNER_INTERVAL} tick ごとに、領域の中心に近い順に決める）だけ、
 * 燃料がなくても「加熱」、灼熱の中なら「極度の加熱」（1 台 {@link #SUPERHEATED_BURNER_HEAT_PER_TICK}、ブレイズケーキの代わり）
 * になる（{@link #burnerHeatAt}）。自分の燃料ですでにその段のバーナーは払わない。2026-11-14 に、前の「極度の加熱 1 台ごとに
 * 耐久値が余分に減る」を置き換えた。
 * <p>
 * 同じ術者の乱流領域と重なったブロックは「熱乱流」（ヒート・ストーム）になる（2026-11-11 ユーザー）: 中の生物に
 * {@link #STORM_INTERVAL} tick ごとに火属性のダメージ（攻撃術式の共通ルール、設定 {@code heat_storm.damageMultiplier}）、
 * Create の加工の速さと Mekanism へ送る熱の総量が 2 倍。
 * <p>
 * 凍結領域と重なったブロックでは、加熱と凍結が打ち消し合ってどちらも効かない（術者は問わない。乱流・湿潤との組み合わせより
 * 先に判定するので、熱乱流・吹雪も起きない。2026-11-11 ユーザー）。
 */
public class EntityHeatZone extends SpellGramZone {
    /** 威力 1 あたりの、範囲を出たあと生物が燃え続ける長さ（秒、仮）。威力 5 で 3 秒。 */
    public static final float BURN_SECONDS_PER_POWER = 0.6F;
    /** 威力 1 あたりの、Mekanism の機械を温める上限の温度（K、仮）。威力 10 で 1000 K（核分裂炉が損傷する 1200 K より下）。 */
    public static final double TEMPERATURE_PER_POWER = 100.0;
    /** 威力の既定値（置く前に設定しなかったとき）。 */
    public static final float DEFAULT_POWER = 5.0F;
    /**
     * 熱の総量の比率: 置いたときのコスト 1 Psi あたりの熱（J、2026-11-15 ユーザー）。熱発電機は熱 1 J をおよそ 0.28 FE にする
     * （ゲーム内の測定: 木炭 1 個を薪木ストーブで 320 kJ → 89.6 kFE）ので、サイリンク発電機と同じ 100 FE/Psi になる値。
     * 威力 5・サイズ 16（コスト 562）の速さ 1 で約 337 J/t。
     */
    public static final double HEAT_PER_PSI = 360.0;
    /**
     * ブレイズバーナー 1 台を「加熱」にするのに熱の総量から使う熱（J/t、2026-11-15 ユーザー）: 木炭 1 個は薪木ストーブで 320 kJ、
     * バーナーでは 1600 tick 加熱なので、木炭と同じ 200 J/t。
     */
    public static final double BURNER_HEAT_PER_TICK = 200.0;
    /**
     * ブレイズバーナー 1 台を「極度の加熱」にするのに使う熱（J/t、加熱の 2 倍、2026-11-15 ユーザー）。ブレイズケーキの溶岩 250 mB は
     * 約 1 MJ（3200 tick で約 310 J/t）なので、材料の手間を足して加熱の 2 倍。
     */
    public static final double SUPERHEATED_BURNER_HEAT_PER_TICK = 400.0;
    private static final int INTERVAL = 10;
    private static final int MAX_EMBERS_PER_TICK = 5;

    /** 辺の色: 炎の橙。 */
    public static final int OUTLINE_COLOR = 0xFF7A2E;
    /** 氷炎地獄の灼熱の辺の色: 赤。 */
    public static final int INFERNO_OUTLINE_COLOR = 0xFF2A10;
    /** 熱乱流の辺の色: 明るい黄。 */
    public static final int STORM_OUTLINE_COLOR = 0xFFD23A;
    /** 灼熱で当たった生物が燃え続ける長さ（秒）。 */
    private static final float INFERNO_BURN_SECONDS = 6.0F;
    /** 熱乱流のダメージの間隔（tick）。 */
    public static final int STORM_INTERVAL = 10;
    /** 熱乱流で当たった生物が燃え続ける長さ（秒）。 */
    private static final float STORM_BURN_SECONDS = 4.0F;
    /** 熱の総量で払うブレイズバーナーを決め直す間隔（tick）。 */
    public static final int BURNER_INTERVAL = 10;

    /** 熱の総量から払って加熱しているブレイズバーナー（バーナーのブロックの位置 → 1 加熱 / 2 極度の加熱）。サーバー側だけ。 */
    private final Map<BlockPos, Integer> poweredBurners = new HashMap<>();
    /** そのバーナーが使っている熱（J/t）。 */
    private double burnerHeatUse;
    /** 氷炎地獄の極寒側から移された熱（J）。次の tick に灼熱側の Mekanism の機械へ送る。サーバー側だけ。 */
    private double movedHeat;
    /** 移された熱の合計（J、GameTest 用）。 */
    private double movedHeatReceived;

    public EntityHeatZone(EntityType<? extends EntityHeatZone> type, Level level) {
        super(type, level);
    }

    public EntityHeatZone(Level level) {
        this(PsitweaksEntities.HEAT_ZONE.get(), level);
    }

    /** 威力（領域の温度、2026-11-11 ユーザー）。燃やす長さ・Mekanism の上限温度・氷炎地獄の強さを決める。 */
    /** 1 体・1 か所あたりの強さに使う威力（{@link #MAX_EFFECT_POWER} まで）。 */
    private float power = DEFAULT_POWER;
    /** 入れた威力（10 より上もそのまま。コストと重なりの強さに使う）。 */
    private float rawPower = DEFAULT_POWER;

    /** 置く前に呼ぶ。 */
    public void setPower(float power) {
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

    /**
     * Create のブレイズバーナー（2026-11-11 の設計 B、{@code BlazeBurnerHeatZoneMixin} から呼ぶ）: ブロック {@code pos} の
     * バーナーを、加熱領域が熱の総量から払って加熱していれば 1（加熱）、極度の加熱なら 2、どちらでもなければ 0（サーバー側）。
     * 払う台数・打ち消し・領域干渉・保護は {@link #allocateBurners} で決める。重なっていれば強いほう。
     */
    public static int burnerHeatAt(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) {
            return 0;
        }
        List<EntityHeatZone> zones = SpellGramIndex.all(level, EntityHeatZone.class);
        if (zones.isEmpty()) {
            return 0;
        }
        // 機体（Sable のサブレベル）の上のバーナーは、今見えている世界の位置で領域を探す（保護は本当の位置で確かめる）。
        BlockPos at = SableRangeCompat.worldBlockOf(level, pos);
        int heat = 0;
        for (EntityHeatZone zone : zones) {
            if (zone.isEnded() || !zone.isInsideAndActive(at)) {
                continue;
            }
            Integer powered = zone.poweredBurners.get(pos);
            if (powered != null) {
                heat = Math.max(heat, powered);
            }
        }
        return heat;
    }

    /** 範囲の中の生物が燃え続ける長さ（秒）: 威力 × 0.6。 */
    public float burnSeconds() {
        return power * BURN_SECONDS_PER_POWER;
    }

    /** Mekanism の熱: これより熱いブロックには送らない（K）: 威力 × 100。 */
    public double maxTemperature() {
        return power * TEMPERATURE_PER_POWER;
    }

    /** 凍結領域と重なったブロックでは効かない（打ち消し合う）。 */
    @Override
    protected Class<? extends SpellGramZone> cancelledBy() {
        return EntityFreezeZone.class;
    }

    @Override
    public int getOutlineColor() {
        return isInferno() ? INFERNO_OUTLINE_COLOR : isStorm() ? STORM_OUTLINE_COLOR : OUTLINE_COLOR;
    }

    @Override
    protected int effectInterval() {
        return INTERVAL;
    }

    @Override
    protected boolean sparesCaster() {
        return true;
    }

    /**
     * 中にいる生物に火をつける（{@link #INTERVAL} tick ごと）。ダメージはバニラの炎上そのもの（毎秒 1、2026-11-14 ユーザー）。
     * <p>
     * バニラの炎上は、燃える残り時間が 20 の倍数になった tick にだけ 1 ダメージを与える。残り時間が {@link #keepBurning}
     * より短くなったら 20 tick 単位で足す（20 で割った余りを変えない）ので、中にいる間は 20 tick ごとにちょうど 1 回当たる。
     * 外に出たあとは、残りの 威力 × 0.6 秒ほど同じように燃え続ける。
     * <p>
     * 前は残り時間を「威力 × 12 tick」に戻していたので、10 tick の間に 20 の倍数を通る威力でだけ毎秒 2、通らない威力
     * （1・3・6・8）では 0 だった。
     */
    @Override
    protected void affect(Entity target) {
        if (target instanceof LivingEntity living) {
            int remaining = living.getRemainingFireTicks();
            int keep = keepBurning();
            if (remaining < keep) {
                living.setRemainingFireTicks(Math.max(0, remaining) + 20 * Mth.positiveCeilDiv(keep - Math.max(0, remaining), 20));
            }
        }
    }

    /** 外に出たあと燃え続ける長さ（tick）: 威力 × 0.6 秒。 */
    public int burnTicks() {
        return Mth.floor(burnSeconds() * 20.0F);
    }

    /**
     * 中にいる間、燃える残り時間をこれより短くしない（tick）: 威力 × 0.6 秒。ただし次に足すまで（{@link #INTERVAL} tick）
     * 火が消えない長さは保つ。
     */
    private int keepBurning() {
        return Math.max(burnTicks(), INTERVAL + 1);
    }

    @Override
    protected void onSpellGramTickServer() {
        super.onSpellGramTickServer();
        if (isEnded() || getArea().isEmpty() || !(level() instanceof ServerLevel level)) {
            return;
        }
        if (tickCount % InfernoZones.INTERVAL == 0) {
            InfernoZones.link(level, this);
            InfernoZones.showHints(level, this);
            if (isInfernoAt(level.getGameTime())) {
                InfernoZones.harm(level, this, PsitweaksDamageTypes.INFERNO_HEAT,
                        target -> target.igniteForSeconds(INFERNO_BURN_SECONDS));
            }
        }
        if (tickCount % STORM_INTERVAL == 0) {
            refreshStorm();
            if (isStorm()) {
                ZoneAttacks.harmScaled(level, this, PsitweaksDamageTypes.HEAT_STORM,
                        PsitweaksConfig.COMMON.heatStormDamageMultiplier.get(),
                        pos -> STORM_DAMAGE_PER_POWER * stormPowerAt(pos), target -> true,
                        target -> target.igniteForSeconds(STORM_BURN_SECONDS));
            }
        }
        if (!(getCaster() instanceof Player caster)) {
            return;
        }
        boolean inferno = isInfernoAt(level.getGameTime());
        boolean storm = isStorm();
        // 世界の範囲と、範囲に重なった機体ごとの範囲（機体の機械にも効かせる）。範囲・領域干渉・乱流は世界の位置で判定し、
        // 保護は機械が本当にある位置で確かめる。
        List<ZoneRegion> regions = regions();
        for (int step = takeMachineSteps(); step > 0; step--) {
            for (ZoneRegion region : regions) {
                CreateCompat.processZone(level, region.area(), machinesIn(region), fanProcess(inferno),
                        pos -> isActiveAt(region.worldPos(pos)) && isStrongestAt(region.worldPos(pos)),
                        pos -> isActiveAt(region.worldPos(pos)) && isStrongestAt(region.worldPos(pos)) && mayUseMachine(level, caster, pos));
            }
        }
        if (storm) {
            // 熱乱流のところは、乱流の威力に応じて上乗せ（威力 10 で 2 倍の速さ）。そのブロックで最も強い乱流のぶんだけ。
            forEachStormStep(getMachineSpeed(), (turbulence, steps) -> {
                for (int i = 0; i < steps; i++) {
                    for (ZoneRegion region : regions) {
                        CreateCompat.processZone(level, region.area(), machinesIn(region), fanProcess(inferno),
                                pos -> stormAt(region.worldPos(pos)) == turbulence && isStrongestAt(region.worldPos(pos)),
                                pos -> stormAt(region.worldPos(pos)) == turbulence && isStrongestAt(region.worldPos(pos)) && mayUseMachine(level, caster, pos));
                    }
                }
            });
        }
        if (tickCount % BURNER_INTERVAL == 0) {
            allocateBurners(level, caster, regions, inferno);
        }
        double total = heatTotalPerTick(inferno);
        double heat = total - burnerHeatUse;
        double moved = movedHeat;
        movedHeat = 0.0;
        if (heat > 0.0 || storm || moved > 0.0) {
            // 世界の範囲と機体の範囲の機械をまとめ、総量を一度に分ける（範囲ごとに総量を払うと、機体に重ねるほど増えてしまう）。
            Map<BlockPos, ZoneRegion> owners = new HashMap<>();
            List<BlockEntity> machines = new ArrayList<>();
            for (ZoneRegion region : regions) {
                for (BlockEntity blockEntity : machinesIn(region)) {
                    if (owners.putIfAbsent(blockEntity.getBlockPos(), region) == null) {
                        machines.add(blockEntity);
                    }
                }
            }
            if (!machines.isEmpty()) {
                java.util.function.Predicate<BlockPos> mayHeat = pos -> {
                    ZoneRegion region = owners.get(pos);
                    return region != null && isActiveAt(region.worldPos(pos)) && isStrongestAt(region.worldPos(pos))
                            && mayUseMachine(level, caster, pos);
                };
                if (heat > 0.0) {
                    MekanismGameplayCompat.supplyZoneHeat(level, machines, heatSideCache(), heat, maxTemperature(), mayHeat);
                }
                if (moved > 0.0) {
                    // 氷炎地獄: 極寒側で奪った熱を、灼熱側の機械へ（受け取りきれない分は消える）。
                    MekanismGameplayCompat.supplyZoneHeat(level, machines, heatSideCache(), moved, maxTemperature(), mayHeat);
                }
                if (storm) {
                    // 熱乱流のところは、乱流の威力に応じて総量を上乗せ（威力 10 で 2 倍）。そのブロックで最も強い乱流のぶんだけ。
                    for (EntityTurbulenceZone turbulence : overlappingStorms()) {
                        MekanismGameplayCompat.supplyZoneHeat(level, machines, heatSideCache(), total * stormBoost(turbulence.getPower()),
                                maxTemperature(), pos -> {
                                    ZoneRegion region = owners.get(pos);
                                    return region != null && stormAt(region.worldPos(pos)) == turbulence
                                            && isStrongestAt(region.worldPos(pos)) && mayUseMachine(level, caster, pos);
                                });
                    }
                }
            }
        }
    }

    /** 払うブレイズバーナー 1 台（バーナーの位置、世界の位置、1 加熱 / 2 極度の加熱）。 */
    private record Burner(BlockPos pos, BlockPos worldPos, int heat) {
    }

    /**
     * 熱の総量から、加熱するブレイズバーナーを決め直す（{@link #BURNER_INTERVAL} tick ごと）。自分の燃料でまだその段にないバーナーを、
     * 領域の中心に近い順に、総量で払えるだけ（加熱 {@link #BURNER_HEAT_PER_TICK}、灼熱の中は極度の加熱
     * {@link #SUPERHEATED_BURNER_HEAT_PER_TICK}）。残りの総量は Mekanism の機械に回る。
     */
    private void allocateBurners(ServerLevel level, Player caster, List<ZoneRegion> regions, boolean inferno) {
        poweredBurners.clear();
        burnerHeatUse = 0.0;
        if (!CreateCompat.isCreateLoaded()) {
            return;
        }
        int wanted = inferno ? 2 : 1;
        List<Burner> burners = new ArrayList<>();
        for (ZoneRegion region : regions) {
            for (BlockEntity blockEntity : machinesIn(region)) {
                if (blockEntity.isRemoved()) {
                    continue;
                }
                int own = CreateCompat.burnerOwnHeat(blockEntity);
                if (own < 0 || own >= wanted) {
                    continue;
                }
                BlockPos pos = blockEntity.getBlockPos();
                BlockPos world = region.worldPos(pos);
                if (isActiveAt(world) && isStrongestAt(world) && mayUseMachine(level, caster, pos)) {
                    burners.add(new Burner(pos, world, wanted));
                }
            }
        }
        if (burners.isEmpty()) {
            return;
        }
        BlockPos center = getArea().center();
        burners.sort(java.util.Comparator.comparingDouble(burner -> burner.worldPos().distSqr(center)));
        double budget = heatTotalPerTick(inferno);
        double cost = inferno ? SUPERHEATED_BURNER_HEAT_PER_TICK : BURNER_HEAT_PER_TICK;
        for (Burner burner : burners) {
            if (burnerHeatUse + cost > budget + 1.0E-9) {
                break;
            }
            poweredBurners.put(burner.pos(), burner.heat());
            burnerHeatUse += cost;
        }
        if (inferno && !poweredBurners.isEmpty()) {
            // 隠し進捗（Create があるときだけ読み込まれる）。
            InfernoZones.award(level, caster, com.moratan251.psitweaks.common.handler.PsitweaksAdvancements.INFERNO_SUPERHEAT);
        }
    }

    /** 熱の総量で今加熱しているブレイズバーナーの数（GameTest 用）。 */
    public int poweredBurnerCount() {
        return poweredBurners.size();
    }

    /**
     * 1 tick あたりの熱の総量（J）: 置いたときのコスト × {@link #HEAT_PER_PSI} ÷ 存続時間 × 速さ。灼熱の間は、相手の凍結領域の
     * 同じ式の分を足す（2 つの領域のコストの合計）。
     */
    public double heatTotalPerTick(boolean inferno) {
        double total = heatTotalPerTick(getPlacementCost(), getLifetimeTicks(), getMachineSpeed());
        if (inferno && infernoPartner() instanceof EntityFreezeZone cold) {
            total += heatTotalPerTick(cold.getPlacementCost(), cold.getLifetimeTicks(), cold.getMachineSpeed());
        }
        return total;
    }

    /** 氷炎地獄の極寒側が奪った熱 {@code heat}（J）を受け取る。次の tick に灼熱側の Mekanism の機械へ送る。 */
    void acceptMovedHeat(double heat) {
        if (heat > 0.0) {
            movedHeat += heat;
            movedHeatReceived += heat;
        }
    }

    /** 極寒側から移された熱の合計（J、GameTest 用）。 */
    public double movedHeatReceived() {
        return movedHeatReceived;
    }

    /**
     * 置いたときのコスト {@code placementCost}（時間の分を含む）、存続時間 {@code lifetimeTicks}、速さ {@code machineSpeed} の領域の
     * 1 tick あたりの熱の総量（J）。コストも存続時間も「時間」に比例するので、1 tick あたりの量は時間によらない。
     */
    public static double heatTotalPerTick(float placementCost, int lifetimeTicks, float machineSpeed) {
        if (lifetimeTicks <= 0) {
            return 0.0;
        }
        return placementCost * HEAT_PER_PSI / lifetimeTicks * machineSpeed;
    }

    /** Create の加工: ふだんは一括燻製、氷炎地獄の灼熱の間は一括製錬。 */
    private static CreateCompat.FanProcess fanProcess(boolean inferno) {
        return inferno ? CreateCompat.FanProcess.BLASTING : CreateCompat.FanProcess.SMOKING;
    }

    @Override
    protected boolean affectsBlocks() {
        return true;
    }

    @Override
    protected boolean mayAffectState(BlockState state) {
        return state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE);
    }

    /** 氷と薄氷を溶かす（氷が自然に溶けるときと同じ）。 */
    @Override
    protected void affectBlock(ServerLevel level, Player caster, BlockPos pos, BlockState state) {
        if (!state.is(Blocks.ICE) && !state.is(Blocks.FROSTED_ICE)) {
            return;
        }
        if (!SpellBlockProtection.mayChange(level, caster, pos)) {
            return;
        }
        if (level.dimensionType().ultraWarm()) {
            level.removeBlock(pos, false);
        } else {
            level.setBlockAndUpdate(pos, IceBlock.meltsInto());
            level.neighborChanged(pos, IceBlock.meltsInto().getBlock(), pos);
        }
    }

    @Override
    protected void spawnClientParticles() {
        if (isStorm()) {
            // 熱乱流: 炎の竜巻が速く回りながら昇る。
            for (AABB storm : stormBoxes()) {
                int count = ZoneParticles.count(storm, 25.0, 4, 14);
                ZoneParticles.vortex(level(), random, storm, count, ParticleTypes.FLAME, 0.4, 0.12);
                ZoneParticles.vortex(level(), random, storm, Math.max(1, count / 3), ParticleTypes.SMALL_FLAME, 0.5, 0.15);
                ZoneParticles.vortex(level(), random, storm, Math.max(1, count / 4), ParticleTypes.LARGE_SMOKE, 0.3, 0.1);
                if (random.nextInt(4) == 0) {
                    ZoneParticles.vortex(level(), random, storm, 1, ParticleTypes.LAVA, 0.2, 0.2);
                }
            }
        }
        // Create があるとき: ケース入りファンを焚き火に当てたときと同じ一括燻製のパーティクル。
        // 機体の上のデポ・ベルトにも出す（機体用の空間に出したパーティクルは、Sable が見えている場所へ移す）。
        for (ZoneRegion region : regions()) {
            CreateCompat.spawnProcessingParticles(level(), region.area(), machinesIn(region), fanProcess(isInferno()));
        }
        AABB box = getOutline();
        double volume = box.getXsize() * box.getYsize() * box.getZsize();
        int embers = (int) Math.min(MAX_EMBERS_PER_TICK, Math.max(1, volume / 150.0)) * (isInferno() ? 4 : 1);
        for (int i = 0; i < embers; i++) {
            double x = box.minX + random.nextDouble() * box.getXsize();
            double y = box.minY + random.nextDouble() * box.getYsize();
            double z = box.minZ + random.nextDouble() * box.getZsize();
            if (getArea().contains(BlockPos.containing(x, y, z))) {
                if (isInferno()) {
                    level().addParticle(random.nextInt(4) == 0 ? ParticleTypes.LAVA : ParticleTypes.FLAME,
                            x, y, z, 0.0, 0.05, 0.0);
                } else {
                    level().addParticle(random.nextInt(3) == 0 ? ParticleTypes.SMOKE : ParticleTypes.SMALL_FLAME,
                            x, y, z, 0.0, 0.02, 0.0);
                }
            }
        }
    }
}
