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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 作動式: アイスサークルの円。凍結属性のダメージを与え、凍えさせる。
 * 円の中の水面を薄氷にする（氷渡り。置いた円は {@link #frostWalk}、追従している円は {@link #tickFollowFrost}）。
 * サークルが世界を変えるのはこれだけ。
 */
public class EntityIceCircle extends SpellGramCircle {

    private static final int FREEZE_TICKS_PER_HIT = 140;
    /** 追従していない円が、動いていないときに薄氷を作り直す間隔（tick）。動いた tick は毎 tick。 */
    public static final int FROST_INTERVAL = 10;

    /** 追従先の地面に触れている体（{@link #groundBody}）が前の tick にいたブロック。サーバー側だけ。 */
    @Nullable
    private BlockPos followBodyPos;
    /** 前の tick に、追従先の体が地面にいたか。サーバー側だけ。 */
    private boolean followBodyGrounded;

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

    /** 追従を始めた tick は、バニラで氷渡りの靴を履いたときと同じく、地面にいればすぐに凍らせる。 */
    @Override
    public void startFollowing(Entity target) {
        super.startFollowing(target);
        resetFollowFrost();
    }

    @Override
    protected void onSpellGramTickServer() {
        if (isEnded()) {
            return;
        }
        if (isEntityFollowMode()) {
            tickFollowFrost();
            return;
        }
        resetFollowFrost();
        if (getLastMoved() > 0.0D || tickCount % FROST_INTERVAL == 0) {
            frostWalk();
        }
    }

    /**
     * 追従していない円の氷渡り: 円の中で、中心の段と 1 つ下の段の水源を薄氷にし、もとからある薄氷の年齢を 0 に戻す（円がある間は
     * 溶けない）。円が去ると、バニラのとおり明るいところから溶ける。
     */
    protected void frostWalk() {
        int top = Mth.floor(getY());
        freeze(getX(), getZ(), top - 1, top, true, null);
    }

    /**
     * 追従している円の氷渡り（バニラの氷渡りに合わせる）: 追従先の体（{@link #groundBody}）が地面にいて、前の tick と違うブロックに
     * 移ったか、地面に降りた tick（追従を始めた tick を含む）だけ、体の足元の 1 つ下の段の水源を薄氷にする。もとからある薄氷の年齢は
     * 戻さないので、止まっていると明るいところから溶ける。薄氷にしたブロックごとに、体を出どころにしてブロックを置いたゲームイベントを出す。
     */
    private void tickFollowFrost() {
        Entity target = getFollowTargetEntity();
        Entity body = target == null ? null : groundBody(target);
        if (body == null) {
            resetFollowFrost();
            return;
        }
        BlockPos pos = body.blockPosition();
        boolean grounded = isOnGround(body);
        boolean changed = !pos.equals(followBodyPos) || !followBodyGrounded;
        followBodyPos = pos;
        followBodyGrounded = grounded;
        if (grounded && changed) {
            int y = pos.getY() - 1;
            freeze(body.getX(), body.getZ(), y, y, false, body);
        }
    }

    private void resetFollowFrost() {
        followBodyPos = null;
        followBodyGrounded = false;
    }

    /**
     * 追従先 {@code target} のうち地面に触れている体: 追従先がボートか、ボートに乗っているならそのボート。ほかの乗り物に乗っているなら
     * null（バニラの氷渡りと同じく、乗っている間は凍らせない）。乗っていなければ追従先そのもの。
     */
    @Nullable
    private static Entity groundBody(Entity target) {
        if (target instanceof Boat || !target.isPassenger()) {
            return target;
        }
        Entity root = target.getRootVehicle();
        return root instanceof Boat ? root : null;
    }

    /**
     * {@code body} が地面にいるか。ボートは、水に浮いて（沈んで）おらず、下面に触れるブロックがあるとき（氷や陸の上）。プレイヤーが
     * 漕ぐボートは、氷の上でもサーバー側の {@code onGround()} が真にならないことがあるので、ボートが自分の状態を決めるのと同じ下面の
     * 判定を使う（下に何もないと {@link Boat#getGroundFriction} は NaN になり、比べると偽）。
     */
    private static boolean isOnGround(Entity body) {
        if (body instanceof Boat boat) {
            return !boat.isInWater() && !boat.isUnderWater() && boat.getGroundFriction() > 0.0F;
        }
        return body.onGround();
    }

    /**
     * 中心 ({@code centerX}, {@code centerZ}) から水平にダメージと同じ半径の円の中で、{@code bottom}〜{@code top} の段の、真上が空気で
     * エンティティのいない水源を薄氷（年齢 0）にする。{@code refreshAge} なら、もとからある薄氷の年齢も 0 に戻す。{@code eventSource} が
     * あれば、薄氷にしたブロックごとにブロックを置いたゲームイベントを出す。保護されたブロックと、どれかの加熱領域が効いているブロックは
     * 変えない。術者がプレイヤーでないとき、領域干渉に止められているときは何もしない。
     */
    private void freeze(double centerX, double centerZ, int bottom, int top, boolean refreshAge, @Nullable Entity eventSource) {
        if (!(level() instanceof ServerLevel level) || !(getCaster() instanceof Player caster) || isSuppressed()) {
            return;
        }
        float radius = getDamageRadius();
        if (radius <= 0.0F) {
            return;
        }
        double radiusSq = (double) radius * radius;
        int minX = Mth.floor(centerX - radius);
        int maxX = Mth.floor(centerX + radius);
        int minZ = Mth.floor(centerZ - radius);
        int maxZ = Mth.floor(centerZ + radius);
        List<EntityHeatZone> heatZones = SpellGramIndex.all(level, EntityHeatZone.class);
        BlockState frosted = Blocks.FROSTED_ICE.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = top; y >= bottom; y--) {
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
                        if (eventSource != null) {
                            level.gameEvent(eventSource, GameEvent.BLOCK_PLACE, pos);
                        }
                    } else if (refreshAge && state.is(Blocks.FROSTED_ICE) && state.getValue(FrostedIceBlock.AGE) > 0) {
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
