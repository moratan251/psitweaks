package com.moratan251.psitweaks.common.entities.SpellGram.region;

import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 術者ごとの、ワールドに読み込まれている領域型の一覧（サーバー側のみ）。
 * エンティティがワールドに入る・出るときに出し入れするので、全エンティティを調べずに術者の魔法式を引ける。
 * 読み込まれていないチャンクにあるものは数えない。
 */
public final class SpellGramRegionIndex {
    private static final Map<UUID, Set<EntitySpellGramRegion>> BY_CASTER = new HashMap<>();

    private SpellGramRegionIndex() {
    }

    static void add(EntitySpellGramRegion region) {
        UUID caster = region.getCasterUuid();
        if (caster != null) {
            BY_CASTER.computeIfAbsent(caster, ignored -> new LinkedHashSet<>()).add(region);
        }
    }

    static void remove(EntitySpellGramRegion region) {
        for (var iterator = BY_CASTER.values().iterator(); iterator.hasNext(); ) {
            Set<EntitySpellGramRegion> regions = iterator.next();
            regions.remove(region);
            if (regions.isEmpty()) {
                iterator.remove();
            }
        }
    }

    /** 術者の領域型を、古い順に返す。 */
    public static List<EntitySpellGramRegion> objectsOf(@Nullable UUID caster) {
        if (caster == null) {
            return List.of();
        }
        Set<EntitySpellGramRegion> regions = BY_CASTER.get(caster);
        if (regions == null) {
            return List.of();
        }
        List<EntitySpellGramRegion> sorted = new ArrayList<>();
        for (EntitySpellGramRegion region : regions) {
            if (!region.isRemoved()) {
                sorted.add(region);
            }
        }
        sorted.sort(Comparator.comparingLong(EntitySpellGramRegion::getCreatedAt));
        return sorted;
    }

    /** 術者の領域型が使っている枠の合計（{@link EntitySpellGramRegion#getCapacityUse}）。 */
    public static int totalUse(@Nullable UUID caster) {
        int total = 0;
        for (EntitySpellGramRegion region : objectsOf(caster)) {
            total += region.getCapacityUse();
        }
        return total;
    }

    public static Set<UUID> casters() {
        return BY_CASTER.keySet();
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        BY_CASTER.clear();
    }
}
