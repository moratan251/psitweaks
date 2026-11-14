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

import java.util.List;

/**
 * 作動式: 凍結領域（仮）の範囲。中の生物を、粉雪に埋まったときと同じように凍えさせる（凍えの値をためる。たまりきると
 * バニラと同じく動きが遅くなり、凍えのダメージを受ける）。凍えない生物（革の防具を着ている、ストレイなど）はバニラと同じく
 * 凍えない。術者自身は凍えさせない（{@link #sparesCaster}）。濡れている生物（湿潤領域を重ねるなど）は、凍えきるまでの
 * 時間が半分になる（{@link #freezeAmount}）。
 * <p>
 * 範囲の中の水源の水のうち、真上が空気のものを氷にする（氷渡りと同じ条件）。中にエンティティがいる水は凍らせない。
 * <p>
 * 同じ術者の加熱領域と 1 つの直方体を二分すると、氷炎地獄の「極寒」側になる（{@link InfernoZones}、隠し要素）。極寒の間に
 * Mekanism の機械から奪った熱は、灼熱側の加熱領域へ移す（2026-11-15 ユーザー、案 A。前の「冷却 4 倍」は外した）。
 * <p>
 * 同じ術者の乱流領域と重なったブロックは「吹雪」になる（2026-11-11 ユーザー）: 中の生物に {@link #INTERVAL} tick ごとに
 * 凍結属性のダメージ（攻撃術式の共通ルール、設定 {@code blizzard.damageMultiplier}）、凍えも 2 倍の速さでたまる
 * （濡れていればさらに 2 倍）。
 * <p>
 * 加熱領域と重なったブロックでは、加熱と凍結が打ち消し合ってどちらも効かない（{@link EntityHeatZone#isActiveAt}）。
 */
public class EntityFreezeZone extends SpellGramZone {
    private static final int INTERVAL = 10;
    /** 粉雪の外で {@link #INTERVAL} の間に抜ける凍えの値（毎 tick 2）。 */
    private static final int THAW_PER_INTERVAL = 20;
    private static final int MAX_FLAKES_PER_TICK = 5;

    /** 辺の色: 氷の水色。 */
    public static final int OUTLINE_COLOR = 0xB8F0FF;
    /** 氷炎地獄の極寒の辺の色: 青白。 */
    public static final int INFERNO_OUTLINE_COLOR = 0x6FD0FF;
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
     * 冷却量の比率: 置いたときのコスト 1 Psi あたりに取り除ける熱（J、2026-11-14・15 ユーザー: 加熱の熱量と対の「冷却量」）。
     * 威力 10・サイズ 16（コスト 1074）の速さ 1 で約 1 MJ/t。冷やしてもエネルギーは生まれないので、熱（360 J/Psi）より桁違いに
     * 大きくてよい（核分裂炉を冷やせる）。
     */
    public static final double COOLING_PER_PSI = 560_000.0;
    /** 威力 1 あたりの、{@link #INTERVAL} ごとの凍えの差し引きの増え（仮）。威力 5 で 20（3.5 秒で凍えきる）。 */
    public static final int NET_FREEZE_PER_POWER = 4;

    /**
     * 威力（冷やす強さ、2026-11-11 ユーザー）。凍えのたまる速さ・1 台あたりの冷える割合・氷炎地獄の強さを決める。1 体・1 か所
     * あたりの強さに使うので {@link #MAX_EFFECT_POWER} まで。
     */
    private float power = DEFAULT_POWER;
    /** 入れた威力（10 より上もそのまま。コストと重なりの強さに使う）。 */
    private float rawPower = DEFAULT_POWER;

    /**
     * Mekanism の冷却で、1 台が 1 tick に温度差を縮める割合の上限（加熱の「威力 × 100 K まで」と対）: 威力 × 0.002 × 機械を
     * 動かす速さ（置いた時点の事象干渉力）、上限 0.5。
     */
    public static double coolFractionPerTick(float power, float machineSpeed) {
        double fraction = Math.max(0.0F, power) * COOL_FRACTION_PER_POWER * Math.max(0.0F, machineSpeed);
        return Math.min(MAX_COOL_FRACTION, fraction);
    }

    /**
     * 冷却量: 1 tick に取り除ける熱の総量（J）: 置いたときのコスト × {@link #COOLING_PER_PSI} ÷ 存続時間 × 速さ。氷炎地獄の
     * 極寒の間は、相手の加熱領域の同じ式の分を足す（灼熱側の熱量と対、2 つの領域のコストの合計）。
     */
    public double coolingTotalPerTick(boolean inferno) {
        double total = coolingTotalPerTick(getPlacementCost(), getLifetimeTicks(), getMachineSpeed());
        if (inferno && infernoPartner() instanceof EntityHeatZone hot) {
            total += coolingTotalPerTick(hot.getPlacementCost(), hot.getLifetimeTicks(), hot.getMachineSpeed());
        }
        return total;
    }

