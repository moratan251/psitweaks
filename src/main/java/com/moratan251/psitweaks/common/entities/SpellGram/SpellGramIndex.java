package com.moratan251.psitweaks.common.entities.SpellGram;

import com.moratan251.psitweaks.common.caster.CasterIds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * ディメンションごとの、ワールドに読み込まれている魔法式オブジェクトの台帳（サーバー側のみ）。
 * オブジェクトがワールドに入る・出るときに {@link SpellGramObject} が出し入れするので、全エンティティを調べずに
 * 場所や術者から引ける。読み込まれていないチャンクにあるものは載らない。サーバーが止まったら空にする。
 * <p>
 * 種類ごと・術者ごとの一覧は、出し入れがあったときだけ作り直して覚える（2026-11-13 の軽量化 A1）。範囲の判定は 1 tick に
 * 何百回も種類ごとの一覧を引くので、引くたびに全オブジェクトを回してリストを作らないようにした。返す一覧は変更できない
 * 写しで、回している途中でオブジェクトが消えても（discard しても）壊れない。
 */
public final class SpellGramIndex {
    private static final Map<ResourceKey<Level>, Bucket> BY_LEVEL = new HashMap<>();
    /** 出し入れや、判定に効く設定（範囲・置いた人・強さ）が変わるたびに増える番号。領域が tick ごとの覚え書きを作り直す目印。 */
    private static long version;

    private SpellGramIndex() {
    }

    /** 1 つのディメンションの台帳と、そこから作った一覧。 */
    private static final class Bucket {
        final Set<SpellGramObject> objects = new LinkedHashSet<>();
        final Map<Class<?>, List<?>> byType = new HashMap<>();
        final Map<UUID, List<SpellGramObject>> byCaster = new HashMap<>();

        void changed() {
            byType.clear();
            byCaster.clear();
        }
    }

    static void add(SpellGramObject object) {
        Bucket bucket = BY_LEVEL.computeIfAbsent(object.level().dimension(), ignored -> new Bucket());
        if (bucket.objects.add(object)) {
            bucket.changed();
        }
        version++;
    }

    static void remove(SpellGramObject object) {
        ResourceKey<Level> dimension = object.level().dimension();
        Bucket bucket = BY_LEVEL.get(dimension);
        if (bucket != null && bucket.objects.remove(object)) {
            bucket.changed();
            if (bucket.objects.isEmpty()) {
                BY_LEVEL.remove(dimension);
            }
        }
        version++;
    }

    /**
     * 台帳に載っているオブジェクトの、判定に効く設定（範囲・置いた人・事象干渉力・強さ）が変わったことを知らせる。領域の tick ごとの
     * 覚え書き（領域干渉・打ち消し・同じ種類の強さ・乱流の一覧）を作り直させる。
     */
    public static void touch() {
        version++;
    }

    /** {@code object} の術者が変わった（術者ごとの一覧を作り直す）。台帳に載っていなければ番号を進めるだけ。 */
    static void changed(SpellGramObject object) {
        version++;
        if (object.level() == null) {
            return;
        }
        Bucket bucket = BY_LEVEL.get(object.level().dimension());
        if (bucket != null && bucket.objects.contains(object)) {
            bucket.changed();
        }
    }

    /** 出し入れや設定の変更のたびに増える番号（{@link #touch}）。 */
    public static long version() {
        return version;
    }

    /** {@code level} にあり、中心が {@code center} から {@code radius} 以内の魔法式オブジェクト。 */
    public static List<SpellGramObject> near(Level level, Vec3 center, double radius) {
        return near(level, center, radius, SpellGramObject.class);
    }

    /** {@link #near(Level, Vec3, double)} のうち {@code type} のもの。 */
    public static <T extends SpellGramObject> List<T> near(Level level, Vec3 center, double radius, Class<T> type) {
        List<T> candidates = all(level, type);
        if (candidates.isEmpty()) {
            return List.of();
        }
        double radiusSqr = radius * radius;
        List<T> found = new ArrayList<>();
        for (T object : candidates) {
            if (!object.isRemoved() && object.position().distanceToSqr(center) <= radiusSqr) {
                found.add(object);
            }
        }
        return found;
    }

    /** {@code level} にある {@code type} の魔法式オブジェクトすべて（消えたものは除く）。変更できない写し。 */
    @SuppressWarnings("unchecked")
    public static <T extends SpellGramObject> List<T> all(Level level, Class<T> type) {
        Bucket bucket = BY_LEVEL.get(level.dimension());
        if (bucket == null) {
            return List.of();
        }
        List<?> cached = bucket.byType.get(type);
        if (cached == null) {
            List<T> found = new ArrayList<>();
            for (SpellGramObject object : bucket.objects) {
                if (type.isInstance(object) && !object.isRemoved()) {
                    found.add(type.cast(object));
                }
            }
            cached = List.copyOf(found);
            bucket.byType.put(type, cached);
        }
        return (List<T>) cached;
    }

    /** {@code level} にある、術者（{@link CasterIds#idOf} の ID）の魔法式オブジェクト。変更できない写し。 */
    public static List<SpellGramObject> ofCaster(Level level, @Nullable UUID caster) {
        Bucket bucket = BY_LEVEL.get(level.dimension());
        if (bucket == null || caster == null) {
            return List.of();
        }
        List<SpellGramObject> cached = bucket.byCaster.get(caster);
        if (cached == null) {
            List<SpellGramObject> found = new ArrayList<>();
            for (SpellGramObject object : bucket.objects) {
                if (!object.isRemoved() && caster.equals(object.getCasterUuid())) {
                    found.add(object);
                }
            }
            cached = List.copyOf(found);
            bucket.byCaster.put(caster, cached);
        }
        return cached;
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        BY_LEVEL.clear();
        version++;
    }
}
