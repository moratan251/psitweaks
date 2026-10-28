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
 * 共通部分では置ける数（{@code spellGramRegion.maxRegions}）だけを見て、超えたら古いものから解く。
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
        if (target == null) {
            evictOverCount(CasterIds.idOf(context.caster), maxRegions() - 1, null);
        }
        return 0;
    }

    /** 術者の領域型の使う枠が変わった（置いた・解いた・性質を足した）。 */
    public static void onChanged(Level level, @Nullable UUID caster) {
    }

    public static int maxRegions() {
        return PsitweaksConfig.COMMON.spellGramMaxRegions.get();
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
