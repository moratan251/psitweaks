package com.moratan251.psitweaks.common.entities.SpellGram;

import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityHeatZone;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.spells.SpellBlockProtection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.List;

/**
 * 作動式: アイスサークルの円。凍結属性のダメージを与え、凍えさせる。
 * 円の中の水面を薄氷にする（バニラの氷渡りと同じ、{@link #frostWalk}）。サークルが世界を変えるのはこれだけ。
 */
public class EntityIceCircle extends SpellGramCircle {

    private static final int FREEZE_TICKS_PER_HIT = 140;
    /** 動いていないときに薄氷を作り直す間隔（tick）。動いた tick は毎 tick。 */
    public static final int FROST_INTERVAL = 10;

    public EntityIceCircle(EntityType<? extends EntityIceCircle> type, Level level) {
        super(type, level);
    }

    public EntityIceCircle(Level level) {
        this(PsitweaksEntities.ICE_CIRCLE.get(), level);
    }

    @Override
    protected ResourceKey<DamageType> damageType() {
        return PsitweaksDamageTypes.ICE_CIRCLE;
    }

    @Override
    protected ParticleOptions ringParticle() {
        return ParticleTypes.SNOWFLAKE;
    }

    @Override
    protected ParticleOptions innerParticle() {
        return ParticleTypes.ITEM_SNOWBALL;
    }

    @Override
    protected void onAreaDamageApplied(LivingEntity target, float damage, DamageSource source) {
        // 当てるたびに足すが、凍り切る量（getTicksRequiredToFreeze）に1回分を足した所で止める。上限がないと、円が消えた後も
        // 当てた回数だけ長く凍ったままになっていた。
        int cap = target.getTicksRequiredToFreeze() + FREEZE_TICKS_PER_HIT;
        target.setTicksFrozen(Math.min(cap, target.getTicksFrozen() + FREEZE_TICKS_PER_HIT));
    }

    @Override
    protected void onSpellGramTickServer() {
        if (isEnded()) {
            return;
        }
        if (getLastMoved() > 0.0D || tickCount % FROST_INTERVAL == 0) {
            frostWalk();
        }
    }

    /**
     * 氷渡り: ダメージと同じ水平の円の中で、中心の段と 1 つ下の段の、真上が空気でエンティティのいない水源を薄氷（年齢 0）にし、
     * もとからある薄氷の年齢を 0 に戻す。溶けるのはバニラのとおり（円が去ると明るいところから溶ける）。保護されたブロックと、
     * どれかの加熱領域が効いているブロックは変えない。術者がプレイヤーでないとき、領域干渉に止められているとき、水に浮いている
     * ボートを追従しているとき（漕いでいるボートが閉じ込められないように）は何もしない。氷の上のボートを追従しているときは凍らせる
     * ので、氷上ボートは先の水面が薄氷になって滑り続けられる。
     */
    protected void frostWalk() {
        if (!(level() instanceof ServerLevel level) || !(getCaster() instanceof Player caster)
                || isSuppressed() || isFollowingBoatInWater()) {
            return;
        }
        float radius = getDamageRadius();
        if (radius <= 0.0F) {
            return;
        }
        double centerX = getX();
        double centerZ = getZ();
        double radiusSq = (double) radius * radius;
        int minX = Mth.floor(centerX - radius);
        int maxX = Mth.floor(centerX + radius);
        int minZ = Mth.floor(centerZ - radius);
        int maxZ = Mth.floor(centerZ + radius);
        int top = Mth.floor(getY());
        List<EntityHeatZone> heatZones = SpellGramIndex.all(level, EntityHeatZone.class);
        BlockState frosted = Blocks.FROSTED_ICE.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = top; y >= top - 1; y--) {
            if (y < level.getMinBuildHeight() || y >= level.getMaxBuildHeight() - 1) {
                continue;
            }
            for (int x = minX; x <= maxX; x++) {
                double dx = x + 0.5D - centerX;
                for (int z = minZ; z <= maxZ; z++) {
                    double dz = z + 0.5D - centerZ;
                    if (dx * dx + dz * dz > radiusSq) {
                        continue;
                    }
                    pos.set(x, y, z);
                    if (!level.isLoaded(pos)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(pos);
                    if (state == IceBlock.meltsInto()) {
                        if (!level.getBlockState(pos.above()).isAir()
                                || !level.isUnobstructed(frosted, pos, CollisionContext.empty())
                                || heatActive(heatZones, pos) || !SpellBlockProtection.mayChange(level, caster, pos)) {
                            continue;
                        }
                        level.setBlockAndUpdate(pos, frosted);
                    } else if (state.is(Blocks.FROSTED_ICE) && state.getValue(FrostedIceBlock.AGE) > 0) {
                        if (heatActive(heatZones, pos) || !SpellBlockProtection.mayChange(level, caster, pos)) {
                            continue;
                        }
                        level.setBlock(pos, state.setValue(FrostedIceBlock.AGE, 0), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    /** ブロック {@code pos} で、どれかの加熱領域（術者を問わない）が効いているか。 */
    private static boolean heatActive(List<EntityHeatZone> heatZones, BlockPos pos) {
        for (EntityHeatZone zone : heatZones) {
            if (!zone.isEnded() && zone.isInsideAndActive(pos)) {
                return true;
            }
        }
        return false;
    }
}
