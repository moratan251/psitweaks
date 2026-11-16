package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.common.compat.CreateCompat;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.spells.SpellBlockProtection;
import com.moratan251.psitweaks.common.spells.combat.CombatStatuses;
import com.moratan251.psitweaks.mixin.ConcretePowderBlockAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * 作動式: 湿潤領域（仮）の範囲。中の生物を濡らす（作動式: アクアカッターと同じ濡れ）。出てからも
 * {@link #SOAK_TICKS} の間は濡れたまま。
 * <p>
 * 範囲の中のブロックにも水をかけたように効く: 火を消し（水入りスプラッシュ瓶と同じく、炎・焚き火・ろうそく）、
 * コンクリートパウダーをコンクリートに固める（{@link #affectBlock}、範囲を 1 段ずつ）。ランダムに選んだブロックで、耕地を
 * 湿らせ、スポンジを濡らす（{@link #randomBlockTick}）。術者がそのブロックを変えてよいか（保護）を確かめる。
 * <p>
 * Create があるときは、同じ術者の乱流領域が重なったところ（暴風雨）だけ、落ちているアイテムと、デポ・ベルトの上のアイテムを
 * 一括洗浄する。乱流がなければ加工しない。速さは置いた時点の事象干渉力で決まる（{@link SpellGramZone#machineSpeedFor}）。
 * <p>
 * 同じ術者の乱流領域と重なったブロックは「暴風雨」になる: 一括洗浄が乱流の威力に応じて速くなり（威力 10 で 2 倍）、水に弱い
 * 生物（ブレイズ・エンダーマン・スノウゴーレム・ストライダー、{@code isSensitiveToWater}）に {@link #STORM_INTERVAL} tick
 * ごとにダメージ（攻撃術式の共通ルール、設定 {@code rainstorm.damageMultiplier}）。
 */
public class EntitySoakZone extends SpellGramZone {
    /** 範囲の中の生物を濡らす長さ（出てから乾くまで、仮）。 */
    public static final int SOAK_TICKS = 60;
    private static final int INTERVAL = 10;
    private static final int MAX_DRIPS_PER_TICK = 6;

    /** 暴風雨のダメージの間隔（tick）。 */
    public static final int STORM_INTERVAL = 10;

    /** 辺の色: 水の青。 */
    public static final int OUTLINE_COLOR = 0x3F8CFF;
    /** 乱流と重なったときの辺の色: 水色。 */
    public static final int STORM_OUTLINE_COLOR = 0x4FE6FF;


    public EntitySoakZone(EntityType<? extends EntitySoakZone> type, Level level) {
        super(type, level);
    }

    public EntitySoakZone(Level level) {
        this(PsitweaksEntities.SOAK_ZONE.get(), level);
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
    protected void affect(Entity target) {
        if (target instanceof LivingEntity living) {
            CombatStatuses.soak(living, SOAK_TICKS);
        }
    }

    @Override
    protected void onSpellGramTickServer() {
        super.onSpellGramTickServer();
        if (isEnded() || getArea().isEmpty() || !(level() instanceof ServerLevel level)) {
            return;
        }
        if (tickCount % STORM_INTERVAL == 0) {
            refreshStorm();
            if (isStorm()) {
                // 暴風雨: 水に弱い生物にダメージ。
                ZoneAttacks.harmScaled(level, this, PsitweaksDamageTypes.RAINSTORM,
                        PsitweaksConfig.COMMON.rainstormDamageMultiplier.get(),
                        pos -> STORM_DAMAGE_PER_POWER * stormPowerAt(pos), LivingEntity::isSensitiveToWater, target -> {
                        });
            }
        }
        if (getCaster() instanceof Player caster) {
            // 洗う速さぶん進め、1 たまるごとに Create の洗浄を 1 回進める。洗うのは同じ術者の乱流領域が重なったところ（暴風雨）
            // だけ。
            // 世界の範囲と、範囲に重なった機体ごとの範囲（機体の上のデポ・ベルトも洗う）。範囲・領域干渉・乱流は世界の位置で
            // 判定し、保護は機械が本当にある位置で確かめる。
            List<ZoneRegion> regions = regions();
            boolean stormy = !overlappingStorms().isEmpty();
            for (int step = takeMachineSteps(); step > 0 && stormy; step--) {
                for (ZoneRegion region : regions) {
                    CreateCompat.processZone(level, region.area(), machinesIn(region), CreateCompat.FanProcess.WASHING,
                            pos -> washesAt(region.worldPos(pos)),
                            pos -> washesAt(region.worldPos(pos)) && mayUseMachine(level, caster, pos));
                }
            }
            if (stormy) {
                // 暴風雨のところは、乱流の威力に応じて上乗せ（威力 10 で 2 倍の速さ）。そのブロックで最も強い乱流のぶんだけ。
                forEachStormStep(getMachineSpeed(), (turbulence, steps) -> {
                    for (int i = 0; i < steps; i++) {
                        for (ZoneRegion region : regions) {
                            CreateCompat.processZone(level, region.area(), machinesIn(region), CreateCompat.FanProcess.WASHING,
                                    pos -> stormAt(region.worldPos(pos)) == turbulence && isStrongestAt(region.worldPos(pos)),
                                    pos -> stormAt(region.worldPos(pos)) == turbulence && isStrongestAt(region.worldPos(pos))
                                            && mayUseMachine(level, caster, pos));
                        }
                    }
                });
            }
        }
    }

    /** 世界の位置 {@code world} で一括洗浄するか（サーバー側）: この領域が効き、同じ術者の乱流領域が吹いているところ。 */
    private boolean washesAt(BlockPos world) {
        return isActiveAt(world) && isStrongestAt(world) && isStormAt(world);
    }

    @Override
    protected boolean affectsBlocks() {
        return true;
    }

    @Override
    protected boolean hasRandomBlockTicks() {
        return true;
    }

    @Override
    protected boolean mayRandomTick(BlockState state) {
        return state.is(Blocks.SPONGE)
                || (state.getBlock() instanceof FarmBlock && state.getValue(FarmBlock.MOISTURE) < FarmBlock.MAX_MOISTURE);
    }

    /**
     * ランダムに選ばれたブロック（{@link SpellGramZone#RANDOM_BLOCK_TICK_CHANCE}）に水をかける: 耕地は水の近くにあるときと同じく
     * 一番湿った状態にし（{@code FarmBlock#randomTick}）、スポンジは濡れたスポンジにする（水を吸ったときと同じ音、水は吸わない）。
     * 同じ術者の加熱領域も効いているところのスポンジは、濡らすのと乾かすのを行き来しないよう、どちらもしない。
     */
    @Override
    protected void randomBlockTick(ServerLevel level, Player caster, BlockPos pos, BlockPos world, BlockState state) {
        boolean sponge = state.is(Blocks.SPONGE);
        boolean farmland = state.getBlock() instanceof FarmBlock && state.getValue(FarmBlock.MOISTURE) < FarmBlock.MAX_MOISTURE;
        if (!sponge && !farmland) {
            return;
        }
        if (sponge && ownZoneActiveAt(level, EntityHeatZone.class, world)) {
            return;
        }
        if (!SpellBlockProtection.mayChange(level, caster, pos)) {
            return;
        }
        if (farmland) {
            level.setBlock(pos, state.setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE), Block.UPDATE_CLIENTS);
            return;
        }
        level.setBlock(pos, Blocks.WET_SPONGE.defaultBlockState(), Block.UPDATE_CLIENTS);
        level.playSound(null, pos, SoundEvents.SPONGE_ABSORB, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    protected boolean mayAffectState(BlockState state) {
        return state.is(BlockTags.FIRE) || CampfireBlock.isLitCampfire(state) || AbstractCandleBlock.isLit(state)
                || state.getBlock() instanceof ConcretePowderBlock;
    }

    /** 1 ブロックに水をかける: 火を消すか、コンクリートパウダーを固める。それ以外は何もしない。 */
    @Override
    protected void affectBlock(ServerLevel level, Player caster, BlockPos pos, BlockState state) {
        boolean fire = state.is(BlockTags.FIRE);
        boolean campfire = CampfireBlock.isLitCampfire(state);
        boolean candle = AbstractCandleBlock.isLit(state);
        Block concrete = state.getBlock() instanceof ConcretePowderBlock powder
                ? ((ConcretePowderBlockAccessor) powder).psitweaks$getConcrete() : null;
        if (!fire && !campfire && !candle && concrete == null) {
            return;
        }
        if (!SpellBlockProtection.mayChange(level, caster, pos)) {
            return;
        }
        if (fire) {
            level.removeBlock(pos, false);
            level.levelEvent(null, LevelEvent.SOUND_EXTINGUISH_FIRE, pos, 0);
        } else if (campfire) {
            level.levelEvent(null, LevelEvent.SOUND_EXTINGUISH_FIRE, pos, 0);
            CampfireBlock.dowse(caster, level, pos, state);
            level.setBlockAndUpdate(pos, state.setValue(CampfireBlock.LIT, false));
        } else if (candle) {
            AbstractCandleBlock.extinguish(caster, state, level, pos);
        } else {
            level.setBlockAndUpdate(pos, concrete.defaultBlockState());
        }
    }

    @Override
    protected void spawnClientParticles() {
        if (isStorm()) {
            List<AABB> storms = stormBoxes();
            // Create があるとき: ケース入りファンの水を当てたときと同じ一括洗浄のパーティクル。洗うのは乱流が重なったところ
            // だけなので、そこにだけ出す（クライアントでは術者を見分けない）。
            // 機体の上のデポ・ベルトにも出す（機体用の空間に出したパーティクルは、Sable が見えている場所へ移す）。
            for (ZoneRegion region : regions()) {
                CreateCompat.spawnProcessingParticles(level(), region.area(), machinesIn(region),
                        pos -> insideAny(storms, net.minecraft.world.phys.Vec3.atCenterOf(region.worldPos(pos)))
                                ? CreateCompat.FanProcess.WASHING : null);
            }
            // 乱流と重なったところ: 水しぶきの竜巻（水しぶきは上向きの速さを 0 にすると横の速さが効く）。
            for (AABB storm : storms) {
                int count = ZoneParticles.count(storm, 20.0, 6, 16);
                ZoneParticles.vortex(level(), random, storm, count, ParticleTypes.SPLASH, 0.5, 0.0);
                ZoneParticles.vortex(level(), random, storm, Math.max(1, count / 3), ParticleTypes.CLOUD, 0.45, 0.04);
            }
        }
        AABB box = getOutline();
        double volume = box.getXsize() * box.getYsize() * box.getZsize();
        int drips = (int) Math.min(MAX_DRIPS_PER_TICK, Math.max(1, volume / 150.0));
        for (int i = 0; i < drips; i++) {
            double x = box.minX + random.nextDouble() * box.getXsize();
            double y = box.minY + random.nextDouble() * box.getYsize();
            double z = box.minZ + random.nextDouble() * box.getZsize();
            if (getArea().contains(BlockPos.containing(x, y, z))) {
                level().addParticle(ParticleTypes.FALLING_WATER, x, y, z, 0.0, 0.0, 0.0);
            }
        }
    }
}