    /** 照準の表示: 冷却量（J/t、極寒なら相手の分も足したもの）。 */
    @Override
    protected double resourcePerTick() {
        return coolingTotalPerTick(isInfernoAt(level().getGameTime()));
    }

    @Override
    public String resourceKey() {
        return "psitweaks.hud.zone_resource.cooling";
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
        return isInferno() ? INFERNO_OUTLINE_COLOR : isStorm() ? STORM_OUTLINE_COLOR : OUTLINE_COLOR;
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
                        pos -> STORM_DAMAGE_PER_POWER * stormPowerAt(pos), target -> true, target -> {
                        });
            }
        }
        // 氷炎地獄の極寒: 当たった生物は凍えきる（加熱領域が組み合わせを確かめて続ける）。
        if (!isEnded() && tickCount % InfernoZones.INTERVAL == 0 && level() instanceof ServerLevel level
                && isInfernoAt(level.getGameTime())) {
            InfernoZones.harm(level, this, PsitweaksDamageTypes.INFERNO_COLD, target -> {
                if (target.canFreeze()) {
                    target.setTicksFrozen(Math.max(target.getTicksFrozen(), target.getTicksRequiredToFreeze() + INTERVAL * 2));
                }
            });
        }
        coolMekanismMachines();
    }

    /**
     * Mekanism の熱を持つ設備を、周囲の温度に向けて冷やす（毎 tick、2026-11-11 の設計 A）。加熱領域と打ち消し合っているブロック・
     * 領域干渉で止められたブロック・術者が機械を使えないブロックは冷やさない。吹雪のブロックは乱流の威力に応じて上乗せ。
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
        double cap = coolingTotalPerTick(isInfernoAt(level.getGameTime()));
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
        double removed = MekanismGameplayCompat.drainZoneHeat(level, machines, heatSideCache(), fraction, cap, pos -> {
            ZoneRegion region = owners.get(pos);
            return region != null && isActiveAt(region.worldPos(pos)) && isStrongestAt(region.worldPos(pos))
                    && mayUseMachine(level, caster, pos);
        });
        if (isStorm()) {
            // 吹雪のところは、乱流の威力に応じて割合と冷却量を上乗せ（威力 10 で 2 倍）。
            for (EntityTurbulenceZone turbulence : overlappingStorms()) {
                double boost = stormBoost(turbulence.getPower());
                removed += MekanismGameplayCompat.drainZoneHeat(level, machines, heatSideCache(),
                        Math.min(MAX_COOL_FRACTION, fraction * boost), cap * boost, pos -> {
                            ZoneRegion region = owners.get(pos);
                            return region != null && stormAt(region.worldPos(pos)) == turbulence
                                    && isStrongestAt(region.worldPos(pos)) && mayUseMachine(level, caster, pos);
                        });
            }
        }
        // 氷炎地獄の極寒: 奪った熱を灼熱側の加熱領域へ移す（2026-11-15 ユーザー、案 A）。
        if (infernoPartner() instanceof EntityHeatZone hot) {
            hot.acceptMovedHeat(removed);
        }
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
    protected boolean affectsBlocks() {
        return true;
    }

    @Override
    protected boolean mayAffectState(BlockState state) {
        return state.is(Blocks.WATER) && state.getFluidState().isSource();
    }

    /** 水源の水で、真上が空気のものを氷にする。 */
    @Override
    protected void affectBlock(ServerLevel level, Player caster, BlockPos pos, BlockState state) {
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
                ZoneParticles.vortex(level(), random, storm, count, ParticleTypes.SNOWFLAKE, 0.55, 0.02);
                ZoneParticles.vortex(level(), random, storm, Math.max(1, count / 3), ParticleTypes.ITEM_SNOWBALL, 0.6, 0.05);
                ZoneParticles.vortex(level(), random, storm, Math.max(1, count / 4), ParticleTypes.CLOUD, 0.45, 0.03);
            }
        }
        AABB box = getOutline();
        double volume = box.getXsize() * box.getYsize() * box.getZsize();
        int flakes = (int) Math.min(MAX_FLAKES_PER_TICK, Math.max(1, volume / 150.0)) * (isInferno() ? 4 : 1);
        for (int i = 0; i < flakes; i++) {
            double x = box.minX + random.nextDouble() * box.getXsize();
            double y = box.minY + random.nextDouble() * box.getYsize();
            double z = box.minZ + random.nextDouble() * box.getZsize();
            if (getArea().contains(BlockPos.containing(x, y, z))) {
                level().addParticle(isInferno() && random.nextInt(3) == 0 ? ParticleTypes.ITEM_SNOWBALL : ParticleTypes.SNOWFLAKE,
                        x, y, z, 0.0, -0.03, 0.0);
            }
        }
    }
}
