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

import java.util.List;

/**
 * 作動式: 加熱領域（仮）の範囲。中の生物に火をつける（出てからも 威力 × 0.6 秒は燃える。濡れている生物は
 * 燃えない、{@code CombatStatuses}）。術者自身は燃やさない（{@link #sparesCaster}）。
 * <p>
 * 範囲の中の氷と薄氷を水に戻す（ネザーのような水の置けないディメンションでは消える。氷塊・青氷はそのまま）。
 * <p>
 * Create があるときは、範囲の中の落ちているアイテムと、デポ・ベルトの上のアイテムを一括燻製する。Mekanism があるときは、
 * 範囲の中の熱を受け取るブロックに、薪木ストーブ 1 台ぶん（{@link #HEAT_PER_TICK}）の熱を毎 tick 送る。どちらも速さは
 * 置いた時点の事象干渉力で決まる（{@link SpellGramZone#machineSpeedFor}）。熱は 威力 × 100 K までしか
 * 上げない（核分裂炉が損傷する 1200 K を越えさせない）。
 * <p>
 * 同じ術者の凍結領域と 1 つの直方体を二分すると、氷炎地獄の「灼熱」側になる（{@link InfernoZones}、隠し要素）。
 * 灼熱の間は、Create の加工が一括燻製から一括製錬に変わり、Mekanism へ送る熱が 4 倍になる（2026-11-11 ユーザー）。
 * <p>
 * Create のブレイズバーナーは、範囲の中なら燃料がなくても「加熱」、灼熱の中なら「極度の加熱」になる（{@link #burnerHeatAt}）。
 * 極度の加熱はふつうブレイズケーキを食べさせ続けて保つ状態なので、灼熱で極度の加熱にしているバーナー 1 台ごとに、加熱領域の
 * 耐久値が時間で減る分の {@link #SUPERHEAT_WEAR_SHARE} 倍だけ余分に減る（2026-11-12 ユーザー、案 C）。台数が多いほど早く消え、
 * 長く保つには時間を長く取るか作動式: 魔法式延長で払い続けることになる。
 * <p>
 * 同じ術者の乱流領域と重なったブロックは「熱乱流」（ヒート・ストーム）になる（2026-11-11 ユーザー）: 中の生物に
 * {@link #STORM_INTERVAL} tick ごとに火属性のダメージ（攻撃術式の共通ルール、設定 {@code heat_storm.damageMultiplier}）、
 * Create の加工と Mekanism へ送る熱が 2 倍。
 * <p>
 * 凍結領域と重なったブロックでは、加熱と凍結が打ち消し合ってどちらも効かない（術者は問わない。乱流・湿潤との組み合わせより
 * 先に判定するので、熱乱流・吹雪も起きない。2026-11-11 ユーザー）。
 */
public class EntityHeatZone extends SpellGramZone {
    /** 威力 1 あたりの、範囲の中の生物が燃え続ける長さ（秒、仮）。威力 5 で 3 秒。 */
    public static final float BURN_SECONDS_PER_POWER = 0.6F;
    /** 威力 1 あたりの、Mekanism の機械を温める上限の温度（K、仮）。威力 10 で 1000 K（核分裂炉が損傷する 1200 K より下）。 */
    public static final double TEMPERATURE_PER_POWER = 100.0;
    /** 威力の既定値（置く前に設定しなかったとき）。 */
    public static final float DEFAULT_POWER = 5.0F;
    /** Mekanism の熱: 基準の速さで 1 tick に送る熱（薪木ストーブ 1 台が燃料を燃やしている間と同じ、仮）。 */
    public static final double HEAT_PER_TICK = 400.0;
    /** 氷炎地獄の灼熱の間、Mekanism へ送る熱の倍率（2026-11-11 ユーザー）。上限の温度は変えない。 */
    public static final double INFERNO_HEAT_FACTOR = 4.0;
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
    /**
     * 灼熱で極度の加熱にしているブレイズバーナー 1 台ごとの、耐久値の余分な減り: 時間で減る分の何倍か（仮）。1 なら 1 台で
     * 半分の時間で消え、3 台なら 4 分の 1。
     */
    public static final float SUPERHEAT_WEAR_SHARE = 1.0F;
    /** 極度の加熱にしているバーナーを数え直す間隔（tick）。 */
    private static final int SUPERHEAT_INTERVAL = 10;

    public EntityHeatZone(EntityType<? extends EntityHeatZone> type, Level level) {
        super(type, level);
    }

    public EntityHeatZone(Level level) {
        this(PsitweaksEntities.HEAT_ZONE.get(), level);
    }

    /** 威力（領域の温度、2026-11-11 ユーザー）。燃やす長さ・Mekanism の上限温度・氷炎地獄の強さを決める。 */
    private float power = DEFAULT_POWER;

