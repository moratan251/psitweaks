package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * 作動式: 領域干渉（仮）の範囲。他者の Psi の作動式は、範囲の中のブロック・位置・エンティティを対象にできない
 * （{@code SpellContext.isInRadius} が偽になり、射程外と同じく失敗する。{@code SpellContextRangeMixin}）。範囲の中にいる
 * 他者は、自分を対象にする作動式も使えない。他者の領域は、範囲の中に中心を置けない。
 * <p>
 * 他人の領域との奪い合い（{@link SpellGramZone#isActiveAt}、どの領域にも共通）では、置いた時点の事象干渉力に
 * 威力 × {@link #CONTEST_BONUS_PER_POWER} を上乗せして比べる（2026-11-14 ユーザー）。勝てば、重なったところでは自分の側の
 * 領域だけが効く。
 * <p>
 * 範囲の中では、術者以外のエンダー系のテレポート（エンダーマン・シュルカーの瞬間移動、エンダーパール、コーラスフルーツ）を
 * 止める。範囲の中から外へも、外から中へも（{@link #onEntityTeleport}、2026-11-14 ユーザー）。
 * <p>
 * 止めるのは、置いた時点の術者の事象干渉力より低い事象干渉力の他者だけ（2026-11-11 ユーザー、案 A）。同じか高い相手は
 * 止めない。「他者」かは術者の {@code getUUID()} で見分ける（自律詠唱機は所有者の UUID を名乗るので、自分の自律詠唱機は
 * 他者に数えない）。Ars Nouveau・Iron's Spells 'n Spellbooks の魔法は止めない（2026-11-09 ユーザー）。
 */
public class EntityInterferenceZone extends SpellGramZone {
    private static final int MAX_MOTES_PER_TICK = 4;

    /** 辺の色: 紫。 */
    public static final int OUTLINE_COLOR = 0xB57BFF;
    /** 奪い合いの事象干渉力への上乗せ: 威力 1 あたり 5%（2026-11-14 ユーザー）。 */
    public static final double CONTEST_BONUS_PER_POWER = 0.05;

    /** 威力（0〜10、サーバー側）。他人の領域との奪い合いで事象干渉力に上乗せする。 */
    private float power;

    public EntityInterferenceZone(EntityType<? extends EntityInterferenceZone> type, Level level) {
        super(type, level);
    }

    public EntityInterferenceZone(Level level) {
        this(PsitweaksEntities.INTERFERENCE_ZONE.get(), level);
    }

    /**
     * 術者 {@code caster} の作動式が点 ({@code x}, {@code y}, {@code z}) を対象にするのを、領域干渉が止めるか（サーバー側）。
     * 他者の領域干渉で、置いた時点の事象干渉力が術者の今の事象干渉力より高く、その点のブロックで効いているもの
     * （さらに強い領域干渉に止められていない）があれば止める。
     */
    public static boolean blocks(Level level, double x, double y, double z, Player caster) {
        List<EntityInterferenceZone> zones = SpellGramIndex.all(level, EntityInterferenceZone.class);
        if (zones.isEmpty()) {
            return false;
        }
        BlockPos pos = BlockPos.containing(x, y, z);
        UUID side = caster.getUUID();
        double interference = SpellGramObject.interferenceOf(caster);
        for (EntityInterferenceZone zone : zones) {
            if (zone.stops(pos, side, interference) && zone.isActiveAt(pos)) {
                return true;
            }
        }
        return false;
    }

    /** 置く前に呼ぶ。威力（0〜10）。 */
    public void configurePower(float power) {
        this.power = Math.max(0.0F, power);
        SpellGramIndex.touch();
    }

    public float getPower() {
        return power;
    }

    /** 奪い合いの事象干渉力: 置いた時点の事象干渉力 ×（1 + 威力 × 5%）。 */
    @Override
    public double contestInterference() {
        return getPlacedInterference() * (1.0 + power * CONTEST_BONUS_PER_POWER);
    }

    /**
     * エンダー系のテレポート（エンダーマン・シュルカーの瞬間移動、エンダーパール、コーラスフルーツ）を、領域干渉の範囲の
     * 中から外へも、外から中へも止める（サーバー側）。止めないのは、その領域干渉を置いた本人（自律詠唱機の領域なら持ち主）
     * だけ。コマンドのテレポートは止めない。
     */
    public static void onEntityTeleport(net.neoforged.neoforge.event.entity.EntityTeleportEvent event) {
        if (!(event instanceof net.neoforged.neoforge.event.entity.EntityTeleportEvent.EnderEntity
                || event instanceof net.neoforged.neoforge.event.entity.EntityTeleportEvent.EnderPearl
                || event instanceof net.neoforged.neoforge.event.entity.EntityTeleportEvent.ChorusFruit)) {
            return;
        }
        Entity entity = event.getEntity();
        if (!entity.level().isClientSide && blocksTeleport(entity.level(), entity, event.getPrev(), event.getTarget())) {
            event.setCanceled(true);
        }
    }

    /** {@code entity} の、点 {@code from} から {@code to} へのテレポートを、どれかの領域干渉が止めるか（サーバー側）。 */
    public static boolean blocksTeleport(Level level, Entity entity, net.minecraft.world.phys.Vec3 from, net.minecraft.world.phys.Vec3 to) {
        List<EntityInterferenceZone> zones = SpellGramIndex.all(level, EntityInterferenceZone.class);
        if (zones.isEmpty()) {
            return false;
        }
        BlockPos start = BlockPos.containing(from);
        BlockPos end = BlockPos.containing(to);
        UUID side = entity.getUUID();
        for (EntityInterferenceZone zone : zones) {
            if (zone.isEnded() || side.equals(zone.getOwnerSide())) {
                continue;
            }
            if (zone.isInsideAndActive(start) || zone.isInsideAndActive(end)) {
                return true;
            }
        }
        return false;
    }

    /** この領域干渉が、ブロック {@code pos} で側 {@code side}・事象干渉力 {@code interference} の相手を止めるか。 */
    private boolean stops(BlockPos pos, @Nullable UUID side, double interference) {
        UUID owner = getOwnerSide();
        return !isEnded() && owner != null && !owner.equals(side) && interference < getPlacedInterference()
                && getArea().contains(pos);
    }

    @Override
    public int getOutlineColor() {
        return OUTLINE_COLOR;
    }

    /** 中のエンティティへの直接の効き目はない（止めるのは他者の作動式と領域）。 */
    @Override
    protected boolean affects(Entity entity) {
        return false;
    }

    @Override
    protected void affect(Entity target) {
    }

    @Override
    protected void spawnClientParticles() {
        AABB box = getOutline();
        double volume = box.getXsize() * box.getYsize() * box.getZsize();
        int motes = (int) Math.min(MAX_MOTES_PER_TICK, Math.max(1, volume / 200.0));
        for (int i = 0; i < motes; i++) {
            double x = box.minX + random.nextDouble() * box.getXsize();
            double y = box.minY + random.nextDouble() * box.getYsize();
            double z = box.minZ + random.nextDouble() * box.getZsize();
            if (getArea().contains(BlockPos.containing(x, y, z))) {
                level().addParticle(ParticleTypes.WITCH, x, y, z, 0.0, 0.0, 0.0);
            }
        }
    }
}
