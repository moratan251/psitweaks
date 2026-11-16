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
 * 作動式: 加熱領域（仮）の範囲。
 * <ul>
 *   <li>中の生物に火をつける（{@link #affect}）。術者自身は燃やさない（{@link #sparesCaster}）。</li>
 *   <li>氷と薄氷を水に戻す（{@link #affectBlock}）。</li>
 *   <li>1 tick あたりの熱の総量（{@link #heatTotalPerTick}）で Create のブレイズバーナーを加熱し（{@link #allocateBurners}）、
 *   残りを Mekanism の熱を受け取るブロックに均等に分ける（温度は {@link #maxTemperature} まで）。</li>
 *   <li>Create があれば、落ちているアイテムとデポ・ベルトの上のアイテムを一括燻製する（{@link #processCreate}）。自分の荷電領域が
 *   重なったところは一括製錬（電気炉）。</li>
 * </ul>
 * 自分の側の凍結領域とは打ち消し合う（{@link #cancelledBy}）。同じ術者の乱流領域と重なると熱乱流になる。同じ術者の凍結領域と
 * 面で触れ合うか重なっていると、凍結領域が運んできた熱を受け取り、熱量に足す（{@link ZoneHeatExchange}、{@link #acceptMovedHeat}）。
 * 仕様の全体と数値の根拠は {@code docs/1.21.1/spellgram-zones.md}。
 */
public class EntityHeatZone extends SpellGramZone {
    /** 威力 1 あたりの、範囲を出たあと生物が燃え続ける長さ（秒、仮）。威力 5 で 3 秒。 */
    public static final float BURN_SECONDS_PER_POWER = 0.6F;
    /** Mekanism の機械を温める上限の温度の基準（K）。上限 = この値 + 威力 × {@link #MAX_TEMPERATURE_PER_POWER}。 */
    public static final double MAX_TEMPERATURE_BASE = 500.0;
    /**
     * 威力 1 あたりの、Mekanism の機械を温める上限の温度の上がり幅（K）。威力 1 で 550 K、威力 10 で 1000 K（核分裂炉が
     * 損傷する 1200 K より下）。周囲の温度によらない。
     */
    public static final double MAX_TEMPERATURE_PER_POWER = 50.0;
    /** 威力の既定値（置く前に設定しなかったとき）。 */
    public static final float DEFAULT_POWER = 5.0F;
    /** 熱の総量の比率: 置いたときのコスト 1 Psi あたりの熱（J）。熱発電機を通すとサイリンク発電機と同じ 100 FE/Psi になる。 */
    public static final double HEAT_PER_PSI = 360.0;
    /** ブレイズバーナー 1 台を「加熱」にするのに熱の総量から使う熱（J/t、木炭で加熱したときと同じ）。 */
    public static final double BURNER_HEAT_PER_TICK = 200.0;
    private static final int INTERVAL = 10;
    private static final int MAX_EMBERS_PER_TICK = 5;

    /** 辺の色: 炎の橙。 */
    public static final int OUTLINE_COLOR = 0xFF7A2E;
    /** 熱乱流の辺の色: 明るい黄。 */
    public static final int STORM_OUTLINE_COLOR = 0xFFD23A;
    /** 熱乱流のダメージの間隔（tick）。 */
    public static final int STORM_INTERVAL = 10;
    /** 熱乱流で当たった生物が燃え続ける長さ（秒）。 */
    private static final float STORM_BURN_SECONDS = 4.0F;
    /** 熱の総量で払うブレイズバーナーを決め直す間隔（tick）。 */
    public static final int BURNER_INTERVAL = 10;

    /** 熱の総量から払って「加熱」にしているブレイズバーナー（バーナーのブロックの位置）。サーバー側だけ。 */
    private final java.util.Set<BlockPos> poweredBurners = new java.util.HashSet<>();
    /** そのバーナーが使っている熱（J/t）。 */
    private double burnerHeatUse;
    /** 触れ合っている凍結領域が運んできた熱を、{@link #BURNER_INTERVAL} tick ぶんためたもの（J）。サーバー側だけ。 */
    private double movedHeatWindow;
    /** 直前の {@link #BURNER_INTERVAL} tick に運ばれてきた熱の 1 tick あたり（J/t）。熱量に足す。サーバー側だけ。 */
    private double movedHeatPerTick;
    /** 運ばれてきた熱の合計（J、GameTest 用）。 */
    private double movedHeatReceived;
    /** 受け渡しの演出: 直前に「運んでいる」演出を出したか（運び始めの共鳴音を 1 回だけ鳴らすため）。サーバー側だけ。 */
    private boolean carryingHint;
    /** 受け渡しの演出: 湯気の音をもう鳴らした、重なっている自分の凍結領域（エンティティの番号）。サーバー側だけ。 */
    private final java.util.Set<Integer> steamedOverlaps = new java.util.HashSet<>();

    public EntityHeatZone(EntityType<? extends EntityHeatZone> type, Level level) {
        super(type, level);
    }

    public EntityHeatZone(Level level) {
        this(PsitweaksEntities.HEAT_ZONE.get(), level);
    }

    /** 1 体・1 か所あたりの強さに使う威力（{@link #MAX_EFFECT_POWER} まで）。燃やす長さ・上限の温度を決める。 */
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
     * Create のブレイズバーナー（{@code BlazeBurnerHeatZoneMixin} から呼ぶ）: ブロック {@code pos} のバーナーを、加熱領域が
     * 熱の総量から払って「加熱」にしているか（サーバー側）。払う台数・打ち消し・領域干渉・保護は {@link #allocateBurners} で
     * 決める。領域からは極度の加熱にしない（2026-11-16 ユーザー）。
     */
    public static boolean isBurnerPowered(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        List<EntityHeatZone> zones = SpellGramIndex.all(level, EntityHeatZone.class);
        if (zones.isEmpty()) {
            return false;
        }
        // 機体（Sable のサブレベル）の上のバーナーは、今見えている世界の位置で領域を探す（保護は本当の位置で確かめる）。
        BlockPos at = SableRangeCompat.worldBlockOf(level, pos);
        for (EntityHeatZone zone : zones) {
            if (!zone.isEnded() && zone.poweredBurners.contains(pos) && zone.isInsideAndActive(at)) {
                return true;
            }
        }
        return false;
    }

    /** 範囲の中の生物が燃え続ける長さ（秒）: 威力 × 0.6。 */
    public float burnSeconds() {
        return power * BURN_SECONDS_PER_POWER;
    }

    /** Mekanism の熱: これより熱いブロックには送らない（K）: {@link #maxTemperatureFor}。 */
    public double maxTemperature() {
        return maxTemperatureFor(power);
    }

    /** 威力 {@code power} の加熱領域が Mekanism の機械を温める上限の温度（K）: 500 + 威力 × 50（威力 10 で頭打ち）。 */
    public static double maxTemperatureFor(float power) {
        return MAX_TEMPERATURE_BASE + Math.max(0.0F, Math.min(power, MAX_EFFECT_POWER)) * MAX_TEMPERATURE_PER_POWER;
    }

    /** 凍結領域と重なったブロックでは効かない（打ち消し合う）。 */
    @Override
    protected Class<? extends SpellGramZone> cancelledBy() {
        return EntityFreezeZone.class;
    }

    @Override
    public int getOutlineColor() {
        return isStorm() ? STORM_OUTLINE_COLOR : OUTLINE_COLOR;
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
     * 中にいる生物に火をつける（{@link #INTERVAL} tick ごと）。ダメージはバニラの炎上そのもの（毎秒 1）。
     * <p>
     * バニラの炎上は、燃える残り時間が 20 の倍数になった tick にだけ当たる。残り時間は 20 tick 単位で足して 20 で割った余りを
     * 変えないので、中にいる間は 20 tick ごとにちょうど 1 回当たる。残り時間を決まった値に戻すと、威力によって毎秒 0 回や
     * 2 回になる。
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
        if (tickCount % ZoneHeatExchange.INTERVAL == 0) {
            ZoneHeatExchange.showHints(level, this);
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
        boolean storm = isStorm();
        // 世界の範囲と、範囲に重なった機体ごとの範囲（機体の機械にも効かせる）。範囲・領域干渉・乱流は世界の位置で判定し、
        // 保護は機械が本当にある位置で確かめる。
        List<ZoneRegion> regions = regions();
        // 自分の荷電領域が重なったところは一括製錬（電気炉）。
        List<EntityChargedZone> arcs = CreateCompat.isCreateLoaded() ? ownChargedZones(level) : List.of();
        for (int step = takeMachineSteps(); step > 0; step--) {
            for (ZoneRegion region : regions) {
                processCreate(level, caster, region, arcs,
                        world -> isActiveAt(world) && isStrongestAt(world));
            }
        }
        if (storm) {
            // 熱乱流のところは、乱流の威力に応じて上乗せ（威力 10 で 2 倍の速さ）。そのブロックで最も強い乱流のぶんだけ。
            forEachStormStep(getMachineSpeed(), (turbulence, steps) -> {
                for (int i = 0; i < steps; i++) {
                    for (ZoneRegion region : regions) {
                        processCreate(level, caster, region, arcs,
                                world -> stormAt(world) == turbulence && isStrongestAt(world));
                    }
                }
            });
        }
        if (tickCount % BURNER_INTERVAL == 0) {
            // 触れ合っている凍結領域が運んできた熱を、直前の間隔でならして熱の総量に入れる（バーナーを決める前に）。
            movedHeatPerTick = movedHeatWindow / BURNER_INTERVAL;
            movedHeatWindow = 0.0;
            allocateBurners(level, caster, regions);
        }
        double total = heatTotalPerTick();
        double heat = total - burnerHeatUse;
        // 照準の表示: 使っている熱 = バーナーに払った分 + Mekanism の機械に渡した分（熱乱流の上乗せは入れない）。
        double used = burnerHeatUse;
        if (heat > 0.0 || storm) {
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
                // 重なった自分の加熱領域は、それぞれが自分の熱量を配る（足し算）。上限の温度は領域ごと。
                java.util.function.Predicate<BlockPos> mayHeat = pos -> {
                    ZoneRegion region = owners.get(pos);
                    return region != null && isActiveAt(region.worldPos(pos)) && mayUseMachine(level, caster, pos);
                };
                if (heat > 0.0) {
                    used += MekanismGameplayCompat.supplyZoneHeat(level, machines, heatSideCache(), heat, maxTemperature(), mayHeat);
                }
                if (storm) {
                    // 熱乱流のところは、乱流の威力に応じて総量を上乗せ（威力 10 で 2 倍）。そのブロックで最も強い乱流のぶんだけ。
                    // 上乗せは乱流が範囲を覆う割合を掛ける（小さな乱流を重ねても増えないように）。運ばれてきた熱には掛けない。
                    for (Map.Entry<EntityTurbulenceZone, Double> entry : stormCoverage().entrySet()) {
                        EntityTurbulenceZone turbulence = entry.getKey();
                        MekanismGameplayCompat.supplyZoneHeat(level, machines, heatSideCache(),
                                ownHeatTotalPerTick() * stormBoost(turbulence.getPower()) * entry.getValue(),
                                maxTemperature(), pos -> {
                                    ZoneRegion region = owners.get(pos);
                                    return region != null && stormAt(region.worldPos(pos)) == turbulence
                                            && mayUseMachine(level, caster, pos);
                                });
                    }
                }
            }
        }
        recordResourceUse(Math.min(total, used));
    }

    /** 払うブレイズバーナー 1 台（バーナーの位置、世界の位置）。 */
    private record Burner(BlockPos pos, BlockPos worldPos) {
    }

    /**
     * 熱の総量から、加熱するブレイズバーナーを決め直す（{@link #BURNER_INTERVAL} tick ごと）。自分の燃料ではまだ加熱になっていない
     * バーナーを、領域の中心に近い順に、総量で払えるだけ（1 台 {@link #BURNER_HEAT_PER_TICK}）。残りの総量は Mekanism の機械に回る。
     */
    private void allocateBurners(ServerLevel level, Player caster, List<ZoneRegion> regions) {
        poweredBurners.clear();
        burnerHeatUse = 0.0;
        if (!CreateCompat.isCreateLoaded()) {
            return;
        }
        List<Burner> burners = new ArrayList<>();
        for (ZoneRegion region : regions) {
            for (BlockEntity blockEntity : machinesIn(region)) {
                if (blockEntity.isRemoved()) {
                    continue;
                }
                int own = CreateCompat.burnerOwnHeat(blockEntity);
                if (own != 0) {
                    continue;
                }
                BlockPos pos = blockEntity.getBlockPos();
                BlockPos world = region.worldPos(pos);
                // 重なった自分の加熱領域どうしでは、ほかの領域がもう払っているバーナーは払わない（二度払いをしない）。
                if (isActiveAt(world) && mayUseMachine(level, caster, pos) && !paidByAnother(level, pos)) {
                    burners.add(new Burner(pos, world));
                }
            }
        }
        if (burners.isEmpty()) {
            return;
        }
        BlockPos center = getArea().center();
        burners.sort(java.util.Comparator.comparingDouble(burner -> burner.worldPos().distSqr(center)));
        double budget = heatTotalPerTick();
        for (Burner burner : burners) {
            if (burnerHeatUse + BURNER_HEAT_PER_TICK > budget + 1.0E-9) {
                break;
            }
            poweredBurners.add(burner.pos());
            burnerHeatUse += BURNER_HEAT_PER_TICK;
        }
    }

    /** ほかの加熱領域が、バーナー {@code pos} をもう払っているか。 */
    private boolean paidByAnother(ServerLevel level, BlockPos pos) {
        for (EntityHeatZone other : SpellGramIndex.all(level, EntityHeatZone.class)) {
            if (other != this && !other.isEnded() && other.poweredBurners.contains(pos)) {
                return true;
            }
        }
        return false;
    }

    /** 熱の総量で今加熱しているブレイズバーナーの数（GameTest 用）。 */
    public int poweredBurnerCount() {
        return poweredBurners.size();
    }

    /**
     * 1 tick あたりの熱の総量（J）: 自分の分（{@link #ownHeatTotalPerTick}）と、触れ合っている凍結領域が運んできた熱（直前の
     * {@link #BURNER_INTERVAL} tick の平均）。ブレイズバーナーを先に払い、残りを Mekanism の機械に配る。
     */
    public double heatTotalPerTick() {
        return ownHeatTotalPerTick() + movedHeatPerTick;
    }

    /**
     * 魔法式が生む熱の総量（J/t）: 置いたときのコスト × {@link #HEAT_PER_PSI} ÷ 存続時間 × 速さ。運ばれてきた熱は入れない
     * （熱乱流の上乗せはこれにだけ掛ける）。
     */
    public double ownHeatTotalPerTick() {
        return heatTotalPerTick(getPlacementCost(), getLifetimeTicks(), getMachineSpeed());
    }

    /** 照準の表示: 熱量（J/t、運ばれてきた熱も足したもの）。 */
    @Override
    protected double resourcePerTick() {
        return heatTotalPerTick();
    }

    /** 照準の表示の 2 行め: 受け取り（熱量に入っている、運ばれてきた熱、J/t）。 */
    @Override
    protected double resourceMovedPerTick() {
        return movedHeatPerTick;
    }

    @Override
    public String resourceMovedKey() {
        return "psitweaks.hud.zone_resource.received";
    }

    /** 直前の {@link #BURNER_INTERVAL} tick に、触れ合っている凍結領域から運ばれてきた熱の 1 tick あたり（J/t、サーバー側）。 */
    public double receivedHeatPerTick() {
        return movedHeatPerTick;
    }

    /** 受け渡しの演出で、今「運んでいる」演出を出しているか（サーバー側、GameTest 用）。 */
    public boolean showsCarryingHint() {
        return carryingHint;
    }

    void setCarryingHint(boolean carrying) {
        carryingHint = carrying;
    }

    java.util.Set<Integer> steamedOverlaps() {
        return steamedOverlaps;
    }

    @Override
    public String resourceKey() {
        return "psitweaks.hud.zone_resource.heat";
    }

    /** 触れ合っている凍結領域が運んできた熱 {@code heat}（J）を受け取る。ためて、次の間隔から熱の総量に足す。 */
    void acceptMovedHeat(double heat) {
        if (heat > 0.0) {
            movedHeatWindow += heat;
            movedHeatReceived += heat;
        }
    }

    /** 凍結領域から運ばれてきた熱の合計（J、GameTest 用）。 */
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

    /** 電気炉の放電 1 本あたりの、火花を置く間隔（ブロック）。 */
    private static final double ARC_STEP = 0.18;
    /** 電気炉の放電の、横へのぶれの大きさ（ブロック）。 */
    private static final double ARC_JITTER = 0.14;

    /**
     * 電気炉（自分の加熱領域に荷電領域が重なり、一括製錬になるところ、2026-11-15 ユーザー）の演出（クライアント側）。地表の上を
     * ぎざぎざの放電が走り、端から白く光る火の粉と炎が立つ。重なりの下が地面に埋まっていても見えるよう、列ごとに地表（重なりの
     * 中で、通れるブロックの下が固いところ）を探して、その上に出す。数は重なりの体積に応じて増やす（上限あり）。
     */
    private void spawnArcFurnaceParticles(AABB overlap) {
        double volume = overlap.getXsize() * overlap.getYsize() * overlap.getZsize();
        // 放電: 地表から 1.5 ブロックまでの高さで、2 点のあいだを火花でぎざぎざに結ぶ。
        int arcs = (int) Math.min(3, Math.max(1, volume / 150.0));
        for (int i = 0; i < arcs; i++) {
            if (random.nextInt(4) != 0) {
                continue;
            }
            double fromX = overlap.minX + random.nextDouble() * overlap.getXsize();
            double fromZ = overlap.minZ + random.nextDouble() * overlap.getZsize();
            double fromFloor = arcFloor(overlap, fromX, fromZ);
            if (Double.isNaN(fromFloor)) {
                continue;
            }
            double angle = random.nextDouble() * Math.PI * 2.0;
            double length = 1.0 + random.nextDouble() * 1.8;
            double toX = Mth.clamp(fromX + Math.cos(angle) * length, overlap.minX, overlap.maxX);
            double toZ = Mth.clamp(fromZ + Math.sin(angle) * length, overlap.minZ, overlap.maxZ);
            double toFloor = arcFloor(overlap, toX, toZ);
            if (Double.isNaN(toFloor)) {
                toFloor = fromFloor;
            }
            net.minecraft.world.phys.Vec3 from = new net.minecraft.world.phys.Vec3(fromX, fromFloor + 0.15 + random.nextDouble() * 1.3, fromZ);
            net.minecraft.world.phys.Vec3 to = new net.minecraft.world.phys.Vec3(toX, toFloor + 0.15 + random.nextDouble() * 1.3, toZ);
            int steps = Math.max(4, (int) Math.ceil(from.distanceTo(to) / ARC_STEP));
            for (int k = 0; k <= steps; k++) {
                double t = (double) k / steps;
                double jitter = k == 0 || k == steps ? 0.0 : ARC_JITTER;
                level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        Mth.lerp(t, from.x, to.x) + (random.nextDouble() - 0.5) * jitter,
                        Mth.lerp(t, from.y, to.y) + (random.nextDouble() - 0.5) * jitter,
                        Mth.lerp(t, from.z, to.z) + (random.nextDouble() - 0.5) * jitter,
                        0.0, 0.0, 0.0);
            }
            // 放電の両端: 白く光る火の粉と、小さな炎。
            level().addParticle(ParticleTypes.END_ROD, from.x, from.y, from.z, 0.0, 0.02, 0.0);
            level().addParticle(ParticleTypes.SMALL_FLAME, to.x, to.y, to.z, 0.0, 0.03, 0.0);
        }
        // 炉の熱: 地表から白い火の粉と炎がゆっくり昇る。
        int glows = (int) Math.min(4, Math.max(1, volume / 120.0));
        for (int i = 0; i < glows; i++) {
            double x = overlap.minX + random.nextDouble() * overlap.getXsize();
            double z = overlap.minZ + random.nextDouble() * overlap.getZsize();
            double floor = arcFloor(overlap, x, z);
            if (Double.isNaN(floor)) {
                continue;
            }
            double y = floor + random.nextDouble() * 0.6;
            if (random.nextInt(3) == 0) {
                level().addParticle(ParticleTypes.END_ROD, x, y, z, 0.0, 0.03, 0.0);
            } else {
                level().addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.04, 0.0);
            }
        }
    }

    /**
     * 列 ({@code x}, {@code z}) の、重なり {@code overlap} の中の地表の高さ: 上から見て、通れるブロック（当たり判定がない）の下が
     * 通れないブロックになる最初の高さ。列の全部が通れるなら重なりの底、全部が埋まっていれば NaN（出さない）。
     */
    private double arcFloor(AABB overlap, double x, double z) {
        int top = Mth.ceil(overlap.maxY) - 1;
        int bottom = Mth.floor(overlap.minY);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean openAbove = false;
        for (int y = top; y >= bottom; y--) {
            pos.set(Mth.floor(x), y, Mth.floor(z));
            boolean open = level().getBlockState(pos).getCollisionShape(level(), pos).isEmpty();
            if (!open && openAbove) {
                return y + 1.0;
            }
            openAbove = open;
        }
        return openAbove ? overlap.minY : Double.NaN;
    }

    /**
     * 範囲 {@code region} で Create の加工を 1 回進める。{@code where} は世界の位置で、加工してよいところ。自分の荷電領域
     * {@code arcs} が重なったところは一括製錬（電気炉）、ほかは一括燻製。
     */
    private void processCreate(ServerLevel level, Player caster, ZoneRegion region, List<EntityChargedZone> arcs,
                               java.util.function.Predicate<BlockPos> where) {
        if (arcs.isEmpty()) {
            CreateCompat.processZone(level, region.area(), machinesIn(region), CreateCompat.FanProcess.SMOKING,
                    pos -> where.test(region.worldPos(pos)),
                    pos -> where.test(region.worldPos(pos)) && mayUseMachine(level, caster, pos));
            return;
        }
        java.util.function.Predicate<BlockPos> arcAt = world -> {
            for (EntityChargedZone arc : arcs) {
                if (arc.isInsideAndActive(world)) {
                    return true;
                }
            }
            return false;
        };
        CreateCompat.processZone(level, region.area(), machinesIn(region), CreateCompat.FanProcess.SMOKING,
                pos -> where.test(region.worldPos(pos)) && !arcAt.test(region.worldPos(pos)),
                pos -> where.test(region.worldPos(pos)) && !arcAt.test(region.worldPos(pos)) && mayUseMachine(level, caster, pos));
        CreateCompat.processZone(level, region.area(), machinesIn(region), CreateCompat.FanProcess.BLASTING,
                pos -> where.test(region.worldPos(pos)) && arcAt.test(region.worldPos(pos)),
                pos -> where.test(region.worldPos(pos)) && arcAt.test(region.worldPos(pos)) && mayUseMachine(level, caster, pos));
    }

    /** 同じ術者の、終わっていない荷電領域（電気炉の判定用、サーバー側）。 */
    private List<EntityChargedZone> ownChargedZones(ServerLevel level) {
        java.util.UUID caster = getCasterUuid();
        if (caster == null) {
            return List.of();
        }
        List<EntityChargedZone> zones = new ArrayList<>();
        for (var object : SpellGramIndex.ofCaster(level, caster)) {
            if (object instanceof EntityChargedZone charged && !charged.isEnded()) {
                zones.add(charged);
            }
        }
        return zones;
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
        // Create があるとき: ケース入りファンを焚き火に当てたときと同じ一括燻製のパーティクル（荷電領域が重なったところは
        // 一括製錬のもの。クライアントでは術者を見分けない）。
        // 機体の上のデポ・ベルトにも出す（機体用の空間に出したパーティクルは、Sable が見えている場所へ移す）。
        if (CreateCompat.isCreateLoaded()) {
            List<EntityChargedZone> arcs = level().getEntitiesOfClass(EntityChargedZone.class, getOutline());
            // 電気炉（荷電領域が重なったところ）の専用の演出（2026-11-15 ユーザー）。
            for (EntityChargedZone arc : arcs) {
                AABB overlap = ZoneAttacks.overlap(getOutline(), arc.getOutline());
                if (overlap != null && overlap.getXsize() > 0.0 && overlap.getYsize() > 0.0 && overlap.getZsize() > 0.0) {
                    spawnArcFurnaceParticles(overlap);
                }
            }
            for (ZoneRegion region : regions()) {
                CreateCompat.spawnProcessingParticles(level(), region.area(), machinesIn(region), pos -> {
                    net.minecraft.world.phys.Vec3 at = net.minecraft.world.phys.Vec3.atCenterOf(region.worldPos(pos));
                    for (EntityChargedZone arc : arcs) {
                        if (arc.getOutline().contains(at)) {
                            return CreateCompat.FanProcess.BLASTING;
                        }
                    }
                    return CreateCompat.FanProcess.SMOKING;
                });
            }
        }
        AABB box = getOutline();
        double volume = box.getXsize() * box.getYsize() * box.getZsize();
        int embers = (int) Math.min(MAX_EMBERS_PER_TICK, Math.max(1, volume / 150.0));
        for (int i = 0; i < embers; i++) {
            double x = box.minX + random.nextDouble() * box.getXsize();
            double y = box.minY + random.nextDouble() * box.getYsize();
            double z = box.minZ + random.nextDouble() * box.getZsize();
            if (getArea().contains(BlockPos.containing(x, y, z))) {
                level().addParticle(random.nextInt(3) == 0 ? ParticleTypes.SMOKE : ParticleTypes.SMALL_FLAME,
                        x, y, z, 0.0, 0.02, 0.0);
            }
        }
    }
}
