package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.common.compat.CreateCompat;
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

import java.util.HashMap;
import java.util.Map;

/**
 * 作動式: 湿潤領域（仮）の範囲。中の生物を濡らす（作動式: アクアカッターと同じ濡れ）。出てからも
 * {@link #SOAK_TICKS} の間は濡れたまま。
 * <p>
 * 範囲の中のブロックにも水をかけたように効く: 火を消し（水入りスプラッシュ瓶と同じく、炎・焚き火・ろうそく）、
 * コンクリートパウダーをコンクリートに固める。範囲を 1 tick に 1 段ずつ下から見ていき、一周したらまた下から。
 * 読み込まれていないチャンクは見ず、術者がそのブロックを変えてよいか（保護）を確かめる。
 * <p>
 * Create があるときは、範囲の中の落ちているアイテムと、デポ・ベルトの上のアイテムを一括洗浄する。速さは置いた時点の
 * 事象干渉力で決まる（事象干渉力はユーティリティの効き目に効かない方針の例外。洗浄は細かく調整する場面がないため、
 * 2026-11-10 ユーザー）。
 */
public class EntitySoakZone extends SpellGramZone {
    /** 範囲の中の生物を濡らす長さ（出てから乾くまで、仮）。 */
    public static final int SOAK_TICKS = 60;
    private static final int INTERVAL = 10;
    private static final int MAX_DRIPS_PER_TICK = 6;

    /** 辺の色: 水の青。 */
    public static final int OUTLINE_COLOR = 0x3F8CFF;

    /** 次にブロックを見る段（y）。範囲の外なら下の段から。 */
    private int scanY = Integer.MIN_VALUE;
    /**
     * Create の一括洗浄の速さ（ケース入りファンの何倍か）。置いた時点の事象干渉力で決まり、置いた後は変わらない
     * （{@link #washSpeedFor}）。サーバー側だけ。
     */
    private float washSpeed = 1.0F;
    private float washProgress;
    /** Create の一括洗浄で、術者が使ってよいデポ・ベルトか（保護）。ブロックごとに一度だけ確かめる。サーバー側だけ。 */
    private final Map<BlockPos, Boolean> washAllowed = new HashMap<>();

    public EntitySoakZone(EntityType<? extends EntitySoakZone> type, Level level) {
        super(type, level);
    }

    public EntitySoakZone(Level level) {
        this(PsitweaksEntities.SOAK_ZONE.get(), level);
    }

    /** 洗う速さの基準: 事象干渉力 1 あたりのケース入りファンに対する倍率（仮）。 */
    public static final float WASH_SPEED_PER_INTERFERENCE = 0.5F;
    /** 洗う速さの上限（ケース入りファンの何倍か、仮）。 */
    public static final float MAX_WASH_SPEED = 2.0F;

    /** 置いた時点の事象干渉力から、Create の一括洗浄の速さ（ケース入りファンの何倍か）。 */
    public static float washSpeedFor(double interference) {
        return (float) Math.max(0.0, Math.min(MAX_WASH_SPEED, interference * WASH_SPEED_PER_INTERFERENCE));
    }

    /** 置く前に呼ぶ。Create の一括洗浄の速さ（ケース入りファンの何倍か）。 */
    public void setWashSpeed(float speed) {
        this.washSpeed = Math.max(0.0F, speed);
    }

    public float getWashSpeed() {
        return washSpeed;
    }

    @Override
    public int getOutlineColor() {
        return OUTLINE_COLOR;
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
        if (!isEnded() && level() instanceof ServerLevel level && getCaster() instanceof Player caster) {
            wetLayer(level, caster);
            // 洗う速さぶん進め、1 たまるごとに Create の洗浄を 1 tick ぶん進める。
            washProgress += washSpeed;
            while (washProgress >= 1.0F) {
                washProgress -= 1.0F;
                CreateCompat.washZone(level, getArea(), pos -> washAllowed.computeIfAbsent(pos.immutable(),
                        key -> SpellBlockProtection.mayChange(level, caster, key)));
            }
        }
    }

    /** 範囲の 1 段ぶんのブロックに水をかける。 */
    private void wetLayer(ServerLevel level, Player caster) {
        ZoneArea area = getArea();
        if (area.isEmpty()) {
            return;
        }
        AABB box = area.bounds();
        int minY = (int) Math.floor(box.minY);
        int maxY = (int) Math.floor(box.maxY) - 1;
        if (scanY < minY || scanY > maxY) {
            scanY = minY;
        }
        int minX = (int) Math.floor(box.minX);
        int maxX = (int) Math.floor(box.maxX) - 1;
        int minZ = (int) Math.floor(box.minZ);
        int maxZ = (int) Math.floor(box.maxZ) - 1;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                pos.set(x, scanY, z);
                if (area.contains(pos) && level.isLoaded(pos)) {
                    wetBlock(level, caster, pos.immutable(), level.getBlockState(pos));
                }
            }
        }
        scanY++;
    }

    /** 1 ブロックに水をかける: 火を消すか、コンクリートパウダーを固める。それ以外は何もしない。 */
    private static void wetBlock(ServerLevel level, Player caster, BlockPos pos, BlockState state) {
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
        CreateCompat.spawnWashingParticles(level(), getArea());
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
