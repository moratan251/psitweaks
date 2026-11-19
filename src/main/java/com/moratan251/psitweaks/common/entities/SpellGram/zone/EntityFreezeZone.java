package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.compat.MekanismGameplayCompat;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.spells.SpellBlockProtection;
import com.moratan251.psitweaks.common.spells.combat.CombatStatuses;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 作動式: 凍結領域（仮）の範囲。
 * <ul>
 *   <li>中の生物を、粉雪に埋まったときと同じように凍えさせる（{@link #freezeAmount}）。術者自身は凍えさせない
 *   （{@link #sparesCaster}）。</li>
 *   <li>ランダムに選んだブロックで、水源の水のうち、真上が空気で中にエンティティがいないものを氷にする（{@link #randomBlockTick}）。</li>
 *   <li>冷却量（{@link #coolingTotalPerTick}）の範囲で、Mekanism の熱を持つ設備を下限（275 − 威力 × 5 K）に向けて冷やす
 *   （{@link #coolMekanismMachines}）。</li>
 * </ul>
 * 自分の側の加熱領域とは打ち消し合う（{@link #cancelledBy}）。同じ術者の乱流領域と重なると吹雪になる。同じ術者の加熱領域と
 * 面で触れ合うか重なっていると、奪った熱のうち周囲の温度より上にあった分を、その加熱領域へ運ぶ（{@link ZoneHeatExchange}）。同じ熱源を
 * 冷やす自分の凍結領域のどれかが触れていれば、その熱源の熱は全部渡る（熱源ごとにまとめる）。凍結領域は熱を作らず、運ぶだけ。
 */
public class EntityFreezeZone extends SpellGramZone {
    private static final int INTERVAL = 10;
    /** 粉雪の外で {@link #INTERVAL} の間に抜ける凍えの値（毎 tick 2）。 */
    private static final int THAW_PER_INTERVAL = 20;
    private static final int MAX_FLAKES_PER_TICK = 5;

    /** 辺の色: 氷の水色。 */
    public static final int OUTLINE_COLOR = 0xB8F0FF;
    /** 吹雪の辺の色: 白。 */
    public static final int STORM_OUTLINE_COLOR = 0xFFFFFF;

    public EntityFreezeZone(EntityType<? extends EntityFreezeZone> type, Level level) {
        super(type, level);
    }

    public EntityFreezeZone(Level level) {
        this(PsitweaksEntities.FREEZE_ZONE.get(), level);
    }

    /** 威力の既定値（置く前に設定しなかったとき）。 */
    public static final float DEFAULT_POWER = 5.0F;
    /**
     * Mekanism の冷却: 威力 1・機械を動かす速さ 1 あたりの、1 tick に温度差を縮める割合（仮）。威力 5・事象干渉力 2 で毎 tick 1%
     * （1500 K の設備が約 12 秒で 400 K）。
     */
    public static final double COOL_FRACTION_PER_POWER = 0.002;
    /** Mekanism の冷却: 1 tick に温度差を縮める割合の上限（仮）。 */
    public static final double MAX_COOL_FRACTION = 0.5;
    /**
     * 冷却量の比率: 置いたときのコスト 1 Psi あたりに取り除ける熱（J）。熱の出し入れを 1 つの物差しで扱えるよう、加熱領域の
     * {@link EntityHeatZone#HEAT_PER_PSI} と同じ。
     */
    public static final double COOLING_PER_PSI = EntityHeatZone.HEAT_PER_PSI;
    /** Mekanism の機械を冷やせる下限の温度の基準（K）。下限 = この値 − 威力 × {@link #MIN_TEMPERATURE_PER_POWER}。 */
    public static final double MIN_TEMPERATURE_BASE = 275.0;
    /**
     * 威力 1 あたりの、冷やせる下限の下げ幅（K）。威力 1 で 270 K、威力 10 で 225 K（約 −48℃）。周囲の温度によらない。
     * 加熱領域の「500 + 威力 × 50 K まで温める」と対になる、1 か所あたりの強さ（威力 10 で頭打ち）。
     */
    public static final double MIN_TEMPERATURE_PER_POWER = 5.0;
    /** 威力 1 あたりの、{@link #INTERVAL} ごとの凍えの差し引きの増え（仮）。威力 5 で 20（3.5 秒で凍えきる）。 */
    public static final int NET_FREEZE_PER_POWER = 4;

    /**
     * 威力（冷やす強さ）。凍えのたまる速さ・1 台あたりの冷える割合・冷やせる下限を決める。1 体・1 か所
     * あたりの強さに使うので {@link #MAX_EFFECT_POWER} まで。
     */
    private float power = DEFAULT_POWER;
    /** 入れた威力（10 より上もそのまま。コストと重なりの強さに使う）。 */
    private float rawPower = DEFAULT_POWER;
    /**
     * 面で触れ合っているか重なっている同じ術者の加熱領域と、つながりの広さ（{@link ZoneHeatExchange#linkArea}）。
     * {@link ZoneHeatExchange#INTERVAL} tick ごとに作り直す。サーバー側だけで、保存も同期もしない。
     */
    private Map<EntityHeatZone, Double> heatContacts = Map.of();
    /**
     * 熱を奪った熱源（{@link MekanismGameplayCompat.ZoneHeatDrain#carriedBySource} のキー）: 今の間隔のものと、1 つ前の間隔のもの。
     * ほかの自分の凍結領域が、同じ熱源を冷やしているかを見るのに使う。サーバー側だけ。
     */
    private Set<Object> cooledSources = newSourceSet();
    private Set<Object> previousCooledSources = Set.of();
    /** 熱源ごとの渡し先と割合（{@link ZoneHeatExchange#route}）。渡し先があるものだけ、次に作り直すまで覚えておく。 */
    private final Map<Object, Map<EntityHeatZone, Double>> heatRoutes = new IdentityHashMap<>();
    /** 触れ合いと渡し先をまだ一度も作っていないか（置いた直後の tick にすぐ作る）。 */
    private boolean exchangeStale = true;
    /** 加熱領域へ運んだ熱の合計（J、GameTest 用）。 */
    private double heatCarried;
    /** 運べたが、渡し先がなくて捨てた熱の合計（J、GameTest 用）。 */
    private double heatDiscarded;

    /**
     * Mekanism の冷却で、1 台が 1 tick に下限との温度差を縮める割合の上限: 威力 × 0.002 × 機械を動かす速さ（置いた時点の
     * 事象干渉力）、上限 0.5。
     */
    public static double coolFractionPerTick(float power, float machineSpeed) {
        double fraction = Math.max(0.0F, power) * COOL_FRACTION_PER_POWER * Math.max(0.0F, machineSpeed);
        return Math.min(MAX_COOL_FRACTION, fraction);
    }

    /** 威力 {@code power} の凍結領域が Mekanism の機械を冷やせる下限の温度（K）: 275 − 威力 × 5（威力 10 で頭打ち）。 */
    public static double minTemperatureFor(float power) {
        return MIN_TEMPERATURE_BASE - Math.max(0.0F, Math.min(power, MAX_EFFECT_POWER)) * MIN_TEMPERATURE_PER_POWER;
    }

    /** Mekanism の機械を冷やせる下限の温度（K）: {@link #minTemperatureFor}。 */
    public double minTemperature() {
        return minTemperatureFor(power);
    }

    /** 冷却量: 1 tick に取り除ける熱の総量（J）: 置いたときのコスト × {@link #COOLING_PER_PSI} ÷ 存続時間 × 速さ。 */
    public double coolingTotalPerTick() {
        return coolingTotalPerTick(getPlacementCost(), getLifetimeTicks(), getMachineSpeed());
    }

    /** 照準の表示: 冷却量（J/t）。 */
    @Override
    protected double resourcePerTick() {
        return coolingTotalPerTick();
    }

    @Override
    public String resourceKey() {
        return "psitweaks.hud.zone_resource.cooling";
    }

    /** 照準の表示の 2 行め: 運搬（奪った熱のうち、実際に加熱領域へ渡った分、J/t）。 */
    @Override
    public String resourceMovedKey() {
        return "psitweaks.hud.zone_resource.carried";
    }

    /** 冷却量を、置いたときのコスト・存続時間・速さから求める。時間によらない。 */
    public static double coolingTotalPerTick(float placementCost, int lifetimeTicks, float machineSpeed) {
        if (lifetimeTicks <= 0) {
            return 0.0;
        }
        return placementCost * COOLING_PER_PSI / lifetimeTicks * machineSpeed;
    }

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

    /** 加熱領域と重なったブロックでは効かない（打ち消し合う）。 */
    @Override
    protected Class<? extends SpellGramZone> cancelledBy() {
        return EntityHeatZone.class;
    }

    @Override
    public int getOutlineColor() {
        return isStorm() ? STORM_OUTLINE_COLOR : OUTLINE_COLOR;
    }

    @Override
    protected void onSpellGramTickServer() {
        super.onSpellGramTickServer();
        if (!isEnded() && tickCount % INTERVAL == 0 && level() instanceof ServerLevel level) {
            refreshStorm();
            if (isStorm()) {
                // 吹雪: 同じ術者の乱流領域と重なったところ。
                ZoneAttacks.harmScaled(level, this, PsitweaksDamageTypes.BLIZZARD,
                        PsitweaksConfig.COMMON.blizzardDamageMultiplier.get(),
                        pos -> STORM_DAMAGE_PER_POWER * comboAttackPowerAt(pos, getPower(),
                                EntityTurbulenceZone.PARTNER_ATTACK_POWER_PER_POWER), target -> true, target -> {
                        });
            }
        }
        // 熱を運ぶ先（面で触れ合っている自分の加熱領域）と、熱源ごとの渡し先を作り直す。Mekanism がなければ運ぶ熱がないので
        // 探さない。
        if ((exchangeStale || tickCount % ZoneHeatExchange.INTERVAL == 0) && level() instanceof ServerLevel level) {
            exchangeStale = false;
            boolean active = !isEnded() && MekanismCompat.isMekanismLoaded();
            heatContacts = active ? ZoneHeatExchange.heatContacts(level, this) : Map.of();
            previousCooledSources = active ? cooledSources : Set.of();
            cooledSources = newSourceSet();
            heatRoutes.clear();
        }
        coolMekanismMachines();
    }

    private static Set<Object> newSourceSet() {
        return Collections.newSetFromMap(new IdentityHashMap<>());
    }

    /** 面で触れ合っている自分の加熱領域と、触れ合う面の広さ（サーバー側、{@link ZoneHeatExchange#route} が読む）。 */
    Map<EntityHeatZone, Double> heatContacts() {
        return heatContacts;
    }

    /** この凍結領域が、今か 1 つ前の間隔に熱源 {@code source} から熱を奪ったか（サーバー側）。 */
    boolean coolsSource(Object source) {
        return cooledSources.contains(source) || previousCooledSources.contains(source);
    }

    /**
     * Mekanism の熱を持つ設備を、下限（{@link #minTemperature}）に向けて冷やす（毎 tick）。加熱領域と打ち消し合っているブロック・
     * 領域干渉で止められたブロック・術者が機械を使えないブロックは冷やさない。吹雪のブロックは乱流の威力に応じて上乗せ。
     * 奪った熱のうち周囲の温度より上にあった分は、熱源ごとに、その熱源を冷やす自分の凍結領域が触れている加熱領域へ、
     * 触れ合う面の広さに比例して運ぶ。
     */
    private void coolMekanismMachines() {
        if (isEnded() || getArea().isEmpty() || !(level() instanceof ServerLevel level)
                || !(getCaster() instanceof Player caster) || !MekanismCompat.isMekanismLoaded()) {
            return;
        }
        double fraction = coolFractionPerTick(power, getMachineSpeed());
        if (fraction <= 0.0) {
            return;
        }
        double cap = coolingTotalPerTick();
        // 世界の範囲と、範囲に重なった機体ごとの範囲（機体の機械も冷やす）をまとめ、冷却量を一度に分ける。範囲・領域干渉・乱流は
        // 世界の位置で判定し、保護は機械が本当にある位置で確かめる。
        java.util.Map<BlockPos, ZoneRegion> owners = new java.util.HashMap<>();
        List<BlockEntity> machines = new java.util.ArrayList<>();
        for (ZoneRegion region : regions()) {
            for (BlockEntity blockEntity : machinesIn(region)) {
                if (owners.putIfAbsent(blockEntity.getBlockPos(), region) == null) {
                    machines.add(blockEntity);
                }
            }
        }
        if (machines.isEmpty()) {
            return;
        }
        // 重なった自分の凍結領域は、それぞれが自分の冷却量で冷やす（足し算）。下限は 275 − 威力 × 5 K。
        double floor = minTemperature();
        MekanismGameplayCompat.ZoneHeatDrain drained = MekanismGameplayCompat.drainZoneHeat(level, machines, heatSideCache(),
                fraction, cap, floor, pos -> {
                    ZoneRegion region = owners.get(pos);
                    return region != null && isActiveAt(region.worldPos(pos)) && mayUseMachine(level, caster, pos);
                });
        // 照準の表示: 使っている冷却量（吹雪の上乗せは入れない）。
        recordResourceUse(drained.removed());
        Map<Object, Double> carried = new IdentityHashMap<>(drained.carriedBySource());
        if (isStorm()) {
            // 吹雪のところは、乱流の威力に応じて割合と冷却量を上乗せ（威力 10 で 2 倍）。冷却量の上乗せは乱流が範囲を覆う割合を
            // 掛ける（小さな乱流を重ねても増えないように）。
            for (java.util.Map.Entry<EntityTurbulenceZone, Double> entry : stormCoverage().entrySet()) {
                EntityTurbulenceZone turbulence = entry.getKey();
                double boost = stormBoost(turbulence.getPower());
                MekanismGameplayCompat.drainZoneHeat(level, machines, heatSideCache(),
                        Math.min(MAX_COOL_FRACTION, fraction * boost), cap * boost * entry.getValue(), floor, pos -> {
                            ZoneRegion region = owners.get(pos);
                            return region != null && stormAt(region.worldPos(pos)) == turbulence
                                    && mayUseMachine(level, caster, pos);
                        }).carriedBySource().forEach((source, amount) -> carried.merge(source, amount, Double::sum));
            }
        }
        carryHeat(level, carried);
    }

    /**
     * 熱源ごとの運べる熱 {@code carried}（J）を、渡し先（{@link ZoneHeatExchange#route}: その熱源を冷やす自分の凍結領域が
     * 触れている加熱領域、触れ合う面の広さに比例）へ減らさずに渡す。渡し先がなければ捨てる。
     */
    private void carryHeat(ServerLevel level, Map<Object, Double> carried) {
        for (Map.Entry<Object, Double> entry : carried.entrySet()) {
            Object source = entry.getKey();
            cooledSources.add(source);
            double amount = entry.getValue();
            if (amount <= 0.0) {
                continue;
            }
            Map<EntityHeatZone, Double> route = heatRoutes.get(source);
            if (route == null) {
                route = ZoneHeatExchange.route(level, this, source);
                // 渡し先がないものは覚えない（同じ熱源を冷やす凍結領域が、あとから触れ合うかもしれない）。
                if (!route.isEmpty()) {
                    heatRoutes.put(source, route);
                }
            }
            double given = 0.0;
            for (Map.Entry<EntityHeatZone, Double> share : route.entrySet()) {
                if (!share.getKey().isEnded()) {
                    double part = amount * share.getValue();
                    share.getKey().acceptMovedHeat(part);
                    given += part;
                }
            }
            heatCarried += given;
            heatDiscarded += amount - given;
            recordResourceMoved(given);
        }
    }

    /** 加熱領域へ運んだ熱の合計（J、GameTest 用）。 */
    public double heatCarried() {
        return heatCarried;
    }

    /** 運べたが、渡し先がなくて捨てた熱の合計（J、GameTest 用）。 */
    public double heatDiscarded() {
        return heatDiscarded;
    }

    @Override
    protected int effectInterval() {
        return INTERVAL;
    }

    @Override
    protected boolean sparesCaster() {
        return true;
    }

    @Override
    protected void affect(Entity target) {
        if (target instanceof LivingEntity living && living.canFreeze()) {
            // 凍えきった後も、次に足すまで（毎 tick 2 ずつ抜ける間）凍えきったままでいられる分だけ上乗せしておく。
            int cap = living.getTicksRequiredToFreeze() + INTERVAL * 2;
            living.setTicksFrozen(Math.min(cap, living.getTicksFrozen()
                    + freezeAmount(power, CombatStatuses.isSoaked(living),
                    stormPowerAt(BlockPos.containing(living.getBoundingBox().getCenter())))));
        }
    }

    /**
     * {@link #INTERVAL} ごとに足す凍えの値。差し引き（足す − 抜ける）は 凍結の威力 × 4 で、濡れていれば 2 倍、吹雪なら乱流の
     * 威力に応じて 1 + 威力 × 0.1 倍になる（凍結の威力 5 で: ふだん 40、濡れていれば 60、威力 10 の吹雪で 60、両方で 100）。
     */
    public static int freezeAmount(float power, boolean soaked, float stormPower) {
        double net = NET_FREEZE_PER_POWER * Math.max(0.0F, power) * (soaked ? 2 : 1) * (1.0 + stormBoost(stormPower));
        return THAW_PER_INTERVAL + (int) Math.round(net);
    }

    @Override
    protected boolean hasRandomBlockTicks() {
        return true;
    }

    @Override
    protected boolean mayRandomTick(BlockState state) {
        return state.is(Blocks.WATER) && state.getFluidState().isSource();
    }

    /**
     * ランダムに選ばれたブロック（{@link SpellGramZone#RANDOM_BLOCK_TICK_CHANCE}）が水源の水で、真上が空気で、中にエンティティが
     * いなければ氷にする。
     */
    @Override
    protected void randomBlockTick(ServerLevel level, Player caster, BlockPos pos, BlockPos world, BlockState state) {
        if (!state.is(Blocks.WATER) || !state.getFluidState().isSource() || !level.getBlockState(pos.above()).isAir()) {
            return;
        }
        BlockState ice = Blocks.ICE.defaultBlockState();
        if (!level.isUnobstructed(ice, pos, CollisionContext.empty())) {
            return;
        }
        if (!SpellBlockProtection.mayChange(level, caster, pos)) {
            return;
        }
        level.setBlockAndUpdate(pos, ice);
    }

    @Override
    protected void spawnClientParticles() {
        if (isStorm()) {
            // 吹雪: 雪が速く渦を巻いて吹き荒れる。
            for (AABB storm : stormBoxes()) {
                int count = ZoneParticles.count(storm, 20.0, 6, 18);
                ZoneParticles.vortex(level(), random, storm, particleCount(count), ParticleTypes.SNOWFLAKE, 0.55, 0.02);
                ZoneParticles.vortex(level(), random, storm, particleCount(Math.max(1, count / 3)), ParticleTypes.ITEM_SNOWBALL, 0.6, 0.05);
                ZoneParticles.vortex(level(), random, storm, particleCount(Math.max(1, count / 4)), ParticleTypes.CLOUD, 0.45, 0.03);
            }
        }
        AABB box = getOutline();
        double volume = box.getXsize() * box.getYsize() * box.getZsize();
        int flakes = particleCount((int) Math.min(MAX_FLAKES_PER_TICK, Math.max(1, volume / 150.0)));
        for (int i = 0; i < flakes; i++) {
            double x = box.minX + random.nextDouble() * box.getXsize();
            double y = box.minY + random.nextDouble() * box.getYsize();
            double z = box.minZ + random.nextDouble() * box.getZsize();
            if (getArea().contains(BlockPos.containing(x, y, z))) {
                level().addParticle(ParticleTypes.SNOWFLAKE, x, y, z, 0.0, -0.03, 0.0);
            }
        }
    }
}