    /** 置く前に呼ぶ。 */
    public void setPower(float power) {
        this.power = Math.max(0.0F, power);
        SpellGramIndex.touch();
    }

    /** 同じ種類が重なったところでは、威力の一番高いものだけが効く。 */
    @Override
    protected double stackStrength() {
        return getPower();
    }

    public float getPower() {
        return power;
    }

    /**
     * Create のブレイズバーナー（2026-11-11 の設計 B、{@code BlazeBurnerHeatZoneMixin} から呼ぶ）: ブロック {@code pos} が
     * 加熱領域の中なら 1（加熱）、氷炎地獄の灼熱の中なら 2（極度の加熱）、どちらでもなければ 0（サーバー側）。打ち消されている
     * ブロック・領域干渉で止められたブロックと、術者が機械を使えないブロックは 0。重なっていれば強いほう。
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
            if (zone.isEnded() || !zone.isInsideAndActive(at) || !(zone.getCaster() instanceof Player caster)
                    || !zone.mayUseMachine(server, caster, pos)) {
                continue;
            }
            if (zone.isInfernoAt(server.getGameTime())) {
                return 2;
            }
            heat = 1;
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

    @Override
    protected void affect(Entity target) {
        if (target instanceof LivingEntity living) {
            living.igniteForSeconds(burnSeconds());
        }
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
        if (inferno && tickCount % SUPERHEAT_INTERVAL == 0 && CreateCompat.isCreateLoaded()) {
            int burners = superheatedBurners(level, caster, regions);
            if (burners > 0) {
                reduceIntegrity(superheatWearPerTick(getMaxIntegrity(), getLifetimeTicks(), getTimeShare(), burners)
                        * SUPERHEAT_INTERVAL);
                // 隠し進捗（Create があるときだけ読み込まれる）。説明に耐久値を余分に使うことを書いてある。
                InfernoZones.award(level, caster, com.moratan251.psitweaks.common.handler.PsitweaksAdvancements.INFERNO_SUPERHEAT);
            }
        }
        double heat = heatPerTick(getMachineSpeed(), inferno);
        if (heat > 0.0) {
            for (ZoneRegion region : regions) {
                List<BlockEntity> machines = machinesIn(region);
                if (machines.isEmpty()) {
                    continue;
                }
                MekanismGameplayCompat.supplyZoneHeat(level, machines, heatSideCache(), heat, maxTemperature(),
                        pos -> isActiveAt(region.worldPos(pos)) && isStrongestAt(region.worldPos(pos)) && mayUseMachine(level, caster, pos));
                if (storm) {
                    for (EntityTurbulenceZone turbulence : overlappingStorms()) {
                        MekanismGameplayCompat.supplyZoneHeat(level, machines, heatSideCache(),
                                heat * stormBoost(turbulence.getPower()), maxTemperature(),
                                pos -> stormAt(region.worldPos(pos)) == turbulence && isStrongestAt(region.worldPos(pos)) && mayUseMachine(level, caster, pos));
                    }
                }
            }
        }
    }

    /** 灼熱でこの領域が極度の加熱にしているブレイズバーナーの数（自分の燃料で極度の加熱のものは数えない）。 */
    private int superheatedBurners(ServerLevel level, Player caster, List<ZoneRegion> regions) {
        int count = 0;
        for (ZoneRegion region : regions) {
            for (BlockEntity blockEntity : machinesIn(region)) {
                BlockPos pos = blockEntity.getBlockPos();
                if (!blockEntity.isRemoved() && CreateCompat.isBurnerNeedingSuperheat(blockEntity) && isActiveAt(region.worldPos(pos)) && isStrongestAt(region.worldPos(pos))
                        && mayUseMachine(level, caster, pos)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 極度の加熱にしているバーナー {@code burners} 台ぶんの、1 tick あたりの耐久値の余分な減り: 時間で減る分（置いたときの
     * 耐久値 × timeShare ÷ 存続時間）× {@link #SUPERHEAT_WEAR_SHARE} × 台数。時間の入力や干渉力によらず、存続時間の割合で
     * 同じだけ縮む。
     */
    public static float superheatWearPerTick(float maxIntegrity, int lifetimeTicks, float timeShare, int burners) {
        if (lifetimeTicks <= 0 || burners <= 0) {
            return 0.0F;
        }
        return maxIntegrity * timeShare / lifetimeTicks * SUPERHEAT_WEAR_SHARE * burners;
    }

    /** Mekanism へ 1 tick に送る熱: 薪木ストーブ 1 台ぶん × 速さ、灼熱の間は 4 倍。 */
    public static double heatPerTick(float machineSpeed, boolean inferno) {
        return HEAT_PER_TICK * machineSpeed * (inferno ? INFERNO_HEAT_FACTOR : 1.0);
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
