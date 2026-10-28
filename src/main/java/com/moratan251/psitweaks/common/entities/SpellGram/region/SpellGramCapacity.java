package com.moratan251.psitweaks.common.entities.SpellGram.region;

import com.moratan251.psitweaks.common.caster.CasterIds;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

import java.util.List;
import java.util.UUID;

/**
 * 術者ごとに置ける領域型の数と枠（試作）。枠の数え方は試作ブランチごとに違い、ここだけを差し替える。
 * この版（CAD の規模で数える版）では、枠 = 作動式の規模（{@link SpellGramPotencyLoad}）。置ける数の上限（{@code maxRegions}）も見る。
 */
public final class SpellGramCapacity {
    private SpellGramCapacity() {
    }

    /**
     * 領域型を置く、または性質を足す前に呼ぶ。足りなければ古いものから解く。
     *
     * @param pieceCost    この作動式の Psi コスト（CAD の効率をかける前）
     * @param piecePotency この作動式の規模
     * @param target       性質を足す相手。新しく置くときは null
     * @return この操作で増える枠の使用量（{@link EntitySpellGramRegion#getCapacityUse}）
     */
    public static int reserve(SpellContext context, int pieceCost, int piecePotency,
                              @Nullable EntitySpellGramRegion target) throws SpellRuntimeException {
        UUID caster = CasterIds.idOf(context.caster);
        if (target == null) {
            evictOverCount(caster, maxRegions() - 1, null);
        }
        int capacity = SpellGramPotencyLoad.capacityOf(context.caster);
        if (capacity >= 0) {
            evictOverUse(caster, capacity - piecePotency, target);
        }
        return piecePotency;
    }

    /** 術者の領域型の使う枠が変わった（置いた・解いた・性質を足した）。 */
    public static void onChanged(Level level, @Nullable UUID caster) {
        SpellGramPotencyLoad.refresh(level.getServer(), caster);
    }

    public static int maxRegions() {
        return PsitweaksConfig.COMMON.spellGramMaxRegions.get();
    }

    /** 古いものから解いて、使う枠の合計を {@code keep} 以下にする。{@code exclude} は解かない。 */
    static void evictOverUse(UUID caster, int keep, @Nullable EntitySpellGramRegion exclude) {
        int total = SpellGramRegionIndex.totalUse(caster);
        for (EntitySpellGramRegion region : SpellGramRegionIndex.objectsOf(caster)) {
            if (total <= keep) {
                return;
            }
            if (region != exclude) {
                total -= region.getCapacityUse();
                region.end(EntitySpellGramRegion.EndReason.REPLACED);
            }
        }
    }

    /** 古いものから解いて、{@code keep} 個以下にする。{@code exclude} は解かない。 */
    static void evictOverCount(UUID caster, int keep, @Nullable EntitySpellGramRegion exclude) {
        List<EntitySpellGramRegion> regions = SpellGramRegionIndex.objectsOf(caster);
        int count = regions.size();
        for (EntitySpellGramRegion region : regions) {
            if (count <= keep) {
                return;
            }
            if (region != exclude) {
                region.end(EntitySpellGramRegion.EndReason.REPLACED);
                count--;
            }
        }
    }
}
