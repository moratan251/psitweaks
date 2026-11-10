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
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * 作動式: 湿潤領域（仮）の範囲。中の生物を濡らす（作動式: アクアカッターと同じ濡れ）。出てからも
 * {@link #SOAK_TICKS} の間は濡れたまま。
 * <p>
 * 範囲の中のブロックにも水をかけたように効く: 火を消し（水入りスプラッシュ瓶と同じく、炎・焚き火・ろうそく）、
 * コンクリートパウダーをコンクリートに固める（{@link #affectBlock}、範囲を 1 段ずつ）。術者がそのブロックを変えてよいか
 * （保護）を確かめる。
 * <p>
 * Create があるときは、範囲の中の落ちているアイテムと、デポ・ベルトの上のアイテムを一括洗浄する。速さは置いた時点の
 * 事象干渉力で決まる（{@link SpellGramZone#machineSpeedFor}。事象干渉力はユーティリティの効き目に効かない方針の例外。
 * 洗浄は細かく調整する場面がないため、2026-11-10 ユーザー）。
 * <p>
 * 同じ術者の乱流領域と重なったブロックは「暴風雨」になる（2026-11-11 ユーザー）: 一括洗浄が 2 倍の速さになり、水に弱い
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
            // 洗う速さぶん進め、1 たまるごとに Create の洗浄を 1 回進める。
            for (int step = takeMachineSteps(); step > 0; step--) {
                CreateCompat.processZone(level, getArea(), CreateCompat.FanProcess.WASHING, this::isActiveAt,
                        pos -> isActiveAt(pos) && mayUseMachine(level, caster, pos));
            }
            if (isStorm()) {
                // 暴風雨のところは、乱流の威力に応じて上乗せ（威力 10 で 2 倍の速さ）。そのブロックで最も強い乱流のぶんだけ。
                forEachStormStep(getMachineSpeed(), (turbulence, steps) -> {
                    for (int i = 0; i < steps; i++) {
                        CreateCompat.processZone(level, getArea(), CreateCompat.FanProcess.WASHING,
                                pos -> stormAt(pos) == turbulence,
                                pos -> stormAt(pos) == turbulence && mayUseMachine(level, caster, pos));
                    }
                });
            }
        }
    }

    @Override
    protected boolean affectsBlocks() {
        return true;
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
        // Create があるとき: ケース入りファンの水を当てたときと同じ一括洗浄のパーティクル。
        CreateCompat.spawnProcessingParticles(level(), getArea(), CreateCompat.FanProcess.WASHING);
        if (isStorm()) {
            // 乱流と重なったところ: 水しぶきの竜巻（水しぶきは上向きの速さを 0 にすると横の速さが効く）。
            for (AABB storm : stormBoxes()) {
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
