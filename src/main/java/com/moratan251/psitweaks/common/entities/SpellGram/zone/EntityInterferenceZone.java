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
 * 他者は、自分を対象にする作動式も使えない。他者の領域は、範囲の中に中心を置けず、すでにある他者の領域は重なった
 * ブロックで効かない（{@link SpellGramZone#isActiveAt}）。
 * <p>
 * 止めるのは、置いた時点の術者の事象干渉力より低い事象干渉力の他者だけ（2026-11-11 ユーザー、案 A）。同じか高い相手は
 * 止めない。「他者」かは術者の {@code getUUID()} で見分ける（自律詠唱機は所有者の UUID を名乗るので、自分の自律詠唱機は
 * 他者に数えない）。Ars Nouveau・Iron's Spells 'n Spellbooks の魔法は止めない（2026-11-09 ユーザー）。
 */
public class EntityInterferenceZone extends SpellGramZone {
    private static final int MAX_MOTES_PER_TICK = 4;

    /** 辺の色: 紫。 */
    public static final int OUTLINE_COLOR = 0xB57BFF;

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

    /**
     * 側 {@code side}・事象干渉力 {@code interference} で置かれた領域が、ブロック {@code pos} で他者の領域干渉に止められるか
     * （サーバー側）。止めるかどうかは 1 段だけ見る（止める側の領域干渉が、さらに別の領域干渉に止められているかは見ない）。
     */
    public static boolean suppresses(Level level, BlockPos pos, @Nullable UUID side, double interference) {
        for (EntityInterferenceZone zone : SpellGramIndex.all(level, EntityInterferenceZone.class)) {
            if (zone.stops(pos, side, interference)) {
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
