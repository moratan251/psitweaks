package com.moratan251.psitweaks.common.entities.SpellGram.zone;

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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * 作動式: 凍結領域（仮）の範囲。中の生物を、粉雪に埋まったときと同じように凍えさせる（凍えの値をためる。たまりきると
 * バニラと同じく動きが遅くなり、凍えのダメージを受ける）。凍えない生物（革の防具を着ている、ストレイなど）はバニラと同じく
 * 凍えない。術者自身は凍えさせない（{@link #sparesCaster}）。濡れている生物（湿潤領域を重ねるなど）は、凍えきるまでの
 * 時間が半分になる（{@link #freezeAmount}）。
 * <p>
 * 範囲の中の水源の水のうち、真上が空気のものを氷にする（氷渡りと同じ条件）。中にエンティティがいる水は凍らせない。
 * <p>
 * 同じ術者の加熱領域と 1 つの直方体を二分すると、氷炎地獄の「極寒」側になる（{@link InfernoZones}、隠し要素）。
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
    /** 威力 1 あたりの、{@link #INTERVAL} ごとの凍えの差し引きの増え（仮）。威力 5 で 20（3.5 秒で凍えきる）。 */
    public static final int NET_FREEZE_PER_POWER = 4;

    /** 威力（冷やす強さ、2026-11-11 ユーザー）。凍えのたまる速さと氷炎地獄の強さを決める。 */
    private float power = DEFAULT_POWER;

    /** 置く前に呼ぶ。 */
    public void setPower(float power) {
        this.power = Math.max(0.0F, power);
    }

    public float getPower() {
        return power;
    }

    /** 加熱領域と重なったブロックでは効かない（打ち消し合う）。 */
    @Override
    public boolean isActiveAt(BlockPos pos) {
        return super.isActiveAt(pos) && (level().isClientSide || !EntityHeatZone.coversAt(level(), pos));
    }

    /** {@code level} のブロック {@code pos} に凍結領域があるか（サーバー側、術者は問わない）。 */
    static boolean coversAt(Level level, BlockPos pos) {
        for (EntityFreezeZone zone : SpellGramIndex.all(level, EntityFreezeZone.class)) {
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
