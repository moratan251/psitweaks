package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.common.compat.CreateCompat;
import com.moratan251.psitweaks.common.compat.MekanismGameplayCompat;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * 作動式: 加熱領域（仮）の範囲。中の生物に火をつける（出てからも {@link #BURN_SECONDS} 秒は燃える。濡れている生物は
 * 燃えない、{@code CombatStatuses}）。術者自身は燃やさない（{@link #sparesCaster}）。
 * <p>
 * 範囲の中の氷と薄氷を水に戻す（ネザーのような水の置けないディメンションでは消える。氷塊・青氷はそのまま）。
 * <p>
 * Create があるときは、範囲の中の落ちているアイテムと、デポ・ベルトの上のアイテムを一括燻製する。Mekanism があるときは、
 * 範囲の中の熱を受け取るブロックに、薪木ストーブ 1 台ぶん（{@link #HEAT_PER_TICK}）の熱を毎 tick 送る。どちらも速さは
 * 置いた時点の事象干渉力で決まる（{@link SpellGramZone#machineSpeedFor}）。熱は {@link #MAX_TEMPERATURE} K までしか
 * 上げない（核分裂炉が損傷する 1200 K を越えさせない）。
 * <p>
 * 同じ術者の凍結領域と 1 つの直方体を二分すると、氷炎地獄の「灼熱」側になる（{@link InfernoZones}、隠し要素）。
 * 灼熱の間は、Create の加工が一括燻製から一括製錬に変わり、Mekanism へ送る熱が 4 倍になる（2026-11-11 ユーザー）。
 * <p>
 * 同じ術者の乱流領域と重なったブロックは「熱乱流」（ヒート・ストーム）になる（2026-11-11 ユーザー）: 中の生物に
 * {@link #STORM_INTERVAL} tick ごとに火属性のダメージ（攻撃術式の共通ルール、設定 {@code heat_storm.damageMultiplier}）、
 * Create の加工と Mekanism へ送る熱が 2 倍。
 * <p>
 * 凍結領域と重なったブロックでは、加熱と凍結が打ち消し合ってどちらも効かない（術者は問わない。乱流・湿潤との組み合わせより
 * 先に判定するので、熱乱流・吹雪も起きない。2026-11-11 ユーザー）。
 */
public class EntityHeatZone extends SpellGramZone {
    /** 範囲の中の生物が燃え続ける長さ（出てから消えるまで、秒、仮）。 */
    public static final float BURN_SECONDS = 3.0F;
    /** Mekanism の熱: 基準の速さで 1 tick に送る熱（薪木ストーブ 1 台が燃料を燃やしている間と同じ、仮）。 */
    public static final double HEAT_PER_TICK = 400.0;
    /** Mekanism の熱: これより熱いブロックには送らない（K）。 */
    public static final double MAX_TEMPERATURE = 1000.0;
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

    public EntityHeatZone(EntityType<? extends EntityHeatZone> type, Level level) {
        super(type, level);
    }

    public EntityHeatZone(Level level) {
        this(PsitweaksEntities.HEAT_ZONE.get(), level);
    }

    /** 凍結領域と重なったブロックでは効かない（打ち消し合う）。 */
    @Override
    public boolean isActiveAt(BlockPos pos) {
        return super.isActiveAt(pos) && (level().isClientSide || !EntityFreezeZone.coversAt(level(), pos));
    }

    /** {@code level} のブロック {@code pos} に加熱領域があるか（サーバー側、術者は問わない）。 */
    static boolean coversAt(Level level, BlockPos pos) {
        for (EntityHeatZone zone : SpellGramIndex.all(level, EntityHeatZone.class)) {
            if (!zone.isEnded() && zone.getArea().contains(pos)) {
                return true;
            }
        }
        return false;
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
            living.igniteForSeconds(BURN_SECONDS);
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
        for (int step = takeMachineSteps(); step > 0; step--) {
            CreateCompat.processZone(level, getArea(), fanProcess(inferno), this::isActiveAt,
                    pos -> isActiveAt(pos) && mayUseMachine(level, caster, pos));
        }
        if (storm) {
            // 熱乱流のところは、乱流の威力に応じて上乗せ（威力 10 で 2 倍の速さ）。そのブロックで最も強い乱流のぶんだけ。
            forEachStormStep(getMachineSpeed(), (turbulence, steps) -> {
                for (int i = 0; i < steps; i++) {
                    CreateCompat.processZone(level, getArea(), fanProcess(inferno), pos -> stormAt(pos) == turbulence,
                            pos -> stormAt(pos) == turbulence && mayUseMachine(level, caster, pos));
                }
            });
        }
        double heat = heatPerTick(getMachineSpeed(), inferno);
        if (heat > 0.0) {
            MekanismGameplayCompat.supplyZoneHeat(level, getArea().blockEntities(level), heat, MAX_TEMPERATURE,
                    pos -> isActiveAt(pos) && mayUseMachine(level, caster, pos));
            if (storm) {
                for (EntityTurbulenceZone turbulence : overlappingStorms()) {
                    MekanismGameplayCompat.supplyZoneHeat(level, getArea().blockEntities(level),
                            heat * stormBoost(turbulence.getPower()), MAX_TEMPERATURE,
                            pos -> stormAt(pos) == turbulence && mayUseMachine(level, caster, pos));
                }
            }
        }
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
        CreateCompat.spawnProcessingParticles(level(), getArea(), fanProcess(isInferno()));
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
